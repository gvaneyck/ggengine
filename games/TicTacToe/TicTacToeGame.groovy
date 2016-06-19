package TicTacToe

import com.gvaneyck.ggengine.game.Game
import com.gvaneyck.ggengine.game.GameInstance
import com.gvaneyck.ggengine.game.actions.Action

class TicTacToeGame implements Game {

    public void init(GameInstance gm, Map<String, Object> gs) {
        gs.currentPlayer = 1
        gs.board = new int[3][3]
    }

    public void turn(GameInstance gm, Map<String, Object> gs) {
        def board = gs.board
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == 0)
                    gm.addAction(gs.currentPlayer, 'play', [i, j])
            }
        }
        gm.resolveOneAction()

        gs.currentPlayer = (gs.currentPlayer == 1 ? 2 : 1)
    }

    public boolean isFinished(GameInstance gm, Map<String, Object> gs) {
        return (getWinner(gs) != 0)
    }

    public Map end(GameInstance gm, Map<String, Object> gs) {
        System.out.println(getWinner(gs))
        return [:]
    }

    @Action('play')
    def play = { gm, gs, x, y ->
        gs.board[x][y] = gs.currentPlayer
    }

    def getWinner = { gs ->
        def board = gs.board

        if (board[0][0] != 0 && board[0][0] == board[1][1] && board[0][0] == board[2][2])
            return board[0][0]

        if (board[2][0] != 0 && board[2][0] == board[1][1] && board[2][0] == board[0][2])
            return board[2][0]

        for (int i = 0; i < 3; i++) {
            if (board[i][0] != 0 && board[i][0] == board[i][1] && board[i][0] == board[i][2])
                return board[i][0]
            if (board[0][i] != 0 && board[0][i] == board[1][i] && board[0][i] == board[2][i])
                return board[0][i]
        }

        return 0
    }

}
