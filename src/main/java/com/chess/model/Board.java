package com.chess.model;

public class Board {
    private Piece[][] board;
    private PieceColor currentTurn;

    public Board() {
        board = new Piece[8][8];
        currentTurn = PieceColor.WHITE; 
        initializeBoard();
    }

    private void initializeBoard() {
        // Black Pieces
        board[0][0] = new Piece(PieceType.ROOK, PieceColor.BLACK);
        board[0][1] = new Piece(PieceType.KNIGHT, PieceColor.BLACK);
        board[0][2] = new Piece(PieceType.BISHOP, PieceColor.BLACK);
        board[0][3] = new Piece(PieceType.QUEEN, PieceColor.BLACK);
        board[0][4] = new Piece(PieceType.KING, PieceColor.BLACK);
        board[0][5] = new Piece(PieceType.BISHOP, PieceColor.BLACK);
        board[0][6] = new Piece(PieceType.KNIGHT, PieceColor.BLACK);
        board[0][7] = new Piece(PieceType.ROOK, PieceColor.BLACK);
        for (int i = 0; i < 8; i++) board[1][i] = new Piece(PieceType.PAWN, PieceColor.BLACK);

        // White Pieces
        for (int i = 0; i < 8; i++) board[6][i] = new Piece(PieceType.PAWN, PieceColor.WHITE);
        board[7][0] = new Piece(PieceType.ROOK, PieceColor.WHITE);
        board[7][1] = new Piece(PieceType.KNIGHT, PieceColor.WHITE);
        board[7][2] = new Piece(PieceType.BISHOP, PieceColor.WHITE);
        board[7][3] = new Piece(PieceType.QUEEN, PieceColor.WHITE);
        board[7][4] = new Piece(PieceType.KING, PieceColor.WHITE);
        board[7][5] = new Piece(PieceType.BISHOP, PieceColor.WHITE);
        board[7][6] = new Piece(PieceType.KNIGHT, PieceColor.WHITE);
        board[7][7] = new Piece(PieceType.ROOK, PieceColor.WHITE);
    }

    public Piece getPiece(int row, int col) {
        if (row < 0 || row > 7 || col < 0 || col > 7) return null;
        return board[row][col];
    }

    public PieceColor getCurrentTurn() {
        return currentTurn;
    }

    // স্ট্যান্ডার্ড নিয়ম অনুযায়ী চালের বৈধতা চেক
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = getPiece(fromRow, fromCol);
        if (piece == null || piece.getColor() != currentTurn) return false;
        if (fromRow == toRow && fromCol == toCol) return false;

        Piece target = getPiece(toRow, toCol);
        if (target != null && target.getColor() == currentTurn) return false;

        int rowDiff = toRow - fromRow;
        int colDiff = Math.abs(toCol - fromCol);

        switch (piece.getType()) {
            case PAWN:
                int dir = (piece.getColor() == PieceColor.WHITE) ? -1 : 1;
                int startRow = (piece.getColor() == PieceColor.WHITE) ? 6 : 1;
                if (colDiff == 0 && rowDiff == dir && target == null) return true;
                if (colDiff == 0 && fromRow == startRow && rowDiff == 2 * dir && target == null && getPiece(fromRow + dir, fromCol) == null) return true;
                if (colDiff == 1 && rowDiff == dir && target != null) return true;
                return false;

            case ROOK:
                return (fromRow == toRow || fromCol == toCol) && isPathClear(fromRow, fromCol, toRow, toCol);

            case BISHOP:
                return (Math.abs(rowDiff) == colDiff) && isPathClear(fromRow, fromCol, toRow, toCol);

            case QUEEN:
                return ((fromRow == toRow || fromCol == toCol) || (Math.abs(rowDiff) == colDiff)) && isPathClear(fromRow, fromCol, toRow, toCol);

            case KNIGHT:
                return (Math.abs(rowDiff) == 1 && colDiff == 2) || (Math.abs(rowDiff) == 2 && colDiff == 1);

            case KING:
                return Math.abs(rowDiff) <= 1 && colDiff <= 1;
        }
        return false;
    }

    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);
        int currRow = fromRow + rowStep;
        int currCol = fromCol + colStep;
        while (currRow != toRow || currCol != toCol) {
            if (board[currRow][currCol] != null) return false;
            currRow += rowStep;
            currCol += colStep;
        }
        return true;
    }

    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (isValidMove(fromRow, fromCol, toRow, toCol)) {
            board[toRow][toCol] = board[fromRow][fromCol];
            board[fromRow][fromCol] = null;
            currentTurn = (currentTurn == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
            return true;
        }
        return false;
    }

    // ডকার এবং ওয়েব ইন্টারফেসে সুন্দর গ্রাফিক্স দেখানোর জন্য HTML রেন্ডারার
    public String toHtmlTable(String selectedId, String validMovesJson) {
        StringBuilder html = new StringBuilder();
        html.append("<style>")
            .append(".chess-board { border-collapse: collapse; margin: 20px auto; box-shadow: 0 5px 15px rgba(0,0,0,0.3); } ")
            .append(".chess-board td { width: 65px; height: 65px; text-align: center; vertical-align: middle; font-size: 30px; cursor: pointer; transition: 0.2s; } ")
            .append(".light { background-color: #f0d9b5; } .dark { background-color: #b58863; } ")
            .append(".selected { background-color: #ffeb3b !important; } ")
            .append(".valid-move { box-shadow: inset 0 0 0 4px #4caf50; } ")
            .append("a { text-decoration: none; display: block; width: 100%; height: 100%; line-height: 65px; } ")
            .append("</style>");

        html.append("<table class='chess-board'>");
        for (int r = 0; r < 8; r++) {
            html.append("<tr>");
            for (int c = 0; c < 8; c++) {
                Piece p = getPiece(r, c);
                String cellId = r + "-" + c;
                String cellClass = ((r + c) % 2 == 0) ? "light" : "dark";
                
                if (cellId.equals(selectedId)) {
                    cellClass += " selected";
                }
                
                // সিলেক্টেড ঘুটির জন্য কোন কোন ঘর বৈধ তা চেক করে ক্লাস বসানো
                if (selectedId != null && !selectedId.isEmpty()) {
                    String[] parts = selectedId.split("-");
                    int fromR = Integer.parseInt(parts[0]);
                    int fromC = Integer.parseInt(parts[1]);
                    if (isValidMove(fromR, fromC, r, c)) {
                        cellClass += " valid-move";
                    }
                }

                html.append("<td class='").append(cellClass).append("'>");
                html.append("<a href='/?click=").append(cellId).append("'>");
                
                if (p != null) {
                    html.append(getUnicodeSymbol(p));
                }
                
                html.append("</a></td>");
            }
            html.append("</tr>");
        }
        html.append("</table>");
        return html.toString();
    }

    // টেক্সটের বদলে আসল সুন্দর চেস আইকন দেখানোর ইউনিকেড মেথড
    private String getUnicodeSymbol(Piece p) {
        boolean isWhite = p.getColor() == PieceColor.WHITE;
        switch (p.getType()) {
            case KING: return isWhite ? "<span style='color:#fff; filter: drop-shadow(1px 1px 1px #000);'>♔</span>" : "♚";
            case QUEEN: return isWhite ? "<span style='color:#fff; filter: drop-shadow(1px 1px 1px #000);'>♕</span>" : "♛";
            case ROOK: return isWhite ? "<span style='color:#fff; filter: drop-shadow(1px 1px 1px #000);'>♖</span>" : "♜";
            case BISHOP: return isWhite ? "<span style='color:#fff; filter: drop-shadow(1px 1px 1px #000);'>♗</span>" : "♝";
            case KNIGHT: return isWhite ? "<span style='color:#fff; filter: drop-shadow(1px 1px 1px #000);'>♘</span>" : "♞";
            case PAWN: return isWhite ? "<span style='color:#fff; filter: drop-shadow(1px 1px 1px #000);'>♙</span>" : "♟";
            default: return "";
        }
    }
}
