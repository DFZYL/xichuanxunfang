package com.weisen.xcxf.activity;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.CaseDao;
import com.weisen.xcxf.bean.CaseReport;
import com.weisen.xcxf.bean.MyLocation;
import com.weisen.xcxf.bean.MyLocationDao;
import com.weisen.xcxf.tool.CommonTool;
import com.weisen.xcxf.tool.FileUtils;
import com.weisen.xcxf.tool.HttpTool;
import com.weisen.xcxf.tool.MD5Tool;
import com.weisen.xcxf.tool.UnIntent;
import com.weisen.xcxf.utils.IntenetUtil;
import com.weisen.xcxf.widget.RecordButton;
import com.weisen.xcxf.widget.RecordButton.OnFinishedRecordListener;

public class RecordingActivitys extends BaseActivity {
	private static final int SOS = 1;
	private RecordButton mRecordButton1 = null;
	private String path;
	private CaseReport report;
	private CaseDao caseDao;
	private MyLocationDao myLocationDao;
	private List<MyLocation> locationList;
	private boolean isUpload = false;
	private String attachName = "";
    private SharedPreferences preferences;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			super.handleMessage(msg);
			switch (msg.what) {
			case DATA_SUCCESS:
				hideProgressDialog();
				String res = (String) msg.obj;
				processResult(res, true);
				break;
			case SOS:
				report.setIsSuccess("false");
				caseDao.addCase(report);
				showShortToast("上报失败!");
				break;
			}
		}
	};

	@Override
	protected void initView() {

		super.initView();
		setContentView(R.layout.activity_recodeing);
        preferences = getSharedPreferences(Constant.APP_SP, MODE_PRIVATE);
		mRecordButton1 = (RecordButton) findViewById(R.id.record_button);
		initTitle();
		iv_left.setVisibility(View.VISIBLE);
		// tv_right.setVisibility(View.VISIBLE);
		// tv_right.setText("提交");
		tv_title.setText(getString(R.string.title_recording));
		path = FileUtils.getVoiceFilePath();
		path += UUID.randomUUID() + ".amr";
		mRecordButton1.setSavePath(path);
		tv_right.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SOS();
			}
		});
	}

	// 紧急求助
	private void SOS() {
		caseDao = new CaseDao(this);
		myLocationDao = new MyLocationDao(this);
		String uid = MyApplication.getInstance().userId;
		locationList = myLocationDao.findAll(uid);
		String id = MyApplication.getInstance().getUserId();
		String random = CommonTool.getRandom();
		String md5Sign = MD5Tool.Md5(MD5Tool.Md5(Constant.KEY + random) + id);
		final Map<String, String> map = new HashMap<String, String>();
		final Map<String, File> fileMap = new HashMap<String, File>();
		final String str = "?id=" + id + "&random=" + random + "&signature="
				+ md5Sign;
        String longitude = preferences.getString(Constant.SP_LONGITUDE,"0.0");
        String latitude = preferences.getString(Constant.SP_LATITUDE,"0.0");
		String alti = MyApplication.getInstance().altitude + "";
		String address = MyApplication.getInstance().address;
		String speed = CommonTool
				.formatFloat(MyApplication.getInstance().speed);
		String bearing = CommonTool
				.formatFloat(MyApplication.getInstance().bearing);
		String accurary = CommonTool
				.formatFloat(MyApplication.getInstance().accurary);
		String time = MyApplication.getInstance().time + "";
		String uLocType = MyApplication.getInstance().uLocType + "";
		// zh
		if (longitude == null || longitude.equals("0.0")) {
			if (!locationList.isEmpty() && locationList != null) {
				uLocType = locationList.get(locationList.size() - 1)
						.getLocType();
				latitude = locationList.get(locationList.size() - 1)
						.getLatitude();
				longitude = locationList.get(locationList.size() - 1)
						.getLongitude();
			} else {
				latitude = "0.0";
				longitude = "0.0";
			}
		}

		if (time == null || time.equals("") || time.equals("null"))
			time = CommonTool.getStringDate(new Date(), "yyyy-MM-dd HH:mm:ss");
		map.put("uType", "紧急求助");
		map.put("uContent", "紧急求助");
		map.put("q", (!isUpload) + "");
		map.put("uRemark", "紧急求助");
		map.put("uTime", time);
		if (isUpload)
			map.put("uUserId", "");
		map.put("uLoti", longitude);
		map.put("uLati", latitude);
		map.put("uAlti", alti);
		map.put("uAddr", "");
		map.put("uSpeed", speed);
		map.put("uDirection", bearing);
		map.put("uAccuracy", accurary);
		map.put("uLocType", uLocType);
		map.put("flag", "1");
		map.put("net", IntenetUtil.getNetworkState(RecordingActivitys.this)+"");
		attachName = "";
		if (path != null && !path.equals(""))
			attachName += path + ",";
		report = new CaseReport();
		report.setuType("紧急求助");
		report.setUid(id);
		report.setuContent("紧急求助");
		report.setQ((!isUpload) + "");
		report.setuTime(time);
		report.setuLati(latitude);
		report.setuLoti(longitude);
		report.setuAlti(alti);
		report.setuAddr("");
		report.setuSpeed(speed);
		report.setuDirection(bearing);
		report.setuAccurary(accurary);
		report.setuRemark("紧急求助");
		report.setAttach(attachName);
		report.setuUserId("");
		report.setuLocType(uLocType);
		report.setFlag("1");
		try {
			if (attachName != null && !attachName.equals("")) {
				String[] names = attachName.split(",");
				for (String name : names) {
					File file = new File(name);
					if (file != null && file.exists()) {
						fileMap.put(name, file);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					method = "upload";
					String res = HttpTool.post(MyApplication.getInstance()
							.getIP() + Constant.CASE_REPORT + str, map,
							fileMap, "attach");
					JSONObject object = CommonTool.parseFromJson(res);
					String isSuccess = object.getString("success");
					if (isSuccess.equals("true")) {
						Message msg = handler.obtainMessage(DATA_SUCCESS);
						msg.obj = res;
						msg.sendToTarget();
					} else {
						Message msg = handler.obtainMessage(SOS);
						msg.sendToTarget();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	protected void processSuccessResult(String res) {
		super.processSuccessResult(res);
		String isSuccess = null;
		String msg = null;
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
			if (method.equals("upload")) {
				report.setIsSuccess("true");
				report.setUploadTime(CommonTool.getStringDate(new Date(),
						"yyyy-MM-dd HH:mm:ss"));
				caseDao.addCase(report);
				showShortToast(msg);
				if (attachName != null && !attachName.equals("")) {
					String[] names = attachName.split(",");
					for (String name : names) {
						File file = new File(name);
						if (file != null && file.exists()) {
							file.delete();
						}
					}
				}
			}
		} else {
			showShortToast(msg);
		}
	}

	@Override
	protected void initEvent() {

		super.initEvent();
		iv_left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				finish();
			}
		});
		mRecordButton1
				.setOnFinishedRecordListener(new OnFinishedRecordListener() {

					public void onFinishedRecord(String audioPath) {
						if (UnIntent
								.isNetworkAvailable(RecordingActivitys.this)) {
							SOS();
							finish();
						} else {
                            report.setIsSuccess("false");
                            caseDao.addCase(report);
							showShortToast("没有网络，上报失败！");
							finish();
						}

					}
				});
	}
}
