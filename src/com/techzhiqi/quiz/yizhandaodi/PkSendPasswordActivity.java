package com.techzhiqi.quiz.yizhandaodi;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import protocol.ClientSendPasswordSignal;
import protocol.EmailValidator;
import protocol.ServerSendPasswordSignal;

import com.techzhiqi.quiz.yizhandaodi.network.NetworkManager;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
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

public class PkSendPasswordActivity extends Activity implements OnClickListener {
	private ProgressDialog progressdialog = null;
	private EditText emailText;
	private EmailValidator validator = new EmailValidator();
	private String emailInput;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pk_send_password);

		emailText = (EditText) findViewById(R.id.pk_sendpwd_edit_email);
		Button sendBtn = (Button) findViewById(R.id.pk_sendpwd_sendBtn);
		sendBtn.setOnClickListener(this);
		Button backBtn = (Button) findViewById(R.id.pk_sendpwd_backBtn);
		backBtn.setOnClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pk_sendpwd_sendBtn:
			emailInput = emailText.getText().toString();
			if (validator.isValide(emailInput)) {
				requestNewPwd();
			} else {
				Toast toast = Toast.makeText(getApplicationContext(),
						"邮箱地址格式错误", Toast.LENGTH_LONG);
				toast.show();
			}
			break;
		case R.id.pk_sendpwd_backBtn:
			finish();
			break;
		}

	}

	private void requestNewPwd() {
		
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
			ServerSendPasswordSignal sSig = null;
			@Override
			protected void onPreExecute() {
				progressdialog = new ProgressDialog(context);
				progressdialog.setTitle("发送新密码中...");
				progressdialog.setMessage("请等待...");
				progressdialog.setCancelable(false);
				progressdialog.setIndeterminate(true);
				progressdialog.show();
			}

			@Override
			protected Void doInBackground(Void... arg0) {
				ClientSendPasswordSignal cSig = new ClientSendPasswordSignal(emailInput);
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
					sSig = (ServerSendPasswordSignal)network.readFromServer();
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
				if(sSig.sentOK){
					Toast toast = Toast.makeText(getApplicationContext(),
							"已发送随机密码至注册邮箱，请查收", Toast.LENGTH_LONG);
					toast.show();
				}else if(!sSig.emailValidated){
					//impossible
				}else if(!sSig.emailRegistered){
					Toast toast = Toast.makeText(getApplicationContext(),
							"该邮箱并未注册", Toast.LENGTH_LONG);
					toast.show();
				}else if(sSig.emailServiceFailed){
					Toast toast = Toast.makeText(getApplicationContext(),
							"密码邮件发送失败，请稍后再试", Toast.LENGTH_LONG);
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
