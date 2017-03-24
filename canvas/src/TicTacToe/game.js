'use strict';

define([ 'phaser', 'CanvasInput', 'phaserTextBox' ], function (Phaser) {

    function Game() {
    }

    Game.prototype.start = function() {
        this.game = new Phaser.Game('100', '100', Phaser.CANVAS, '');
        this.game.state.add('Game', this);
        this.game.state.start('Game');
    };

    Game.prototype.create = function() {
        // this.makeInput(20, 20, 400, 20);
        this.game.world.add(new Phaser.TextBox(this.game, 20, 20, 200, 'foo', { placeHolder: 'bar' }));

        var text = '- phaser -\n with a sprinkle of \n pixi dust.';
        var style = { font: '65px Arial', fill: '#ff0044', align: 'center' };
        this.game.add.text(this.game.world.centerX-300, 0, text, style);
    };

    Game.prototype.makeInput = function(x, y, width, fontSize) {
        var padding =  Math.floor(fontSize / 4);
        var height = fontSize + padding * 2 + 1;
        var inputWidth = width - padding * 2 - 1;

        var bitmap = this.game.add.bitmapData(width, height);
        var sprite = this.game.add.sprite(x, y, bitmap);

        sprite.canvasInput = new CanvasInput({
            canvas: bitmap.canvas,
            fontSize: fontSize,
            fontFamily: 'Arial',
            fontColor: '#212121',
            width: inputWidth,
            padding: padding,
            borderWidth: 1,
            borderColor: '#000',
            borderRadius: 1,
            boxShadow: '0px 0px 0px #fff',
            innerShadow: '0px 0px 5px rgba(0, 0, 0, 0.5)',
            placeHolder: 'Enter message here...'
        });
        sprite.inputEnabled = true;
        sprite.input.useHandCursor = true;
        // myInput.events.onInputUp.add(function(sprite) { sprite.canvasInput.focus(); }, myInput);
        sprite.events.onInputUp.add(this.inputFocus, sprite);
    };

    Game.prototype.inputFocus = function(sprite) {
        sprite.canvasInput.focus();
    };

    return Game;
});
