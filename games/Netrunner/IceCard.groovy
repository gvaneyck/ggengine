package Netrunner

import com.gvaneyck.ggengine.Action

abstract class IceCard extends Card {

    public play() {
        gs.corp.hand.remove(this)
        
        def servers = gs.corp.servers.size()
        
        def actions = []
        for (int i = 0; i <= servers; i++) {
            actions << new Action(this, "preInstall", [i])
        }
        gm.presentActions(actions)
    }
    
    public preInstall(int serverIdx) {
        if (gs.corp.servers.size() == serverIdx) {
            gs.corp.servers << []
        }
        
        def server = gs.corp.servers[serverIdx]
        if (server.isEmpty()) {
            install(serverIdx)
            return
        }
        
        def actions = []
        for (int i = 0; i < server.size(); i++) {
            actions << new Action(this, "trashPreInstall", [serverIdx, server[i]])
        }
        
        if (gs.corp.credits >= server.size()) {
            actions << new Action(this, "install", [serverIdx])
        }
        
        gm.presentActions(actions) 
    }
    
    public trashPreInstall(int serverIdx, InstalledIce toTrash) {
        gs.corp.servers[serverIdx].remove(toTrash)
        gs.corp.discard << toTrash.card
        preInstall(serverIdx)
    }
    
    public install(int serverIdx) {
        def server = gs.corp.servers[serverIdx]
        gs.corp.credits -= server.size()
        server << new InstalledIce(this)
    }
    
    public canPlay() {
        return true
    }
}