/// Board ///

function Board(x, y, cw, ch) {
    UIElement.call(this, x, y, cw * 5 + 60, ch + 20);
    this.highlight = '';
    this.piles = {
        blue: new Pile(this.x + 10, this.y + 10, cw, ch),
        green: new Pile(this.x + cw + 20, this.y + 10, cw, ch),
        red: new Pile(this.x + 2 * cw + 30, this.y + 10, cw, ch),
        white: new Pile(this.x + 3 * cw + 40, this.y + 10, cw, ch),
        yellow: new Pile(this.x + 4 * cw + 50, this.y + 10, cw, ch)
    };
}

Board.prototype = Object.create(UIElement.prototype);
Board.prototype.constructor = Board;

Board.prototype.getChildren = function() {
    var children = [];
    for (var pileColor in this.piles) {
        children.push(this.piles[pileColor]);
    }
    return children;
};

Board.prototype.draw = function(context) {
    context.beginPath();
    context.rect(this.x, this.y, this.width, this.height);
    context.lineWidth = 1;
    context.strokeStyle = 'black';
    context.stroke();

    var xOffset = this.x + 10;
    var yOffset = this.y + 10;
    for (var pileColor in this.piles) {
        var pile = this.piles[pileColor];
        pile.draw(context);

        if (this.highlight == pileColor) {
            context.beginPath();
            context.rect(pile.x, pile.y, pile.width, pile.height);
            context.lineWidth = 7;
            context.strokeStyle = (pileColor == 'white' ? 'black' : pileColor);
            context.stroke();
        }
    }
};

Board.prototype.resetPiles = function() {
    for (var pileColor in this.piles) {
        this.piles[pileColor].pile = [];
    }
};


/// Pile ///
function Pile(x, y, width, height, xOffset, yOffset) {
    UIElement.call(this, x, y, width, height);
    this.pile = [];
    this.xOffset = (xOffset != undefined ? xOffset : 0);
    this.yOffset = (yOffset != undefined ? yOffset : 0);
}

Pile.prototype = Object.create(UIElement.prototype);
Pile.prototype.constructor = Pile;

Pile.prototype.addCard = function(card) {
    this.pile.push(card);
    card.curX = card.x = this.x;
    card.curY = card.y = this.y;
    card.width = this.width;
    card.height = this.height;
};

Pile.prototype.getZLevel = function() {
    return (this.pile.length == 0 ? this.zLevel : this.pile[this.pile.length - 1].zLevel);
};

Pile.prototype.drawEmpty = function(context) {
    context.beginPath();
    context.rect(this.x, this.y, this.width, this.height);
    context.fillStyle = 'grey';
    context.fill();
    context.lineWidth = 1;
    context.strokeStyle = 'grey';
    context.stroke();
};

Pile.prototype.draw = function(context) {
    if (this.xOffset == 0 && this.yOffset == 0) {
        if (this.pile.length <= 1) {
            this.drawEmpty(context);
        }
        if (this.pile.length >= 1) {
            this.pile[this.pile.length - 1].draw(context);
        }
    }
    else {

    }
};

Pile.prototype.handleMouseDrag = function(xy, xDelta, yDelta) {
    if (this.pile.length >= 1) {
        return this.pile[this.pile.length - 1].handleMouseDrag(xy, xDelta, yDelta);
    }
    return false;
};

Pile.prototype.handleMouseUp = function(xy) {
    if (this.pile.length >= 1) {
        return this.pile[this.pile.length - 1].handleMouseUp(xy);
    }
    return false;
};


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
    this.draggable = true;
}

Card.prototype = Object.create(UIElement.prototype);
Card.prototype.constructor = Card;

Card.prototype.draw = function(context) {
    context.beginPath();
    context.rect(this.curX, this.curY, this.width, this.height);
    context.fillStyle = this.color;
    context.fill();
    context.lineWidth = 1;
    context.strokeStyle = 'black';
    context.stroke();

    if (this.value != -1) {
        context.font = '32pt Calibri';
        context.fillStyle = 'white';
        context.strokeStyle = 'black';
        context.lineWidth = 1;
        context.fillText(this.value, this.curX + 3, this.curY + 32);
        context.strokeText(this.value, this.curX + 3, this.curY + 32);
        var textWidth = context.measureText(this.value).width;
        context.fillText(this.value, this.curX + this.width - textWidth - 3, this.curY + this.height - 5);
        context.strokeText(this.value, this.curX + this.width - textWidth - 3, this.curY + this.height - 5);
    }
};

Card.prototype.handleMouseDrag = function(xy, xDelta, yDelta) {
    if (!this.draggable) {
        return false;
    }

    board.highlight = this.color;

    this.curX += xDelta;
    this.curY += yDelta;
    if (this.zLevel != 1000) {
        this.oldZLevel = this.zLevel;
        this.zLevel = 1000;
    }
    return true;
};

Card.prototype.handleMouseUp = function(xy) {
    board.highlight = '';
    this.curX = this.x;
    this.curY = this.y;
    this.zLevel = this.oldZLevel;
    return true;
};

/// Code begins

var websocket;

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

var cw = 100;
var ch = 150;
var board;

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
    console.log(JSON.stringify(cmd));
    if (websocket != undefined) {
        websocket.send(JSON.stringify(cmd));
    }
}

function renderTest(canvasElement) {
    uiManager = new UIManager(canvasElement);

    board = new Board(10, (uiManager.canvas.height - (ch + 20)) / 2, cw, ch);
    uiManager.addElement(board);

    var gameState = JSON.parse('{"1":{"table":{"red":[],"white":[{"value":8,"color":"white"}],"blue":[],"green":[],"yellow":[]},"hand":[{"value":8,"color":"green"},{"value":10,"color":"red"},{"value":0,"color":"white"},{"value":4,"color":"white"},{"value":6,"color":"white"},{"value":10,"color":"white"},{"value":0,"color":"yellow"},{"value":9,"color":"yellow"}]},"2":{"table":{"red":[],"white":[{"value":8,"color":"white"}],"blue":[],"green":[],"yellow":[]}},"discard":{"red":[],"white":[{"value":8,"color":"white"}],"blue":[],"green":[],"yellow":[]},"deck":44,"currentPlayer":1}');
    loadGameState(gameState);
}

function loadGameState(gameState) {
    console.log(gameState);

    // Clean old elements from uiManager
    for (var i = uiManager.elements.length - 1; i >= 0; i--) {
        var element = uiManager.elements[i];
        if (element instanceof Card) {
            uiManager.elements.splice(i, 1);
        }
    }

    // Generate new elements
    var cardsInHand = gameState[player].hand.length;
    var yOffset = 10;
    var yIncr = (uiManager.canvas.height - 20 - ch) / (cardsInHand - 1);
    var xOffset = board.width + 100;

    for (var i = 0; i < cardsInHand; i++) {
        var cardData = gameState[player].hand[i];
        var card = new Card(xOffset, yOffset, cw, ch, cardData);
        card.zLevel = yOffset;
        if (cardsInHand == 7) {
            card.draggable = false;
        }
        uiManager.addElement(card);

        yOffset += yIncr;
    }

    // Handle board
    board.resetPiles();
    for (var color in gameState.discard) {
        var pile = gameState.discard[color];
        for (var i = 0; i < pile.length; i++) {
            board.piles[color].addCard(new Card(0, 0, 0, 0, pile[i]));
        }
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
