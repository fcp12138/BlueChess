package com.fcp.bluechess.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.fcp.bluechess.R;
import com.fcp.bluechess.bluetooth.BlueToothFindHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * 查询蓝牙界面
 * Created by fcp on 2015/9/7.
 */
public class FindActivity extends Activity implements BlueToothFindHelper.OnAddBlueDevicesListener{
    /**
     * 蓝牙帮助类
     */
    private BlueToothFindHelper blueToothFindHelper;

    private ArrayList<BluetoothDevice> bondedDevicesArrayList;//绑定的设备数据
    private ArrayList<BluetoothDevice> unbondDevicesArrayList;//未绑定设备的数据
    private BlueDeviceAdapter unbondAdapter;//未绑定设备的适配器
    private BlueDeviceAdapter bondedAdapter;//绑定设备的适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blueToothFindHelper =new BlueToothFindHelper(this);//初始化
        setContentView(R.layout.activity_find);
        initView();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        //搜索设备按钮
        findViewById(R.id.research_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blueToothFindHelper.searchDevices();//后台开启搜索功能（以广播形式发送，接口回调接受）
            }
        });
        //未绑定设备
        final ListView unbundListView= (ListView) findViewById(R.id.unbund_devices_list);
        unbondDevicesArrayList=new ArrayList<>();
        unbondAdapter=new BlueDeviceAdapter(this,unbondDevicesArrayList);
        unbundListView.setAdapter(unbondAdapter);
        unbundListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //进行匹配
                try {
                    Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                    if ((boolean) createBondMethod.invoke(unbondDevicesArrayList.get(position))) {
                        // 将绑定好的设备添加的已绑定list集合
                        bondedDevicesArrayList.add(unbondDevicesArrayList.get(position));
                        // 将绑定好的设备从未绑定list集合中移除
                        unbondDevicesArrayList.remove(position);
                        bondedAdapter.notifyDataSetChanged();
                        unbondAdapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //已匹配的设备
        ListView bondedListview= (ListView) findViewById(R.id.bunded_devices_list);
        bondedDevicesArrayList=new ArrayList<>();
        blueToothFindHelper.getLocalBondedDevices(bondedDevicesArrayList);//获得已绑定的设备数据
        bondedAdapter=new BlueDeviceAdapter(this,bondedDevicesArrayList);
        bondedListview.setAdapter(bondedAdapter);
        bondedListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(FindActivity.this,GameActivity.class);
                intent.putExtra("type", GameActivity.CREATE_CLIENT);
                intent.putExtra("address",bondedDevicesArrayList.get(position).getAddress());
                startActivity(intent);
            }
        });
    }

    /**
     * 当接收到未匹配设备变化
     * @param arrayList ArrayList
     */
    @Override
    public void unbondDevices(ArrayList<BluetoothDevice> arrayList) {
        unbondDevicesArrayList.clear();
        unbondDevicesArrayList.addAll(arrayList);
        unbondAdapter.notifyDataSetChanged();
    }
    /**
     * 当接收到已匹配设备变化
     * @param arrayList ArrayList
     */
    @Override
    public void bondDevices(ArrayList<BluetoothDevice> arrayList) {
        bondedDevicesArrayList.clear();
        bondedDevicesArrayList.addAll(arrayList);
        bondedAdapter.notifyDataSetChanged();
    }


    /**
     * listview适配器
     */
    class BlueDeviceAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private ArrayList<BluetoothDevice> mDate;

        public BlueDeviceAdapter(Context context,ArrayList<BluetoothDevice> mDate) {
            this.mInflater=LayoutInflater.from(context);
            this.mDate=mDate;
        }
        @Override
        public int getCount() {
            return mDate.size();
        }
        @Override
        public Object getItem(int position) {
            return mDate.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView==null){
                viewHolder=new ViewHolder();
                convertView=mInflater.inflate(R.layout.activity_find_bundlist_item, parent ,false);
                viewHolder.textView=(TextView)convertView.findViewById(R.id.device_name);
                convertView.setTag(viewHolder);
            }else{
                viewHolder=(ViewHolder)convertView.getTag();
            }
            viewHolder.textView.setText(mDate.get(position).getName());
            return convertView;
        }
        private class ViewHolder {
            TextView textView;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        blueToothFindHelper.unregisterBlueReceiver();//注销广播
    }
}

