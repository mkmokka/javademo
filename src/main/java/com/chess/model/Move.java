package com.chess.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Move {
    private Position from;
    private Position to;

    public Move() {}

    public Move(Position from, Position to) {
        this.from = from;
        this.to = to;
    }

    @JsonProperty("from")
    public Position from() { return from; }

    @JsonProperty("to")
    public Position to() { return to; }

    public void setFrom(Position from) { this.from = from; }
    public void setTo(Position to) { this.to = to; }
}
