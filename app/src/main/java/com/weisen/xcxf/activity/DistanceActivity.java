package com.weisen.xcxf.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.weisen.xcxf.R;
import com.weisen.xcxf.adapter.DistanceAdapter;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.UserLength;
import com.weisen.xcxf.bean.UserLengthDao;


public class DistanceActivity extends BaseActivity{
	private LayoutInflater inflater;
	private DistanceAdapter adapter;
	
	@Override
	protected void initView() {
		
		super.initView();
		setContentView(R.layout.activity_distance);
		inflater = LayoutInflater.from(this);
		initTitle();
		tv_title.setText("巡防记录");
		iv_left.setVisibility(View.VISIBLE);
		iv_left.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		final ListView distance_list = (ListView) findViewById(R.id.lv_distance);
	final 	UserLengthDao  cUserLengthDao = new UserLengthDao(DistanceActivity.this);
		List<UserLength> list =
		 cUserLengthDao.findAll(MyApplication.getInstance().getUserId());
		adapter = new DistanceAdapter(list,inflater);
		distance_list.setAdapter(adapter);
		tv_right.setVisibility(View.VISIBLE);
		tv_right.setText("清空记录");
		tv_right.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(DistanceActivity.this)

						.setTitle("确定要清空此记录么？")

						.setMessage("记录清空后不可恢复！")

						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								cUserLengthDao.deletealllength();
								List<UserLength> list1 =
										cUserLengthDao.findAll(MyApplication.getInstance().getUserId());
								adapter = new DistanceAdapter(list1,inflater);
								distance_list.setAdapter(adapter);
								dialog.dismiss();
							}
						})
				         .setNegativeButton("取消", new DialogInterface.OnClickListener() {
							 @Override
							 public void onClick(DialogInterface dialog, int which) {
								 dialog.dismiss();
							 }
						 })
						.show();

			}
		});

	}
	
	@Override
	protected void initData() {
		
		super.initData();
	}
}
