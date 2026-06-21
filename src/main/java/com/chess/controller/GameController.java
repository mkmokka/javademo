package com.chess.controller;

import com.chess.model.*;
import com.chess.service.GameService;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class GameController {
    
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    // রেসপন্স অবজেক্ট সরাসরি পাঠানো হচ্ছে যাতে স্প্রিং ইন্টারনাল কোনো ডাটা ক্র্যাশ না করে
    @PostMapping("/api/game/create/{mode}")
    public GameSession createGame(@PathVariable("mode") String mode) {
        System.out.println("Processing Game Creation for Mode: " + mode);
        return gameService.createGame(mode);
    }

    @PostMapping("/api/game/join/{gameId}")
    public GameSession joinGame(@PathVariable("gameId") String gameId) {
        System.out.println("Processing Game Join for ID: " + gameId);
        return gameService.joinGame(gameId);
    }

    @GetMapping("/api/game/status/{gameId}")
    public GameSession getStatus(@PathVariable("gameId") String gameId) {
        return gameService.joinGame(gameId);
    }

    @PostMapping("/api/game/move/{gameId}")
    public GameSession handleMove(@PathVariable("gameId") String gameId, @RequestBody Move move) {
        return gameService.executePlayerMove(gameId, move);
    }
}
