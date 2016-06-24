package com.weisen.xcxf.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.weisen.xcxf.R;
import com.weisen.xcxf.widget.MovieRecorderView;

/**
 * Created by zhou on 2015/12/21.
 */
public class MovieReccorderActivity extends BaseActivity {

    private MovieRecorderView mRecorderView;
    private Button mShootBtn;
    private boolean isFinish = true;

    @Override
    protected void initView() {
        super.initView();
        setContentView(R.layout.activity_movie_record);
        System.out.println("进入movie!!!");
        mRecorderView = (MovieRecorderView) findViewById(R.id.movieRecorderView);
        mShootBtn = (Button) findViewById(R.id.shoot_button);

        mShootBtn.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mRecorderView.record(new MovieRecorderView.OnRecordFinishListener() {

                        @Override
                        public void onRecordFinish() {
                            handler.sendEmptyMessage(1);
                        }
                    });
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (mRecorderView.getTimeCount() > 1)
                        handler.sendEmptyMessage(1);
                    else {
                        // if (mRecorderView.getVecordFile() != null)
                        //	mRecorderView.getVecordFile().delete();
                        mRecorderView.stop();
                        Toast.makeText(MovieReccorderActivity.this, "视频录制时间太短",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        isFinish = true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        isFinish = false;
        mRecorderView.stop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            finishActivity();
        }
    };

    private void finishActivity() {
        if (isFinish) {
            mRecorderView.stop();
            Intent i = new Intent();
            i.putExtra("moviePath", mRecorderView.getmVecordFile().getAbsolutePath());
            i.putExtra("movieTimes",mRecorderView.getTimeCount());
            setResult(88,i);
            finish();
            // startActivity(this, mRecorderView.getVecordFile().toString());
        }
    }

    /**
     * 录制完成回调
     *
     * @author liuyinjun
     *
     * @date 2015-2-9
     */

}
