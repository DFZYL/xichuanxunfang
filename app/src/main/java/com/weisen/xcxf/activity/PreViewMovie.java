package com.weisen.xcxf.activity;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.weisen.xcxf.R;

/**
 * Created by zhou on 2015/12/21.
 */
public class PreViewMovie extends BaseActivity {

    private LinearLayout playBtn;
    private LinearLayout pauseBtn;
    private String path;
    private SurfaceView playView;
    private MediaPlayer player;
    private ImageView playImg,pauseImg;
    private TextView playTv,pauseTv;

    int max;
    int position;
    @Override
    protected void initView() {
        super.initView();
        setContentView(R.layout.activity_preview);
        path = getIntent().getStringExtra("path");
        max = getIntent().getIntExtra("timeCount", 10);
        if(TextUtils.isEmpty(path)){
            showShortToast("找不到文件!!");
            return;
        }
        player=new MediaPlayer();
        playBtn= (LinearLayout) findViewById(R.id.play_btn);
        pauseBtn= (LinearLayout) findViewById(R.id.pause_btn);
        playImg = (ImageView) findViewById(R.id.iv_play);
        playTv = (TextView) findViewById(R.id.tv_play);
        pauseImg = (ImageView) findViewById(R.id.iv_pause);
        pauseTv = (TextView) findViewById(R.id.tv_pause);
        playView=(SurfaceView) this.findViewById(R.id.play_surfaceV);
        playView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        playView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (position>0) {
                    try {
                        //开始播放
                        play();
                        //并直接从指定位置开始播放
                        player.seekTo(position);
                        position=0;
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {

            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position==0){
                    play();
                }else{
                    player.start();
                }
                playImg.setBackgroundResource(R.drawable.playh);
                playTv.setTextColor(getResources().getColor(R.color.mgreen));
                pauseTv.setTextColor(getResources().getColor(R.color.white));
                pauseImg.setBackgroundResource(R.drawable.stop);
            }
        });
        //暂停
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playImg.setBackgroundResource(R.drawable.play);
                playTv.setTextColor(getResources().getColor(R.color.white));
                pauseTv.setTextColor(getResources().getColor(R.color.mgreen));
                pauseImg.setBackgroundResource(R.drawable.stoph);
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.start();
                }
            }
        });

    }


    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    private Bitmap getVideoThumb(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(1000L, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
        return bitmap;
    }


    @Override
    protected void onPause() {
        //先判断是否正在播放
        if (player.isPlaying()) {
            //如果正在播放我们就先保存这个播放位置
            position=player.getCurrentPosition()
            ;
            player.stop();
        }
        super.onPause();
    }
    private void play()
    {
        try {
            Log.d("play:", "");
            player.reset();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //设置需要播放的视频
            player.setDataSource(path);
            Log.d("play:",path);
            //把视频画面输出到SurfaceView
            player.setDisplay(playView.getHolder());
            player.prepare();
            //播放
            player.start();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }


}
