package com.udacity.project4.authentication

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import org.hamcrest.CoreMatchers.`is`
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class AuthenticationActivityTest{
    @Test
    fun clickLoginButton_NavigateToFirebaseLogin(){
        //GIVEN - On the Authentication activity
        launchActivity<AuthenticationActivity>().use {
            it.moveToState(Lifecycle.State.RESUMED)

        //WHEN - Click "Login button"
            onView(withId(R.id.login_btn)).perform(click())

        //THEN - Verify change of activity lifecycle as we navigate to the Fire base Login
            assertThat(it.state,`is` (Lifecycle.State.CREATED))
        }

    }
}