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

var ch = 150;
var cw = 100;

var board;
var gs;

function loadGameState() {
    if (state.gameState == undefined) {
        return;
    }
    gs = state.gameState;
    var me = gs.me;
    var them = (me == 1 ? 2 : 1);

    // Clean old elements from uiManager
    uiManager.elements = [];

    board = new LostCitiesBoard(10, (uiManager.canvas.height - (ch + 20)) / 2, cw, ch);

    // Set global game state stuff
    var generalLabel = new Label(board.width + 20, board.y - 10, '');
    var cardsInHand = gs[me].hand.length;
    if (gs.currentPlayer != me) {
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
        var cardData = gs[me].hand[i];

        var card = new LostCitiesCard(cardData, xOffset, yOffset, cw, ch);
        card.location = 'hand';
        card.handIdx = i;
        card.zLevel = yOffset;

        uiManager.addElement(card);

        yOffset += yIncr;
    }

    // Handle board
    board.resetPiles();
    for (var color in gs.discard) {
        var pile = gs.discard[color];
        for (var i = 0; i < pile.length; i++) {
            var card = new LostCitiesCard(pile[i]);
            card.location = 'discard';
            board.piles[color].addCard(card);
        }
    }

    // Handle player tableau
    for (var color in gs[them].table) {
        var pile = gs[them].table[color];
        for (var i = 0; i < pile.length; i++) {
            var card = new LostCitiesCard(pile[i]);
            card.location = 'myTable';
            board.p1[color].addCard(card);
        }
    }
    for (var color in gs[me].table) {
        var pile = gs[me].table[color];
        for (var i = 0; i < pile.length; i++) {
            var card = new LostCitiesCard(pile[i]);
            card.location = 'theirTable';
            board.p2[color].addCard(card);
        }
    }

    // Handle deck
    var deck = new LostCitiesCard({color: 'black', value: gs.deck}, board.width + 20, board.y + 10, 100, 150);
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

function handleActions() {

}
