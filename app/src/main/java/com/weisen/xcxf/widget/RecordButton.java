package com.weisen.xcxf.widget;

import java.io.File;
import java.io.IOException;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.weisen.xcxf.R;

public class RecordButton extends Button {
	long totalTime;
	private static int SAMPLE_RATE_IN_HZ = 8000;

	public RecordButton(Context context) {
		super(context);
		init();
	}

	public RecordButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public RecordButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public void setSavePath(String path) {
		mFileName = path;
	}

	public void setOnFinishedRecordListener(OnFinishedRecordListener listener) {
		finishedListener = listener;
	}

	// 录制时间
	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	private String mFileName = null;

	private OnFinishedRecordListener finishedListener;

	private static final int MIN_INTERVAL_TIME = 1000;// 2s
	private long startTime;

	private Dialog recordIndicator;

	private static int[] res = { R.drawable.mic_2, R.drawable.mic_3,
			R.drawable.mic_4, R.drawable.mic_5 };

	private static ImageView view;

	private MediaRecorder recorder;

	private ObtainDecibelThread thread;

	private Handler volumeHandler;

	private void init() {
		volumeHandler = new ShowVolumeHandler();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (mFileName == null)
			return false;

		int action = event.getAction();
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			initDialogAndStartRecord();
			break;
		case MotionEvent.ACTION_UP:
            Log.i("distance",event.getY()+":"+height/2);
            if(Math.abs(event.getY())>height/2){
                cancelRecord();
            }else{
                finishRecord();
            }
			break;
		case MotionEvent.ACTION_CANCEL:// 当手指移动到view外面，会cancel
            if(Math.abs(event.getY())>height/2){
                cancelRecord();
            }else{
                finishRecord();
            }
			break;
		}

		return true;
	}

	private void initDialogAndStartRecord() {

		startTime = System.currentTimeMillis();
		recordIndicator = new Dialog(getContext(),
				R.style.like_toast_dialog_style);
		view = new ImageView(getContext());
		view.setImageResource(R.drawable.mic_2);

		recordIndicator.setContentView(view, new LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		recordIndicator.setOnDismissListener(onDismiss);
		LayoutParams lp = recordIndicator.getWindow().getAttributes();
		lp.gravity = Gravity.CENTER;

		startRecording();
		recordIndicator.show();
	}

	private void finishRecord() {
		stopRecording();
		recordIndicator.dismiss();

		long intervalTime = System.currentTimeMillis() - startTime;
		if (intervalTime < MIN_INTERVAL_TIME) {
			Toast.makeText(getContext(), "时间太短！", Toast.LENGTH_SHORT).show();
			File file = new File(mFileName);
			file.delete();
			return;
		} else {
			setTotalTime(intervalTime);
		}

		if (finishedListener != null)
			finishedListener.onFinishedRecord(mFileName);
	}

	private void cancelRecord() {
		stopRecording();
		recordIndicator.dismiss();
		Toast.makeText(getContext(), "取消录音！", Toast.LENGTH_SHORT).show();
		File file = new File(mFileName);
		file.delete();
	}

	private void startRecording() {
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setAudioSamplingRate(SAMPLE_RATE_IN_HZ);
		recorder.setOutputFile(mFileName);

		try {
			recorder.prepare();
			recorder.start();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		thread = new ObtainDecibelThread();
		thread.start();

	}

	private void stopRecording() {
		if (thread != null) {
			thread.exit();
			thread = null;
		}
		try {
			if (recorder != null) {
				recorder.stop();
				recorder.release();
				recorder = null;
			}
		}  catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	private class ObtainDecibelThread extends Thread {

		private volatile boolean running = true;

		public void exit() {
			running = false;
		}

		@Override
		public void run() {
			while (running) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (recorder == null || !running) {
					break;
				}
				int x = recorder.getMaxAmplitude();
				if (x != 0) {
					int f = (int) (10 * Math.log(x) / Math.log(10));
					if (f < 26)
						volumeHandler.sendEmptyMessage(0);
					else if (f < 32)
						volumeHandler.sendEmptyMessage(1);
					else if (f < 38)
						volumeHandler.sendEmptyMessage(2);
					else
						volumeHandler.sendEmptyMessage(3);

				}

			}
		}

	}

	private OnDismissListener onDismiss = new OnDismissListener() {

		public void onDismiss(DialogInterface dialog) {
			stopRecording();
		}
	};

	static class ShowVolumeHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			view.setImageResource(res[msg.what]);
		}
	}

	public interface OnFinishedRecordListener {
		public void onFinishedRecord(String audioPath);
	}

}
