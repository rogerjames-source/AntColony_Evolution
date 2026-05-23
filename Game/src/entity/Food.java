package entity;

import java.awt.*;

public class Food {
    public double x, y;
    public int size = 20;

    public Food(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics2D g2, Image img) {
        g2.drawImage(img, (int)x - size / 2, (int)y - size / 2, size, size, null);
    }
}