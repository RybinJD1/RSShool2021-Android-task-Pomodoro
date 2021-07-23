package com.rsschool.pomodoro.timer

data class Timer(
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean,
    var finishTime: Long = 0L
)