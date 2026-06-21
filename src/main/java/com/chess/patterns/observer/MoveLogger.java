package com.chess.patterns.observer;

import com.chess.model.Move;

public class MoveLogger implements GameObserver {
    public void onMoveExecuted(String gameId, Move move) {
        System.out.println("Game [" + gameId + "] Move Logged: From (" 
            + move.from().row() + "," + move.from().col() + ") to (" 
            + move.to().row() + "," + move.to().col() + ")");
    }
}
