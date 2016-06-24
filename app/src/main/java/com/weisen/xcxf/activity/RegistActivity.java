package com.weisen.xcxf.activity;

import android.view.View;

import com.weisen.xcxf.R;


public class RegistActivity extends BaseActivity{
	
	@Override
	protected void initView() {
		
		super.initView();
		setContentView(R.layout.activity_regist);
		initTitle();
		iv_left.setVisibility(View.VISIBLE);
		tv_title.setText("注册");
		
		
	}
	
}
