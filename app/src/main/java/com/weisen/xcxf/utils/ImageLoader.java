package com.weisen.xcxf.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.view.View;
import android.widget.ImageView;

import net.tsz.afinal.FinalBitmap;
import org.xutils.ImageManager;
import org.xutils.x;


/**
 * Created by skn on 2016/4/6/11:20.
 */
public class ImageLoader {
    private static ImageLoader instance;
    private static final String TAG = "ImageLoader";
    public FinalBitmap loader;
    private ImageManager manager;
    public static ImageLoader getInstance(Context context) {
        if (instance == null) {
            if (instance == null) {
                instance = new ImageLoader(context);
            }
        }
        return instance;
    }

    private ImageLoader(Context context) {
        loader = FinalBitmap.create(context);
//        loader.configLoadingImage(R.drawable.ic_main_2);
//        loader.configLoadfailImage(R.drawable.ic_main_2);
        manager = x.image();
    }

    public void disPlayDefault(View view, String url) {
        loader.display(view, url);
    }

    public void xDisplay(ImageView imageView,String url){
        manager.bind(imageView, url);
    }


    public static  Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }

}
