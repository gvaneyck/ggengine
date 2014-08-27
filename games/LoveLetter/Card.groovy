package LoveLetter

import com.gvaneyck.ggengine.Action

abstract class Card {

    static gm
    static gs

    def value
    def name

    def Card() {
    	this.name = this.class.simpleName
    }

    def play() {
        def player = gs[gs.currentPlayer]

        def opt1 = player.hand
        def opt2 = player.hand2

        player.hand = (this == opt1 ? opt2 : opt1)
        player.remove('hand2')
        player.table << this
        player.immune = false

        playCard()
    }

    abstract playCard()

    public String toString() {
    	return "${name} (${value})"
	}
}
