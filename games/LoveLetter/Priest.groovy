package LoveLetter

import LoveLetter.Card
import com.gvaneyck.ggengine.Action

class Priest extends Card {

    def Priest() {
        value = 2
    }
    
    def playCard() {
        def actions = []
        (1..gs.maxPlayers).each {
            if (!gs[it].eliminated && it != gs.currentPlayer) {
                actions << new Action('Priest.priestEffect', [it])
            }
        }
        gm.presentActions(actions)
    }
    
    def priestEffect(int target) {
        def cur = gs.currentPlayer
        def them = gs[target]
        
        if (them.immune) {
            gm.announce("Player ${cur} tried to get the priest to tell him about player ${target}, but he refused")        
            return
        }

        gm.announce("Player ${cur} convinced the priest to tell him about what player ${target} was doing")
        gm.announce(cur, "Player ${target} has ${them.hand}")
    }
}
