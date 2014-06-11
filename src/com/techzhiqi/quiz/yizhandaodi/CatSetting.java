package com.techzhiqi.quiz.yizhandaodi;

import com.techzhiqi.quiz.yizhandaodi.quiz.Constants;
import com.techzhiqi.quiz.yizhandaodi.quiz.GamePlay;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class CatSetting extends Activity implements OnClickListener {
	private int cat_id = -1;
	static final String[] CATEGORIES = new String[] { "综合常识", "体育运动", "科普科学",
			"流行娱乐", "历史", "地理", "语言文学", "艺术" };
	private ProgressDialog progressdialog;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cat_setting);
		ListView listView = (ListView) findViewById(R.id.catlist);
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.activity_cat_setting, R.id.catlist, CATEGORIES);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.list_content,  CATEGORIES);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				cat_id = position+1;
				
				AlertDialog.Builder builder = new AlertDialog.Builder(CatSetting.this);
				builder.setMessage("开始答题?")
				       .setCancelable(false)
				       .setPositiveButton("是", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   Intent i;
				        	   
				        	   progressdialog = ProgressDialog.show(CatSetting.this, "", 
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
				        	   /*
				        	    * 
				        	    
								// Initialise Game with retrieved question set 
								GamePlay c = new GamePlay();
								c.initGame(getApplicationContext());
								c.getQManager().setCat(cat_id);
								// TODO: get user options: diff, cat, tpc, isRandom, startQID

								((QuizApplication) getApplication()).setCurrentGame(c);
								*/
								// Start Game Now.. //
								i = new Intent(CatSetting.this,QuestionActivity.class);
								startActivityForResult(i, Constants.CATSETTINGBUTTON);
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
		Button backBtn = (Button) findViewById(R.id.backBtn);
		backBtn.setOnClickListener(this);
	}

	public void onClick(View v) {
		/**
		 * check which settings set and return to menu
		 */
		switch (v.getId()) {
		case R.id.backBtn:
			finish();
			break;
		default:
			finish();
		}
	}
}
