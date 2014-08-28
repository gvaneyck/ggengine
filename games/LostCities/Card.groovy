package LostCities

class Card {

    static gm
    static gs

    def value
    def color

    def Card(value, color) {
        this.value = value
        this.color = color
    }

    def play() {
        def player = gs[gs.currentPlayer]
        player.table[color] << this
        player.hand.remove(this)
    }

    def discard() {
        gs.discard[color] << this
        gs.lastDiscard = color
    }

    public String toString() {
    	return "${color} ${(value == 0 ? '!' : value)}"
	}
}
