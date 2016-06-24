package com.weisen.xcxf.adapter;

import java.util.List;

import com.weisen.xcxf.R;
import com.weisen.xcxf.app.MyApplication;
import com.weisen.xcxf.bean.MessageReply;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ReplyAdapter extends BaseAdapter{
	private LayoutInflater inflater;
	private List<MessageReply> replyList;
	public ReplyAdapter(LayoutInflater inflater,List<MessageReply> replyList) {
		super();
		this.inflater = inflater;
		this.replyList = replyList;
	}

	@Override
	public int getCount() {
		
		return replyList.size();
	}

	@Override
	public Object getItem(int position) {
		
		return replyList.get(position);
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
			convertView = inflater.inflate(R.layout.item_reply, null);
			holder.userName = (TextView) convertView.findViewById(R.id.userName);
			holder.context_txt = (TextView) convertView.findViewById(R.id.replyTxt);
			holder.time_txt = (TextView) convertView.findViewById(R.id.time);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.userName.setText(MyApplication.getInstance().getName()+":");
		holder.context_txt.setText(replyList.get(position).getReplyTxt());
		holder.time_txt.setText(replyList.get(position).getReplyTime());

		return convertView;
	}

	class ViewHolder {
		TextView userName;
		TextView context_txt;
		TextView time_txt;
	}
}
