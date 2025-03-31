/*Bera Çevik - 230610022
Eray Sona - 230611015
Beyza Nur Güç - 230611001
Hatice Kübra Aydın - 230611048
Serkan Gürbüz - 230611003
*/
package TypingGame;

import java.awt.Color;

public class Beam {
	    int startX, startY, endX, endY;
	    Color color;
	    long creationTime;
	    static final int LIFETIME = 500; // Beam visible for 500ms

	    public Beam(int startX, int startY, int endX, int endY, Color color) {
	        this.startX = startX;
	        this.startY = startY;
	        this.endX = endX;
	        this.endY = endY;
	        this.color = color;
	        this.creationTime = System.currentTimeMillis();
	    }

	    public boolean isExpired() {
	        return System.currentTimeMillis() - creationTime > LIFETIME;
	    }
	
}
