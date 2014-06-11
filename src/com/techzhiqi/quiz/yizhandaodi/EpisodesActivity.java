package com.techzhiqi.quiz.yizhandaodi;

import java.io.IOException;
import java.net.UnknownHostException;

import protocol.ClientRegistrationSignal;
import protocol.ServerRegistrationSignal;

import com.techzhiqi.quiz.yizhandaodi.PkWelcomeActivity.RegistrationError;
import com.techzhiqi.quiz.yizhandaodi.network.NetworkManager;
import com.techzhiqi.quiz.yizhandaodi.pk.GameManager;
import com.techzhiqi.quiz.yizhandaodi.quiz.Constants;
import com.techzhiqi.quiz.yizhandaodi.quiz.GamePlay;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;

public class EpisodesActivity extends Activity implements OnClickListener {
	public final static String EP_ARRAY_TAG = "com.techzhiqi.quiz.yizhandaodi.eparray";
	private ProgressDialog progressdialog;
	private Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_episodes);
		Button backBtn = (Button) findViewById(R.id.episodes_backBtn);
		backBtn.setOnClickListener(this);
		context = this;
		Intent i = getIntent();
		final String[] epArray = i.getStringArrayExtra(EP_ARRAY_TAG);
		ListView epListView = (ListView) findViewById(R.id.eplist);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.list_content,  epArray);
		epListView.setAdapter(adapter);
		
		epListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(EpisodesActivity.this);
				final String epName = String.copyValueOf(epArray[position].toCharArray());
				builder.setMessage("开始答题?"+ epName)
				       .setCancelable(false)
				       .setPositiveButton("是", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	  /* Intent i;
				        	   
				        	   progressdialog = ProgressDialog.show(EpisodesActivity.this, "", 
						                "载入题库...", true);
								Thread workt=new Thread(new Runnable()
					            {
					                public void run()
					                {
					                	//Initialise Game with retrieved question set ///
					        			GamePlay c = new GamePlay();
					        			c.initGame(getApplicationContext());
					        			c.setCat(cat_id);
					        			((QuizApplication)getApplication()).setCurrentGame(c);  
					        			progressdialog.dismiss();
					                }
					            });
				        	   workt.start();
				        	   try{
				        		   workt.join();
				        	   }catch(InterruptedException e){
				   				Log.e("yizhandaodi", "splash playbtn", e);
				   				}
				        	  
								i = new Intent(EpisodesActivity.this,QuestionActivity.class);
								startActivityForResult(i, Constants.CATSETTINGBUTTON);
				           }*/
				        	   
				        	   AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
				 
				       			@Override
				       			protected void onPreExecute() {
				       				
				       				progressdialog = new ProgressDialog(context);
				       				progressdialog.setTitle("载入题库...");
				       				progressdialog.setMessage("请等待...");
				       				progressdialog.setCancelable(false);
				       				progressdialog.setIndeterminate(true);
				       				progressdialog.show();
				       			}
				       			
				       			@Override
				       			protected Void doInBackground(Void... arg0) {
				       				GamePlay c = new GamePlay();
				        			c.initGame(getApplicationContext());
				        			c.setEpisode(epName);
				        			((QuizApplication)getApplication()).setCurrentGame(c);  
				       				return null;
				       			}
				       			
				       			@Override
				       			protected void onPostExecute(Void result) {
				       				if (progressdialog!=null) {
				       					progressdialog.dismiss();
				       				}
				       				Intent i = new Intent(EpisodesActivity.this,QuestionActivity.class);
									startActivity(i);
				       			}
				       		};
				       		task.execute((Void[])null);
				           }
				       })
				       .setNegativeButton("否", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
	}
	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()){
		case R.id.episodes_backBtn:{
			finish();
		}
		}
		
	}

	

}
