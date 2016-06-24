package com.weisen.xcxf.tool;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

/**
 * 
 * @author JQY
 * 
 *         写一些通用的方法，例如生成时间的格式、生成32位随机码
 * 
 */
public class CommonTool {

	/**
	 * 
	 * @return 10位随机码
	 */
	public static String getRandom() {
		String str = "";
		for (int i = 0; i < 2; i++) {
			int demo = (int) (Math.random() * 100000);
			str += demo + "";
		}
		return str;
	}
	
	// string类型转换为long类型
	 	// strTime要转换的String类型的时间
	 	// formatType时间格式
	 	// strTime的时间格式和formatType的时间格式必须相同
	 	public static long stringToLong(String strTime, String formatType)
	 			throws ParseException {
	 		Date date = stringToDate(strTime, formatType); // String类型转成date类型
	 		if (date == null) {
	 			return 0;
	 		} else {
	 			long currentTime = dateToLong(date); // date类型转成long类型
	 			return currentTime;
	 		}
	 	}
	 // string类型转换为date类型
	 	// strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
	 	// HH时mm分ss秒，
	 	// strTime的时间格式必须要与formatType的时间格式相同
	 	public static Date stringToDate(String strTime, String formatType)
	 			throws ParseException {
	 		SimpleDateFormat formatter = new SimpleDateFormat(formatType);
	 		Date date = null;
	 		date = formatter.parse(strTime);
	 		return date;
	 	}
	 // date类型转换为long类型
	 	// date要转换的date类型的时间
	 	public static long dateToLong(Date date) {
	 		return date.getTime();
	 	}

	/**
	 * 
	 * @return 当前时间，以yyyyMMddHHmmss形式返回
	 */
	public static String getNowDate() {
		String nowDate = "";
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		nowDate = format.format(date);
		return nowDate;
	}

	/**
	 * 
	 * @return 当前时间，将yyyyMMddHHmmss形式的数据返回
	 */
	public static Date getDate(String time) {

		Date date = null;
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
			date = format.parse(time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}

	public static Date getDate(String time, String formatStyle) {

		Date date = null;
		try {
			SimpleDateFormat format = new SimpleDateFormat(formatStyle);
			date = format.parse(time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * 
	 * @return 将时间以想要的形式
	 */
	public static String getStringDate(Date date, String formatStyle) {
		String stringDate = "";
		try {
			SimpleDateFormat format = new SimpleDateFormat(formatStyle);
			stringDate = format.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringDate;
	}

	/**
	 * 
	 * @return 将时间以想要的形式
	 */
	public static String getStringDate(long time, String formatStyle) {
		String stringDate = "";
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(time * 1000);
			SimpleDateFormat format = new SimpleDateFormat(formatStyle);
			stringDate = format.format(calendar.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringDate;
	}

	/**
	 * 
	 * @return 将时间以想要的形式
	 */
	public static String getStringDate(String time, String formatStyle) {
		String stringDate = "";
		try {
			Date date = getDate(time);
			SimpleDateFormat format = new SimpleDateFormat(formatStyle);
			stringDate = format.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringDate;
	}

	/**
	 * 
	 * @return 将时间以想要的形式
	 */
	public static String getStringDate(String time, String formatStyle1,
			String formatStyle2) {
		String stringDate = "";
		try {
			Date date = getDate(time, formatStyle1);
			SimpleDateFormat format = new SimpleDateFormat(formatStyle2);
			stringDate = format.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringDate;
	}

	/**
	 * @return 将字符串转化成json
	 */
	public static JSONObject parseFromJson(String json) {
		JSONObject object = new JSONObject();
		if (TextUtils.isEmpty(json))
			return null;

		try {
			object = new JSONObject(json);
			return object;
		} catch (JSONException e) {

			e.printStackTrace();
		}
		return object;
	}

	/**
	 * 
	 * @return 获取JSON对象中的JSON
	 */
	public static JSONObject getJsonObj(JSONObject obj, String key) {
		JSONObject jsonobj = new JSONObject();
		if (obj.has(key)) {
			try {
				jsonobj = obj.getJSONObject(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return jsonobj;
	}

	/**
	 * 
	 * @return 获取JSON对象中的JSONArray
	 */
	public static JSONArray getJsonArry(JSONObject obj, String key) {
		JSONArray jsonobj = new JSONArray();
		if (obj.has(key)) {
			try {
				jsonobj = obj.getJSONArray(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return jsonobj;
	}

	/**
	 * 
	 * @return 获取JSON对象中的String
	 */
	public static String getJsonString(JSONObject obj, String key) {
		String str = "";
		if (obj.has(key)) {
			try {
				str = obj.getString(key);
				if (str.equals("null"))
					str = "";
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return str;
	}

	/**
	 * 
	 * @return 获取JSON对象中的String
	 */
	public static Object getJsonObject(JSONObject obj, String key) {
		Object object = "";
		if (obj.has(key)) {
			try {
				object = obj.get(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return object;
	}

	/**
	 * 
	 * @return 获取JSON对象中的boolean
	 */
	public static boolean getJsonBoolean(JSONObject obj, String key) {
		boolean flag = false;
		if (obj.has(key)) {
			try {
				flag = obj.getBoolean(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return flag;
	}

	/**
	 * 
	 * @return 获取JSON对象中的Float
	 */
	public static double getJsonFloat(JSONObject obj, String key) {
		double f = 0;
		if (obj.has(key)) {
			try {
				f = obj.getDouble(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return f;
	}

	/**
	 * 
	 * @return 获取JSON对象中的double
	 */
	public static double getJsonDouble(JSONObject obj, String key) {
		double dd = 0;
		if (obj.has(key)) {
			try {
				dd = obj.getDouble(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return dd;
	}

	/**
	 * 
	 * @return 获取JSON对象中的int
	 */
	public static int getJsonInt(JSONObject obj, String key) {
		int str = 0;
		if (obj.has(key)) {
			try {
				str = obj.getInt(key);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return str;
	}

	public static SpannableString getStr(String s) {
		String[] zhekou = s.split("/");
		SpannableString ss = new SpannableString(s);
		ss.setSpan(new ForegroundColorSpan(Color.RED), 0,
				zhekou[0].length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		ss.setSpan(new ForegroundColorSpan(Color.BLACK),
				zhekou[0].length() + 1, s.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return ss;
	}

	// 获取ApiKey
	public static String getMetaValue(Context context, String metaKey) {
		Bundle metaData = null;
		String apiKey = null;
		if (context == null || metaKey == null) {
			return null;
		}
		try {
			ApplicationInfo ai = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(),
							PackageManager.GET_META_DATA);
			if (null != ai) {
				metaData = ai.metaData;
			}
			if (null != metaData) {
				apiKey = metaData.getString(metaKey);
			}
		} catch (NameNotFoundException e) {

		}
		return apiKey;
	}

	/**
	 * dip转为 px
	 */
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 * px 转为 dip
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 检测网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetWorkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}

		return false;
	}

	public static String getString(Context context, int resId) {
		return context.getResources().getString(resId);
	}

	public static String getTopActivity(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

		if (runningTaskInfos != null)
			return runningTaskInfos.get(0).topActivity.getClassName();
		else
			return "";
	}

	public static String format(String str) {
		if (str == null)
			return "";
		else
			return str;
	}

	// 手机号以158****3443的 形式表现
	public static String subString(String dataMobile) {
		String dataPhone = "";
		try {
			dataPhone = dataMobile.substring(0, 3) + "****"
					+ dataMobile.substring(7, 11);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataPhone;
	}

	public static DisplayImageOptions getOptions(int id) {

		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.showImageOnFail(id).showStubImage(id).showImageForEmptyUri(id)
				.cacheInMemory().bitmapConfig(Bitmap.Config.RGB_565)
				.cacheOnDisc().build();
		return options;
	}

	public static String getMiddlePic(String pic) {
		if (pic == null || pic.equals(""))
			return "";
		else
			return pic + "_b.jpg";
	}

	public static String getSmallPic(String pic) {
		if (pic == null || pic.equals(""))
			return "";
		else
			return pic + "_sum.jpg";
	}

	public static String formatFloat(double s) {
		String str = s + "";
		if (str == null)
			return "";
		else {
			int index = str.indexOf(".");
			if (index > 0)
				str = str.substring(0, index + 2);

		}
		return str;
	}

	// 服务是否运行
	public static boolean isServiceRunning(Context context, String serviceName) {
		boolean isRunning = false;
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> lists = am.getRunningServices(Integer.MAX_VALUE);

		for (RunningServiceInfo info : lists) {// 获取运行服务再启动
			if (info.service.getClassName().equals(serviceName)) {
				Log.i("Service1进程", "" + info.service.getClassName());
				isRunning = true;
			}
		}
//		//zh 10.5添加重启服务
//		if (!isRunning) {
//			Intent i = new Intent(context, MyLocationService.class); 
//		    context.startService(i);
//		}
		
		return isRunning;
	}

	// 进程是否运行
	public static boolean isProessRunning(Context context, String proessName) {

		boolean isRunning = false;
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		List<RunningAppProcessInfo> lists = am.getRunningAppProcesses();
		for (RunningAppProcessInfo info : lists) {
			if (info.processName.equals(proessName)) {
				 Log.i("Service1进程--", "" + info.processName);
				isRunning = true;
			}
		}

		return isRunning;
	}
}
