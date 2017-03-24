define([], function (Phaser) {
    'use strict';

    function Game(game) {
        this.game = game;
    }

    Game.prototype = {
        start: function() {
            this.game = new Phaser.Game('100', '100', Phaser.CANVAS);
        },

        preload: function() {
            this.game.load.image('logo', 'assets/phaser.png');
        },

        create: function() {
            var logo = this.game.add.sprite(this.game.world.centerX, this.game.world.centerY, 'logo');
            logo.anchor.setTo(0.5, 0.5);
        }
    };

    return Game;
});
