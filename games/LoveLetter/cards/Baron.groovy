package LoveLetter.cards

import LoveLetter.Card
import com.gvaneyck.ggengine.Action

class Baron extends Card {

    def Baron() {
        value = 3
    }

    def playCard() {
        def actions = []
        (1..Card.gs.maxPlayers).each {
            if (!Card.gs[it].eliminated && it != Card.gs.currentPlayer) {
                actions << new Action(this, 'baronEffect', [it])
            }
        }
        Card.gm.presentActions(actions)
    }

    def baronEffect(int target) {
        def cur = Card.gs.currentPlayer
        def me = Card.gs[cur]
        def them = Card.gs[target]

        if (them.immune) {
            Card.gm.announce("Player ${cur} asked the baron for an appraisal of player ${target}, but he was sidetracked by a handmaid")
            return
        }

        if (me.hand.value > them.hand.value) {
            them.eliminated = true
            Card.gs.remainingPlayers--
            Card.gm.announce("Player ${cur} asked the baron to compare himself with player ${target} and came out on top! (${them.hand})")
        }
        else if (me.hand.value < them.hand.value) {
            me.eliminated = true
            Card.gs.remainingPlayers--
            Card.gm.announce("Player ${cur} asked the baron to compare himself with player ${target}, but it backfired! (${me.hand})")
        }
        else {
            Card.gm.announce("Player ${cur} asked the baron to compare himself with player ${target} and they were found equal")
        }
    }
}
