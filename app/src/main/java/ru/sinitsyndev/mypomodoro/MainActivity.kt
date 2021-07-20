package ru.sinitsyndev.mypomodoro

import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import ru.sinitsyndev.mypomodoro.databinding.ActivityMainBinding
import java.io.Serializable


class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0
    var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        binding.addNewStopwatchButton.setOnClickListener {

            val newMinutes = binding.editMinutes.text.toString().toInt()
            if ( newMinutes > 0 ) {
                stopwatches.add(Stopwatch(nextId++, newMinutes.toLong()*UNIT_SEC*60, false, newMinutes.toLong()*UNIT_SEC*60))
                stopwatchAdapter.submitList(stopwatches.toList())
            }
            else {
                Toast.makeText(this@MainActivity, "Enter minutes", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun start(id: Int) {

        //сначала стопаем все
        stopAll()

        val stopwatch = stopwatches.find { it.id == id }

        if (  stopwatch != null ) {
            changeStopwatch(id, stopwatch.currentSec, true)
            startTimer(id, stopwatch.currentSec, true)
        }

    }

    override fun stop(id: Int, currentMs: Long) {

        val stopwatch = stopwatches.find { it.id == id }

        if (  stopwatch != null ) {
            changeStopwatch(id, stopwatch.currentSec, false)
            stopTimer()
        }

    }

    private fun stopAll() {
        stopwatches.forEach {
            it.isStarted = false
        }
        stopwatchAdapter.notifyDataSetChanged()
        stopTimer()
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())

        val activeStopwatch = stopwatches.find {
                it.isStarted
            }
        if (activeStopwatch == null) stopAll()

    }

    override fun startTimer(id: Int, currentSec: Long, isStarted: Boolean) {

        timer?.cancel()
        timer = getCountDownTimer(id, currentSec, isStarted)
        timer?.start()

    }

    override fun stopTimer() {
        timer?.cancel()
    }


    override fun getCountDownTimer(id: Int, currentSec: Long, isStarted: Boolean): CountDownTimer {
        return object : CountDownTimer(currentSec*UNIT_SEC, UNIT_SEC) {
            val interval = UNIT_SEC

            override fun onTick(millisUntilFinished: Long) {

                val stopwatch = stopwatches.find { it.id == id }
                if (  stopwatch != null ) {
                    stopwatch.currentSec = stopwatch.currentSec-interval
                    if ( (stopwatch.currentSec%2) == 0L )
                    {
                        changeStopwatch(id, stopwatch.currentSec+1, isStarted)
                    } else {
                        changeStopwatch(id, stopwatch.currentSec-1, isStarted)
                    }

                    if (stopwatch.currentSec <= 0 ) {
                        stopAll()
                        val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 500)
                        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 200)
                        Toast.makeText(this@MainActivity, "Timer ended", Toast.LENGTH_LONG).show()
                    }
                }

            }

            override fun onFinish() {
                stopAll()
            }
        }
    }

    private fun changeStopwatch(id: Int, currentSec: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(Stopwatch(it.id, currentSec ?: it.currentSec, isStarted, it.period))
            } else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val activeStopwatch = stopwatches.find { it.isStarted }
        if (  activeStopwatch != null ) {
            println("start service for started")
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(STARTED_TIMER_OBJECT, activeStopwatch)
            startService(startIntent)
        }


    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    private companion object {

        private const val UNIT_SEC = 1000L

    }

}