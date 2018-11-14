package com.ironsource.adapters.chartboost;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Chartboost.CBFramework;
import com.chartboost.sdk.Chartboost.CBMediation;
import com.chartboost.sdk.Libraries.CBLogging.Level;
import com.chartboost.sdk.Model.CBError.CBImpressionError;
import com.ironsource.mediationsdk.AbstractAdapter;
import com.ironsource.mediationsdk.IntegrationData;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.sdk.InterstitialSmashListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoSmashListener;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import org.json.JSONObject;
import static com.chartboost.sdk.Trace.Trace.CHARTBOOST_CACHE_REWARDED_VIDEO;
import static com.chartboost.sdk.Trace.Trace.CHARTBOOST_CACHE_INTERSTITIAL;
import static com.chartboost.sdk.Trace.Trace.CHARTBOOST_SHOW_INTERSTITIAL;
import static com.chartboost.sdk.Trace.Trace.CHARTBOOST_SHOW_REWARDED_VIDEO;
import static com.chartboost.sdk.Trace.Trace.DELEGATE_DID_CACHE_INTERSTITIAL;
import static com.chartboost.sdk.Trace.Trace.DELEGATE_DID_CACHE_REWARDED;
import static com.chartboost.sdk.Trace.Trace.DELEGATE_DID_CLICK_INTERSTITIAL;
import static com.chartboost.sdk.Trace.Trace.DELEGATE_DID_CLICK_REWARDED;
import static com.chartboost.sdk.Trace.Trace.DELEGATE_DID_CLOSE_INTERSTITIAL;
import static com.chartboost.sdk.Trace.Trace.DELEGATE_DID_CLOSE_REWARDED;
import static com.chartboost.sdk.Trace.Trace.DELEGATE_DID_COMPLETE_REWARDED;
import static com.chartboost.sdk.Trace.Trace.DELEGATE_DID_DISMISS_INTERSTITIAL;
import static com.chartboost.sdk.Trace.Trace.DELEGATE_DID_DISMISS_REWARDED;
import static com.chartboost.sdk.Trace.Trace.DELEGATE_DID_DISPLAY_INTERSTITIAL;
import static com.chartboost.sdk.Trace.Trace.DELEGATE_DID_DISPLAY_REWARDED;
import static com.chartboost.sdk.Trace.Trace.DELEGATE_DID_FAIL_TO_LOAD_INTERSTITIAL;
import static com.chartboost.sdk.Trace.Trace.DELEGATE_DID_FAIL_TO_LOAD_REWARDED;
import static com.chartboost.sdk.Trace.Trace.DELEGATE_DID_INITIALIZE;
import static com.chartboost.sdk.Trace.Trace.DELEGATE_WILL_DISPLAY_INTERSTITIAL;

import com.chartboost.sdk.Sdk;

// decompiled ironsource adapter (tweaked to log to logentries.com)
@SuppressWarnings("unused")
class ChartboostAdapter extends AbstractAdapter {
    private static final String VERSION = "4.1.4";
    private static final String ADAPTER_NAME = "ironsource_adapter";
    private ChartboostAdapter.CbDelegate mDelegate;
    private Activity mActivity;
    private Boolean mAlreadyCalledInit = false;
    private boolean mDidCallLoad = false;
    private boolean mDidCallInitInterstitial = false;
    private boolean mInitiatedSuccessfully = false;
    private final String APP_ID = "appID";
    private final String APP_SIGNATURE = "appSignature";
    private Boolean mConsentCollectingUserData = null;

    public static ChartboostAdapter startAdapter(String providerName) {
        return new ChartboostAdapter(providerName);
    }

    private ChartboostAdapter(String providerName) {
        super(providerName);
    }

    public static IntegrationData getIntegrationData(Activity activity) {
        IntegrationData ret = new IntegrationData("Chartboost", VERSION);
        ret.activities = new String[]{"com.chartboost.sdk.CBImpressionActivity"};
        return ret;
    }

    public String getVersion() {
        return VERSION;
    }

    public String getCoreSDKVersion() {
        return Chartboost.getSDKVersion();
    }

    protected synchronized void setConsent(boolean consent) {
        if (this.mAlreadyCalledInit) {
            Chartboost.setPIDataUseConsent(this.mActivity, consent ? Chartboost.CBPIDataUseConsent.YES_BEHAVIORAL :
                    Chartboost.CBPIDataUseConsent.UNKNOWN);
        } else {
            this.mConsentCollectingUserData = consent;
        }

    }

    public void onResume(Activity activity) {
        if (activity != null) {
            this.mActivity = activity;
            Chartboost.onStart(activity);
            Chartboost.onResume(activity);
        }

    }

    public void onPause(Activity activity) {
        if (activity != null) {
            Chartboost.onPause(activity);
            Chartboost.onStop(activity);
        }

    }

    private void init(final Activity activity, final String userId, final String type, final String appId, final String appSignature) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                synchronized(this) {
                    if (!ChartboostAdapter.this.mAlreadyCalledInit) {
                        ChartboostAdapter.this.mAlreadyCalledInit = true;
                        ChartboostAdapter.this.mActivity = activity;
                        if (ChartboostAdapter.this.mConsentCollectingUserData != null) {
                            ChartboostAdapter.this.setConsent(ChartboostAdapter.this.mConsentCollectingUserData);
                        }

                        Chartboost.startWithAppId(activity, appId, appSignature);
                        Chartboost.setDelegate(ChartboostAdapter.this.mDelegate);
                        boolean isDebugEnabled = false;

                        try {
                            isDebugEnabled = true;//ChartboostAdapter.this.isAdaptersDebugEnabled();
                        } catch (NoSuchMethodError var5) {
                            Log.e("ChartboostAdapter", "ouch");
                        }

                        if (isDebugEnabled) {
                            Chartboost.setLoggingLevel(Level.ALL);
                        } else {
                            Chartboost.setLoggingLevel(Level.NONE);
                        }

                        if ("Unity".equals(ChartboostAdapter.this.getPluginType()) && !TextUtils.isEmpty(ChartboostAdapter.this.getPluginFrameworkVersion())) {
                            Chartboost.setFramework(CBFramework.CBFrameworkUnity, ChartboostAdapter.this.getPluginFrameworkVersion());
                        }

                        Chartboost.setMediation(CBMediation.CBMediationironSource, VERSION);
                        Chartboost.setCustomId(userId);
                        Sdk.get().track.traceMediation(CHARTBOOST_CACHE_INTERSTITIAL, "", ADAPTER_NAME, VERSION);
                        Chartboost.setAutoCacheAds(true);
                        Chartboost.onCreate(activity);
                        Chartboost.onStart(activity);
                        Chartboost.onResume(activity);
                    }

                    if (ChartboostAdapter.this.mInitiatedSuccessfully) ChartboostAdapter.this.reportInterstitialInitSuccess();
                    if (type.equals("RV")) {
                        Sdk.get().track.traceMediation(CHARTBOOST_CACHE_REWARDED_VIDEO, "Default", ADAPTER_NAME, VERSION);
                        Chartboost.cacheRewardedVideo("Default");
                    } else if (type.equals("IS")) {
                        IronSource.loadInterstitial();
                    }

                }
            }
        });
    }

    public void initRewardedVideo(Activity activity, String appKey, String userId, JSONObject config, RewardedVideoSmashListener listener) {
        if (!TextUtils.isEmpty(config.optString("appID")) && !TextUtils.isEmpty(config.optString("appSignature"))) {
            if (this.mDelegate == null) {
                this.mDelegate = new ChartboostAdapter.CbDelegate();
            }

            this.init(activity, userId, "RV", config.optString("appID"), config.optString("appSignature"));
        } else {
            if (listener != null) {
                listener.onRewardedVideoAvailabilityChanged(false);
            }

        }
    }

    public void fetchRewardedVideo(JSONObject config) {
    }

    public void showRewardedVideo(JSONObject config, RewardedVideoSmashListener listener) {
        this.mActiveRewardedVideoSmash = listener;
        if (Chartboost.hasRewardedVideo("Default")) {
            Sdk.get().track.traceMediation(CHARTBOOST_SHOW_REWARDED_VIDEO, "Default", ADAPTER_NAME, VERSION);
            Chartboost.showRewardedVideo("Default");
        } else {
            Sdk.get().track.traceMediation(CHARTBOOST_CACHE_REWARDED_VIDEO, "Default", ADAPTER_NAME, VERSION);
            Chartboost.cacheRewardedVideo("Default");
            if (this.mActiveRewardedVideoSmash != null) {
                this.mActiveRewardedVideoSmash.onRewardedVideoAdShowFailed(ErrorBuilder.buildNoAdsToShowError("Rewarded Video"));
            }

            for (RewardedVideoSmashListener smash : this.mAllRewardedVideoSmashes) {
                if (smash != null) {
                    smash.onRewardedVideoAvailabilityChanged(false);
                }
            }
        }

    }

    public boolean isRewardedVideoAvailable(JSONObject config) {
        return Chartboost.hasRewardedVideo("Default");
    }

    public void initInterstitial(Activity activity, String appKey, String userId, JSONObject config, InterstitialSmashListener listener) {
        if (!TextUtils.isEmpty(config.optString("appID")) && !TextUtils.isEmpty(config.optString("appSignature"))) {
            this.mDidCallInitInterstitial = true;
            if (this.mDelegate == null) {
                this.mDelegate = new ChartboostAdapter.CbDelegate();
            }

            this.init(activity, userId, "IS", config.optString("appID"), config.optString("appSignature"));
        } else {
            if (listener != null) {
                listener.onInterstitialInitFailed(ErrorBuilder.buildInitFailedError("Missing params", "Interstitial"));
            }

        }
    }

    public void loadInterstitial(JSONObject config, InterstitialSmashListener listener) {
        this.mDidCallLoad = true;
        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            public void run() {
                Sdk.get().track.traceMediation(CHARTBOOST_CACHE_INTERSTITIAL, "Default", ADAPTER_NAME, VERSION);
                Chartboost.cacheInterstitial("Default");
            }
        });
    }

    public void showInterstitial(JSONObject config, InterstitialSmashListener listener) {
        this.mActiveInterstitialSmash = listener;
        if (Chartboost.hasInterstitial("Default")) {
            Sdk.get().track.traceMediation(CHARTBOOST_SHOW_INTERSTITIAL, "Default", ADAPTER_NAME, VERSION);
            Chartboost.showInterstitial("Default");
        } else if (this.mActiveInterstitialSmash != null) {
            this.mActiveInterstitialSmash.onInterstitialAdShowFailed(ErrorBuilder.buildNoAdsToShowError("Interstitial"));
        }

    }

    public boolean isInterstitialReady(JSONObject config) {
        return Chartboost.hasInterstitial("Default");
    }

    private void reportInterstitialInitSuccess() {
        if (this.mDidCallInitInterstitial) {
            this.mDidCallInitInterstitial = false;

            for (InterstitialSmashListener smash : this.mAllInterstitialSmashes) {
                if (smash != null) {
                    smash.onInterstitialInitSuccess();
                }
            }
        }

    }

    private void updateRVAvailability(boolean available) {

        for (RewardedVideoSmashListener smash : this.mAllRewardedVideoSmashes) {
            if (smash != null) {
                smash.onRewardedVideoAvailabilityChanged(available);
            }
        }

    }

    private class CbDelegate extends ChartboostDelegate {
        private CbDelegate() {
        }

        public void didDisplayRewardedVideo(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_DISPLAY_REWARDED, location, ADAPTER_NAME, VERSION);
            if (ChartboostAdapter.this.mActiveRewardedVideoSmash != null) {
                ChartboostAdapter.this.mActiveRewardedVideoSmash.onRewardedVideoAdOpened();
            }

        }

        public void didCacheRewardedVideo(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_CACHE_REWARDED, location, ADAPTER_NAME, VERSION);
            ChartboostAdapter.this.updateRVAvailability(true);
        }

        public void didFailToLoadRewardedVideo(String location, CBImpressionError error) {
            Sdk.get().track.traceMediation(DELEGATE_DID_FAIL_TO_LOAD_REWARDED, location, ADAPTER_NAME, VERSION);
            if (!Chartboost.hasRewardedVideo("Default")) {
                ChartboostAdapter.this.updateRVAvailability(false);
            } else {
                ChartboostAdapter.this.updateRVAvailability(true);
            }

        }

        public void didDismissRewardedVideo(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_DISMISS_REWARDED, location, ADAPTER_NAME, VERSION);

            if (ChartboostAdapter.this.mActiveRewardedVideoSmash != null) {
                ChartboostAdapter.this.mActiveRewardedVideoSmash.onRewardedVideoAdEnded();
            }

        }

        public void didCloseRewardedVideo(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_CLOSE_REWARDED, location, ADAPTER_NAME, VERSION);
            if (ChartboostAdapter.this.mActiveRewardedVideoSmash != null) {
                ChartboostAdapter.this.mActiveRewardedVideoSmash.onRewardedVideoAdClosed();
            }

        }

        public void didClickRewardedVideo(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_CLICK_REWARDED, location, ADAPTER_NAME, VERSION);
            if (ChartboostAdapter.this.mActiveRewardedVideoSmash != null) {
                ChartboostAdapter.this.mActiveRewardedVideoSmash.onRewardedVideoAdClicked();
            }

        }

        public void didCompleteRewardedVideo(String location, int reward) {
            Sdk.get().track.traceMediation(DELEGATE_DID_COMPLETE_REWARDED, location, ADAPTER_NAME, VERSION);
            if (ChartboostAdapter.this.mActiveRewardedVideoSmash != null) {
                ChartboostAdapter.this.mActiveRewardedVideoSmash.onRewardedVideoAdRewarded();
            }

        }

        public void willDisplayVideo(String location) {
            Sdk.get().track.traceMediation(DELEGATE_WILL_DISPLAY_INTERSTITIAL, location, ADAPTER_NAME, VERSION);
        }

        public void didCacheInterstitial(String location) {
            if (ChartboostAdapter.this.mDidCallLoad) {
                ChartboostAdapter.this.mDidCallLoad = false;
                Sdk.get().track.traceMediation(DELEGATE_DID_CACHE_INTERSTITIAL, location, ADAPTER_NAME, VERSION);

                for (InterstitialSmashListener smash : ChartboostAdapter.this.mAllInterstitialSmashes) {
                    if (smash != null) {
                        smash.onInterstitialAdReady();
                    }
                }
            }

        }

        public void didFailToLoadInterstitial(String location, CBImpressionError error) {
            if (ChartboostAdapter.this.mDidCallLoad) {
                ChartboostAdapter.this.mDidCallLoad = false;
                Sdk.get().track.traceMediation(DELEGATE_DID_FAIL_TO_LOAD_INTERSTITIAL, location, ADAPTER_NAME, VERSION);

                for (InterstitialSmashListener smash : ChartboostAdapter.this.mAllInterstitialSmashes) {
                    if (smash != null) {
                        smash.onInterstitialAdLoadFailed(ErrorBuilder.buildLoadFailedError(error.toString()));
                    }
                }
            }

        }

        public void didDismissInterstitial(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_DISMISS_INTERSTITIAL, location, ADAPTER_NAME, VERSION);
        }

        public void didCloseInterstitial(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_CLOSE_INTERSTITIAL, location, ADAPTER_NAME, VERSION);
            if (ChartboostAdapter.this.mActiveInterstitialSmash != null) {
                ChartboostAdapter.this.mActiveInterstitialSmash.onInterstitialAdClosed();
            }

        }

        public void didClickInterstitial(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_CLICK_INTERSTITIAL, location, ADAPTER_NAME, VERSION);
            if (ChartboostAdapter.this.mActiveInterstitialSmash != null) {
                ChartboostAdapter.this.mActiveInterstitialSmash.onInterstitialAdClicked();
            }

        }

        public void didDisplayInterstitial(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_DISPLAY_INTERSTITIAL, location, ADAPTER_NAME, VERSION);
            if (ChartboostAdapter.this.mActiveInterstitialSmash != null) {
                ChartboostAdapter.this.mActiveInterstitialSmash.onInterstitialAdOpened();
                ChartboostAdapter.this.mActiveInterstitialSmash.onInterstitialAdShowSucceeded();
            }

        }

        public void didInitialize() {
            Sdk.get().track.traceMediation(DELEGATE_DID_INITIALIZE, null, ADAPTER_NAME, VERSION);
            ChartboostAdapter.this.mInitiatedSuccessfully = true;
            ChartboostAdapter.this.reportInterstitialInitSuccess();
        }
    }
}

