import com.gvaneyck.ggengine.Action

class Baron extends Card {

    def Baron() {
        value = 3
    }
    
    def playCard() {
        def actions = []
        (1..gs.maxPlayers).each {
            if (!gs[it].eliminated && it != gs.currentPlayer) {
                actions << new Action('Baron.baronEffect', [it])
            }
        }
        gm.presentActions(actions)
    }
    
    def baronEffect(int target) {
        def cur = gs.currentPlayer
        def me = gs[cur]
        def them = gs[target]
        
        if (them.immune) {
            gm.announce("Player ${cur} asked the baron for an appraisal of player ${target}, but he was sidetracked by a handmaid")
            return
        }
            
        if (me.hand > them.hand) {
            them.eliminated = true
            gs.remainingPlayers--
            gm.announce("Player ${cur} asked the baron to compare himself with player ${target} and came out on top! (${them.hand})")
        }
        else if (me.hand < them.hand) {
            me.eliminated = true
            gs.remainingPlayers--
            gm.announce("Player ${cur} asked the baron to compare himself with player ${target}, but it backfired! (${me.hand})")
        }
        else {
            gm.announce("Player ${cur} asked the baron to compare himself with player ${target} and they were found equal")
        }
    }
}
