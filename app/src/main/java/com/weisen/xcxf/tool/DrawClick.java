package com.weisen.xcxf.tool;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public class DrawClick extends View{
	
	  
	    public DrawClick(Context context) {
		super(context);
	}

		public void onDraw(Canvas canvas) {  
	        Paint paint = new Paint();  
	        paint.setAntiAlias(true);  
	        paint.setColor(Color.BLACK);  
	        paint.setStyle(Paint.Style.STROKE); // 空心的画笔  
	        paint.setStrokeWidth(5); // 设置paint的外框宽度  
	  
	        drawDial(canvas, paint); // 绘制表盘  
	        drawHand(canvas, paint); // 绘制时针、分针、秒针  
	  
	    }  
	  
	    @SuppressLint("ResourceAsColor")
		public void drawHand(Canvas canvas, Paint paint) {  
	        int x = 310;  
	        int y = x;  
	        int hour;  
	        int minute;  
	        int second;  
	  
	        final Calendar calendar = Calendar.getInstance();  
	        hour = calendar.get(Calendar.HOUR);  
	        minute = calendar.get(Calendar.MINUTE);  
	        second = calendar.get(Calendar.SECOND);  
	  
	        float h = ((hour + (float) minute / 60) / 12) * 360;  
	        float m = ((minute + (float) second / 60) / 60) * 360;  
	        float s = ((float) second / 60) * 360;  
	  
	        // 时针  
	        paint.setColor(Color.WHITE);  
	        canvas.save(); // 线锁定画布  
	        canvas.rotate(h, x / 2, y / 2); // 旋转画布  
	        Path path1 = new Path();  
	        path1.moveTo(x / 2, y / 2); // 开始的基点  
	        path1.lineTo(x / 2, y / 4); // 最后的基点  
	        canvas.drawPath(path1, paint);  
	        canvas.restore();  
	  
	        // 分针  
	        paint.setColor(Color.RED);  
	        canvas.save();  
	        canvas.rotate(m, x / 2, y / 2); // 旋转画布  
	        Path path2 = new Path();  
	        path2.moveTo(x / 2, y / 2); // 开始的基点  
	        path2.lineTo(x / 2, y / 6); // 最后的基点  
	        canvas.drawPath(path2, paint);  
	        canvas.restore();  
	  
	        // 秒针  
	        paint.setColor(Color.BLUE);  
	        canvas.save();  
	        canvas.rotate(s, x / 2, y / 2); // 旋转画布  
	        Path path3 = new Path();  
	        path3.moveTo(x / 2, y / 2); // 开始的基点  
	        path3.lineTo(x / 2, y / 9); // 最后的基点  
	        canvas.drawPath(path3, paint);  
	        canvas.restore();  
	  
	    }  
	  
	    public void drawDial(Canvas canvas, Paint paint) {  
	        int x = 310;  
	        int y = x;  
	        paint.setColor(Color.WHITE);  
	  
	        canvas.drawCircle(x / 2, y / 2, x / 2 - 2, paint);  
	        canvas.drawCircle(x / 2, y / 2, x / 40, paint);  
	  
	        Path path9 = new Path(); // 接下来的是，画时针的刻度  
	        path9.moveTo(2, y / 2);  
	        path9.lineTo(y / 18, y / 2);  
	        canvas.drawPath(path9, paint);  
	  
	        Path path12 = new Path();  
	        path12.moveTo(x / 2, 2);  
	        path12.lineTo(x / 2, y / 18);  
	        canvas.drawPath(path12, paint);  
	  
	        Path path3 = new Path();  
	        path3.moveTo(x - 2, y / 2);  
	        path3.lineTo(x - x / 18, y / 2);  
	        canvas.drawPath(path3, paint);  
	  
	        Path path6 = new Path();  
	        path6.moveTo(x / 2, y - 2);  
	        path6.lineTo(x / 2, y - y / 18);  
	        canvas.drawPath(path6, paint);  
	  
	        canvas.save();  
	        canvas.rotate(32, x / 2, y / 2);  
	        Path path10 = new Path();  
	        path10.moveTo(2, y / 2);  
	        path10.lineTo(x / 32, y / 2);  
	        canvas.drawPath(path10, paint);  
	  
	        Path path1 = new Path();  
	        path1.moveTo(x / 2, 2);  
	        path1.lineTo(x / 2, y / 32);  
	        canvas.drawPath(path1, paint);  
	  
	        Path path4 = new Path();  
	        path4.moveTo(x - 1, y / 2);  
	        path4.lineTo(x - x / 32, y / 2);  
	        canvas.drawPath(path4, paint);  
	  
	        Path path7 = new Path();  
	        path7.moveTo(x / 2, y - 2);  
	        path7.lineTo(x / 2, y - y / 32);  
	        canvas.drawPath(path7, paint);  
	        canvas.restore();  
	  
	        canvas.save();  
	        canvas.rotate(60, x / 2, y / 2);  
	        Path path11 = new Path();  
	        path11.moveTo(2, y / 2);  
	        path11.lineTo(x / 32, y / 2);  
	        canvas.drawPath(path11, paint);  
	  
	        Path path2 = new Path();  
	        path2.moveTo(x / 2, 2);  
	        path2.lineTo(x / 2, y / 32);  
	        canvas.drawPath(path2, paint);  
	  
	        Path path5 = new Path();  
	        path5.moveTo(x - 2, y / 2);  
	        path5.lineTo(x - x / 32, y / 2);  
	        canvas.drawPath(path5, paint);  
	  
	        Path path8 = new Path();  
	        path8.moveTo(x / 2, y - 2);  
	        path8.lineTo(x / 2, y - y / 32);  
	        canvas.drawPath(path8, paint);  
	        canvas.restore();  
	    }  
}
