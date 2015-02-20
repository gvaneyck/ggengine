// Big TODOs
// - Throttle draw calls
// - Scroll Area - Line breaks due to width
// - Label - Copy text, links, emoji, onHover card images
// - Text box - control key, shift selection, copy/paste, don't scroll left until off left side
// - Deal with half pixels
// - Linking in game elements -> via drag or shift/ctrl click
// - Voice chat?

// Event handlers:
// handleMouseDown = Hit element + Mouse down (automatically gain focus)
// handleMouseDrag = Focused element + Mouse move while mouse is down
// handleMouseClick - Hit element + Focused element + Mouse up
// handleMouseDoubleClick - Hit element + Focused element + 2x Mouse up within interval
// handleMouseWheel - Focused element + Mouse scroll
// handleKey - Focused element + Key press


/// Helper functions ///

function isClicked(xy, element) {
    return (element.x <= xy.x
        && element.y <= xy.y
        && element.x + element.width >= xy.x
        && element.y + element.height >= xy.y);
}


/// Base class ///

function UIElement(context, x, y, width, height) {
    this.context = context;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.focus = false;
    this.visible = true;
}

UIElement.prototype.draw = function() { };
UIElement.prototype.setFocus = function(focus) { this.focus = focus; return false; };
UIElement.prototype.handleMouseDown = function(xy) { return false; };
UIElement.prototype.handleMouseDrag = function(xDelta, yDelta) { return false; };
UIElement.prototype.handleMouseClick = function(xy) { return false; };
UIElement.prototype.handleMouseDoubleClick = function(xy) { return false; };
UIElement.prototype.handleMouseWheel = function(yDelta) { return false; };
UIElement.prototype.handleKey = function(e) { return false; };


/// Label ///

function Label(context, x, y, text) {
    UIElement.call(this, context, x, y, 0, 0);
    this.onSizeChange = function() { };
    this.setText(text);
}

Label.prototype = Object.create(UIElement.prototype);
Label.prototype.constructor = Label;

Label.prototype.setText = function(text) {
    this.context.font = '12pt Calibri';
    var maxWidth = 0;
    var height = -6; // Remove padding
    var lines = text.split("\n");
    for (var i = 0; i < lines.length; i++) {
        var line = lines[i];
        var measure = this.context.measureText(line);
        maxWidth = Math.max(maxWidth, measure.width);
        height += 20;
    }

    this.width = maxWidth;
    this.height = height;

    this.text = text;

    this.onSizeChange();
};

Label.prototype.draw = function() {
    this.context.font = '12pt Calibri';
    var lines = this.text.split("\n");
    var yPos = this.y + 12;
    for (var i = 0; i < lines.length; i++) {
        var line = lines[i];
        this.context.fillText(line, this.x, yPos);
        yPos += 20;
    }
};


/// Textbox ///

function Textbox(context, x, y, width, height) {
    UIElement.call(this, context, x, y, width, height);
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

Textbox.prototype.draw = function() {
    // Set clip area
    this.context.save();
    this.context.beginPath();
    this.context.rect(this.x + 3, this.y, this.width - 6, this.height);
    this.context.clip();

    this.context.beginPath();

    if (this.focus) {
        var cursorXpos = this.x + this.context.measureText(this.text.substring(0, this.cursorPos)).width + 3 + this.xScroll;

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
        this.context.moveTo(cursorXpos, this.y + 3);
        this.context.lineTo(cursorXpos, this.y + this.height - 3);
    }

    this.context.strokeStyle = 'black';
    this.context.stroke();

    this.context.font = '12pt Calibri';
    this.context.fillText(this.text, this.x + 3 + this.xScroll, this.y + this.height - 3);

    this.context.restore();

    // Draw border
    this.context.beginPath();
    this.context.rect(this.x, this.y, this.width, this.height);
    this.context.strokeStyle = 'black';
    this.context.stroke();
};


/// ScrollArea ///

function ScrollArea(context, x, y, width, height, element) {
    UIElement.call(this, context, x, y, width, height);
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

    var self = this;
    element.onSizeChange = function () { self.updateScrollBarForText(self) };

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

ScrollArea.prototype.updateScrollBarForText = function(scrollArea) {
    // Called by label changing text and scroll wheel
    var areaHeight = scrollArea.height;
    var eleHeight = scrollArea.element.height;
    var yScroll = scrollArea.scrollBar.yScroll;

    // If we were at the bottom, auto-scroll
    if (yScroll + areaHeight - 6 == eleHeight - 20 // 20 is one line of text
        || !scrollArea.scrollBar.visible && areaHeight - 6 < eleHeight) {
        yScroll = eleHeight - (areaHeight - 6);
    }

    var barHeight = areaHeight * areaHeight / eleHeight;
    barHeight = Math.max(10, Math.min(areaHeight, barHeight));
    var barOffset = yScroll / (eleHeight - (areaHeight - 6)) * (areaHeight - barHeight);
    barOffset = Math.max(0, barOffset);

    scrollArea.scrollBar.height = barHeight;
    scrollArea.scrollBar.visible = (barHeight != scrollArea.height);
    if (!scrollArea.scrollBar.visible) {
        scrollArea.scrollBar.yScroll = 0;
        scrollArea.scrollBar.y = scrollArea.y;
    }
    else {
        scrollArea.scrollBar.yScroll = yScroll;
        scrollArea.scrollBar.y = scrollArea.y + barOffset;
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

ScrollArea.prototype.draw = function() {
    // Clip
    this.context.save();
    this.context.beginPath();
    this.context.rect(this.x + 3, this.y + 3, this.width - 6 - (this.scrollBar.visible ? 10 : 0), this.height - 6);
    this.context.clip();

    // Draw child
    this.element.x = this.x + 3;
    this.element.y = this.y + 3 - this.scrollBar.yScroll;
    this.element.draw();

    // Restore
    this.context.restore();

    // Draw border + scrollbar
    this.context.beginPath();
    this.context.rect(this.x, this.y, this.width, this.height);
    this.context.strokeStyle = '#000000';
    this.context.stroke();

    if (this.scrollBar.visible) {
        this.context.beginPath();
        this.context.rect(this.x + this.width - 10, this.y, 10, this.height);
        this.context.fillStyle = '#cccccc';
        this.context.fill();
        this.context.stroke();

        this.context.beginPath();
        this.context.rect(this.scrollBar.x, this.scrollBar.y, this.scrollBar.width, this.scrollBar.height);
        this.context.fillStyle = '#000000';
        this.context.fill();
        this.context.stroke();
    }
};

ScrollArea.prototype.handleMouseDown = function(xy) {
    if (this.scrollBar.visible) {
        this.scrollBar.focus = isClicked(xy, this.scrollBar)
    }
    return false;
};

ScrollArea.prototype.handleMouseDrag = function(xDelta, yDelta) {
    // Check if scrollbar is focused
    if (this.scrollBar.focus) {
        return this.updateScrollBarForScroll(yDelta);
    }
    return false;
};

ScrollArea.prototype.handleMouseWheel = function(yDelta) {
    return this.updateScrollBarForScroll(yDelta * 2);
};
