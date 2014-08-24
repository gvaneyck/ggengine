package Netrunner

abstract class HardwareCard extends Card {

    abstract playCard()

    public play() {
        gs.runner.hand.remove(this)
        playCard()
        gs.runner.hardware << this
    }
}
