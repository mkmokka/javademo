package com.chess.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Position {
    private int row;
    private int col;

    // Default constructor for Jackson parsing
    public Position() {}

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @JsonProperty("row")
    public int row() { return row; }

    @JsonProperty("col")
    public int col() { return col; }

    public void setRow(int row) { this.row = row; }
    public void setCol(int col) { this.col = col; }

    public boolean isValid() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
