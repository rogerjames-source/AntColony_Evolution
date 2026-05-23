package util;

import java.awt.*;

public class EvolveParticle {
    public double x, y;
    public float currentRadius;
    public float limitRadius;
    public int opacity = 255;

    public EvolveParticle(double x, double y, int playerSize) {
        this.x = x;
        this.y = y;
        this.currentRadius = playerSize / 2f;
        this.limitRadius = currentRadius + 75f;
    }

    public void update() {
        currentRadius += 3.5f;
        opacity = (int)(255 * (1f - (currentRadius / limitRadius)));
        if (opacity < 0) opacity = 0;
    }

    public boolean isDead() {
        return currentRadius >= limitRadius || opacity <= 0;
    }

    public void draw(Graphics2D g2) {
        g2.setColor(new Color(0, 255, 127, opacity));
        g2.setStroke(new BasicStroke(4f));
        int sizeOffset = (int)(currentRadius * 2);
        g2.drawOval((int)(x - currentRadius), (int)(y - currentRadius), sizeOffset, sizeOffset);
    }
}