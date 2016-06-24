package com.weisen.xcxf.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.os.Build;

import com.weisen.xcxf.R;
import com.weisen.xcxf.tool.LEDView;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FirstpageActivity extends BaseActivity{
	private LEDView ledView;
	@SuppressLint("NewApi")
	@Override
	protected void initView() {
		
		super.initView();
		setContentView(R.layout.activity_firstpage);
		ledView = (LEDView) findViewById(R.id.ledview);
		ActionBar actionBar = getActionBar();  
		  actionBar.setDisplayHomeAsUpEnabled(true); 
	}
	@Override
	protected void onResume() {
		super.onResume();
		ledView.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		ledView.stop();
	}
	@Override
	protected void initData() {
		
		super.initData();
	}
	
	@Override
	protected void initEvent() {
		
		super.initEvent();
	}

}
