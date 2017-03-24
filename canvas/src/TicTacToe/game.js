'use strict';

define([ 'phaser', 'phaserTextBox' ], function (Phaser) {

    function Game() {
    }

    Game.prototype.start = function() {
        this.game = new Phaser.Game('100', '100', Phaser.CANVAS, '');
        this.game.state.add('Game', this);
        this.game.state.start('Game');
    };

    Game.prototype.preload = function() {
        this.game.load.image('button', 'assets/button.png');
    };

    Game.prototype.create = function() {
        this.game.stage.backgroundColor = '#ffffff';

        this.username = this.game.world.add(new Phaser.TextBox(this.game, 100, 20, 200, '', { placeHolder: 'Username' }));
        this.password = this.game.world.add(new Phaser.TextBox(this.game, 100, 60, 200, '', { placeHolder: 'Password' }));
        this.password.onsubmit = this.login;

        this.button = this.game.add.image(226, 100, 'button');
        this.button.scale.setTo(0.03, 0.03);
        this.button.inputEnabled = true;
        this.button.input.useHandCursor = true;
        this.button.events.onInputUp.add(this.login, this);

        var style = { font: '14px Arial', fontWeight: 'bold' };
        this.game.add.text(20, 23, 'Username', style);
        this.game.add.text(20, 63, 'Password', style);
    };

    Game.prototype.login = function() {
        console.log(this.username.text);
        console.log(this.password.text);
    };

    return Game;
});
