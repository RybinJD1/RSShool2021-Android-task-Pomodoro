package com.rsschool.pomodoro.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.rsschool.pomodoro.COMMAND_ID
import com.rsschool.pomodoro.COMMAND_START
import com.rsschool.pomodoro.COMMAND_STOP
import com.rsschool.pomodoro.STARTED_TIMER_TIME_MS
import com.rsschool.pomodoro.databinding.ActivityMainBinding
import com.rsschool.pomodoro.services.ForegroundService
import com.rsschool.pomodoro.timer.Timer
import com.rsschool.pomodoro.timer.TimerAdapter
import com.rsschool.pomodoro.timer.TimerListener

class MainActivity : AppCompatActivity(), TimerListener, LifecycleObserver {
    private lateinit var binding: ActivityMainBinding
    private val timerAdapter = TimerAdapter(this)
    private val timers = mutableListOf<Timer>()
    private var nextId = 0
    private var currentTime = 0L
    private var index: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timerAdapter
        }

        binding.addNewTimerButton.setOnClickListener {
            val time = binding.inputTime.text.toString()
            if (time != "") {
                timers.add(Timer(nextId++, time.toLong() * 1000 * 60, false))
                timerAdapter.submitList(timers.toList())
                binding.inputTime.text = null
            }
        }
    }

    override fun start(id: Int) {
        timers.map {
            if (it.isStarted) stop(it.id)
        }
        index = timers.indexOf(timers.find { it.id == id })
        timers[index].run {
            isStarted = true
            finishTime = System.currentTimeMillis() + currentMs
            currentTime = finishTime
        }
        timerAdapter.submitList(timers.toList())
    }

    override fun stop(id: Int) {
        val index = timers.indexOf(timers.find { it.id == id })
        timers[index].run {
            currentMs = finishTime - System.currentTimeMillis()
            isStarted = false
        }
        currentTime = 0L
        timerAdapter.submitList(timers.toList())
    }

    override fun delete(id: Int) {
        stop(id)
        onAppForegrounded()
        timers.remove(timers.find { it.id == id })
        timerAdapter.submitList(timers.toList())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startIntent.putExtra(STARTED_TIMER_TIME_MS, currentTime)
        startService(startIntent)
    }

    override fun onDestroy() {
        if (index != -1) {
            stop(index)
        }
        onAppForegrounded()
        super.onDestroy()
    }
}