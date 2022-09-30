package com.udacity.project4.locationreminders.reminderslist

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.FakeReminderDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
/**
* Unit tests for implementation of [RemindersListViewModel]
 * */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
 class RemindersListViewModelTest{
  // Subject under test
  private lateinit var remindersViewModel: RemindersListViewModel


 // Declare fake data source
  private lateinit var fakeDataSource: FakeReminderDataSource



 //Set the main coroutines dispatcher for unit testing
 @ExperimentalCoroutinesApi
 @get:Rule
 var mainCoroutineRule= MainCoroutineRule()

 // Execute each task synchronously using Architecture Components
 @get:Rule
 var instantTaskExecutorRule=InstantTaskExecutorRule()

 // Tests for `loadReminders()` function

 @Test
 fun loadReminders_getRemindersList(){
 //GIVEN

    //Create 3 Reminders
  val reminder1=ReminderDTO("Reminder1",
   "Description1","Home", 37.42219,-122.08400,"test1")
  val reminder2=ReminderDTO("Reminder2",
   "Description2","Cafe",37.41779463858377,-122.13033537419037,"test2")
  val reminder3=ReminderDTO("Reminder3",
   "Description2","Park", 37.41320833153759,-122.13693756068962,"test3")

   //Add reminders to mutable list
  val reminderList= mutableListOf(reminder1,reminder2,reminder3)

   //Initialize fake data source with reminders list
  fakeDataSource= FakeReminderDataSource(reminderList)

   //Initialize view model using fake data source
  remindersViewModel= RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)

  //When
    //Now try to load reminders from the view model
  remindersViewModel.loadReminders()

  //THEN
  val viewModelListSize=remindersViewModel.remindersList.getOrAwaitValue().size
    //Make sure that the size of list loaded is the same as previously declared reminders list
   assertThat(viewModelListSize,`is`(reminderList.size))
 }

 @Test
 fun loadReminders_emptyList(){
  //GIVEN
  val reminderList= mutableListOf<ReminderDTO>()

   //Initialize fake data source with empty reminders list
  fakeDataSource= FakeReminderDataSource(reminderList)

   //Initialize view model using fake data source
  remindersViewModel= RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)

  //When
   //Now try to load reminders from the view model
  remindersViewModel.loadReminders()

  //THEN
  val viewModelList=remindersViewModel.remindersList.getOrAwaitValue()
   //Make sure that the loaded list is empty
  assertThat(viewModelList,`is`(emptyList<ReminderDTO>()))
 }

 @Test
 fun loadReminders_Error(){
  //GIVEN
  val reminderList= mutableListOf<ReminderDTO>()



  //Initialize fake data source with null list
  fakeDataSource= FakeReminderDataSource(reminderList)

  fakeDataSource.setReturnError(true)

  //Initialize view model using fake data source
  remindersViewModel= RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)

  //When
  //Now try to load reminders from the view model
  remindersViewModel.loadReminders()

  //THEN
  val viewModelError=remindersViewModel.error.getOrAwaitValue()
  //Make sure that the loaded list is null
  assertThat(viewModelError,`is`(true))
 }

//Test `deleteReminders()` function
 @Test
 fun  deleteReminders_SendRemindersListThenClear(){
 //GIVEN

  //Create 3 Reminders
 val reminder1=ReminderDTO("Reminder1",
  "Description1","Home", 37.42219,-122.08400,"test1")
 val reminder2=ReminderDTO("Reminder2",
  "Description2","Cafe",37.41779463858377,-122.13033537419037,"test2")
 val reminder3=ReminderDTO("Reminder3",
  "Description2","Park", 37.41320833153759,-122.13693756068962,"test3")

  //Add reminders to mutable list
 val reminderList= mutableListOf(reminder1,reminder2,reminder3)

  //Initialize fake data source with reminders list
 fakeDataSource= FakeReminderDataSource(reminderList)

  //Initialize view model using fake data source
 remindersViewModel= RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)

 //WHEN
 remindersViewModel.deleteReminders()

 //THEN
 val viewModelListSize= fakeDataSource.reminders?.size
 //Make sure that the size of list loaded is the same as previously declared reminders list
 assertThat(viewModelListSize,`is`(0))
 }

 // Test for loading
 @Test
 fun check_loading(){
  //GIVEN
  val reminderList= mutableListOf<ReminderDTO>()


  //Initialize fake data source with empty reminders list
  fakeDataSource= FakeReminderDataSource(reminderList)

  //Initialize view model using fake data source
  remindersViewModel= RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)

  //When
   //First pause coroutine
   mainCoroutineRule.pauseDispatcher()

   //load reminders from the view model
  remindersViewModel.loadReminders()

  //THEN
  var showLoading=remindersViewModel.showLoading.getOrAwaitValue()

  //Make sure that the "ShowLoading" Boolean value is true
  assertThat(showLoading,`is`(true))

  mainCoroutineRule.resumeDispatcher()

  showLoading=remindersViewModel.showLoading.getOrAwaitValue()
  assertThat(showLoading,`is`(false))
 }


 //Clean up
 @After
 fun stop_Koin(){
  stopKoin()
 }

}