var Login = function() {};
Login.prototype = {
    preload: function() { },
    create: function() { },
    update: function() { },
    render: function() { }
};

var Lobby = function() {};
Lobby.prototype = {
    preload: function() { },
    create: function() { },
    update: function() { },
    render: function() { }
};

var TicTacToe = function() {};
TicTacToe.prototype = {
    preload: function() { },
    create: function() { },
    update: function() { },
    render: function() { }
};

var config = {
    width: 800,
    height: 600,
    renderer: Phaser.CANVAS,
    antialias: true,
    multiTexture: true
};
var game = new Phaser.Game(config);
game.state.add('Login', login);
game.state.add('MainLobby', mainLobby);
game.state.add('Game', gameLobby);
game.state.start('Login');

function preload() {
}

function create() {

    game.stage.backgroundColor = 0x5d5d5d;

    sprite = game.add.sprite(200, 200, 'pic');
    sprite.inputEnabled = true;
    sprite.input.enableDrag();

    var style = { font: "32px Arial", fill: "#ff0044", wordWrap: true, wordWrapWidth: sprite.width, align: "center", backgroundColor: "#ffff00" };

    text = game.add.text(0, 0, "- text on a sprite -\ndrag me", style);
    text.anchor.set(0.5);

}

function update() {

    text.x = Math.floor(sprite.x + sprite.width / 2);
    text.y = Math.floor(sprite.y + sprite.height / 2);

}
