// Global states wrapped cleanly
window.gameId = null;
window.stompClient = null;
window.selectedSquare = null;
window.boardState = null;

const pieceSymbols = {
    'PAWN': '♟', 'ROOK': '♜', 'KNIGHT': '♞', 'BISHOP': '♝', 'QUEEN': '♛', 'KING': '♚'
};

// Safe fallback utility for URLs
function getApiUrl(path) {
    return window.location.origin + path;
}

function updateStatus(desc) {
    const statusDiv = document.getElementById('status');
    if (statusDiv) {
        statusDiv.innerText = "Status: " + (desc || "Processing...");
    }
}

function createGame(mode) {
    updateStatus("Creating session...");
    const url = getApiUrl('/api/game/create/' + mode);
    
    fetch(url, { 
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
            alert("Mismatched response configuration from cloud.");
        }
    })
    .catch(function(err) {
        console.error("Game Creation Error:", err);
        updateStatus("Creation failed.");
    });
}

function joinGame() {
    const inputField = document.getElementById('roomCode');
    const code = inputField ? inputField.value.trim().toUpperCase() : "";
    if (!code) {
        alert("Please enter a valid room code.");
        return;
    }
    updateStatus("Joining room...");
    const url = getApiUrl('/api/game/join/' + code);

    fetch(url, { 
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
    window.gameId = game.gameId;
    document.getElementById('menu').style.display = 'none';
    document.getElementById('game-area').style.display = 'block';
    document.getElementById('displayId').innerText = window.gameId;
    
    renderBoard(game.board.grid);
    updateStatus(game.state.statusDescription || "READY");
    connectWebSocket(window.gameId);
}

function connectWebSocket(id) {
    const baseSecureUrl = window.location.origin + '/ws-chess';
    console.log("Connecting SockJS secure tunnel to:", baseSecureUrl);
    
    const socket = new SockJS(baseSecureUrl, null, {transports: ['websocket', 'xhr-streaming', 'xhr-polling']});
    window.stompClient = Stomp.over(socket);
    window.stompClient.debug = null; 
    
    window.stompClient.connect({}, function() {
        console.log("STOMP Live Tunnel Connected.");
        window.stompClient.subscribe('/topic/game/' + id, function(message) {
            const updatedGame = JSON.parse(message.body);
            renderBoard(updatedGame.board.grid);
            updateStatus(updatedGame.state.statusDescription);
        });
    }, function(error) {
        console.error("STOMP Error:", error);
        updateStatus("Sync lost. Retrying...");
        setTimeout(function() { connectWebSocket(id); }, 5000);
    });
}

function renderBoard(grid) {
    window.boardState = grid;
    const boardDiv = document.getElementById('board');
    if (!boardDiv) return;
    boardDiv.innerHTML = '';
    
    for (let r = 0; r < 8; r++) {
        for (let c = 0; c < 8; c++) {
            const square = document.createElement('div');
            square.className = "square " + ((r + c) % 2 === 0 ? 'light' : 'dark');
            square.dataset.row = r;
            square.dataset.col = c;
            
            const piece = grid[r][c];
            if (piece) {
                square.innerText = pieceSymbols[piece.type] || '';
                square.style.color = (piece.color === 'WHITE') ? '#1e88e5' : '#d32f2f';
            }
            
            square.onclick = function() { handleSquareClick(r, c); };
            boardDiv.appendChild(square);
        }
    }
}

function handleSquareClick(r, c) {
    if (!window.selectedSquare) {
        if (window.boardState[r][c]) {
            window.selectedSquare = { row: r, col: c };
            const activeSquare = document.querySelector("[data-row='" + r + "'][data-col='" + c + "']");
            if (activeSquare) activeSquare.style.background = "#baca44";
        }
    } else {
        const move = {
            from: { row: parseInt(window.selectedSquare.row), col: parseInt(window.selectedSquare.col) },
            to: { row: parseInt(r), col: parseInt(c) }
        };
        
        if (window.stompClient && window.stompClient.connected) {
            window.stompClient.send('/app/game/' + window.gameId + '/move', {}, JSON.stringify(move));
        }
        window.selectedSquare = null;
        renderBoard(window.boardState);
    }
}
