package com.weisen.xcxf.adapter;

import java.util.List;

import com.weisen.xcxf.R;
import com.weisen.xcxf.bean.UserLength;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DistanceAdapter extends BaseAdapter{
	private LayoutInflater inflater;
	private List<UserLength> list;
	
	public DistanceAdapter(List<UserLength> list,LayoutInflater inflater) {
		super();
		this.list = list;
		this.inflater = inflater;
	}
	
	@Override
	public int getCount() {
		
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.item_distance, null);
			holder.distance_txt = (TextView) convertView.findViewById(R.id.distance);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String distance = list.get(position).getMyLength();
		holder.distance_txt.setText(list.get(position).getMyTime()+"     里程 "+distance);

		return convertView;
	}
	class ViewHolder {
		TextView distance_txt;
	}
}
