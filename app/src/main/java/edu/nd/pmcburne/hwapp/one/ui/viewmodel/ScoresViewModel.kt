package edu.nd.pmcburne.hwapp.one.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.nd.pmcburne.hwapp.one.data.repo.ScoresRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class ScoresViewModel(
    private val repository: ScoresRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _uiState = MutableStateFlow(ScoresUiState())
    val uiState: StateFlow<ScoresUiState> = _uiState.asStateFlow()

    init {
        loadScores()
    }

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
        loadScores()
    }

    fun setGender(gender: String) {
        _uiState.value = _uiState.value.copy(selectedGender = gender)
        loadScores()
    }

    fun refresh() {
        loadScores()
    }

    private fun loadScores() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val (games, gotFreshData) = repository.getScores(
                    date = _selectedDate.value,
                    gender = _uiState.value.selectedGender,
                    forceRefresh = true
                )

                _uiState.value = _uiState.value.copy(
                    games = games,
                    isLoading = false,
                    isOnlineData = gotFreshData,
                    errorMessage = if (!gotFreshData && games.isNotEmpty()) {
                        "Offline mode: showing saved scores"
                    } else if (!gotFreshData && games.isEmpty()) {
                        "No internet and no saved scores for this date"
                    } else {
                        null
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load scores"
                )
            }
        }
    }
}

class ScoresViewModelFactory(
    private val repository: ScoresRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScoresViewModel::class.java)) {
            return ScoresViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}