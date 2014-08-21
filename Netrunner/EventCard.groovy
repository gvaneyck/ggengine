package Netrunner

abstract class EventCard extends Card {

    abstract playCard()

    public play(Card card) {
        gs.runner.hand.remove(card)
        playCard()
        gs.runner.discard << card
    }
}
