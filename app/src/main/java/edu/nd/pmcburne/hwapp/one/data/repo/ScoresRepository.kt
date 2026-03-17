package edu.nd.pmcburne.hwapp.one.data.repo

import edu.nd.pmcburne.hwapp.one.data.local.GameDao
import edu.nd.pmcburne.hwapp.one.data.local.GameEntity
import edu.nd.pmcburne.hwapp.one.data.remote.RetrofitInstance
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ScoresRepository(
    private val dao: GameDao
) {
    suspend fun getScores(date: LocalDate, gender: String, forceRefresh: Boolean = true): Pair<List<GameEntity>, Boolean> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dbDate = date.format(formatter)

        return if (forceRefresh) {
            try {
                val response = RetrofitInstance.api.getScores(
                    gender = gender,
                    year = date.year.toString(),
                    month = "%02d".format(date.monthValue),
                    day = "%02d".format(date.dayOfMonth)
                )

                val entities = response.games.map { wrapper ->
                    val g = wrapper.game
                    GameEntity(
                        gameId = g.gameID,
                        date = dbDate,
                        gender = gender,
                        awayTeam = g.away.names.short.ifBlank { g.away.names.full },
                        homeTeam = g.home.names.short.ifBlank { g.home.names.full },
                        awayScore = g.away.score,
                        homeScore = g.home.score,
                        gameState = g.gameState,
                        startTime = g.startTime,
                        currentPeriod = g.currentPeriod,
                        contestClock = g.contestClock,
                        finalMessage = g.finalMessage,
                        awayWinner = g.away.winner,
                        homeWinner = g.home.winner
                    )
                }

                dao.insertAll(entities)
                Pair(dao.getGamesForDateAndGender(dbDate, gender), true)
            } catch (e: Exception) {
                Pair(dao.getGamesForDateAndGender(dbDate, gender), false)
            }
        } else {
            Pair(dao.getGamesForDateAndGender(dbDate, gender), false)
        }
    }
}