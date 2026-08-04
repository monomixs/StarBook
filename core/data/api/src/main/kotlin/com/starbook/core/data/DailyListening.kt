package com.starbook.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_listening")
public data class DailyListening(
  @PrimaryKey
  val date: LocalDate,
  val listenedMs: Long = 0,
)
