package com.weisen.xcxf.activity;

import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.weisen.xcxf.Constant;
import com.weisen.xcxf.R;
import com.weisen.xcxf.adapter.ReportAdapter;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.CaseDao;
import com.weisen.xcxf.bean.CaseReport;
import com.weisen.xcxf.tool.CommonTool;
import com.weisen.xcxf.widget.PullToRefreshView;
import com.weisen.xcxf.widget.PullToRefreshView.OnFooterRefreshListener;

public class ReportListActivity extends BaseActivity {

	private PullToRefreshView pl_refresh;
	private ListView lv_report_list;
	private ReportAdapter reportAdapter;
	private List<CaseReport> reportList, reportList2,reportList3;
	private UpdateReportReceiver updateReportReceiver;
	private CaseDao caseDao;

	@Override
	protected void initView() {
		super.initView();
		setContentView(R.layout.activity_report_list);
		pl_refresh = (PullToRefreshView) findViewById(R.id.pl_refresh);
		pl_refresh.setEnablePullTorefresh(false);
		lv_report_list = (ListView) findViewById(R.id.lv_report_list);
		empty = findViewById(R.id.empty);
		lv_report_list.setEmptyView(empty);
		initTitle();
		iv_left.setVisibility(View.VISIBLE);
		tv_title.setText(getStringResource(R.string.title_report_list));
		iv_right.setVisibility(View.VISIBLE);
		iv_right.setBackgroundResource(R.drawable.repoint_clean);
		iv_right.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				cleanList();//删除已经上报成功的事件类型
			}

		});
	}
	private void cleanList() {
		new AlertDialog.Builder(this)
		.setTitle("提示")
		.setMessage("您确定要删除事件上报记录？")
		.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,int which) {
						dialogFlag = true;
						String uid = MyApplication.getInstance().userId;
						caseDao = new CaseDao(ReportListActivity.this);
						if (caseDao.getCount(uid)!=0) {
							caseDao.deleteList(uid);
							reportAdapter.notifyDataSetChanged();
							refresh();
						}else{
							dialog.dismiss();
						}
						reportAdapter.notifyDataSetChanged();
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
	protected void initData() {
		
		super.initData();
		updateReportReceiver = new UpdateReportReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.BROADCAST_UPDATE_REPORT);
		registerReceiver(updateReportReceiver, filter);

		pageNum = 1;
		pageSize = 10;
		caseDao = new CaseDao(this);
		reportList = caseDao.getList(pageNum, pageSize);
		reportAdapter = new ReportAdapter(this, reportList);
		lv_report_list.setAdapter(reportAdapter);
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
		pl_refresh.setOnFooterRefreshListener(new OnFooterRefreshListener() {

			@Override
			public void onFooterRefresh(PullToRefreshView view) {
				
				pageNum++;
				reportList2 = caseDao.getList(pageNum, pageSize);
				if (reportList2.size() == 0) {
					pl_refresh.setEnablePullLoadMoreDataStatus(false);
					showShortToast("数据加载完毕！");
				} else
					reportList.addAll(reportList2);
				pl_refresh.onFooterRefreshComplete();
			}
		});
	}

	class UpdateReportReceiver extends BroadcastReceiver {

        @Override
		public void onReceive(Context arg0, Intent intent) {
			
			if (intent.getAction().equals(Constant.BROADCAST_UPDATE_REPORT)) {
				String id = intent.getStringExtra("caseId");
				for (CaseReport report : reportList) {
					if (report.getId().equals(id)) {
						report.setIsSuccess("true");
						report.setUploadTime(CommonTool.getStringDate(
								new Date(), "yyyy-MM-dd HH:mm:ss"));
					}
				}
				reportAdapter.notifyDataSetChanged();
			}
		}
	}
	 /** 
     * 刷新 
     */  
    private void refresh() {  
        finish();
        Intent intent = new Intent(ReportListActivity.this, ReportListActivity.class);  
        startActivity(intent);  
    }  

	@Override
	protected void onResume() {
		
		super.onResume();
		reportAdapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
		if (updateReportReceiver != null)
			unregisterReceiver(updateReportReceiver);
	}
}
