package com.weisen.xcxf.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Selection;
import android.text.Spannable;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.jpush.android.api.JPushInterface;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.NaviParaOption;
import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.adapter.ReplyAdapter;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.MessageReply;
import com.weisen.xcxf.bean.MessageReplyDao;
import com.weisen.xcxf.bean.Notice;
import com.weisen.xcxf.bean.NoticeDao;
import com.weisen.xcxf.tool.CommonTool;
import com.weisen.xcxf.tool.NotScollListView;

public class MessageDetailActivity extends BaseActivity implements
		OnClickListener {
	private LayoutInflater inflater;
	private String id, nid, title, content, flag, url,time,latitude,longitude,userName,userIphone,userDescription;
	private WebView wv_message;
	private LinearLayout ll_message,linearLayout1,ly_messageReport;
	private TextView tv_message_title, tv_message_content,notice_map,notice_time,tv_userName,tv_userPhone;
	private Notice notice;

	private NotScollListView message_reply_list;
	private EditText message_ed_txt;
	private Button send_reply;
	private String sendTxt,replyContent;
	private ImageView add_reply_img;
	private ReplyAdapter adapter;
	private List<MessageReply> replyList;
	private MessageReplyDao replyDao;

	@Override
	protected void initView() {

		setContentView(R.layout.activity_message_detail);
//		initPush();
		inflater = LayoutInflater.from(this);
		ll_message = (LinearLayout) findViewById(R.id.ll_message);
		wv_message = (WebView) findViewById(R.id.wv_message);
		tv_message_title = (TextView) findViewById(R.id.tv_message_title);
		tv_message_content = (TextView) findViewById(R.id.tv_message_content);
		notice_time = (TextView) findViewById(R.id.notice_time);
		notice_map = (TextView) findViewById(R.id.notice_map);
		tv_userName = (TextView) findViewById(R.id.tv_userName);
		tv_userPhone = (TextView) findViewById(R.id.tv_userPhone);
		notice_map.setVisibility(View.GONE);
		initWebView();
		initTitle();
		iv_left.setVisibility(View.VISIBLE);
		tv_title.setText(getStringResource(R.string.title_message));

		linearLayout1 = (LinearLayout) findViewById(R.id.linearLayout1);
		ly_messageReport = (LinearLayout) findViewById(R.id.ly_messageReport);
		ly_messageReport.setVisibility(View.INVISIBLE);
		message_reply_list = (NotScollListView) findViewById(R.id.message_reply_list);
		message_ed_txt = (EditText) findViewById(R.id.message_ed_txt);
		message_ed_txt.setText("收到，立即前往！");
		// 控制光标位置
		CharSequence text = message_ed_txt.getText();
		if (text instanceof Spannable) {
			Spannable spanText = (Spannable) text;
			Selection.setSelection(spanText, text.length());
		}
		add_reply_img = (ImageView) findViewById(R.id.add_reply_img);
		send_reply = (Button) findViewById(R.id.send_reply);
		hindInput();
	}

	@Override
	protected void initData() {
		NoticeDao noticeDao = new NoticeDao(this);
		id = format(getIntent().getStringExtra("id"));
		nid = format(getIntent().getStringExtra("nid"));
		title = format(getIntent().getStringExtra("title"));
		content = format(getIntent().getStringExtra("content"));
		flag = format(getIntent().getStringExtra("flag"));
		url = format(getIntent().getStringExtra("url"));
		time = format(getIntent().getStringExtra("time"));
		latitude = format(getIntent().getStringExtra("latitude"));
		longitude = format(getIntent().getStringExtra("longitude"));
		userName = format(getIntent().getStringExtra("userIname"));
		userIphone = format(getIntent().getStringExtra("userIphone"));
		userDescription = format(getIntent().getStringExtra("userDescription"));

		if (!"".equals(id)) {
			notice = noticeDao.getById(id);
			if (notice != null) {
				nid = notice.getNid();
				title = notice.getTitle();
				content = notice.getContent();
				flag = notice.getFlag();
				url = notice.getUrl();
				time = notice.getTime();
				latitude = notice.getLatitude();
				longitude = notice.getLongitude();
				userName = notice.getUserIname();
				userIphone = notice.getUserPhone();
				userDescription = notice.getUserDescription();

			}
		}
		if (flag.equals("2")) {//url富文本
			linearLayout1.setVisibility(View.GONE);

			ll_message.setVisibility(View.GONE);
			wv_message.setVisibility(View.VISIBLE);
			if (!url.equals(""))

				if(url.contains("?"))
				{
					url+="&curId="+MyApplication.getInstance().getUserId();
				}
			else {
					url+="?curId="+MyApplication.getInstance().getUserId();
				}
				wv_message.loadUrl(url);



		} else if (flag.equals("1")) {//纯文本
			hindInput();
			wv_message.setVisibility(View.GONE);
			ll_message.setVisibility(View.VISIBLE);
			ly_messageReport.setVisibility(View.GONE);
			linearLayout1.setVisibility(View.GONE);
			tv_message_title.setText(title);
			tv_userName.setText(userName);
			tv_userPhone.setText(userIphone);
			tv_message_content.setText(content);
			Linkify.addLinks(tv_userPhone, Linkify.PHONE_NUMBERS);
			notice_time.setText(CommonTool.getStringDate(time, "yyyy-MM-dd HH:mm:ss"));
		}else if(flag.equals("3")){//经纬度
			addDatas();
			linearLayout1.setVisibility(View.VISIBLE);
			ly_messageReport.setVisibility(View.VISIBLE);
			tv_message_title.setText(title);
			tv_message_content.setText(userDescription);
			tv_userName.setText(userName);
			tv_userPhone.setText(userIphone);
			Linkify.addLinks(tv_userPhone, Linkify.PHONE_NUMBERS);
			notice_time.setText(CommonTool.getStringDate(time, "yyyy-MM-dd HH:mm:ss"));
			notice_map.setVisibility(View.VISIBLE);
			notice_map.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
//					Intent intent = new Intent(MessageDetailActivity.this,GotoLocationActivity.class);
//					intent.putExtra("latitude", latitude);
//					intent.putExtra("longitude", longitude);
//					startActivity(intent);

					LatLng startPoint=new LatLng(MyApplication.getInstance().latitude,MyApplication.getInstance().longitude);
					LatLng endPoint=new LatLng(Double.parseDouble(notice.getLatitude()),Double.parseDouble(notice.getLongitude()));
					Log.d(Double.toString(MyApplication.getInstance().latitude),Double.toString(MyApplication.getInstance().longitude));
					Log.d(notice.getLatitude(),notice.getLongitude());
					// 构建 导航参数
					NaviParaOption para = new NaviParaOption();

					para.startPoint(startPoint);

					para.startName("从这里开始");
					para.endPoint(endPoint);
					para.endName("到这里结束");

					try {

						BaiduMapNavigation.openBaiduMapNavi(para,MessageDetailActivity.this);

					} catch (BaiduMapAppNotSupportNaviException e) {
						e.printStackTrace();
						AlertDialog.Builder builder = new AlertDialog.Builder(MessageDetailActivity.this);
						builder.setMessage("您尚未安装百度地图app或app版本过低，请下载或更新后重试");
						builder.setTitle("提示");
						builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();

							}

						});

//					builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							dialog.dismiss();
//						}
//					});

						builder.create().show();
					};

				}

			});
		}
		dialogFlag = false;
		Map<String,String> map = new HashMap<String,String>();
		map.put("q", nid);
		getServer(MyApplication.getInstance().getIP()+Constant.DEAL_MSG, map, "upload");
	}

	private String format(String str) {
		if (str == null || str.equals("") || str.equals("null"))
			return "";
		else
			return str;
	}

	@Override
	protected void initEvent() {

		iv_left.setOnClickListener(this);
		send_reply.setOnClickListener(this);
		add_reply_img.setOnClickListener(this);
	}

	private void initWebView() {
		wv_message.getSettings().setAllowFileAccess(true);// 设置允许访问文件数据
		wv_message.getSettings().setBuiltInZoomControls(true);// 设置支持缩放
		wv_message.getSettings().setSavePassword(false); // 设置是否保存密码

		wv_message.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

		// 设置支持JavaScript脚本
		wv_message.getSettings().setJavaScriptEnabled(true);
		// 设置支持各种不同的设备

		wv_message.getSettings()
				.setUserAgentString(
						"Mozilla/5.0 (Linux; U; Android 4.2.2; zh-cn;) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/534.30");
		wv_message.setWebViewClient(new myWebViewClient());
		wv_message.setWebChromeClient(new MyWebChromeClient());


	}

	class myWebViewClient extends WebViewClient {
		// 新开页面时用自己定义的webview来显示，不用系统自带的浏览器来显示
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			view.loadUrl(url);
			return false;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			hideKeyboard();
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			hideKeyboard();

		}
	}

	class MyWebChromeClient extends WebChromeClient {
		// 设置网页加载的进度条
		public void onProgressChanged(WebView view, int newProgress) {
			getWindow().setFeatureInt(Window.FEATURE_PROGRESS,
					newProgress * 100);
			super.onProgressChanged(view, newProgress);
		}
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.iv_left:
			finish();
			break;
		case R.id.add_reply_img:
			showDialog();
			break;
		case R.id.send_reply:
			hindInput();
			sendTxt = message_ed_txt.getText().toString();
			message_ed_txt.setText("");
			replyMessage(sendTxt);
			break;
		}
	}

	private void replyMessage(String sendTxt) {
		if (sendTxt.equals("")) {
			showShortToast("回复内容不能为空！");
			return;
		}else{
			replyList = new ArrayList<MessageReply>();
			String time = CommonTool.getStringDate(new Date(),"yyyy-MM-dd HH:mm:ss");
			MessageReply reply = new MessageReply();
			reply.setUid(nid);
			reply.setReplyTxt(sendTxt);
			reply.setReplyTime(time);
			replyList.add(reply);
			replyDao = new MessageReplyDao(MessageDetailActivity.this);
			replyDao.addReply(reply);
			addDatas();
			adapter.notifyDataSetChanged();

			Map<String, String> map = new HashMap<String, String>();
			map.put("q", nid);
			map.put("searchValue", sendTxt);
			method = "reply";
			getServer(MyApplication.getInstance().getIP() + Constant.SEND_MESSAGE,map, "upload");
			showShortToast("回复成功!");
		}
	}
	private void addDatas() {
		replyDao = new MessageReplyDao(MessageDetailActivity.this);
		replyList = replyDao.findAll(nid);
		if (replyList.isEmpty()) {
			ly_messageReport.setVisibility(View.GONE);
			return;
		}else{
			adapter = new ReplyAdapter(inflater,replyList);
			message_reply_list.setAdapter(adapter);
		}
	}

	private void showDialog() {
		 AlertDialog.Builder builder = new AlertDialog.Builder(MessageDetailActivity.this);
		 builder.setTitle("请选择一条回复");
		 final String[] cities = {"收到，马上去。", "事件未处理完","事件已处理成功","事件需请示上级"};
		builder.setItems(cities, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				replyContent = cities[which];
				replyMessage(replyContent);
				showShortToast("回复成功!");
				dialog.dismiss();
			}
		});
		builder.show();
	}

	@Override
	protected void processSuccessResult(String res){
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
			if (msg.equals("反馈成功！")) {
				showShortToast("回复成功!");
			}

		}
	}

	private void hindInput() {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(MessageDetailActivity.this.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
	}
	protected void initPush() {
		// 初始化 JPush。如果已经初始化，但没有登录成功，则执行重新登录。
		JPushInterface.init(getApplicationContext());
	}
}
