package com.weisen.xcxf.activity;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.adapter.TrailListAdapter;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.MyLocation;
import com.weisen.xcxf.bean.MyLocationDao;
import com.weisen.xcxf.service.StepDetector;
import com.weisen.xcxf.tool.UnIntent;


public class TrailListActivity extends BaseActivity implements OnClickListener {
    private LayoutInflater inflater;
    private ListView trail_list;
    private MyLocationDao myLocationDao;
    private List<MyLocation> myLocation_list;
    private TrailListAdapter adapter;
    private TextView btn1, btn2, btn3;
    private String uid;
    private ImageView iv_left;
    private TextView gpsStatus, mobileStatus, wifiStatus, dataFrom, data, isGetting, time_clock, count, move, time;
    private long beginTime;
    private UpdateLocationReceiver updateLocationReceiver;
    private LocationManager lm;
    private String isMoved = "状态:移动";
    private SensorManager mSensorManager;// 传感器服务
    private StepDetector detector;// 传感器监听对象

    @Override
    protected void initView() {
        setContentView(R.layout.activity_trail_list);
        inflater = LayoutInflater.from(this);
        beginTime = System.currentTimeMillis();
        trail_list = (ListView) findViewById(R.id.lv_report);
        mobileStatus = (TextView) findViewById(R.id.mobile_status);
        wifiStatus = (TextView) findViewById(R.id.wifi_status);
        isGetting = (TextView) findViewById(R.id.is_getting);
        move = (TextView) findViewById(R.id.is_move);
        dataFrom = (TextView) findViewById(R.id.data_from);
        gpsStatus = (TextView) findViewById(R.id.gps_status);
        time_clock = (TextView) findViewById(R.id.time_clock);
        count = (TextView) findViewById(R.id.count);
        data = (TextView) findViewById(R.id.data);
        btn1 = (TextView) findViewById(R.id.btn1);
        btn1.setOnClickListener(this);
        btn2 = (TextView) findViewById(R.id.btn2);
        btn3 = (TextView) findViewById(R.id.btn3);
        btn3.setOnClickListener(this);
        btn2.setOnClickListener(this);
        iv_left = (ImageView) findViewById(R.id.iv_left);
        iv_left.setOnClickListener(this);
        myLocationDao = new MyLocationDao(TrailListActivity.this);
        uid = MyApplication.getInstance().getUserId();
        myLocation_list = myLocationDao.getUnUploadList(uid);
        time = (TextView) findViewById(R.id.time);
        if (myLocation_list.size() != 0 && myLocation_list != null) {
            adapter = new TrailListAdapter(myLocation_list, inflater);
            trail_list.setAdapter(adapter);
        }

        btn2.setText("已上传 (" + myLocationDao.getCount(uid) + ")");
        btn1.setText("未上传 (" + myLocationDao.getUnUploadCount(uid) + ")");
        btn3.setText("本地数据 (" + (myLocationDao.getUnUploadCount(uid) + myLocationDao.getCount(uid)) + ")");
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        updateView();
        updateLocationReceiver = new UpdateLocationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.BROADCAST_UPDATE_LOCATION);
        registerReceiver(updateLocationReceiver, filter);

        updataLocationTypeReceiver = new UpdataLocationTypeReceiver();
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(Constant.UPDATEUPDATELOCATIONTYPE);
        registerReceiver(updataLocationTypeReceiver, filter2);

        getLocationReceiver = new GetLocationReceiver();
        IntentFilter filter3 = new IntentFilter();
        filter3.addAction(Constant.GETLOACTIONACTION);


        registerReceiver(getLocationReceiver, filter3);detector = new StepDetector(this);
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        // 注册传感器，注册监听器
        mSensorManager.registerListener(detector,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);

//
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        while (true) {
//                            String s;
//                            if (detector.isMoved) {
//                                s = "状态:移动";
//                            } else {
//                                s = "状态:静止";
//                            }
//                            move.setText(s);
//                        }
//                    }
//                });
//
//
//        };
//
//    }).start();
    }
    class UpdateLocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent intent) {

            if (intent.getAction().equals(Constant.BROADCAST_UPDATE_LOCATION)) {
                btn2.setText("已上传 (" + myLocationDao.getCount(uid) + ")");
                btn1.setText("未上传 (" + myLocationDao.getUnUploadCount(uid) + ")");
                btn3.setText("本地数据 (" + (myLocationDao.getUnUploadCount(uid) + myLocationDao.getCount(uid)) + ")");
            }
        }
    }

    int mCount;
    String dataStr = "";
    UpdataLocationTypeReceiver updataLocationTypeReceiver;

    class UpdataLocationTypeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constant.UPDATEUPDATELOCATIONTYPE)) {
                dataStr = intent.getStringExtra("date");
                mCount = intent.getIntExtra("count", 200);
                isMoved = intent.getStringExtra("isMoved");
                updateView();
            }
        }
    }

    String timeStr = "";
    String locationStr;
    String dataFromStr;
    GetLocationReceiver getLocationReceiver;

    class GetLocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constant.GETLOACTIONACTION)) {
                locationStr = intent.getStringExtra("location");
                dataFromStr = intent.getStringExtra("from");
                timeStr = "时间:" + intent.getStringExtra("time");
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        locationStr = "";
//                        dataFromStr = "";
//                        updateView();
//                    }
//                },5000);
                updateView();
            }
        }
    }

    private void updateView() {
        if (mCount == 0) {
            time_clock.setText("获取:等待");
        } else {
            time_clock.setText("下次:" + mCount + "米");
        }

    if(UnIntent.isMobileNetworkAvailable(this))

    {
        mobileStatus.setText("MOB:开");
    }

    else

    {
        mobileStatus.setText("MOB:关");
    }

    if(UnIntent.isWifiConnected(this))

    {
        wifiStatus.setText("WIFI:开");
    }

    else

    {
        wifiStatus.setText("WIFI:关");
    }

    if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER))

    {
        gpsStatus.setText("GPS:开");
    }

    else

    {
        gpsStatus.setText("GPS:关");
    }

    move.setText(isMoved);
    count.setText("成功:"+(myLocationDao.getUnUploadCount(uid)+myLocationDao.getCount(uid)));
    isGetting.setText(dataStr);
    dataFrom.setText(dataFromStr);
    data.setText(locationStr);
    time.setText(timeStr);
}

    @Override
    protected void onResume() {
        super.onResume();
        btn2.setText("已上传(" + myLocationDao.getCount(uid) + ")");
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (updateLocationReceiver != null)
            unregisterReceiver(updateLocationReceiver);
        if (updataLocationTypeReceiver != null)
            unregisterReceiver(updataLocationTypeReceiver);
        if (getLocationReceiver != null) {
            unregisterReceiver(getLocationReceiver);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                myLocation_list = myLocationDao.getUnUploadList(uid);
                adapter = new TrailListAdapter(myLocation_list, inflater);
                btn1.setTextColor(getResources().getColor(R.color.comen_blue));
                btn2.setTextColor(getResources().getColor(R.color.black));
                btn3.setTextColor(getResources().getColor(R.color.black));
//                btn1.setBackgroundColor(getResources().getColor(R.color.mblue));
//                btn2.setBackgroundColor(getResources().getColor(R.color.white));
//                btn3.setBackgroundColor(getResources().getColor(R.color.white));
                trail_list.setAdapter(adapter);
                break;
            case R.id.btn2:
                myLocation_list = myLocationDao.findAlls(uid);
                btn2.setTextColor(getResources().getColor(R.color.comen_blue));
                btn1.setTextColor(getResources().getColor(R.color.black));
                btn3.setTextColor(getResources().getColor(R.color.black));
                adapter = new TrailListAdapter(myLocation_list, inflater);
                trail_list.setAdapter(adapter);
                break;
            case R.id.btn3:
                btn3.setTextColor(getResources().getColor(R.color.comen_blue));
                btn1.setTextColor(getResources().getColor(R.color.black));
                btn2.setTextColor(getResources().getColor(R.color.black));
                myLocation_list = myLocationDao.findAll(uid);
                adapter = new TrailListAdapter(myLocation_list, inflater);
                trail_list.setAdapter(adapter);
                break;


            case R.id.iv_left:
                finish();
                break;
            default:
                break;
        }

    }

}
