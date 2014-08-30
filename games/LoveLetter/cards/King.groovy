package LoveLetter.cards

import LoveLetter.Card
import com.gvaneyck.ggengine.Action

class King extends Card {

    def King() {
        value = 6
    }

    def playCard() {
        def actions = []
        (1..gs.maxPlayers).each {
            if (!gs[it].eliminated && it != gs.currentPlayer) {
                actions << new Action(this, 'kingEffect', [it])
            }
        }
        Card.gm.presentActions(actions)
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
