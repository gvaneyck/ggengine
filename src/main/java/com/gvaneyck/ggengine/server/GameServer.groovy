package com.gvaneyck.ggengine.server

import com.gvaneyck.ggengine.game.GameInstanceFactory
import com.gvaneyck.ggengine.game.GameManager
import com.gvaneyck.ggengine.game.actions.ActionOption
import com.gvaneyck.ggengine.server.rooms.GameRoom
import com.gvaneyck.ggengine.ui.ConsoleUI
import com.gvaneyck.ggengine.ui.GGui

public class GameServer implements Runnable, GGui {

    def name
    def game
    def gameInstance
    final Thread gameThread

    def playerNameToId = [:]
    def idToPlayer = [:]

    def actionOptions
    def choice

    def done = false

    public GameServer(GameRoom room) {
        this.game = room.game
        this.name = room.name
        int i = 1
        idToPlayer = room.users.collectEntries { [ i++, it ] }
        playerNameToId = idToPlayer.collectEntries { [ it.value.name, it.key ] }
        gameThread = new Thread(this)
        gameThread.start()
    }

    public doReconnect(User player) {
        if (!playerNameToId.containsKey(player.name)) {
            return
        }

        def playerId = playerNameToId[player.name]
        idToPlayer[playerId] = player
        showChoicesToPlayer(playerId, player)
    }

    public setChoice(User player, action, args) {
        def playerId = playerNameToId[player.name]
        if (playerId == null) {
            return
        }

        synchronized (gameThread) {
            if (choice != null) {
                return
            }

            actionOptions.each {
                if (it.matches(playerId, action, args)) {
                    choice = it
                }
            }

            if (choice != null) {
                gameThread.notify()
            }
        }
    }

    @Override
    public void sendMessage(int player, String message) {
        idToPlayer[player].send([cmd: 'message', message: message])
    }

    @Override
    public ActionOption resolveChoice(List<ActionOption> actions) {
        synchronized (gameThread) {
            actionOptions = actions
            idToPlayer.each { id, player ->
                showChoicesToPlayer(id, player)
            }

            while (choice == null) {
                gameThread.wait()
            }

            def choicePicked = choice
            choice = null

            return choicePicked
        }
    }

    private showChoicesToPlayer(id, player) {
        player.send([cmd: 'gs', gs: gameInstance.getGameState(id)])

        def playerActions = actionOptions.findAll { it.getPlayerId() == id }
        if (playerActions) {
            player.send([cmd: 'actions', actions: playerActions])
        }
    }

    @Override
    public void resolveEnd(Map data) {
        def cmd = [
                cmd: 'end',
                message: 'Winner is P' + data.winner + ' with ' + data.points + ' points!'
        ]
        idToPlayer.each { id, player ->
            player.send(cmd)
        }
        done = true
    }

    @Override
    void run() {
        gameInstance = GameInstanceFactory.getGameInstance('games', game, this, [players: playerNameToId.size()])
        gameInstance.startGame()
    }
}
