package com.fcp.bluechess.bluetooth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;


import com.fcp.bluechess.R;

import java.util.ArrayList;

/**
 * 蓝牙帮助类
 * Created by fcp on 2015/8/8.
 */
public class BlueToothFindHelper {
    /**
     * 系统蓝牙控制面板开启系数
     */
    public static int REQUEST_ENABLE_BT=0x100;
    /**
     * 蓝牙适配器
     */
    private BluetoothAdapter bluetoothAdapter;
    /**
     * 相关联的activity
     */
    private Activity activity;

    private ArrayList<BluetoothDevice> unbondDevices = null; // 用于存放未配对蓝牙设备
    private ArrayList<BluetoothDevice> bondDevices = null;// 用于存放已配对蓝牙设备

    private OnAddBlueDevicesListener onAddBlueDevicesListener;//回调的接口


    public BlueToothFindHelper(Activity activity) {
        this.activity=activity;
        this.unbondDevices = new ArrayList<>();
        this.bondDevices = new ArrayList<>();
        try {
            onAddBlueDevicesListener= (OnAddBlueDevicesListener) activity;
        }catch (ClassCastException e){
            Log.d("fcp","activity is not implements OnAddBlueDevicesListener");
        }
        //获取默认蓝牙适配器
        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        //注册蓝牙监听
        setBlueToothReceiver();
        //核实打开蓝牙
        checkIsOpen();
    }

    //====================================================================蓝牙搜索==================================================================

    /**
     * 核实打开蓝牙
     * @return boolean
     */
    public boolean checkIsOpen(){
        if(bluetoothAdapter!=null){//不为空标识支持蓝牙
            if(!bluetoothAdapter.isEnabled()){
                //bluetoothAdapter.enable();//开启蓝牙
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }else{
            return false;
        }
        return true;
    }

     /**
     * 获得当前绑定的设备信息
     * @param arrayList ArrayList
     */
    public void getLocalBondedDevices(ArrayList<BluetoothDevice> arrayList){
        arrayList.clear();
        arrayList.addAll(bluetoothAdapter.getBondedDevices());
    }

    /**
     * 搜索蓝牙设备
     */
    public void searchDevices() {
        if(bluetoothAdapter!=null){
            this.bondDevices.clear();
            bondDevices.addAll(bluetoothAdapter.getBondedDevices());
            this.unbondDevices.clear();
            // 寻找蓝牙设备，android会将查找到的设备以广播形式发出去
            this.bluetoothAdapter.startDiscovery();
        }else{
            Toast.makeText(activity, R.string.no_supper_devices,Toast.LENGTH_SHORT).show();
        }
    }

    //=================================================================广播获得设备=============================================================

    /**
     * 添加未绑定蓝牙设备到list集合
     *
     * @param device BluetoothDevice
     */
    private void addUnbondDevices(BluetoothDevice device) {
        if (!this.unbondDevices.contains(device)) {
            this.unbondDevices.add(device);
        }
    }

    /**
     * 添加已绑定蓝牙设备到list集合
     *
     * @param device BluetoothDevice
     */
    private void addBandDevices(BluetoothDevice device) {
        if (!this.bondDevices.contains(device)) {
            this.bondDevices.add(device);
        }
    }

    private BroadcastReceiver broadcastReceiver;//广播接收器

    /**
     * 注册监听广播
     */
    private void setBlueToothReceiver() {
        // 设置广播信息过滤
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        // 注册广播接收器，接收并处理搜索结果
        broadcastReceiver=new BlueToothReceiver();
        activity.registerReceiver(broadcastReceiver,intentFilter);
    }

    /**
     * 广播注销
     */
    public void unregisterBlueReceiver(){
        if(broadcastReceiver!=null) activity.unregisterReceiver(broadcastReceiver);
    }

    /**
     * 蓝牙监听接受器
     */
    class BlueToothReceiver extends BroadcastReceiver{
        ProgressDialog progressDialog = null;
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)){
                Log.d("fcp","ACTION_FOUND");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    addBandDevices(device);
                } else {
                    addUnbondDevices(device);
                }
            }else if(intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
                Log.d("fcp","ACTION_DISCOVERY_STARTED");
                progressDialog = ProgressDialog.show(context, "请稍等...", "搜索蓝牙设备中...", true);
            }else if(intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                Log.d("fcp", "ACTION_DISCOVERY_FINISHED");//设备搜索完毕
                if(onAddBlueDevicesListener!=null){
                    onAddBlueDevicesListener.bondDevices(bondDevices);
                    onAddBlueDevicesListener.unbondDevices(unbondDevices);
                }
                progressDialog.dismiss();
            }else if(intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                Log.d("fcp","ACTION_STATE_CHANGED");//蓝牙状态监听

            }
        }
    }

    /**
     * 遍历Devices获得数据
     * @param arrayList ArrayList
     * @return ArrayList
     */
    /*private ArrayList<HashMap<String,Object>> getName_AddressFromArr(ArrayList<BluetoothDevice> arrayList){
        ArrayList<HashMap<String,Object>> mapArrayList=new ArrayList<>();
        for(BluetoothDevice bluetoothDevice:arrayList){
            HashMap<String ,Object> hashMap=new HashMap<>();
            hashMap.put("name",bluetoothDevice.getName());
            hashMap.put("address",bluetoothDevice.getAddress());
            mapArrayList.add(hashMap);
        }
        return mapArrayList;
    }*/

    /**
     * 蓝牙搜索添加回调接口
     */
    public interface OnAddBlueDevicesListener {
        //未匹配设备的变动
        void unbondDevices(ArrayList<BluetoothDevice> arrayList);
        //匹配数量的变动
        void bondDevices(ArrayList<BluetoothDevice> arrayList);
    }

}
