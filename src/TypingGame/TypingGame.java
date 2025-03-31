/*Bera Çevik - 230610022
Eray Sona - 230611015
Beyza Nur Güç - 230611001
Hatice Kübra Aydın - 230611048
Serkan Gürbüz - 230611003
*/
package TypingGame;

import javax.swing.*;

public class TypingGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Typing Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            GamePanel gamePanel = new GamePanel();

            MainMenu mainMenu = new MainMenu(frame);
            frame.add(mainMenu);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setUndecorated(true); 
            frame.setVisible(true);
        });
    }
}
