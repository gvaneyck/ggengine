package LoveLetter.cards

import LoveLetter.Card
import com.gvaneyck.ggengine.Action

class Guard extends Card {

    def Guard() {
        value = 1
    }

    def playCard() {
        def actions = []
        (1..gs.maxPlayers).each {
            if (!gs[it].eliminated && it != gs.currentPlayer) {
                actions << new Action(this, 'makeGuess', [it])
            }
        }
        Card.gm.presentActions(actions)
    }

    def makeGuess(int target) {
        def actions = []
        (2..8).each {
            actions << new Action(this, 'guardEffect', [target, it])
        }
        Card.gm.presentActions(actions)
    }

    def guardEffect(int target, int card) {
        def cur = gs.currentPlayer
        def them = gs[target]

        if (them.immune) {
            return
        }

        if (them.hand.value == card) {
            them.eliminated = true
            gs.remainingPlayers--
        }
    }
}
