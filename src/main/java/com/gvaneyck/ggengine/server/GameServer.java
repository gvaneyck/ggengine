package com.gvaneyck.ggengine.server;

import com.gvaneyck.ggengine.game.GameInstance;
import com.gvaneyck.ggengine.game.GameInstanceFactory;
import com.gvaneyck.ggengine.game.actions.ActionOption;
import com.gvaneyck.ggengine.server.dto.server.ServerActionsDto;
import com.gvaneyck.ggengine.server.dto.server.ServerEndDto;
import com.gvaneyck.ggengine.server.dto.server.ServerGsDto;
import com.gvaneyck.ggengine.server.dto.server.ServerMessageDto;
import com.gvaneyck.ggengine.server.rooms.GameRoom;
import com.gvaneyck.ggengine.server.util.JSON;
import com.gvaneyck.ggengine.ui.GGui;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class GameServer implements Runnable, GGui {

    private String name;
    private String game;
    private GameInstance gameInstance;
    private final Thread gameThread;

    private Map<String, Integer> playerNameToId;
    private Map<Integer, User> idToPlayer;

    private List<ActionOption> actionOptions;
    private ActionOption choice;

    private boolean done;

    public GameServer(GameRoom room) {
        name = room.getName();
        game = room.getGame();

        int i = 1;
        for (User user : room.getUsers()) {
            playerNameToId.put(user.getName(), i);
            idToPlayer.put(i, user);
            i++;
        }

        done = false;

        gameThread = new Thread(this);
        gameThread.start();
    }

    public void doReconnect(User player) {
        String playerName = player.getName();
        if (!playerNameToId.containsKey(playerName)) {
            return;
        }

        int playerId = playerNameToId.get(playerName);
        idToPlayer.put(playerId, player);
        showChoicesToPlayer(playerId, player);
    }

    private void showChoicesToPlayer(int playerId, User player) {
        player.send(new ServerGsDto(gameInstance.getGameState(playerId)));

        List<ActionOption> playerOptions = new ArrayList<>();
        for (ActionOption actionOption : actionOptions) {
            if (actionOption.getPlayerId() == playerId) {
                playerOptions.add(actionOption);
            }
        }

        if (!playerOptions.isEmpty()) {
            player.send(new ServerActionsDto(playerOptions));
        }
    }

    public void setChoice(User player, String action, Object[] args) {
        Integer playerId = playerNameToId.get(player.getName());
        if (playerId == null) {
            return;
        }

        synchronized (gameThread) {
            if (choice != null) {
                return;
            }

            for (ActionOption actionOption : actionOptions) {
                if (actionOption.matches(playerId, action, args)) {
                    choice = actionOption;
                    gameThread.notify();
                    return;
                }
            }
        }
    }

    @Override
    public void sendMessage(int player, String message) {
        User user = idToPlayer.get(player);
        if (user == null) {
            System.err.print("Invalid player ID " + player + " in game " + game);
            return;
        }

        user.send(new ServerMessageDto("game", message, System.currentTimeMillis()));
    }

    @Override
    public ActionOption resolveChoice(List<ActionOption> actions) {
        synchronized (gameThread) {
            actionOptions = actions;
            for (Map.Entry<Integer, User> entry : idToPlayer.entrySet()) {
                showChoicesToPlayer(entry.getKey(), entry.getValue());
            }

            try {
                gameThread.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (choice == null) {
                System.err.println("resolveChoice was notified without a choice being specified");
            }

            ActionOption choicePicked = choice;
            choice = null;

            return choicePicked;
        }
    }

    @Override
    public void resolveEnd(Map data) {
        ServerEndDto cmd = new ServerEndDto("game", JSON.writeValueAsString(data), System.currentTimeMillis());
        for (User user : idToPlayer.values()) {
            user.send(cmd);
        }
        done = true;
    }

    @Override
    public void run() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("players", playerNameToId.size());
        gameInstance = GameInstanceFactory.getGameInstance("games", game, this, params);
        gameInstance.startGame();
    }
}
