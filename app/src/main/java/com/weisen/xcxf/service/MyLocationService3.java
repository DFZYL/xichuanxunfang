package com.weisen.xcxf.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
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

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;
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
import java.util.Timer;
import java.util.TimerTask;


public class MyLocationService3 extends Service {
    private static final String TAG = "LocationService";
    public Timer mTimer = new Timer();// 定时器
    private LocationManager lm;
    private Location location;
    private LocationMode tempMode = LocationMode.FOLLOWING;
    private LocationClient locationClient;
    public long periodTime;
    public String userId, beginTime, endTime;
    public double latitude, longitude, altitude, speed, bearing, accuracy;
    public String address = "", time = "", locType = "1";
    public MyLocationDao locationDao;
    private int locationId;
    private SharedPreferences preferences;
    private int count = 0;
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
    private MyLocation oldLocation;
    private AlarmManager am ;//全局的定时服务
    private SensorManager mSensorManager;// 传感器服务
    private StepDetector detector;// 传感器监听对象
    private MyLocationListener locationListener;
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
    PendingIntent sender;
    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        receiver = new MyBroadcastReceiver();
        registerReceiver(receiver, filter);
        locationDao = new MyLocationDao(this);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getCriteria();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10,
                gpslocationListener);
       // lm.addGpsStatusListener(gpsStatusListener);

        locationClient = new LocationClient(this);
        locationListener = new MyLocationListener();
        locationClient.registerLocationListener(locationListener);

        //全局定时系统
        am = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmReceiver = new AlarmReceiver();
        IntentFilter i = new IntentFilter();
        i.addAction("repeat");
        registerReceiver(alarmReceiver, i);
        detector = new StepDetector(this);
        // 获取传感器的服务，初始化传感器
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        // 注册传感器，注册监听器
        mSensorManager.registerListener(detector,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @SuppressLint("NewApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preferences = getSharedPreferences(Constant.APP_SP, MODE_MULTI_PROCESS);
//        periodTime = Long.parseLong(preferences.getString(Constant.SP_PERIOD,
//                (Constant.DEFAULT_PERIOD + "")));
        periodTime = 1*60000;
       // Toast.makeText(MyLocationService.this, "服务重启!!!", 0).show();
        // periodTime = 60000*5;
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
            mStartForeground = MyLocationService3.class.getMethod(
                    "startForeground", mStartForegroundSignature);
            mStopForeground = MyLocationService3.class.getMethod(
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
            if (am!=null){
                am.cancel(sender);
            }
            Intent stop = new Intent(this, MyLocationService3.class);
            stopService(stop);
        } else {
            if (intent != null) {
                isRun = intent.getBooleanExtra("isRun", false);
            }
            if (!isRun) {
                if (mTimer != null) {
                    mTimer.cancel();// 退出之前的mTimer
                }
                if (am!=null){
                    am.cancel(sender);
                }
                if (periodTime != 0) {
                    mTimer = new Timer();// new一个Timer,否则会报错
                    Intent intent2 = new Intent("repeat");
                    sender = PendingIntent.getBroadcast(this, 0, intent2, 0);
                    if(am==null){
                        am = (AlarmManager) getSystemService(ALARM_SERVICE);
                    }
                    am.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),periodTime,sender);
                    if(detector==null){
                        detector = new StepDetector(this);
                        // 获取传感器的服务，初始化传感器
                        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
                        // 注册传感器，注册监听器
                        mSensorManager.registerListener(detector,
                                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                SensorManager.SENSOR_DELAY_FASTEST);
                    }
                  //  timerTask();
//                    if(am!=null){
//                        am.cancel(sender);
//                    }else{
//                        IntentFilter i = new IntentFilter();
//                        i.addAction("repeat");
//                        registerReceiver(alarmReceiver,i);
//                        Intent i2 = new Intent("repeat");
//                        sender = PendingIntent.getBroadcast(this, 0, i2, 0);
//                        am.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),periodTime,sender);
//                    }

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
    private AlarmReceiver alarmReceiver;
   public class AlarmReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("进入循环执行!!!");
            getLocation();
        }
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
                   // getLocation();
                    break;
                case 99:
                    if (location==null){
                        Intent i = new Intent(Constant.UPDATEUPDATELOCATIONTYPE);
                        if(detector.isMoved){
                            detector.isMoved = false;
                            if(locationClient!=null){
                                locationClient.unRegisterLocationListener(locationListener);
                            }
                            locationClient = new LocationClient(MyLocationService3.this);
                            InitLocation();
                            locationClient.registerLocationListener(locationListener);
                            locationClient.start();
                            i.putExtra("date", "百度获取位置中.....");
                            i.putExtra("count",count);
                            i.putExtra("isMoved", "状态:移动");
                            preferences.edit().putString(Constant.SP_PERIOD,periodTime+"").commit();
                            sendBroadcast(i);
                        }else{
                            i.putExtra("date", "手机没有移动!");
                            i.putExtra("count",count);
                            i.putExtra("isMoved", "状态:静止");
                            sendBroadcast(i);
                        }
                    }else{
                        preferences.edit().putString(Constant.SP_PERIOD,periodTime+"").commit();
//                        if(location != null){
//                            saveLocation();
//                        }else{
//                          //  Toast.makeText(MyLocationService.this, "当前GPS无法定位", 0).show();
//                        }
                    }
                    break;
                case 88:
                    if(location != null){
                        saveLocation();
                    }else{
                      //  Toast.makeText(MyLocationService.this, "当前GPS无法定位", 0).show();
                    }
                    preferences.edit().putString(Constant.SP_PERIOD,periodTime+"").commit();
                    break;
            }
        }
    };

    //gps状态发生改变
    GpsStatus.Listener gpsStatusListener=new GpsStatus.Listener(){
        public void onGpsStatusChanged(int event) {
            if(event==GpsStatus.GPS_EVENT_FIRST_FIX){
                //第一次定位
            }else if(event==GpsStatus.GPS_EVENT_SATELLITE_STATUS){
                //卫星状态改变
                GpsStatus gpsStauts= lm.getGpsStatus(null); // 取当前状态
                int maxSatellites = gpsStauts.getMaxSatellites(); //获取卫星颗数的默认最大值
                Iterator<GpsSatellite> it = gpsStauts.getSatellites().iterator();//创建一个迭代器保存所有卫星
                int count = 0;
                while (it.hasNext() && count <= maxSatellites) {
                    count++;
                    GpsSatellite s = it.next();
                }
              //  System.out.println("搜索到："+count+"颗卫星");
               // Toast.makeText(MyLocationService.this,"搜索到："+count+"颗卫星",0).show();
            }else if(event==GpsStatus.GPS_EVENT_STARTED){
                //定位启动
            }else if(event==GpsStatus.GPS_EVENT_STOPPED){
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
            accuracy = (int) gpsLocation.getAccuracy();
            location = gpsLocation;
            System.out.println("Gps定位回调");
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            if(latitude == 0 || longitude == 0){
                return;
            }
            DecimalFormat d = new DecimalFormat("#.000000");
            System.out.println("sourceGPS" + d.format(latitude) + ":" + d.format(longitude));
            LatLng latLng = convertGPSToBaidu(new LatLng(latitude, longitude));
            latitude = latLng.latitude;
            longitude = latLng.longitude;
//            BigDecimal bg = new BigDecimal(latitude);
//            latitude = bg.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
//            longitude = new BigDecimal(longitude).setScale(6,BigDecimal.ROUND_HALF_UP).doubleValue();
            System.out.println("convertGPS" + latitude + ":" + longitude);
            altitude = gpsLocation.getAltitude();
            speed = gpsLocation.getSpeed();
            Log.i("GPS速度", "速度::" + speed);
            setSpaceBySpeed();
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
            i.putExtra("time",time);
            DecimalFormat d2 = new DecimalFormat("#.0");
            i.putExtra("location", "纬度:" + latitude + "经度:" + longitude + "速度:" + d2.format(speed) + "精度:" + d2.format(accuracy));
            sendBroadcast(i);
            address = getAddressbyGeoPoint(gpsLocation.getLatitude(),
                    gpsLocation.getLongitude());
//            if(lastGPSLoc == null){
//                saveLocation();
//                lastGPSLoc = new LatLng(latitude,longitude);
//            }else{
//                if(DistanceUtil.getDistance(new LatLng(latitude,longitude),lastGPSLoc)>= 100){
//                    saveLocation();
//                    lastGPSLoc = new LatLng(latitude,longitude);
//                }
//            }
            String[] latTmp = d.format(latitude).split("\\.");
            String[] lotTmp = d.format(longitude).split("\\.");
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
                preferences.edit().putString(Constant.SP_LATITUDE ,latitude+"").commit();
                preferences.edit().putString(Constant.SP_LONGITUDE,longitude+"").commit();
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
                    Toast.makeText(MyLocationService3.this, "当前GPS状态为可见状态", 0).show();
                    break;
                // GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i(TAG, "当前GPS状态为服务区外状态");
                    initAm(false);
                    Toast.makeText(MyLocationService3.this, "当前GPS状态为服务区外状态", 0).show();
                    break;
                // GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    initAm(false);
                    Log.i(TAG, "当前GPS状态为暂停服务状态");
                    break;
            }
        }

        /**
         * GPS开启时触发
         */
        public void onProviderEnabled(String provider) {
            // location = lm.getLastKnownLocation(bestProvider);

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
    private long newPeriodTime;
    private void setSpaceBySpeed() {

           double hSpeed = speed;
            if(hSpeed >= 0.1&& hSpeed<3){
                periodTime = 10;
            }else if(hSpeed >=3 && hSpeed<9 ){
                periodTime = 300;
            }else if(hSpeed >= 9 && hSpeed<14){
                periodTime = 20*1000;
            }else if(hSpeed >=14){
                periodTime = 10*1000;
            }else {
                periodTime = 5*60*1000;
            }
            if(newPeriodTime != periodTime){
               // Toast.makeText(this,"当前速度发生变化:"+hSpeed,0).show();
                newPeriodTime = periodTime;
                preferences.edit().putString(Constant.SP_PERIOD,periodTime+"").commit();
                initAm(true);
            }
    }

    private void initAm(boolean gpsVisable) {
        if(am != null){
            am.cancel(sender);
        }else {
            am = (AlarmManager) getSystemService(ALARM_SERVICE);
        }
        Intent intent2 = new Intent("repeat");
        sender = PendingIntent.getBroadcast(this, 0, intent2, 0);
        if(!gpsVisable){
            periodTime = 1*60*1000;
            preferences.edit().putString(Constant.SP_PERIOD,periodTime+"").commit();
        }
        saveLocation();
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + periodTime, periodTime, sender);
    }

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

    private void InitLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开GPRS
        option.setScanSpan(10000);// 设置定位请求时间
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度，默认值gcj02
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        option.disableCache(true);
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
                accuracy = bd.getRadius();
                locType = "0";
                if (bd.getTime() != null) {
                    time = CommonTool.getStringDate(bd.getTime(),
                            "yyyy-M-d HH:mm:ss", "yyyy-MM-dd HH:mm:ss");
                }
                Intent i = new Intent(Constant.GETLOACTIONACTION);
                //发送广播 在巡防记录哪里显示
                i.putExtra("from", "网络获取位置成功:");
                i.putExtra("time",time);
                DecimalFormat d2 = new DecimalFormat("#.0");
                i.putExtra("location", "纬度:" + latitude + "经度:" + longitude + "速度:" + d2.format(speed) + "精度:" + d2.format(accuracy));
                sendBroadcast(i);
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
                    intent.putExtra("count",count);
                    if(detector.isMoved){
                        intent.putExtra("isMoved","状态:移动");
                        preferences.edit().putString(Constant.SP_PERIOD,periodTime+"").commit();
                    }else{
                        intent.putExtra("isMoved", "状态:静止");
                    }
                    sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(Constant.UPDATEUPDATELOCATIONTYPE);
                    intent.putExtra("date", "网络获取位置中...");
                    intent.putExtra("count",count);
                    if(detector.isMoved){
                        intent.putExtra("isMoved","状态:移动");
                        preferences.edit().putString(Constant.SP_PERIOD,periodTime+"").commit();
                    }else{
                        intent.putExtra("isMoved", "状态:静止");
                    }
                    sendBroadcast(intent);
                }
                if (latTmp[1].length() >= 6 && lotTmp[1].length() >= 6) {
                    MyApplication.getInstance().latitude = latitude;
                    MyApplication.getInstance().longitude = longitude;
                    MyApplication.getInstance().address = address;
                    MyApplication.getInstance().time = time;
                    MyApplication.getInstance().uLocType = "0";
                    MyApplication.getInstance().speed = speed;
                    preferences.edit().putString(Constant.SP_LATITUDE ,latitude+"").commit();
                    preferences.edit().putString(Constant.SP_LONGITUDE,longitude+"").commit();
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
        count++;
        Intent i = new Intent(Constant.UPDATEUPDATELOCATIONTYPE);
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = null;
//            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10,
//                    gpslocationListener);
            i.putExtra("date", "GPS获取位置中...");
            i.putExtra("count",count);
            if(detector.isMoved){
                i.putExtra("isMoved","设备状态:移动");
                preferences.edit().putString(Constant.SP_PERIOD,periodTime+"").commit();
            }else{
                i.putExtra("isMoved", "设备状态:静止");
            }
            if (UnIntent.isNetworkAvailable1(MyLocationService3.this)) {
                if(periodTime==60000){
                    //间隔时间为一分钟 说明是gps不可见的时候 等待gps的定位时间:
                    handler.sendEmptyMessageDelayed(99, 50000);
                }else{
                    saveLocation();
                }
            }else{
               // handler.sendEmptyMessageDelayed(88, periodTime);
                saveLocation();
            }
                //没一分钟 去检测GPS是否有数据 没有数据 开启百度地图上sdk去定位;
               // handler.sendEmptyMessageDelayed(99, 50000);

        } else {
            i.putExtra("count", count);
            if (UnIntent.isNetworkAvailable1(MyLocationService3.this)) {
                if(detector.isMoved){
                    if(locationClient!=null){
                        locationClient.unRegisterLocationListener(locationListener);
                    }
                    detector.isMoved = false;
                    locationClient = new LocationClient(this);
                    InitLocation();
                    locationClient.registerLocationListener(locationListener);
                    locationClient.start();
                    i.putExtra("date", "网路获取位置中...");
                    preferences.edit().putString(Constant.SP_PERIOD,periodTime+"").commit();
                    i.putExtra("isMoved","手机状态:移动");
                }else{
                    i.putExtra("date", "手机没有移动.");
                    i.putExtra("isMoved", "设备状态:静止");
                }
            } else {
                i.putExtra("date", "目前不能定位...");
            }
        }
        sendBroadcast(i);

    }
    public void saveLocation() {
        if (userId != null && userId.length() == 32) {
            Log.i("location", "latitude:" + latitude + "longitude" + longitude);
            DecimalFormat d = new DecimalFormat("#.000000");
//            Double aDouble = Double.valueOf(oldLocation.getLatitude());
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
//                                    MyApplication.getInstance().altitude = altitude;
//                                MyApplication.getInstance().longitude = longitude;
//            e.printStackTrace();
//        }
//
//        double distance = DistanceUtil.getDistance(new LatLng(latitude, longitude), new LatLng(aDouble, aDouble1));
//        if(distance/time1>100){
//            System.out.println("飘逸数据");
//            return;
//        }

            String[] latTmp = String.valueOf(latitude).split("\\.");
            String[] lotTmp = String.valueOf(longitude).split("\\.");
            if (latTmp[1].length() >= 6 && lotTmp[1].length() >= 6) {

            }else{
                System.out.println("数据不符合要求");
                time = "";// zh 10.9
                latitude = 0.0;
                longitude = 0.0;
                altitude = 0.0;
                address = "";
                speed = 0.0;
                bearing = 0.0;
                accuracy = 0.0;
                locType = "";
                Toast.makeText(this,"数据不符合要求不存储上传",0).show();
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
            System.out.println(beginDate);
            System.out.println(endDate);
            if (cur.after(beginDate) && cur.before(endDate)) {
                if (time == null || time.equals(""))
                    time = CommonTool.getStringDate(cur, "yyyy-MM-dd HH:mm:ss");
                String battery = MyApplication.getInstance().battery + "";
                MyLocation mlocation = new MyLocation();
                mlocation.setUid(userId);
                mlocation.setLatitude(latitude + "");
                mlocation.setLongitude(longitude + "");
                mlocation.setAltitude(altitude + "");
                mlocation.setAddress("");
                mlocation.setSpeed(CommonTool.formatFloat(speed));
                mlocation.setBearing(CommonTool.formatFloat(bearing));
                mlocation.setAccurary(CommonTool.formatFloat(accuracy));
                mlocation.setBattery(battery);
                mlocation.setLocType(locType);
                mlocation.setTime(time);
                if(lastUploadLatLng!=null){
                    double distance = DistanceUtil.getDistance(new LatLng(latitude, longitude), lastUploadLatLng);
                    if(distance>100000){
                        lastUploadLatLng = new LatLng(latitude,longitude);
                        Toast.makeText(MyLocationService3.this, "不正常数据!", 0).show();
                        return;
                    }
                }
                lastUploadLatLng = new LatLng(latitude,longitude);
                locationId = locationDao.addLocation(mlocation);
                Log.i("上传次数", ":" + count);
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

    // 广播启动定位服务
    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                if (!userId.equals("")) {
                    Intent service = new Intent(MyLocationService3.this,
                            MyLocationService3.class);
                    service.putExtra("isRun", true);
                    startService(service);
                } else {
                    Intent service = new Intent(MyLocationService3.this,
                            MyLocationService3.class);
                    stopService(service);
                }
                if(lm!=null&&gpslocationListener!=null&&lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10,
                            gpslocationListener);
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
        if(alarmReceiver !=null){
            unregisterReceiver(alarmReceiver);
        }
        if(am!=null){
            am.cancel(sender);
        }
        if (detector != null) {
            mSensorManager.unregisterListener(detector);
        }
    }

    // 释放设备电源锁
    private void releaseWakeLock() {
        if (null != wakeLock && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }
}
