package Netrunner

import com.gvaneyck.ggengine.Game
import com.gvaneyck.ggengine.Action

import Netrunner.cards.core.corporation.*
import Netrunner.cards.core.runner.*

class NetrunnerGame extends Game {
    static gm
    static gs
    
    public void init() {
        gs.events = []

    	gs.corp = [:]
        gs.corp.credits = 5
        gs.corp.maxHandSize = 5
        gs.corp.discard = []
        gs.corp.recurring = []

        gs.corp.servers = []
        (1..3).each { gs.corp.servers << [] }

    	gs.runner = [:]
        gs.runner.credits = 5
        gs.runner.maxHandSize = 5
		gs.runner.discard = []
        gs.runner.recurring = []

		gm.presentActions([ new Action(this, "mulliganCorp", [true]), new Action(this, "mulliganCorp", [false]) ])
		gm.presentActions([ new Action(this, "mulliganRunner", [true]), new Action(this, "mulliganRunner", [false]) ])
    }
    
    public void initCorpDeck() {
        gs.corp.deck = []
        (1..29).each { gs.corp.deck << new HedgeFund() }
        (1..20).each { gs.corp.deck << new WallOfStatic() }
        Collections.shuffle(gs.corp.deck)
        
        gs.corp.hand = []
        (1..5).each { gs.corp.hand << gs.corp.deck.remove(0) }
    }
    
    public void initRunnerDeck() {
        gs.runner.deck = []
        (1..45).each { gs.runner.deck << new SureGamble() }
        Collections.shuffle(gs.runner.deck)
        
        gs.runner.hand = []
        (1..5).each { gs.runner.hand << gs.runner.deck.remove(0) }
    }
    
    public void turn() {
        def actions

        gs.cur = gs.corp
    	gs.clicks = 3
    	corpDraw()
    	
    	while (gs.clicks > 0) {
    		actions = []
    		actions << new Action(this, "corpDraw")
    		actions << new Action(this, "corpCredit")
    		if (gs.runner.tags > 0) {
    			gs.runner.resources.each {
    				actions << new Action(this, "corpTrash", [it])
    			}
    		}
    		
    		gs.corp.hand.each {
    			if (it.canPlay()) {
    				actions << new Action(it, "play")
    			}
    		}
    		
    		gm.presentActions(actions)
    		gs.clicks--
    	}
    	
    	while (gs.corp.hand.size() > gs.corp.maxHandSize) {
    	   actions = []
    	   gs.corp.hand.each {
    	       actions << new Action(this, "corpDiscardCard", [it])
	       }
	       gm.presentActions(actions)
    	}

        gs.cur = gs.runner
    	gs.clicks = 4
    	
    	while (gs.clicks > 0) {
    		actions = []
    		actions << new Action(this, "runnerDraw")
    		actions << new Action(this, "runnerCredit")
    		if (gs.runner.tags > 0 && gs.runner.credits >= 2) {
    			actions << new Action(this, "runnerRemoveTag")
    		}
    		
    		gs.runner.hand.each {
    			if (it.canPlay()) {
    				actions << new Action(it, "play")
    			}
    		}
    		
    		gm.presentActions(actions)
    		gs.clicks--
    	}

        while (gs.runner.hand.size() > gs.runner.maxHandSize) {
           actions = []
           gs.runner.hand.each {
               actions << new Action(this, "runnerDiscardCard", [it])
           }
           gm.presentActions(actions)
        }
    }

    public void mulliganCorp(Boolean doMulligan) {
    	if (doMulligan) {
	        initCorpDeck()
		}
    }

    public void mulliganRunner(Boolean doMulligan) {
    	if (doMulligan) {
	        initRunnerDeck()
		}
    }
    
    public void corpDraw() {
        gs.corp.hand << gs.corp.deck.remove(0)
    }
    
    public void corpCredit() {
    	gs.corp.credits++
    }
    
    public void corpTrashResource(Card target) {
    	gs.runner.heap << target
    	gs.corp.credits -= 2
    }
    
    public void corpDiscardCard(Card c) {
        gs.corp.hand.remove(c)
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
    
    public void runnerDiscardCard(Card c) {
        gs.runner.hand.remove(c)
    }
    
    public boolean isFinished() {
        return false
    }

    public void end() {
        gm.announce("WTF")
    }
}
