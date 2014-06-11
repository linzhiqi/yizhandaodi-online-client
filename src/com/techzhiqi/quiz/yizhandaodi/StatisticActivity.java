package com.techzhiqi.quiz.yizhandaodi;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.techzhiqi.quiz.yizhandaodi.quiz.GamePlay;


public class StatisticActivity extends Activity implements OnClickListener {
	private long mLastPress = -1;
	private static final long BACK_THRESHOLD = DateUtils.SECOND_IN_MILLIS / 2;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistic);
		GamePlay currentGame = ((QuizApplication) getApplication())
				.getCurrentGame();

		TextView results = (TextView) findViewById(R.id.statistics);
		List<String> records = currentGame.getRecords();
		results.setText(getResult(records, currentGame.getRound(),
				currentGame.getRight()));

		// handle button actions
		Button finishBtn = (Button) findViewById(R.id.finishBtn);
		finishBtn.setOnClickListener(this);
	}
	
	private String getResult(List<String> records, int round, int right){
		DecimalFormat df = new DecimalFormat("###.##");
		double rate = (double) (right*100/round);
		StringBuffer result = new StringBuffer().append("您这一轮一共答题: "+Integer.toString(round)+"道\n答对: "+Integer.toString(right)+"道\n正确率: "+ df.format(rate) +"%\n");
		Iterator<String> it=records.iterator();
		while(it.hasNext()){
			result.append(it.next()+"\n");
		}
		return result.toString();
	}


	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.finishBtn:
			finish();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Compare against last pressed time, and if user hit multiple times
		// in quick succession, we should consider bailing out early.
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			long currentPress = SystemClock.uptimeMillis();
			if (currentPress - mLastPress < BACK_THRESHOLD) {
				return super.onKeyDown(keyCode, event);
			}
			mLastPress = currentPress;
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}