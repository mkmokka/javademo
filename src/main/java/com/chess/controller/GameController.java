package com.chess.controller;

import com.chess.model.*;
import com.chess.service.GameService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
// Render Cloud CORS Policy হ্যান্ডেল করার জন্য অফিশিয়াল ডিক্লেয়ারেশন
@CrossOrigin(
    origins = "*", 
    allowedHeaders = "*", 
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
public class GameController {
    
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    // HTTP Options প্রি-ফ্লাইট রিকোয়েস্ট সিকিউরিটি পাসের জন্য
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public void handleOptions() {}

    @PostMapping("/create")
    @ResponseBody
    public GameSession createGame(@RequestParam("mode") String mode) {
        System.out.println("Processing Game Creation Request. Mode: " + mode);
        return gameService.createGame(mode);
    }

    @PostMapping("/join")
    @ResponseBody
    public GameSession joinGame(@RequestParam("gameId") String gameId) {
        System.out.println("Processing Game Join Request. Room Code: " + gameId);
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
