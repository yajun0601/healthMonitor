package com.health.surfaceviews;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class WaveDrawing extends DrawableObject {
	private Path mPath;
	private Paint mPaint;
	
	public WaveDrawing(Path path, Paint paint) {
		mPath = path;
		mPaint = paint;
	}
	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		canvas.drawPath(mPath, mPaint);
	}

}
