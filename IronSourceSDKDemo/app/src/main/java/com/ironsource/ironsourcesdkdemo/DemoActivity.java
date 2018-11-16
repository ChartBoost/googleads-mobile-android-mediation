package com.ironsource.ironsourcesdkdemo;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chartboost.sdk.Chartboost;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;
import com.ironsource.mediationsdk.utils.IronSourceUtils;

public class DemoActivity extends Activity implements RewardedVideoListener, InterstitialListener {

    private final String TAG = "DemoActivity";
    private static final String APP_KEY = "7264149d";
    private static final String DUMMY_GAID = "cb885ef9-469f-4a70-8d00-86184ac711a8";
    private Button mVideoButton;
    private Button mInterstitialShowButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        String type = null;
        if (getIntent() != null) type = getIntent().getStringExtra("type");
        initUIElements();
        initIronSource(type);
    }

    private void initIronSource(String type) {
        if (type == null || type.equals("rw")) IronSource.setRewardedVideoListener(this);
        if (type == null || type.equals("is")) IronSource.setInterstitialListener(this);
        IronSource.setUserId(DUMMY_GAID);
        if (type == null) IronSource.init(this, APP_KEY);
        else IronSource.init(this, APP_KEY, type.equals("rw") ?
                IronSource.AD_UNIT.REWARDED_VIDEO : IronSource.AD_UNIT.INTERSTITIAL);

        updateButtonsState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // call the IronSource onResume method
        IronSource.onResume(this);
        updateButtonsState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // call the IronSource onPause method
        IronSource.onPause(this);
        updateButtonsState();
    }

    /**
     * Handle the button state according to the status of the IronSource producs
     */
    private void updateButtonsState() {
            handleVideoButtonState(IronSource.isRewardedVideoAvailable());
            handleInterstitialShowButtonState(IronSource.isInterstitialReady());
    }



    /**
     * initialize the UI elements of the activity
     */
    private void initUIElements() {
        mVideoButton = findViewById(R.id.rv_button);
        mVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    // check if video is available
                    if (IronSource.isRewardedVideoAvailable())
                        //show rewarded video
                        IronSource.showRewardedVideo();
            }
        });

        mInterstitialShowButton = findViewById(R.id.is_button);
        mInterstitialShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    // check if interstitial is available
                    if (IronSource.isInterstitialReady()) {
                        //show the interstitial
                        IronSource.showInterstitial();
                }
            }
        });

        TextView versionTV = findViewById(R.id.version_txt);
        versionTV.setText(getResources().getString(R.string.version, IronSourceUtils.getSDKVersion()));
    }

    /**
     * Set the Rewareded Video button state according to the product's state
     *
     * @param available if the video is available
     */
    public void handleVideoButtonState(final boolean available) {
        final String text;
        final int color;
        if (available) {
            color = Color.BLUE;
            text = getResources().getString(R.string.show_rv);
        } else {
            color = Color.BLACK;
            text = getResources().getString(R.string.initializing) + " " + getResources().getString(R.string.rv);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVideoButton.setTextColor(color);
                mVideoButton.setText(text);
                mVideoButton.setEnabled(available);

            }
        });
    }

    /**
     * Set the Show Interstitial button state according to the product's state
     *
     * @param available if the interstitial is available
     */
    public void handleInterstitialShowButtonState(final boolean available) {
        final String text;
        final int color;
        if (available) {
            color = Color.BLUE;
            text = getResources().getString(R.string.show_is);
        } else {
            color = Color.BLACK;
            text = getResources().getString(R.string.initializing) + " " + getResources().getString(R.string.is);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mInterstitialShowButton.setTextColor(color);
                mInterstitialShowButton.setText(text);
                mInterstitialShowButton.setEnabled(available);
            }
        });
    }

    // --------- stuff for tests --------------------

    private ProgressListener listener;

    public interface ProgressListener {
        void waitWithTest();
        void continueWithTest(); // click the show button when the ad is loaded
    }

    public void setProgressListener(ProgressListener progressListener) {
        listener = progressListener;
    }

    // --------- IronSource Rewarded Video Listener ---------

    @Override
    public void onRewardedVideoAdOpened() {
        // called when the video is opened (clicked on "Earn Coins")
        Log.e(TAG, "onRewardedVideoAdOpened");
        if (listener!=null) listener.waitWithTest();
    }

    @Override
    public void onRewardedVideoAdClosed() {
    }

    @Override
    public void onRewardedVideoAvailabilityChanged(boolean b) {
        // called when the video availbility has changed (e.g. ad is cached)
        Log.e(TAG, "onRewardedVideoAvailabilityChanged" + " " + b);
        handleVideoButtonState(b);
        if (b && listener!=null) listener.continueWithTest();
    }

    @Override
    public void onRewardedVideoAdStarted() {
    }

    @Override
    public void onRewardedVideoAdEnded() {
    }

    @Override
    public void onRewardedVideoAdRewarded(Placement placement) {
        // called when the video has been rewarded and a reward can be given to the user
        Log.e(TAG, "onRewardedVideoAdRewarded" + " " + placement);
        if (listener!=null) listener.continueWithTest();
    }

    @Override
    public void onRewardedVideoAdShowFailed(IronSourceError ironSourceError) {
        Log.e(TAG, "onRewardedVideoAdShowFailed" + " " + ironSourceError.getErrorMessage());
    }

    @Override
    public void onRewardedVideoAdClicked(Placement placement) {
        
    }

    // --------- IronSource Interstitial Listener ---------

    @Override
    public void onInterstitialAdClicked() {
        // called when the interstitial has been clicked
        Log.d(TAG, "onInterstitialAdClicked");
    }

    @Override
    public void onInterstitialAdReady() {
        // called when the interstitial is ready
        Log.d(TAG, "onInterstitialAdReady");
        handleInterstitialShowButtonState(true);
        if (listener!=null) listener.continueWithTest();

    }

    @Override
    public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
        Log.d(TAG, "onInterstitialAdLoadFailed" + " " + ironSourceError.getErrorMessage());
        handleInterstitialShowButtonState(false);
    }

    @Override
    public void onInterstitialAdOpened() {
        // called when the interstitial is shown
        Log.d(TAG, "onInterstitialAdOpened");
        if (listener!=null) listener.waitWithTest();
    }

    @Override
    public void onInterstitialAdClosed() {
        // called when the interstitial has been closed
        Log.d(TAG, "onInterstitialAdClosed");
        handleInterstitialShowButtonState(false);
        if (listener!=null) listener.waitWithTest();
    }

    @Override
    public void onInterstitialAdShowSucceeded() {
        // called when the interstitial has been successfully shown
        Log.d(TAG, "onInterstitialAdShowSucceeded");
        if (listener!=null) listener.continueWithTest();
    }

    @Override
    public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
        Log.d(TAG, "onInterstitialAdShowFailed" + " " + ironSourceError.getErrorMessage());
        handleInterstitialShowButtonState(false);
    }
}
