/*Bera Çevik - 230610022
Eray Sona - 230611015
Beyza Nur Güç - 230611001
Hatice Kübra Aydın - 230611048
Serkan Gürbüz - 230611003
*/
package TypingGame;

import javax.swing.*;
import java.awt.*;

public class InGameMenu extends JPanel {
    private final JFrame parentFrame;
    private final JPanel gamePanel;
    private final SoundPlayer soundPlayer;

    public InGameMenu(JFrame frame, JPanel gamePanel, SoundPlayer soundPlayer) {
        this.parentFrame = frame;
        this.gamePanel = gamePanel;
        this.soundPlayer = soundPlayer;
        initializeMenu();
    }

    private void initializeMenu() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.BLACK);

        JLabel menuLabel = new JLabel("Paused");
        menuLabel.setFont(new Font("Arial", Font.BOLD, 48));
        menuLabel.setForeground(Color.CYAN);
        menuLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton resumeButton = new JButton("Resume");
        styleButton(resumeButton);
        resumeButton.setFocusPainted(false);
        resumeButton.setBackground(Color.BLACK);
        resumeButton.setForeground(Color.CYAN);
        resumeButton.addActionListener(e -> resumeGame());

        JButton optionsButton = new JButton("Options");
        styleButton(optionsButton);
        optionsButton.setFocusPainted(false);
        optionsButton.setBackground(Color.BLACK);
        optionsButton.setForeground(Color.CYAN);
        optionsButton.addActionListener(e -> openOptionsMenu());

        JButton mainMenuButton = new JButton("Main Menu");
        styleButton(mainMenuButton);
        mainMenuButton.setFocusPainted(false);
        mainMenuButton.setBackground(Color.BLACK);
        mainMenuButton.setForeground(Color.CYAN);
        mainMenuButton.addActionListener(e -> returnToMainMenu());

        JButton exitButton = new JButton("Exit");
        styleButton(exitButton);
        exitButton.setFocusPainted(false);
        exitButton.setBackground(Color.BLACK);
        exitButton.setForeground(Color.CYAN);
        exitButton.addActionListener(e -> System.exit(0));

        add(Box.createVerticalGlue());
        add(menuLabel);
        add(Box.createRigidArea(new Dimension(0, 30)));
        add(resumeButton);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(optionsButton);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(mainMenuButton);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(exitButton);
        add(Box.createVerticalGlue());
    }

    private void resumeGame() {
        parentFrame.setContentPane(gamePanel);
        parentFrame.revalidate();
        parentFrame.repaint();
        if (gamePanel instanceof GamePanel) {
            ((GamePanel) gamePanel).resume();
        }
        gamePanel.requestFocusInWindow();
    }

    private void openOptionsMenu() {
        OptionsMenu optionsMenu = new OptionsMenu(parentFrame, this);
        parentFrame.setContentPane(optionsMenu);
        parentFrame.revalidate();
    }

    private void returnToMainMenu() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to return to the main menu? Unsaved progress will be lost.",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (soundPlayer != null) {
                soundPlayer.stopBackgroundMusic();
            }
            parentFrame.setContentPane(new MainMenu(parentFrame));
            parentFrame.revalidate();
        }
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 24));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(200, 50));
    }
}
