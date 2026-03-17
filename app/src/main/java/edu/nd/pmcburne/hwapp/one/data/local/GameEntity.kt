package edu.nd.pmcburne.hwapp.one.data.local

import androidx.room.Entity

@Entity(
    tableName = "games",
    primaryKeys = ["gameId", "date", "gender"]
)
data class GameEntity(
    val gameId: String,
    val date: String,      // yyyy-MM-dd
    val gender: String,    // men or women

    val awayTeam: String,
    val homeTeam: String,
    val awayScore: String,
    val homeScore: String,

    val gameState: String,
    val startTime: String,
    val currentPeriod: String,
    val contestClock: String,
    val finalMessage: String,

    val awayWinner: Boolean,
    val homeWinner: Boolean
)