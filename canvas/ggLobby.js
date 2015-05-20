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
    chatLabel: null,
    chatBox: null,
    messagesScrollArea: null
};

function initUi() {
    ui.generalLabel = new Label(10, 33, '');

    ui.nameLabel = new Label(10, 13, 'Enter nickname: ');
    ui.nameBox = new Textbox(ui.nameLabel.width + 10, 10, 200, 20);
    ui.nameBox.submitHandler = function(msg) {
        login(msg, ui.serverBox.text);
    };

    ui.serverLabel = new Label(10, 38, 'Server: ');
    ui.serverBox = new Textbox(ui.nameLabel.width + 10, 35, 200, 20);
    ui.serverBox.text = '10.202.74.4:9003';
    ui.serverLabel.x = ui.nameLabel.width + 10 - ui.serverLabel.width;

    ui.loginButton = new Button(ui.nameBox.x + ui.nameBox.width + 10, 10, 'Login');
    ui.loginButton.handleMouseClick = function(x, y) {
        login(ui.nameBox.text, ui.serverBox.text);
        return false;
    };

    ui.createButton = new Button(10, 10, 'Create Game');
    ui.createButton.visible = false;

    ui.lobbyTable = new Table(10, 38, 500);
    ui.lobbyTable.emptyText = 'No games found';
    ui.lobbyTable.visible = false;

    ui.chatLabel = new Label(550, 13, 'Chat: ');
    ui.chatLabel.visible = false;
    ui.chatBox = new Textbox(ui.chatLabel.width + 550, 10, 400 - ui.chatLabel.width, 20);
    ui.chatBox.visible = false;
    ui.chatBox.submitHandler = function(msg) {
        sendCmd({cmd: 'msg', msg: msg, target: 'General'});
    };

    var messagesLabel = new FixedWidthLabel(550, 38, 384, '');
    ui.messagesScrollArea = new ScrollArea(550, 38, 400, 300, messagesLabel);
    ui.messagesScrollArea.visible = false;
}

function setupLobby(canvasElement, gameName) {
    state.gameName = gameName;

    initUi();

    uiManager = new UIManager(canvasElement);
    uiManager.addElements(ui);
    //uiManager.onresize = loadGameState;
}

function login(nickname, host) {
    websocket = new ReconnectingWebSocket(host);
    websocket.connect();
    websocket.onmessage = onMessage;
    sendCmd({cmd: 'setName', name: nickname});
}

/// Web sockets ///

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
