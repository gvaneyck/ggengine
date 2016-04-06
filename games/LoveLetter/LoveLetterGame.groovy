package LoveLetter

import LoveLetter.cards.Baron
import LoveLetter.cards.Countess
import LoveLetter.cards.Guard
import LoveLetter.cards.Handmaid
import LoveLetter.cards.King
import LoveLetter.cards.Priest
import LoveLetter.cards.Prince
import LoveLetter.cards.Princess
import com.gvaneyck.ggengine.Action
import com.gvaneyck.ggengine.Game

class LoveLetterGame extends Game {
    static gm
    static gs

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
        }

        gs.currentPlayer = 1
    }

    public void turn() {
        int cur = gs.currentPlayer

        def card1 = gs[cur].hand
        def card2 = gs.deck.remove(0)
        gs[cur].hand2 = card2

        if ((card1.value == 7 || card2.value == 7)
        	&& (card1.value == 6 || card1.value == 5 || card2.value == 6 || card2.value == 5)) {
            // Forced Duchess play
            gm.addAction(card1.value == 7 ? new Action(cur, card1, 'play') : new Action(cur, card2, 'play'))
        }
        else {
            // Play either card
            gm.addAction(new Action(cur, card1, 'play'))
            gm.addAction(new Action(cur, card2, 'play'))
        }
        gm.resolveActions()

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

    public boolean isFinished() {
        return (gs.remainingPlayers == 1 || gs.deck.size() == 0)
    }

    public Map end() {
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

        return [:]
    }
}
