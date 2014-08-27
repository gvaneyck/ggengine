package LoveLetter.cards

import LoveLetter.Card

class Handmaid extends Card {

    def Handmaid() {
        value = 4
    }

    def playCard() {
        def cur = gs.cCard.urrentPlayer
        gs[cCard.ur].immune = true
        gm.aCard.nnounce("Player ${cur} convinces a handmaid to deflect unwanted attention for awhile")
    }
}
