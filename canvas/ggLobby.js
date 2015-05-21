var websocket;


var state = {
    gameName: null,
    playerName: null,
    lobbies: {},
    game: null
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
    exitButton: null,

    chatLabel: null,
    chatBox: null,
    messagesScrollArea: null
};

function setupLobby(canvasElement, gameName) {
    uiManager = new UIManager(canvasElement);
    state.gameName = gameName;

    initUi();
    //uiManager.onresize = loadGameState;
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

    ui.gameNameLabel = new Label(10, 38, 'Game Name: ');
    ui.gameNameBox = new Textbox(ui.gameNameLabel.width + 10, 35, 200, 20);
    ui.gameNameBox.submitHandler = function(msg) {
        createGame(msg);
    };
    ui.gameNameButton = new Button(ui.gameNameBox.x + ui.gameNameBox.width + 10, 35, 'Create');
    ui.gameNameButton.handleMouseClick = function(x, y) {
        createGame(ui.gameNameBox.text);
        return true;
    };

    ui.gamePlayerLabel = new Label(10, 38, '');
    ui.gamePlayerList = new Label(10, 58, '');

    ui.exitButton = new Button(10, 10, 'Exit Game');
    ui.exitButton.handleMouseClick = function(x, y) {
        leaveGame();
        return true;
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

function createGame(gameName) {
    ui.gameNameLabel.visible = false;
    ui.gameNameBox.visible = false;
    ui.gameNameButton.visible = false;

    ui.gamePlayerLabel.visible = true;
    ui.gamePlayerList.visible = true;

    ui.gamePlayerLabel.setText('Player List for \'' + gameName + '\'');
    ui.gameNameLabel.setText(state.playerName + '\n' + state.playerName);

    sendCmd({ cmd: "createGame", name: gameName });
    // TODO: Start game
}

function leaveGame() {
    ui.gameNameLabel.visible = false;
    ui.gameNameBox.visible = false;
    ui.gameNameButton.visible = false;
    ui.gamePlayerLabel.visible = false;
    ui.gamePlayerList.visible = false;
    ui.exitButton.visible = false;

    ui.createButton.visible = true;
    ui.lobbyTable.visible = true;

    // TODO: Leave game (if in one)
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
        state.lobbies.remove(cmd.name);
    }
    else if (cmd.cmd == 'lobbyJoin') {
        var lobby = state.lobbies[cmd.name];
        lobby.members = lobby.members.concat(cmd.members);
    }
    else if (cmd.cmd == 'lobbyLeave') {
        var lobby = state.lobbies[cmd.name];
        if (cmd.member == name) {
            lobby.members = [];
        }
        else {
            lobby.members.splice(lobby.members.indexOf(cmd.member), 1);
        }
    }
    else if (cmd.cmd == 'chat') {
        var text = ui.messagesScrollArea.element.text;
        if (text.length != 0) {
            text += '\n';
        }
        text += formatChatLine(cmd);
        ui.messagesScrollArea.element.setText(text);
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

//function gameMessage(evt) {
//    var cmd = JSON.parse(evt.data);
//    if (cmd.cmd == 'gs') {
//        state.game = JSON.parse(cmd.gs);
//        loadGameState();
//    }
//}
