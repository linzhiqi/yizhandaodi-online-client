/**
 * 
 */
package com.techzhiqi.quiz.yizhandaodi.quiz;

import java.util.ArrayList;
import java.util.List;
import com.techzhiqi.quiz.yizhandaodi.db.DBHelper;
import android.content.Context;
import android.database.SQLException;


/**
 * @author zhiqi.lin
 * 
 * This class represents the current game being played
 * tracks the score and player details
 *
 */
public class GamePlay {
	public final static String MAIN_DB = "merged.db";
	private QuestionManager qManager = null;
	private int numRounds;
	private int difficulty;
	private String playerName;
	private int right;
	private int wrong;
	private int round;
	private List<String> records = new ArrayList<String>();
	
	
	public GamePlay(){
		qManager = new QuestionManager(null,null,null,null,null);
	}
	
	public QuestionManager getQManager(){
		return qManager;
	}
	
	public void initGame(Context ct){
		/*
		 * clean field "used" to 0
		 */
		DBHelper myDbHelper = new DBHelper(ct, MAIN_DB);
		
		myDbHelper.createDataBase();
		
		try {
			myDbHelper.openDataBase();
			myDbHelper.cleanUsed();
		}catch(SQLException sqle){
			throw sqle;
		}finally{
			myDbHelper.close();
			//dialog.dismiss(); 
		}	
	}
	
	/**
	 * @return the playerName
	 */
	public String getPlayerName() {
		return playerName;
	}
	/**
	 * @param playerName the playerName to set
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
	public void addRecord(String record){
		records.add(record);
	}
	
	public List<String> getRecords(){
		return records;
	}
	/**
	 * @return the right
	 */
	public int getRight() {
		return right;
	}
	/**
	 * @param right the right to set
	 */
	public void setRight(int right) {
		this.right = right;
	}
	/**
	 * @return the wrong
	 */
	public int getWrong() {
		return wrong;
	}
	/**
	 * @param wrong the wrong to set
	 */
	public void setWrong(int wrong) {
		this.wrong = wrong;
	}
	/**
	 * @return the round
	 */
	public int getRound() {
		return round;
	}
	/**
	 * @param round the round to set
	 */
	public void setRound(int round) {
		this.round = round;
	}
	/**
	 * @param difficulty the difficulty to set
	 */
	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}
	/**
	 * @return the difficulty
	 */
	public int getDifficulty() {
		return difficulty;
	}
	
	
	/**
	 * method to increment the number of correct answers this game
	 */
	public void incrementRightAnswers(){
		right ++;
	}
	
	/**
	 * method to increment the number of incorrect answers this game
	 */
	public void incrementWrongAnswers(){
		wrong ++;
	}
	/**
	 * @param numRounds the numRounds to set
	 */
	public void setNumRounds(int numRounds) {
		this.numRounds = numRounds;
	}
	/**
	 * @return the numRounds
	 */
	public int getNumRounds() {
		return numRounds;
	}
	
	/**
	 * method that checks if the game is over
	 * @return boolean
	 */
	public boolean isGameOver(){
		return (getRound() >= getNumRounds());
	}

	public String getCurrentQ(){
		return this.qManager.getCurrentQ();
	}
	
	public String getNextQ(Context ct, boolean isDiffChange){
		return this.qManager.getNextQ(ct, isDiffChange);
	}
	
	public String getCurrentA(){
		return this.qManager.getCurrentA();
	}
	
	public boolean checkAnswer(String inputA){
		return this.qManager.checkAnswer(inputA);
	}

	public void setDiff(Integer diff) {
		this.qManager.setDiff(diff);
	}
	public int getDiff(){
		return this.qManager.getDiff();
	}
	
	public void setCat(Integer cat) {
		this.qManager.setCat(cat);
	}
	
	public int getCat(){
		return this.qManager.getCat();
	}
	
	public void setTopic(Integer tpc){
		this.qManager.setTopic(tpc);
	}
	
	public int getTopic(){
		return this.qManager.getTopic();
	}
	
	public void setEpisode(String epName){
		this.qManager.setEpisode(epName);
	}
}
