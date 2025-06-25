package xyz.ashnove.justatimer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import xyz.ashnove.justatimer.data.HistoryRepository
import xyz.ashnove.justatimer.data.ThemeRepository

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val historyRepository = HistoryRepository(application)
    private val themeRepository = ThemeRepository(application)

    private val _time = MutableStateFlow(600L)
    val time = _time.asStateFlow()

    private val _duration = MutableStateFlow(600L) // 10 minutes default
    val duration = _duration.asStateFlow()

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Stopped)
    val timerState = _timerState.asStateFlow()

    private val _history = MutableStateFlow<List<Pair<Long, Long>>>(emptyList())
    val history = _history.asStateFlow()

    private val _theme = MutableStateFlow("Black")
    val theme = _theme.asStateFlow()

    private val availableThemes = listOf("Black", "White", "Rose")

    private var timerJob: Job? = null

    init {
        _time.value = _duration.value
        historyRepository.history
            .onEach { _history.value = it }
            .launchIn(viewModelScope)
        themeRepository.theme
            .onEach { _theme.value = it }
            .launchIn(viewModelScope)
    }

    fun setDuration(seconds: Long) {
        if (timerState.value != TimerState.Running) {
            _duration.value = seconds
            _time.value = seconds
            if(timerState.value == TimerState.Paused){
                _timerState.value = TimerState.Stopped
            }
        }
    }

    fun adjustMinutes(delta: Int) {
        if (timerState.value == TimerState.Running) return
        val currentMinutes = _duration.value / 60
        val currentSeconds = _duration.value % 60
        var newMinutes = currentMinutes + delta
        if (newMinutes < 0) newMinutes = 99
        if (newMinutes > 99) newMinutes = 0
        val newDuration = newMinutes * 60 + currentSeconds
        setDuration(newDuration)
    }

    fun adjustSeconds(delta: Int) {
        if (timerState.value == TimerState.Running) return
        val currentMinutes = _duration.value / 60
        val currentSeconds = _duration.value % 60
        var newSeconds = currentSeconds + delta
        if (newSeconds < 0) newSeconds = 59
        if (newSeconds > 59) newSeconds = 0
        val newDuration = currentMinutes * 60 + newSeconds
        setDuration(newDuration)
    }

    fun toggleTheme() {
        val currentThemeIndex = availableThemes.indexOf(_theme.value)
        val nextThemeIndex = (currentThemeIndex + 1) % availableThemes.size
        val nextTheme = availableThemes[nextThemeIndex]
        viewModelScope.launch {
            themeRepository.setTheme(nextTheme)
        }
    }

    fun deleteHistory(timestamp: Long) {
        viewModelScope.launch {
            historyRepository.deleteHistory(timestamp)
        }
    }

    fun startTimer() {
        if (timerState.value != TimerState.Running) {
            _timerState.value = TimerState.Running
            timerJob = viewModelScope.launch {
                while (_time.value > 0) {
                    delay(1000)
                    _time.value--
                }
                if (_time.value <= 0) {
                    stopTimer()
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = TimerState.Paused
    }

    fun stopTimer() {
        val wasRunningOrPaused = timerState.value == TimerState.Running || timerState.value == TimerState.Paused
        timerJob?.cancel()
        _timerState.value = TimerState.Stopped
        _time.value = _duration.value
        if (wasRunningOrPaused) {
            viewModelScope.launch {
                historyRepository.addHistory(_duration.value)
            }
        }
    }
}

sealed class TimerState {
    data object Running : TimerState()
    data object Paused : TimerState()
    data object Stopped : TimerState()
} 