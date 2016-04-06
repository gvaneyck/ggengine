package LoveLetter.cards

import LoveLetter.Card
import com.gvaneyck.ggengine.Action

class Guard extends Card {

    def Guard() {
        value = 1
    }

    def playCard() {
        (1..gs.maxPlayers).each {
            if (!gs[it].eliminated && it != gs.currentPlayer) {
                gm.addAction(new Action(gs.currentPlayer, this, 'makeGuess', [it]))
            }
        }
        gm.resolveActions()
    }

    def makeGuess(int target) {
        (2..8).each {
            gm.addAction(new Action(gs.currentPlayer, this, 'guardEffect', [target, it]))
        }
        gm.resolveActions()
    }

    def guardEffect(int target, int card) {
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
