package com.fcp.bluechess.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * 蓝牙操作基类
 * Created by fcp on 2015/9/7.
 */
public abstract class BlueLinkWorker implements BlueOperator{

    protected final String TAG = this.getClass().getSimpleName();
    /**
     * uuid for SDP record or service record uuid to lookup RFCOMM channel
     */
    public static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * 适配器
     */
    protected BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    /**
     * 内容
     */
    protected Context context;

    /**
     * 是否连接
     */
    protected boolean isConnection = false;

    /**
     * 端口
     */
    protected BluetoothSocket socket ;

    /**
     * 发送通道
     */
    protected OutputStream outputStream = null;

    /**
     * 接收通道
     */
    protected InputStream inputStream = null;

    /**
     * 服务端线程处理类
     */
    protected CreateHandler createHandler = null;

    /**
     * 生成监听器
     */
    protected OnCreateListener onCreateListener;


    public BlueLinkWorker(Context context) {
        this.context = context;
        createHandler =new CreateHandler(this);
    }

    /**
     * 打开蓝牙
     * @param activity Activity
     */
    public void openBlueTooth(Activity activity){
        if(!bluetoothAdapter.isEnabled()){//未打开
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
            activity.startActivityForResult(enableBtIntent, BlueToothFindHelper.REQUEST_ENABLE_BT);
        }
    }

    //==============================================================================================生成
    /**
     * 生成
     */
    public abstract void create( OnCreateListener onCreateListener );

    /**
     * 处理线程返回值
     */
    static class CreateHandler extends Handler {
        WeakReference<BlueLinkWorker> weakReference;
        public CreateHandler(BlueLinkWorker blueLinkWorker) {
            weakReference=new WeakReference<>(blueLinkWorker);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BlueLinkWorker blueLinkWorker=weakReference.get();
            if(blueLinkWorker!=null && blueLinkWorker.onCreateListener!=null){
                if(msg.what==0){
                    blueLinkWorker.createSuccess();
                }else{
                    blueLinkWorker.createFail();
                }
            }
        }
    }

    protected abstract void createSuccess();//创建成功

    protected abstract void createFail();//创建失败


    //==============================================================================================接收


    /**
     * 接收数据
     */
    public void receive(){
        myHandler=new MyHandler();
        new ReceicerThread().start();
    }


    private MyHandler myHandler;


    // 回调的接口
    private OnReceiveDataListener onReceiveDataListener;//接受数据

    /**
     * 接受发送到的数据
     */
    class MyHandler extends Handler {

        public MyHandler() {}
        public MyHandler(Looper L) {super(L);}

        // 子类必须重写此方法,接受数据
        @Override
        public void handleMessage(Message msg) {
            Log.d("MyHandler", "handleMessage......");
            super.handleMessage(msg);
            byte[] buffer ;
            buffer = (byte[])msg.obj;

            String str = new String(buffer, 0, msg.arg1);
            if(onReceiveDataListener!=null)onReceiveDataListener.onReceiverSuccess(str);
        }
    }


    class ReceicerThread extends Thread{
        byte[] buffer = new byte[1024]; // buffer store for the stream
        int bytes; // bytes returned from read()
        @Override
        public void run() {
            Looper.prepare();
            // Keep listening to the InputStream until an exception occurs
            while (isConnection) {
                try {
                    // Read from the InputStream
                    bytes = inputStream.read(buffer);
                    Message msg = myHandler.obtainMessage();
                    msg.obj = buffer;
                    msg.arg1 = bytes;
                    myHandler.sendMessage(msg);
                    // Send the obtained bytes to the UI Activity
                    //transHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
            Looper.loop();
        }
    }

    public void setOnReceiveDataListener(OnReceiveDataListener onReceiveDataListener) {
        this.onReceiveDataListener = onReceiveDataListener;
    }


    //==============================================================================================发送


    /**
     * 发送数据
     */
    public void send(String sendData){
        if (this.isConnection) {
            try {
                byte[] data = sendData.getBytes("UTF-8");
                outputStream.write(data, 0, data.length);
                outputStream.flush();
            } catch (IOException e) {
                Log.d(TAG,"发送失败！");
            }
        } else {
            Toast.makeText(this.context, "设备未连接，请重新连接！", Toast.LENGTH_SHORT).show();
        }
    }



    //==============================================================================================释放

    /**
     * 关闭资源
     */
    public void close(){
        isConnection=false;
        try {
            if(inputStream!=null) {
                inputStream.close();
            }
            if(outputStream!=null){
                outputStream.close();
            }
            if(socket!=null){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
