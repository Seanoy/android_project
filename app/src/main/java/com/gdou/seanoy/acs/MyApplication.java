package com.gdou.seanoy.acs;

import android.app.Application;

import java.net.Socket;

/**
 * 自定义的MyApplication继承Application 用于放置全局变量
 *
 * @author way
 *
 */
public class MyApplication extends Application {
    /**
     * 引发异常：在一些不规范的代码中经常看到Activity或者是Service当中定义许多静态成员属性。这样做可能会造成许多莫名其妙的 null
     * pointer异常。
     */

    /**
     * 异常分析：Java虚拟机的垃圾回收机制会主动回收没有被引用的对象或属性。在内存不足时，虚拟机会主动回收处于后台的Activity或
     * Service所占用的内存。当应用再次去调用静态属性或对象的时候，就会造成null pointer异常
     */

    /**
     * 解决异常：Application在整个应用中，只要进程存在，Application的静态成员变量就不会被回收，不会造成null pointer异常
     */

    //TCP socket
    private Socket GlobalSocket;
    //标志位
    boolean connect_done = false;


    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
    }

    //socket部分
    public Socket getSocket() {
        return GlobalSocket;
    }
    public void setSocket(Socket socket) {
        if(socket != null)
        this.GlobalSocket = socket;
    }

    //flag部分
    public boolean getConnectFlag(){
        return connect_done;
    }
    public void setConnectFlag(boolean flag){
        this.connect_done = flag;
    }



}

