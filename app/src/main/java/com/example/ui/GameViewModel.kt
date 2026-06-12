package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BusinessState
import com.example.data.GameRepository
import com.example.data.LeaderboardEntry
import com.example.data.PlayerState
import com.example.data.StockState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())
        
        // Initialize database if empty inside coroutine
        viewModelScope.launch(Dispatchers.IO) {
            repository.initializeDatabaseIfEmpty()
        }
    }

    val playerState: StateFlow<PlayerState?> = repository.playerState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val businesses: StateFlow<List<BusinessState>> = repository.businesses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stocks: StateFlow<List<StockState>> = repository.stocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaderboard: StateFlow<List<LeaderboardEntry>> = repository.leaderboard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Local live balance for micro-ticking (smooth updates on screen)
    private val _liveBalance = MutableStateFlow<Double>(0.0)
    val liveBalance: StateFlow<Double> = _liveBalance.asStateFlow()

    // Navigation and screen UI states
    private val _currentTab = MutableStateFlow<Int>(2) // Starts on 'Заработок' tab like in clickers
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _currentInvestTab = MutableStateFlow<Int>(0) // 0 = Акции, 1 = Крипта, 2 = Недвижимость
    val currentInvestTab: StateFlow<Int> = _currentInvestTab.asStateFlow()

    private val _showStockMarket = MutableStateFlow<Boolean>(false)
    val showStockMarket: StateFlow<Boolean> = _showStockMarket.asStateFlow()

    // Stock sorting sub-selection (Screenshot 3 header: "Наибольшие дивиденды", "Наименьшие дивиденды" etc)
    private val _stockSortOption = MutableStateFlow<Int>(0) // 0 = default, 1 = high yield, 2 = low yield
    val stockSortOption: StateFlow<Int> = _stockSortOption.asStateFlow()

    // Shop / Upgrades Modal Visibility
    private val _showShopModal = MutableStateFlow<Boolean>(false)
    val showShopModal: StateFlow<Boolean> = _showShopModal.asStateFlow()

    // Transaction Success HUD Overlay text
    private val _showTransactionSuccess = MutableStateFlow<String?>(null)
    val showTransactionSuccess: StateFlow<String?> = _showTransactionSuccess.asStateFlow()

    fun triggerSuccessAnimation(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _showTransactionSuccess.value = message
            delay(1500)
            _showTransactionSuccess.value = null
        }
    }

    // No ADS purchase simulation (adds 20% to clicking and passive income!)
    private val _isNoAdsPurchased = MutableStateFlow<Boolean>(false)
    val isNoAdsPurchased: StateFlow<Boolean> = _isNoAdsPurchased.asStateFlow()

    // Active screen overlays (e.g. details of own actions)
    private val _showMyStocksDetails = MutableStateFlow<Boolean>(false)
    val showMyStocksDetails: StateFlow<Boolean> = _showMyStocksDetails.asStateFlow()

    init {
        // Collect real database emissions to set/correct our live balance representation
        viewModelScope.launch {
            playerState.collect { state ->
                if (state != null) {
                    // Update live balance if it differs significantly or is uninitialized
                    if (Math.abs(_liveBalance.value - state.balance) > 5.0 || _liveBalance.value == 0.0) {
                        _liveBalance.value = state.balance
                    }
                    _isNoAdsPurchased.value = state.isNoAdsActive
                }
            }
        }

        // Ticker loop for passive income and random events
        viewModelScope.launch(Dispatchers.Default) {
            val tickIntervalMs = 200L
            var databaseSyncCounter = 0L
            var stocksFluctuateCounter = 0L
            var warningGeneratorCounter = 0L

            while (true) {
                delay(tickIntervalMs)
                
                // Calculate income
                val currentStocks = stocks.value
                val player = playerState.value

                if (player != null) {
                    // Stock dividends per hour
                    val stockHourly = currentStocks.sumOf { (it.currentPrice * it.ownedCount) * (it.dividendYield / 24.0) }
                    
                    val totalHourlyIncomeRaw = stockHourly
                    val totalHourlyIncome = if (_isNoAdsPurchased.value) totalHourlyIncomeRaw * 1.25 else totalHourlyIncomeRaw

                    if (totalHourlyIncome > 0) {
                        val addedPerTick = (totalHourlyIncome / 3600.0) * (tickIntervalMs / 1000.0)
                        _liveBalance.value += addedPerTick
                    }

                    // Periodic DB saves and background simulation (stocks only)
                    databaseSyncCounter += tickIntervalMs
                    if (databaseSyncCounter >= 4000L) { // Sync to DB every 4 seconds
                        databaseSyncCounter = 0
                        repository.applyPassiveIncomeOnlyStocks(4.0)
                    }
                }

                // Periodic stock market updates (every 6 seconds)
                stocksFluctuateCounter += tickIntervalMs
                if (stocksFluctuateCounter >= 6000L) {
                    stocksFluctuateCounter = 0
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.fluctuateStockPrices()
                        repository.updateLeaderboardBalances()
                    }
                }

                // Random supply alert simulator for businesses (every 20 seconds)
                warningGeneratorCounter += tickIntervalMs
                if (warningGeneratorCounter >= 20000L) {
                    warningGeneratorCounter = 0
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.triggerRandomBusinessWarnings()
                    }
                }
            }
        }
    }

    // Accumulates active/purchased businesses passive income proportional part once a second
    fun accrueBusinessPassiveIncomeOneSecond() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentBusinesses = businesses.value
            val player = playerState.value ?: return@launch
            
            // Sum overall business passive income
            val businessHourlySum = currentBusinesses.sumOf { it.currentHourlyIncome }
            if (businessHourlySum > 0) {
                val multiplier = if (player.isNoAdsActive) 1.25 else 1.0
                val totalHourlyAdjusted = businessHourlySum * multiplier
                val proportionalSecondSlice = totalHourlyAdjusted / 3600.0
                
                // Increment visual balance representation
                _liveBalance.value += proportionalSecondSlice
                
                // Persist 1 second of passive business income to DB
                repository.applyBusinessPassiveIncome(1.0)
            }
        }
    }

    // GAME INTERACTIVE CONTROLS

    fun click() {
        viewModelScope.launch(Dispatchers.Main) {
            val player = playerState.value ?: return@launch
            val clickAmount = if (_isNoAdsPurchased.value) player.clickIncome * 1.3 else player.clickIncome
            _liveBalance.value += clickAmount
            
            // Persist to DB asynchronously
            viewModelScope.launch(Dispatchers.IO) {
                repository.clickToEarn()
            }
        }
    }

    fun upgradeClick() {
        viewModelScope.launch(Dispatchers.IO) {
            val p = playerState.value
            if (p != null && _liveBalance.value >= p.upgradeCost) {
                repository.upgradeClick()
                triggerSuccessAnimation("Сила клика увеличена! 🚀")
            }
        }
    }

    fun toggleNoAds() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleNoAds()
            val active = playerState.value?.isNoAdsActive ?: false
            triggerSuccessAnimation(if (!active) "Буст рекламы активирован! ✨" else "Буст выключен")
        }
    }

    fun buyStock(symbol: String, count: Int = 1) {
        viewModelScope.launch(Dispatchers.IO) {
            val stock = stocks.value.find { it.symbol == symbol }
            if (stock != null) {
                val cost = stock.currentPrice * count
                if (_liveBalance.value >= cost) {
                    repository.buyStock(symbol, count)
                    triggerSuccessAnimation("Куплено: +$count акций ${stock.name}! 📈")
                }
            }
        }
    }

    fun sellStock(symbol: String, count: Int = 1) {
        viewModelScope.launch(Dispatchers.IO) {
            val stock = stocks.value.find { it.symbol == symbol }
            if (stock != null && stock.ownedCount >= count) {
                val yield = stock.currentPrice * count
                repository.sellStock(symbol, count)
                triggerSuccessAnimation("Продано: $count акций ${stock.name}! 📉")
            }
        }
    }

    fun buyOrUpgradeBusiness(businessId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val business = businesses.value.find { it.id == businessId }
            if (business != null) {
                val cost = business.currentUpgradeCost
                if (_liveBalance.value >= cost) {
                    val isUpgrade = business.level > 0
                    repository.startOrUpgradeBusiness(businessId)
                    if (isUpgrade) {
                        triggerSuccessAnimation("${business.name} улучшен до ${business.level + 1} ур! 🏢")
                    } else {
                        triggerSuccessAnimation("${business.name} успешно запущен! 🎉")
                    }
                }
            }
        }
    }

    fun collectBusinessAlert(businessId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.collectBusinessAlert(businessId)
        }
    }

    fun setCurrentTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    fun setCurrentInvestTab(tabIndex: Int) {
        _currentInvestTab.value = tabIndex
    }

    fun showStockMarket(show: Boolean) {
        _showStockMarket.value = show
    }

    fun showMyStocksDetails(show: Boolean) {
        _showMyStocksDetails.value = show
    }

    fun setStockSortOption(option: Int) {
        _stockSortOption.value = option
    }

    fun showShopModal(show: Boolean) {
        _showShopModal.value = show
    }

    // Helper functions for displaying UI values nicely
    fun formatDouble(value: Double): String {
        return String.format("%,.2f", value).replace(",", " ")
    }

    fun formatDoubleNoCeil(value: Double): String {
        if (value >= 1_000_000_000.0) {
            return String.format("%.2fB", value / 1_000_000_000.0)
        }
        if (value >= 1_000_000.0) {
            return String.format("%.2fM", value / 1_000_000.0)
        }
        return String.format("%,.2f", value).replace(",", " ")
    }
}
