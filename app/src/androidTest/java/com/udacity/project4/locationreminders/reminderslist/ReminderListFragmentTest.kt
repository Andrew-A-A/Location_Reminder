package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeReminderDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
@MediumTest
@ExperimentalCoroutinesApi
class ReminderListFragmentTest {
    private lateinit var viewModel: RemindersListViewModel
    private lateinit var dataSource: ReminderDataSource


    @Before
    fun initViewModel() {
        //Create 3 Reminders
        val reminder1 = ReminderDTO(
            "Reminder1",
            "Description1", "Home", 37.42219, -122.08400, "test1"
        )
        val reminder2 = ReminderDTO(
            "Reminder2",
            "Description2", "Cafe", 37.41779463858377, -122.13033537419037, "test2"
        )
        val reminder3 = ReminderDTO(
            "Reminder3",
            "Description2", "Park", 37.41320833153759, -122.13693756068962, "test3"
        )
        dataSource = FakeReminderDataSource(mutableListOf(reminder1, reminder2, reminder3))
        //Create a view model and add reminders in the reminders list inside it
        viewModel = RemindersListViewModel(Application(), dataSource)
    }

    @Test
    fun clickAddTask_navigateToSaveReminderFragment() {
        runBlockingTest {
            //GIVEN - On Reminders list screen
            val scenario =
                launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
            val navController = mock(NavController::class.java)
            scenario.onFragment {
                Navigation.setViewNavController(it.view!!, navController)
            }

            //WHEN - Click "Add reminder button"
            onView(withId(R.id.addReminderFAB))
                .perform(click())

            //THEN - Verify that we navigate to the "Save Reminder" Screen
            verify(navController).navigate(
                ReminderListFragmentDirections.toSaveReminder()
            )
        }

    }

    @Test
    fun noTasksIsVisible_emptyList() {
        runBlockingTest {
            //GIVEN - On Reminders list screen
            val scenario =
                launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
            val navController = mock(NavController::class.java)
            scenario.onFragment {
                Navigation.setViewNavController(it.view!!, navController)
            }

            //WHEN - View model has no tasks
            viewModel.deleteReminders()

            //THEN - Verify that we navigate to the "Save Reminder" Screen
            onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        }
    }
}