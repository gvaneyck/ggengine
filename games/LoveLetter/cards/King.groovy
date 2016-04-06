package LoveLetter.cards

import LoveLetter.Card
import com.gvaneyck.ggengine.Action

class King extends Card {

    def King() {
        value = 6
    }

    def playCard() {
        (1..gs.maxPlayers).each {
            if (!gs[it].eliminated && it != gs.currentPlayer) {
                gm.addAction(new Action(gs.currentPlayer, this, 'kingEffect', [it]))
            }
        }
        gm.resolveActions()
    }

    def kingEffect(int target) {
        def cur = gs.currentPlayer
        def me = gs[cur]
        def them = gs[target]

        if (them.immune) {
            return
        }

        def temp = me.hand
        me.hand = them.hand
        them.hand = temp
    }
}
