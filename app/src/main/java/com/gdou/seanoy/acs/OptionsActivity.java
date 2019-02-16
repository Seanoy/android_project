package com.gdou.seanoy.acs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class OptionsActivity extends AppCompatActivity implements View.OnClickListener {

    Button openDoorButton, attendanceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        //Button部分
        openDoorButton = findViewById(R.id.open_door_page_button);
        attendanceButton = findViewById(R.id.attendance_page_button);

        //监听按钮
        openDoorButton.setOnClickListener(this);
        attendanceButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        if(v.getId() == R.id.open_door_page_button){
            Intent intent = new Intent(this, OpenDoorActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.attendance_page_button){
            Intent intent = new Intent(this, AttendanceActivity.class);
            startActivity(intent);
        }
    }


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
