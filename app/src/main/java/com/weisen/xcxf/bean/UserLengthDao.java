package com.weisen.xcxf.bean;

import java.util.ArrayList;
import java.util.List;

import com.weisen.xcxf.db.DbOpenHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UserLengthDao {
	private DbOpenHelper helper = null;
	public static final String TABLE_NAME = "my_length";
	
	public UserLengthDao(Context context) {
		helper = DbOpenHelper.getInstance(context);
	}
	
	public int addLength(UserLength length) {
		
		SQLiteDatabase database = null;
		int id = -1;
		try {
			database = helper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("uid", length.getUid());
			values.put("myTime", length.getMyTime());
			values.put("myLength", length.getMyLength());

			id = (int) database.insert(TABLE_NAME, null, values);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return id;
	}
	
	public void updateLength(String length,String time) {
		String sql = "update " + TABLE_NAME + " set myLength='" + length+"' where myTime= '" + time+"' ";
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL(sql);
	}
	
	public  List<UserLength> findAll(String uid){
		SQLiteDatabase database=null;
		List<UserLength> list=new ArrayList<UserLength>();
		Cursor cursor=null;
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("select * from " + TABLE_NAME
					+ " where  uid = '" + uid + "' order by myTime desc", null);
			while (cursor.moveToNext()) {
				UserLength length = new UserLength();
				length.setUid(cursor.getString(cursor.getColumnIndex("uid")));
				length.setMyTime(cursor.getString(cursor.getColumnIndex("myTime")));
				length.setMyLength(cursor.getString(cursor.getColumnIndex("myLength")));
				list.add(length);
			}
		} catch (Exception e) {
			Log.i("测试","数据库查询失败");
		}finally {
			if (database != null) {
				database.close();
			}
		}
		return list;
	}
	public  String findTime(String uid,String time){
		SQLiteDatabase database=null;
		Cursor cursor=null;
		String myTime="";
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("select * from " + TABLE_NAME
					+ " where  myTime='"+ time + "' and uid = '" + uid + "' ", null);
			while (cursor.moveToNext()) {
				myTime=cursor.getString(cursor.getColumnIndex("myTime"));
			}
		} catch (Exception e) {
			Log.i("测试","数据库查询失败");
		}finally {
			if (database != null) {
				database.close();
			}
		}
		return myTime;
	}

	public  void  deletealllength(){
        String sql="delete from "+TABLE_NAME;
		SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(sql);
		db.close();

	}
	public  List<MyLocation> findAllsbytime(String uid,String time){
		//String time = CommonTool.getStringDate(new Date(),"yyyy-MM-dd");
		SQLiteDatabase database=null;
		List<MyLocation> list=new ArrayList<MyLocation>();
		Cursor cursor=null;
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("select * from " + TABLE_NAME
					+ " where myTime='"+ time +"'and uid = '" + uid + "' order by uTime asc",null);
			while (cursor.moveToNext()) {
				MyLocation location = new MyLocation();
				location.setId(cursor.getString(cursor.getColumnIndex("_id")));
				location.setUid(cursor.getString(cursor.getColumnIndex("uid")));
				location.setLatitude(cursor.getString(cursor.getColumnIndex("latitude")));
				location.setLongitude(cursor.getString(cursor.getColumnIndex("longitude")));
				location.setAltitude(cursor.getString(cursor.getColumnIndex("altitude")));
				location.setAddress(cursor.getString(cursor.getColumnIndex("address")));
				location.setSpeed(cursor.getString(cursor.getColumnIndex("speed")));
				location.setBearing(cursor.getString(cursor.getColumnIndex("bearing")));
				location.setAccurary(cursor.getString(cursor.getColumnIndex("accurary")));
				location.setBattery(cursor.getString(cursor.getColumnIndex("battery")));
				location.setLocType(cursor.getString(cursor.getColumnIndex("locType")));
				location.setTime(cursor.getString(cursor.getColumnIndex("myTime")));
				list.add(location);
			}
		} catch (Exception e) {
			Log.i("测试","数据库查询失败");
		}finally {
			if (database != null) {
				database.close();
			}
		}
		return list;
	}

}
