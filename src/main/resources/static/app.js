var gameId = null;
var selectedSquare = null;
var boardState = null;

var pieceSymbols = {
    'PAWN': '♟', 'ROOK': '♜', 'KNIGHT': '♞', 'BISHOP': '♝', 'QUEEN': '♛', 'KING': '♚'
};

function updateStatus(desc) {
    var statusDiv = document.getElementById('status');
    if (statusDiv) statusDiv.innerText = "Status: " + (desc || "Processing...");
}

function createGame(mode) {
    updateStatus("Creating session...");
    fetch('/api/game/create/' + mode, { method: 'POST' })
    .then(function(res) { return res.json(); })
    .then(function(game) { initGameSession(game); })
    .catch(function(err) { updateStatus("Creation failed."); });
}

function joinGame() {
    var code = document.getElementById('roomCode').value.trim().toUpperCase();
    if (!code) return alert("Enter code");
    updateStatus("Joining...");
    fetch('/api/game/join/' + code, { method: 'POST' })
    .then(function(res) { return res.json(); })
    .then(function(game) { initGameSession(game); })
    .catch(function(err) { updateStatus("Join failed."); });
}

function initGameSession(game) {
    gameId = game.gameId;
    document.getElementById('menu').style.display = 'none';
    document.getElementById('game-area').style.display = 'block';
    document.getElementById('displayId').innerText = gameId;
    
    renderBoard(game.board.grid);
    updateStatus(game.statusDescription);
    
    setInterval(refreshGameState, 2000);
}

function refreshGameState() {
    if (!gameId) return;
    fetch('/api/game/status/' + gameId)
    .then(function(res) { return res.json(); })
    .then(function(game) {
        if (game && game.board) {
            renderBoard(game.board.grid);
            updateStatus(game.statusDescription);
        }
    });
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
        }
    } else {
        var move = {
            from: { row: selectedSquare.row, col: selectedSquare.col },
            to: { row: r, col: c }
        };
        
        fetch('/api/game/move/' + gameId, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(move)
        })
        .then(function(res) { return res.json(); })
        .then(function(game) {
            renderBoard(game.board.grid);
            updateStatus(game.statusDescription);
        });
        
        selectedSquare = null;
        renderBoard(boardState);
    }
}
