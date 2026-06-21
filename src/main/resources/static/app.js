let gameId = null;
let stompClient = null;
let selectedSquare = null;
let boardState = null;

const pieceSymbols = {
    'PAWN': '♟', 'ROOK': '♜', 'KNIGHT': '♞', 'BISHOP': '♝', 'QUEEN': '♛', 'KING': '♚'
};

function createGame(mode) {
    fetch(`/api/game/create?mode=${mode}`, { method: 'POST' })
        .then(res => res.json())
        .then(game => {
            initGameSession(game);
        });
}

function joinGame() {
    const code = document.getElementById('roomCode').value;
    fetch(`/api/game/join?gameId=${code}`, { method: 'POST' })
        .then(res => res.json())
        .then(game => {
            initGameSession(game);
        });
}

function initGameSession(game) {
    gameId = game.gameId;
    document.getElementById('menu').style.display = 'none';
    document.getElementById('game-area').style.display = 'block';
    document.getElementById('displayId').innerText = gameId;
    
    connectWebSocket(gameId);
    renderBoard(game.board.grid);
    updateStatus(game.state.statusDescription);
}

function connectWebSocket(id) {
    const ws = new WebSocket(`ws://${window.location.host}/ws-chess`);
    stompClient = Stomp.over(ws);
    stompClient.connect({}, () => {
        stompClient.subscribe(`/topic/game/${id}`, (message) => {
            const game = JSON.parse(message.body);
            renderBoard(game.board.grid);
            updateStatus(game.state.statusDescription);
        });
    });
}

function updateStatus(desc) {
    document.getElementById('status').innerText = "Status: " + desc;
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
                square.innerText = pieceSymbols[piece.type];
                if (piece.color === 'WHITE') square.style.color = 'blue';
                else square.style.color = 'red';
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
        }
    } else {
        const move = {
            from: { row: selectedSquare.row, col: selectedSquare.col },
            to: { row: r, col: c }
        };
        stompClient.send(`/app/game/${gameId}/move`, {}, JSON.stringify(move));
        selectedSquare = null;
    }
}
