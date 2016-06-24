package com.weisen.xcxf.activity;

import java.util.UUID;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.weisen.xcxf.R;
import com.weisen.xcxf.tool.FileUtils;
import com.weisen.xcxf.widget.RecordButton;
import com.weisen.xcxf.widget.RecordButton.OnFinishedRecordListener;

public class RecordingActivity extends BaseActivity {
	private RecordButton mRecordButton = null;
	private String path;

	@Override
	protected void initView() {
		
		super.initView();
		setContentView(R.layout.activity_record);
		mRecordButton = (RecordButton) findViewById(R.id.record_button);
		initTitle();
		iv_left.setVisibility(View.VISIBLE);
		tv_title.setText(getString(R.string.title_record));
		path = FileUtils.getVoiceFilePath();
		path += UUID.randomUUID() + ".amr";
		mRecordButton.setSavePath(path);
	}

	@Override
	protected void initEvent() {
		
		super.initEvent();
		iv_left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				
				finish();
			}
		});
		mRecordButton
				.setOnFinishedRecordListener(new OnFinishedRecordListener() {

					public void onFinishedRecord(String audioPath) {

						Intent intent = new Intent(RecordingActivity.this,CaseReportActivity.class);
						intent.putExtra("totaltime",Math.round(mRecordButton.getTotalTime() / 1000));
						intent.putExtra("filePath", path);
						setResult(12, intent);
						finish();
					}
				});
	}
}
