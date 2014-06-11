/**
 * 
 */
package com.techzhiqi.quiz.yizhandaodi;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.techzhiqi.quiz.yizhandaodi.quiz.Constants;
import com.techzhiqi.quiz.yizhandaodi.quiz.GamePlay;




/**
 * @author zhiqi.lin
 * 
 */

public class QuestionActivity extends Activity implements OnClickListener {

	private static final long BACK_THRESHOLD = DateUtils.SECOND_IN_MILLIS / 2;
	static final String STATE_RESUMED = "willberesumed";
	static final String TIMER_STATE = "timestate";

	private String currentQ;
	private GamePlay currentGame;
	private long mLastPress = -1;
	private int mIsResumed = 0;
	CountDownTimer myTimer;
	private long myTime = 10000;
	TextView mTv = null;

	@TargetApi(3)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		/**
		 * Configure current game and get question
		 */
		currentGame = ((QuizApplication) getApplication()).getCurrentGame();

		mTv = (TextView) findViewById(R.id.timer);
		SharedPreferences settings = getSharedPreferences("TIMERTIME", 0);
		myTime = settings.getInt("timertime", 40000);
		//mTv.setText("test");
		Button nextBtn = (Button) findViewById(R.id.submitBtn);
		Button passBtn = (Button) findViewById(R.id.passBtn);
		passBtn.setOnClickListener(this);
		nextBtn.setOnClickListener(this);

		/**
		 * Update the question and answer options..
		 */
		
		if (savedInstanceState != null) {
			// Restore value of members from saved state
			mIsResumed = savedInstanceState.getInt(STATE_RESUMED);
			if (mIsResumed == 1) {
				currentQ = currentGame.getCurrentQ();
			}
			myTime = savedInstanceState.getLong(TIMER_STATE);
			myTimer = (CountDownTimer) getLastNonConfigurationInstance();
			Log.i("yizhandaodi", "if (myTimer==null): "+ (myTimer==null));
			myTimer.cancel();
			
		} else {
			currentQ = currentGame.getNextQ(this, false);
			currentGame.setRound(currentGame.getRound() + 1);
			
		}
		setQuestion();
		myTimer = new CountDownTimer(myTime, 100) {

			@Override
			public void onTick(long millisUntilFinished) {
				mTv.setText("剩余时间: " + millisUntilFinished / 1000 + "."
						+ (millisUntilFinished % 1000) / 100+"秒");
				myTime = millisUntilFinished;
			}

			@Override
			public void onFinish() {
				Log.i("yizhandaodi", "onFinish() is called,myTime="
						+ myTime);
				if (myTime / 1000 == 0) {
					mTv.setText("时间到!");
					Intent i = new Intent(QuestionActivity.this,
							AnswersActivity.class);
					String answer = getSelectedAnswer();
					if (answer == null)
						answer = "§";
					i.putExtra(Constants.EXTRA_MESSAGE, answer);
					startActivity(i);
					finish();

				}
			}
		}.start();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return myTimer;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save the user's current game state
		mIsResumed = 1;
		savedInstanceState.putInt(STATE_RESUMED, mIsResumed);
		savedInstanceState.putLong(TIMER_STATE, myTime);
		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}

	/**
	 * Method to set the text for the question and answers from the current
	 * games current question
	 */
	private void setQuestion() {
		// set the question text from current question
		TextView qText = (TextView) findViewById(R.id.question);
		qText.setText(currentQ);

	}

	public void onClick(View arg0) {
		switch(arg0.getId()){
		
		case R.id.submitBtn:
			String answer = getSelectedAnswer();
			if (answer == null)
				answer = "§";
			if (currentGame.checkAnswer(answer)) {
				myTimer.cancel();
				Intent i = new Intent(QuestionActivity.this, AnswersActivity.class);
				i.putExtra(Constants.EXTRA_MESSAGE, answer);
				startActivity(i);
				finish();
			} else {
				TextView checkText = (TextView) findViewById(R.id.checkresult);
				EditText inputText = (EditText) findViewById(R.id.input_answer);
				inputText.setText("");
				checkText.setVisibility(View.VISIBLE);
				checkText.setText("回答:\"" + answer + "\"错误， 请尝试其他答案");
				// LayoutParams params= new
				// RelativeLayout.LayoutParams(RelativeLayout.BELOW,checkText.getId());
				// eText.setLayoutParams(params);
				ViewGroup vg = (ViewGroup) findViewById(R.id.ViewGroup_question);
				vg.invalidate();
			}
			break;
		case R.id.passBtn:
			myTimer.cancel();
			Intent i = new Intent(QuestionActivity.this, AnswersActivity.class);
			i.putExtra(Constants.EXTRA_MESSAGE, "");
			startActivity(i);
			finish();
		
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 * 
	 * This method is to override the back button on the phone to prevent users
	 * from navigating back in to the quiz
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Compare against last pressed time, and if user hit multiple times
		// in quick succession, we should consider bailing out early.
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			long currentPress = SystemClock.uptimeMillis();
			if (currentPress - mLastPress < BACK_THRESHOLD) {
				myTimer.cancel();
				return super.onKeyDown(keyCode, event);
			}
			mLastPress = currentPress;
			Toast toast = Toast.makeText(getApplicationContext(), "双击退出键退出答题", Toast.LENGTH_SHORT);
			toast.show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private String getSelectedAnswer() {
		EditText editAnswer = (EditText) findViewById(R.id.input_answer);
		return editAnswer.getText().toString();
	}

	
	/**
	 * Method to return the difficulty settings
	 * 
	 * @return
	 * 
	 *         private int getDifficultySettings() { SharedPreferences settings
	 *         = getSharedPreferences(Constants.SETTINGS, 0); int diff =
	 *         settings.getInt(Constants.DIFFICULTY, Constants.MEDIUM); return
	 *         diff; }
	 */
}
