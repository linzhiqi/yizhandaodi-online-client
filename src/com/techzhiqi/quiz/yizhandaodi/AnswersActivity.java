/**
 * 
 */
package com.techzhiqi.quiz.yizhandaodi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.techzhiqi.quiz.yizhandaodi.quiz.Constants;
import com.techzhiqi.quiz.yizhandaodi.quiz.GamePlay;
import com.techzhiqi.quiz.yizhandaodi.quiz.QuestionManager;

/**
 * @author zhiqi.lin
 * 
 */
public class AnswersActivity extends Activity implements OnClickListener {
	private  GamePlay currentGame = null;
	private String currentQ = null;
	private String inputAnswer = null;
	private String currentA = null;
	private boolean isCorrect = true;
	private int mIsResumed = 0;
	static final String STATE_RESUMED = "willberesumed";
	/**
	 * Keep track of last time user tapped "back" hard key. When pressed more
	 * than once within {@link #BACK_THRESHOLD}, we treat let the back key fall
	 * through and close the app.
	 */
	private long mLastPress = -1;
	private String logTAG="yizhandaodi";
	private static final long BACK_THRESHOLD = DateUtils.SECOND_IN_MILLIS / 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		setContentView(R.layout.answers);

		inputAnswer = intent.getStringExtra(Constants.EXTRA_MESSAGE);
		this.currentGame = ((QuizApplication) getApplication())
				.getCurrentGame();
		
		currentQ = currentGame.getCurrentQ();
		Log.i(logTAG, "answerActivity getcurrentQ: if(currentQ==null)"+ (currentQ==null)); 
		currentA = currentGame.getCurrentA();
		// handle button actions
		Button nextRoundBtn = (Button) findViewById(R.id.nextRoundBtn);
		Button endGameBtn = (Button) findViewById(R.id.endGame);
		nextRoundBtn.setOnClickListener(this);
		endGameBtn.setOnClickListener(this);
		isCorrect = setAnswerCheck(inputAnswer);
		setQuestion();
		setAnswer(currentA);
		if (savedInstanceState != null) {

		} else {
			addRecordToGame(isCorrect, currentGame, currentQ, inputAnswer,
					currentA);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save the user's current game state
		mIsResumed = 1;
		savedInstanceState.putInt(STATE_RESUMED, mIsResumed);

		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}

	private void addRecordToGame(boolean isCorrect, GamePlay currentGame,
			String question, String inputAnswer, String stdAnswer) {
		if (isCorrect) {
			currentGame.addRecord(Integer.toString(currentGame.getRound())
					+ ". " + question + "\n 您的回答: " + inputAnswer
					+ "(正确)\n 标准答案:" + stdAnswer);
			currentGame.setRight(currentGame.getRight() + 1);
		} else {
			currentGame.addRecord(Integer.toString(currentGame.getRound())
					+ ". " + question + "\n 您的回答: " + inputAnswer
					+ "(错误)\n 标准答案:" + stdAnswer);
			currentGame.setWrong(currentGame.getWrong() + 1);
		}
	}

	private boolean setAnswerCheck(String input) {
		String showResult = null;
		TextView qText = (TextView) findViewById(R.id.answers_check);
		if (this.currentGame.checkAnswer(input)) {
			showResult = "回答正确!";
			qText.setTextColor(-16711936);
			qText.setHighlightColor(16711936);
			qText.setText(showResult);
			return true;
		} else {
			showResult = "回答错误!";
			qText.setTextColor(-65536);
			qText.setHighlightColor(-65536);
			qText.setText(showResult);
			return false;
		}
	}


	private void setQuestion() {
		// set the question text from current question
		TextView qText = (TextView) findViewById(R.id.answers_question);
		qText.setText(this.currentQ);

	}

	private void setAnswer(String showAnswer) {
		String answer = "答案: " + showAnswer;
		TextView aText = (TextView) findViewById(R.id.answers_answer);
		aText.setText(answer);
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
				return super.onKeyDown(keyCode, event);
			}
			mLastPress = currentPress;
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nextRoundBtn: {
			Intent i = new Intent(this, QuestionActivity.class);
			startActivity(i);
			finish();
			break;
		}
		case R.id.endGame: {
			Intent i = new Intent(this, StatisticActivity.class);
			startActivity(i);
			finish();
			break;
		}
		}

	}

}
