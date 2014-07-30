import com.gvaneyck.ggengine.Action

class Prince extends Card {

	def Prince() {
		value = 5
	}
	
	def playCard() {
		def actions = []
    	(1..gs.maxPlayers).each {
    		if (!gs[it].eliminated) {
    			actions << new Action('Prince.princeEffect', [it])
    		}
    	}
    	gm.presentActions(actions)
	}
	
	def princeEffect(int target) {
		def cur = gs.currentPlayer
		def them = gs[target]
		
		if (them.immune) {
			gm.announce("Player ${cur} tries to find the prince, but he is nowhere to be found")
			return
		}

		if (them.hand) {
			them.eliminated = true
			gs.remainingPlayers--
			gm.announce("Player ${cur} starts a rumor about player ${target} and the Princess, and the Prince finds out that it is actually true!")
			return
		}

		them.table << them.hand
		if (gs.deck.isEmpty()) {
			them.hand = gs.removedCard
		}
		else {
			them.hand = gs.deck.remove(0)
		}
		
		gm.announce("Player ${cur} starts a rumor about player ${target}, but it is quickly found out to be false")
	}
}
