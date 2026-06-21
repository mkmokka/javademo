package com.chess.controller;

import com.chess.model.*;
import com.chess.service.GameService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class GameController {
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/api/game/create")
    public GameSession createGame(@RequestParam String mode) {
        return gameService.createGame(mode);
    }

    @PostMapping("/api/game/join")
    public GameSession joinGame(@RequestParam String gameId) {
        GameSession session = gameService.joinGame(gameId);
        if (session != null) {
            messagingTemplate.convertAndSend("/topic/game/" + gameId, session);
        }
        return session;
    }

    @MessageMapping("/game/{gameId}/move")
    public void handleMove(@DestinationVariable String gameId, @Payload Move move) {
        GameSession updatedSession = gameService.executePlayerMove(gameId, move);
        messagingTemplate.convertAndSend("/topic/game/" + gameId, updatedSession);
    }
}
