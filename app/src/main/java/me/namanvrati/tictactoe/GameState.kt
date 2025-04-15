package me.namanvrati.tictactoe

data class GameState(
    val board: List<List<String>> = List(3) { MutableList(3) { "" } },
    val currentPlayer: String = "X",
    val gameType: GameType = GameType.TWO_PLAYER,
    val difficulty: Difficulty = Difficulty.MEDIUM
) {
    fun checkWinner(): String? {
        val lines = listOf(
            // Rows
            board[0], board[1], board[2],
            // Columns
            listOf(board[0][0], board[1][0], board[2][0]),
            listOf(board[0][1], board[1][1], board[2][1]),
            listOf(board[0][2], board[1][2], board[2][2]),
            // Diagonals
            listOf(board[0][0], board[1][1], board[2][2]),
            listOf(board[0][2], board[1][1], board[2][0])
        )
        for (line in lines) {
            if (line.all { it == "X" }) return "X"
            if (line.all { it == "O" }) return "O"
        }
        return if (board.flatten().all { it.isNotEmpty() }) "Draw" else null
    }
}
