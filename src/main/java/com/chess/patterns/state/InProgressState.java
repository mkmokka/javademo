package com.chess.patterns.state;

public class InProgressState implements GameState {
    public String getStatusDescription() { return "IN_PROGRESS"; }
    public boolean canMove() { return true; }
}
