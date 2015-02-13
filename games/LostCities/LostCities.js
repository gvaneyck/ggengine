/// UI classes

// Base class

function UIElement(context, x, y, width, height) {
    this.context = context;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.focus = false;
    this.visible = true;
}

UIElement.prototype.draw = function() { };
UIElement.prototype.handleKey = function(e) { return false; };

UIElement.prototype.handleClick = function(xy) {
    this.focus = this.isClicked(xy);
    return this.focus
};

UIElement.prototype.isClicked = function(xy) {
    return (this.x <= xy.x
        && this.y <= xy.y
        && this.x + this.width >= xy.x
        && this.y + this.height >= xy.y);
};

// Label

function Label(context, x, y, text) {
    context.font = '12pt Calibri';
    var measure = context.measureText(text);
    UIElement.call(this, context, x, y, measure.width, 12);
    this.text = text;
}

Label.prototype = Object.create(UIElement.prototype);
Label.prototype.constructor = Label;

Label.prototype.draw = function() {
    this.context.font = '12pt Calibri';
    this.context.fillText(this.text, this.x, this.y + 12);
};

// Textbox

function Textbox(context, x, y, width, height) {
    UIElement.call(this, context, x, y, width, height);
    this.text = '';
    this.cursorPos = 0;
    this.submitHandler = new function(msg) {};
}

Textbox.prototype = Object.create(UIElement.prototype);
Textbox.prototype.constructor = Textbox;

Textbox.prototype.handleKey = function(e) {
    var handled = false;
    if (e.charCode != 0) {
        handled = true;
        var keyPressed = String.fromCharCode(e.charCode);
        this.text = this.text.substring(0, this.cursorPos) + keyPressed + this.text.substring(this.cursorPos);
        this.cursorPos++;
    }
    else {
        // TODO e.ctrlKey
        if (e.keyCode == 8) { // Backspace
            handled = true;
            if (this.cursorPos > 0) {
                this.text = this.text.substring(0, this.cursorPos - 1) + this.text.substring(this.cursorPos);
                this.cursorPos--;
            }
        }
        else if (e.keyCode == 46) { // Delete
            handled = true;
            if (this.cursorPos < this.text.length) {
                this.text = this.text.substring(0, this.cursorPos) + this.text.substring(this.cursorPos + 1);
            }
        }
        else if (e.keyCode == 37) { // Left
            handled = true;
            if (this.cursorPos > 0) {
                this.cursorPos--;
            }
        }
        else if (e.keyCode == 39) { // Right
            handled = true;
            if (this.cursorPos < this.text.length) {
                this.cursorPos++;
            }
        }
        else if (e.keyCode == 13) { // Enter
            handled = true;
            this.submitHandler(this.text);
            this.cursorPos = 0;
            this.text = '';
        }
    }

    if (handled) {
        e.preventDefault();
    }
    return handled;
};

Textbox.prototype.draw = function() {
    this.context.beginPath();

    this.context.rect(this.x, this.y, this.width, this.height);

    this.context.font = '12pt Calibri';
    this.context.fillText(this.text, this.x + 3, this.y + this.height - 3);

    if (this.focus) {
        var cursorXpos = this.x + this.context.measureText(this.text.substring(0, this.cursorPos)).width + 3;
        this.context.moveTo(cursorXpos, this.y + 3);
        this.context.lineTo(cursorXpos, this.y + this.height - 3);
    }

    this.context.strokeStyle = 'black';
    this.context.stroke();
};

/// Code begins

var state = 'NAME';

var uiElements = [ ];

var lobbies = {};
var chats = [];

var fullWidth = 640;
var fullHeight = 480;

var gCanvasElement;
var gContext;

function initGame(canvasElement) {
    gCanvasElement = canvasElement;
    gCanvasElement.addEventListener("click", clickHandler, false);
    document.addEventListener("keypress", typingHandler, false);
    gContext = gCanvasElement.getContext("2d");
    sizeWindow();

    var nameLabel = new Label(gContext, 10, 13, 'Enter nickname: ');
    var nameBox = new Textbox(gContext, nameLabel.width + 10, 10, 200, 20);
    nameBox.submitHandler = function(msg) {
        sendCmd({cmd: 'setName', name: msg});
    };

    var chatBox = new Textbox(gContext, 10, 10, 200, 20);
    chatBox.visible = false;
    chatBox.submitHandler = function(msg) {
        sendCmd({cmd: 'msg', msg: msg, target: 'General'});
    };

    uiElements.push(nameLabel, nameBox, chatBox);

    draw();

    window.onresize = function (event) {
        sizeWindow();
    };

    openWebSocket();
}

function clickHandler(e) {
    var xy = getCursorPosition(e);
    for (var i = 0; i < uiElements.length; i++) {
        var element = uiElements[i];
        if (element.visible) {
            var handled = element.handleClick(xy);
            if (handled) {
                break;
            }
        }
    }
    draw();
}

function typingHandler(e) {
    for (var i = 0; i < uiElements.length; i++) {
        var element = uiElements[i];
        if (element.visible && element.focus) {
            var handled = element.handleKey(e);
            if (handled) {
                break;
            }
        }
    }
    draw();
}

function getCursorPosition(e) {
    var data = {};
    if (e.pageX != undefined && e.pageY != undefined) {
        data.x = e.pageX;
        data.y = e.pageY;
    }
    else {
        data.x = e.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
        data.y = e.clientY + document.body.scrollTop + document.documentElement.scrollTop;
    }
    data.x -= gCanvasElement.offsetLeft;
    data.y -= gCanvasElement.offsetTop;
    data.x = Math.min(data.x, fullWidth);
    data.y = Math.min(data.y, fullHeight);
    return data;
}

function draw() {
    gContext.clearRect(0, 0, fullWidth, fullHeight);

    for (var i = 0; i < uiElements.length; i++) {
        var element = uiElements[i];
        if (element.visible) {
            element.draw();
        }
    }

    gContext.font = '12pt Calibri';
    var yPos = 50;
    for (var i = 0; i < chats.length; i++) {
        var chat = chats[i];
        var date = new Date(chat.time);
        var dateString = date.getHours()
            + ':'
            + (date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes())
            + ':'
            + (date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds());
        var message = dateString + ' ' + chat.from + ' (to ' + chat.to + '): ' + chat.msg;
        gContext.fillText(message, 10, yPos);
        yPos += 20;
    }
}

function sizeWindow() {
    gCanvasElement.width = window.innerWidth;
    gCanvasElement.height = window.innerHeight;
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
    console.log("CONNECTED");
}

function onClose(evt) {
    console.log("DISCONNECTED");
}

function onMessage(evt) {
    var cmd = JSON.parse(evt.data);
    if (cmd.cmd == 'nameSelected') {
        if (cmd.success) {

        }
        else {

        }
    }
    else if (cmd.cmd == 'lobbyList') {
        for (var name in cmd.data) {
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
            lobby.members.remove(cmd.member);
        }
    }
    else if (cmd.cmd == 'chat') {
        chats.push(cmd);
        draw();
    }
    console.log("MSG: " + evt.data);
}

function onError(evt) {
    console.log('ERROR: ' + evt.data);
}

function sendCmd(cmd) {
    websocket.send(JSON.stringify(cmd));
}
