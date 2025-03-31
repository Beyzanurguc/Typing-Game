/*Bera Çevik - 230610022
Eray Sona - 230611015
Beyza Nur Güç - 230611001
Hatice Kübra Aydın - 230611048
Serkan Gürbüz - 230611003
*/
package TypingGame;

import javax.swing.*;
import java.awt.*;

public class EndScreen extends JPanel {
    private final int score, wave, totalEnemiesKilled;
    private final Image backgroundImage;

    public EndScreen(GamePanel gp, int score, int wave, int totalEnemiesKilled) {
        this.score = score;
        this.wave = wave;
        this.totalEnemiesKilled = totalEnemiesKilled;

        // Arka plan görüntüsünü yükle
        backgroundImage = new ImageIcon("galaxy3.gif").getImage();

        // Panel yerleşimi
        setLayout(null); // Özel yerleşim için null layout
        setPreferredSize(new Dimension(1000, 800));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Arka planı çiz
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        // Karakter ve düşman türlerini çiz
        Graphics2D g2d = (Graphics2D) g.create();
        drawPlayerCharacter(g2d);
        drawEnemyTypes(g2d);
        g2d.dispose();

        // Game Over ve diğer yazıları çiz
        drawCenteredText(g);
    }

    private void drawCenteredText(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Yazı fontları ve renk
        Font gameOverFont = new Font("Serif", Font.BOLD, 80);
        Font statsFont = new Font("Arial", Font.PLAIN, 40);
        g2d.setColor(Color.WHITE);

        // Game Over yazısı
        g2d.setFont(gameOverFont);
        String gameOverText = "GAME OVER";
        drawCenteredString(g2d, gameOverText, getWidth() / 2, 150);

        // Skorlar
        g2d.setFont(statsFont);
        drawCenteredString(g2d, "Score: " + score, getWidth() / 2, 250);
        drawCenteredString(g2d, "Wave: " + wave, getWidth() / 2, 310);
        drawCenteredString(g2d, "Kill Count: " + totalEnemiesKilled, getWidth() / 2, 370);

        // Çıkış butonu
        JButton exitButton = new JButton("Exit");
        exitButton.setBounds(getWidth() / 2 - 50, 450, 100, 40);
        exitButton.addActionListener(e -> System.exit(0));
        this.add(exitButton);
    }

    private void drawCenteredString(Graphics2D g2d, String text, int centerX, int y) {
        FontMetrics metrics = g2d.getFontMetrics();
        int x = centerX - metrics.stringWidth(text) / 2;
        g2d.drawString(text, x, y);
    }

    private void drawPlayerCharacter(Graphics2D g) {
        int playerX = 200;
        int playerY = getHeight() / 2; // Dikey ortalama

        // Hafif gölge
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(playerX - 120, playerY + 30, 180, 90);

        // Pelerin
        Polygon cape = new Polygon();
        cape.addPoint(playerX, playerY);
        cape.addPoint(playerX - 180, playerY + 150);
        cape.addPoint(playerX + 30, playerY + 180);
        g.setColor(new Color(160, 0, 160, 128));
        g.fillPolygon(cape);

        // Asa
        g.setStroke(new BasicStroke(9));
        g.setColor(new Color(139, 69, 19));
        g.drawLine(playerX + 90, playerY, playerX + 90, playerY + 120);
        g.fillOval(playerX + 75, playerY - 15, 30, 30);

        // Gövde
        g.setColor(new Color(70, 200, 210));
        g.fillOval(playerX - 60, playerY - 60, 120, 120);

        g.setColor(new Color(0, 80, 80));
        g.setStroke(new BasicStroke(6));
        g.drawOval(playerX - 60, playerY - 60, 120, 120);
    }

    private void drawEnemyTypes(Graphics2D g) {
        int offsetX = getWidth() - 150;
        int startY = getHeight() / 2 - 100;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGlowingStar(g, offsetX, startY, 25, Color.MAGENTA);
        startY += 80;
        drawGlowingOval(g, offsetX - 25, startY, 50, 50, Color.RED);
        startY += 80;
        drawGlowingRect(g, offsetX - 25, startY, 50, 50, Color.BLUE);
    }

    private void drawGlowingRect(Graphics2D g, int x, int y, int w, int h, Color c) {
        int layers = 7;
        for (int i = layers; i >= 1; i--) {
            float alpha = 0.03f * i;
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * 255)));
            g.fillRect(x - i, y - i, w + 2 * i, h + 2 * i);
        }
        g.setColor(c);
        g.fillRect(x, y, w, h);
    }

    private void drawGlowingOval(Graphics2D g, int x, int y, int w, int h, Color c) {
        int layers = 7;
        for (int i = layers; i >= 1; i--) {
            float alpha = 0.03f * i;
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * 255)));
            g.fillOval(x - i, y - i, w + 2 * i, h + 2 * i);
        }
        g.setColor(c);
        g.fillOval(x, y, w, h);
    }

    private void drawGlowingStar(Graphics2D g, int centerX, int centerY, int radius, Color c) {
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 5 * i;
            int r = (i % 2 == 0) ? radius : radius / 2;
            xPoints[i] = centerX + (int) (Math.cos(angle) * r);
            yPoints[i] = centerY + (int) (Math.sin(angle) * r);
        }

        int layers = 7;
        for (int i = layers; i >= 1; i--) {
            float alpha = 0.03f * i;
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * 255)));

            Polygon glowStar = new Polygon(xPoints, yPoints, 10);
            glowStar.translate(-i, -i);
            g.fillPolygon(glowStar);
            glowStar.translate(i, i);
        }

        g.setColor(c);
        g.fillPolygon(xPoints, yPoints, 10);
    }
}
