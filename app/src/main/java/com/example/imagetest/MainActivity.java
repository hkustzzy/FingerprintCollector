package com.example.imagetest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.FloatMath;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private RelativeLayout main;
	private ImageView marker;
	private ImageView imageView;
	private int imageIndex = 1;
	private TextView text;
	private static int COMPRESS_RATE = 2;
	private MultiPointTouchListener mtpl;
    String imgFilePath;
    String filename;
	
	private Button pickButton;
	private Button startButton;
    private Spinner category;
    private Spinner map;
    private Canvas canvas;
    private Paint paint;
    Boolean flag = false;

	private float x, y;
    private float xx, yy;

    private boolean AP_FILTERING = true;

	private static final int MARKER_WIDTH = 40;
	private static final int MARKER_HEIGHT = 40;
	
	private float scale = 13;// scale is the count of  pixels per meter.

	private static int scanCount = 0;

	private WifiManager wifiManager;

	private static int TARGET_SCAN_TIMES = 10;
	private static String BASE_PATH = Environment.getExternalStorageDirectory() + "/wherami/hkust";
    private static String PRE_PATH = Environment.getExternalStorageDirectory() + "/wherami";


    private static final String[] wifi_names = {"sMobileNet","Universities WiFi","Y5ZONE","Alumni","eduroam","PCCW"};


	private List<ArrayList<Integer>> signals = new ArrayList<ArrayList<Integer>>();
	private List<Fingerprint> fingerprints = new ArrayList<Fingerprint>();
	private Map<String, Integer> macMap = new HashMap<String, Integer>();
	private int count = 0;//ap count
	private String floorName[] = {"Ground","Floor1","Floor2","Floor3","Floor4","Floor5","Floor6","Floor7","LG1","LG2","LG4","LG5","LG7"};


    private static boolean isExit = false;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };


    /**
     * @author
     * function 用于扫描SD卡上的文件
     *
     */

    public List<String> getListOnSys(File file) {
        //从根目录开始扫描
        Log.i("filescan", file.getPath());
        //HashMap<String, String> fileList = new HashMap<String, String>();
        List<String> fileList = new ArrayList<String>();
        getFileList(file, fileList);
        return fileList;
    }
    /**
     * @param path
     * @param fileList
     * 注意的是并不是所有的文件夹都可以进行读取的，权限问题
     */
    private void getFileList(File path, List<String>fileList){
        //如果是文件夹的话
        if(path.isDirectory()){
            //返回文件夹中有的数据
            File[] files = path.listFiles();
            //先判断下有没有权限，如果没有权限的话，就不执行了
            if(null == files){
                Log.i("filescan", "null or not permited");
                return;
            }

            for(int i = 0; i < files.length; i++){
                getFileList(files[i], fileList);
            }
        }
        //如果是文件的话直接加入
        else{
            if(path.getAbsolutePath().endsWith("jpg")){
                Log.i("filescan", path.getAbsolutePath());
                //进行文件的处理
                String filePath = path.getAbsolutePath();
                //文件名
                String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
                //添加
                filePath = filePath.substring(filePath.indexOf(PRE_PATH) + PRE_PATH.length());

                filename = filePath.substring(0,filePath.lastIndexOf("/"));
                filename = filename.substring(filename.lastIndexOf("/") + 1);
                imageIndex = Integer.valueOf(filename)-1000;

                fileList.add(floorName[imageIndex] + " ," + filePath);
            }
        }
    }

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        //setContentView(new SampleView(this));

		pickButton = (Button) findViewById(R.id.action_pick);
		startButton = (Button) findViewById(R.id.action_start);
		imageView = (ImageView) findViewById(R.id.imageView);
        category = (Spinner) findViewById(R.id.category);

        //setImageView(imageIndex);
		text = (TextView) findViewById(R.id.text);
		mtpl = new MultiPointTouchListener();
		imageView.setOnTouchListener(mtpl);
		main = new RelativeLayout(this);
		marker = new ImageView(this);

                RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(MARKER_WIDTH,MARKER_HEIGHT);
        parms.setMargins(-100,-100, 0,0);
        marker.setLayoutParams(parms);
        marker.setBackgroundResource(R.drawable.marker);
        main.addView(marker);
        this.addContentView(main, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		MultiPointTouchListener.marker = marker;
        wifiManager = (WifiManager) getSystemService("wifi");

		registerReceiver(new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {

                if(flag){
                    List<ScanResult> results = wifiManager.getScanResults();
                    for (ScanResult result : results) {
                        if (AP_FILTERING && !isFocus(result.SSID)) {
                            continue;
                        }
                        if (macMap.containsKey(result.BSSID)) {
                            Integer index = (Integer) macMap.get(result.BSSID);
                            signals.get(index).add(result.level);
                        } else {
                            macMap.put(result.BSSID, count);
                            signals.add(new ArrayList<Integer>());
                            signals.get(count).add(result.level);
                            count++;
                        }
                    }
                    scanCount++;
                    if (scanCount <TARGET_SCAN_TIMES) {
                        System.out.println("size scanCount:"+scanCount);
                        startButton.setText("倒计时 "+ (10-scanCount));
                        wifiManager.startScan();
                    } else {
                        Date now = new Date();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                        String date = dateFormat.format(now);
                        double medianSignals[] = new double[macMap.size()];
                        String macList[] = new String[macMap.size()];
                        Set<String> macs = macMap.keySet();

                        for (String mac : macs) {
                            int index = macMap.get(mac);
                            macList[index] = mac;
                            List<Integer> tmp = signals.get(index);
                            Collections.sort(tmp);
                            if (tmp.size() > 0) {
                                if (tmp.size() % 2 == 1) {
                                    medianSignals[index] = tmp.get(tmp.size() / 2);
                                } else {
                                    medianSignals[index] = (tmp.get(tmp.size() / 2) + tmp
                                            .get((tmp.size() - 1) / 2)) / 2.0;
                                }
                            }
                            tmp.clear();
                        }

                        fingerprints.add(new Fingerprint(imageIndex,date,new Point2D(x,y),macList,medianSignals));
                        System.out.println("size +1:" + imageIndex);
                        pickButton.setEnabled(true);
                        startButton.setEnabled(true);
                        startButton.setText("开始录入");
                        flag = false;
                    }
                }
			}
		}, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));


        File ff= new File(PRE_PATH);
        //HashMap<String, String> filelist =
        List<String> flist = new ArrayList<String>();
        if(ff.exists()){
            flist = getListOnSys(ff);
        }
        else{
            Decompress decompress = null;
            try {
                Log.i("decompress", "start");
                InputStream inputStream = getAssets().open("wherami.zip");
                decompress = new Decompress(inputStream, Environment.getExternalStorageDirectory() + "/");
                Log.i("decompress", decompress.toString());
                decompress.unzip();
                inputStream.close();

                flist = getListOnSys(ff);
            } catch (IOException e) {
                e.printStackTrace();
            }



        }

        ArrayAdapter adapter=new ArrayAdapter(this,android.R.layout.simple_spinner_item,flist);
        //设置下拉列表风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter添加到spinner中
        category.setAdapter(adapter);
        //添加Spinner事件监听
        category.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
        {
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String)adapterView.getSelectedItem();
                Toast.makeText(getApplicationContext(),item,Toast.LENGTH_LONG).show();

                setImageView(item.split(",")[1].trim());
                imageView.setOnTouchListener(mtpl);

                //设置显示当前选择的项
                //arg0.setVisibility(View.VISIBLE);
            }
        });
    }

	private void setImageView(String path){
		imgFilePath = PRE_PATH + path.trim();
		Log.i("imgFilePath",imgFilePath);
        filename = imgFilePath.substring(0,imgFilePath.lastIndexOf("/"));
        filename = filename.substring(filename.lastIndexOf("/") + 1);
        imageIndex = Integer.valueOf(filename)-1000;
        System.out.println(filename);
        Bitmap bmp = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = COMPRESS_RATE;
		bmp = BitmapFactory.decodeFile(imgFilePath, options).copy(Bitmap.Config.ARGB_8888, true);

        int WIDTH = 20;
        int HEIGHT = 20;
        canvas = new Canvas(bmp);
        float w = bmp.getWidth();
        float h = bmp.getHeight();
        int xCount=(int)w/WIDTH;
        int yCount=(int)h/HEIGHT;
        paint=new Paint();
        canvas.drawBitmap(bmp, 0, 0, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        paint.setColor(Color.BLUE);
        paint.setAlpha(50);
        for(int i=0;i<xCount;i++){
            for(int j=0;j<yCount;j++){
                canvas.drawRect(i*WIDTH, j*HEIGHT,
                        i*WIDTH+WIDTH, j*HEIGHT+HEIGHT, paint);
            }
        }
		imageView.setImageBitmap(bmp);
	}
	
	private boolean isFocus(String name) {
		for (int i = 0; i < wifi_names.length; i++) {
			if (wifi_names[i].equals(name)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onPick(View v) {

		imageView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				float[] point = new float[2];
				point[0] = event.getX();
				point[1] = event.getY();

                setMarker((int)point[0],(int)point[1]);

                //canvas.drawPoint((int)point[0],(int)point[1],paint);

				Matrix matrix = new Matrix();
				if (imageView.getImageMatrix().invert(matrix)) {
					matrix.mapPoints(point);
					x = point[0] * COMPRESS_RATE;
					y = point[1] * COMPRESS_RATE;
                    xx = point[0];
                    yy = point[1];

                    text.setText("X:" + x + "|Y:" + y);
				} else {
					text.setText("Matrix is not invertible?");
				}

				imageView.setOnTouchListener(mtpl);
				return true;
			}

		});

	}

	
	public void onStart(View v) {
		Toast.makeText(getApplicationContext(), "Collecting Wi-Fi data...", Toast.LENGTH_LONG)
				.show();
		pickButton.setEnabled(false);
		startButton.setEnabled(false);
		scanCount = 0;
		count = 0;
		signals.clear();
		macMap.clear();
        flag = true;
		wifiManager.startScan();
        setMeasuredPoint(xx,yy);
	}

	public void onEnd(View v) {
		produceTxtRecord();
		Toast.makeText(getApplicationContext(), "信息已保存",
				Toast.LENGTH_LONG).show();
	}

    public void onDelete(View v){
        if(!fingerprints.isEmpty()){
            fingerprints.remove(fingerprints.size() - 1);
            Toast.makeText(getApplicationContext(),"已清除",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"无记录",Toast.LENGTH_LONG).show();
        }
    }

    private void setMeasuredPoint(float x, float y){
        //Log.i("Position", "[" + x + "," + y + "]");
        //float xx = x - MARKER_WIDTH/2;
        //float yy = y - MARKER_HEIGHT + 4;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2);
        System.out.println("draw a point");
        canvas.drawPoint(x,y,paint);
    }

    private void setMarker(int x, int y){
		Log.i("Position", "[" + x + "," + y + "]");
		MarginLayoutParams params=(MarginLayoutParams )marker.getLayoutParams();
		params.setMargins(x - MARKER_WIDTH/2,y - MARKER_HEIGHT + 4, 0,0);
        marker.setLayoutParams(params);
    }


	private void produceTxtRecord(){
		String pathString = Environment.getExternalStorageDirectory()
				.getPath();
		File file = new File( pathString+ "/" + "fingerprint");
        if(!file.exists()){
           file.mkdirs();
        }
        File ff = new File(pathString+ "/" + "fingerprint" +"/fp_"+ filename +".txt");
        try {
            FileWriter fw = new FileWriter(ff,true);
            for (Fingerprint fp : fingerprints) {
                fw.write(fp.imageIndex+";"+fp.time+";"+fp.p.x+";"+fp.p.y+";"+Arrays.toString(fp.macs)+";"+Arrays.toString(fp.strengths)+"\n");
            }
			fw.flush();
			fw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
        fingerprints.clear();

	}


    private class Fingerprint{
		public String time;
		public int imageIndex;
		public Point2D p;
		public String[] macs;
		public double[] strengths;
		public Fingerprint(int index, String time, Point2D p,String[] macs, double[] strengths) {
			super();
			this.p = p;
			this.strengths = strengths;
			this.macs = macs;
			this.time = time;
			this.imageIndex = index;
		}
		
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast toast = Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            // 利用handler延迟发送更改状态信息
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }

}
