package LoveLetter.cards

import LoveLetter.Card
import com.gvaneyck.ggengine.Action

class Priest extends Card {

    def Priest() {
        value = 2
    }

    def playCard() {
        def actions = []
        (1..gs.maxPlayers).each {
            if (!gs[it].eliminated && it != gs.currentPlayer) {
                actions << new Action(this, 'priestEffect', [it])
            }
        }
        Card.gm.presentActions(actions)
    }

    def priestEffect(int target) {
        def cur = gs.currentPlayer
        def them = gs[target]

        if (them.immune) {
            return
        }
    }
}
