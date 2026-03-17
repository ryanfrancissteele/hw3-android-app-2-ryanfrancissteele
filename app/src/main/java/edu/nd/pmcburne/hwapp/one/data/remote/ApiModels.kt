package edu.nd.pmcburne.hwapp.one.data.remote

data class ScoreboardResponse(
    val games: List<GameWrapper> = emptyList()
)

data class GameWrapper(
    val game: ApiGame
)

data class ApiGame(
    val gameID: String,
    val away: ApiTeam,
    val home: ApiTeam,
    val gameState: String,
    val startTime: String = "",
    val currentPeriod: String = "",
    val contestClock: String = "",
    val finalMessage: String = ""
)

data class ApiTeam(
    val score: String = "",
    val winner: Boolean = false,
    val names: TeamNames
)

data class TeamNames(
    val short: String = "",
    val full: String = ""
)