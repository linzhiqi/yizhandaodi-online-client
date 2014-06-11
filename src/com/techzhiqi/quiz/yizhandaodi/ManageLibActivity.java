package com.techzhiqi.quiz.yizhandaodi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.techzhiqi.quiz.yizhandaodi.db.DBHelper;
import com.techzhiqi.quiz.yizhandaodi.quiz.GamePlay;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Build;

public class ManageLibActivity extends Activity implements OnClickListener {
	public final static String MISSING_LIST_TAG = "com.techzhiqi.quiz.yizhandaodi.missinglist";
	public List<String> missingEpList;
	ProgressDialog progressdialog;
	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_lib);
		context = this;
		Intent intent = getIntent();

		if (intent.getCharSequenceExtra(MISSING_LIST_TAG) == null) {
			finish();
		}
		String missingEpStr = intent.getCharSequenceExtra(MISSING_LIST_TAG)
				.toString();
		String[] missingEpArray = missingEpStr.split(",");
		missingEpList = Arrays.asList(missingEpArray);
		
		Button backBtn = (Button) findViewById(R.id.manage_lib_backBtn);
		backBtn.setOnClickListener(this);
		Button downloadBtn = (Button) findViewById(R.id.manage_lib_downloadBtn);
		downloadBtn.setOnClickListener(this);
		TextView missingEpisodesView = (TextView) findViewById(R.id.manage_lib_missingEpisodes);
		missingEpisodesView.setText(missingEpStr);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.manage_lib_backBtn: {
			finish();
			break;
		}
		case R.id.manage_lib_downloadBtn: {
			downloadEps();
			break;
		}
		}
	}

	private void downloadEps() {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			String missingList = null;

			@Override
			protected void onPreExecute() {
				progressdialog = new ProgressDialog(context);
				progressdialog.setTitle("题库更新中...");
				progressdialog.setMessage("请等待...");
				progressdialog.setCancelable(false);
				progressdialog.setIndeterminate(true);
				progressdialog.show();
			}

			@Override
			protected Void doInBackground(Void... arg0) {
				for (String ep : missingEpList) {

					final String str = String.copyValueOf(ep.toCharArray());
					ManageLibActivity.this.runOnUiThread(new Runnable() {

						public void run() {
							progressdialog.setMessage("下载" + str + "期题目");
						}
					});

					if (downloadEp(ep + ".db")) {
						ManageLibActivity.this.runOnUiThread(new Runnable() {

							public void run() {
								progressdialog.setMessage("下载" + str + "期题目成功");
							}
						});
						
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						ManageLibActivity.this.runOnUiThread(new Runnable() {

							public void run() {
								progressdialog.setMessage("正在注入" + str + "期题目");
							}
						});

						if (mergeEp(ep + ".db")) {
							ManageLibActivity.this
									.runOnUiThread(new Runnable() {
										public void run() {
											progressdialog.setMessage(str
													+ "期题目注入数据库成功");
										}
									});

						} else {
							ManageLibActivity.this
									.runOnUiThread(new Runnable() {
										public void run() {
											progressdialog.setMessage(str
													+ "期题目注入数据库失败");
										}
									});
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} else {
						ManageLibActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								progressdialog.setMessage("下载" + str + "期题目失败");
							}
						});

					}
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				ManageLibActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						progressdialog.setTitle("题库更新完成");
						progressdialog.setMessage("");
					}
				});
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}

			private boolean mergeEp(String ep) {
				String fileName = "/data/data/com.techzhiqi.quiz.yizhandaodi/databases/"
						+ ep;
				File f = new File(fileName);
				if (!f.exists()) {
					Log.i("yzdd-mergeDb", fileName + " doesn't exist");
					return false;
				}

				Log.i("yzdd-mergeDb", fileName + " exists");
				DBHelper epDb = new DBHelper(ManageLibActivity.this, ep);
				
				epDb.createDataBase();
				try {
					epDb.openDataBase();
				} catch (SQLException sqle) {
					throw sqle;
				}

				List<ContentValues> rows = (List<ContentValues>) epDb.select();
				Log.i("yzdd-mergeDb", rows.size() + "rows got from " + ep);
				epDb.close();
				
				boolean failed = false;
				DBHelper mainDb = new DBHelper(ManageLibActivity.this,
						GamePlay.MAIN_DB);
				mainDb.createDataBase();
				try {
					mainDb.openDataBase();
				} catch (SQLException sqle) {
					throw sqle;
				}
				//mainDb.getSQLiteDatabase().beginTransaction();
				for(ContentValues row : rows){
					long idInserted = mainDb.insert(row);
					Log.i("yzdd-mergeDb", "id:" + idInserted + " is inserted");
					if (idInserted == -1) {
						failed = true;
					}

				}
				mainDb.close();
				
				/*if (!failed) {
					mainDb.getSQLiteDatabase().setTransactionSuccessful();
				}
				mainDb.getSQLiteDatabase().endTransaction();*/

				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				 f.delete(); 
				 Log.i("yzdd-mergeDb",fileName + " deleted");

				if (!failed) {
					return true;
				} else {
					return false;
				}
			}

			@SuppressLint("NewApi")
			private boolean downloadEp(String ep) {
				InputStream is = null;
				FileOutputStream out = null;
				Log.i("yzdd-getEpisodeDB", "");
				String outFileName = "/data/data/com.techzhiqi.quiz.yizhandaodi/databases/"
						+ ep;
				try {
					out = new FileOutputStream(outFileName);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return false;
				}
				String encoding = Base64.encodeToString(
						"user:pass".getBytes(), Base64.NO_WRAP);
				try {
					String productionUrl = "http://chinesemonster.serveblog.net:8080/yizhandaodiDB/getMissingDb?dbname="
							+ ep;
					URL url = new URL(
							"http://ec2-54-238-174-249.ap-northeast-1.compute.amazonaws.com:8080/yizhandaodiDB/getMissingDb?dbname="
									+ ep);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setReadTimeout(60000 /* milliseconds */);
					conn.setConnectTimeout(20000 /* milliseconds */);
					conn.setRequestProperty("Authorization", "Basic "
							+ encoding);
					conn.setRequestMethod("GET");
					conn.setDoInput(true);
					// Starts the query
					conn.connect();
					int response = conn.getResponseCode();
					Log.d("yizhandaodi-getMissingDb", "The response is: "
							+ response);
					if (response == 200) {
						is = conn.getInputStream();
						byte[] buffer = new byte[2048];
						int bytesRead;
						while ((bytesRead = is.read(buffer)) != -1) {
							out.write(buffer, 0, bytesRead);
						}
						out.flush();
						return true;
					} else {
						return false;
					}

				} catch (IOException e) {
					Log.e("yizhandaodi-getMissingDb", "io exception", e);
					File f = new File(outFileName);
					if (f.exists()) {
						f.delete();
					}
					return false;
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			}

			@Override
			protected void onPostExecute(Void result) {
				if (progressdialog != null) {
					progressdialog.dismiss();
				}
				finish();
			}
		};
		task.execute((Void[]) null);
	}
}
