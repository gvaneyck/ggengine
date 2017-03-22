// Big TODOs
// - Label - Copy text, links, emoji, onHover card images
// - Add in full keyboard support for textboxes
// - Text box - control key, shift selection, copy/paste, don't scroll left until off left side
// - Authentication/Sessions (cookie + server)
// - Send action to clients when chosen for logging
// - Don't send gamestate if it hasn't changed for a player (hide decision timing)
// - Deal with half pixels
// - Redo temp context shenanigans in resize to actually create an element and destroy it on draw
// - Add set pos methods for cascading to children, as well as for empty constructors for dynamically positioned elements
// - Browser compatibility/jQuery handlers/Mobile
// - Linking in game elements -> via drag or shift/ctrl click
// - Dirty rectangles drawing
// - Voice chat?
// - If same name, reconnecting to multiple games and/or multiple players in same game
// - Double encoding game state
// - Reconnect to lobbies
// - Player names in games
// - Redraw doesn't always happen when changing tabs
// - Default visibility false?


// Event handlers:
// handleMouseDown = Hit element + Mouse down (automatically gain focus via setFocus)
// handleMouseHover = Hit element + Mouse move (automatically gain hover via setHover)
// handleMouseDrag = Focused element + Mouse move while mouse is down
// handleMouseUp = Focused element + Mouse up
// handleMouseClick = Hit element + Focused element + Mouse up
// handleMouseDoubleClick = Hit element + Focused element + 2x Mouse up within interval
// handleMouseWheel = Focused element + Mouse scroll
// handleKey = Focused element + Key press

// Notes:
// - True is the default return value, with the exception of MouserHover & Key
// - Returning true stops event propogation, including prevent default
// - If you want to redraw, you should set dirty during the event handler


/// Helper functions ///

var imageCache = new function() {
    var cache = {};
    this.getImage = function(path) {
        if (cache[path] == undefined) {
            var img = new Image();
            img.src = path;
            cache[path] = img;
        }
        return cache[path];
    }
};

function isClicked(x, y, element) {
    return (element.x <= x
        && element.y <= y
        && element.x + element.width >= x
        && element.y + element.height >= y);
}

function getCursorPosition(e, bounds) {
    var data = {};
    if (e.pageX != undefined && e.pageY != undefined) {
        data.x = e.pageX;
        data.y = e.pageY;
    } else {
        data.x = e.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
        data.y = e.clientY + document.body.scrollTop + document.documentElement.scrollTop;
    }
    data.x = Math.min(data.x, bounds.width);
    data.y = Math.min(data.y, bounds.height);
    return data;
}

function initRootContainer(container, canvas) {
    document.addEventListener("mousedown", function(e) { if (container.mouseDownHandler.call(container, e, getCursorPosition(e, canvas))) { e.preventDefault(); } });
    document.addEventListener("mousemove", function(e) { if (container.mouseMoveHandler.call(container, e, getCursorPosition(e, canvas))) { e.preventDefault(); } });
    document.addEventListener("mouseup", function(e) { if (container.mouseUpHandler.call(container, e, getCursorPosition(e, canvas))) { e.preventDefault(); } });
    document.addEventListener("wheel", function(e) { if (container.mouseWheelHandler.call(container, e)) { e.preventDefault(); } }, false);
    document.addEventListener("keydown", function(e) { if (container.typingHandler.call(container, e)) { e.preventDefault(); } });

    var sizeWindow = function() {
        // TODO: Figure out why opening console sometimes wipes canvas w/out redraw
        // Resize current canvas
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;

        // Chain call for things like re-doing layout
        container.onresize();
        container.dirty = true;
    };
    window.addEventListener("resize", sizeWindow, false);
    sizeWindow();

    // Start draw loop
    var drawLoop = function() {
        requestAnimationFrame(drawLoop);
        if (container.isDirty()) {
            var context = canvas.getContext("2d");
            context.clearRect(0, 0, canvas.width, canvas.height);
            container.draw(context);
        }
    };
    drawLoop();
}


/// Base class ///

function UIElement(x, y, width, height) {
    this.setX(x);
    this.setY(y);
    this.setWidth(width);
    this.setHeight(height);
    this.zLevel = 0;
    this.focus = false;
    this.hover = false;
    this.visible = true;
    this.dirty = false;
}

UIElement.prototype.setX = function(x) { if (this.x != Math.floor(x)) { this.x = Math.floor(x); this.dirty = true; } };
UIElement.prototype.setY = function(y) { if (this.y != Math.floor(y)) { this.y = Math.floor(y); this.dirty = true; } };
UIElement.prototype.setWidth = function(width) { if (this.width != Math.ceil(width)) { this.width = Math.ceil(width); this.dirty = true; } };
UIElement.prototype.setHeight = function(height) { if (this.height != Math.ceil(height)) { this.height = Math.ceil(height); this.dirty = true; } };
UIElement.prototype.setVisible = function(visible) { if (this.visible != visible) { this.visible = visible; this.dirty = true; } };

UIElement.prototype.scratchPad = document.createElement("canvas").getContext("2d");
UIElement.prototype.getZLevel = function() { return this.zLevel; };
UIElement.prototype.isDirty = function() { return this.dirty; };
UIElement.prototype.draw = function(context) { this.dirty = false };
UIElement.prototype.setFocus = function(focus) { this.focus = focus; };
UIElement.prototype.setHover = function(hover) { this.hover = hover; };
UIElement.prototype.handleMouseDown = function(x, y) { return true; };
UIElement.prototype.handleMouseHover = function(x, y) { return false; };
UIElement.prototype.handleMouseDrag = function(x, y, xDelta, yDelta) { return true; };
UIElement.prototype.handleMouseUp = function(x, y) { return true; };
UIElement.prototype.handleMouseClick = function(x, y) { return true; };
UIElement.prototype.handleMouseRightClick = function(x, y) { return true; };
UIElement.prototype.handleMouseDoubleClick = function(x, y) { return true; };
UIElement.prototype.handleMouseWheel = function(yDelta) { return true; };
UIElement.prototype.handleKey = function(e) { return false; };

UIElement.prototype.highlight = function(context) {
    context.beginPath();
    context.rect(this.x - 2, this.y - 2, this.width + 4, this.height + 4);
    context.lineWidth = 3;
    context.strokeStyle = "black";
    context.stroke();
};


/// Container ///

function Container(x, y) {
    if (x == undefined) {
        x = 0;
        y = 0;
    }
    UIElement.call(this, x, y, 0, 0);
    this.elements = [];
    this.dirty = false;
}

Container.prototype = Object.create(UIElement.prototype);
Container.prototype.constructor = Container;

Container.state = {
    isMouseDown: false,
    lastMousePos: { },
    lastClick: { element: null, time: 0 }
};

Container.prototype.onresize = function() {
    // TODO: Chain
};

Container.prototype.onlyVisible = function(elements) {
    for (var i in this.elements) {
        var e = this.elements[i];
        if (elements.includes(e)) {
            e.setVisible(true);
        } else {
            e.setVisible(false);
        }
    }
};

Container.prototype.isDirty = function() {
    if (this.dirty) {
        return true;
    }

    for (var i = 0; i < this.elements.length; i++) {
        if (this.elements[i].isDirty()) {
            return true;
        }
    }

    return false;
};

Container.prototype.draw = function(context) {
    this.sortElements();

    context.save();
    context.translate(this.x, this.y);

    for (var i = 0; i < this.elements.length; i++) {
        var element = this.elements[i];
        if (element.visible) {
            element.draw(context);
        }
    }

    context.restore();

    this.dirty = false;
};

Container.prototype.sortElements = function() {
    // Sort by z order ascending
    this.elements.sort(function(a, b) {
        return a.getZLevel() - b.getZLevel();
    });
};

Container.prototype.addElement = function(element) {
    if (element != undefined && element instanceof UIElement) {
        this.elements.push(element);
        this.dirty = true;
    }
};

Container.prototype.addElements = function(elements) {
    for (var key in elements) {
        if (key instanceof UIElement) {
            this.addElement(key);
        } else if (elements[key] instanceof UIElement) {
            this.addElement(elements[key]);
        }
    }
};

Container.prototype.mouseDownHandler = function(e, xy) {
    this.sortElements();
    var handled = false;

    Container.state.isMouseDown = true;
    Container.state.lastMousePos = xy;

    for (var i = this.elements.length - 1; i >= 0; i--) {
        var element = this.elements[i];
        if (!handled && element.visible && element instanceof Container) {
            handled = element.mouseDownHandler.call(element, e, xy);
        } else if (!handled && element.visible && isClicked(xy.x - this.x, xy.y - this.y, element)) {
            element.setFocus(true);
            handled = element.handleMouseDown(xy.x - this.x, xy.y - this.y);
        } else {
            element.setFocus(false);
        }
    }

    return handled;
};

Container.prototype.mouseMoveHandler = function(e, xy) {
    var hoverHandled = this.mouseHoverHandler(e, xy);
    var dragHandled = false;
    if (Container.state.isMouseDown) {
        dragHandled = this.mouseDragHandler(e, xy);
        Container.state.lastMousePos = xy;
    }
    return (hoverHandled || dragHandled);
};

Container.prototype.mouseHoverHandler = function(e, xy) {
    this.sortElements();
    var handled = false;

    for (var i = this.elements.length - 1; i >= 0; i--) {
        var element = this.elements[i];
        if (!handled && element.visible && element instanceof Container) {
            handled = element.mouseMoveHandler.call(element, e, xy);
        } else if (!handled && element.visible && isClicked(xy.x - this.x, xy.y - this.y, element)) {
            element.setHover(true);
            handled = element.handleMouseHover(xy.x - this.x, xy.y - this.y);
        } else {
            element.setHover(false);
        }
    }

    return handled;
};

Container.prototype.mouseDragHandler = function(e, xy) {
    this.sortElements();
    var handled = false;

    for (var i = this.elements.length - 1; i >= 0; i--) {
        var element = this.elements[i];
        if (!element.visible) {
            continue;
        }

        if (element instanceof Container) {
            handled = element.mouseDragHandler.call(element, e, xy);
        } else if (element.focus) {
            var xDelta = xy.x - Container.state.lastMousePos.x;
            var yDelta = xy.y - Container.state.lastMousePos.y;
            handled = element.handleMouseDrag(xy.x - this.x, xy.y - this.y, xDelta, yDelta);
        }

        if (handled) {
            break;
        }
    }

    return handled;
};

Container.prototype.mouseUpHandler = function(e, xy) {
    this.sortElements();
    Container.state.isMouseDown = false;
    var handled = false;

    for (var i = this.elements.length - 1; i >= 0; i--) {
        var element = this.elements[i];
        if (!element.visible) {
            continue;
        }

        if (element instanceof Container) {
            handled = element.mouseUpHandler.call(element, e, xy);
        } else if (element.focus) {
            handled = element.handleMouseUp(xy.x - this.x, xy.y - this.y);
            if (isClicked(xy.x, xy.y, element)) {
                var clickHandled = false;

                var now = new Date().getTime();
                if (e.button === 2) {
                    clickHandled = element.handleMouseRightClick(xy.x - this.x, xy.y - this.y);
                } else if (Container.state.lastClick.element == element && now - Container.state.lastClick.time < 500) {
                    clickHandled = element.handleMouseDoubleClick(xy.x - this.x, xy.y - this.y);
                    Container.state.lastClick.element = null;
                } else {
                    clickHandled = element.handleMouseClick(xy.x - this.x, xy.y - this.y);
                    Container.state.lastClick.element = element;
                    Container.state.lastClick.time = now;
                }

                if (clickHandled) {
                    handled = true;
                }
            }
        }

        if (handled) {
            break;
        }
    }

    return handled;
};

Container.prototype.mouseWheelHandler = function(e) {
    this.sortElements();
    var handled = false;

    for (var i = this.elements.length - 1; i >= 0; i--) {
        var element = this.elements[i];
        if (!element.visible) {
            continue;
        }

        if (element instanceof Container) {
            handled = element.mouseWheelHandler.call(element, e);
        } else if (element.focus) {
            handled = element.handleMouseWheel(e.deltaY);
        }

        if (handled) {
            break;
        }
    }

    return handled;
};

Container.prototype.typingHandler = function(e) {
    this.sortElements();
    var handled = false;

    for (var i = this.elements.length - 1; i >= 0; i--) {
        var element = this.elements[i];
        if (!element.visible) {
            continue;
        }

        if (element instanceof Container) {
            handled = element.typingHandler.call(element, e);
        } else if (element.focus) {
            handled = element.handleKey(e);
        }

        if (handled) {
            break;
        }
    }

    return handled;
};


/// Label ///

function Label(x, y, text) {
    // Label without position
    if (y == undefined) {
        text = x;
        x = 0;
        y = 0;
    }
    UIElement.call(this, x, y, 0, 0);

    this.fontSize = "12pt";
    this.font = "Calibri";
    this.color = "black";

    this.onSizeChange = function() { };
    this.setText(text);
}

Label.prototype = Object.create(UIElement.prototype);
Label.prototype.constructor = Label;

Label.prototype.setText = function(text) {
    this.text = text;
    this.measureText();
    this.dirty = true;
};

Label.prototype.measureText = function() {
    this.scratchPad.font = this.fontSize + " " + this.font;
    var maxWidth = 0;
    var height = -6; // Remove padding
    var lines = this.text.split("\n");
    for (var i = 0; i < lines.length; i++) {
        var line = lines[i];
        var measure = this.scratchPad.measureText(line);
        maxWidth = Math.max(maxWidth, measure.width);
        height += 20;
    }

    this.setWidth(maxWidth);
    this.setHeight(height);

    this.onSizeChange();
};

Label.prototype.draw = function(context) {
    context.font = this.fontSize + " " + this.font;
    var lines = this.text.split("\n");
    var yPos = this.y + 12;
    for (var i = 0; i < lines.length; i++) {
        var line = lines[i];
        context.fillStyle = this.color;
        context.fillText(line, this.x, yPos);
        yPos += 20;
    }

    this.dirty = false;
};


/// FixedWidthLabel ///

function FixedWidthLabel(x, y, width, text) {
    Label.call(this, x, y, "");
    this.setWidth(width);
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
    this.setHeight(height);
    this.onSizeChange();
};

FixedWidthLabel.prototype.fitText = function(text, width) {
    this.scratchPad.font = "12pt Calibri";
    var fittedText = [];
    var words = text.split(" ");

    var line = "";
    for (var n = 0; n < words.length; n++) {
        var testLine = line + words[n];
        var lineWidth = this.scratchPad.measureText(testLine).width;
        if (lineWidth > width && n > 0) {
            fittedText.push(line);
            line = words[n];
        }
        else {
            line = testLine + " ";
        }
    }

    if (line.length > 0) {
        fittedText.push(line.trim());
    }

    return fittedText;
};

FixedWidthLabel.prototype.draw = function(context) {
    context.font = "12pt Calibri";
    var lines = this.text.split("\n");
    var yPos = this.y + 12;
    for (var i = 0; i < lines.length; i++) {
        var line = lines[i];
        context.fillText(line, this.x, yPos);
        yPos += 20;
    }

    this.dirty = false;
};


/// Textbox ///

function Textbox(x, y, width, height) {
    UIElement.call(this, x, y, width, height);
    this.text = "";
    this.cursorPos = 0;
    this.xScroll = 0;
    this.submitHandler = function(msg) { };
}

Textbox.prototype = Object.create(UIElement.prototype);
Textbox.prototype.constructor = Textbox;

Textbox.prototype.setFocus = function(focus) {
    if (this.focus != focus) {
        this.focus = focus;
        this.dirty = true;
    }
};

Textbox.prototype.handleKey = function(e) {
    var oldText = this.text;
    var oldCursorPos = this.cursorPos;

    // TODO e.ctrlKey e.shiftKey
    var char = String.fromCharCode(e.keyCode);
    if (e.keyCode == 8) { // Backspace
        if (this.cursorPos > 0) {
            this.text = this.text.substring(0, this.cursorPos - 1) + this.text.substring(this.cursorPos);
            this.cursorPos--;
        }
    } else if (e.keyCode == 46) { // Delete
        if (this.cursorPos < this.text.length) {
            this.text = this.text.substring(0, this.cursorPos) + this.text.substring(this.cursorPos + 1);
        }
    } else if (e.keyCode == 37) { // Left
        if (this.cursorPos > 0) {
            this.cursorPos--;
        }
    } else if (e.keyCode == 39) { // Right
        if (this.cursorPos < this.text.length) {
            this.cursorPos++;
        }
    } else if (e.keyCode == 13) { // Enter
        this.submitHandler(this.text);
        this.cursorPos = 0;
        this.text = "";
    } else if (e.ctrlKey) {
        // No ctrl handling yet
    } else if (char >= "A" && char <= "Z" || char == " " || char >= "0" && char <="9") {
        var trueChar;
        if (char >= "A" && char <= "Z") {
            trueChar = (e.shiftKey ? char : String.fromCharCode(e.keyCode + 32));
        }
        else {
            trueChar = char;
        }
        this.text = this.text.substring(0, this.cursorPos) + trueChar + this.text.substring(this.cursorPos);
        this.cursorPos++;
    }

    this.dirty = (oldText != this.text || oldCursorPos != this.cursorPos);
    return this.dirty;
};

Textbox.prototype.draw = function(context) {
    // Set clip area
    context.save();
    context.beginPath();
    context.rect(this.x + 3, this.y, this.width - 6, this.height);
    context.clip();

    context.beginPath();

    if (this.focus) {
        var cursorXpos = Math.ceil(this.x + context.measureText(this.text.substring(0, this.cursorPos)).width + 3 + this.xScroll);

        // Adjust cursor if it"s out of the box
        if (cursorXpos > this.x + this.width - 3) {
            this.xScroll -= cursorXpos - (this.x + this.width - 3);
            cursorXpos = this.x + this.width - 3;
        }
        else if (cursorXpos < this.x + 3) {
            this.xScroll -= cursorXpos - (this.x + 3);
            cursorXpos = this.x + 3;
        }

        // Adjust again if there"s extra text and the cursor isn"t at the end
        if (cursorXpos < this.x + this.width - 3) {
            var availSpace = Math.min(-this.xScroll, (this.x + this.width - 3) - cursorXpos);
            this.xScroll += availSpace;
            cursorXpos += availSpace;
        }
        context.moveTo(cursorXpos, this.y + 3);
        context.lineTo(cursorXpos, this.y + this.height - 3);
    }

    context.lineWidth = 2;
    context.strokeStyle = "black";
    context.stroke();

    context.font = "12pt Calibri";
    context.fillText(this.text, this.x + 3 + this.xScroll, this.y + this.height - 4);

    context.restore();

    // Draw border
    context.beginPath();
    context.rect(this.x, this.y, this.width, this.height);
    context.lineWidth = 2;
    context.strokeStyle = "black";
    context.stroke();

    this.dirty = false;
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
    element.onSizeChange = function() { _this.updateScrollBarForText.call(_this) };

    this.updateScrollBarForText(this);
}

ScrollArea.prototype = Object.create(UIElement.prototype);
ScrollArea.prototype.constructor = ScrollArea;

ScrollArea.prototype.setFocus = function(focus) {
    if (!focus) {
        this.scrollBar.focus = false;
    }
    this.focus = focus;
};

ScrollArea.prototype.updateScrollBarForText = function() {
    // Called by label changing text and scroll wheel
    var areaHeight = this.height;
    var eleHeight = this.element.height;
    var yScroll = this.scrollBar.yScroll;

    if (this.element instanceof FixedWidthLabel) {
        // If we were at the bottom, auto-scroll
        if (yScroll + areaHeight - 6 == eleHeight - 20 // 20 is one line of text
            || !this.scrollBar.visible && areaHeight - 6 < eleHeight) {
            yScroll = eleHeight - (areaHeight - 6);
        }
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

    this.dirty = (oldYScroll != this.scrollBar.yScroll);
};

ScrollArea.prototype.draw = function(context) {
    // Clip
    context.save();
    context.beginPath();
    context.rect(this.x + 3, this.y + 3, this.width - 6 - (this.scrollBar.visible ? 10 : 0), this.height - 6);
    context.clip();

    // Draw child
    this.element.setX(this.x + 3);
    this.element.setY(this.y + 3 - this.scrollBar.yScroll);
    this.element.draw(context);

    // Restore
    context.restore();

    // Draw border + scrollbar
    context.beginPath();
    context.rect(this.x, this.y, this.width, this.height);
    context.lineWidth = 2;
    context.strokeStyle = "black";
    context.stroke();

    if (this.scrollBar.visible) {
        context.beginPath();
        context.rect(this.x + this.width - 10, this.y, 10, this.height);
        context.fillStyle = "#cccccc";
        context.fill();
        context.lineWidth = 2;
        context.strokeStyle = "black";
        context.stroke();

        context.beginPath();
        context.rect(this.scrollBar.x, this.scrollBar.y, this.scrollBar.width, this.scrollBar.height);
        context.fillStyle = "black";
        context.fill();
        context.lineWidth = 2;
        context.strokeStyle = "black";
        context.stroke();
    }

    this.dirty = false;
};

ScrollArea.prototype.handleMouseDown = function(x, y) {
    if (this.scrollBar.visible) {
        this.scrollBar.focus = isClicked(x, y, this.scrollBar)
    }
    return true;
};

ScrollArea.prototype.handleMouseDrag = function(x, y, xDelta, yDelta) {
    // Check if scrollbar is focused
    if (this.scrollBar.focus) {
        this.updateScrollBarForScroll(yDelta);
    }
    return true;
};

ScrollArea.prototype.handleMouseWheel = function(yDelta) {
    if (this.scrollBar.visible) {
        this.updateScrollBarForScroll(yDelta * 2);
    }
    return true;
};


/// Button ///

function Button(x, y, text) {
    Label.call(this, x, y, text);
    this.disabled = false;
}

Button.prototype = Object.create(Label.prototype);
Button.prototype.constructor = Button;

Button.prototype.draw = function(context) {
    context.font = "12pt Calibri";
    context.fillStyle = "black";
    context.fillText(this.text, this.x + 3, this.y + 15);

    context.beginPath();
    context.rect(this.x, this.y, this.width + 6, this.height + 6);
    context.lineWidth = 2;
    context.strokeStyle = "black";
    context.stroke();

    this.dirty = false;
};


/// Table ///

function Table(x, y, width) {
    UIElement.call(this, x, y, width, 0);
    this.elements = [];
    this.emptyText = "";
}

Table.prototype = Object.create(UIElement.prototype);
Table.prototype.constructor = Table;

Table.prototype.onCellClick = function(row, col) { };

Table.prototype.calcWidths = function() {
    var maxWidths = [];
    for (var i = 0; i < this.elements.length; i++) {
        var row = this.elements[i];
        for (var j = 0; j < row.length; j++) {
            if (maxWidths[j] == undefined || maxWidths[j] < row[j].width) {
                maxWidths[j] = row[j].width
            }
        }
    }

    // Fix last column width
    var xOff = 0;
    for (var i = 0; i < maxWidths.length - 1; i++) {
        xOff += maxWidths[i] - 6;
    }
    maxWidths[maxWidths.length - 1] = this.width - xOff - 6;

    return maxWidths;
};

Table.prototype.handleMouseClick = function(x, y) {
    var maxWidths = this.calcWidths();
    var row = Math.floor((y - this.y) / 20);
    var col = 0;
    var xOff = x - this.x;
    for (var i = 0; i < maxWidths.length; i++) {
        xOff -= maxWidths[i] + 6;
        if (xOff <= 0) {
            col = i;
            break;
        }
    }

    this.onCellClick(row, col);
    return true;
};

Table.prototype.draw = function(context) {
    this.setHeight(this.elements.length * 20);
    if (this.elements.length == 0) {
        this.setHeight(20);
        context.font = "12pt Calibri";
        context.fillText(this.emptyText, this.x + 3, this.y + 15);
    }

    // Draw border
    context.beginPath();
    context.rect(this.x, this.y, this.width, this.height);
    context.lineWidth = 2;
    context.strokeStyle = "black";
    context.stroke();

    // Draw row lines
    var yOff = 20;
    for (var i = 0; i < this.elements.length - 1; i++) {
        context.beginPath();
        context.moveTo(this.x, this.y + yOff);
        context.lineTo(this.x + this.width, this.y + yOff);
        context.stroke();
        yOff += 20;
    }

    // Draw col lines
    var maxWidths = this.calcWidths();
    var xOff = 0;
    for (var i = 0; i < maxWidths.length - 1; i++) {
        xOff += maxWidths[i] + 6;
        context.beginPath();
        context.moveTo(this.x + xOff, this.y);
        context.lineTo(this.x + xOff, this.y + this.height);
        context.stroke();
    }

    // Draw elements
    var yPos = this.y + 3;
    for (var i = 0; i < this.elements.length; i++) {
        var xPos = this.x + 3;

        var row = this.elements[i];
        for (var j = 0; j < row.length; j++) {
            // Set clip area
            context.save();
            context.beginPath();
            context.rect(xPos - 2, yPos - 2, maxWidths[j] + 4, 18);
            context.clip();

            row[j].setX(xPos);
            row[j].setY(yPos);
            row[j].draw(context);
            xPos += maxWidths[j] + 6;

            // Restore context
            context.restore();
        }

        yPos += 20;
    }

    this.dirty = false;
};


/// Picture ///

function Picture(x, y, width, height, path) {
    if (y == undefined) {
        this.path = x;
        UIElement.call(this, 0, 0, 0, 0);
    }
    else {
        this.path = path;
        UIElement.call(this, x, y, width, height);
    }

    this.img = imageCache.getImage(this.path);
    this.loaded = this.img.complete;
    if (!this.loaded) {
        var _this = this;
        this.img.addEventListener("load", function () {
            _this.loaded = true;
            _this.dirty = true;
        }, false);
    }
}

Picture.prototype = Object.create(UIElement.prototype);
Picture.prototype.constructor = Picture;

Picture.prototype.draw = function(context) {
    if (this.loaded) {
        context.drawImage(this.img, this.x, this.y, this.width, this.height);
    }
    else {
        context.beginPath();
        context.rect(this.x, this.y, this.width, this.height);
        context.lineWidth = 2;
        context.strokeStyle = "black";
        context.stroke();
    }

    this.dirty = false;
};

