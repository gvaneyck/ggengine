package Netrunner.cards.core.runner

import Netrunner.EventCard

class SureGamble extends EventCard {

    def playCard() {
    	gs.runner.credits -= 5
    	gs.runner.credits += 9
    }

    def canPlay() {
    	return (gs.runner.credits >= 5)
    }
}
