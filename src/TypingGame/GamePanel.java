/*Bera Çevik - 230610022
Eray Sona - 230611015
Beyza Nur Güç - 230611001
Hatice Kübra Aydın - 230611048
Serkan Gürbüz - 230611003
*/
package TypingGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.FileNameMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    private final List<Word> words = new ArrayList<>(); // Ekranda belirecek kelimelerin listesini muhafaza eder
    private final List<Word> pendingWords = new ArrayList<>(); // Doğru zamanda ortaya çıkacak kelimelerin geçici deposu
    private final List<Explosion> explosions = new ArrayList<>(); // Patlama efektlerini elinde tutar
    private final List<Beam> beams = new ArrayList<>();
    
    private final Random random = new Random(); // Muhtelif rastgelelik icra etmek için kullanılır
    private boolean running; // Oyun döngüsünü idame ettirmek için kullanılan bayrak
    private String inputText = ""; // Oyuncunun hâlihazırda girdiği metni kaydeder
    private int currentWave = 1; // Mevcut dalgayı tayin eder
    private int waveWordCount = 1; // Bu dalgadaki kelime miktarı
    private int playerX;
    private int playerY;
    private int playerLives = 3; // Oyuncunun canları
    private int score = 0; // Oyuncunun puanı
    private int combo = 0; // O anki kombonun değeri
    private int totalKill =0;
    private boolean showWaveInfo = true; // Dalga bilgisi göstermek için kullanılır
    private long waveInfoStartTime; // Dalga bilgisinin ekranda kalacağı zamanı başlatır
    private long gameStartTime; // Oyunun başlama zamanını tutar
    private boolean allowEnemySpawning = false; // Dalga bilgisi gösterilirken düşman çıkmasını engellemek için
    private static final int TARGET_FPS = 60;
    private static final long OPTIMAL_TIME = 1000 / TARGET_FPS; // Her kare için hedeflenmiş milisaniye süresi
    private static final int WAVE_DURATION = 8000; // Her dalganın milisaniye cinsinden müddeti
    private SoundPlayer soundPlayer;

    
    // Eskiden "private Image background;" diyorduk, şimdi ImageIcon kullanıyoruz:
    private ImageIcon backgroundIcon;

    public GamePanel() {
        
        initializePanel(); // Panel hazırlıklarını yapar
        generateWordsForWave(); // İlk dalga için kelimeleri oluşturur
        gameStartTime = System.currentTimeMillis(); // Oyunun başlangıç anını tayin eder
        waveInfoStartTime = System.currentTimeMillis(); // Dalga bilgisinin ekranda kalacağı zamanı başlatır
        
        // Arkaplan GIF'i ImageIcon olarak yükle
        backgroundIcon = new ImageIcon("galaxy.gif");
    }

    private void initializePanel() {
        setBackground(Color.BLACK); // Arkaplan rengini siyah tayin eder
        setFocusable(true); // Panelin klavye girdilerini almasını sağlar
        addKeyListener(this); // Klavye olaylarını dinlemek için ekleme yapar
    }

    private void generateWordsForWave() {
        words.clear(); // Mevcut kelimeleri boşaltır
        pendingWords.clear(); // Bekleyen kelimeleri de boşaltır

        List<String> wordList = readWordsFromFile("words.txt"); // Dosyadan kelimeleri okur
        for (int i = 0; i < waveWordCount; i++) {
            String word = wordList.get(random.nextInt(wordList.size())); // Rastgele bir kelime seçer
            Word newWord = createWord(word); // Yeni bir kelime nesnesi üretir
            pendingWords.add(newWord); // Bekleyen kelimeler listesine ekler
            newWord.spawnTime = System.currentTimeMillis() + random.nextInt(WAVE_DURATION); // Kelimenin çıkış anını ayarlar
        }
    }

    /**
     * Bir dikdörtgeni, etrafına parıltı (glow) efekti ekleyerek çizer.
     */
    private void drawGlowingRect(Graphics2D g, int x, int y, int width, int height, Color baseColor) {
        // Kaç katman glow çizeceğiz?
        int layers = 7; 
        // Her katmanda ne kadar şeffaflık olsun?
        // Yüksek layers değeriyle daha yumuşak bir geçiş sağlanır.
        for (int i = layers; i >= 1; i--) {
            float alpha = 0.03f * i; // 0.03 -> Deneyerek gözünüze uygun değeri bulabilirsiniz
            g.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int) (alpha * 255)));
            // Her katmanda asıl şeklin etrafını genişleterek çiziyoruz
            g.fillRect(x - i, y - i, width + 2 * i, height + 2 * i);
        }
        // Son olarak asıl dikdörtgeni baz renk ile çiz
        g.setColor(baseColor);
        g.fillRect(x, y, width, height);
    }

    /**
     * Bir ovali (daireyi) parıltı efektiyle çizer.
     */
    private void drawGlowingOval(Graphics2D g, int x, int y, int width, int height, Color baseColor) {
        int layers = 7; 
        for (int i = layers; i >= 1; i--) {
            float alpha = 0.03f * i; 
            g.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int) (alpha * 255)));
            g.fillOval(x - i, y - i, width + 2 * i, height + 2 * i);
        }
        g.setColor(baseColor);
        g.fillOval(x, y, width, height);
    }

    /**
     * Yıldızı glow efektiyle çizer. 
     */
    private void drawGlowingStar(Graphics2D g, int centerX, int centerY, int radius, Color baseColor) {
        // Önce, yıldızı çizmek için kullanacağımız koordinatları bulalım.
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 5 * i; 
            int r = (i % 2 == 0) ? radius : radius / 2; 
            xPoints[i] = centerX + (int) (Math.cos(angle) * r);
            yPoints[i] = centerY + (int) (Math.sin(angle) * r);
        }
        // Yıldızın ilk ucu, merkezden kayıyor olabilir; istersek hizalayabiliriz ama
        // basitçe orijinal metotla aynı mantığı koruyalım.

        // Katman katman glow
        int layers = 7;
        for (int i = layers; i >= 1; i--) {
            float alpha = 0.03f * i; 
            g.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int) (alpha * 255)));
            // her katmanda azıcık büyüterek çizelim
            // Büyütme için transform gerekebilir ama basitçe “Stroke” kullanarak kontur da yapabilirsiniz.
            // Doldurma (fill) yapacaksak, “scale” gibi bir işlem gerek. 
            // Kaba tabirle, burada sadece “çiz” diyerek bir deneme yaklaşımı yapıyoruz.
            
            // Yöntem 1: Yıldızın bir Path2D nesnesini oluşturup stroke ile çizersek:
            // Yöntem 2: Basit yama: Birkaç piksel kaydırarak dolduralım.
            // Gerçek glow için transform kullanmak daha sağlıklı. 
            // Burada da “harsh hack” diyelim:
            
            Polygon glowStar = new Polygon(xPoints, yPoints, 10);
            // küçük bir kayma verelim:
            glowStar.translate(-i, -i); 
            g.fillPolygon(glowStar);
            // geriye koy
            glowStar.translate(i, i);
        }

        // Son katmanda asıl yıldızı doldur
        g.setColor(baseColor);
        g.fillPolygon(xPoints, yPoints, 10);
    }

    
    
    private Word createWord(String wordText) {
        int x = random.nextInt(1200); // Rastgele x konumu
        int y = 0; // Ekranın tepesinden başlar
        Word newWord = new Word(wordText, x, y);

        // Kelimenin hız ihtimalini belirler
        int probability = random.nextInt(10); // 0 ile 9 arası değer üretir
        if (probability < 6) {
            newWord.speed = 1; // %60 sürat 1
        } else if (probability < 9) {
            newWord.speed = 2; // %30 sürat 2
        } else {
            newWord.speed = 3; // %10 sürat 3
        }

        return newWord;
    }

    private List<String> readWordsFromFile(String filename) {
        // İlgili kelime dosyasını okur ve listeye atar
        List<String> wordList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)))) {
            String line;
            while ((line = br.readLine()) != null) {
                wordList.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wordList;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // ImageIcon içindeki GIF'i çizelim (animasyonu ImageIcon'ın kendisi handle eder).
        if (backgroundIcon != null) {
            g2d.drawImage(backgroundIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
        }

        // Her daim görüntülenmesi gereken sabit öğeleri çizer
        drawWords(g2d); // Kelimeleri resmeder
        drawExplosions(g2d); // Patlamaları çizer
        drawUserInput(g2d); // Oyuncunun girdiği metni gösterir
        drawPlayerCharacter(g2d); // Oyuncu karakterini çizer
        drawBeams(g2d);
        drawPlayerLives(g2d); // Oyuncunun can adedini gösterir
        drawWaveNumber(g2d); // Mevcut dalga sayısını ekrana koyar
        drawGameTimer(g2d); // Oyun zamanını gösterir
        drawScore(g2d); // Puanı gösterir
        drawCombo(g2d); // Kombo değerini gösterir

        // Dalga bilgisi gösterilmesi gerekiyorsa onu da çizer
        if (showWaveInfo) {
            drawWaveInfo(g2d);
        }
    }

    private void drawWaveInfo(Graphics2D g) {
        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 80));
        String waveText = "Wave " + currentWave; // Dalgaya dair metni oluşturur
        int textWidth = g.getFontMetrics().stringWidth(waveText);
        g.drawString(waveText, (getWidth() - textWidth) / 2, getHeight() / 2 - 20);

        // Sayaç kısmını yazar
        g.setFont(new Font("Arial", Font.PLAIN, 30));
        long elapsed = (System.currentTimeMillis() - waveInfoStartTime) / 1000; // Dalga bilgisinin yayınlanma süresi
        int countdown = 3 - (int) elapsed;
        if (countdown >= 0) {
            String countdownText = "Starting in " + countdown;
            int countdownWidth = g.getFontMetrics().stringWidth(countdownText);
            g.drawString(countdownText, (getWidth() - countdownWidth) / 2, getHeight() / 2 + 20);
        } else {
            allowEnemySpawning = true; // Dalga başlamışsa düşman doğumuna izin ver
        }
    }

    private long comboStartTime = System.currentTimeMillis();

    
    private Color interpolateColor(Color c1, Color c2, float ratio) {
        if (ratio < 0f) ratio = 0f;
        if (ratio > 1f) ratio = 1f;

        int r = (int) (c1.getRed()   + (c2.getRed()   - c1.getRed())   * ratio);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * ratio);
        int b = (int) (c1.getBlue()  + (c2.getBlue()  - c1.getBlue())  * ratio);
        int a = (int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * ratio);

        return new Color(r, g, b, a);
    }
    
    
    private void drawWaveNumber(Graphics2D g) {
        g.setColor(new Color(0xFFCFEF));
        g.setFont(new Font("Arial", Font.BOLD, 25));
        String waveText = "Wave: " + currentWave; // Dalga sayısını metin hâline getirir
        g.drawString(waveText, getWidth() - 130, 50);
    }

    private void drawGameTimer(Graphics2D g) {
        g.setColor(new Color(0xFFCFEF));
        g.setFont(new Font("Arial", Font.BOLD, 20));
        long elapsed = (System.currentTimeMillis() - gameStartTime) / 1000; // Oyun başladığından beri geçen saniye
        String timerText = "Time: " + elapsed + "s";
        g.drawString(timerText, getWidth() - 123, 80);
    }

    private void drawScore(Graphics2D g) {
        g.setColor(new Color(0x06DCD4));
        g.setFont(new Font("Arial", Font.BOLD, 25));
        String scoreText = "Score: " + score; // Puan yazısı
        g.drawString(scoreText, 20, 50);
    }

    private void drawCombo(Graphics2D g) {
        if (combo > 0) {
        	
        	if(0<combo && combo<10) {
                g.setColor(new Color(0x00ffb7));
                g.setFont(new Font("Arial", Font.BOLD, 20 + 2*combo));
                String comboText = combo + "x"; // Kombo bilgisini metinleştirir
                int textWidth = g.getFontMetrics().stringWidth(comboText);
                g.drawString(comboText, (getWidth() - textWidth) / 2, 60 + combo);
        	}
        	
        	
        	if (9 < combo && combo < 20) {
        	    // Zaman hesabı
        	    long now = System.currentTimeMillis();
        	    long elapsed = now - comboStartTime; 
        	    
        	    // Nabız değerini sinüs dalgası ile oluştur (0.95 ~ 1.05 aralığında salınıyor)
        	    float pulse = 1.0f + 0.05f * (float) Math.sin(elapsed / 100.0);

        	    // Temel font boyutu
        	    int baseFontSize = 20 + 2 * combo;
        	    // Pulse ile büyüyüp küçülen font boyutu
        	    int animatedFontSize = (int) (baseFontSize * pulse);

        	    // Renk animasyonu: Sarıdan turuncuya geçiş
        	    // ratio: 0 ~ 1 arasında gidip gelen bir dalga
        	    float ratio = 0.5f * (1.0f + (float) Math.sin(elapsed / 200.0));
        	    Color animatedColor = interpolateColor(new Color(0xFFCC00), new Color(0xFF6600), ratio);

        	    // Font ve renk ayarla
        	    g.setFont(new Font("Arial", Font.BOLD, animatedFontSize));
        	    g.setColor(animatedColor);

        	    String comboText = "! " + combo + "X !";
        	    int textWidth = g.getFontMetrics().stringWidth(comboText);
        	    
        	    // Hafifçe dikeyde inip çıkan bir efekt de ekleyebiliriz (5 piksel dalgalanma)
        	    int baseY = 60 + combo; 
        	    int waveOffset = (int) (5 * Math.sin(elapsed / 150.0));
        	    int yPos = baseY + waveOffset;
        	    
        	    g.drawString(comboText, (getWidth() - textWidth) / 2, yPos);
        	}
        	
        	
        	if (19 < combo && combo <30) {
        	    // 1) Zaman & sinüs dalgaları
        	    long now = System.currentTimeMillis();
        	    long elapsed = now - comboStartTime; 
        	    
        	    // Nabız efekti: font boyutu %5 oranında dalgalansın
        	    float pulse = 1.0f + 0.05f * (float) Math.sin(elapsed / 100.0);
        	    int baseFontSize = 20 + 2 * combo;
        	    int animatedFontSize = (int) (baseFontSize * pulse);

        	    // Renk geçişi: #FF00D4 ↔ #FF0000
        	    // ratio, sinüs dalgasıyla 0 ~ 1 arasında gidip gelsin
        	    float ratio = 0.5f * (1.0f + (float) Math.sin(elapsed / 200.0));
        	    Color color1 = new Color(0xFF00D4); // Mor/pembe
        	    Color color2 = new Color(0xFF0000); // Kırmızı
        	    Color animatedColor = interpolateColor(color1, color2, ratio);

        	    // 2) Yazı özelliklerini uygula
        	    g.setFont(new Font("Arial", Font.BOLD, animatedFontSize));
        	    g.setColor(animatedColor);

        	    // Hafif yukarı-aşağı titreşim
        	    int baseY = 60 + combo;
        	    int waveOffset = (int) (5 * Math.sin(elapsed / 150.0));
        	    int yPos = baseY + waveOffset;

        	    // 3) Metin çiz
        	    String comboText = combo + "X WFT??!";
        	    int textWidth = g.getFontMetrics().stringWidth(comboText);
        	    g.drawString(comboText, (getWidth() - textWidth) / 2, yPos);
        	}
        	if (29 < combo) {
        	    // 1) Zaman & sinüs dalgaları
        	    long now = System.currentTimeMillis();
        	    long elapsed = now - comboStartTime; 
        	    
        	    // Nabız efekti: font boyutu %5 oranında dalgalansın
        	    float pulse = 1.0f + 0.05f * (float) Math.sin(elapsed / 100.0);
        	    int baseFontSize = 20 + 2 * combo;
        	    int animatedFontSize = (int) (baseFontSize * pulse);

        	    // Renk geçişi: #FF00D4 ↔ #FF0000
        	    // ratio, sinüs dalgasıyla 0 ~ 1 arasında gidip gelsin
        	    float ratio = 0.5f * (1.0f + (float) Math.sin(elapsed / 200.0));
        	    Color color1 = new Color(0xFF00D4); // Mor/pembe
        	    Color color2 = new Color(0xFF0000); // Kırmızı
        	    Color animatedColor = interpolateColor(color1, color2, ratio);

        	    // 2) Yazı özelliklerini uygula
        	    g.setFont(new Font("Arial", Font.BOLD, animatedFontSize));
        	    g.setColor(animatedColor);

        	    // Hafif yukarı-aşağı titreşim
        	    int baseY = 60 + combo;
        	    int waveOffset = (int) (5 * Math.sin(elapsed / 150.0));
        	    int yPos = baseY + waveOffset;

        	    // 3) Metin çiz
        	    String comboText = combo + "X you hacking?";
        	    int textWidth = g.getFontMetrics().stringWidth(comboText);
        	    g.drawString(comboText, (getWidth() - textWidth) / 2, yPos);
        	}
        }
    }

    private void drawStar(Graphics2D g2d, int centerX, int centerY, int radius) {
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];

        // Yıldızın dış ve iç köşelerini hesapla
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 5 * i; // 5 köşe için açı
            int r = (i % 2 == 0) ? radius : radius / 2; // Dış ve iç köşe yarıçapı
            xPoints[i] = centerX + (int) (Math.cos(angle) * r);
            yPoints[i] = centerY + (int) (Math.sin(angle) * r);
        }

        // Yıldızın ilk köşesinin ucunu kelimenin ortasına hizalayacağız
        int deltaX = xPoints[0] - centerX;
        int deltaY = yPoints[0] - centerY;

        // Yıldızın koordinatlarını hizala
        for (int i = 0; i < 10; i++) {
            xPoints[i] -= deltaX;
            yPoints[i] -= deltaY;
        }

        // Yıldızı çiz
        g2d.fillPolygon(xPoints, yPoints, 10);
    }
    private void drawWords(Graphics2D g) {
        // Evvela fontu büyütelim
        Font mevcutFont = g.getFont();
        // İster sabit bir değer (mesela 24.0f), ister mevcud boyutun katı
        // Font buyukFont = mevcutFont.deriveFont(mevcutFont.getSize() * 2.0f);
        Font buyukFont = mevcutFont.deriveFont(24.0f);
        g.setFont(buyukFont);

        for (Word word : words) {
            // Yeni fonta göre yeniden FontMetrics al
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(word.text);
            int textHeight = fm.getHeight();

            // Metin arka planı
            g.setColor(Color.black);
            g.fillRect(word.x - 5, word.y - textHeight + 5, textWidth + 10, textHeight);

            // Metni çiz
            g.setColor(Color.white);
            g.drawString(word.text, word.x, word.y);

            // Düşman 'glow' şekli
            if (word.speed == 1) {
                // Yavaş (speed=1)
                Color enemyColor = new Color(0xFFF28F);
                drawGlowingRect(g, word.x, word.y + 5, 60, 60, enemyColor);
            } else if (word.speed == 2) {
                // Orta (speed=2)
                Color enemyColor = new Color(0xA0F4BC);
                drawGlowingOval(g, word.x, word.y + 5, 40, 40, enemyColor);
            } else if (word.speed == 3) {
                // Hızlı (speed=3)
                Color enemyColor = new Color(0xF72E9D);
                drawGlowingStar(
                    g,
                    word.x + fm.stringWidth(word.text) / 2,
                    word.y + 30,
                    10,
                    enemyColor
                );
            }
        }
    }


    private void drawExplosions(Graphics2D g) {
        explosions.removeIf(Explosion::isExpired); // Süresi dolan patlamaları listeden siler
        for (Explosion explosion : explosions) {
            explosion.update(); // Patlamayı günceller
            explosion.draw(g); // Patlamayı çizer
        }
    }

    private void drawUserInput(Graphics2D g) {
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        int inputWidth = fm.stringWidth(inputText);
        int centeredX = playerX - inputWidth / 2; // Oyuncu karakterinin üzerinde ortalamak için hesaplama
        g.drawString(inputText, centeredX - 10, playerY - 20);
    }

    private void drawPlayerCharacter(Graphics2D g) {
        // Kenar yumuşatma ayarı
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Karakterin konumunu belirle
        playerX = getWidth() / 2 + 25; // Yatayda ortala + küçük bir öteleme
        playerY = getHeight() - 100;   // Alta yakın konum

        // Gölge
        g.setColor(new Color(0, 0, 0, 50)); // Hafif saydam siyah
        g.fillOval(playerX - 40, playerY + 10, 60, 60);

        // Önce pelerini çizelim (karakterin arkasında duracak)
        // Basit bir üçgen pelerin
        Polygon pelerin = new Polygon();
        pelerin.addPoint(playerX - 10, playerY + 10); // Pelerinin üst tepesi, yuvarlağın arkasına yakın
        pelerin.addPoint(playerX - 50, playerY + 50); // Hafif sola doğru açılan alt sol köşe
        pelerin.addPoint(playerX + 10, playerY + 60); // Alt sağ köşe (sanki karakterin arkasından sarkıyor)
        g.setColor(new Color(100, 0, 100, 128)); // Morumsu, yarısaydam ton
        g.fillPolygon(pelerin);

        // Asa (karakterin sağ tarafına fazlaca basit bir çizgi + ufak bir top ekleyelim)
        g.setStroke(new BasicStroke(4)); // Asanın kalınlığı
        g.setColor(new Color(139, 69, 19)); // Ağaçsı kahverengi
        // Dikey bir çizgi olarak asa
        g.drawLine(playerX + 30, playerY, playerX + 30, playerY + 50);
        // Tepesinde ufak bir daire
        g.fillOval(playerX + 25, playerY - 5, 10, 10);

        // Karakterin yuvarlak gövdesi (gradient boyama)
        GradientPaint gradient = new GradientPaint(
            playerX - 35, playerY, new Color(0x4FFBDF),
            playerX + 15, playerY + 50, new Color(0x2CA9D8)
        );
        g.setPaint(gradient);
        g.fillOval(playerX - 35, playerY, 50, 50);

        // Yuvarlağın dış çizgisi
        g.setColor(new Color(0x008080)); // Teal
        g.setStroke(new BasicStroke(3));
        g.drawOval(playerX - 35, playerY, 50, 50);
    }


    private void drawPlayerLives(Graphics2D g) {
        g.setFont(new Font("Arial",Font.BOLD,20));
        g.setColor(new Color(0x06DCD4));
        g.drawString("Lives: " + playerLives, 25, 80); // Oyuncunun can adedini metin olarak sunar
    }

    public void start() {
        running = true; // Oyun döngüsünü aktif hale getirir
        new Thread(this).start(); // Yeni bir iş parçacığı üzerinden oyunu başlatır
        soundPlayer = new SoundPlayer();
        soundPlayer.playBackgroundMusic("galacticknight.wav", true); // Tema müziğini başlat
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis(); // Döngünün son kaydedilen zamanını tutar

        while (running) {
            long now = System.currentTimeMillis(); // Mevcut zaman
            long updateLength = now - lastTime; // Bir önceki kareden bu kareye kadar geçen süre
            lastTime = now; // Zamanı günceller

            // Dalga bilgisinin 3 saniyeden fazla gösterilmeyeceğini kararlaştırır
            if (showWaveInfo && (System.currentTimeMillis() - waveInfoStartTime >= 3000)) {
                showWaveInfo = false; 
                allowEnemySpawning = true; 
            }

            // Dalga bilgisi gösterilmiyorsa ve düşman doğumuna izin varsa
            if (!showWaveInfo && allowEnemySpawning) {
                spawnPendingWords(now); // Doğma vakti gelmiş kelimeleri sahneye atar
                updateWordPositions();  // Kelimelerin konumunu yeniler
                checkUserInput();       // Oyuncunun girdiği metni kontrol eder
                checkCollisions();      // Oyuncu ile kelimeler çarpışmış mı diye bakar
            }
            repaint(); // Ekranı tazeler
            
            // Oyuncunun canı bitmişse oyun döngüsünü durdurur
            
            if (playerLives <= 0) {
                running = false;
                soundPlayer.stopBackgroundMusic();
                EndScreen endScreen = new EndScreen(this, score, currentWave, totalKill); // totalEnemiesKilled yerine doğru değeri aktar
                setLayout(new BorderLayout());
                removeAll();
                add(endScreen, BorderLayout.CENTER);
                revalidate();
                repaint();
            }

           
            // Ekranda kelime kalmamış ve bekleyen de yoksa yeni dalgaya geç
            if (words.isEmpty() && pendingWords.isEmpty() && !showWaveInfo) {
                advanceToNextWave(); // Dalga sayısını artırarak yeni dalga başlatır
                allowEnemySpawning = false; 
            }

            // Hedeflenen FPS'ye göre bir sonraki kareye kadar uyutma
            long sleepTime = OPTIMAL_TIME - (System.currentTimeMillis() - now);
            if (sleepTime > 0) {
                sleep(sleepTime);
            }
        }
    }

    private void spawnPendingWords(long currentTime) {
        // Doğma vakti gelen kelimeleri sahneye atar
        List<Word> toSpawn = new ArrayList<>();
        for (Word word : pendingWords) {
            if (currentTime >= word.spawnTime) {
                toSpawn.add(word);
            }
        }
        words.addAll(toSpawn);       // Sahnedeki kelimelere ekler
        pendingWords.removeAll(toSpawn); // Bekleyenlerden çıkarır
    }

    private void updateWordPositions() {
        // Her kelimenin dikey konumunu hızlarına göre değiştirir
        words.forEach(word -> {
            word.y += word.speed + 1;
            // Ekran dışında kalırlarsa tekrar yukarıdan başlatır
            if (word.y > getHeight()) {
                word.y = 0;
                word.x = random.nextInt(1200);
                if (word.speed <3) {
                	word.speed+=1;
                }
            }
        });
    }
    private void drawBeams(Graphics2D g) {
        beams.removeIf(Beam::isExpired);

        for (Beam beam : beams) {
            float progress = (float) (System.currentTimeMillis() - beam.creationTime) / Beam.LIFETIME;
            if (progress > 1) progress = 1;

            // Dynamic width that decreases over time
            float beamWidth = 6 * (1 - progress);
            g.setStroke(new BasicStroke(beamWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Create a gradient for the beam
            GradientPaint gradient = new GradientPaint(
                beam.startX, beam.startY, new Color(255, 255, 0, (int) (255 * (1 - progress))),
                beam.endX, beam.endY, new Color(255, 0, 0, (int) (255 * (1 - progress)))
            );
            g.setPaint(gradient);
            g.drawLine(beam.startX, beam.startY, beam.endX, beam.endY);

            // Add a glow effect by drawing wider, transparent strokes behind the main beam
            for (int i = 1; i <= 3; i++) {
                float glowWidth = beamWidth + i * 2;
                g.setStroke(new BasicStroke(glowWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(255, 0, 0, (int) (50 * (1 - progress))));
                g.drawLine(beam.startX, beam.startY, beam.endX, beam.endY);
            }
        }
    }

    private void checkUserInput() {
        List<Word> matchedWords = new ArrayList<>();
        for (Word word : words) {
            if (word.text.equalsIgnoreCase(inputText)) {
                matchedWords.add(word);
                explosions.add(new Explosion(word.x, word.y));
                score += 1 + combo;
                totalKill++;
                combo++;
                beams.add(new Beam(playerX + 30, playerY, word.x + 30, word.y, Color.RED));

                // Doğru yazılan kelime için ses efekti çal
                if (soundPlayer != null) {
                    soundPlayer.playSoundEffect("correct_music.wav");
                }
            }
        }
        words.removeAll(matchedWords);
        if (!matchedWords.isEmpty()) {
            inputText = "";
        }
    }
    

    private void checkCollisions() {
        // Oyuncu ile kelimelerin alanları çakışıyor mu diye bakar
        words.removeIf(word -> {
            Rectangle playerBounds = new Rectangle(playerX, playerY, 50, 50);
            Rectangle wordBounds = new Rectangle(word.x, word.y, 50, 50);
            if (playerBounds.intersects(wordBounds)) {
                playerLives--; // Çarpışma varsa oyuncu can kaybeder
                return true; // Kelime sahneden silinir
            }
            return false;
        });
    }

    private void advanceToNextWave() {
        // Bir sonraki dalgaya geçiş yapar, dalga sayılarını ve kelime miktarını günceller
        currentWave++;
        waveWordCount = currentWave; // Dalgadaki kelime miktarını dalga sayısına eşit kılar
        showWaveInfo = true; 
        waveInfoStartTime = System.currentTimeMillis(); 

        generateWordsForWave(); // This can now be outside the if-block
    }


    private void sleep(long milliseconds) {
        // Verilen süre kadar iş parçacığını uyutur
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Klavyeden tuş basımıyla ilgili metni yakalar
        char keyChar = e.getKeyChar();
        if (Character.isLetterOrDigit(keyChar)) {
            inputText += keyChar; // Harf veya rakamsa girdi metnine ekler
        } else if (keyChar == '\b' && inputText.length() > 0) {
            // Geri tuşu basılmışsa ve metin boş değilse son karakteri siler
            inputText = inputText.substring(0, inputText.length() - 1);
            combo = 0; // Geri tuşu basıldığı anda kombo sıfırlanır
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_ESCAPE) {
            pauseGame();
            openInGameMenu();
        }
    }

    private void pauseGame() {
        running = false;
    }

    private void openInGameMenu() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this); // Get the main frame
        InGameMenu menu = new InGameMenu(parentFrame, this, soundPlayer);
        parentFrame.setContentPane(menu);
        parentFrame.revalidate();
    }

    public void resume() {
        running = true;
        new Thread(this).start();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Tuş bırakıldığı an; şimdilik bir eylem yapmıyoruz
    }
}
