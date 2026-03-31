package main;

import javax.swing.*;

public class GameWindow {

    JFrame frame;

    public GameWindow(GamePanel bgPanel){

        frame = new JFrame();
        frame.setSize(800,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);



        ImageIcon image = new ImageIcon("res/sprite/zombie.png"); //creates an Image Icon
        frame.setIconImage(image.getImage());
        frame.setTitle("Marry Me Or Die Trying");

        bgPanel.setLayout(null);
        frame.setContentPane(bgPanel);

        frame.setResizable(false);

        frame.setVisible(true);
    }
}