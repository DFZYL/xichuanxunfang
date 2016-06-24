package com.weisen.xcxf.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.weisen.xcxf.R;
import com.weisen.xcxf.bean.PeriodTime;

public class PeriodAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<PeriodTime> list;

	public PeriodAdapter(Context context,List<PeriodTime> list) {
		inflater = LayoutInflater.from(context);
		this.list = list;
	}

	public int getCount() {
		return list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater
					.inflate(R.layout.item_period_time, null);
			holder.tv_type_name = (TextView) convertView
					.findViewById(R.id.tv_type_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tv_type_name.setText(list.get(position).getName());

		return convertView;
	}

	class ViewHolder {
		TextView tv_type_name;
	}

}
