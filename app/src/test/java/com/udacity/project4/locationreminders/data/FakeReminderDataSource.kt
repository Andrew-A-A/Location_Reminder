package com.udacity.project4.locationreminders.data
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Test double for Reminders data source

class FakeReminderDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()): ReminderDataSource {


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
     reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error(
            Exception("No Reminders Found").message
        )
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
      reminders!!.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        reminders?.find { it.id == id }?.let {
            return Result.Success(it)
        }
        return Result.Error(
            Exception("Not found Reminder").message
        )
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}