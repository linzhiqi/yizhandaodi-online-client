package com.techzhiqi.quiz.yizhandaodi.quiz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import android.content.Context;
import android.database.SQLException;
import android.util.Log;

import com.techzhiqi.quiz.yizhandaodi.db.DBHelper;
import com.techzhiqi.quiz.yizhandaodi.quiz.Constants;

public class QuestionManager {
	private int difficulty = Constants.Difficulty_ALL;
	private int category = Constants.Category_ALL;
	private int topic = Constants.Topic_ALL;
	private String episodeName;
	private Boolean isRandom = true; // false means pick questions with the
										// order
										// of the field "_id"
	private int startQId = 0;
	// private QuestionCache qCache = null;
	private ArrayList<Question> qCache = null;
	private ListIterator<Question> qIt = null;
	private Question currentQ = null;
	private String logTAG = "yizhandaodi";

	public QuestionManager(Integer diff, Integer cat, Integer tpc,
			Boolean isRandom, Integer startQId) {
		setDiff(diff);
		setCat(cat);
		setTopic(tpc);

		if (isRandom != null) {
			this.isRandom = isRandom;
		}

		if ((this.isRandom == false) && (startQId != null)) {
			this.startQId = startQId;
		}

		// qCache = new ArrayList<Question>();
		// qIt = qCache.iterator();
	}

	public void setDiff(Integer diff) {
		if (diff != null) {
			this.difficulty = diff;
		}

	}

	public int getDiff() {
		return this.difficulty;
	}

	public void setCat(Integer cat) {
		if (cat != null) {
			this.category = cat;
		}

	}

	public int getCat() {
		return this.category;
	}

	public void setTopic(Integer tpc) {
		if (tpc != null) {
			this.topic = tpc;
		}

	}

	public int getTopic() {
		return this.topic;
	}

	public String getCurrentQ() {
		return this.getCurrentQObject().getQuestion();
	}

	/*
	 * can return null pointer, in this case the caller should end the game
	 */
	public String getNextQ(Context ct, boolean isDiffChange) {
		Question q = this.getNextQObject(ct, isDiffChange);

		if (q == null) {
			return null;
		} else {
			return q.getQuestion();
		}
	}

	public String getCurrentA() {
		return getAnswerList(this.getCurrentQObject().getAnswer()).get(0);
	}

	/*
	 * can return null pointer, in this case the caller should end the game
	 */
	public String getNextA(Context ct, boolean isDiffChange) {
		Question q = this.getNextQObject(ct, isDiffChange);
		if (q == null) {
			return null;
		} else {
			return getAnswerList(q.getAnswer()).get(0);
		}
	}

	public boolean checkAnswer(String inputA) {
		String input = inputA.trim();
		Question q = this.getCurrentQObject();

		switch (q.getCheckId()) {
		case 0:
			return ContainsCaseInsensitive(getAnswerList(q.getAnswer()), input);
		case 1:
			return ContainsSameCharSetCaseInsensitive(
					getAnswerList(q.getAnswer()), input);
		default:
			return ContainsCaseInsensitive(getAnswerList(q.getAnswer()), input);
		}

	}

	private boolean ContainsCaseInsensitive(List<String> searchList,
			String searchTerm) {
		for (String item : searchList) {
			if ((item.length() == searchTerm.length())
					&& item.equalsIgnoreCase(searchTerm))
				return true;
		}
		return false;
	}

	private boolean ContainsSameCharSetCaseInsensitive(List<String> searchList,
			String searchTerm) {
		for (String item : searchList) {
			if ((item.length() == searchTerm.length())
					&& SameCharSetCaseInsensitive(item, searchTerm))
				return true;
		}
		return false;
	}

	private boolean SameCharSetCaseInsensitive(String a, String b) {
		char[] arrayA = a.toLowerCase().toCharArray();
		char[] arrayB = b.toLowerCase().toCharArray();
		Arrays.sort(arrayA);
		Arrays.sort(arrayB);
		return Arrays.equals(arrayA, arrayB);
	}

	private List<String> getAnswerList(String answer) {
		String[] answerArray = answer.split("\\$");
		List<String> answerList = Arrays.asList(answerArray);
		return answerList;
	}

	private Question getCurrentQObject() {

		if (this.currentQ != null) {
			return this.currentQ;
		} else {
			Log.e("yizhandaodi", "getCurrentQ() null pointer!!");
			return null;
		}
	}

	private Question getNextQObject(Context ct, boolean isDiffChange) {
		Question q = null;
		int id = 0;
		if ((this.qCache == null) || (!this.qIt.hasNext()) || (isDiffChange)) {
			if (refreshQCache(ct) > 0) {
				return getNextQObject(ct, isDiffChange);
			} else {
				// means no more questons
				Log.e("yizhandaodi", "getNextQObject(): no more questions!!");
				this.currentQ = null;
				return null;
			}
		} else {
			q = this.qIt.next();
			this.currentQ = q;
			if (this.isRandom) {
				// TODO: set the field "used" to 1
				DBHelper myDbHelper = new DBHelper(ct, GamePlay.MAIN_DB);
				try {
					myDbHelper.openDataBase();
				} catch (SQLException sqle) {
					throw sqle;
				}
				id = q.getId();
				myDbHelper.setUsed(id);
				myDbHelper.close();
			}
			return q;
		}

	}

	/**
	 * 
	 * @return the number of fetched records
	 */
	private int refreshQCache(Context ct) {

		DBHelper myDbHelper = new DBHelper(ct, GamePlay.MAIN_DB);

		myDbHelper.createDataBase();

		try {
			myDbHelper.openDataBase();
		} catch (SQLException sqle) {
			throw sqle;
		}
		if (isRandom) {

		}
		ArrayList<Question> questions = null;
		if (this.episodeName == null) {
			questions = (ArrayList<Question>) myDbHelper.getQuestionSet(
					this.difficulty, this.category, this.topic, this.isRandom,
					this.startQId, Constants.CACHESIZE);
		} else {
			questions = (ArrayList<Question>) myDbHelper
					.getQuestionSetOfEp(this.episodeName);
			Log.i("QuestionManager", "got " + questions.size()
					+ " questions for " + this.episodeName);
		}
		myDbHelper.close();
		this.qCache = questions;
		this.qIt = this.qCache.listIterator();
		Log.i(logTAG, "refreshQcache cachesize: " + qCache.size());
		return qCache.size();

	}

	public void setEpisode(String epName) {
		this.episodeName = epName;
	}

	public String getEpisode() {
		return episodeName;
	}
}
