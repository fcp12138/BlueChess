package com.fcp.bluechess.service;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.fcp.bluechess.sqlite.DBOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 数据库服务(结果记录表)
 * Created by fcp on 2015/9/9.
 */
public class GameService {

    private SQLiteDatabase db;
    private DBOpenHelper helper;

    public GameService(Context context) {
        helper = DBOpenHelper.Instance(context);

    }

    /**
     * 插入
     * @param game_result 比赛结果
     * @param game_time 比赛结束时间
     * @param game_person 比赛对手
     */
    public void insertData(int game_result,long game_time,String game_person){
        // 获取可读写数据库
        db = helper.getWritableDatabase();

        db.execSQL("insert into Game_TBL "
                + " (game_result ,game_time,game_person)"
                + " values (?,?,?)"
                , new Object[]{game_result,game_time,game_person});


        helper.close();
    }


    /**
     * 获得历史记录
     * @return ArrayList
     */
    public ArrayList<HashMap<String, Object>> getData(int index ,int num ){

        // 获取可读写数据库
        db = helper.getWritableDatabase();

        Cursor cursor=db.rawQuery("select game_result,game_time,game_person from Game_TBL order by game_time desc limit ?,?"
                , new String[]{""+index, ""+(index+num)});
        //解析
        ArrayList<HashMap<String, Object>> mData= new ArrayList<>();

        while(cursor.moveToNext()){
            HashMap<String, Object> map=new HashMap<>();
            map.put("game_result", cursor.getInt(0));
            map.put("game_time", cursor.getLong(1));
            map.put("game_person", cursor.getString(2));
            mData.add(map);
        }
        cursor.close();
        helper.close();
        return mData;
    }

}
