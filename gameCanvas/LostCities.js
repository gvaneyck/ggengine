/// Card ///

function Card(x, y, width, height, data) {
    UIElement.call(this, x, y, width, height);
    this.value = -1;
    this.color = 'black';
    if (data != undefined) {
        this.value = data.value;
        this.color = data.color;
    }
    this.curX = x;
    this.curY = y;
}

Card.prototype = Object.create(UIElement.prototype);
Card.prototype.constructor = Card;

Card.prototype.draw = function(context) {
    if (this.curX == this.x && this.curY == this.y) {
        context.beginPath();
        context.rect(this.x, this.y, this.width, this.height);
        context.fillStyle = this.color;
        context.fill();
        context.strokeStyle = 'black';
        context.stroke();
    }
    else {
        context.beginPath();
        context.rect(this.x, this.y, this.width, this.height);
        context.fillStyle = 'grey';
        context.fill();
        context.strokeStyle = 'grey';
        context.stroke();

        context.beginPath();
        context.rect(this.curX, this.curY, this.width, this.height);
        context.fillStyle = this.color;
        context.fill();
        context.strokeStyle = 'black';
        context.stroke();
    }

    if (this.value != -1) {
        context.font = '32pt Calibri';
        context.fillStyle = 'white';
        context.strokeStyle = 'black';
        context.fillText(this.value, this.curX + 3, this.curY + 32);
        context.strokeText(this.value, this.curX + 3, this.curY + 32);
    }
};

Card.prototype.handleMouseDrag = function(xy, xDelta, yDelta) {
    this.curX += xDelta;
    this.curY += yDelta;
    this.zLevel = 1000;
    return true;
};

Card.prototype.handleMouseUp = function(xy) {
    this.curX = this.x;
    this.curY = this.y;
    this.zLevel = 0;
    return true;
};

/// Code begins

var player = '1';
var opponent = '2';
var name;

var lobbies = {};

var uiManager;

var errLabel = {};
var nameLabel = {};
var nameBox = {};
var chatBox = {};
var chatArea = {};
var chatArea2 = {};

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
    //websocket = new WebSocket('ws://127.0.0.1:9003/');
    websocket = new WebSocket('ws://76.91.23.89:9003');
    websocket.onopen = function (evt) { onOpen(evt); };
    websocket.onclose = function (evt) { onClose(evt); };
    websocket.onmessage = function (evt) { onMessage(evt); };
    websocket.onerror = function (evt) { onError(evt); };
}

function onOpen(evt) {
    console.log('CONNECTED');
    connected = true;
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
    var gameState = JSON.parse('{"1":{"table":{"red":[],"white":[],"blue":[],"green":[],"yellow":[]},"hand":[{"value":8,"color":"green"},{"value":10,"color":"red"},{"value":0,"color":"white"},{"value":4,"color":"white"},{"value":6,"color":"white"},{"value":10,"color":"white"},{"value":0,"color":"yellow"},{"value":9,"color":"yellow"}]},"2":{"table":{"red":[],"white":[],"blue":[],"green":[],"yellow":[]}},"discard":{"red":[],"white":[],"blue":[],"green":[],"yellow":[]},"deck":44,"currentPlayer":1}');
    uiManager = new UIManager(canvasElement);
    loadGameState(gameState);
}

function loadGameState(gameState) {
    console.log(gameState);

    // Clean old elements from uiManager
    for (var i = uiManager.elements.length - 1; i >= 0; i--) {
        if (uiManager.elements[i] instanceof Card) {
            uiManager.elements.splice(i, 1);
        }
    }

    // Generate new elements
    var cw = 100;
    var ch = 150;

    var yOffset1 = uiManager.canvas.height - ch - 10;
    var yOffset2 = 10;
    var xOffset = 10;

    for (var i = 0; i < gameState[player].hand.length; i++) {
        var cardData = gameState[player].hand[i];
        var card = new Card(xOffset, yOffset1, cw, ch, cardData);
        uiManager.addElement(card);

        var card2 = new Card(xOffset, yOffset2, cw, ch);
        uiManager.addElement(card2);

        xOffset += cw + 10;
    }

    // Mark dirty
    uiManager.dirty = true
}

function gameTest(canvasElement, p) {
    player = '' + p;
    opponent = (p == 1 ? '2' : '1');
    uiManager = new UIManager(canvasElement);
    openWebSocket();
    websocket.onmessage = gameMessage;
    websocket.onopen = function(e) {
        sendCmd({cmd: 'setName', name: 'Jabe' + p});
        sendCmd({cmd: 'setPlayerId', id: p});
    };
}

function gameMessage(evt) {
    console.log('MSG: ' + evt.data);
    var cmd = JSON.parse(evt.data);
    if (cmd.cmd == 'gs') {
        loadGameState(JSON.parse(cmd.gs));
    }
    uiManager.dirty = true
}

function playCard(idx) {
    sendCmd()
}
