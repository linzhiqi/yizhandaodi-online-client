package com.techzhiqi.quiz.yizhandaodi;

import java.util.List;

import com.techzhiqi.quiz.yizhandaodi.db.DBHelper;
import com.techzhiqi.quiz.yizhandaodi.quiz.Constants;
import com.techzhiqi.quiz.yizhandaodi.quiz.GamePlay;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class LocalPortalActivity extends Activity implements OnClickListener {
	private ProgressDialog progressdialog;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_local_portal);
		context = this;
		Button episodePlayBtn = (Button) findViewById(R.id.local_portal_episodeBtn);
		episodePlayBtn.setOnClickListener(this);
		Button randomPlayBtn = (Button) findViewById(R.id.randomPlayBtn);
		randomPlayBtn.setOnClickListener(this);
		Button diffSettingsBtn = (Button) findViewById(R.id.diffsettingBtn);
		diffSettingsBtn.setOnClickListener(this);
		Button catSettingBtn = (Button) findViewById(R.id.catsettingBtn);
		catSettingBtn.setOnClickListener(this);
		Button backBtn = (Button) findViewById(R.id.local_portal_backBtn);
		backBtn.setOnClickListener(this);
	}

	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.local_portal_episodeBtn: {
			AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
				String[] epArray = null;
				@Override
				protected void onPreExecute() {
					progressdialog = new ProgressDialog(context);
					progressdialog.setTitle("载入题库列表...");
					progressdialog.setMessage("请等待...");
					progressdialog.setCancelable(false);
					progressdialog.setIndeterminate(true);
					progressdialog.show();
				}

				@Override
				protected Void doInBackground(Void... arg0) {
					DBHelper mainDb = new DBHelper(LocalPortalActivity.this,"merged.db");
					mainDb.createDataBase();
					mainDb.openDataBase();
					List<String> list = mainDb.getEpisodes();
					mainDb.close();
					epArray = list.toArray(new String[list.size()]);
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					
					if(epArray==null || epArray.length == 0){
						LocalPortalActivity.this.runOnUiThread(new Runnable() {

							public void run() {
								progressdialog.setTitle("本地没有任何一期的题库，请首先更新题库");
							}
						});
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					progressdialog.dismiss();
					
					Intent i = new Intent(LocalPortalActivity.this,
							EpisodesActivity.class);
					i.putExtra(EpisodesActivity.EP_ARRAY_TAG, epArray);
					startActivity(i);
				}
			};

			task.execute((Void[]) null);

			break;
		}
		case R.id.randomPlayBtn: {
			AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

				@Override
				protected void onPreExecute() {
					progressdialog = ProgressDialog.show(context, "",
							"载入题库...", true);
				}

				@Override
				protected Void doInBackground(Void... arg0) {
					GamePlay c = new GamePlay();
					c.initGame(getApplicationContext());
					// TODO: get user options: diff, cat, tpc, isRandom,
					// startQID

					((QuizApplication) getApplication()).setCurrentGame(c);
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					progressdialog.dismiss();
					Intent i = new Intent(LocalPortalActivity.this,
							QuestionActivity.class);
					startActivityForResult(i, Constants.PLAYBUTTON);
				}
			};

			task.execute((Void[]) null);

			break;
		}

		case R.id.diffsettingBtn: {
			Intent i = new Intent(this, DiffSetting.class);
			startActivityForResult(i, Constants.DIFFSETTINGBUTTON);
			break;
		}

		case R.id.catsettingBtn: {
			Intent i = new Intent(this, CatSetting.class);
			startActivityForResult(i, Constants.CATSETTINGBUTTON);
			break;
		}
		case R.id.local_portal_backBtn: {
			finish();
			break;
		}
		}

	}

}
