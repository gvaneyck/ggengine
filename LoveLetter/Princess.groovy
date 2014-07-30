class Princess extends Card {

	def Princess() {
		value = 8
	}
	
	def playCard() {
		def cur = gs.currentPlayer
    	gs[cur].eliminated = true
    	gs.remainingPlayers--
    	gm.announce("Player ${cur} is accidentally seen with the Princess and expelled!")
	}
}
