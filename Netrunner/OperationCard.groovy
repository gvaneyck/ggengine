package Netrunner

abstract class OperationCard extends Card {

    abstract playCard()

    public play(Card card) {
        gs.corp.hand.remove(card)
        playCard()
        gs.corp.discard << card
    }
}
