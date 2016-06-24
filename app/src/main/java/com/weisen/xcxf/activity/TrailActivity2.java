package com.weisen.xcxf.activity;

import android.view.View;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.model.LatLng;
import com.weisen.xcxf.R;

/**
 * Created by skn on 2016/1/15/15:16.
 */
public class TrailActivity2 extends BaseActivity {

    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
    private LocationClient locationClient;
    @Override
    protected void initView() {
        super.initView();
        setContentView(R.layout.activity_trail);
        mMapView = (MapView) findViewById(R.id.mv_map);
        mBaiduMap = mMapView.getMap();
        findViewById(R.id.btn_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(str==null){
                    showShortToast("没有定位成功!!!");
                }else{
                    showShortToast(str);
                }
            }
        });
//        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.zoomTo(18.0f);
//        mBaiduMap.setMapStatus(mMapStatusUpdate);
//        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
//                .fromResource(R.drawable.location_marker);
//        MyLocationConfiguration config = new MyLocationConfiguration(
//                mCurrentMode, true, mCurrentMarker);
//        mBaiduMap.setMyLocationConfigeration(config);
        mBaiduMap.setMyLocationEnabled(true);
        locationClientStart();
    }
    private void locationClientStart() {
        locationClient = new LocationClient(this);
        locationClient.registerLocationListener(myLocationListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开GPRS
        option.setScanSpan(1000);// 设置定位请求时间
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度，默认值gcj02
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        locationClient.setLocOption(option);
        locationClient.start();
    }
    String str;
    public MyLocationListener myLocationListener =  new MyLocationListener();
    boolean isFristLocation = true;
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            str = bdLocation.getAddrStr();
            showShortToast("请现在的位置在" + str);
            if (isFristLocation) {
                isFristLocation = false;
                LatLng ll = new LatLng(bdLocation.getLatitude(),
                        bdLocation.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
            }
        }
    }

}
