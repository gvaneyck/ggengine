'use strict';

(function() {
    requirejs.config({
        paths: {
            CanvasInput: 'lib/CanvasInput',
            phaser: 'lib/phaser.min',
            phaserTextBox: 'lib/phaserTextBox',
            game: 'TicTacToe/game'
        },
        shim: {
            'CanvasInput': {
                exports: 'CanvasInput'
            },
            'phaser': {
                exports: 'Phaser'
            },
            'phaserTextBox': {
                deps: ['CanvasInput', 'phaser'],
                exports: 'Phaser'
            }
        }
    });

    require(['game'], function (Game) {
        var game = new Game();
        game.start();
    });
}());
