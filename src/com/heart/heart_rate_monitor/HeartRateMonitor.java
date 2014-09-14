package com.heart.heart_rate_monitor;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.health.heart_rate_monitor.R;
import com.health.surfaceviews.CanvasView;
import com.health.surfaceviews.Constants;


/**
 * This class extends Activity to handle a picture preview, process the preview
 * for a red values and determine a heart beat.
 * 
 * @author zheng yajun <yajun0601@gmail.com>
 */
@SuppressLint("NewApi")
public class HeartRateMonitor extends Activity {
	private String title = "Signal Strength"; 
	private static XYSeries series;  
	private static XYMultipleSeriesDataset mDataset; 
	private static GraphicalView chart;  
	private static XYMultipleSeriesRenderer renderer; 
	private static int addX = -1;
	private static int addY; 
	static int[] xv = new int[100]; 
	static int[] yv = new int[100]; 
	
	
	private static int waitSeconds = 0;
    private static final String TAG = "HeartRateMonitor";
    private static final AtomicBoolean processing = new AtomicBoolean(false);
    private static SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    private static View image = null;
    private static TextView text = null;
    private static ToggleButton onOffToggleBtn;
	/** Called when the activity is first created. */
	private static CanvasView view;
	private FrameLayout surfaceViewFrame;

    private static WakeLock wakeLock = null;

    private static int averageIndex = 0;
    private static final int averageArraySize = 10;
    private static final int[] averageArray = new int[averageArraySize];

    public static enum TYPE {
        GREEN, RED
    };

    private static TYPE currentType = TYPE.GREEN;

    public static TYPE getCurrent() {
        return currentType;
    }

    private static SurfaceHolder surfaceHolder;
	
    private static int beatsIndex = 0;
    private static final int beatsArraySize = 3;
    private static final int[] beatsArray = new int[beatsArraySize];
    private static double beats = 0;
    private static long startTime = 0;
    private static int imgAvg = 0;
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);    
        preview = (SurfaceView) findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
       
		surfaceViewFrame = (FrameLayout) findViewById(R.id.surfaceviewFrame);
		view = new CanvasView(this);
		surfaceViewFrame.addView(view, 0);
		view.setDrawingMode(Constants.FREE_DRAWING);

		surfaceHolder = view.getHolder();
		
        image = findViewById(R.id.image);
        text = (TextView) findViewById(R.id.text);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");       
        //setPreviewFpsRange
        
      //������������������ϵ����е㣬��һ����ļ��ϣ�������Щ�㻭������ 
        series = new XYSeries(title);  
      //����һ�����ݼ���ʵ����������ݼ�������������ͼ�� 
        mDataset = new XYMultipleSeriesDataset();  
      //���㼯��ӵ�������ݼ���
        mDataset.addSeries(series);  
      //���¶������ߵ���ʽ�����Եȵȵ����ã�renderer�൱��һ��������ͼ������Ⱦ�ľ�� 
      int color = Color.RED; 
      PointStyle style = PointStyle.CIRCLE; 
      renderer = buildRenderer(color, style, true); 
      LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
    //���ú�ͼ�����ʽ
      setChartSettings(renderer, "X", "Y", 0, 100, 20, 100, Color.WHITE, Color.WHITE); 
      // ����ͼ��
      chart = ChartFactory.getLineChartView(this, mDataset, renderer); 
      //��ͼ����ӵ�������ȥ
      layout.addView(chart, new LayoutParams(LayoutParams.FILL_PARENT, 
      LayoutParams.FILL_PARENT)); 

    }
    @SuppressLint("NewApi")
	protected XYMultipleSeriesRenderer buildRenderer(int color, PointStyle style, boolean fill) { 
    	XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();  
    	//����ͼ�������߱������ʽ��������ɫ����Ĵ�С�Լ��ߵĴ�ϸ�� 
    	XYSeriesRenderer r = new XYSeriesRenderer(); 
    	r.setColor(color); r.setPointStyle(style); 
    	r.setFillPoints(fill); r.setLineWidth(3); 
    	renderer.addSeriesRenderer(r);  
    	return renderer; 
    	}
    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String xTitle, String yTitle, 
    		double xMin, double xMax, double yMin, double yMax, int axesColor, int labelsColor) { 
    	//�йض�ͼ�����Ⱦ�ɲο�api�ĵ� 
    	renderer.setChartTitle(title); 
    	renderer.setXTitle(xTitle); 
    	renderer.setYTitle(yTitle); 
    	renderer.setXAxisMin(xMin); 
    	renderer.setXAxisMax(xMax); 
    	renderer.setYAxisMin(yMin); 
    	renderer.setYAxisMax(yMax); 
    	renderer.setAxesColor(axesColor); 
    	renderer.setLabelsColor(labelsColor); 
    	renderer.setShowGrid(true); 
    	renderer.setGridColor(Color.GREEN); 
    	renderer.setXLabels(20); 
    	renderer.setYLabels(10); 
    	renderer.setXTitle("Time"); 
    	renderer.setYTitle("dBm"); 
    	renderer.setYLabelsAlign(Align.RIGHT); 
    	renderer.setPointSize((float) 2); 
    	renderer.setShowLegend(false); 
    }
    private static void updateChart(int yy) {  
    	//���ú���һ����Ҫ���ӵĽڵ� 
    	addX = 0; 
    	addY = yy; //�Ƴ����ݼ��оɵĵ㼯
    	mDataset.removeSeries(series); 
    	//�жϵ�ǰ�㼯�е����ж��ٵ㣬��Ϊ��Ļ�ܹ�ֻ������100�������Ե���������100ʱ��������Զ��100 
    	int length = series.getItemCount(); 
    	if (length > 100) { 
    		length = 100;  
    	}
    	//���ɵĵ㼯��x��y����ֵȡ��������backup�У����ҽ�x��ֵ��1�������������ƽ�Ƶ�Ч�� 
    	for (int i = 0; i < length; i++) { 
    		xv[i] = (int) series.getX(i) + 1;
    		yv[i] = (int) series.getY(i); 
    		} 
    	//�㼯����գ�Ϊ�������µĵ㼯��׼�� 
    	series.clear();  
    	//���²����ĵ����ȼ��뵽�㼯�У�Ȼ����ѭ�����н�����任���һϵ�е㶼���¼��뵽�㼯�� 
    	//�����������һ�°�˳��ߵ�������ʲôЧ������������ѭ���壬������²����ĵ� 
    	series.add(addX, addY);   
    	for (int k = 0; k < length; k++) { 
    		series.add(xv[k], yv[k]); 
    		} 
    	//�����ݼ�������µĵ㼯 
    	mDataset.addSeries(series);  
    	//��ͼ���£�û����һ�������߲�����ֶ�̬ 
    	//����ڷ�UI���߳��У���Ҫ����postInvalidate()������ο�api chart.invalidate(); 
    	chart.invalidate();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @SuppressLint("NewApi")
	private void openCamera(){

        camera = Camera.open();
        
        Camera.Parameters camParas = camera.getParameters();
        List<int[]> range=camParas.getSupportedPreviewFpsRange();
        Log.d("range", "range:"+range.size());
        for(int j=0;j<range.size();j++) {
                int[] r=range.get(j);
                for(int k=0;k<r.length;k++) {
                        Log.d("....", ".."+r[k]);
                }
        } 
        camParas.setPreviewFpsRange(15000, 15000);
        camera.setParameters(camParas);
        startTime = System.currentTimeMillis();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        //CameraInfo cameraInfo;
        wakeLock.acquire();
        openCamera();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();

        wakeLock.release();
        close_camera();
    }
    private void close_camera(){

        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }
    public int imgAvg(){
    	return imgAvg;
    }
    private static PreviewCallback previewCallback = new PreviewCallback() {
    	float times = 1;
    	 public void drawWave(){
         	Canvas c;
         	c = null;
	            try {
	                c = surfaceHolder.lockCanvas(null);
	                view.onDraw(c);
	            } finally {
	                if (c != null) {
	                    surfaceHolder.unlockCanvasAndPost(c);
	                }
	            }
    	 }
        /**
         * {@inheritDoc}
         */
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            if (data == null) throw new NullPointerException();
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) throw new NullPointerException();

            if (!processing.compareAndSet(false, true)) return;        	
            /*
            Camera.Parameters camParas = camera.getParameters();
            int range[] = {3,4};
            
            camParas.getPreviewFpsRange(range);
            Log.i(TAG, "preview fps: " + range.length);
            for(int i=0; i<range.length; i ++){
            	Log.i(TAG,"fps: " + range[i]);
            }*/
            int width = size.width;
            int height = size.height;
            //Log.i(TAG,"width: "+width + " height: "+height);
            imgAvg = ImageProcessing.decodeYUV420SPtoYSum(data.clone(), height, width);


            //Log.i(TAG, "imgAvg="+imgAvg);
            if (imgAvg == 0 || imgAvg == 255) {
                processing.set(false);
                return;
            }
            int averageArrayAvg = 0;
            int averageArrayCnt = 0;
            for (int i = 0; i < averageArray.length; i++) {
                if (averageArray[i] > 0) {
                    averageArrayAvg += averageArray[i];
                    averageArrayCnt++;
                }
            }

            int rollingAverage = (averageArrayCnt > 0) ? (averageArrayAvg / averageArrayCnt) : 0;
            if(rollingAverage !=0){
            	
            	int middleOfView = (chart.getHeight() - chart.getTop())/2;
            	int yy = imgAvg/(rollingAverage /middleOfView + 1);
            	if( yy / times > 100 ){
            		times += 0.5;            		
            	}
            	if(yy/times < 20){
            		times -= 0.1;
            	}
                updateChart((int)(yy/times));
                
            	int middleOfView1 = (view.getHeight() - view.getTop())/2;
                 view.yy = imgAvg/(rollingAverage /middleOfView1 + 1);
                Log.i(TAG,"Y:"+view.yy + " H:" + chart.getHeight() + " avg:"+ rollingAverage +" img: " + imgAvg);

            	drawWave();
            }
            
            
            TYPE newType = currentType;
            if (imgAvg < rollingAverage) {
                newType = TYPE.RED;
                if (newType != currentType) {
                    beats++;
                    // Log.d(TAG, "BEAT!! beats="+beats);
                }
            } else if (imgAvg > rollingAverage) {
                newType = TYPE.GREEN;
            }

            if (averageIndex == averageArraySize) 
            	averageIndex = 0;
            
            averageArray[averageIndex] = imgAvg;
            averageIndex++;

            // Transitioned from one state to another to the same
            if (newType != currentType) {
                currentType = newType;
                image.postInvalidate();
            }

            long endTime = System.currentTimeMillis();
            double totalTimeInSecs = (endTime - startTime) / 1000d;
            if (totalTimeInSecs >= 15) {
                double bps = (beats / totalTimeInSecs);
                int dpm = (int) (bps * 60d);
                if (dpm < 30 || dpm > 180) {
                    startTime = System.currentTimeMillis();
                    beats = 0;
                    processing.set(false);
                    return;
                }

                // Log.d(TAG,
                // "totalTimeInSecs="+totalTimeInSecs+" beats="+beats);

                if (beatsIndex == beatsArraySize) beatsIndex = 0;
                beatsArray[beatsIndex] = dpm;
                beatsIndex++;

                int beatsArrayAvg = 0;
                int beatsArrayCnt = 0;
                for (int i = 0; i < beatsArray.length; i++) {
                    if (beatsArray[i] > 0) {
                        beatsArrayAvg += beatsArray[i];
                        beatsArrayCnt++;
                    }
                }
                int beatsAvg = (beatsArrayAvg / beatsArrayCnt);
                text.setText(String.valueOf(beatsAvg));
                startTime = System.currentTimeMillis();
                beats = 0;
            }
            processing.set(false);
        }
    };

    private static SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

            //Log.d(TAG, "surfaceChanged width=" + width + " height=" + height);
            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                //Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
            }
            camera.setParameters(parameters);
            camera.startPreview();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
    };

    private static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea < resultArea) {
                    	result = size;

                    	Log.d(TAG, "----" + "width:"+size.width +"height" + size.height);
                    }
                }
            }
        }
        if(result != null)
        	Log.d(TAG, "result" + "width:"+result.width +"height" + result.height);
        return result;
    }
}
