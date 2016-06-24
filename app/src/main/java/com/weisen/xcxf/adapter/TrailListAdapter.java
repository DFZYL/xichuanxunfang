package com.weisen.xcxf.adapter;

import java.util.List;

import com.blueware.agent.android.util.T;
import com.weisen.xcxf.R;
import com.weisen.xcxf.bean.MyLocation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TrailListAdapter extends BaseAdapter{
	private LayoutInflater inflater;
	private List<MyLocation> myLocation_list;
	public TrailListAdapter(List<MyLocation> myLocation_list,LayoutInflater inflater){
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
					.inflate(R.layout.activity_trail_list_item, null);
			holder.text_time= (TextView) convertView.findViewById(R.id.text_time);
			holder.text_model = (TextView) convertView.findViewById(R.id.text_model);
			holder.text_latitude=(TextView)convertView.findViewById(R.id.text_latitude);
			holder.text_longitude=(TextView)convertView.findViewById(R.id.text_longitude);
			holder.text_speed=(TextView)convertView.findViewById(R.id.text_speed);
			holder.text_accuracy=(TextView)convertView.findViewById(R.id.text_accuracy);
            holder.haiba=(TextView)convertView.findViewById(R.id.list_haiba);

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
		//holder.time_txt.setText("模式:"+str+ "    时间:"+myLocation_list.get(position).getTime());
		holder.text_model.setText("模式："+ str);
		holder.text_time.setText(myLocation_list.get(position).getTime());

		String lat =  myLocation_list.get(position).getLatitude().length()>9?myLocation_list.get(position).getLatitude().substring(0,9):myLocation_list.get(position).getLatitude();
        String lon = myLocation_list.get(position).getLongitude().length()>10?myLocation_list.get(position).getLongitude().substring(0,10):myLocation_list.get(position).getLongitude();
       // holder.latlng_txt.setText("N:" + lat + " E:" + lon + " 速度:"+Math.round(Double.parseDouble(str1)*3.6*10)/10.0+"km/h"+" 精度:"+str2);
		holder.text_latitude.setText("纬度："+lat);
		holder.text_longitude.setText("经度："+lon);
		if(Double.parseDouble(myLocation_list.get(position).getSpeed())==0){
			holder.text_speed.setText("");
		}else{
			holder.text_speed.setText("速度:"+(int)(Double.parseDouble(myLocation_list.get(position).getSpeed())*3.6)+"km/h");
		}



		if(Double.parseDouble( myLocation_list.get(position).getAccurary())==0){
			holder.text_accuracy.setText("");


		}else{

			String s="";
			if(myLocation_list.get(position).getNet()!=null) {
				switch (Integer.parseInt(myLocation_list.get(position).getNet())) {
					case 0:
						break;
					case 1:
						s = "wifi:";
						break;
					case 2:
						s = "2G:";
						break;
					case 3:
						s = "3G:";
						break;
					case 4:
						s = "4G:";
						break;
				}
			}
			holder.text_accuracy.setText("("+s+ myLocation_list.get(position).getAccurary().substring(0, myLocation_list.get(position).getAccurary().indexOf("."))+")");
		}

		if(Double.parseDouble(myLocation_list.get(position).getAltitude())==0){
			holder.haiba.setText("");
		}else{
			holder.haiba.setText("海拔:"+(myLocation_list.get(position).getAltitude().substring(0,myLocation_list.get(position).getAltitude().indexOf("."))));
		}

		return convertView;
	}

	class ViewHolder {
		TextView text_model;
		TextView text_time;
		TextView text_latitude;
		TextView text_longitude;
		TextView text_speed;
		TextView text_accuracy;
		TextView haiba;
	}

    public String converLat(String srcStr){

        return  null;
    }

}
