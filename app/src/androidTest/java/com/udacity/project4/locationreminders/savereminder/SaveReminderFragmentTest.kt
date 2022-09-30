package com.udacity.project4.locationreminders.savereminder

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.udacity.project4.R
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.AllOf.allOf
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not


@MediumTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class SaveReminderFragmentTest{


    @Test
    fun clickSave_NoLocationSelected(){
        //GIVEN - On the save reminder screen
        launchFragmentInContainer<SaveReminderFragment>(Bundle(),R.style.AppTheme)

        //WHEN - write title then click on save reminder button
        onView(withId(R.id.reminderTitle)).perform(typeText("Title1"))
        onView(withId(R.id.reminderTitle)).perform(closeSoftKeyboard())
        onView(withId(R.id.saveReminder)).perform(click())
        //THEN - verify that a snack bar appeared to tell user to select location
        onView(withText(R.string.err_select_location))
            .check(matches(isDisplayed()))
    }

    @Test
    fun clickSelectLocation(){
        runBlockingTest {
        //GIVEN - On the save reminder screen
        val scenario= launchFragmentInContainer<SaveReminderFragment>(Bundle(),R.style.AppTheme)
        val navController= mock(NavController::class.java)
        scenario.onFragment{
            Navigation.setViewNavController(it.view!!,navController)
        }
        //WHEN - click Select Location
        onView(withId(R.id.selectLocation)).perform(click())

        //THEN - Verify that we navigate to select location
        verify(navController).navigate(SaveReminderFragmentDirections
            .actionSaveReminderFragmentToSelectLocationFragment())

    }
    }

}