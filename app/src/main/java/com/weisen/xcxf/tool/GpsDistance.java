package com.weisen.xcxf.tool;

import android.location.Location;

public class GpsDistance {
	//GPS计算两点不同经纬度的方法getdistance函数：
	public static double GetDistance(double lat1, double lng1, double lat2, double lng2) {
		float[] results = new float[1];
		Location.distanceBetween(lat1, lng1, lat2, lng2, results); // 系统自带函数
		return results[0]; // 返回距离
	}
}
