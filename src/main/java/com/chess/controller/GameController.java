package com.chess.controller;

import com.chess.model.*;
import com.chess.service.GameService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class GameController {
    
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    // ক্লাউড সিকিউরিটি পাসের জন্য একদম ক্লিন রুট এপিআই পাথ
    @PostMapping("/api/game/create/{mode}")
    public GameSession createGame(@PathVariable("mode") String mode) {
        System.out.println("Executing Create Game Mode: " + mode);
        return gameService.createGame(mode);
    }

    @PostMapping("/api/game/join/{gameId}")
    public GameSession joinGame(@PathVariable("gameId") String gameId) {
        System.out.println("Executing Join Game ID: " + gameId);
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
