/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weisen.xcxf.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.weisen.xcxf.Constant;
import com.weisen.xcxf.bean.NoticeDao;

public class DbOpenHelper extends SQLiteOpenHelper {
	private static final String TAG = "DbOpenHelper";
	private static final int DATABASE_VERSION = 3;
	private static DbOpenHelper instance;
	private Context context;

	private static final String CREATE_TABLE_NOTICE = "CREATE TABLE "
			+ NoticeDao.TABLE_NAME + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ NoticeDao.NOTICE_ID + " TEXT , "
			+ NoticeDao.NOTICE_TITLE + " TEXT , "
			+ NoticeDao.NOTICE_CONTENT + " TEXT , "
			+ NoticeDao.NOTICE_URL + " TEXT , "
			+ NoticeDao.NOTICE_FLAG + " TEXT , "
			+ NoticeDao.NOTICE_IS_READ + " TEXT , "
			+ NoticeDao.NOTICE_TIME + " TEXT , "
			+ NoticeDao.NOTICE_USERINAME + " TEXT , "
			+ NoticeDao.NOTICE_USERIPHONE + " TEXT , "
			+ NoticeDao.NOTICE_USERDESCRIPTION + " TEXT , "
			+ NoticeDao.NOTICE_LATITUDE + " TEXT , "
			+ NoticeDao.NOTICE_LONGITUDE + " TEXT);";


	private static final String CREATE_TABLE_CASE = "CREATE TABLE case_report "
			+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT,uid TEXT, "
			+ "uType TEXT,uLoti TEXT,uLati TEXT,uAlti TEXT,uAddr TEXT,uTime TEXT,"
			+ "uSpeed TEXT,uDirection TEXT,uAccurary TEXT,uLocType TEXT,"
			+ "uContent TEXT,attach TEXT,q TEXT,uRemark TEXT,uUserId TEXT,uUploadTime TEXT,isSuccess TEXT,flag TEXT);";

	private static final String CREATE_TABLE_LOCATION = "CREATE TABLE my_location "
			+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT,uid TEXT, "
			+ "latitude TEXT,longitude TEXT,altitude TEXT,address TEXT,speed TEXT,bearing TEXT,accurary TEXT,locType TEXT,"
			+ "battery TEXT,uTime TEXT,uUploadTime TEXT,isSuccess TEXT,net TEXT);";
	
	private final static String CREATE_TABLE_REPORT = "CREATE TABLE my_reply "
			+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "uid TEXT,replytxt TEXT,replytime TEXT);";
	
	private final static String CREATE_TABLE_USERLENGTH = "CREATE TABLE my_length "
			+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "uid TEXT,myTime TEXT,myLength TEXT);";
	//在已有的表中添加一列
	private static final String ALERT_TABLE_LOCATION = "ALTER TABLE my_location add net TEXT;";

	public DbOpenHelper(Context context) {
		super(context, getName(context), null, DATABASE_VERSION);
	}

	public static String getName(Context context) {
		String userId = "";
		SharedPreferences preferences = context.getSharedPreferences(Constant.APP_SP, context.MODE_MULTI_PROCESS);
		userId = preferences.getString(Constant.SP_USERID, "");
		String name = "wanggehua" + userId + ".db";
		return name;
	}

	public Context getContext() {
		return context;
	}

	@SuppressLint("NewApi")
	public static DbOpenHelper getInstance(Context context) {
		if (instance == null) {
			instance = new DbOpenHelper(context.getApplicationContext());
		}
		if (instance != null) {
			if (instance.getDatabaseName().equals(getName(context)))
				return instance;
			else
				instance = new DbOpenHelper(context.getApplicationContext());
		}
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_NOTICE);
		db.execSQL(CREATE_TABLE_CASE);
		db.execSQL(CREATE_TABLE_LOCATION);
		db.execSQL(CREATE_TABLE_REPORT);
		db.execSQL(CREATE_TABLE_USERLENGTH);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (newVersion == 3) {
			Log.i(TAG, "onUpgrade: 执行");
			db.execSQL(ALERT_TABLE_LOCATION);
		}
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (newVersion == 3) {
			Log.i(TAG, "onDowngrade: ");
			db.execSQL(ALERT_TABLE_LOCATION);
		}

	}

	public void closeDB() {
		if (instance != null) {
			try {
				SQLiteDatabase db = instance.getWritableDatabase();
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			instance = null;
		}
	}
}
