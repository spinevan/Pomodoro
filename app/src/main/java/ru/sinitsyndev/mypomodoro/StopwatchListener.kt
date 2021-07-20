package ru.sinitsyndev.mypomodoro

import android.os.CountDownTimer

interface StopwatchListener {

    fun start(id: Int)

    fun stop(id: Int, currentMs: Long)

    fun delete(id: Int)

    fun startTimer(id: Int, currentSec: Long, isStarted: Boolean)

    fun stopTimer()

    fun getCountDownTimer(id: Int, currentSec: Long, isStarted: Boolean): CountDownTimer

}