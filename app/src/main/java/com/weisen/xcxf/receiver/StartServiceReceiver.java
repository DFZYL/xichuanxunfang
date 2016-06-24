package com.weisen.xcxf.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.weisen.xcxf.Constant;

public class StartServiceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		
		String userId = "";
		SharedPreferences preferences = context.getSharedPreferences(
				Constant.APP_SP, context.MODE_MULTI_PROCESS);
		userId = preferences.getString(Constant.SP_USERID, "");
//		if (userId == null || userId.equals(""))
//			userId = "";
//		if (!userId.equals("")) {
//			Intent localIntent = new Intent(context, MyLocationService.class);
//			localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			context.startService(localIntent);
//		}
	}

}
