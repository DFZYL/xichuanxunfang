package com.weisen.xcxf.activity;

import java.io.File;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.tool.CommonTool;
import com.weisen.xcxf.tool.HttpTool;
import com.weisen.xcxf.tool.MD5Tool;

public class BaseActivity extends FragmentActivity {

	public static final int DATA_SUCCESS = 0;
	public ProgressDialog progressDialog;
	public String method = "";
	// 当前页数
	protected int pageNum = 1;
	// 每页显示数量，默认位10
	protected int pageSize = 10;
	public boolean dialogFlag = true;
	protected ImageView iv_left, iv_right;
	protected TextView tv_title, tv_right;
	protected View empty;
	protected String refreshing = "";
	private InputMethodManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		MyApplication.instance.addActivity(this);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		initView();
		initData();
		initEvent();
	}


    public  boolean isMobileNO(String mobiles) {
        Pattern p = Pattern.compile("^1[3,4,5,8][0-9]{9}$");
        Matcher m = p.matcher(mobiles);

        return m.matches();
    }


    public  boolean isPassword(String str) {
        Pattern p = Pattern.compile("^\\w{6,16}$");
        Matcher m = p.matcher(str);
        return m.matches();
    }
	/**
	 * 
	 * 获取网络请求 map——传递的参数 isSign——是否需要签名 method——值为两种，get和upload，get代表从网络获取
	 * upload代表向网络请求数据，为了加载语句的统一性
	 * 
	 */
	public void getServer(String url, Map<String, String> map, String method) {
		if (map == null)
			map = new HashMap<String, String>();
		String random = CommonTool.getRandom();
		String id = MyApplication.getInstance().getUserId();
		map.put("random", random);
		String md5Sign = MD5Tool.Md5(MD5Tool.Md5(Constant.KEY + random) + id);
		map.put("signature", md5Sign);
		map.put("id", id);
		System.out.println(url + "=" + map);
		RequestParams params = new RequestParams(map);
		if (dialogFlag)
			showProgressDialog(method, "");
		HttpTool.post(url, params, new AsyncHttpResponseHandler() {

			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				String res = new String(arg2);
				System.out.println(res);
				processResult(res, true);
			}

			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				processResult(null, false);
			}
		});
	}

	/**
	 * 
	 * 获取网络请求 map——传递的参数 isSign——是否需要签名 method——值为两种，get和upload，get代表从网络获取
	 * upload代表向网络请求数据，为了加载语句的统一性
	 * 
	 */
	public void getFileServer(String url, Map<String, String> map,
			String fileNames, String method) {
		if (map == null)
			map = new HashMap<String, String>();
		String random = CommonTool.getRandom();
		String id = MyApplication.getInstance().getUserId();
		map.put("random", random);
		String md5Sign = MD5Tool.Md5(MD5Tool.Md5(Constant.KEY + random) + id);
		map.put("signature", md5Sign);
		map.put("id", id);
		RequestParams params = new RequestParams(map);
		File[] files = null;
		try {
			if (fileNames != null && !fileNames.equals("")) {
				String[] names = fileNames.split(",");
				files = new File[names.length];
				int i = 0;
				for (String name : names) {
					File file = new File(name);
					if (file != null && file.exists()) {
						files[i] = file;
						params.put("attach", files);
						i++;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (dialogFlag)
			showProgressDialog(method, "");
		HttpTool.postFile(url, params, new AsyncHttpResponseHandler() {

			public void onSuccess(int arg0, Header[] headers, byte[] arg2) {
				// 遍历头信息
				String res = new String(arg2);
				System.out.println(res);
				processResult(res, true);
			}

			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				processResult(null, false);
			}
		});
	}

	/*
	 * 展示提交或者加载数据的进度框
	 */
	public void showProgressDialog(String method, String message) {
		if (progressDialog == null)
			progressDialog = new ProgressDialog(this);
		String progressDialogMessage = "";
		if (!progressDialog.isShowing()) {
			if (method.equals("get"))
				progressDialogMessage = getStringResource(R.string.get);
			else if (method.equals("upload"))
				progressDialogMessage = getStringResource(R.string.upload);
			else
				progressDialogMessage = message;
			progressDialog.setMessage(progressDialogMessage);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
		}
	}

	/*
	 * 隐藏对话框
	 */
	public void hideProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog.cancel();
		}
	}

	/*
	 * 处理网络请求后的结果
	 */
	protected void processResult(String res, boolean flag) {
		if (dialogFlag)
			hideProgressDialog();
		if (!flag) {
			if (dialogFlag)
				showShortToast(getStringResource(R.string.httpError));
			processFailResult();
			return;
		}
		if (res == null || "".equals(res)) {
			if (dialogFlag)
				showShortToast(getStringResource(R.string.httpNoData));
			processFailResult();
			return;
		}
		JSONObject object = CommonTool.parseFromJson(res);
		String status = CommonTool.getJsonString(object, "success");
		if (status == null || !status.equals("true")) {
			String errorMessage = CommonTool.getJsonString(object, "msg");
			if (errorMessage == null || errorMessage.equals("")) {
				if (dialogFlag)
					showShortToast(getStringResource(R.string.getDataError));
			} else {
				errorMessage = URLDecoder.decode(errorMessage);
				if (dialogFlag)
					showShortToast(errorMessage);
			}
			return;
		} else {
			processSuccessResult(res);
		}
	}

	/*
	 * 短的toast
	 */
	public void toastNotNull(String name) {
		Toast.makeText(this, name + "不能为空!", Toast.LENGTH_SHORT).show();
	}

	/*
	 * 短的toast
	 */
	public void showShortToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	/*
	 * 长的toast
	 */
	protected void showLongToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	/*
	 * 获取配置文件中的字符串
	 */
	protected String getStringResource(Integer StringId) {
		return getResources().getString(StringId);
	}

	protected void processFailResult() {
		
	}

	protected void processSuccessResult(String res) {
		
	}

	public void processSuccessButNoData(JSONObject object) {
		
		String errorMessage = CommonTool.getJsonString(object, "msg");
		if (errorMessage == null || errorMessage.equals(""))
			showShortToast(getStringResource(R.string.getDataError));
		else {
			errorMessage = URLDecoder.decode(errorMessage);
			showShortToast(errorMessage);
		}
		return;
	}

	@Override
	protected void onPause() {
		
		super.onPause();
	}

	@Override
	protected void onResume() {
		
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog.cancel();
		}
		super.onDestroy();
	}

	protected void initView() {
	}

	protected void initData() {
	}

	protected void initEvent() {
	}

	protected void initTitle() {
		iv_left = (ImageView) findViewById(R.id.iv_left);
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_right = (TextView) findViewById(R.id.tv_right);
		iv_right = (ImageView) findViewById(R.id.iv_right);
	}

	public void hideKeyboard() {
		if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getCurrentFocus() != null)
				manager.hideSoftInputFromWindow(getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
}
