/*
 * Copyright (C) 2014 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ads.mediation.sample.mediationsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

/**
 * A simple {@link android.app.Activity} that displays adds using the sample adapter and sample
 * custom event.
 */
public class MainActivity extends AppCompatActivity {

    private InterstitialAd adapterInterstitial;
    private RewardedVideoAd rewardedVideoAd;
    private Button adapterButton;
    private Button adapterVideoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sample adapter interstitial button.
        adapterButton = (Button) findViewById(R.id.adapter_button);
        adapterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapterInterstitial.isLoaded()) {
                    adapterInterstitial.show();
                }
            }
        });

        // Sample adapter interstitial.
        adapterInterstitial = new InterstitialAd(this);
        adapterInterstitial.setAdUnitId(
                getResources().getString(R.string.adapter_interstitial_ad_unit_id));
        adapterInterstitial.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Toast.makeText(MainActivity.this,
                        "Error loading adapter interstitial, code " + errorCode,
                        Toast.LENGTH_SHORT).show();
                adapterButton.setEnabled(true);
            }

            @Override
            public void onAdLoaded() {
                adapterButton.setEnabled(true);
            }

            @Override
            public void onAdOpened() {
                adapterButton.setEnabled(false);
            }

            @Override
            public void onAdClosed() {
                adapterInterstitial.loadAd(new AdRequest.Builder().build());
            }
        });

        AdRequest interstitialAdRequest = new AdRequest.Builder()
                .build();
        adapterInterstitial.loadAd(interstitialAdRequest);

        // Sample adapter rewarded video button.
        adapterVideoButton = (Button) findViewById(R.id.adapter_rewarded_button);
        adapterVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rewardedVideoAd.isLoaded()) {
                    rewardedVideoAd.show();
                } else {
                    loadRewardedVideoAd();
                }
            }
        });

        /**
         * Sample adapter rewarded video ad.
         */
        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                adapterVideoButton.setEnabled(true);
                adapterVideoButton.setText("Show SampleAdapter Rewarded Video");
            }

            @Override
            public void onRewardedVideoAdOpened() {}

            @Override
            public void onRewardedVideoStarted() {}

            @Override
            public void onRewardedVideoAdClosed() {
                loadRewardedVideoAd();
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {}

            @Override
            public void onRewardedVideoAdLeftApplication() {}

            @Override
            public void onRewardedVideoAdFailedToLoad(int errorCode) {
                Toast.makeText(MainActivity.this,
                        "Sample adapter rewarded video ad failed with code: " + errorCode,
                        Toast.LENGTH_SHORT).show();
                adapterVideoButton.setEnabled(true);
                adapterVideoButton.setText("Load SampleAdapter Rewarded Video");
            }

            @Override
            public void onRewardedVideoCompleted() {}
        });

        loadRewardedVideoAd();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Activity resumed, update the current activity in Sample SDK's sample rewarded video.
        rewardedVideoAd.resume(MainActivity.this);
    }

    private void loadRewardedVideoAd() {
        adapterVideoButton.setEnabled(false);
        rewardedVideoAd.loadAd(getString(R.string.adapter_rewarded_video_ad_unit_id),
                new AdRequest.Builder().build());
    }
}
