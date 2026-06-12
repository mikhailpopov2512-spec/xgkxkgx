package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM player_state WHERE id = 1")
    fun getPlayerState(): Flow<PlayerState?>

    @Query("SELECT * FROM player_state WHERE id = 1")
    suspend fun getPlayerStateSync(): PlayerState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerState(state: PlayerState)

    // Businesses
    @Query("SELECT * FROM business_state")
    fun getAllBusinesses(): Flow<List<BusinessState>>

    @Query("SELECT * FROM business_state")
    suspend fun getAllBusinessesSync(): List<BusinessState>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinesses(businesses: List<BusinessState>)

    @Update
    suspend fun updateBusiness(business: BusinessState)

    // Stocks
    @Query("SELECT * FROM stock_state")
    fun getAllStocks(): Flow<List<StockState>>

    @Query("SELECT * FROM stock_state")
    suspend fun getAllStocksSync(): List<StockState>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<StockState>)

    @Update
    suspend fun updateStock(stock: StockState)

    // Leaderboard
    @Query("SELECT * FROM leaderboard ORDER BY rank ASC")
    fun getLeaderboard(): Flow<List<LeaderboardEntry>>

    @Query("SELECT * FROM leaderboard ORDER BY rank ASC")
    suspend fun getLeaderboardSync(): List<LeaderboardEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboard(entries: List<LeaderboardEntry>)

    @Query("DELETE FROM leaderboard")
    suspend fun clearLeaderboard()
}
