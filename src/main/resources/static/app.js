let gameId = null;
let stompClient = null;
let selectedSquare = null;
let boardState = null;

const pieceSymbols = {
    'PAWN': '♟', 'ROOK': '♜', 'KNIGHT': '♞', 'BISHOP': '♝', 'QUEEN': '♛', 'KING': '♚'
};

// Base URL helper to ensure compatibility with Render's HTTPS/WSS environment
const getApiBase = () => `${window.location.protocol}//${window.location.host}`;
const getWsBase = () => `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws-chess`;

function createGame(mode) {
    updateStatus("Creating session...");
    fetch(`${getApiBase()}/api/game/create?mode=${mode}`, { 
        method: 'POST',
        headers: { 'Accept': 'application/json' }
    })
    .then(res => {
        if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
        return res.json();
    })
    .then(game => {
        if (game && game.gameId) {
            initGameSession(game);
        } else {
            alert("Failed to read Room Code from server schema.");
        }
    })
    .catch(err => {
        console.error("Creation Error:", err);
        updateStatus("Failed to create room. Check console.");
    });
}

function joinGame() {
    const code = document.getElementById('roomCode').value.trim().toUpperCase();
    if (!code) {
        alert("Please enter a valid room code.");
        return;
    }
    updateStatus("Joining room...");
    fetch(`${getApiBase()}/api/game/join?gameId=${code}`, { 
        method: 'POST',
        headers: { 'Accept': 'application/json' }
    })
    .then(res => {
        if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
        return res.json();
    })
    .then(game => {
        if (game && game.gameId) {
            initGameSession(game);
        } else {
            alert("Room not found or invalid code.");
            updateStatus("Invalid Code");
        }
    })
    .catch(err => {
        console.error("Join Error:", err);
        updateStatus("Connection failed.");
    });
}

function initGameSession(game) {
    gameId = game.gameId;
    document.getElementById('menu').style.display = 'none';
    document.getElementById('game-area').style.display = 'block';
    document.getElementById('displayId').innerText = gameId;
    
    connectWebSocket(gameId);
    renderBoard(game.board.grid);
    updateStatus(game.state.statusDescription || "READY");
}

function connectWebSocket(id) {
    const wsUrl = getWsBase();
    console.log("Connecting WebSocket to:", wsUrl);
    const ws = new WebSocket(wsUrl);
    
    stompClient = Stomp.over(ws);
    // Suppress heavy debug logs in browser console
    stompClient.debug = null; 
    
    stompClient.connect({}, () => {
        console.log("WebSocket connected successfully.");
        stompClient.subscribe(`/topic/game/${id}`, (message) => {
            const updatedGame = JSON.parse(message.body);
            renderBoard(updatedGame.board.grid);
            updateStatus(updatedGame.state.statusDescription);
        });
    }, (error) => {
        console.error("STOMP Protocol Error:", error);
        updateStatus("Live Sync Error. Retrying...");
        setTimeout(() => connectWebSocket(id), 5000); // Auto reconnect
    });
}

function updateStatus(desc) {
    document.getElementById('status').innerText = "Status: " + (desc || "Processing...");
}

function renderBoard(grid) {
    boardState = grid;
    const boardDiv = document.getElementById('board');
    boardDiv.innerHTML = '';
    for (let r = 0; r < 8; r++) {
        for (let c = 0; c < 8; c++) {
            const square = document.createElement('div');
            square.className = `square ${(r + c) % 2 === 0 ? 'light' : 'dark'}`;
            square.dataset.row = r;
            square.dataset.col = c;
            
            const piece = grid[r][c];
            if (piece) {
                square.innerText = pieceSymbols[piece.type] || '';
                if (piece.color === 'WHITE') {
                    square.style.color = '#3b82f6'; // Clean distinct blue
                } else {
                    square.style.color = '#ef4444'; // Clean distinct red
                }
            }
            
            square.onclick = () => handleSquareClick(r, c);
            boardDiv.appendChild(square);
        }
    }
}

function handleSquareClick(r, c) {
    if (!selectedSquare) {
        if (boardState[r][c]) {
            selectedSquare = { row: r, col: c };
            // Optional: highlight selected cell visually
            document.querySelector(`[data-row='${r}'][data-col='${c}']`).style.background = "#baca44";
        }
    } else {
        const move = {
            from: { row: selectedSquare.row, col: selectedSquare.col },
            to: { row: r, col: c }
        };
        
        if (stompClient && stompClient.connected) {
            stompClient.send(`/app/game/${gameId}/move`, {}, JSON.stringify(move));
        } else {
            alert("Lost live connection to cloud server. Refresh page.");
        }
        selectedSquare = null;
        renderBoard(boardState); // Reset highlighting
    }
}
