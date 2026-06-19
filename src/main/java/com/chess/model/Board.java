package com.chess.model;

public class Board {
    private Piece[][] board;
    private PieceColor currentTurn;

    public Board() {
        board = new Piece[8][8];
        currentTurn = PieceColor.WHITE; // খেলা হোয়াইট দিয়ে শুরু হবে
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

    // স্ট্যান্ডার্ড চেস নিয়ম অনুযায়ী চাল বৈধ কিনা চেক
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
}
