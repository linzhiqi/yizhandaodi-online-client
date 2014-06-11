package com.techzhiqi.quiz.yizhandaodi;

import java.io.IOException;

import protocol.ClientAnswerSignal;
import protocol.CorrectAnswerSignal;
import protocol.IncorrectAnswerSignal;
import protocol.ServerGameResultSignal;
import protocol.ServerQuestionSignal;
import protocol.ServerSignal;
import protocol.SignalTypes;

import com.techzhiqi.quiz.yizhandaodi.network.NetworkManager;
import com.techzhiqi.quiz.yizhandaodi.pk.GameManager;
import com.techzhiqi.quiz.yizhandaodi.quiz.Constants;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class PkAnsweringActivity extends Activity implements OnClickListener {

	CountDownTimer myTimer;
	private long myTime = 40000;
	TextView mTv = null;
	private volatile boolean waitingAnswerCheck = false;
	private volatile boolean gotResultAfterTimeup = false;
	private volatile boolean timeup = false;

	private ProgressDialog progressdialog = null;
	private Context context;
	EditText answerText;
	TextView isRightText;
	TextView isWatcher;
	Button nextBtn;
	GameManager gM;
	volatile boolean timeUp = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// this way has problem
		setContentView(R.layout.activity_pk_answering);
		getWindow().addFlags(
				android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		context = this;
		gM = ((QuizApplication) getApplication()).getCurrentOnlineGame();
		mTv = (TextView) findViewById(R.id.pk_answering_timer);
		nextBtn = (Button) findViewById(R.id.pk_answering_submitBtn);
		// Button passBtn = (Button) findViewById(R.id.pk_answering_passBtn);
		// passBtn.setOnClickListener(this);
		nextBtn.setOnClickListener(this);
		isWatcher = (TextView) findViewById(R.id.pk_answering_should_answer);
		TextView qText = (TextView) findViewById(R.id.pk_answering_question);
		isRightText = (TextView) findViewById(R.id.pk_answering_checkresult);
		answerText = (EditText) findViewById(R.id.pk_answering_input_answer);
		Intent intent = getIntent();
		String question = intent.getStringExtra(PkPairedActivity.QUESTION_TAG);
		boolean shouldAnswer = intent.getBooleanExtra(
				PkPairedActivity.SHOULDANSWER_TAG, false);
		qText.setText("题目: " + question);

		if (!shouldAnswer) {
			AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
				GameManager gM = ((QuizApplication) getApplication())
						.getCurrentOnlineGame();
				NetworkManager network = NetworkManager.getInstance();
				volatile boolean nextQuestion = false;
				volatile boolean youWin = false;
				ServerSignal recvSig = null;

				@Override
				protected void onPreExecute() {
					/*
					 * progressdialog = new ProgressDialog(context);
					 * progressdialog.setTitle("对手答题中...");
					 * progressdialog.setCancelable(false);
					 * progressdialog.setIndeterminate(true); progressdialog
					 * .setProgressStyle(android.R.attr.progressBarStyleSmall);
					 * progressdialog.getWindow().setGravity(Gravity.BOTTOM);
					 * progressdialog.show();
					 */
					isWatcher.setText("对手答题中");
					answerText.setEnabled(false);
					nextBtn.setEnabled(false);
				}

				@Override
				protected Void doInBackground(Void... arg0) {

					/*
					 * should still be connected try { network.connect2Server();
					 * 
					 * } catch (IOException e) { Log.i("yzdd",
					 * "connecting failed when receiving signal when waiting competitor's answer\n"
					 * + e.getMessage()); }
					 */
					while (true) {
						try {
							recvSig = network.readFromServer();
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							Log.i("yzdd",
									"recv failed when receiving signal when waiting competitor's answer\n"
											+ e.getMessage());
							e.printStackTrace();

						}
						if (recvSig instanceof IncorrectAnswerSignal) {
							Log.i("yzdd", "incorrectAnswersignal is received");
							final String competitorAnswer = ((IncorrectAnswerSignal) recvSig).answer;
							PkAnsweringActivity.this
									.runOnUiThread(new Runnable() {

										public void run() {
											answerText
													.setText(competitorAnswer);
											isRightText.setTextColor(-65536);
											isRightText.setText("对手回答错误");
											nextQuestion = false;
										}
									});

							continue;
						} else if (recvSig instanceof CorrectAnswerSignal) {
							Log.i("yzdd", "correctAnswersignal is received");
							myTimer.cancel();
							runOnUiThread(new Runnable() {
								public void run() {
									Log.i("yzdd", "going to update ui, 回答正确");
									answerText
											.setText(((CorrectAnswerSignal) recvSig).answer);
									isRightText.setTextColor(-16711936);
									isRightText
											.setText("对手回答正确!标准答案:"
													+ ((CorrectAnswerSignal) recvSig).standardAnswer);
									nextQuestion = true;
								}
							});
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						} else if (recvSig instanceof ServerGameResultSignal) {
							Log.i("yzdd", "ServerGameResultSignal received");
							nextQuestion = false;
							youWin = true;
							runOnUiThread(new Runnable() {
								public void run() {
									isRightText.setTextColor(-65536);
									isRightText
											.setText("对手答题失败!标准答案:"
													+ ((ServerGameResultSignal) recvSig).standardAnswer);
								}
							});
							try {
								network.closeConnection();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							try {
								Thread.sleep(4000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}
					}
					/*
					 * try { network.closeConnection(); } catch (IOException e)
					 * { // TODO Auto-generated catch block e.printStackTrace();
					 * }
					 */
					return null;
				}

				protected void onPostExecute(Void result) {
					if (nextQuestion) {
						myTimer.cancel();
						Intent intent = new Intent(PkAnsweringActivity.this,
								PkAnsweringActivity.class);
						intent.putExtra(PkPairedActivity.QUESTION_TAG,
								((CorrectAnswerSignal) recvSig).nextQuestion);
						intent.putExtra(PkPairedActivity.SHOULDANSWER_TAG,
								((CorrectAnswerSignal) recvSig).shouldAnswer);
						startActivity(intent);
						finish();
					}

					if (youWin) {
						myTimer.cancel();
						Intent intent = new Intent(PkAnsweringActivity.this,
								PkPairedActivity.class);
						intent.putExtra(PkPairedActivity.ISWINNER_TAG, true);
						startActivity(intent);
						finish();
					}
				}
			};
			task.execute((Void[]) null);

			myTimer = new CountDownTimer(myTime, 100) {

				public void onTick(long millisUntilFinished) {
					mTv.setText("剩余时间: " + millisUntilFinished / 1000 + "."
							+ (millisUntilFinished % 1000) / 100 + "秒");
					myTime = millisUntilFinished;
				}

				public void onFinish() {
					Log.i("yizhandaodi", "onFinish() is called,myTime="
							+ myTime);
					if (myTime / 1000 == 0) {
						mTv.setText("时间到!");
					}
				}
			}.start();
		} else {
			myTimer = new CountDownTimer(myTime, 100) {
				ServerSignal sSig = null;

				public void onTick(long millisUntilFinished) {
					mTv.setText("剩余时间: " + millisUntilFinished / 1000 + "."
							+ (millisUntilFinished % 1000) / 100 + "秒");
					myTime = millisUntilFinished;
				}

				public void onFinish() {
					Log.i("yizhandaodi", "onFinish() is called,myTime="
							+ myTime);
					if (myTime / 1000 == 0) {
						AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

							NetworkManager network = NetworkManager
									.getInstance();

							@Override
							protected void onPreExecute() {
								mTv.setText("时间到!");
								nextBtn.setEnabled(false);
								timeup = true;
							}

							@Override
							protected Void doInBackground(Void... arg0) {
								//as ALLOWED_NETWORK_LATENCY=4s
								for (int i = 0; i < 40; i++) {
									if (!waitingAnswerCheck) {
										break;
									} else {
										Log.i("yzdd-timer",
												"waitingAnserCheck is true");
										try {
											Thread.sleep(100);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}

								}
								
								try {
									if (gotResultAfterTimeup) {
										Log.i("yzdd-timer", "gotResultAfterTimeup is true");
										return null;
									}else{
										Log.i("yzdd-timer",
												"readFromServer() is called");
										sSig = network.readFromServer();
										Log.i("yzdd-timer",
												"readFromServer() is returned");
									}
									
								} catch (ClassNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									Log.e("yzdd-timer",
											"readFromServer() io exception: "
													+ e.getMessage());
									e.printStackTrace();
									// likely the sendAnswer has closed the
									// connection, so it should already got
									// result
									if (gotResultAfterTimeup) {
										Log.i("yzdd-timer", "gotResultAfterTimeup is true");
										return null;
									} else {
										// very unlikely here
										runOnUiThread(new Runnable() {
											public void run() {
												Log.i("yzdd-timer",
														"runnable is called, will updae isRightText");

												isRightText
														.setTextColor(-65536);
												isRightText
														.setText("由于网络超时,答题失败！请在稳定的网络条件下进行答题");
											}
										});
										try {
											Thread.sleep(4000);
										} catch (InterruptedException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
										Intent intent = new Intent(
												PkAnsweringActivity.this,
												PkPairedActivity.class);
										intent.putExtra(
												PkPairedActivity.ISWINNER_TAG,
												false);
										startActivity(intent);
										finish();
										return null;
									}

								}

								if (sSig instanceof ServerGameResultSignal) {
									Log.i("yzdd-timer",
											"ServerGameResultSignal is received");
									runOnUiThread(new Runnable() {
										public void run() {
											Log.i("yzdd-timer",
													"runnable is called, will updae isRightText");

											isRightText.setTextColor(-65536);
											isRightText
													.setText("答题失败!标准答案:"
															+ ((ServerGameResultSignal) sSig).standardAnswer);
										}
									});
									try {
										network.closeConnection();
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									try {
										Thread.sleep(4000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									Intent intent = new Intent(
											PkAnsweringActivity.this,
											PkPairedActivity.class);
									intent.putExtra(
											PkPairedActivity.ISWINNER_TAG,
											false);
									startActivity(intent);
									finish();
								} else {
									// impossible here
									try {
										network.closeConnection();
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									Log.e("yzdd-timeup",
											"impossible here! read response timeout or impossible server signal type: "
													+ (sSig == null ? "null"
															: sSig.getType()));
									finish();
								}

								return null;
							}
						};
						task.execute((Void[]) null);
					}
				}
			}.start();
		}

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
		case R.id.pk_answering_submitBtn: {
			if (waitingAnswerCheck == true) {
				return;
			}
			String iAnswer = answerText.getText().toString();
			sendAnswer(iAnswer);

			break;
		}

		/*
		 * case R.id.pk_answering_passBtn: {
		 * 
		 * break; }
		 */
		}
	}

	private void sendAnswer(String input) {
		if (input.equals("")) {
			return;
		}
		final ClientAnswerSignal sig = new ClientAnswerSignal(input);
		sig.accountId = gM.getUid();
		sig.sessionId = gM.getSessionId();

		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			NetworkManager network = NetworkManager.getInstance();
			ServerSignal recvSig = null;

			protected Void doInBackground(Void... arg0) {
				try {
					waitingAnswerCheck = true;
					// network.connect2Server(); should be still connected
					network.send2Server(sig);
				} catch (IOException e) {
					Log.i("yzdd",
							"send answer to server failed\n" + e.getMessage());
				}

				try {
					Log.i("yzdd-sendAnswer", "readFromServer() is called");
					recvSig = network.readFromServer();
					Log.i("yzdd-sendAnswer", "readFromServer() returned");
				} catch (ClassNotFoundException e) {
					Log.e("yzdd-sendAnswer", e.getMessage());

				} catch (IOException e) {
					Log.e("yzdd-sendAnswer", "readFromServer() interrupted. "
							+ e.getMessage());
					return null;
				}

				if (recvSig instanceof CorrectAnswerSignal) {
					Log.i("yzdd-sendAnswer", "CorrectAnswerSignal is received");

					runOnUiThread(new Runnable() {
						public void run() {
							nextBtn.setEnabled(false);
							isRightText.setTextColor(-16711936);
							isRightText
									.setText("回答正确!标准答案:"
											+ ((CorrectAnswerSignal) recvSig).standardAnswer);
						}
					});
					if (timeup) {
						gotResultAfterTimeup = true;
					}
					waitingAnswerCheck = false;
					myTimer.cancel();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Intent i = new Intent(PkAnsweringActivity.this,
							PkAnsweringActivity.class);
					i.putExtra(PkPairedActivity.QUESTION_TAG,
							((CorrectAnswerSignal) recvSig).nextQuestion);
					i.putExtra(PkPairedActivity.SHOULDANSWER_TAG,
							((CorrectAnswerSignal) recvSig).shouldAnswer);
					startActivity(i);
					finish();
				} else if (recvSig instanceof IncorrectAnswerSignal) {
					Log.i("yzdd-sendAnswer",
							"IncorrectAnswerSignal is received");
					runOnUiThread(new Runnable() {
						public void run() {
							isRightText.setTextColor(-65536);
							isRightText.setText("回答错误!");
							waitingAnswerCheck = false;
						}
					});
				} else if (recvSig instanceof ServerGameResultSignal) {

					gotResultAfterTimeup = true;
					waitingAnswerCheck = false;
					Log.e("yzdd-sendAnswer",
							"received ServerGameResultSignal signal");

					runOnUiThread(new Runnable() {
						public void run() {
							Log.i("yzdd-sendAnswer",
									"runnable is called, will updae isRightText");

							isRightText.setTextColor(-65536);
							isRightText.setText("由于网络超时,答题失败！请在稳定的网络条件下进行答题");
						}
					});

					try {
						Thread.sleep(4000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						network.closeConnection();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					Intent intent = new Intent(PkAnsweringActivity.this,
							PkPairedActivity.class);
					intent.putExtra(PkPairedActivity.ISWINNER_TAG, false);
					startActivity(intent);
					finish();
				}

				return null;
			}

			protected void onPostExecute(Void result) {

			}
		};

		task.execute((Void[]) null);
	}
}
