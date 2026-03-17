package edu.nd.pmcburne.hwapp.one.ui.viewmodel

import edu.nd.pmcburne.hwapp.one.data.local.GameEntity

data class ScoresUiState(
    val selectedGender: String = "men",
    val games: List<GameEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isOnlineData: Boolean = true,
    val errorMessage: String? = null
)