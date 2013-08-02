package com.neo.callshow.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.neo.callshow.MyApplication;
import com.neo.callshow.R;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

public class LocWindow extends Service {
	public final static String B_ACTION_NEW_OUTGOING_CALL = Intent.ACTION_NEW_OUTGOING_CALL;
	private static final String TAG = "LocWindow";
	private final int BUFFER_SIZE = 15000000;
	private String path = "/data"
			+ Environment.getDataDirectory().getAbsolutePath()
			+ "/com.neo.callshow/info.db";
	private BroadcastReceiverMgr mBroadcastReceiver;
	// 定义浮动窗口布局
	LinearLayout mFloatLayout;
	WindowManager.LayoutParams wmParams;
	// 创建浮动窗口设置布局参数的对象
	WindowManager mWindowManager;
	SQLiteDatabase db = null;
	Button mFloatView;
	DisplayMetrics dm = null;
	TelephonyManager tManager;
	CustomPhoneCallListener cpListener;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		registerIt();
		dm = getApplicationContext().getResources().getDisplayMetrics();
		db = openDatabase(path);
		wmParams = new WindowManager.LayoutParams();
		// 获取WindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager) getApplication().getSystemService(
				getApplication().WINDOW_SERVICE);
		tManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		cpListener = new CustomPhoneCallListener();
		// 通过TelephonyManager监听通话状态的改变
		tManager.listen(cpListener, PhoneStateListener.LISTEN_CALL_STATE);
		// createFloatView();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		tManager.listen(cpListener, PhoneStateListener.LISTEN_NONE);
		if (mFloatLayout != null) {
			mWindowManager.removeView(mFloatLayout);
		}
		unregisterIt();
		// tManager.listen(null, PhoneStateListener.LISTEN_CALL_STATE);
	}

	private void createFloatView() {

		// 设置window type
		wmParams.type = LayoutParams.TYPE_PHONE;
		// 设置图片格式，效果为背景透明
		wmParams.format = PixelFormat.RGBA_8888;
		// 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
		wmParams.flags =
		// LayoutParams.FLAG_NOT_TOUCH_MODAL |
		LayoutParams.FLAG_NOT_FOCUSABLE
		// LayoutParams.FLAG_NOT_TOUCHABLE
		;

		// 调整悬浮窗显示的停靠位置为左侧置顶
		wmParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
		// 悬浮框位置为原点，设置x、y初始值
		wmParams.x = 0;
		wmParams.y = 0;

		// 设置悬浮窗口长宽数据
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

		LayoutInflater inflater = LayoutInflater.from(getApplication());
		// 获取浮动窗口视图所在布局
		mFloatLayout = (LinearLayout) inflater.inflate(R.layout.loc_window,
				null);
		// 添加mFloatLayout
		mWindowManager.addView(mFloatLayout, wmParams);

		Log.i(TAG, "mFloatLayout-->left" + mFloatLayout.getLeft());
		Log.i(TAG, "mFloatLayout-->right" + mFloatLayout.getRight());
		Log.i(TAG, "mFloatLayout-->top" + mFloatLayout.getTop());
		Log.i(TAG, "mFloatLayout-->bottom" + mFloatLayout.getBottom());

		// 浮动窗口按钮
		mFloatView = (Button) mFloatLayout.findViewById(R.id.float_id);

		if ((((MyApplication) getApplication()).getLocation())
				.equalsIgnoreCase("")) {
			mFloatView
					.setText(((MyApplication) getApplication()).getPhoneNum());
			Log.d(TAG, "" + ((MyApplication) getApplication()).getPhoneNum());
		} else {
			mFloatView
					.setText(((MyApplication) getApplication()).getLocation());
			// mFloatView
			// .setText(((MyApplication) getApplication()).getPhoneNum());
		}

		mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		Log.i(TAG, "Width/2--->" + mFloatView.getMeasuredWidth() / 2);
		Log.i(TAG, "Height/2--->" + mFloatView.getMeasuredHeight() / 2);
		// 设置监听浮动窗口的触摸移动
		mFloatView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				// getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
				wmParams.x = ((int) event.getRawX() - (int) (dm).widthPixels / 2);
				// wmParams.x = 0;
				// Log.i(TAG, "Width/2--->" + mFloatView.getMeasuredWidth()/2);
				Log.i(TAG, "RawX" + event.getRawX());
				Log.i(TAG, "X" + event.getX());
				// 25为状态栏的高度

				wmParams.y = (int) event.getRawY()
						- mFloatView.getMeasuredHeight() / 2 - 25;
				// wmParams.y = 0;
				// Log.i(TAG, "Width/2--->" + mFloatView.getMeasuredHeight()/2);
				Log.i(TAG, "RawY" + event.getRawY());
				Log.i(TAG, "Y" + event.getY());
				// 刷新
				mWindowManager.updateViewLayout(mFloatLayout, wmParams);
				return false;
			}
		});

		mFloatView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		});
	}

	// 注册广播
	public void registerIt() {
		mBroadcastReceiver = new BroadcastReceiverMgr();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
		intentFilter.setPriority(Integer.MAX_VALUE);
		registerReceiver(mBroadcastReceiver, intentFilter);
		Log.d("receiver", "register");
	}

	// 撤销广播
	public void unregisterIt() {
		unregisterReceiver(mBroadcastReceiver);
		Log.d("receiver", "unregister");

	}

	public class BroadcastReceiverMgr extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			String action = arg1.getAction();
			String phoneNumber = arg1.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			((MyApplication) getApplication()).setPhoneNum(phoneNumber);
			// 呼出电话
			if (action.equals(LocWindow.B_ACTION_NEW_OUTGOING_CALL)) {
				new AsyncTask<Integer, Integer, String[]>() {
					Long time = (long) 0;

					protected void onPreExecute() {
						time = System.currentTimeMillis();
						super.onPreExecute();
					}

					@Override
					protected void onCancelled() {
						super.onCancelled();
					}

					protected String[] doInBackground(Integer... params) {
						int length = (((MyApplication) getApplication())
								.getPhoneNum()).length();
						if (length > 7) {
							Search(((MyApplication) getApplication())
									.getPhoneNum().substring(0, 7));
						} else {
							((MyApplication) getApplication()).setLocation("");
						}
						return null;
					}

					protected void onPostExecute(String[] result) {
						if (((MyApplication) getApplication()).getFlag() == 1)
							// startService();
							createFloatView();
						time = System.currentTimeMillis() - time;
						// Toast.makeText(getApplicationContext(), time + "",
						// Toast.LENGTH_SHORT).show();
						super.onPostExecute(result);
					}
				}.execute(0);

			}

		}

	}

	/**
	 * 查询归属地
	 * 
	 * @param substring
	 */
	protected void Search(String substring) {
		// TODO Auto-generated method stub
		db = SQLiteDatabase.openOrCreateDatabase(path, null);
		String sql = "SELECT location FROM info_date WHERE mobile='"
				+ substring + "';";

		Cursor cur = db.rawQuery(sql, null);
		Log.d("search", "finish");
		if (cur != null && cur.moveToFirst()) {
			do {
				String location = cur.getString(cur.getColumnIndex("location"));
				((MyApplication) getApplication()).setLocation(location);
				Log.d("locaiton", location);
			} while ((cur.moveToNext()));
			Log.d("Loading", "finish");
			cur.close();
			db.close();
		} else {
			cur.close();
			db.close();
			AreaSearch(substring.substring(0, 4));
		}
	}

	private void AreaSearch(String substring) {
		// TODO Auto-generated method stub
		db = SQLiteDatabase.openOrCreateDatabase(path, null);
		String sql = "SELECT location FROM info_date WHERE area='" + substring
				+ "';";
		Cursor cur = db.rawQuery(sql, null);
		if (cur != null && cur.moveToFirst()) {
			String location = cur.getString(cur.getColumnIndex("location"));
			((MyApplication) getApplication()).setLocation(location);
			cur.close();
			db.close();
		} else {
			cur.close();
			db.close();
			AreaSearchSecondary(substring.substring(0, 3));
		}
	}

	private void AreaSearchSecondary(String substring) {
		db = SQLiteDatabase.openOrCreateDatabase(path, null);
		String sql = "SELECT location FROM info_date WHERE area='" + substring
				+ "';";
		Cursor cur = db.rawQuery(sql, null);
		if (cur != null && cur.moveToFirst()) {
			String location = cur.getString(cur.getColumnIndex("location"));
			((MyApplication) getApplication()).setLocation(location);
			cur.close();
			db.close();
		} else {
			((MyApplication) getApplication()).setLocation("");
			cur.close();
			db.close();
		}
	}

	/**
	 * 通话状态监听
	 * 
	 * @author FranklinNEO
	 * 
	 */
	public class CustomPhoneCallListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			((MyApplication) getApplication()).setPhoneNum(incomingNumber);
			switch (state) {
			// 挂断电话
			case TelephonyManager.CALL_STATE_IDLE:
				if (((MyApplication) getApplication()).getFlag() == 1)
					if (mFloatLayout != null) {
						mWindowManager.removeView(mFloatLayout);
						mFloatLayout = null;
					}
				// stopService();
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				break;
			// 当电话呼入时
			case TelephonyManager.CALL_STATE_RINGING:
				new AsyncTask<Integer, Integer, String[]>() {
					long time = 0;

					protected void onPreExecute() {
						time = System.currentTimeMillis();
						super.onPreExecute();
					}

					@Override
					protected void onCancelled() {
						super.onCancelled();
					}

					protected String[] doInBackground(Integer... params) {
						int length = ((MyApplication) getApplication())
								.getPhoneNum().length();
						if (length > 7) {
							Search(((MyApplication) getApplication())
									.getPhoneNum().substring(0, 7));
						} else {
							((MyApplication) getApplication()).setLocation("");
						}
						return null;
					}

					protected void onPostExecute(String[] result) {
						if (((MyApplication) getApplication()).getFlag() == 1)
							// startService();
							createFloatView();
						Log.d("phone", "ring");
						time = System.currentTimeMillis() - time;
						// Toast.makeText(getApplicationContext(), time + "",
						// Toast.LENGTH_SHORT).show();
						super.onPostExecute(result);
					}
				}.execute(0);
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	}

	private SQLiteDatabase openDatabase(String dbfile) {
		try {
			if (!(new File(dbfile).exists())) {
				// 判断数据库文件是否存在，若不存在则执行导入，否则直接打开数据库
				InputStream is = getApplicationContext().getResources()
						.openRawResource(R.raw.info); // 欲导入的数据库
				FileOutputStream fos = new FileOutputStream(dbfile);
				byte[] buffer = new byte[BUFFER_SIZE];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			}
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile,
					null);
			return db;
		} catch (FileNotFoundException e) {
			Log.e("Database", "File not found");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("Database", "IO exception");
			e.printStackTrace();
		}
		return null;
	}

}
