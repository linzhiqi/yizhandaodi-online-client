package com.techzhiqi.quiz.yizhandaodi;

import java.io.BufferedInputStream;

import com.google.ads.AdRequest.ErrorCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import protocol.ClientRegistrationSignal;
import protocol.ServerRegistrationSignal;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.techzhiqi.quiz.yizhandaodi.PkWelcomeActivity.RegistrationError;
import com.techzhiqi.quiz.yizhandaodi.db.DBHelper;
import com.techzhiqi.quiz.yizhandaodi.network.NetworkManager;
import com.techzhiqi.quiz.yizhandaodi.pk.GameManager;
import com.techzhiqi.quiz.yizhandaodi.quiz.Constants;
import com.techzhiqi.quiz.yizhandaodi.quiz.GamePlay;
import com.techzhiqi.quiz.yizhandaodi.util.DbDownloader;
import com.techzhiqi.quiz.yizhandaodi.util.Flags;

public class SplashActivity extends Activity implements OnClickListener {

	private ProgressDialog progressdialog;
	private Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		context = this;

		// ////////////////////////////////////////////////////////////////////
		// ////// GAME MENU /////////////////////////////////////////////////
		Button playPortalBtn = (Button) findViewById(R.id.playPortalBtn);
		playPortalBtn.setOnClickListener(this);
		Button rulesBtn = (Button) findViewById(R.id.rulesBtn);
		rulesBtn.setOnClickListener(this);
		Button checkUpdateBtn = (Button) findViewById(R.id.check_updateBtn);
		checkUpdateBtn.setOnClickListener(this);
		Button exitBtn = (Button) findViewById(R.id.exitBtn);
		exitBtn.setOnClickListener(this);
	}

	/**
	 * Listener for game menu
	 */
	public void onClick(View v) {
		Intent i = null;

		switch (v.getId()) {
		case R.id.playPortalBtn: {
			i = new Intent(this, LocalPortalActivity.class);
			startActivity(i);
			break;
		}
		case R.id.rulesBtn:
			i = new Intent(this, RulesActivity.class);
			startActivityForResult(i, Constants.RULESBUTTON);
			break;

		case R.id.check_updateBtn: {
			// updateLib();
			if (!hasConnection()) {
				Toast toast = Toast.makeText(getApplicationContext(),
						"请确认数据网络可用", Toast.LENGTH_SHORT);
				toast.show();
				break;
			}
			checkUpdate();
			break;
		}
		case R.id.exitBtn:
			finish();
			break;
		}

	}

	@SuppressLint("NewApi")
	private void checkUpdate() {

		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			String missingList = null;

			@Override
			protected void onPreExecute() {
				progressdialog = new ProgressDialog(context);
				progressdialog.setTitle("服务器检索中...");
				progressdialog.setMessage("请等待...");
				progressdialog.setCancelable(false);
				progressdialog.setIndeterminate(true);
				progressdialog.show();
			}

			@Override
			protected Void doInBackground(Void... arg0) {
				InputStream is = null;
				String epList = listtoString(getEpList());
				Log.i("yzdd-getEpisodeList", epList);
				String encoding = Base64.encodeToString(
						"user:pass".getBytes(), Base64.NO_WRAP);
				try {
					String productionUrl = "http://chinesemonster.serveblog.net:8080/yizhandaodiDB/getMissingList?dblist="
							+ epList;
					URL url = new URL(productionUrl);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setReadTimeout(10000 /* milliseconds */);
					conn.setConnectTimeout(15000 /* milliseconds */);
					conn.setRequestProperty("Authorization", "Basic "
							+ encoding);
					conn.setRequestMethod("GET");
					conn.setDoInput(true);
					// Starts the query
					conn.connect();
					int response = conn.getResponseCode();
					Log.d("yizhandaodi-getMissingList", "The response is: "
							+ response);
					if (response == 200) {
						BufferedReader in = new BufferedReader(
								new InputStreamReader(conn.getInputStream()));
						missingList = in.readLine();

					} else {

					}

				} catch (IOException e) {
					Log.e("yizhandaodi-checkupdate", "io exception", e);
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (progressdialog != null) {
					progressdialog.dismiss();
				}
				if (missingList == null || missingList.length() == 0) {
					if (missingList == null) {
						Log.i("yzdd-checkupdate", "missingList is null");
					} else {
						Log.i("yzdd-checkupdate", "missingList is \"\"");
					}

					Toast toast = Toast.makeText(getApplicationContext(),
							"暂时没有更新呢", Toast.LENGTH_SHORT);
					toast.show();
				} else {
					Intent i = new Intent(SplashActivity.this,
							ManageLibActivity.class);
					i.putExtra(ManageLibActivity.MISSING_LIST_TAG, missingList);
					startActivity(i);
				}

			}
		};
		task.execute((Void[]) null);
	}

	public List<String> getEpList() {
		DBHelper mainDb = new DBHelper(this, "merged.db");
		mainDb.createDataBase();
		mainDb.openDataBase();
		List<String> list = mainDb.getEpisodes();
		mainDb.close();
		return list;
	}

	public String listtoString(List<String> list) {
		StringBuilder builder = new StringBuilder();
		Iterator<String> it = list.iterator();
		while (it.hasNext()) {
			builder.append(it.next());
			if (it.hasNext()) {
				builder.append(',');
			}
		}
		return builder.toString();
	}

	private boolean hasConnection() {

		getApplicationContext();
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		} else {
			return false;
		}

	}

	/*
	 * private void updateLib() {
	 * 
	 * ConnectivityManager connMgr = (ConnectivityManager)
	 * getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo networkInfo =
	 * connMgr.getActiveNetworkInfo(); if (networkInfo != null &&
	 * networkInfo.isConnected()) { flags.network=true; progressdialog =
	 * ProgressDialog.show(this, "", "同步题库...", true); Thread work = new
	 * Thread(new Runnable() {
	 * 
	 * public void run() {
	 * 
	 * try { DbDownloader downloader = new
	 * DbDownloader(SplashActivity.this,flags); if
	 * (downloader.download("http://chinesemonster.serveblog.net:8080/getDb/GetDb"
	 * )) { Log.i("yizhandaodi", "download succeed!"); } else {
	 * Log.i("yizhandaodi", "download failed!"); }
	 * 
	 * } catch (IOException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); flags.download = false; } progressdialog.dismiss();
	 * } }); work.start();
	 * 
	 * try { work.join(); } catch (InterruptedException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 * 
	 * } else { flags.network = false;
	 * 
	 * } }
	 */

}
