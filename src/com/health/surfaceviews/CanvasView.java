package com.health.surfaceviews;

import java.util.ArrayList;

import com.health.heart_rate_monitor.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
/**
 * This class extends the SurfaceView class and is designed draw the heartbeat wave.
 * 
 * @author @author zheng yajun <yajun0601@gmail.com>
 */
public class CanvasView extends SurfaceView implements SurfaceHolder.Callback {

	private static final float STROKE_WIDTH = 2;

	private UIThread uiThread;

	private Path mPath;
	private Paint mPaint;

	private Path oldPath;
	
	private DrawableObject drawableObject = new FreeDrawing(mPath, mPaint);;

	private float mX;
	private float mY;

	public float xx = 0, yy = 0, yy_offset = 0;
	private float TOUCH_TOLERANCE = 8;

	private RectF dirtyRect;
	private Rect outRect;
	private int mMode = Constants.FREE_DRAWING;
	
	private ArrayList<DrawableObject> objectsToDraw;
	
	public CanvasView(Context context) {
		super(context);
		getHolder().addCallback(this);
		
		objectsToDraw = new ArrayList<DrawableObject>();

		mPath = new Path();

		FrameLayout surfaceViewFrame = (FrameLayout) findViewById(R.id.surfaceviewFrame);
		//surfaceViewFrame.getDrawingRect(outRect);
		
		//xx = outRect.centerX();
	}

	public void clearCanvas() {
		mPath.reset();
	}
	
	public void setDrawingMode(int drawingMode) {
		mMode = drawingMode;
	}

	@Override
	public void onDraw(Canvas canvas) {
		setPaintProperties();
	
		xx += 4;
		if(xx > canvas.getWidth()){
			xx = 0;
			mX = 0;
			yy_offset += 150;
			clearCanvas();
			
		}
		

		//if (dx >= TOUCH_TOLERANCE  || dy >= TOUCH_TOLERANCE) 
		{
			//mPath.quadTo(mX, mY, (xx + mX)/2, (yy + mY)/2);
//			mPath.lineTo(xx, 20*yy - 1500 + yy_offset);
//			mPath.lineTo(xx, 1000 - 10*yy);
			mPath.quadTo(mX, mY, (xx + mX)/2, (1000 - 10*yy + mY)/2);
			mX = xx;
			mY = 1000 - 10*yy;
		}
		
		
		if (canvas != null) {
			canvas.drawColor(Color.WHITE);
			
			synchronized (objectsToDraw) {
				for (DrawableObject drawableObject : objectsToDraw) {
					drawableObject.draw(canvas);
				}
			}
		}
	}

	public void stopUIThread() {
		uiThread.setRunning(false);
	}

	private void setPaintProperties() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(Color.rgb((int)xx, 100, 0));
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(STROKE_WIDTH);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		oldPath = mPath;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		
		switch (mMode) {
		case Constants.FREE_DRAWING:
			drawableObject = new FreeDrawing(mPath, mPaint);
			break;

		default:
			break;
		}
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			synchronized (objectsToDraw) {
				objectsToDraw.add(drawableObject);
			}
			mPath.moveTo(x, y);
			mX = x;
			mY = y;

			break;

		case MotionEvent.ACTION_MOVE:
			switch (mMode) {
			case Constants.FREE_DRAWING:
				float dx = Math.abs(x - mX);
				float dy = Math.abs(y - mY);

				if (dx >= TOUCH_TOLERANCE  || dy >= TOUCH_TOLERANCE) {
					mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);

					mX = x;
					mY = y;
				}
				break;
			case Constants.LINE_DRAWING:
				float lx = Math.abs(x - mX);
				float ly = Math.abs(y - mY);

				if (lx >= TOUCH_TOLERANCE  || ly >= TOUCH_TOLERANCE) {
					mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);

					mX = x;
					mY = y;
				}
				break;
			}
			break;

		case MotionEvent.ACTION_UP:
			switch (mMode) {
			case Constants.FREE_DRAWING:
				mPath.moveTo(x, y);
				break;
			
			case Constants.LINE_DRAWING:
				mPath.moveTo(x, y);
				break;
			}
			break;
		}
		return true;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		uiThread = new UIThread(this);
		//uiThread.setRunning(true);
		//uiThread.start();
	}

	public void restoreOldPath() {
		mPath = oldPath;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		uiThread.setRunning(false);
	}

	public RectF getDirtyRect() {
		if(dirtyRect == null) {
			dirtyRect = new RectF();
		}
		return dirtyRect ;
	}
}
