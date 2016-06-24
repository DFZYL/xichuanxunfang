package com.weisen.xcxf.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.weisen.xcxf.R;

public class AboutActivity extends BaseActivity {

	private TextView vison_code,url;
	private TextView phone_num;
	private PackageInfo info;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		initTitle();
        url = (TextView) findViewById(R.id.cop_url);
		iv_left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		tv_title.setText("关于我们");
		try {
			PackageManager manager = this.getPackageManager();
			info = manager.getPackageInfo(this.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		vison_code = (TextView) findViewById(R.id.vision_code);
		vison_code.setText("V" + info.versionName);
		phone_num = (TextView) findViewById(R.id.phone_num);
		phone_num.setText("技术支持:" + "400-999-3316");
		Linkify.addLinks(phone_num, Linkify.PHONE_NUMBERS);
        Linkify.addLinks(url,Linkify.WEB_URLS);
	}

}
