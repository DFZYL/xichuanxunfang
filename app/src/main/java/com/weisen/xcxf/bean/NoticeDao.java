package com.weisen.xcxf.bean;

import java.util.ArrayList;
import java.util.List;

import com.weisen.xcxf.db.DbOpenHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class NoticeDao {
	private DbOpenHelper helper = null;
	public static final String TABLE_NAME = "sys_notice";
	public static final String NOTICE_ID = "nid";
	public static final String NOTICE_TITLE = "title";
	public static final String NOTICE_CONTENT = "content";
	public static final String NOTICE_FLAG = "flag";
	public static final String NOTICE_URL = "url";
	public static final String NOTICE_TIME = "time";
	public static final String NOTICE_IS_READ = "isread";
	public static final String NOTICE_LATITUDE = "latitude";
	public static final String NOTICE_LONGITUDE = "longitude";
	public static final String NOTICE_USERINAME = "userIname";
	public static final String NOTICE_USERIPHONE = "userIphone";
	public static final String NOTICE_USERDESCRIPTION = "userDescription";

	public NoticeDao(Context context) {
		helper = DbOpenHelper.getInstance(context);
	}

	public boolean addNotice(Notice notice) {
		
		boolean flag = false;
		SQLiteDatabase database = null;
		long id = -1;
		try {
			database = helper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("nid", notice.getNid());
			values.put("flag", notice.getFlag());
			values.put("title", notice.getTitle());
			values.put("content", notice.getContent());
			values.put("url", notice.getUrl());
			values.put("time", notice.getTime());
			values.put("isread", notice.getIsRead());
			values.put("latitude", notice.getLatitude());
			values.put("longitude", notice.getLongitude());
			values.put("userIname", notice.getUserIname());
			values.put("userIphone", notice.getUserPhone());
			values.put("userDescription", notice.getUserDescription());
			id = database.insert(TABLE_NAME, null, values);
			flag = (id != -1 ? true : false);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return flag;
	}

	public void deleteById(String id)
	{

		boolean flag = false;
		SQLiteDatabase database = null;
		database=helper.getWritableDatabase();

		database.delete(TABLE_NAME,"_id=?",new String[]{String.valueOf(id)});
		database.close();
	}
	public void deleteAllById()
	{

		boolean flag = false;
		SQLiteDatabase database = null;
		database=helper.getWritableDatabase();
		database.delete(TABLE_NAME,"_id>=?",new String[]{String.valueOf('0')});
		database.close();
	}
	public List<Notice> getList(int page,int num) {
		
		List<Notice> list = new ArrayList<Notice>();
		SQLiteDatabase database = null;
		Cursor cursor = null;
		try {
			int begin = (page - 1)*num;
			int end = page * num;
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("select * from " + TABLE_NAME
					+ " order by time desc limit " + begin + "," + end, null);
			while (cursor.moveToNext()) {
				Notice notice = new Notice();
				notice.setId(cursor.getString(cursor.getColumnIndex("_id")));
				notice.setNid(cursor.getString(cursor.getColumnIndex("nid")));
				notice.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				notice.setContent(cursor.getString(cursor.getColumnIndex("content")));
				notice.setFlag(cursor.getString(cursor.getColumnIndex("flag")));
				notice.setUrl(cursor.getString(cursor.getColumnIndex("url")));
				notice.setTime(cursor.getString(cursor.getColumnIndex("time")));
				notice.setIsRead(cursor.getString(cursor.getColumnIndex("isread")));
				notice.setLatitude(cursor.getString(cursor.getColumnIndex("latitude")));
				notice.setLongitude(cursor.getString(cursor.getColumnIndex("longitude")));
				notice.setUserIname(cursor.getString(cursor.getColumnIndex("userIname")));
				notice.setUserPhone(cursor.getString(cursor.getColumnIndex("userIphone")));
				notice.setUserDescription(cursor.getString(cursor.getColumnIndex("userDescription")));
				list.add(notice);
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

	public Notice getById(String id) {
		
		Notice notice = new Notice();
		SQLiteDatabase database = null;
		Cursor cursor = null;
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("select * from " + TABLE_NAME
					+ " where _id = " + id, null);
			while (cursor.moveToNext()) {
				notice.setId(cursor.getString(cursor.getColumnIndex("_id")));
				notice.setNid(cursor.getString(cursor.getColumnIndex("nid")));
				notice.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				notice.setContent(cursor.getString(cursor.getColumnIndex("content")));
				notice.setUrl(cursor.getString(cursor.getColumnIndex("url")));
				notice.setFlag(cursor.getString(cursor.getColumnIndex("flag")));
				notice.setTime(cursor.getString(cursor.getColumnIndex("time")));
				notice.setIsRead(cursor.getString(cursor.getColumnIndex("isread")));
				notice.setLatitude(cursor.getString(cursor.getColumnIndex("latitude")));
				notice.setLongitude(cursor.getString(cursor.getColumnIndex("longitude")));
				notice.setUserIname(cursor.getString(cursor.getColumnIndex("userIname")));
				notice.setUserPhone(cursor.getString(cursor.getColumnIndex("userIphone")));
				notice.setUserDescription(cursor.getString(cursor.getColumnIndex("userDescription")));
			}

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return notice;
	}
	
	public int getUnReadCount() {
		
		SQLiteDatabase database = null;
		int count = 0;
		Cursor cursor = null;
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("select * from " + TABLE_NAME
					+ " where isread = 'N'", null);
			count = cursor.getCount();

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return count;
	}

	public void updateUnReadState(String id) {
		String sql = "update " + TABLE_NAME +" set seq=0 where name='"
				+ TABLE_NAME + "'";
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL(sql);
	}
}
