package LoveLetter.cards

import LoveLetter.Card

class Princess extends Card {

    def Princess() {
        value = 8
    }

    def playCard() {
        def cur = gs.currentPlayer
        gs[cur].eliminated = true
        gs.remainingPlayers--
    }
}
