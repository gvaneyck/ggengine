package Netrunner

import com.gvaneyck.ggengine.Action

abstract class Card {

    static gm
    static gs
    
    def name
    
    def Card() {
    	this.name = this.class.name
    }
    
    abstract play()
    
    abstract playCard()
    
    abstract canPlay()
    
    public String toString() {
    	return "${name}"
	}
	
	def getAction() {
		return new Action("${name}.play")
	}
}
