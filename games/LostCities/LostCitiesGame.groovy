package LostCities

import com.gvaneyck.ggengine.Action
import com.gvaneyck.ggengine.Game

class LostCitiesGame extends Game {
    static gm
    static gs

    public void init() {
        def colors = [ 'red', 'white', 'blue', 'green', 'yellow' ]

        gs.deck = []
        colors.each { color ->
            (1..3).each {
                gs.deck << new Card(0, color)
            }
            (2..10).each { value ->
                gs.deck << new Card(value, color)
            }
        }
        Collections.shuffle(gs.deck, gm.rand)

        gs.discard = [:]
        colors.each { color ->
            gs.discard[color] = []
        }

        (1..2).each { it ->
            gs[it] = [:]
            gs[it].hand = []
            (1..8).each { i ->
                gs[it].hand << gs.deck.remove(0)
            }
            gs[it].hand.sort()

            gs[it].table = [:]
            colors.each { color ->
                gs[it].table[color] = []
            }
        }

        gs.currentPlayer = 1
    }

    public void turn() {
        def cur = gs.currentPlayer
        def curp = gs[cur]

        curp.hand.sort { a, b ->
            def result = a.color.compareTo(b.color)
            if (result == 0) {
                result = a.value.compareTo(b.value)
            }
            return result
        }

        def actions = []
        curp.hand.eachWithIndex { card, i ->
            def pile = curp.table[card.color]
            if (pile.isEmpty() || pile.last().value <= card.value) {
                actions << new Action(cur, this, 'playCard', [i])
            }
        }
        curp.hand.eachWithIndex { card, i ->
            actions << new Action(cur, this, 'discardCard', [i])
        }

        gm.presentActions(actions)

        actions = []
        actions << new Action(cur, this, 'drawDeck')
        gs.discard.each { color, pile ->
            if (!pile.isEmpty() && color != gs.lastDiscard) {
                actions << new Action(cur, this, 'drawPile', color)
            }
        }

        gm.presentActions(actions)

        gs.currentPlayer = (cur == 1 ? 2 : 1)
    }

    def playCard(idx) {
        def player = gs[gs.currentPlayer]
        def card = player.hand.remove(idx)
        player.table[card.color] << card
        gs.lastDiscard = null
    }

    def discardCard(idx) {
        def card = gs[gs.currentPlayer].hand.remove(idx)
        gs.discard[card.color] << card
        gs.lastDiscard = card.color
    }

    def drawDeck() {
        gs[gs.currentPlayer].hand << gs.deck.remove(0)
        gs[gs.currentPlayer].hand.sort()
    }

    def drawPile(color) {
        def pile = gs.discard[color]
        gs[gs.currentPlayer].hand << pile.pop()
        gs[gs.currentPlayer].hand.sort()
    }

    public boolean isFinished() {
        return (gs.deck.isEmpty())
    }

    public void end() {
        def scores = []
        (1..2).each {
            def total = 0
            def player = gs[it]
            player.table.each { color, pile ->
                def multiplier = 1
                def pileValue = -20

                if (pile.size() >= 8) {
                    total += 20
                }
                pile.each { card ->
                    pileValue += card.value
                    if (card.value == 0) {
                        multiplier++
                    }
                }

                total += pileValue * multiplier
            }

            scores << total
        }

        if (scores[0] > scores[1]) {
            println "Winner is player 1"
        }
        else if (scores[0] < scores[1]) {
            println "Winner is player 2"
        }
        else {
            println "It's a tie!"
        }
    }
}
