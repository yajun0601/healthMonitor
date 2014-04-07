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
import android.util.Log;
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

	private Path mPath = new Path();
	private Paint mPaint = new Paint();

	private Path oldPath;
	
	private DrawableObject drawableObject;

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
		//FrameLayout surfaceViewFrame = (FrameLayout) findViewById(R.id.surfaceviewFrame);

		drawableObject = new FreeDrawing(mPath, mPaint);
		objectsToDraw.add(drawableObject);

		yy_offset = this.getHeight()/2;
		mX = xx;
		mY = yy_offset;
		mPath.moveTo(xx, yy_offset);


		setPaintProperties();
	}

	public void clearCanvas() {
		mPath.reset();
	}
	
	public void setDrawingMode(int drawingMode) {
		mMode = drawingMode;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if(objectsToDraw.size() == 0)
			return;
		if(canvas == null)
			return;
		final int deltaY= 100;
		float y;
		//y= yy*((1+canvas.getHeight())/deltaY - deltaY - 1);
		y = yy*(canvas.getHeight()/deltaY) - canvas.getHeight()*(canvas.getHeight()/deltaY - 1)/2;
		//Log.i(VIEW_LOG_TAG,"H:"+canvas.getHeight() +" W:"+canvas.getWidth()+ " yy:" + yy + " y:"+y);
		xx += 8;
		if(xx > canvas.getWidth()){
			xx = 0;
			mX = 0;
			//yy_offset += 150;
			clearCanvas();
		}
		

		//if (dx >= TOUCH_TOLERANCE  || dy >= TOUCH_TOLERANCE) 
		{
			//mPath.quadTo(mX, mY, (xx + mX)/2, (yy + mY)/2);
//			mPath.lineTo(xx, 20*yy - 1500 + yy_offset);
//			mPath.lineTo(xx, 1000 - 10*yy);
			mPath.quadTo(mX, mY, (xx + mX)/2, (y + mY)/2);
			mX = xx;
			mY = y;
		}
		
		
		if (canvas != null) {
			canvas.drawColor(Color.WHITE);
			//Log.d(VIEW_LOG_TAG, "len of objectsToDraw " + objectsToDraw.size() + "mPath" );
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
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(Color.rgb((int)200, 100, 0));
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
