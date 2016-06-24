package com.weisen.xcxf.widget;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.weisen.xcxf.R;

public class MyPop extends PopupWindow {

	private TextView tv_take_photo, tv_voice, tv_cancel,tv_get_video,tv_pick_photo;
	private View view;

	public MyPop(Activity context, OnClickListener itemsOnClick,boolean voiceFlag) {
		super(context);
		LayoutInflater inflater = LayoutInflater.from(context);
		view = inflater.inflate(R.layout.popwindow_add, null);
		tv_take_photo = (TextView) view.findViewById(R.id.tv_take_pic);
		tv_pick_photo = (TextView) view.findViewById(R.id.tv_get_pic);
		tv_voice = (TextView) view.findViewById(R.id.tv_voice);
		tv_cancel = (TextView) view.findViewById(R.id.tv_pic_cancle);
        tv_get_video = (TextView) view.findViewById(R.id.tv_get_voide);
		if(voiceFlag){
            tv_voice.setVisibility(View.VISIBLE);
            tv_get_video.setVisibility(View.VISIBLE);
            tv_pick_photo.setVisibility(View.GONE);
        }

		else{
            tv_voice.setVisibility(View.GONE);
            tv_get_video.setVisibility(View.GONE);
            tv_pick_photo.setVisibility(View.VISIBLE);
            view.findViewById(R.id.div_get_voide).setVisibility(View.GONE);
        }

		// 取消按钮
		tv_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 销毁弹出框
				dismiss();
			}
		});
		// 设置按钮监听
//		tv_pick_photo.setOnClickListener(itemsOnClick);
		tv_take_photo.setOnClickListener(itemsOnClick);
		tv_voice.setOnClickListener(itemsOnClick);
        tv_get_video.setOnClickListener(itemsOnClick);
        tv_pick_photo.setOnClickListener(itemsOnClick);
		// 设置SelectPicPopupWindow的View
		this.setContentView(view);
		// 设置SelectPicPopupWindow弹出窗体的宽
		this.setWidth(LayoutParams.FILL_PARENT);
		// 设置SelectPicPopupWindow弹出窗体的高
		this.setHeight(LayoutParams.WRAP_CONTENT);
		// 设置SelectPicPopupWindow弹出窗体可点击
		this.setFocusable(true);
		// 设置SelectPicPopupWindow弹出窗体动画效果
//		this.setAnimationStyle(R.style.AnimBottom);
//		// 实例化一个ColorDrawable颜色为半透明
//		ColorDrawable dw = new ColorDrawable(R.color.white);
//		// 设置SelectPicPopupWindow弹出窗体的背景
//		this.setBackgroundDrawable(dw);
		// mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
		view.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				int height = view.findViewById(R.id.pop_layout).getTop();
				int y = (int) event.getY();
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (y < height) {
						// dismiss();
					}
				}
				return true;
			}
		});

	}
	
	public void hidePic() {
//		tv_pick_photo.setVisibility(View.GONE);
		tv_take_photo.setVisibility(View.GONE);
        view.findViewById(R.id.div_take_pic).setVisibility(View.GONE);
	}
	
	public void hideVoice() {
//		tv_pick_photo.setVisibility(View.VISIBLE);
		//tv_take_photo.setVisibility(View.VISIBLE);
		tv_voice.setVisibility(View.GONE);
	}
	
    public void showAll(){
        tv_take_photo.setVisibility(View.VISIBLE);
        tv_voice.setVisibility(View.VISIBLE);
        tv_get_video.setVisibility(View.VISIBLE);
        view.findViewById(R.id.div_take_pic).setVisibility(View.VISIBLE);
        view.findViewById(R.id.div_get_voide).setVisibility(View.VISIBLE);
    }

    public void hideVideo(){
        tv_get_video.setVisibility(View.GONE);
        view.findViewById(R.id.div_get_voide).setVisibility(View.GONE);
    }



}
