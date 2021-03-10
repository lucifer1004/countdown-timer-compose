/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androiddevchallenge.ui.theme.MyTheme
import kotlinx.coroutines.InternalCoroutinesApi
import kotlin.math.absoluteValue

class TimerViewModel : ViewModel() {
    private var _timer: CountDownTimer? = null
    private val _isRunning = MutableLiveData(false)
    private val _isPaused = MutableLiveData(false)
    private val _isFinished = MutableLiveData(false)
    private val _hours = MutableLiveData(0L)
    private val _min = MutableLiveData(0L)
    private val _sec = MutableLiveData(0L)
    val isRunning: LiveData<Boolean> = _isRunning
    val isPaused: LiveData<Boolean> = _isPaused
    val isFinished: LiveData<Boolean> = _isFinished
    val hours: LiveData<Long> = _hours
    val min: LiveData<Long> = _min
    val sec: LiveData<Long> = _sec

    private fun getTimeAsSeconds(): Long {
        return _hours.value!! * 3600L + _min.value!! * 60L + _sec.value!!
    }

    fun setHours(newHours: Long) {
        _hours.value = when {
            newHours > 23L -> 0L
            newHours < 0L -> 23L
            else -> newHours
        }
    }

    fun setMin(newMin: Long) {
        _min.value = when {
            newMin > 59L -> 0L
            newMin < 0L -> 59L
            else -> newMin
        }
    }

    fun setSec(newSec: Long) {
        _sec.value = when {
            newSec > 59L -> 0L
            newSec < 0L -> 59L
            else -> newSec
        }
    }

    fun start() {
        if (getTimeAsSeconds() == 0L)
            return

        _isFinished.value = false
        _isRunning.value = true
        _isPaused.value = false
        _timer = object : CountDownTimer(getTimeAsSeconds() * 1000, 1 * 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (_sec.value!! > 0) {
                    _sec.value = _sec.value?.minus(1)
                } else if (_min.value!! > 0) {
                    _min.value = _min.value?.minus(1)
                    _sec.value = 59L
                } else if (_hours.value!! > 0) {
                    _hours.value = _hours.value?.minus(1)
                    _min.value = 59L
                    _sec.value = 59L
                }
            }

            override fun onFinish() {
                _isFinished.value = true
                stop()
            }
        }.start()
    }

    fun pause() {
        _isPaused.value = true
        _timer?.cancel()
    }

    fun stop() {
        _hours.value = 0L
        _min.value = 0L
        _sec.value = 0L
        _isRunning.value = false
        _isPaused.value = false
        _timer?.cancel()
    }
}

class MainActivity : AppCompatActivity() {
    val timerViewModel by viewModels<TimerViewModel>()

    @ExperimentalAnimationApi
    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp()
            }
        }
    }
}

@ExperimentalAnimationApi
@InternalCoroutinesApi
@Composable
fun MyApp(viewModel: TimerViewModel = viewModel()) {
    val isRunning: Boolean by viewModel.isRunning.observeAsState(false)
    val isPaused: Boolean by viewModel.isPaused.observeAsState(false)
    val isFinished: Boolean by viewModel.isFinished.observeAsState(false)

    Surface(color = MaterialTheme.colors.background) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopAppBar {
                Text(
                    text = "TIMER",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.W900,
                )
            }
            Column(
                Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Wheel("hours", viewModel.hours, { hours -> viewModel.setHours(hours) })
                    Wheel("min", viewModel.min, { min -> viewModel.setMin(min) })
                    Wheel("sec", viewModel.sec, { sec -> viewModel.setSec(sec) })
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { viewModel.stop() },
                        modifier = Modifier.width(100.dp),
                        shape = CircleShape,
                        enabled = isRunning,
                    ) {
                        Text(text = "Cancel")
                    }
                    OutlinedButton(
                        onClick = {
                            if (isRunning && !isPaused) {
                                viewModel.pause()
                            } else {
                                viewModel.start()
                            }
                        },
                        modifier = Modifier.width(100.dp),
                        shape = CircleShape,
                    ) {
                        Text(
                            text = when {
                                isPaused -> "Resume"
                                !isRunning -> "Start"
                                else -> "Pause"
                            }
                        )
                    }
                }
            }
        }
    }

    if (isFinished) {
        Toast.makeText(
            LocalContext.current,
            "Time's up!",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@InternalCoroutinesApi
@Composable
fun Wheel(
    label: String,
    digit: LiveData<Long>,
    setDigit: (Long) -> Unit,
    viewModel: TimerViewModel = viewModel()
) {
    val digit: Long by digit.observeAsState(0L)
    var lastScroll by remember { mutableStateOf(System.currentTimeMillis()) }
    val scrollState = rememberScrollableState { delta ->
        val now = System.currentTimeMillis()
        if (now - lastScroll > 50 && delta.absoluteValue > 10f) {
            if (delta > 1f) {
                setDigit(digit + 1)
            } else if (delta < -1f) {
                setDigit(digit - 1)
            }
            lastScroll = now
        }
        delta
    }
    val isRunning: Boolean by viewModel.isRunning.observeAsState(false)

    Row(
        Modifier
            .height(150.dp)
            .width(120.dp)
            .scrollable(
                state = scrollState,
                orientation = Orientation.Vertical,
                enabled = !isRunning,

            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Text(digit.toString().padStart(2, '0'), fontSize = 40.sp)
        Text(label)
    }
}

@ExperimentalAnimationApi
@InternalCoroutinesApi
@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp()
    }
}

@ExperimentalAnimationApi
@InternalCoroutinesApi
@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp()
    }
}
