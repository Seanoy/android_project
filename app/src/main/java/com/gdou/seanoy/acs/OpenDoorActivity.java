package com.gdou.seanoy.acs;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

public class OpenDoorActivity extends AppCompatActivity {

    Button confirm2Button;             //开门确认按钮
    EditText passwordSendEditText;     //密码输入框
    TextView text_hint2;               //提示信息
    String message2Send;
    SendThread sendThread;
    MyApplication application = new MyApplication();

    public static final int INPUTPASSWORD = 0x03;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_door);

        application = (MyApplication) getApplicationContext();

        text_hint2 = findViewById(R.id.hint2);
        passwordSendEditText = findViewById(R.id.password);
        confirm2Button = findViewById(R.id.confirm_button2);

        confirm2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(application.getConnectFlag()) {
                    message2Send = passwordSendEditText.getText().toString();
                    //使用连接成功后得到的socket构造发送线程,每点击一次send按钮触发一次发送线程
                    sendThread = new  SendThread(application.getSocket());
                    sendThread.start();
                }
                else{//未连接的提示信息
                    SendMessagetoHandler(OYActivity.DEBUG,"未连接服务器！");
                }
            }
        });


    }

    /*****************Handler初始化*****************/
    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
                if (msg.what == INPUTPASSWORD){
                    Bundle bundle = msg.getData();
                    text_hint2.append("Password:"+bundle.getString("string1")+"\n");
                }
                else if (msg.what == OYActivity.DEBUG) {
                    Bundle bundle = msg.getData();
                    text_hint2.append("Debug:"+bundle.getString("string1")+"\n");
                }
                else if (msg.what == OYActivity.RECEIVEDATAFROMSERVER) {
                    Bundle bundle = msg.getData();
                    text_hint2.append("Server:"+bundle.getString("string1")+"\n");
                }
        }

    };



    /*****************子线程更新UI*****************/
    public void SendMessagetoHandler(final int messageType , String string1toHandler){
        Message msg = new Message();
        msg.what = messageType;    //消息类型
        Bundle bundle = new Bundle();
        bundle.clear();
        bundle.putString("string1", string1toHandler); //向bundle中添加字符串
        msg.setData(bundle);
        myHandler.sendMessage(msg);
    }


    /*****************发送线程*****************/
    class SendThread extends Thread{
        private Socket mSocket;
        //发送线程的构造函数，由连接线程传入套接字
        private SendThread(Socket socket) {mSocket = socket;}

        @Override
        public void run() {
            try{
                if(!message2Send.equals("")){
                OutputStream outputStream = mSocket.getOutputStream();
                //向服务器发送信息
                outputStream.write(("PSW"+message2Send).getBytes("gbk"));
                outputStream.flush();
                //更新UI:显示发送出的数据
                SendMessagetoHandler(INPUTPASSWORD,message2Send);
                Looper.prepare();
                Toast.makeText(getApplicationContext(),"发送成功！",Toast.LENGTH_SHORT).show();
                Looper.loop();}
                else {
                    SendMessagetoHandler(OYActivity.DEBUG,"请输入开门密码");
                }
            }catch (IOException e) {
                e.printStackTrace();
                //更新UI：显示发送错误信息
                SendMessagetoHandler(OYActivity.DEBUG,"发送失败！");
            }
        }
    }



    /*****************菜单栏*****************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.setting){
            return true;
        }
        if(id == R.id.exit){
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            System.exit(0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }






}
