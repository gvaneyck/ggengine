class Countess extends Card {

    def Countess() {
        value = 7
    }
    
    def playCard() {
        def cur = gs.currentPlayer
        gm.announce("Player ${cur} was seen with the Countess (ohoho!)")
    }
}
