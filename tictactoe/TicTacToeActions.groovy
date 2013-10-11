class TicTacToeActions {
    def gs
    
    def play(int x, int y) {
        gs.board[x][y] = gs.currentPlayer
    }
    
    def changeTurn() {
        if (gs.currentPlayer == 1)
            gs.currentPlayer = 2
        else
            gs.currentPlayer = 1
    }
}