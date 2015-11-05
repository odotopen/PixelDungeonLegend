package com.watabou.pixeldungeon;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.odotopen.pixeldungeon.ml.R;

public class Ads extends Activity {
	private InterstitialAd mInterstitialAd;
    private CountDownTimer mCountDownTimer;
    private Button mRetryButton;
    private ImageView enterImage;
    private int countShowAd = 0;  
    //public static final String MyPREFERENCES = "MyPrefs" ;
    //SharedPreferences sharedpreferences;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ads); 		
		 // Create the InterstitialAd and set the adUnitId.
		//sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		
        mInterstitialAd = new InterstitialAd(this);
        // Defined in res/values/strings.xml
        mInterstitialAd.setAdUnitId(getString(R.string.banner_popup_ad_unit_id));

        // Create the "retry" button, which tries to show an interstitial between game plays.
        mRetryButton = ((Button) findViewById(R.id.retry_button));
        mRetryButton.setVisibility(View.INVISIBLE);
        
        enterImage = ((ImageView) findViewById(R.id.enter)); 
        enterImage.setClickable(true); 
        enterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	updateCountShowAds();
            	finish();
                /*Intent intent = new Intent(Ads.this, PixelDungeon.class);
            	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();*/
            }
        });
        
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInterstitial();
            }
        });

        // Create the game timer, which counts down to the end of the level
        // and shows the "retry" button.
        final TextView textView = ((TextView) findViewById(R.id.timer));
        mCountDownTimer = new CountDownTimer(1, 1) {
            @Override
            public void onTick(long millisUnitFinished) {
                //textView.setText("seconds remaining: " + ((millisUnitFinished / 1000) + 1));
            }   
            
            @Override
            public void onFinish() {
                //textView.setText("done!"); 
                mRetryButton.setVisibility(View.VISIBLE); 
                mRetryButton.performClick();                           
            }
        };
        
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() { 
            	
            	 /*updateCountShowAds();
            	 SharedPreferences.Editor editor = sharedpreferences.edit();                 
                 editor.putString("ClosedAds", "1");
                 editor.commit();  
                 Intent intentPixelDungeon = new Intent(Ads.this, PixelDungeon.class);
                 Ads.this.startActivity(intentPixelDungeon); */
                
                 
            }
        }); 
        
		/*AdView mAdViewFooter = (AdView) findViewById(R.id.adView);
	    AdRequest adRequestFooter = new AdRequest.Builder().build();
	    mAdViewFooter.loadAd(adRequestFooter);*/
	}
	
	public void updateCountShowAds(){
		this.countShowAd = 1;
	}
	
	@Override
    public void onPause() {
        // Cancel the timer if the game is paused.
        mCountDownTimer.cancel();
        super.onPause();
    }
	@Override
    public void onResume() {
        // Initialize the timer if it hasn't been initialized yet.
        // Start the game.
		  
        super.onResume();
        if(this.countShowAd == 0){
        	startGame();
        }  
            
    }
	public void showInterstitial() {		
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            //Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
            startGame();
        }        
    }
	
	public void startGame() {
		this.countShowAd = 1;
		// Hide the retry button, load the ad, and start the timer.
		mRetryButton.setVisibility(View.VISIBLE);
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
        mCountDownTimer.start();
             
       
    }
	
}
