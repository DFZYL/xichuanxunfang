package com.weisen.xcxf.adapter;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.inner.GeoPoint;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.weisen.xcxf.R;
import com.weisen.xcxf.activity.GotoLocationActivity;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.Notice;
import com.weisen.xcxf.tool.CommonTool;
import com.baidu.mapapi.navi.NaviParaOption;
public class MessageAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private List<Notice> sysNoticeList;

	public MessageAdapter(Context context, List<Notice> sysNoticeList) {
		this.context = context;
		this.sysNoticeList = sysNoticeList;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		
		return sysNoticeList.size();
	}

	@Override
	public Object getItem(int position) {
		
		return sysNoticeList.get(position);
	}

	@Override
	public long getItemId(int position) {
		
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup arg2) {
		
		ViewHolder holder = null;
		if (view == null) {
			view = inflater.inflate(R.layout.item_message, null);
			holder = new ViewHolder();
			holder.tv_sysmessage_time = (TextView) view.findViewById(R.id.tv_date);
			holder.tv_sysmessage_content = (TextView) view.findViewById(R.id.tv_msg_content);
			holder.tv_sysmessage_title = (TextView) view.findViewById(R.id.tv_msg_title);
			holder.goto_map = (TextView) view.findViewById(R.id.goto_map);
			holder.ly_goto_map = (LinearLayout) view.findViewById(R.id.ly_goto_map);
			view.setTag(holder);
		} else
			holder = (ViewHolder) view.getTag();
		final Notice notice = sysNoticeList.get(position);
		holder.tv_sysmessage_time.setText(CommonTool.getStringDate(notice.getTime(), "yyyy-MM-dd HH:mm:ss"));
		holder.tv_sysmessage_title.setText(notice.getTitle());
		holder.tv_sysmessage_content.setText(notice.getContent());
		
		if (notice.getFlag().equals("3")) {
			holder.ly_goto_map.setVisibility(View.VISIBLE);
		}else{
            holder.ly_goto_map.setVisibility(View.GONE);
        }
		holder.ly_goto_map.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				Intent intent = new Intent(context,GotoLocationActivity.class);
//				String lat = notice.getLatitude();
//				intent.putExtra("latitude", notice.getLatitude());
//				intent.putExtra("longitude", notice.getLongitude());
//				context.startActivity(intent);


				LatLng startPoint=new LatLng(MyApplication.getInstance().latitude,MyApplication.getInstance().longitude);
				LatLng endPoint=new LatLng(Double.parseDouble(notice.getLatitude()),Double.parseDouble(notice.getLongitude()));
				Log.d(Double.toString(MyApplication.getInstance().latitude),Double.toString(MyApplication.getInstance().longitude));
				Log.d(notice.getLatitude(),notice.getLongitude());
				// 构建 导航参数
				NaviParaOption para = new NaviParaOption();

			   para.startPoint(startPoint);

				para.startName("从这里开始");
				para.endPoint(endPoint);
				para.endName("到这里结束");

				try {

					BaiduMapNavigation.openBaiduMapNavi(para, context);

				} catch (BaiduMapAppNotSupportNaviException e) {
					e.printStackTrace();
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setMessage("您尚未安装百度地图app或app版本过低，请下载或更新后重试");
					builder.setTitle("提示");
					builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

						}

					});

//					builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							dialog.dismiss();
//						}
//					});

					builder.create().show();
				};

				}


			});
		return  view;
		};


	class ViewHolder {
		TextView tv_sysmessage_time, tv_sysmessage_title,tv_sysmessage_content,goto_map;
		LinearLayout ly_goto_map;
	}
}
