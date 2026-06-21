package com.chess.patterns.state;

public interface GameState {
    String getStatusDescription();
    boolean canMove();
}
