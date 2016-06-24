package com.weisen.xcxf.tool;

import android.os.CountDownTimer;

public class TimeCount extends CountDownTimer {

	public TimeCount(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
	}
	@Override
	public void onTick(long millisUntilFinished) {
		if(countDownTimerListener!=null){
			countDownTimerListener.onStart(millisUntilFinished);
		}
	}

	@Override
	public void onFinish() {
		if(countDownTimerListener!=null){
			countDownTimerListener.onFinished();
		}
	}
	
	private onCountDownTimerListener countDownTimerListener;
	
	public void setCountDownTimerListener(
			onCountDownTimerListener countDownTimerListener) {
		this.countDownTimerListener = countDownTimerListener;
	}

	public interface onCountDownTimerListener{
		void onStart(long millisUntilFinished);
		void onFinished();
	}

}
