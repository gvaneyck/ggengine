import com.gvaneyck.ggengine.Action

class King extends Card {

    def King() {
        value = 6
    }
    
    def playCard() {
        def actions = []
        (1..gs.maxPlayers).each {
            if (!gs[it].eliminated && it != gs.currentPlayer) {
                actions << new Action('King.kingEffect', [it])
            }
        }
        gm.presentActions(actions)
    }
    
    def kingEffect(int target) {
        def cur = gs.currentPlayer
        def me = gs[cur]
        def them = gs[target]
        
        if (them.immune) {
            gm.announce("Player ${cur} was about to ask a favor of the king, but was interrupted by a handmaid")  
            return
        }

        def temp = me.hand
        me.hand = them.hand
        them.hand = temp
        
        gm.announce("Player ${cur} asked a favor of the king, and was granted it with player ${target}")            
    }
}
