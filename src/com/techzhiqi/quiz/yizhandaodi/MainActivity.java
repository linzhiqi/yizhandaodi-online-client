package com.techzhiqi.quiz.yizhandaodi;


import android.os.Bundle;
import com.google.ads.*;
import com.google.ads.AdRequest.ErrorCode;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class MainActivity extends Activity implements OnClickListener, AdListener{
	private boolean isAdReceived = false;
	private InterstitialAd interstitial;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isAdReceived = false;
		interstitial = new InterstitialAd(this, "ca-app-pub-1089499077959134/8438159208");
		// Create ad request
		
	    AdRequest adRequest = new AdRequest();
	    adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
	    adRequest.addTestDevice("C8263928AC59F12E2EAFBD8BE1F15A34");

	    // Begin loading your interstitial
	    interstitial.loadAd(adRequest);
	    
	 // Set Ad Listener to use the callbacks below
	    interstitial.setAdListener(this);
	    
        Button playBtn = (Button) findViewById(R.id.localGameBtn);
		playBtn.setOnClickListener(this);
		Button diffSettingsBtn = (Button) findViewById(R.id.onlineGameBtn);
		diffSettingsBtn.setOnClickListener(this);
        Button exitBtn = (Button) findViewById(R.id.exitBtn);
		exitBtn.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent i=null;
		
		switch (v.getId()) {
		case R.id.localGameBtn:
			i = new Intent(this, SplashActivity.class);
			startActivity(i);
			break;
			
		case R.id.onlineGameBtn:{
			if(this.isAdReceived){
				 interstitial.show();
			}else{
				i = new Intent(this, PkWelcomeActivity.class);
				startActivity(i);
			}
			
			break;
		}	
		case R.id.exitBtn:
			finish();
			break;
			
		}
	
		
	}

	@Override
	public void onDismissScreen(Ad arg0) {
		Intent i = new Intent(this, PkWelcomeActivity.class);
		startActivity(i);
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
