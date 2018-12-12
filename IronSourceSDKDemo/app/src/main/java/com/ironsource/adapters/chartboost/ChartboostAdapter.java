package com.ironsource.adapters.chartboost;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.chartboost.sdk.CBLocation;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Libraries.CBLogging;
import com.chartboost.sdk.Model.CBError;
import com.chartboost.sdk.Sdk;
import com.ironsource.ironsourcesdkdemo.BuildConfig;
import com.ironsource.mediationsdk.AbstractAdapter;
import com.ironsource.mediationsdk.IntegrationData;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.sdk.InterstitialSmashListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoSmashListener;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.IronSourceConstants;

import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

import static com.chartboost.sdk.Trace.Trace.CHARTBOOST_CACHE_INTERSTITIAL;
import static com.chartboost.sdk.Trace.Trace.CHARTBOOST_CACHE_REWARDED_VIDEO;
import static com.chartboost.sdk.Trace.Trace.CHARTBOOST_SET_DELEGATE;
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

/**
 * IronSource's Chartboost adapter, as found in their download, chartboostadapter-4.1.7-sources.jar
 * Created by pnina.r on 3/8/16.
 * Modified by Chartboost to log to logentries.com + fixed behavior.
 */
class ChartboostAdapter extends AbstractAdapter {

    private static final String VERSION = "4.1.7";
    private static final String ADAPTER_NAME = "ironsource_adapter";

    private CbDelegate mDelegate;
    private Activity mActivity;

    private Boolean mAlreadyCalledInit = false;
    private boolean mDidInitSuccessfully = false;

    private final String APP_ID = "appID";
    private final String APP_SIGNATURE = "appSignature";
    private final String AD_LOCATION = "adLocation";

    private ConcurrentHashMap<String, InterstitialSmashListener> mLocationToIsListener;
    private ConcurrentHashMap<String, RewardedVideoSmashListener> mLocationToRvListener;

    public static ChartboostAdapter startAdapter(String providerName) {
        return new ChartboostAdapter(providerName);
    }

    private ChartboostAdapter(String providerName) {
        super(providerName);
        mLocationToIsListener = new ConcurrentHashMap<>();
        mLocationToRvListener = new ConcurrentHashMap<>();
    }

    public static IntegrationData getIntegrationData(Activity activity) {
        IntegrationData ret = new IntegrationData("Chartboost", VERSION);
        ret.activities = new String[]{"com.chartboost.sdk.CBImpressionActivity"};
        return ret;
    }

    public static String getAdapterSDKVersion(){
        String sdkVersion = null;
        try{
            sdkVersion = Chartboost.getSDKVersion();
        }
        catch (Exception ex){
        }
        return  sdkVersion;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getCoreSDKVersion() {
        return Chartboost.getSDKVersion();
    }


    protected synchronized void setConsent(boolean consent) {
        if (consent) {
            Chartboost.setPIDataUseConsent(mActivity, Chartboost.CBPIDataUseConsent.YES_BEHAVIORAL);
        } else {
            Chartboost.setPIDataUseConsent(mActivity, Chartboost.CBPIDataUseConsent.NO_BEHAVIORAL);
        }
    }


    // ********** Base **********

    @Override
    public void onResume(Activity activity) {
        if (activity != null) {
            mActivity = activity;
            Chartboost.onStart(activity);
            Chartboost.onResume(activity);
        }
    }

    @Override
    public void onPause(Activity activity) {
        if (activity != null) {
            Chartboost.onPause(activity);
            Chartboost.onStop(activity);
        }
    }

    // ********** Init **********

    private void init(final Activity activity, final String userId, final String type, final String appId, final String appSignature) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    if (!mAlreadyCalledInit) {
                        mAlreadyCalledInit = true;
                        mActivity = activity;
                        mDelegate = new CbDelegate();

                        Chartboost.setDelegate(mDelegate);
                        Chartboost.startWithAppId(activity, appId, appSignature);
                        // better late than never :)
                        Sdk.get().track.traceMediation(CHARTBOOST_SET_DELEGATE, null, ADAPTER_NAME, VERSION);

                        if (BuildConfig.DEBUG) {
                            Chartboost.setLoggingLevel(CBLogging.Level.ALL);
                        } else {
                            Chartboost.setLoggingLevel(CBLogging.Level.NONE);
                        }

                        if ("Unity".equals(getPluginType()) && !TextUtils.isEmpty(getPluginFrameworkVersion()))
                            Chartboost.setFramework(Chartboost.CBFramework.CBFrameworkUnity, getPluginFrameworkVersion());
                        Chartboost.setMediation(Chartboost.CBMediation.CBMediationironSource, VERSION);
                        Chartboost.setCustomId(userId);
                        Chartboost.setAutoCacheAds(true); // THIS IS ONLY FOR REWARDED, GAME NEEDS TO CACHE INTERSTITIALS MANUALLY

                        Chartboost.onCreate(activity);
                        Chartboost.onStart(activity);
                        Chartboost.onResume(activity);
                    }
                }
            }
        });
    }

    // ********** RewardedVideo **********

    @Override
    public synchronized void initRewardedVideo(Activity activity, String appKey, String userId, JSONObject config, RewardedVideoSmashListener listener) {
        if (TextUtils.isEmpty(config.optString(APP_ID)) || TextUtils.isEmpty(config.optString(APP_SIGNATURE))) {
            if (listener != null)
                listener.onRewardedVideoAvailabilityChanged(false);
            return;
        }
        String locationId = getLocationId(config);
        mLocationToRvListener.put(locationId, listener);
        init(activity, userId, IronSourceConstants.REWARDED_VIDEO_EVENT_TYPE, config.optString(APP_ID), config.optString(APP_SIGNATURE));

        if (mDidInitSuccessfully) {
            Sdk.get().track.traceMediation(CHARTBOOST_CACHE_REWARDED_VIDEO, "Default", ADAPTER_NAME, VERSION);
            Chartboost.cacheRewardedVideo(locationId);
        }
    }

    @Override
    public void fetchRewardedVideo(JSONObject config) { /* no-op */ }

    @Override
    public void showRewardedVideo(JSONObject config, RewardedVideoSmashListener listener) {
        final String locationId = getLocationId(config);

        if (Chartboost.hasRewardedVideo(locationId)) {
            Sdk.get().track.traceMediation(CHARTBOOST_SHOW_REWARDED_VIDEO, "Default", ADAPTER_NAME, VERSION);
            Chartboost.showRewardedVideo(locationId);
        } else {
            Sdk.get().track.traceMediation(CHARTBOOST_CACHE_REWARDED_VIDEO, "Default", ADAPTER_NAME, VERSION);
            Chartboost.cacheRewardedVideo(locationId);
            if (mLocationToRvListener.containsKey(locationId)) {
                mLocationToRvListener.get(locationId).onRewardedVideoAdShowFailed(ErrorBuilder.buildNoAdsToShowError(IronSourceConstants.REWARDED_VIDEO_AD_UNIT));
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(JSONObject config) {
        return Chartboost.hasRewardedVideo(getLocationId(config));
    }

    // ********** Interstitial **********

    @Override
    public synchronized void initInterstitial(Activity activity, String appKey, String userId, JSONObject config, InterstitialSmashListener listener) {
        if (TextUtils.isEmpty(config.optString(APP_ID)) || TextUtils.isEmpty(config.optString(APP_SIGNATURE))) {
            if (listener != null)
                listener.onInterstitialInitFailed(ErrorBuilder.buildInitFailedError("Missing params", IronSourceConstants.INTERSTITIAL_AD_UNIT));
            return;
        }
        String locationId = getLocationId(config);
        mLocationToIsListener.put(locationId, listener);
        init(activity, userId, IronSourceConstants.INTERSTITIAL_EVENT_TYPE, config.optString(APP_ID), config.optString(APP_SIGNATURE));

        if (mDidInitSuccessfully) {
            listener.onInterstitialInitSuccess();
        }
    }

    @Override
    public void loadInterstitial(JSONObject config, InterstitialSmashListener listener) {
        Handler h = new Handler(Looper.getMainLooper());
        final String locationId = getLocationId(config);
        h.post(new Runnable() {
            @Override
            public void run() {
                Sdk.get().track.traceMediation(CHARTBOOST_CACHE_INTERSTITIAL, "Default", ADAPTER_NAME, VERSION);
                Chartboost.cacheInterstitial(locationId);
            }
        });
    }

    @Override
    public void showInterstitial(JSONObject config, InterstitialSmashListener listener) {
        String locationId = getLocationId(config);

        if (Chartboost.hasInterstitial(locationId)) {
            Sdk.get().track.traceMediation(CHARTBOOST_SHOW_INTERSTITIAL, "Default", ADAPTER_NAME, VERSION);
            Chartboost.showInterstitial(locationId);
        } else {
            if (mLocationToIsListener.containsKey(locationId))
                mLocationToIsListener.get(locationId).onInterstitialAdShowFailed(ErrorBuilder.buildNoAdsToShowError(IronSourceConstants.INTERSTITIAL_AD_UNIT));
        }
    }

    @Override
    public boolean isInterstitialReady(JSONObject config) {
        return Chartboost.hasInterstitial(getLocationId(config));
    }

    // ********** Chartboost Delegates **********

    private class CbDelegate extends ChartboostDelegate {
        // Called after a rewarded video has been displayed on the screen.
        @Override
        public void didDisplayRewardedVideo(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_DISPLAY_REWARDED, location, ADAPTER_NAME, VERSION);
            if (mLocationToRvListener.get(location) != null) {
                mLocationToRvListener.get(location).onRewardedVideoAdOpened();
            }
        }

        // Called after a rewarded video has been loaded from the Chartboost API servers and cached locally
        @Override
        public void didCacheRewardedVideo(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_CACHE_REWARDED, location, ADAPTER_NAME, VERSION);
            if (mLocationToRvListener.get(location) != null) {
                mLocationToRvListener.get(location).onRewardedVideoAvailabilityChanged(true);
            }
        }

        // Called after a rewarded video has attempted to load from the Chartboost API servers but failed.
        @Override
        public void didFailToLoadRewardedVideo(String location, CBError.CBImpressionError error) {
            Sdk.get().track.traceMediation(DELEGATE_DID_FAIL_TO_LOAD_REWARDED, location, ADAPTER_NAME, VERSION);
            if (mLocationToRvListener.get(location) != null) {
                mLocationToRvListener.get(location).onRewardedVideoAvailabilityChanged(Chartboost.hasRewardedVideo(location));
            }

        }

        // Called after a rewarded video has been dismissed.
        @Override
        public void didDismissRewardedVideo(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_DISMISS_REWARDED, location, ADAPTER_NAME, VERSION);
            if (mLocationToRvListener.get(location) != null) {
                mLocationToRvListener.get(location).onRewardedVideoAdClosed();
            }
        }

        // Called after a rewarded video has been closed.
        @Override
        public void didCloseRewardedVideo(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_CLOSE_REWARDED, location, ADAPTER_NAME, VERSION);
            // FIXME -- why IronSource is not calling onRewardedVideoAdClosed() here?
            Sdk.get().track.traceMediation(CHARTBOOST_CACHE_REWARDED_VIDEO, location, ADAPTER_NAME, VERSION);
            mLocationToRvListener.get(location).onRewardedVideoAvailabilityChanged(false);
        }

        // Called after a rewarded video has been clicked.
        @Override
        public void didClickRewardedVideo(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_CLICK_REWARDED, location, ADAPTER_NAME, VERSION);
            if (mLocationToRvListener.get(location) != null)
                mLocationToRvListener.get(location).onRewardedVideoAdClicked();
        }

        // Called after a rewarded video has been viewed completely and user is eligible for reward.
        @Override
        public void didCompleteRewardedVideo(String location, int reward) {
            Sdk.get().track.traceMediation(DELEGATE_DID_COMPLETE_REWARDED, location, ADAPTER_NAME, VERSION);
            if (mLocationToRvListener.get(location) != null)
                mLocationToRvListener.get(location).onRewardedVideoAdRewarded();
        }

        // Implement to be notified of when a video will be displayed on the screen for a given CBLocation. You can then do things like mute effects and sounds.
        @Override
        public void willDisplayVideo(String location) {
            Sdk.get().track.traceMediation(DELEGATE_WILL_DISPLAY_INTERSTITIAL, location, ADAPTER_NAME, VERSION);
        }

        // Called after an interstitial has been loaded from the Chartboost API
        @Override
        public void didCacheInterstitial(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_CACHE_INTERSTITIAL, location, ADAPTER_NAME, VERSION);
            if (mLocationToIsListener.get(location) != null)
                mLocationToIsListener.get(location).onInterstitialAdReady();
        }

        // Called after an interstitial has attempted to load from the Chartboost API servers but failed.
        @Override
        public void didFailToLoadInterstitial(String location, CBError.CBImpressionError error) {
            Sdk.get().track.traceMediation(DELEGATE_DID_FAIL_TO_LOAD_INTERSTITIAL, location, ADAPTER_NAME, VERSION);
            if (mLocationToIsListener.get(location) != null)
                mLocationToIsListener.get(location).onInterstitialAdLoadFailed(ErrorBuilder.buildLoadFailedError(error.toString()));
        }

        // Called after an interstitial has been dismissed.
        @Override
        public void didDismissInterstitial(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_DISMISS_INTERSTITIAL, location, ADAPTER_NAME, VERSION);
            if (mLocationToIsListener.get(location) != null)
                mLocationToIsListener.get(location).onInterstitialAdClosed();
        }

        // Called after an interstitial has been closed.
        @Override
        public void didCloseInterstitial(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_CLOSE_INTERSTITIAL, location, ADAPTER_NAME, VERSION);
            // FIXME -- why IronSource is not calling onInterstitialAdClosed() here?
        }

        // Called after an interstitial has been clicked.
        @Override
        public void didClickInterstitial(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_CLICK_INTERSTITIAL, location, ADAPTER_NAME, VERSION);
            if (mLocationToIsListener.get(location) != null)
                mLocationToIsListener.get(location).onInterstitialAdClicked();
        }

        // Called after an interstitial has been displayed on the screen.
        @Override
        public void didDisplayInterstitial(String location) {
            Sdk.get().track.traceMediation(DELEGATE_DID_DISPLAY_INTERSTITIAL, location, ADAPTER_NAME, VERSION);
            if (mLocationToIsListener.get(location) != null) {
                mLocationToIsListener.get(location).onInterstitialAdOpened();
                mLocationToIsListener.get(location).onInterstitialAdShowSucceeded();
            }
        }

        //Called after the SDK has been successfully initialized
        @Override
        public void didInitialize() {
            synchronized (this) {
                Sdk.get().track.traceMediation(DELEGATE_DID_INITIALIZE, null, ADAPTER_NAME, VERSION);
                mDidInitSuccessfully = true;

                for (String key : mLocationToRvListener.keySet()) {
                    Sdk.get().track.traceMediation(CHARTBOOST_CACHE_REWARDED_VIDEO, "Default", ADAPTER_NAME, VERSION);
                    Chartboost.cacheRewardedVideo(key);
                }

                for (String key : mLocationToIsListener.keySet()) {
                    mLocationToIsListener.get(key).onInterstitialInitSuccess();
                }
            }
        }
    }

    // ********** Helpers **********


    private String getLocationId(JSONObject config) {
        String locationId = config.optString(AD_LOCATION);
        if (TextUtils.isEmpty(locationId)) {
            locationId = CBLocation.LOCATION_DEFAULT;
        }
        return locationId;
    }

}
