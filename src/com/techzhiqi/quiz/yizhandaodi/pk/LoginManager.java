package com.techzhiqi.quiz.yizhandaodi.pk;

import java.io.IOException;
import java.net.UnknownHostException;

import protocol.ClientLoginSignal;
import protocol.ServerLoginSignal;


import android.util.Log;

import com.techzhiqi.quiz.yizhandaodi.network.NetworkManager;

public class LoginManager {

	static GameManager gM;

	public static void setGameManager(GameManager m) {
		gM = m;
	}

	public static LoginError Login(String email, String userName, String password) {
		LoginError error = new LoginError();
		error.hasError = false;
		NetworkManager network = NetworkManager.getInstance();
		ClientLoginSignal sig = new ClientLoginSignal();
		sig.setEmail(email);
		sig.setName(userName);
		sig.setPassword(password);
		ServerLoginSignal result = null;
		try {
			network.connect2ServerWithTimeout();
			network.send2Server(sig);
			result = (ServerLoginSignal) network.readFromServer();
			network.closeConnection();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			error.hasError = true;
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			error.hasError = true;
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (result==null) {
			Log.i("yzdd", "readFromServer return null");
			error.hasError = true;
			error.isNetworkIssue = true;
		}else if (result.isOk()){
			Log.i("yzdd", "readFromServer return OK");
			error.hasError = false;
			gM.setUid(result.getAccountId());
			gM.setSessionId(result.getSessionId());
		} else {
			Log.i("yzdd", "readFromServer return Else");
			error.hasError = true;
			error.type = result.getError();
		}
		
		return error;
	}

}
