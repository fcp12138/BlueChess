package com.fcp.bluechess.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.fcp.bluechess.R;
import com.fcp.bluechess.utils.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 适配器
 * Created by fcp on 2015/9/9.
 */
public class MyAdapter extends BaseAdapter {

    /**
     * 数据集
     */
    private ArrayList<HashMap<String, Object>> mData;
    private LayoutInflater mInflater;

    public MyAdapter(Context context,ArrayList<HashMap<String, Object>> mData) {
        this.mData=mData;
        this.mInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int arg0) {
        return mData.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder ;
        //获得view
        if(convertView==null){
            holder=new ViewHolder();
            convertView = mInflater.inflate(R.layout.list_item, parent,false);
            holder.timeTextView=(TextView)convertView.findViewById(R.id.time_text);
            holder.personTextView=(TextView)convertView.findViewById(R.id.person_text);
            holder.resultTextView=(TextView)convertView.findViewById(R.id.result_text);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        holder.personTextView.setText(mData.get(position).get("game_person").toString());
        if(mData.get(position).get("game_result")==0){
            holder.resultTextView.setText("败");
        }else{
            holder.resultTextView.setText("胜");
        }
        holder.timeTextView.setText(DateUtils.longToTimeFormat((long) mData.get(position).get("game_time")));
        return convertView;
    }


    private class ViewHolder{

        TextView timeTextView;
        TextView personTextView;
        TextView resultTextView;

    }
}
