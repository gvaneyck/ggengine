function loadGameState() {
    if (state.gameState == undefined) {
        return;
    }
    console.log(state.gameState);

    // mainContainer.elements = [];
}

function handleActions() {
    console.log(state.actions);

    // mainContainer.addElement(ui.roomLabel);
    // mainContainer.addElement(ui.chatLabel);
    // mainContainer.addElement(ui.chatBox);
    // mainContainer.addElement(ui.messagesScrollArea);
    // mainContainer.dirty = true;
}

function makeSendCmd(action) {
    return function() {
        sendCmd({cmd: "action", action: action.method, args: action.args});
        return true;
    }
}
