package Netrunner

import com.gvaneyck.ggengine.Action

abstract class RunnerCard extends Card {

    def play() {
    	playCard()
    	gs.runner.hand.remove(this)
    	gs.runner.discard << this
    }
}
