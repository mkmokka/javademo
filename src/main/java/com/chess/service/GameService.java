package com.chess.service;

import com.chess.model.*;
import com.chess.patterns.strategy.RandomBotStrategy;
import com.chess.patterns.strategy.MoveStrategy;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    private final Map<String, GameSession> games = new ConcurrentHashMap<>();
    private final MoveStrategy botStrategy = new RandomBotStrategy();

    public GameSession createGame(String mode) {
        GameSession session = new GameSession(mode);
        games.put(session.getGameId(), session);
        return session;
    }

    public GameSession joinGame(String gameId) {
        GameSession session = games.get(gameId);
        if (session != null && "FRIEND".equalsIgnoreCase(session.getMode()) && "WAITING_FOR_PLAYER".equals(session.getStatusDescription())) {
            session.setStatusDescription("IN_PROGRESS");
        }
        return session;
    }

    public GameSession executePlayerMove(String gameId, Move move) {
        GameSession session = games.get(gameId);
        if (session != null && session.makeMove(move)) {
            // কম্পিউটার মোড এবং বটের চাল চালার লজিক
            if ("COMPUTER".equalsIgnoreCase(session.getMode()) && session.getCurrentTurn() == PieceColor.BLACK) {
                Move botMove = botStrategy.calculateMove(session.getBoard());
                if (botMove != null) {
                    session.makeMove(botMove);
                }
            }
        }
        return session;
    }
}
