var websocket;


var state = {
    gameName: null,
    playerName: null,
    activeRoom: null,
    rooms: { lobby: {}, game: {}, direct: {} },
    game: null,
    loadGameState: function() {},
    handleActions: function() {}
};

var uiManager;

var ui = {
    generalLabel: null,
    nameLabel: null,
    nameBox: null,
    serverLabel: null,
    serverBox: null,
    loginButton: null,

    createButton: null,
    lobbyTable: null,

    gameNameLabel: null,
    gameNameBox: null,
    gameNameButton: null,
    gamePlayerLabel: null,
    gamePlayerList: null,
    startButton: null,
    exitButton: null,

    chatLabel: null,
    chatBox: null,
    messagesScrollArea: null
};

function setupLobby(canvasElement, gameName, loadGameState, handleActions, renderTest) {
    uiManager = new UIManager(canvasElement);
    state.gameName = gameName;

    if (renderTest == true) {
        loadGameState(); // Render test
    }
    else {
        initUi();
        showLoginUI();
        state.loadGameState = loadGameState;
        uiManager.onresize = loadGameState;
        state.handleActions = handleActions;
        uiManager.dirty = true;
    }
}

function initUi() {
    ui.generalLabel = new Label(10, 53, '');

    ui.nameLabel = new Label(10, 13, 'Enter nickname: ');
    ui.nameBox = new Textbox(ui.nameLabel.width + 10, 10, 200, 20);
    ui.nameBox.submitHandler = function(msg) {
        login(msg, ui.serverBox.text);
    };

    ui.serverLabel = new Label(10, 38, 'Server: ');
    ui.serverBox = new Textbox(ui.nameLabel.width + 10, 35, 200, 20);
    ui.serverBox.text = 'gvane1wl1:9998';
    ui.serverLabel.x = ui.nameLabel.width + 10 - ui.serverLabel.width;

    ui.loginButton = new Button(ui.nameBox.x + ui.nameBox.width + 10, 10, 'Login');
    ui.loginButton.handleMouseClick = function(x, y) {
        login(ui.nameBox.text, ui.serverBox.text);
        return false;
    };

    ui.createButton = new Button(10, 10, 'Create Game');
    ui.createButton.handleMouseClick = function(x, y) {
        startCreateGame();
        return true;
    };

    ui.lobbyTable = new Table(10, 38, 500);
    ui.lobbyTable.emptyText = 'No games found';
    ui.lobbyTable.onCellClick = function(row, col) {
        if (col == 0 && row > 0) {
            sendCmd({cmd: 'joinLobby', name: ui.lobbyTable.elements[row][2].text })
        }
    };

    ui.gameNameLabel = new Label(10, 38, 'Game Name: ');
    ui.gameNameBox = new Textbox(ui.gameNameLabel.width + 10, 35, 200, 20);
    ui.gameNameBox.submitHandler = function(msg) {
        createGameLobby(msg);
    };
    ui.gameNameButton = new Button(ui.gameNameBox.x + ui.gameNameBox.width + 10, 35, 'Create');
    ui.gameNameButton.handleMouseClick = function(x, y) {
        createGameLobby(ui.gameNameBox.text);
        return true;
    };

    ui.gamePlayerLabel = new Label(10, 38, '');
    ui.gamePlayerList = new Label(10, 58, '');

    ui.exitButton = new Button(10, 10, 'Exit Game');
    ui.exitButton.handleMouseClick = function(x, y) {
        leaveGame();
        return false;
    };

    ui.startButton = new Button(ui.exitButton.width + 20, 10, 'Start Game');
    ui.startButton.handleMouseClick = function(x, y) {
        startGame();
        return false;
    };

    ui.chatLabel = new Label(550, 318, 'Chat: ');
    ui.chatBox = new Textbox(ui.chatLabel.width + 550, 315, 400 - ui.chatLabel.width, 20);
    ui.chatBox.submitHandler = function(msg) {
        sendCmd({cmd: 'msg', msg: msg, type: state.activeRoom.type, target: state.activeRoom.name});
    };

    var messagesLabel = new FixedWidthLabel(550, 10, 384, '');
    ui.messagesScrollArea = new ScrollArea(550, 10, 400, 300, messagesLabel);

    uiManager.addElements(ui);
}

function showLoginUI() {
    ui.generalLabel.x = 10;
    ui.generalLabel.y = 60;
    ui.generalLabel.visible = true;
    ui.nameLabel.visible = true;
    ui.nameBox.visible = true;
    ui.serverLabel.visible = true;
    ui.serverBox.visible = true;
    ui.loginButton.visible = true;

    ui.createButton.visible = false;
    ui.lobbyTable.visible = false;
    ui.gameNameLabel.visible = false;
    ui.gameNameBox.visible = false;
    ui.gameNameButton.visible = false;
    ui.gamePlayerLabel.visible = false;
    ui.gamePlayerList.visible = false;
    ui.exitButton.visible = false;
    ui.startButton.visible = false;
    ui.chatLabel.visible = false;
    ui.chatBox.visible = false;
    ui.messagesScrollArea.visible = false;

    uiManager.dirty = true;
}

function login(nickname, host) {
    ui.generalLabel.setText("Connecting...");
    ui.generalLabel.visible = true;
    uiManager.dirty = true;

    websocket = new ReconnectingWebSocket(host);
    websocket.connect();
    websocket.onclose = onClose;
    websocket.onmessage = onMessage;
    state.playerName = nickname;
    sendCmd({cmd: 'login', name: nickname});
}

function startCreateGame() {
    ui.createButton.visible = false;
    ui.lobbyTable.visible = false;

    ui.gameNameLabel.visible = true;
    ui.gameNameBox.visible = true;
    ui.gameNameButton.visible = true;
    ui.exitButton.visible = true;
}

function createGameLobby(lobbyName) {
    sendCmd({ cmd: 'makeLobby', name: lobbyName, game: state.gameName, maxSize: 4 });
}

function leaveGame() {
    ui.generalLabel.visible = false;
    ui.gameNameLabel.visible = false;
    ui.gameNameBox.visible = false;
    ui.gameNameButton.visible = false;
    ui.gamePlayerLabel.visible = false;
    ui.gamePlayerList.visible = false;
    ui.exitButton.visible = false;
    ui.startButton.visible = false;

    ui.createButton.visible = true;
    ui.lobbyTable.visible = true;

    uiManager.dirty = true;

    sendCmd({ cmd: 'leaveLobby', name: state.activeRoom });
}

function startGame() {
    sendCmd({ cmd: 'startGame', name: state.activeRoom });
}

/// Web sockets ///

function onClose(evt) {
    for (var key in ui) {
        ui[key].visible = false;
    }

    ui.generalLabel.setText('You were disconnected from the server.  Reconnecting...');
    ui.generalLabel.x = 10;
    ui.generalLabel.y = 10;
    ui.generalLabel.visible = true;
    uiManager.dirty = true;

    sendCmd({cmd: 'login', name: state.playerName});
}

function onMessage(evt) {
    var cmd = JSON.parse(evt.data);
    if (cmd.cmd == 'login') {
        if (cmd.success) {
            state.playerName = cmd.name;

            ui.generalLabel.visible = false;
            ui.nameBox.visible = false;
            ui.nameLabel.visible = false;
            ui.serverBox.visible = false;
            ui.serverLabel.visible = false;
            ui.loginButton.visible = false;

            ui.createButton.visible = true;
            ui.lobbyTable.visible = true;
            ui.chatBox.visible = true;
            ui.chatLabel.visible = true;
            ui.messagesScrollArea.visible = true;
        } else {
            ui.generalLabel.setText('Invalid name');
        }
    } else if (cmd.cmd == 'roomJoin') {
        var room = state.rooms[cmd.type][cmd.name];
        if (room == undefined) {
            room = { type: cmd.type, name: cmd.name };
            state.rooms[cmd.type][cmd.name] = room;
        }

        if (cmd.members != undefined) {
            room.members = cmd.members;
        }
        if (cmd.member != undefined) {
            room.members = room.members.concat(cmd.members)
        }
        if (cmd.messages != undefined) {
            room.messages = cmd.messages;
            updateChat(room.messages);
        }

        state.activeRoom = room;
        if (cmd.type == 'game') {
            showGameLobbyUI();
        }
//    } else if (cmd.cmd == 'roomCreate') {
//        state.lobbies[cmd.name] = {name: cmd.name, members: []};
//    } else if (cmd.cmd == 'roomDestroy') {
//        delete state.lobbies[cmd.name];
//    } else if (cmd.cmd == 'roomJoin') {
//        var lobby = state.lobbies[cmd.name];
//        lobby.members = lobby.members.concat(cmd.members);
//
//        if (cmd.name != 'General') {
//            state.lobbyName = cmd.name;
//            state.lobby = lobby;
//            showGameLobbyUI();
//        }
//    } else if (cmd.cmd == 'roomLeave') {
//        var lobby = state.lobbies[cmd.name];
//        if (cmd.member == name) {
//            lobby.members = [];
//        } else {
//            lobby.members.splice(lobby.members.indexOf(cmd.member), 1);
//        }
//        ui.gamePlayerList.setText(lobby.members.join('\n'));
    } else if (cmd.cmd == 'chat') {
        var room = state.rooms[cmd.type][cmd.room];
        room.messages = room.messages.concat({ time: cmd.time, message: cmd.message, from: cmd.from });
        if (room == state.activeRoom) {
            updateChat(room.messages);
        }
//    } else if (cmd.cmd == 'message') {
//        if (text.length != 0) {
//            text += '\n';
//        }
//        text += cmd.message;
//        ui.messagesScrollArea.element.setText(text);
//    } else if (cmd.cmd == 'gs') {
//        state.gameState = cmd.gs;
//        state.loadGameState();
//    } else if (cmd.cmd == 'actions') {
//        state.actions = cmd.actions;
//        state.handleActions();
//    } else if (cmd.cmd == 'end') {
//        handleEndGame(cmd);
    }

    if (cmd.cmd == 'roomCreate' || cmd.cmd == 'roomDestroy') {
        var games = [];
        for (var name in state.lobbies) {
            if (name != 'General') {
                games.push([new Label('Click to Join'), new Label(state.gameName), new Label(name)])
            }
        }

        if (games.length > 0) {
            games.splice(0, 0, [new Label('Join'), new Label('Game'), new Label('Name')])
        }

        ui.lobbyTable.elements = games;
    }

    uiManager.dirty = true;
}

function updateChat(messages) {
    var text = '';
    for (var i in messages) {
        var message = messages[i];

        var date = new Date(message.time);
        var dateString = date.getHours()
            + ':'
            + (date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes())
            + ':'
            + (date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds());

        text += dateString + ' ';
        if (message.from != undefined) {
            text += message.from + ': ';
        }
        text += message.message + '\n';
    }
    ui.messagesScrollArea.element.setText(text);
}

function sendCmd(cmd) {
    websocket.send(JSON.stringify(cmd));
}

function handleEndGame(cmd) {
    uiManager.elements = [];

    initUi();
    for (var i in uiManager.elements) {
        uiManager.elements[i].visible = false;
    }

    ui.chatBox.visible = true;
    ui.chatLabel.visible = true;
    ui.messagesScrollArea.visible = true;
    showGameLobbyUI();

    ui.generalLabel.x = ui.startButton.x + ui.startButton.width + 20;
    ui.generalLabel.y = 13;
    ui.generalLabel.setText(cmd.message);
    ui.generalLabel.visible = true;

    uiManager.dirty = true;
}

function showGameLobbyUI() {
    ui.gameNameLabel.visible = false;
    ui.gameNameBox.visible = false;
    ui.gameNameButton.visible = false;
    ui.createButton.visible = false;
    ui.lobbyTable.visible = false;

    ui.gamePlayerLabel.visible = true;
    ui.gamePlayerList.visible = true;
    ui.startButton.visible = true;
    ui.exitButton.visible = true;

    ui.gamePlayerLabel.setText('Player List for \'' + state.activeRoom + '\'');
    ui.gamePlayerList.setText(state.lobby.members.join('\n'));
}
