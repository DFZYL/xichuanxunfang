package com.weisen.xcxf.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.weisen.xcxf.db.DbOpenHelper;
import com.weisen.xcxf.tool.CommonTool;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class CaseDao {
	private DbOpenHelper helper = null;
	public static final String TABLE_NAME = "case_report";

	public CaseDao(Context context) {
		helper = DbOpenHelper.getInstance(context);
	}

	public int addCase(CaseReport caseReport) {
		
		SQLiteDatabase database = null;
		int id = -1;
		try {
			database = helper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("uid", caseReport.getUid());
			values.put("uType", caseReport.getuType());
			values.put("uLoti", caseReport.getuLoti());
			values.put("uLati", caseReport.getuLati());
			values.put("uAlti", caseReport.getuAlti());
			values.put("uAddr", caseReport.getuAddr());
			values.put("uSpeed", caseReport.getuSpeed());
			values.put("uDirection", caseReport.getuDirection());
			values.put("uAccurary", caseReport.getuAccurary());
			values.put("uLocType", caseReport.getuLocType());
			values.put("uTime", caseReport.getuTime());
			values.put("uContent", caseReport.getuContent());
			values.put("attach", caseReport.getAttach());
			values.put("q", caseReport.getQ());
			values.put("uRemark", caseReport.getuRemark());
			values.put("uUserId", caseReport.getuUserId());
			values.put("isSuccess", caseReport.getIsSuccess());
			values.put("uUploadTime", caseReport.getUploadTime());
			values.put("flag", caseReport.getFlag());
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

	public void updateCase(int id) {
		String time = CommonTool.getStringDate(new Date(),
				"yyyy-MM-dd HH:mm:ss");
		String sql = "update " + TABLE_NAME
				+ " set isSuccess='true',uUploadTime='" + time + "' where _id="
				+ id;
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL(sql);
	}

	public String getAttach(int id) {
		String attach = "";
		SQLiteDatabase database = null;
		Cursor cursor = null;
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("select * from " + TABLE_NAME
					+ " where _id = " + id, null);
			while (cursor.moveToNext()) {
				attach = cursor.getString(cursor.getColumnIndex("attach"));
			}

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return attach;
	}

	public List<CaseReport> getUnUploadList(String uid) {
		
		List<CaseReport> list = new ArrayList<CaseReport>();
		SQLiteDatabase database = null;
		Cursor cursor = null;
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("select * from " + TABLE_NAME
					+ " where isSuccess='false' and uid = '" + uid + "'", null);
			while (cursor.moveToNext()) {
				CaseReport caseReport = new CaseReport();
				caseReport.setId(cursor.getInt(cursor.getColumnIndex("_id"))
						+ "");
				caseReport.setuType(cursor.getString(cursor
						.getColumnIndex("uType")));
				caseReport.setuLati(cursor.getString(cursor
						.getColumnIndex("uLati")));
				caseReport.setuLoti(cursor.getString(cursor
						.getColumnIndex("uLoti")));
				caseReport.setuAlti(cursor.getString(cursor
						.getColumnIndex("uAlti")));
				caseReport.setuAddr(cursor.getString(cursor
						.getColumnIndex("uAddr")));
				caseReport.setuSpeed(cursor.getString(cursor
						.getColumnIndex("uSpeed")));
				caseReport.setuDirection(cursor.getString(cursor
						.getColumnIndex("uDirection")));
				caseReport.setuAccurary(cursor.getString(cursor
						.getColumnIndex("uAccurary")));
				caseReport.setuTime(cursor.getString(cursor
						.getColumnIndex("uTime")));
				caseReport.setuContent(cursor.getString(cursor
						.getColumnIndex("uContent")));
				caseReport.setuLocType(cursor.getString(cursor
						.getColumnIndex("uLocType")));

				caseReport.setAttach(cursor.getString(cursor
						.getColumnIndex("attach")));
				caseReport.setQ(cursor.getString(cursor.getColumnIndex("q")));
				caseReport.setuUserId(cursor.getString(cursor
						.getColumnIndex("uUserId")));
				caseReport.setuRemark(cursor.getString(cursor
						.getColumnIndex("uRemark")));
//				caseReport.setFlag(cursor.getString(cursor
//						.getColumnIndex("flag")));
				list.add(caseReport);
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
	public List<CaseReport> getCaseList(String uid) {
		
		List<CaseReport> list = new ArrayList<CaseReport>();
		SQLiteDatabase database = null;
		Cursor cursor = null;
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("select * from " + TABLE_NAME
					+ " where isSuccess='true' and uid = '" + uid + "'", null);
			while (cursor.moveToNext()) {
				CaseReport caseReport = new CaseReport();
				caseReport.setId(cursor.getInt(cursor.getColumnIndex("_id")) + "");
				caseReport.setuType(cursor.getString(cursor
						.getColumnIndex("uType")));
				caseReport.setuLati(cursor.getString(cursor
						.getColumnIndex("uLati")));
				caseReport.setuLoti(cursor.getString(cursor
						.getColumnIndex("uLoti")));
				caseReport.setuAlti(cursor.getString(cursor
						.getColumnIndex("uAlti")));
				caseReport.setuAddr(cursor.getString(cursor
						.getColumnIndex("uAddr")));
				caseReport.setuSpeed(cursor.getString(cursor
						.getColumnIndex("uSpeed")));
				caseReport.setuDirection(cursor.getString(cursor
						.getColumnIndex("uDirection")));
				caseReport.setuAccurary(cursor.getString(cursor
						.getColumnIndex("uAccurary")));
				caseReport.setuTime(cursor.getString(cursor
						.getColumnIndex("uTime")));
				caseReport.setuContent(cursor.getString(cursor
						.getColumnIndex("uContent")));
				caseReport.setuLocType(cursor.getString(cursor
						.getColumnIndex("uLocType")));
				
				caseReport.setAttach(cursor.getString(cursor
						.getColumnIndex("attach")));
				caseReport.setQ(cursor.getString(cursor.getColumnIndex("q")));
				caseReport.setuUserId(cursor.getString(cursor
						.getColumnIndex("uUserId")));
				caseReport.setuRemark(cursor.getString(cursor.getColumnIndex("uRemark")));
//				caseReport.setFlag(cursor.getString(cursor
//						.getColumnIndex("flag")));
				list.add(caseReport);
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

	public List<CaseReport> getList(int page, int num) {
		
		List<CaseReport> list = new ArrayList<CaseReport>();
		SQLiteDatabase database = null;
		Cursor cursor = null;
		try {
			int begin = (page - 1) * num;
			int end = page * num;
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("select * from " + TABLE_NAME
					+ " order by uTime desc limit " + begin + "," + end, null);
			while (cursor.moveToNext()) {
				CaseReport caseReport = new CaseReport();
				caseReport.setId(cursor.getInt(cursor.getColumnIndex("_id"))+ "");
				caseReport.setuType(cursor.getString(cursor
						.getColumnIndex("uType")));
				caseReport.setuLati(cursor.getString(cursor
						.getColumnIndex("uLati")));
				caseReport.setuLoti(cursor.getString(cursor
						.getColumnIndex("uLoti")));
				caseReport.setuAlti(cursor.getString(cursor
						.getColumnIndex("uAlti")));
				caseReport.setuAddr(cursor.getString(cursor
						.getColumnIndex("uAddr")));
				caseReport.setuSpeed(cursor.getString(cursor
						.getColumnIndex("uSpeed")));
				caseReport.setuDirection(cursor.getString(cursor
						.getColumnIndex("uDirection")));
				caseReport.setuAccurary(cursor.getString(cursor
						.getColumnIndex("uAccurary")));
				caseReport.setuTime(cursor.getString(cursor
						.getColumnIndex("uTime")));
				caseReport.setuContent(cursor.getString(cursor
						.getColumnIndex("uContent")));
				caseReport.setuLocType(cursor.getString(cursor
						.getColumnIndex("uLocType")));

				caseReport.setAttach(cursor.getString(cursor
						.getColumnIndex("attach")));
				caseReport.setQ(cursor.getString(cursor.getColumnIndex("q")));
				caseReport.setuUserId(cursor.getString(cursor
						.getColumnIndex("uUserId")));
				caseReport.setuRemark(cursor.getString(cursor
						.getColumnIndex("uRemark")));
				caseReport.setIsSuccess(cursor.getString(cursor
						.getColumnIndex("isSuccess")));
				caseReport.setUploadTime(cursor.getString(cursor
						.getColumnIndex("uUploadTime")));
				caseReport.setFlag(cursor.getString(cursor
						.getColumnIndex("flag")));
				list.add(caseReport);
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
	public List<CaseReport> deleteList(String uid) {
		
		List<CaseReport> list = new ArrayList<CaseReport>();
		SQLiteDatabase database = null;
		Cursor cursor = null;
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("delete from " + TABLE_NAME
					+ " where isSuccess='true' and uid = '" + uid + "'", null);
			while (cursor.moveToNext()) {
				CaseReport caseReport = new CaseReport();
				caseReport.setId(cursor.getInt(cursor.getColumnIndex("_id"))
						+ "");
				caseReport.setuType(cursor.getString(cursor
						.getColumnIndex("uType")));
				caseReport.setuLati(cursor.getString(cursor
						.getColumnIndex("uLati")));
				caseReport.setuLoti(cursor.getString(cursor
						.getColumnIndex("uLoti")));
				caseReport.setuAlti(cursor.getString(cursor
						.getColumnIndex("uAlti")));
				caseReport.setuAddr(cursor.getString(cursor
						.getColumnIndex("uAddr")));
				caseReport.setuSpeed(cursor.getString(cursor
						.getColumnIndex("uSpeed")));
				caseReport.setuDirection(cursor.getString(cursor
						.getColumnIndex("uDirection")));
				caseReport.setuAccurary(cursor.getString(cursor
						.getColumnIndex("uAccurary")));
				caseReport.setuTime(cursor.getString(cursor
						.getColumnIndex("uTime")));
				caseReport.setuContent(cursor.getString(cursor
						.getColumnIndex("uContent")));
				caseReport.setuLocType(cursor.getString(cursor
						.getColumnIndex("uLocType")));
				
				caseReport.setAttach(cursor.getString(cursor
						.getColumnIndex("attach")));
				caseReport.setQ(cursor.getString(cursor.getColumnIndex("q")));
				caseReport.setuUserId(cursor.getString(cursor
						.getColumnIndex("uUserId")));
				caseReport.setuRemark(cursor.getString(cursor
						.getColumnIndex("uRemark")));
				caseReport.setIsSuccess(cursor.getString(cursor
						.getColumnIndex("isSuccess")));
				caseReport.setUploadTime(cursor.getString(cursor
						.getColumnIndex("uUploadTime")));
				caseReport.setFlag(cursor.getString(cursor
						.getColumnIndex("flag")));
				list.add(caseReport);
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
		
		SQLiteDatabase database = null;
		Cursor cursor = null;
		int size = 0;
		try {
			database = helper.getReadableDatabase();
			cursor = database.rawQuery("select * from " + TABLE_NAME
					+ " where isSuccess='true' and uid = '" + uid + "'", null);
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
	
}
