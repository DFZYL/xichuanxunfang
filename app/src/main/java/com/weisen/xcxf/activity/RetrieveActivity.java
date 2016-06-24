package com.weisen.xcxf.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.tool.CommonTool;
import com.weisen.xcxf.tool.TimeCount;


public class RetrieveActivity extends BaseActivity implements OnClickListener{
	
	private EditText et_Tphone;
	private Button btn_send;
	private String name,phone,code;
	private EditText etCode;
    private TextView getCode;
    long countTime;
    @Override
	protected void initView() {
		super.initView();
		setContentView(R.layout.activity_retrieve);
		initTitle();
		iv_left.setVisibility(View.VISIBLE);
		tv_title.setText("找回密码");
		et_Tphone = (EditText) findViewById(R.id.et_Tphone);
		btn_send = (Button) findViewById(R.id.btn_send);
        etCode = (EditText) findViewById(R.id.et_code);
        getCode = (TextView) findViewById(R.id.get_code);
        countTime = MyApplication.getInstance().countTime;
        if(countTime!=0){
            timeCount = new TimeCount(countTime, 1000);
            getCode.setEnabled(false);
            timeCount.start();
            timeCount.setCountDownTimerListener(new TimeCount.onCountDownTimerListener() {
                @Override
                public void onStart(long millisUntilFinished) {
                    getCode.setBackgroundColor(getResources().getColor(R.color.alpha_dark));
                    getCode.setText("重新发送" + "(" + millisUntilFinished / 1000
                            + ")");
                }
                @Override
                public void onFinished() {
                    getCode.setEnabled(true);
                    getCode.setBackgroundColor(getResources().getColor(R.color.blue));
                    getCode.setText("发送验证码");
                }
            });
        }
	}
	
	@Override
	protected void initData() {
		super.initData();
		iv_left.setOnClickListener(this);
		btn_send.setOnClickListener(this);
        getCode.setOnClickListener(this);
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
            case R.id.get_code:
                getCode();
                break;
		default:
			break;
		}
	}

	private void send() {
        phone = et_Tphone.getText().toString();
        code = etCode.getText().toString();
//        if (phone.equals("")) {
//            showShortToast("手机号码不能为空！");
//            return;
//        } else {
//            if (Commons.validateMobileNumber(phone) == false) {
//                showShortToast("手机号码不正确！");
//                return;
//            }
//        }

        if(!isMobileNO(phone)){
            showShortToast("手机号码不正确！");
            return;
        }
        if(TextUtils.isEmpty(code) || code.length()<6){
            showShortToast("请输入正确的验证码！");
            return;
        }
		Map<String, String> map = new HashMap<String, String>();
        map.put("iphone", phone);
		map.put("code", code);
		method = "send";
		getServer(MyApplication.getInstance().getIP() + Constant.CHECK_CODE,map,"upload");
	};
	@Override
	protected void processSuccessResult(String res) {
		super.processSuccessResult(res);

        if(method.equals("getCode")){
            String isSuccess = null;
            String codeStr = "";
            JSONObject object = CommonTool.parseFromJson(res);
            try {
                isSuccess = object.getString("success");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (isSuccess.equals("true")) {
                showShortToast("验证码已发送,请查收!");
                MyApplication.getInstance().startCountTime();
                startCountTime();
            }else{
                showShortToast("验证码发送失败!");
            }
            }
        else if(method.equals("send")){
            String isSuccess = null;
            String userId = null;
            JSONObject object = CommonTool.parseFromJson(res);
            try {
                isSuccess = object.getString("success");
                userId = object.getString("code");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (isSuccess.equals("true")) {
                Intent in = new Intent(RetrieveActivity.this,ResetPwdActivity.class);
                in.putExtra("userId", userId);
                in.putExtra("phone",phone);
                startActivity(in);
            }else{

            }
        }
	}
    public void getCode() {
        phone = et_Tphone.getText().toString();
        if(!isMobileNO(phone)){
            showShortToast("手机号码不正确！");
            return;
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("iphone", phone);
        method = "getCode";
        getServer(MyApplication.getInstance().getIP() + Constant.GET_CODE, map, "get");
    }
    TimeCount timeCount;
    private void startCountTime() {
        timeCount = new TimeCount(60000, 1000);
        getCode.setEnabled(false);
        timeCount.start();
        timeCount.setCountDownTimerListener(new TimeCount.onCountDownTimerListener() {
            @Override
            public void onStart(long millisUntilFinished) {
                getCode.setBackgroundColor(getResources().getColor(R.color.alpha_dark));
                getCode.setText("重新发送" + "(" + millisUntilFinished / 1000
                        + ")");
            }
            @Override
            public void onFinished() {
                getCode.setEnabled(true);
                getCode.setBackgroundColor(getResources().getColor(R.color.blue));
                getCode.setText("发送验证码");
            }
        });
    }
}
