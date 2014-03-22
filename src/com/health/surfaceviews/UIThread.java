package com.health.surfaceviews;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class UIThread extends Thread {

    private static boolean toRun = false;
    private CanvasView canvasView;
    private SurfaceHolder surfaceHolder;
	
	public UIThread(CanvasView canvasView) {
		this.canvasView = canvasView;
		surfaceHolder = canvasView.getHolder();
	}
	
	public boolean isThreadRunning() {
		return toRun;
	}

	public void setRunning(boolean run) {
		toRun = run;
	}
	
	@SuppressLint("WrongCall")
	@Override
	public void run() {
		Canvas c;
	    while (toRun) {
	            c = null;
	            try {
	                c = surfaceHolder.lockCanvas(null);
	                canvasView.onDraw(c);
	            } finally {
	                if (c != null) {
	                    surfaceHolder.unlockCanvasAndPost(c);
	                }
	            }
	            /*
	            try {
					Thread.sleep(20, 10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
	    }
	}

}
