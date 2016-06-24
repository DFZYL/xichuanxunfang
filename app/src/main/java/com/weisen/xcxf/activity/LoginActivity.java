package com.weisen.xcxf.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.jpush.android.api.JPushInterface;

import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.tool.CommonTool;

/**
 * 
 * @author JQY 登录界面
 */
public class LoginActivity extends BaseActivity implements OnClickListener {

	private EditText et_username, et_password, et_ip;
	private Button bt_login, bt_default, bt_confirm, bt_cancel,btn_regist;
	private String username, password, ip;
	private SharedPreferences preferences;
	private Editor editor;
	Dialog dialog;
	private String method;

	private LocationManager lm;
	
	private TelephonyManager tm;
	private RelativeLayout moble_rl;
	private TextView moble_num,retrieve_pwd;

	@Override
	protected void initView() {
		
		super.initView();
		setContentView(R.layout.activity_login);
		initTitle();
		et_username = (EditText) findViewById(R.id.et_username);
		et_password = (EditText) findViewById(R.id.et_password);
		bt_login = (Button) findViewById(R.id.bt_login);
		btn_regist = (Button) findViewById(R.id.btn_regist);
		iv_left.setVisibility(View.GONE);
		iv_right.setVisibility(View.GONE);
		iv_right.setImageResource(R.drawable.home_setting);
		retrieve_pwd = (TextView) findViewById(R.id.retrieve_pwd);
		
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);//
		moble_num = (TextView) findViewById(R.id.moble_num);
		moble_rl = (RelativeLayout) findViewById(R.id.moble_rl);
		moble_num.setText(tm.getDeviceId());
		if (tm.getDeviceId().equals("")) {
			moble_rl.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					moble_num.setText(tm.getDeviceId());
				}
			});
		}
	}

	@Override
	protected void initData() {
		
		super.initData();
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		preferences = getSharedPreferences(Constant.APP_SP, MODE_PRIVATE);
		String title = preferences.getString(Constant.SP_APPNAME,Constant.DEFAULT_TITLE);
		editor = preferences.edit();
		tv_title.setText(title);
//		Intent intent = new Intent(this, MyLocationService.class);
//		startService(intent);
		// 判断GPS是否正常启动
		if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			// 返回开启GPS导航设置界面
			Intent SettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivityForResult(SettingIntent, 0);
			return;
		}
	}

	@Override
	protected void initEvent() {
		
		super.initEvent();
		iv_left.setOnClickListener(this);
		bt_login.setOnClickListener(this);
		iv_right.setOnClickListener(this);
		btn_regist.setOnClickListener(this);
		retrieve_pwd.setOnClickListener(this);
		et_username.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				

			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				
				username = et_username.getText().toString().trim();
				password = et_password.getText().toString().trim();
				if (username.equals("") || password.equals("")) {
					bt_login.setBackgroundColor(getResources().getColor(R.color.low_gray));
					bt_login.setClickable(false);
				} else {
					bt_login.setBackgroundColor(getResources().getColor(R.color.blue));
					bt_login.setClickable(true);
				}
			}

		});
		et_password.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				

			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				
				username = et_username.getText().toString().trim();
				password = et_password.getText().toString().trim();
				if (username.equals("") || password.equals("")) {
					bt_login.setBackgroundColor(getResources().getColor(R.color.gray));
					bt_login.setClickable(false);
				} else {
					bt_login.setBackgroundColor(getResources().getColor(R.color.blue));
					bt_login.setClickable(true);
				}
			}

		});
	}

	public void showDialog() {
		dialog = new Dialog(LoginActivity.this, R.style.myDialogTheme);
		dialog.setContentView(R.layout.alert_setdialog);
		dialog.setTitle("单位设置");
		et_ip = (EditText) dialog.findViewById(R.id.et_ip);
		et_ip.setText(MyApplication.getInstance().getIP());
		bt_default = (Button) dialog.findViewById(R.id.bt_default);
		bt_confirm = (Button) dialog.findViewById(R.id.bt_confirm);
		bt_cancel = (Button) dialog.findViewById(R.id.bt_cancel);
		bt_default.setOnClickListener(this);
		bt_confirm.setOnClickListener(this);
		bt_cancel.setOnClickListener(this);
		dialog.show();
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.iv_left:
			finish();
			break;
		case R.id.retrieve_pwd:
			Intent in = new Intent(this,RetrieveActivity.class);
			startActivity(in);
			break;
		case R.id.bt_login:
			username = et_username.getText().toString();
			password = et_password.getText().toString();
			if (username.trim().equals("")) {
				toastNotNull("用户名");
				break;
			}
			if (!isPassword(password)) {
                showShortToast("请输入正确的密码");
				break;
			}


			Build build = new Build();
			String model = build.MODEL;
			final Map<String, String> map = new HashMap<String, String>();
			map.put("userName", username);
			map.put("password", password);
			map.put("dName", build.MANUFACTURER);
			map.put("dType", model);
			map.put("dNo", tm.getDeviceId());
			map.put("channelId", JPushInterface.getRegistrationID(getApplicationContext()));
			map.put("dImsi", tm.getSubscriberId());
			map.put("addr", MyApplication.getInstance().address);
			map.put("loti", MyApplication.getInstance().longitude + "");
			map.put("lati", MyApplication.getInstance().latitude + "");
			method = "login";
			getServer(MyApplication.getInstance().getIP() + Constant.LOGIN,map, "upload");


			break;
		case R.id.iv_right:
			showDialog();
			break;
		case R.id.bt_default:
			et_ip.setText(Constant.IP);
			break;
		case R.id.bt_confirm:
			ip = et_ip.getText().toString();
			if (ip == null || ip.equals("")) {
				toastNotNull("ip");
				break;
			}
			MyApplication.getInstance().setIP(ip);
			dialog.dismiss();
			break;
		case R.id.bt_cancel:
			dialog.dismiss();
			break;
		}
	}

	@Override
	protected void processSuccessResult(String res) {
		
		super.processSuccessResult(res);
		if (method.equals("login")) {
			JSONObject object = CommonTool.parseFromJson(res);
            System.out.println(object);
            String userId = CommonTool.getJsonString(object, "userId");
			String name = CommonTool.getJsonString(object, "userName");
			String userIcon = CommonTool.getJsonString(object, "userIcon");
			String beginTime = CommonTool.getJsonString(object, "btime");
			String endTime = CommonTool.getJsonString(object, "etime");
            String net =  CommonTool.getJsonString(object, "net");
            String comId =  CommonTool.getJsonString(object, "comId");
            System.out.println(net);
            editor.putString(Constant.SP_USERNAME, username);
			editor.putString(Constant.SP_PWD, password);
            System.out.println("userid>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + userId);
            editor.putString(Constant.SP_USERID, userId);
			editor.putString(Constant.SP_BEGIN, beginTime);
			editor.putString(Constant.SP_END, endTime);
            if(!TextUtils.isEmpty(net)){
                editor.putLong(Constant.LIULIANG,Long.valueOf(net));
            }
            if(!TextUtils.isEmpty(comId)){
                editor.putString(Constant.COMID,comId);
            }
			editor.commit();
            MyApplication.getInstance().setUserId(userId);
			MyApplication.getInstance().setHeadPic(userIcon);
			MyApplication.getInstance().setName(name);
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		} else if (method.equals("getPeriodTime")) {
			JSONObject object = CommonTool.parseFromJson(res);
			String beginTime = CommonTool.getJsonString(object, "btime");
			String etime = CommonTool.getJsonString(object, "etime");
			editor.putString(Constant.SP_BEGIN, beginTime);
			editor.putString(Constant.SP_END, etime);
			editor.commit();
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		
		super.onNewIntent(intent);
	}
}
