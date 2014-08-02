import com.gvaneyck.ggengine.Action

abstract class Card {

    def gm
    def gs
    
    def value
    def name
    
    def Card() {
    	this.name = this.class.name
    }
        
    def play() {
        preTurn()
        playCard()
        postTurn()
    }
    
    abstract playCard()
    
    def preTurn() {
        def player = gs[gs.currentPlayer]
        
        def opt1 = player.hand
        def opt2 = player.hand2
        
        player.hand = (value == opt1.value ? opt2 : opt1)
        player.remove('hand2')
        player.table << value
        player.immune = false
    }
    
    def postTurn() {
        if (gs.remainingPlayers > 1) {
            changeTurn()
        }
    }
    
    def changeTurn() {
        def nextPlayer = gs.currentPlayer
        
        nextPlayer = (nextPlayer == gs.maxPlayers ? 1 : nextPlayer + 1)
        while (gs[nextPlayer].eliminated) {
            nextPlayer = (nextPlayer == gs.maxPlayers ? 1 : nextPlayer + 1)
        }
        
        gs.currentPlayer = nextPlayer
    }
    
    public String toString() {
    	return "${name} (${value})"
	}
	
	def getAction() {
		return new Action("${name}.play")
	}
}
