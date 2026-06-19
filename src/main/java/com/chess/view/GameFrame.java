package com.chess.view;

import com.chess.model.Board;

public class GameFrame {
    public static String renderHtml(Board board, String mode, String roomCode) {
        return "<!DOCTYPE html><html><head><title>Aiven Chess Server</title>" +
                "<style>body{font-family:sans-serif; text-align:center; background:#fafafa; color:#333;}" +
                ".btn{padding:10px 20px; font-size:16px; background:#4CAF50; color:white; border:none; border-radius:4px; cursor:pointer; text-decoration:none; margin:5px;}"
                +
                "</style>" +
                "<script>" +
                "let selectedCell = null;" +
                "function cellClicked(r, c, mode, code) {" +
                "  if(!selectedCell) {" +
                "    selectedCell = {row: r, col: c};" +
                "    alert('Selected piece at ' + r + ',' + c + '. Now click destination.');" +
                "  } else {" +
                "    window.location.href = '/move?mode=' + mode + '&code=' + code + '&fromR=' + selectedCell.row + '&fromC=' + selectedCell.col + '&toR=' + r + '&toC=' + c;"
                +
                "  }" +
                "}" +
                "</script></head><body>" +
                "<h1>♟️ Render Distributed Chess Engine ♟️</h1>" +
                (roomCode.equals("null") ? "<h3>Mode: Single Player vs Bot</h3>"
                        : "<h3>Online Room Code: <span style='color:blue;'>" + roomCode + "</span></h3>")
                +
                "<a href='/' class='btn' style='background:#f44336;'>Back to Main Menu</a>" +
                board.toHtmlTable(mode, roomCode) +
                "<p>Click a piece, then click a tile to move.</p></body></html>";
    }

    public static String renderMenu() {
        return "<!DOCTYPE html><html><head><title>Chess Launcher</title>" +
                "<style>body{font-family:sans-serif; text-align:center; padding-top:50px; background:#f4f4f9;}" +
                ".card{background:white; padding:4px 30px 40px; display:inline-block; border-radius:8px; box-shadow:0 4px 8px rgba(0,0,0,0.1);}"
                +
                ".btn{display:block; width:250px; padding:12px; margin:15px auto; font-size:16px; background:#007bff; color:white; border:none; border-radius:5px; cursor:pointer; text-decoration:none;}"
                +
                "input{padding:10px; font-size:16px; width:230px; text-align:center; border:1px solid #ccc; border-radius:4px;}"
                +
                "</style></head><body>" +
                "<div class='card'><h2>🎮 Welcome to Multiplayer Chess</h2>" +
                "<a href='/start?mode=single' class='btn' style='background:#28a745;'>1 Player (VS Computer Bot)</a><hr>"
                +
                "<a href='/start?mode=create' class='btn'>Create Online Match</a>" +
                "<form action='/start' method='get'>" +
                "  <input type='hidden' name='mode' value='join'>" +
                "  <input type='text' name='code' placeholder='Enter Unique Code' required><br>" +
                "  <button type='submit' class='btn' style='background:#17a2b8;'>Join Match (2nd PC)</button>" +
                "</form></div></body></html>";
    }
}
