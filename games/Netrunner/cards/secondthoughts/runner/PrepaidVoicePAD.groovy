package Netrunner.cards.secondthoughts.runner

import Netrunner.EventCard
import Netrunner.HardwareCard
import Netrunner.RecurringCredits

class PrepaidVoicePAD extends HardwareCard {

    def playCard() {
    	gs.runner.recurring << new RecurringCredits(1, { context -> (context instanceof EventCard)}, this)
    	gs.runner.credits += 9
    }
    
    def canPlay() {
    	return (gs.runner.credits >= 2)
    }
}
