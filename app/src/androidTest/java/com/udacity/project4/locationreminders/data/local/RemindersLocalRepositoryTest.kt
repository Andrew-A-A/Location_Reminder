package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class RemindersLocalRepositoryTest{

    //Subject under test
    private lateinit var repository: RemindersLocalRepository


    private lateinit var database: RemindersDatabase


    //Execute each task synchronously using architecture components
    @get:Rule
    var instantTaskExecutorRule= InstantTaskExecutorRule()


    // Build in-memory database as information stored in memory will disappear when the tests end
    @Before
    fun initializeDatabase(){
        database= Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        repository= RemindersLocalRepository(database.reminderDao(),Dispatchers.Unconfined)
    }

    @After
    fun closeDatabase(){
        database.close()
    }
    @Test
    fun getReminder_getReminderFromRepositoryById(){
        runBlockingTest {
           //GIVEN - add one reminder to the repository
            val addedReminder= ReminderDTO("Reminder1",
                "Description1","Home", 37.42219,-122.08400,"test1")
            repository.saveReminder(addedReminder)

            //WHEN - get the reminder added
            val loadedReminder=repository.getReminder(addedReminder.id) as Result.Success

            //THEN - verify the loaded reminder data
            assertThat(loadedReminder.data.id , `is`(addedReminder.id))
            assertThat(loadedReminder.data.description , `is`(addedReminder.description))
            assertThat(loadedReminder.data.title , `is`(addedReminder.title))
            assertThat(loadedReminder.data.location , `is`(addedReminder.location))
            assertThat(loadedReminder.data.longitude , `is`(addedReminder.longitude))
            assertThat(loadedReminder.data.latitude , `is`(addedReminder.latitude))
        }

    }

    @Test
    fun getReminder_TryToGetReminderDoesNotExist() {
     runBlockingTest {
        //GIVEN - repository with no data
        val reminder1=ReminderDTO("Reminder1",
            "Description1","Home", 37.42219,-122.08400,"test1")

        //WHEN - try to load data with id not in the repository
        val message = (repository.getReminder(reminder1.id) as? Result.Error)?.message

        //THEN - verify the error message
        Assert.assertThat<String>(message, CoreMatchers.notNullValue())
        assertThat(message,`is`("Reminder not found!"))
     }
    }

    @Test
    fun getReminders_GetListOfRemindersFromRepository(){
        runBlockingTest{
            //GIVEN - Repository with 3 reminders added
            //Create 3 Reminders
            val reminder1=ReminderDTO("Reminder1",
                "Description1","Home", 37.42219,-122.08400,"test1")
            val reminder2=ReminderDTO("Reminder2",
                "Description2","Cafe",37.41779463858377,-122.13033537419037,"test2")
            val reminder3=ReminderDTO("Reminder3",
                "Description2","Park", 37.41320833153759,-122.13693756068962,"test3")
            repository.saveReminder(reminder1)
            repository.saveReminder(reminder2)
            repository.saveReminder(reminder3)

            //WHEN - try to load reminders list
            val loadedReminders=repository.getReminders() as Result.Success

            //THEN - verify that size of loaded list is 3
            //(Same as number of added reminders)
            assertThat(loadedReminders.data.size,`is`(3))

        }
    }
    @Test
    fun deleteAllReminders_GetEmptyList(){
        runBlockingTest {
            //GIVEN - add one reminder to the repository
            val addedReminder= ReminderDTO("Reminder1",
                "Description1","Home", 37.42219,-122.08400,"test1")
            repository.saveReminder(addedReminder)

            //WHEN - Delete all the reminders added
            repository.deleteAllReminders()
            val loadedReminders=repository.getReminders() as Result.Success

            //THEN - verify that the loaded list is empty
            assertThat(loadedReminders.data.size,`is`(0))
        }
    }



}