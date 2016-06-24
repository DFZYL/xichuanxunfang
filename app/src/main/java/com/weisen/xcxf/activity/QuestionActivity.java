package com.weisen.xcxf.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.app.MyApplication;

public class QuestionActivity extends BaseActivity implements OnClickListener{
	private String ed_content;
	private EditText ed_txt;
	@Override
	protected void initView() {
		
		super.initView();
		setContentView(R.layout.activity_question);
		initTitle();
		iv_left.setVisibility(View.VISIBLE);
		iv_left.setOnClickListener(this);
		tv_right.setVisibility(View.VISIBLE);
		tv_right.setText("提交");
		tv_right.setOnClickListener(this);
		tv_title.setText("问题反馈");
		
		ed_txt = (EditText) findViewById(R.id.et_txt);
		ed_content = ed_txt.getText().toString();
	}
	
	@Override
	protected void initData() {
		
		super.initData();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_left:
			finish();
			break;
		case R.id.tv_right:
			ed_content = ed_txt.getText().toString();
			if (ed_content.equals("")) {
				showShortToast("内容不能为空");
			}else{
				commitDatas();
			}
			break;
		default:
			break;
		}
	}

	private void commitDatas() {
		Build build = new Build();
		String model = build.MODEL;
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);//
		Map<String, String> map = new HashMap<String, String>();
		map.put("id", MyApplication.getInstance().getUserId());
		map.put("q", ed_content);
		map.put("searchName", build.MANUFACTURER);
		map.put("searchValue", tm.getDeviceId());
		getServer(MyApplication.getInstance().getIP() + Constant.SEND_QUESTION,map, "upload");
	}
	
	@Override
	protected void processSuccessResult(String res) {
		
		super.processSuccessResult(res);
		String isSuccess=null;
		String msg=null;
		JSONObject jsonobj = null;
		try {
			jsonobj = new JSONObject(res);
			isSuccess = jsonobj.getString("success");
			msg = jsonobj.getString("msg");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (isSuccess.equals("true")) {
			showShortToast(msg);
			finish();
			ed_txt.setText("请输入问题描述...");
		}else{
			showShortToast("反馈失败！");
		}
	}
}
