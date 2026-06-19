package com.chess;

import com.chess.model.Board;
import com.chess.view.GameFrame;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SpringBootApplication
@RestController
public class Main {

    private static final Map<String, Board> localMatches = new HashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    // আপনার 'course' ডাটাবেজ ব্যবহার করে নতুন 'chess' ডাটাবেজ অটো-ক্রিয়েট করার
    // মেথড
    private Connection getDatabaseConnection() throws Exception {
        String fullUrl = System.getenv("DB_URL"); // Render থেকে আপনার 'course' এর URL আসবে
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        Class.forName("com.mysql.cj.jdbc.Driver");

        // ১. প্রথমে আপনার বর্তমান 'course' ডাটাবেজেই কানেক্ট হওয়া
        try (Connection conn = DriverManager.getConnection(fullUrl, user, password);
                Statement stmt = conn.createStatement()) {

            // ২. সার্ভারের ভেতরে নতুন 'chess' নামে ডাটাবেজটি তৈরি করা (যদি না থাকে)
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS chess");
        } catch (Exception e) {
            System.out.println("Database auto-creation log: " + e.getMessage());
        }

        // ৩. এবার ইউআরএল-এর 'course' অংশটি পরিবর্তন করে নতুন 'chess' ডাটাবেজের ডাইনামিক
        // কানেকশন তৈরি করা
        String chessUrl = fullUrl;
        if (fullUrl.contains("/course")) {
            chessUrl = fullUrl.replace("/course", "/chess");
        }

        // ৪. নতুন 'chess' ডাটাবেজের সাথে ফাইনাল কানেকশন রিটার্ন
        return DriverManager.getConnection(chessUrl, user, password);
    }

    @GetMapping("/")
    public String showMenu() {
        return GameFrame.renderMenu();
    }

    @GetMapping("/start")
    public String startMatch(@RequestParam String mode, @RequestParam(required = false) String code) {
        if (mode.equalsIgnoreCase("single")) {
            Board botBoard = new Board();
            localMatches.put("bot", botBoard);
            return GameFrame.renderHtml(botBoard, "single", "null");
        }

        String roomCode = (mode.equalsIgnoreCase("create")) ? UUID.randomUUID().toString().substring(0, 5).toUpperCase()
                : code;
        Board board = new Board();

        try (Connection conn = getDatabaseConnection()) {
            // নতুন 'chess' ডাটাবেজের ভেতরে মাল্টিপ্লেয়ার টেবিল তৈরি
            try (PreparedStatement schemaStmt = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS chess_rooms (" +
                            "room_code VARCHAR(10) PRIMARY KEY, " +
                            "current_turn VARCHAR(10))")) {
                schemaStmt.executeUpdate();
            }

            if (mode.equalsIgnoreCase("create")) {
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO chess_rooms (room_code, current_turn) VALUES (?, 'WHITE') " +
                                "ON DUPLICATE KEY UPDATE room_code=room_code")) {
                    insertStmt.setString(1, roomCode);
                    insertStmt.executeUpdate();
                }
                localMatches.put(roomCode, board);
            } else {
                try (PreparedStatement checkStmt = conn
                        .prepareStatement("SELECT room_code FROM chess_rooms WHERE room_code = ?")) {
                    checkStmt.setString(1, roomCode);
                    ResultSet rs = checkStmt.executeQuery();
                    if (!rs.next()) {
                        return "<h2>❌ Error: Invalid Joining Code! Room not found in Aiven Chess Database.</h2><a href='/'>Go Back</a>";
                    }
                }
                if (!localMatches.containsKey(roomCode)) {
                    localMatches.put(roomCode, board);
                } else {
                    board = localMatches.get(roomCode);
                }
            }
        } catch (Exception e) {
            return "<h2>❌ Database Error: " + e.getMessage() + "</h2>" +
                    "<p>দয়া করে নিশ্চিত করুন Render-এ DB_URL (course সহ URLটি), DB_USER, এবং DB_PASSWORD সঠিকভাবে সেট করা আছে কিনা।</p>"
                    +
                    "<a href='/'>Go Back</a>";
        }

        return GameFrame.renderHtml(board, "online", roomCode);
    }

    @GetMapping("/move")
    public String handleMove(@RequestParam String mode, @RequestParam String code,
            @RequestParam int fromR, @RequestParam int fromC,
            @RequestParam int toR, @RequestParam int toC) {

        String key = mode.equalsIgnoreCase("single") ? "bot" : code;
        Board board = localMatches.get(key);

        if (board == null) {
            return "<h2>❌ Match session expired. Please restart.</h2><a href='/'>Go Back</a>";
        }

        board.movePiece(fromR, fromC, toR, toC);

        if (mode.equalsIgnoreCase("single")) {
            board.makeBotMove();
        }

        return GameFrame.renderHtml(board, mode, code);
    }
}
