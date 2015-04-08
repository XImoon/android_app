package com.ximoon.weichat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	//单例模式！！！解决数据库死锁问题
	static String DATABASE_NAME = "bank.db";
	static int VERSION = 1;
	//1.创建数据库
	private DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		System.out.println("DBHelper.DBHelper()");
	}
	private static DBHelper dbHelper;
	//对外提供一个方法来实例化这个类
	public static  DBHelper getDBHelperInstance (Context context){
		if(dbHelper == null){
			dbHelper = new DBHelper(context, DATABASE_NAME, null, VERSION);
		}
		return dbHelper;
	}
	//2.创建表  视图  存储过程 触发器
	@Override
	public void onCreate(SQLiteDatabase db) {
		//System.out.println("DBHelper.onCreate()");
		String sql = "create table account(_id Integer primary key autoincrement,card_id text,name text,balance real check(balance>=0))";
		db.execSQL(sql);
		
		
	}
	//3.数据库升级用  之前少写了一列，添加一列，升级必须改变版本号
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		System.out.println("DBHelper.onUpgrade()");
	}

}
