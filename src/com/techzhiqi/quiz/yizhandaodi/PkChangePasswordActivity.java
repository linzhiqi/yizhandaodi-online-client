package com.techzhiqi.quiz.yizhandaodi;

import java.io.IOException;
import java.net.UnknownHostException;

import com.techzhiqi.quiz.yizhandaodi.network.NetworkManager;
import com.techzhiqi.quiz.yizhandaodi.pk.GameManager;

import protocol.ClientChangePasswordSignal;
import protocol.ClientSendPasswordSignal;
import protocol.EmailValidator;
import protocol.ServerChangePasswordSignal;
import protocol.ServerSendPasswordSignal;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

public class PkChangePasswordActivity extends Activity implements OnClickListener {

	private ProgressDialog progressdialog = null;
	private EditText currentPwd;
	private EditText newPwd;
	private EditText confirmNewPwd;
	private String cPwd;
	private String nPwd;
	private String confirmPwd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pk_change_password);
		
		currentPwd = (EditText) findViewById(R.id.pk_current_pwd_edit);
		newPwd = (EditText) findViewById(R.id.pk_new_pwd_edit);
		confirmNewPwd = (EditText) findViewById(R.id.pk_new_pwd_again_edit);
		Button sendBtn = (Button) findViewById(R.id.pk_changpassword_sendBtn);
		sendBtn.setOnClickListener(this);
		Button backBtn = (Button) findViewById(R.id.pk_changepassword_backBtn);
		backBtn.setOnClickListener(this);
		
	}

	
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pk_changpassword_sendBtn:
			cPwd = currentPwd.getText().toString();
			nPwd = newPwd.getText().toString();
			confirmPwd = confirmNewPwd.getText().toString();
			if (cPwd.equals("") || nPwd.equals("") || confirmPwd.equals("")) {
				Toast toast = Toast.makeText(getApplicationContext(),
						"密码不能为空", Toast.LENGTH_LONG);
				toast.show();
			} else if(!nPwd.equals(confirmPwd)) {
				Toast toast = Toast.makeText(getApplicationContext(),
						"新密码两次输入不一致，请重新输入", Toast.LENGTH_LONG);
				toast.show();
			} else {
				changePwd();
			}
			break;
		case R.id.pk_changepassword_backBtn:
			finish();
			break;
		}

	}

	private void changePwd() {
		
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isConnected()) {
			Toast toast = Toast.makeText(getApplicationContext(),
					"请确认数据网络可用", Toast.LENGTH_LONG);
			toast.show();
			return;
		}
		final Context context = this;
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			boolean serverIsResponding = false;
			ServerChangePasswordSignal sSig = null;
			GameManager gM = ((QuizApplication) getApplication())
					.getCurrentOnlineGame();
			@Override
			protected void onPreExecute() {
				progressdialog = new ProgressDialog(context);
				progressdialog.setTitle("修改请求提交中...");
				progressdialog.setMessage("请等待...");
				progressdialog.setCancelable(false);
				progressdialog.setIndeterminate(true);
				progressdialog.show();
			}

			@Override
			protected Void doInBackground(Void... arg0) {
				ClientChangePasswordSignal cSig = new ClientChangePasswordSignal(cPwd,nPwd);
				cSig.accountId=(gM.getUid());
				cSig.sessionId=(gM.getSessionId());
				Log.i("yzdd-changePwd","accountId = " + cSig.accountId);
				NetworkManager network = NetworkManager.getInstance();
				
				try {
					network.connect2ServerWithTimeout();
					network.send2Server(cSig);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					serverIsResponding = false;
					e.printStackTrace();
					return null;
				} catch (IOException e) {
					serverIsResponding = false;
					e.printStackTrace();
					return null;
				}
				
				try {
					sSig = (ServerChangePasswordSignal)network.readFromServer();
					serverIsResponding = true;
					network.closeConnection();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					serverIsResponding = false;
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {			
				if (progressdialog!=null) {
					progressdialog.dismiss();
				}
				
				if(!serverIsResponding){
					Toast toast = Toast.makeText(getApplicationContext(),
							"服务器未响应", Toast.LENGTH_LONG);
					toast.show();
					return;
				}
				if(sSig.pwdChangeOK){
					Toast toast = Toast.makeText(getApplicationContext(),
							"密码修改成功", Toast.LENGTH_LONG);
					toast.show();
				}else if(sSig.PwdTooLong){
					Toast toast = Toast.makeText(getApplicationContext(),
							"密码太长，应少于40个字符", Toast.LENGTH_LONG);
					toast.show();
				}else if(sSig.currentPwdIncorrect){
					Toast toast = Toast.makeText(getApplicationContext(),
							"当前密码错误，请确认后重试", Toast.LENGTH_LONG);
					toast.show();
				}else if(sSig.dbFailed){
					Toast toast = Toast.makeText(getApplicationContext(),
							"服务器修改操作失败，请稍后再试", Toast.LENGTH_LONG);
					toast.show();
				}else{
					Toast toast = Toast.makeText(getApplicationContext(),
							"服务器返回异常", Toast.LENGTH_LONG);
					toast.show();
				}
			}
			
		};
		
		task.execute((Void[]) null);
	}
}
