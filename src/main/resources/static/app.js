var gameId = null;
var selectedSquare = null;
var boardState = null;
var validMoves = [];
var gameMode = null;
var myColor = "WHITE"; // Default player is White

var pieceSymbols = {
    'PAWN': '♟', 'ROOK': '♜', 'KNIGHT': '♞', 'BISHOP': '♝', 'QUEEN': '♛', 'KING': '♚'
};

function updateStatus(desc, turn, mode) {
    var statusDiv = document.getElementById('status');
    if (!statusDiv) return;
    
    if (desc.startsWith("GAME_OVER")) {
        statusDiv.innerHTML = "<span style='color:#ff5722; font-weight:bold;'>🎉 " + desc.replace(/_/g, ' ') + "</span>";
        return;
    }

    if (mode === "COMPUTER") {
        statusDiv.innerText = (turn === "WHITE") ? "🟢 Your Turn" : "🤖 Computer's Turn";
    } else {
        statusDiv.innerText = (turn === myColor) ? "🟢 Your Turn" : "👤 Friend's Turn";
    }
}

function createGame(mode) {
    gameMode = mode;
    fetch('/api/game/create/' + mode, { method: 'POST' })
    .then(res => res.json())
    .then(game => { initGameSession(game); });
}

function joinGame() {
    var code = document.getElementById('roomCode').value.trim().toUpperCase();
    if (!code) return alert("Enter valid code");
    gameMode = "FRIEND";
    myColor = "BLACK"; // Joining player takes Black side
    fetch('/api/game/join/' + code, { method: 'POST' })
    .then(res => res.json())
    .then(game => { initGameSession(game); });
}

function initGameSession(game) {
    gameId = game.gameId;
    document.getElementById('menu').style.display = 'none';
    document.getElementById('game-area').style.display = 'block';
    
    // কম্পিউটার মোড হলে রুম কোড সেকশন হাইড থাকবে
    var codeHeader = document.getElementById('code-header');
    if (gameMode === "COMPUTER") {
        codeHeader.style.display = 'none';
    } else {
        codeHeader.style.display = 'block';
        document.getElementById('displayId').innerText = gameId;
    }
    
    renderBoard(game.board.grid);
    updateStatus(game.statusDescription, game.currentTurn, gameMode);
    
    setInterval(refreshGameState, 1500);
}

function refreshGameState() {
    if (!gameId) return;
    fetch('/api/game/status/' + gameId)
    .then(res => res.json())
    .then(game => {
        if (game && game.board) {
            if (!selectedSquare) { 
                renderBoard(game.board.grid);
            }
            updateStatus(game.statusDescription, game.currentTurn, gameMode);
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
            square.dataset.row = r;
            square.dataset.col = c;
            
            var piece = grid[r][c];
            if (piece) {
                square.innerText = pieceSymbols[piece.type] || '';
                square.style.color = (piece.color === 'WHITE') ? '#1e88e5' : '#d32f2f';
            }
            
            // বৈধ চালের ঘরগুলো হাইলাইট করার লজিক
            if (validMoves.some(m => m.row === r && m.col === c)) {
                square.style.backgroundColor = "#baca44"; // Yellow Highlight
            }
            
            (function(row, col) {
                square.onclick = function() { handleSquareClick(row, col); };
            })(r, c);
            
            boardDiv.appendChild(square);
        }
    }
}

// দাবার গুটি অনুযায়ী ক্লায়েন্ট সাইড বেসিক মুভ ক্যালকুলেশন (হাইলাইটের জন্য)
function calculateLocalValidMoves(r, c, piece) {
    var moves = [];
    var dir = (piece.color === "WHITE") ? -1 : 1;

    if (piece.type === "PAWN") {
        var nextRow = r + dir;
        if (nextRow >= 0 && nextRow < 8 && !boardState[nextRow][c]) {
            moves.push({row: nextRow, col: c});
            // Initial double step
            var doubleRow = r + (dir * 2);
            if (((piece.color === "WHITE" && r === 6) || (piece.color === "BLACK" && r === 1)) && !boardState[doubleRow][c]) {
                moves.push({row: doubleRow, col: c});
            }
        }
        // Captures
        if (c > 0 && boardState[r+dir][c-1] && boardState[r+dir][c-1].color !== piece.color) moves.push({row: r+dir, col: c-1});
        if (c < 7 && boardState[r+dir][c+1] && boardState[r+dir][c+1].color !== piece.color) moves.push({row: r+dir, col: c+1});
    } 
    else if (piece.type === "ROOK" || piece.type === "QUEEN") {
        var paths = [[1,0], [-1,0], [0,1], [0,-1]];
        paths.forEach(p => {
            var step = 1;
            while(true) {
                var nr = r + p[0]*step, nc = c + p[1]*step;
                if (nr < 0 || nr >= 8 || nc < 0 || nc >= 8) break;
                if (!boardState[nr][nc]) moves.push({row: nr, col: nc});
                else {
                    if (boardState[nr][nc].color !== piece.color) moves.push({row: nr, col: nc});
                    break;
                }
                step++;
            }
        });
    }
    else if (piece.type === "BISHOP" || piece.type === "QUEEN") {
        var paths = [[1,1], [1,-1], [-1,1], [-1,-1]];
        paths.forEach(p => {
            var step = 1;
            while(true) {
                var nr = r + p[0]*step, nc = c + p[1]*step;
                if (nr < 0 || nr >= 8 || nc < 0 || nc >= 8) break;
                if (!boardState[nr][nc]) moves.push({row: nr, col: nc});
                else {
                    if (boardState[nr][nc].color !== piece.color) moves.push({row: nr, col: nc});
                    break;
                }
                step++;
            }
        });
    }
    else if (piece.type === "KNIGHT") {
        var offsets = [[2,1],[2,-1],[-2,1],[-2,-1],[1,2],[1,-2],[-1,2],[-1,-2]];
        offsets.forEach(o => {
            var nr = r + o[0], nc = c + o[1];
            if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) {
                if (!boardState[nr][nc] || boardState[nr][nc].color !== piece.color) moves.push({row: nr, col: nc});
            }
        });
    }
    else if (piece.type === "KING") {
        for(var i = -1; i <= 1; i++) {
            for(var j = -1; j <= 1; j++) {
                var nr = r + i, nc = c + j;
                if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8 && (i !== 0 || j !== 0)) {
                    if (!boardState[nr][nc] || boardState[nr][nc].color !== piece.color) moves.push({row: nr, col: nc});
                }
            }
        }
    }
    return moves;
}

function handleSquareClick(r, c) {
    if (!selectedSquare) {
        var piece = boardState[r][c];
        if (piece) {
            selectedSquare = { row: r, col: c };
            validMoves = calculateLocalValidMoves(r, c, piece);
            renderBoard(boardState);
        }
    } else {
        // চেক করা হচ্ছে চালটি হাইলাইটেড (বৈধ) ঘরের মধ্যে আছে কিনা
        var isMoveValid = validMoves.some(m => m.row === r && m.col === c);
        
        if (isMoveValid) {
            var move = {
                from: { row: selectedSquare.row, col: selectedSquare.col },
                to: { row: r, col: c }
            };
            
            fetch('/api/game/move/' + gameId, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(move)
            })
            .then(res => res.json())
            .then(game => {
                renderBoard(game.board.grid);
                updateStatus(game.statusDescription, game.currentTurn, gameMode);
            });
        }
        
        selectedSquare = null;
        validMoves = [];
        renderBoard(boardState);
    }
}
