package com.weisen.xcxf.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.activity.LoginActivity;
import com.weisen.xcxf.activity.MainActivity;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.GPSLocationDatas;
import com.weisen.xcxf.bean.LocationDatas;
import com.weisen.xcxf.bean.MyLocation;
import com.weisen.xcxf.bean.MyLocationDao;
import com.weisen.xcxf.tool.CommonTool;
import com.weisen.xcxf.tool.FileUtils;
import com.weisen.xcxf.tool.HttpTool;
import com.weisen.xcxf.tool.MD5Tool;
import com.weisen.xcxf.tool.UnIntent;
import com.weisen.xcxf.tool.UploadTool;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MyLocationService2 extends Service {
    private final double EARTH_RADIUS = 6378137.0;
    private static final String TAG = "LocationService";
    public Timer mTimer = new Timer();// 定时器
    private LocationManager lm;
    private Location location;
    private LocationMode tempMode = LocationMode.FOLLOWING;
    private LocationClient locationClient;
    public long periodTime;
    public String userId, beginTime, endTime;
    public double latitude, longitude, altitude, speed, bearing, accuracy;
    public double oldLatitude, oldLongitude;
    public String address = "", time = "", locType = "1";

    public MyLocationDao locationDao;
    private int locationId;
    private SharedPreferences preferences;
    private String bestProvider;
    private int i = 0;
    private MyBroadcastReceiver receiver;
    private boolean mReflectFlg = false, isRun = false;
    private static final int NOTIFICATION_ID = 1; // 如果id设置为0,会导致不能设置为前台service
    private static final Class<?>[] mSetForegroundSignature = new Class[]{boolean.class};
    private static final Class<?>[] mStartForegroundSignature = new Class[]{
            int.class, Notification.class};
    private static final Class<?>[] mStopForegroundSignature = new Class[]{boolean.class};

    private NotificationManager mNM;
    private Method mSetForeground;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mSetForegroundArgs = new Object[1];
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];

    private WakeLock wakeLock;

    private LocationDatas bdLocation_datas = new LocationDatas();
    private GPSLocationDatas gpsLocation_datas = new GPSLocationDatas();
    private MyLocation oldLocation;

    @Override
    public IBinder onBind(Intent arg0) {

        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {

        super.onStart(intent, startId);
        acquireWakeLock();
    }

    // 开启电源锁
    private void acquireWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, getClass()
                    .getCanonicalName());
            if (null != wakeLock) {
                wakeLock.acquire();
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        receiver = new MyBroadcastReceiver();
        registerReceiver(receiver, filter);

        locationDao = new MyLocationDao(this);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getCriteria();
        // 为获取地理位置信息时设置查询条件
        // bestProvider = lm.getBestProvider(getCriteria(), true);
        // // 获取位置信息
        // //如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER
        // location = lm.getLastKnownLocation(bestProvider);

        locationClient = new LocationClient(this);
        MyLocationListener listener = new MyLocationListener();
        locationClient.registerLocationListener(listener);

    }

    @SuppressLint("NewApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preferences = getSharedPreferences(Constant.APP_SP, MODE_MULTI_PROCESS);
        periodTime = Long.parseLong(preferences.getString(Constant.SP_PERIOD,
                (Constant.DEFAULT_PERIOD + "")));
        beginTime = preferences.getString(Constant.SP_BEGIN,
                Constant.DEFAULT_BEGIN);
        userId = preferences.getString(Constant.SP_USERID, "");
        endTime = preferences.getString(Constant.SP_END, Constant.DEFAULT_END);
        if (beginTime == null || beginTime.equals(""))
            beginTime = Constant.DEFAULT_BEGIN;
        if (endTime == null || endTime.equals(""))
            endTime = Constant.DEFAULT_END;
        mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            mStartForeground = MyLocationService2.class.getMethod(
                    "startForeground", mStartForegroundSignature);
            mStopForeground = MyLocationService2.class.getMethod(
                    "stopForeground", mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            mStartForeground = mStopForeground = null;
        }

        try {
            mSetForeground = getClass().getMethod("setForeground",
                    mSetForegroundSignature);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "操作系统没有startForeground或者setForeground服务!");
        }

        Notification.Builder builder = new Notification.Builder(this);
        Intent clickIntent = new Intent();
        if (userId.equals(""))
            clickIntent.setClass(this, LoginActivity.class);
        else
            clickIntent.setClass(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                clickIntent, 0);
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.drawable.icon3);
        builder.setTicker("智能巡护");
        builder.setContentTitle("智能巡护");
        builder.setContentText("正在运行智能巡护");
        Notification notification = builder.build();
        startForegroundCompat(NOTIFICATION_ID, notification);
        if (userId.equals("")) {
            if (mTimer != null) {
                mTimer.cancel();// 退出之前的mTimer
            }
            Intent stop = new Intent(this, MyLocationService2.class);
            stopService(stop);
        } else {
            if (intent != null) {
                isRun = intent.getBooleanExtra("isRun", false);
            }
            if (!isRun) {
                if (mTimer != null) {
                    mTimer.cancel();// 退出之前的mTimer
                }
                if (periodTime != 0) {
                    mTimer = new Timer();// new一个Timer,否则会报错
                    timerTask();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void timerTask() {
        // 创建定时线程执行更新任务
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(200);
            }
        }, 0, periodTime);
    }

    Handler handler = new Handler() {

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
        }

        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            switch (msg.what) {
                case 200:
                    getLocation();
                    break;
                case 99:
                    if (location==null){
                        InitLocation();
                        locationClient.start();
                        Intent i = new Intent(Constant.UPDATEUPDATELOCATIONTYPE);
                        i.putExtra("date", "百度获取位置中...");
                        sendBroadcast(i);
                    }else{
                        saveLocation();
                    }
                    break;
            }
        }
    };


    // 位置监听
    private LocationListener gpslocationListener = new LocationListener() {
        /**
         * 位置信息变化时触发
         */
        public void onLocationChanged(Location gpsLocation) {
            accuracy = (int) gpsLocation.getAccuracy();
            location = gpsLocation;
            System.out.println("Gps定位回调");
            //lm.removeUpdates(gpslocationListener);
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            System.out.println("sourceGPS" + latitude + ":" + longitude);
            LatLng latLng = convertGPSToBaidu(new LatLng(latitude, longitude));
            latitude = latLng.latitude;
            longitude = latLng.longitude;
            DecimalFormat d = new DecimalFormat("#.000000");
            latitude = Double.valueOf(d.format(latitude));
            longitude = Double.valueOf(d.format(longitude));
            System.out.println("convertGPS" + latitude + ":" + longitude);
            altitude = gpsLocation.getAltitude();
            speed = gpsLocation.getSpeed();
            bearing = gpsLocation.getBearing();
            locType = "1";
            if (gpsLocation.getTime() != 0) {
                time = CommonTool.getStringDate(gpsLocation.getTime() / 1000,
                        "yyyy-MM-dd HH:mm:ss");
            } else
                time = CommonTool.getStringDate(new Date(),
                        "yyyy-MM-dd HH:mm:ss");

            Intent i = new Intent(Constant.GETLOACTIONACTION);
            //发送广播 在巡防记录哪里显示
            i.putExtra("from", "GPS获取到数据:");
            i.putExtra("location", "纬度:" + latitude + "经度:" + longitude + "时间:" + time + "精度:" + accuracy);
            sendBroadcast(i);
//            oldLocation.setLatitude("" + latitude);
//            oldLocation.setLongitude("" + longitude);
//            oldLocation.setTime(time);
            address = getAddressbyGeoPoint(gpsLocation.getLatitude(),
                    gpsLocation.getLongitude());
            String[] latTmp = String.valueOf(latitude).split("\\.");
            String[] lotTmp = String.valueOf(longitude).split("\\.");
            if (latTmp[1].length() >= 6 && lotTmp[1].length() >= 6) {
                MyApplication.getInstance().latitude = latitude;
                MyApplication.getInstance().longitude = longitude;
                MyApplication.getInstance().altitude = altitude;
                MyApplication.getInstance().address = address;
                MyApplication.getInstance().speed = speed;
                MyApplication.getInstance().bearing = bearing;
                MyApplication.getInstance().accurary = accuracy;
                MyApplication.getInstance().time = time;
                MyApplication.getInstance().uLocType = locType;
            } else {
                return;
            }

            System.out.println("GPS:"
                    + CommonTool.getStringDate(new Date(), "HH:mm:ss")
                    + ";latitude:" + latitude + ";longitude:" + longitude
                    + ";altitude:" + altitude + ";address:" + address + "accuracy" + accuracy
                    + ";time:" + time);
        }

        /**
         * GPS状态变化时触发
         */
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                // GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    Log.i(TAG, "当前GPS状态为可见状态");
                    Toast.makeText(MyLocationService2.this, "当前GPS状态为可见状态", 0).show();
                    break;
                // GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i(TAG, "当前GPS状态为服务区外状态");
                    Toast.makeText(MyLocationService2.this, "当前GPS状态为服务区外状态", 0).show();
                    break;
                // GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.i(TAG, "当前GPS状态为暂停服务状态");
                    break;
            }
        }

        /**
         * GPS开启时触发
         */
        public void onProviderEnabled(String provider) {
            // location = lm.getLastKnownLocation(bestProvider);
            if (location == null) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10,
                        gpslocationListener);
            }
        }

        /**
         * GPS禁用时触发
         */
        public void onProviderDisabled(String provider) {
            location = null;
        }
    };

    // 位置监听
//	private LocationListener networkLocationListener = new LocationListener() {
//
//		/**
//		 * 位置信息变化时触发
//		 */
//		public void onLocationChanged(Location netLocation) {
//			location = netLocation;
//			lm.removeUpdates(networkLocationListener);
//			latitude = netLocation.getLatitude();
//			longitude = netLocation.getLongitude();
//			altitude = netLocation.getAltitude();
//			speed = netLocation.getSpeed();
//			bearing = netLocation.getBearing();
//			accuracy = netLocation.getAccuracy();
//			locType = "1";
//			if (netLocation.getTime() != 0) {
//				time = CommonTool.getStringDate(netLocation.getTime() / 1000,
//						"yyyy-MM-dd HH:mm:ss");
//			} else
//				time = CommonTool.getStringDate(new Date(),
//						"yyyy-MM-dd HH:mm:ss");
//			address = getAddressbyGeoPoint(netLocation.getLatitude(),
//					netLocation.getLongitude());
//			MyApplication.getInstance().latitude = latitude;
//			MyApplication.getInstance().longitude = longitude;
//			MyApplication.getInstance().altitude = altitude;
//			MyApplication.getInstance().address = address;
//			MyApplication.getInstance().speed = speed;
//			MyApplication.getInstance().bearing = bearing;
//			MyApplication.getInstance().accurary = accuracy;
//			MyApplication.getInstance().time = time;
//			MyApplication.getInstance().uLocType = locType;
//
//			System.out.println("network:"
//					+ CommonTool.getStringDate(new Date(), "HH:mm:ss")
//					+ ";latitude:" + latitude + ";longitude:" + longitude
//					+ ";altitude:" + altitude + ";address:" + address
//					+ ";time:" + time);
//		}
//
//		/**
//		 * GPS状态变化时触发
//		 */
//		public void onStatusChanged(String provider, int status, Bundle extras) {
//			switch (status) {
//			// GPS状态为可见时
//			case LocationProvider.AVAILABLE:
//				break;
//			// GPS状态为服务区外时
//			case LocationProvider.OUT_OF_SERVICE:
//				Log.i(TAG, "当前GPS状态为服务区外状态");
//				break;
//			// GPS状态为暂停服务时
//			case LocationProvider.TEMPORARILY_UNAVAILABLE:
//				Log.i(TAG, "当前GPS状态为暂停服务状态");
//				break;
//			}
//		}
//
//		/**
//		 * GPS开启时触发
//		 */
//		public void onProviderEnabled(String provider) {
//			// location = lm.getLastKnownLocation(bestProvider);
//			if (location == null) {
//				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
//						0, networkLocationListener);
//			}
//		}
//
//		/**
//		 * GPS禁用时触发
//		 */
//		public void onProviderDisabled(String provider) {
//			location = null;
//		}
//
//	};

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

    // gps获取地址信息
    private String getAddressbyGeoPoint(double latitude, double longitude) {
        List<Address> result = null;
        String address = "";
        // 先将Location转换为GeoPoint
        // GeoPoint gp=getGeoByLocation(location);
        try {
            if (latitude != 0.0) {
                // 获取Geocoder，通过Geocoder就可以拿到地址信息
                Geocoder gc = new Geocoder(this, Locale.getDefault());
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
        option.setOpenGps(true);// 打开GPRS
        option.setScanSpan(Integer.MAX_VALUE);// 设置定位请求时间
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度，默认值gcj02
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        locationClient.setLocOption(option);
    }

    /**
     * 实现定位回调监听
     */
    public class MyLocationListener implements BDLocationListener {

        public void onReceiveLocation(BDLocation bd) {
            if (bd != null && bd.hasAddr()) {
                System.out.println("进入百度回调");
                locationClient.stop();
                latitude = bd.getLatitude();
                longitude = bd.getLongitude();
                address = bd.getAddrStr();
                speed = bd.getSpeed();
                locType = "0";
//				if (address == null || address.equals("")
//						|| address.equals("null")) {
//					address = getAddressbyGeoPoint(bd.getLatitude(),
//							bd.getLongitude());
//				}
                if (bd.getTime() != null) {
                    time = CommonTool.getStringDate(bd.getTime(),
                            "yyyy-M-d HH:mm:ss", "yyyy-MM-dd HH:mm:ss");
                }

                Intent i = new Intent(Constant.GETLOACTIONACTION);
                //发送广播 在巡防记录哪里显示
                i.putExtra("from", "百度获取到数据:");
                i.putExtra("location", "纬度:" + latitude + "经度:" + longitude + "时间:" + time + "地址:" + address);
                sendBroadcast(i);


//                oldLocation.setLatitude(""+latitude);
//                oldLocation.setLongitude(""+longitude);
//                oldLocation.setTime(time);

                String[] latTmp = String.valueOf(latitude).split("\\.");
                String[] lotTmp = String.valueOf(longitude).split("\\.");
                System.out.println("baidu:"
                        + CommonTool.getStringDate(new Date(), "HH:mm:ss")
                        + ";latitude:" + latitude + ";longitude:" + longitude
                        + ";altitude:" + altitude + ";address:" + address
                        + ";time:" + time);
                saveLocation();
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Intent intent = new Intent(Constant.UPDATEUPDATELOCATIONTYPE);
                    intent.putExtra("date", "GPS获取位置中...");
                    sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(Constant.UPDATEUPDATELOCATIONTYPE);
                    intent.putExtra("date", "百度获取位置中...");
                    sendBroadcast(intent);
                }
                if (latTmp[1].length() >= 6 && lotTmp[1].length() >= 6) {
                    MyApplication.getInstance().latitude = latitude;
                    MyApplication.getInstance().longitude = longitude;
                    MyApplication.getInstance().address = address;
                    MyApplication.getInstance().time = time;
                    MyApplication.getInstance().uLocType = "0";
                    MyApplication.getInstance().speed = speed;
                } else {
                    return;
                }
            }
        }
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

    public File getLogPath() {
        String path = FileUtils.getRootFilePath() + "log";
        File file = new File(path);
        if (!file.exists()) {
            File parentFile = new File(file.getParent());
            if (!parentFile.exists())
                parentFile.mkdir();
            file.mkdir();
        }
        return file;
    }

    public void getLocation() {
        initLation();//开启定位操作...
        Log.i("location", "latitude:" + latitude + "longitude" + longitude);

//        Log.i("时速",speed+">>>>>>");
//        Toast.makeText(this,speed+"<<<<<",0).show();
//        if(i>1&&(int) (speed * 3.6 * 100) / 100.0>150){
//            Log.e(TAG,"非正常数据");
//            return;
//        }
        //  System.out.println("old" + oldLatitude+":"+oldLongitude);
        //  double distace = DistanceUtil.getDistance(new LatLng(oldLatitude,oldLongitude),new LatLng(latitude,longitude));
//        System.out.println("距离" + (int) distace);
//        Toast.makeText(this,distace+"米",0).show();
//        if(i>1 && (int)distace<100){
//            System.out.println("距离"+ "小于100米");
//            return;
//        }

//        Double aDouble = Double.valueOf(oldLocation.getLatitude());
//        Double aDouble1 = Double.valueOf(oldLocation.getLongitude());
//        if (aDouble==latitude && aDouble1==longitude)
//        {
//            System.out.println("两次经纬度一样");
//            return;
//        }
//        Double time1 = 0.0;
//        try {
//            long oldTime = CommonTool.stringToLong(oldLocation.getTime(),"yyyy-MM-dd HH:mm:ss");
//            long newTime = CommonTool.stringToLong(time,"yyyy-MM-dd HH:mm:ss");
//            System.out.println("old" + oldTime + "new" + newTime);
//            long counttime = newTime-oldTime;
//            time1 = Double.valueOf(String.valueOf(counttime/1000));
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        double distance = DistanceUtil.getDistance(new LatLng(latitude, longitude), new LatLng(aDouble, aDouble1));
//        if(distance/time1>100){
//            System.out.println("飘逸数据");
//            return;
//        }


        if (i > Integer.MAX_VALUE)
            i = 1;
        if (i > 0) {
            // 保存数据
            String[] latTmp = String.valueOf(latitude).split("\\.");
            String[] lotTmp = String.valueOf(longitude).split("\\.");
            if (latTmp[1].length() >= 6 && lotTmp[1].length() >= 6) {
                //saveLocation();
            }
        }

    }

    private void initLation() {
        Intent i = new Intent(Constant.UPDATEUPDATELOCATIONTYPE);
//        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
//                && UnIntent.isNetworkAvailable1(MyLocationService.this)) {
//            // if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10,
//                    gpslocationListener);
//            // }
//            System.out.println(location == null);
//            if (location == null) {
//                        InitLocation();
//                        locationClient.start();
//                        i.putExtra("date", "百度获取位置中...");
//                    }else{
//                         location =null;
//                         i.putExtra("date", "GPS获取位置中...");
//            }
//        } else {
//            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
//                    && !UnIntent.isNetworkAvailable1(MyLocationService.this) ) {
//                location = null;
//                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10,
//                        gpslocationListener);
//                    i.putExtra("date", "GPS获取位置中...");
//            }
//            if (UnIntent.isNetworkAvailable1(MyLocationService.this)
//                    && !lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                            InitLocation();
//                            locationClient.start();
//                            i.putExtra("date", "百度获取位置中...");
//            }
//        }

        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            i.putExtra("date", "GPS获取位置中...");
            if (UnIntent.isNetworkAvailable1(MyLocationService2.this)) {
                location = null;
                handler.sendEmptyMessageDelayed(99, 20000);
            }
        } else {
            if (UnIntent.isNetworkAvailable1(MyLocationService2.this)) {
                InitLocation();
                locationClient.start();
                i.putExtra("date", "百度获取位置中...");
            } else {
                i.putExtra("date", "目前不能定位...");
            }
        }
        sendBroadcast(i);
    }

    public void saveLocation() {
        if (userId != null && userId.length() == 32) {
            String[] latTmp = String.valueOf(latitude).split("\\.");
            String[] lotTmp = String.valueOf(longitude).split("\\.");
            if (latTmp[1].length() >= 6 && lotTmp[1].length() >= 6) {
                i++;
            }else{
                System.out.println("数据不符合要求");
                return;
            }
            Date cur = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(cur);
            Calendar beginCalendar = Calendar.getInstance();
            beginCalendar.setTime(cur);
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(cur);
            int beginHour = Integer.parseInt(CommonTool.getStringDate(
                    beginTime, "HH:mm", "HH"));
            int beginMinu = Integer.parseInt(CommonTool.getStringDate(
                    beginTime, "HH:mm", "mm"));
            int endHour = Integer.parseInt(CommonTool.getStringDate(endTime,
                    "HH:mm", "HH"));
            int endMinu = Integer.parseInt(CommonTool.getStringDate(endTime,
                    "HH:mm", "mm"));
            beginCalendar.set(Calendar.HOUR_OF_DAY, beginHour);
            beginCalendar.set(Calendar.MINUTE, beginMinu);
            endCalendar.set(Calendar.HOUR_OF_DAY, endHour);
            endCalendar.set(Calendar.MINUTE, endMinu);
            Date beginDate = beginCalendar.getTime();
            Date endDate = endCalendar.getTime();
            if (cur.after(beginDate) && cur.before(endDate)) {
                if (time == null || time.equals(""))
                    time = CommonTool.getStringDate(cur, "yyyy-MM-dd HH:mm:ss");
                String battery = MyApplication.getInstance().battery + "";
                MyLocation mlocation = new MyLocation();
                mlocation.setUid(userId);
                mlocation.setLatitude(latitude + "");
                mlocation.setLongitude(longitude + "");
                mlocation.setAltitude(altitude + "");
                mlocation.setAddress(address);
                mlocation.setSpeed(CommonTool.formatFloat(speed));
                mlocation.setBearing(CommonTool.formatFloat(bearing));
                mlocation.setAccurary(CommonTool.formatFloat(accuracy));
                mlocation.setBattery(battery);
                mlocation.setLocType(locType);
                mlocation.setTime(time);
                locationId = locationDao.addLocation(mlocation);
                Log.i("上传次数", ":" + i);
                // 上传数据
                Map<String, String> map = new HashMap<String, String>();
                map.put("uLoti", longitude + "");
                map.put("uLati", latitude + "");
                map.put("uAlti", altitude + "");
                map.put("uAddr", "");
                map.put("uSpeed", CommonTool.formatFloat(speed));
                map.put("uDirection", CommonTool.formatFloat(bearing));
                map.put("uAccuracy", CommonTool.formatFloat(accuracy));
                map.put("battery", battery);
                map.put("uTime", time);
                map.put("uLocType", MyApplication.getInstance().uLocType);
                getServer(MyApplication.getInstance().getIP()
                        + Constant.UPLOAD_TRAIL, map);
                // // 过滤经纬度小于小数点6位的数据
                // if (String.valueOf(latitude).length() >= 9 &&
                // String.valueOf(longitude).length() >= 10) {
                // // 过滤GPS数据，距离/时间
                // if (MyApplication.getInstance().uLocType.equals("1")) {
                // if (gpsLocation_datas.getLat()==0 &&
                // gpsLocation_datas.getLng()==0) {
                // gpsLocation_datas.setTime(time);
                // gpsLocation_datas.setLat(latitude);
                // gpsLocation_datas.setLng(longitude);
                // locationId = locationDao.addLocation(location);//插入本地数据库
                // getServer(MyApplication.getInstance().getIP() +
                // Constant.UPLOAD_TRAIL, map);//上传数据
                // }else{
                // long oldtime = 0;
                // long newtime = 0;
                // try {//转化时间格式
                // oldtime =
                // CommonTool.stringToLong(gpsLocation_datas.getTime(),
                // "yyyy-MM-dd HH:mm:ss");
                // newtime = CommonTool.stringToLong(time,
                // "yyyy-MM-dd HH:mm:ss");
                // } catch (ParseException e) {
                // e.printStackTrace();
                // }
                // long counttime = newtime-oldtime;
                // Double time1 =
                // Double.valueOf(String.valueOf(counttime/1000));
                // //gps计算两点间距离
                // Double gpsDistance =
                // GpsDistance.GetDistance(gpsLocation_datas.getLat(),
                // gpsLocation_datas.getLng(), latitude, longitude);
                // if (gpsDistance/time1 > 0.5 && gpsDistance/time1 < 100) {
                // locationId = locationDao.addLocation(location);
                // gpsLocation_datas.setTime(time);
                // gpsLocation_datas.setLat(latitude);
                // gpsLocation_datas.setLng(longitude);
                // getServer(MyApplication.getInstance().getIP() +
                // Constant.UPLOAD_TRAIL, map);
                //
                // // }
                // }
                // }
                // }else{//过滤百度数据，距离/时间
                // // if (MyApplication.getInstance().uLocType.equals("0"))
                // {
                // if (bdLocation_datas.getMyLat()== 0 &&
                // bdLocation_datas.getMyLng() == 0) {
                // bdLocation_datas.setMyLat(latitude);
                // bdLocation_datas.setMyLng(longitude);
                // bdLocation_datas.setMyTime(time);
                // locationId = locationDao.addLocation(location);
                // getServer(MyApplication.getInstance().getIP() +
                // Constant.UPLOAD_TRAIL, map);
                // }else{
                // LatLng latlng1 = new LatLng(bdLocation_datas.getMyLat(),
                // bdLocation_datas.getMyLng());
                // LatLng latlng2 = new LatLng(latitude, longitude);
                // //百度计算两点间距离
                // double str = DistanceUtil.getDistance(latlng1, latlng2);
                // long oldtime = 0;
                // long newtime = 0;
                // try {
                // oldtime =
                // CommonTool.stringToLong(bdLocation_datas.getMyTime(),
                // "yyyy-MM-dd HH:mm:ss");
                // newtime = CommonTool.stringToLong(time,
                // "yyyy-MM-dd HH:mm:ss");
                // } catch (ParseException e) {
                // e.printStackTrace();
                // }
                // long counttime = newtime-oldtime;
                // Double time1 =
                // Double.valueOf(String.valueOf(counttime/1000+""));
                // if (str/time1>0.5 && str/time1<100) {
                // locationId = locationDao.addLocation(location);
                // bdLocation_datas.setMyLat(latitude);
                // bdLocation_datas.setMyLng(longitude);
                // bdLocation_datas.setMyTime(time);
                // getServer(MyApplication.getInstance().getIP() +
                // Constant.UPLOAD_TRAIL, map);
                //
                // // }
                // }
                // }
                // }
                // }
                time = "";// zh 10.9
                latitude = 0.0;
                longitude = 0.0;
                altitude = 0.0;
                address = "";
                speed = 0.0;
                bearing = 0.0;
                accuracy = 0.0;
                locType = "";
            }
        }
    }


    // 请求服务器
    public void getServer(String url, Map<String, String> map) {
        if (map == null)
            map = new HashMap<String, String>();
        String random = CommonTool.getRandom();
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
                Log.i("上传位置失败", new String(arg2));
            }
        });
    }

    /**
     * 服务器返回数据
     *
     * @param res
     */
    protected void processResult(String res) {
        if (res == null || "".equals(res)) {
            return;
        }
        JSONObject object = CommonTool.parseFromJson(res);
        String status = CommonTool.getJsonString(object, "success");
        if (status == null || !status.equals("true")) {
            return;
        } else {
            locationDao.updateLocation(locationId);
            Intent intent = new Intent();
            intent.setAction(Constant.BROADCAST_UPDATE_LOCATION);
            sendBroadcast(intent);
            UploadTool uploadTool = new UploadTool(this);
            uploadTool.upload();
        }
    }

    void invokeMethod(Method method, Object[] args) {
        try {
            method.invoke(this, args);
        } catch (InvocationTargetException e) {
            // Should not happen.
            Log.w("ApiDemos", "Unable to invoke method", e);
        } catch (IllegalAccessException e) {
            // Should not happen.
            Log.w("ApiDemos", "Unable to invoke method", e);
        }
    }

    /**
     * 这是围绕新startForeground方法的包装，使用旧的 如果不是可用的API。
     */
    void startForegroundCompat(int id, Notification notification) {
        if (mReflectFlg) {
            // If we have the new startForeground API, then use it.
            if (mStartForeground != null) {
                mStartForegroundArgs[0] = Integer.valueOf(id);
                mStartForegroundArgs[1] = notification;
                invokeMethod(mStartForeground, mStartForegroundArgs);
                return;
            }

            // Fall back on the old API.
            mSetForegroundArgs[0] = Boolean.TRUE;
            invokeMethod(mSetForeground, mSetForegroundArgs);
            mNM.notify(id, notification);
        } else {
            /*
			 * 还可以使用以下方法，当sdk大于等于5时，调用sdk现有的方法startForeground设置前台运行，
			 * 否则调用反射取得的sdk level 5（对应Android 2.0）以下才有的旧方法setForeground设置前台运行
			 */

            if (VERSION.SDK_INT >= 5) {
                startForeground(id, notification);
            } else {
                // Fall back on the old API.
                mSetForegroundArgs[0] = Boolean.TRUE;
                invokeMethod(mSetForeground, mSetForegroundArgs);
                mNM.notify(id, notification);
            }
        }
    }

    /**
     * This is a wrapper around the new stopForeground method, using the older
     * APIs if it is not available.
     */
    void stopForegroundCompat(int id) {
        if (mReflectFlg) {
            // If we have the new stopForeground API, then use it.
            if (mStopForeground != null) {
                mStopForegroundArgs[0] = Boolean.TRUE;
                invokeMethod(mStopForeground, mStopForegroundArgs);
                return;
            }

            // Fall back on the old API. Note to cancel BEFORE changing the
            // foreground state, since we could be killed at that point.
            mNM.cancel(id);
            mSetForegroundArgs[0] = Boolean.FALSE;
            invokeMethod(mSetForeground, mSetForegroundArgs);
        } else {
			/*
			 * 还可以使用以下方法，当sdk大于等于5时，调用sdk现有的方法stopForeground停止前台运行， 否则调用反射取得的sdk
			 * level 5（对应Android 2.0）以下才有的旧方法setForeground停止前台运行
			 */

            if (VERSION.SDK_INT >= 5) {
                stopForeground(true);
            } else {
                // Fall back on the old API. Note to cancel BEFORE changing the
                // foreground state, since we could be killed at that point.
                mNM.cancel(id);
                mSetForegroundArgs[0] = Boolean.FALSE;
                invokeMethod(mSetForeground, mSetForegroundArgs);
            }
        }
    }

    // 广播启动定位服务
    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                if (!userId.equals("")) {
                    Intent service = new Intent(MyLocationService2.this,
                            MyLocationService2.class);
                    service.putExtra("isRun", true);
                    startService(service);
                } else {
                    Intent service = new Intent(MyLocationService2.this,
                            MyLocationService2.class);
                    stopService(service);
                }
            }
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        releaseWakeLock();
        userId = "";
        if (receiver != null)
            unregisterReceiver(receiver);
    }

    // 释放设备电源锁
    private void releaseWakeLock() {
        if (null != wakeLock && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }
}
