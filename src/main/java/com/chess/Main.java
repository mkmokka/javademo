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
        // Render.io এর ডায়নামিক পোর্ট রিড করা (না থাকলে ডিফল্ট ৮০৮০)
        String portStr = System.getenv("PORT");
        int port = (portStr != null) ? Integer.parseInt(portStr) : 8080;

        // ০.০.০.০ বাইন্ডিং ক্লাউড ডেপ্লয়মেন্টের জন্য বাধ্যতামূলক
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/", new ChessHandler());
        server.setExecutor(null); 
        System.out.println("Chess Web Server started on port: " + port);
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

            StringBuilder response = new StringBuilder();
            response.append("<html><head><title>Chess Game</title><meta charset='UTF-8'>");
            
            // রিয়েল-টাইম আপডেট: প্রতি ১.৫ সেকেন্ড পর পর ইন্টারফেস রিফ্রেশ হবে (থ্রেড ছাড়া রিয়েল টাইম)
            response.append("<meta http-equiv='refresh' content='1.5;url=/'>");
            
            response.append("</head><body style='font-family:sans-serif; text-align:center; background-color:#2c3e50; color:#ecf0f1;'>");
            response.append("<h2>Chess Live Multiplayer Interface</h2>");
            response.append("<h3>Turn: <span style='color:").append(board.getCurrentTurn() == PieceColor.WHITE ? "#fff" : "#111").append("; background-color:#888; padding:2px 8px; border-radius:4px;'>").append(board.getCurrentTurn()).append("</span></h3>");
            
            if (selectedRow != -1) {
                response.append("<p style='color:#2ecc71; font-weight:bold;'>Piece Selected! Green borders show standard legal squares.</p>");
            } else {
                response.append("<p>Select a piece from your side to move.</p>");
            }

            response.append(board.toHtmlTable(selectedId, ""));
            response.append("<br><a href='/' style='color:#e74c3c; font-weight:bold; text-decoration:none; font-size:16px;'>Manual Sync Board</a>");
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
