var websocket;


var state = {
    gameName: null,
    playerName: null,
    lobbies: {},
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
        state.loadGameState = loadGameState;
        uiManager.onresize = loadGameState;
        state.handleActions = handleActions;
    }
}

function initUi() {
    ui.generalLabel = new Label(10, 33, '');

    ui.nameLabel = new Label(10, 13, 'Enter nickname: ');
    ui.nameBox = new Textbox(ui.nameLabel.width + 10, 10, 200, 20);
    ui.nameBox.submitHandler = function(msg) {
        login(msg, ui.serverBox.text);
    };

    ui.serverLabel = new Label(10, 38, 'Server: ');
    ui.serverBox = new Textbox(ui.nameLabel.width + 10, 35, 200, 20);
    ui.serverBox.text = 'gvane1wd2:9003';
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

    ui.chatLabel = new Label(550, 13, 'Chat: ');
    ui.chatBox = new Textbox(ui.chatLabel.width + 550, 10, 400 - ui.chatLabel.width, 20);
    ui.chatBox.submitHandler = function(msg) {
        sendCmd({cmd: 'msg', msg: msg, target: 'General'});
    };

    var messagesLabel = new FixedWidthLabel(550, 38, 384, '');
    ui.messagesScrollArea = new ScrollArea(550, 38, 400, 300, messagesLabel);

    showLoginUI();

    uiManager.addElements(ui);
}

function showLoginUI() {
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
    websocket = new ReconnectingWebSocket(host);
    websocket.connect();
    websocket.onclose = onClose;
    websocket.onmessage = onMessage;
    sendCmd({cmd: 'setName', name: nickname});
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
    sendCmd({ cmd: "makeLobby", name: lobbyName, game: state.gameName, maxSize: 4 });
}

function leaveGame() {
    ui.gameNameLabel.visible = false;
    ui.gameNameBox.visible = false;
    ui.gameNameButton.visible = false;
    ui.gamePlayerLabel.visible = false;
    ui.gamePlayerList.visible = false;
    ui.exitButton.visible = false;
    ui.startButton.visible = false;

    ui.createButton.visible = true;
    ui.lobbyTable.visible = true;

    sendCmd({ cmd: "leaveLobby", name: state.gameName });
}

function startGame() {
    sendCmd({ cmd: "startGame", name: state.gameName });
}

/// Web sockets ///

function onClose(evt) {
    for (var key in ui) {
        ui[key].visible = false;
    }

    ui.generalLabel.setText('You were disconnected from the server.  Reconnecting...');
    ui.generalLabel.y = 10;
    ui.generalLabel.visible = true;
    uiManager.dirty = true;

    sendCmd({cmd: 'setName', name: state.playerName});
}

function onMessage(evt) {
    var cmd = JSON.parse(evt.data);
    if (cmd.cmd == 'nameSelect') {
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
        }
        else {
            ui.generalLabel.setText('Invalid name');
        }
    }
    else if (cmd.cmd == 'lobbyList') {
        for (var i = 0; i < cmd.names.length; i++) {
            var name = cmd.names[i];
            state.lobbies[name] = {name: name, members: []};
        }
    }
    else if (cmd.cmd == 'lobbyCreate') {
        state.lobbies[cmd.name] = {name: cmd.name, members: []};
    }
    else if (cmd.cmd == 'lobbyDestroy') {
        delete state.lobbies[cmd.name];
    }
    else if (cmd.cmd == 'lobbyJoin') {
        var lobby = state.lobbies[cmd.name];
        lobby.members = lobby.members.concat(cmd.members);

        if (cmd.name != 'General') {
            ui.gameNameLabel.visible = false;
            ui.gameNameBox.visible = false;
            ui.gameNameButton.visible = false;
            ui.createButton.visible = false;
            ui.lobbyTable.visible = false;

            ui.gamePlayerLabel.visible = true;
            ui.gamePlayerList.visible = true;
            ui.startButton.visible = true;
            ui.exitButton.visible = true;

            ui.gamePlayerLabel.setText('Player List for \'' + cmd.name + '\'');
            ui.gamePlayerList.setText(lobby.members.join('\n'));

            state.gameName = cmd.name;
        }
    }
    else if (cmd.cmd == 'lobbyLeave') {
        var lobby = state.lobbies[cmd.name];
        if (cmd.member == name) {
            lobby.members = [];
        }
        else {
            lobby.members.splice(lobby.members.indexOf(cmd.member), 1);
        }
        ui.gamePlayerList.setText(lobby.members.join('\n'));
    }
    else if (cmd.cmd == 'chat') {
        var text = ui.messagesScrollArea.element.text;
        if (text.length != 0) {
            text += '\n';
        }
        text += formatChatLine(cmd);
        ui.messagesScrollArea.element.setText(text);
    }
    else if (cmd.cmd == 'gs') {
        state.gameState = JSON.parse(cmd.gs);
        state.loadGameState();
    }
    else if (cmd.cmd == 'actions') {
        state.actions = cmd.actions;
        state.handleActions();
    }

    if (cmd.cmd == 'lobbyList' || cmd.cmd == 'lobbyCreate' || cmd.cmd == 'lobbyDestroy') {
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

function formatChatLine(chat) {
    var date = new Date(chat.time);
    var dateString = date.getHours()
        + ':'
        + (date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes())
        + ':'
        + (date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds());
    var message = dateString + ' [' + chat.to + '] ' + chat.from + ': ' + chat.msg;

    return message;
}

function sendCmd(cmd) {
    websocket.send(JSON.stringify(cmd));
}
