package LostCities

import com.gvaneyck.ggengine.gamestate.GameStateSerializer

// TODO make filter instead
// Serializer just for passing full map & changes over
class GSS extends GameStateSerializer{
    public String serializeGameState(Map gs, int player) {
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

        return serialize(temp, player)
    }
}
