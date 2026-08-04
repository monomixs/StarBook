package com.starbook.core.data.repo

import com.starbook.core.data.DailyListening
import com.starbook.core.data.repo.internals.dao.DailyListeningDao
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
public class DailyListeningRepoImpl(
  private val dao: DailyListeningDao,
) : DailyListeningRepo {

  override fun allFlow(): Flow<List<DailyListening>> = dao.allFlow()

  override suspend fun get(date: LocalDate): DailyListening? = dao.get(date)

  override fun getFlow(date: LocalDate): Flow<DailyListening?> = dao.getFlow(date)

  override suspend fun addTime(date: LocalDate, ms: Long) {
    val current = dao.get(date) ?: DailyListening(date, 0)
    dao.insert(current.copy(listenedMs = current.listenedMs + ms))
  }
}
