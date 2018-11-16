package com.ironsource.ironsourcesdkdemo;

import android.support.test.espresso.IdlingResource;

public class ProgressIdlingResource implements IdlingResource {

    private ResourceCallback resourceCallback;
    private boolean idle = false;

    ProgressIdlingResource(DemoActivity activity){
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
        return "My idling resource";
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

