package com.chess.controller;

import com.chess.model.*;
import com.chess.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class GameController {
    
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    // একদম রুট লেভেলের ম্যাপিং যাতে প্রক্সি সার্ভার খুব সহজে পাথ আইডেন্টিফাই করতে পারে
    @PostMapping("/api/game/create/{mode}")
    public ResponseEntity<GameSession> createGame(@PathVariable("mode") String mode) {
        System.out.println("REST Request to Create Game Mode: " + mode);
        GameSession session = gameService.createGame(mode);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/api/game/join/{gameId}")
    public ResponseEntity<GameSession> joinGame(@PathVariable("gameId") String gameId) {
        System.out.println("REST Request to Join Game ID: " + gameId);
        GameSession session = gameService.joinGame(gameId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }

    @GetMapping("/api/game/status/{gameId}")
    public ResponseEntity<GameSession> getStatus(@PathVariable("gameId") String gameId) {
        GameSession session = gameService.joinGame(gameId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }

    @PostMapping("/api/game/move/{gameId}")
    public ResponseEntity<GameSession> handleMove(@PathVariable("gameId") String gameId, @RequestBody Move move) {
        System.out.println("REST Move Request Received for ID: " + gameId);
        GameSession session = gameService.executePlayerMove(gameId, move);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }
}
