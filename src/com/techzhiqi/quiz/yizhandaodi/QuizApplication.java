/**
 * 
 */
package com.techzhiqi.quiz.yizhandaodi;

import com.techzhiqi.quiz.yizhandaodi.pk.GameManager;

import com.techzhiqi.quiz.yizhandaodi.quiz.GamePlay;

import android.app.Application;

/**
 * @author zhiqi.lin
 *
 */
public class QuizApplication extends Application{
	private GamePlay currentGame;
	private GameManager onlineGame;

	/**
	 * @param currentGame the current local Game to set
	 */
	public void setCurrentGame(GamePlay currentGame) {
		this.currentGame = currentGame;
	}

	/**
	 * @return the current local Game
	 */
	public GamePlay getCurrentGame() {
		return currentGame;
	}
	
	public void setOnlineGame(GameManager gameManager){
		onlineGame = gameManager;
	}
	
	public GameManager getCurrentOnlineGame(){
		return onlineGame;
	}
}
