package com.chess;

import com.chess.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Main extends JFrame {
    private Board board;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private JPanel boardPanel;

    public Main() {
        board = new Board();
        setTitle("Chess Game");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        boardPanel = new JPanel(new GridLayout(8, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
            }
        };

        boardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int size = boardPanel.getWidth() / 8;
                int col = e.getX() / size;
                int row = e.getY() / size;

                if (selectedRow == -1) {
                    Piece p = board.getPiece(row, col);
                    if (p != null && p.getColor() == board.getCurrentTurn()) {
                        selectedRow = row;
                        selectedCol = col;
                    }
                } else {
                    boolean moved = board.makeMove(selectedRow, selectedCol, row, col);
                    if (!moved) {
                        Piece p = board.getPiece(row, col);
                        if (p != null && p.getColor() == board.getCurrentTurn()) {
                            selectedRow = row;
                            selectedCol = col;
                        } else {
                            selectedRow = -1;
                            selectedCol = -1;
                        }
                    } else {
                        selectedRow = -1;
                        selectedCol = -1;
                    }
                }
                // এই ইভেন্টটি ঘটার সাথে সাথেই UI রিফ্রেশ হবে, কোনো ব্যাকগ্রাউন্ড লুপ লাগবে না
                boardPanel.repaint(); 
            }
        });

        add(boardPanel);
        setVisible(true);
    }

    private void drawBoard(Graphics g) {
        int size = boardPanel.getWidth() / 8;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if ((r + c) % 2 == 0) g.setColor(new Color(240, 217, 181));
                else g.setColor(new Color(181, 136, 99));
                g.fillRect(c * size, r * size, size, size);

                if (r == selectedRow && c == selectedCol) {
                    g.setColor(new Color(255, 255, 0, 128));
                    g.fillRect(c * size, r * size, size, size);
                }

                // সম্ভাব্য চালের ঘরগুলো সবুজ বর্ডার দিয়ে হাইলাইট করবে
                if (selectedRow != -1 && board.isValidMove(selectedRow, selectedCol, r, c)) {
                    g.setColor(new Color(0, 255, 0, 180));
                    g.drawRect(c * size + 3, r * size + 3, size - 6, size - 6);
                }

                Piece p = board.getPiece(r, c);
                if (p != null) {
                    g.setColor(p.getColor() == PieceColor.WHITE ? Color.WHITE : Color.BLACK);
                    g.setFont(new Font("Arial", Font.BOLD, 18));
                    String text = p.getType().name().substring(0, 2);
                    g.drawString(text, c * size + size / 4, r * size + size / 2 + 6);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
