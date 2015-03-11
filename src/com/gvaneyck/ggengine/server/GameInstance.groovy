package com.gvaneyck.ggengine.server

import com.gvaneyck.ggengine.Action
import com.gvaneyck.ggengine.GameManager
import com.gvaneyck.ggengine.gamestate.GameStateSerializer
import com.gvaneyck.ggengine.ui.GGui

public class GameInstance implements Runnable, GGui {

    static gss = new GameStateSerializer()

    def name
    def gameManager
    Thread gameThread

    def playerIds = [:]
    def players = [:]

    def actionOptions
    def choice

    public GameInstance(name) {
        this.name = name
        gameThread = new Thread(this)
        gameThread.start()
    }

    public doReconnect(player) {
        if (!playerIds.containsKey(player.name)) {
            return
        }

        def playerId = playerIds[player.name]
        players[playerId] = player
        showChoicesToPlayer(playerId, player)
    }

    public setChoice(action, args) {
        synchronized (gameThread) {
            if (choice != null) {
                return
            }

            actionOptions.each {
                if (it.matches(action, args)) {
                    choice = it
                }
            }

            if (choice != null) {
                gameThread.notify()
            }
        }
    }

    @Override
    public Action getChoice() {
        synchronized (gameThread) {
            while (choice == null) {
                gameThread.wait()
            }

            def choicePicked = choice
            choice = null

            return choicePicked
        }
    }

    @Override
    public void showChoices(List<Action> actions) {
        actionOptions = actions
        players.each { id, player ->
            showChoicesToPlayer(id, player)
        }
    }

    private showChoicesToPlayer(id, player) {
        player.send([cmd: 'gs', gs: gss.serialize(gameManager.getGameState(id))])

        def playerActions = actionOptions.findAll { it.playerId == id }
        if (playerActions) {
            player.send([cmd: 'actions', actions: playerActions.toString()])
        }
    }

    @Override
    void run() {
        gameManager = new GameManager([:], this)
        gameManager.loadGame('games', 'LostCities')
        gameManager.gameLoop()
    }
}
