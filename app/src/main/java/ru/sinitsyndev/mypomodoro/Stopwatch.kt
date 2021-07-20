package ru.sinitsyndev.mypomodoro

import android.os.Parcelable
import kotlinx.parcelize.*

@Parcelize
data class Stopwatch(
    val id: Int,
    var currentSec: Long,
    var isStarted: Boolean,
    val period: Long
) : Parcelable