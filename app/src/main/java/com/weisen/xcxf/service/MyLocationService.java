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
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.DistanceUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.activity.LoginActivity;
import com.weisen.xcxf.activity.MainActivity;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.MyLocation;
import com.weisen.xcxf.bean.MyLocationDao;
import com.weisen.xcxf.tool.CommonTool;
import com.weisen.xcxf.tool.FileUtils;
import com.weisen.xcxf.tool.HttpTool;
import com.weisen.xcxf.tool.MD5Tool;
import com.weisen.xcxf.tool.UnIntent;
import com.weisen.xcxf.tool.UploadTool;
import com.weisen.xcxf.utils.IntenetUtil;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MyLocationService extends Service {
    private static final String TAG = "LocationService";
    private static long systemtime=0L;
    private int rate = 5;
    private int proDistance;
    private Boolean Haspic=false;
    private LocationManager lm;
    private Location location;
    private LocationClient locationClient;
    public int periodTime;
    public String userId, beginTime, endTime;
    public double latitude, longitude, altitude, speed, bearing, accuracy;
    public String address = "", time = "", locType = "1";
    public MyLocationDao locationDao;
    private int locationId;
    private SharedPreferences preferences;
    private static  int count = 0;
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
    private MyLocationListener locationListener;
    private String photopath;
    private SensorManager mSensorManager;// 传感器服务
    private StepDetector detector;// 传感器监听对象

    @Override
    public IBinder onBind(Intent arg0) {

        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Haspic=intent.getBooleanExtra("haspic",false);
        photopath=intent.getStringExtra("photopath");
        acquireWakeLock();
        ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mobileInfo.isConnected() || wifiInfo.isConnected()) {
            UploadTool uploadTool = new UploadTool(MyLocationService.this);
            uploadTool.upload();
            System.out.print("检查未上传开启上传");
     }


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



    BatteryReceiver batteryReceiver;
    @Override
    public void onCreate() {
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        super.onCreate();
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        receiver = new MyBroadcastReceiver();
        registerReceiver(receiver, filter);
        locationDao = new MyLocationDao(this);
        getCriteria();


        IntentFilter intentFilter = new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED);
        batteryReceiver = new BatteryReceiver();
        registerReceiver(batteryReceiver, intentFilter);


        detector = new StepDetector(this);
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        // 注册传感器，注册监听器
        mSensorManager.registerListener(detector,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    private int distance;
    @SuppressLint("NewApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Haspic=intent.getBooleanExtra("haspic",false);
        photopath=intent.getStringExtra("photopath");
        preferences = getSharedPreferences(Constant.APP_SP, MODE_MULTI_PROCESS);
        distance = preferences.getInt(Constant.DEFAULTDISTANCE,200);
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
            mStartForeground = MyLocationService.class.getMethod(
                    "startForeground", mStartForegroundSignature);
            mStopForeground = MyLocationService.class.getMethod(
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
        builder.setSmallIcon(R.drawable.icon4);
        builder.setTicker("淅川综治");
        builder.setContentTitle("淅川综治");
//        if(MyApplication.isWork)
            String status=MyApplication.getInstance().getIsWork()?"工作中":"休息中";

        long day = systemtime/(24*60);
       long  hour = (systemtime%(24*60))/60;
       long  minute = systemtime%60;
       if(day>0){

           builder.setContentText("运行:"+day+"天"+hour+"时"+minute+"分 "+status+"("+count+")");

       }else if(hour>0){
           builder.setContentText("运行:"+hour+"时"+minute+"分 "+status+"("+count+")");

       }else {

           builder.setContentText("运行:"+minute+"分 "+status+"("+count+")");

       }


        Notification notification = builder.build();
        startForegroundCompat(NOTIFICATION_ID, notification);
        if (userId.equals("")||distance==0) {
            Intent stop = new Intent(this, MyLocationService.class);
            stopService(stop);
        } else {
            if (intent != null) {
                isRun = intent.getBooleanExtra("isRun", false);
            }
        }
       // return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private AlarmReceiver alarmReceiver;

    public class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }


    private boolean isFristBaidu = true;
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
                    // getLocation();
                    break;
                case 99:
                    if (location == null) {
                        Intent i = new Intent(Constant.UPDATEUPDATELOCATIONTYPE);
                        i.putExtra("date", "百度获取位置中.....");
                        i.putExtra("count", distance);
                        i.putExtra("isMoved", "状态:移动");
                        preferences.edit().putString(Constant.SP_PERIOD, periodTime + "").commit();
                        sendBroadcast(i);
                        startLocationClient();
                    } else {
                        preferences.edit().putString(Constant.SP_PERIOD, periodTime + "").commit();
                    }
                    break;
                case 88:
                    break;
            }
        }
    };

    //gps状态发生改变
    GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
                //第一次定位
            } else if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                //卫星状态改变
                GpsStatus gpsStauts = lm.getGpsStatus(null); // 取当前状态
                int maxSatellites = gpsStauts.getMaxSatellites(); //获取卫星颗数的默认最大值
                Iterator<GpsSatellite> it = gpsStauts.getSatellites().iterator();//创建一个迭代器保存所有卫星
                int count = 0;
                while (it.hasNext() && count <= maxSatellites) {
                    count++;
                    GpsSatellite s = it.next();
                }
                //  System.out.println("搜索到："+count+"颗卫星");
                // Toast.makeText(MyLocationService.this,"搜索到："+count+"颗卫星",0).show();
            } else if (event == GpsStatus.GPS_EVENT_STARTED) {
                //定位启动
            } else if (event == GpsStatus.GPS_EVENT_STOPPED) {
                //定位结束
            }
        }
    };


    private LatLng lastGPSLoc;
    // 位置监听
    private LocationListener gpslocationListener = new LocationListener() {
        /**
         * 位置信息变化时触发
         */
        public void onLocationChanged(Location gpsLocation) {
            count++;
            accuracy = (int) gpsLocation.getAccuracy();
            isFristBaidu = true;
            location = gpsLocation;
            if (accuracy <= 50) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                if (latitude == 0 || longitude == 0) {
                    return;
                }
                DecimalFormat d = new DecimalFormat("#.000000");
                LatLng latLng = convertGPSToBaidu(new LatLng(latitude, longitude));
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                System.out.println("convertGPS" + latitude + ":" + longitude);
                altitude = gpsLocation.getAltitude();
                speed = gpsLocation.getSpeed();
                setSpaceBySpeed();
                Log.i("GPS速度", "速度::" + speed);
                bearing = gpsLocation.getBearing();
                locType = "1";
                if (gpsLocation.getTime() != 0) {
//                    time = CommonTool.getStringDate(gpsLocation.getTime() / 1000,
//                            "yyyy-MM-dd HH:mm:ss");
                    time = CommonTool.getStringDate(new Date(),
                            "yyyy-MM-dd HH:mm:ss");
                } else
                    time = CommonTool.getStringDate(new Date(),
                            "yyyy-MM-dd HH:mm:ss");
                Intent i = new Intent(Constant.GETLOACTIONACTION);
                //发送广播 在巡防记录哪里显示
                i.putExtra("from", "GPS获取到数据:");
                i.putExtra("time", time);
                DecimalFormat d2 = new DecimalFormat("#.0");
                i.putExtra("location", "纬度:" + latitude + "经度:" + longitude + "速度:" + d2.format(speed) + "精度:" + d2.format(accuracy));
                sendBroadcast(i);
                address = getAddressbyGeoPoint(gpsLocation.getLatitude(),
                        gpsLocation.getLongitude());


                if(detector.isMoved&&speed<2.0){
                    saveLocation();
                    return;
                }
                if (lastGPSLoc == null) {
                    lastGPSLoc = new LatLng(latitude, longitude);
                    saveLocation();
                } else {
                    if (DistanceUtil.getDistance(new LatLng(latitude, longitude), lastGPSLoc) >= distance) {
                        lastGPSLoc = new LatLng(latitude, longitude);
                        saveLocation();
                    }
                }
                MyApplication.getInstance().latitude = latitude;
                MyApplication.getInstance().longitude = longitude;
                MyApplication.getInstance().altitude = altitude;
                MyApplication.getInstance().address = address;
                MyApplication.getInstance().speed = speed;
                MyApplication.getInstance().bearing = bearing;
                MyApplication.getInstance().accurary = accuracy;
                MyApplication.getInstance().time = time;
                MyApplication.getInstance().uLocType = locType;
                preferences.edit().putString(Constant.SP_LATITUDE, latitude + "").commit();
                preferences.edit().putString(Constant.SP_LONGITUDE, longitude + "").commit();
            }
        }

        /**
         * GPS状态变化时触发
         */
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                // GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    Log.i(TAG, "当前GPS状态为可见状态");
                    showToast("当前GPS状态为可见状态");
                    break;
                // GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i(TAG, "当前GPS状态为服务区外状态");
                    // initAm(false);
                    showToast("当前GPS状态为服务区外状态");
                    break;
                // GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    // initAm(false);
                    Log.i(TAG, "当前GPS状态为暂停服务状态");
                    break;
            }
        }

        /**
         * GPS开启时触发
         */
        public void onProviderEnabled(String provider) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10,
                    gpslocationListener);
        }

        /**
         * GPS禁用时触发
         */
        public void onProviderDisabled(String provider) {
            location = null;
        }
    };

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
            }
        } catch (Exception e) {
        }
        return address;
    }

    private void setSpaceBySpeed() {
        double hSpeed = speed;
        if(hSpeed >= 0.1&& hSpeed<3){
            distance =100;
        }else if(hSpeed >=3 && hSpeed<10 ){
            distance = 200;
        }else if(hSpeed >= 10&& hSpeed<20){
            distance = 500;
        }else if(hSpeed >=20&&hSpeed<30){
            distance = 1000;
        }
        else if(hSpeed >=30&&hSpeed<40){
            distance = 1500;
        }
        else if(hSpeed >40){
            distance = 2000;
        }

        else {
            distance = 500;
        }
    }
    private void InitLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开GPRS
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度，默认值gcj02
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        option.disableCache(true);
        option.setProdName("weisen");
        locationClient.setLocOption(option);
    }

    private LatLng lastBaiduLoc;
    private int baiduCount;

    /**
     * 实现定位回调监听
     */
    public class MyLocationListener implements BDLocationListener {

        public void onReceiveLocation(BDLocation bd) {
            count++;
            locationClient.stop();
            if (bd != null && bd.hasAddr()) {
                latitude = bd.getLatitude();
                longitude = bd.getLongitude();
                address = bd.getAddrStr();
                speed = bd.getSpeed();
                setSpaceBySpeed();
                accuracy = bd.getRadius();
                locType = "0";
                if (bd.getTime() != null) {
                    time = CommonTool.getStringDate(bd.getTime(),
                            "yyyy-M-d HH:mm:ss", "yyyy-MM-dd HH:mm:ss");
                }
                Intent i = new Intent(Constant.GETLOACTIONACTION);
                //发送广播 在巡防记录哪里显示
                i.putExtra("from", "网络获取位置成功");
                i.putExtra("time", time);
                DecimalFormat d2 = new DecimalFormat("#.0");
                i.putExtra("location", "纬度:" + latitude + "经度:" + longitude + "速度:" + d2.format(speed) + "精度:" + d2.format(accuracy));
                sendBroadcast(i);
                MyApplication.getInstance().latitude = latitude;
                MyApplication.getInstance().longitude = longitude;
                MyApplication.getInstance().address = address;
                MyApplication.getInstance().time = time;
                MyApplication.getInstance().uLocType = "0";
                MyApplication.getInstance().speed = speed;
                preferences.edit().putString(Constant.SP_LATITUDE, latitude + "").commit();
                preferences.edit().putString(Constant.SP_LONGITUDE, longitude + "").commit();
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Intent intent = new Intent(Constant.UPDATEUPDATELOCATIONTYPE);
                    intent.putExtra("date", "GPS获取位置中...");
                    intent.putExtra("count", distance);
                    if (detector.isMoved){
                        intent.putExtra("isMoved", "状态:移动");}
                    else {
                        intent.putExtra("isMoved", "状态:静止");
                    }
                    preferences.edit().putString(Constant.SP_PERIOD, periodTime + "").commit();
                    sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(Constant.UPDATEUPDATELOCATIONTYPE);
                    intent.putExtra("date", "网络获取位置中...");
                    intent.putExtra("count", distance);
                    if (detector.isMoved){
                    intent.putExtra("isMoved", "状态:移动");}
                    else {
                        intent.putExtra("isMoved", "状态:静止");
                    }

                    preferences.edit().putString(Constant.SP_PERIOD, periodTime + "").commit();
                    sendBroadcast(intent);
                }

                if(detector.isMoved){
                    saveLocation();
                    return;
                }
                    if(lastBaiduLoc == null){
                        Log.i("第一次百度","....................");
                        lastBaiduLoc = new LatLng(latitude, longitude);
                        saveLocation();
                    }else{
                        if ((int) DistanceUtil.getDistance(new LatLng(latitude, longitude), lastBaiduLoc) >= distance) {
                            lastBaiduLoc = new LatLng(latitude, longitude);
                            saveLocation();
                        }
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

        Intent i = new Intent(Constant.UPDATEUPDATELOCATIONTYPE);
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = null;
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10,
                    gpslocationListener);
            i.putExtra("date", "GPS获取位置中...");
            i.putExtra("count", distance);
            if (detector.isMoved){
                i.putExtra("isMoved", "状态:移动");}
            else {
                i.putExtra("isMoved", "状态:静止");
            }
            preferences.edit().putString(Constant.SP_PERIOD, periodTime + "").commit();
            if (UnIntent.isNetworkAvailable1(MyLocationService.this)) {
                //每一分钟 去检测GPS是否有数据 没有数据 开启百度地图上sdk去定位;
                handler.removeMessages(99);
                handler.sendEmptyMessageDelayed(99, 30000);
            }
        } else {
            i.putExtra("count", distance);
            if (UnIntent.isNetworkAvailable1(MyLocationService.this)) {
                startLocationClient();
                i.putExtra("date", "网路获取位置中...");
                preferences.edit().putString(Constant.SP_PERIOD, periodTime + "").commit();
                if (detector.isMoved){
                    i.putExtra("isMoved", "状态:移动");}
                else {
                    i.putExtra("isMoved", "状态:静止");
                }
            } else {
                i.putExtra("date", "目前不能定位...");
            }
        }
        sendBroadcast(i);
    }

    private void startLocationClient() {
        if (locationClient != null && locationListener != null) {
            locationClient.stop();
            locationClient.unRegisterLocationListener(locationListener);
        }
        locationClient = new LocationClient(this);
        locationListener = new MyLocationListener();
        InitLocation();
        locationClient.registerLocationListener(locationListener);
        locationClient.start();
    }

    public void saveLocation() {
        detector.isMoved = false;
        if (userId != null && userId.length() == 32) {
            Log.i("location", "latitude:" + latitude + "longitude" + longitude);
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
            System.out.println(beginDate);
            System.out.println(endDate);

           if (cur.after(beginDate) && cur.before(endDate)) {
                if (time == null || time.equals(""))
                    time = CommonTool.getStringDate(cur, "yyyy-MM-dd HH:mm:ss");
                MyLocation mlocation = new MyLocation();
                mlocation.setNet(IntenetUtil.getNetworkState(MyLocationService.this)+"");
                mlocation.setUid(userId);
                mlocation.setLatitude(latitude + "");
                mlocation.setLongitude(longitude + "");
                mlocation.setAltitude(altitude + "");
                mlocation.setAddress("");
                mlocation.setSpeed(CommonTool.formatFloat(speed));
                mlocation.setBearing(CommonTool.formatFloat(bearing));
                mlocation.setAccurary(CommonTool.formatFloat(accuracy));
                mlocation.setBattery(preferences.getString("battery", "50"));
                mlocation.setLocType(locType);
                mlocation.setTime(time);

                if (lastUploadLatLng != null) {
                    double distance = DistanceUtil.getDistance(new LatLng(latitude, longitude), lastUploadLatLng);
                    if (distance > 1000000) {
                        lastUploadLatLng = new LatLng(latitude, longitude);
//                        showToast("非正常数据!");
                        return;
                    }
                }
                lastUploadLatLng = new LatLng(latitude, longitude);
                long match = locationDao.getMatchCount(userId,latitude+"",longitude+"");
                if(match!=0){
//
                    return;
                }
                locationId = locationDao.addLocation(mlocation);
                Log.i("上传次数", ":" + count);
                // 上传数据
                Map<String, String> map = new HashMap<String, String>();
                map.put("uLoti", longitude + "");
                map.put("net",mlocation.getNet());
                map.put("uLati", latitude + "");
                map.put("uAlti", altitude + "");
                map.put("uAddr", "");
                map.put("uSpeed", CommonTool.formatFloat(speed));
                map.put("uDirection", CommonTool.formatFloat(bearing));
                map.put("uAccuracy", CommonTool.formatFloat(accuracy));
                map.put("battery", preferences.getString("battery", "50"));
                map.put("uTime", time);
                map.put("uLocType", MyApplication.getInstance().uLocType);
                 final Map<String, File> fileMap = new HashMap<String, File>();
                    getServer(MyApplication.getInstance().getIP()
                            + Constant.UPLOAD_TRAIL, map);


                time = "";// zh 10.9
                latitude = 0.0;
                longitude = 0.0;
                altitude = 0.0;
                address = "";
                speed = 0.0;
                bearing = 0.0;
                accuracy = 0.0;
                locType = "";
            }else{
               System.out.println("不在上报时间段");
           }
        }
    }



    private void showToast(String s) {
        Toast.makeText(MyLocationService.this, s, Toast.LENGTH_SHORT).show();
    }


    LatLng lastUploadLatLng;




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
                Log.i("上传位置失败", new String(arg2 + ""));
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
//            UploadTool uploadTool = new UploadTool(this);
//            uploadTool.upload();
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
            mNM.cancel(id);
            mSetForegroundArgs[0] = Boolean.FALSE;
            invokeMethod(mSetForeground, mSetForegroundArgs);
        } else {
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

    // 广播启动定位服务,系统每一分钟发出的广播
    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                System.out.println("进入循环执行!!!");

                systemtime++;
                ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (mobileInfo.isConnected() || wifiInfo.isConnected()) {
                    UploadTool uploadTool = new UploadTool(MyLocationService.this);
                    uploadTool.upload();
                    System.out.println("检查未上传开启上传");
                }
                if(distance != 0){
                    if(systemtime%rate == 0){
                        getLocation();
                    }

                }
                if (!userId.equals("")) {
                    Intent service = new Intent(MyLocationService.this,
                            MyLocationService.class);
                    service.putExtra("isRun", true);
                    startService(service);


                    if(distance == 0){
                        Intent service2 = new Intent(MyLocationService.this,
                                MyLocationService.class);
                        stopService(service2);
                    }
                } else {
                    Intent service = new Intent(MyLocationService.this,
                            MyLocationService.class);
                    stopService(service);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        systemtime=0;
        count=0;
        releaseWakeLock();
        userId = "";
        if (receiver != null)
            unregisterReceiver(receiver);
        if (alarmReceiver != null) {
            unregisterReceiver(alarmReceiver);
        }
//        if (am != null) {
//            am.cancel(sender);
//        }
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }

        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            lm.removeUpdates(gpslocationListener);
        }

    }

    // 释放设备电源锁
    private void releaseWakeLock() {
        if (null != wakeLock && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    public int battery;

    /**
     * 广播接受者
     */
    class BatteryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 判断它是否是为电量变化的Broadcast Action
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                // 获取当前电量
                int level = intent.getIntExtra("level", 0);
                // 电量的总刻度
                int scale = intent.getIntExtra("scale", 100);
                // 把它转成百分比
                battery = (level * 100) / scale;
                if (battery != 0) {
                    MyApplication.getInstance().battery = battery;
                    preferences.edit().putString("battery", battery + "").commit();
                }
            }
        }
    }

class  systemcount{
    Date date;
    int count;
}


}
