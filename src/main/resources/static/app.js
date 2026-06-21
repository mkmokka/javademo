var gameId = null;
var selectedSquare = null;
var boardState = null;
var validMoves = [];
var gameMode = null;
var myColor = "WHITE"; // Default creating player is White
var currentTurnState = "WHITE";
var promotionPending = null;

var pieceSymbols = {
    'PAWN': '♟', 'ROOK': '♜', 'KNIGHT': '♞', 'BISHOP': '♝', 'QUEEN': '♛', 'KING': '♚'
};

function updateStatus(desc, turn, mode) {
    currentTurnState = turn;
    var statusDiv = document.getElementById('status');
    if (!statusDiv) return;
    
    if (desc && desc.startsWith("GAME_OVER")) {
        statusDiv.innerHTML = "<span style='color:#ff5722; font-weight:bold;'>🎉 " + desc.replace(/_/g, ' ') + "</span>";
        return;
    }

    if (mode === "COMPUTER") {
        statusDiv.innerText = (turn === "WHITE") ? "🟢 Your Turn (White)" : "🤖 Computer's Turn (Black)";
    } else {
        statusDiv.innerText = (turn === myColor) ? "🟢 Your Turn" : "👤 Friend's Turn";
    }
}

function createGame(mode) {
    gameMode = mode;
    myColor = "WHITE"; 
    fetch('/api/game/create/' + mode, { method: 'POST' })
    .then(res => res.json())
    .then(game => { initGameSession(game); });
}

function joinGame() {
    var code = document.getElementById('roomCode').value.trim().toUpperCase();
    if (!code) return alert("Enter valid code");
    gameMode = "FRIEND";
    myColor = "BLACK"; // Joining friend is always Black
    fetch('/api/game/join/' + code, { method: 'POST' })
    .then(res => res.json())
    .then(game => { initGameSession(game); });
}

function initGameSession(game) {
    gameId = game.gameId;
    document.getElementById('menu').style.display = 'none';
    document.getElementById('game-area').style.display = 'block';
    
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
    if (!gameId || promotionPending) return;
    fetch('/api/game/status/' + gameId)
    .then(res => res.json())
    .then(game => {
        if (game && game.board && !selectedSquare) {
            renderBoard(game.board.grid);
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
            
            // বর্ডার হাইলাইট লজিক
            var moveInfo = validMoves.find(m => m.row === r && m.col === c);
            if (moveInfo) {
                if (moveInfo.isCastling) {
                    square.style.border = "4px solid #9c27b0"; // ক্যাসলিং এর জন্য বেগুনী বর্ডার
                } else {
                    square.style.border = "4px solid #4caf50"; // সাধারণ চালের জন্য সবুজ বارهای বর্ডার
                }
                square.style.boxSizing = "border-box";
            } else {
                square.style.border = "none";
            }
            
            (function(row, col) {
                square.onclick = function() { handleSquareClick(row, col); };
            })(r, c);
            
            boardDiv.appendChild(square);
        }
    }
}

function calculateLocalValidMoves(r, c, piece) {
    var moves = [];
    var dir = (piece.color === "WHITE") ? -1 : 1;

    if (piece.type === "PAWN") {
        var nextRow = r + dir;
        if (nextRow >= 0 && nextRow < 8 && !boardState[nextRow][c]) {
            moves.push({row: nextRow, col: c, isCastling: false});
            var doubleRow = r + (dir * 2);
            if (((piece.color === "WHITE" && r === 6) || (piece.color === "BLACK" && r === 1)) && !boardState[doubleRow][c]) {
                moves.push({row: doubleRow, col: c, isCastling: false});
            }
        }
        if (c > 0 && boardState[r+dir][c-1] && boardState[r+dir][c-1].color !== piece.color) moves.push({row: r+dir, col: c-1, isCastling: false});
        if (c < 7 && boardState[r+dir][c+1] && boardState[r+dir][c+1].color !== piece.color) moves.push({row: r+dir, col: c+1, isCastling: false});
    } 
    else if (piece.type === "ROOK" || piece.type === "QUEEN") {
        var paths = [[1,0], [-1,0], [0,1], [0,-1]];
        paths.forEach(p => {
            var step = 1;
            while(true) {
                var nr = r + p[0]*step, nc = c + p[1]*step;
                if (nr < 0 || nr >= 8 || nc < 0 || nc >= 8) break;
                if (!boardState[nr][nc]) moves.push({row: nr, col: nc, isCastling: false});
                else {
                    if (boardState[nr][nc].color !== piece.color) moves.push({row: nr, col: nc, isCastling: false});
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
                if (!boardState[nr][nc]) moves.push({row: nr, col: nc, isCastling: false});
                else {
                    if (boardState[nr][nc].color !== piece.color) moves.push({row: nr, col: nc, isCastling: false});
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
                if (!boardState[nr][nc] || boardState[nr][nc].color !== piece.color) moves.push({row: nr, col: nc, isCastling: false});
            }
        });
    }
    else if (piece.type === "KING") {
        for(var i = -1; i <= 1; i++) {
            for(var j = -1; j <= 1; j++) {
                var nr = r + i, nc = c + j;
                if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8 && (i !== 0 || j !== 0)) {
                    if (!boardState[nr][nc] || boardState[nr][nc].color !== piece.color) moves.push({row: nr, col: nc, isCastling: false});
                }
            }
        }
        // ক্যাসলিং চালের স্পেশাল বর্ডার সাজেশন লজিক
        if (piece.color === "WHITE" && r === 7 && c === 4) {
            if (!boardState[7][5] && !boardState[7][6] && boardState[7][7] && boardState[7][7].type === "ROOK") moves.push({row: 7, col: 6, isCastling: true});
            if (!boardState[7][3] && !boardState[7][2] && !boardState[7][1] && boardState[7][0] && boardState[7][0].type === "ROOK") moves.push({row: 7, col: 2, isCastling: true});
        }
        if (piece.color === "BLACK" && r === 0 && c === 4) {
            if (!boardState[0][5] && !boardState[0][6] && boardState[0][7] && boardState[0][7].type === "ROOK") moves.push({row: 0, col: 6, isCastling: true});
            if (!boardState[0][3] && !boardState[0][2] && !boardState[0][1] && boardState[0][0] && boardState[0][0].type === "ROOK") moves.push({row: 0, col: 2, isCastling: true});
        }
    }
    return moves;
}

function handleSquareClick(r, c) {
    if (promotionPending) return;

    if (!selectedSquare) {
        var piece = boardState[r][c];
        if (!piece) return;
        
        // 🔒 টার্ন লক প্রোটেকশন চেক
        if (piece.color !== currentTurnState || piece.color !== myColor) {
            alert("It's not your turn or not your piece!");
            return;
        }
        
        selectedSquare = { row: r, col: c };
        validMoves = calculateLocalValidMoves(r, c, piece);
        renderBoard(boardState);
    } else {
        var isMoveValid = validMoves.some(m => m.row === r && m.col === c);
        
        if (isMoveValid) {
            var activePiece = boardState[selectedSquare.row][selectedSquare.col];
            
            // ♟️ বোড়ে শেষ লাইনে পৌঁছালে প্রোমোশন পপ-আপ ট্রিগার
            if (activePiece.type === "PAWN" && (r === 0 || r === 7)) {
                promotionPending = { from: selectedSquare, to: { row: r, col: c } };
                document.getElementById('promotion-modal').style.display = 'block';
                return;
            }
            
            sendMoveToServer(selectedSquare, { row: r, col: c }, null);
        }
        
        selectedSquare = null;
        validMoves = [];
        renderBoard(boardState);
    }
}

function selectPromotion(type) {
    if (!promotionPending) return;
    document.getElementById('promotion-modal').style.display = 'none';
    sendMoveToServer(promotionPending.from, promotionPending.to, type);
    promotionPending = null;
    selectedSquare = null;
    validMoves = [];
}

function sendMoveToServer(from, to, promoType) {
    var move = { from: from, to: to, promotionType: promoType };
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
