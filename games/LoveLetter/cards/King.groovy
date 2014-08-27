package LoveLetter.cards

import LoveLetter.Card
import com.gvaneyck.ggengine.Action

class King extends Card {

    def King() {
        value = 6
    }

    def playCard() {
        def actions = []
        (1..Card.gs.maxPlayers).each {
            if (!Card.gs[it].eliminated && it != Card.gs.currentPlayer) {
                actions << new Action(this, 'kingEffect', [it])
            }
        }
        Card.gm.presentActions(actions)
    }

    def kingEffect(int target) {
        def cur = Card.gs.currentPlayer
        def me = Card.gs[cur]
        def them = Card.gs[target]

        if (them.immune) {
            Card.gm.announce("Player ${cur} was about to ask a favor of the king, but was interrupted by a handmaid")
            return
        }

        def temp = me.hand
        me.hand = them.hand
        them.hand = temp

        Card.gm.announce("Player ${cur} asked a favor of the king, and was granted it with player ${target}")
    }
}
