package LoveLetter.cards

import LoveLetter.Card
import com.gvaneyck.ggengine.Action

class Priest extends Card {

    def Priest() {
        value = 2
    }

    def playCard() {
        def actions = []
        (1..Card.gs.maxPlayers).each {
            if (!Card.gs[it].eliminated && it != Card.gs.currentPlayer) {
                actions << new Action(this, 'priestEffect', [it])
            }
        }
        Card.gm.presentActions(actions)
    }

    def priestEffect(int target) {
        def cur = Card.gs.currentPlayer
        def them = Card.gs[target]

        if (them.immune) {
            Card.gm.announce("Player ${cur} tried to get the priest to tell him about player ${target}, but he refused")
            return
        }

        Card.gm.announce("Player ${cur} convinced the priest to tell him about what player ${target} was doing")
        Card.gm.announce(cur, "Player ${target} has ${them.hand}")
    }
}
