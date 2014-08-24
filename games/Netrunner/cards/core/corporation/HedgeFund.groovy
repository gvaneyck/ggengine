package Netrunner.cards.core.corporation

import Netrunner.*

class HedgeFund extends OperationCard {

    def playCard() {
    	gs.corp.credits -= 5
    	gs.corp.credits += 9
    }

    def canPlay() {
    	return (gs.corp.credits >= 5)
    }
}