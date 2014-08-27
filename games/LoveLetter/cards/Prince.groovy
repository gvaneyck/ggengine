package LoveLetter.cards

import LoveLetter.Card
import com.gvaneyck.ggengine.Action

class Prince extends Card {

    def Prince() {
        value = 5
    }

    def playCard() {
        def actions = []
        (1..Card.gs.maxPlayers).each {
            if (!Card.gs[it].eliminated) {
                actions << new Action(this, 'princeEffect', [it])
            }
        }
        Card.gm.presentActions(actions)
    }

    def princeEffect(int target) {
        def cur = Card.gs.currentPlayer
        def them = Card.gs[target]

        if (them.immune) {
            Card.gm.announce("Player ${cur} tries to find the prince, but he is nowhere to be found")
            return
        }

        if (them.hand.value == 8) {
            them.eliminated = true
            Card.gs.remainingPlayers--
            Card.gm.announce("Player ${cur} starts a rumor about player ${target} and the Princess, and the Prince finds out that it is actually true!")
            return
        }

        them.table << them.hand
        if (Card.gs.deck.isEmpty()) {
            them.hand = Card.gs.removedCard
        }
        else {
            them.hand = Card.gs.deck.remove(0)
        }

        Card.gm.announce("Player ${cur} starts a rumor about player ${target}, but it is quickly found out to be false")
    }
}
