package Netrunner

import com.gvaneyck.ggengine.Game
import com.gvaneyck.ggengine.Action

import Netrunner.cards.core.corporation.HedgeFund
import Netrunner.cards.core.runner.SureGamble

class NetrunnerGame extends Game {
    static gm
    static gs
    
    public void init() {
    	gs.corp = [:]
    	gs.runner = [:]
    	
        gs.corp.credits = 5
        gs.corp.deck = []
        (1..49).each { gs.corp.deck << new HedgeFund() }
        Collections.shuffle(gs.corp.deck)
        
        gs.corp.hand = []
		(1..5).each { gs.corp.hand << gs.corp.deck.remove(0) }

        gs.runner.credits = 5
        gs.runner.deck = []
        (1..45).each { gs.runner.deck << new SureGamble() }
        Collections.shuffle(gs.runner.deck)
        
        gs.runner.hand = []
		(1..5).each { gs.runner.hand << gs.runner.deck.remove(0) }

		gm.presentActions([ new Action("NetrunnerGame.mulliganCorp", [true]), new Action("NetrunnerGame.mulliganCorp", [false]) ])
		gm.presentActions([ new Action("NetrunnerGame.mulliganRunner", [true]), new Action("NetrunnerGame.mulliganRunner", [false]) ])
    }
    
    public void turn() {
        gs.cur = gs.corp
    	gs.clicks = 3
    	corpDraw()
    	
    	while (gs.clicks > 0) {
    		def actions = []
    		actions << new Action("NetrunnerGame.corpDraw")
    		actions << new Action("NetrunnerGame.corpCredit")
    		if (gs.runner.tags > 0) {
    			gs.runner.resources.each {
    				actions << new Action("NetrunnerGame.corpTrash", [it])
    			}
    		}
    		
    		gs.corp.hand.each {
    			if (it.canPlay()) {
    				actions << it.getAction()
    			}
    		}
    		
    		gm.presentActions(actions)
    		gs.clicks--
    	}

        gs.cur = gs.runner
    	gs.clicks = 4
    	
    	while (gs.clicks > 0) {
    		def actions = []
    		actions << new Action("NetrunnerGame.runnerDraw")
    		actions << new Action("NetrunnerGame.runnerCredit")
    		if (gs.runner.tags > 0 && gs.runner.credits >= 2) {
    			actions << new Action("NetrunnerGame.runnerRemoveTag")
    		}
    		
    		gs.runner.hand.each {
    			if (it.canPlay()) {
    				actions << it.getAction()
    			}
    		}
    		
    		gm.presentActions(actions)
    		gs.clicks--
    	}
    }
    
    public void mulliganCorp(doMulligan) {
    	if (doMulligan) {
	        gs.corp.deck = []
	        (1..49).each { gs.corp.deck << new HedgeFund() }
	        Collections.shuffle(gs.corp.deck)
	        
	        gs.corp.hand = []
			(1..5).each { gs.corp.hand << gs.corp.deck.remove(0) }
		}
    }

    public void mulliganRunner(doMulligan) {
    	if (doMulligan) {
	        gs.runner.deck = []
	        (1..45).each { gs.runner.deck << new SureGamble() }
	        Collections.shuffle(gs.runner.deck)
	        
	        gs.runner.hand = []
			(1..5).each { gs.runner.hand << gs.runner.deck.remove(0) }
		}
    }
    
    public void corpDraw() {
        gs.corp.hand << gs.corp.deck.remove(0)
    }
    
    public void corpCredit() {
    	gs.corp.credits++
    }
    
    public void corpTrash(target) {
    	gs.runner.heap << target
    	gs.corp.credits -= 2
    }
    
    public void runnerDraw() {
        gs.runner.hand << gs.runner.deck.remove(0)
    }
    
    public void runnerCredit() {
    	gs.runner.credits++
    }
    
    public void runnerRemoveTag() {
    	gs.runner.credits -= 2
    	gs.runner.tags--
    }
    
    public boolean isFinished() {
        return false
    }

    public void end() {
        gm.announce("WTF")
    }
}
