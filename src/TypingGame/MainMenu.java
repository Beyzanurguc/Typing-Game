/*Bera Çevik - 230610022
Eray Sona - 230611015
Beyza Nur Güç - 230611001
Hatice Kübra Aydın - 230611048
Serkan Gürbüz - 230611003
*/
package TypingGame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class MainMenu extends JPanel {
    private final JFrame parentFrame;
    private boolean isFullScreen = true;
    private ImageIcon backgroundIcon;
    

    public MainMenu(JFrame frame) {
    	
        this.parentFrame = frame;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.BLACK);
        backgroundIcon = new ImageIcon("galaxy3.gif");

        JLabel titleLabel = new JLabel("Typing Game", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 60));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.PLAIN, 24));
        startButton.setFocusPainted(false);
        startButton.setBackground(Color.BLACK);
        startButton.setForeground(Color.CYAN);
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(e -> startGame());

        JButton optionsButton = new JButton("Options");
        optionsButton.setFont(new Font("Arial", Font.PLAIN, 24));
        optionsButton.setFocusPainted(false);
        optionsButton.setBackground(Color.BLACK);
        optionsButton.setForeground(Color.CYAN);
        optionsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        optionsButton.addActionListener(e -> showOptions());

        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.PLAIN, 24));
        exitButton.setFocusPainted(false);
        exitButton.setBackground(Color.BLACK);
        exitButton.setForeground(Color.CYAN);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.addActionListener(e -> System.exit(0));

        add(Box.createVerticalGlue());
        add(titleLabel);
        add(Box.createRigidArea(new Dimension(0, 50)));
        add(startButton);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(optionsButton);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(exitButton);
        add(Box.createVerticalGlue());
        
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
       
        
        if (backgroundIcon != null) {
            g.drawImage(backgroundIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
        }
        }

    private void startGame() {
        parentFrame.getContentPane().removeAll();
        GamePanel gamePanel = new GamePanel();
        
        parentFrame.add(gamePanel);
        parentFrame.revalidate();
        parentFrame.repaint();

        gamePanel.requestFocusInWindow();
        gamePanel.start();
    }

    private void showOptions() {
        parentFrame.setContentPane(new OptionsMenu(parentFrame, this));
        parentFrame.revalidate();
    }

}
