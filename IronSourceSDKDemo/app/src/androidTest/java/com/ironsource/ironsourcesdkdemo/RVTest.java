package com.ironsource.ironsourcesdkdemo;

import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.web.webdriver.Locator;
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
import static android.support.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.getText;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webClick;
import static org.hamcrest.CoreMatchers.containsString;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RVTest {

    @Rule
    public ActivityTestRule<DemoActivity> activityTestRule =
            new ActivityTestRule<>(DemoActivity.class, false, false);

    @Test
    public void watchRewardedVideosACoupleOfTimes() {
        Intent intent = new Intent();
        intent.putExtra("type", "RV");
        activityTestRule.launchActivity(intent);
        IdlingRegistry.getInstance().register(new ProgressIdlingResource(activityTestRule.getActivity()));
        for (int i = 0; i < 3; i++) {
            activityTestRule.getActivity().waitWithTest(); // wait until next ad loads
            onView(withText(R.string.show_rv)).check(matches(isDisplayed())).perform(click());
            onWebView()
                    .withNoTimeout()
                    .withElement(findElement(Locator.ID, "pre-roll-container"))
                    .withContextualElement(findElement(Locator.ID, "pre-roll-cta"))
                    .check(webMatches(getText(), containsString("Earn Coins")))
                    .perform(webClick());
            activityTestRule.getActivity().waitWithTest();
            Espresso.pressBack();
        }

        activityTestRule.finishActivity();
    }
}