package com.techzhiqi.quiz.yizhandaodi;

import java.io.IOException;

import protocol.ClientSignal;
import protocol.ServerPairedSignal;
import protocol.ServerSignal;
import protocol.SignalTypes;

import com.techzhiqi.quiz.yizhandaodi.network.NetworkManager;
import com.techzhiqi.quiz.yizhandaodi.pk.GameManager;
import com.techzhiqi.quiz.yizhandaodi.pk.LoginManager;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class PkLoggedInActivity extends Activity implements OnClickListener {

	private ProgressDialog progressdialog = null;
	private Context context;
	final static String SERVER_PAIR_SIG_TAG = "com.techzhiqi.quiz.yizhandaodi.server_pair_sig";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.activity_pk_logged_in);
		context = this;
		getWindow().addFlags(
				android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		GameManager gM = ((QuizApplication) getApplication())
				.getCurrentOnlineGame();
		TextView userName = (TextView) findViewById(R.id.pk_user_name);
		userName.setText("昵称: " + gM.getName());
		TextView userRecord = (TextView) findViewById(R.id.pk_user_record);
		userRecord.setText("胜/负: " + gM.getWin() + " / "
				+ gM.getLose());
		Button logoffBtn = (Button) findViewById(R.id.pk_logoff_Btn);
		logoffBtn.setOnClickListener(this);
		Button pairBtn = (Button) findViewById(R.id.pk_random_pair_Btn);
		pairBtn.setOnClickListener(this);
		Button changePwdBtn = (Button) findViewById(R.id.pk_loggedin_changepwd_Btn);
		changePwdBtn.setOnClickListener(this);
		//Button rankingBtn = (Button) findViewById(R.id.pk_rankinglist_Btn);
		//rankingBtn.setOnClickListener(this);
		// may add wait for challenge button
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_pk_logged_in, menu);
		return true;
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
		return true;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pk_logoff_Btn: {
			logOff();
			finish();
			break;
		}

		case R.id.pk_random_pair_Btn: {
			pairing();
			break;
		}

		/**case R.id.pk_rankinglist_Btn: {

			break;
		}*/
		
		case R.id.pk_loggedin_changepwd_Btn: {
			Intent i = new Intent(PkLoggedInActivity.this,
					PkChangePasswordActivity.class);
			startActivity(i);
			break;
		}

		}

	}

	private void logOff() {
		GameManager gM = ((QuizApplication) getApplication())
				.getCurrentOnlineGame();
		NetworkManager network = NetworkManager.getInstance();
		ClientSignal sig = new ClientSignal(SignalTypes.C_LOG_OFF);
		sig.setAccountId(gM.getUid());
		sig.setSessionId(gM.getSessionId());
		try {
			network.connect2ServerWithTimeout();
			network.send2Server(sig);
			network.closeConnection();
			Log.i("yzdd", "C_LOG_OFF sent to server in logOff()");
		} catch (IOException e) {
			Log.i("yzdd",
					"send2Server C_LOG_OFF io exception\n" + e.getMessage());
		}
	}

	private void pairing() {
		final ServerPairedSignal ssig = new ServerPairedSignal();
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			GameManager gM = ((QuizApplication) getApplication())
					.getCurrentOnlineGame();
			NetworkManager network = NetworkManager.getInstance();

			@Override
			protected void onPreExecute() {
				progressdialog = new ProgressDialog(context);
				progressdialog.setTitle("配对中...");
				progressdialog.setMessage("请等待...\n单击返回键取消");
				progressdialog.setCancelable(true);
				progressdialog.setOnCancelListener(new OnCancelListener() {

					public void onCancel(DialogInterface dialog) {

						Log.i("yzdd", "progressdialog's onCancel() is called");
						if (network.isConnected()) {
							try {
								network.closeConnection();
								Log.i("yzdd", "connection is closed");

							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}

				});
				progressdialog.setIndeterminate(true);
				progressdialog.show();
			}

			@Override
			protected Void doInBackground(Void... arg0) {

				ClientSignal sig = new ClientSignal(SignalTypes.C_REQ_PAIR);
				sig.setAccountId(gM.getUid());
				sig.setSessionId(gM.getSessionId());
				try {
					network.connect2Server();
					network.send2Server(sig);
				} catch (IOException e) {
					Log.i("yzdd",
							"send2Server C_REQ_PAIR io exception\n"
									+ e.getMessage());
				}

				ServerSignal recvSig = null;

				try {
					recvSig = network.readFromServer();
				} catch (ClassNotFoundException e) {
					Log.i("yzdd", e.getMessage());

				} catch (IOException e) {
					Log.i("yzdd",
							"readFromServer io exception\n" + e.getMessage());
					try {
						network.closeConnection();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					return null;
				}

				try {
					network.closeConnection();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (recvSig instanceof ServerPairedSignal) {
					ssig.name = ((ServerPairedSignal) recvSig).name;
					ssig.win = ((ServerPairedSignal) recvSig).win;
					ssig.lose = ((ServerPairedSignal) recvSig).lose;
				} else {
					Log.i("yzdd",
							"readFromServer wrong signal type - not ServerPairedSignal");
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				Log.i("yzdd", "onPostExecute() is called");

				if (ssig.name == null) {// canceled by UI
					ClientSignal sig = new ClientSignal(
							SignalTypes.C_CANCEL_PAIR);
					sig.setAccountId(gM.getUid());
					sig.setSessionId(gM.getSessionId());

					try {
						if (!network.isConnected()) {
							network.connect2Server();
							network.send2Server(sig);
							Log.i("yzdd",
									"C_CANCEL_PAIR sent to server in onPostExecute()");
							network.closeConnection();
						} else {
							Log.i("yzdd",
									"onPostExecute - socket is still connected");
						}
					} catch (IOException e) {
						Log.i("yzdd",
								"send2Server C_CANCEL_PAIR io exception\n"
										+ e.getMessage());
					}
					ssig.name = null;
					ssig.win = 0;
					ssig.lose = 0;
					progressdialog.dismiss();
				} else {// got response from server
					
					Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
					MediaPlayer mediaPlayer = new MediaPlayer();
					try {
					      mediaPlayer.setDataSource(context, defaultRingtoneUri);
					      mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
					      mediaPlayer.prepare();
					      mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

					         @Override
					         public void onCompletion(MediaPlayer mp)
					         {
					            mp.release();
					         }
					      });
					  mediaPlayer.start();
					} catch (IllegalArgumentException e) {
					 e.printStackTrace();
					} catch (SecurityException e) {
					 e.printStackTrace();
					} catch (IllegalStateException e) {
					 e.printStackTrace();
					} catch (IOException e) {
					 e.printStackTrace();
					}

					progressdialog.dismiss();
					
					Intent i = new Intent(PkLoggedInActivity.this,
							PkPairedActivity.class);
					i.putExtra(SERVER_PAIR_SIG_TAG, ssig);
					startActivity(i);
				}
			}
		};
		task.execute((Void[]) null);
	}
}
