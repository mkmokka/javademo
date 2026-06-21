let gameId = null;
let stompClient = null;
let selectedSquare = null;
let boardState = null;

const pieceSymbols = {
    'PAWN': '♟', 'ROOK': '♜', 'KNIGHT': '♞', 'BISHOP': '♝', 'QUEEN': '♛', 'KING': '♚'
};

// API বেস পাথ রিটার্ন করার ফাংশন (যা আগে মিসিং ছিল)
const getApiBase = () => `${window.location.protocol}//${window.location.host}`;

function createGame(mode) {
    updateStatus("Creating session...");
    fetch(`${getApiBase()}/api/game/create/${mode}`, { 
        method: 'POST',
        headers: { 
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
    })
    .then(res => {
        if (!res.ok) throw new Error(`HTTP Error: ${res.status}`);
        return res.json();
    })
    .then(game => {
        if (game && game.gameId) {
            initGameSession(game);
        } else {
            alert("Mismatched response from cloud server.");
        }
    })
    .catch(err => {
        console.error("Game Creation Error:", err);
        updateStatus("Creation failed.");
    });
}

function joinGame() {
    const code = document.getElementById('roomCode').value.trim().toUpperCase();
    if (!code) {
        alert("Please enter a valid room code.");
        return;
    }
    updateStatus("Joining room...");
    fetch(`${getApiBase()}/api/game/join/${code}`, { 
        method: 'POST',
        headers: { 
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
    })
    .then(res => {
        if (!res.ok) throw new Error(`HTTP Error: ${res.status}`);
        return res.json();
    })
    .then(game => {
        if (game && game.gameId) {
            initGameSession(game);
        } else {
            alert("Room code not found.");
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
    
    renderBoard(game.board.grid);
    updateStatus(game.state.statusDescription || "READY");
    connectWebSocket(gameId);
}

function connectWebSocket(id) {
    const baseSecureUrl = window.location.origin + '/ws-chess';
    console.log("Connecting SockJS secure tunnel to:", baseSecureUrl);
    
    const socket = new SockJS(baseSecureUrl, null, {transports: ['websocket', 'xhr-streaming', 'xhr-polling']});
    stompClient = Stomp.over(socket);
    stompClient.debug = null; 
    
    stompClient.connect({}, () => {
        console.log("STOMP Live Connected.");
        stompClient.subscribe(`/topic/game/${id}`, (message) => {
            const updatedGame = JSON.parse(message.body);
            renderBoard(updatedGame.board.grid);
            updateStatus(updatedGame.state.statusDescription);
        });
    }, (error) => {
        console.error("STOMP Error:", error);
        updateStatus("Sync lost. Retrying...");
        setTimeout(() => connectWebSocket(id), 5000);
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
            from: { row: parseInt(selectedSquare.row), col: parseInt(selectedSquare.col) },
            to: { row: parseInt(r), col: parseInt(c) }
        };
        
        if (stompClient && stompClient.connected) {
            stompClient.send(`/app/game/${gameId}/move`, {}, JSON.stringify(move));
        }
        selectedSquare = null;
        renderBoard(boardState);
    }
}
