package com.a9ts.a9ts.addappointment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.time.*

class StepTwoViewModel(friendUserId: String, friendFullName: String) :
    ViewModel(), KoinComponent {

    private val dateTimeInSeconds: Long
        get() = chosenDateInSeconds + chosenTimeInSeconds

    private val authService: AuthService by inject()
    private val databaseService: DatabaseService by inject()

    private val _friendUserId = MutableLiveData<String>()
    private val friendUserId: LiveData<String>
        get() = _friendUserId


    private var chosenDateInSeconds : Long
    private var chosenTimeInSeconds : Long

    private val _dateText = MutableLiveData("Today")
    val dateText: LiveData<String>
        get() = _dateText

    private val _timeText = MutableLiveData("$DEFAULT_TIME_HOURS:00")
    val timeText: LiveData<String>
        get() = _timeText

    private val _dateClicked = MutableLiveData<Boolean>()
    val dateClicked: LiveData<Boolean>
        get() = _dateClicked

    private val _timeClicked = MutableLiveData<Boolean>()
    val timeClicked: LiveData<Boolean>
        get() = _timeClicked


    private val _submitClicked = MutableLiveData<Boolean?>(null)
    val submitClicked: LiveData<Boolean?>
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


    fun onDateChanged(headerText: String, dateInSeconds: Long) {
        _dateText.value = headerText
        this.chosenDateInSeconds = dateInSeconds
    }

    

    fun onSubmit() {
        viewModelScope.launch {
            _submitClicked.value = databaseService.sendAppointment(
                authService.authUserId,
                friendUserId.value!!,
                dateTimeInSeconds
            )
        }
    }

    fun onSubmitDone() {
        _submitClicked.value = null
    }


    fun onTimeChanged(hour: Int, minute: Int) {
        val zeroPaddedMinutes = minute.toString().padStart(2, '0')
        _timeText.value = "$hour:$zeroPaddedMinutes"
        chosenTimeInSeconds = (hour * 3600 + minute * 60).toLong()
    }

    init {
        _friendFullName.value = friendFullName
        _friendUserId.value = friendUserId

        chosenDateInSeconds = LocalDate.now().toEpochDay()*24*3600

        Timber.d("chosenDateInSeconds: $chosenDateInSeconds")

        chosenTimeInSeconds = DEFAULT_TIME_HOURS * 3600
    }

    companion object{
        const val DEFAULT_TIME_HOURS = 23L
    }
}