/// Code begins

var name;

var lobbies = {};

var uiManager;

var errLabel;
var nameLabel;
var nameBox;
var chatBox;
var chatArea;
var chatArea2;

function initGame(canvasElement) {
    uiManager = new UIManager(canvasElement);

    errLabel = new Label(10, 33, '');

    nameLabel = new Label(10, 13, 'Enter nickname: ');
    nameBox = new Textbox(nameLabel.width + 10, 10, 200, 20);
    nameBox.submitHandler = function(msg) {
        sendCmd({cmd: 'setName', name: msg});
    };

    chatBox = new Textbox(10, 10, 200, 20);
    chatBox.visible = false;
    chatBox.submitHandler = function(msg) {
        sendCmd({cmd: 'msg', msg: msg, target: 'General'});
    };
    chatArea = new FixedWidthLabel(10, 38, 384, '');
    chatArea2 = new ScrollArea(10, 38, 400, 100, chatArea);
    chatArea2.visible = false;

    uiManager.addElement(errLabel);
    uiManager.addElement(nameLabel);
    uiManager.addElement(nameBox);
    uiManager.addElement(chatBox);
    uiManager.addElement(chatArea2);

    openWebSocket();
}

/// Web sockets ///

function openWebSocket() {
    websocket = new WebSocket('ws://127.0.0.1:9003/');
    websocket.onopen = function (evt) { onOpen(evt); };
    websocket.onclose = function (evt) { onClose(evt); };
    websocket.onmessage = function (evt) { onMessage(evt); };
    websocket.onerror = function (evt) { onError(evt); };
}

function onOpen(evt) {
    console.log('CONNECTED');
}

function onClose(evt) {
    console.log('DISCONNECTED');
}

function onMessage(evt) {
    console.log('MSG: ' + evt.data);
    var cmd = JSON.parse(evt.data);
    if (cmd.cmd == 'nameSelect') {
        if (cmd.success) {
            name = cmd.name;
            errLabel.visible = false;
            nameBox.visible = false;
            nameLabel.visible = false;
            chatBox.visible = true;
            chatArea2.visible = true;
        }
        else {
            errLabel.setText('Invalid name');
        }
    }
    else if (cmd.cmd == 'lobbyList') {
        for (var i = 0; i < cmd.names.length; i++) {
            var name = cmd.names[i];
            lobbies[name] = {name: name, members: []};
        }
    }
    else if (cmd.cmd == 'lobbyCreate') {
        lobbies[cmd.name] = {name: cmd.name, members: []};
    }
    else if (cmd.cmd == 'lobbyDestroy') {
        lobbies.remove(cmd.name);
    }
    else if (cmd.cmd == 'lobbyJoin') {
        var lobby = lobbies[cmd.name];
        lobby.members = lobby.members.concat(cmd.members);
    }
    else if (cmd.cmd == 'lobbyLeave') {
        var lobby = lobbies[cmd.name];
        if (cmd.member == name) {
            lobby.members = [];
        }
        else {
            lobby.members.splice(lobby.members.indexOf(cmd.member), 1);
        }
    }
    else if (cmd.cmd == 'chat') {
        var text = chatArea.text;
        if (text.length != 0) {
            text += '\n';
        }
        text += formatChatLine(cmd);
        chatArea.setText(text);
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

function onError(evt) {
    console.log('ERROR: ' + evt.data);
}

function sendCmd(cmd) {
    websocket.send(JSON.stringify(cmd));
}

function renderTest(canvasElement) {
    var gameState = JSON.parse('{"1":{"table":{"red":[], "white":[], "blue":[], "green":[], "yellow":[]}, "hand":["red 5", "blue !", "white 10", "blue 3", "red 7", "white 2", "green 5", "blue 7"]}, "2":{"table":{"red":[], "white":[], "blue":[], "green":[], "yellow":[]}}, "discard":{"red":[], "white":[], "blue":[], "green":[], "yellow":[]}, "deck":44, "currentPlayer":1}');
    uiManager = new UIManager(canvasElement);
    loadGameState(gameState);
}

function loadGameState(gameState) {
    console.log(gameState);

    // Clean old elements from uiManager


    // Generate new elements
    var cw = 100;
    var ch = 150;

    var yOffset1 = uiManager.canvas.height - ch - 10;
    var yOffset2 = 10;
    var xOffset = 10;

    for (var i = 0; i < gameState['1'].hand.length; i++) {
        var cardData = gameState['1'].hand[i];
        var card = new Card(xOffset, yOffset1, cw, ch, cardData);
        uiManager.addElement(card);

        var card2 = new Card(xOffset, yOffset2, cw, ch, '');
        uiManager.addElement(card2);

        xOffset += cw + 10;
    }

    // Mark dirty
    uiManager.dirty = true
}

/// Card ///

function Card(x, y, width, height, data) {
    UIElement.call(this, x, y, width, height);
    var dataParts = data.split(' ');
    if (dataParts.length == 2) {
        this.color = dataParts[0];
        this.text = dataParts[1];
    }
    else {
        this.text = '';
    }
}

Card.prototype = Object.create(UIElement.prototype);
Card.prototype.constructor = Card;

Card.prototype.draw = function(context) {
    context.beginPath();
    context.rect(this.x, this.y, this.width, this.height);
    context.fillStyle = this.color;
    context.fill();
    context.strokeStyle = 'black';
    context.stroke();

    if (this.text.length > 0) {
        context.font = '32pt Calibri';
        context.fillStyle = 'white';
        context.strokeStyle = 'black';
        context.fillText(this.text, this.x + 3, this.y + 32);
        context.strokeText(this.text, this.x + 3, this.y + 32);
    }
};
