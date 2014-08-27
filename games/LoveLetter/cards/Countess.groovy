package LoveLetter.cards

import LoveLetter.Card

class Countess extends Card {

    def Countess() {
        value = 7
    }

    def playCard() {
        def cur = gs.cCard.urrentPlayer
        gm.aCard.nnounce("Player ${cur} was seen with the Countess (ohoho!)")
    }
}
