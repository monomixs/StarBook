package com.starbook.core.data.repo

import com.starbook.core.data.DailyListening
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

public interface DailyListeningRepo {
  public fun allFlow(): Flow<List<DailyListening>>
  public suspend fun get(date: LocalDate): DailyListening?
  public fun getFlow(date: LocalDate): Flow<DailyListening?>
  public suspend fun addTime(date: LocalDate, ms: Long)
}
