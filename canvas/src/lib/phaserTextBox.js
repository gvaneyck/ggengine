'use strict';

Phaser.TextBox = function(game, x, y, width, text, options) {

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
    // TODO: Destroy CanvasInput
    // TODO: Make sure CanvasInput cleans up itself well (doesn't double destroy anything, cleans up events)
    this.texture.destroy(true);
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

// Object.defineProperty(Phaser.TextBox.prototype, 'text', {
//
//     get: function() {
//         return this._text;
//     },
//
//     set: function(value) {
//
//         if (value !== this._text)
//         {
//             this._text = value.toString() || '';
//             this.dirty = true;
//
//             if (this.parent)
//             {
//                 this.updateTransform();
//             }
//         }
//
//     }
//
// });
//
// Object.defineProperty(Phaser.TextBox.prototype, 'cssFont', {
//
//     get: function() {
//         return this.componentsToFont(this._fontComponents);
//     },
//
//     set: function(value)
//     {
//         value = value || 'bold 20pt Arial';
//         this._fontComponents = this.fontToComponents(value);
//         this.updateFont(this._fontComponents);
//     }
//
// });
//
// Object.defineProperty(Phaser.TextBox.prototype, 'font', {
//
//     get: function() {
//         return this._fontComponents.fontFamily;
//     },
//
//     set: function(value) {
//
//         value = value || 'Arial';
//         value = value.trim();
//
//         // If it looks like the value should be quoted, but isn't, then quote it.
//         if (!/^(?:inherit|serif|sans-serif|cursive|fantasy|monospace)$/.exec(value) && !/['",]/.exec(value))
//         {
//             value = "'" + value + "'";
//         }
//
//         this._fontComponents.fontFamily = value;
//         this.updateFont(this._fontComponents);
//
//     }
//
// });
//
// Object.defineProperty(Phaser.TextBox.prototype, 'fontSize', {
//
//     get: function() {
//
//         var size = this._fontComponents.fontSize;
//
//         if (size && /(?:^0$|px$)/.exec(size))
//         {
//             return parseInt(size, 10);
//         }
//         else
//         {
//             return size;
//         }
//
//     },
//
//     set: function(value) {
//
//         value = value || '0';
//
//         if (typeof value === 'number')
//         {
//             value = value + 'px';
//         }
//
//         this._fontComponents.fontSize = value;
//         this.updateFont(this._fontComponents);
//
//     }
//
// });
//
// Object.defineProperty(Phaser.TextBox.prototype, 'fontWeight', {
//
//     get: function() {
//         return this._fontComponents.fontWeight || 'normal';
//     },
//
//     set: function(value) {
//
//         value = value || 'normal';
//         this._fontComponents.fontWeight = value;
//         this.updateFont(this._fontComponents);
//
//     }
//
// });
//
// Object.defineProperty(Phaser.TextBox.prototype, 'fontStyle', {
//
//     get: function() {
//         return this._fontComponents.fontStyle || 'normal';
//     },
//
//     set: function(value) {
//
//         value = value || 'normal';
//         this._fontComponents.fontStyle = value;
//         this.updateFont(this._fontComponents);
//
//     }
//
// });
//
// Object.defineProperty(Phaser.TextBox.prototype, 'fontVariant', {
//
//     get: function() {
//         return this._fontComponents.fontVariant || 'normal';
//     },
//
//     set: function(value) {
//
//         value = value || 'normal';
//         this._fontComponents.fontVariant = value;
//         this.updateFont(this._fontComponents);
//
//     }
//
// });
//
// Object.defineProperty(Phaser.TextBox.prototype, 'fill', {
//
//     get: function() {
//         return this.style.fill;
//     },
//
//     set: function(value) {
//
//         if (value !== this.style.fill)
//         {
//             this.style.fill = value;
//             this.dirty = true;
//         }
//
//     }
//
// });
//
// Object.defineProperty(Phaser.TextBox.prototype, 'align', {
//
//     get: function() {
//         return this.style.align;
//     },
//
//     set: function(value) {
//
//         if (value !== this.style.align)
//         {
//             this.style.align = value;
//             this.dirty = true;
//         }
//
//     }
//
// });
//
// Object.defineProperty(Phaser.TextBox.prototype, 'stroke', {
//
//     get: function() {
//         return this.style.stroke;
//     },
//
//     set: function(value) {
//
//         if (value !== this.style.stroke)
//         {
//             this.style.stroke = value;
//             this.dirty = true;
//         }
//
//     }
//
// });
//
// Object.defineProperty(Phaser.TextBox.prototype, 'strokeThickness', {
//
//     get: function() {
//         return this.style.strokeThickness;
//     },
//
//     set: function(value) {
//
//         if (value !== this.style.strokeThickness)
//         {
//             this.style.strokeThickness = value;
//             this.dirty = true;
//         }
//
//     }
//
// });
//
// Object.defineProperty(Phaser.TextBox.prototype, 'width', {
//
//     get: function() {
//
//         if (this.dirty)
//         {
//             this.updateText();
//             this.dirty = false;
//         }
//
//         return this.scale.x * (this.texture.frame.width / this.resolution);
//     },
//
//     set: function(value) {
//
//         this.scale.x = value / this.texture.frame.width;
//         this._width = value;
//     }
//
// });
//
