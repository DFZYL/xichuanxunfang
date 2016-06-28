package com.weisen.xcxf.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.DistanceUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.MyLocation;
import com.weisen.xcxf.bean.MyLocationDao;
import com.weisen.xcxf.bean.UserLength;
import com.weisen.xcxf.bean.UserLengthDao;
import com.weisen.xcxf.service.MyLocationService;
import com.weisen.xcxf.tool.Bimp;
import com.weisen.xcxf.tool.CommonTool;
import com.weisen.xcxf.tool.FileUtils;
import com.weisen.xcxf.tool.HttpTool;
import com.weisen.xcxf.tool.MD5Tool;
import com.weisen.xcxf.tool.MyOrientationListener;
import com.weisen.xcxf.tool.MyOrientationListener.OnOrientationListener;
import com.weisen.xcxf.tool.TimeCount;
import com.weisen.xcxf.tool.UnIntent;
import com.weisen.xcxf.utils.IntenetUtil;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrailActivity extends BaseActivity implements OnClickListener {
    private String WORKPIC,UNWORKPIC;
    private TextView tv_trail;
    private Handler handler;
    private RelativeLayout ry_bg, rl_net;
    private Button btn_left;
    private ImageView img_right;
    private TextView userName, nowTime;
    private MapView mMapView = null;
    private  int WORKPHOTO=1;
    private int UNWORKPHOTO=2;
    private static final String TAG = "TrailActivity";
    private BaiduMap mBaiduMap;
    private LocationClient locationClient;
    private LatLng latLng;
    private double latitude, longitude, currentLatidude, currentLongitude;
    private double gpsLatitude, gpsLongitude;
    private String type,photoPath;
    private MyLocationDao locationDao;
    private List<LatLng> myLatlng_list;
    private List<MyLocation> myLocation_list;
    private List<Double> distance_list = new ArrayList<Double>();// 距离
    private Double sum = 0.0;
    private LatLng startLatlng;
    private LatLng endLatlng;
    private LatLng currentLatlng;
    BitmapDescriptor bdA = BitmapDescriptorFactory.fromResource(R.drawable.end);// 起点
    BitmapDescriptor bdB = BitmapDescriptorFactory
            .fromResource(R.drawable.start);// 终点
    private float mCurrentAccracy = 100.f;// 当前的精度
    private LocationMode mCurrentMode = LocationMode.FOLLOWING;// 当前定位的模式跟随定位
    private MyOrientationListener myOrientationListener;// 方向传感器的监听器
    private int mXDirection;// 方向传感器X方向的值
    private LocationManager lm;// android定位管理器
    private Handler mHandler;
    protected static final int UPDATE_TEXT = 0;
    MyConnectReceiver connectReceiver;
    private double f1;
    private String time;
    private SharedPreferences sp;
    boolean isWork;
    private Boolean isWeixing=false;
    private ImageView weixing;
    @Override
    protected void initView() {

        setContentView(R.layout.activity_trail);
        String  ImagePath = FileUtils.getImgFilePath();
        if (!ImagePath.endsWith(File.separator)) {
           ImagePath = ImagePath+ File.separator;
        }
        File dirFile = new File(ImagePath);
        dirFile.delete();
        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Toast.makeText(TrailActivity.this,msg.what,Toast.LENGTH_LONG).show();
            }
        };
        sp = getSharedPreferences(Constant.APP_SP, MODE_PRIVATE);
        isWork = sp.getBoolean(Constant.ISWORK,true);
        type = getIntent().getStringExtra("type");
        if (type == null || type.equals(""))
            type = "trail";
        btn_left = (Button) findViewById(R.id.btn_left);
        btn_left.setOnClickListener(this);
        img_right = (ImageView) findViewById(R.id.img_right);
        img_right.setOnClickListener(this);
        weixing=(ImageView)findViewById(R.id.image_weixing);
        weixing.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                if(isWeixing)
                {
                    mMapView.getMap().setMapType(BaiduMap.MAP_TYPE_NORMAL);
                    isWeixing=false;
                    weixing.setImageResource(R.drawable.map_weixing);
                }
                else {
                    isWeixing=true;
                    mMapView.getMap().setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                    weixing.setImageResource(R.drawable.map_pingmian);
                }
            }
        });
        tv_title = (TextView) findViewById(R.id.tv_title);
        if(isWork){

            btn_left.setText("结束工作");
            tv_title.setText("工作中");
            startService(new Intent(TrailActivity.this, MyLocationService.class));
            MyApplication.getInstance().setServiceIsOpen(true);
        }else{

            btn_left.setText("开始工作");
            tv_title.setText("我的位置");
            stopService(new Intent(TrailActivity.this, MyLocationService.class));


        }
        //getWorkStatus();
        ry_bg = (RelativeLayout) findViewById(R.id.rl_bg);
        ry_bg.getBackground().setAlpha(200);
        rl_net = (RelativeLayout) findViewById(R.id.rl_net);
        rl_net.setVisibility(View.GONE);
        rl_net.setOnClickListener(this);
        userName = (TextView) findViewById(R.id.userName);
        nowTime = (TextView) findViewById(R.id.nowTime);
        nowTime.setOnClickListener(this);
        userName.setText(MyApplication.getInstance().getName());
        nowTime.setText(CommonTool.getStringDate(new Date(), "yyyy年MM月dd日"));
        tv_trail = (TextView) findViewById(R.id.tv_trail);
        tv_trail.setOnClickListener(this);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mMapView = (MapView) findViewById(R.id.mv_map);
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.zoomTo(18.0f);
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.location_marker);
        MyLocationConfiguration config = new MyLocationConfiguration(
                mCurrentMode, true, mCurrentMarker);
        mBaiduMap.setMyLocationConfigeration(config);
        mBaiduMap.setMyLocationEnabled(true);
        locationClient();
        findGPSTask();
        trailDataList();
        trailDistance();
        insertLength();
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getCriteria();
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10,
                    gpslocationListener);
        }
        initOritationListener();
    }

    private void setMyLoaction() {
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.zoomTo(18.0f);
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.location_marker);
        MyLocationConfiguration config = new MyLocationConfiguration(
                mCurrentMode, true, mCurrentMarker);
        mBaiduMap.setMyLocationConfigeration(config);
//        if (latitude != 0 && longitude != 0) {
//            MyLocationData locData = new MyLocationData.Builder()
//                    .accuracy(100)
//                    .latitude(latitude)
//                    .direction(mXDirection)
//                    .longitude(longitude)
//                    .build();
//            mBaiduMap.setMyLocationData(locData);
//            latitude = 0;
//            longitude = 0;
//        } else {
//            if (gpsLatitude != 0) {
//                MyLocationData locData = new MyLocationData.Builder()
//                        .accuracy(mCurrentAccracy)
//                        .latitude(gpsLatitude)
//                        .direction(mXDirection)
//                        .longitude(gpsLongitude)
//                        .build();
//                mBaiduMap.setMyLocationData(locData);
//                gpsLatitude = 0;
//                gpsLongitude = 0;
//            }
//        }
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(mCurrentAccracy)
                .latitude(currentLatidude)
                .direction(mXDirection)
                .longitude(currentLongitude)
                .build();
        mBaiduMap.setMyLocationData(locData);
        Log.i(TAG, "setMyLoaction: latitude"+currentLatidude);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==WORKPHOTO)
        {
            if (resultCode==RESULT_OK)
            {
                   btn_left.setEnabled(true);
             //   Toast.makeText(TrailActivity.this, "拍照成功", Toast.LENGTH_SHORT).show();
                WORKPIC= Bimp.compressBitmap(photoPath,600,600,true,"work");
//                showWorkDialog();

            }

        }
        if(requestCode==UNWORKPHOTO)
        {
            if (resultCode==RESULT_OK)
            {

                btn_left.setEnabled(true);
               // Toast.makeText(TrailActivity.this, "拍照成功", Toast.LENGTH_SHORT).show();
               UNWORKPIC= Bimp.compressBitmap(photoPath,600,600,true,"unwork");
                showNotWorkDialog();
            }

        }

    }

    public void getWorkStatus() {
        showProgressDialog("","正在获取数据...");
        Map<String, String> map = new HashMap<String, String>();
        String random = CommonTool.getRandom();
        map.put("random", random);
        String md5Sign = MD5Tool.Md5(MD5Tool.Md5(Constant.KEY + random)
                + MyApplication.getInstance().getUserId());
        map.put("signature", md5Sign);
        map.put("id", MyApplication.getInstance().getUserId());
        System.out.println("getWorkStatus" + map.toString());
        RequestParams params = new RequestParams(map);
        HttpTool.post(MyApplication.getInstance().getIP() + Constant.GETWORKSTATUS, params, new AsyncHttpResponseHandler() {

            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                String res = new String(arg2);
                System.out.println(res);
                 hideProgressDialog();
                JSONObject object = CommonTool.parseFromJson(res);
                String status = CommonTool.getJsonString(object, "status");
                if (status.equals("Y")) {
                    showShortToast("工作中!");
                    MyApplication.isWork = true;

                } else {
                    showShortToast("未工作!");
                    MyApplication.isWork = false;

                }
            }

            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                hideProgressDialog();
                System.out.println(new String(arg2));
            }
        });
    }


    private LocationListener gpslocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            currentLatlng = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng converLatlng = convertGPSToBaidu(currentLatlng);
            gpsLatitude = converLatlng.latitude;
            gpsLongitude = converLatlng.longitude;
            mCurrentAccracy = location.getAccuracy();
            if (location.getTime() != 0) {
                time = CommonTool.getStringDate(location.getTime() / 1000,
                        "yyyy-MM-dd HH:mm:ss");
            } else
                time = CommonTool.getStringDate(new Date(),
                        "yyyy-MM-dd HH:mm:ss");
            currentLatidude = converLatlng.latitude;
            currentLongitude = converLatlng.longitude;
            MyApplication.getInstance().latitude = gpsLatitude;
            MyApplication.getInstance().longitude = gpsLongitude;
            if (isFristLocation) {
                isFristLocation = false;
                LatLng ll = new LatLng(converLatlng.latitude,
                        converLatlng.longitude);
                MapStatusUpdate u = MapStatusUpdateFactory
                        .newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
            }
            setMyLoaction();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

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

    private boolean checkDataNormal(double latitude, double longitude) {

        String[] latTmp = String.valueOf(latitude).split("\\.");
        String[] lotTmp = String.valueOf(longitude).split("\\.");
        if (latTmp[1].length() >= 6 && lotTmp[1].length() >= 6) {
            return true;
        } else {
            return false;
        }
    }


    private void trailDistance() {
        for (int j = 0; j < myLatlng_list.size() - 1; j++) {
            LatLng mLatLng1 = myLatlng_list.get(j);
            LatLng mLatLng2 = myLatlng_list.get(j + 1);
            double str = DistanceUtil.getDistance(mLatLng1, mLatLng2);
            distance_list.add(str);
        }
        for (int i = 0; i < distance_list.size(); i++) {
            sum = sum + distance_list.get(i);
        }
        // 保留两位小数
        BigDecimal bg = new BigDecimal(sum / 1000);
        f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        tv_trail.setText(f1 + "Km");
    }

    @SuppressLint("SimpleDateFormat")
    private void insertLength() {
        String myTime = CommonTool.getStringDate(new Date(), "yyyy年MM月dd日");
        UserLength cUserLength = new UserLength();
        cUserLength.setUid(MyApplication.getInstance().getUserId());
        cUserLength.setMyTime(myTime);
        cUserLength.setMyLength(f1 + "Km");
        UserLengthDao cUserLengthDao = new UserLengthDao(TrailActivity.this);
        String uid = MyApplication.getInstance().getUserId();
        String oldtime = cUserLengthDao.findTime(uid, myTime);
        if (oldtime.equals("")) {
            cUserLengthDao.addLength(cUserLength);
        } else {
            cUserLengthDao.updateLength(f1 + "Km", oldtime);
        }
    }

    /**
     * 初始化定位 *
     */
    private void locationClient() {
        
        locationClient = new LocationClient(this);
        locationClient.registerLocationListener(myLocationListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开GPRS
        option.setScanSpan(500);// 设置定位请求时间
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度，默认值gcj02
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        locationClient.setLocOption(option);
        locationClient.start();
        Log.i(TAG, "locationClient: start");
    }

    public MyLocationListener myLocationListener = new MyLocationListener();
    boolean isFristLocation = true;

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            // map view 销毁后不在处理新接收的位置
            Log.i(TAG, "onReceiveLocation: 收到位置");
            if (bdLocation == null || mMapView == null) {
                return;
            }
            // 构造定位数据
            latitude = bdLocation.getLatitude();
            longitude = bdLocation.getLongitude();
            mCurrentAccracy = bdLocation.getRadius();
            String str = bdLocation.getAddrStr();
            time = CommonTool.getStringDate(new Date(),
                    "yyyy-MM-dd HH:mm:ss");
            currentLatidude = bdLocation.getLatitude();
            currentLongitude = bdLocation.getLongitude();
            MyApplication.getInstance().latitude = latitude;
            MyApplication.getInstance().longitude = longitude;

            MyApplication.getInstance().mapAddr.put("locationAddr", str);
            MyApplication.getInstance().address = str;
            MyApplication.getInstance().mapAddr.put("trailLatitude",
                    String.valueOf(latitude));
            MyApplication.getInstance().mapAddr.put("trailLongitude",
                    String.valueOf(longitude));
            if (isFristLocation) {
                isFristLocation = false;
                LatLng ll = new LatLng(bdLocation.getLatitude(),
                        bdLocation.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
            }
            setMyLoaction();
        }
    }

    protected void onStart() {
        // 开启图层定位
        mBaiduMap.setMyLocationEnabled(true);
        if (locationClient != null) {
            locationClient.start();
        }
        // 开启方向传感器
        if (myOrientationListener != null)
            myOrientationListener.start();
        super.onStart();
    }

    @Override
    protected void onStop() {
        // 关闭图层定位
        if (locationClient != null) {
            mBaiduMap.setMyLocationEnabled(false);
            locationClient.stop();
        }

        // 关闭方向传感器
        if (myOrientationListener != null)
            myOrientationListener.stop();
        super.onStop();
    }

    /**
     * 初始化方向传感器
     */
    private void initOritationListener() {
        myOrientationListener = new MyOrientationListener(
                getApplicationContext());
        myOrientationListener
                .setOnOrientationListener(new OnOrientationListener() {
                    @Override
                    public void onOrientationChanged(float x) {
                        mXDirection = (int) x;
                        // 构造定位数据
                        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                                .fromResource(R.drawable.location_marker);
                        MyLocationConfiguration config = new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker);
                        mBaiduMap.setMyLocationConfigeration(config);
                        if (currentLatidude == 0) {
                            return;
                        }
                        MyLocationData locData = new MyLocationData.Builder()
                                .accuracy(mCurrentAccracy)
                                .latitude(currentLatidude)
                                .direction(mXDirection)
                                .longitude(currentLongitude)
                                .build();
                        mBaiduMap.setMyLocationData(locData);
                    }
                });
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
        //   mTimer.schedule(mTimerTask,3000,3000);
        startGPS = 0;
        locationClient();
        findNet();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
        mHandler.removeMessages(UPDATE_TEXT);
        // mTimer.cancel();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectReceiver);
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
    }

    private boolean isLineShow = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_right:
                trailDataList();
//                if (myLatlng_list.size() < 3) {
//                    showShortToast("暂时没有轨迹数据");
//                    break;
//                } else {
//                    if (!isLineShow) {
//                        MapStatusUpdate u = MapStatusUpdateFactory
//                                .newLatLng(myLatlng_list.get(0));
//                        mBaiduMap.animateMapStatus(u);
//                        initOverlay();
//                        isLineShow = true;
//                    } else {
//                        mBaiduMap.clear();
//                        isLineShow = false;
//                    }
//                }
//                WifiManager     wifiManager=(WifiManager)super.getSystemService(Context.WIFI_SERVICE);
//                wifiManager.setWifiEnabled(false);
                Intent i=new Intent(TrailActivity.this,TrailPlayActvity.class);
                i.putExtra("latitude",latitude);
                i.putExtra("longitude",longitude);
                startActivity(i);
                break;
            case R.id.rl_net:
                Intent SettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(SettingIntent, 0);
                break;
            case R.id.nowTime:
                Intent in1 = new Intent(TrailActivity.this, DistanceActivity.class);
                startActivity(in1);
                break;
            case R.id.tv_trail:
                Intent in = new Intent(TrailActivity.this, TrailListActivity.class);
                startActivity(in);
                break;
            case R.id.btn_left:
                String str = btn_left.getText().toString();
                if (UnIntent.isNetworkAvailable(this)) {
                    locationClient();
                }
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    getCriteria();
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10,
                            gpslocationListener);
                }
                if (TextUtils.isEmpty(time)) {
                    showShortToast("没有定位成功,请您稍后.....");
                    return;
                }
                if ("开始工作".equals(str)) {
                    MyApplication.isWork=false;
                    showWorkDialog();
//                     btn_left.setEnabled(false);
//                    photoPath = FileUtils.getImgFilePath() + UUID.randomUUID() + ".jpg";
//                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT,
//                            Uri.fromFile(new File(photoPath)));
//
//                    intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
//                    Log.d("pic",photoPath);
//                    startActivityForResult(intent, WORKPHOTO);// 采用ForResult打开

                }
                if ("结束工作".equals(str)) {
                    MyApplication.isWork=true;
                    showNotWorkDialog();
//                    btn_left.setEnabled(false);
//                    photoPath = FileUtils.getImgFilePath() + UUID.randomUUID() + ".jpg";
//                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT,
//                            Uri.fromFile(new File(photoPath)));
//
//                    intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
//                    Log.d("pic",photoPath);
//                    startActivityForResult(intent, UNWORKPHOTO);// 采用
                }

//                    sendDatas();


                break;
            default:
                break;
        }
    }

    private void showNotWorkDialog() {

        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("结束工作后系统不再上报您的位置信息，是否继续？")
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                btn_left.setText("开始工作");
                                MyApplication.isWork=false;

                                btn_left.setBackgroundResource(R.color.low_gray);
                                tv_title.setText("我的位置");
                                btn_left.setEnabled(false);
                                sendDatas(1);
                                sp.edit().putBoolean(Constant.ISWORK, false).commit();
                                timeCount = new TimeCount(60000,1000);
                                timeCount.start();
                                timeCount.setCountDownTimerListener(new TimeCount.onCountDownTimerListener() {
                                    @Override
                                    public void onStart(long millisUntilFinished) {

                                    }

                                    @Override
                                    public void onFinished() {
                                        btn_left.setEnabled(true);
                                        btn_left.setBackgroundResource(R.color.red);
                                    }
                                });
                                MyApplication.getInstance().setServiceIsOpen(false);
                                stopService(new Intent(TrailActivity.this, MyLocationService.class));
                            }
                        })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        }).show();

    }

    TimeCount timeCount;

    private void showWorkDialog() {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("开始工作后系统将自动上报您的位置信息，是否继续？")
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                btn_left.setText("结束工作");
                                MyApplication.isWork=true;
                                btn_left.setBackgroundResource(R.color.low_gray);
                                tv_title.setText("正在工作");
                                btn_left.setEnabled(false);
                                sendDatas(0);
                                sp.edit().putBoolean(Constant.ISWORK, true).commit();
                                timeCount = new TimeCount(60000, 1000);
                                timeCount.start();
                                timeCount.setCountDownTimerListener(new TimeCount.onCountDownTimerListener() {
                                    @Override
                                    public void onStart(long millisUntilFinished) {

                                    }

                                    @Override
                                    public void onFinished() {
                                        btn_left.setEnabled(true);
                                        btn_left.setBackgroundResource(R.color.red);
                                    }
                                });
                               Intent i= new Intent(TrailActivity.this, MyLocationService.class);
                                i.putExtra("haspic",true);
                               i.putExtra("photopath",photoPath);
                                startService(i);
                                MyApplication.getInstance().setServiceIsOpen(true);

                                dialog.dismiss();
                            }
                        })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        }).show();
    }


    private void sendDatas(int i) {
        String path="";
        String id = MyApplication.getInstance().getUserId();
        String random = CommonTool.getRandom();
        String md5Sign = MD5Tool.Md5(MD5Tool.Md5(Constant.KEY + random) + id);
        final String str = "?id=" + id + "&random=" + random + "&signature=" + md5Sign;
         final    Map<String, String> map = new HashMap<String, String>();
            if (i == 0) {
                map.put("uWork", "Y");
//                path=WORKPIC;

        } else if (i == 1) {
            map.put("uWork", "N");
//                path=UNWORKPIC;
        }

        map.put("uLoti", currentLongitude + "");
        map.put("uLati", currentLatidude + "");
        map.put("uAlti", "0.0");
        // map.put("uAddr", str);
        map.put("uAddr", "");
        map.put("uSpeed", "0.0");
        map.put("uDirection", "0.0");
        map.put("uAccuracy", "0.0");
        map.put("battery", "0.0");
        map.put("uTime", time);
        map.put("uLocType", "0");
        map.put("net", IntenetUtil.getNetworkState(TrailActivity.this)+"");

        time = "";
        getServer(MyApplication.getInstance().getIP() + Constant.UPLOAD_TRAIL,
                map);// 上传数据
//

//       final Map<String,File>fileMap=new HashMap<String,File>();
//        fileMap.put("icon",new File(path));
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                try {
//                    method = "upload";
//                    String res = HttpTool.post02(MyApplication.getInstance()
//                                    .getIP() + Constant.UPLOAD_TRAILPIC+str,
//                            map, fileMap);
//                    if (res != null && !res.equals("")) {
//
//                           processResult(res);
//                    } else {
//                    Toast.makeText(TrailActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    // 请求服务器
    public void getServer(String url, Map<String, String> map) {
        if (map == null)
            map = new HashMap<String, String>();
        String random = CommonTool.getRandom();
        map.put("random", random);
        String md5Sign = MD5Tool.Md5(MD5Tool.Md5(Constant.KEY + random)
                + MyApplication.getInstance().getUserId());
        map.put("signature", md5Sign);
        map.put("id", MyApplication.getInstance().getUserId());
        System.out.println("上传数据" + map.toString());
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
        if (status.equals("true")) {
            //showShortToast("上报成功!");
            Toast.makeText(TrailActivity.this,"上报成功",Toast.LENGTH_SHORT).show();
        } else {
            String msg = CommonTool.getJsonString(object, "msg");
            Toast.makeText(TrailActivity.this,msg,Toast.LENGTH_SHORT).show();
        }
    }




    private void trailDataList() {
        locationDao = new MyLocationDao(getApplicationContext());
        String uid = MyApplication.getInstance().getUserId();
        myLocation_list = locationDao.findAll(uid);
        myLatlng_list = new ArrayList<LatLng>();
        if (!myLocation_list.isEmpty() && myLocation_list != null) {
            for (int i = 0; i < myLocation_list.size() - 1; i++) {
                Double lat1 = Double.parseDouble(myLocation_list.get(i)
                        .getLatitude());
                Double lng1 = Double.parseDouble(myLocation_list.get(i)
                        .getLongitude());
                LatLng latlng1 = new LatLng(lat1, lng1);
                myLatlng_list.add(latlng1);
            }
        }
    }

    public LatLng convertGPSToBaidu(LatLng sourceLatLng) {
        // 将GPS设备采集的原始GPS坐标转换成百度坐标
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        // sourceLatLng待转换坐标
        converter.coord(sourceLatLng);
        LatLng desLatLng = converter.convert();
        return desLatLng;
    }

    // 画出轨迹，并给起点和终点设置图标
    private void initOverlay() {
        mBaiduMap.clear();
        startLatlng = myLatlng_list.get(0);
        endLatlng = myLatlng_list.get(myLatlng_list.size() - 1);
        OverlayOptions ooA = new MarkerOptions().position(startLatlng)
                .icon(bdA).zIndex(9).draggable(true);
        Marker startMarker = (Marker) (mBaiduMap.addOverlay(ooA));
        OverlayOptions ooB = new MarkerOptions().position(endLatlng).icon(bdB)
                .zIndex(5);
        Marker endMarker = (Marker) (mBaiduMap.addOverlay(ooB));
        // 折线显示
        PolylineOptions ooPolyline = new PolylineOptions().width(6)
                .color(0xAAFF0000).points(myLatlng_list);
        mBaiduMap.addOverlay(ooPolyline);
    }

    // 异步获取数据
    private class trailAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            tv_trail.setText(String.valueOf(f1) + "Km");
        }

        @Override
        protected String doInBackground(String... params) {
            trailDataList();
            for (int j = 0; j < myLatlng_list.size() - 1; j++) {
                LatLng mLatLng1 = myLatlng_list.get(j);
                LatLng mLatLng2 = myLatlng_list.get(j + 1);
                double str = DistanceUtil.getDistance(mLatLng1, mLatLng2);

                distance_list.add(str);
            }
            for (int i = 0; i < distance_list.size(); i++) {
                sum = sum + distance_list.get(i);
            }
            // 保留两位小数
            BigDecimal bg = new BigDecimal(sum / 1000);
            f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            return String.valueOf(f1);
        }


        @Override
        protected void onPostExecute(String f1) {
            tv_trail.setText(f1 + "Km");
        }

    }

    /**
     * 检测GPS *
     */
    private void findGPSTask() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_TEXT:
                        findNet();
                        break;
                    case 88:
                        if (locationClient != null) {
                            locationClient.unRegisterLocationListener(myLocationListener);
                        }
                        locationClient();
                        break;
                    case 99:
                        getCriteria();
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10,
                                gpslocationListener);
                        break;

                }
            }
        };
        findNet();
    }

    int startGPS = 0;

    private void findNet() {
        startGPS++;
        if (startGPS == 10) {
            if (UnIntent.isNetworkAvailable(this)) {
                locationClient();
            }
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                getCriteria();
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10,
                        gpslocationListener);
            }
            startGPS = 0;
        }
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            rl_net.setVisibility(View.VISIBLE);
        } else {
            rl_net.setVisibility(View.GONE);
        }
        mHandler.sendEmptyMessageDelayed(UPDATE_TEXT, 1000);
    }

    class MyConnectReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (UnIntent.isNetworkAvailable(TrailActivity.this)) {
                mHandler.sendEmptyMessage(88);
            } else {
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    mHandler.sendEmptyMessage(99);
                }
            }
        }
    }

    public boolean deleteDirectory(String sPath) {
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        boolean flag=false;
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        //删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            //删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } //删除子目录
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }
}
