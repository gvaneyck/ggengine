package LoveLetter

import LoveLetter.Card

class Handmaid extends Card {

    def Handmaid() {
        value = 4
    }
    
    def playCard() {
        def cur = gs.currentPlayer
        gs[cur].immune = true
        gm.announce("Player ${cur} convinces a handmaid to deflect unwanted attention for awhile")
    }
}
