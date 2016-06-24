package com.weisen.xcxf.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.weisen.xcxf.Constant;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.CaseDao;
import com.weisen.xcxf.bean.CaseReport;
import com.weisen.xcxf.bean.MyLocation;
import com.weisen.xcxf.bean.MyLocationDao;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class UploadTool {

	Context context;
	private List<CaseReport> reportList = new ArrayList<CaseReport>();
	private List<MyLocation> locationList = new ArrayList<MyLocation>();
	private CaseDao caseDao;
	private MyLocationDao locationDao;
	private static final int DATA_SUCCESS = 0;
	private int locationIndex = 0;
	private int caseIndex = 0;

	public UploadTool(Context context) {
		this.context = context;
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
			switch (msg.what) {
			case DATA_SUCCESS:
				String res = (String) msg.obj;
				JSONObject object = CommonTool.parseFromJson(res);
				String status = CommonTool.getJsonString(object, "success");
				int flagIndex = msg.arg2;

				if (status != null) {
					if (status.equals("true")) {
						if (flagIndex == 1) {
							int caseId = msg.arg1;
							caseDao.updateCase(caseId);
							String attachName = caseDao.getAttach(caseId);
							if (attachName != null && !attachName.equals("")) {
								String[] names = attachName.split(",");
								for (String name : names) {
									File file = new File(name);
									if (file != null && file.exists()) {
										file.delete();
									}
								}
							}
							Intent intent = new Intent();
							intent.setAction(Constant.BROADCAST_UPDATE_REPORT);
							intent.putExtra("caseId", caseId + "");
							context.sendBroadcast(intent);
							caseIndex++;
							if (caseIndex < reportList.size()) {
								CaseReport report = reportList.get(caseIndex);
								uploadReport(report);
							}
						} else if (flagIndex == 2) {
							int locationId = msg.arg1;
							locationDao.updateLocation(locationId);
							locationIndex++;
							Intent intent = new Intent();
							intent.setAction(Constant.BROADCAST_UPDATE_LOCATION);
							context.sendBroadcast(intent);
							if (locationIndex < locationList.size()) {
								MyLocation myLocation = locationList.get(locationIndex);
								uploadLocation(myLocation);
							}
						}
					}

					if (flagIndex == 1) {
						caseIndex++;
						if (caseIndex < reportList.size()) {
							CaseReport report = reportList.get(caseIndex);
							uploadReport(report);
						}
					} else if (flagIndex == 2) {
						locationIndex++;
						if (locationIndex < locationList.size()) {
							MyLocation myLocation = locationList.get(locationIndex);
							uploadLocation(myLocation);
						}
					}
				}
				break;
			}
		}
	};
	/**
	 * 检测到网络时，上传本地未上传成功的
	 */
	public void upload() {
		String uid = MyApplication.getInstance().getUserId();
		if (uid != null && !uid.equals("")) {
			locationIndex = 0;
			caseIndex = 0;
			reportList.clear();
			locationList.clear();
			caseDao = new CaseDao(context);
			locationDao = new MyLocationDao(context);
			reportList = caseDao.getUnUploadList(uid);
			locationList = locationDao.getUnUploadList(uid);
			if (reportList != null && reportList.size() > 0) {
//			for (int i = 0; i < reportList.size(); i++) {
//                Log.i("upload",i+"次");
					CaseReport report = reportList.get(0);
					uploadReport(report);
//			}
			}
			if (locationList != null && locationList.size() > 0) {
//				for (int i = 0; i < locationList.size(); i++) {
//                    Log.i("upload",i+"次");
                   MyLocation location = locationList.get(0);
                   // uploadLocation(locationList);
					uploadLocation(location);
//				}
			}
		}
	}

    private void uploadLocation( List<MyLocation> locationList1) {
        String id = MyApplication.getInstance().getUserId();
        Map<String, String> map = new HashMap<String, String>();
        map.put("id",id);
        map.put("patch",locationList.toString());
        RequestParams params = new RequestParams(map);
        HttpTool.post(MyApplication.getInstance().getIP()
                        + Constant.UPLOAD_LOCATIONS, params,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                        Toast.makeText(context,"未上传轨迹上传成功!",Toast.LENGTH_SHORT).show();
                        String res = new String(bytes);
                        if (res != null && !"".equals(res)) {
                            JSONObject object = CommonTool.parseFromJson(res);
                            String status = CommonTool.getJsonString(object,"success");
                            if (status != null && status.equals("true")) {
                                for(MyLocation myLocation : locationList){
                                    locationDao.updateLocation(Integer.parseInt(myLocation.getId()));
                                }
                            }
                        }
                    }
                    @Override
                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                        Toast.makeText(context,"未上传轨迹上传失败!",Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void uploadLocation(MyLocation myLocation) {
		final String lid = myLocation.getId();
		Map<String, String> map = new HashMap<String, String>();
		map.put("net",myLocation.getNet());
		map.put("uLoti", myLocation.getLongitude());
		map.put("uLati", myLocation.getLatitude());
		map.put("uAlti", myLocation.getAltitude());
		map.put("uSpeed", myLocation.getSpeed());
		map.put("uDirection", myLocation.getBearing());
		map.put("uAddr", "");
		map.put("uTime", myLocation.getTime());
		map.put("uAccuracy", myLocation.getAccurary());
		map.put("battery", myLocation.getBattery());
		map.put("uLocType", myLocation.getLocType());
		String random = CommonTool.getRandom();
		String id = MyApplication.getInstance().getUserId();
		map.put("random", random);
		String md5Sign = MD5Tool.Md5(MD5Tool.Md5(Constant.KEY + random) + id);
		map.put("signature", md5Sign);
		map.put("id", id);
		RequestParams params = new RequestParams(map);
		HttpTool.post(MyApplication.getInstance().getIP()
				+ Constant.UPLOAD_TRAIL, params,
				new AsyncHttpResponseHandler() {
					public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
						String res = new String(arg2);
						System.out.println(res);
						if (res != null && !"".equals(res)) {
							JSONObject object = CommonTool.parseFromJson(res);
							String status = CommonTool.getJsonString(object,"success");
							if (status != null && status.equals("true")) {
								Message msg = handler.obtainMessage(DATA_SUCCESS);
								if (lid != null && !lid.equals("")) {
									msg.arg1 = Integer.parseInt(lid);
								}
								msg.arg2 = 2;
								msg.obj = res;
								msg.sendToTarget();
							}
						}
					}

					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
					}
				});
	}

	public void uploadReport(CaseReport report) {
		String uid = MyApplication.getInstance().getUserId();
		final Map<String, String> map = new HashMap<String, String>();
		final Map<String, File> fileMap = new HashMap<String, File>();
		final String id = report.getId();
		String random = CommonTool.getRandom();
		String md5Sign = MD5Tool.Md5(MD5Tool.Md5(Constant.KEY + random) + uid);
		final String str = "?id=" + uid + "&random=" + random + "&signature="
				+ md5Sign;
		map.put("uType", report.getuType());
		map.put("uContent", report.getuContent());
		map.put("q", report.getQ());
		map.put("uRemark", report.getuRemark());
		map.put("uTime", report.getuTime());
		map.put("uUserId", report.getuUserId());
		map.put("uLoti", report.getuLoti());
		map.put("uLati", report.getuLati());
		map.put("uAlti", report.getuAlti());
		map.put("uAddr", report.getuAddr());
		map.put("uSpeed", report.getuSpeed());
		map.put("uDirection", report.getuDirection());
		map.put("uAccuracy", report.getuAccurary());
		map.put("uLocType", report.getuLocType());
		String attachName = report.getAttach();
        System.out.println(map.toString());
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
					String res = HttpTool.post(MyApplication.getInstance()
							.getIP() + Constant.CASE_REPORT + str, map,
							fileMap, "attach");
					if (res != null && !res.equals("")) {
						Message msg = handler.obtainMessage(DATA_SUCCESS);
						if (id != null && !id.equals("")) {
							msg.arg1 = Integer.parseInt(id);
						}
						msg.arg2 = 1;
						msg.obj = res;
						msg.sendToTarget();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
