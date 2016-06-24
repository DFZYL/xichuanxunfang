package com.weisen.xcxf.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.weisen.xcxf.R;
import com.weisen.xcxf.bean.MyLocation;

import java.util.List;

public class TrailListAdapter1 extends BaseAdapter{
	private LayoutInflater inflater;
	private List<MyLocation> myLocation_list;
	public TrailListAdapter1(List<MyLocation> myLocation_list, LayoutInflater inflater){
		super();
		this.inflater = inflater;
		this.myLocation_list = myLocation_list;
		
	}

	@Override
	public int getCount() {
		
		return myLocation_list.size();
	}

	@Override
	public Object getItem(int position) {
		
		return myLocation_list.get(position);
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
			convertView = inflater
					.inflate(R.layout.activity_trail_list_item1, null);
			holder.time_txt = (TextView) convertView.findViewById(R.id.time);
			holder.latlng_txt = (TextView) convertView.findViewById(R.id.latlng);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String str = myLocation_list.get(position).getLocType();
		String str1 = myLocation_list.get(position).getSpeed();
		String str2 = myLocation_list.get(position).getAccurary();
        if(str.equals("0")){
            str = "网络";
        }else{
            str = "GPS";
        }
		holder.time_txt.setText("模式:"+str+ "    时间:"+myLocation_list.get(position).getTime());
        String lat =  myLocation_list.get(position).getLatitude().length()>9?myLocation_list.get(position).getLatitude().substring(0,9):myLocation_list.get(position).getLatitude();
        String lon = myLocation_list.get(position).getLongitude().length()>10?myLocation_list.get(position).getLongitude().substring(0,10):myLocation_list.get(position).getLongitude();
        holder.latlng_txt.setText("N:" + lat + " E:" + lon + " 速度:"+Math.round(Double.parseDouble(str1)*3.6*10)/10.0+"km/h"+" 精度:"+str2);
		return convertView;
	}

	class ViewHolder {
		TextView time_txt;
		TextView latlng_txt;
	}

    public String converLat(String srcStr){

        return  null;
    }

}
