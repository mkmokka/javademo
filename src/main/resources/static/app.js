var gameId = null;
var wsClient = null;
var selectedSquare = null;
var boardState = null;

var pieceSymbols = {
    'PAWN': '♟', 'ROOK': '♜', 'KNIGHT': '♞', 'BISHOP': '♝', 'QUEEN': '♛', 'KING': '♚'
};

function updateStatus(desc) {
    var statusDiv = document.getElementById('status');
    if (statusDiv) {
        statusDiv.innerText = "Status: " + (desc || "Processing...");
    }
}

function createGame(mode) {
    updateStatus("Creating session...");
    var targetUrl = window.location.origin + '/api/game/create/' + mode;
    
    fetch(targetUrl, { 
        method: 'POST',
        headers: { 
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
    })
    .then(function(res) {
        if (!res.ok) throw new Error("HTTP Error: " + res.status);
        return res.json();
    })
    .then(function(game) {
        if (game && game.gameId) {
            initGameSession(game);
        } else {
            alert("Mismatched data model from server.");
        }
    })
    .catch(function(err) {
        console.error("Game Creation Mismatch:", err);
        updateStatus("Creation failed.");
    });
}

function joinGame() {
    var inputField = document.getElementById('roomCode');
    var code = inputField ? inputField.value.trim().toUpperCase() : "";
    if (!code) {
        alert("Please enter a valid room code.");
        return;
    }
    updateStatus("Joining room...");
    var targetUrl = window.location.origin + '/api/game/join/' + code;

    fetch(targetUrl, { 
        method: 'POST',
        headers: { 
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
    })
    .then(function(res) {
        if (!res.ok) throw new Error("HTTP Error: " + res.status);
        return res.json();
    })
    .then(function(game) {
        if (game && game.gameId) {
            initGameSession(game);
        } else {
            alert("Room code not found.");
            updateStatus("Invalid Code");
        }
    })
    .catch(function(err) {
        console.error("Join Error:", err);
        updateStatus("Connection failed.");
    });
}

function initGameSession(game) {
    gameId = game.gameId;
    document.getElementById('menu').style.display = 'none';
    document.getElementById('game-area').style.display = 'block';
    document.getElementById('displayId').innerText = gameId;
    
    renderBoard(game.board.grid);
    updateStatus(game.state.statusDescription || "READY");
    connectNativeWebSocket(gameId);
}

function connectNativeWebSocket(id) {
    var protocol = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
    var wsUrl = protocol + window.location.host + '/ws-chess/websocket';
    console.log("Connecting Pure Native WebSocket to:", wsUrl);
    
    wsClient = new WebSocket(wsUrl);
    
    wsClient.onopen = function() {
        console.log("Native WebSocket Tunnel Active.");
        // Register connection session
        var joinPayload = {
            type: "SUBSCRIBE",
            destination: "/topic/game/" + id
        };
        wsClient.send(JSON.stringify(joinPayload));
    };
    
    wsClient.onmessage = function(event) {
        try {
            var game = JSON.parse(event.data);
            if (game && game.board) {
                renderBoard(game.board.grid);
                updateStatus(game.state.statusDescription);
            }
        } catch(e) {
            // Suppress binary STOMP heatbeats frames safely
        }
    };
    
    wsClient.onerror = function(err) {
        console.error("WebSocket Mismatch:", err);
    };
    
    wsClient.onclose = function() {
        console.log("Sync lost. Reconnecting...");
        setTimeout(function() { connectNativeWebSocket(id); }, 5000);
    };
}

function renderBoard(grid) {
    boardState = grid;
    var boardDiv = document.getElementById('board');
    if (!boardDiv) return;
    boardDiv.innerHTML = '';
    
    for (var r = 0; r < 8; r++) {
        for (var c = 0; c < 8; c++) {
            var square = document.createElement('div');
            square.className = "square " + ((r + c) % 2 === 0 ? 'light' : 'dark');
            square.dataset.row = r;
            square.dataset.col = c;
            
            var piece = grid[r][c];
            if (piece) {
                square.innerText = pieceSymbols[piece.type] || '';
                square.style.color = (piece.color === 'WHITE') ? '#1e88e5' : '#d32f2f';
            }
            
            (function(row, col) {
                square.onclick = function() { handleSquareClick(row, col); };
            })(r, c);
            
            boardDiv.appendChild(square);
        }
    }
}

function handleSquareClick(r, c) {
    if (!selectedSquare) {
        if (boardState[r][c]) {
            selectedSquare = { row: r, col: c };
            var activeSquare = document.querySelector("[data-row='" + r + "'][data-col='" + c + "']");
            if (activeSquare) activeSquare.style.background = "#baca44";
        }
    } else {
        var move = {
            from: { row: parseInt(selectedSquare.row), col: parseInt(selectedSquare.col) },
            to: { row: parseInt(r), col: parseInt(c) }
        };
        
        // Native clean message delivery packet format
        if (wsClient && wsClient.readyState === WebSocket.OPEN) {
            var msg = "SEND\ndestination:/app/game/" + gameId + "/move\n\n" + JSON.stringify(move) + "\u0000";
            wsClient.send(msg);
        }
        selectedSquare = null;
        renderBoard(boardState);
    }
}
