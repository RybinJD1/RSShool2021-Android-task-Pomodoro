package com.rsschool.pomodoro.timer

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.rsschool.pomodoro.*
import com.rsschool.pomodoro.databinding.TimerItemBinding
import kotlinx.coroutines.*


class TimerViewHolder(
    private val binding: TimerItemBinding,
    private val listener: TimerListener,
    private val context: Context
) :
    RecyclerView.ViewHolder(binding.root) {

    private var countDownTimer: CountDownTimer? = null


    fun bind(timer: Timer) {
        binding.time.text = displayTime(timer.currentMs)
        isVisible(true)
        if (timer.currentMs <= 0L) finish(timer)
        else {
            binding.customView.setPeriod(timer.currentMs)
            updateCustomTimer(timer.currentMs)
            if (timer.isStarted) startTimer(timer)
            else stopTimer()
        }
        initButtonsListeners(timer)
    }

    private fun initButtonsListeners(timer: Timer) {
        binding.startPauseButton.setOnClickListener {
            if (timer.isStarted) {
                binding.startPauseButton.text = START
                listener.stop(timer.id)
            } else {
                binding.startPauseButton.text = STOP
                listener.start(timer.id)
                startTimer(timer)
            }
        }

        binding.deleteButton.setOnClickListener {
            listener.delete(timer.id)
        }
    }

    private fun startTimer(timer: Timer) {
        binding.startPauseButton.text = STOP
        countDownTimer?.cancel()
        countDownTimer = getCountDownTimer(timer)
        countDownTimer?.start()
        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer() {
        binding.startPauseButton.text = START
        countDownTimer?.cancel()
        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(timer: Timer): CountDownTimer {
        return object : CountDownTimer(timer.currentMs, INTERVAL) {

            override fun onTick(millisUntilFinished: Long) {
                if (timer.isStarted) {
                    if (timer.currentMs <= 0L) {
                        onFinish()
                    }
                    timer.currentMs = timer.finishTime - System.currentTimeMillis()
                    binding.time.text = displayTime(timer.currentMs)
                    updateCustomTimer(timer.currentMs)
                } else {
                    stopTimer()
                }
            }

            override fun onFinish() {
                timer.isStarted = false
                finish(timer)
            }
        }
    }

    private fun finish(timer: Timer) {
        listener.stop(timer.id)
        countDownTimer?.cancel()
        binding.time.text = START_TIME
        isVisible(false)
        vibrateAndSoundNotify(context)
        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun vibrateAndSoundNotify(context: Context) {
        val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        RingtoneManager.getRingtone(context, notification).play()
        val vibrator = getSystemService(context, Vibrator::class.java)
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= 26) {
                it.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(1000)
            }
        }
    }

    private fun updateCustomTimer(time: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.customView.setCurrent(time)
            delay(INTERVAL)
        }
    }

    private fun isVisible(visible: Boolean) {
        if (visible) {
            binding.timerLayout.setBackgroundColor(Color.TRANSPARENT)
        } else {
            binding.timerLayout.setBackgroundColor(Color.RED)
        }
        binding.startPauseButton.isInvisible = !visible
        binding.customView.isInvisible = !visible

    }

    private companion object {
        private const val START = "START"
        private const val STOP = "STOP"
    }
}