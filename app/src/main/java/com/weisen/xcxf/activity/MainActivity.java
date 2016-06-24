package com.weisen.xcxf.activity;

import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.MyLocationDao;
import com.weisen.xcxf.service.MonitoringService;
import com.umeng.update.UmengUpdateAgent;
import com.weisen.xcxf.service.MyLocationService;

public class MainActivity extends TabActivity {

	private TabHost tabHost;
	private View tab0, tab1, tab2, tab3;
	private Intent intent0, intent1, intent2, intent3;
	private static final String TAB0 = "tab0", TAB1 = "tab1", TAB2 = "tab2",TAB3 = "tab3";
	private LocalActivityManager manager;
	private LinearLayout ll_tabs;
	public static MainActivity mainActivity;
	private SharedPreferences preferences;
	private long mExitTime;
	private LocationManager lm;
	private CaseReportActivity mActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		initView();
	}

	@SuppressWarnings("deprecation")
	protected void initView() {
		setContentView(R.layout.activity_main);
		initPush();
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		UmengUpdateAgent.update(this);// 自动更新
		preferences = getSharedPreferences(Constant.APP_SP, MODE_PRIVATE);
		manager = new LocalActivityManager(this, true);
		manager.dispatchCreate(null);
		ll_tabs = (LinearLayout) findViewById(R.id.ll_tabs);
		// tabHost = (TabHost) findViewById(R.id.tb_host);
		// tabHost.setup();
		// tabHost.setup(manager);
		tabHost = getTabHost();

		tab0 = LayoutInflater.from(this).inflate(R.layout.tab_widget, null);
		tab0.findViewById(R.id.iv_tab).setBackgroundResource(R.drawable.tab_message);
		((TextView) tab0.findViewById(R.id.tv_tab)).setText(getStringResource(R.string.title_trail));
		
		tab1 = LayoutInflater.from(this).inflate(R.layout.tab_widget, null);
		tab1.findViewById(R.id.iv_tab).setBackgroundResource(R.drawable.tab_case);
		((TextView) tab1.findViewById(R.id.tv_tab)).setText(getStringResource(R.string.title_case_report));

		tab2 = LayoutInflater.from(this).inflate(R.layout.tab_widget, null);
		tab2.findViewById(R.id.iv_tab).setBackgroundResource(R.drawable.tab_trail);
		((TextView) tab2.findViewById(R.id.tv_tab)).setText(getStringResource(R.string.title_message));

		tab3 = LayoutInflater.from(this).inflate(R.layout.tab_widget, null);
		tab3.findViewById(R.id.iv_tab).setBackgroundResource(R.drawable.tab_self);
		((TextView) tab3.findViewById(R.id.tv_tab)).setText(getStringResource(R.string.title_self));

		intent0 = new Intent(MainActivity.this, TrailActivity.class);
		intent1 = new Intent(MainActivity.this, CaseReportActivity.class);
		intent2 = new Intent(MainActivity.this, MessageActivity.class);
		intent3 = new Intent(MainActivity.this, SelfCenterActivity.class);

		tabHost.addTab(tabHost.newTabSpec(TAB0).setIndicator(tab0).setContent(intent0));
		tabHost.addTab(tabHost.newTabSpec(TAB1).setIndicator(tab1).setContent(intent1));
		tabHost.addTab(tabHost.newTabSpec(TAB2).setIndicator(tab2).setContent(intent2));
		tabHost.addTab(tabHost.newTabSpec(TAB3).setIndicator(tab3).setContent(intent3));
		int flag = getIntent().getIntExtra("flag", 0);
		tabHost.setCurrentTab(flag);
		
		//启动service定位服务
		Intent intent = new Intent(MainActivity.this, MyLocationService.class);
		startService(intent);
		//启动service流量监控服务
		Intent intent1 = new Intent(MainActivity.this, MonitoringService.class);
		startService(intent1);
		// 判断GPS是否正常启动
		if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Toast.makeText(this, "请开启GPS导航...", Toast.LENGTH_SHORT).show();
			// 返回开启GPS导航设置界面
			Intent SettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivityForResult(SettingIntent, 0);
			return;
		}
//		MyLocationDao locationDao = new MyLocationDao(MainActivity.this);
//		String uid = MyApplication.getInstance().userId;
//		locationDao.delete(uid);
	}

		
	protected String getStringResource(Integer StringId) {
		return getResources().getString(StringId);
	}

	protected void initPush() {
		// 初始化 JPush。如果已经初始化，但没有登录成功，则执行重新登录。
		JPushInterface.init(getApplicationContext());
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();

		manager.dispatchResume();
		JPushInterface.onResume(MainActivity.this);
	}
	
	@Override
	protected void onPause() {
		
		super.onPause();
		JPushInterface.onPause(MainActivity.this);
	}

	@Override
	protected void onStop() {
		manager.dispatchStop();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		mainActivity = null;
		super.onDestroy();
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getRepeatCount() == 0) {
				// 得到当前的tab
				int current = tabHost.getCurrentTab();
				if (current != 0) {
					tabHost.setCurrentTab(0);
				} else {
					if ((System.currentTimeMillis() - mExitTime) > 2000) {// 如果两次按键时间间隔大于2000毫秒，则不退出
						Toast.makeText(this, "再点一次退出程序", Toast.LENGTH_SHORT).show();
						mExitTime = System.currentTimeMillis();// 更新mExitTime
					} else {
						int id = android.os.Process.myPid();
						if (id != 0) {
							android.os.Process.killProcess(id);
						}
					}
				}
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		int flag = intent.getIntExtra("flag", 0);
		tabHost.setCurrentTab(flag);
	}
	
}
