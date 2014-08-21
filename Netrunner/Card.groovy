package Netrunner

import com.gvaneyck.ggengine.Action

abstract class Card {

    static gm
    static gs
    
    def name
    
    def Card() {
    	this.name = this.class.simpleName
    }
    
    abstract play(Card card)
    
    abstract canPlay()
    
    public String toString() {
    	return "${name}"
	}
	
	public Action getAction() {
	   return new Action("${name}.play", [this])
	}
}
