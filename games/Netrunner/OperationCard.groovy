package Netrunner

abstract class OperationCard extends Card {

    abstract playCard()

    public play() {
        gs.corp.hand.remove(this)
        playCard()
        gs.corp.discard << this
    }
}
