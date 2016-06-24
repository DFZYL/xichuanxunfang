package com.weisen.xcxf.tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.weisen.xcxf.Constant;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.os.Environment;

public class FileUtils {

	public static String SDPATH = Environment.getExternalStorageDirectory()
			+ "/" + Constant.FILE_NAME + "/";

	public static boolean hasSDCard() {
		String status = Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			return false;
		}
		return true;
	}

	public static Bitmap compressImageFromFile(String srcPath) {  
        BitmapFactory.Options newOpts = new BitmapFactory.Options();  
        newOpts.inJustDecodeBounds = true;//只读边,不读内容  
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);  
  
        newOpts.inJustDecodeBounds = false;  
        int w = newOpts.outWidth;  
        int h = newOpts.outHeight;  
        
        
        
        float hh = 800f;//  
        float ww = 480f;//  
        int be = 1;  
        if (w > h && w > ww) {  
            be = (int) (newOpts.outWidth / ww);  
        } else if (w < h && h > hh) {  
            be = (int) (newOpts.outHeight / hh);  
        }  
        if (be <= 0)  
            be = 1;  
        newOpts.inSampleSize = be;//设置采样率  
          
        newOpts.inPreferredConfig = Config.ARGB_8888;//该模式是默认的,可不设  
        newOpts.inPurgeable = true;// 同时设置才会有效  
        newOpts.inInputShareable = true;//。当系统内存不够时候图片自动被回收  
          
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);  
        return bitmap;  
    } 
	

	public static void saveBitmap(Bitmap bm, String picName) {
		try {
			File f = new File(picName);
			FileOutputStream out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.JPEG, 70, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static File createSDDir(String dirName) throws IOException {
		File dir = new File(SDPATH + dirName);
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
		}
		return dir;
	}

	public static boolean isFileExist(String fileName) {
		File file = new File(SDPATH + fileName);
		file.isFile();
		return file.exists();
	}

	public static void delFile(String fileName) {
		File file = new File(SDPATH + fileName);
		if (file.isFile()) {
			file.delete();
		}
		file.exists();
	}

	public static void deleteDir() {
		File dir = new File(SDPATH);
		if (dir == null || !dir.exists() || !dir.isDirectory())
			return;

		for (File file : dir.listFiles()) {
			if (file.isFile())
				file.delete();
			else if (file.isDirectory())
				deleteDir();
		}
		dir.delete();
	}

	public static boolean fileIsExists(String path) {
		try {
			File f = new File(path);
			if (!f.exists()) {
				return false;
			}
		} catch (Exception e) {

			return false;
		}
		return true;
	}

	public static String getRootFilePath() {
		if (hasSDCard()) {
			File file = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/" + Constant.FILE_NAME + "/");
			if (!file.exists()) {
				file.mkdirs();
			}
			return Environment.getExternalStorageDirectory().getAbsolutePath()
					+ "/" + Constant.FILE_NAME + "/";
		} else {
			return Environment.getDataDirectory().getAbsolutePath() + "/data/"; // filePath:
		}
	}

	public static String getImgFilePath() {
		if (hasSDCard()) {
			File file = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/" + Constant.FILE_NAME + "/");
			if (!file.exists()) {
				file.mkdirs();
			}
			File imgFile = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/" + Constant.FILE_NAME + "/image/");
			if (!imgFile.exists()) {
				imgFile.mkdirs();
			}
			return Environment.getExternalStorageDirectory().getAbsolutePath()
					+ "/" + Constant.FILE_NAME + "/image/";
		} else {
			return Environment.getDataDirectory().getAbsolutePath() + "/data/";
		}
	}

	public static String getVoiceFilePath() {
		if (hasSDCard()) {
			File file = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/" + Constant.FILE_NAME + "/");
			if (!file.exists()) {
				file.mkdirs();
			}
			File voiceFile = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/" + Constant.FILE_NAME + "/voice/");
			if (!voiceFile.exists()) {
				voiceFile.mkdirs();
			}
			return Environment.getExternalStorageDirectory().getAbsolutePath()
					+ "/" + Constant.FILE_NAME + "/voice/";
		} else {
			return Environment.getDataDirectory().getAbsolutePath() + "/data/";
		}
	}
}
