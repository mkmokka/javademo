let gameId = null;
let stompClient = null;
let selectedSquare = null;
let boardState = null;

const pieceSymbols = {
    'PAWN': '♟', 'ROOK': '♜', 'KNIGHT': '♞', 'BISHOP': '♝', 'QUEEN': '♛', 'KING': '♚'
};

function createGame(mode) {
    updateStatus("Creating session...");
    // Render proxy ফ্রেন্ডলি রিলেটিভ পাথ ব্যবহার করা হলো
    fetch(`/api/game/create/${mode}`, { 
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
    .then(res => {
        if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
        return res.json();
    })
    .then(game => {
        if (game && game.gameId) {
            initGameSession(game);
        } else {
            alert("Mismatched data scheme received.");
        }
    })
    .catch(err => {
        console.error("Creation Mismatch:", err);
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
    fetch(`/api/game/join/${code}`, { 
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
    .then(res => {
        if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
        return res.json();
    })
    .then(game => {
        if (game && game.gameId) {
            initGameSession(game);
        } else {
            alert("Room code not found.");
        }
    })
    .catch(err => {
        console.error("Join Failure:", err);
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
    // SockJS রিলেটিভ এন্ডপয়েন্ট বাইন্ডিং
    const socket = new SockJS('/ws-chess');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; 
    
    stompClient.connect({}, () => {
        console.log("STOMP Tunnel Active.");
        stompClient.subscribe(`/topic/game/${id}`, (message) => {
            const updatedGame = JSON.parse(message.body);
            renderBoard(updatedGame.board.grid);
            updateStatus(updatedGame.state.statusDescription);
        });
    }, (error) => {
        console.error("STOMP error:", error);
        updateStatus("Reconnecting sync...");
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
            from: { row: selectedSquare.row, col: selectedSquare.col },
            to: { row: r, col: c }
        };
        
        if (stompClient && stompClient.connected) {
            stompClient.send(`/app/game/${gameId}/move`, {}, JSON.stringify(move));
        }
        selectedSquare = null;
        renderBoard(boardState);
    }
}
