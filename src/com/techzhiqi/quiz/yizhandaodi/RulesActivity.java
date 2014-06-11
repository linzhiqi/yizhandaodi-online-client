/**
 * 
 */
package com.techzhiqi.quiz.yizhandaodi;

//import com.tmm.android.chuck.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * @author zhiqi.lin
 * 
 */
public class RulesActivity extends Activity implements OnClickListener {

	AlertDialog alertDialog;
	int timertime;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rules);

		// finish button
		Button backBtn = (Button) findViewById(R.id.backBtn);
		Button settingBtn = (Button) findViewById(R.id.timersettingBtn);
		backBtn.setOnClickListener(this);
		settingBtn.setOnClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.timersettingBtn: {
			
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View layout = inflater.inflate(R.layout.timersetting,	(ViewGroup) findViewById(R.id.timersettingslayout));
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setView(inflater.inflate(R.layout.timersetting, null))
					.setTitle("设定答题时间")
					.setPositiveButton("保存设置", new DialogInterface.OnClickListener() {
			               public void onClick(DialogInterface dialog, int id) {
			            	   SharedPreferences settings = getSharedPreferences("TIMERTIME",0);
			            	   SharedPreferences.Editor editor = settings.edit();
			            	   editor.putInt("timertime", RulesActivity.this.timertime*1000);
			            	   editor.commit();
			            	   dialog.cancel();
			               }
			           })
			           .setNegativeButton("取消", new DialogInterface.OnClickListener() {
			               public void onClick(DialogInterface dialog, int id) {
			                   dialog.cancel();
			               }
			           });
			alertDialog = builder.create();
			alertDialog.show();
			
			
			SeekBar sb = (SeekBar) alertDialog.findViewById(R.id.seekBar1);
			
			sb.setMax(60);
			sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					// Do something here with new value
					TextView t = (TextView)alertDialog.findViewById(R.id.textView1);
					RulesActivity.this.timertime = progress + 20;
					t.setText(progress+20 + " 秒");
				}

				public void onStartTrackingTouch(SeekBar arg0) {
					// TODO Auto-generated method stub

				}

				public void onStopTrackingTouch(SeekBar arg0) {
					// TODO Auto-generated method stub

				}
			});
			
			
			break;
		}
		case R.id.backBtn: {
			finish();
		}
		}
		/**
		 * if the back button is clicked then go back to the main menu
		 */

	}

}
