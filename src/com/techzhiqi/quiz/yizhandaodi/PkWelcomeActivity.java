package com.techzhiqi.quiz.yizhandaodi;

import java.io.IOException;
import java.net.UnknownHostException;

import protocol.*;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.techzhiqi.quiz.yizhandaodi.network.NetworkManager;
import com.techzhiqi.quiz.yizhandaodi.pk.GameManager;
import com.techzhiqi.quiz.yizhandaodi.pk.LoginError;
import com.techzhiqi.quiz.yizhandaodi.pk.LoginManager;
import com.techzhiqi.quiz.yizhandaodi.quiz.Constants;

public class PkWelcomeActivity extends Activity implements OnClickListener {
	private LoginError loginError = null;
	private ProgressDialog progressdialog = null;
	private String _email, _userName, _password;
	private EditText emailText, userNameText, passwordText;
	private Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.activity_pk_welcome);
		emailText = (EditText) findViewById(R.id.editEmail);
		userNameText = (EditText) findViewById(R.id.editUserName);
		passwordText = (EditText) findViewById(R.id.editPassword);

		SharedPreferences accountPref = getSharedPreferences(Constants.ACCOUNT,
				0);
		String emailAddress = accountPref.getString(Constants.EMAIL,
				Constants.UNKNOWN);
		String userName = accountPref.getString(Constants.USERNAME,
				Constants.UNKNOWN);
		String password = accountPref.getString(Constants.PASSWORD,
				Constants.UNKNOWN);
		Log.i("yzdd","email:"+emailAddress+"|name:"+userName+"|pwd:"+password);
		if (!emailAddress.equals(Constants.UNKNOWN)){
			emailText.setText(emailAddress);
		}else{
			emailText.setHint("请填写真实邮箱作为账号");
		}
		if(!userName.equals(Constants.UNKNOWN)){
			userNameText.setText(userName);
		}else{
			userNameText.setHint("请填写昵称");
		}
		if(!password.equals(Constants.UNKNOWN)){
			passwordText.setText(password);
		}else{
			passwordText.setHint("请输入密码");
		}

		Button loginBtn = (Button) findViewById(R.id.loginExistedAccount);
		loginBtn.setOnClickListener(this);
		Button registerBtn = (Button) findViewById(R.id.registerNewAccount);
		registerBtn.setOnClickListener(this);
		Button exitBtn = (Button) findViewById(R.id.pk_welcome_backBtn);
		exitBtn.setOnClickListener(this);
		TextView forgetLink = (TextView) findViewById(R.id.forgetPassword);
		forgetLink.setOnClickListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_pk_welcome, menu);
		return true;
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.loginExistedAccount: {
			if (!isValidLoginInput()) {
				Toast toast = Toast.makeText(getApplicationContext(),
						"请输入邮箱及密码", Toast.LENGTH_SHORT);
				toast.show();
				break;
			}
			updateAccountPref();
			if (!hasConnection()) {
				Toast toast = Toast.makeText(getApplicationContext(),
						"请确认数据网络可用", Toast.LENGTH_SHORT);
				toast.show();
				break;
			}
			
			login();

			break;
		}

		case R.id.registerNewAccount:
			if (!isValidRegisterInput()) {
				Toast toast = Toast.makeText(getApplicationContext(),
						"请输入邮箱及密码", Toast.LENGTH_SHORT);
				toast.show();
				break;
			}
			updateAccountPref();
			if (!hasConnection()) {
				Toast toast = Toast.makeText(getApplicationContext(),
						"请确认数据网络可用", Toast.LENGTH_SHORT);
				toast.show();
				break;
			}

			register();
			break;
		case R.id.pk_welcome_backBtn:
			finish();
			break;
		
		case R.id.forgetPassword:
			Intent i = new Intent(PkWelcomeActivity.this, PkSendPasswordActivity.class);
			startActivity(i);
			break;			
		}
	}
	
	 class RegistrationError{
		public boolean hasError;
		public boolean isNetworkIssue;
		public RegistrationErrorType type;
	}

	private void register() {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			ServerRegistrationSignal sSig = null;
			RegistrationError error = new RegistrationError();
			@Override
			protected void onPreExecute() {
				error.hasError = false;
				progressdialog = new ProgressDialog(context);
				progressdialog.setTitle("注册中...");
				progressdialog.setMessage("请等待...");
				progressdialog.setCancelable(false);
				progressdialog.setIndeterminate(true);
				progressdialog.show();
			}
			
			@Override
			protected Void doInBackground(Void... arg0) {
				GameManager gM = new GameManager();
				((QuizApplication) getApplication()).setOnlineGame(gM);
				NetworkManager network = NetworkManager.getInstance();
				ClientRegistrationSignal sig = new ClientRegistrationSignal(PkWelcomeActivity.this._email,
						PkWelcomeActivity.this._userName,
						PkWelcomeActivity.this._password);
				
				try {
					network.connect2ServerWithTimeout();
					network.send2Server(sig);
					sSig = (ServerRegistrationSignal) network.readFromServer();
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
				
				if (sSig==null) {
					Log.i("yzdd-registration", "readFromServer return null");
					error.hasError = true;
					error.isNetworkIssue = true;
				}else if (sSig.registrationOk){
					Log.i("yzdd-registration", "readFromServer return OK");
					error.hasError = false;
					
					PkWelcomeActivity.this.runOnUiThread(new Runnable() {

						public void run() {
							progressdialog.setTitle("注册成功，将自动登陆");
						}
					});
					
					gM.setUid(sSig.accountId);
					gM.setSessionId(sSig.sessionId);
					gM.setName(sSig.name);
					try {
						Thread.sleep(4000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					Log.i("yzdd", "readFromServer return Else");
					error.hasError = true;
					error.type = sSig.error;
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				if (progressdialog!=null) {
					progressdialog.dismiss();
				}
				if (error.hasError) {
					indicateRegistrationError(error);
				}else{
				Intent i = new Intent(PkWelcomeActivity.this, PkLoggedInActivity.class);
				startActivity(i);
				}
				
			}
		};
		task.execute((Void[])null);

	}


	protected void indicateRegistrationError(RegistrationError error) {
		String str = null;
		if(error.isNetworkIssue){
			str = "服务器未响应";
		}else{
			switch(error.type){
			case EMAIL_ALREADY_EXIST:{
				this.emailText.setHint("邮箱已存在");
				str = "邮箱已存在";
				break;
			}
			case NAME_ALREADY_EXIST:{
				this.passwordText.setHint("昵称已存在");
				str = "昵称已存在";
				break;
			}
			default:{
				str = "服务器未响应";
				break;
			}
			}
		}
		
		Toast toast = Toast.makeText(getApplicationContext(), str,
				Toast.LENGTH_LONG);
		toast.show();
		
	}

	private boolean isValidLoginInput() {

		this._email = emailText.getText().toString().trim();
		this._userName = userNameText.getText().toString().trim();
		this._password = passwordText.getText().toString().trim();

		if (!this._email.equals("") && !this._password.equals("")) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isValidRegisterInput() {
		this._email = emailText.getText().toString().trim();
		this._userName = userNameText.getText().toString().trim();
		this._password = passwordText.getText().toString().trim();

		if (!this._email.equals("") && !this._userName.equals("")
				&& !this._password.equals("")) {
			return true;
		} else {
			return false;
		}
	}

	private boolean hasConnection() {

		getApplicationContext();
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		} else {
			return false;
		}

	}

	private void login() {

		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			ServerLoginSignal sSig = null;
			LoginError error = new LoginError();
			@Override
			protected void onPreExecute() {
				progressdialog = new ProgressDialog(context);
				progressdialog.setTitle("登陆中...");
				progressdialog.setMessage("请等待...");
				progressdialog.setCancelable(false);
				progressdialog.setIndeterminate(true);
				progressdialog.show();
			}
			
			@Override
			protected Void doInBackground(Void... arg0) {
				GameManager gM = new GameManager();
				((QuizApplication) getApplication()).setOnlineGame(gM);
				NetworkManager network = NetworkManager.getInstance();
				ClientLoginSignal sig = new ClientLoginSignal();
				sig.setEmail(PkWelcomeActivity.this._email);
				sig.setName(PkWelcomeActivity.this._userName);
				sig.setPassword(PkWelcomeActivity.this._password);
				
				try {
					network.connect2ServerWithTimeout();
					network.send2Server(sig);
					sSig = (ServerLoginSignal) network.readFromServer();
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
				
				if (sSig==null) {
					Log.i("yzdd-login", "readFromServer return null");
					error.hasError = true;
					error.isNetworkIssue = true;
				}else if (sSig.isOk()){
					Log.i("yzdd-login", "readFromServer return OK");
					error.hasError = false;					
					gM.setUid(sSig.accountId);
					gM.setSessionId(sSig.sessionId);
					gM.setName(sSig.name);
					gM.setWin(sSig.win);
					gM.setLose(sSig.lose);
					
				} else {
					Log.i("yzdd", "readFromServer return Else");
					error.hasError = true;
					error.type = sSig.error;
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				if (progressdialog!=null) {
					progressdialog.dismiss();
				}
				if (error.hasError) {
					indicateLoginError(error);
				}else{
					Intent i = new Intent(PkWelcomeActivity.this, PkLoggedInActivity.class);
					startActivity(i);
				}
			}
		};
		task.execute((Void[])null);
	}
	
	private void updateAccountPref() {
		Log.i("yzdd","updateAccountPref() is called");
		SharedPreferences accountPref = getSharedPreferences(
				Constants.ACCOUNT, 0);
		SharedPreferences.Editor editor = accountPref.edit();
		editor.putString(Constants.EMAIL, PkWelcomeActivity.this._email);
		editor.putString(Constants.USERNAME,
				PkWelcomeActivity.this._userName);
		editor.putString(Constants.PASSWORD,
				PkWelcomeActivity.this._password);
		editor.commit();
	}

	private void indicateLoginError(LoginError error) {
		String str = null;
		if(error.isNetworkIssue){
			str = "服务器未响应";
		}else{
			switch(error.type){
			case EMAIL_NOT_EXIST:{
				this.emailText.setHint("邮箱不存在");
				str = "邮箱不存在";
				break;
			}
			case PASSWORD_NOT_MATCH:{
				this.passwordText.setHint("密码错误");
				str = "密码错误";
				break;
			}
			default:{
				str = "服务器未响应";
				break;
			}
			}
		}
		
		Toast toast = Toast.makeText(getApplicationContext(), str,
				Toast.LENGTH_LONG);
		toast.show();
	}

}
