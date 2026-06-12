package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_state")
data class PlayerState(
    @PrimaryKey val id: Int = 1,
    val balance: Double = 50000.0, // Let's start with generous $50,000 so the user has some action immediately, or $522,693.90 to match the screenshots perfectly over time!
    val totalEarned: Double = 50000.0,
    val clickLevel: Int = 1,
    val clickIncome: Double = 5.0, // per click
    val upgradeCost: Double = 150.0,
    val isNoAdsActive: Boolean = false,
    val totalClicks: Int = 0,
    val bonusIncome: Double = 0.0,
    val availableSlots: Int = 4,
    val totalSlots: Int = 9
)

@Entity(tableName = "business_state")
data class BusinessState(
    @PrimaryKey val id: String,
    val name: String,
    val subtitle: String,
    val iconName: String, // "factory", "crane", "bank", "delivery"
    val level: Int, // 0 means locked, 1+ means level
    val baseIncome: Double, // hourly base income
    val upgradeCostMultiplier: Double = 1.6,
    val baseUpgradeCost: Double = 25000.0,
    val isWarningActive: Boolean = false
) {
    val currentHourlyIncome: Double
        get() = if (level == 0) 0.0 else baseIncome * level

    val currentUpgradeCost: Double
        get() = baseUpgradeCost * Math.pow(upgradeCostMultiplier, level.toDouble())
}

@Entity(tableName = "stock_state")
data class StockState(
    @PrimaryKey val symbol: String, // "Amason", "Appli", "CicssoSystems", "Coca", etc.
    val name: String,
    val currentPrice: Double,
    val priceHistoryString: String, // JSON or comma-separated list of previous prices
    val ownedCount: Int = 0,
    val percentageChange: Double = 0.0,
    val logoName: String,
    val isUp: Boolean = true,
    val dividendYield: Double = 0.02 // dividend rate
) {
    val priceHistory: List<Float>
        get() = priceHistoryString.split(",").mapNotNull { it.toFloatOrNull() }
}

@Entity(tableName = "leaderboard")
data class LeaderboardEntry(
    @PrimaryKey val id: String,
    val name: String,
    val balance: Double,
    val rank: Int,
    val avatarColorHex: String,
    val isPlayer: Boolean = false
)
