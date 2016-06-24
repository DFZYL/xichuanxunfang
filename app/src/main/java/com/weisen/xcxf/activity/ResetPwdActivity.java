package com.weisen.xcxf.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.tool.CommonTool;

import cn.jpush.android.api.JPushInterface;

public class ResetPwdActivity extends BaseActivity implements OnClickListener{
	
	private EditText et_pwd1,et_pwd2;
	private Button btn_send;
	private String pwd1,pwd2,phone;
	private String username;
    private TelephonyManager tm;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
	@Override
	protected void initView() {
		super.initView();
		setContentView(R.layout.activity_resetpwd);
		initTitle();
        preferences = getSharedPreferences(Constant.APP_SP, MODE_PRIVATE);
        editor = preferences.edit();
		iv_left.setVisibility(View.VISIBLE);
		tv_title.setText("找回密码");
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);//
        username = getIntent().getStringExtra(phone);
		et_pwd1 = (EditText) findViewById(R.id.et_pwd1);
		et_pwd2 = (EditText) findViewById(R.id.et_pwd2);
		btn_send = (Button) findViewById(R.id.btn_send);
	}
	
	@Override
	protected void initData() {
		super.initData();
		iv_left.setOnClickListener(this);
		btn_send.setOnClickListener(this);
	}
	
	protected void initEvent() {
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_left:
			finish();
			break;
		case R.id.btn_send:
			send();
			break;

		default:
			break;
		}
	}

	private void send() {
		pwd1 = et_pwd1.getText().toString();
		pwd2 = et_pwd2.getText().toString();
		if (pwd1.equals("") && pwd1.length()>=6) {
			showShortToast("密码不能为空且不能小于6位");
			return;
		}
		if (pwd2.equals("") && pwd2.length()>=6) {
			showShortToast("密码不能为空且不能小于6位");
			return;
		}
		String userId = getIntent().getStringExtra("userId");
        phone = getIntent().getStringExtra("phone");
		if (pwd1.equals(pwd2)) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("iphone", phone);
			map.put("code", userId);
			map.put("pwd", pwd1);
			getServer(MyApplication.getInstance().getIP() + Constant.RESETPWD,map, "upload");
		}else {
			showShortToast("两次密码输入不一致");
		}
	};
	@Override
	protected void processSuccessResult(String res) {
		super.processSuccessResult(res);
        String isSuccess = null;
        if(method.equals("login")){
            JSONObject object = CommonTool.parseFromJson(res);
            System.out.println(object);
            String userId = CommonTool.getJsonString(object, "userId");
            String name = CommonTool.getJsonString(object, "userName");
            String userIcon = CommonTool.getJsonString(object, "userIcon");
            String beginTime = CommonTool.getJsonString(object, "btime");
            String endTime = CommonTool.getJsonString(object, "etime");
            String net =  CommonTool.getJsonString(object, "net");
            System.out.println(net);
            editor.putString(Constant.SP_USERNAME, username);
            editor.putString(Constant.SP_PWD, pwd1);
            System.out.println("userid>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + userId);
            editor.putString(Constant.SP_USERID, userId);
            editor.putString(Constant.SP_BEGIN, beginTime);
            editor.putString(Constant.SP_END, endTime);
            if(!TextUtils.isEmpty(net)){
                editor.putLong(Constant.LIULIANG,Long.valueOf(net));
            }
            editor.commit();
            MyApplication.getInstance().setUserId(userId);
            MyApplication.getInstance().setHeadPic(userIcon);
            MyApplication.getInstance().setName(name);
            Intent intent = new Intent(ResetPwdActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else{
            JSONObject object = CommonTool.parseFromJson(res);
            try {
                isSuccess = object.getString("success");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (isSuccess.equals("true")) {
//                Intent in = new Intent(this,LoginActivity.class);
//                startActivity(in);
                autologin();
            }
        }
	}

    private void autologin() {
        Build build = new Build();
        String model = build.MODEL;
        Map<String, String> map = new HashMap<String, String>();
        map.put("userName", username);
        map.put("password", pwd1);
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

    }

}
