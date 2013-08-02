package com.neo.callshow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.neo.callshow.service.LocWindow;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class CallShow extends Activity implements OnClickListener {

	private String path = "/data"
			+ Environment.getDataDirectory().getAbsolutePath()
			+ "/com.neo.callshow/info.db";

	SQLiteDatabase db = null;
	SQLiteDatabase setting = null;

	private final int BUFFER_SIZE = 15000000;

	public Dialog loading = null;
	private EditText search_et = null;
	private TextView Loc_tv = null;
	// private TextView Load_tv = null;
	private Button loc_btn = null;
	private Button load_btn = null;
	private ToggleButton tbtn = null;
	private ToggleButton shift = null;
	private SharedPreferences sp = null;
	private Boolean ServiceStart;
	private Boolean LoadFinish;
	private Boolean ShowFloat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call_show);
		sp = getSharedPreferences("Setting", Context.MODE_PRIVATE);
		ServiceStart = sp.getBoolean("ServiceStart", false);
		LoadFinish = sp.getBoolean("LoadFinish", false);
		ShowFloat = sp.getBoolean("ShowFloat", false);

		((MyApplication) getApplication()).setFlag(1);
		loading = new Dialog(CallShow.this, R.style.mmdialog);
		loading.setContentView(R.layout.loading_dialog);
		search_et = (EditText) findViewById(R.id.search_bar_et);
		Loc_tv = (TextView) findViewById(R.id.Loctv);
		// Load_tv = (TextView) findViewById(R.id.loadtv);
		loc_btn = (Button) findViewById(R.id.LocBtn);
		loc_btn.setOnClickListener(this);
		load_btn = (Button) findViewById(R.id.LoadBtn);
		load_btn.setOnClickListener(this);
		tbtn = (ToggleButton) findViewById(R.id.toggleBtn);
		tbtn.setChecked(ShowFloat);
		tbtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					((MyApplication) getApplication()).setFlag(1);
					ShowFloat = true;
				} else {
					((MyApplication) getApplication()).setFlag(0);
					ShowFloat = false;
				}
			}
		});
		
		// File file = new File(URL, DB_FILE_NAME);
		// setting = SQLiteDatabase.openOrCreateDatabase(file,
		// null);
		// String sql = "SELECT * FROM setting_data;";
		// Cursor cur = setting.rawQuery(sql, null);
		// if (cur != null && cur.moveToFirst()) {
		// ((MyApplication) getApplication()).setFlag(cur.getInt(cur
		// .getColumnIndex("flag")));
		// ((MyApplication) getApplication()).setLoad(cur.getInt(cur
		// .getColumnIndex("load")));
		// ((MyApplication) getApplication()).setState(cur.getInt(cur
		// .getColumnIndex("state")));
		// cur.close();
		// setting.close();
		// } else {
		// ((MyApplication) getApplication()).setFlag(0);
		// ((MyApplication) getApplication()).setLoad(0);
		// ((MyApplication) getApplication()).setState(0);
		// cur.close();
		// setting.close();
		// }
		// if (((MyApplication) getApplication()).getFlag() > 0) {
		// tbtn.setChecked(true);
		// } else {
		// tbtn.setChecked(false);
		// }
		//
		// if (((MyApplication) getApplication()).getLoad() > 0) {
		// load_btn.setEnabled(false);
		// } else {
		// load_btn.setEnabled(true);
		// }
		//
		// if (((MyApplication) getApplication()).getState() > 0) {
		// shift.setChecked(true);
		// } else {
		// shift.setChecked(false);
		// }

		shift = (ToggleButton) findViewById(R.id.toggleTitle);
		shift.setChecked(ServiceStart);
		shift.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					startService();

					Toast.makeText(getApplicationContext(), "服务已开启",
							Toast.LENGTH_SHORT).show();
					((MyApplication) getApplication()).setState(1);
					ServiceStart = true;
				} else {
					stopService();
					// unregisterReceiver(LocWindow.mBroadcastReceiver);
					Toast.makeText(getApplicationContext(), "服务已关闭",
							Toast.LENGTH_SHORT).show();
					((MyApplication) getApplication()).setState(0);
					ServiceStart = false;
				}
			}
		});

		new AsyncTask<Integer, Integer, String[]>() {

			protected void onPreExecute() {
				super.onPreExecute();
				loading.show();
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
			}

			protected String[] doInBackground(Integer... params) {
				db = openDatabase(path);
				return null;
			}

			protected void onPostExecute(String[] result) {
				((MyApplication) getApplication()).setLoad(1);
				loading.dismiss();
				if (ServiceStart)
					startService();
				super.onPostExecute(result);
			}
		}.execute(0);

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("ServiceStart", ServiceStart);
		Log.d("ServiceStart", ServiceStart + "");
		editor.putBoolean("LoadFinish", LoadFinish);
		Log.d("LoadFinish", LoadFinish + "");
		editor.putBoolean("ShowFloat", ShowFloat);
		Log.d("ShowFloat", ShowFloat + "");
		editor.commit();
		super.onBackPressed();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.call_show, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public void startService() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(CallShow.this, LocWindow.class);
		startService(intent);
		Log.d("service","start");
	}

	public void stopService() {
		Intent intent = new Intent(CallShow.this, LocWindow.class);
		stopService(intent);
		Log.d("service","stop");
	}

	// public class CustomPhoneCallListener extends PhoneStateListener {
	// @Override
	// public void onCallStateChanged(int state, String incomingNumber) {
	// ((MyApplication) getApplication()).setPhoneNum(incomingNumber);
	// switch (state) {
	// // 挂断电话
	// case TelephonyManager.CALL_STATE_IDLE:
	// if (((MyApplication) getApplication()).getFlag())
	// stopService();
	// break;
	// case TelephonyManager.CALL_STATE_OFFHOOK:
	// break;
	// // 当电话呼入时
	// case TelephonyManager.CALL_STATE_RINGING:
	// new AsyncTask<Integer, Integer, String[]>() {
	//
	// protected void onPreExecute() {
	// super.onPreExecute();
	// }
	//
	// @Override
	// protected void onCancelled() {
	// super.onCancelled();
	// }
	//
	// protected String[] doInBackground(Integer... params) {
	// Search(((MyApplication) getApplication()).getPhoneNum()
	// .substring(0, 7));
	// return null;
	// }
	//
	// protected void onPostExecute(String[] result) {
	// if (((MyApplication) getApplication()).getFlag()) {
	// startService();
	// }
	// super.onPostExecute(result);
	// }
	// }.execute(0);
	// break;
	// }
	// super.onCallStateChanged(state, incomingNumber);
	// }
	// }

	private SQLiteDatabase openDatabase(String dbfile) {
		try {
			if (!(new File(dbfile).exists())) {
				// 判断数据库文件是否存在，若不存在则执行导入，否则直接打开数据库
				InputStream is = CallShow.this.getResources().openRawResource(
						R.raw.info); // 欲导入的数据库
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
		}
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.LocBtn:
			String phonenum = search_et.getText().toString().trim();
			((MyApplication) getApplication()).setPhoneNum(phonenum);
			if (!(new File(path).exists())) {
				Toast.makeText(getApplicationContext(), "请先导入数据库",
						Toast.LENGTH_SHORT).show();
			} else if (phonenum.length() < 7) {
				Toast.makeText(getApplicationContext(), "请输入完整的电话号码",
						Toast.LENGTH_SHORT).show();
			} else {
				new AsyncTask<Integer, Integer, String[]>() {

					protected void onPreExecute() {
						super.onPreExecute();
						loading.show();
					}

					@Override
					protected void onCancelled() {
						super.onCancelled();
					}

					protected String[] doInBackground(Integer... params) {
						Search(((MyApplication) getApplication()).getPhoneNum()
								.substring(0, 7));
						return null;
					}

					protected void onPostExecute(String[] result) {
						Loc_tv.setText("查询归属地: "
								+ ((MyApplication) getApplication())
										.getLocation());
						loading.dismiss();
						super.onPostExecute(result);
					}
				}.execute(0);
			}

			break;
		case R.id.LoadBtn:

			new AsyncTask<Integer, Integer, String[]>() {

				protected void onPreExecute() {
					super.onPreExecute();
					loading.show();
				}

				@Override
				protected void onCancelled() {
					super.onCancelled();
				}

				protected String[] doInBackground(Integer... params) {
					db = openDatabase("/data"
							+ Environment.getDataDirectory().getAbsolutePath()
							+ "/com.neo.callshow/info.db");
					return null;
				}

				protected void onPostExecute(String[] result) {
					super.onPostExecute(result);
					loading.dismiss();
				}
			}.execute(0);

			break;
		default:
			break;
		}
	}

}
