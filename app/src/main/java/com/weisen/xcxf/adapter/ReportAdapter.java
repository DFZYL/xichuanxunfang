package com.weisen.xcxf.adapter;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.bean.CaseReport;
import com.weisen.xcxf.bean.CaseType;
import com.weisen.xcxf.bean.CaseWorker;

public class ReportAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private List<CaseReport> reportList;
	private SharedPreferences preferences;
	private List<CaseType> caseTypeList, childCaseTypeList1,
			childCaseTypeList2;
	private List<CaseWorker> caseWorkerList;

	public ReportAdapter(Context context, List<CaseReport> reportList) {
		this.context = context;
		this.reportList = reportList;
		inflater = LayoutInflater.from(context);

		preferences = context.getSharedPreferences(Constant.APP_SP, 0);
		String caseTypeStr = preferences.getString(Constant.SP_CASE_TYPE, "");
		if (caseTypeStr != null && !caseTypeStr.equals("")) {
			caseTypeList = CaseType.parseList(caseTypeStr, "list");
		}
		if (caseTypeList != null && caseTypeList.size() > 0) {
			childCaseTypeList1 = CaseType.getChildList1(caseTypeList);
		}
		String caseWorkerStr = preferences.getString(Constant.SP_CASE_WORKERS,"");
		if (caseWorkerStr != null && !caseWorkerStr.equals("")) {
			caseWorkerList = CaseWorker.parseList(caseWorkerStr, "list");
		}
	}

	@Override
	public int getCount() {
		
		return reportList.size();
	}

	@Override
	public Object getItem(int position) {
		
		return reportList.get(position);
	}

	@Override
	public long getItemId(int position) {
		
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup arg2) {
		
		ViewHolder holder = null;
		String typeName = "";
		if (view == null) {
			view = inflater.inflate(R.layout.item_report, null);
			holder = new ViewHolder();
			holder.tv_report_type = (TextView) view
					.findViewById(R.id.tv_report_type);
			holder.tv_report_time = (TextView) view
					.findViewById(R.id.tv_report_time);
			holder.tv_report_address = (TextView) view
					.findViewById(R.id.tv_report_address);
			holder.tv_report_deal = (TextView) view
					.findViewById(R.id.tv_report_deal);
			holder.tv_report_result = (TextView) view
					.findViewById(R.id.tv_report_result);
			holder.ll_upload_time = (LinearLayout) view
					.findViewById(R.id.ll_upload_time);
			holder.tv_upload_time = (TextView) view
					.findViewById(R.id.tv_upload_time);
			view.setTag(holder);
		} else
			holder = (ViewHolder) view.getTag();
		CaseReport report = reportList.get(position);
		if (report.getFlag().equals("1")) {
			typeName = report.getuRemark();
		}else{
			String childTypeId = report.getuType();
			CaseType childType = CaseType.getById(caseTypeList, childTypeId);
			CaseType parentType = CaseType.getParentById(caseTypeList, childType);
			if (parentType != null)
				typeName += parentType.getName();
			if (childType != null)
				typeName += "——" + childType.getName();
		}
		String time = report.getuTime();
		String address = report.getuAddr();
		String q = report.getQ();
		String isUploadName = "";
		String uploadTime = report.getUploadTime();
		String isSuccess = report.getIsSuccess();
		if (q.equals("true"))
			isUploadName = "自办自结";
		else {
			isUploadName = "上报";
			CaseWorker worker = CaseWorker.getById(caseWorkerList,report.getuUserId());
			if (worker != null)
				isUploadName += "(" + worker.getName() + ")";
		}
		holder.tv_report_type.setText(typeName);
		holder.tv_report_time.setText(time);
		// holder.tv_report_address.setText(address);
		holder.tv_report_deal.setText(isUploadName);
		if (isSuccess.equals("true")) {
			holder.tv_report_result.setText("已报");
			holder.tv_report_result.setBackgroundResource(R.drawable.bg_red);
			holder.ll_upload_time.setVisibility(View.VISIBLE);
			holder.tv_upload_time.setText(uploadTime);
		} else {
			holder.tv_report_result.setText("待报");
			holder.tv_report_result.setBackgroundResource(R.drawable.bg_gray);
			holder.ll_upload_time.setVisibility(View.GONE);
		}
		return view;
	}

	class ViewHolder {
		TextView tv_report_type, tv_report_time, tv_report_address,
				tv_report_deal, tv_report_result, tv_upload_time;
		LinearLayout ll_upload_time;
	}
}
