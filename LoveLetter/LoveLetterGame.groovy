import com.gvaneyck.ggengine.Game
import com.gvaneyck.ggengine.Action

class LoveLetterGame extends Game {
    def gm
    def gs
    
    public void init() {
        gs.maxPlayers = 2
        gs.remainingPlayers = 2
        
        gs.deck = []
        (1..1).each { gs.deck << 8 }
        (1..1).each { gs.deck << 7 }
        (1..1).each { gs.deck << 6 }
        (1..2).each { gs.deck << 5 }
        (1..2).each { gs.deck << 4 }
        (1..2).each { gs.deck << 3 }
        (1..2).each { gs.deck << 2 }
        (1..5).each { gs.deck << 1 }
        Collections.shuffle(gs.deck)
        
        gs.removedCard = gs.deck.remove(0)
        
        (1..gs.maxPlayers).each {
            gs[it] = [ hand: gs.deck.remove(0), table: [], eliminated: false, immune: false ]
        }

        gs.currentPlayer = 1
    }
    
    public void turn() {
        def cur = gs.currentPlayer
        gm.announce("Player ${cur}'s turn")
        
        def card1 = gs[cur].hand
        def card2 = gs.deck.remove(0)
        gs[cur].hand2 = card2
        gm.announce(cur, "Your hand is ${card1} and you drew ${card2}")

        def actions = []
        if ((card1 == 7 || card2 == 7) && (card1 == 6 || card1 == 5 || card2 == 6 || card2 == 5)) {
            actions << buildAction(7)
        }
        else {
            actions << buildAction(card1)
            actions << buildAction(card2)
        }

        gm.presentActions(actions)
    }
    
    def buildAction(int card) {
        switch (card) {
            case 8: return new Action('Princess.play')
            case 7: return new Action('Countess.play')
            case 6: return new Action('King.play')
            case 5: return new Action('Prince.play')
            case 4: return new Action('Handmaid.play')
            case 3: return new Action('Baron.play')
            case 2: return new Action('Priest.play')
            case 1:    return new Action('Guard.play')
        }
    }
    
    public boolean isFinished() {
        return (gs.remainingPlayers == 1 || gs.deck.size() == 0)
    }

    public void end() {
        def winner
        def highest = 0
        def tiebreaker = 0
        (1..gs.maxPlayers).each {
            def tb = 0
            gs[it].table.each { it2 -> tb += it2 }
            if (!gs[it].eliminated && (gs[it].hand > highest || gs[it].hand == highest && tb > tiebreaker)) {
                winner = it
                highest = gs[it].hand
                tiebreaker = tb
            }
        }

        gm.announce("Winner is ${winner}")
    }
}
