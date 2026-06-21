package com.chess.service;

import com.chess.model.*;
import com.chess.patterns.state.InProgressState;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    private final Map<String, GameSession> games = new ConcurrentHashMap<>();

    public GameSession createGame(String mode) {
        GameSession session = new GameSession(mode);
        games.put(session.getGameId(), session);
        return session;
    }

    public GameSession joinGame(String gameId) {
        GameSession session = games.get(gameId);
        if (session != null && "FRIEND".equalsIgnoreCase(session.getMode()) && !(session.getState() instanceof InProgressState)) {
            session.setState(new InProgressState());
        }
        return session;
    }

    public GameSession executePlayerMove(String gameId, Move move) {
        GameSession session = games.get(gameId);
        if (session != null && session.makeMove(move)) {
            if ("COMPUTER".equalsIgnoreCase(session.getMode()) && session.getCurrentTurn() == PieceColor.BLACK) {
                Move botMove = session.getBlackStrategy().calculateMove(session.getBoard());
                if (botMove != null) {
                    session.makeMove(botMove);
                }
            }
        }
        return session;
    }
}
