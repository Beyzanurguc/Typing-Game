/*Bera Çevik - 230610022
Eray Sona - 230611015
Beyza Nur Güç - 230611001
Hatice Kübra Aydın - 230611048
Serkan Gürbüz - 230611003
*/
package TypingGame;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Explosion {
    int x; // Patlamanın x koordinatı
    int y; // Patlamanın y koordinatı
    int baseSize; // Patlamanın temel boyutu
    long startTime; // Patlama başlangıç zamanı
    private static final int DURATION = 1400; // Patlama süresi (milisaniye) – bir miktar uzattık
    private List<Particle> particles; // Patlamadan çıkan parçacıklar
    private Color baseColor; // Başlangıçtaki patlama rengi

    // Shockwave parametreleri
    private boolean drawShockwave = true; // Şok dalgası (halka) görünür mü
    private float shockwaveThickness = 3f; // Halka çizgisi kalınlığı
    private Color shockwaveColor = Color.WHITE; // Halka rengi

    public Explosion(int x, int y) {
        this.x = x +25;
        this.y = y +25;
        // Boyut 30 ile 50 arasında
        this.baseSize = 30 + new Random().nextInt(20);
        this.startTime = System.currentTimeMillis();
        this.baseColor = generateRandomColor();
        this.particles = generateParticles();
    }

    private List<Particle> generateParticles() {
        List<Particle> particleList = new ArrayList<>();
        Random random = new Random();
        int particleCount = 25 + random.nextInt(35); // 25 ile 60 arasında parçacık

        for (int i = 0; i < particleCount; i++) {
            double angle = random.nextDouble() * 2 * Math.PI; // Rastgele açı
            double speed = 2 + random.nextDouble() * 3;       // Hız: 2 - 5
            // Rastgele şekil: 0 -> daire, 1 -> üçgen, 2 -> mini-yıldız
            int shapeType = random.nextInt(3);
            particleList.add(new Particle(x, y, angle, speed, generateRandomColor(), shapeType));
        }
        return particleList;
    }

    private Color generateRandomColor() {
        Random random = new Random();
        return new Color(random.nextInt(256),
                         random.nextInt(256),
                         random.nextInt(256));
    }

    public boolean isExpired() {
        // Patlama süresinin dolup dolmadığını kontrol eder
        return System.currentTimeMillis() - startTime > DURATION;
    }

    public void update() {
        // Her parçacığın hareketini günceller
        for (Particle particle : particles) {
            particle.update();
        }
    }

    /**
     * Patlamayı çiz. Neon bir daire, ek shockwave, parçacıklar ve ufak renk dalgalanmaları.
     */
    public void draw(Graphics2D g) {
        if (isExpired()) return;

        long elapsed = System.currentTimeMillis() - startTime;
        float progress = (float) elapsed / DURATION; // 0.0 -> başlangıç, 1.0 -> bitiş

        // Patlamanın boyutunda ufak titreşim verelim
        double flicker = Math.sin(elapsed / 60.0) * 5; 
        int currentSize = (int) (baseSize + flicker);

        // Renk zamanla değişsin: baseColor'dan beyaza doğru
        Color currentColor = interpolateColor(baseColor, Color.WHITE, progress * 0.7f);

        // ==========================
        // 1) Glowlu daire katmanları
        // ==========================
        Graphics2D g2d = (Graphics2D) g.create();
        // Her katmanda artan saydamlıkla daireler çiziyoruz
        int glowLayers = 6;
        for (int i = glowLayers; i > 0; i--) {
            float alpha = 0.05f * i; 
            g2d.setColor(new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), (int) (alpha * 255)));

            // Biraz daha büyük daire çiz
            int size = currentSize + i * 15;
            g2d.fillOval(x - size / 2, y - size / 2, size, size);
        }
        g2d.dispose();

        // ==========================
        // 2) Şok dalgası
        // ==========================
        if (drawShockwave) {
            float shockProgress = progress * 1.2f; 
            float maxRadius = baseSize * 4; // Şok dalgası ne kadar ilerlesin
            float radius = shockProgress * maxRadius; 

            // Shockwave yavaşça saydamlaşsın
            float alpha = 1.0f - shockProgress;
            if (alpha < 0f) alpha = 0f;

            // Halkanın çizimi
            Graphics2D shockG = (Graphics2D) g.create();
            shockG.setStroke(new BasicStroke(shockwaveThickness));
            shockG.setColor(new Color(shockwaveColor.getRed(), shockwaveColor.getGreen(),
                    shockwaveColor.getBlue(), (int) (alpha * 255)));
            shockG.drawOval((int) (x - radius), (int) (y - radius), (int) (2 * radius), (int) (2 * radius));
            shockG.dispose();
        }

        // ==========================
        // 3) Asıl dolu daire (merkez)
        // ==========================
        g.setColor(currentColor);
        g.fillOval(x - currentSize / 2, y - currentSize / 2, currentSize, currentSize);

        // ==========================
        // 4) Parçacıkları çiz
        // ==========================
        for (Particle particle : particles) {
            particle.draw(g, progress);
        }
    }

    /**
     * Renk interpolasyonu: c1 -> c2 doğrultusunda progress oranında karışım yapar.
     */
    private Color interpolateColor(Color c1, Color c2, float progress) {
        if (progress > 1f) progress = 1f;
        int r = (int) (c1.getRed()   + (c2.getRed()   - c1.getRed())   * progress);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int) (c1.getBlue()  + (c2.getBlue()  - c1.getBlue())  * progress);
        int a = (int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * progress);
        return new Color(r, g, b, a);
    }

    /**
     * Parçacık alt sınıfı: boyutu, şekli, hareketi, glow efekti vb.
     */
    private class Particle {
        double x; 
        double y; 
        double angle; 
        double speed; 
        Color color; 
        int shapeType; 
        double size; 

        Particle(double x, double y, double angle, double speed, Color color, int shapeType) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.speed = speed;
            this.color = color;
            this.shapeType = shapeType;
            this.size = 8 + new Random().nextDouble() * 8; // 8 - 16
        }

        void update() {
            // Hareket
            x += Math.cos(angle) * speed;
            y += Math.sin(angle) * speed;
            // Sürtünme
            speed *= 0.92;
        }

        // progress: patlamanın ne kadar ilerlediği (0.0 -> 1.0)
        void draw(Graphics2D g, float progress) {
            // Patlama süresi içinde yavaşça yok olma
            float alphaFactor = 1.0f - progress * 1.2f;
            if (alphaFactor < 0f) alphaFactor = 0f;

            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaFactor));
            
            // Birkaç katman glow ekleyelim
            int glowLayers = 3; 
            for (int i = glowLayers; i > 0; i--) {
                float alphaLayer = 0.2f * i * alphaFactor; 
                Color glowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alphaLayer * 255));
                g2d.setColor(glowColor);
                double layerSize = size + i * 2;
                
                // Şekil seçimi
                if (shapeType == 0) {
                    // Daire
                    g2d.fillOval((int) (x - layerSize / 2), (int) (y - layerSize / 2),
                                 (int) layerSize, (int) layerSize);
                } else if (shapeType == 1) {
                    // Üçgen
                    Polygon triangle = createTriangle(x, y, layerSize);
                    g2d.fillPolygon(triangle);
                } else {
                    // Mini-yıldız
                    Polygon star = createStar(x, y, layerSize);
                    g2d.fillPolygon(star);
                }
            }
            
            g2d.dispose();
        }

        /**
         * Basit bir eşkenar üçgen yaratır.
         */
        private Polygon createTriangle(double cx, double cy, double size) {
            Polygon triangle = new Polygon();
            for (int i = 0; i < 3; i++) {
                double theta = 2 * Math.PI * i / 3;
                int px = (int) (cx + size * Math.cos(theta));
                int py = (int) (cy + size * Math.sin(theta));
                triangle.addPoint(px, py);
            }
            return triangle;
        }

        /**
         * Basit 5 uçlu yıldız. 
         */
        private Polygon createStar(double cx, double cy, double outerRadius) {
            Polygon star = new Polygon();
            int innerRadius = (int) (outerRadius / 2); 
            for (int i = 0; i < 10; i++) {
                double r = (i % 2 == 0) ? outerRadius : innerRadius;
                double theta = Math.PI / 5 * i;
                int px = (int) (cx + r * Math.cos(theta));
                int py = (int) (cy + r * Math.sin(theta));
                star.addPoint(px, py);
            }
            return star;
        }
    }
}
