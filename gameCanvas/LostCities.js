/// Code begins

var isMouseDown = false;
var lastMousePos = { };
var lastClick = { element: null, time: 0 };

var state = 'NAME';
var name;

var lobbies = {};

var fullWidth = 640;
var fullHeight = 480;

var gCanvasElement;
var gContext;

var uiElements = [ ];

var errLabel;
var nameLabel;
var nameBox;
var chatBox;
var chatArea;
var chatArea2;

function initGame(canvasElement) {
    window.onresize = function (e) { sizeWindow(); };

    gCanvasElement = canvasElement;
    document.addEventListener('mousedown', mouseDownHandler);
    document.addEventListener('mousemove', mouseMoveHandler);
    document.addEventListener('mouseup', mouseUpHandler);
    document.addEventListener('wheel', mouseWheelHandler, false);
    document.addEventListener('keypress', typingHandler);
    gContext = gCanvasElement.getContext('2d');
    sizeWindow();

    errLabel = new Label(gContext, 10, 33, '');

    nameLabel = new Label(gContext, 10, 13, 'Enter nickname: ');
    nameBox = new Textbox(gContext, nameLabel.width + 10, 10, 200, 20);
    nameBox.submitHandler = function(msg) {
        sendCmd({cmd: 'setName', name: msg});
    };

    chatBox = new Textbox(gContext, 10, 10, 200, 20);
    chatBox.visible = false;
    chatBox.submitHandler = function(msg) {
        sendCmd({cmd: 'msg', msg: msg, target: 'General'});
    };
    chatArea = new Label(gContext, 10, 38, '');
    chatArea2 = new ScrollArea(gContext, 10, 38, 400, 100, chatArea);
    chatArea2.visible = false;

    uiElements.push(errLabel, nameLabel, nameBox, chatBox, chatArea2);

    draw();

    openWebSocket();
}

function mouseDownHandler(e) {
    e.preventDefault();
    var xy = getCursorPosition(e);

    isMouseDown = true;
    lastMousePos = xy;

    var dirty = false;
    for (var i = 0; i < uiElements.length; i++) {
        var element = uiElements[i];
        if (element.visible && isClicked(xy, element)) {
            if (element.setFocus(true)) { dirty = true; }
            if (element.handleMouseDown(xy)) { dirty = true; }
        }
        else {
            if (element.setFocus(false)) { dirty = true; }
        }
    }

    if (dirty) {
        draw();
    }
}

function mouseMoveHandler(e) {
    if (isMouseDown) {
        var xy = getCursorPosition(e);
        for (var i = 0; i < uiElements.length; i++) {
            var element = uiElements[i];
            if (element.visible && element.focus) {
                e.preventDefault();
                var xDelta = xy.x - lastMousePos.x;
                var yDelta = xy.y - lastMousePos.y;
                var dirty = element.handleMouseDrag(xDelta, yDelta);
                if (dirty) {
                    draw();
                }
                break;
            }
        }
        lastMousePos = xy;
    }
}

function mouseUpHandler(e) {
    isMouseDown = false;
    var xy = getCursorPosition(e);
    for (var i = 0; i < uiElements.length; i++) {
        var element = uiElements[i];
        if (element.visible && element.focus && isClicked(xy, element)) {
            e.preventDefault();
            var now = new Date().getTime();
            var dirty = false;
            if (lastClick.element == element && now - lastClick.time < 500) {
                dirty = element.handleMouseDoubleClick(xy);
                lastClick.element = null;
            }
            else {
                dirty = element.handleMouseClick(xy);
                lastClick.element = element;
                lastClick.time = now;
            }

            if (dirty) {
                draw();
            }

            break;
        }
    }
}

function mouseWheelHandler(e) {
    for (var i = 0; i < uiElements.length; i++) {
        var element = uiElements[i];
        if (element.visible && element.focus) {
            e.preventDefault();
            var dirty = element.handleMouseWheel(e.deltaY);
            if (dirty) {
                draw();
            }
            break;
        }
    }
}

function typingHandler(e) {
    for (var i = 0; i < uiElements.length; i++) {
        var element = uiElements[i];
        if (element.visible && element.focus) {
            var dirty = element.handleKey(e);
            if (dirty) {
                e.preventDefault();
                draw();
            }
            break;
        }
    }
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
}

function sizeWindow() {
    fullWidth = gCanvasElement.width = window.innerWidth;
    fullHeight = gCanvasElement.height = window.innerHeight;
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
    draw();
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
