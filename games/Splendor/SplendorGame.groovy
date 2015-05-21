package Splendor

import com.gvaneyck.ggengine.Action
import com.gvaneyck.ggengine.Game

class SplendorGame extends Game {
    static gm
    static gs

    public void init() {
        gs.decks = [[], [], []]

        def firstLine = true
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

        Collections.shuffle(gs.decks[0], gm.rand)
        Collections.shuffle(gs.decks[1], gm.rand)
        Collections.shuffle(gs.decks[2], gm.rand)

        def maxGems = (gs.players == 4 ? 7 : gs.players == 3 ? 5 : 4)
        gs.bank = [
            red: maxGems,
            green: maxGems,
            blue: maxGems,
            white: maxGems,
            black: maxGems,
            gold: 5
        ]

        gs.markets = [[], [], []]
        (1..4).each { it ->
            gs.markets[0] << gs.decks[0].remove(0)
            gs.markets[1] << gs.decks[1].remove(0)
            gs.markets[2] << gs.decks[2].remove(0)
        }

        (1..gs.players).each {
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

        // TODO: Nobles

        gs.currentPlayer = 1
    }

    public void turn() {
        int cur = gs.currentPlayer
        def curp = gs.get(cur)

        def actions = []
        gs.markets.eachWithIndex { market, i ->
            market.eachWithIndex { card, j ->
                def reqWild = 0
                ['red', 'green', 'blue', 'white', 'black'].each { color ->
                    if (curp.bank[color] + curp.prod[color] < card.reqs[color]) {
                        reqWild += card.reqs[color] - (curp.bank[color] + curp.prod[color])
                    }
                }

                if (reqWild <= curp.bank.gold) {
                    actions << new Action(cur, this, 'buyCard', [i, j])
                }

                if (curp.stash.size() < 3) {
                    actions << new Action(cur, this, 'stashCard', [i, j])
                }
            }
        }

        if (curp.stash.size() < 3) {
            if (gs.decks[0].size() > 0) { actions << new Action(cur, this, 'stashRandomCard', [0]) }
            if (gs.decks[1].size() > 0) { actions << new Action(cur, this, 'stashRandomCard', [1]) }
            if (gs.decks[2].size() > 0) { actions << new Action(cur, this, 'stashRandomCard', [2]) }
        }

        gs.bank.each { color, amt ->
            if (color != 'gold' && amt > 0) {
                actions << new Action(cur, this, 'takeGem', [color, 1])
            }
        }

        gm.presentActions(actions)

        handleGemCap()

        gs.currentPlayer++
        if (gs.currentPlayer > gs.players) {
            gs.currentPlayer = 1
        }
    }

    def buyCard(tier, idx) {
        int cur = gs.currentPlayer
        def curp = gs.get(cur)

        def card = gs.markets[tier][idx]

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
        curp.poitns += card.points

        gs.markets[tier][idx] = null
        if (gs.decks[tier].size() > 0) {
            gs.decks[tier].remove(0)
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
            gs.decks[tier].remove(0)
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

            def actions = []
            gs.bank.each { color, amt ->
                if (color == gs.lastColor1) {
                    if (amt >= 3) {
                        actions << new Action(cur, this, 'takeGem', [color, 2])
                    }
                }
                else if (color != 'gold' && amt > 0) {
                    actions << new Action(cur, this, 'takeGem', [color, 2])
                }
            }
            gm.presentActions(actions)
        }
        else if (number == 2) {
            gs.lastColor2 = pickedColor

            def actions = []
            if (gs.lastColor1 != gs.lastColor2) {
                gs.bank.each { color, amt ->
                    if (color != gs.lastColor1 && color != gs.lastColor2 && color != 'gold' && amt > 0) {
                        actions << new Action(cur, this, 'takeGem', [color, 3])
                    }
                }
            }
            gm.presentActions(actions)
        }
    }

    def handleGemCap() {
        int cur = gs.currentPlayer
        def curp = gs.get(cur)

        def totalGems = 0
        curp.bank.each { color, amt -> totalGems += amt }

        if (totalGems > 10) {
            def actions = []
            curp.bank.each { color, amt ->
                if (amt > 0) {
                    actions << new Action(cur, this, 'discardGem', [color])
                }
            }
            gm.presentActions(actions)
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

    public void end() {
        println "It's a tie!"
    }
}
