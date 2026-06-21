package com.chess.controller;

import com.chess.model.*;
import com.chess.service.GameService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class GameController {
    
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    // Path Variable ব্যবহারের ফলে ক্লাউড নেটওয়ার্কিং ট্রাফিক কোনো বাধা ছাড়াই পাস হবে
    @PostMapping("/create/{mode}")
    @ResponseBody
    public GameSession createGame(@PathVariable("mode") String mode) {
        return gameService.createGame(mode);
    }

    @PostMapping("/join/{gameId}")
    @ResponseBody
    public GameSession joinGame(@PathVariable("gameId") String gameId) {
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
