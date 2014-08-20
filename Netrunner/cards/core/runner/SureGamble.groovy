package Netrunner.cards.core.runner

import Netrunner.*

class SureGamble extends RunnerCard {

    def playCard() {
    	gs.runner.credits -= 5
    	gs.runner.credits += 9
    }
    
    def canPlay() {
    	return (gs.runner.credits >= 5)
    }
}
