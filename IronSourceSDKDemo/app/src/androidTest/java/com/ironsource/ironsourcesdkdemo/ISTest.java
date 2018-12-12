package com.ironsource.ironsourcesdkdemo;

import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ISTest {

    @Rule
    public ActivityTestRule<DemoActivity> activityTestRule =
            new ActivityTestRule<>(DemoActivity.class, false, false);

    @Test
    public void openInterstitialACoupleOfTimes() {
        Intent intent = new Intent();
        intent.putExtra("type", "IS");
        activityTestRule.launchActivity(intent);
        IdlingRegistry.getInstance().register(new ProgressIdlingResource(activityTestRule.getActivity()));
        activityTestRule.getActivity().waitWithTest();
        for (int i = 0; i < 3; i++) {
            onView(withText(R.string.show_is)).check(matches(isDisplayed())).perform(click());
            Espresso.pressBack();
        }
        activityTestRule.finishActivity();
    }
}