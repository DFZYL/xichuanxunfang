package com.weisen.xcxf.adapter;

import java.util.List;

import com.weisen.xcxf.R;
import com.weisen.xcxf.bean.CaseType;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CasrReportListAdapter extends BaseAdapter{
	private List<CaseType> reportList;
	private LayoutInflater inflater;
	
	public CasrReportListAdapter(List<CaseType> reportList,LayoutInflater inflater){
		super();
		this.reportList = reportList;
		this.inflater = inflater;
		
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater
					.inflate(R.layout.item_casereportlist, null);
			holder.caseName = (TextView) convertView.findViewById(R.id.caseName);
			holder.chooseCase = (ImageView) convertView.findViewById(R.id.chooseCase);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if (reportList.get(position).getIsChooseCase().equals("0")) {
			holder.chooseCase.setBackgroundResource(R.drawable.case_add);
		} else if (reportList.get(position).getIsChooseCase().equals("1")) {
			holder.chooseCase.setBackgroundResource(R.drawable.case_clear);
		}

		holder.caseName.setText(reportList.get(position).getName());
		return convertView;
	}
	public class ViewHolder {
		public TextView caseName;
		public ImageView chooseCase;
	}

}
