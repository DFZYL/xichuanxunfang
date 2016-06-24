package com.weisen.xcxf.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.HeatMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.weisen.xcxf.R;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.MyLocation;
import com.weisen.xcxf.bean.MyLocationDao;
import com.weisen.xcxf.bean.UserLengthDao;
import com.weisen.xcxf.tool.Bimp;
import com.weisen.xcxf.widget.MonthDateView;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.weisen.xcxf.tool.CommonTool;
import com.weisen.xcxf.widget.ZoomControlsView;

/**
 * Created by dfzha on 2016/3/24.
 */
public class TrailPlayActvity extends BaseActivity  implements OnGetGeoCoderResultListener{
private MapView mapView;
    GeoCoder geocoder;
    private Button play,trailplay;
    private ImageView back;
    private BaiduMap mBaiduMap;
    private BitmapDescriptor icon;
    private Marker mMarker;
    private Integer index = 0;
    private List<MyLocation> myLocation_list;//定位信息列表
    private   MyLocationDao myLocationDao;
    private List<LatLng> points=new ArrayList<LatLng>();
    private String uid;
    private List<LatLng> points2=new ArrayList<LatLng>();
    private boolean flag;
    private double latitude,longitude;
    Bitmap bitmapOrg;
    private ImageView iv_left;
    private ImageView iv_right,iv_down;
    private TextView tv_date,tv_week,tv_today;
    private  TextView tv_time,tv_address,tv_locationtype,tv_nopoints;
    private Animation calendar_down,calendar_up;
    private MonthDateView monthDateView;
    private LinearLayout linearlayout_calendar;
    private RelativeLayout relativelayout_calendartitle;
    private boolean Isdown=false;
    private ZoomControlsView zcvZomm;
    @Override
    protected void initView() {

        setContentView(R.layout.activity_trailplay);


//         初始化搜索模块，注册事件监听
        geocoder = GeoCoder.newInstance();
        geocoder.setOnGetGeoCodeResultListener(this);

        mapView=(MapView)findViewById(R.id.map_view);
        mBaiduMap=mapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);//设置地图缩放级别
        mBaiduMap.setMapStatus(msu);
        mapView.showZoomControls(false);//隐藏缩放控件
        relativelayout_calendartitle=(RelativeLayout)findViewById(R.id.layout_calendar_title);
        iv_left = (ImageView) findViewById(R.id.iv_leftd);
        iv_right = (ImageView) findViewById(R.id.iv_right);
//        iv_lefts=(ImageView)findViewById(R.id.iv_lefts);
//        iv_rights=(ImageView)findViewById(R.id.iv_rights);
        iv_down=(ImageView)findViewById(R.id.iv_down);
        monthDateView = (MonthDateView) findViewById(R.id.monthDateView);
        tv_date = (TextView) findViewById(R.id.date_text);
        tv_week  =(TextView) findViewById(R.id.week_text);
        tv_today = (TextView) findViewById(R.id.tv_today);
        tv_time=(TextView)findViewById(R.id.text_time);
        tv_address=(TextView)findViewById(R.id.text_address);
        tv_locationtype=(TextView)findViewById(R.id.text_locationtype);
        tv_nopoints=(TextView)findViewById(R.id.tv_nopoints);//没有轨迹记录

        linearlayout_calendar=(LinearLayout)findViewById(R.id.layout_calendar);
        monthDateView.setTextView(tv_date,tv_week);
        Calendar calendar = Calendar.getInstance();
        monthDateView.setSelectYearMonth(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DATE));

        setOnlistener();
        linearlayout_calendar.setVisibility(View.VISIBLE);
        linearlayout_calendar.setVisibility(View.INVISIBLE);



        calendar_down=new ScaleAnimation(1,1,0,1);
        calendar_down.setDuration(300);
        DecelerateInterpolator i =new DecelerateInterpolator();
        calendar_down.setInterpolator(i);;
        calendar_down.setFillAfter(false);
        calendar_down.setZAdjustment(android.view.animation.Animation.ZORDER_BOTTOM);

        calendar_up=new ScaleAnimation(1,1,1,0);
        calendar_up.setDuration(300);
        calendar_up.setInterpolator(i);
        calendar_up.setFillAfter(false);
        calendar_up.setZAdjustment(android.view.animation.Animation.ZORDER_BOTTOM);


        back=(ImageView)findViewById(R.id.iv_left);


        play=(Button)findViewById(R.id.btn_trailplaydown);//播放按钮

        //设置头像
        String img1 =  MyApplication.getInstance().getHeadPic();
        String[] heads = img1.split("/");
        System.out.println(heads[heads.length - 1].substring(0, 7));
        if (img1 != null && !img1.equals("") && !heads[heads.length-1].substring(0,7).equals("default")
           && new File(ImageLoader.getInstance().
                    getDiscCache().get(img1).getPath().toString()).exists()
                ){


          //  Toast.makeText(TrailPlayActvity.this,"获取头像"+ImageLoader.getInstance().
               //     getDiscCache().get(img1).getPath().toString(),Toast.LENGTH_SHORT).show();
            bitmapOrg=BitmapFactory.decodeFile(ImageLoader.getInstance().
                    getDiscCache().get(img1).getPath().toString());
            bitmapOrg= Bimp.toRoundBitmap(bitmapOrg);
            bitmapOrg=Bimp.FIXxy(80,80,bitmapOrg);
        }else {
            bitmapOrg = BitmapFactory.decodeResource(getResources(),
                    R.drawable.icon_guiji);
        }
        //头像角度有偏差，设置修正
        Matrix matrix = new Matrix();
            matrix.postRotate(-45);
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0,
                    bitmapOrg.getWidth(), bitmapOrg.getHeight(), matrix, true);
        icon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);//

    }


    private void setOnlistener(){
        iv_left.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                monthDateView.onLeftClick();
                initpoints(Date(monthDateView.strdate));
              //  Toast.makeText(TrailPlayActvity.this,tv_date.getText(),Toast.LENGTH_SHORT).show();

            }
        });

         //日历的展开和合上
        iv_down.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if(Isdown)
                {
                    linearlayout_calendar.setAnimation(calendar_up);
                    relativelayout_calendartitle.setVisibility(View.INVISIBLE);
                    linearlayout_calendar.setVisibility(View.INVISIBLE);
                    Isdown=false;
                    iv_down.setImageResource(R.drawable.down_arrow);
                    relativelayout_calendartitle.setVisibility(View.VISIBLE);


                }
                else {
                    linearlayout_calendar.setVisibility(View.VISIBLE);
                    Isdown=true;
                    iv_down.setImageResource(R.drawable.up_arrow);
                    linearlayout_calendar.startAnimation(calendar_down);
                }



            }
        });

        iv_right.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                monthDateView.onRightClick();
                initpoints(Date(monthDateView.strdate));
              //  Toast.makeText(TrailPlayActvity.this,tv_date.getText(),Toast.LENGTH_SHORT).show();
            }
        });



//        tv_today.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                monthDateView.setTodayToView();
//                initpoints(Date(monthDateView.strdate));
//              //  Toast.makeText(TrailPlayActvity.this,tv_date.getText(),Toast.LENGTH_SHORT).show();
//            }
//        });

    }

    public void initOverlay()//初始化轨迹

    {
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(points.get(0));
//        mBaiduMap.setMapStatus(u);
mBaiduMap.animateMapStatus(u);
    }

    //初始化轨迹点
    private  void initpoints(String time)
    {


        points.clear();
        points2.clear();
        mBaiduMap.clear();
        flag=false;
        play.setText("播放轨迹");
        index=0;
        myLocation_list = myLocationDao.findAllsbytime(uid,time);
        showpoints();
    }
//在地图上显示轨迹点
    private void showpoints() {

        if(myLocation_list.size()>2)
        {

            int net=0,gps=0;

            for(MyLocation m:myLocation_list)
            {

//                Log.d(m.getLatitude(),m.getLongitude());
                points.add(new LatLng(Double.parseDouble(m.getLatitude())
                        ,Double.parseDouble(m.getLongitude())));
                 if (m.getLocType().equals("0")){
                     net+=1;
                 }else
                 {
                     gps+=1;
                 }

            }
            MapStatusUpdate u= MapStatusUpdateFactory.newLatLng(points.get(myLocation_list.size()-1));
//            mBaiduMap.setMapStatus(u);
            mBaiduMap.animateMapStatus(u);
            OverlayOptions ooA = new MarkerOptions()
                    .position(new LatLng(points.get(myLocation_list.size()-1).latitude,
                            points.get(myLocation_list.size()-1).longitude))
                    .icon(icon).draggable(true).anchor(0.5f,0.5f);
            mMarker = (Marker) (mBaiduMap.addOverlay(ooA));
            tv_nopoints.setVisibility(View.INVISIBLE);
            tv_locationtype.setVisibility(View.VISIBLE);
            tv_address.setVisibility(View.VISIBLE);
            tv_time.setVisibility(View.VISIBLE);
          play.setVisibility(View.VISIBLE);
            for(LatLng p:points)
            {
                OverlayOptions  cs = new DotOptions().radius(8)
                        .color(0xFF3684D7).center(p).zIndex(2);
                mBaiduMap.addOverlay(cs);

            }
            String s1[]=myLocation_list.get(0).getTime().split("\\s");
            String s2[]=myLocation_list.get(myLocation_list.size()-1).getTime().split("\\s");
            tv_time.setText(s1[1]+'~'+s2[1]);
            tv_locationtype.setText(" GPS："+Integer.toString(gps)+"  基站："+Integer.toString(net));
            CoordinateConverter coordinateConverter=new CoordinateConverter();
            coordinateConverter.coord(points.get(points.size()-1));
          coordinateConverter.from(CoordinateConverter.CoordType.COMMON);
            geocoder.reverseGeoCode(new ReverseGeoCodeOption().location(points.get(points.size()-1)));

        }
        else if(myLocation_list.size()>0)
        {


            int net=0,gps=0;

            for(MyLocation m:myLocation_list)
            {

//                Log.d(m.getLatitude(),m.getLongitude());
                points.add(new LatLng(Double.parseDouble(m.getLatitude())
                        ,Double.parseDouble(m.getLongitude())));
                if (m.getLocType().equals("0")){
                    net+=1;
                }else
                {
                    gps+=1;
                }

            }


            MapStatusUpdate u= MapStatusUpdateFactory.newLatLng(points.get(myLocation_list.size()-1));
//            mBaiduMap.setMapStatus(u);
            mBaiduMap.animateMapStatus(u);
            OverlayOptions ooA = new MarkerOptions()
                    .position(new LatLng(points.get(myLocation_list.size()-1).latitude,
                            points.get(myLocation_list.size()-1).longitude))
                    .icon(icon).draggable(true).anchor(0.5f,0.5f);
            mMarker = (Marker) (mBaiduMap.addOverlay(ooA));
            tv_nopoints.setVisibility(View.INVISIBLE);
            tv_locationtype.setVisibility(View.VISIBLE);
            tv_address.setVisibility(View.VISIBLE);
            tv_time.setVisibility(View.VISIBLE);

            for(LatLng p:points)
            {
                OverlayOptions  cs = new DotOptions().radius(8)
                        .color(0xFF3684D7).center(p).zIndex(2);
                mBaiduMap.addOverlay(cs);

            }
            String s1[]=myLocation_list.get(0).getTime().split("\\s");
            String s2[]=myLocation_list.get(myLocation_list.size()-1).getTime().split("\\s");
            tv_time.setText(s1[1]+'~'+s2[1]);
            tv_locationtype.setText(" GPS数据："+Integer.toString(gps)+"  网络数据："+Integer.toString(net));
            CoordinateConverter coordinateConverter=new CoordinateConverter();
            coordinateConverter.coord(points.get(points.size()-1));
            coordinateConverter.from(CoordinateConverter.CoordType.COMMON);
            geocoder.reverseGeoCode(new ReverseGeoCodeOption().location(points.get(points.size()-1)));

        }
        else
        {
            tv_nopoints.setVisibility(View.VISIBLE);
            tv_time.setVisibility(View.INVISIBLE);
            tv_address.setVisibility(View.INVISIBLE);
            tv_locationtype.setVisibility(View.INVISIBLE);
            play.setVisibility(View.INVISIBLE);
            MapStatusUpdate u=MapStatusUpdateFactory.newLatLng(new LatLng(latitude,longitude));
//            mBaiduMap.setMapStatus(u);
            mBaiduMap.animateMapStatus(u);

        }
    }

    Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1) {

                if(points.size()>=3) {

                    flag = true;
                    play.setText("暂停播放");
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            start();
                        }
                    }).start();
                }
                else
                {
                    relativelayout_calendartitle.setVisibility(View.VISIBLE);
                    Toast.makeText(TrailPlayActvity.this,"暂无轨迹记录",Toast.LENGTH_SHORT).show();
                }
            }
            if (msg.what == 2) {
                flag = false;
            }
            if (msg.what == 3) {
                play.setText("重新播放");
                points2.clear();
                relativelayout_calendartitle.setVisibility(View.VISIBLE);
                flag = false;
            }
            return false;
        }
    });



//轨迹播放控制，采用递归
    public void start() {
        if (flag) {
            if (mMarker != null) {
                mMarker.remove();
            }
//            Matrix matrix = new Matrix();
//            matrix.postRotate(0);
//            Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0,
//                    bitmapOrg.getWidth(), bitmapOrg.getHeight(), matrix, true);


            OverlayOptions ooA = new MarkerOptions()
                    .position(new LatLng(points.get(index).latitude,
                            points.get(index).longitude))
                    .icon(icon).draggable(true).anchor(0.5f,0.5f);

            mMarker = (Marker) (mBaiduMap.addOverlay(ooA));

            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(points
                    .get(index));
            mBaiduMap.setMapStatus(u);
            if (points2.size() <= 1) {

                points2.add(points.get(index));
                points2.add(points.get(index + 1));
            } else {
                points2.clear();
                points2.add(points.get(index-1));
                points2.add(points.get(index));

                OverlayOptions s = new PolylineOptions().width(15)
                        .color(0xFF0099cc).points(points2);
                mBaiduMap.addOverlay(s);
            }


               if(index==0) {
                   OverlayOptions    cs = new DotOptions().radius(12)
                           .color(0xFF00cc00).center(points.get(index)).zIndex(2);
                   mBaiduMap.addOverlay(cs);
               }else if(index==points.size()-1)
               {
                   OverlayOptions  cs = new DotOptions().radius(12)
                           .color(0xFFFF3300).center(points.get(index)).zIndex(2);
                   mBaiduMap.addOverlay(cs);
               }
               else
               {

                   OverlayOptions  cs = new DotOptions().radius(8)
                           .color(0xFF3684D7).center(points.get(index)).zIndex(2);
                   mBaiduMap.addOverlay(cs);
               }

            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            index++;
            if (index != points.size()) {
                start();
            } else {
                Message message = new Message();
                message.what = 3;
                handler.sendMessage(message);
            }
        }
    }
    //按钮点击
    public void resetOverlay(View view) {
        Button button = (Button) view;
        relativelayout_calendartitle.setVisibility(View.INVISIBLE);
        linearlayout_calendar.setVisibility(View.INVISIBLE);
        Isdown=false;
        iv_down.setImageResource(R.drawable.down_arrow);
  //flag 表示是否在播放中
        if (!flag) {

            if (index == points.size()&&points.size()>=3) {
                mBaiduMap.clear();
                index = 0;
                initOverlay();// 初始化
            }

            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        } else {
            button.setText("继续播放");
            Message message = new Message();
            message.what = 2;
            handler.sendMessage(message);
        }
    }
    @Override
    protected void initEvent() {

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetOverlay(v);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        geocoder.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void initData() {

        Intent i=getIntent();
        latitude=i.getDoubleExtra("latitude",0);
        longitude=i.getDoubleExtra("longitude",0);

        myLocationDao = new MyLocationDao(TrailPlayActvity.this);
        uid = MyApplication.getInstance().getUserId();
        myLocation_list = myLocationDao.findAllsbytime(uid,CommonTool.getStringDate(new Date(),"yyyy-MM-dd"));
        showpoints();


        monthDateView.setDateClick(new MonthDateView.DateClick() {

            @Override
            public void onClickOnDate() {

                initpoints(Date(monthDateView.strdate));

            }
        });
        zcvZomm=(ZoomControlsView) findViewById(R.id.zcv_zoom);
        zcvZomm.setMapView(mapView);//设置百度地图控件
    }

    /***
     *   将时间转化为固定格式
     * @param time
     * @return
     */
    public  String  Date(String time) {

        time=  time.replace("年","-");
         time=time.replace("月","-");
        time =time.replace("日","");
        String s[] = time.split("-");
        String s1 = s[0] + "-";
        if (s[1].length() == 1) {
            s1 += "0" + s[1] + "-";
        } else {
            s1 += s[1] + "-";
        }

        if (s[2].length() == 1) {
            s1 += "0" + s[2];
        } else
        {
            s1+=s[2];
    }
        return  s1;
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {


    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {

        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {

            tv_address.setText("请联网获取位置");
            return;
        }
        tv_address.setText(result.getAddress());
    }
}
