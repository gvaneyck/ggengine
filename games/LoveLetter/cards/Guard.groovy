package LoveLetter.cards

import LoveLetter.Card
import com.gvaneyck.ggengine.Action

class Guard extends Card {

    def Guard() {
        value = 1
    }

    def playCard() {
        def actions = []
        (1..Card.gs.maxPlayers).each {
            if (!Card.gs[it].eliminated && it != Card.gs.currentPlayer) {
                actions << new Action(this, 'makeGuess', [it])
            }
        }
        Card.gm.presentActions(actions)
    }

    def makeGuess(int target) {
        def actions = []
        (2..8).each {
            actions << new Action(this, 'guardEffect', [target, it])
        }
        Card.gm.presentActions(actions)
    }

    def guardEffect(int target, int card) {
        def cur = Card.gs.currentPlayer
        def them = Card.gs[target]

        if (them.immune) {
            Card.gm.announce("Player ${cur} told a guard to watch out for player ${target}, but the guard got distracted by a handmaid")
            return
        }

        if (them.hand.value == card) {
            them.eliminated = true
            Card.gs.remainingPlayers--
            Card.gm.announce("Player ${cur} told a guard about a consipracy between player ${target} and ${them.hand}, and they were caught!")
        }
        else {
            Card.gm.announce("Player ${cur} asked a guard to watch out for player ${target} and ${card}, but he didn't find them")
        }
    }
}
