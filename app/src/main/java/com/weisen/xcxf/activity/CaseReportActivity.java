package com.weisen.xcxf.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.weisen.xcxf.R;
import com.weisen.xcxf.adapter.CaseTypeAdapter;
import com.weisen.xcxf.adapter.CaseWorkerAdapter;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.CaseDao;
import com.weisen.xcxf.bean.CaseReport;
import com.weisen.xcxf.bean.CaseType;
import com.weisen.xcxf.bean.CaseWorker;
import com.weisen.xcxf.bean.MyLocation;
import com.weisen.xcxf.bean.MyLocationDao;
import com.weisen.xcxf.fragment.CaseReportFragment;
import com.weisen.xcxf.fragment.TestFragment;
import com.weisen.xcxf.tool.Bimp;
import com.weisen.xcxf.tool.FileUtils;
import com.weisen.xcxf.tool.ImageItem;
import com.weisen.xcxf.widget.MyGridViewIn;
import com.weisen.xcxf.widget.MyPop;
import com.weisen.xcxf.widget.NoScrollGridView;

import java.io.File;
import java.util.List;

public class CaseReportActivity extends BaseActivity{
    private RelativeLayout rl_worker;
    private TextView tv_case_type, tv_case_category, tv_case_worker,
            locationAddr_txt;
    private ImageView addCasetype;
    private EditText et_case_desc1, et_case_remark;
    private View caseTypeView, caseWorkerView;
    private NoScrollGridView gv_case_type, gv_case_type2,
            gv_case_worker;
    private MyGridViewIn gv_img;
    // private RadioButton rb_case_upload, rb_case_deal;

    private CaseTypeAdapter caseTypeAdapter, caseTypeAdapter2;
    private CaseWorkerAdapter caseWorkerAdapter;

    private SharedPreferences preferences;
    private Editor editor;

    private boolean isUpload = false, isType = true, isPlaying = false;
    private String caseTypeStr, caseWorkerStr, caseTypeId = "",
            caseCategoryId = "", caseWorkerId = "", caseDesc, caseRemark;
    public String photoPath, attachName = "", voiceName = "", moviePath = "";
    public int times = 0, movieTimes;// 录音时长
    private MediaPlayer mPlayer = null;
    private List<CaseType> caseTypeList, childCaseTypeList1,
            childCaseTypeList2, mCaseTypesList;
    private List<CaseWorker> caseWorkerList;
    private CaseDao caseDao;
    //	private GridAdapter imgAdapter;
    private Button bt_upload;
    private MyPop pw;

    public static final int PHOTOHRAPH = 1;// 拍照
    public static final int PHOTOZOOM = 2; // 缩放
    public static final String IMAGE_UNSPECIFIED = "image/*";

    private static final int DATA_FAIL = 1;
    private static final int DATA_IMG = 2;
    private static final int SOS = 3;
    public static Bitmap bimap;
    private CaseReport report;
    private MyLocationDao myLocationDao;
    private List<MyLocation> locationList;
    private String uLocType;

    private FrameLayout content;
    private GridView gridView;
    private CaseReportFragment caseReportFragment;
    private int[] imageIds = {R.drawable.titlesh,R.drawable.rl};
    private String[] titles = {"事件上传","请假申请"};
    private WebView webView;
    private ProgressBar progressBar;
    @Override
    protected void initView() {
        super.initView();
        setContentView(R.layout.activity_casesreported);
        content = (FrameLayout) findViewById(R.id.content);
        webView = (WebView) findViewById(R.id.webview);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        initTitle();
        iv_left.setVisibility(View.GONE);
		tv_title.setText(getString(R.string.title_case_report));
		tv_right.setVisibility(View.VISIBLE);
		tv_right.setText("紧急求助");
        tv_right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iny1 = new Intent(CaseReportActivity.this,
                        RecordingActivitys.class);
                startActivity(iny1);
            }
        });
        //initWebView();
        caseReportFragment = new CaseReportFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.content,caseReportFragment).commit();
        gridView = (GridView) findViewById(R.id.list);
        adapter = new MyAdapter();
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==0){
                    getSupportFragmentManager().beginTransaction().replace(R.id.content,caseReportFragment).commit();
                }else {
                    getSupportFragmentManager().beginTransaction().replace(R.id.content,new TestFragment()).commit();
                }
            }
        });
    }

    private void initWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);  //支持js
        webSettings.setUseWideViewPort(false);  //将图片调整到适合webview的大小
        webSettings.setSupportZoom(true);  //支持缩放
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webView.loadUrl(MyApplication.getInstance().getIP()+"test/test.jsp");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                String scheme = uri.getScheme();
                if ("xunfang".equals(scheme)) {

                }
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (progressBar == null) {
                    return;
                }
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private MyAdapter adapter;
    class MyAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return imageIds.length+1;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = View.inflate(CaseReportActivity.this,R.layout.item_mountil_case,null);
            ImageView imageView = (ImageView) v.findViewById(R.id.icon);
            TextView textView = (TextView) v.findViewById(R.id.tv);
//            if(i==0){
//                imageView.setBackgroundResource(R.drawable.titlesh);
//                textView.setText("事件上报");
//            }
//           else if(i==1){
//                imageView.setBackgroundResource(R.drawable.rl);
//                textView.setText("请假申请");
//            }else{
//                imageView.setBackgroundResource(R.drawable.rl);
//                textView.setText("请假申请");
//            }

        if(i<=imageIds.length-1){
            imageView.setBackgroundResource(imageIds[i]);
            textView.setText(titles[i]);
        }else{
            imageView.setBackgroundResource(R.drawable.add_case);
            textView.setText("新增");
        }
            return v;
        }
    }


    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 接语音
		if (resultCode == 12) {
			times = data.getIntExtra("totaltime", 0);
			if (times > 0) {
				if (voiceName != null && !voiceName.equals("")) {
					File file = new File(voiceName);
					if (file != null && file.exists()) {
						file.delete();
					}
				}
				voiceName = data.getStringExtra("filePath");
                caseReportFragment.imgAdapter.notifyDataSetChanged();
			}
		}
        if (resultCode==88){
                moviePath = data.getStringExtra("moviePath");
                movieTimes = data.getIntExtra("movieTimes", 1);
            caseReportFragment.imgAdapter.notifyDataSetChanged();
        }
		if (resultCode == Activity.RESULT_OK) {
                Bitmap bm = FileUtils.compressImageFromFile(photoPath);
                // Bitmap bm = BitmapFactory.decodeFile(photoPath);
                ImageItem takePhoto = new ImageItem();
                takePhoto.setBitmap(bm);
                takePhoto.setType("photo");
                takePhoto.setImagePath(photoPath);
                Bimp.tempSelectBitmap.add(takePhoto);
                System.out.println(Bimp.tempSelectBitmap.size()+"<<<<<<<<");
                caseReportFragment.imgAdapter.notifyDataSetChanged();
		}
	}

}



//	private UpdateTypeReceiver updateTypeReceiver;
//	private ConnectionReceiver mConnectionReceiver;

//	private Handler handler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//
//			super.handleMessage(msg);
//			switch (msg.what) {
//			case DATA_SUCCESS:
//				hideProgressDialog();
//				String res = (String) msg.obj;
//				processResult(res, true);
//				break;
//			case DATA_FAIL:
//				report.setIsSuccess("false");
//				caseDao.addCase(report);
//				hideProgressDialog();
//				showShortToast("上报成功!");
//				clear();
//				break;
//			case DATA_IMG:
//				imgAdapter.notifyDataSetChanged();
//				break;
//			case SOS:
//				showShortToast("上报成功");
//				break;
//			}
//		}
//	};
//
//	@Override
//	protected void initView() {
//
//		PublicWay.activityList.add(this);
//		Res.init(this);
//		setContentView(R.layout.activity_casesreported);
//		// 广播刷新界面，由类型界面回跳过来
//		IntentFilter intentFilter = new IntentFilter();
//		intentFilter.addAction("action.refreshFriend");
//		registerReceiver(mRefreshBroadcastReceiver, intentFilter);
//		initTitle();// 加载头标题
//		iv_left.setVisibility(View.GONE);
//		tv_title.setText(getString(R.string.title_case_report));
//		tv_right.setVisibility(View.VISIBLE);
//		tv_right.setText("紧急求助");
//		rl_worker = (RelativeLayout) findViewById(R.id.rl_worker);
//		tv_case_type = (TextView) findViewById(R.id.tv_case_type);
//		tv_case_category = (TextView) findViewById(R.id.tv_case_category);
//		tv_case_worker = (TextView) findViewById(R.id.tv_case_worker);
//		et_case_desc1 = (EditText) findViewById(R.id.et_case_desc1);
//		et_case_remark = (EditText) findViewById(R.id.et_case_remark);
//		// rb_case_upload = (RadioButton) findViewById(R.id.rb_case_upload);
//		// rb_case_deal = (RadioButton) findViewById(R.id.rb_case_deal);
//		gv_img = (MyGridViewIn) findViewById(R.id.gv_img);
//		gv_img.setSelector(new ColorDrawable(Color.TRANSPARENT));
//		gv_case_type = (NoScrollGridView) findViewById(R.id.gv_case_type);
//		gv_case_type2 = (NoScrollGridView) findViewById(R.id.gv_case_type2);
//		gv_case_worker = (NoScrollGridView) findViewById(R.id.gv_case_worker);
//		caseTypeView = LayoutInflater.from(this).inflate(
//                R.layout.case_type_list, null);
//		caseWorkerView = LayoutInflater.from(this).inflate(
//                R.layout.case_type_list, null);
//		bt_upload = (Button) findViewById(R.id.bt_upload);
//		pw = new MyPop(CaseReportActivity.this, this, true);
//		addCasetype = (ImageView) findViewById(R.id.addCaseType);
//		locationAddr_txt = (TextView) findViewById(R.id.locationAddr_txt);
//		locationAddr_txt.setText(MyApplication.getInstance().address);
//		GpsTool gpsTool = new GpsTool(this);
//		gpsTool.getAddr();
//
//		hideKeyboard();
//
//	}
//
//	@Override
//	protected void initData() {
//
//		Bimp.tempSelectBitmap.clear();
//		updateTypeReceiver = new UpdateTypeReceiver();
//		IntentFilter filter = new IntentFilter();
//		filter.addAction(Constant.BROADCAST_UPDATE_TYPE);
//		registerReceiver(updateTypeReceiver, filter);
//		caseDao = new CaseDao(this);
//		dialogFlag = false;
//		mCaseTypesList = new ArrayList<CaseType>();
//		caseTypeList = new ArrayList<CaseType>();
//		caseTypeAdapter = new CaseTypeAdapter(this);
//		caseTypeAdapter2 = new CaseTypeAdapter(this);
//		gv_case_type.setAdapter(caseTypeAdapter);
//		gv_case_type2.setAdapter(caseTypeAdapter2);
//
//		caseWorkerList = new ArrayList<CaseWorker>();
//		caseWorkerAdapter = new CaseWorkerAdapter(this);
//		gv_case_worker.setAdapter(caseWorkerAdapter);
//		preferences = getSharedPreferences(Constant.APP_SP, MODE_PRIVATE);
//		editor = preferences.edit();
//		// 获取本地存储数据事件类型
//		caseTypeStr = preferences.getString(Constant.SP_CASE_TYPE, "");
//		if (caseTypeStr != null && !caseTypeStr.equals("")
//				&& !caseTypeStr.equals("{\"list\":[]}")) {
//			caseTypeList = CaseType.parseList(caseTypeStr, "list");
//			for (int i = 0; i < caseTypeList.size(); i++) {
//				CaseType mCaseType = caseTypeList.get(i);
//				// 获取类型值为1的数据插入列表
//				if (mCaseType.getIsChooseCase().equals("1")) {
//					mCaseTypesList.add(mCaseType);
//				}
//			}
//			childCaseTypeList1 = CaseType.getChildList1(mCaseTypesList);
//			caseTypeAdapter.setList(childCaseTypeList1);
//
//			if (childCaseTypeList1.isEmpty()) {
//				childCaseTypeList1 = CaseType.getChildList1(caseTypeList);
//				caseTypeAdapter.setList(childCaseTypeList1);
//				// dialogFlag = false;
//				// method = "getType";
//				// getServer(MyApplication.getInstance().getIP() +
//				// Constant.CASE_TYPE,null, "get");
//			}
//		} else {// 本地没有数据请求网络下载
//			dialogFlag = false;
//			method = "getType";
//			getServer(MyApplication.getInstance().getIP() + Constant.CASE_TYPE,
//					null, "get");
//		}
//
//		// 子层列表关联查询
//		caseWorkerStr = preferences.getString(Constant.SP_CASE_WORKERS, "");
//		if (caseWorkerStr != null && !caseWorkerStr.equals("")) {
//			caseWorkerList = CaseWorker.parseList(caseWorkerStr, "list");
//			if (caseWorkerList != null && caseWorkerList.size() > 0)
//				caseWorkerAdapter.setList(caseWorkerList);
//			else if (method.equals("")) {
//				method = "getWorker";
//				getServer(MyApplication.getInstance().getIP()
//						+ Constant.CASE_USER, null, "get");
//			}
//		} else if (method.equals("")) {
//			method = "getWorker";
//			getServer(MyApplication.getInstance().getIP() + Constant.CASE_USER,
//					null, "get");
//		}
//		// 插入图片列表
//		imgAdapter = new GridAdapter(this);
//		imgAdapter.update();
//		gv_img.setAdapter(imgAdapter);
//	}
//
//	@Override
//	protected void initEvent() {
//		tv_right.setOnClickListener(this);
//		iv_left.setVisibility(View.GONE);
//		bt_upload.setOnClickListener(this);
//		addCasetype.setOnClickListener(this);
//		gv_case_type.setOnItemClickListener(new OnItemClickListener() {
//
//			public void onItemClick(AdapterView<?> arg0, View arg1,
//					int position, long arg3) {
//				et_case_desc1.setVisibility(View.VISIBLE);
//				CaseType type = childCaseTypeList1.get(position);
//				if (!caseTypeId.equals(type.getId())) {
//					caseTypeAdapter.setSelected(position);
//					caseTypeAdapter2.setSelected(-1);
//					caseTypeId = type.getId();
//					String caseTypeName = type.getName();
//					tv_case_type.setText(caseTypeName);
//					childCaseTypeList2 = CaseType.getChildList2(caseTypeList,
//							caseTypeId);
//					caseTypeAdapter2.setList(childCaseTypeList2);
//					caseCategoryId = "";
//					tv_case_category.setText("");
//					et_case_desc1.setText("");
//				}
//			}
//		});
//		gv_case_type2.setOnItemClickListener(new OnItemClickListener() {
//
//			public void onItemClick(AdapterView<?> arg0, View arg1,
//					int position, long arg3) {
//				CaseType category = childCaseTypeList2.get(position);
//				if (!caseCategoryId.equals(category.getId())) {
//					caseTypeAdapter2.setSelected(position);
//					caseCategoryId = category.getId();
//					String caseCategoryName = category.getName();
//					tv_case_category.setText(caseCategoryName);
//					et_case_desc1.setText(caseCategoryName);
//				}
//			}
//		});
//		gv_case_worker.setOnItemClickListener(new OnItemClickListener() {
//
//			public void onItemClick(AdapterView<?> arg0, View arg1,
//					int position, long arg3) {
//				CaseWorker worker = caseWorkerList.get(position);
//				if (!caseWorkerId.equals(worker.getId())) {
//					caseWorkerAdapter.setSelected(position);
//					caseWorkerId = worker.getId();
//					String workerName = worker.getName();
//					tv_case_worker.setText(workerName);
//				}
//
//			}
//		});
//		// rb_case_upload.setOnCheckedChangeListener(new
//		// OnCheckedChangeListener() {
//		//
//		// @Override
//		// public void onCheckedChanged(CompoundButton arg0,
//		// boolean isCheck) {
//		//
//		// if (isCheck) {
//		// isUpload = true;
//		// gv_case_worker.setVisibility(View.VISIBLE);
//		// }
//		//
//		// }
//		// });
//		// rb_case_deal.setOnCheckedChangeListener(new OnCheckedChangeListener()
//		// {
//		//
//		// @Override
//		// public void onCheckedChanged(CompoundButton arg0, boolean isCheck) {
//		//
//		// if (isCheck) {
//		// isUpload = false;
//		// rl_worker.setVisibility(View.GONE);
//		// gv_case_worker.setVisibility(View.GONE);
//		// }
//		// }
//		// });
//		gv_img.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1,
//					int position, long arg3) {
//				if (caseTypeId.equals("") && caseCategoryId.equals("")) {
//					bt_upload.setBackgroundColor(getResources().getColor(
//							R.color.gray));
//					bt_upload.setClickable(false);
//				} else {
//					bt_upload.setBackgroundColor(getResources().getColor(
//							R.color.blue));
//					bt_upload.setClickable(true);
//				}
//
//				if (position == Bimp.tempSelectBitmap.size()) {
//					if (voiceName != null && !voiceName.equals("")) {
//						// 语音
//						if (isPlaying) {
//							stopPlaying();
//							isPlaying = false;
//						} else {
//							startPlaying(voiceName);
//							isPlaying = true;
//						}
//					} else {
//                        if(!TextUtils.isEmpty(moviePath)){
//                           startActivity(new Intent(CaseReportActivity.this,PreViewMovie.class)
//                                   .putExtra("path",moviePath)
//                           .putExtra("timeCount",movieTimes));
//                        }else{
//                            hideKeyboard();
//                            showPw(2);
//                        }
////						if (position == 3) {
////							showPw(0);
////						}
////                        else if(position==4){
////                           showPw(4);
////                        }
////                        else
////							showPw(2);
//					}
//				} else if (position == Bimp.tempSelectBitmap.size() + 1) {
//                    if(!TextUtils.isEmpty(voiceName)&&!TextUtils.isEmpty(moviePath)){
//                        startActivity(new Intent(CaseReportActivity.this,PreViewMovie.class).putExtra("path",moviePath));
//                    }
//                   else if(TextUtils.isEmpty(voiceName)&&!TextUtils.isEmpty(moviePath)){
//                        hideKeyboard();
//                        showPw(1);
//                    }
//				}
//                else if (position == Bimp.tempSelectBitmap.size() + 2){
//                    hideKeyboard();
//                    showPw(1);
//                }
//                else {
//					Intent intent = new Intent(CaseReportActivity.this,
//							GalleryActivity.class);
//					intent.putExtra("position", "1");
//					intent.putExtra("ID", position);
//					startActivity(intent);
//				}
//			}
//		});
//	}
//
//	private void showPw(int position) {
////		if (position == 0) {
////			pw.hidePic();
////		} else if (position == 1) {
////			pw.hideVoice();
////		}
////        else if(position==4){
////            pw.hidePic();
////            pw.hideVoice();
////        }
////        else
////			pw.showPic();
//        pw.showAll();
//        if(Bimp.tempSelectBitmap.size()==3){
//            pw.hidePic();
//        }
//        if(!TextUtils.isEmpty(voiceName)){
//            pw.hideVoice();
//        }
//        if(!TextUtils.isEmpty(moviePath)){
//            pw.hideVideo();
//        }
//
//		pw.showAtLocation(gv_img, Gravity.BOTTOM, 0, 0);
//	}
//
//	@Override
//	protected void processSuccessResult(String res) {
//
//		super.processSuccessResult(res);
//		if (method.equals("getType")) {
//			caseTypeList = CaseType.parseList(res, "list");
//			childCaseTypeList1 = CaseType.getChildList1(caseTypeList);
//			caseTypeAdapter.setList(childCaseTypeList1);
//
//			editor.putString(Constant.SP_CASE_TYPE,
//					CaseType.getStr(caseTypeList));
//			editor.commit();
//			if (caseWorkerList == null || caseWorkerList.size() == 0) {
//				method = "getWorker";
//				getServer(MyApplication.getInstance().getIP()
//						+ Constant.CASE_USER, null, "get");
//			}
//		}
//		if (method.equals("getWorker")) {
//			editor.putString(Constant.SP_CASE_WORKERS, res);
//			editor.commit();
//			caseWorkerList = CaseWorker.parseList(res, "list");
//			caseWorkerAdapter.setList(caseWorkerList);
//		}
//		if (method.equals("upload")) {
//			report.setIsSuccess("true");
//			report.setUploadTime(CommonTool.getStringDate(new Date(),
//					"yyyy-MM-dd HH:mm:ss"));
//			caseDao.addCase(report);
//			showShortToast("上报成功!");
//			if (attachName != null && !attachName.equals("")) {
//				String[] names = attachName.split(",");
//				for (String name : names) {
//					File file = new File(name);
//					if (file != null && file.exists()) {
//						file.delete();
//					}
//				}
//			}
//			clear();
//		}
//	}
//
//	@Override
//	protected void onResume() {
//
//		super.onResume();
//	}
//
//	private BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			String action = intent.getAction();
//			if (action.equals("action.refreshFriend")) {
//				initData();
//			}
//		}
//	};
//
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		// 接语音
//		if (resultCode == 12) {
//			times = data.getIntExtra("totaltime", 0);
//			if (times > 0) {
//				if (voiceName != null && !voiceName.equals("")) {
//					File file = new File(voiceName);
//					if (file != null && file.exists()) {
//						file.delete();
//					}
//				}
//				voiceName = data.getStringExtra("filePath");
//			}
//		}
//        if (resultCode==88){
//                moviePath = data.getStringExtra("moviePath");
//                movieTimes = data.getIntExtra("movieTimes",1);
//        }
//
//		if (resultCode != Activity.RESULT_OK) {
//			return;
//		} else {
//			if (requestCode == PHOTOHRAPH) {
//
//				Bitmap bm = FileUtils.compressImageFromFile(photoPath);
//				// Bitmap bm = BitmapFactory.decodeFile(photoPath);
//				ImageItem takePhoto = new ImageItem();
//				takePhoto.setBitmap(bm);
//				takePhoto.setType("photo");
//				takePhoto.setImagePath(photoPath);
//				Bimp.tempSelectBitmap.add(takePhoto);
//
//			}
//		}
//
//	}
//
//	@Override
//	public void onClick(View v) {
//
//		switch (v.getId()) {
//            case R.id.tv_get_voide:
//                startActivityForResult(new Intent(this,MovieReccorderActivity.class),88);
//                pw.dismiss();
//                break;
//
//		case R.id.iv_left:
//			finish();
//			break;
//		case R.id.tv_right:
//			Intent iny1 = new Intent(CaseReportActivity.this,
//					RecordingActivitys.class);
//			startActivity(iny1);
//			break;
//		case R.id.addCaseType:
//			Intent in = new Intent(CaseReportActivity.this,
//					CaseReportListActivity.class);
//			startActivityForResult(in, Constant.REQUEST_CODE);
//			break;
//		case R.id.tv_take_pic:
//			pw.dismiss();
//			takePhoto();
//			break;
//		case R.id.tv_get_pic:
//			pw.dismiss();
//			getPhoto();
//			break;
//		case R.id.rl_case_type:
//			if (childCaseTypeList1 == null || childCaseTypeList1.size() == 0) {
//				showShortToast("未获取到数据！");
//				break;
//			}
//			isType = true;
//			caseTypeAdapter.setList(childCaseTypeList1);
//			hideKeyboard();
//			break;
//		case R.id.rl_case_category:
//			if (caseTypeId == null || caseTypeId.equals("")) {
//				showShortToast("请先选择事件类型！");
//				break;
//			}
//			if (childCaseTypeList2 == null || childCaseTypeList2.size() == 0) {
//				showShortToast("未获取到数据！");
//				break;
//			}
//			caseTypeAdapter.setList(childCaseTypeList2);
//			isType = false;
//			hideKeyboard();
//			break;
//		case R.id.rl_case_worker:
//			if (caseWorkerList == null || caseWorkerList.size() == 0) {
//				showShortToast("未获取到数据！");
//				break;
//			}
//			hideKeyboard();
//			break;
//		case R.id.tv_voice:
//			pw.dismiss();
//			Intent iny = new Intent(CaseReportActivity.this,
//					RecordingActivity.class);
//			startActivityForResult(iny, 12);
//			break;
//		case R.id.bt_upload:
//			// 提交
//			if (caseTypeId == null || caseTypeId.equals("")) {
//				toastNotNull("事件类型");
//				break;
//			}
//			if (caseCategoryId == null || caseCategoryId.equals("")) {
//				toastNotNull("事件类别");
//				break;
//			}
//			caseDesc = et_case_desc1.getText().toString();
//			if (caseDesc == null || caseDesc.equals("")) {
//				toastNotNull("事件描述");
//				break;
//			}
//			// if (isUpload) {
//			// if (caseWorkerId == null || caseWorkerId.equals("")) {
//			// toastNotNull("指定上级");
//			// break;
//			// }
//			// }
//			if (Bimp.tempSelectBitmap.size() == 0 && voiceName.equals("")) {
//				showShortToast("请至少选择一个文件");
//				break;
//			}
//
//			hideKeyboard();
//			myLocationDao = new MyLocationDao(this);
//			String uid = MyApplication.getInstance().userId;
//			locationList = myLocationDao.findAll(uid);
//			GpsTool gpsTool = new GpsTool(this);
//			gpsTool.getAddr();
//			caseRemark = et_case_remark.getText().toString();
//			String id = MyApplication.getInstance().getUserId();
//			String random = CommonTool.getRandom();
//			String md5Sign = MD5Tool.Md5(MD5Tool.Md5(Constant.KEY + random)
//					+ id);
//			final Map<String, String> map = new HashMap<String, String>();
//			final Map<String, File> fileMap = new HashMap<String, File>();
//			final String str = "?id=" + id + "&random=" + random
//					+ "&signature=" + md5Sign;
//			String longitude = MyApplication.getInstance().longitude + "";
//			String latitude = MyApplication.getInstance().latitude + "";
//			String alti = MyApplication.getInstance().altitude + "";
//			String address = MyApplication.getInstance().address;
//			String speed = CommonTool
//					.formatFloat(MyApplication.getInstance().speed);
//			String bearing = CommonTool
//					.formatFloat(MyApplication.getInstance().bearing);
//			String accurary = CommonTool.formatFloat(MyApplication
//					.getInstance().accurary);
//			String time = MyApplication.getInstance().time + "";
//			String uLocType = MyApplication.getInstance().uLocType + "";
//			// zh
//			if (longitude == null || longitude.equals("0.0")) {
//				if (!locationList.isEmpty() && locationList != null) {
//					uLocType = locationList.get(locationList.size() - 1)
//							.getLocType();
//					latitude = locationList.get(locationList.size() - 1)
//							.getLatitude();
//					longitude = locationList.get(locationList.size() - 1)
//							.getLongitude();
//				} else {
//					latitude = "0.0";
//					longitude = "0.0";
//				}
//			}
//
//			// if (String.valueOf(latitude).length() < 9
//			// && String.valueOf(longitude).length() < 10) {
//			// showShortToast("位置精确度不够，请重试!");
//			// break;
//			// }
//
//			if (time == null || time.equals("") || time.equals("null"))
//				time = CommonTool.getStringDate(new Date(),
//						"yyyy-MM-dd HH:mm:ss");
//			map.put("uType", caseCategoryId);
//			map.put("uContent", caseDesc);
//			map.put("q", (!isUpload) + "");
//			map.put("uRemark", caseRemark);
//			map.put("uTime", time);
//			if (isUpload)
//				map.put("uUserId", caseWorkerId);
//			map.put("uLoti", longitude);
//			map.put("uLati", latitude);
//			map.put("uAlti", alti);
//			map.put("uAddr", address);
//			map.put("uSpeed", speed);
//			map.put("uDirection", bearing);
//			map.put("uAccuracy", accurary);
//			map.put("uLocType", uLocType);
//			attachName = "";
//			if (Bimp.tempSelectBitmap.size() >= 1) {
//				for (int i = 0; i < Bimp.tempSelectBitmap.size(); i++) {
//					ImageItem imageItem = Bimp.tempSelectBitmap.get(i);
//					String fileName = FileUtils.getImgFilePath()
//							+ String.valueOf(System.currentTimeMillis())
//							+ ".jpg";
//					FileUtils.saveBitmap(imageItem.getBitmap(), fileName);
//					attachName += fileName + ",";
//				}
//			}
//			if (voiceName != null && !voiceName.equals(""))
//				attachName += voiceName + ",";
//			report = new CaseReport();
//			report.setuType(caseCategoryId);
//			report.setUid(id);
//			report.setuContent(caseDesc);
//			report.setQ((!isUpload) + "");
//			report.setuTime(time);
//			report.setuLati(latitude);
//			report.setuLoti(longitude);
//			report.setuAlti(alti);
//			report.setuAddr(address);
//			report.setuSpeed(speed);
//			report.setuDirection(bearing);
//			report.setuAccurary(accurary);
//			report.setuRemark(caseRemark);
//			report.setAttach(attachName);
//			report.setuUserId(caseWorkerId);
//			report.setuLocType(uLocType);
//			report.setFlag("0");
//			try {
//				if (attachName != null && !attachName.equals("")) {
//					String[] names = attachName.split(",");
//					for (String name : names) {
//						File file = new File(name);
//						if (file != null && file.exists()) {
//							fileMap.put(name, file);
//						}
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			dialogFlag = true;
//			showProgressDialog("upload", "");
//			new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					try {
//						method = "upload";
//						String res = HttpTool.post(MyApplication.getInstance()
//								.getIP() + Constant.CASE_REPORT + str, map,
//								fileMap, "attach");
//						if (res != null && !res.equals("")) {
//							Message msg = handler.obtainMessage(DATA_SUCCESS);
//							msg.obj = res;
//							msg.sendToTarget();
//						} else {
//							Message msg = handler.obtainMessage(DATA_FAIL);
//							msg.sendToTarget();
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}).start();
//			break;
//		}
//	}
//
//	// 上传照片
//	private void takePhoto() {
//		if (Bimp.tempSelectBitmap.size() == 3) {
//			showShortToast("最多只能选择三张图片");
//		} else {
//			photoPath = FileUtils.getImgFilePath() + UUID.randomUUID() + ".jpg";
//			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//			intent.putExtra(MediaStore.EXTRA_OUTPUT,
//					Uri.fromFile(new File(photoPath)));
//			startActivityForResult(intent, PHOTOHRAPH);// 采用ForResult打开
//		}
//	}
//
//	private void getPhoto() {
//		if (Bimp.tempSelectBitmap.size() == 3) {
//			showShortToast("最多只能选择三张图片");
//		} else {
//			Intent photoIntent = new Intent(CaseReportActivity.this,
//					AlbumActivity.class);
//			startActivity(photoIntent);
//		}
//	}
//
//	public void startPlaying(String path ) {
//		if (voiceName != null && !"".equals(voiceName)) {
//			try {
//				mPlayer = new MediaPlayer();
//				mPlayer.setDataSource(path);
//				mPlayer.prepare();
//				mPlayer.start();
//				mPlayer.setOnCompletionListener(new OnCompletionListener() {
//
//					@Override
//					public void onCompletion(MediaPlayer arg0) {
//						isPlaying = false;
//						mPlayer.release();
//						mPlayer = null;
//					}
//
//				});
//			} catch (IOException e) {
//			}
//		}
//	}
//
//	public void stopPlaying() {
//		if (mPlayer != null) {
//			mPlayer.stop();
//			mPlayer.release();
//			mPlayer = null;
//		}
//	}
//
//	private void clear() {
//		et_case_desc1.setVisibility(View.GONE);
//		tv_case_type.setText("");
//		tv_case_category.setText("");
//		tv_case_worker.setText("");
//		et_case_desc1.setText("");
//		et_case_remark.setText("");
//		// rb_case_deal.setChecked(true);
//		attachName = "";
//		caseTypeId = "";
//		caseCategoryId = "";
//		caseWorkerId = "";
//		voiceName = "";
//		times = 0;
//		childCaseTypeList2.clear();
//		caseTypeAdapter2.setList(childCaseTypeList2);
//		caseTypeAdapter.setSelected(-1);
//		caseTypeAdapter2.setSelected(-1);
//		caseWorkerAdapter.setSelected(-1);
//		Bimp.tempSelectBitmap.clear();
//		Bimp.max = 0;
//		imgAdapter.notifyDataSetChanged();
//	}
//
//	class UpdateTypeReceiver extends BroadcastReceiver {
//
//		@Override
//		public void onReceive(Context arg0, Intent intent) {
//
//			if (intent.getAction().equals(Constant.BROADCAST_UPDATE_TYPE)) {
//				caseTypeStr = preferences.getString(Constant.SP_CASE_TYPE, "");
//				if (caseTypeStr != null && !caseTypeStr.equals("")) {
//					caseTypeList = CaseType.parseList(caseTypeStr, "list");
//					childCaseTypeList1 = CaseType.getChildList1(caseTypeList);
//				}
//				caseWorkerStr = preferences.getString(Constant.SP_CASE_WORKERS,
//						"");
//				if (caseWorkerStr != null && !caseWorkerStr.equals("")) {
//					List<CaseWorker> workerList = CaseWorker.parseList(
//							caseWorkerStr, "list");
//					caseWorkerList.clear();
//					caseWorkerList.addAll(workerList);
//				}
//				caseTypeAdapter.notifyDataSetChanged();
//				caseWorkerAdapter.notifyDataSetChanged();
//			}
//		}
//	}
//
//	@Override
//	protected void onRestart() {
//
//		super.onRestart();
//		imgAdapter.update();
//		// imgAdapter.notifyDataSetChanged();
//	}
//
//	public class GridAdapter extends BaseAdapter {
//		private LayoutInflater inflater;
//		private int selectedPosition = -1;
//		private boolean shape;
//
//		public boolean isShape() {
//			return shape;
//		}
//
//		public void setShape(boolean shape) {
//			this.shape = shape;
//		}
//
//		public GridAdapter(Context context) {
//			inflater = LayoutInflater.from(context);
//		}
//
//		public void update() {
//			loading();
//		}
//
//		@Override
//		public int getCount() {
//            if(!TextUtils.isEmpty(voiceName)&&!TextUtils.isEmpty(moviePath)){
//                return (Bimp.tempSelectBitmap.size() + 3);
//            }
//			    else if (!TextUtils.isEmpty(voiceName)||!TextUtils.isEmpty(moviePath)) {
//                    return (Bimp.tempSelectBitmap.size() + 2);}
//            else
//				return (Bimp.tempSelectBitmap.size() + 1);
//		}
//
//		@Override
//		public Object getItem(int arg0) {
//			return null;
//		}
//
//		@Override
//		public long getItemId(int arg0) {
//			return 0;
//		}
//
//		public void setSelectedPosition(int position) {
//			selectedPosition = position;
//		}
//
//		public int getSelectedPosition() {
//			return selectedPosition;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			ViewHolder holder = null;
//			if (convertView == null) {
//				convertView = inflater.inflate(R.layout.item_published_grida,
//						parent, false);
//				holder = new ViewHolder();
//				holder.image = (ImageView) convertView
//						.findViewById(R.id.item_grida_image);
//				holder.delete = (ImageView) convertView
//						.findViewById(R.id.iv_delete);
//				holder.tv_time = (TextView) convertView
//						.findViewById(R.id.tv_time);
//				convertView.setTag(holder);
//			} else {
//				holder = (ViewHolder) convertView.getTag();
//			}
//			if (position == Bimp.tempSelectBitmap.size()) {
//				if (voiceName != null && !voiceName.equals("")) {
//					holder.image.setImageResource(R.drawable.voice_default);
//					holder.delete.setVisibility(View.VISIBLE);
//					holder.tv_time.setText(times + "s");
//					holder.tv_time.setVisibility(View.VISIBLE);
//				} else if(!TextUtils.isEmpty(moviePath)) {
//                    holder.image.setImageResource(R.drawable.voice_default);
//                    holder.delete.setVisibility(View.VISIBLE);
//                    holder.tv_time.setText(movieTimes + "s");
//                    holder.tv_time.setVisibility(View.VISIBLE);
//                }
//                    else{
//                        holder.image.setImageResource(R.drawable.camera_default);
//                        holder.delete.setVisibility(View.GONE);
//                        holder.tv_time.setVisibility(View.GONE);
//                    }
//
//			} else if (position == Bimp.tempSelectBitmap.size() + 1) {
//                //有录音 且有摄像时候
//                if(!TextUtils.isEmpty(moviePath) && !TextUtils.isEmpty(voiceName)){
//                    holder.image.setImageResource(R.drawable.voice_default);
//                    holder.delete.setVisibility(View.VISIBLE);
//                    holder.tv_time.setText(movieTimes + "s");
//                    holder.tv_time.setVisibility(View.VISIBLE);
//                }else {
//                    holder.image.setImageResource(R.drawable.camera_default);
//                    holder.delete.setVisibility(View.GONE);
//                    holder.tv_time.setVisibility(View.GONE);
//                }
//			}
//            else if(position ==  Bimp.tempSelectBitmap.size() + 2){
//                holder.image.setImageResource(R.drawable.camera_default);
//                holder.delete.setVisibility(View.GONE);
//                holder.tv_time.setVisibility(View.GONE);
//            }
//			 else {
//				holder.tv_time.setVisibility(View.GONE);
//				holder.delete.setVisibility(View.VISIBLE);
//				// holder.image.setImageBitmap(imgList.get(position));
//				holder.image.setImageBitmap(Bimp.tempSelectBitmap.get(position)
//						.getBitmap());
//			}
//
//
//			final int index = position;
//			holder.delete.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View arg0) {
//
//					if (index == Bimp.tempSelectBitmap.size()|| index ==Bimp.tempSelectBitmap.size()+1 ) {
//                        moviePath = "";
//                        movieTimes = 0;
//						voiceName = "";
//						times = 0;
//						notifyDataSetChanged();
//					} else {
//						Bimp.tempSelectBitmap.remove(index);
//						Bimp.max--;
//						notifyDataSetChanged();
//					}
//				}
//			});
//			return convertView;
//		}
//
//		public class ViewHolder {
//			public ImageView image, delete;
//			public TextView tv_time;
//		}
//
//		public void loading() {
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					while (true) {
//						if (Bimp.max == Bimp.tempSelectBitmap.size()) {
//							Message message = new Message();
//							message.what = DATA_IMG;
//							handler.sendMessage(message);
//							break;
//						} else {
//							Bimp.max += 1;
//							Message message = new Message();
//							message.what = DATA_IMG;
//							handler.sendMessage(message);
//						}
//					}
//				}
//			}).start();
//		}
//	}
//
//}
