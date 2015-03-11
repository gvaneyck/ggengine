// Big TODOs
// - Implement hover
// - Split up xy into x and y
// - Label - Copy text, links, emoji, onHover card images
// - Text box - control key, shift selection, copy/paste, don't scroll left until off left side
// - Authentication/Sessions (cookie + server)
// - Send action to clients when chosen for logging
// - Auto-pick single actions
// - Don't send gamestate if it hasn't changed for a player (hide decision timing)
// - Deal with half pixels
// - requestAnimationFrame instead of draw loop, http://www.paulirish.com/2011/requestanimationframe-for-smart-animating/
// - Redo temp context shenanigans in resize to actually create an element and destroy it on draw
// - Reload game state on resize
// - Browser compatibility/jQuery handlers/Mobile
// - Linking in game elements -> via drag or shift/ctrl click
// - Dirty rectangles drawing
// - Voice chat?


// Event handlers:
// handleMouseDown = Hit element + Mouse down (automatically gain focus)
// handleMouseDrag = Focused element + Mouse move while mouse is down
// handleMouseUp = Focused element + Mouse up
// handleMouseClick = Hit element + Focused element + Mouse up
// handleMouseDoubleClick = Hit element + Focused element + 2x Mouse up within interval
// handleMouseWheel = Focused element + Mouse scroll
// handleKey = Focused element + Key press


/// Helper functions ///

function isClicked(xy, element) {
    return (element.x <= xy.x
        && element.y <= xy.y
        && element.x + element.width >= xy.x
        && element.y + element.height >= xy.y);
}

function getCursorPosition(e, bounds) {
    var data = {};
    if (e.pageX != undefined && e.pageY != undefined) {
        data.x = e.pageX;
        data.y = e.pageY;
    }
    else {
        data.x = e.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
        data.y = e.clientY + document.body.scrollTop + document.documentElement.scrollTop;
    }
    data.x = Math.min(data.x, bounds.width);
    data.y = Math.min(data.y, bounds.height);
    return data;
}


/// UI Manager ///
function UIManager(canvas) {
    this.canvas = canvas;
    this.elements = [];
    this.dirty = false;
    this.sizeWindow();

    // State vars
    this.isMouseDown = false;
    this.lastMousePos = { };
    this.lastClick = { element: null, time: 0 };

    // Attach events
    var _this = this;
    document.addEventListener('mousedown', function(e) { _this.sortElements(); _this.mouseDownHandler.call(_this, e); });
    document.addEventListener('mousemove', function(e) { _this.sortElements(); _this.mouseMoveHandler.call(_this, e); });
    document.addEventListener('mouseup', function(e) { _this.sortElements(); _this.mouseUpHandler.call(_this, e); });
    document.addEventListener('wheel', function(e) { _this.sortElements(); _this.mouseWheelHandler.call(_this, e); }, false);
    document.addEventListener('keypress', function(e) { _this.sortElements(); _this.typingHandler.call(_this, e); });
    window.onresize = function(e) { _this.sizeWindow.call(_this, e); };

    // Start draw loop
    var fps = 60;
    setInterval(function() {
        _this.draw.call(_this);
    }, 1000 / fps);
}

UIManager.prototype.scratchPad = document.createElement('canvas').getContext('2d');

UIManager.prototype.sizeWindow = function(e) {
    // Draw current canvas to temp canvas
    this.scratchPad.drawImage(this.canvas, 0, 0);

    // Resize current canvas
    this.canvas.width = window.innerWidth;
    this.canvas.height = window.innerHeight;

    // Draw temp canvas back to the current canvas
    this.canvas.getContext('2d').drawImage(this.scratchPad.canvas, 0, 0);
    this.dirty = true;
};

UIManager.prototype.draw = function() {
    if (!this.dirty) { return; }

    this.sortElements();

    var context = this.canvas.getContext('2d');
    context.clearRect(0, 0, this.canvas.width, this.canvas.height);

    for (var i = 0; i < this.elements.length; i++) {
        var element = this.elements[i];
        if (element.visible) {
            element.draw(context);
        }
    }

    this.dirty = false;
};

UIManager.prototype.sortElements = function() {
    // Sort by z order
    this.elements.sort(function(a, b) {
        return a.getZLevel() - b.getZLevel();
    });
};

UIManager.prototype.addElement = function(element) {
    var startIdx = this.elements.length;
    this.elements.push(element);
    this.elements = this.elements.concat(element.getChildren());
    element.scratchPad = this.scratchPad;
    this.dirty = true;
};


UIManager.prototype.mouseDownHandler = function(e) {
    e.preventDefault();
    var xy = getCursorPosition(e, this.canvas);

    this.isMouseDown = true;
    this.lastMousePos = xy;

    var handled = false;
    for (var i = this.elements.length - 1; i >= 0; i--) {
        var element = this.elements[i];
        if (!handled && element.visible && isClicked(xy, element)) {
            if (element.setFocus(true)) { this.dirty = true; }
            if (element.handleMouseDown(xy)) { this.dirty = true; }
            handled = true;
        }
        else {
            if (element.setFocus(false)) { this.dirty = true; }
        }
    }
};

UIManager.prototype.mouseMoveHandler = function(e) {
    if (this.isMouseDown) {
        var xy = getCursorPosition(e, this.canvas);
        for (var i = this.elements.length - 1; i >= 0; i--) {
            var element = this.elements[i];
            if (element.visible && element.focus) {
                e.preventDefault();
                var xDelta = xy.x - this.lastMousePos.x;
                var yDelta = xy.y - this.lastMousePos.y;
                if (element.handleMouseDrag(xy, xDelta, yDelta)) { this.dirty = true; }
                break;
            }
        }
        this.lastMousePos = xy;
    }
};

UIManager.prototype.mouseUpHandler = function(e) {
    this.isMouseDown = false;
    var xy = getCursorPosition(e, this.canvas);
    for (var i = this.elements.length - 1; i >= 0; i--) {
        var element = this.elements[i];
        var handled = false;
        if (element.visible && element.focus) {
            if (element.handleMouseUp(xy)) { this.dirty = true; }
            handled = true;
        }
        if (element.visible && element.focus && isClicked(xy, element)) {
            var now = new Date().getTime();
            if (this.lastClick.element == element && now - this.lastClick.time < 500) {
                if (element.handleMouseDoubleClick(xy)) { this.dirty = true; }
                this.lastClick.element = null;
            }
            else {
                if (element.handleMouseClick(xy)) { this.dirty = true; }
                this.lastClick.element = element;
                this.lastClick.time = now;
            }
            handled = true;
        }
        if (handled) {
            e.preventDefault();
            break;
        }
    }
};

UIManager.prototype.mouseWheelHandler = function(e) {
    for (var i = this.elements.length - 1; i >= 0; i--) {
        var element = this.elements[i];
        if (element.visible && element.focus) {
            e.preventDefault();
            if (element.handleMouseWheel(e.deltaY)) { this.dirty = true; }
            break;
        }
    }
};

UIManager.prototype.typingHandler = function(e) {
    for (var i = this.elements.length - 1; i >= 0; i--) {
        var element = this.elements[i];
        if (element.visible && element.focus) {
            if (element.handleKey(e)) {
                e.preventDefault();
                this.dirty = true;
            }
            break;
        }
    }
};


/// Base class ///

function UIElement(x, y, width, height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.zLevel = 0;
    this.focus = false;
    this.visible = true;
}

UIElement.prototype.getChildren = function() { return []; };
UIElement.prototype.getZLevel = function() { return this.zLevel; };
UIElement.prototype.draw = function(context) { };
UIElement.prototype.setFocus = function(focus) { this.focus = focus; return false; };
UIElement.prototype.handleMouseDown = function(xy) { return false; };
UIElement.prototype.handleMouseDrag = function(xy, xDelta, yDelta) { return false; };
UIElement.prototype.handleMouseUp = function(xy) { return false; };
UIElement.prototype.handleMouseClick = function(xy) { return false; };
UIElement.prototype.handleMouseDoubleClick = function(xy) { return false; };
UIElement.prototype.handleMouseWheel = function(yDelta) { return false; };
UIElement.prototype.handleKey = function(e) { return false; };


/// Label ///

function Label(x, y, text) {
    UIElement.call(this, x, y, 0, 0);
    this.onSizeChange = function() { };
    this.setText(text);
}

Label.prototype = Object.create(UIElement.prototype);
Label.prototype.constructor = Label;

Label.prototype.setText = function(text) {
    this.scratchPad.font = '12pt Calibri';
    var maxWidth = 0;
    var height = -6; // Remove padding
    var lines = text.split("\n");
    for (var i = 0; i < lines.length; i++) {
        var line = lines[i];
        var measure = this.scratchPad.measureText(line);
        maxWidth = Math.max(maxWidth, measure.width);
        height += 20;
    }

    this.width = maxWidth;
    this.height = height;

    this.text = text;

    this.onSizeChange();
};

Label.prototype.draw = function(context) {
    context.font = '12pt Calibri';
    var lines = this.text.split("\n");
    var yPos = this.y + 12;
    for (var i = 0; i < lines.length; i++) {
        var line = lines[i];
        context.fillText(line, this.x, yPos);
        yPos += 20;
    }
};


/// FixedWidthLabel ///

function FixedWidthLabel(x, y, width, text) {
    Label.call(this, x, y, '');
    this.width = width;
    this.setText(text);
}

FixedWidthLabel.prototype = Object.create(Label.prototype);
FixedWidthLabel.prototype.constructor = FixedWidthLabel;

FixedWidthLabel.prototype.setText = function(text) {
    var height = -6; // Remove padding

    var lines = text.split("\n");
    var newLines = [];
    for (var i = 0; i < lines.length; i++) {
        var line = lines[i];
        var fittedText = this.fitText(line, this.width);
        height += 20 * fittedText.length;
        newLines = newLines.concat(fittedText);
    }

    this.text = newLines.join("\n");
    this.height = height;
    this.onSizeChange();
};

FixedWidthLabel.prototype.fitText = function(text, width) {
    this.scratchPad.font = '12pt Calibri';
    var fittedText = [];
    var words = text.split(' ');

    var line = '';
    for (var n = 0; n < words.length; n++) {
        var testLine = line + words[n];
        var lineWidth = this.scratchPad.measureText(testLine).width;
        if (lineWidth > width && n > 0) {
            fittedText.push(line);
            line = words[n];
        }
        else {
            line = testLine + ' ';
        }
    }

    if (line.length > 0) {
        fittedText.push(line.trim());
    }

    return fittedText;
};

FixedWidthLabel.prototype.draw = function(context) {
    context.font = '12pt Calibri';
    var lines = this.text.split("\n");
    var yPos = this.y + 12;
    for (var i = 0; i < lines.length; i++) {
        var line = lines[i];
        context.fillText(line, this.x, yPos);
        yPos += 20;
    }
};


/// Textbox ///

function Textbox(x, y, width, height) {
    UIElement.call(this, x, y, width, height);
    this.text = '';
    this.cursorPos = 0;
    this.xScroll = 0;
    this.submitHandler = function(msg) { };
}

Textbox.prototype = Object.create(UIElement.prototype);
Textbox.prototype.constructor = Textbox;

Textbox.prototype.setFocus = function(focus) {
    if (this.focus != focus) {
        this.focus = focus;
        return true;
    }
    return false;
};

Textbox.prototype.handleKey = function(e) {
    var oldText = this.text;
    var oldCursorPos = this.cursorPos;

    if (e.charCode != 0) {
        var keyPressed = String.fromCharCode(e.charCode);
        this.text = this.text.substring(0, this.cursorPos) + keyPressed + this.text.substring(this.cursorPos);
        this.cursorPos++;
    }
    else {
        // TODO e.ctrlKey
        if (e.keyCode == 8) { // Backspace
            if (this.cursorPos > 0) {
                this.text = this.text.substring(0, this.cursorPos - 1) + this.text.substring(this.cursorPos);
                this.cursorPos--;
            }
        }
        else if (e.keyCode == 46) { // Delete
            if (this.cursorPos < this.text.length) {
                this.text = this.text.substring(0, this.cursorPos) + this.text.substring(this.cursorPos + 1);
            }
        }
        else if (e.keyCode == 37) { // Left
            if (this.cursorPos > 0) {
                this.cursorPos--;
            }
        }
        else if (e.keyCode == 39) { // Right
            if (this.cursorPos < this.text.length) {
                this.cursorPos++;
            }
        }
        else if (e.keyCode == 13) { // Enter
            this.submitHandler(this.text);
            this.cursorPos = 0;
            this.text = '';
        }
    }

    return (oldText != this.text || oldCursorPos != this.cursorPos);
};

Textbox.prototype.draw = function(context) {
    // Set clip area
    context.save();
    context.beginPath();
    context.rect(this.x + 3, this.y, this.width - 6, this.height);
    context.clip();

    context.beginPath();

    if (this.focus) {
        var cursorXpos = this.x + context.measureText(this.text.substring(0, this.cursorPos)).width + 3 + this.xScroll;

        // Adjust cursor if it's out of the box
        if (cursorXpos > this.x + this.width - 3) {
            this.xScroll -= cursorXpos - (this.x + this.width - 3);
            cursorXpos = this.x + this.width - 3;
        }
        else if (cursorXpos < this.x + 3) {
            this.xScroll -= cursorXpos - (this.x + 3);
            cursorXpos = this.x + 3;
        }

        // Adjust again if there's extra text and the cursor isn't at the end
        if (cursorXpos < this.x + this.width - 3) {
            var availSpace = Math.min(-this.xScroll, (this.x + this.width - 3) - cursorXpos);
            this.xScroll += availSpace;
            cursorXpos += availSpace;
        }
        context.moveTo(cursorXpos, this.y + 3);
        context.lineTo(cursorXpos, this.y + this.height - 3);
    }

    context.lineWidth = 1;
    context.strokeStyle = 'black';
    context.stroke();

    context.font = '12pt Calibri';
    context.fillText(this.text, this.x + 3 + this.xScroll, this.y + this.height - 3);

    context.restore();

    // Draw border
    context.beginPath();
    context.rect(this.x, this.y, this.width, this.height);
    context.lineWidth = 1;
    context.strokeStyle = 'black';
    context.stroke();
};


/// ScrollArea ///

function ScrollArea(x, y, width, height, element) {
    UIElement.call(this, x, y, width, height);
    this.element = element;
    this.scrollBar = {
        focus: false,
        visible: false,
        yScroll: 0,
        x: x + width - 10,
        y: y,
        width: 10,
        height: height
    };

    var _this = this;
    element.onSizeChange = function () { _this.updateScrollBarForText.call(_this) };

    this.updateScrollBarForText(this);
}

ScrollArea.prototype = Object.create(UIElement.prototype);
ScrollArea.prototype.constructor = ScrollArea;

ScrollArea.prototype.setFocus = function(focus) {
    if (!focus) {
        this.scrollBar.focus = false;
    }
    this.focus = focus;
    return false;
};

ScrollArea.prototype.updateScrollBarForText = function() {
    // Called by label changing text and scroll wheel
    var areaHeight = this.height;
    var eleHeight = this.element.height;
    var yScroll = this.scrollBar.yScroll;

    // If we were at the bottom, auto-scroll
    if (yScroll + areaHeight - 6 == eleHeight - 20 // 20 is one line of text
        || !this.scrollBar.visible && areaHeight - 6 < eleHeight) {
        yScroll = eleHeight - (areaHeight - 6);
    }

    var barHeight = areaHeight * areaHeight / eleHeight;
    barHeight = Math.max(10, Math.min(areaHeight, barHeight));
    var barOffset = yScroll / (eleHeight - (areaHeight - 6)) * (areaHeight - barHeight);
    barOffset = Math.max(0, barOffset);

    this.scrollBar.height = barHeight;
    this.scrollBar.visible = (barHeight != this.height);
    if (!this.scrollBar.visible) {
        this.scrollBar.yScroll = 0;
        this.scrollBar.y = this.y;
    }
    else {
        this.scrollBar.yScroll = yScroll;
        this.scrollBar.y = this.y + barOffset;
    }
};

ScrollArea.prototype.updateScrollBarForScroll = function(yDelta) {
    var oldYScroll = this.scrollBar.yScroll;

    var barOffset = this.scrollBar.y - this.y;
    barOffset += yDelta;
    barOffset = Math.max(0, Math.min(this.height - this.scrollBar.height, barOffset));
    this.scrollBar.y = barOffset + this.y;
    this.scrollBar.yScroll = barOffset * (this.element.height - (this.height - 6)) / (this.height - this.scrollBar.height);

    return (oldYScroll != this.scrollBar.yScroll);
};

ScrollArea.prototype.draw = function(context) {
    // Clip
    context.save();
    context.beginPath();
    context.rect(this.x + 3, this.y + 3, this.width - 6 - (this.scrollBar.visible ? 10 : 0), this.height - 6);
    context.clip();

    // Draw child
    this.element.x = this.x + 3;
    this.element.y = this.y + 3 - this.scrollBar.yScroll;
    this.element.draw(context);

    // Restore
    context.restore();

    // Draw border + scrollbar
    context.beginPath();
    context.rect(this.x, this.y, this.width, this.height);
    context.lineWidth = 1;
    context.strokeStyle = 'black';
    context.stroke();

    if (this.scrollBar.visible) {
        context.beginPath();
        context.rect(this.x + this.width - 10, this.y, 10, this.height);
        context.fillStyle = '#cccccc';
        context.fill();
        context.lineWidth = 1;
        context.strokeStyle = 'black';
        context.stroke();

        context.beginPath();
        context.rect(this.scrollBar.x, this.scrollBar.y, this.scrollBar.width, this.scrollBar.height);
        context.fillStyle = 'black';
        context.fill();
        context.lineWidth = 1;
        context.strokeStyle = 'black';
        context.stroke();
    }
};

ScrollArea.prototype.handleMouseDown = function(xy) {
    if (this.scrollBar.visible) {
        this.scrollBar.focus = isClicked(xy, this.scrollBar)
    }
    return false;
};

ScrollArea.prototype.handleMouseDrag = function(xy, xDelta, yDelta) {
    // Check if scrollbar is focused
    if (this.scrollBar.focus) {
        return this.updateScrollBarForScroll(yDelta);
    }
    return false;
};

ScrollArea.prototype.handleMouseWheel = function(yDelta) {
    if (this.scrollBar.visible) {
        return this.updateScrollBarForScroll(yDelta * 2);
    }
    return false;
};

