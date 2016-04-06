package Splendor

import com.gvaneyck.ggengine.gamestate.GameStateFilter
import groovy.json.JsonException
import groovy.json.JsonOutput

class GSF implements GameStateFilter {
    public Map filterGameState(Map gs, int player) {
        def temp = [:]
        (1..gs.players).each {
            temp[it] = gs[it].clone()
            if (player != it) {
                temp[it].stash = gs[it].stash.collect { it2 -> new Card(tier: it2.tier) }
            }
        }

        temp.markets = gs.markets
        temp.bank = gs.bank
        temp.decks = [
                gs.decks[0].size(),
                gs.decks[1].size(),
                gs.decks[2].size()
        ]
        temp.nobles = gs.nobles
        temp.currentPlayer = gs.currentPlayer
        temp.players = gs.players

//        println "Player " + player
//        println JsonOutput.prettyPrint(JsonOutput.toJson(temp))
//        println player + ' ' + JsonException.toJson(temp)
        return temp
    }
}
