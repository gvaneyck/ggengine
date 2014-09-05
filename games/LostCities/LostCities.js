var kBoardWidth = 9;
var kBoardHeight = 9;
var kPieceWidth = 50;
var kPieceHeight = 50;
var kPixelWidth = 1 + (kBoardWidth * kPieceWidth);
var kPixelHeight = 1 + (kBoardHeight * kPieceHeight);

var gCanvasElement;
var gDrawingContext;
var gPattern;

var gPieces;
var gNumPieces;
var gSelectedPieceIndex;
var gSelectedPieceHasMoved;
var gMoveCount;
var gGameInProgress;

function Cell(row, column) {
    this.row = row;
    this.column = column;
}

function getCursorPosition(e) {
    /* returns Cell with .row and .column properties */
    var x;
    var y;
    if (e.pageX != undefined && e.pageY != undefined) {
        x = e.pageX;
        y = e.pageY;
    }
    else {
        x = e.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
        y = e.clientY + document.body.scrollTop + document.documentElement.scrollTop;
    }
    x -= gCanvasElement.offsetLeft;
    y -= gCanvasElement.offsetTop;
    x = Math.min(x, kBoardWidth * kPieceWidth);
    y = Math.min(y, kBoardHeight * kPieceHeight);
    var cell = new Cell(Math.floor(y/kPieceHeight), Math.floor(x/kPieceWidth));
    return cell;
}

function halmaOnClick(e) {
    var cell = getCursorPosition(e);
    for (var i = 0; i < gNumPieces; i++) {
        if ((gPieces[i].row == cell.row) &&
            (gPieces[i].column == cell.column)) {
            clickOnPiece(i);
            return;
        }
    }
    clickOnEmptyCell(cell);
}

function clickOnEmptyCell(cell) {
    if (gSelectedPieceIndex == -1) { return; }
    var rowDiff = Math.abs(cell.row - gPieces[gSelectedPieceIndex].row);
    var columnDiff = Math.abs(cell.column - gPieces[gSelectedPieceIndex].column);
    if ((rowDiff <= 1) &&
        (columnDiff <= 1)) {
        /* we already know that this click was on an empty square,
           so that must mean this was a valid single-square move */
        gPieces[gSelectedPieceIndex].row = cell.row;
        gPieces[gSelectedPieceIndex].column = cell.column;
        gMoveCount += 1;
        gSelectedPieceIndex = -1;
        gSelectedPieceHasMoved = false;
        drawBoard();
        return;
    }
    if ((((rowDiff == 2) && (columnDiff == 0)) ||
        ((rowDiff == 0) && (columnDiff == 2)) ||
        ((rowDiff == 2) && (columnDiff == 2))) &&
        isThereAPieceBetween(gPieces[gSelectedPieceIndex], cell)) {
        /* this was a valid jump */
        if (!gSelectedPieceHasMoved) {
            gMoveCount += 1;
        }
        gSelectedPieceHasMoved = true;
        gPieces[gSelectedPieceIndex].row = cell.row;
        gPieces[gSelectedPieceIndex].column = cell.column;
        drawBoard();
        return;
    }
    gSelectedPieceIndex = -1;
    gSelectedPieceHasMoved = false;
    drawBoard();
}

function clickOnPiece(pieceIndex) {
    if (gSelectedPieceIndex == pieceIndex) { return; }
    gSelectedPieceIndex = pieceIndex;
    gSelectedPieceHasMoved = false;
    drawBoard();
}

function isThereAPieceBetween(cell1, cell2) {
    /* note: assumes cell1 and cell2 are 2 squares away
       either vertically, horizontally, or diagonally */
    var rowBetween = (cell1.row + cell2.row) / 2;
    var columnBetween = (cell1.column + cell2.column) / 2;
    for (var i = 0; i < gNumPieces; i++) {
        if ((gPieces[i].row == rowBetween) &&
            (gPieces[i].column == columnBetween)) {
            return true;
        }
    }
    return false;
}

function isTheGameOver() {
    for (var i = 0; i < gNumPieces; i++) {
        if (gPieces[i].row > 2) {
            return false;
        }
        if (gPieces[i].column < (kBoardWidth - 3)) {
            return false;
        }
    }
    return true;
}

function drawBoard() {
    if (gGameInProgress && isTheGameOver()) {
	    endGame();
    }

    gDrawingContext.clearRect(0, 0, kPixelWidth, kPixelHeight);

    gDrawingContext.beginPath();

    /* vertical lines */
    for (var x = 0; x <= kPixelWidth; x += kPieceWidth) {
	gDrawingContext.moveTo(0.5 + x, 0);
	gDrawingContext.lineTo(0.5 + x, kPixelHeight);
    }

    /* horizontal lines */
    for (var y = 0; y <= kPixelHeight; y += kPieceHeight) {
	gDrawingContext.moveTo(0, 0.5 + y);
	gDrawingContext.lineTo(kPixelWidth, 0.5 +  y);
    }

    /* draw it! */
    gDrawingContext.strokeStyle = "#ccc";
    gDrawingContext.stroke();

    for (var i = 0; i < 9; i++) {
	    drawPiece(gPieces[i], i == gSelectedPieceIndex);
    }

    saveGameState();
}

function drawPiece(p, selected) {
    var column = p.column;
    var row = p.row;
    var x = (column * kPieceWidth) + (kPieceWidth/2);
    var y = (row * kPieceHeight) + (kPieceHeight/2);
    var radius = (kPieceWidth/2) - (kPieceWidth/10);
    gDrawingContext.beginPath();
    gDrawingContext.arc(x, y, radius, 0, Math.PI*2, false);
    gDrawingContext.closePath();
    gDrawingContext.strokeStyle = "#000";
    gDrawingContext.stroke();
    if (selected) {
        gDrawingContext.fillStyle = "#000";
        gDrawingContext.fill();
    }
}

if (typeof resumeGame != "function") {
    saveGameState = function() {
	    return false;
    }
    resumeGame = function() {
	    return false;
    }
}

function newGame() {
    gPieces = [new Cell(kBoardHeight - 3, 0),
	       new Cell(kBoardHeight - 2, 0),
	       new Cell(kBoardHeight - 1, 0),
	       new Cell(kBoardHeight - 3, 1),
	       new Cell(kBoardHeight - 2, 1),
	       new Cell(kBoardHeight - 1, 1),
	       new Cell(kBoardHeight - 3, 2),
	       new Cell(kBoardHeight - 2, 2),
	       new Cell(kBoardHeight - 1, 2)];
    gNumPieces = gPieces.length;
    gSelectedPieceIndex = -1;
    gSelectedPieceHasMoved = false;
    gMoveCount = 0;
    gGameInProgress = true;
    drawBoard();
}

function endGame() {
    gSelectedPieceIndex = -1;
    gGameInProgress = false;
}

function sizeWindow() {
    gCanvasElement.width = window.innerWidth;
    gCanvasElement.height = window.innerHeight;
}

function initGame(canvasElement) {
    gCanvasElement = canvasElement;
    gCanvasElement.addEventListener("click", halmaOnClick, false);
    gDrawingContext = gCanvasElement.getContext("2d");
    sizeWindow();

    if (!resumeGame()) {
	    newGame();
    }

    window.onresize = function(event) {
        sizeWindow();
    };

    openWebSocket();
}

/// Web sockets ///

function openWebSocket()
{
    websocket = new WebSocket('ws://127.0.0.1:9003/');
    websocket.onopen = function(evt) { onOpen(evt) };
    websocket.onclose = function(evt) { onClose(evt) };
    websocket.onmessage = function(evt) { onMessage(evt) };
    websocket.onerror = function(evt) { onError(evt) };
}

function onOpen(evt)
{
    websocket.send("setName,Jabe");
    websocket.send("makeLobby,LostCities,Jabe's Lobby");
    websocket.send("msg,Jabe,Hi1");
    websocket.send("msg,General,Hi2");
    websocket.send("msg,Jabe's Lobby,Hi3");
}

function onClose(evt)
{
    alert("DISCONNECTED");
}

function onMessage(evt)
{
    alert(evt.data);
}

function onError(evt)
{
    alert('ERROR: ' + evt.data);
}
