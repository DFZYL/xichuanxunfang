package com.weisen.xcxf.tool;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public class LoadImageTask extends AsyncTask<String, Void, Bitmap>{

	@Override
	protected Bitmap doInBackground(String... params) {
		
		Bitmap bitmap = null;
		try {
			String url = params[0];
			String fileName = params[1];
			HttpTool.writeImage(url,fileName);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} 
		return bitmap;
	}

}
