package com.company;


import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class UserInterface extends JPanel implements MouseListener, MouseMotionListener{


    public static final int BORDERS = 25;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 800;
    public static final int TILE_WITDH = (WIDTH - BORDERS * 2)/8;
    public static final int TILE_HEIGHT = (HEIGHT - BORDERS * 2)/8;

    static int x = 0,y = 0;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        super.setBackground(Color.WHITE);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        drawBoard(g);

        Image chessPieceImage = new ImageIcon("ChessPieces.png").getImage();
        g.drawImage(chessPieceImage, x, y, x + 64, y + 64, 0, 0, 64, 64  , this);

    }

    public void drawBoard(Graphics g) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i + j) % 2 != 0) {
                    g.setColor(Color.BLACK);
                    g.fillRect(i * TILE_WITDH + BORDERS, j * TILE_HEIGHT + BORDERS, TILE_WITDH, TILE_HEIGHT);
                }
            }
        }
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        x = e.getX();
        y = e.getY();
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
