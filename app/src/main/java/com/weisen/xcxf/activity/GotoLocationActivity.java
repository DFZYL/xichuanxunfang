package com.weisen.xcxf.activity;

import java.util.ArrayList;
import java.util.List;

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
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.weisen.xcxf.R;
import com.weisen.xcxf.service.MyLocationService.MyLocationListener;
import com.weisen.xcxf.tool.MyOrientationListener;
import com.weisen.xcxf.tool.MyOrientationListener.OnOrientationListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class GotoLocationActivity extends Activity implements OnClickListener {

	private MapView mMapView = null;
	private BaiduMap mBaiduMap;
	private LocationClient locationClient;
	private LatLng latLng;
	private double latitude, longitude;
	private float mCurrentAccracy;// 当前的精度
	public MyLocationListener mMyLocationListener;// 定位的监听器
	private LocationMode mCurrentMode = LocationMode.FOLLOWING;// 当前定位的模式
	private volatile boolean isFristLocation = true;// 是否是第一次定位
	private MyOrientationListener myOrientationListener;// 方向传感器的监听器
	private int mXDirection;// 方向传感器X方向的值
	private BitmapDescriptor mIconMaker;// 初始化全局 bitmap 信息，不用时及时 recycle
	
	private ImageView iv_left;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gotolocation);

        BNRoutePlanNode  startPoint = null;

        iv_left = (ImageView) findViewById(R.id.iv_left);
		iv_left.setOnClickListener(this);
		mMapView = (MapView) findViewById(R.id.mv_map);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMyLocationEnabled(true);
		mIconMaker = BitmapDescriptorFactory.fromResource(R.drawable.location_marker);
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.zoomTo(17.0f);
		mBaiduMap.setMapStatus(mMapStatusUpdate);
		locationClient = new LocationClient(this);
		locationClient.registerLocationListener(new BDLocationListener() {

			@Override
			public void onReceiveLocation(BDLocation bdLocation) {
				// map view 销毁后不在处理新接收的位置
				if (bdLocation == null || mMapView == null)
					return;
				// 构造定位数据
				MyLocationData locData = new MyLocationData.Builder()
						.accuracy(bdLocation.getRadius())
						// 此处设置开发者获取到的方向信息，顺时针0-360
						.direction(mXDirection)
						.latitude(bdLocation.getLatitude())
						.longitude(bdLocation.getLongitude()).build();
				mCurrentAccracy = bdLocation.getRadius();
				
				latitude = bdLocation.getLatitude();
				longitude = bdLocation.getLongitude();
				latLng = new LatLng(latitude, longitude);

				// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
				mBaiduMap.setMyLocationData(locData);
				// 设置自定义图标
				BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
						.fromResource(R.drawable.location_marker);
				MyLocationConfiguration config = new MyLocationConfiguration(
						mCurrentMode, true, mCurrentMarker);
				mBaiduMap.setMyLocationConfigeration(config);
				// 第一次定位时，将地图位置移动到当前位置
				if (isFristLocation) {
					isFristLocation = false;
					MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
					mBaiduMap.animateMapStatus(u);
                    //两点之间划线
                    String lat = getIntent().getStringExtra("latitude");
                    String lng = getIntent().getStringExtra("longitude");
                    LatLng mLatLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));//目的地
                    OverlayOptions ooB = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.start)).position(latLng);
                    OverlayOptions ooA = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.end)).position(mLatLng);
                    Marker start = (Marker) mBaiduMap.addOverlay(ooA);
                    Marker end = (Marker) mBaiduMap.addOverlay(ooB);
                    List<LatLng> latlngList = new ArrayList<LatLng>();
                    latlngList.add(latLng);// 起点
                    latlngList.add(mLatLng);
                    // 折线显示
                    //	mBaiduMap.clear();
                    OverlayOptions ooPolyline = new PolylineOptions().width(5).color(0xAAFF0000).points(latlngList);
                    mBaiduMap.addOverlay(ooPolyline);

				}

			}
		});



		InitLocation();
		initOritationListener();
	}


	/** 定位模式 **/
	private void InitLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开GPRS
		option.setScanSpan(60*1000);// 设置定位请求时间
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度，默认值gcj02
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		locationClient.setLocOption(option);
	}

	/** 初始化定位 **/
	private void locationClient() {

	}

	protected void onStart() {
		// 开启图层定位
		mBaiduMap.setMyLocationEnabled(true);
		if (!locationClient.isStarted()) {
			locationClient.start();
		}
		// 开启方向传感器
		myOrientationListener.start();
		super.onStart();
	}

	@Override
	protected void onStop() {
		// 关闭图层定位
		mBaiduMap.setMyLocationEnabled(false);
		locationClient.stop();

		// 关闭方向传感器
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
						MyLocationData locData = new MyLocationData.Builder()
								.accuracy(mCurrentAccracy)
								// 此处设置开发者获取到的方向信息，顺时针0-360
								.direction(mXDirection).latitude(latitude)
								.longitude(longitude).build();
						// 设置定位数据
						mBaiduMap.setMyLocationData(locData);
						// 设置自定义图标
						BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
								.fromResource(R.drawable.location_marker);
						MyLocationConfiguration config = new MyLocationConfiguration(
								mCurrentMode, true, mCurrentMarker);
						mBaiduMap.setMyLocationConfigeration(config);

					}
				});
	}

	@Override
	protected void onResume() {
		
		super.onResume();
		mMapView.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_left:
			finish();
			break;

		default:
			break;
		}

	}

}
