package com.rsschool.pomodoro


const val START_TIME = "00:00:00"
const val INVALID = "INVALID"
const val COMMAND_START = "COMMAND_START"
const val COMMAND_STOP = "COMMAND_STOP"
const val COMMAND_ID = "COMMAND_ID"
const val STARTED_TIMER_TIME_MS = "STARTED_TIMER_TIME"
const val INTERVAL = 1000L
const val ZERO = 0L

fun displayTime(time: Long): String {
    if (time <= ZERO) {
        return START_TIME
    }
    val h = time / 1000 / 3600
    val m = time / 1000 % 3600 / 60
    val s = time / 1000 % 60

    return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
}

private fun displaySlot(count: Long): String {
    return if (count / 10L > ZERO) {
        "$count"
    } else {
        "0$count"
    }
}



