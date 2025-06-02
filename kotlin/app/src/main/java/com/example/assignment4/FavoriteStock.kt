package com.example.assignment4

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FavoriteStock(
    val id: String,
    val tickerSymbol: String,
    val companyName: String,
    val currentPrice: Double,
    val changeInPrice: Double,
) : Parcelable
