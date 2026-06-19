package com.chess.model;

import java.io.Serializable;

public class Board implements Serializable {
    private final Piece[][] grid;

    public Board() {
        this.grid = new Piece[8][8];
        setupBoard();
    }

    public Piece getPiece(int row, int col) {
        return grid[row][col];
    }

    public void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow >= 0 && fromRow < 8 && fromCol >= 0 && fromCol < 8 &&
                toRow >= 0 && toRow < 8 && toCol >= 0 && toCol < 8) {
            grid[toRow][toCol] = grid[fromRow][fromCol];
            grid[fromRow][fromCol] = null;
        }
    }

    public void makeBotMove() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (grid[r][c] != null && grid[r][c].getColor() == PieceColor.BLACK) {
                    int nextRow = r + 1;
                    if (nextRow < 8 && (grid[nextRow][c] == null || grid[nextRow][c].getColor() == PieceColor.WHITE)) {
                        movePiece(r, c, nextRow, c);
                        return;
                    }
                }
            }
        }
    }

    private void setupBoard() {
        for (int i = 0; i < 8; i++) {
            grid[1][i] = new Piece(PieceType.PAWN, PieceColor.BLACK);
            grid[6][i] = new Piece(PieceType.PAWN, PieceColor.WHITE);
        }
        setupRow(0, PieceColor.BLACK);
        setupRow(7, PieceColor.WHITE);
    }

    private void setupRow(int row, PieceColor color) {
        grid[row][0] = new Piece(PieceType.ROOK, color);
        grid[row][1] = new Piece(PieceType.KNIGHT, color);
        grid[row][2] = new Piece(PieceType.BISHOP, color);
        grid[row][3] = new Piece(PieceType.QUEEN, color);
        grid[row][4] = new Piece(PieceType.KING, color);
        grid[row][5] = new Piece(PieceType.BISHOP, color);
        grid[row][6] = new Piece(PieceType.KNIGHT, color);
        grid[row][7] = new Piece(PieceType.ROOK, color);
    }

    public String toHtmlTable(String mode, String roomCode) {
        StringBuilder html = new StringBuilder();
        html.append("<table style='border-collapse: collapse; margin: 20px auto; border: 5px solid #333;'>");
        for (int r = 0; r < 8; r++) {
            html.append("<tr>");
            for (int c = 0; c < 8; c++) {
                String bg = ((r + c) % 2 == 0) ? "#f0d9b5" : "#b58863";
                Piece p = grid[r][c];
                String sym = (p != null) ? p.getSymbol() : "";
                String txtColor = (p != null && p.getColor() == PieceColor.WHITE) ? "#fff" : "#000";

                html.append(String.format(
                        "<td style='background:%s; width:60px; height:60px; text-align:center; font-size:36px; color:%s; cursor:pointer;' "
                                +
                                "onclick='cellClicked(%d,%d,\"%s\",\"%s\")'>%s</td>",
                        bg, txtColor, r, c, mode, roomCode, sym));
            }
            html.append("</tr>");
        }
        html.append("</table>");
        return html.toString();
    }
}
