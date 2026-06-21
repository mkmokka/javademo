package com.chess.patterns.state;

public class GameOverState implements GameState {
    private final String winner;
    public GameOverState(String winner) { this.winner = winner; }
    public String getStatusDescription() { return "GAME_OVER_WINNER_" + winner; }
    public boolean canMove() { return false; }
}
