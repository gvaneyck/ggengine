function ReconnectingWebSocket(url) {
    this.url = url;
}

ReconnectingWebSocket.prototype.connect = function() {
    var _this = this;
    this.websocket = new WebSocket('ws://' + this.url);
    this.websocket.onopen = function (e) { _this._onOpen(e); };
    this.websocket.onmessage = function (e) { _this._onMessage(e); };
    this.websocket.onerror = function (e) { _this._onError(e); };
    this.websocket.onclose = function (e) { _this._onClose(e); };
};

ReconnectingWebSocket.prototype._onOpen = function(e) {
    console.log('OPEN');
    if (this.onopen != undefined) {
        this.onopen(e);
    }
};

ReconnectingWebSocket.prototype._onMessage = function(e) {
    console.log('MESSAGE: ' + e.data);
    if (this.onmessage != undefined) {
        this.onmessage(e);
    }
};

ReconnectingWebSocket.prototype._onError = function(e) {
    console.log('ERROR: ' + e.data);
    if (this.onerror != undefined) {
        this.onerror(e);
    }
};

ReconnectingWebSocket.prototype._onClose = function(e) {
    console.log('CLOSE');
    if (this.onclose != undefined) {
        this.onclose(e);
    }
    setTimeout(this.connect(), 1000);
};

ReconnectingWebSocket.prototype.send = function(data) {
    console.log(data);
    if (this.websocket.readyState == WebSocket.OPEN) {
        this.websocket.send(data);
    }
};
