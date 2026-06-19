package com.chess;

import com.chess.model.*;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static Board board = new Board();
    private static int selectedRow = -1;
    private static int selectedCol = -1;

    public static void main(String[] args) throws IOException {
        // ডকার কন্টেইনারের জন্য ৮০৮০ পোর্টে লাইটওয়েট ওয়েব সার্ভার চালু করা হচ্ছে
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new ChessHandler());
        server.setExecutor(null); 
        System.out.println("Chess Game Server started on http://localhost:8080");
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
            }

            // রিয়েল-টাইম ইউজার ইন্টারফেস তৈরির রেসপন্স (HTML)
            StringBuilder response = new StringBuilder();
            response.append("<html><head><title>Chess Game</title><meta charset='UTF-8'>");
            
            // রিয়েল-টাইম আপডেটের জন্য অটো-রিফ্রেশ মেটা ট্যাগ (প্রতি সেকেন্ডে অপর প্লেয়ারের চাল চেক করবে)
            response.append("<meta http-equiv='refresh' content='2;url=/'>");
            
            response.append("</head><body style='font-family:Arial, sans-serif; text-align:center; background-color:#2c3e50; color:#ecf0f1;'>");
            response.append("<h2>Chess Game (Multiplayer/Computer)</h2>");
            response.append("<h3>Current Turn: <span style='color:").append(board.getCurrentTurn() == PieceColor.WHITE ? "#fff" : "#000").append("; text-transform:uppercase;'>").append(board.getCurrentTurn()).append("</span></h3>");
            
            if (selectedRow != -1) {
                response.append("<p style='color:#4caf50;'>Piece Selected! Green borders show valid moves.</p>");
            } else {
                response.append("<p>Click on your piece to see valid moves.</p>");
            }

            // বোর্ড জেনারেট করা
            response.append(board.toHtmlTable(selectedId, ""));
            
            response.append("<br><a href='/' style='color:#e74c3c; font-weight:bold; font-size:18px;'>Refresh Board</a>");
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
