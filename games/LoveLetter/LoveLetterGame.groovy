package LoveLetter

import LoveLetter.Baron
import LoveLetter.Countess
import LoveLetter.Guard
import LoveLetter.Handmaid
import LoveLetter.King
import LoveLetter.Priest
import LoveLetter.Prince
import LoveLetter.Princess
import com.gvaneyck.ggengine.Game
import com.gvaneyck.ggengine.Action

class LoveLetterGame extends Game {
    def gm
    def gs
    
    public void init() {
        gs.remainingPlayers = gs.maxPlayers
        
        gs.deck = []
        (1..1).each { gs.deck << new Princess() }
        (1..1).each { gs.deck << new Countess() }
        (1..1).each { gs.deck << new King() }
        (1..2).each { gs.deck << new Prince() }
        (1..2).each { gs.deck << new Handmaid() }
        (1..2).each { gs.deck << new Baron() }
        (1..2).each { gs.deck << new Priest() }
        (1..5).each { gs.deck << new Guard() }
        Collections.shuffle(gs.deck)
        
        gs.removedCard = gs.deck.remove(0)
        
        (1..gs.maxPlayers).each {
            gs[it] = [ hand: gs.deck.remove(0), table: [], eliminated: false, immune: false ]
            gm.announce(it, "Your starting hand is ${gs[it].hand}")
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
        if ((card1.value == 7 || card2.value == 7)
        	&& (card1.value == 6 || card1.value == 5 || card2.value == 6 || card2.value == 5)) {
            actions << buildAction(new Action('Countess.play'))
        }
        else {
            actions << card1.getAction()
            actions << card2.getAction()
        }

        gm.presentActions(actions)
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
            gs[it].table.each { it2 -> tb += it2.value }
            if (!gs[it].eliminated && (gs[it].hand.value > highest || gs[it].hand.value == highest && tb > tiebreaker)) {
                winner = it
                highest = gs[it].hand.value
                tiebreaker = tb
            }
        }

        gm.announce("Winner is ${winner} with ${highest}")
    }
}
