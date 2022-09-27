package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

/*
* Tests for [RemindersDao]
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {
    private lateinit var database: RemindersDatabase

    //Execute each task synchronously using architecture components
    @get:Rule
    var instantTaskExecutorRule= InstantTaskExecutorRule()


   // Build in-memory database as information stored in memory will disappear when the tests end
    @Before
    fun initializeDatabase(){
        database= Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    // Delete the database from memory after the tests end
    @After
    fun closeDatabase(){
        database.close()
    }

    @Test
    fun saveReminderAndGetById(){
      runBlockingTest {
          //GIVEN - insert a Reminder
          val addedReminder=ReminderDTO("Reminder",
              "Description","Location", 37.42219,-122.08400)
          database.reminderDao().saveReminder(addedReminder)

          //WHEN - get the reminder from database by id
          val loadedReminder=database.reminderDao().getReminderById(addedReminder.id)

          //THEN - verify that the loaded reminder contains same data of added reminder
          assertThat(loadedReminder?.id,`is`(addedReminder.id))
          assertThat(loadedReminder?.title,`is`(addedReminder.title))
          assertThat(loadedReminder?.latitude,`is`(addedReminder.latitude))
          assertThat(loadedReminder?.longitude,`is`(addedReminder.longitude))
          assertThat(loadedReminder?.location,`is`(addedReminder.location))
          assertThat(loadedReminder?.description,`is`(addedReminder.description))
      }

    }

    @Test
    fun saveThreeRemindersAndGetList(){
        runBlockingTest {
            //GIVEN - insert a Reminder
            //Create 3 Reminders
            val reminder1=ReminderDTO("Reminder1",
                "Description1","Home", 37.42219,-122.08400,"test1")
            val reminder2=ReminderDTO("Reminder2",
                "Description2","Cafe",37.41779463858377,-122.13033537419037,"test2")
            val reminder3=ReminderDTO("Reminder3",
                "Description2","Park", 37.41320833153759,-122.13693756068962,"test3")

            database.reminderDao().saveReminder(reminder1)
            database.reminderDao().saveReminder(reminder2)
            database.reminderDao().saveReminder(reminder3)

            //WHEN - get the reminders list from database
            val loadedReminders=database.reminderDao().getReminders()

            //THEN - verify that the loaded reminders list size is 3
            // (Same as number of added reminders)
            assertThat(loadedReminders.size,`is`(3))
        }
    }

    @Test
    fun saveThreeRemindersThenDeleteAll(){
        runBlockingTest {
            //GIVEN - insert a Reminder
            //Create 3 Reminders
            val reminder1=ReminderDTO("Reminder1",
                "Description1","Home", 37.42219,-122.08400,"test1")
            val reminder2=ReminderDTO("Reminder2",
                "Description2","Cafe",37.41779463858377,-122.13033537419037,"test2")
            val reminder3=ReminderDTO("Reminder3",
                "Description2","Park", 37.41320833153759,-122.13693756068962,"test3")

            database.reminderDao().saveReminder(reminder1)
            database.reminderDao().saveReminder(reminder2)
            database.reminderDao().saveReminder(reminder3)

            //WHEN - Delete all Reminders
            database.reminderDao().deleteAllReminders()
            val loadedReminders=database.reminderDao().getReminders()

            //THEN - verify that the loaded reminders list is empty
            assertThat(loadedReminders.size,`is`(0))
        }
    }

}