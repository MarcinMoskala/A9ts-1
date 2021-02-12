package com.a9ts.a9ts.addappointment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import java.time.ZoneId

class StepTwoViewModel(friendUserId: String, friendFullName: String) :
    ViewModel(), KoinComponent {

    private val dateTimeInMillis: Long
        get() = dateInMillis + timeInMillis

    private val authService: AuthService by inject()
    private val databaseService: DatabaseService by inject()

    private val _friendUserId = MutableLiveData<String>()
    val friendUserId: LiveData<String>
        get() = _friendUserId


    private var dateInMillis : Long
    private var timeInMillis : Long

    private val _dateText = MutableLiveData<String>("Today")
    val dateText: LiveData<String>
        get() = _dateText

    private val _timeText = MutableLiveData<String>("$DEFAULT_TIME_HOURS:00")
    val timeText: LiveData<String>
        get() = _timeText

    private val _dateClicked = MutableLiveData<Boolean>()
    val dateClicked: LiveData<Boolean>
        get() = _dateClicked

    private val _timeClicked = MutableLiveData<Boolean>()
    val timeClicked: LiveData<Boolean>
        get() = _timeClicked


    private val _submitClicked = MutableLiveData<Boolean>()
    val submitClicked: LiveData<Boolean>
        get() = _submitClicked


    private val _friendFullName = MutableLiveData<String>()
    val friendFullName: LiveData<String>
        get() = _friendFullName


    fun onDateClicked() {
        _dateClicked.value = true
    }

    fun onDateClickedDone() {
        _dateClicked.value = false
    }

    fun onTimeClicked() {
        _timeClicked.value = true
    }

    fun onTimeClickedDone() {
        _timeClicked.value = false
    }


    fun onDateChanged(headerText: String?, dateInMillis: Long) {
        _dateText.value = headerText
        this.dateInMillis = dateInMillis
    }

    fun onSubmit() {

//        databaseService.sendAppointment(authService.authUserId, friendUserId, friendFullName, dateTimeInMillis)
        _submitClicked.value = true
    }

    fun onSubmitDone() {
        _submitClicked.value = false
    }


    fun onTimeChanged(hour: Int, minute: Int) {
        val zeroPaddedMinutes = minute.toString().padStart(2, '0')
        _timeText.value = "$hour:$zeroPaddedMinutes"
        timeInMillis = hour.toLong()*3600*1000 + minute.toLong()*60*1000
    }

    init {
        _friendFullName.value = friendFullName
        _friendUserId.value = friendUserId

        val localDate = LocalDate.now()
        val zoneId = ZoneId.systemDefault()
        dateInMillis = localDate.atStartOfDay(zoneId).toEpochSecond() * 1000
        timeInMillis = DEFAULT_TIME_HOURS * 3600 * 1000
    }

    companion object{
        const val DEFAULT_TIME_HOURS = 16L
    }
}