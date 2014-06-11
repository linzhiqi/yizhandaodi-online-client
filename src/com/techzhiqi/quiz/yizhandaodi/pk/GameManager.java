package com.techzhiqi.quiz.yizhandaodi.pk;

public class GameManager {
	long uid;
	long sessionId;
	String name;
	long win;
	long lose;
	String competitorName;
	long competitorWin;
	long competitorLose;
	
	public void setUid(long id){
		this.uid = id;
	}
	
	public long getUid(){
		return uid;
	}
	
	public void setSessionId(long id){
		this.sessionId = id;
	}
	
	public long getSessionId(){
		return sessionId;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName(){
		return name;
	}

	public void setWin(long win) {
		this.win = win;
		
	}
	
	public long getWin(){
		return win;
	}

	public void setLose(long lose) {
		this.lose = lose;
		
	}
	
	public long getLose(){
		return lose;
	}

	public void setCompetitorName(String name) {
		competitorName = name;
	}
	
	public String getCompetitorName(){
		return competitorName;
	}

	public void setCompetitorWin(long win) {
		competitorWin = win;
		
	}
	
	public long getcompetitorWin(){
		return competitorWin;
	}

	public void setCompetitorLose(long lose) {
		competitorLose = lose;
		
	}
	
	public long getcompetitorLose(){
		return competitorLose;
	}
}
