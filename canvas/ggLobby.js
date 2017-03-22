var websocket;


var state = {
    gameName: null,
    playerName: null,
    activeRoom: null,
    rooms: { lobby: {}, game: {}, direct: {} },
    game: null,
    gameStateHandler: function(gs) {},
    actionHandler: function(actions) {}
};

var mainContainer;
var gameContainer = new Container();
var generalMessage; // TODO
var ui = {
    loginContainer: null,
    gameBrowseContainer: null,
    gameCreateContainer: null,
    gameLobbyContainer: null,
    chatContainer: null,
    gameContainer: gameContainer
};

var loginUI = {
    nameLabel: null,
    nameBox: null,
    serverLabel: null,
    serverBox: null,
    loginButton: null
};

var gameBrowseUI = {
    createButton: null,
    lobbyTable: null
};

var gameCreateUI = {
    gameNameLabel: null,
    gameNameBox: null,
    gameNameButton: null,
    exitButton: null
};

var gameLobbyUI = {
    gamePlayerLabel: null,
    gamePlayerList: null,
    startButton: null,
    exitButton: null
};

var chatUI = {
    roomLabel: null,
    messagesScrollArea: null,
    chatLabel: null,
    chatBox: null
};

function setupLobby(canvasElement, gameName, gameStateHandler, actionHandler) {
    state.gameName = gameName;

    initUi();
    initRootContainer(mainContainer, canvasElement);

    showLoginUI();
    // TODO: mainContainer.onresize = gameStateHandler;
    state.gameStateHandler = gameStateHandler;
    state.actionHandler = actionHandler;
}

function initUi() {
    generalMessage = new Label(10, 600, "");

    loginUI.nameLabel = new Label(10, 13, "Enter nickname: ");
    loginUI.nameBox = new Textbox(loginUI.nameLabel.width + 10, 10, 200, 20);
    loginUI.nameBox.submitHandler = function(msg) {
        login(msg, loginUI.serverBox.text);
    };
    loginUI.serverLabel = new Label(10, 38, "Server: ");
    loginUI.serverBox = new Textbox(loginUI.nameLabel.width + 10, 35, 200, 20);
    loginUI.serverBox.text = "localhost:9998";
    loginUI.serverLabel.x = loginUI.nameLabel.width + 10 - loginUI.serverLabel.width;
    loginUI.loginButton = new Button(loginUI.nameBox.x + loginUI.nameBox.width + 10, 10, "Login");
    loginUI.loginButton.handleMouseClick = function(x, y) {
        login(loginUI.nameBox.text, loginUI.serverBox.text);
        return true;
    };

    gameBrowseUI.createButton = new Button(10, 10, "Create Game");
    gameBrowseUI.createButton.handleMouseClick = function(x, y) {
        startCreateGame();
        return true;
    };
    gameBrowseUI.lobbyTable = new Table(10, 38, 500);
    gameBrowseUI.lobbyTable.emptyText = "No games found";
    gameBrowseUI.lobbyTable.onCellClick = function(row, col) {
        if (col == 0 && row > 0) {
            sendCmd({cmd: "joinRoom", type: "game", name: gameBrowseUI.lobbyTable.elements[row][2].text })
        }
    };

    var exitButton = new Button(10, 10, "Exit Game");
    exitButton.handleMouseClick = function(x, y) {
        leaveGame();
        return true;
    };

    gameCreateUI.gameNameLabel = new Label(10, 38, "Game Name: ");
    gameCreateUI.gameNameBox = new Textbox(gameCreateUI.gameNameLabel.width + 10, 35, 200, 20);
    gameCreateUI.gameNameBox.submitHandler = function(msg) {
        createGameLobby(msg);
    };
    gameCreateUI.gameNameButton = new Button(gameCreateUI.gameNameBox.x + gameCreateUI.gameNameBox.width + 10, 35, "Create");
    gameCreateUI.gameNameButton.handleMouseClick = function(x, y) {
        createGameLobby(gameCreateUI.gameNameBox.text);
        return true;
    };
    gameCreateUI.exitButton = exitButton;

    gameLobbyUI.gamePlayerLabel = new Label(10, 38, "");
    gameLobbyUI.gamePlayerList = new Label(10, 58, "");
    gameLobbyUI.startButton = new Button(exitButton.width + 20, 10, "Start Game");
    gameLobbyUI.startButton.handleMouseClick = function(x, y) {
        startGame();
        return true;
    };
    //ui.generalLabel.x = gameLobbyUI.startButton.x + gameLobbyUI.startButton.width + 20;
    //ui.generalLabel.y = 13;
    //ui.generalLabel.setText(cmd.message);
    //ui.generalLabel.setVisible(true);
    gameLobbyUI.exitButton = exitButton;

    chatUI.roomLabel = new Label(0, 10, "");
    chatUI.messagesScrollArea = new ScrollArea(0, 30, 400, 300, new FixedWidthLabel(550, 35, 384, ""));
    chatUI.chatLabel = new Label(0, 338, "Chat: ");
    chatUI.chatBox = new Textbox(chatUI.chatLabel.width, 335, 400 - chatUI.chatLabel.width, 20);
    chatUI.chatBox.submitHandler = function(msg) {
        sendCmd({cmd: "msg", msg: msg, type: state.activeRoom.type, target: state.activeRoom.name});
    };

    ui.loginContainer = new Container();
    ui.loginContainer.addElements(loginUI);
    ui.gameBrowseContainer = new Container();
    ui.gameBrowseContainer.addElements(gameBrowseUI);
    ui.gameCreateContainer = new Container();
    ui.gameCreateContainer.addElements(gameCreateUI);
    ui.gameLobbyContainer = new Container();
    ui.gameLobbyContainer.addElements(gameLobbyUI);
    ui.chatContainer = new Container(550, 0);
    ui.chatContainer.addElements(chatUI);

    mainContainer = new Container();
    mainContainer.addElements(ui);
}

function showLoginUI() {
    mainContainer.onlyVisible([ ui.loginContainer ]);
}

function login(nickname, host) {
    generalMessage.setText("Connecting...");

    websocket = new ReconnectingWebSocket(host);
    websocket.connect();
    websocket.onclose = onClose;
    websocket.onmessage = onMessage;
    state.playerName = nickname;
    sendCmd({cmd: "login", name: nickname});
}

function startCreateGame() {
    mainContainer.onlyVisible([ ui.chatContainer, ui.gameCreateContainer ]);
}

function createGameLobby(lobbyName) {
    sendCmd({ cmd: "joinRoom", name: lobbyName, game: state.gameName, maxSize: 4 });
}

function leaveGame() {
    mainContainer.onlyVisible([ ui.chatContainer, ui.gameBrowseContainer ]);

    if (state.activeRoom.type == "game") {
        sendCmd({ cmd: "leaveRoom", type: "game", name: state.activeRoom.name });
        state.activeRoom = state.rooms.lobby["General"];
        updateChat();
    }
}

function startGame() {
    sendCmd({ cmd: "startGame", name: state.activeRoom.name });
}

/// Web sockets ///

function onClose(evt) {
    mainContainer.onlyVisible([ ]);

    generalMessage.setText("You were disconnected from the server.  Reconnecting...");

    sendCmd({cmd: "login", name: state.playerName});
}

function onMessage(evt) {
    generalMessage.setText("");

    var cmd = JSON.parse(evt.data);
    if (cmd.cmd == "login") {
        if (cmd.success) {
            state.playerName = cmd.name;

            mainContainer.onlyVisible([ ui.chatContainer, ui.gameBrowseContainer ]);
        } else {
            generalMessage.setText("Invalid name");
        }
    } else if (cmd.cmd == "roomJoin") {
        var room = state.rooms[cmd.type][cmd.name];
        if (room == undefined) {
            room = { type: cmd.type, name: cmd.name };
            state.rooms[cmd.type][cmd.name] = room;
        }

        state.activeRoom = room;

        if (cmd.members != undefined) {
            room.members = cmd.members;
        }
        if (cmd.member != undefined) {
            room.members = room.members.concat(cmd.members)
        }
        if (cmd.messages != undefined) {
            room.messages = cmd.messages;
            updateChat();
        }

        if (cmd.type == "game") {
            showGameLobbyUI();
        }
    } else if (cmd.cmd == "gameList") {
        state.rooms.game = {};
        for (var i in cmd.names) {
            var name = cmd.names[i];
            state.rooms.game[name] = {type: "game", name: name, members: [], messages: []};
        }
    } else if (cmd.cmd == "roomCreate") {
        state.rooms[cmd.type][cmd.name] = {type: cmd.type, name: cmd.name, members: [], messages: []};
    } else if (cmd.cmd == "roomLeave") {
        var room = state.rooms[cmd.type][cmd.name];
        room.members.splice(room.members.indexOf(cmd.member), 1);
        gameLobbyUI.gamePlayerList.setText(room.members.join("\n"));
    } else if (cmd.cmd == "roomDestroy") {
        delete state.rooms[cmd.type][cmd.name];
    } else if (cmd.cmd == "chat") {
        var room = state.rooms[cmd.type][cmd.room];
        room.messages = room.messages.concat({ time: cmd.time, message: cmd.message, from: cmd.from });
        if (room == state.activeRoom) {
            updateChat();
        }
    } else if (cmd.cmd == "message") {
        state.activeRoom = state.activeRoom.messages.concat({ time: cmd.time, message: cmd.message });
        updateChat();
    } else if (cmd.cmd == "gs") {
        state.gameStateHandler(cmd.gs);
    } else if (cmd.cmd == "actions") {
        state.actionHandler(cmd.actions);
    } else if (cmd.cmd == "end") {
        handleEndGame(cmd);
    }

    if (cmd.cmd == "gameList" || cmd.cmd == "roomCreate" || cmd.cmd == "roomDestroy") {
        var games = [];
        for (var name in state.rooms.game) {
            games.push([new Label("Click to Join"), new Label(state.gameName), new Label(name)])
        }

        if (games.length > 0) {
            games.splice(0, 0, [new Label("Join"), new Label("Game"), new Label("Name")])
        }

        gameBrowseUI.lobbyTable.elements = games;
    }
}

function updateChat() {
    var text = "";
    for (var i in state.activeRoom.messages) {
        var message = state.activeRoom.messages[i];

        var date = new Date(message.time);
        var dateString = date.getHours()
            + ":"
            + (date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes())
            + ":"
            + (date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds());

        text += dateString + " ";
        if (message.from != undefined) {
            text += message.from + ": ";
        }
        text += message.message + "\n";
    }
    chatUI.messagesScrollArea.element.setText(text);

    chatUI.roomLabel.setText(state.activeRoom.name);
}

function sendCmd(cmd) {
    websocket.send(JSON.stringify(cmd));
}

function handleEndGame(cmd) {
    mainContainer.elements = [];

    for (var i in mainContainer.elements) {
        mainContainer.elements[i].setVisible(false);
    }

    showGameLobbyUI();
}

function showGameLobbyUI() {
    mainContainer.onlyVisible([ ui.chatContainer, ui.gameLobbyContainer ]);

    gameLobbyUI.gamePlayerLabel.setText("Player List for \"" + state.activeRoom.name + "\"");
    gameLobbyUI.gamePlayerList.setText(state.activeRoom.members.join("\n"));
}
