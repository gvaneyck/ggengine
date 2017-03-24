'use strict';

Phaser.TextBox = function(game, x, y, width, text, options) {
    // TODO: Properly handle scaling (see _res in Phaser.Text)
    x = x || 0;
    y = y || 0;
    width = width || 200;

    if (text === undefined || text === null) {
        text = '';
    } else {
        text = text.toString();
    }

    this.type = Phaser.TextBox;
    this.physicsType = Phaser.SPRITE;
    this._padding = options.padding || 3;

    this._xCanvas1 = Phaser.CanvasPool.create(this);
    this._xCanvas2 = Phaser.CanvasPool.create(this);

    Phaser.Sprite.call(this, game, x, y, PIXI.Texture.fromCanvas(this._xCanvas1));

    this._canvasInput = new CanvasInput({
        value: text,
        renderCanvas: this._xCanvas1,
        shadowCanvas: this._xCanvas2,
        padding: this._padding,
        width: width - this._padding * 2 - 2,
        fontSize: options.fontSize,
        fontFamily: options.fontFamily,
        fontWeight: options.fontWeight,
        fontStyle: options.fontStyle,
        placeHolder: options.placeHolder,
        borderColor: '#000',
        boxShadow: '0px 0px 0px rgba(255, 255, 255, 1)'
    });

    // Enable input
    this.inputEnabled = true;
    this.input.useHandCursor = true;
    this.events.onInputUp.add(function(sprite) { sprite._canvasInput.focus(); }, this);

    // Override render to only render if dirty is true
    var self = this;
    this.dirty = false;
    this._renderInternal = this._canvasInput.render;
    this._canvasInput.render = function() { self.dirty = true; };

    // Fix cursor blink behavior
    this._canvasInput.onkeydown(function() {
        var self = this;
        self._cursor = true;
        clearInterval(self._cursorInterval);
        self._cursorInterval = setInterval(function() {
            self._cursor = !self._cursor;
            self.render();
        }, 500)
    });
};

Phaser.TextBox.prototype = Object.create(Phaser.Sprite.prototype);
Phaser.TextBox.prototype.constructor = Phaser.TextBox;

Phaser.TextBox.prototype.destroy = function(destroyChildren) {
    // TODO: Make sure all canvases are returned to the pool
    // TODO: Make sure CanvasInput cleans up itself well (doesn't double destroy anything, cleans up events)
    this.texture.destroy(true);
    this._canvasInput.destroy();
    Phaser.Component.Destroy.prototype.destroy.call(this, destroyChildren);
};

Phaser.TextBox.prototype._renderWebGL = function(renderSession) {
    if (this.dirty) {
        this._renderInternal.call(this._canvasInput);
        this.dirty = false;
    }

    PIXI.Sprite.prototype._renderWebGL.call(this, renderSession);

};

Phaser.TextBox.prototype._renderCanvas = function(renderSession) {
    if (this.dirty) {
        this._renderInternal.call(this._canvasInput);
        this.dirty = false;
    }

    PIXI.Sprite.prototype._renderCanvas.call(this, renderSession);

};

Object.defineProperty(Phaser.TextBox.prototype, 'text', {
    get: function() {
        return this._canvasInput.value();
    },
    set: function(value) {
        this._canvasInput.value(value);
    }
});

Object.defineProperty(Phaser.TextBox.prototype, 'width', {
    get: function() {
        return this._canvasInput.width() + this._padding * 2 + 2;
    },
    set: function(value) {
        this._canvasInput.width(value - this._padding * 2 - 2);
    }
});

Object.defineProperty(Phaser.TextBox.prototype, 'padding', {
    get: function() {
        return this._padding;
    },
    set: function(value) {
        this._padding = value;
        this._canvasInput.padding(value);
        this._canvasInput.width(this._canvasInput.width() - this._padding * 2 - 2);
    }
});

Object.defineProperty(Phaser.TextBox.prototype, 'fontSize', {
    get: function() {
        return this._canvasInput.fontSize();
    },
    set: function(value) {
        this._canvasInput.fontSize(value);
    }
});

Object.defineProperty(Phaser.TextBox.prototype, 'fontFamily', {
    get: function() {
        return this._canvasInput.fontFamily();
    },
    set: function(value) {
        this._canvasInput.fontFamily(value);
    }
});

Object.defineProperty(Phaser.TextBox.prototype, 'fontWeight', {
    get: function() {
        return this._canvasInput.fontWeight();
    },
    set: function(value) {
        this._canvasInput.fontWeight(value);
    }
});

Object.defineProperty(Phaser.TextBox.prototype, 'fontStyle', {
    get: function() {
        return this._canvasInput.fontStyle();
    },
    set: function(value) {
        this._canvasInput.fontStyle(value);
    }
});

Object.defineProperty(Phaser.TextBox.prototype, 'placeHolder', {
    get: function() {
        return this._canvasInput.placeHolder();
    },
    set: function(value) {
        this._canvasInput.placeHolder(value);
    }
});
