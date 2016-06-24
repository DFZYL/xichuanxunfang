package com.weisen.xcxf.adapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseAdapter {
	// 数据库对象
	private SQLiteDatabase mSQLiteDatabase = null;
	// 数据库名名称
	private final static String DATABASE_NAME = "Flow.db";
	// 表名
	private final static String TABLE_NAME = "table1";
	// 主键，ID
	private final static String TABLE_ID = "FlowID";
	// 程序名称
	// private final static String TABLE_PRO = "ProName";
	// 上行流量，单位byte
	private final static String TABLE_UPF = "UpFlow";
	// 下载流量，单位byte
	private final static String TABLE_DPF = "DownFlow";
	// 储存日期
	// 格式：YYYY-MM-DD HH:MM:SS
	private final static String TABLE_TIME = "Time";
	// 联网类型，有WIFI和GPRS
	private final static String TABLE_WEB = "WebType";
	// 数据库版本号
	private final static int DB_VERSION = 1;

	private Context mContext = null;

	// /创建表的语句，用于第一次创建数据库时，创建表
	private final static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
			+ " (" + TABLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ TABLE_UPF + " Long," + TABLE_DPF + " Long," + TABLE_WEB
			+ " INTEGER," + TABLE_TIME + " DATETIME)";

	// 数据库adapter，用于创建数据库
	private DatabaseHelper mDatabaseHelper = null;

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DB_VERSION);
		}

		/*
		 * 创建数据库 创建表
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL(CREATE_TABLE);
		}

		/*
		 * 数据库跟新删除表并重新创建新表
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			// db.execSQL("DROP TABLE IF EXISTS notes");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}

	/* 构造函数 获取context */
	public DatabaseAdapter(Context context) {
		mContext = context;
	}

	// 打开数据库，返回数据库对象
	public void open() throws SQLException {
		mDatabaseHelper = new DatabaseHelper(mContext);
		mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
	}

	// 关闭数据库
	public void close() {
		mDatabaseHelper.close();
	}

	/* 插入一条数据 */
	public void insertData(long UpFlow, long DownFlow, int WebType, Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:MM");
		String dataString = sdf.format(date);
		String insertData = " INSERT INTO " + TABLE_NAME + " (" + TABLE_UPF
				+ ", " + TABLE_DPF + "," + TABLE_WEB + "," + TABLE_TIME
				+ " ) values('" + UpFlow + "', '" + DownFlow + "','" + WebType
				+ "',datetime('" + dataString + "'));";
		mSQLiteDatabase.execSQL(insertData);
	}

	// 更新数据
	public void updateData(long upFlow, long downFlow, int webType, Date date) {
		String dataString = new SimpleDateFormat("yyyy-MM-dd").format(date);
		String updateData = " UPDATE " + TABLE_NAME + " SET " + TABLE_UPF
				+ "='" + upFlow + "' , " + TABLE_DPF + "='" + downFlow
				+ "' WHERE " + TABLE_WEB + "='" + webType + "' and "
				+ TABLE_TIME + " like '" + dataString + "%'";
		mSQLiteDatabase.execSQL(updateData);
	}

	/* 检查是否存在该条数据 */
	public Cursor check(int netType, Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dataString = sdf.format(date);
		// long[] list = null ;
		Cursor mCursor = mSQLiteDatabase.query(TABLE_NAME, new String[] {
				TABLE_UPF + " AS upPro", TABLE_DPF + " AS dwPro" }, TABLE_WEB
				+ "='" + netType + "' and " + TABLE_TIME + " like '"
				+ dataString + "%'", null, null, null, null, null);// date转化
		// mCursor.close();
		return mCursor;
	}

	/* 查询今日流量数据 */
	public Cursor fetchDayFlow(int year, int month, int day, int netType) {
		StringBuffer date = new StringBuffer();
		date.append(String.valueOf(year) + "-");
		if (month < 10) {
			date.append("0" + String.valueOf(month) + "-");
		} else {
			date.append(String.valueOf(month) + "-");
		}
		if (day < 10) {
			date.append("0" + String.valueOf(day));
		} else {
			date.append(String.valueOf(day));
		}
		Cursor mCursor = mSQLiteDatabase.query(TABLE_NAME, new String[] {
				  TABLE_UPF + " AS sumUp",
				 TABLE_DPF + " as sumDw" }, TABLE_WEB + "='" + netType
				+ "' and " + TABLE_TIME + " LIKE '" + date.toString() + "%'",
				null, null, null, null, null);
		// mCursor.close();
		return mCursor;
	}

	/* 查询每月流量 可用于月报表和月流量统计 */
	public Cursor fetchMonthFlow(int year, int Month, int netType) {
		StringBuffer date = new StringBuffer();
		date.append(String.valueOf(year) + "-");
		if (Month < 10) {
			date.append("0" + String.valueOf(Month) + "-");
		} else {
			date.append(String.valueOf(Month) + "-");
		}
		Cursor mCursor = mSQLiteDatabase.query(TABLE_NAME, new String[] {
				"sum(" + TABLE_UPF + ") AS monthUp",
				"sum(" + TABLE_DPF + ") as monthDw" }, TABLE_WEB + "='"
				+ netType + "' and " + TABLE_TIME + " LIKE '" + date.toString()
				+ "%'", null, null, null, null, null);
		// mCursor.close();
		return mCursor;
	}

	// 计算每天的上传流量
	public long getProFlowUp(int netType, Date date) {
		Cursor cur = check(netType, date);
		long UP = 0;
		if (cur.moveToNext()) {
			int up = cur.getColumnIndex("upPro");
			UP = cur.getLong(up);

			// return UP ;
		}
		cur.close();
		return UP;

	}

	// 计算每天的下载流量
	public long getProFlowDw(int netType, Date date) {
		Cursor cur = check(netType, date);
		long UP = 0;
		if (cur.moveToNext()) {
			int up = cur.getColumnIndex("dwPro");
			UP = cur.getLong(up);
		}
		cur.close();
		return UP;
	}

	/* 计算每日的流量 */
	public long calculate(int year, int month, int day, int netType) {
		Cursor calCurso = fetchDayFlow(year, month, day, netType);
		long sum = 0;
		if (calCurso != null) {
			if (calCurso.moveToFirst()) {
				do {
					int upColumn = calCurso.getColumnIndex("sumUp");
					int dwColumn = calCurso.getColumnIndex("sumDw");
					sum = calCurso.getLong(upColumn)
							+ calCurso.getLong(dwColumn);
				} while (calCurso.moveToNext());
			}
		}
		// calCurso.close();
		return sum;
	}

	// 计算本周上传流量
	public long weekUpFloew(int netType) {
		Calendar date1 = Calendar.getInstance();
		date1.set(Calendar.DAY_OF_WEEK, date1.getFirstDayOfWeek());
		long flowUp = 0;
		for (int i = 0; i < 7; i++) {

			int y = date1.get(Calendar.YEAR);
			int m = date1.get(Calendar.MONTH) + 1;
			int d = date1.get(Calendar.DAY_OF_MONTH);
			flowUp += calculateUp(y, m, d, netType);
			date1.add(Calendar.DAY_OF_WEEK, 1);

		}
		return flowUp;
	}

	// 计算本周下载流量
	public long weekDownFloew(int netType) {
		Calendar date1 = Calendar.getInstance();// 得到现在的日期
		date1.set(Calendar.DAY_OF_WEEK, date1.getFirstDayOfWeek());// 将日期改为今天所在周的第一天
		long flowDw = 0;
		for (int i = 0; i < 7; i++) {

			int y = date1.get(Calendar.YEAR);
			int m = date1.get(Calendar.MONTH) + 1;
			int d = date1.get(Calendar.DAY_OF_MONTH);
			flowDw += calculateDw(y, m, d, netType);
			date1.add(Calendar.DAY_OF_WEEK, 1); // date1加一天
		}

		return flowDw;
	}

	// 计算每月上传流量
	public long calculateUpForMonth(int year, int Month, int netType) {
		Cursor lCursor = fetchMonthFlow(year, Month, netType);
		long sum = 0;

		if (lCursor != null) {
			if (lCursor.moveToFirst()) {
				do {
					int upColumn = lCursor.getColumnIndex("monthUp");
					sum += lCursor.getLong(upColumn);
				} while (lCursor.moveToNext());
			}
			lCursor.close();
		}
		return sum;
	}

	// 计算每月下载流量
	public long calculateDnForMonth(int year, int Month, int netType) {
		Cursor lCursor = fetchMonthFlow(year, Month, netType);
		long sum = 0;

		if (lCursor != null) {
			if (lCursor.moveToFirst()) {
				do {
					int dwColumn = lCursor.getColumnIndex("monthDw");
					sum += lCursor.getLong(dwColumn);
				} while (lCursor.moveToNext());
			}
			lCursor.close();
		}
		return sum;
	}

	/* 计算某月的流量 */
	public long calculateForMonth(int year, int Month, int netType) {
		Cursor lCursor = fetchMonthFlow(year, Month, netType);
		long sum;
		long monthSum = 0;

		if (lCursor != null) {
			if (lCursor.moveToFirst()) {
				do {
					int upColumn = lCursor.getColumnIndex("monthUp");
					int dwColumn = lCursor.getColumnIndex("monthDw");
					sum = lCursor.getLong(upColumn) + lCursor.getLong(dwColumn);
					monthSum += sum;
				} while (lCursor.moveToNext());
			}
			lCursor.close();
		}
		return monthSum;
	}

	/* 计算每日的上传流量 */
	public long calculateUp(int year, int month, int day, int netType) {
		Cursor calCurso = fetchDayFlow(year, month, day, netType);
		long sum = 0;
		if (calCurso != null) {
			if (calCurso.moveToFirst()) {
				do {
					int upColumn = calCurso.getColumnIndex("sumUp");
					sum = calCurso.getLong(upColumn);
				} while (calCurso.moveToNext());
			}
		}
		// calCurso.close();
		return sum;
	}

	/* 计算每日的xiazai流量 */
	public long calculateDw(int year, int month, int day, int netType) {
		Cursor calCurso = fetchDayFlow(year, month, day, netType);
		long sum = 0;
		if (calCurso != null) {
			if (calCurso.moveToFirst()) {
				do {
					int dwColumn = calCurso.getColumnIndex("sumDw");
					sum = calCurso.getLong(dwColumn);
				} while (calCurso.moveToNext());
			}
		}
		// calCurso.close();
		return sum;
	}

	/* 计算每个程序的流量 */
	// public Cursor programmeCur(String proName, int netType) {
	// Cursor mCursor = mSQLiteDatabase.query(TABLE_NAME, new String[] {
	// TABLE_PRO, "sum(" + TABLE_UPF + ") AS upPro",
	// "sum(" + TABLE_DPF + ") AS dwPro" }, TABLE_PRO + "= '"
	// + proName + "' and " + TABLE_WEB + "=" + netType, null, null,
	// null, null, null);// dateת��
	// return mCursor;
	// }

	/* 计算每个程序的流量 */
	// public long calPro(String proName, int netType) {
	// Cursor cursor = programmeCur(proName, netType);
	// long upFlow;
	// long downFlow;
	// long flow = 0;
	// long countFlow = 0;
	// if (cursor.moveToFirst()) {
	// do {
	// int upCol = cursor.getColumnIndex("upPro");
	// int downCol = cursor.getColumnIndex("dwPro");
	// upFlow = cursor.getLong(upCol);
	// downFlow = cursor.getLong(downCol);
	// flow = upFlow + downFlow;
	// countFlow += flow;
	// } while (cursor.moveToNext());
	// }
	// cursor.close();
	// return countFlow;
	// }

	/* �������г�������� */
	// public Cursor allProgrammeCur(int netType) {
	// Cursor mCursor = mSQLiteDatabase.query(TABLE_NAME, new String[] {
	// TABLE_PRO, "sum(" + TABLE_UPF + ") AS upPro",
	// "sum(" + TABLE_DPF + ") AS dwPro" }, TABLE_WEB + "=" + netType,
	// null, null, TABLE_PRO, null, null);// dateת��
	// return mCursor;
	// }
	//
	// public ProgrammeHolder[] calAllPro(int netType) {
	// Cursor cursor = allProgrammeCur(netType);
	// int count = cursor.getCount();
	// ProgrammeHolder[] programmeHolder = new ProgrammeHolder[count];
	// long upFlow;
	// long downFlow;
	// long flow;
	// String proName;
	// ProgrammeHolder ph ;
	// int i =0;
	// if (cursor.moveToFirst()) {
	// do {
	// flow =0;
	//
	// int proNameId = cursor.getColumnIndex(TABLE_PRO);
	// int upCol = cursor.getColumnIndex("upPro");
	// int downCol = cursor.getColumnIndex("dwPro");
	//
	// proName = cursor.getString(proNameId);
	// upFlow = cursor.getLong(upCol);
	// downFlow = cursor.getLong(downCol);
	// flow = upFlow + downFlow;
	//
	// ph = new ProgrammeHolder();
	// ph.setTraffic(flow);
	// ph.setName(proName);
	//
	// programmeHolder[i] = ph;
	// i++;
	// } while (cursor.moveToNext());
	// }
	// cursor.close();
	// return programmeHolder;
	// }
	/* 清空数据 */
	public void deleteAll() {
		mSQLiteDatabase.execSQL("DROP TABLE " + TABLE_NAME);
	}

	public void converDate() {

	}

	public void clear() {
		mSQLiteDatabase.delete(TABLE_NAME, null, null);
	}
}
