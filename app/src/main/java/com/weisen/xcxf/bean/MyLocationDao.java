package com.weisen.xcxf.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.weisen.xcxf.db.DbOpenHelper;
import com.weisen.xcxf.tool.CommonTool;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;


public class MyLocationDao {

	private DbOpenHelper helper = null;
	public static final String TABLE_NAME = "my_location";

	public MyLocationDao(Context context) {
		helper = DbOpenHelper.getInstance(context);
	}

	public int addLocation(MyLocation location) {
		
		SQLiteDatabase database = null;
		int id = -1;
		try {
			database = helper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("uid", location.getUid());
			values.put("latitude", location.getLatitude());
			values.put("longitude", location.getLongitude());
			values.put("altitude", location.getAltitude());
			values.put("address", location.getAddress());
			values.put("speed", location.getSpeed());
			values.put("bearing", location.getBearing());
			values.put("accurary", location.getAccurary());
			values.put("battery", location.getBattery());
			values.put("locType", location.getLocType());
			values.put("uTime", location.getTime());
			values.put("net",location.getNet());
			values.put("isSuccess", "false");

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

	public void updateLocation(int id) {
		String time = CommonTool.getStringDate(new Date(),"yyyy-MM-dd HH:mm:ss");
		String sql = "update " + TABLE_NAME
				+ " set isSuccess='true',uUploadTime='" + time + "' where _id="
				+ id;
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL(sql);
	}

	public List<MyLocation> getUnUploadList(String uid) {
		String time = CommonTool.getStringDate(new Date(),"yyyy-MM-dd");
		List<MyLocation> list = new ArrayList<MyLocation>();
		SQLiteDatabase database = null;
		Cursor cursor = null;
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("select * from " + TABLE_NAME
					+ " where isSuccess = 'false' and uTime>='"+ time + " 00:00:00' and uid = '" + uid + "' order by uTime desc",
					null);
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
				location.setTime(cursor.getString(cursor.getColumnIndex("uTime")));
				location.setNet(cursor.getString(cursor.getColumnIndex("net")));


				list.add(location);
			}

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return list;
	}

	public int getCount(String uid) {
		String time = CommonTool.getStringDate(new Date(),"yyyy-MM-dd");
		SQLiteDatabase database = null;
		Cursor cursor = null;
		int size = 0;
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("select * from " + TABLE_NAME
                    + " where isSuccess = 'true' and uTime>='" + time + " 00:00:00' and uid = '" + uid + "' order by uTime desc", null);
			size = cursor.getCount();

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return size;
	}
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public long getMatchCount(String uid,String lat,String lon){
        String time = CommonTool.getStringDate(new Date(),"yyyy-MM-dd");
        SQLiteDatabase db = helper.getReadableDatabase();
        long count;
        count = DatabaseUtils.queryNumEntries(db, TABLE_NAME,
                "isSuccess=?  AND uTime>=? AND uid=? AND latitude=?  AND longitude=?" ,
                new String[]{"true",time + " 00:00:00",uid,lat,lon});
        db.close();
        return count;
    }
	public int getUnUploadCount(String uid) {
		
		String time = CommonTool.getStringDate(new Date(),"yyyy-MM-dd");
		SQLiteDatabase database = null;
		Cursor cursor = null;
		int size = 0;
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("select * from " + TABLE_NAME
					+ " where isSuccess = 'false' and uTime>='"+ time + " 00:00:00' and uid = '" + uid + "' order by uTime desc",
					null);
			size = cursor.getCount();
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return size;
	}
	
	public  List<MyLocation> findAll(String uid){
		String time = CommonTool.getStringDate(new Date(),"yyyy-MM-dd");
		SQLiteDatabase database=null;
		List<MyLocation> list=new ArrayList<MyLocation>();
		Cursor cursor=null;
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("select * from " + TABLE_NAME
					+ " where uTime>='"+ time + " 00:00:00' and uid = '" + uid + "' order by uTime desc",null);
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

				location.setTime(cursor.getString(cursor.getColumnIndex("uTime")));
				location.setNet(cursor.getString(cursor.getColumnIndex("net")));

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



	public  List<MyLocation> findAlls(String uid){
		String time = CommonTool.getStringDate(new Date(),"yyyy-MM-dd");
		SQLiteDatabase database=null;
		List<MyLocation> list=new ArrayList<MyLocation>();
		Cursor cursor=null;
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("select * from " + TABLE_NAME
					+ " where isSuccess = 'true' and uTime>='"+ time + " 00:00:00' and uid = '" + uid + "' order by uTime desc",null);
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
				location.setTime(cursor.getString(cursor.getColumnIndex("uTime")));
				location.setNet(cursor.getString(cursor.getColumnIndex("net")));
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

	public  List<MyLocation> findAllsbytime(String uid,String time){
		//String time = CommonTool.getStringDate(new Date(),"yyyy-MM-dd");
		SQLiteDatabase database=null;
		List<MyLocation> list=new ArrayList<MyLocation>();
		Cursor cursor=null;
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("select * from " + TABLE_NAME
					+ " where uTime>='"+ time + " 00:00:00'and uTime<='"+time+" 23:59:59'"+" and uid = '" + uid + "' order by uTime asc",null);

//
//			cursor = database.rawQuery("select * from " + TABLE_NAME
//					+ " where uTime>='"+ time + " 00:00:00' and uid = '" + uid + "' order by uTime asc",null);

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
				location.setNet(cursor.getString(cursor.getColumnIndex("net")));

				location.setTime(cursor.getString(cursor.getColumnIndex("uTime")));
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
	public  List<MyLocation> delete(String uid){
		String time = CommonTool.getStringDate(new Date(),"yyyy-MM-dd");
		SQLiteDatabase database=null;
		List<MyLocation> list=new ArrayList<MyLocation>();
		Cursor cursor=null;
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("delete from " + TABLE_NAME
					+ " where isSuccess = 'true' and uTime<='"+ time + " 00:00:00' and uid = '" + uid + "' ",null);
			while (cursor.moveToNext()) {
				MyLocation location = new MyLocation();

				location.setNet(cursor.getString(cursor.getColumnIndex("net")));
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
				location.setTime(cursor.getString(cursor.getColumnIndex("uTime")));
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
	public  List<MyLocation> deleteTrue(String uid){
		String time = CommonTool.getStringDate(new Date(),"yyyy-MM-dd");
		SQLiteDatabase database=null;
		List<MyLocation> list=new ArrayList<MyLocation>();
		Cursor cursor=null;
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("delete from " + TABLE_NAME
					+ " where isSuccess = 'true' and uid = '" + uid + "' ",null);
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
				location.setTime(cursor.getString(cursor.getColumnIndex("uTime")));
				location.setNet(cursor.getString(cursor.getColumnIndex("net")));

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
