package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class GameRepository(private val gameDao: GameDao) {

    val playerState: Flow<PlayerState?> = gameDao.getPlayerState()
    val businesses: Flow<List<BusinessState>> = gameDao.getAllBusinesses()
    val stocks: Flow<List<StockState>> = gameDao.getAllStocks()
    val leaderboard: Flow<List<LeaderboardEntry>> = gameDao.getLeaderboard()

    suspend fun initializeDatabaseIfEmpty() {
        val existingState = gameDao.getPlayerStateSync()
        if (existingState == null) {
            // Seed Player State - starts completely from scratch
            val initialPlayer = PlayerState(
                id = 1,
                balance = 0.0, 
                totalEarned = 0.0,
                clickLevel = 1, 
                clickIncome = 1.0, 
                upgradeCost = 10.0, 
                isNoAdsActive = false,
                totalClicks = 0
            )
            gameDao.insertPlayerState(initialPlayer)

            // Seed Businesses - starts from level 0 (locked)
            val initialBusinesses = listOf(
                BusinessState(
                    id = "metro_post",
                    name = "Метро Пост",
                    subtitle = "Служба доставки",
                    iconName = "delivery",
                    level = 0,
                    baseIncome = 8.0,
                    baseUpgradeCost = 100.0,
                    isWarningActive = false
                ),
                BusinessState(
                    id = "vector",
                    name = "Вектор Пром",
                    subtitle = "Производственный завод",
                    iconName = "factory",
                    level = 0,
                    baseIncome = 50.0,
                    baseUpgradeCost = 750.0,
                    isWarningActive = false
                ),
                BusinessState(
                    id = "apex",
                    name = "Апекс Констракшн",
                    subtitle = "Строительный подрядчик",
                    iconName = "crane",
                    level = 0, 
                    baseIncome = 600.0,
                    baseUpgradeCost = 12000.0,
                    isWarningActive = false
                ),
                BusinessState(
                    id = "horizon",
                    name = "Горизонт Банк",
                    subtitle = "Страховая компания",
                    iconName = "bank",
                    level = 0, 
                    baseIncome = 12000.0,
                    baseUpgradeCost = 250000.0,
                    isWarningActive = false
                ),
                BusinessState(
                    id = "it_corp",
                    name = "Сайбер Системс",
                    subtitle = "IT Сектор",
                    iconName = "tech",
                    level = 0,
                    baseIncome = 180000.0,
                    baseUpgradeCost = 3500000.0,
                    isWarningActive = false
                )
            )
            gameDao.insertBusinesses(initialBusinesses)

            // Seed Stocks - premium stock option starts with 0 owned shares
            val initialStocks = listOf(
                StockState(
                    symbol = "Amason",
                    name = "Amason",
                    currentPrice = 116.87,
                    priceHistoryString = "125.0,121.2,118.4,116.87",
                    ownedCount = 0, 
                    percentageChange = -6.49,
                    logoName = "amason",
                    isUp = false,
                    dividendYield = 0.035
                ),
                StockState(
                    symbol = "Appli",
                    name = "Appli",
                    currentPrice = 219.65,
                    priceHistoryString = "230.1,225.4,222.0,219.65",
                    ownedCount = 0,
                    percentageChange = -4.16,
                    logoName = "appli",
                    isUp = false,
                    dividendYield = 0.028
                ),
                StockState(
                    symbol = "CicssoSystems",
                    name = "CicssoSystems",
                    currentPrice = 54.00,
                    priceHistoryString = "51.2,52.8,53.1,54.00",
                    ownedCount = 0,
                    percentageChange = 3.35,
                    logoName = "cicsso",
                    isUp = true,
                    dividendYield = 0.041
                ),
                StockState(
                    symbol = "Coca",
                    name = "Coca",
                    currentPrice = 70.77,
                    priceHistoryString = "68.2,69.1,69.5,70.77",
                    ownedCount = 0,
                    percentageChange = 3.07,
                    logoName = "coca",
                    isUp = true,
                    dividendYield = 0.038
                ),
                StockState(
                    symbol = "Fort",
                    name = "Fort",
                    currentPrice = 12.56,
                    priceHistoryString = "11.2,11.8,12.1,12.56",
                    ownedCount = 0,
                    percentageChange = 6.17,
                    logoName = "fort",
                    isUp = true,
                    dividendYield = 0.052
                ),
                StockState(
                    symbol = "Henda",
                    name = "Henda",
                    currentPrice = 46.61,
                    priceHistoryString = "48.2,47.5,47.1,46.61",
                    ownedCount = 0,
                    percentageChange = -1.29,
                    logoName = "henda",
                    isUp = false,
                    dividendYield = 0.031
                ),
                StockState(
                    symbol = "Mikrosolt",
                    name = "Mikrosolt",
                    currentPrice = 371.26,
                    priceHistoryString = "380.2,375.4,373.1,371.26",
                    ownedCount = 0,
                    percentageChange = -0.89,
                    logoName = "mikrosolt",
                    isUp = false,
                    dividendYield = 0.019
                ),
                StockState(
                    symbol = "Moestrocard",
                    name = "Moestrocard",
                    currentPrice = 366.82,
                    priceHistoryString = "399.1,385.2,374.0,366.82",
                    ownedCount = 0,
                    percentageChange = -8.65,
                    logoName = "moestrocard",
                    isUp = false,
                    dividendYield = 0.015
                )
            )
            gameDao.insertStocks(initialStocks)

            // Seed Leaderboard entries
            val initialLeaderboard = listOf(
                LeaderboardEntry("1", "Уоррен Баффет", 89452070.0, 1, "#FF8F00"),
                LeaderboardEntry("2", "Илон Клац", 72153400.0, 2, "#1E88E5"),
                LeaderboardEntry("3", "Крипто Король", 35460200.0, 3, "#8E24AA"),
                LeaderboardEntry("4", "Кликер Кинг", 12543900.0, 4, "#43A047"),
                LeaderboardEntry("5", "Владимир Инвест", 4522900.0, 5, "#D81B60"),
                LeaderboardEntry("6", "Алиса Финтех", 489700.0, 6, "#7E57C2"),
                LeaderboardEntry("7", "Стив Клик", 312000.0, 7, "#26A69A"),
                LeaderboardEntry("8", "Инвестор Боб", 124500.0, 8, "#D4E157"),
                LeaderboardEntry("9", "Стартапер Алекс", 45000.0, 9, "#FF7043"),
                LeaderboardEntry("player", "Вы (Игрок)", 0.0, 10, "#29B6F6", isPlayer = true)
            )
            gameDao.insertLeaderboard(initialLeaderboard)
        }
    }

    suspend fun clickToEarn() {
        val player = gameDao.getPlayerStateSync() ?: return
        val currentClickIncome = player.clickIncome
        val newBalance = player.balance + currentClickIncome
        val newTotal = player.totalEarned + currentClickIncome

        val updated = player.copy(
            balance = newBalance,
            totalEarned = newTotal,
            totalClicks = player.totalClicks + 1
        )
        gameDao.insertPlayerState(updated)
        updatePlayerRank(newBalance)
    }

    suspend fun upgradeClick() {
        val player = gameDao.getPlayerStateSync() ?: return
        if (player.balance >= player.upgradeCost) {
            val newBalance = player.balance - player.upgradeCost
            val newLevel = player.clickLevel + 1
            // Formula for click income increases
            val newClickIncome = player.clickIncome * 1.55 + 10.0
            // Formula for next upgrade cost
            val newUpgradeCost = player.upgradeCost * 1.85

            val updated = player.copy(
                balance = newBalance,
                clickLevel = newLevel,
                clickIncome = Math.round(newClickIncome * 100.0) / 100.0,
                upgradeCost = Math.round(newUpgradeCost / 50000.0) * 50000.0 // keep it rounded or clear
            )
            gameDao.insertPlayerState(updated)
            updatePlayerRank(newBalance)
        }
    }

    suspend fun toggleNoAds() {
        val player = gameDao.getPlayerStateSync() ?: return
        val updated = player.copy(isNoAdsActive = !player.isNoAdsActive)
        gameDao.insertPlayerState(updated)
    }

    suspend fun startOrUpgradeBusiness(businessId: String) {
        val player = gameDao.getPlayerStateSync() ?: return
        val businessList = gameDao.getAllBusinessesSync()
        val business = businessList.find { it.id == businessId } ?: return

        val cost = business.currentUpgradeCost
        if (player.balance >= cost) {
            val newBalance = player.balance - cost
            val updatedBusiness = business.copy(
                level = business.level + 1,
                isWarningActive = false // upgrade clears warning
            )
            
            val updatedPlayer = player.copy(balance = newBalance)
            gameDao.insertPlayerState(updatedPlayer)
            gameDao.updateBusiness(updatedBusiness)
            updatePlayerRank(newBalance)
        }
    }

    suspend fun collectBusinessAlert(businessId: String) {
        val player = gameDao.getPlayerStateSync() ?: return
        val businessList = gameDao.getAllBusinessesSync()
        val business = businessList.find { it.id == businessId } ?: return

        if (business.isWarningActive) {
            // Resolve warning and award a small instant bonus (e.g. 10 minutes of business passive income)
            val bonus = business.currentHourlyIncome * 0.16
            val updatedPlayer = player.copy(balance = player.balance + bonus)
            val updatedBusiness = business.copy(isWarningActive = false)
            
            gameDao.insertPlayerState(updatedPlayer)
            gameDao.updateBusiness(updatedBusiness)
            updatePlayerRank(updatedPlayer.balance)
        }
    }

    suspend fun triggerRandomBusinessWarnings() {
        val businesses = gameDao.getAllBusinessesSync()
        if (businesses.isNotEmpty()) {
            val updated = businesses.map { b ->
                if (b.level > 0 && Math.random() < 0.14) {
                    b.copy(isWarningActive = true)
                } else {
                    b
                }
            }
            gameDao.insertBusinesses(updated)
        }
    }

    // Buy 1 share or a custom amount of shares of a stock
    suspend fun buyStock(symbol: String, count: Int = 1) {
        val player = gameDao.getPlayerStateSync() ?: return
        val stocks = gameDao.getAllStocksSync()
        val stock = stocks.find { it.symbol == symbol } ?: return

        val totalCost = stock.currentPrice * count
        if (player.balance >= totalCost) {
            val newBalance = player.balance - totalCost
            val updatedStock = stock.copy(ownedCount = stock.ownedCount + count)
            val updatedPlayer = player.copy(balance = newBalance)

            gameDao.insertPlayerState(updatedPlayer)
            gameDao.updateStock(updatedStock)
            updatePlayerRank(newBalance)
        }
    }

    // Sell 1 share or a custom amount of shares of a stock
    suspend fun sellStock(symbol: String, count: Int = 1) {
        val player = gameDao.getPlayerStateSync() ?: return
        val stocks = gameDao.getAllStocksSync()
        val stock = stocks.find { it.symbol == symbol } ?: return

        if (stock.ownedCount >= count) {
            val totalProfit = stock.currentPrice * count
            val newBalance = player.balance + totalProfit
            val updatedStock = stock.copy(ownedCount = stock.ownedCount - count)
            val updatedPlayer = player.copy(balance = newBalance)

            gameDao.insertPlayerState(updatedPlayer)
            gameDao.updateStock(updatedStock)
            updatePlayerRank(newBalance)
        }
    }

    // Update stock prices randomly (fluctuation simulator) to make market feel fully real
    suspend fun fluctuateStockPrices() {
        val stocksAll = gameDao.getAllStocksSync()
        val updated = stocksAll.map { stock ->
            val volatility = 0.04 // max 4% fluctuation
            val priceChangeFraction = (Math.random() - 0.49) * volatility // positive or negative
            val changePercent = priceChangeFraction * 100.0
            
            val newPrice = Math.max(1.5, stock.currentPrice * (1.0 + priceChangeFraction))
            val roundedPrice = Math.round(newPrice * 100.0) / 100.0

            // Update history list
            val history = stock.priceHistory.toMutableList()
            if (history.size >= 8) {
                history.removeAt(0)
            }
            history.add(roundedPrice.toFloat())
            val newHistoryString = history.joinToString(",")

            stock.copy(
                currentPrice = roundedPrice,
                priceHistoryString = newHistoryString,
                percentageChange = Math.round(changePercent * 100.0) / 100.0,
                isUp = priceChangeFraction >= 0
            )
        }
        gameDao.insertStocks(updated)
    }

    // Recalculates other player's balance dynamically and adjusts rankings so leaderboard changes!
    suspend fun updateLeaderboardBalances() {
        val player = gameDao.getPlayerStateSync() ?: return
        val leaderboardList = gameDao.getLeaderboardSync().toMutableList()

        // Simulating other players minor progress
        val updated = leaderboardList.map { entry ->
            if (entry.isPlayer) {
                entry.copy(balance = player.balance)
            } else {
                // Randomly increase of other player bounds slightly to feel alive
                val increaseRate = 0.005 + Math.random() * 0.015
                val added = entry.balance * (increaseRate / 3600.0 * 5) // since called every few secs
                entry.copy(balance = entry.balance + added)
            }
        }

        // Re-sort and rank them
        val sorted = updated.sortedByDescending { it.balance }
        val ranked = sorted.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
        gameDao.insertLeaderboard(ranked)
    }

    private suspend fun updatePlayerRank(playerBalance: Double) {
        val leaderboardList = gameDao.getLeaderboardSync()
        val updatedList = leaderboardList.map { entry ->
            if (entry.isPlayer) {
                entry.copy(balance = playerBalance)
            } else {
                entry
            }
        }
        val sorted = updatedList.sortedByDescending { it.balance }
        val ranked = sorted.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
        gameDao.insertLeaderboard(ranked)
    }

    suspend fun applyPassiveIncome(seconds: Double = 1.0) {
        val player = gameDao.getPlayerStateSync() ?: return
        val currentBusinesses = gameDao.getAllBusinessesSync()
        val currentStocks = gameDao.getAllStocksSync()

        // Sum business passive income
        val businessIncome = currentBusinesses.sumOf { it.currentHourlyIncome }
        // Sum stock dividend passive income (e.g. 3% of current price * count per hour)
        val stockIncome = currentStocks.sumOf { (it.currentPrice * it.ownedCount) * (it.dividendYield / 24.0) } // let's say dividend is relative to some hour yield

        val hourlyTotal = businessIncome + stockIncome
        if (hourlyTotal > 0) {
            val incomePerSecond = hourlyTotal / 3600.0
            val addedIncome = incomePerSecond * seconds
            
            val updated = player.copy(
                balance = player.balance + addedIncome,
                totalEarned = player.totalEarned + addedIncome
            )
            gameDao.insertPlayerState(updated)
            updatePlayerRank(updated.balance)
        }
    }
}
