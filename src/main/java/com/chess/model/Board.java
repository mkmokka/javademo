package com.chess.model;

public class Board {
    private Piece[][] board;
    private PieceColor currentTurn;

    public Board() {
        board = new Piece[8][8];
        currentTurn = PieceColor.WHITE; // খেলা সবসময় হোয়াইট দিয়ে শুরু হবে
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

    // চালটি বৈধ কি না যাচাই করার মেথড
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = getPiece(fromRow, fromCol);
        if (piece == null) return false;

        // নিজের টার্ন ছাড়া অন্য রঙের ঘুটি চালা যাবে না
        if (piece.getColor() != currentTurn) return false;

        // একই জায়গায় চাল দেওয়া যাবে না
        if (fromRow == toRow && fromCol == toCol) return false;

        // গন্তব্যে নিজের রঙের ঘুটি থাকলে খাওয়া যাবে না
        Piece target = getPiece(toRow, toCol);
        if (target != null && target.getColor() == currentTurn) return false;

        int targetRowDiff = toRow - fromRow;
        int targetColDiff = Math.abs(toCol - fromCol);

        // পিস অনুযায়ী স্ট্যান্ডার্ড নিয়মের লজিক
        switch (piece.getType()) {
            case PAWN:
                int direction = (piece.getColor() == PieceColor.WHITE) ? -1 : 1;
                int startRow = (piece.getColor() == PieceColor.WHITE) ? 6 : 1;
                
                // সোজা ১ ঘর চলা (সামনে খালি থাকতে হবে)
                if (targetColDiff == 0 && targetRowDiff == direction && target == null) return true;
                // প্রথম চালে সোজা ২ ঘর চলা
                if (targetColDiff == 0 && fromRow == startRow && targetRowDiff == 2 * direction 
                    && target == null && getPiece(fromRow + direction, fromCol) == null) return true;
                // কোণাকুণি শত্রু ঘুটি খাওয়া
                if (targetColDiff == 1 && targetRowDiff == direction && target != null) return true;
                return false;

            case ROOK:
                return (fromRow == toRow || fromCol == toCol) && isPathClear(fromRow, fromCol, toRow, toCol);

            case BISHOP:
                return (Math.abs(targetRowDiff) == targetColDiff) && isPathClear(fromRow, fromCol, toRow, toCol);

            case QUEEN:
                boolean isRookMove = (fromRow == toRow || fromCol == toCol);
                boolean isBishopMove = (Math.abs(targetRowDiff) == targetColDiff);
                return (isRookMove || isBishopMove) && isPathClear(fromRow, fromCol, toRow, toCol);

            case KNIGHT:
                return (Math.abs(targetRowDiff) == 1 && targetColDiff == 2) || (Math.abs(targetRowDiff) == 2 && targetColDiff == 1);

            case KING:
                return Math.abs(targetRowDiff) <= 1 && targetColDiff <= 1;
        }
        return false;
    }

    // মাঝপথে অন্য কোনো ঘুটি আছে কি না চেক করার মেথড (Rook, Bishop, Queen এর জন্য)
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

    // চাল কার্যকর করা এবং টার্ন পরিবর্তন
    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (isValidMove(fromRow, fromCol, toRow, toCol)) {
            board[toRow][toCol] = board[fromRow][fromCol];
            board[fromRow][fromCol] = null;
            // টার্ন পরিবর্তন (White -> Black -> White)
            currentTurn = (currentTurn == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
            return true;
        }
        return false;
    }
}
