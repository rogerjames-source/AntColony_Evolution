package entity;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Random;

public class Predator {
    public double x, y;
    public double angle;
    public double baseSpeed;
    public double currentSpeed;
    public int size;
    public String type;

    private int leapCooldown = 0;
    private double leapDx = 0;
    private double leapDy = 0;
    private Random random = new Random();
    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    public Predator(double x, double y, String type) {
        this.type = type;
        this.x = x;
        this.y = y;
        configureStats();
    }

    private void configureStats() {
        if (type.equals("Spider")) {
            this.size = 32;
            this.baseSpeed = 1.5 + random.nextDouble() * 0.8;
        } else {
            this.size = 40;
            this.baseSpeed = 0;
        }
        this.currentSpeed = this.baseSpeed;
    }

    public void updateSpeedScale(double multiplier) {
        if (type.equals("Spider")) this.currentSpeed = this.baseSpeed * multiplier;
    }

    public void respawnAfterKill(double playerX, double playerY) {
        boolean safeSpawn = false;
        while (!safeSpawn) {
            this.x = random.nextBoolean() ? -60 : WIDTH + 60;
            this.y = random.nextInt(HEIGHT);
            if (Math.hypot(this.x - playerX, this.y - playerY) > 200) safeSpawn = true;
        }
        leapCooldown = 0;
        leapDx = 0;
        leapDy = 0;
    }

    public void hunt(double playerX, double playerY, String globalDiff, int totalPredators) {
        if (type.equals("Spider")) {
            angle = Math.atan2(playerY - y, playerX - x);
            x += Math.cos(angle) * currentSpeed;
            y += Math.sin(angle) * currentSpeed;
        } else if (type.equals("Frog")) {
            if (leapCooldown <= 0) {
                angle = Math.atan2(playerY - y, playerX - x);
                double dynamicLeapForce = totalPredators > 11 ? 13.5 : (totalPredators > 7 ? 10.5 : 7.5);
                leapDx = Math.cos(angle) * dynamicLeapForce;
                leapDy = Math.sin(angle) * dynamicLeapForce;
                leapCooldown = (globalDiff.equals("INSANE") ? 15 : 45) + random.nextInt(30);
            } else {
                x += leapDx;
                y += leapDy;
                leapDx *= 0.85;
                leapDy *= 0.85;
                leapCooldown--;
            }
        }
    }

    public void separate(ArrayList<Predator> others, double minDist) {
        for (Predator other : others) {
            if (other != this) {
                double d = Math.hypot(this.x - other.x, this.y - other.y);
                if (d < minDist && d > 0) {
                    this.x += ((this.x - other.x) / d) * 1.2;
                    this.y += ((this.y - other.y) / d) * 1.2;
                }
            }
        }
    }

    public void draw(Graphics2D g2, Image img) {
        AffineTransform oldTransform = g2.getTransform();
        g2.translate(x, y);
        g2.rotate(angle + Math.PI / 2);
        g2.drawImage(img, -size / 2, -size / 2, size, size, null);
        g2.setTransform(oldTransform);
    }
}