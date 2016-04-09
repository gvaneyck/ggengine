/// Code begins

function loadGameState() {
    if (state.gameState == undefined) {
        return;
    }
    console.log(state.gameState);

    mainContainer.elements = [];
}

function handleActions() {
    console.log(state.actions);

    var label = new Label(10, 10, 'Select your champion');
    mainContainer.addElement(label);

    var xPos = 10;
    for (var idx in state.actions) {
        var action = state.actions[idx];
        var image = new Picture(xPos, 33, 100, 100, 'icons/' + action.args[1] + '.png');
        image.handleMouseClick = makeSendCmd(action);
        mainContainer.addElement(image);
        xPos += 110;
    }
    mainContainer.addElement(ui.roomLabel);
    mainContainer.addElement(ui.chatLabel);
    mainContainer.addElement(ui.chatBox);
    mainContainer.addElement(ui.messagesScrollArea);

    mainContainer.dirty = true;
}

function makeSendCmd(action) {
    return function() {
        sendCmd({cmd: 'action', action: action.method, args: action.args});
        return true;
    }
}