package com.fcp.bluechess.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Looper;

import java.io.IOException;

/**
 * 蓝牙连接客户端
 * Created by fcp on 2015/9/7.
 */
public class BlueToothClient extends BlueLinkWorker {
    //进度条
    private ProgressDialog progressDialog;
    //远程蓝牙地址
    private String address;

    public BlueToothClient(Context context, String address) {
        super(context);
        this.address = address;
    }

    @Override
    public void create(OnCreateListener onCreateListener) {
        this.onCreateListener=onCreateListener;
        progressDialog=ProgressDialog.show(context,"请稍等","客户端连接中...",true);
        if(!isConnection) {
            if(this.bluetoothAdapter.isDiscovering()){//断开搜索
                this.bluetoothAdapter.cancelDiscovery();
            }
            new Thread(new ConnectRunnable()).start();
        }
    }


    /**
     * 连接蓝牙设备
     */
    private boolean connect() {
        try {
            //远程蓝牙设备
            BluetoothDevice bluetoothDevice=bluetoothAdapter.getRemoteDevice(address);
            socket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
        } catch (Exception e) {
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return false;
        }
        return true;
    }


    /**
     * 连接异步
     */
    class ConnectRunnable implements Runnable{
        @Override
        public void run() {
            Looper.prepare();
            if(connect()){
                createHandler.sendEmptyMessage(0);
            }else{
                createHandler.sendEmptyMessage(1);
            }
            Looper.loop();
        }
    }

    @Override
    protected void createSuccess() {
        try {
            inputStream=socket.getInputStream();
            outputStream = socket.getOutputStream();
            isConnection=true;
            if(onCreateListener!=null)onCreateListener.onCreateSuccess();
            //开启接受线程
            receive();
        } catch (IOException e) {
            e.printStackTrace();
            if(onCreateListener!=null)onCreateListener.onCreateFail("连接失败");
        } finally {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void createFail() {
        progressDialog.dismiss();
        if(onCreateListener!=null)onCreateListener.onCreateFail("连接请求失败");
    }
}
