package com.fcp.bluechess.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothServerSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;

/**
 * 蓝牙连接服务端
 * Created by fcp on 2015/9/7.
 */
public class BlueToothServer extends BlueLinkWorker {

    private ProgressDialog progressDialog;


    public BlueToothServer(Context context) {
        super(context);
    }


    @Override
    public void create(OnCreateListener onCreateListener) {
        if(isConnection){
            Log.d(TAG,"BlueToothServer has created");//已经连接
        }else{
            if(this.bluetoothAdapter.isDiscovering()){//断开搜索
                this.bluetoothAdapter.cancelDiscovery();
            }
            progressDialog=ProgressDialog.show(context,"请稍等","服务端等待连接中...",true);
            this.onCreateListener=onCreateListener;
            new ServerThread().start();//开启线程
        }
    }



    /**
     * 生成服务器线程
     */
    class ServerThread extends Thread{
        @Override
        public void run() {
            BluetoothServerSocket serverSocket = null;
            try {
                // 要建立一个ServerSocket对象，需要使用adapter.listenUsingRfcommWithServiceRecord方法
                serverSocket=bluetoothAdapter.listenUsingRfcommWithServiceRecord("myServerSocket", uuid);
                socket = serverSocket.accept(10000);//超时限10s
                createHandler.sendEmptyMessage(0);
            } catch (IOException e) {
                e.printStackTrace();
                createHandler.sendEmptyMessage(1);
            } finally {
                if(serverSocket!=null){
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void createSuccess() {
        try {
            inputStream=socket.getInputStream();
            outputStream = socket.getOutputStream();
            isConnection=true;
            if(onCreateListener!=null)onCreateListener.onCreateSuccess();
            //开启接受线程
            receive();
        } catch (IOException e) {
            if(onCreateListener!=null)onCreateListener.onCreateFail("开启服务器失败");
            close();
            e.printStackTrace();
        } finally {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void createFail() {
        if(onCreateListener!=null)onCreateListener.onCreateFail("建立服务器失败");
        progressDialog.dismiss();
    }


}
