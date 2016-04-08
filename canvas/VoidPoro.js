/// Code begins

function loadGameState() {
    if (state.gameState == undefined) {
        return;
    }
    console.log(state.gameState);

    uiManager.elements = [];
}

function handleActions() {
    console.log(state.actions);

    var label = new Label(10, 10, 'Select your champion');
    uiManager.addElement(label);

    var xPos = 10;
    for (var idx in state.actions) {
        var action = state.actions[idx];
        var image = new Picture(xPos, 33, 100, 100, 'icons/' + action.args[1] + '.png');
        image.handleMouseClick = makeSendCmd(action);
        uiManager.addElement(image);
        xPos += 110;
    }
    uiManager.addElement(ui.roomLabel);
    uiManager.addElement(ui.chatLabel);
    uiManager.addElement(ui.chatBox);
    uiManager.addElement(ui.messagesScrollArea);

    uiManager.dirty = true;
}

function makeSendCmd(action) {
    return function() {
        sendCmd({cmd: 'action', action: action.method, args: action.args});
    }
}