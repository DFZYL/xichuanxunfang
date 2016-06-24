package com.weisen.xcxf.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import cn.jpush.android.api.JPushInterface;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.tool.CommonTool;

/**
 * 
 * @author JQY 欢迎界面，下载欢迎界面图片
 */

public class WelcomeActivity extends BaseActivity {
	private static final int sleepTime = 3000;
	private long start, costTime,waitTime;
	private ImageView iv_welcome;
	private String oldLogo, oldLogoPath;
	private SharedPreferences preferences;
	private Editor editor;
	protected static final int DATA_IMG = 1;
	private Intent intent = null;
    private boolean isLoad;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
			switch (msg.what) {
			case DATA_SUCCESS:
				costTime = System.currentTimeMillis() - start;
				// 等待sleeptime时长
				if (sleepTime - costTime > 0) {
					waitTime = sleepTime - costTime;
				}
				String userId = MyApplication.getInstance().getUserId();
				if (userId != null && !userId.equals("")) {
					intent = new Intent(WelcomeActivity.this,MainActivity.class);
                    //getWorkStatus(userId);

				} else {
					// 进入主页面
					intent = new Intent(WelcomeActivity.this,LoginActivity.class);
				}
				new Handler().postDelayed(new Runnable() {
		            public void run() {
		                startActivity(intent);
		                finish();
		            }
		        }, waitTime);
				break;
			case 1:
                ImageLoader.getInstance().displayImage(oldLogoPath, iv_welcome,MyApplication.getInstance().options);
                costTime = System.currentTimeMillis() - start;
                // 等待sleeptime时长
                if (sleepTime - costTime > 0) {
                    waitTime = sleepTime - costTime;
                }
                String userId2 = MyApplication.getInstance().getUserId();
                if (userId2 != null && !userId2.equals("")) {
                    intent = new Intent(WelcomeActivity.this,MainActivity.class);
                   // getWorkStatus(userId2);
                } else {
                    // 进入主页面
                    intent = new Intent(WelcomeActivity.this,LoginActivity.class);
                }
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        startActivity(intent);
                        finish();
                    }
                }, waitTime);

				break;
			}
		}
	};

    public void getWorkStatus(String id){
        Map<String, String> map = new HashMap<String, String>();
        map.put("id",  id);
        method = "getStatus";
        getServer(MyApplication.getInstance().getIP() + Constant.GETWORKSTATUS, map,"get");
    }

	@Override
	protected void initView() {
		
		super.initView();
		start = System.currentTimeMillis();
		setContentView(R.layout.activity_welcome);
		iv_welcome = (ImageView) findViewById(R.id.iv_welcome);
//		initPush();
	}

	@Override
	protected void initData() {
		
		super.initData();
		preferences = getSharedPreferences(Constant.APP_SP, MODE_PRIVATE);
        isLoad = preferences.getBoolean("isLoad", false);
		oldLogo = preferences.getString(Constant.SP_APPIMG, "");
        editor = preferences.edit();
        if(preferences.getBoolean(Constant.ISFRIST,false)){
            if ("".equals(preferences.getString(Constant.BSEIP,""))){
                editor.putString(Constant.SP_IP, Constant.IP).commit();
            }
            editor.putString(Constant.BSEIP,Constant.IP).commit();
            editor.putString(Constant.SP_IP, Constant.IP).commit();
            editor.putBoolean(Constant.ISFRIST,false);
        }
        if(!preferences.getString(Constant.BSEIP,"").equals(Constant.IP)){
            editor.putString(Constant.SP_IP, Constant.IP).commit();
        }
        String userId = preferences.getString(Constant.SP_USERID, "");
        if(userId == null || userId ==""){
              editor.putString(Constant.SP_APPIMGPATH,"").commit();
        }
		oldLogoPath = preferences.getString(Constant.SP_APPIMGPATH, "");
		dialogFlag = false;
		if (!oldLogoPath.equals("")) {
			handler.sendEmptyMessage(1);
            System.out.println("没有加载图片");
        }else{
            Map<String, String> map = new HashMap<String, String>();
            map.put("p",  preferences.getString(Constant.COMID,""));
            method = "getConfig";
            getServer(MyApplication.getInstance().getIP() + Constant.WELCOME, map,"get");
            System.out.println("从网络获取");
        }
	}

	@Override
	protected void initEvent() {
		
		super.initEvent();
	}

	protected void initPush() {
		// 初始化 JPush。如果已经初始化，但没有登录成功，则执行重新登录。
		JPushInterface.init(getApplicationContext());
	}

	@Override
	protected void processSuccessResult(String res) {
		super.processSuccessResult(res);
        if(method.equals("getConfig")){
            JSONObject object = CommonTool.parseFromJson(res);
            String title = CommonTool.getJsonString(object, "title");
            String logo = CommonTool.getJsonString(object, "logo");
            editor.putString(Constant.SP_APPIMGPATH,logo).commit();
            editor.putString(Constant.SP_APPNAME,title).commit();
            oldLogoPath = logo;
            System.out.println("路径:::"+oldLogoPath);
            handler.sendEmptyMessage(1);
        }
        if(method.equals("getStatus")){
            JSONObject object = CommonTool.parseFromJson(res);
            System.out.println("getStatus"+object.toString());
        }

	}
    public  void copy(Context context) {
        try {
            String filepath = Environment.getExternalStorageDirectory() + "/"
                    + "BaiduMapSDK" + "/" + "vmp" + "/" + "h" + "/"
                    + "Zheng_Zhou_Shi_268.dat_svc";
            String path = Environment.getExternalStorageDirectory() + "/"
                    + "BaiduMapSDK" + "/" + "vmp" + "/" + "h" + "/";
            File file = new File(path);
            if (!file.exists()) {
                file.mkdir();
            }
            if (!(new File(filepath)).exists()) {
                new File(filepath).createNewFile();
            }
                InputStream is = context.getAssets().open(
                        "Zheng_Zhou_Shi_268.dat_svc");
            FileOutputStream fos = new FileOutputStream(filepath);
                byte[] buffer = new byte[8192];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	@Override
	protected void processFailResult() {
		
		handler.sendEmptyMessage(DATA_SUCCESS);
	}

	@Override
	protected void onResume() {
		super.onResume();
		JPushInterface.onResume(WelcomeActivity.this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		JPushInterface.onPause(WelcomeActivity.this);
	}
}
