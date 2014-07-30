import com.gvaneyck.ggengine.Game
import com.gvaneyck.ggengine.Action

class TicTacToeGame extends Game {
    def gs
    
	public void init() {
        gs.currentPlayer = 1
        gs.board = new int[3][3]
	}
	
	public void turn() {
        def board = gs.board
        def actions = []
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == 0)
                    actions << new Action('TicTacToeActions.play', [i, j])
            }
        }
        presentActions(actions)
	}
	
	public boolean isFinished() {
        return (getWinner() != 0)
	}

    public void end() {
    	System.out.println(getWinner())
    }

    def getWinner() {
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