package entity;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import javax.swing.ImageIcon;

public class PlayerAnt {
    public double x, y;
    public double targetX, targetY;
    public double angle;
    public double speed = 3.5;
    public int size = 16;
    public int exp = 0;
    public int expNeeded = 100;
    public String evolutionStage = "Larva";
    private Image currentFormImg;

    public PlayerAnt(double x, double y, HashMap<String, Image> speciesImages) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        updateImage(speciesImages);
    }

    public void move() {
        double distance = Math.hypot(targetX - x, targetY - y);
        if (distance > 4) {
            angle = Math.atan2(targetY - y, targetX - x);
            x += Math.cos(angle) * speed;
            y += Math.sin(angle) * speed;
        }
    }

    public boolean gainExp(int amount) {
        if (evolutionStage.equals("Imperial Queen") || evolutionStage.equals("Army Commander")) return false;
        exp += amount;
        if (exp >= expNeeded) {
            exp = 0;
            return true;
        }
        return false;
    }

    public void evolveInto(String targetForm, HashMap<String, Image> speciesImages) {
        this.evolutionStage = targetForm;
        switch (targetForm) {
            case "Worker Ant":       size = 24; speed = 4.2; expNeeded = 400; break;
            case "Guerilla Scout":   size = 24; speed = 5.8; expNeeded = 400; break;
            case "Soldier Ant":      size = 36; speed = 4.6; expNeeded = 600; break;
            case "Leafcutter Raider":size = 32; speed = 5.0; expNeeded = 580; break;
            case "Bullet Ant Knight":size = 42; speed = 3.2; expNeeded = 650; break;
            case "Imperial Queen":   size = 56; speed = 5.4; break;
            case "Army Commander":   size = 52; speed = 5.6; break;
        }
        updateImage(speciesImages);
    }

    public void updateImage(HashMap<String, Image> speciesImages) {
        currentFormImg = speciesImages.get(evolutionStage);
        if (currentFormImg == null) {
            String cleanName = evolutionStage.toLowerCase().replace(" ", "_");
            currentFormImg = new ImageIcon("res/img/" + cleanName + ".png").getImage();
        }
    }

    public void draw(Graphics2D g2) {
        AffineTransform oldTransform = g2.getTransform();
        g2.translate(x, y);
        g2.rotate(angle + Math.PI / 2);
        g2.drawImage(currentFormImg, -size / 2, -size / 2, size, size, null);
        g2.setTransform(oldTransform);
    }
}