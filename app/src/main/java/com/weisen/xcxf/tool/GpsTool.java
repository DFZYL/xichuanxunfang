package com.weisen.xcxf.tool;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.weisen.xcxf.Constant;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.MyLocationDao;

public class GpsTool {

    private Context context;
    private LocationManager lm;
    private Location location;
    private LocationMode tempMode = LocationMode.Hight_Accuracy;
    private LocationClient locationClient;
    public double latitude, longitude, altitude, speed, bearing, accurary;
    public String address = "", time = "", locType = "1";
    public MyLocationDao locationDao;
    private String bestProvider;
    SharedPreferences preferences;

    public GpsTool(Context context) {
        // TODO Auto-generated constructor stub
        this.context = context;
        preferences = context.getSharedPreferences(Constant.APP_SP, context.MODE_PRIVATE);
    }

    public void getAddr() {
        lm = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        // 为获取地理位置信息时设置查询条件
        bestProvider = lm.getBestProvider(getCriteria(), true);
        // 获取位置信息
        // 如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER
        location = lm.getLastKnownLocation(bestProvider);
        locationClient = new LocationClient(context);
        MyLocationListener listener = new MyLocationListener();
        locationClient.registerLocationListener(listener);
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getCriteria();
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                    locationListener);
        }
        if (UnIntent.isNetworkAvailable(context)) {
            InitLocation();
            locationClient.start();
        }
//        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
//                    0, networkLocationListener);
//        }
//		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
//				|| lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//			if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//
//				if (location == null) {
//
//				}
//			}
//			if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
//						0, networkLocationListener);
//				if (location == null) {
//					InitLocation();
//					locationClient.start();
//				}
//			}
//		} else {
//			try {
//				new Thread(new Runnable() {
//
//					@Override
//					public void run() {
//						try {
//							Location cellLocation = UtilTool
//									.callGear(context);
//							if (cellLocation != null) {
//								latitude = cellLocation.getLatitude();
//								longitude = cellLocation.getLongitude();
//								locType = "0";
//								if (cellLocation.getTime() != 0) {
//									time = CommonTool.getStringDate(
//											cellLocation.getTime() / 1000,
//											"yyyy-MM-dd HH:mm:ss");
//								} else
//									time = CommonTool.getStringDate(new Date(),
//											"yyyy-MM-dd HH:mm:ss");
//								address = getAddressbyGeoPoint(
//										cellLocation.getLatitude(),
//										cellLocation.getLongitude());
//                                if (checkDataNormal(latitude,longitude)){
//                                    preferences.edit().putString(Constant.SP_LATITUDE ,latitude+"").commit();
//                                    preferences.edit().putString(Constant.SP_LONGITUDE,longitude+"").commit();
//                                    MyApplication.getInstance().altitude = altitude;
//                                    MyApplication.getInstance().longitude = longitude;
//                                }
//
//								MyApplication.getInstance().time = time;
//								MyApplication.getInstance().uLocType = "0";
//                                MyApplication.getInstance().address = address;
//							} else {
//								InitLocation();
//								locationClient.start();
//							}
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//				}).start();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
    }


    //将gps坐标转换为百度坐标
    public LatLng convertGPSToBaidu(LatLng sourceLatLng) {
        // 将GPS设备采集的原始GPS坐标转换成百度坐标
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        // sourceLatLng待转换坐标
        converter.coord(sourceLatLng);
        LatLng desLatLng = converter.convert();
        return desLatLng;
    }

    // 位置监听
    private LocationListener locationListener = new LocationListener() {
        /**
         * 位置信息变化时触发
         */
        public void onLocationChanged(Location gpsLocation) {
            lm.removeUpdates(locationListener);
            latitude = gpsLocation.getLatitude();
            longitude = gpsLocation.getLongitude();
            altitude = gpsLocation.getAltitude();
            speed = gpsLocation.getSpeed();
            bearing = gpsLocation.getBearing();
            accurary = gpsLocation.getAccuracy();
            locType = "1";
            LatLng latLng = convertGPSToBaidu(new LatLng(latitude, longitude));
            latitude = latLng.latitude;
            longitude = latLng.longitude;
            if (gpsLocation.getTime() != 0) {
                time = CommonTool.getStringDate(gpsLocation.getTime() / 1000,
                        "yyyy-MM-dd HH:mm:ss");
            } else
                time = CommonTool.getStringDate(new Date(),
                        "yyyy-MM-dd HH:mm:ss");
            address = getAddressbyGeoPoint(latitude,
                    longitude);

            MyApplication.getInstance().latitude = latitude;
            MyApplication.getInstance().longitude = longitude;
            preferences.edit().putString(Constant.SP_LATITUDE, latitude + "").commit();
            preferences.edit().putString(Constant.SP_LONGITUDE, longitude + "").commit();
            MyApplication.getInstance().address = address;
            MyApplication.getInstance().speed = speed;
            MyApplication.getInstance().bearing = bearing;
            MyApplication.getInstance().accurary = accurary;
            MyApplication.getInstance().time = time;
            MyApplication.getInstance().uLocType = "1";
            System.out.println("GPS:"
                    + CommonTool.getStringDate(new Date(), "HH:mm:ss")
                    + ";latitude:" + latitude + ";longitude:" + longitude
                    + ";altitude:" + altitude + ";address:" + address
                    + ";time:" + time);
        }

        /**
         * GPS状态变化时触发
         */
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                // GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    break;
                // GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    break;
                // GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    break;
            }
        }

        /**
         * GPS开启时触发
         */
        public void onProviderEnabled(String provider) {
            location = lm.getLastKnownLocation(bestProvider);
        }

        /**
         * GPS禁用时触发
         */
        public void onProviderDisabled(String provider) {
            location = null;
        }

    };

    private void showToast(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

//    // 位置监听
//    private LocationListener networkLocationListener = new LocationListener() {
//        /**
//         * 位置信息变化时触发
//         */
//        public void onLocationChanged(Location netLocation) {
//            lm.removeUpdates(networkLocationListener);
//            latitude = netLocation.getLatitude();
//            longitude = netLocation.getLongitude();
//            altitude = netLocation.getAltitude();
//            speed = netLocation.getSpeed();
//            bearing = netLocation.getBearing();
//            accurary = netLocation.getAccuracy();
//            locType = "1";
//            if (netLocation.getTime() != 0) {
//                time = CommonTool.getStringDate(netLocation.getTime() / 1000,
//                        "yyyy-MM-dd HH:mm:ss");
//            } else
//                time = CommonTool.getStringDate(new Date(),
//                        "yyyy-MM-dd HH:mm:ss");
//            LatLng latLng = convertGPSToBaidu(new LatLng(latitude, longitude));
//            latitude = latLng.latitude;
//            longitude = latLng.longitude;
//            address = getAddressbyGeoPoint(latitude,
//                    longitude);
//            if (checkDataNormal(latitude, longitude)) {
//                MyApplication.getInstance().latitude = latitude;
//                MyApplication.getInstance().longitude = longitude;
//                preferences.edit().putString(Constant.SP_LATITUDE, latitude + "").commit();
//                preferences.edit().putString(Constant.SP_LONGITUDE, longitude + "").commit();
//            }
//            MyApplication.getInstance().altitude = altitude;
//            MyApplication.getInstance().address = address;
//            MyApplication.getInstance().speed = speed;
//            MyApplication.getInstance().bearing = bearing;
//            MyApplication.getInstance().accurary = accurary;
//            MyApplication.getInstance().time = time;
//            MyApplication.getInstance().uLocType = "1";
//            System.out.println("network:"
//                    + CommonTool.getStringDate(new Date(), "HH:mm:ss")
//                    + ";latitude:" + latitude + ";longitude:" + longitude
//                    + ";altitude:" + altitude + ";address:" + address
//                    + ";time:" + time);
//        }
//
//        /**
//         * GPS状态变化时触发
//         */
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//            switch (status) {
//                // GPS状态为可见时
//                case LocationProvider.AVAILABLE:
//                    break;
//                // GPS状态为服务区外时
//                case LocationProvider.OUT_OF_SERVICE:
//                    break;
//                // GPS状态为暂停服务时
//                case LocationProvider.TEMPORARILY_UNAVAILABLE:
//                    break;
//            }
//        }
//
//        /**
//         * GPS开启时触发
//         */
//        public void onProviderEnabled(String provider) {
//            location = lm.getLastKnownLocation(bestProvider);
//        }
//
//        /**
//         * GPS禁用时触发
//         */
//        public void onProviderDisabled(String provider) {
//            location = null;
//        }
//
//    };

    /**
     * 返回查询条件
     *
     * @return
     */
    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 设置是否要求速度
        criteria.setSpeedRequired(true);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(true);
        // 设置是否需要方位信息
        criteria.setBearingRequired(true);
        // 设置是否需要海拔信息
        criteria.setAltitudeRequired(true);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

    // 获取地址信息
    private String getAddressbyGeoPoint(double latitude, double longitude) {
        List<Address> result = null;
        String address = "";
        // 先将Location转换为GeoPoint
        // GeoPoint gp=getGeoByLocation(location);
        try {
            if (latitude != 0) {
                // 获取Geocoder，通过Geocoder就可以拿到地址信息
                Geocoder gc = new Geocoder(context, Locale.getDefault());
                result = gc.getFromLocation(latitude, longitude, 1);
            }
            if (result != null && result.size() > 0) {
                Address addr = result.get(0);
                int line = addr.getMaxAddressLineIndex();
                if (line >= 0)
                    address = addr.getAddressLine(0);
                // for (int i = 0; i <= line; i++) {
                // address += addr.getAddressLine(i);
                // }
            }
        } catch (Exception e) {
        }
        return address;
    }

    private void InitLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(tempMode);// 设置定位模式
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度，默认值gcj02
        option.setOpenGps(true);
        option.setScanSpan(0);
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        option.disableCache(true);
        locationClient.setLocOption(option);
    }

    /**
     * 实现实位回调监听
     */

    public class MyLocationListener implements BDLocationListener {

        public void onReceiveLocation(BDLocation bd) {
            if (bd != null && bd.hasAddr()) {
                locationClient.stop();
                latitude = bd.getLatitude();
                longitude = bd.getLongitude();
                address = bd.getAddrStr();
                locType = "0";
                if (address == null || address.equals("")
                        || address.equals("null")) {
                    address = getAddressbyGeoPoint(bd.getLatitude(),
                            bd.getLongitude());
                }
                if (bd.getTime() != null) {
                    time = CommonTool.getStringDate(bd.getTime(),
                            "yyyy-M-d HH:mm:ss", "yyyy-MM-dd HH:mm:ss");
                }

                MyApplication.getInstance().latitude = latitude;
                MyApplication.getInstance().longitude = longitude;
                preferences.edit().putString(Constant.SP_LATITUDE, latitude + "").commit();
                preferences.edit().putString(Constant.SP_LONGITUDE, longitude + "").commit();
                MyApplication.getInstance().altitude = altitude;
                MyApplication.getInstance().time = time;
                MyApplication.getInstance().uLocType = "0";
                MyApplication.getInstance().address = address;
            }
        }
    }

    private boolean checkDataNormal(double latitude, double longitude) {
        String[] latTmp = String.valueOf(latitude).split("\\.");
        String[] lotTmp = String.valueOf(longitude).split("\\.");
        if (latTmp[1].length() >= 6 && lotTmp[1].length() >= 6) {
            return true;
        } else {
            return false;
        }
    }
}
