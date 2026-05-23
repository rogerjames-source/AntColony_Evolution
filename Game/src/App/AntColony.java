package App;

import entity.Food;
import entity.PlayerAnt;
import entity.Predator;
import state.Difficulty;
import state.GameState;
import util.EvolveParticle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class AntColony extends JPanel implements Runnable {
    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    private int currentMaxPredators = 4;
    private final int NUM_FOOD = 15;

    private GameState currentState = GameState.COVER_PAGE;
    private Difficulty chosenDifficulty = Difficulty.EASY;
    private boolean sfxEnabled = true;

    private PlayerAnt player;
    private ArrayList<Food> foodList;
    private ArrayList<Predator> predators;
    private Random random = new Random();

    private static HashSet<String> unlockedAntsRegistry = new HashSet<>();
    private ArrayList<String> evolutionOptions = new ArrayList<>();

    private final String[] ALL_SPECIES = {
            "Larva", "Worker Ant", "Guerilla Scout", "Soldier Ant",
            "Leafcutter Raider", "Bullet Ant Knight", "Imperial Queen", "Army Commander"
    };

    private int damageFlashTimer = 0;
    private int shakeTimer = 0;
    private ArrayList<EvolveParticle> evolveParticles = new ArrayList<>();

    private Image titleBgImg, bgImg, spiderImg, frogImg, foodImg;
    private Image heartFullImg, heartEmptyImg;
    private Font gameFont;

    private HashMap<String, Image> buttonImages = new HashMap<>();
    private HashMap<String, Image> speciesImages = new HashMap<>();

    private int lives = 3;
    private int maxLives = 3;
    private int score = 0;
    private ArrayList<Integer> highScores = new ArrayList<>();

    private boolean hit700 = false;
    private boolean hit1000 = false;
    private boolean hit1500 = false;

    private Rectangle covPlayBtn = new Rectangle(WIDTH / 2 - 120, HEIGHT / 2 - 50, 240, 50);
    private Rectangle covShowcaseBtn = new Rectangle(WIDTH / 2 - 120, HEIGHT / 2 + 15, 240, 50);
    private Rectangle covSettingsBtn = new Rectangle(WIDTH / 2 - 120, HEIGHT / 2 + 80, 240, 50);
    private Rectangle covExitBtn = new Rectangle(WIDTH / 2 - 120, HEIGHT / 2 + 145, 240, 50);

    private Rectangle diffEasyBtn = new Rectangle(WIDTH / 2 - 250, HEIGHT / 2 + 10, 140, 40);
    private Rectangle diffHardBtn = new Rectangle(WIDTH / 2 - 70, HEIGHT / 2 + 10, 140, 40);
    private Rectangle diffInsaneBtn = new Rectangle(WIDTH / 2 + 110, HEIGHT / 2 + 10, 140, 40);
    private Rectangle diffStartBtn = new Rectangle(WIDTH / 2 - 110, HEIGHT / 2 + 90, 220, 50);
    private Rectangle diffBackBtn = new Rectangle(WIDTH / 2 - 110, HEIGHT / 2 + 160, 220, 40);

    private Rectangle setSfxToggleBtn = new Rectangle(WIDTH / 2 - 150, HEIGHT / 2 - 10, 300, 50);
    private Rectangle setBackBtn = new Rectangle(WIDTH / 2 - 100, HEIGHT / 2 + 90, 200, 45);

    private Rectangle restartBtn = new Rectangle(WIDTH / 2 - 230, HEIGHT / 2 + 175, 210, 45);
    private Rectangle menuBtn = new Rectangle(WIDTH / 2 + 20, HEIGHT / 2 + 175, 210, 45);
    private Rectangle showcaseBackBtn = new Rectangle(WIDTH / 2 - 100, HEIGHT - 75, 200, 45);

    private Rectangle leftChoiceCard = new Rectangle(WIDTH / 2 - 220, HEIGHT / 2 - 60, 200, 120);
    private Rectangle rightChoiceCard = new Rectangle(WIDTH / 2 + 20, HEIGHT / 2 - 60, 200, 120);

    public AntColony() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);

        unlockedAntsRegistry.add("Larva");

        titleBgImg = new ImageIcon("res/img/title_background.png").getImage();
        bgImg = new ImageIcon("res/img/background.jpg").getImage();
        spiderImg = new ImageIcon("res/img/spider.png").getImage();
        frogImg = new ImageIcon("res/img/frog.png").getImage();
        foodImg = new ImageIcon("res/img/leaf.png").getImage();
        heartFullImg = new ImageIcon("res/img/heart_full.png").getImage();
        heartEmptyImg = new ImageIcon("res/img/heart_empty.png").getImage();

        for (String ant : ALL_SPECIES) {
            String cleanName = ant.toLowerCase().replace(" ", "_");
            speciesImages.put(ant, new ImageIcon("res/img/" + cleanName + ".png").getImage());
        }

        try {
            File fontFile = new File("res/font/sweet_milk.ttf");
            if (fontFile.exists()) {
                gameFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(gameFont);
            } else {
                gameFont = new Font("Courier New", Font.BOLD, 16);
            }
        } catch (Exception e) {
            gameFont = new Font("Courier New", Font.BOLD, 16);
        }

        player = new PlayerAnt(WIDTH / 2.0, HEIGHT / 2.0, speciesImages);
        foodList = new ArrayList<>();
        predators = new ArrayList<>();

        adjustDifficultySettings();
        spawnInitialFood();

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (currentState == GameState.PLAYING) {
                    player.targetX = e.getX();
                    player.targetY = e.getY();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point click = e.getPoint();

                if (currentState == GameState.COVER_PAGE) {
                    if (covPlayBtn.contains(click)) {
                        playSound("res/sfx/click.wav");
                        currentState = GameState.DIFFICULTY_MENU;
                    } else if (covShowcaseBtn.contains(click)) {
                        playSound("res/sfx/click.wav");
                        currentState = GameState.SHOWCASE_MENU;
                    } else if (covSettingsBtn.contains(click)) {
                        playSound("res/sfx/click.wav");
                        currentState = GameState.SETTINGS_MENU;
                    } else if (covExitBtn.contains(click)) {
                        playSound("res/sfx/click.wav");
                        System.exit(0);
                    }
                }
                else if (currentState == GameState.DIFFICULTY_MENU) {
                    if (diffEasyBtn.contains(click))   { playSound("res/sfx/click.wav"); chosenDifficulty = Difficulty.EASY; }
                    if (diffHardBtn.contains(click))   { playSound("res/sfx/click.wav"); chosenDifficulty = Difficulty.HARD; }
                    if (diffInsaneBtn.contains(click)) { playSound("res/sfx/click.wav"); chosenDifficulty = Difficulty.INSANE; }

                    if (diffStartBtn.contains(click)) {
                        playSound("res/sfx/click.wav");
                        adjustDifficultySettings();
                        currentState = GameState.PLAYING;
                    } else if (diffBackBtn.contains(click)) {
                        playSound("res/sfx/click.wav");
                        currentState = GameState.COVER_PAGE;
                    }
                }
                else if (currentState == GameState.SETTINGS_MENU) {
                    if (setSfxToggleBtn.contains(click)) {
                        sfxEnabled = !sfxEnabled;
                        playSound("res/sfx/click.wav");
                    } else if (setBackBtn.contains(click)) {
                        playSound("res/sfx/click.wav");
                        currentState = GameState.COVER_PAGE;
                    }
                }
                else if (currentState == GameState.SHOWCASE_MENU) {
                    if (showcaseBackBtn.contains(click)) {
                        playSound("res/sfx/click.wav");
                        currentState = GameState.COVER_PAGE;
                    }
                }
                else if (currentState == GameState.EVOLUTION_SELECT) {
                    if (leftChoiceCard.contains(click) && evolutionOptions.size() > 0) {
                        applyEvolutionBranch(evolutionOptions.get(0));
                    } else if (rightChoiceCard.contains(click) && evolutionOptions.size() > 1) {
                        applyEvolutionBranch(evolutionOptions.get(1));
                    }
                }
                else if (currentState == GameState.GAME_OVER) {
                    if (restartBtn.contains(click)) {
                        playSound("res/sfx/click.wav");
                        resetGame();
                    } else if (menuBtn.contains(click)) {
                        playSound("res/sfx/click.wav");
                        backToMenu();
                    }
                }
            }
        });

        new Thread(this).start();
    }

    private void triggerEvolutionWindow() {
        evolutionOptions.clear();
        currentState = GameState.EVOLUTION_SELECT;
        playSound("res/sfx/evolve_ready.wav");

        switch (player.evolutionStage) {
            case "Larva":
                evolutionOptions.add("Worker Ant");
                evolutionOptions.add("Guerilla Scout");
                break;
            case "Worker Ant":
            case "Guerilla Scout":
                evolutionOptions.add("Soldier Ant");
                evolutionOptions.add("Leafcutter Raider");
                evolutionOptions.add("Bullet Ant Knight");
                Collections.shuffle(evolutionOptions);
                while (evolutionOptions.size() > 2) evolutionOptions.remove(0);
                break;
            case "Soldier Ant":
            case "Leafcutter Raider":
            case "Bullet Ant Knight":
                evolutionOptions.add("Imperial Queen");
                evolutionOptions.add("Army Commander");
                break;
        }
    }

    private void applyEvolutionBranch(String targetForm) {
        player.evolveInto(targetForm, speciesImages);
        unlockedAntsRegistry.add(targetForm);

        evolveParticles.add(new EvolveParticle(player.x, player.y, player.size));
        playSound("res/sfx/evolve.wav");

        adjustDifficultySettings();
        currentState = GameState.PLAYING;
    }

    private void playSound(String filename) {
        if (!sfxEnabled) return;
        new Thread(() -> {
            try {
                File soundFile = new File(filename);
                if (soundFile.exists()) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(AudioSystem.getAudioInputStream(soundFile));
                    clip.start();
                }
            } catch (Exception e) {
                System.err.println("Audio error: " + filename);
            }
        }).start();
    }

    private void resetGame() {
        lives = maxLives; score = 0;
        hit700 = false; hit1000 = false; hit1500 = false;
        damageFlashTimer = 0; shakeTimer = 0;
        evolveParticles.clear();
        player = new PlayerAnt(WIDTH / 2.0, HEIGHT / 2.0, speciesImages);
        predators.clear(); foodList.clear();
        adjustDifficultySettings();
        spawnInitialFood();
        currentState = GameState.PLAYING;
    }

    private void backToMenu() {
        lives = maxLives; score = 0;
        hit700 = false; hit1000 = false; hit1500 = false;
        damageFlashTimer = 0; shakeTimer = 0;
        evolveParticles.clear();
        player = new PlayerAnt(WIDTH / 2.0, HEIGHT / 2.0, speciesImages);
        predators.clear(); foodList.clear();
        adjustDifficultySettings();
        spawnInitialFood();
        currentState = GameState.COVER_PAGE;
    }

    private void archiveCurrentScore(int finalScore) {
        highScores.add(finalScore);
        highScores.sort(Collections.reverseOrder());
        while (highScores.size() > 5) highScores.remove(highScores.size() - 1);
    }

    private void adjustDifficultySettings() {
        double stageMultiplier = 1.0;

        switch (player.evolutionStage) {
            case "Larva":            currentMaxPredators = 4;  stageMultiplier = 1.0; break;
            case "Worker Ant":       currentMaxPredators = 6;  stageMultiplier = 1.2; break;
            case "Guerilla Scout":   currentMaxPredators = 6;  stageMultiplier = 1.1; break;
            case "Soldier Ant":      currentMaxPredators = 9;  stageMultiplier = 1.5; break;
            case "Leafcutter Raider":currentMaxPredators = 10; stageMultiplier = 1.4; break;
            case "Bullet Ant Knight":currentMaxPredators = 8;  stageMultiplier = 1.3; break;
            case "Imperial Queen":   currentMaxPredators = 12; stageMultiplier = 1.9; break;
            case "Army Commander":   currentMaxPredators = 13; stageMultiplier = 1.8; break;
        }

        if (score >= 700 && !hit700) { currentMaxPredators += 2; stageMultiplier += 0.3; hit700 = true; }
        if (score >= 1000 && !hit1000) { currentMaxPredators += 2; stageMultiplier += 0.4; hit1000 = true; }
        if (score >= 1500 && !hit1500) { currentMaxPredators += 3; stageMultiplier += 0.6; hit1500 = true; }
        if (score > 1500) stageMultiplier += (score - 1500) / 1000.0;

        double globalDifficultyModifier = 1.0;
        if (chosenDifficulty == Difficulty.HARD)   globalDifficultyModifier = 1.3;
        if (chosenDifficulty == Difficulty.INSANE) globalDifficultyModifier = 1.6;

        double finalVelocityScale = stageMultiplier * globalDifficultyModifier;

        for (Predator p : predators) p.updateSpeedScale(finalVelocityScale);

        while (predators.size() < currentMaxPredators) {
            double rx = random.nextBoolean() ? -50 : WIDTH + 50;
            double ry = random.nextInt(HEIGHT);
            String type = random.nextBoolean() ? "Spider" : "Frog";
            Predator newEnemy = new Predator(rx, ry, type);
            newEnemy.updateSpeedScale(finalVelocityScale);
            predators.add(newEnemy);
        }
    }

    private void spawnInitialFood() {
        foodList.clear();
        for (int i = 0; i < NUM_FOOD; i++) {
            double rx = random.nextInt(WIDTH - 60) + 30;
            double ry = random.nextInt(HEIGHT - 60) + 30;
            foodList.add(new Food(rx, ry));
        }
    }

    @Override
    public void run() {
        while (true) {
            if (currentState == GameState.PLAYING) updateGame();
            repaint();
            try { Thread.sleep(16); } catch (InterruptedException e) { break; }
        }
    }

    private void updateGame() {
        player.move();

        for (int i = evolveParticles.size() - 1; i >= 0; i--) {
            EvolveParticle p = evolveParticles.get(i);
            p.update();
            if (p.isDead()) evolveParticles.remove(i);
        }

        if (damageFlashTimer > 0) damageFlashTimer--;
        if (shakeTimer > 0) shakeTimer--;

        for (int i = foodList.size() - 1; i >= 0; i--) {
            Food food = foodList.get(i);
            double dist = Math.hypot(player.x - food.x, player.y - food.y);

            if (dist < (player.size / 2.0 + food.size / 2.0)) {
                int gainedExp = player.evolutionStage.equals("Leafcutter Raider") ? 25 : 15;
                boolean structuralThresholdHit = player.gainExp(gainedExp);
                score += 10;
                foodList.remove(i);

                if (structuralThresholdHit) triggerEvolutionWindow();
                else playSound("res/sfx/eat.wav");

                if ((score >= 700 && !hit700) || (score >= 1000 && !hit1000) || (score >= 1500 && !hit1500)) {
                    adjustDifficultySettings();
                }
            }
        }

        if (foodList.size() < NUM_FOOD) {
            double rx = (double) (random.nextInt(WIDTH - 60) + 30);
            double ry = (double) (random.nextInt(HEIGHT - 60) + 30);
            foodList.add(new Food(rx, ry));
        }

        for (Predator predator : predators) {
            predator.hunt(player.x, player.y, chosenDifficulty.name(), predators.size());
            predator.separate(predators, 35.0);

            double distToPlayer = Math.hypot(player.x - predator.x, player.y - predator.y);
            if (distToPlayer < (player.size / 2.0 + predator.size / 2.0)) {
                lives--;
                predator.respawnAfterKill(player.x, player.y);
                damageFlashTimer = 10;
                if (!player.evolutionStage.equals("Bullet Ant Knight")) shakeTimer = 12;

                if (lives <= 0) {
                    playSound("res/sfx/gameover.wav");
                    archiveCurrentScore(score);
                    currentState = GameState.GAME_OVER;
                } else {
                    playSound("res/sfx/hit.wav");
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        AffineTransform originalViewTransform = g2.getTransform();
        if (currentState == GameState.PLAYING && shakeTimer > 0) {
            g2.translate(random.nextInt(11) - 5, random.nextInt(11) - 5);
        }

        switch (currentState) {
            case COVER_PAGE:       renderCoverPage(g2); break;
            case DIFFICULTY_MENU:  renderDifficultyMenu(g2); break;
            case SETTINGS_MENU:    renderSettingsMenu(g2); break;
            case PLAYING:          renderGameContent(g2); break;
            case EVOLUTION_SELECT: renderEvolutionChoicePanel(g2); break;
            case GAME_OVER:        renderGameOverScreen(g2); break;
            case SHOWCASE_MENU:    renderShowcaseMenu(g2); break;
        }

        g2.setTransform(originalViewTransform);

        if (currentState == GameState.PLAYING && damageFlashTimer > 0) {
            g2.setColor(new Color(255, 0, 0, Math.min(damageFlashTimer * 20, 180)));
            g2.fillRect(0, 0, WIDTH, HEIGHT);
        }
    }

    private void renderCoverPage(Graphics2D g2) {
        g2.drawImage(titleBgImg, 0, 0, WIDTH, HEIGHT, null);
        g2.setColor(new Color(0, 0, 0, 150)); g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(new Color(223, 177, 98)); g2.setFont(gameFont.deriveFont(Font.BOLD, 60f));
        g2.drawString("ANT COLONY : EVOLUTION", WIDTH / 2 - 200, HEIGHT / 2 - 120);
        g2.setFont(gameFont.deriveFont(Font.ITALIC, 16f)); g2.setColor(Color.WHITE);
        g2.drawString("Group - Sevenn : The Mulag", WIDTH / 2 - 60, HEIGHT / 2 - 90);

        drawMenuButton(g2, covPlayBtn, "START RUN", false, new Color(218, 165, 32), "res/img/btn.png");
        drawMenuButton(g2, covShowcaseBtn, "ANT SHOWCASE GALLERY", false, new Color(34, 139, 34), "res/img/btn.png");
        drawMenuButton(g2, covSettingsBtn, "SETTINGS", false, new Color(70, 130, 180), "res/img/btn.png");
        drawMenuButton(g2, covExitBtn, "EXIT GAME", false, new Color(178, 34, 34), "res/img/btn.png");
    }

    private void renderDifficultyMenu(Graphics2D g2) {
        g2.drawImage(titleBgImg, 0, 0, WIDTH, HEIGHT, null);
        g2.setColor(new Color(0, 0, 0, 150)); g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(new Color(223, 177, 98)); g2.setFont(gameFont.deriveFont(Font.BOLD, 42f));
        g2.drawString("CHOOSE DIFFICULTY LEVEL", WIDTH / 2 - 170, HEIGHT / 2 - 90);

        drawMenuButton(g2, diffEasyBtn, "EASY", chosenDifficulty == Difficulty.EASY, new Color(46, 139, 87), "res/img/btn.png");
        drawMenuButton(g2, diffHardBtn, "HARD", chosenDifficulty == Difficulty.HARD, new Color(205, 92, 92), "res/img/btn.png");
        drawMenuButton(g2, diffInsaneBtn, "INSANE", chosenDifficulty == Difficulty.INSANE, new Color(139, 0, 0), "res/img/btn.png");
        drawMenuButton(g2, diffStartBtn, "LAUNCH ANTS", false, new Color(218, 165, 32), "res/img/btn.png");
        drawMenuButton(g2, diffBackBtn, "RETURN", false, Color.DARK_GRAY, "res/img/btn.png");
    }

    private void renderSettingsMenu(Graphics2D g2) {
        g2.drawImage(titleBgImg, 0, 0, WIDTH, HEIGHT, null);
        g2.setColor(new Color(0, 0, 0, 160)); g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(new Color(223, 177, 98)); g2.setFont(gameFont.deriveFont(Font.BOLD, 46f));
        g2.drawString("CONTROL PANEL", WIDTH / 2 - 170, HEIGHT / 2 - 100);

        String sfxText = "AUDIO: " + (sfxEnabled ? "ENABLED" : "MUTED");
        drawMenuButton(g2, setSfxToggleBtn, sfxText, sfxEnabled, sfxEnabled ? new Color(46, 139, 87) : Color.DARK_GRAY, null);
        drawMenuButton(g2, setBackBtn, "SAVE & RETURN", false, new Color(218, 165, 32), "res/img/btn.png");
    }

    private void renderShowcaseMenu(Graphics2D g2) {
        g2.drawImage(titleBgImg, 0, 0, WIDTH, HEIGHT, null);
        g2.setColor(new Color(15, 15, 20, 235)); g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(new Color(223, 177, 98)); g2.setFont(gameFont.deriveFont(Font.BOLD, 36f));
        g2.drawString("EVOLUTIONARY DICTIONARY", 40, 55);
        g2.setFont(gameFont.deriveFont(Font.PLAIN, 14f)); g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("COMPLETION: " + unlockedAntsRegistry.size() + " / " + ALL_SPECIES.length + " DISCOVERED", 40, 85);

        int cols = 4, cardW = 165, cardH = 180, startX = 40, startY = 115, gapX = 25, gapY = 25;

        for (int i = 0; i < ALL_SPECIES.length; i++) {
            String antName = ALL_SPECIES[i];
            boolean isUnlocked = unlockedAntsRegistry.contains(antName);
            int cx = startX + (i % cols) * (cardW + gapX);
            int cy = startY + (i / cols) * (cardH + gapY);

            g2.setColor(isUnlocked ? new Color(30, 35, 45, 240) : new Color(20, 20, 20, 200));
            g2.fillRect(cx, cy, cardW, cardH);
            g2.setColor(isUnlocked ? new Color(70, 130, 180, 100) : new Color(40, 40, 40, 100));
            g2.drawRect(cx, cy, cardW, cardH);

            Image sprite = speciesImages.get(antName);
            if (sprite != null) {
                int imgSize = 48, ix = cx + (cardW - imgSize) / 2, iy = cy + 20;
                if (isUnlocked) {
                    g2.drawImage(sprite, ix, iy, imgSize, imgSize, null);
                } else {
                    g2.setColor(new Color(0, 0, 0, 220)); g2.fillRect(ix, iy, imgSize, imgSize);
                    g2.setColor(Color.DARK_GRAY); g2.setFont(gameFont.deriveFont(Font.BOLD, 28f));
                    g2.drawString("?", ix + 16, iy + 34);
                }
            }

            g2.setFont(gameFont.deriveFont(Font.BOLD, 12f));
            if (isUnlocked) {
                g2.setColor(new Color(223, 177, 98)); g2.drawString(antName, cx + 12, cy + 90);
                g2.setFont(gameFont.deriveFont(Font.PLAIN, 10f)); g2.setColor(Color.LIGHT_GRAY);
                switch (antName) {
                    case "Larva":
                        g2.drawString("Speed: 3.5  Size: 16", cx + 12, cy + 115);
                        g2.drawString("The origin baseline.", cx + 12, cy + 135);
                        break;
                    case "Worker Ant":
                        g2.drawString("Speed: 4.2  Size: 24", cx + 12, cy + 115);
                        g2.drawString("Stable balanced harvester.", cx + 12, cy + 135);
                        break;
                    case "Guerilla Scout":
                        g2.drawString("Speed: 5.8  Size: 18", cx + 12, cy + 115);
                        g2.drawString("High agility target.", cx + 12, cy + 135);
                        break;
                    case "Soldier Ant":
                        g2.drawString("Speed: 4.6  Size: 36", cx + 12, cy + 115);
                        g2.drawString("Rugged predator slayer.", cx + 12, cy + 135);
                        break;
                    case "Leafcutter Raider":
                        g2.drawString("Speed: 5.0  Size: 32", cx + 12, cy + 115);
                        g2.drawString("Massive food EXP scaling.", cx + 12, cy + 135);
                        break;
                    case "Bullet Ant Knight":
                        g2.drawString("Speed: 3.2  Size: 42", cx + 12, cy + 115);
                        g2.drawString("Immune to knockback effect.", cx + 12, cy + 135);
                        break;
                    case "Imperial Queen":
                        g2.drawString("Speed: 5.4  Size: 56", cx + 12, cy + 115);
                        g2.drawString("Apex colonial sovereign.", cx + 12, cy + 135);
                        break;
                    case "Army Commander":
                        g2.drawString("Speed: 5.6  Size: 52", cx + 12, cy + 115);
                        g2.drawString("Guards block enemy paths.", cx + 12, cy + 135);
                        break;
                }
            } else {
                g2.setColor(Color.DARK_GRAY); g2.drawString("LOCKED SPECIES", cx + 12, cy + 90);
                g2.setFont(gameFont.deriveFont(Font.ITALIC, 10f));
                g2.drawString("Mutate into this form to", cx + 12, cy + 115);
                g2.drawString("reveal secret capabilities.", cx + 12, cy + 130);
            }
        }
        drawMenuButton(g2, showcaseBackBtn, "MAIN MENU", false, new Color(218, 165, 32), "res/img/btn.png");
    }

    private void renderEvolutionChoicePanel(Graphics2D g2) {
        renderGameContent(g2);
        g2.setColor(new Color(0, 0, 0, 180)); g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(Color.ORANGE); g2.setFont(gameFont.deriveFont(Font.BOLD, 32f));
        g2.drawString("MUTATION SIGNAL DETECTED!", WIDTH / 2 - 135, HEIGHT / 2 - 120);
        g2.setFont(gameFont.deriveFont(Font.PLAIN, 15f)); g2.setColor(Color.WHITE);
        g2.drawString("Select your next evolutionary adaptation path:", WIDTH / 2 - 115, HEIGHT / 2 - 90);

        if (evolutionOptions.size() > 0) {
            drawMenuButton(g2, leftChoiceCard, evolutionOptions.get(0), false, new Color(70, 130, 180), "res/img/btn.png");
        }
        if (evolutionOptions.size() > 1) {
            drawMenuButton(g2, rightChoiceCard, evolutionOptions.get(1), false, new Color(154, 205, 50), "res/img/btn.png");
        }
    }

    private void drawMenuButton(Graphics2D g2, Rectangle rect, String label, boolean isSelected, Color baseColor, String imagePath) {
        if (imagePath != null) {
            Image btnImg = buttonImages.get(imagePath);
            if (btnImg == null && new File(imagePath).exists()) {
                btnImg = new ImageIcon(imagePath).getImage();
                buttonImages.put(imagePath, btnImg);
            }
            if (btnImg != null) {
                g2.drawImage(btnImg, rect.x, rect.y, rect.width, rect.height, null);
                g2.setColor(isSelected ? new Color(255, 255, 255, 40) : new Color(0, 0, 0, 80));
                g2.fillRect(rect.x, rect.y, rect.width, rect.height);
            } else {
                g2.setColor(isSelected ? baseColor.brighter() : new Color(25, 25, 25, 230));
                g2.fillRect(rect.x, rect.y, rect.width, rect.height);
            }
        } else {
            g2.setColor(isSelected ? baseColor.brighter() : new Color(25, 25, 25, 230));
            g2.fillRect(rect.x, rect.y, rect.width, rect.height);
        }

        if (isSelected) {
            g2.setStroke(new BasicStroke(1));
            for (int i = 1; i <= 6; i++) {
                g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), Math.max(0, 150 - (i * 25))));
                g2.drawRect(rect.x - i, rect.y - i, rect.width + (i * 2), rect.height + (i * 2));
            }
        }

        g2.setColor(Color.WHITE); g2.setFont(gameFont.deriveFont(Font.BOLD, 14f));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, rect.x + (rect.width - fm.stringWidth(label)) / 2, rect.y + (rect.height - fm.getHeight()) / 2 + fm.getAscent());
    }

    private void renderGameContent(Graphics2D g2) {
        g2.drawImage(bgImg, 0, 0, WIDTH, HEIGHT, null);
        for (Food food : foodList) food.draw(g2, foodImg);
        for (Predator pred : predators) pred.draw(g2, pred.type.equals("Spider") ? spiderImg : frogImg);
        player.draw(g2);
        for (EvolveParticle p : evolveParticles) p.draw(g2);
        renderHUD(g2);
    }

    private void renderGameOverScreen(Graphics2D g2) {
        g2.drawImage(bgImg, 0, 0, WIDTH, HEIGHT, null);
        g2.setPaint(new GradientPaint(0, 0, new Color(15, 10, 10, 220), 0, HEIGHT, new Color(55, 5, 5, 240)));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(Color.RED); g2.setFont(gameFont.deriveFont(Font.BOLD, 60f));
        g2.drawString("DIED IN THE WILD", WIDTH / 2 - 130, HEIGHT / 2 - 180);
        g2.setColor(Color.WHITE); g2.setFont(gameFont.deriveFont(Font.BOLD, 18f));
        g2.drawString("FINAL EVOLUTION: " + player.evolutionStage, WIDTH / 2 - 100, HEIGHT / 2 - 135);
        g2.drawString("YOUR SCORE: " + score, WIDTH / 2 - 40, HEIGHT / 2 - 105);
        g2.setColor(new Color(223, 177, 98)); g2.setFont(gameFont.deriveFont(Font.BOLD, 20f));
        g2.drawString("--- TOP 5 RECORDS ---", WIDTH / 2 - 70, HEIGHT / 2 - 50);

        int drawY = HEIGHT / 2 - 20;
        for (int i = 0; i < 5; i++) {
            String rankLine = (i + 1) + ". ";
            if (i < highScores.size()) {
                rankLine += highScores.get(i) + " PTS";
                g2.setColor(highScores.get(i) == score ? Color.GREEN : Color.LIGHT_GRAY);
            } else {
                rankLine += "---"; g2.setColor(Color.DARK_GRAY);
            }
            g2.drawString(rankLine, WIDTH / 2 - 40, drawY);
            drawY += 26;
        }

        drawMenuButton(g2, restartBtn, "PLAY AGAIN", false, new Color(46, 139, 87), "res/img/btn_restart.png");
        drawMenuButton(g2, menuBtn, "MAIN MENU", false, new Color(70, 130, 180), "res/img/btn_menu.png");
    }

    private void renderHUD(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 140)); g2.fillRect(15, 15, 250, 140);
        g2.setColor(Color.WHITE); g2.setFont(gameFont.deriveFont(Font.BOLD, 14f));
        g2.drawString("STAGE: " + player.evolutionStage, 25, 35);
        g2.drawString("SCORE: " + score, 25, 85);
        g2.setColor(Color.ORANGE); g2.drawString("ENEMIES: " + predators.size(), 25, 110);
        g2.setColor(Color.WHITE); g2.drawString("HEALTH:", 25, 62);

        for (int i = 0; i < maxLives; i++) {
            g2.drawImage(i < lives ? heartFullImg : heartEmptyImg, 95 + (i * 22), 49, 16, 16, null);
        }

        g2.setColor(Color.DARK_GRAY); g2.fillRect(25, 130, 220, 10);
        g2.setColor(Color.GREEN);
        g2.fillRect(25, 130, (int) (220 * Math.min((double) player.exp / player.expNeeded, 1.0)), 10);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Ant Colony: Evolution");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Image appLogo = new ImageIcon("res/img/logo.png").getImage();
            frame.setIconImage(appLogo);

            frame.add(new AntColony());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}