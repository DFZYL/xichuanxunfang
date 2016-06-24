package com.weisen.xcxf.activity;

import java.util.HashMap;
import java.util.Map;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.app.MyApplication;

public class UpdatePwdActivity extends BaseActivity implements OnClickListener {

	private EditText et_old_pwd, et_new_pwd, et_new_pwd2;
	private String oldPass, newPass, newPass01, uId;
	private Button bt_submit;

	@Override
	protected void initView() {
		
		super.initView();
		setContentView(R.layout.activity_modifypwd);
		et_old_pwd = (EditText) findViewById(R.id.et_old_pwd);
		et_new_pwd = (EditText) findViewById(R.id.et_new_pwd);
		et_new_pwd2 = (EditText) findViewById(R.id.et_new_pwd2);
		bt_submit = (Button) findViewById(R.id.bt_submit);
		initTitle();
		iv_left.setVisibility(View.VISIBLE);
		tv_title.setText(getString(R.string.title_update_pwd));
	}

	@Override
	protected void initEvent() {
		
		super.initEvent();
		bt_submit.setOnClickListener(this);
		iv_left.setOnClickListener(this);
	}


	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.iv_left:
			finish();
			break;
		case R.id.bt_submit:
			newPass = et_new_pwd.getText().toString().trim();
			newPass01 = et_new_pwd2.getText().toString().trim();
			oldPass = et_old_pwd.getText().toString().trim();
			if (oldPass == null || oldPass.equals("")) {
				toastNotNull("旧密码");
				break;
			}
			if (newPass == null || newPass.equals("")) {
				toastNotNull("新密码");
				break;
			}
			if (newPass01 == null || newPass01.equals("")) {
				toastNotNull("确认密码");
				break;
			}
			if (!newPass.equals(newPass01)) {
				showShortToast("两次密码不相同");
				break;
			}
			hideKeyboard();
			Map<String, String> map = new HashMap<String, String>();
			map.put("q", oldPass);
			map.put("searchName", newPass);
			getServer(MyApplication.getInstance().getIP() + Constant.UPDATE_PWD,map, "upload");

			break;
		}
	}
	
	@Override
	protected void processSuccessResult(String res) {
		
		super.processSuccessResult(res);
		showShortToast("修改成功!");
		finish();
	}
}
