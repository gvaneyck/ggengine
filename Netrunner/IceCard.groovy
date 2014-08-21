package Netrunner

import com.gvaneyck.ggengine.Action

abstract class IceCard extends Card {

    public play(Card card) {
        gs.corp.hand.remove(card)
        
        def servers = gs.corp.servers.size()
        
        def actions = []
        for (int i = 0; i <= servers; i++) {
            actions << new Action("IceCard.preInstall", [card, i])
        }
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
            actions << new Action("IceCard.trashPreInstall", [card, serverIdx, server[i]])
        }
        
        if (gs.corp.credits >= server.size()) {
            actions << new Action("IceCard.install", [card, serverIdx])
        } 
    }
    
    public trashPreInstall(Card card, int serverIdx, InstalledIce toTrash) {
        gs.corp.servers[server].remove(toTrash)
        preInstall(card, serverIdx)
    }
    
    public install(Card card, int serverIdx) {
        def server = gs.corp.servers[serverIdx]
        gs.corp.credts -= server.size()
        server << new InstalledIce(card)
    }
    
    public canPlay() {
        return true
    }
}
