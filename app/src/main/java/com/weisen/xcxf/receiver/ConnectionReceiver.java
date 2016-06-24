package com.weisen.xcxf.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ConnectionReceiver extends BroadcastReceiver {

	private Context context;
	@Override
	public void onReceive(Context context, Intent arg1) {
        this.context = context;

    }
}
