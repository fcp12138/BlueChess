package com.fcp.bluechess.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper{
	
	/**
	 * 数据库版本
	 */
	private static final int VERSION = 1;
	/**
	 * 数据库名称
	 */
	private static final String DBName = "wuziqi.db";
	
	//数据库
	private static DBOpenHelper instance;
	
	//单例模式
	public static DBOpenHelper Instance(Context context) {  
        if (instance == null) {  
            instance = new DBOpenHelper(context);  
        }   
        return instance;  
    }  
	
	private DBOpenHelper(Context context, String name, CursorFactory factory,int version) {
		super(context, name, factory, version);
	}
	
	private DBOpenHelper(Context context){
		super(context,DBName,null,VERSION);
	}
	
	/**
	 * 数据库建立（仅运行一次）
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {    
	
		//商品表
		db.execSQL("create table IF NOT EXISTS Game_TBL "
				+ "(game_id integer primary key autoincrement,"										//app_num_time
				+ "game_result integer not null,"                         	    					//比赛的结果  0： 败  1：胜
				+ "game_time long not null,"                      									//比赛结束时间
				+ "game_person varchar(20) not null)"												//（改为描述）
				);

	}
	

	
	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		/*应该避免用户存放于数据库中的数据丢失,以下方法需改动*/
		//先删除
		db.execSQL("DROP TABLE IF EXISTS Game_TBL");
		//重建
        onCreate(db);
	}

}
