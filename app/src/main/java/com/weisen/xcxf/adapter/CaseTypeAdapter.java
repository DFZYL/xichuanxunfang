package com.weisen.xcxf.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.weisen.xcxf.R;
import com.weisen.xcxf.bean.CaseType;

/**
 * @author Administrator
 * 
 */
public class CaseTypeAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<CaseType> list;
	private int selectedPosition = -1;
	private Context context;

	public CaseTypeAdapter(Context context) {
		this.context = context;
		inflater = LayoutInflater.from(context);
		list = new ArrayList<CaseType>();
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
			convertView = inflater.inflate(R.layout.item_case_type, null);
			holder.tv_type_name = (TextView) convertView
					.findViewById(R.id.tv_type_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tv_type_name.setText(list.get(position).getName());
		if (selectedPosition == position) {
			holder.tv_type_name.setTextColor(context.getResources().getColor(
					R.color.white));
			holder.tv_type_name
					.setBackgroundResource(R.drawable.bg_round_selected);
		} else {
			holder.tv_type_name.setTextColor(context.getResources().getColor(
					R.color.tv_black));
			holder.tv_type_name.setBackgroundResource(R.drawable.bg_round);
		}
		return convertView;
	}

	public void setList(List<CaseType> alist) {
		this.list.clear();
		if (alist != null)
			this.list.addAll(alist);
		notifyDataSetChanged();
	}

	public void setSelected(int position) {
		selectedPosition = position;
		notifyDataSetChanged();
	}

	class ViewHolder {
		TextView tv_type_name;
	}

}
