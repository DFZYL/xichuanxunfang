package com.weisen.xcxf.widget;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OutputFormat;
import android.media.MediaRecorder.VideoEncoder;
import android.media.MediaRecorder.VideoSource;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.weisen.xcxf.R;

/**
 * ��Ƶ���ſؼ�
 * 
 * @author liuyinjun  http://www.oschina.net/code/snippet_1460984_46115
 * 
 * @date 2015-2-5
 */
public class MovieRecorderView extends LinearLayout implements OnErrorListener {

	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private ProgressBar mProgressBar;

	private MediaRecorder mMediaRecorder;
	private Camera mCamera;
	private Timer mTimer;// ��ʱ��
	private OnRecordFinishListener mOnRecordFinishListener;// ¼����ɻص��ӿ�

	private int mWidth;// ��Ƶ�ֱ��ʿ��
	private int mHeight;// ��Ƶ�ֱ��ʸ߶�
	private boolean isOpenCamera;// �Ƿ�һ��ʼ�ʹ�����ͷ
	private int mRecordMaxTime;// һ�������ʱ��
	private int mTimeCount;// ʱ�����
	private File mVecordFile = null;// �ļ�

	public MovieRecorderView(Context context) {
		this(context, null);
	}

	public MovieRecorderView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public MovieRecorderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.MovieRecorderView, defStyle, 0);
		mWidth = a.getInteger(R.styleable.MovieRecorderView_width, 320);// Ĭ��320
		mHeight = a.getInteger(R.styleable.MovieRecorderView_height, 240);// Ĭ��240

		isOpenCamera = a.getBoolean(
				R.styleable.MovieRecorderView_is_open_camera, true);// Ĭ�ϴ�
		mRecordMaxTime = a.getInteger(
				R.styleable.MovieRecorderView_record_max_time, 15);// Ĭ��Ϊ10

		LayoutInflater.from(context)
				.inflate(R.layout.movie_recorder_view, this);
		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		mProgressBar.setMax(mRecordMaxTime);// ���ý���������

		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(new CustomCallBack());
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		a.recycle();
	}

	private class CustomCallBack implements Callback {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			if (!isOpenCamera)
				return;
			try {
				initCamera();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (!isOpenCamera)
				return;
			freeCameraResource();
		}

	}

	private void initCamera() throws IOException {
		if (mCamera != null) {
			freeCameraResource();
		}
		try {
			mCamera = Camera.open();
		} catch (Exception e) {
			e.printStackTrace();
			freeCameraResource();
		}
		if (mCamera == null)
			return;

		setCameraParams();

		mCamera.setDisplayOrientation(90);
		mCamera.setPreviewDisplay(mSurfaceHolder);
		mCamera.startPreview();
		mCamera.unlock();
	}

	private void setCameraParams() {
		if (mCamera != null) {
			Parameters params = mCamera.getParameters();
			params.set("orientation", "portrait");
			mCamera.setParameters(params);
		}
	}

	private void freeCameraResource() {
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.lock();
			mCamera.release();
			mCamera = null;
		}
	}

	private void createRecordDir() {
		File sampleDir = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "im/video/");
		if (!sampleDir.exists()) {
			sampleDir.mkdirs();
		}
		File vecordDir = sampleDir;
		// �����ļ�
		try {
			mVecordFile = File.createTempFile("recording", ".mp4", vecordDir);// mp4��ʽ
			// LogUtils.i(mVecordFile.getAbsolutePath());

		} catch (IOException e) {
		}
	}

	/**
	 * ��ʼ��
	 * 
	 * @author liuyinjun
	 * @date 2015-2-5
	 * @throws IOException
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void initRecord() throws IOException {
		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.reset();
		if (mCamera != null)
			mMediaRecorder.setCamera(mCamera);
		mMediaRecorder.setOnErrorListener(this);
		mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
		mMediaRecorder.setVideoSource(VideoSource.CAMERA);// ��ƵԴ
		mMediaRecorder.setAudioSource(AudioSource.MIC);// ��ƵԴ
		mMediaRecorder.setOutputFormat(OutputFormat.MPEG_4);// ��Ƶ�����ʽ
		mMediaRecorder.setAudioEncoder(AudioEncoder.AMR_NB);// ��Ƶ��ʽ
//
		mMediaRecorder.setVideoSize(mWidth, mHeight);// ���÷ֱ��ʣ�


		// mMediaRecorder.setVideoFrameRate(16);// ����Ұ���ȥ���ˣ��о�ûʲô��
		mMediaRecorder.setVideoEncodingBitRate(1 * 1024 * 512 * 2);// ����֡Ƶ�ʣ�Ȼ���������
		mMediaRecorder.setOrientationHint(90);// �����ת90�ȣ���������¼��

		mMediaRecorder.setVideoEncoder(VideoEncoder.MPEG_4_SP);// ��Ƶ¼�Ƹ�ʽ
		// mediaRecorder.setMaxDuration(Constant.MAXVEDIOTIME * 1000);
		mMediaRecorder.setOutputFile(mVecordFile.getAbsolutePath());
		mMediaRecorder.prepare();
		try {
			mMediaRecorder.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��ʼ¼����Ƶ
	 * 
	 * @author liuyinjun
	 * @date 2015-2-5
	 * @param onRecordFinishListener
	 *            �ﵽָ��ʱ��֮��ص��ӿ�
	 */
	public void record(final OnRecordFinishListener onRecordFinishListener) {
		this.mOnRecordFinishListener = onRecordFinishListener;
		createRecordDir();
		try {
			if (!isOpenCamera)// ���δ������ͷ�����
				initCamera();
			initRecord();
			mTimeCount = 0;// ʱ����������¸�ֵ
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mTimeCount++;
					mProgressBar.setProgress(mTimeCount);// ���ý����
					if (mTimeCount == mRecordMaxTime) {// �ﵽָ��ʱ�䣬ֹͣ����
						stop();
						if (mOnRecordFinishListener != null)
							mOnRecordFinishListener.onRecordFinish();
					}
				}
			}, 0, 1000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ֹͣ����
	 * 
	 * @author liuyinjun
	 * @date 2015-2-5
	 */
	public void stop() {
		stopRecord();
		releaseRecord();
		freeCameraResource();
	}

	/**
	 * ֹͣ¼��
	 * 
	 * @author liuyinjun
	 * @date 2015-2-5
	 */
	public void stopRecord() {
		mProgressBar.setProgress(0);
		if (mTimer != null)
			mTimer.cancel();
		if (mMediaRecorder != null) {
			// ���ú󲻻��
			mMediaRecorder.setOnErrorListener(null);
			mMediaRecorder.setPreviewDisplay(null);
			try {
				mMediaRecorder.stop();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (RuntimeException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * �ͷ���Դ
	 * 
	 * @author liuyinjun
	 * @date 2015-2-5
	 */
	private void releaseRecord() {
		if (mMediaRecorder != null) {
			mMediaRecorder.setOnErrorListener(null);
			try {
				mMediaRecorder.release();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		mMediaRecorder = null;
	}

	public int getTimeCount() {
		return mTimeCount;
	}

	/**
	 * @return the mVecordFile
	 */
	public File getmVecordFile() {
     return mVecordFile;
     }

     /**
	 * ¼����ɻص��ӿ�
	 * 
	 * @author liuyinjun
	 * 
	 * @date 2015-2-5
	 */
	public interface OnRecordFinishListener {
		public void onRecordFinish();
	}

	@Override
	public void onError(MediaRecorder mr, int what, int extra) {
		try {
			if (mr != null)
				mr.reset();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
