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
        xPos += 110;
        uiManager.addElement(image);
    }

    uiManager.dirty = true;
}
