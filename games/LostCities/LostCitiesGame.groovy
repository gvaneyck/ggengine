package LostCities

import LoveLetter.cards.*
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
        Collections.shuffle(gs.deck)

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

            gs[it].table = [:]
            colors.each { color ->
                gs[it].table[color] = []
            }

            gm.announce(it, "Your starting hand is ${gs[it].hand}")
        }

        gs.currentPlayer = 1
    }

    public void turn() {
        def cur = gs.currentPlayer
        def curp = gs[cur]
        gm.announce("Player ${cur}'s turn")

        curp.hand.sort { a, b ->
            def result = a.color.compareTo(b.color)
            if (result == 0) {
                result = a.value.compareTo(b.value)
            }
            return result
        }

        gm.announce(cur, "Your hand is ${curp.hand}")
        gm.announce(cur, "Your board is ${curp.table}")

        def actions = []
        curp.hand.each { card ->
            def pile = curp.table[card.color]
            if (pile.isEmpty() || pile.last().value <= card.value) {
                actions << new Action(card, 'play')
            }
        }
        curp.hand.each { card ->
            actions << new Action(card, 'discard')
        }

        gm.presentActions(actions)

        actions = []
        actions << new Action(this, 'drawDeck')
        gs.discard.each { color, pile ->
            if (!pile.isEmpty() && color != gs.lastDiscard) {
                actions << new Action(this, 'drawPile', color)
            }
        }

        gm.presentActions(actions)

        gs.currentPlayer = (cur == 1 ? 2 : 1)
    }

    def drawDeck() {
        gs[gs.currentPlayer].hand << gs.deck.remove(0)
    }

    def drawPile(color) {
        def pile = gs.discard[color]
        gs[gs.currentPlayer].hand << pile.pop()
    }

    public boolean isFinished() {
        return (gs.deck.isEmpty())
    }

    public void end() {
        def scores = []
        (1..2) {
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

        gm.announce("Scores: ${scores[0]} vs ${scores[1]}")

        if (scores[0] > scores[1]) {
            gm.announce("Winner is player 1")
        }
        else if (scores[0] < scores[1]) {
            gm.announce("Winner is player 2")
        }
        else {
            gm.announce("It's a tie!")
        }
    }
}
