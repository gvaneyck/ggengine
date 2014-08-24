package Netrunner

abstract class EventCard extends Card {

    abstract playCard()

    public play() {
        gs.runner.hand.remove(this)
        playCard()
        gs.runner.discard << this
    }
}
