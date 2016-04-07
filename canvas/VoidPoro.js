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
}
