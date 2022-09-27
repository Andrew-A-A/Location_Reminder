package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*


@RunWith(AndroidJUnit4::class)
class SelectLocationFragmentTest{

    @Test
    fun clickSave_NoLocationSelected(){
        //GIVEN - on Select location fragment
       launchFragmentInContainer<SelectLocationFragment>(Bundle(),R.style.AppTheme)
        //WHEN - Click save button without selecting a location
        onView(withId(R.id.save_btn)).perform(click())
        //THEN - Toast tells user to select a location appear
        onView(withText(R.string.please_select_location)).check(matches(isDisplayed()))
    }

    @Test
    fun clickSave_navigateToSaveLocationFragment(){
        //GIVEN - on select location fragment
        val scenario=launchFragmentInContainer<SelectLocationFragment>(Bundle(),R.style.AppTheme)
        val navController=mock(NavController::class.java)
        scenario.onFragment{
            Navigation.setViewNavController(it.view!!,navController)
        }
        //WHEN - Select location and click save button
            //Add marker on the map
        onView(withId(R.id.map)).perform(longClick())
            //Click save button
        onView(withId(R.id.save_btn)).perform(click())
            //Click Yes on the dialog box to confirm the selected location
        onView(withText("Yes")).perform(click())
        //THEN - navigate back to the SaveReminder Fragment
        verify(navController).popBackStack()
    }
    @Test
    fun clickSave_clickNoToTheConfirmDialog(){
        //GIVEN - on select location fragment
        val scenario=launchFragmentInContainer<SelectLocationFragment>(Bundle(),R.style.AppTheme)
        val navController=mock(NavController::class.java)
        scenario.onFragment{
            Navigation.setViewNavController(it.view!!,navController)
        }
        //WHEN - Select location and click save button
            //Add marker on the map
        onView(withId(R.id.map)).perform(longClick())
            //Click save button
        onView(withId(R.id.save_btn)).perform(click())
            //Click Yes on the dialog box to confirm the selected location
        onView(withText("No")).perform(click())
        //THEN - dialog box dismiss and no navigation happens
        onView(withText("No")).check(doesNotExist())
        verifyNoInteractions(navController)
    }

}

