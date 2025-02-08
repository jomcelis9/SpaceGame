import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class SpaceGame extends JPanel implements KeyListener, ActionListener {

    private static final int WIDTH = 800, HEIGHT = 600;
    private Timer timer;
    private double spaceshipX, spaceshipY;
    private Vector2 velocity;
    private List<Bullet> bullets;
    private List<Enemy> enemies;
    private int score;
    private Difficulty difficulty;
    private Image spaceshipImage, slipperImage,canImage,backgroundImage;
    

    public enum Difficulty {
        EASY, NORMAL, HARD
    }

    public SpaceGame(Difficulty difficulty) {

        backgroundImage = new ImageIcon("images/road.png").getImage();
        spaceshipImage = new ImageIcon("images/pinoy.png").getImage();
        slipperImage = new ImageIcon("images/slipper.png").getImage();
    
        canImage = new ImageIcon("images/can.png").getImage();
        this.difficulty = difficulty;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);
        spaceshipX = WIDTH / 2;
        spaceshipY = HEIGHT / 2;
        velocity = new Vector2(0, 0);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        score = 0;

        timer = new Timer(16, this); // Roughly 60 FPS
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT, null); // Stretch image to fit screen
        } 

        int spaceshipWidth = 25; // Desired width
        int spaceshipHeight = 75; // Desired height
        if (spaceshipImage != null) {
            g2d.drawImage(spaceshipImage, 
                          (int) (spaceshipX - spaceshipWidth / 2), // Centering the image
                          (int) (spaceshipY - spaceshipHeight / 2), 
                          spaceshipWidth, 
                          spaceshipHeight, 
                          null);
        }
        // Draw bullets

    // Define slipper width and height
        int slipperWidth = 13;  // Desired width of slipper
        int slipperHeight = 38; // Desired height of slippers        
        // Draw bullets as slippers
        if (slipperImage != null) {
            for (Bullet bullet : bullets) {
                g2d.drawImage(
                    slipperImage, 
                    (int) (bullet.x - slipperWidth / 2), 
                    (int) (bullet.y - slipperHeight / 2), 
                    slipperWidth, 
                    slipperHeight, 
                    null
                );
            }
        }

        // Draw enemies
        // g2d.setColor(Color.GREEN);
        int canWidth = 30;
        int canHeight = 45;
        if (canImage != null) {
            for (Enemy enemy : enemies) {
                g2d.translate(enemy.x, enemy.y); // Move to enemy's position
                g2d.rotate(enemy.rotation);    // Rotate canvas by the enemy's rotation
                g2d.drawImage(
                    canImage,
                    -canWidth / 2,  // Center the image
                    -canHeight / 2,
                    canWidth,
                    canHeight,
                    null
                );
                g2d.rotate(-enemy.rotation);   // Reset rotation
                g2d.translate(-enemy.x, -enemy.y); // Reset translation
            }
        }    

        // Draw score
        g2d.setColor(Color.WHITE);
        g2d.drawString("Score: " + score, 10, 20);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A) {
            velocity.x = -5;
        } else if (key == KeyEvent.VK_D) {
            velocity.x = 5;
        } else if (key == KeyEvent.VK_W) {
            velocity.y = -5;
        } else if (key == KeyEvent.VK_S) {
            velocity.y = 5;
        } else if (key == KeyEvent.VK_SPACE) {
            // Shoot a bullet
            double bulletSpeed = 10;
            bullets.add(new Bullet(spaceshipX, spaceshipY, 0, -bulletSpeed)); // Bullet moves upwards
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_D) {
            velocity.x = 0;
        }
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_S) {
            velocity.y = 0;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void actionPerformed(ActionEvent e) {
        // Update spaceship position based on velocity
        spaceshipX += velocity.x;
        spaceshipY += velocity.y;

        // Handle bullets
        for (int i = 0; i < bullets.size(); i++) {
            Bullet bullet = bullets.get(i);
            bullet.y += bullet.dy; // Bullets move upwards

            // Remove bullets out of bounds
            if (bullet.y < 0) {
                bullets.remove(i);
            }
        }

        // Handle enemies
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            enemy.moveTowards(spaceshipX, spaceshipY); // Move enemy towards the spaceship

            // Check for bullet collisions with enemies
            for (int j = 0; j < bullets.size(); j++) {
                Bullet bullet = bullets.get(j);
                if (Math.abs(bullet.x - enemy.x) < 15 && Math.abs(bullet.y - enemy.y) < 15) {
                    // Bullet hit the enemy, remove both
                    bullets.remove(j);
                    enemies.remove(i);
                    score += 10; // Increase score
                    break;
                }
            }

            // Check if an enemy collides with the spaceship
            if (Math.abs(enemy.x - spaceshipX) < 15 && Math.abs(enemy.y - spaceshipY) < 15) {
                gameOver();
                return;
            }
        }

        // Spawn enemies based on difficulty
        if (Math.random() < (difficulty == Difficulty.EASY ? 0.01 : (difficulty == Difficulty.NORMAL ? 0.03 : 0.05))) {
            spawnEnemy();
        }

        // Remove enemies out of bounds
        enemies.removeIf(enemy -> enemy.x < 0 || enemy.x > WIDTH || enemy.y < 0 || enemy.y > HEIGHT);

        repaint();
    }

    private void spawnEnemy() {
        // Randomly spawn enemies at a position outside of the spaceship
        double angle = Math.random() * 2 * Math.PI;
        double distance = Math.random() * 100 + 50;
        double ex = spaceshipX + Math.cos(angle) * distance;
        double ey = spaceshipY + Math.sin(angle) * distance;
        enemies.add(new Enemy(ex, ey, difficulty == Difficulty.HARD ? 2 : 1)); // Faster in hard mode
    }

    private void gameOver() {
        JOptionPane.showMessageDialog(this, "Game Over! Your Score: " + score);
        System.exit(0);
    }

    public static void main(String[] args) {
        String[] options = {"Easy", "Normal", "Hard"};
        String choice = (String) JOptionPane.showInputDialog(null, "Choose Difficulty", "Difficulty", JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        Difficulty difficulty = Difficulty.valueOf(choice.toUpperCase());

        JFrame frame = new JFrame("Space Game");
        SpaceGame game = new SpaceGame(difficulty);
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    } 

    // Simple 2D vector class
    public static class Vector2 {
        public double x, y;

        public Vector2(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void set(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void add(Vector2 v) {
            this.x += v.x;
            this.y += v.y;
        }

        public void subtract(Vector2 v) {
            this.x -= v.x;
            this.y -= v.y;
        }

        public void multiply(double scalar) {
            this.x *= scalar;
            this.y *= scalar;
        }

        public double magnitude() {
            return Math.sqrt(x * x + y * y);
        }

        public void normalize() {
            double mag = magnitude();
            if (mag > 0) {
                x /= mag;
                y /= mag;
            }
        }
    }

    public static class Bullet {
        double x, y, dx, dy;

        public Bullet(double x, double y, double dx, double dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }
    }

    public static class Enemy {
        double x, y, dx, dy;
        double rotation;

        public Enemy(double x, double y, int speed) {
            this.x = x;
            this.y = y;
            this.dx = Math.cos(Math.random() * 2 * Math.PI) * speed;
            this.dy = Math.sin(Math.random() * 2 * Math.PI) * speed;
        }

        public void moveTowards(double targetX, double targetY) {
            double angle = Math.atan2(targetY - this.y, targetX - this.x);
            this.dx = Math.cos(angle) * 2; // Move at speed 2 towards the spaceship
            this.dy = Math.sin(angle) * 2;
            this.x += dx;
            this.y += dy;

                // Increment rotation for spinning effect
        this.rotation += 0.1; // Adjust speed of rotation as needed
        if (this.rotation >= 2 * Math.PI) {
            this.rotation -= 2 * Math.PI; // Keep rotation within 0 to 2π
        }


    }
        
    }
}