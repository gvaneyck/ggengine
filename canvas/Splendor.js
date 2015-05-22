function loadGameState() {
//    var data = JSON.parse('{"cmd":"gs","gs":"{\\"1\\":{\\"bank\\":{\\"red\\":0,\\"green\\":0,\\"blue\\":0,\\"white\\":0,\\"black\\":0,\\"gold\\":0},\\"prod\\":{\\"red\\":0,\\"green\\":0,\\"blue\\":0,\\"white\\":0,\\"black\\":0},\\"points\\":0,\\"devs\\":[],\\"stash\\":[{\\"id\\":1},{\\"id\\":2}]},\\"2\\":{\\"bank\\":{\\"red\\":0,\\"green\\":0,\\"blue\\":0,\\"white\\":0,\\"black\\":0,\\"gold\\":0},\\"prod\\":{\\"red\\":0,\\"green\\":0,\\"blue\\":0,\\"white\\":0,\\"black\\":0},\\"points\\":0,\\"devs\\":[],\\"stash\\":[]},\\"markets\\":[[{\\"gem\\":\\"white\\",\\"points\\":0,\\"id\\":7,\\"reqs\\":{\\"red\\":0,\\"green\\":0,\\"blue\\":3,\\"white\\":0,\\"black\\":0},\\"tier\\":1},{\\"gem\\":\\"green\\",\\"points\\":0,\\"id\\":20,\\"reqs\\":{\\"red\\":3,\\"green\\":0,\\"blue\\":0,\\"white\\":0,\\"black\\":0},\\"tier\\":1},{\\"gem\\":\\"red\\",\\"points\\":0,\\"id\\":30,\\"reqs\\":{\\"red\\":0,\\"green\\":1,\\"blue\\":1,\\"white\\":1,\\"black\\":1},\\"tier\\":1},{\\"gem\\":\\"white\\",\\"points\\":0,\\"id\\":4,\\"reqs\\":{\\"red\\":1,\\"green\\":2,\\"blue\\":1,\\"white\\":0,\\"black\\":1},\\"tier\\":1}],[{\\"gem\\":\\"red\\",\\"points\\":2,\\"id\\":62,\\"reqs\\":{\\"red\\":0,\\"green\\":0,\\"blue\\":0,\\"white\\":3,\\"black\\":5},\\"tier\\":2},{\\"gem\\":\\"green\\",\\"points\\":2,\\"id\\":57,\\"reqs\\":{\\"red\\":0,\\"green\\":0,\\"blue\\":2,\\"white\\":4,\\"black\\":1},\\"tier\\":2},{\\"gem\\":\\"blue\\",\\"points\\":3,\\"id\\":48,\\"reqs\\":{\\"red\\":0,\\"green\\":0,\\"blue\\":6,\\"white\\":0,\\"black\\":0},\\"tier\\":2},{\\"gem\\":\\"red\\",\\"points\\":3,\\"id\\":60,\\"reqs\\":{\\"red\\":6,\\"green\\":0,\\"blue\\":0,\\"white\\":0,\\"black\\":0},\\"tier\\":2}],[{\\"gem\\":\\"black\\",\\"points\\":4,\\"id\\":84,\\"reqs\\":{\\"red\\":6,\\"green\\":3,\\"blue\\":0,\\"white\\":0,\\"black\\":3},\\"tier\\":3},{\\"gem\\":\\"blue\\",\\"points\\":4,\\"id\\":72,\\"reqs\\":{\\"red\\":0,\\"green\\":0,\\"blue\\":3,\\"white\\":6,\\"black\\":3},\\"tier\\":3},{\\"gem\\":\\"white\\",\\"points\\":4,\\"id\\":76,\\"reqs\\":{\\"red\\":3,\\"green\\":0,\\"blue\\":0,\\"white\\":3,\\"black\\":6},\\"tier\\":3},{\\"gem\\":\\"blue\\",\\"points\\":5,\\"id\\":82,\\"reqs\\":{\\"red\\":0,\\"green\\":0,\\"blue\\":3,\\"white\\":7,\\"black\\":0},\\"tier\\":3}]],\\"bank\\":{\\"red\\":4,\\"green\\":4,\\"blue\\":4,\\"white\\":4,\\"black\\":4,\\"gold\\":5},\\"decks\\":[36,26,16],\\"currentPlayer\\":1,\\"players\\":2}"}');
//    var gs = JSON.parse(data.gs);

    if (state.gameState == undefined) {
        return;
    }
    var gs = state.gameState;

    // Clean old elements from uiManager
    uiManager.elements = [];

    // Bank
    var offset = 10;
    for (var color in gs.bank) {
        var amt = gs.bank[color];
        var card = new Card({value: amt}, 10, offset, 75, 75);
        var img = new Picture('images/Splendor/' + color + '.png');
        card.setCardBack(img);
        (function (color) {
            card.handleMouseDoubleClick = function (x, y) {
                if (state.gemAction == 'takeGem' && color != 'gold') {
                    sendCmd({cmd: 'action', action: 'takeGem', args: [color, parseInt(state.gemIndex)]});
                }
                else {
                    sendCmd({cmd: 'action', action: 'discardGem', args: [color]});
                }
            };
        }(color));

        uiManager.addElement(card);

        offset += 85;
    }

    // Market
    offset = 10;
    for (var tier in gs.markets) {
        var market = gs.markets[tier];

        var offset2 = 150;
        for (var i = 0; i < 4; i++) {
            var card = new Card({}, offset2, offset, 160, 160);
            if (market[i] != null) {
                var img = new Picture('images/Splendor/' + market[i].id + '.png');
                card.setCardBack(img);
                (function (tier, index) {
                    card.handleMouseDoubleClick = function (x, y) {
                        sendCmd({cmd: 'action', action: 'buyCard', args: [tier, index]});
                    };
                    card.handleMouseRightClick = function (x, y) {
                        sendCmd({cmd: 'action', action: 'stashCard', args: [tier, index]});
                    };
                }(parseInt(tier), i));
            }

            uiManager.addElement(card);

            offset2 += 170;
        }

        var stats = {};
        stats.value = gs.decks[tier];
        stats.value3 = 'Tier ' + (parseInt(tier) + 1);
        stats.color = (tier == '0' ? 'DarkGreen' : tier == '1' ? 'GoldenRod' : 'Navy');
        var card = new Card(stats, offset2, offset, 160, 160);
        (function (tier) {
            card.handleMouseRightClick = function(x, y) {
                sendCmd({cmd: 'action', action: 'stashRandomCard', args: [tier]});
            };
        }(parseInt(tier)));
        uiManager.addElement(card);

        offset += 170;
    }

    // Players
    offset = 10;
    for (var i = 1; i <= gs.players; i++) {
        var player = gs['' + i];

        var offset2 = 1200;
        var playerName = new Label(offset2 - 135, offset + 35, 'P' + i);
        playerName.fontSize = '24pt';
        uiManager.addElement(playerName);

        var playerPoints = new Label(offset2 - 75, offset + 35, player.points + ' pts');
        playerPoints.fontSize = '24pt';
        uiManager.addElement(playerPoints);

        if (gs.currentPlayer == i) {
            var asterisk = new Label(offset2 - 150, offset + 40, '*');
            asterisk.fontSize = '24pt';
            uiManager.addElement(asterisk);
        }

        for (var color in player.bank) {
            var stats = {};
            stats.value = player.bank[color];
            stats.value2 = (player.prod[color] != undefined ? '+' + player.prod[color] : ' ');
            var card = new Card(stats, offset2, offset, 75, 75);
            var img = new Picture('images/Splendor/' + color + '.png');
            card.setCardBack(img);
            if (i == gs.me) {
                (function (color) {
                    card.handleMouseDoubleClick = function (x, y) {
                        if (state.gemAction == 'takeGem' && color != 'gold') {
                            sendCmd({cmd: 'action', action: 'takeGem', args: [color, parseInt(state.gemIndex)]});
                        }
                        else {
                            sendCmd({cmd: 'action', action: 'discardGem', args: [color]});
                        }
                    };
                }(color));
            }

            uiManager.addElement(card);

            offset2 += 85;
        }

        var reserveCards = new Label(offset2 + 10, offset + 35, player.stash.length + ' reserved');
        reserveCards.fontSize = '18pt';
        uiManager.addElement(reserveCards);

        offset += 85;
    }

    // Reserve
    var me = gs['' + gs.me];
    var reserve = new Label(1080, 420, 'Reserve:');
    reserve.fontSize = '24pt';
    uiManager.addElement(reserve);

    offset = 1200;
    for (var i = 0; i < me.stash.length; i++) {
        var card = new Card({}, offset, 350, 160, 160);
        var img = new Picture('images/Splendor/' + me.stash[i].id + '.png');
        card.setCardBack(img);
        (function (idx) {
            card.handleMouseDoubleClick = function(x, y) {
                sendCmd({cmd: 'action', action: 'buyReserveCard', args: [idx]});
            };
        }(i));

        uiManager.addElement(card);

        offset += 170;
    }
}

function handleActions() {
    var options = {};
    for (var i = 0; i < state.actions.length; i++) {
        var action = state.actions[i];

        var dash = action.indexOf('-');
        var dot = action.indexOf('.');
        var openParens = action.indexOf('(');
        var closeParens = action.indexOf(')');

        var player = action.substring(0, dash);
        var instance = action.substring(dash + 1, dot);
        var actionName = action.substring(dot + 1, openParens);
        var args = action.substring(openParens + 1, closeParens).split(', ');

        if (actionName == 'takeGem') {
            state.gemAction = actionName;
            state.gemIndex = args[1];
            if (state.gemIndex == '1') {
                options.takeGem = 'Double click a gem to take it.';
            }
            else if (state.gemIndex == '2') {
                options.takeGem = 'Double click a second gem to take it.';
            }
            else {
                options.takeGem = 'Double click a third gem to take it.';
            }
        }
        else if (actionName == 'stashCard' || actionName == 'stashRandomCard') {
            options.stashCard = 'Right click a card to reserve it.'
        }
        else if (actionName == 'buyCard' || actionName == 'buyReserveCard') {
            options.buyCard = 'Double click a card to buy it.'
        }
        else if (actionName == 'discardGem') {
            state.gemAction = actionName;
            options.discardGem = 'Double click a gem to discard it.'
        }
    }

    var messages = [];
    for (var key in options) {
        messages.push(options[key]);
    }

    var message = new Label(1080, 520, messages.join('  '));
    uiManager.addElement(message);
}
