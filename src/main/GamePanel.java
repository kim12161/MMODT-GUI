package main;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {

    private Image backgroundImage;

    public GamePanel(String fileName){
        backgroundImage = new ImageIcon(fileName).getImage();
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);

        if(backgroundImage != null){
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}