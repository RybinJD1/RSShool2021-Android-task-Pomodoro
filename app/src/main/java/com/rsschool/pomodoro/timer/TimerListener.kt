package com.rsschool.pomodoro.timer

interface TimerListener {

    fun start(id: Int)

    fun stop(id: Int)

    fun delete(id: Int)
}