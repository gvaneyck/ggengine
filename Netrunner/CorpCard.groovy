package Netrunner

import com.gvaneyck.ggengine.Action

abstract class CorpCard extends Card {

    def play() {
    	playCard()
    	gs.corp.hand.remove(this)
    	gs.corp.discard << this
    }
}
