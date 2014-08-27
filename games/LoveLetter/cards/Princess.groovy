package LoveLetter.cards

import LoveLetter.Card

class Princess extends Card {

    def Princess() {
        value = 8
    }

    def playCard() {
        def cur = Card.gs.currentPlayer
        Card.gs[cur].eliminated = true
        Card.gs.remainingPlayers--
        Card.gm.announce("Player ${cur} is accidentally seen with the Princess and expelled!")
    }
}
