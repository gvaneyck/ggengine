package LoveLetter.cards

import LoveLetter.Card
import com.gvaneyck.ggengine.Action

class Prince extends Card {

    def Prince() {
        value = 5
    }

    def playCard() {
        def actions = []
        (1..gs.maxPlayers).each {
            if (!gs[it].eliminated) {
                actions << new Action(this, 'princeEffect', [it])
            }
        }
        Card.gm.presentActions(actions)
    }

    def princeEffect(int target) {
        def cur = gs.currentPlayer
        def them = gs[target]

        if (them.immune) {
            return
        }

        if (them.hand.value == 8) {
            them.eliminated = true
            gs.remainingPlayers--
            return
        }

        them.table << them.hand
        if (gs.deck.isEmpty()) {
            them.hand = gs.removedCard
        }
        else {
            them.hand = gs.deck.remove(0)
        }
    }
}
