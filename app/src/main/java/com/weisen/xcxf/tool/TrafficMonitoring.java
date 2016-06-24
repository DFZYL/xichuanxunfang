package com.weisen.xcxf.tool;

import java.math.BigDecimal;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;

public class TrafficMonitoring {
	Context context;
	ConnectivityManager cm;
	NetworkInfo nwi;
	long lastTraffic = 0;
	long currentTraffic;

	public TrafficMonitoring() {
	}

	public TrafficMonitoring(Context context) {
		this.context = context;
		cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		nwi = cm.getActiveNetworkInfo();
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

	public static long traffic_Monitoring() {
		long recive_Total = TrafficStats.getTotalRxBytes();
		long send_Total = TrafficStats.getTotalTxBytes();
		long total = recive_Total + send_Total;
		return total;
	}

	public static long mReceive() {
		return TrafficStats.getMobileRxBytes();
	}

	public static long mSend() {
		return TrafficStats.getMobileTxBytes();
	}

	public static long wSend() {
		return TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();
	}

	public static long wReceive() {
		return TrafficStats.getTotalTxBytes() - TrafficStats.getMobileRxBytes();
	}

	public static long monitoringEachApplicationReceive(int uid) {
		return TrafficStats.getUidRxBytes(uid);
	}

	public static long monitoringEachApplicationSend(int uid) {
		return TrafficStats.getUidTxBytes(uid);
	}

	public static String convertTraffic(long traffic) {
		BigDecimal trafficKB;
		BigDecimal trafficMB;
		BigDecimal trafficGB;
		BigDecimal temp = new BigDecimal(traffic);
		BigDecimal divide = new BigDecimal(1000);
		trafficKB = temp.divide(divide, 2, 1);
		if (trafficKB.compareTo(divide) > 0) {
			trafficMB = trafficKB.divide(divide, 2, 1);
			if (trafficMB.compareTo(divide) > 0) {
				trafficGB = trafficMB.divide(divide, 2, 1);
				return trafficGB.doubleValue() + "GB";
			} else {
				return trafficMB.doubleValue() + "MB";
			}
		} else {
			return trafficKB.doubleValue() + "KB";
		}
	}
}
