package Netrunner

abstract class Card {

    static gm
    static gs

    def name

    def Card() {
    	this.name = this.class.simpleName
    }

    abstract play()

    abstract canPlay()

    public String toString() {
    	return "${name}"
	}
}
