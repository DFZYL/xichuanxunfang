package com.weisen.xcxf.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import com.weisen.xcxf.Constant;

import com.weisen.xcxf.R;
import com.weisen.xcxf.adapter.PeriodAdapter;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.CaseType;
import com.weisen.xcxf.bean.CaseWorker;
import com.weisen.xcxf.bean.MyLocationDao;
import com.weisen.xcxf.bean.PeriodTime;
import com.weisen.xcxf.service.MyLocationService;
import com.weisen.xcxf.tool.CommonTool;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;
import com.weisen.xcxf.utils.ImageLoader;
import com.weisen.xcxf.widget.CircleImageView;

public class SelfCenterActivity extends BaseActivity implements OnClickListener {

	private static final int sleepTime = 2000;
	private long start, costTime;
//	private RelativeLayout rl_info, rl_update_pwd, rl_update_version,rl_offlinemap,
//			rl_period, rl_update_data, rl_my_report,rl_about,rl_delete_data,rl_trafficStats,rl_start_service,rl_question;
	private ImageView icon_updata;
	private Button bt_exit;
	private TextView tv_new_version, tv_name,name2, tv_time, tv_period,tv_open_service;
	private String name, head, beginTime, endTime, periodName;
	private boolean isUpdate = false, isNoUpdate = false;
	private ScrollView scrollView;
	private int period;
	private SharedPreferences preferences;
	private Editor editor;
	private PopupWindow popupWindow;
	private View periodView;
	private ListView lv_period;
	private List<PeriodTime> periodList;
	private PeriodAdapter periodAdater;
	private SwitchCompat switchCompat;

	private PackageInfo info;
    private CircleImageView iv_head,userIcon;
	private static final String TAG = "SelfCenterActivity";
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			super.handleMessage(msg);
			switch (msg.what) {
			case DATA_SUCCESS:
				Intent intent = new Intent(SelfCenterActivity.this, MyLocationService.class);
				stopService(intent);
				MyApplication.getInstance().logout();
				finish();
				int id = android.os.Process.myPid();
				if (id != 0) {
					android.os.Process.killProcess(id);
				}
				break;
			}
		}
	};

	@Override
	protected void initView() {
		super.initView();
		setContentView(R.layout.activity_selfcenter2);
		MyApplication.getInstance().setServiceIsOpen(true);
		try {
			PackageManager manager = this.getPackageManager();
			info = manager.getPackageInfo(this.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		rl_info = (RelativeLayout) findViewById(R.id.rl_info);
//		rl_update_pwd = (RelativeLayout) findViewById(R.id.rl_update_pwd);
//		rl_update_version = (RelativeLayout) findViewById(R.id.rl_update_version);
//		rl_update_data = (RelativeLayout) findViewById(R.id.rl_update_data);
//		rl_my_report = (RelativeLayout) findViewById(R.id.rl_my_report);
//		rl_period = (RelativeLayout) findViewById(R.id.rl_period);
//		rl_about = (RelativeLayout) findViewById(R.id.rl_about);
//		rl_delete_data = (RelativeLayout) findViewById(R.id.rl_delete_data);
//		rl_trafficStats = (RelativeLayout) findViewById(R.id.rl_trafficStats);
//		rl_offlinemap = (RelativeLayout) findViewById(R.id.rl_offlinemap);
//		rl_start_service = (RelativeLayout) findViewById(R.id.rl_start_service);
//		rl_question = (RelativeLayout) findViewById(R.id.rl_question);
		icon_updata = (ImageView) findViewById(R.id.icon_updata);
		icon_updata.setVisibility(View.GONE);
		tv_new_version = (TextView) findViewById(R.id.tv_new_version);
		tv_new_version.setText("V" + info.versionName);
		iv_head = (CircleImageView) findViewById(R.id.iv_head);
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_time = (TextView) findViewById(R.id.tv_time);
		tv_period = (TextView) findViewById(R.id.tv_period);
		bt_exit = (Button) findViewById(R.id.bt_exit);
        name2 = (TextView) findViewById(R.id.tv_name2);
		scrollView=(ScrollView)findViewById(R.id.scrollView);

		initTitle();
		iv_left.setVisibility(View.GONE);
        iv_right.setVisibility(View.GONE);
		tv_title.setText(getString(R.string.title_self));
		periodView = LayoutInflater.from(this).inflate(R.layout.period_list,null);
		lv_period = (ListView) periodView.findViewById(R.id.lv_period);
		tv_open_service=(TextView)findViewById(R.id.tv_open_service);

		switchCompat =(SwitchCompat) findViewById(R.id.switch_btn);
		checkService();
		switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					tv_open_service.setText("关闭位置上报");
                     MyApplication.getInstance().setServiceIsOpen(true);
					startService(new Intent(SelfCenterActivity.this, MyLocationService.class));
				} else {

						tv_open_service.setText("开启位置上报");
					MyApplication.getInstance().setServiceIsOpen(false);

					stopService(new Intent(SelfCenterActivity.this, MyLocationService.class));
				}
			}
		});
	}

	private void checkService() {
		if (MyApplication.getInstance().getServiceOpen()) {
			tv_open_service.setText("关闭位置服务");
			switchCompat.setChecked(true);
		} else {
			tv_open_service.setText("开启位置服务");
			switchCompat.setChecked(false);
		}
	}

	@Override
	protected void initData() {

		super.initData();
		periodList = PeriodTime.getList();
		periodAdater = new PeriodAdapter(this, periodList);
		lv_period.setAdapter(periodAdater);
		name = MyApplication.getInstance().getName();
		head = MyApplication.getInstance().getHeadPic();
        String[] heads = head.split("/");
        preferences = getSharedPreferences(Constant.APP_SP, MODE_MULTI_PROCESS);
		editor = preferences.edit();
		beginTime = preferences.getString(Constant.SP_BEGIN,Constant.DEFAULT_BEGIN);
		endTime = preferences.getString(Constant.SP_END, Constant.DEFAULT_END);
		MyApplication.getInstance().beginTime = beginTime;
		MyApplication.getInstance().endTime = endTime;
		periodName = preferences.getInt(Constant.DEFAULTDISTANCE,
				200)+"米";
		tv_time.setText(beginTime + "-" + endTime);
        if(periodName.equals("0米")){
            tv_period.setText("不上传");
        }else{
            tv_period.setText(periodName);
        }
		if (head != null && !head.equals("") && !heads[heads.length-1].substring(0,7).equals("default")){
//            ImageLoader.getInstance().displayImage(head, iv_head,MyApplication.getInstance().options);

			ImageLoader.getInstance(this).disPlayDefault(iv_head, head);

		}else{
            iv_head.setBackgroundResource(R.drawable.user_default);
            if(name.length()>2){
                name2.setText(name.substring(name.length()-2,name.length()));
            }else{
                name2.setText(name);
            }
        }
		tv_name.setText(name);
		UmengUpdateAgent.setUpdateAutoPopup(false);
		UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
            public void onUpdateReturned(int updateStatus,
                                         UpdateResponse updateInfo) {

                switch (updateStatus) {
                    case UpdateStatus.Yes: // has update
                        if (isUpdate) {
                            UmengUpdateAgent.showUpdateDialog(
                                    SelfCenterActivity.this, updateInfo);
                        } else {
                            icon_updata.setVisibility(View.VISIBLE);
                            isUpdate = true;
                        }
                        break;
                    case UpdateStatus.No: // has no update
                        icon_updata.setVisibility(View.GONE);
                        if (isNoUpdate) {
                            showShortToast("已经是最新版本！");
                        }
                        isNoUpdate = true;
                        break;
                    case UpdateStatus.Timeout: // time out
                        showShortToast("更新超时！");
                        break;
                }
            }
        });
	}

	@Override
	protected void initEvent() {

		super.initEvent();
        //	private RelativeLayout rl_info, rl_update_pwd, rl_update_version,rl_offlinemap,
//			rl_period, rl_update_data, rl_my_report,rl_about,rl_delete_data,rl_trafficStats,rl_start_service,rl_question;
		findViewById(R.id.rl_update_pwd).setOnClickListener(this);
		findViewById(R.id.rl_update_version).setOnClickListener(this);
		findViewById(R.id.rl_info).setOnClickListener(this);
		findViewById(R.id.rl_period).setOnClickListener(this);
		bt_exit.setOnClickListener(this);
		findViewById(R.id.rl_update_data).setOnClickListener(this);
		findViewById(R.id.rl_my_report).setOnClickListener(this);
		findViewById(R.id.rl_about).setOnClickListener(this);
		findViewById(R.id.rl_delete_data).setOnClickListener(this);
		findViewById(R.id.rl_trafficStats).setOnClickListener(this);
		findViewById(R.id.rl_offlinemap).setOnClickListener(this);
		findViewById(R.id.rl_start_service).setOnClickListener(this);
		findViewById(R.id.rl_question).setOnClickListener(this);
		findViewById(R.id.rl_update_version).performClick();
		periodView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (popupWindow != null)
                    popupWindow.dismiss();
            }
        });
		lv_period.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    final int position, long arg3) {
                periodName = periodList.get(position).getName();
                AlertDialog.Builder builder = new AlertDialog.Builder(SelfCenterActivity.this);
                builder.setMessage("您要设置" + periodName + "距离上报,请确定操作?");
                builder.setTitle("设置上报距离");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        period = periodList.get(position).getTime();
                        tv_period.setText(periodName);
                        editor.putString(Constant.SP_PERIOD, period + "");
                        editor.putInt(Constant.DEFAULTDISTANCE, period);
                        editor.putString(Constant.SP_PERIOD_NAME, periodName);
                        MyApplication.getInstance().period = period + "";
                        editor.commit();
                        popupWindow.dismiss();
                        dialog.dismiss();

                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        popupWindow.dismiss();
                    }
                });
                builder.create().show();
            }
        });



	}
	private void cleanList() {
		new AlertDialog.Builder(this)
		.setTitle("提示")
		.setMessage("您确定要删除历史轨迹？")
		.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,int which) {
						dialogFlag = true;
						//清除历史轨迹
						MyLocationDao locationDao = new MyLocationDao(SelfCenterActivity.this);
						String uid = MyApplication.getInstance().userId;
						locationDao.delete(uid);
                        showShortToast("清除成功!");
					}
				})
		.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
					}
				}).show();
	}
	private void startSercice() {
		new AlertDialog.Builder(this)
		.setTitle("提示")
		.setMessage("您要重新启动定位服务吗？")
		.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog,int which) {
				dialogFlag = true;
				//启动定位服务
				Intent cIntent1 = new Intent(SelfCenterActivity.this,MyLocationService.class);
				startService(cIntent1);
				MyApplication.getInstance().setServiceIsOpen(true);
                showShortToast("重启成功!");
				checkService();
			}
		})
		.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog,
					int which) {
				dialog.dismiss();
			}
		}).show();
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.rl_info:
			Intent intent = new Intent(SelfCenterActivity.this,SelfInfoActivity.class);
			startActivityForResult(intent, 100);
			break;
		case R.id.rl_update_pwd:
			Intent updatePwdIntent = new Intent(SelfCenterActivity.this,UpdatePwdActivity.class);
			startActivity(updatePwdIntent);
			break;
		case R.id.rl_my_report:
			Intent reportIntent = new Intent(SelfCenterActivity.this,ReportListActivity.class);
			startActivity(reportIntent);
			break;
		case R.id.rl_about:
			Intent aboutIntent = new Intent(SelfCenterActivity.this,AboutActivity.class);
			startActivity(aboutIntent);
			break;
		case R.id.rl_trafficStats:
			Intent mIntent = new Intent(SelfCenterActivity.this,TrafficStatsActivity.class);
			startActivity(mIntent);
			break;
		case R.id.rl_offlinemap:
			Intent cIntent = new Intent(SelfCenterActivity.this,OfflineMapActivity.class);
			startActivity(cIntent);
			break;
		case R.id.rl_question:
			Intent mIntent1 = new Intent(SelfCenterActivity.this,QuestionActivity.class);
			startActivity(mIntent1);
			break;
		case R.id.rl_start_service:
			startSercice();
			break;
		case R.id.rl_delete_data:
			cleanList();
			break;
		case R.id.rl_update_version:
			UmengUpdateAgent.forceUpdate(this);
			break;
		case R.id.rl_period:
			showWindow();
            break;
		case R.id.rl_update_data:
			dialogFlag = true;
			method = "update";
			getServer(MyApplication.getInstance().getIP() + Constant.UPDATE_DATA, null, "get");
			break;
		case R.id.bt_exit:
            View view1 = View.inflate(getApplicationContext(),R.layout.layout_dialog,null);
            final EditText psw = (EditText) view1.findViewById(R.id.psw);
            new AlertDialog.Builder(this)
					.setTitle("退出提示")
					.setMessage("请输入密码验证")
                    .setView(view1)
					.setPositiveButton("确认",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialogFlag = true;
                                    start = System.currentTimeMillis();
                                    method = "exit";
                                    String passWord = psw.getText().toString().trim();
                                    if(!isPassword(passWord)){
                                        showShortToast("密码不能为空!");
                                        return;
                                    }
                                    Map<String,String> map = new HashMap<String,String>();
                                    map.put("pwd",passWord);
                                    getServer(MyApplication.getInstance()
                                                    .getIP() + Constant.EXIT, map,
                                            "upload");
                                }
                            })
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).show();
            psw.setFocusable(true);
            psw.setFocusableInTouchMode(true);
            psw.requestFocus();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                               public void run() {
                                   InputMethodManager inputManager =
                                           (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                   inputManager.showSoftInput(psw, 0);
                               }
                           },
                    200);
            break;
		}
	}

	@Override
	protected void processSuccessResult(String res) {
		super.processSuccessResult(res);
		if (method.equals("exit")) {
			handler.sendEmptyMessage(DATA_SUCCESS);
		} else if (method.equals("getPeriodTime")) {
			JSONObject object = CommonTool.parseFromJson(res);
			String beginTime = CommonTool.getJsonString(object, "btime");
			String etime = CommonTool.getJsonString(object, "etime");
			editor.putString(Constant.SP_BEGIN, beginTime);
			editor.putString(Constant.SP_END, etime);
			editor.commit();
		} else if (method.equals("update")) {
            showShortToast("更新数据成功!");
			JSONObject object = CommonTool.parseFromJson(res);
			List<CaseType> caseTypeList = CaseType.parseList(res, "types");
			List<CaseWorker> caseWorkerList = CaseWorker.parseList(res,"upUsers");
			String newBeginTime = CommonTool.getJsonString(object, "btime");
			String newEndTime = CommonTool.getJsonString(object, "etime");
			String caseTypeStr = preferences.getString(Constant.SP_CASE_TYPE, "");
            List<CaseType> caseTypeList1 = CaseType.parseList(caseTypeStr, "list");
            List<CaseType> tempList = new ArrayList<CaseType>();
            for(int i= 0;i<caseTypeList.size();i++){
                CaseType caseType; boolean flag = false;
                for (int j=0;j<caseTypeList1.size();j++){
                    if(caseTypeList.get(i).getId().equals(caseTypeList1.get(j).getId())){
                            caseType = caseTypeList1.get(j);
                            tempList.add(caseType);
                            flag = true;
                            break;
                        }
                }
                    if(!flag){
                        tempList.add(caseTypeList.get(i));
                    }
            }
			editor.putString(Constant.SP_CASE_TYPE,CaseType.getStr(tempList));
			editor.putString(Constant.SP_CASE_WORKERS,CaseWorker.getStr(caseWorkerList));
			editor.putString(Constant.SP_BEGIN, newBeginTime);
			editor.putString(Constant.SP_END, newEndTime);
			editor.commit();
			if (!newBeginTime.equals(beginTime) || !newEndTime.equals(endTime)) {
				Intent intent = new Intent(this, MyLocationService.class);
				startService(intent);
				beginTime = newBeginTime;
				endTime = newEndTime;
				tv_time.setText(beginTime + "-" + endTime);
				MyApplication.getInstance().beginTime = newBeginTime;
				MyApplication.getInstance().endTime = newEndTime;
			}
			Intent intent = new Intent();
			intent.setAction(Constant.BROADCAST_UPDATE_TYPE);
			sendBroadcast(intent);
		}
	}

	// 事件类型
	private void showWindow() {
		if (popupWindow == null) {
			popupWindow = new PopupWindow(periodView,
					WindowManager.LayoutParams.MATCH_PARENT,
					WindowManager.LayoutParams.MATCH_PARENT);
		}
		// 使其聚集
		popupWindow.setFocusable(true);
		// 设置允许在外点击消失
		popupWindow.setOutsideTouchable(true);
		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		// popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.alpha_dark)));
		popupWindow.showAtLocation(tv_period, Gravity.CENTER, 0, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (resultCode == 100) {
			if (intent != null) {
                System.out.println("dasdasd");
                name = intent.getStringExtra("name");
				head = intent.getStringExtra("headPic");
				tv_name.setText(name);
//				if (head != null && !head.equals(""))
//					ImageLoader.getInstance().displayImage(head, iv_head,
//							CommonTool.getOptions(R.drawable.user_head));
                String[] heads = head.split("/");
                if (head != null && !head.equals("") && !heads[heads.length-1].substring(0,7).equals("default")){
//                    ImageLoader.getInstance().displayImage(head, iv_head,MyApplication.getInstance().options);
					ImageLoader.getInstance(this).disPlayDefault(iv_head, head);

					name2.setVisibility(View.GONE);
                }else{
                    iv_head.setBackgroundResource(R.drawable.user_default);
                    name2.setText(name);
                }

			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		name = MyApplication.getInstance().getName();
		head = MyApplication.getInstance().getHeadPic();
		tv_name.setText(name);
		Log.i(TAG, "onResume: "+head);
		checkService();
//		if (head != null && !head.equals(""))
//			ImageLoader.getInstance().displayImage(head, iv_head);
	}
	@Override
	protected void processFailResult() {

		if (method.equals("exit")) {
			handler.sendEmptyMessage(DATA_SUCCESS);
		}
	}
}
