package edu.nd.pmcburne.hwapp.one

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.nd.pmcburne.hwapp.one.data.local.AppDatabase
import edu.nd.pmcburne.hwapp.one.data.local.GameEntity
import edu.nd.pmcburne.hwapp.one.data.repo.ScoresRepository
import edu.nd.pmcburne.hwapp.one.ui.theme.HWStarterRepoTheme
import edu.nd.pmcburne.hwapp.one.ui.viewmodel.ScoresViewModel
import edu.nd.pmcburne.hwapp.one.ui.viewmodel.ScoresViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {

    private val viewModel: ScoresViewModel by viewModels {
        val dao = AppDatabase.getDatabase(applicationContext).gameDao()
        ScoresViewModelFactory(ScoresRepository(dao))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HWStarterRepoTheme {
                ScoresApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoresApp(viewModel: ScoresViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("College Basketball Scores") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            DateSelector(
                date = selectedDate,
                onDateSelected = { viewModel.setDate(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            GenderToggle(
                selectedGender = uiState.selectedGender,
                onGenderSelected = { viewModel.setGender(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.refresh() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh")
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            uiState.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (uiState.games.isEmpty() && !uiState.isLoading) {
                Text("No games found for this date.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.games) { game ->
                        GameCard(game)
                    }
                }
            }
        }
    }
}

@Composable
fun DateSelector(
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    val context = androidx.compose.ui.platform.LocalContext.current

    OutlinedButton(
        onClick = {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
                },
                date.year,
                date.monthValue - 1,
                date.dayOfMonth
            ).show()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Date: ${date.format(formatter)}")
    }
}

@Composable
fun GenderToggle(
    selectedGender: String,
    onGenderSelected: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        FilterChip(
            selected = selectedGender == "men",
            onClick = { onGenderSelected("men") },
            label = { Text("Men") }
        )
        FilterChip(
            selected = selectedGender == "women",
            onClick = { onGenderSelected("women") },
            label = { Text("Women") }
        )
    }
}

@Composable
fun GameCard(game: GameEntity) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "${game.awayTeam} @ ${game.homeTeam}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            when (game.gameState.lowercase()) {
                "pre" -> {
                    Text("Status: Upcoming")
                    Text("Start Time: ${game.startTime}")
                }
                "live" -> {
                    Text("Status: In Progress")
                    Text("${game.awayTeam}: ${game.awayScore}")
                    Text("${game.homeTeam}: ${game.homeScore}")
                    Text("Period: ${formatPeriod(game.currentPeriod)}")
                    Text("Time Remaining: ${game.contestClock}")
                }
                "final" -> {
                    Text("Status: Final")
                    Text("${game.awayTeam}: ${game.awayScore}")
                    Text("${game.homeTeam}: ${game.homeScore}")
                    Text("Final")
                    Text("Winner: ${winnerName(game)}")
                }
                else -> {
                    Text("Status: ${game.gameState}")
                    if (game.awayScore.isNotBlank() || game.homeScore.isNotBlank()) {
                        Text("${game.awayTeam}: ${game.awayScore}")
                        Text("${game.homeTeam}: ${game.homeScore}")
                    }
                    if (game.startTime.isNotBlank()) {
                        Text("Start Time: ${game.startTime}")
                    }
                }
            }
        }
    }
}

fun winnerName(game: GameEntity): String {
    return when {
        game.awayWinner -> game.awayTeam
        game.homeWinner -> game.homeTeam
        else -> "Unknown"
    }
}

fun formatPeriod(period: String): String {
    return when (period.trim()) {
        "1" -> "1st"
        "2" -> "2nd"
        "3" -> "3rd"
        "4" -> "4th"
        else -> period
    }
}

@Preview(showBackground = true)
@Composable
fun GameCardPreview() {
    HWStarterRepoTheme {
        GameCard(
            game = GameEntity(
                gameId = "1",
                date = "2026-03-16",
                gender = "men",
                awayTeam = "UVA",
                homeTeam = "Duke",
                awayScore = "67",
                homeScore = "72",
                gameState = "final",
                startTime = "",
                currentPeriod = "2",
                contestClock = "0:00",
                finalMessage = "Final",
                awayWinner = false,
                homeWinner = true
            )
        )
    }
}