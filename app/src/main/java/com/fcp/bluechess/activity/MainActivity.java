package com.fcp.bluechess.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


import com.fcp.bluechess.R;
import com.fcp.bluechess.adapter.MyAdapter;
import com.fcp.bluechess.service.GameService;
import com.fcp.bluechess.view.XListView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 主界面
 * Created by fcp on 2015/9/7.
 */
public class MainActivity extends AppCompatActivity implements XListView.IXListViewListener{

    private XListView listView ;
    private MyAdapter myAdapter;
    /**
     * 数据
     */
    private ArrayList<HashMap<String, Object>> data;
    /**
     * 加载统计
     */
    private int index;
    /**
     * 是否在异步
     */
    private boolean isTask=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    /**
     * 初始化设备
     */
    private void initView() {
        //作为客户端点击
        findViewById(R.id.open_client).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开搜索界面
                startActivity(new Intent(MainActivity.this,FindActivity.class));
            }
        });
        //开启服务器
        findViewById(R.id.open_server).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开游戏界面
                Intent intent = new Intent(MainActivity.this,GameActivity.class);
                intent.putExtra("type", GameActivity.CREATE_SERVER);
                startActivity(intent);
            }
        });
        listView = (XListView) findViewById(R.id.xlistview_id);
        listView.setPullLoadEnable(false);
        listView.setPullRefreshEnable(true);
        listView.setXListViewListener(this);
        data=new ArrayList<>();
        myAdapter=new MyAdapter(this, data);
        listView.setAdapter(myAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        onRefresh();
    }

    @Override
    public void onRefresh() {
        if(!isTask){
            index=0;
            listView.setPullLoadEnable(true);
            new MyTask(false).execute();
        }
    }

    @Override
    public void onLoadMore() {
        if(!isTask){
            new MyTask(true).execute();
        }
    }


    /**
     * 获得历史线程
     */
    private class MyTask extends AsyncTask<Void, Void, ArrayList<HashMap<String, Object>>> {

        boolean isload;

        public MyTask(boolean isload) {
            this.isload = isload;
        }

        @Override
        protected ArrayList<HashMap<String, Object>> doInBackground(Void... params) {
            isTask=true;
            return new GameService(MainActivity.this).getData(index,20);
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, Object>> hashMaps) {
            super.onPostExecute(hashMaps);

            index+=hashMaps.size();
            if(isload){
                if(hashMaps.size()==0){
                    listView.setPullLoadEnable(false);//加载完
                }else{
                    //上拉加载
                    data.addAll(hashMaps);
                    listView.stopLoadMore();
                    myAdapter.notifyDataSetChanged();
                }
            }else{
                data.clear();
                data.addAll(hashMaps);
                listView.stopRefresh();
                myAdapter.notifyDataSetChanged();
            }
            isTask=false;
        }
    }

}
