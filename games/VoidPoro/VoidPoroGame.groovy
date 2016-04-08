package VoidPoro

import com.gvaneyck.ggengine.Action
import com.gvaneyck.ggengine.Game
import com.gvaneyck.ggengine.GameManager

class VoidPoroGame extends Game {
    static GameManager gm
    static gs

    def champions = [ 'Ahri', 'Master Yi', 'Caitlyn', 'Rammus' ]

    public void init() {
        gs.player = [:]
        (1..gs.players).each { i ->
            gs.player[i] = [:]
            champions.each { champ ->
                gm.addAction(new Action(i, this, 'initPlayer', [ i, champ ]))
            }
        }
        gm.resolveActions()

//        Summoner spells disabled
//        +Damage to Jungle monsters
//        -Damage to Jungle monsters
//        +1 Movement (Janna’s Tailwind)
//        Store Sale! More store items AND a discount!
//        Lucky find! (+1 Void Crystal)
//        Discard an active card in-hand (Voidspawn eats your item)
//        Re-shuffle hand and draw new cards
//        Double gold from encounters
//        Double encounters
        
        def phaseOne = [
                [ name: '', message: 'Summoner spells disabled', mod: 'NO_SUMMONER'],
                [ name: '', message: '+Damage to Jungle monsters', mod: 'JUNGLE+'],
                [ name: '', message: '-Damage to Jungle monsters', mod: 'JUNGLE-'],
                [ name: 'Janna’s Tailwind', message: '+1 Movement', mod: 'MOVE+'],
                [ name: 'Store Sale!', message: 'More store items AND a discount!', mod: 'SALE'],
                [ name: 'Lucky find!', message: '+1 Void Crystal', mod: 'CRYSTAL+'],
                [ name: 'Voidspawn eats your item', message: 'Discard an active card in-hand', mod: 'ITEM-'],
                [ name: '', message: 'Re-shuffle hand and draw new cards', mod: 'REDRAW'],
                [ name: '', message: 'Double gold from encounters', mod: 'GOLD+'],
                [ name: '', message: 'Double encounters', mod: 'ENCOUNTER+'],
        ]
        Collections.shuffle(phaseOne)
        
        gs.events = []
        (0..4).each { gs.events << phaseOne[it] }
        
        gs.turn = 0
    }

    public void initPlayer(int i, String champion) {
        gs.player[i].champion = champion
    }

    public void turn() {
        def event = gs.events[gs.turn]
        gm.sendMessage("${event.name}: ${event.message}")

        // Draw event card
        // - level up
        // - refresh store
        // Move
        // Buy
        // Encounters
        // Battles
        // Clear store
        
        gs.turn++
    }

    public boolean isFinished() {
        return gs.turn == 5
    }

    public Map end() {
        return gs
    }
}
