package Netrunner

import com.gvaneyck.ggengine.Action

abstract class IceCard extends Card {

    public play(Card card) {
        gs.corp.hand.remove(card)
        
        def servers = gs.corp.servers.size()
        
        def actions = []
        for (int i = 0; i <= servers; i++) {
            actions << new Action("${name}.preInstall", [card, i])
        }
        gm.presentActions(actions)
    }
    
    public preInstall(Card card, int serverIdx) {
        if (gs.corp.servers.size() == serverIdx) {
            gs.corp.servers << []
        }
        
        def server = gs.corp.servers[serverIdx]
        if (server.isEmpty()) {
            install(card, serverIdx)
            return
        }
        
        def actions = []
        for (int i = 0; i < server.size(); i++) {
            actions << new Action("${name}.trashPreInstall", [card, serverIdx, server[i]])
        }
        
        if (gs.corp.credits >= server.size()) {
            actions << new Action("${name}.install", [card, serverIdx])
        }
        
        gm.presentActions(actions) 
    }
    
    public trashPreInstall(Card card, int serverIdx, InstalledIce toTrash) {
        gs.corp.servers[serverIdx].remove(toTrash)
        gs.corp.discard << toTrash.card
        preInstall(card, serverIdx)
    }
    
    public install(Card card, int serverIdx) {
        def server = gs.corp.servers[serverIdx]
        gs.corp.credits -= server.size()
        server << new InstalledIce(card)
    }
    
    public canPlay() {
        return true
    }
}
