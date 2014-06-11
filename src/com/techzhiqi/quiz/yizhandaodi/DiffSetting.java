package com.techzhiqi.quiz.yizhandaodi;

import com.techzhiqi.quiz.yizhandaodi.quiz.Constants;
import com.techzhiqi.quiz.yizhandaodi.quiz.GamePlay;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.support.v4.app.NavUtils;

public class DiffSetting extends Activity implements OnClickListener{
	
	private ProgressDialog progressdialog;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diffsetting);
        
        /**
		 * set listener on update button
		 */
		Button playBtn = (Button) findViewById(R.id.playBtn);
		Button backBtn = (Button) findViewById(R.id.backBtn);
		playBtn.setOnClickListener(this);
		backBtn.setOnClickListener(this);
    }
    
    public void onClick(View v) {
    	/**
		 * check which settings set and return to menu
		 */
    	switch (v.getId()){
		case R.id.playBtn :
			if (!checkSelected()) {
				return;
			} else {
				Intent i;
				progressdialog = ProgressDialog.show(this, "", 
		                "载入题库...", true);
				Thread workt=new Thread(new Runnable()
	            {
	                public void run()
	                {
	                	//Initialise Game with retrieved question set ///
	        			GamePlay c = new GamePlay();
	        			c.initGame(getApplicationContext());
	        			c.setDiff(getSelectedSetting());
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
				//Initialise Game with retrieved question set ///
				/*
				 * 
				 
				GamePlay c = new GamePlay();
				c.initGame(this);
				c.getQManager().setDiff(getSelectedSetting());
				//TODO: get user options: diff, cat, tpc, isRandom, startQID
				
				((QuizApplication)getApplication()).setCurrentGame(c);  
				*/
				//Start Game Now.. //
				i = new Intent(this, QuestionActivity.class);
				startActivityForResult(i, Constants.DIFFSETTINGBUTTON);	
			}
			break;
		default:
			finish();
    	}
			
    }
    
    /**
	 * Method to check that a checkbox is selected
	 * 
	 * @return boolean
	 */
	private boolean checkSelected() {
		RadioButton c1 = (RadioButton) findViewById(R.id.easySetting);
		RadioButton c2 = (RadioButton) findViewById(R.id.mediumSetting);
		RadioButton c3 = (RadioButton) findViewById(R.id.hardSetting);
		return (c1.isChecked() || c2.isChecked() || c3.isChecked());
	}
	
	/**
	 * Get the selected setting
	 */
	private int getSelectedSetting() {
		RadioButton c1 = (RadioButton) findViewById(R.id.easySetting);
		RadioButton c2 = (RadioButton) findViewById(R.id.mediumSetting);
		if (c1.isChecked()) {
			return Constants.Difficulty_EASY;
		}
		if (c2.isChecked()) {
			return Constants.Difficulty_MEDIUM;
		}

		return Constants.Difficulty_EXTREME;
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.diffsetting, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
