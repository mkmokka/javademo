package com.chess.controller;

import com.chess.model.*;
import com.chess.service.GameService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {
    
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/create/{mode}")
    public GameSession createGame(@PathVariable String mode) {
        return gameService.createGame(mode);
    }

    @PostMapping("/join/{gameId}")
    public GameSession joinGame(@PathVariable String gameId) {
        return gameService.joinGame(gameId);
    }

    @GetMapping("/status/{gameId}")
    public GameSession getStatus(@PathVariable String gameId) {
        return gameService.joinGame(gameId); // সার্ভিস থেকে কারেন্ট স্টেট রিটার্ন করবে
    }

    @PostMapping("/move/{gameId}")
    public GameSession handleMove(@PathVariable String gameId, @RequestBody Move move) {
        return gameService.executePlayerMove(gameId, move);
    }
}
