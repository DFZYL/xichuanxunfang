package com.weisen.xcxf.tool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.widget.Toast;

/**
 * 调用相机，图库和图片裁剪的工具类
 * 
 * @author 李洋
 * 
 */
public class ImageTools {

	/**
	 * 处理完bitmap的回调
	 * 
	 * @author 李洋
	 * 
	 */
	public interface OnBitmapCreateListener {
		/**
		 * 
		 * @param bitmap
		 *            获取的图像
		 * @param path
		 *            路径
		 */
		void onBitmapCreate(Bitmap bitmap, String path);
	}

	/**
	 * 屏蔽默认的构造方法
	 */
	private ImageTools() {

	}

	/**
	 * 是否要裁剪
	 */
	private boolean mShouldClip;

	private Context mContext;
	/**
	 * 显示选择对话框
	 */
	private String[] mItems = new String[] { "相机", "图库" };
	/**
	 * 默认的保存路径
	 */
	private String mFolderString = "/yunfengComman/";
	private static String mPath;
	/**
	 * 相机标记 在onActivityResult中可以switch这个标记
	 */
	public static final int CAMARA = 300;
	/**
	 * 图库标记 在onActivityResult中可以switch这个标记
	 */
	public static final int GALLERY = 301;
	/**
	 * 裁剪标记 在onActivityResult中可以switch这个标记
	 */
	public static final int BITMAP = 302;

	/**
	 * 默认返回的最大图片高度
	 */
	private int defaultHeight = 720;
	/**
	 * 默认返回的最大图片宽度
	 */
	private int defaultWidth = 1280;

	/**
	 * 默认裁剪后的宽度
	 */
	private int defaultClipWidth = 320;
	/**
	 * 默认裁剪后的高度
	 */
	private int defaultClipHeight = 320;
	/**
	 * 标记是从fragment启动还是从activity启动（
	 * fragment调用的startactivityForresult只能在fragment中回调onactivityresult
	 * ,activity同理）
	 */
	private boolean fromFragment;
	private Fragment mFragment;

	public void setShouldClip(boolean shouldClip) {
		mShouldClip = shouldClip;
	}

	public void setClipWidth(int width) {
		defaultClipWidth = width;
	}

	public void setClipHeight(int height) {
		defaultClipHeight = height;
	}

	public void setFromFragment(boolean from) {
		fromFragment = from;
	}

	public void setFragment(Fragment fragment) {
		mFragment = fragment;
	}

	/**
	 * 设置缓存路径
	 * 
	 * @param folder
	 */
	public void setFolder(String folder) {
		mFolderString = folder;
	}

	public void setHeight(int height) {
		defaultHeight = height;
	}

	public void setWidth(int width) {
		this.defaultWidth = width;
	}

	/**
	 * 从activity调用 用这个构造方法
	 * 
	 * @param context
	 */
	public ImageTools(Context context) {
		this.mContext = context;
		initFile();
	}

	/**
	 * 从fragment调用 用这个构造方法
	 * 
	 * @param fragment
	 */
	public ImageTools(Fragment fragment) {
		this.mFragment = fragment;
		this.mContext = fragment.getActivity();
		fromFragment = true;
		initFile();
	}

	/**
	 * 图片转字符串
	 * 
	 * @param bitmap
	 * @return
	 */
	@SuppressLint("NewApi")
	public String bitmapToString(Bitmap bitmap) {
		if (bitmap != null) {
			String str = null;
			ByteArrayOutputStream bStream = new ByteArrayOutputStream();
			bitmap.compress(CompressFormat.JPEG, 100, bStream);
			byte[] bytes = bStream.toByteArray();
			str = Base64.encodeToString(bytes, Base64.DEFAULT);
			return str;
		}
		return null;
	}

	/**
	 * 创建相关的文件和文件夹
	 */
	private void initFile() {
		File file = new File(Environment.getExternalStorageDirectory() + mFolderString);
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(file + ".nomedia");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				try {
					throw new IOException("无法创建" + file.toString() + "文件");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * 显示 相机或者图库的对话框
	 */
	public void showGetImageDialog(String title) {
		Builder mBuilder = new Builder(mContext);
		mBuilder.setTitle(title);
		mBuilder.setItems(mItems, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				switch (which) {
				case 0:
					openCamara();
					break;
				case 1:
					openGallery();
					break;
				default:
					break;
				}
			}
		});
		mBuilder.show();
	}

	/**
	 * 打开相机
	 */
	public void openCamara() {
		Intent intent = new Intent();
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
		} else {
			mPath = Environment.getExternalStorageDirectory().getAbsolutePath() + mFolderString;
			mPath += System.currentTimeMillis() + ".jpg";
			intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mPath)));
			if (fromFragment) {
				mFragment.startActivityForResult(intent, CAMARA);
			} else {
				((Activity) mContext).startActivityForResult(intent, CAMARA);
			}
		}

	}

	/**
	 * 打开图库
	 */
	public void openGallery() {
		Intent intent;
		intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		if (fromFragment) {
			mFragment.startActivityForResult(intent, GALLERY);
		} else {
			((Activity) mContext).startActivityForResult(intent, GALLERY);
		}
	}

	public boolean onActivityResult(int requestCode, int resultCode, Intent data, OnBitmapCreateListener createListener) {
		if (resultCode != Activity.RESULT_OK) {
			return false;
		}
		switch (requestCode) {
		case BITMAP:

			Bitmap bitmap = getBitmapFromZoomPhoto(data);
			createListener.onBitmapCreate(bitmap, "");
			break;
		case GALLERY:
			if (mShouldClip) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = mContext.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String filePath = cursor.getString(columnIndex);
				startZoomPhoto(Uri.fromFile(new File(filePath)), defaultClipWidth, defaultClipHeight);
			} else {
				createListener.onBitmapCreate(getBitmapFromGallery(data), "");
			}

			break;
		case CAMARA:
			if (mShouldClip) {
				getBitmapFromCamara(new OnBitmapCreateListener() {

					@Override
					public void onBitmapCreate(Bitmap bitmap, String path) {
						// TODO Auto-generated method stub
						startZoomPhoto(Uri.fromFile(new File(path)), defaultClipWidth, defaultClipHeight);
					}
				});
			} else {
				getBitmapFromCamara(createListener);
			}

			break;
		default:
			break;
		}
		return true;

	}




	/**
	 * 从onactivityresult中获取bitmap，异步方法，在回调中处理bitmap
	 * 
	 * @param onBitmapCreateListener
	 */
	@Deprecated
	public void getBitmapFromCamara(
			final OnBitmapCreateListener onBitmapCreateListener) {
		final Handler mHandler = new Handler() {
			public void handleMessage(Message msg) {
				onBitmapCreateListener.onBitmapCreate((Bitmap) msg.obj, mPath);
			};
		};
		new Thread() {
			public void run() {
				Options op = new Options();
				op.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(mPath, op);
				int size = calculateInSampleSize(op, defaultWidth,
						defaultHeight);
				// while (true) {
				// width /= 2;
				// height /= 2;
				// size *= 2;
				// if (width < defaultWidth || height < defaultHeight) {
				// break;
				// }
				// }
				op = new Options();
				op.inJustDecodeBounds = false;
				op.inSampleSize = size;
				Bitmap mBitmap = BitmapFactory.decodeFile(mPath, op);
				Message message = new Message();
				message.obj = mBitmap;
				message.what = 0;
				mHandler.sendMessage(message);
			};
		}.start();

	}

	// 计算图片的缩放值
	public static int calculateInSampleSize(Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;// 获取图片的高
		final int width = options.outWidth;// 获取图片的框
		int inSampleSize = 4;
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;// 求出缩放值
	}

	/**
	 * 从onactivityresult中获取bitmap
	 * 
	 * @param data
	 *            onactivityresult中获取的intent
	 * @return
	 */
	@Deprecated
	public Bitmap getBitmapFromGallery(Intent data) {
		Uri selectedImage = data.getData();
		String[] filePathColumn = { MediaStore.Images.Media.DATA };

		Cursor cursor = mContext.getContentResolver().query(selectedImage,
				filePathColumn, null, null, null);
		cursor.moveToFirst();

		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		String filePath = cursor.getString(columnIndex);
		cursor.close();
		Options op = new Options();
		op.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, op);
		int size = calculateInSampleSize(op, defaultWidth, defaultHeight);
		op = new Options();
		op.inJustDecodeBounds = false;
		op.inSampleSize = size;
		Bitmap mBitmap = BitmapFactory.decodeFile(filePath, op);
		return mBitmap;
	}

	/**
	 * 裁剪图片
	 * 
	 * @param uri
	 *            图片的路径URL 可以用uri.FromFile(File)获取
	 * @param outputX
	 *            裁剪的宽度
	 * @param outputY
	 *            裁剪的高度
	 */
	@Deprecated
	public void startZoomPhoto(Uri uri, int outputX, int outputY) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		//Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.setDataAndType(uri, "image/*");
		// 设置裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", outputX);
		intent.putExtra("outputY", outputY);
		intent.putExtra("return-data", true);
		try {
			if (fromFragment) {
				mFragment.startActivityForResult(intent, BITMAP);
			} else {
				((Activity) mContext).startActivityForResult(intent, BITMAP);
			}
		} catch (ActivityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(mContext, "未找到可以剪裁图片的程序", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 获取裁剪后的图片
	 * 
	 * @param data
	 * @return
	 */
	@Deprecated
	public Bitmap getBitmapFromZoomPhoto(Intent data) {
		Bundle extras = data.getExtras();
		if (extras != null) {
			return extras.getParcelable("data");

		}
		return null;
	}
}
