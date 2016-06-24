package com.weisen.xcxf.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import cn.jpush.android.api.JPushInterface;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.SDKInitializer;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.weisen.xcxf.Constant;
import com.weisen.xcxf.bean.CaseType;
import com.weisen.xcxf.db.DbOpenHelper;
import com.weisen.xcxf.tool.CommonTool;
import com.weisen.xcxf.tool.FileUtils;
import com.weisen.xcxf.tool.TimeCount;
import com.blueware.agent.android.BlueWare;

public class MyApplication extends Application implements
        UncaughtExceptionHandler {
    private static final String TAG = "JPush";
    public static Context applicationContext;
    public static MyApplication instance;
    private DbOpenHelper typeSqlHelper;
    public String userId, name, headPic, ip, channelId;
    public static boolean isWork;
    private List<Activity> activityList = new ArrayList<Activity>();
    public double latitude, longitude, altitude, speed, bearing, accurary;
    public String period, beginTime, endTime;
    public int battery;
    public String address, uLocType = "1", time;
    private SharedPreferences sp;
     public  BMapManager mBMapManager;
    public Map<String, String> mapAddr = new HashMap<String, String>();
    public Map<String, String> netDatas = new HashMap<String, String>();
    public List<CaseType> mCaseTypesList = new ArrayList<CaseType>();
    public DisplayImageOptions options;

    public void addActivity(Activity activity) {
        if (!activityList.contains(activity))
            activityList.add(activity);
    }

    public void clearActivity() {
        for (Activity activity : activityList)
            activity.finish();
    }

    @Override
    public void onCreate() {

        super.onCreate();

        instance = this;
        applicationContext = this;
        SDKInitializer.initialize(getApplicationContext());
        BlueWare.withApplicationToken("82DAC07F5406DE5F0EEF604992DC615A08").start(this);

        JPushInterface.setDebugMode(true); // 设置开启日志,发布时请关闭日志
        JPushInterface.init(this); // 初始化 JPush

        /**
         * 异步加载图片全局配置
         */
//		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
//				.bitmapConfig(Bitmap.Config.RGB_565).cacheInMemory()
//				.cacheOnDisc() // 同上
//				.build();
//		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
//				this).defaultDisplayImageOptions(defaultOptions)
//				.threadPriority(Thread.NORM_PRIORITY - 3).threadPoolSize(4)
//				.denyCacheImageMultipleSizesInMemory()
//				.discCacheFileNameGenerator(new Md5FileNameGenerator())
//				.discCacheFileCount(50)
//				.memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
//				.tasksProcessingOrder(QueueProcessingType.LIFO).build();

        File cacheDir = StorageUtils.getOwnCacheDirectory(this, "yunfeng/Cache");
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCacheExtraOptions(480, 800) // max width, max height，即保存的每个缓存文件的最大长宽
                .threadPoolSize(3) //线程池内线程的数量
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024) // 内存缓存的最大值
                .discCacheSize(50 * 1024 * 1024)  // SD卡缓存的最大值
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                        // 由原先的discCache -> diskCache
                .discCache(new UnlimitedDiscCache(cacheDir))
                .imageDownloader(new BaseImageDownloader(this, 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
                .build();
        ImageLoader.getInstance().init(config);
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        MyBroadcastReceiver receiver = new MyBroadcastReceiver();
        registerReceiver(receiver, filter);


        options = new DisplayImageOptions.Builder()
                .cacheInMemory()                        // 设置下载的图片是否缓存在内存中
                .cacheOnDisc()                          // 设置下载的图片是否缓存在SD卡中
                .build();                                   // 创建配置过得DisplayImageOption对象

        // 注册广播接受者java代码
        IntentFilter intentFilter = new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED);
        // 创建广播接受者对象
        BatteryReceiver batteryReceiver = new BatteryReceiver();
        // 注册receiver
        registerReceiver(batteryReceiver, intentFilter);

        UncaughtExceptionHandler originalHandler = Thread
                .getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        sp = getSharedPreferences(Constant.APP_SP, MODE_PRIVATE);
        isWork = sp.getBoolean(Constant.ISWORK,true);





    }

    public void setServiceIsOpen(boolean open) {
      sp.edit().putBoolean("isRunning", open).commit();
    }

    public boolean getServiceOpen() {
        return sp.getBoolean("isRunning", false);
    }

    public boolean getIsWork() {
     return    sp.getBoolean(Constant.ISWORK,true);
    }
    /**
     * 获取数据库Helper
     */
    public DbOpenHelper getSQLHelper() {
        if (typeSqlHelper == null)
            typeSqlHelper = DbOpenHelper.getInstance(instance);
        return typeSqlHelper;
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public String getUserId() {
        if (userId == null || userId.equals("")) {
            SharedPreferences preferences = instance.getSharedPreferences(
                    Constant.APP_SP, MODE_MULTI_PROCESS);
            userId = preferences.getString(Constant.SP_USERID, "");
        }
        if (userId == null || userId.equals(""))
            userId = "";
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        SharedPreferences preferences = instance.getSharedPreferences(
                Constant.APP_SP, MODE_MULTI_PROCESS);
        Editor editor = preferences.edit();
        editor.putString(Constant.SP_USERID, userId);
        editor.commit();
    }


    public String getName() {
        if (name == null || name.equals("")) {
            SharedPreferences preferences = instance.getSharedPreferences(
                    Constant.APP_SP, MODE_MULTI_PROCESS);
            name = preferences.getString(Constant.SP_NAME, "");
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
        SharedPreferences preferences = instance.getSharedPreferences(
                Constant.APP_SP, MODE_MULTI_PROCESS);
        Editor editor = preferences.edit();
        editor.putString(Constant.SP_NAME, name);
        editor.commit();
    }

    public String getHeadPic() {
        if (headPic == null || headPic.equals("")) {
            SharedPreferences preferences = instance.getSharedPreferences(
                    Constant.APP_SP, MODE_MULTI_PROCESS);
            headPic = preferences.getString(Constant.SP_HEADPIC, "");
        }
        return headPic;
    }

    public void setHeadPic(String headPic) {
        this.headPic = headPic;
        SharedPreferences preferences = instance.getSharedPreferences(
                Constant.APP_SP, MODE_MULTI_PROCESS);
        Editor editor = preferences.edit();
        editor.putString(Constant.SP_HEADPIC, headPic);
        editor.commit();
    }

    public boolean ipIsChange(){

        return false;
    }

    public String getIP() {
        if (ip == null || ip.equals("")) {

            SharedPreferences preferences = instance.getSharedPreferences(
                    Constant.APP_SP, MODE_MULTI_PROCESS);
            ip = preferences.getString(Constant.SP_IP, Constant.IP);
        }
        if (ip == null || ip.equals(""))
            ip = Constant.IP;
        return ip;
    }

    public void setIP(String ip) {
        this.ip = ip;
        if (!ip.endsWith("/")) {
            ip += "/";
        }
        SharedPreferences preferences = instance.getSharedPreferences(
                Constant.APP_SP, MODE_MULTI_PROCESS);
        Editor editor = preferences.edit();
        editor.putString(Constant.SP_IP, ip);
        editor.commit();
    }

    public String getPerioid() {
        if (period == null || period.equals("")) {
            SharedPreferences preferences = instance.getSharedPreferences(
                    Constant.APP_SP, MODE_MULTI_PROCESS);
            period = preferences.getString(Constant.SP_PERIOD,
                    (Constant.DEFAULT_PERIOD + ""));
        }
        return period;
    }

    public String getBeginTime() {
        if (beginTime == null || beginTime.equals("")) {
            SharedPreferences preferences = instance.getSharedPreferences(
                    Constant.APP_SP, MODE_MULTI_PROCESS);
            beginTime = preferences.getString(Constant.SP_BEGIN,
                    Constant.DEFAULT_BEGIN);
        }
        return beginTime;
    }

    public String getEndTime() {
        if (endTime == null || endTime.equals("")) {
            SharedPreferences preferences = instance.getSharedPreferences(
                    Constant.APP_SP, MODE_MULTI_PROCESS);
            endTime = preferences.getString(Constant.SP_END,
                    Constant.DEFAULT_END);
        }
        return endTime;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    /**
     * 退出登录,清空数据
     */
    public void logout() {
        // 先调用sdk logout，在清理app中自己的数据
        clearAllSp();
    }

    private void clearAllSp() {
        this.userId = "";
        this.headPic = "";
        this.name = "";
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.altitude = 0.0;
        this.speed = 0.0;
        this.bearing = 0.0;
        this.accurary = 0.0;
        SharedPreferences preferences = instance.getSharedPreferences(
                Constant.APP_SP, MODE_MULTI_PROCESS);
        Editor editor = preferences.edit();
        editor.putString(Constant.SP_PWD, "");
        editor.putString(Constant.SP_USERID, "");
        editor.putString(Constant.SP_NAME, "");
        editor.putString(Constant.SP_USERNAME, "");
        editor.putString(Constant.SP_HEADPIC, "");
        editor.putString(Constant.SP_PERIOD, Constant.DEFAULT_PERIOD + "");
        editor.putString(Constant.SP_PERIOD_NAME, Constant.DEFAULT_PERIOD_NAME
                + "");
        editor.putString(Constant.SP_BEGIN, Constant.DEFAULT_BEGIN);
        editor.putString(Constant.SP_END, Constant.DEFAULT_END);
        editor.commit();
    }


    @Override
    public void uncaughtException(Thread arg0, Throwable ex) {

        try {
            File fold = getCrashPath();
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "yyyy-MM-dd HH.mm.ss.SSS");
            String fileName = "crash " + sdf.format(new Date()) + ".txt";
            File file = new File(fold, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter ps = new PrintWriter(fos);
            ex.printStackTrace(ps);
            ps.close();
        } catch (Exception e) {
            Log.e(MyApplication.class.getName(), "Can not save exception.", e);
        }
    }

    public File getCrashPath() {
        String path = FileUtils.getRootFilePath() + "crash";
        File file = new File(path);
        if (!file.exists()) {
            File parentFile = new File(file.getParent());
            if (!parentFile.exists())
                parentFile.mkdir();
            file.mkdir();
        }
        return file;
    }

    /**
     * 广播接收器，检测是否启动，每隔1分钟自启动服务
     **/
    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                if (!MyApplication.getInstance().getUserId().equals("")) {
                    if (!CommonTool.isServiceRunning(applicationContext,
                            "com.weisen.xcxf.service.MyLocationService")) {
//                        if (isWork) {
//                            Intent service = new Intent(applicationContext,
//                                    MyLocationService.class);
//                            startService(service);
//                        }

                    }
                }
            }
        }
    }

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
            }
        }

    }

    TimeCount timeCount;
    public long countTime;

    public void startCountTime() {
        timeCount = new TimeCount(60000, 1000);
        timeCount.start();
        timeCount.setCountDownTimerListener(new TimeCount.onCountDownTimerListener() {
            @Override
            public void onStart(long millisUntilFinished) {
                countTime = millisUntilFinished;
            }

            @Override
            public void onFinished() {
            }
        });
    }


}
