package LostCities

import com.gvaneyck.ggengine.gamestate.GameStateFilter

class GSF implements GameStateFilter {
    public Map filterGameState(Map gs, int player) {
        def temp = [:]
        temp[1] = [table: gs[1].table]
        temp[2] = [table: gs[2].table]

        if (player == 1) {
            temp[1].hand = gs[1].hand
        }
        else if (player == 2) {
            temp[2].hand = gs[2].hand
        }

        temp.discard = gs.discard
        temp.deck = gs.deck.size()
        temp.currentPlayer = gs.currentPlayer

        println player + " " + temp
        return temp
    }
}
