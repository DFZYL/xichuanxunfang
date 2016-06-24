package com.weisen.xcxf.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.adapter.CasrReportListAdapter;
import com.weisen.xcxf.adapter.CasrReportListAdapter.ViewHolder;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.CaseType;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class CaseReportListActivity extends BaseActivity implements
		OnClickListener {
	private LayoutInflater inflater;
	private ListView caseReportList;
	private CasrReportListAdapter adapter;
	private SharedPreferences preferences;
	private Editor editor;
	private CaseType mCaseType;

	public static List<Boolean> mChecked;
	private List<CaseType> caseTypeList, caseTypeList1;
	private ViewHolder holder;

	@Override
	protected void initView() {
		super.initView();
		setContentView(R.layout.activity_casereportlist);
		inflater = LayoutInflater.from(this);
		initTitle();
		tv_title.setText("常用事件类型");
		tv_right.setVisibility(View.VISIBLE);
		tv_right.setText("确定");
		tv_right.setOnClickListener(this);
		iv_left.setOnClickListener(this);
		caseReportList = (ListView) findViewById(R.id.caseReportList);

//		dialogFlag = true;
//		method = "update";
//		getServer(MyApplication.getInstance().getIP()
//				+ Constant.UPDATE_DATA, null, "get");
	}

	@Override
	protected void initData() {
		super.initData();

		caseTypeList = new ArrayList<CaseType>();
		caseTypeList1 = new ArrayList<CaseType>();
		preferences = getSharedPreferences(Constant.APP_SP, MODE_PRIVATE);
		editor = preferences.edit();

		String caseTypeStr = preferences.getString(Constant.SP_CASE_TYPE, "");
		if (caseTypeStr != null && !caseTypeStr.equals("")) {
			caseTypeList = CaseType.parseList(caseTypeStr, "list");//得到父层，子层数据列表
			caseTypeList1 = CaseType.getChildList1(caseTypeList);//得到父层数据列表
			adapter = new CasrReportListAdapter(caseTypeList1, inflater);
			caseReportList.setAdapter(adapter);
		}
		
		if (caseTypeList == null || caseTypeList.size() == 0) {
			dialogFlag = false;
			method = "getType";
            Map<String, String> map = new HashMap<String, String>();
            map.put("p",  preferences.getString(Constant.COMID,""));

			getServer(MyApplication.getInstance().getIP() + Constant.CASE_TYPE,map, "get");
		} 
		
		caseReportList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				holder = (ViewHolder) view.getTag();
				mCaseType = (CaseType) parent.getItemAtPosition(position);
				
				if (mCaseType.getIsChooseCase().equals("0")) {
					mCaseType.setIsChooseCase("1");
				}else if (mCaseType.getIsChooseCase().equals("1")){
					mCaseType.setIsChooseCase("0");
				}
				if (mCaseType.getIsChooseCase().equals("0")) {
					holder.chooseCase.setBackgroundResource(R.drawable.case_add);
				}else if (mCaseType.getIsChooseCase().equals("1")) {
					holder.chooseCase.setBackgroundResource(R.drawable.case_clear);
				}
			}
		});
	}

    @Override
    protected void processSuccessResult(String res) {
        caseTypeList = CaseType.parseList(res, "list");//得到父层，子层数据列表
        caseTypeList1 = CaseType.getChildList1(caseTypeList);//得到父层数据列表
        adapter = new CasrReportListAdapter(caseTypeList1, inflater);
        caseReportList.setAdapter(adapter);
    }

    @Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_left:
			finish();
			break;
		case R.id.tv_right:
			//把所有数据写入本地
			editor.putString(Constant.SP_CASE_TYPE,CaseType.getStr(caseTypeList)).commit();
			// 广播通知  ，刷新界面
			Intent intent = new Intent();
			intent.setAction("action.refreshFriend");
			sendBroadcast(intent);
			finish();
			break;

		default:
			break;
		}
	}

}
