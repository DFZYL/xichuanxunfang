package com.weisen.xcxf.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.activity.AlbumActivity;
import com.weisen.xcxf.activity.CaseReportActivity;
import com.weisen.xcxf.activity.CaseReportListActivity;
import com.weisen.xcxf.activity.GalleryActivity;
import com.weisen.xcxf.activity.MovieReccorderActivity;
import com.weisen.xcxf.activity.PreViewMovie;
import com.weisen.xcxf.activity.RecordingActivity;
import com.weisen.xcxf.adapter.CaseTypeAdapter;
import com.weisen.xcxf.adapter.CaseWorkerAdapter;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.CaseDao;
import com.weisen.xcxf.bean.CaseReport;
import com.weisen.xcxf.bean.CaseType;
import com.weisen.xcxf.bean.CaseWorker;
import com.weisen.xcxf.bean.MyLocation;
import com.weisen.xcxf.bean.MyLocationDao;
import com.weisen.xcxf.receiver.ConnectionReceiver;
import com.weisen.xcxf.tool.Bimp;
import com.weisen.xcxf.tool.CommonTool;
import com.weisen.xcxf.tool.FileUtils;
import com.weisen.xcxf.tool.GpsTool;
import com.weisen.xcxf.tool.HttpTool;
import com.weisen.xcxf.tool.ImageItem;
import com.weisen.xcxf.tool.MD5Tool;
import com.weisen.xcxf.utils.IntenetUtil;
import com.weisen.xcxf.widget.MyGridViewIn;
import com.weisen.xcxf.widget.MyPop;
import com.weisen.xcxf.widget.NoScrollGridView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.view.View.OnClickListener;

import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * Created by zhou on 2015/12/21.
 */
public class CaseReportFragment extends Fragment implements OnClickListener {
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
    private SharedPreferences.Editor editor;

    private boolean isUpload = false, isType = true, isPlaying = false;
    private String caseTypeStr, caseWorkerStr, caseTypeId = "",
            caseCategoryId = "", caseWorkerId = "", caseDesc, caseRemark;
    private MediaPlayer mPlayer = null;
    private List<CaseType> caseTypeList, childCaseTypeList1,
            childCaseTypeList2, mCaseTypesList;
    private List<CaseWorker> caseWorkerList;
    private CaseDao caseDao;
    public GridAdapter imgAdapter;
    private Button bt_upload;
    private MyPop pw;

    public static final int PHOTOHRAPH = 1;// 拍照
    public static final int PHOTOZOOM = 2; // 缩放
    public static final String IMAGE_UNSPECIFIED = "image/*";
    public String attachName = "";
    private static final int DATA_FAIL = 1;
    private static final int DATA_IMG = 2;
    private static final int SOS = 3;
    public static Bitmap bimap;
    private CaseReport report;
    private MyLocationDao myLocationDao;
    private List<MyLocation> locationList;
    private String uLocType;
    private ImageView iv_paizhao,iv_luxiang,iv_luyin;
    private UpdateTypeReceiver updateTypeReceiver;
    private ConnectionReceiver mConnectionReceiver;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    ctx.hideProgressDialog();
                    String res = (String) msg.obj;
                    JSONObject object = CommonTool.parseFromJson(res);
                    String status = CommonTool.getJsonString(object, "success");

                    if (status == null || !status.equals("true")) {
                        String msg1 = CommonTool.getJsonString(object, "msg");
                        ctx.hideProgressDialog();
                        ctx.showShortToast("上报失败原因"+msg1);
                        clear();
                    } else {
                        ctx.hideProgressDialog();
                        ctx.showShortToast("上报成功!!");
                        if (attachName != null && !attachName.equals("")) {
                            String[] names = attachName.split(",");
                            for (String name : names) {
                                File file = new File(name);
                                if (file != null && file.exists()) {
                                    file.delete();
                                }
                            }
                        }
                        report.setUploadTime(CommonTool.getStringDate(new Date(),
                                "yyyy-MM-dd HH:mm:ss"));
                        report.setIsSuccess("true");
                       // caseDao.addCase(report);

                        clear();
                    }
                    break;
                case DATA_FAIL:
                    ctx.hideProgressDialog();
                    report.setIsSuccess("false");
                    caseDao.addCase(report);
                    ctx.showShortToast("请检查网络连接!");
                    clear();
                    break;
                case DATA_IMG:
                    // imgAdapter.notifyDataSetChanged();
                    break;
                case SOS:
                    ctx.showShortToast("上报成功");
                    break;
            }
        }
    };

    protected void initData() {
        Bimp.tempSelectBitmap.clear();
        updateTypeReceiver = new UpdateTypeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.BROADCAST_UPDATE_TYPE);
        ctx.registerReceiver(updateTypeReceiver, filter);
        caseDao = new CaseDao(ctx);
        ctx.dialogFlag = false;
        mCaseTypesList = new ArrayList<CaseType>();
        caseTypeList = new ArrayList<CaseType>();
        caseTypeAdapter = new CaseTypeAdapter(ctx);
        caseTypeAdapter2 = new CaseTypeAdapter(ctx);
        gv_case_type.setAdapter(caseTypeAdapter);
        gv_case_type2.setAdapter(caseTypeAdapter2);
        caseWorkerList = new ArrayList<CaseWorker>();
        caseWorkerAdapter = new CaseWorkerAdapter(ctx);
        gv_case_worker.setAdapter(caseWorkerAdapter);
        preferences = ctx.getSharedPreferences(Constant.APP_SP, ctx.MODE_PRIVATE);
        editor = preferences.edit();
        // 插入图片列表
        imgAdapter = new GridAdapter(ctx);
        imgAdapter.update();
        gv_img.setAdapter(imgAdapter);
    }
    // 获取本地存储数据事件类型
    private void getCaseType() {
        caseTypeStr = preferences.getString(Constant.SP_CASE_TYPE, "");
        mCaseTypesList = new ArrayList<CaseType>();
        if (caseTypeStr != null && !caseTypeStr.equals("")
                && !caseTypeStr.equals("{\"list\":[]}")) {
            caseTypeList = CaseType.parseList(caseTypeStr, "list");
            for (int i = 0; i < caseTypeList.size(); i++) {
                CaseType mCaseType = caseTypeList.get(i);
                // 获取类型值为1的数据插入列表
                if (mCaseType.getIsChooseCase().equals("1")) {
                    mCaseTypesList.add(mCaseType);
                }
            }
            childCaseTypeList1 = CaseType.getChildList1(mCaseTypesList);
            if (childCaseTypeList1.isEmpty()) {
//                childCaseTypeList1 = CaseType.getChildList1(caseTypeList);
//                caseTypeAdapter.setList(childCaseTypeList1);
                ctx.showShortToast("请选择事件类型!");
                // dialogFlag = false;
                // method = "getType";
                // getServer(MyApplication.getInstance().getIP() +
                // Constant.CASE_TYPE,null, "get");
            } else {
                caseTypeAdapter.setList(childCaseTypeList1);
            }
        } else {// 本地没有数据请求网络下载
            ctx.dialogFlag = false;
            ctx.method = "getType";
            Map<String, String> map = new HashMap<String, String>();
            map.put("p",  preferences.getString(Constant.COMID,""));
            ctx.getServer(MyApplication.getInstance().getIP() + Constant.CASE_TYPE,
                    map, "get");
        }
        // 子层列表关联查询
        caseWorkerStr = preferences.getString(Constant.SP_CASE_WORKERS, "");
        if (caseWorkerStr != null && !caseWorkerStr.equals("")) {
            caseWorkerList = CaseWorker.parseList(caseWorkerStr, "list");
            if (caseWorkerList != null && caseWorkerList.size() > 0)
                caseWorkerAdapter.setList(caseWorkerList);
            else if (ctx.method.equals("")) {
                ctx.method = "getWorker";
                ctx.getServer(MyApplication.getInstance().getIP()
                        + Constant.CASE_USER, null, "get");
            }
        } else if (ctx.method.equals("")) {
            ctx.method = "getWorker";
            ctx.getServer(MyApplication.getInstance().getIP() + Constant.CASE_USER,
                    null, "get");
        }
    }


    private boolean isHaveFile() {
        if (Bimp.tempSelectBitmap.size() != 0 || !TextUtils.isEmpty(ctx.voiceName) || !TextUtils.isEmpty(ctx.moviePath)) {
            return true;
        } else {
            return false;
        }
    }

    protected void initEvent() {
        bt_upload.setBackgroundColor(getResources().getColor(
                                R.color.blue));
        iv_luxiang.setOnClickListener(this);
        iv_paizhao.setOnClickListener(this);
        iv_luyin.setOnClickListener(this);
        bt_upload.setOnClickListener(this);
        addCasetype.setOnClickListener(this);
//        gv_case_type.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            public void onItemClick(AdapterView<?> arg0, View arg1,
//                                    int position, long arg3) {
//                et_case_desc1.setVisibility(View.VISIBLE);
//                CaseType type = childCaseTypeList1.get(position);
//                if (!caseTypeId.equals(type.getId())) {
//                    if (TextUtils.isEmpty(caseCategoryId) || !isHaveFile()) {
//                        bt_upload.setBackgroundColor(getResources().getColor(
//                                R.color.gray));
//                        bt_upload.setClickable(false);
//                    } else {
//                        bt_upload.setBackgroundColor(getResources().getColor(
//                                R.color.blue));
//                        bt_upload.setClickable(true);
//                    }
//                    caseTypeAdapter.setSelected(position);
//                    caseTypeAdapter2.setSelected(-1);
//                    caseTypeId = type.getId();
//                    String caseTypeName = type.getName();
//                    tv_case_type.setText(caseTypeName);
//                    childCaseTypeList2 = CaseType.getChildList2(caseTypeList,
//                            caseTypeId);
//                    caseTypeAdapter2.setList(childCaseTypeList2);
//                    caseCategoryId = "";
//                    tv_case_category.setText("");
//                    et_case_desc1.setText("");
//                }
//            }
//        });
//        gv_case_type2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            public void onItemClick(AdapterView<?> arg0, View arg1,
//                                    int position, long arg3) {
//                CaseType category = childCaseTypeList2.get(position);
//                if (TextUtils.isEmpty(caseTypeId) || !isHaveFile()) {
//                    bt_upload.setBackgroundColor(getResources().getColor(
//                            R.color.gray));
//                    bt_upload.setClickable(false);
//                } else {
//                    bt_upload.setBackgroundColor(getResources().getColor(
//                            R.color.blue));
//                    bt_upload.setClickable(true);
//                }
//                if (!caseCategoryId.equals(category.getId())) {
//                    caseTypeAdapter2.setSelected(position);
//                    caseCategoryId = category.getId();
//                    String caseCategoryName = category.getName();
//                    tv_case_category.setText(caseCategoryName);
//                    et_case_desc1.setText(caseCategoryName);
//                }
//            }
//        });
        gv_case_worker.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                CaseWorker worker = caseWorkerList.get(position);
                if (!caseWorkerId.equals(worker.getId())) {
                    caseWorkerAdapter.setSelected(position);
                    caseWorkerId = worker.getId();
                    String workerName = worker.getName();
                    tv_case_worker.setText(workerName);
                }

            }
        });


        // rb_case_upload.setOnCheckedChangeListener(new
        // OnCheckedChangeListener() {
        //
        // @Override
        // public void onCheckedChanged(CompoundButton arg0,
        // boolean isCheck) {
        //
        // if (isCheck) {
        // isUpload = true;
        // gv_case_worker.setVisibility(View.VISIBLE);
        // }
        //
        // }
        // });
        // rb_case_deal.setOnCheckedChangeListener(new OnCheckedChangeListener()
        // {
        //
        // @Override
        // public void onCheckedChanged(CompoundButton arg0, boolean isCheck) {
        //
        // if (isCheck) {
        // isUpload = false;
        // rl_worker.setVisibility(View.GONE);
        // gv_case_worker.setVisibility(View.GONE);
        // }
        // }
        // });

       // 事件类型及紧急性
        gv_img.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
//                if (caseTypeId.equals("") || caseCategoryId.equals("")) {
//                    bt_upload.setBackgroundColor(getResources().getColor(
//                            R.color.gray));
//                    bt_upload.setClickable(false);
//                } else {
//                    bt_upload.setBackgroundColor(getResources().getColor(
//                            R.color.blue));
//                    bt_upload.setClickable(true);
//                }

                if (position == Bimp.tempSelectBitmap.size()) {
                    if (ctx.voiceName != null && !ctx.voiceName.equals("")) {
                        // 语音
                        if (isPlaying) {
                            stopPlaying();
                            isPlaying = false;
                        } else {
                            startPlaying(ctx.voiceName);
                            isPlaying = true;
                        }
                    } else if (!TextUtils.isEmpty(ctx.moviePath)) {
                        startActivity(new Intent(ctx, PreViewMovie.class)
                                .putExtra("path", ctx.moviePath)
                                .putExtra("timeCount", ctx.movieTimes));
                    } else {
                        ctx.hideKeyboard();
                        showPw(2);
                    }
                } else if (position == Bimp.tempSelectBitmap.size() + 1) {
                    if (!TextUtils.isEmpty(ctx.voiceName) && !TextUtils.isEmpty(ctx.moviePath)) {
                        startActivity(new Intent(ctx, PreViewMovie.class).putExtra("path", ctx.moviePath));
                    } else {
                        ctx.hideKeyboard();
                        showPw(1);
                    }
                } else if (position == Bimp.tempSelectBitmap.size() + 2) {
                    ctx.hideKeyboard();
                    showPw(1);
                } else {
                    Intent intent = new Intent(ctx,
                            GalleryActivity.class);
                    intent.putExtra("position", "1");
                    intent.putExtra("ID", position);
                    startActivity(intent);
                }
            }
        });
   }

    private void showPw(int position) {
//		if (position == 0) {
//			pw.hidePic();
//		} else if (position == 1) {
//			pw.hideVoice();
//		}
//        else if(position==4){
//            pw.hidePic();
//            pw.hideVoice();
//        }
//        else
//			pw.showPic();
        pw.showAll();
        if (Bimp.tempSelectBitmap.size() == 3) {
            pw.hidePic();
        }
        if (!TextUtils.isEmpty(ctx.voiceName)) {
            pw.hideVoice();
        }
        if (!TextUtils.isEmpty(ctx.moviePath)) {
            pw.hideVideo();
        }
        pw.showAtLocation(gv_img, Gravity.BOTTOM, 0, 0);
    }


    public void processSuccessResult(String res) {
        if (ctx.method.equals("getType")) {
            caseTypeList = CaseType.parseList(res, "list");
            childCaseTypeList1 = CaseType.getChildList1(caseTypeList);
            caseTypeAdapter.setList(childCaseTypeList1);
            editor.putString(Constant.SP_CASE_TYPE,
                    CaseType.getStr(caseTypeList));
            editor.commit();
            if (caseWorkerList == null || caseWorkerList.size() == 0) {
                ctx.method = "getWorker";
                ctx.getServer(MyApplication.getInstance().getIP()
                        + Constant.CASE_USER, null, "get");
            }
        }
        if (ctx.method.equals("getWorker")) {
            editor.putString(Constant.SP_CASE_WORKERS, res);
            editor.commit();
            caseWorkerList = CaseWorker.parseList(res, "list");
            caseWorkerAdapter.setList(caseWorkerList);
        }
        if (ctx.method.equals("upload")) {
            report.setIsSuccess("true");
            report.setUploadTime(CommonTool.getStringDate(new Date(),
                    "yyyy-MM-dd HH:mm:ss"));
            //caseDao.addCase(report);
            ctx.showShortToast("上报成功!");
            if (attachName != null && !attachName.equals("")) {
                String[] names = attachName.split(",");
                for (String name : names) {
                    File file = new File(name);
                    if (file != null && file.exists()) {
                        file.delete();
                    }
                }
            }
           // clear();
        }
    }


    private BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("action.refreshFriend")) {
                initData();
            }
        }
    };

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tv_get_voide:
                startActivityForResult(new Intent(ctx, MovieReccorderActivity.class), 88);
                pw.dismiss();
                break;
            case R.id.iv_shexiang:
                startActivityForResult(new Intent(ctx, MovieReccorderActivity.class), 88);
                pw.dismiss();
                break;
            case R.id.addCaseType:
                Intent in = new Intent(ctx,
                        CaseReportListActivity.class);
                startActivityForResult(in, Constant.REQUEST_CODE);
                break;
            case R.id.tv_take_pic:
                pw.dismiss();
                takePhoto();
                break;
            case R.id.iv_paizhao:
                pw.dismiss();
                takePhoto();
                break;
            case R.id.tv_get_pic:
                pw.dismiss();
                getPhoto();
                break;
            case R.id.rl_case_type:
                if (childCaseTypeList1 == null || childCaseTypeList1.size() == 0) {
                    ctx.showShortToast("未获取到数据！");
                    break;
                }
                isType = true;
                caseTypeAdapter.setList(childCaseTypeList1);
                ctx.hideKeyboard();
                break;
            case R.id.rl_case_category:
                if (caseTypeId == null || caseTypeId.equals("")) {
                    ctx.showShortToast("请先选择事件类型！");
                    break;
                }
                if (childCaseTypeList2 == null || childCaseTypeList2.size() == 0) {
                    ctx.showShortToast("未获取到数据！");
                    break;
                }
                caseTypeAdapter.setList(childCaseTypeList2);
                isType = false;
                break;
            case R.id.rl_case_worker    :
                if (caseWorkerList == null || caseWorkerList.size() == 0) {
                    ctx.showShortToast("未获取到数据！");
                    break;
                }
                break;
            case R.id.tv_voice:
                pw.dismiss();
                Intent iny = new Intent(ctx,
                        RecordingActivity.class);
                startActivityForResult(iny, 12);
                break;
            case R.id.iv_luyin:
                pw.dismiss();
                Intent iny1 = new Intent(ctx,
                        RecordingActivity.class);
                startActivityForResult(iny1, 12);
                break;
            case R.id.bt_upload:
                // 提交
//                if (caseTypeId == null || caseTypeId.equals("")) {
//                    ctx.showShortToast("事件类型");
//                    break;
//                }
//                if (caseCategoryId == null || caseCategoryId.equals("")) {
//                    ctx.showShortToast("事件类别");
//                    break;
//                }
                caseTypeId="默认";
                caseCategoryId="";

                        caseDesc = et_case_remark.getText().toString();
//                if (caseDesc == null || caseDesc.equals("")) {
//                    ctx.showShortToast("事件描述");
//                    break;
//                }
                // if (isUpload) {
                // if (caseWorkerId == null || caseWorkerId.equals("")) {
                // toastNotNull("指定上级");
                // break;
                // }
                // }
                if (Bimp.tempSelectBitmap.size() == 0 && ctx.voiceName.equals("") && TextUtils.isEmpty(ctx.moviePath)) {
                    ctx.showShortToast("请至少选择一个文件");
                    break;
                }

                myLocationDao = new MyLocationDao(ctx);
                String uid = MyApplication.getInstance().userId;
                locationList = myLocationDao.findAll(uid);
                caseRemark = et_case_remark.getText().toString();
                String id = MyApplication.getInstance().getUserId();
                String random = CommonTool.getRandom();
                String md5Sign = MD5Tool.Md5(MD5Tool.Md5(Constant.KEY + random)
                        + id);
                final Map<String, String> map = new HashMap<String, String>();
                final Map<String, File> fileMap = new HashMap<String, File>();
                final String str = "?id=" + id + "&random=" + random
                        + "&signature=" + md5Sign;
                String longitude = MyApplication.getInstance().longitude + "";
                String latitude = MyApplication.getInstance().latitude + "";
                String alti = MyApplication.getInstance().altitude + "";
                String address = MyApplication.getInstance().address;
                String speed = CommonTool
                        .formatFloat(MyApplication.getInstance().speed);
                String bearing = CommonTool
                        .formatFloat(MyApplication.getInstance().bearing);
                String accurary = CommonTool.formatFloat(MyApplication
                        .getInstance().accurary);
                String uLocType = MyApplication.getInstance().uLocType + "";

                // zh
                if (longitude == null || longitude.equals("0.0")) {
                        ctx.showShortToast("没有定位信息!");
                        break;
                }
                String time = CommonTool.getStringDate(new Date(),
                        "yyyy-MM-dd HH:mm:ss");
                map.put("uType",caseCategoryId);
                map.put("uContent", caseDesc);
                map.put("q", (!isUpload) + "");
                map.put("uRemark", caseRemark);
                map.put("uTime", time);
                if (isUpload)
                    map.put("uUserId", caseWorkerId);
                map.put("uLoti", longitude);
                map.put("uLati", latitude);
                map.put("uAlti", alti);
                map.put("net", IntenetUtil.getNetworkState(this.getActivity())+"");
                map.put("uAddr", "");
                map.put("uSpeed", speed);
                map.put("uDirection", bearing);
                map.put("uAccuracy", accurary);
                map.put("uLocType", uLocType);
                attachName = "";
                if (Bimp.tempSelectBitmap.size() >= 1) {
                    for (int i = 0; i < Bimp.tempSelectBitmap.size(); i++) {
                        ImageItem imageItem = Bimp.tempSelectBitmap.get(i);
                        String fileName = FileUtils.getImgFilePath()
                                + String.valueOf(System.currentTimeMillis())
                                + ".jpg";
                        FileUtils.saveBitmap(imageItem.getBitmap(), fileName);
                        attachName += fileName + ",";
                    }
                }
                if (!TextUtils.isEmpty(ctx.moviePath)) {
                    attachName += ctx.moviePath + ",";
                }
                if (ctx.voiceName != null && !ctx.voiceName.equals(""))
                    attachName += ctx.voiceName + ",";

                report = new CaseReport();
                report.setuType(caseCategoryId);
                report.setUid(id);
                report.setuContent(caseDesc);
                report.setQ((!isUpload) + "");
                report.setuTime(time);
                report.setuLati(latitude);
                report.setuLoti(longitude);
                report.setuAlti(alti);
                report.setuAddr("");
                report.setuSpeed(speed);
                report.setuDirection(bearing);
                report.setuAccurary(accurary);
                report.setuRemark(caseRemark);
                report.setAttach(attachName);
                report.setuUserId(caseWorkerId);
                report.setuLocType(uLocType);
                report.setFlag("0");
                try {
                    if (attachName != null && !attachName.equals("")) {
                        System.out.println("attachName:::" + attachName);
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
                ctx.dialogFlag = true;
                ctx.showProgressDialog("upload", "");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ctx.method = "upload";
                            String res = HttpTool.post(MyApplication.getInstance()
                                            .getIP() + Constant.CASE_REPORT + str, map,
                                    fileMap, "attach");

                            if (res != null && !res.equals("")) {
                                Message msg = handler.obtainMessage(ctx.DATA_SUCCESS);
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
                break;
        }
    }

    // 上传照片
    private void takePhoto() {
        if (Bimp.tempSelectBitmap.size() == 3) {
            ctx.showShortToast("最多只能选择三张图片");
        } else {
            ctx.photoPath = FileUtils.getImgFilePath() + UUID.randomUUID() + ".jpg";
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(new File(ctx.photoPath)));
            startActivityForResult(intent, ctx.PHOTOHRAPH);// 采用ForResult打开
        }
    }

    private void getPhoto() {
        if (Bimp.tempSelectBitmap.size() == 3) {
            ctx.showShortToast("最多只能选择三张图片");
        } else {
            Intent photoIntent = new Intent(ctx,
                    AlbumActivity.class);
            startActivity(photoIntent);
        }
    }

    public void startPlaying(String path) {
        if (ctx.voiceName != null && !"".equals(ctx.voiceName)) {
            try {
                mPlayer = new MediaPlayer();
                mPlayer.setDataSource(path);
                mPlayer.prepare();
                mPlayer.start();
                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer arg0) {
                        isPlaying = false;
                        mPlayer.release();
                        mPlayer = null;
                    }

                });
            } catch (IOException e) {
            }
        }
    }

    public void stopPlaying() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void clear() {
//        et_case_desc1.setVisibility(View.GONE);
//        tv_case_type.setText("");
//        tv_case_category.setText("");
//        tv_case_worker.setText("");
//        et_case_desc1.setText("");
        et_case_remark.setText("");
        // rb_case_deal.setChecked(true);
        attachName = "";
        caseTypeId = "";
        caseCategoryId = "";
        caseWorkerId = "";
        ctx.voiceName = "";
        ctx.moviePath = "";
        ctx.times = 0;
//        childCaseTypeList2.clear();
//        caseTypeAdapter2.setList(childCaseTypeList2);
//        caseTypeAdapter.setSelected(-1);
//        caseTypeAdapter2.setSelected(-1);
//        caseWorkerAdapter.setSelected(-1);
        Bimp.tempSelectBitmap.clear();
        Bimp.max = 0;
        imgAdapter.notifyDataSetChanged();
    }

    class UpdateTypeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent intent) {

            if (intent.getAction().equals(Constant.BROADCAST_UPDATE_TYPE)) {
                caseTypeStr = preferences.getString(Constant.SP_CASE_TYPE, "");
                if (caseTypeStr != null && !caseTypeStr.equals("")) {
                    caseTypeList = CaseType.parseList(caseTypeStr, "list");
                    childCaseTypeList1 = CaseType.getChildList1(caseTypeList);
                }
                caseWorkerStr = preferences.getString(Constant.SP_CASE_WORKERS,
                        "");
                if (caseWorkerStr != null && !caseWorkerStr.equals("")) {
                    List<CaseWorker> workerList = CaseWorker.parseList(
                            caseWorkerStr, "list");
                    caseWorkerList.clear();
                    caseWorkerList.addAll(workerList);
                }
                caseTypeAdapter.notifyDataSetChanged();
                caseWorkerAdapter.notifyDataSetChanged();
            }
        }
    }

    public class GridAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private int selectedPosition = -1;
        private boolean shape;

        public boolean isShape() {
            return shape;
        }

        public void setShape(boolean shape) {
            this.shape = shape;
        }

        public GridAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public void update() {
            //  loading();
        }

        @Override
        public int getCount() {
            if (Bimp.tempSelectBitmap.size() == 3 && !TextUtils.isEmpty(ctx.voiceName) && !TextUtils.isEmpty(ctx.moviePath)) {
                return 5;
            }
            if (!TextUtils.isEmpty(ctx.voiceName) && !TextUtils.isEmpty(ctx.moviePath)) {
                return (Bimp.tempSelectBitmap.size() + 3);
            } else if (!TextUtils.isEmpty(ctx.voiceName) || !TextUtils.isEmpty(ctx.moviePath)) {
                return (Bimp.tempSelectBitmap.size() + 2);
            } else {
                return (Bimp.tempSelectBitmap.size() + 1);
            }
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        public void setSelectedPosition(int position) {
            selectedPosition = position;
        }

        public int getSelectedPosition() {
            return selectedPosition;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_published_grida,
                        parent, false);
                holder = new ViewHolder();
                holder.image = (ImageView) convertView
                        .findViewById(R.id.item_grida_image);
                holder.delete = (ImageView) convertView
                        .findViewById(R.id.iv_delete);
                holder.tv_time = (TextView) convertView
                        .findViewById(R.id.tv_time);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (position == Bimp.tempSelectBitmap.size()) {
                if (ctx.voiceName != null && !ctx.voiceName.equals("")) {
                    holder.image.setImageResource(R.drawable.voice_default);
                    holder.delete.setVisibility(View.VISIBLE);
                    holder.tv_time.setText(ctx.times + "s");
                    holder.tv_time.setVisibility(View.VISIBLE);
                } else if (!TextUtils.isEmpty(ctx.moviePath)) {
                    holder.image.setImageResource(R.drawable.movie_defaul);
                    holder.delete.setVisibility(View.VISIBLE);
                    holder.tv_time.setText(ctx.movieTimes + "s");
                    holder.tv_time.setVisibility(View.VISIBLE);
                } else {
                    holder.image.setImageResource(R.drawable.camera_default);
                    holder.delete.setVisibility(View.GONE);
                    holder.tv_time.setVisibility(View.GONE);
                }

            } else if (position == Bimp.tempSelectBitmap.size() + 1) {
                //有录音 且有摄像时候
                if (!TextUtils.isEmpty(ctx.moviePath) && !TextUtils.isEmpty(ctx.voiceName)) {
                    holder.image.setImageResource(R.drawable.movie_defaul);
                    holder.delete.setVisibility(View.VISIBLE);
                    holder.tv_time.setText(ctx.movieTimes + "s");
                    holder.tv_time.setVisibility(View.VISIBLE);
                } else {
                    holder.image.setImageResource(R.drawable.camera_default);
                    holder.delete.setVisibility(View.GONE);
                    holder.tv_time.setVisibility(View.GONE);
                }
            } else if (position == Bimp.tempSelectBitmap.size() + 2) {
                holder.image.setImageResource(R.drawable.camera_default);
                holder.delete.setVisibility(View.GONE);
                holder.tv_time.setVisibility(View.GONE);
            } else {
                holder.tv_time.setVisibility(View.GONE);
                holder.delete.setVisibility(View.VISIBLE);
                // holder.image.setImageBitmap(imgList.get(position));
                holder.image.setImageBitmap(Bimp.tempSelectBitmap.get(position)
                        .getBitmap());
            }


            final int index = position;
            holder.delete.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (index == Bimp.tempSelectBitmap.size()) {
                        if (ctx.voiceName != null) {
                            ctx.voiceName = "";
                            ctx.times = 0;
                        } else {
                            ctx.moviePath = "";
                            ctx.movieTimes = 0;
                        }
                    } else if (index == Bimp.tempSelectBitmap.size() + 1) {
                        ctx.moviePath = "";
                        ctx.movieTimes = 0;
                    } else {
                        Bimp.tempSelectBitmap.remove(index);
                        Bimp.max--;
                    }
                    notifyDataSetChanged();
                }
            });
            EnabaleSubmit();

            return convertView;
        }

        public class ViewHolder {
            public ImageView image, delete;
            public TextView tv_time;
        }

        public void loading() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (Bimp.max == Bimp.tempSelectBitmap.size()) {
                            Message message = new Message();
                            message.what = DATA_IMG;
                            handler.sendMessage(message);
                            break;
                        } else {
                            Bimp.max += 1;
                            Message message = new Message();
                            message.what = DATA_IMG;
                            handler.sendMessage(message);
                        }
                    }
                }
            }).start();
        }
    }

    private CaseReportActivity ctx;

    @Override
    public void onResume() {
        super.onResume();
        locationAddr_txt.setText(MyApplication.getInstance().address);
      //  getCaseType();
        GpsTool gpsTool = new GpsTool(ctx);
        gpsTool.getAddr();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_case, null);
        ctx = (CaseReportActivity) getActivity();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action.refreshFriend");
        getActivity().registerReceiver(mRefreshBroadcastReceiver, intentFilter);
        iv_paizhao=(ImageView)view.findViewById(R.id.iv_paizhao);
        iv_luxiang=(ImageView)view.findViewById(R.id.iv_shexiang);
        iv_luyin=(ImageView) view.findViewById(R.id.iv_luyin);
        rl_worker = (RelativeLayout) view.findViewById(R.id.rl_worker);
        tv_case_type = (TextView) view.findViewById(R.id.tv_case_type);
        tv_case_category = (TextView) view.findViewById(R.id.tv_case_category);
        tv_case_worker = (TextView) view.findViewById(R.id.tv_case_worker);
        et_case_desc1 = (EditText) view.findViewById(R.id.et_case_desc1);
        et_case_remark = (EditText) view.findViewById(R.id.et_case_remark);
        // rb_case_upload = (RadioButton) findViewById(R.id.rb_case_upload);
        // rb_case_deal = (RadioButton) findViewById(R.id.rb_case_deal);
        gv_img = (MyGridViewIn) view.findViewById(R.id.gv_img);
        gv_img.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gv_case_type = (NoScrollGridView) view.findViewById(R.id.gv_case_type);
        gv_case_type2 = (NoScrollGridView) view.findViewById(R.id.gv_case_type2);
        gv_case_worker = (NoScrollGridView) view.findViewById(R.id.gv_case_worker);
        caseTypeView = LayoutInflater.from(getActivity()).inflate(
                R.layout.case_type_list, null);
        caseWorkerView = LayoutInflater.from(ctx).inflate(
                R.layout.case_type_list, null);
        bt_upload = (Button) view.findViewById(R.id.bt_upload);
        pw = new MyPop(ctx, this, true);
        addCasetype = (ImageView) view.findViewById(R.id.addCaseType);
        locationAddr_txt = (TextView) view.findViewById(R.id.locationAddr_txt);
        locationAddr_txt.setText(MyApplication.getInstance().address);
        initData();
        initEvent();
        EnabaleSubmit();
        et_case_remark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                EnabaleSubmit();

            }
        });
        return view;

    }
    private boolean EnabaleSubmit() {
        if (!TextUtils.isEmpty(et_case_remark.getText().toString())) {

            bt_upload.setBackgroundResource(R.color.blue);
            bt_upload.setEnabled(true);
            return  true;


        }
        else   if (Bimp.tempSelectBitmap.size()> 0 || !TextUtils.isEmpty(ctx.voiceName) || !TextUtils.isEmpty(ctx.moviePath)) {
            bt_upload.setBackgroundResource(R.color.blue);
            bt_upload.setEnabled(true);

            return true;
        }

        bt_upload.setBackgroundResource(R.color.low_gray);
        bt_upload.setEnabled(false);
        return false;
    }
}
