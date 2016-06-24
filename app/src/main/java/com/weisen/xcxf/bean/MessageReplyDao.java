package com.weisen.xcxf.bean;

import java.util.ArrayList;
import java.util.List;

import com.weisen.xcxf.db.DbOpenHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class MessageReplyDao {
	private DbOpenHelper helper = null;
	public static final String TABLE_NAME = "my_reply";
	
	public MessageReplyDao(Context context) {
		helper = DbOpenHelper.getInstance(context);
	}
	
	public int addReply(MessageReply reply) {
		
		SQLiteDatabase database = null;
		int id = -1;
		try {
			database = helper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("replytime", reply.getReplyTime());
			values.put("uid", reply.getUid());
			values.put("replytxt", reply.getReplyTxt());

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
	
	public  List<MessageReply> findAll(String uid){
		SQLiteDatabase database=null;
		List<MessageReply> list=new ArrayList<MessageReply>();
		Cursor cursor=null;
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("select * from " + TABLE_NAME
					+ " where  uid = '" + uid + "' order by replytime desc", null);
			while (cursor.moveToNext()) {
				MessageReply report = new MessageReply();
				report.setUid(cursor.getString(cursor.getColumnIndex("uid")));
				report.setReplyTxt(cursor.getString(cursor.getColumnIndex("replytxt")));
				report.setReplyTime(cursor.getString(cursor.getColumnIndex("replytime")));
				list.add(report);
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
