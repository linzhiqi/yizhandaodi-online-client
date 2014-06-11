package com.techzhiqi.quiz.yizhandaodi;

import java.io.IOException;
import com.google.ads.*;
import com.google.ads.AdRequest.ErrorCode;

import protocol.ClientSignal;
import protocol.ServerPairedSignal;
import protocol.ServerQuestionSignal;
import protocol.ServerSignal;
import protocol.SignalTypes;

import com.techzhiqi.quiz.yizhandaodi.network.NetworkManager;
import com.techzhiqi.quiz.yizhandaodi.pk.GameManager;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
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

public class PkPairedActivity extends Activity implements OnClickListener, AdListener {

	private ProgressDialog progressdialog = null;
	private Context context;
	public static final String QUESTION_TAG = "com.techzhiqi.quiz.yizhandaodi.question";
	public static final String SHOULDANSWER_TAG = "com.techzhiqi.quiz.yizhandaodi.shouldanswer";
	public static final String ISWINNER_TAG = "com.techzhiqi.quiz.yizhandaodi.iswinner";
	volatile boolean _pair_canceled = false;
	private volatile boolean waiting_start = false;
	boolean showResult = false;
	private boolean isAdReceived = false;
	private InterstitialAd interstitial;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ServerPairedSignal sPairedSig = null;
		waiting_start = false;
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.activity_pk_paired);
		getWindow().addFlags(
				android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		context = this;
		GameManager gM = ((QuizApplication) getApplication())
				.getCurrentOnlineGame();
		Intent intent = getIntent();
		if (intent.getSerializableExtra(PkLoggedInActivity.SERVER_PAIR_SIG_TAG) != null) {
			sPairedSig = (ServerPairedSignal) intent
					.getSerializableExtra(PkLoggedInActivity.SERVER_PAIR_SIG_TAG);
			gM.setCompetitorName(sPairedSig.name);
			gM.setCompetitorWin(sPairedSig.win);
			gM.setCompetitorLose(sPairedSig.lose);
		} else {
			showResult = true;
			
			isAdReceived = false;
			interstitial = new InterstitialAd(this, "ca-app-pub-1089499077959134/2391625606");
			// Create ad request
			AdRequest adRequest = new AdRequest();
		    adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
		    adRequest.addTestDevice("C8263928AC59F12E2EAFBD8BE1F15A34");
		    // Begin loading your interstitial
		    interstitial.loadAd(adRequest);
		    
		 // Set Ad Listener to use the callbacks below
		    interstitial.setAdListener(this);
			
			Log.i("yzdd-paired", "show result");
			TextView isWinner = (TextView) findViewById(R.id.pk_paired_iswinner);
			if (intent.getBooleanExtra(ISWINNER_TAG, false)) {
				gM.setCompetitorLose(gM.getcompetitorLose() + 1);
				isWinner.setText("你胜利了！");
			} else {
				gM.setCompetitorWin(gM.getcompetitorWin() + 1);
				isWinner.setText("你被击败了...");
			}

		}

		TextView competitorName = (TextView) findViewById(R.id.pk_competitor_name);
		competitorName.setText("昵称: " + gM.getCompetitorName());
		TextView competitorRecord = (TextView) findViewById(R.id.pk_competitor_record);
		competitorRecord.setText("胜/负: " + gM.getcompetitorWin() + " / "
				+ gM.getcompetitorLose());
		Button cancelBtn = (Button) findViewById(R.id.pk_cancel_pair_Btn);
		cancelBtn.setOnClickListener(this);
		Button readyBtn = (Button) findViewById(R.id.pk_game_ready_Btn);
		readyBtn.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_pk_paired, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
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
		if(this.waiting_start==false){
			return true;
		}else{
			return super.onKeyDown(keyCode, event);
		}
		
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pk_cancel_pair_Btn: {
			if(this.isAdReceived){
				cancelPair();
				this.interstitial.show();
			}else{
				cancelPair();
				finish();
			}
			break;
		}

		case R.id.pk_game_ready_Btn: {
			waitToStart();
			break;
		}
		}

	}

	private void cancelPair() {
		NetworkManager network = NetworkManager.getInstance();
		GameManager gM = ((QuizApplication) getApplication())
				.getCurrentOnlineGame();
		ClientSignal sig = new ClientSignal(SignalTypes.C_CANCEL_PAIR);
		sig.setAccountId(gM.getUid());
		sig.setSessionId(gM.getSessionId());
		try {
			network.connect2ServerWithTimeout();
			network.send2Server(sig);
			network.closeConnection();
		} catch (IOException e) {
			Log.i("yzdd",
					"send2Server C_CANCEL_PAIR io exception\n" + e.getMessage());
		}

		ServerSignal recvSig = null;
	}

	/**
	 * 
	 * @return if the other gamer also click ready, returns question signal.
	 *         Otherwise(this gamer don't want wait and sends C_CLIENT_QUITGAME)
	 *         then returns null
	 */
	private void waitToStart() {

		final ServerQuestionSignal questionSig = new ServerQuestionSignal();

		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			GameManager gM = ((QuizApplication) getApplication())
					.getCurrentOnlineGame();
			NetworkManager network = NetworkManager.getInstance();

			@Override
			protected void onPreExecute() {
				waiting_start = true;
				progressdialog = new ProgressDialog(context);
				progressdialog.setTitle("准备就绪...");
				progressdialog.setMessage("等待对手就绪...\n单击返回键取消游戏");
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

				ClientSignal sig = new ClientSignal(
						SignalTypes.C_CLIENT_NEXTGAME);
				sig.setAccountId(gM.getUid());
				sig.setSessionId(gM.getSessionId());
				try {
					network.connect2Server();
					network.send2Server(sig);
				} catch (IOException e) {
					Log.i("yzdd",
							"send2Server C_CLIENT_NEXTGAME io exception\n"
									+ e.getMessage());
				}

				ServerSignal recvSig = null;

				try {
					recvSig = network.readFromServer();
				} catch (ClassNotFoundException e) {
					Log.i("yzdd", e.getMessage());

				} catch (IOException e) {
					Log.i("yzdd",
							"readFromServer() interrupted. " + e.getMessage());
					try {
						network.closeConnection();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					return null;
				}

				/*
				 * don't close connection, so server can send competitor's
				 * answer try { network.closeConnection(); } catch (IOException
				 * e) { // TODO Auto-generated catch block e.printStackTrace();
				 * }
				 */

				if (recvSig instanceof ServerQuestionSignal) {
					questionSig.question = ((ServerQuestionSignal) recvSig).question;
					questionSig.shouldAnswer = ((ServerQuestionSignal) recvSig).shouldAnswer;
				} else if (recvSig.getType() == SignalTypes.S_PAIRCANCELED) {
					Log.i("yzdd", "recv S_PAIRCANCELED signal");
					_pair_canceled = true;
					try {
						network.closeConnection();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else {
					Log.i("yzdd",
							"readFromServer wrong signal type - not ServerPairedSignal");
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				Log.i("yzdd", "onPostExecute() is called");

				if (questionSig.question == null) {// canceled by UI or received
													// paircanceld signal
					if (_pair_canceled) {
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
						waiting_start = false;
						progressdialog.dismiss();
						Log.i("yzdd", "_pair_canceled is true");
						Toast toast = Toast.makeText(getApplicationContext(),
								"对方已退出配对", Toast.LENGTH_LONG);
						toast.show();
						Log.i("yzdd", "toast is shown");
						// may need sleep a while here
						finish();
					} else {
						Log.i("yzdd", "canceled by ui");
						waiting_start = false;
						ClientSignal sig = new ClientSignal(
								SignalTypes.C_CLIENT_QUITGAME);
						sig.setAccountId(gM.getUid());
						sig.setSessionId(gM.getSessionId());

						try {
							if (!network.isConnected()) {
								network.connect2Server();
								network.send2Server(sig);
								Log.i("yzdd",
										"C_CLIENT_QUITGAME sent to server in onPostExecute()");
								network.closeConnection();
							} else {
								Log.i("yzdd",
										"onPostExecute - socket is still connected");
							}
						} catch (IOException e) {
							Log.i("yzdd",
									"send2Server C_CLIENT_QUITGAME io exception\n"
											+ e.getMessage());
						}
						progressdialog.dismiss();
					}
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

					Log.i("yzdd", "got question:" + questionSig.question);
					Intent i = new Intent(PkPairedActivity.this,
							PkAnsweringActivity.class);
					i.putExtra(QUESTION_TAG, questionSig.question);
					i.putExtra(SHOULDANSWER_TAG, questionSig.shouldAnswer);
					startActivity(i);
					finish();
				}
			}

		};
		task.execute((Void[]) null);
	}

	@Override
	public void onDismissScreen(Ad arg0) {
		finish();
	}

	@Override
	public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
		this.isAdReceived = false;
		
	}

	@Override
	public void onLeaveApplication(Ad arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPresentScreen(Ad arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceiveAd(Ad arg0) {
		this.isAdReceived = true;
	}
}