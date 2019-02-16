package com.gdou.seanoy.acs;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;


public class OYActivity extends AppCompatActivity implements View.OnClickListener{

        /*****************各参数声明*****************/
        //Handler中的消息类型
        public static final int DEBUG = 0x00;
        public static final int RECEIVEDATAFROMSERVER = 0x01;
        public static final int SENDDATATOSERVER = 0x02;


        //存放全局变量和方法
        public static MyApplication application = new MyApplication();

        //线程
        Socket socket = null;               //成功建立一次连接后获得的套接字
        ConnectThread connectThread;        //当run方法执行完后，线程就会退出，故不需要主动关闭
        SendThread sendThread;              //发送线程,由send按键触发
        ReceiveThread receiveThread;        //接收线程，连接成功后一直运行

        //待发送的消息
        String messagetoSend = "";

        //控件
        static TextView displayTextView;          //显示接收、发送的数据及Debug信息

        Button sendButton;                 //发送按钮，点击触发发送线程
        Button connectButton;              //连接按钮，点击触发连接线程
        Button clearButton;                //清除按钮
        Button optionsButton;              //进入选择界面按钮（前提是连接成功）
        EditText messagetoSendEditText;    //发送数据输入框
        EditText iPEditText;               //ip地址输入框
        EditText portEditText;             //端口输入框




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


        /*****************Handler初始化*****************/
        static public Handler myHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == RECEIVEDATAFROMSERVER) {
                    Bundle bundle = msg.getData();
                    displayTextView.append("Server:"+bundle.getString("string1")+"\n");
                }
                else if (msg.what == DEBUG) {
                    Bundle bundle = msg.getData();
                    displayTextView.append("Debug:"+bundle.getString("string1")+"\n");
                }
                else if (msg.what == SENDDATATOSERVER) {
                    Bundle bundle = msg.getData();
                    displayTextView.append("Client:"+bundle.getString("string1")+"\n");
                }
            }

        };



        /*****************子线程更新UI*****************/
        public static void SendMessagetoHandler(final int messageType , String string1toHandler){
            Message msg = new Message();
            msg.what = messageType;    //消息类型
            Bundle bundle = new Bundle();
            bundle.clear();
            bundle.putString("string1", string1toHandler); //向bundle中添加字符串
            msg.setData(bundle);
            myHandler.sendMessage(msg);
        }

    /*****************程序初始化Activity实例*****************/
    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_oy);

            //TextView部分
            displayTextView = findViewById(R.id.DisplayTextView);

            //Application部分
            application = (MyApplication) getApplicationContext();

            //EditText部分
            messagetoSendEditText =  findViewById(R.id.MessagetoSendEditText);
            iPEditText = findViewById(R.id.IPEditText);
            portEditText = findViewById(R.id.PortEditText);

            //Button部分
            connectButton = findViewById(R.id.ConnectButton);
            sendButton =  findViewById(R.id.SendButton);
            clearButton = findViewById(R.id.ClearButton);
            optionsButton = findViewById(R.id.Options_Button);

            //监听按钮
            connectButton.setOnClickListener(this);
            sendButton.setOnClickListener(this);
            clearButton.setOnClickListener(this);
            optionsButton.setOnClickListener(this);

        }


        /*****************控制按钮动作实现的功能*****************/
        @Override
        public void onClick(View v){
            if  (v.getId() == R.id.ConnectButton){
                connectThread= new ConnectThread();
                connectThread.start();
            }

            else if (v.getId() == R.id.SendButton){
                if(application.getConnectFlag()) {
                    messagetoSend =messagetoSendEditText.getText().toString();
                    //使用连接成功后得到的socket构造发送线程,每点击一次send按钮触发一次发送线程
                    sendThread = new SendThread(application.getSocket());
                    sendThread.start();
                }
                else{//未连接的提示信息
                    SendMessagetoHandler(DEBUG,"未连接服务器！");
                }
            }

            else if (v.getId() == R.id.ClearButton){
                displayTextView.setText("");
            }

            else if (v.getId() == R.id.Options_Button){
                if(application.getConnectFlag()){
                Intent intent = new Intent(this, OptionsActivity.class);
                startActivity(intent);
                }
                else{//未连接的提示信息
                    SendMessagetoHandler(DEBUG,"未连接服务器！");
                }
            }

        }



        /*****************连接线程*****************/
        class ConnectThread extends Thread{

            @Override
            public void run() {
                try{
                    if(!application.getConnectFlag())//限制只能接入一次，防止多次接入
                    {
                    //连接服务器 并设置连接超时为1秒
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(iPEditText.getText().toString(),
                            Integer.parseInt(portEditText.getText().toString())), 1000);
                    application.setSocket(socket);
                    //更新UI:连接成功
                    SendMessagetoHandler(DEBUG,"服务器连接成功！");
                    //打开接收线程
                    receiveThread = new ReceiveThread(socket);
                    receiveThread.start();
                    }
                    else
                    {
                        SendMessagetoHandler(DEBUG,"你已接入TCP网络！");
                    }
                }catch (SocketTimeoutException aa) {
                    //连接失败
                    application.setConnectFlag(false);
                    //更新UI:连接失败
                    SendMessagetoHandler(DEBUG,"服务器连接失败！");
                    return;     //直接返回
                } catch (IOException e) {
                    e.printStackTrace();

                }
                //连接成功
                application.setConnectFlag(true);

            }
        }



        /*****************发送线程*****************/
        class SendThread extends Thread{
            private Socket mSocket;
            //发送线程的构造函数，由连接线程传入套接字
            private SendThread(Socket socket) {mSocket = socket;}

            @Override
            public void run() {
                try{
                    if(!messagetoSend.equals("")) {
                        OutputStream outputStream = mSocket.getOutputStream();
                        //向服务器发送信息
                        outputStream.write(messagetoSend.getBytes("gbk"));
                        outputStream.flush();
                        //更新UI:显示发送出的数据
                        SendMessagetoHandler(SENDDATATOSERVER, messagetoSend);
                    }
                    else {
                        SendMessagetoHandler(DEBUG, "Send Something");
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                    //更新UI：显示发送错误信息
                    SendMessagetoHandler(DEBUG,"发送失败！");
                }
            }
        }



        /*****************接收线程*****************/
         class ReceiveThread extends Thread{

            public Socket mSocket;
            byte[] b=new byte[1024];
            int length ;
            Charset charset = Charset.forName("GBK");//中文字符集，用于显示中文
            //接收线程的构造函数，由连接线程传入套接字
            public ReceiveThread(Socket socket){mSocket = socket;}

            @Override
            public void run() {
                try {
                    while((length = mSocket.getInputStream().read(b)) != -1)
                        {  //在流产生时执行里面的代码
                            String r_msg = new String(b,0,length,charset);
                            SendMessagetoHandler(RECEIVEDATAFROMSERVER,r_msg);
                        }
                }catch (IOException e){
                    e.printStackTrace();
                    //更新UI：显示发送错误信息
                    SendMessagetoHandler(DEBUG,"接收失败！");
                }
            }
        }








}

