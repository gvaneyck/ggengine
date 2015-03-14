/// LostCitiesBoard ///

function LostCitiesBoard(x, y, cw, ch) {
    UIElement.call(this, x, y, cw * 5 + 60, ch + 20);
    this.piles = {
        blue: new Pile(this.x + 10, this.y + 10, cw, ch),
        green: new Pile(this.x + cw + 20, this.y + 10, cw, ch),
        red: new Pile(this.x + 2 * cw + 30, this.y + 10, cw, ch),
        white: new Pile(this.x + 3 * cw + 40, this.y + 10, cw, ch),
        yellow: new Pile(this.x + 4 * cw + 50, this.y + 10, cw, ch)
    };
    this.p1 = {
        blue: new Pile(this.x + 10, this.y - 10 - ch, cw, ch, 0, -40),
        green: new Pile(this.x + cw + 20, this.y - 10 - ch, cw, ch, 0, -40),
        red: new Pile(this.x + 2 * cw + 30, this.y - 10 - ch, cw, ch, 0, -40),
        white: new Pile(this.x + 3 * cw + 40, this.y - 10 - ch, cw, ch, 0, -40),
        yellow: new Pile(this.x + 4 * cw + 50, this.y - 10 - ch, cw, ch, 0, -40)
    };
    this.p2 = {
        blue: new Pile(this.x + 10, this.y + 30 + ch, cw, ch, 0, 40),
        green: new Pile(this.x + cw + 20, this.y + 30 + ch, cw, ch, 0, 40),
        red: new Pile(this.x + 2 * cw + 30, this.y + 30 + ch, cw, ch, 0, 40),
        white: new Pile(this.x + 3 * cw + 40, this.y + 30 + ch, cw, ch, 0, 40),
        yellow: new Pile(this.x + 4 * cw + 50, this.y + 30 + ch, cw, ch, 0, 40)
    };

    for (var color in this.piles) {
        var pile = this.piles[color];
        pile.color = color;
        pile.handleMouseDoubleClick = function(x, y) {
            if (gs.state == 'DRAW_CARD' && this.pile.length >= 1) {
                sendCmd({cmd: 'action', action: 'drawPile', args: [this.color]});
            }
        }
    }

    this.draggedCard = null;
}

LostCitiesBoard.prototype = Object.create(UIElement.prototype);
LostCitiesBoard.prototype.constructor = LostCitiesBoard;

LostCitiesBoard.prototype.getChildren = function() {
    var children = [];
    for (var pileColor in this.piles) {
        children.push(this.piles[pileColor]);
        children.push(this.p1[pileColor]);
        children.push(this.p2[pileColor]);
    }
    return children;
};

LostCitiesBoard.prototype.draw = function(context) {
    context.beginPath();
    context.rect(this.x, this.y, this.width, this.height);
    context.lineWidth = 1;
    context.strokeStyle = 'black';
    context.stroke();

    for (var pileColor in this.piles) {
        this.p1[pileColor].draw(context);
        this.p2[pileColor].draw(context);

        var pile = this.piles[pileColor];
        pile.draw(context);

        if (this.draggedCard != null) {
            var cardColor = this.draggedCard.color;
            var myPile = this.p2[cardColor];
            var discardPile = this.piles[cardColor];

            var highlightMine = true;
            if (myPile.pile.length >= 1) {
                var topCard = myPile.pile[myPile.pile.length - 1];
                if (this.draggedCard.value < topCard.value) {
                    highlightMine = false;
                }
            }

            if (highlightMine) {
                if (myPile.hover) {
                    myPile.highlight(context);
                }
                else if (discardPile.hover) {
                    discardPile.highlight(context);
                }
                else {
                    myPile.highlight(context);
                    discardPile.highlight(context);
                }
            }
            else {
                discardPile.highlight(context);
            }
        }
    }
};

LostCitiesBoard.prototype.resetPiles = function() {
    for (var pileColor in this.piles) {
        this.piles[pileColor].reset();
        this.p1[pileColor].reset();
        this.p2[pileColor].reset();
    }
};


/// LostCitiesCard ///

function LostCitiesCard(data, x, y, width, height) { Card.call(this, data, x, y, width, height); } // No constructor
LostCitiesCard.prototype = Object.create(Card.prototype);
LostCitiesCard.prototype.constructor = LostCitiesCard;

LostCitiesCard.prototype.setHover = function(hover) {
    if (this.hover != hover) {
        this.hover = hover;
        return true;
    }
    return false;
};

LostCitiesCard.prototype.handleMouseDown = function(x, y) {
    var dirty = Card.prototype.handleMouseDown.call(this, x, y);
    if (gs.state == 'PLAY_OR_DISCARD' && this.location == 'hand') {
        board.draggedCard = this;
        dirty = true;
    }
    return dirty;
};

LostCitiesCard.prototype.handleMouseHover = function(x, y) {
    return (board.draggedCard != this);
};

LostCitiesCard.prototype.handleMouseUp = function(x, y) {
    var dirty = Card.prototype.handleMouseUp.call(this, x, y);
    if (board.draggedCard != null) {
        board.draggedCard = null;
        dirty = true;
    }

    if (gs.state == 'PLAY_OR_DISCARD') {
        for (var color in board.piles) {
            if (isClicked(x, y, board.piles[color])) {
                sendCmd({cmd: 'action', action: 'discardCard', args: [this.handIdx]});
            }
        }
        for (var color in board.p2) {
            if (isClicked(x, y, board.p2[color])) {
                sendCmd({cmd: 'action', action: 'playCard', args: [this.handIdx]});
            }
        }
    }

    return dirty;
};

LostCitiesCard.prototype.draw = function(context) {
    Card.prototype.draw.call(this, context);
    if (board.draggedCard == null && this.location == 'hand' && this.hover) {
        this.highlight(context);
    }
};


/// Code begins

var websocket;

var gs = {me: '1', them: '2'};

var name;

var lobbies = {};

var uiManager;
var gameState;

var generalLabel = {};
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
    uiManager.onresize = loadGameState;

    generalLabel = new Label(10, 33, '');

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

    uiManager.addElement(generalLabel);
    uiManager.addElement(nameLabel);
    uiManager.addElement(nameBox);
    uiManager.addElement(chatBox);
    uiManager.addElement(chatArea2);

    openWebSocket();
    websocket.onmessage = onMessage;
}

/// Web sockets ///

function openWebSocket() {
    websocket = new ReconnectingWebSocket('127.0.0.1:9003');
    websocket.connect();
}

function onMessage(evt) {
    console.log('MSG: ' + evt.data);
    var cmd = JSON.parse(evt.data);
    if (cmd.cmd == 'nameSelect') {
        if (cmd.success) {
            name = cmd.name;
            generalLabel.visible = false;
            nameBox.visible = false;
            nameLabel.visible = false;
            chatBox.visible = true;
            chatArea2.visible = true;
        }
        else {
            generalLabel.setText('Invalid name');
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

function sendCmd(cmd) {
    websocket.send(JSON.stringify(cmd));
}

function renderTest(canvasElement) {
    uiManager = new UIManager(canvasElement);
    uiManager.onresize = loadGameState;

    board = new LostCitiesBoard(10, (uiManager.canvas.height - (ch + 20)) / 2, cw, ch);
    generalLabel = new Label(board.width + 20, board.y - 10, '');
    uiManager.addElement(board);
    uiManager.addElement(generalLabel);

    gameState = JSON.parse('{"1":{"table":{"red":[],"white":[{"value":7,"color":"white"},{"value":8,"color":"white"}],"blue":[],"green":[],"yellow":[]},"hand":[{"value":8,"color":"green"},{"value":10,"color":"red"},{"value":0,"color":"white"},{"value":4,"color":"white"},{"value":6,"color":"white"},{"value":10,"color":"white"},{"value":0,"color":"yellow"},{"value":9,"color":"yellow"}]},"2":{"table":{"red":[],"white":[{"value":7,"color":"white"},{"value":8,"color":"white"}],"blue":[],"green":[],"yellow":[]}},"discard":{"red":[],"white":[{"value":2,"color":"white"},{"value":8,"color":"white"}],"blue":[],"green":[],"yellow":[]},"deck":44,"currentPlayer":1}');
    loadGameState();
}

function loadGameState() {
    if (gameState == undefined) {
        return;
    }

    // Clean old elements from uiManager
    for (var i = uiManager.elements.length - 1; i >= 0; i--) {
        var element = uiManager.elements[i];
        if (element instanceof Card) {
            uiManager.elements.splice(i, 1);
        }
    }

    // Set global game state stuff
    var cardsInHand = gameState[gs.me].hand.length;
    if (gameState.currentPlayer != gs.me) {
        gs.state = 'NOT_MY_TURN';
        generalLabel.setText('Opponent\'s turn');
    }
    else if (cardsInHand == 8) {
        gs.state = 'PLAY_OR_DISCARD';
        generalLabel.setText('Play or discard');
    }
    else if (cardsInHand == 7) {
        gs.state = 'DRAW_CARD';
        generalLabel.setText('Draw a card');
    }
    else {
        console.log('Couldn\'t determine game state');
    }

    // Handle hand
    var yOffset = 10;
    var yIncr = (uiManager.canvas.height - 20 - ch) / (cardsInHand - 1);
    var xOffset = board.width + 160;

    for (var i = 0; i < cardsInHand; i++) {
        var cardData = gameState[gs.me].hand[i];

        var card = new LostCitiesCard(cardData, xOffset, yOffset, cw, ch);
        card.location = 'hand';
        card.handIdx = i;
        card.zLevel = yOffset;

        uiManager.addElement(card);

        yOffset += yIncr;
    }

    // Handle board
    board.resetPiles();
    for (var color in gameState.discard) {
        var pile = gameState.discard[color];
        for (var i = 0; i < pile.length; i++) {
            var card = new LostCitiesCard(pile[i]);
            card.location = 'discard';
            board.piles[color].addCard(card);
        }
    }

    // Handle player tableau
    for (var color in gameState[gs.them].table) {
        var pile = gameState[gs.them].table[color];
        for (var i = 0; i < pile.length; i++) {
            var card = new LostCitiesCard(pile[i]);
            card.location = 'myTable';
            board.p1[color].addCard(card);
        }
    }
    for (var color in gameState[gs.me].table) {
        var pile = gameState[gs.me].table[color];
        for (var i = 0; i < pile.length; i++) {
            var card = new LostCitiesCard(pile[i]);
            card.location = 'theirTable';
            board.p2[color].addCard(card);
        }
    }

    // Handle deck
    var deck = new LostCitiesCard({color: 'black', value: gameState.deck}, board.width + 20, board.y + 10, 100, 150);
    deck.location = 'deck';
    deck.draggable = false;
    deck.handleMouseDoubleClick = function(x, y) {
        if (gs.state == 'DRAW_CARD') {
            sendCmd({cmd: 'action', action: 'drawDeck'});
        }
    };
    uiManager.addElement(deck);

    // Mark dirty
    uiManager.dirty = true;
}

function gameTest(canvasElement, p) {
    uiManager = new UIManager(canvasElement);
    uiManager.onresize = loadGameState;

    board = new LostCitiesBoard(10, (uiManager.canvas.height - (ch + 20)) / 2, cw, ch);
    generalLabel = new Label(board.width + 20, board.y - 10, '');
    uiManager.addElement(board);
    uiManager.addElement(generalLabel);

    gs.me = p;
    gs.them = (p == 1 ? 2 : 1);

    openWebSocket();
    //websocket = new WebSocket('ws://127.0.0.1:9003');
    websocket.onmessage = gameMessage;
    websocket.onopen = function(e) {
        sendCmd({cmd: 'setName', name: 'Jabe' + p});
    };
}

function gameMessage(evt) {
    console.log('MSG: ' + evt.data);
    var cmd = JSON.parse(evt.data);
    if (cmd.cmd == 'gs') {
        gameState = JSON.parse(cmd.gs);
        loadGameState();
    }
}
