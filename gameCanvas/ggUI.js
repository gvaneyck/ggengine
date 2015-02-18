/// UI classes

// Base class

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
UIElement.prototype.handleKey = function(e) { return false; };

UIElement.prototype.handleClick = function(xy) {
    this.focus = this.isClicked(xy);
    return this.focus
};

UIElement.prototype.isClicked = function(xy) {
    return (this.x <= xy.x
        && this.y <= xy.y
        && this.x + this.width >= xy.x
        && this.y + this.height >= xy.y);
};

// Label

function Label(context, x, y, text) {
    UIElement.call(this, context, x, y, 0, 0);
    this.setText(text);
}

Label.prototype = Object.create(UIElement.prototype);
Label.prototype.constructor = Label;

Label.prototype.setText = function(text) {
    this.context.font = '12pt Calibri';
    var maxWidth = 0;
    var height = 0;
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

// Textbox

function Textbox(context, x, y, width, height) {
    UIElement.call(this, context, x, y, width, height);
    this.text = '';
    this.cursorPos = 0;
    this.xScroll = 0;
    this.submitHandler = new function(msg) {};
}

Textbox.prototype = Object.create(UIElement.prototype);
Textbox.prototype.constructor = Textbox;

Textbox.prototype.handleKey = function(e) {
    var handled = false;
    if (e.charCode != 0) {
        handled = true;
        var keyPressed = String.fromCharCode(e.charCode);
        this.text = this.text.substring(0, this.cursorPos) + keyPressed + this.text.substring(this.cursorPos);
        this.cursorPos++;
    }
    else {
        // TODO e.ctrlKey
        if (e.keyCode == 8) { // Backspace
            handled = true;
            if (this.cursorPos > 0) {
                this.text = this.text.substring(0, this.cursorPos - 1) + this.text.substring(this.cursorPos);
                this.cursorPos--;
            }
        }
        else if (e.keyCode == 46) { // Delete
            handled = true;
            if (this.cursorPos < this.text.length) {
                this.text = this.text.substring(0, this.cursorPos) + this.text.substring(this.cursorPos + 1);
            }
        }
        else if (e.keyCode == 37) { // Left
            handled = true;
            if (this.cursorPos > 0) {
                this.cursorPos--;
            }
        }
        else if (e.keyCode == 39) { // Right
            handled = true;
            if (this.cursorPos < this.text.length) {
                this.cursorPos++;
            }
        }
        else if (e.keyCode == 13) { // Enter
            handled = true;
            this.submitHandler(this.text);
            this.cursorPos = 0;
            this.text = '';
        }
    }

    if (handled) {
        e.preventDefault();
    }
    return handled;
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

// ScrollArea (only works for labels for now)
function ScrollArea(context, x, y, width, height, element) {
    UIElement.call(this, context, x, y, width, height);
    this.element = element;
}

ScrollArea.prototype = Object.create(UIElement.prototype);
ScrollArea.prototype.constructor = Label;

ScrollArea.prototype.draw = function() {
    // Clip
    // Draw child
    // Restore
    // Draw border + scrollbar
};

ScrollArea.prototype.handleClick = function(xy) {
    this.focus = this.isClicked(xy);
    this.element.focus = this.focus;
    // Pass call through to child
    return this.focus
};

// Add scrolling functionality

