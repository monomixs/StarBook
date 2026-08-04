package com.starbook.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.starbook.core.data.DailyListening
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
public interface DailyListeningDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insert(dailyListening: DailyListening)

  @Query("SELECT * FROM daily_listening WHERE date = :date")
  public suspend fun get(date: LocalDate): DailyListening?

  @Query("SELECT * FROM daily_listening ORDER BY date DESC")
  public fun allFlow(): Flow<List<DailyListening>>

  @Query("SELECT * FROM daily_listening WHERE date = :date")
  public fun getFlow(date: LocalDate): Flow<DailyListening?>

  @Query("DELETE FROM daily_listening")
  public suspend fun deleteAll()
}
