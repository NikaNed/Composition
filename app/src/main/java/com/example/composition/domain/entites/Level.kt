package com.example.composition.domain.entites

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class Level : Parcelable{
    TEST, EASY, NORMAL, HARD
}