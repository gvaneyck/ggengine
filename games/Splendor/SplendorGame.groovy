package Splendor

import com.gvaneyck.ggengine.Action
import com.gvaneyck.ggengine.Game

class SplendorGame extends Game {
    static gm
    static gs

    public void init() {

        // Load developments
        def firstLine = true
        gs.decks = [[], [], []]
        new File('games/Splendor/splendor.csv').eachLine { line ->
            if (firstLine) {
                firstLine = false
                return
            }

            def parts = line.split(',')
            def card = new Card(
                    id: parts[0].toInteger(),
                    tier: parts[1].toInteger(),
                    points: parts[2].toInteger(),
                    gem: parts[3],
                    reqs: [
                            red: parts[4].toInteger(),
                            green: parts[5].toInteger(),
                            blue: parts[6].toInteger(),
                            white: parts[7].toInteger(),
                            black: parts[8].toInteger()
                    ]
            )

            gs.decks[card.tier - 1] << card
        }

        // Load nobles
        firstLine = true
        def allNobles = []
        new File('games/Splendor/nobles.csv').eachLine { line ->
            if (firstLine) {
                firstLine = false
                return
            }

            def parts = line.split(',')
            def noble = new Noble(
                    id: parts[0].toInteger(),
                    points: parts[1].toInteger(),
                    reqs: [
                            red: parts[2].toInteger(),
                            green: parts[3].toInteger(),
                            blue: parts[4].toInteger(),
                            white: parts[5].toInteger(),
                            black: parts[6].toInteger()
                    ]
            )

            allNobles << noble
        }

        // Shuffling
        Collections.shuffle(gs.decks[0], gm.rand)
        Collections.shuffle(gs.decks[1], gm.rand)
        Collections.shuffle(gs.decks[2], gm.rand)
        Collections.shuffle(allNobles, gm.rand)

        // Setup bank
        def maxGems = (gs.users == 4 ? 7 : gs.users == 3 ? 5 : 4)
        gs.bank = [
            red: maxGems,
            green: maxGems,
            blue: maxGems,
            white: maxGems,
            black: maxGems,
            gold: 5
        ]

        // Setup market
        gs.markets = [[], [], []]
        (1..4).each { it ->
            gs.markets[0] << gs.decks[0].remove(0)
            gs.markets[1] << gs.decks[1].remove(0)
            gs.markets[2] << gs.decks[2].remove(0)
        }

        // Select nobles
        gs.nobles = []
        (1..(gs.users + 1)).each {
            gs.nobles << allNobles.remove(0)
        }

        // Setup players
        (1..gs.users).each {
            gs[it] = [:]
            gs[it].bank = [
                    red: 0,
                    green: 0,
                    blue: 0,
                    white: 0,
                    black: 0,
                    gold: 0
            ]
            gs[it].prod = [
                    red: 0,
                    green: 0,
                    blue: 0,
                    white: 0,
                    black: 0
            ]
            gs[it].points = 0
            gs[it].devs = []
            gs[it].stash = []
        }

        gs.currentPlayer = 1
    }

    public void turn() {
        int cur = gs.currentPlayer
        def curp = gs.get(cur)

        // Buy & Stash
        gs.markets.eachWithIndex { market, i ->
            market.eachWithIndex { card, j ->
                if (card != null) {
                    if (canBuy(curp, card)) {
                        gm.addAction(new Action(cur, this, 'buyCard', [i, j]))
                    }

                    if (curp.stash.size() < 3) {
                        gm.addAction(new Action(cur, this, 'stashCard', [i, j]))
                    }
                }
            }
        }

        // Stash random card
        if (curp.stash.size() < 3) {
            if (gs.decks[0].size() > 0) { gm.addAction(new Action(cur, this, 'stashRandomCard', [0])) }
            if (gs.decks[1].size() > 0) { gm.addAction(new Action(cur, this, 'stashRandomCard', [1])) }
            if (gs.decks[2].size() > 0) { gm.addAction(new Action(cur, this, 'stashRandomCard', [2])) }
        }

        // Take a gem
        gs.bank.each { color, amt ->
            if (color != 'gold' && amt > 0) {
                gm.addAction(new Action(cur, this, 'takeGem', [color, 1]))
            }
        }

        // Buy from stash
        curp.stash.eachWithIndex { card, idx ->
            if (canBuy(curp, card)) {
                gm.addAction(new Action(cur, this, 'buyStashCard', [idx]))
            }
        }

        gm.resolveActions()

        handleGemCap()

        gs.currentPlayer++
        if (gs.currentPlayer > gs.users) {
            gs.currentPlayer = 1
        }
    }

    def canBuy(player, card) {
        def reqWild = 0
        ['red', 'green', 'blue', 'white', 'black'].each { color ->
            if (player.bank[color] + player.prod[color] < card.reqs[color]) {
                reqWild += card.reqs[color] - (player.bank[color] + player.prod[color])
            }
        }
        return (reqWild <= player.bank.gold)
    }

    def buyCard(tier, idx) {
        int cur = gs.currentPlayer
        def curp = gs.get(cur)

        def card = gs.markets[tier][idx]

        ['red', 'green', 'blue', 'white', 'black'].each { color ->
            int cost = Math.max(0, card.reqs[color] - curp.prod[color])
            curp.bank[color] -= cost
            gs.bank[color] += cost

            if (curp.bank[color] < 0) {
                def goldSpent = -curp.bank[color]
                curp.bank.gold -= goldSpent
                gs.bank[color] -= goldSpent
                gs.bank.gold += goldSpent

                curp.bank[color] = 0
            }
        }

        curp.devs << card
        curp.prod[card.gem]++
        curp.points += card.points

        gs.markets[tier][idx] = null
        if (gs.decks[tier].size() > 0) {
            gs.markets[tier][idx] = gs.decks[tier].remove(0)
        }

        checkNobles(curp)
    }

    def buyStashCard(idx) {
        int cur = gs.currentPlayer
        def curp = gs.get(cur)

        def card = curp.stash[idx]

        ['red', 'green', 'blue', 'white', 'black'].each { color ->
            def cost = (card.reqs[color] - curp.prod[color])
            curp.bank[color] -= cost
            gs.bank[color] += cost

            if (curp.bank[color] < 0) {
                def goldSpent = -curp.bank[color]
                curp.bank.gold -= goldSpent
                gs.bank[color] -= goldSpent
                gs.bank.gold += goldSpent

                curp.bank[color] = 0
            }
        }

        curp.devs << card
        curp.prod[card.gem]++
        curp.points += card.points

        curp.stash.remove(idx)

        checkNobles(curp)
    }

    def checkNobles(curp) {
        gs.nobles.eachWithIndex { noble, idx ->
            if (noble != null) {
                def valid = true
                curp.prod.each { color, amt ->
                    if (amt < noble.reqs[color]) {
                        valid = false
                    }
                }

                if (valid) {
                    curp.points += noble.points
                    gs.nobles[idx] = null
                }
            }
        }
    }

    def stashCard(tier, idx) {
        int cur = gs.currentPlayer
        def curp = gs.get(cur)

        curp.stash << gs.markets[tier][idx]

        if (gs.bank.gold > 0) {
            gs.bank.gold--
            curp.bank.gold++
        }

        gs.markets[tier][idx] = null
        if (gs.decks[tier].size() > 0) {
            gs.markets[tier][idx] = gs.decks[tier].remove(0)
        }
    }

    def stashRandomCard(tier) {
        int cur = gs.currentPlayer
        def curp = gs.get(cur)

        curp.stash << gs.decks[tier].remove(0)

        if (gs.bank.gold > 0) {
            gs.bank.gold--
            curp.bank.gold++
        }
    }

    def takeGem(pickedColor, number) {
        int cur = gs.currentPlayer
        def curp = gs.get(cur)

        gs.bank[pickedColor]--
        curp.bank[pickedColor]++

        if (number == 1) {
            gs.lastColor1 = pickedColor

            gs.bank.each { color, amt ->
                if (color == gs.lastColor1) {
                    if (amt >= 3) {
                        gm.addAction(new Action(cur, this, 'takeGem', [color, 2]))
                    }
                }
                else if (color != 'gold' && amt > 0) {
                    gm.addAction(new Action(cur, this, 'takeGem', [color, 2]))
                }
            }
            gm.resolveActions()
        }
        else if (number == 2) {
            gs.lastColor2 = pickedColor

            if (gs.lastColor1 != gs.lastColor2) {
                gs.bank.each { color, amt ->
                    if (color != gs.lastColor1 && color != gs.lastColor2 && color != 'gold' && amt > 0) {
                        gm.addAction(new Action(cur, this, 'takeGem', [color, 3]))
                    }
                }
                gm.resolveActions()
            }
        }
    }

    def handleGemCap() {
        int cur = gs.currentPlayer
        def curp = gs.get(cur)

        def totalGems = 0
        curp.bank.each { color, amt -> totalGems += amt }

        if (totalGems > 10) {
            curp.bank.each { color, amt ->
                if (amt > 0) {
                    gm.addAction(new Action(cur, this, 'discardGem', [color]))
                }
            }
            gm.resolveActions()
        }
    }

    def discardGem(color) {
        int cur = gs.currentPlayer
        def curp = gs.get(cur)

        gs.bank[color]++
        curp.bank[color]--

        handleGemCap()
    }

    public boolean isFinished() {
        return (gs.currentPlayer == 1 && gs.find { key, value -> value instanceof Map && value.points != null && value.points >= 15 })
    }

    public Map end() {
        def winner = 0
        def points = 0
        def cards = 0
        (1..gs.users).each {
            def player = gs[it]
            def pCards = 0
            player.prod.each { color, amt -> pCards += amt }
            if (player.points > points || (player.points == points && pCards < cards)) {
                winner = it
                points = player.points
                cards = pCards
            }
        }

        return [ winner: winner, points: points ]
    }
}
