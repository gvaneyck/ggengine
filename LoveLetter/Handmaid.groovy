class Handmain extends Card {

    def Handmain() {
        value = 4
    }
    
    def playCard() {
        def cur = gs.currentPlayer
        gs[cur].immune = true
        gm.announce("Player ${cur} convinces a handmaid to deflect unwanted attention for awhile")
    }
}
