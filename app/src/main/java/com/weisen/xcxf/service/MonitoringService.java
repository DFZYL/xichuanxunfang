package com.weisen.xcxf.service;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.weisen.xcxf.Constant;
import com.weisen.xcxf.adapter.DatabaseAdapter;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.tool.CommonTool;
import com.weisen.xcxf.tool.HttpTool;
import com.weisen.xcxf.tool.MD5Tool;
import com.weisen.xcxf.tool.UnIntent;
import com.weisen.xcxf.tool.UploadTool;

public class MonitoringService extends Service {

	private DatabaseAdapter dbAdapter;
	private Handler handler = new Handler();
	private long mobileRx = 0, mobileTx = 0;
	private long old_mobileRx = 0, old_mobileTx = 0;
	private long mrx = 0, mtx = 0;
	private long mobileRx_all = 0, mobileTx_all = 0;
	private Intent in = new Intent("Runnable");
    private LocationManager lm;
	int threadNum;
	static int count = 12;
	NetworkInfo nwi;
	private ApplicationInfo info;
    SharedPreferences sp;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
    Calendar currentCa;
	@Override
	public void onCreate() {
		info = getApplicationInfo();
		old_mobileRx = TrafficStats.getUidRxBytes(info.uid);
		old_mobileTx = TrafficStats.getUidTxBytes(info.uid);
        dbAdapter = new DatabaseAdapter(MonitoringService.this);
        dbAdapter.open();
	//long trafficstats = old_mobileRx + old_mobileTx;
        currentCa = Calendar.getInstance();
        int year = currentCa.get(Calendar.YEAR);
        int month = currentCa.get(Calendar.MONTH) + 1;
        int day = currentCa.get(Calendar.DATE);
        String userId = MyApplication.getInstance().getUserId();
        long  trafficstats = dbAdapter.calculateForMonth(year,month,1);
        if(!TextUtils.isEmpty(userId)){
            saveDatas(trafficstats);
        }
		handler.post(thread);
        myConnectReceiver = new MyConnectReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(myConnectReceiver, filter);
		super.onCreate();
	}

	private void saveDatas(long trafficstats) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("q", String.valueOf(trafficstats));
		getServer(MyApplication.getInstance().getIP() + Constant.SEND_DATAS,
				map);
	}

	public void getServer(String url, Map<String, String> map) {
		if (map == null)
			map = new HashMap<String, String>();
		String random = CommonTool.getRandom();
		String userId = MyApplication.getInstance().getUserId();
		map.put("random", random);
		String md5Sign = MD5Tool.Md5(MD5Tool.Md5(Constant.KEY + random)
				+ userId);
		map.put("signature", md5Sign);
		map.put("id", userId);
		RequestParams params = new RequestParams(map);
		HttpTool.post(url, params, new AsyncHttpResponseHandler() {
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				String res = new String(arg2);
				System.out.println(res);
				processResult(res);
			}

			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
			}
		});
	}

	protected void processResult(String res) {
		if (res == null || "".equals(res)) {
			return;
		}
		JSONObject object = CommonTool.parseFromJson(res);
		String status = CommonTool.getJsonString(object, "success");
	}



    Runnable thread = new Runnable() {

        public void run() {

            dbAdapter = new DatabaseAdapter(MonitoringService.this);
            dbAdapter.open();

            mobileRx = TrafficStats.getUidRxBytes(info.uid);
            mobileTx = TrafficStats.getUidTxBytes(info.uid);

            if (mobileRx == -1 && mobileTx == -1) {
                in.putExtra("mobileRx", "No");
                in.putExtra("mobileTx", "No");
            } else {
                mrx = (mobileRx - old_mobileRx);
                old_mobileRx = mobileRx;
                mtx = (mobileTx - old_mobileTx);
                old_mobileTx = mobileTx;
            }
            Date date = new Date();
            mobileRx_all += mrx;
            mobileTx_all += mtx;
            if (count == 12) {
                if (mobileTx_all != 0 || mobileRx_all != 0) {
                    Cursor checkMobile = dbAdapter.check(1, date);
                    if(!UnIntent.isWifiConnected(MonitoringService.this)){
                    if (checkMobile.moveToNext()) {
                        long up = dbAdapter.getProFlowUp(1, date);
                        long dw = dbAdapter.getProFlowDw(1, date);
                        mobileTx_all += up;
                        mobileRx_all += dw;
                        dbAdapter.updateData(mobileTx_all, mobileRx_all, 1,
                                date);
                    }else{
                        dbAdapter.insertData(mobileTx_all, mobileRx_all, 1,
                                date);
                    }
                    }
                }
                mobileTx_all = 0;
                mobileRx_all = 0;
                count = 1;
            }
            count++;
            dbAdapter.close();
            handler.postDelayed(thread, 500);
        }

    };

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		handler.post(thread);
		return super.onStartCommand(intent, flags, startId);
	}

	public static long monitoringEachApplicationReceive(int uid) {
		long receive = TrafficStats.getUidRxBytes(uid);
		if (receive == -1)
			receive = 0;
		return receive;
	}

	public static long monitoringEachApplicationSend(int uid) {
		long send = TrafficStats.getUidRxBytes(uid);
		if (send == -1)
			send = 0;
		return send;
	}

	public int getNetType() {
		if (nwi != null) {
			String net = nwi.getTypeName();
			if (net.equals("WIFI")) {
				return 0;
			} else {
				return 1;
			}
		} else {
			return -1;
		}
	}


    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 88:
                    ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                    NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (mobileInfo.isConnected() || wifiInfo.isConnected()) {
                        UploadTool uploadTool = new UploadTool(MonitoringService.this);
                        uploadTool.upload();
                    }
                    break;
            }
        }
    };
    MyConnectReceiver myConnectReceiver;
    class MyConnectReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
                mHandler.sendEmptyMessage(88);
        }
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(thread);
        unregisterReceiver(myConnectReceiver);
		Log.v("CountService", "on destroy");
	}
}
