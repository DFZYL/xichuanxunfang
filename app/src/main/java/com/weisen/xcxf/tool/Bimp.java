package com.weisen.xcxf.tool;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.util.Log;

public class Bimp {
	public static int max = 0;
	
	public static ArrayList<ImageItem> tempSelectBitmap = new ArrayList<ImageItem>();   //选择的图片的临时列表

	public static Bitmap revitionImageSize(String path) throws IOException {
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				new File(path)));
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(in, null, options);
		in.close();
		int i = 0;
		Bitmap bitmap = null;
		while (true) {
			if ((options.outWidth >> i <= 1000)
					&& (options.outHeight >> i <= 1000)) {
				in = new BufferedInputStream(
						new FileInputStream(new File(path)));
				options.inSampleSize = (int) Math.pow(2.0D, i);
				options.inJustDecodeBounds = false;
				bitmap = BitmapFactory.decodeStream(in, null, options);
				break;
			}
			i += 1;
		}
		return bitmap;
	}
	public final static int getDegress(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * rotate the bitmap
	 * @param bitmap
	 * @param degress
	 * @return
	 */
	public static Bitmap rotateBitmap(Bitmap bitmap, int degress) {
		if (bitmap != null) {
			Matrix m = new Matrix();
			m.postRotate(degress);
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
			return bitmap;
		}
		return bitmap;
	}

	/**

	 * @return
	 */
	public final static int caculateInSampleSize(BitmapFactory.Options options, int rqsW, int rqsH) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (rqsW == 0 || rqsH == 0) return 1;
		if (height > rqsH || width > rqsW) {
			final int heightRatio = Math.round((float) height/ (float) rqsH);
			final int widthRatio = Math.round((float) width / (float) rqsW);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	/**
	 * 压缩指定路径的图片，并得到图片对象

	 * @param path bitmap source path
	 * @return Bitmap {@link android.graphics.Bitmap}
	 */
	public final static Bitmap compressBitmap(String path, int rqsW, int rqsH) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = caculateInSampleSize(options, rqsW, rqsH);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	/**
	 * 压缩指定路径图片，并将其保存在缓存目录中，通过isDelSrc判定是否删除源文件，并获取到缓存后的图片路径

	 * @param srcPath
	 * @param rqsW
	 * @param rqsH
	 * @param isDelSrc
	 * @return
	 */
	public final static String compressBitmap(String srcPath, int rqsW, int rqsH, boolean isDelSrc,String state) {
		Bitmap bitmap = compressBitmap(srcPath, rqsW, rqsH);
		File srcFile = new File(srcPath);
		String desPath = FileUtils.getImgFilePath() + state+".jpg";
		int degree = getDegress(srcPath);
		try {
			if (degree != 0) bitmap = rotateBitmap(bitmap, degree);
			File file = new File(desPath);
			FileOutputStream  fos = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG,70, fos);
			fos.close();
			if (isDelSrc) srcFile.deleteOnExit();
		} catch (Exception e) {
			// TODO: handle exception

		}
		return desPath;
	}
	public static  Bitmap toRoundBitmap(Bitmap bitmap) {
		//圆形图片宽高
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		//正方形的边长
		int r = 0;
		//取最短边做边长
		if(width > height) {
			r = height;
		} else {
			r = width;
		}
		//构建一个bitmap
		Bitmap backgroundBmp = Bitmap.createBitmap(width,
				height, Bitmap.Config.ARGB_8888);
		//new一个Canvas，在backgroundBmp上画图
		Canvas canvas = new Canvas(backgroundBmp);
		Paint paint = new Paint();
		//设置边缘光滑，去掉锯齿
		paint.setAntiAlias(true);
		//宽高相等，即正方形
		RectF rect = new RectF(0, 0, r, r);
		//通过制定的rect画一个圆角矩形，当圆角X轴方向的半径等于Y轴方向的半径时，
		//且都等于r/2时，画出来的圆角矩形就是圆形
		canvas.drawRoundRect(rect, r/2, r/2, paint);
		//设置当两个图形相交时的模式，SRC_IN为取SRC图形相交的部分，多余的将被去掉
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		//canvas将bitmap画在backgroundBmp上
		canvas.drawBitmap(bitmap, null, rect, paint);
		//返回已经绘画好的backgroundBmp
		return backgroundBmp;
	}

	public  static Bitmap FIXxy(int newWidth,int newHeight,Bitmap bitmapOrg )
	{
		int width = bitmapOrg.getWidth();
		int height = bitmapOrg.getHeight();



		//计算缩放率，新尺寸除原始尺寸
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();

		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);

		//旋转图片 动作
		matrix.postRotate(45);

		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0,
				width, height, matrix, true);

            return  resizedBitmap;

	}
}
