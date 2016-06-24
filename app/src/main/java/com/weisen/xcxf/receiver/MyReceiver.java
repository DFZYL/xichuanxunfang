package com.weisen.xcxf.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import com.weisen.xcxf.Constant;
import com.weisen.xcxf.activity.MessageDetailActivity;
import com.weisen.xcxf.bean.Notice;
import com.weisen.xcxf.bean.NoticeDao;
import com.weisen.xcxf.tool.CommonTool;

import cn.jpush.android.api.JPushInterface;

/**
 * 自定义接收器
 * 
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
public class MyReceiver extends BroadcastReceiver {
	private static final String TAG = "JPush";

	@Override
	public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
		Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));
		// 注册ID的广播这个比较重要，因为所有的推送服务都必须，注册才可以额接收消息  
        // 注册是在后台自动完成的，如果不能注册成功，那么所有的推送方法都无法正常进行  
        // 这个注册的消息，可以发送给自己的业务服务器上。也就是在用户登录的时候，给自己的服务器发送  
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Log.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);
//            processCustomMessage(context,bundle);
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
        	Log.d(TAG, "[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
//        	processCustomMessage(context,bundle);
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 接收到推送下来的通知");
            int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            Log.d(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);
            processCustomMessage(context,bundle);
            
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 用户点击打开了通知");
            String type = bundle.getString(JPushInterface.EXTRA_EXTRA);
            String nid = "",url = "",flag = "3",latitude = "",longitude = "",userIname = "",userIphone = "",userDescription = "";
    		if (type != null && !"".equals(type)) {
    			JSONObject Object = CommonTool.parseFromJson(type);
    			url = CommonTool.getJsonString(Object, "url");
    			flag = CommonTool.getJsonString(Object, "flag");
    			nid = CommonTool.getJsonString(Object, "id");
    			latitude = CommonTool.getJsonString(Object, "latitude");
    			longitude = CommonTool.getJsonString(Object, "longitude");
    			userIname = CommonTool.getJsonString(Object, "userName");
    			userIphone = CommonTool.getJsonString(Object, "userIphone");
    			userDescription = CommonTool.getJsonString(Object, "userDescription");
    		}
	        String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
	        String context1 = bundle.getString(JPushInterface.EXTRA_ALERT);
	        Intent intent2 = new Intent();
	        intent2.setAction(Constant.BROADCAST_UPDATE_MESSAGE);
	        context.sendBroadcast(intent2);
    		Intent intent1 = new Intent(context, MessageDetailActivity.class);
    		intent1.putExtra("nid", nid);
    		intent1.putExtra("title", bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE));
    		intent1.putExtra("content", bundle.getString(JPushInterface.EXTRA_ALERT));
    		intent1.putExtra("flag", flag);
    		intent1.putExtra("url", url);
    		intent1.putExtra("time", CommonTool.getNowDate());
    		intent1.putExtra("latitude", latitude);
    		intent1.putExtra("longitude", longitude);
    		intent1.putExtra("userIname", userIname);
    		intent1.putExtra("userIphone", userIphone);
    		intent1.putExtra("userDescription", userDescription);
    		intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
    				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
    		context.startActivity(intent1);
        	
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..
        	
        } else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
        	boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
        	Log.w(TAG, "[MyReceiver]" + intent.getAction() +" connected state change to "+connected);
        } else {
        	Log.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
        }
	}

	// 打印所有的 intent extra 数据
	@SuppressLint("NewApi")
	private static String printBundle(Bundle bundle) {
		StringBuilder sb = new StringBuilder();  
        for (String key : bundle.keySet()) {  
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {  
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));  
            }else if(key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)){  
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));  
            }   
            else {  
                sb.append("\nkey:" + key + ", value:" + bundle.getString(key));  
            }  
        }  
        return sb.toString();  
	}
	
	public boolean isEmpty(String s) {
		if (null == s)
			return true;
		if (s.length() == 0)
			return true;
		if (s.trim().length() == 0)
			return true;
		return false;
	}
	
	//发送消息
		private void processCustomMessage(Context context, Bundle bundle) {
			String nid = "",url = "",flag = "3",latitude = "",longitude = "",userIname = "",userIphone = "",userDescription = "";
			String customContentString = bundle.getString(JPushInterface.EXTRA_EXTRA);
			if (customContentString != null && !"".equals(customContentString)) {
				JSONObject Object = CommonTool.parseFromJson(customContentString);
				url = CommonTool.getJsonString(Object, "url");
				flag = CommonTool.getJsonString(Object, "flag");
				nid = CommonTool.getJsonString(Object, "id");
				latitude = CommonTool.getJsonString(Object, "latitude");
				longitude = CommonTool.getJsonString(Object, "longitude");
				userIname = CommonTool.getJsonString(Object, "userName");
				userIphone = CommonTool.getJsonString(Object, "userIphone");
				userDescription = CommonTool.getJsonString(Object, "userDescription");
			}
	        Notice notice = new Notice();
	        notice.setNid(nid);
	        notice.setTitle(bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE));//标题
            notice.setContent(bundle.getString(JPushInterface.EXTRA_ALERT));//内容
	        notice.setFlag(flag);
	        notice.setUrl(url);
	        notice.setIsRead("N");
	        notice.setTime(CommonTool.getNowDate());
	        notice.setLatitude(latitude);
	        notice.setLongitude(longitude);
	        notice.setUserIname(userIname);
	        notice.setUserPhone(userIphone);
	        notice.setUserDescription(userDescription);
	        NoticeDao noticeDao = new NoticeDao(context);
	        noticeDao.addNotice(notice);
	        Intent intent = new Intent();
	        intent.setAction(Constant.BROADCAST_UPDATE_MESSAGE);
	        context.sendBroadcast(intent);
		}
}
