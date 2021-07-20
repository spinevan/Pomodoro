package ru.sinitsyndev.mypomodoro

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import ru.sinitsyndev.mypomodoro.databinding.StopwatchItemBinding

class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(stopwatch: Stopwatch) {
        binding.stopwatchTimer.text = stopwatch.currentSec.displayTime()
        binding.customViewTimer.setPeriod(stopwatch.period)
        binding.customViewTimer.setCurrent(stopwatch.currentSec)

        println("ViewHolder bind id= ${stopwatch.id} isStarted= ${stopwatch.isStarted}")
        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer(stopwatch)
        }

        initButtonsListeners(stopwatch)
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.startPauseButton.setOnClickListener {
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentSec)
                stopTimer(stopwatch)
            } else {
                if ( stopwatch.currentSec > 2 ) {
                    listener.start(stopwatch.id)
                    startTimer(stopwatch)
                }
            }
        }

        binding.deleteButton.setOnClickListener { listener.delete(stopwatch.id) }
    }

    private fun startTimer(stopwatch: Stopwatch) {

        binding.startPauseButton.text = "pause"

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer(stopwatch: Stopwatch) {

        binding.startPauseButton.text = "start"

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }


    private companion object {

        private const val START_TIME = "00:00:00"

    }
}