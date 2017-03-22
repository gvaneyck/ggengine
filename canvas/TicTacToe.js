function gameStateHandler(gameState) {
    console.log(gameState);

    // mainContainer.elements = [];
}

function actionHandler(actions) {
    console.log(actions);

    // mainContainer.addElement(ui.roomLabel);
    // mainContainer.addElement(ui.chatLabel);
    // mainContainer.addElement(ui.chatBox);
    // mainContainer.addElement(ui.messagesScrollArea);
    // mainContainer.dirty = true;
}

function makeSendCmd(action, args) {
    return function() {
        sendCmd({cmd: "action", action: action, args: args});
        return true;
    }
}
