package LoveLetter.cards

import LoveLetter.Card
import com.gvaneyck.ggengine.Action

class Baron extends Card {

    def Baron() {
        value = 3
    }

    def playCard() {
        (1..gs.maxPlayers).each {
            if (!gs[it].eliminated && it != gs.currentPlayer) {
                gm.addAction(new Action(gs.currentPlayer, this, 'baronEffect', [it]))
            }
        }
        gm.resolveActions()
    }

    def baronEffect(int target) {
        def cur = gs.currentPlayer
        def me = gs[cur]
        def them = gs[target]

        if (them.immune) {
            return
        }

        if (me.hand.value > them.hand.value) {
            them.eliminated = true
            gs.remainingPlayers--
        }
        else if (me.hand.value < them.hand.value) {
            me.eliminated = true
            gs.remainingPlayers--
        }
    }
}
