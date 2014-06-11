package com.techzhiqi.quiz.yizhandaodi.quiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Question {
	private int id;
	private String question;
	private String answer;
	private int rating;
	//test checkerId=2
	private int checkerId=0;
	
	
	public int getId(){
		return id;
	}
	
	public void setId(int id){
		this.id=id;
	}
	
	/**
	 * @return the question
	 */
	public String getQuestion() {
		return question;
	}
	/**
	 * @param question the question to set
	 */
	public void setQuestion(String question) {
		this.question = question;
	}
	/**
	 * @return the answer
	 */
	public String getAnswer() {
		return answer;
	}
	/**
	 * @param answer the answer to set
	 */
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	/**
	 * @return the rating
	 */
	public int getRating() {
		return rating;
	}
	/**
	 * @param rating the rating to set
	 */
	public void setRating(int rating) {
		this.rating = rating;
	}
	
	public void setCheckerId(int checkerId){
		this.checkerId=checkerId;
	}
	
	public int getCheckId(){
		return this.checkerId;
	}
	
	/**
	 * @return the option1
	 */
	
	
	public List<String> getQuestionOptions(){
		List<String> shuffle = new ArrayList<String>();
		shuffle.add(answer);
		Collections.shuffle(shuffle);
		return shuffle;
	}

}
