let gameId = null;
let stompClient = null;
let selectedSquare = null;
let boardState = null;

const pieceSymbols = {
    'PAWN': '♟', 'ROOK': '♜', 'KNIGHT': '♞', 'BISHOP': '♝', 'QUEEN': '♛', 'KING': '♚'
};

const getApiBase = () => `${window.location.protocol}//${window.location.host}`;

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
            alert("Failed to create room instance.");
        }
    })
    .catch(err => {
        console.error("Creation Error:", err);
        updateStatus("Failed to create room.");
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
    // Standard secure/unsecure fallbacks using SockJS protocol container
    const socketEndpoint = `${getApiBase()}/ws-chess`;
    console.log("Initializing SockJS tunnel on:", socketEndpoint);
    
    const socket = new SockJS(socketEndpoint);
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // Clean production environment logging
    
    stompClient.connect({}, () => {
        console.log("STOMP Session established over SockJS.");
        stompClient.subscribe(`/topic/game/${id}`, (message) => {
            const updatedGame = JSON.parse(message.body);
            renderBoard(updatedGame.board.grid);
            updateStatus(updatedGame.state.statusDescription);
        });
    }, (error) => {
        console.error("Transport Protocol failure:", error);
        updateStatus("Sync lost. Reconnecting...");
        setTimeout(() => connectWebSocket(id), 4000);
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
                // Blue color for White side, Deep Crimson/Red for Black side
                square.style.color = (piece.color === 'WHITE') ? '#1e88e5' : '#d32f2f';
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
            alert("Reconnecting to Cloud Server...");
        }
        selectedSquare = null;
        renderBoard(boardState);
    }
}
