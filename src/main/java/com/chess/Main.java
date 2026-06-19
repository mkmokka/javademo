package com.chess;

import com.chess.model.*;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Main {
    private static Board board = new Board();
    private static int selectedRow = -1;
    private static int selectedCol = -1;

    public static void main(String[] args) throws IOException {
        // ডকার নেটওয়ার্কের জন্য সার্ভারটি চালু করা হচ্ছে
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new ChessHandler());
        server.setExecutor(null); 
        System.out.println("Chess Web Server started successfully on port 8080!");
        server.start();
    }

    static class ChessHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String selectedId = "";

            if (query != null && query.startsWith("click=")) {
                String clickCoords = query.substring(6);
                String[] coords = clickCoords.split("-");
                int row = Integer.parseInt(coords[0]);
                int col = Integer.parseInt(coords[1]);

                if (selectedRow == -1) {
                    Piece p = board.getPiece(row, col);
                    if (p != null && p.getColor() == board.getCurrentTurn()) {
                        selectedRow = row;
                        selectedCol = col;
                        selectedId = row + "-" + col;
                    }
                } else {
                    boolean moved = board.makeMove(selectedRow, selectedCol, row, col);
                    if (!moved) {
                        Piece p = board.getPiece(row, col);
                        if (p != null && p.getColor() == board.getCurrentTurn()) {
                            selectedRow = row;
                            selectedCol = col;
                            selectedId = row + "-" + col;
                        } else {
                            selectedRow = -1;
                            selectedCol = -1;
                        }
                    } else {
                        selectedRow = -1;
                        selectedCol = -1;
                    }
                }
            } else if (selectedRow != -1) {
                selectedId = selectedRow + "-" + selectedCol;
            }

            // রিয়েল-টাইম সিঙ্ক ইউআই রেসপন্স (HTML)
            StringBuilder response = new StringBuilder();
            response.append("<html><head><title>Chess Game</title><meta charset='UTF-8'>");
            
            // রিয়েল-টাইম আপডেট ইন্টিগ্রেশন: প্রতি ১.৫ সেকেন্ডে অপর প্লেয়ারের চাল সিঙ্ক করবে
            response.append("<meta http-equiv='refresh' content='1.5;url=/'>");
            
            response.append("</head><body style='font-family:sans-serif; text-align:center; background-color:#2c3e50; color:#ecf0f1;'>");
            response.append("<h2>Chess Multiplayer Interface</h2>");
            response.append("<h3>Turn: <span style='color:").append(board.getCurrentTurn() == PieceColor.WHITE ? "#fff" : "#111").append("; background-color:#888; padding:2px 8px; border-radius:4px;'>").append(board.getCurrentTurn()).append("</span></h3>");
            
            if (selectedRow != -1) {
                response.append("<p style='color:#2ecc71; font-weight:bold;'>Piece Selected! Green borders represent standard legal squares.</p>");
            } else {
                response.append("<p>Select a piece from your side to move.</p>");
            }

            // জেনারেট করা বোর্ড ইন্টারফেসে যুক্ত করা
            response.append(board.toHtmlTable(selectedId, ""));
            response.append("<br><a href='/' style='color:#e74c3c; font-weight:bold; font-size:16px;'>Manual Refresh</a>");
            response.append("</body></html>");

            byte[] bytes = response.toString().getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}
