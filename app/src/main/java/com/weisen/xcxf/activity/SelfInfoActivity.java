package com.weisen.xcxf.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.tool.CommonTool;
import com.weisen.xcxf.tool.FileUtils;
import com.weisen.xcxf.tool.HttpTool;
import com.weisen.xcxf.tool.ImageTools;
import com.weisen.xcxf.tool.MD5Tool;
import com.weisen.xcxf.tool.UnIntent;
import com.weisen.xcxf.widget.CircleImageView;
import com.weisen.xcxf.widget.MyPop;

public class SelfInfoActivity extends BaseActivity implements OnClickListener {

	private LinearLayout ll_address;
	private RadioGroup group_self_info;
	private RadioButton rb_man, rb_woman;
	private EditText et_name, et_phone, et_email, et_nation, et_sign;
	private TextView tv_addr,name2;
	private TextView tv_upload;
	private boolean isUpdate = false, isNoUpdate = false;
	private String name, sex, phone, email, sign, img, nation, addr, loti,
			lati, img_path;
	private MyPop pw;
	Map<String, File> fileMap = new HashMap<String, File>();
	private static final int DATA_FAIL = 1;
	
	private SharedPreferences preferences;
	private Editor editor;
    private CircleImageView userIcon;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
			switch (msg.what) {
			case DATA_SUCCESS:
				showShortToast("上传成功");
				String res = (String) msg.obj;
				tv_upload.setText("上传图片");
				userIcon.setClickable(true);
				try {
					JSONObject object = new JSONObject(res);
					img = CommonTool.getJsonString(object, "iconUrl");
					MyApplication.getInstance().setHeadPic(img);
                    Intent intent = new Intent(SelfInfoActivity.this,SelfCenterActivity.class);
                    intent.putExtra("name", name);
                    name2.setVisibility(View.GONE);
                    intent.putExtra("headPic", img);
                    setResult(100, intent);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case DATA_FAIL:
				showShortToast("上传失败!");
				tv_upload.setText("上传图片");
				userIcon.setClickable(true);
				break;
			}
		}
	};
    private final static Pattern emailer = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
    public static boolean isEmail(String email) {
        if (email == null || email.trim().length() == 0)
            return false;
        return emailer.matcher(email).matches();
    }

	@Override
	protected void initView() {
		
		super.initView();
		setContentView(R.layout.activity_modifyinfo);
		ll_address = (LinearLayout) findViewById(R.id.ll_address);
        userIcon = (CircleImageView) findViewById(R.id.iv_self_info);
        name2 = (TextView) findViewById(R.id.tv_name2);
        tv_upload = (TextView) findViewById(R.id.tv_upload);
		et_phone = (EditText) findViewById(R.id.et_phone);
		et_name = (EditText) findViewById(R.id.et_name);
		et_email = (EditText) findViewById(R.id.et_email);
		et_nation = (EditText) findViewById(R.id.et_nation);
		et_sign = (EditText) findViewById(R.id.et_sign);
		tv_addr = (TextView) findViewById(R.id.tv_addr);
		group_self_info = (RadioGroup) findViewById(R.id.gp_self_info);
		rb_man = (RadioButton) group_self_info.findViewById(R.id.rb_man);
		rb_woman = (RadioButton) group_self_info.findViewById(R.id.rb_woman);
		initTitle();
		iv_left.setVisibility(View.VISIBLE);
		tv_title.setText(getString(R.string.title_selfinfo));
		tv_right.setVisibility(View.VISIBLE);
		pw = new MyPop(SelfInfoActivity.this, this,false);
		hideKeyboard();
		preferences = getSharedPreferences(Constant.APP_SP, MODE_PRIVATE);
		editor = preferences.edit();
        nameStr = preferences.getString(Constant.SP_USERINFO_NAME, "");
	}
    String  nameStr;
	@Override
	protected void initData() {
		
		super.initData();
		if (UnIntent.isNetworkAvailable(SelfInfoActivity.this)) {
			method = "get";
			getServer(MyApplication.getInstance().getIP() + Constant.USER_INFO,null, "get");
			
		}else{
			et_name.setText(preferences.getString(Constant.SP_USERINFO_NAME, ""));
			et_phone.setText(preferences.getString(Constant.SP_USERINFO_PHONE, ""));
			et_email.setText(preferences.getString(Constant.SP_USERINFO_EMAIL, ""));
			et_nation.setText(preferences.getString(Constant.SP_USERINFO_NATION, ""));
			tv_addr.setText(MyApplication.getInstance().address);
			et_sign.setText(preferences.getString(Constant.SP_USERINFO_SIGN, ""));
			String sex1 = preferences.getString(Constant.SP_USERINFO_SEX,"");
			String img1 =  MyApplication.getInstance().getHeadPic();
			if (sex1 != null && sex1.equals("男")) {
				rb_man.setChecked(true);
			}
			if (sex1 != null && sex1.equals("女")) {
				rb_woman.setChecked(true);
			}

            String[] heads = img1.split("/");
            System.out.println(heads[heads.length - 1].substring(0, 7));
            System.out.println("头像路径"+img1);
            System.out.println("长度"+nameStr.length());
            if (img1 != null && !img1.equals("") && !heads[heads.length-1].substring(0,7).equals("default")){

                ImageLoader.getInstance().displayImage(img1, userIcon,MyApplication.getInstance().options);
            }else{
                userIcon.setBackgroundResource(R.drawable.user_default);
                if(nameStr.length()>2){
                    name2.setText(nameStr.substring(nameStr.length()-2, nameStr.length()));
                }else{
                    name2.setText(nameStr);
                }
            }
		}
	}

	@Override
	protected void initEvent() {
		
		super.initEvent();
		ll_address.setOnClickListener(this);
		iv_left.setOnClickListener(this);
		tv_right.setOnClickListener(this);
		userIcon.setOnClickListener(this);
		tv_upload.setOnClickListener(this);
		group_self_info
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup arg0, int arg1) {
						
						if (rb_man.isChecked()) {
							rb_woman.setChecked(false);
							sex = "男";
						} else if (rb_woman.isChecked()) {
							rb_man.setChecked(false);
							sex = "女";
						}
					}
				});
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.iv_left:
			finish();
			break;
		case R.id.ll_address:
//			Intent addrIntent = new Intent(SelfInfoActivity.this,TrailActivity.class);
//			addrIntent.putExtra("type", "getAddr");
//			startActivityForResult(addrIntent, 4);
            tv_addr.setText(MyApplication.getInstance().address);
			break;
		case R.id.tv_right:
			name = et_name.getText().toString().trim();
			phone = et_phone.getText().toString().trim();
			email = et_email.getText().toString().trim();
			nation = et_nation.getText().toString().trim();
			addr = tv_addr.getText().toString().trim();
			sign = et_sign.getText().toString().trim();
			if (name == null || name.equals("")) {
				toastNotNull("姓名");
			}
            if(!TextUtils.isEmpty(email)){
                if(!isEmail(email)){
                    showShortToast("请输入正确邮箱地址!");
                    return;
                }
            }
            if(!isMobileNO(phone)&&!TextUtils.isEmpty(phone)){
                showShortToast("请输入正确手机号码!");
                return;
            }
			method = "upload";
			Map<String, String> map = new HashMap<String, String>();
			map.put("uName", name);
			map.put("q", sex);
			map.put("uIphone", phone);
			map.put("uEmail", email);
			map.put("uNation", nation);
			map.put("uAddr", addr);
			map.put("uLoti", loti);
			map.put("uLati", lati);
			map.put("uRemark", sign + "");
			getServer(MyApplication.getInstance().getIP() + Constant.UPDATE_USER_INFO, map, "upload");
			break;
		case R.id.tv_upload:
			hideKeyboard();
			pw.showAtLocation(userIcon, Gravity.BOTTOM, 0, 0);
			break;
		case R.id.iv_self_info:
			hideKeyboard();
			pw.showAtLocation(userIcon, Gravity.BOTTOM, 0, 0);
			break;
		case R.id.tv_take_pic:
			pw.dismiss();
			takePhoto();
			break;
		case R.id.tv_get_pic:
			pw.dismiss();
			getPhoto();
			break;
		}
	}

	private void getPhoto() {
       Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		/* 取得相片后返回本画面 */
		startActivityForResult(intent, 1);
	}

	/* 拍照 */
	private void takePhoto() {
		img_path = FileUtils.getImgFilePath() + "head.jpg";
		Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent2.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(new File(img_path)));
		startActivityForResult(intent2, 2);// 采用ForResult打开
	}

	@Override
	@SuppressLint("SdCardPath")
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageTools imageTools = new ImageTools(this);
        imageTools.onActivityResult(requestCode, resultCode, data, new ImageTools.OnBitmapCreateListener() {
            @Override
            public void onBitmapCreate(Bitmap bitmap, String path) {

            }
        });
		switch (requestCode) {
		case 1:
			if (resultCode == RESULT_OK) {
				Uri imgUri = data.getData();
				String[] proj = { MediaColumns.DATA };

				// 好像是android多媒体数据库的封装接口，具体的看Android文档
				Cursor cursor = managedQuery(imgUri, proj, null, null, null);
                if(cursor==null){
                    showShortToast("请选择本地相片!");
                    return ;
                }
				// 按我个人理解 这个是获得用户选择的图片的索引值
				int column_index = cursor
						.getColumnIndexOrThrow(MediaColumns.DATA);
				// 将光标移至开头 ，这个很重要，不小心很容易引起越界

				cursor.moveToFirst();
				// 最后根据索引值获取图片路径
				img_path = cursor.getString(column_index);
				if (img_path.equals("") || img_path == null) {
					showShortToast("还没有选择图片");
					return;
				}
				startPhotoZoom(imgUri, 150);
				// cropPhoto(img_path);
			}
			break;
		case 2:
			if (resultCode == RESULT_OK) {
				try {
					MediaStore.Images.Media.insertImage(getContentResolver(),
							img_path, "head", null);
					File imageFile = new File(img_path);
					startPhotoZoom(Uri.fromFile(imageFile), 150);
					// cropPhoto(img_path);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			break;
		case 3:
			if (data != null) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					Bitmap photo = extras.getParcelable("data");
					userIcon.setImageBitmap(photo);
					try {
						img_path = FileUtils.getRootFilePath() + "head.jpg";
						File file = new File(img_path);
						if (file.exists()) {
							file.delete();
							file.exists();
						} else {
							file.exists();
						}
						FileOutputStream fOut = null;
						try {
							fOut = new FileOutputStream(file);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						photo.compress(Bitmap.CompressFormat.PNG, 100, fOut);
						try {
							fOut.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
						try {
							fOut.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						try {
							uploadFile(img_path, MyApplication.getInstance()
									.getIP() + Constant.UPDATE_USER_HEAD);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			break;
		case 4:
			if (data != null) {
				addr = data.getStringExtra("addr");
				loti = data.getStringExtra("loti");
				lati = data.getStringExtra("lati");
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/* 剪切图片150*150 */
	private void startPhotoZoom(Uri uri, int size) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// crop为true是设置在开启的intent中设置显示的view可以剪裁
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX,outputY 是剪裁图片的宽高
		intent.putExtra("outputX", size);
		intent.putExtra("outputY", size);
		intent.putExtra("return-data", true);

		startActivityForResult(intent, 3);
	}

	public void uploadFile(String path, String url) {

		File file = new File(path);
		if (file.exists() && file.length() > 0) {
			tv_upload.setText("上传中..");
			userIcon.setClickable(false);

			fileMap.put("icon", file);
			String id = MyApplication.getInstance().getUserId();
			String random = CommonTool.getRandom();
			String md5Sign = MD5Tool.Md5(MD5Tool.Md5(Constant.KEY + random) + id);
			final String str = "?id=" + id + "&random=" + random + "&signature=" + md5Sign;

			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						method = "upload";
						String res = HttpTool.post(MyApplication.getInstance()
								.getIP() + Constant.UPDATE_USER_HEAD + str,
								null, fileMap, "icon");
						if (res != null && !res.equals("")) {
							Message msg = handler.obtainMessage(DATA_SUCCESS);
							msg.obj = res;
							msg.sendToTarget();
						} else {
							Message msg = handler.obtainMessage(DATA_FAIL);
							msg.sendToTarget();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		} else {
			showShortToast("文件不存在");
		}

	}

	@Override
	protected void processSuccessResult(String res) {
		super.processSuccessResult(res);
		if (method.equals("get")) {
			JSONObject object = CommonTool.parseFromJson(res);
			name = CommonTool.getJsonString(object, "name");
			sex = CommonTool.getJsonString(object, "sex");
			//img = CommonTool.getJsonString(object, "icon");
            img = MyApplication.getInstance().getHeadPic();
			phone = CommonTool.getJsonString(object, "iphone");
			email = CommonTool.getJsonString(object, "email");
			nation = CommonTool.getJsonString(object, "nation");
			addr = CommonTool.getJsonString(object, "addr");
			sign = CommonTool.getJsonString(object, "remark");
			loti = CommonTool.getJsonString(object, "loti");
			lati = CommonTool.getJsonString(object, "lati");
			editor.putString(Constant.SP_USERINFO_NAME, name);
			editor.putString(Constant.SP_USERINFO_SEX, sex);
			editor.putString(Constant.SP_USERINFO_PHONE, phone);
			editor.putString(Constant.SP_USERINFO_EMAIL, email);
			editor.putString(Constant.SP_USERINFO_NATION, nation);
			editor.putString(Constant.SP_USERINFO_SIGN, sign);
			editor.putString(Constant.SP_USERINFO_IMG, img);
			editor.commit();
			if (addr == null || addr.equals(""))
				addr = MyApplication.getInstance().address;
			if (loti == null || loti.equals(""))
				loti = MyApplication.getInstance().longitude + "";
			if (lati == null || lati.equals(""))
				lati = MyApplication.getInstance().latitude + "";
			
			et_name.setText(name);
			et_phone.setText(phone);
			et_email.setText(email);
			et_nation.setText(nation);
			tv_addr.setText(addr);
			et_sign.setText(sign);
			if (sex != null && sex.equals("男")) {
				rb_man.setChecked(true);
			}
			if (sex != null && sex.equals("女")) {
				rb_woman.setChecked(true);
			}
            String[] heads = img.split("/");
            if (img != null && !img.equals("") && !heads[heads.length-1].substring(0,7).equals("default")){
                ImageLoader.getInstance().displayImage(img, userIcon,MyApplication.getInstance().options);
            }else{
                userIcon.setBackgroundResource(R.drawable.user_default);
                if(nameStr.length()>2){
                    name2.setText(name.substring(name.length()-2, name.length()));
                }else{
                    name2.setText(name);
                }
            }
		} else if (method.equals("upload")) {
			showShortToast("修改成功!");
			MyApplication.getInstance().setName(name);
			Intent intent = new Intent(SelfInfoActivity.this,SelfCenterActivity.class);
			intent.putExtra("name", name);
			intent.putExtra("headPic", img);
			setResult(100, intent);
		}
	}

}
