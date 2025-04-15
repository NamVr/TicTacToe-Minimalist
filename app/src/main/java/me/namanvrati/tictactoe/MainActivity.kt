package me.namanvrati.tictactoe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.ArrowBack

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var selectedMode by remember { mutableStateOf<Pair<Boolean, String?>?>(null) }

            if (selectedMode == null) {
                ModeSelectionScreen { isSinglePlayer, difficulty ->
                    selectedMode = Pair(isSinglePlayer, difficulty)
                }
            } else {
                val (isSinglePlayer, difficulty) = selectedMode!!

                // Back press handler to go back to mode selection
                BackHandler {
                    selectedMode = null
                }

                TicTacToeGame(
                    isSinglePlayer = isSinglePlayer,
                    difficulty = difficulty,
                    onBack = { selectedMode = null }
                )
            }
        }

    }
}

/* --------------- AI Logic with Immutable Board ------------------ */

// Returns a new state with AI move applied.
fun makeAIMove(state: GameState): GameState {
    if (state.checkWinner() != null || state.currentPlayer != "O") return state

    val (row, col) = when (state.difficulty) {
        Difficulty.EASY -> findFirstAvailableMove(state)
        Difficulty.MEDIUM -> findRandomMove(state)
        Difficulty.HARD -> findBestMove(state) // Minimax will determine the best move.
    }

    return if (row != -1 && col != -1) {
        val updatedBoard = state.board.mapIndexed { i, rowList ->
            rowList.mapIndexed { j, cell ->
                if (i == row && j == col) "O" else cell
            }
        }
        state.copy(
            board = updatedBoard,
            currentPlayer = "X",
        )
    } else state
}

private fun findFirstAvailableMove(state: GameState): Pair<Int, Int> {
    for (i in 0..2) {
        for (j in 0..2) {
            if (state.board[i][j].isEmpty()) return Pair(i, j)
        }
    }
    return Pair(-1, -1)
}

private fun findRandomMove(state: GameState): Pair<Int, Int> {
    val emptyCells = mutableListOf<Pair<Int, Int>>()
    for (i in 0..2) {
        for (j in 0..2) {
            if (state.board[i][j].isEmpty()) {
                emptyCells.add(Pair(i, j))
            }
        }
    }
    return if (emptyCells.isNotEmpty()) emptyCells.random() else Pair(-1, -1)
}

private fun minimax(state: GameState, depth: Int, isMaximizing: Boolean): Int {
    val winner = state.checkWinner()
    if (winner != null) return when (winner) {
        "O" -> 10 - depth
        "X" -> -10 + depth
        "Draw" -> 0
        else -> 0
    }


    return if (isMaximizing) {
        var best = Int.MIN_VALUE
        for (i in 0..2) {
            for (j in 0..2) {
                if (state.board[i][j].isEmpty()) {
                    val newBoard = state.board.mapIndexed { row, rowList ->
                        rowList.mapIndexed { col, cell ->
                            if (row == i && col == j) "O" else cell
                        }
                    }
                    best = maxOf(best, minimax(state.copy(board = newBoard), depth + 1, false))
                }
            }
        }
        best
    } else {
        var best = Int.MAX_VALUE
        for (i in 0..2) {
            for (j in 0..2) {
                if (state.board[i][j].isEmpty()) {
                    val newBoard = state.board.mapIndexed { row, rowList ->
                        rowList.mapIndexed { col, cell ->
                            if (row == i && col == j) "X" else cell
                        }
                    }
                    best = minOf(best, minimax(state.copy(board = newBoard), depth + 1, true))
                }
            }
        }
        best
    }
}

private fun findBestMove(state: GameState): Pair<Int, Int> {
    var bestScore = Int.MIN_VALUE
    var move = Pair(-1, -1)
    for (i in 0..2) {
        for (j in 0..2) {
            if (state.board[i][j].isEmpty()) {
                val newBoard = state.board.mapIndexed { row, rowList ->
                    rowList.mapIndexed { col, cell ->
                        if (row == i && col == j) "O" else cell
                    }
                }
                val score = minimax(state.copy(board = newBoard), 0, false)
                if (score > bestScore) {
                    bestScore = score
                    move = Pair(i, j)
                }
            }
        }
    }
    return move
}

/* --------------- UI Composable(s) ------------------ */

@Composable
fun ModeSelectionScreen(onModeSelected: (Boolean, String?) -> Unit) {
    var showDifficultyDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tic Tac Toe",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = { onModeSelected(false, null) },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(vertical = 8.dp)
        ) {
            Text("Two Player Mode")
        }

        Button(
            onClick = { showDifficultyDialog = true },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(vertical = 8.dp)
        ) {
            Text("Single Player Mode")
        }

        if (showDifficultyDialog) {
            AlertDialog(
                onDismissRequest = { showDifficultyDialog = false },
                title = { Text("Select Difficulty") },
                text = {
                    Column {
                        Button(onClick = {
                            showDifficultyDialog = false
                            onModeSelected(true, "Easy")
                        }) { Text("Easy 😴") }
                        Button(onClick = {
                            showDifficultyDialog = false
                            onModeSelected(true, "Medium")
                        }) { Text("Medium 🤔") }
                        Button(onClick = {
                            showDifficultyDialog = false
                            onModeSelected(true, "Hard")
                        }) { Text("Hard 🧠") }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToeGame(isSinglePlayer: Boolean, difficulty: String?, onBack: () -> Unit) {
    var gameState by remember {
        mutableStateOf(
            GameState(
                gameType = if (isSinglePlayer) GameType.SINGLE_PLAYER else GameType.TWO_PLAYER,
                difficulty = when (difficulty) {
                    "Easy" -> Difficulty.EASY
                    "Medium" -> Difficulty.MEDIUM
                    "Hard" -> Difficulty.HARD
                    else -> Difficulty.MEDIUM
                }
            )
        )
    }

    val context = LocalContext.current

    // AI Move Trigger for Single Player mode
    LaunchedEffect(gameState.currentPlayer, gameState.board, gameState.checkWinner()) {
        if (
            gameState.gameType == GameType.SINGLE_PLAYER &&
            gameState.currentPlayer == "O" &&
            gameState.checkWinner() == null
        ) {
            delay(100) // Simulate thinking delay
            gameState = makeAIMove(gameState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color(0xFF121212),
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.gle/dJYzXDQxQdgXyEE27"))
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Feedback,
                            contentDescription = "Feedback",
                            tint = Color.White
                        )
                    }
                },
            )
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212))
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Optional: display current turn
                    Text(
                        text = "Turn: ${gameState.currentPlayer}",
                        fontSize = 18.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Tic Tac Toe",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )

                    for (row in 0..2) {
                        Row {
                            for (col in 0..2) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .padding(4.dp)
                                        .background(Color.DarkGray, RoundedCornerShape(8.dp))
                                        .clickable(
                                            enabled = gameState.board[row][col].isEmpty() &&
                                                    gameState.checkWinner() == null &&
                                                    (gameState.gameType == GameType.TWO_PLAYER || gameState.currentPlayer == "X")
                                        ) {
                                            // Update board immutably on player move
                                            val updatedBoard = gameState.board.mapIndexed { i, rowList ->
                                                rowList.mapIndexed { j, cell ->
                                                    if (i == row && j == col) gameState.currentPlayer else cell
                                                }
                                            }
                                            val nextPlayer = if (gameState.currentPlayer == "X") "O" else "X"
                                            gameState = gameState.copy(
                                                board = updatedBoard,
                                                currentPlayer = nextPlayer,
                                            )

                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = gameState.board[row][col],
                                        fontSize = 32.sp,
                                        color = if (gameState.board[row][col] == "X") Color.Cyan else Color.Yellow
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    gameState.checkWinner()?.let { winner ->
                        Text(
                            text = if (winner == "Draw") "It's a Draw!" else "$winner Wins!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Button(
                            onClick = {
                                gameState = GameState(gameType = gameState.gameType, difficulty = gameState.difficulty)
                            },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Restart Game")
                        }
                    }
                }
            }
        }
    }
}
