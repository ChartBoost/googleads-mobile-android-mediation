package com.ironsource.ironsourcesdkdemo;

import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.IdlingResource;

import java.util.concurrent.TimeUnit;

public class ProgressIdlingResource implements IdlingResource {

    private ResourceCallback resourceCallback;
    private boolean idle = false;

    ProgressIdlingResource(DemoActivity activity){
        IdlingPolicies.setMasterPolicyTimeout(2, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(2, TimeUnit.MINUTES);
        activity.setProgressListener (new DemoActivity.ProgressListener() {
                                               @Override
                                               public void waitWithTest() {
                                                   idle = false;
                                               }

                                               @Override
                                               public void continueWithTest() {
                                                   if (resourceCallback == null) return;
                                                   resourceCallback.onTransitionToIdle();
                                                   idle = true;
                                               }
                                           });
    }

    @Override
    public String getName() {
        return "chartboost ad";
    }

    @Override
    public boolean isIdleNow() {
        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }
}

