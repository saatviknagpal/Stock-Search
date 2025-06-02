package com.example.assignment4
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Stock(
    val tickerSymbol: String,
    val name: String,
    var quantity: Int = 0,
    var totalCost: Double = 0.0,
    var currentPrice: Double,
    var averageCostPerShare: Double = 0.0,
    var marketValue: Double = 0.0,
    var changeInPrice: Double = 0.0,
    var changeInPricePercentage: Double = 0.0
) : Parcelable