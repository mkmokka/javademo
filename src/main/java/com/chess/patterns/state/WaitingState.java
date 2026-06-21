package com.chess.patterns.state;

public class WaitingState implements GameState {
    public String getStatusDescription() { return "WAITING_FOR_PLAYER"; }
    public boolean canMove() { return false; }
}
