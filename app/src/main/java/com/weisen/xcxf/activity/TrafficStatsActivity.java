package com.weisen.xcxf.activity;

import java.util.Calendar;

import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.adapter.DatabaseAdapter;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.MyLocationDao;
import com.weisen.xcxf.tool.TrafficMonitoring;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TrafficStatsActivity extends Activity implements OnClickListener{
	private MyLocationDao myLocationDao;
	private String uid;
	private Button btnback;
	private TextView locationData1, locationData2, GPday, GPmon;
	DatabaseAdapter dbAdapter;
	Calendar currentCa;
    SharedPreferences sp;
	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		public void run() {
			this.update();
			handler.postDelayed(this, 1000 * 60);// 间隔120秒
		}

		void update() {
			GPday = (TextView) findViewById(R.id.todayGP);
			GPmon = (TextView) findViewById(R.id.monthGP);
			currentCa = Calendar.getInstance();
			int year = currentCa.get(Calendar.YEAR);
			int month = currentCa.get(Calendar.MONTH) + 1;
			int day = currentCa.get(Calendar.DATE);
			long dup = dbAdapter.calculateUp(year, month, day, 1);
			long ddw = dbAdapter.calculateDw(year, month, day, 1);
			long ddw1 = dbAdapter.calculate(year, month, day, 1);

			String gjtz = TrafficMonitoring.convertTraffic(ddw1);
			GPday.setText(gjtz);
            long  trafficstats = dbAdapter.calculateForMonth(year,month, 1);
            String gbyz = TrafficMonitoring.convertTraffic(liuliang+trafficstats);
            GPmon.setText(gbyz);
		}
	};
    long liuliang;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trafficstats);
		locationData1 = (TextView) findViewById(R.id.locationData2-1);
		locationData2 = (TextView) findViewById(R.id.locationData2);
		myLocationDao = new MyLocationDao(TrafficStatsActivity.this);
		uid = MyApplication.getInstance().getUserId();
		locationData1.setText(String.valueOf(myLocationDao.getCount(uid))+"条");
		locationData2.setText(String.valueOf(myLocationDao.getUnUploadCount(uid))+"条");
		btnback = (Button) findViewById(R.id.backLiuLiang);
		btnback.setOnClickListener(this);
        sp = getSharedPreferences(Constant.APP_SP,MODE_PRIVATE);
        liuliang = sp.getLong(Constant.LIULIANG,0);
		dbAdapter = new DatabaseAdapter(this);
		dbAdapter.open();
		handler.postDelayed(runnable, 1000 * 6);
	}
	
	@Override
	protected void onResume() {
		
		super.onResume();
		GPday = (TextView) findViewById(R.id.todayGP);
		GPmon = (TextView) findViewById(R.id.monthGP);
		currentCa = Calendar.getInstance();
		int year = currentCa.get(Calendar.YEAR);
		int month = currentCa.get(Calendar.MONTH) + 1;
		int day = currentCa.get(Calendar.DATE);
//		long dup = dbAdapter.calculateUp(year, month, day, 1);
//		long ddw = dbAdapter.calculateDw(year, month, day, 1);
		long ddw1 = dbAdapter.calculate(year, month, day, 1);
		String gjtz = TrafficMonitoring.convertTraffic(ddw1);
		GPday.setText(gjtz);
        long  trafficstats = dbAdapter.calculateForMonth(year,month, 1);
        String gbyz = TrafficMonitoring.convertTraffic(liuliang+trafficstats);
		GPmon.setText(gbyz);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.backLiuLiang:
			finish();
			break;

		default:
			break;
		}
		
	}
}
