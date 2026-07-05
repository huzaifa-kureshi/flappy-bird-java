import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    // ================= GAME SIZE =================
    int WIDTH;
    int HEIGHT;

    // ================= IMAGES =================
    Image bgImg, birdImg, topPipeImg, bottomPipeImg;

    // ================= BIRD =================
    int birdX, birdY;
    int birdW = 34, birdH = 24;

    class Bird {
        int x, y;
        Bird() {
            x = birdX;
            y = birdY;
        }
    }

    // ================= PIPE =================
    int pipeW = 64, pipeH = 512;

    class Pipe {
        int x, y;
        boolean passed = false;
        Image img;

        Pipe(int y, Image img) {
            this.x = WIDTH;
            this.y = y;
            this.img = img;
        }
    }

    // ================= GAME VARIABLES =================
    Bird bird;
    ArrayList<Pipe> pipes = new ArrayList<>();
    Random random = new Random();

    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;

    Timer gameLoop;
    Timer pipeTimer;

    boolean gameOver = false;
    boolean paused = false;
    int score = 0;

    // ================= PAUSE BUTTON =================
    JButton pauseButton;

    // ================= CONSTRUCTOR =================
    FlappyBird() {

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = screen.width;
        HEIGHT = screen.height - 80;

        birdX = WIDTH / 8;
        birdY = HEIGHT / 2;

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        // Panel uses absolute positioning so the pause button can float
        // on top of the custom-painted game surface.
        setLayout(null);

        // load images
        bgImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        bird = new Bird();

        // ---- Pause Button setup ----
        pauseButton = new JButton("Pause");
        pauseButton.setFocusable(false); // keep keyboard focus on the game panel
        pauseButton.setFont(new Font("Arial", Font.BOLD, 16));
        pauseButton.setBounds(WIDTH - 110, 20, 90, 36);
        pauseButton.addActionListener(e -> {
            togglePause();
            requestFocusInWindow();
        });
        add(pauseButton);

        pipeTimer = new Timer(1500, e -> placePipes());
        gameLoop = new Timer(1000 / 60, this);

        pipeTimer.start();
        gameLoop.start();
    }

    // ================= PAUSE TOGGLE =================
    void togglePause() {
        if (gameOver) return;
        paused = !paused;
        pauseButton.setText(paused ? "Resume" : "Pause");
        repaint();
    }

    // ================= PIPE SPAWN =================
    void placePipes() {
        if (paused || gameOver) return;

        int gap = HEIGHT / 4;
        int y = -pipeH / 4 - random.nextInt(pipeH / 2);

        pipes.add(new Pipe(y, topPipeImg));
        pipes.add(new Pipe(y + pipeH + gap, bottomPipeImg));
    }

    // ================= DRAW =================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(bgImg, 0, 0, WIDTH, HEIGHT, null);
        g.drawImage(birdImg, bird.x, bird.y, birdW, birdH, null);

        for (Pipe p : pipes) {
            g.drawImage(p.img, p.x, p.y, pipeW, pipeH, null);
        }

        g.setColor(Color.WHITE);

        if (paused) {
            drawPause(g);
        } else if (gameOver) {
            drawGameOver(g);
        } else {
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("Score: " + score, 20, 40);
        }
    }

    void drawPause(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 60));
        g.drawString("PAUSED", WIDTH / 2 - 120, HEIGHT / 2 - 40);

        g.setFont(new Font("Arial", Font.PLAIN, 28));
        g.drawString("Press P or click Resume", WIDTH / 2 - 170, HEIGHT / 2 + 20);
    }

    void drawGameOver(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 60));
        g.drawString("GAME OVER", WIDTH / 2 - 200, HEIGHT / 2 - 100);

        g.setFont(new Font("Arial", Font.PLAIN, 36));
        g.drawString("Score: " + score, WIDTH / 2 - 80, HEIGHT / 2 - 30);

        g.setFont(new Font("Arial", Font.PLAIN, 28));
        g.drawString("Press R to Restart", WIDTH / 2 - 130, HEIGHT / 2 + 40);
        g.drawString("Press ESC to Exit", WIDTH / 2 - 120, HEIGHT / 2 + 80);
    }

    // ================= GAME LOGIC =================
    void move() {
        if (paused || gameOver) return;

        velocityY += gravity;
        bird.y += velocityY;

        for (Pipe p : pipes) {
            p.x += velocityX;

            if (!p.passed && bird.x > p.x + pipeW) {
                p.passed = true;
                score++;
            }

            if (collision(bird, p)) gameOver = true;
        }

        if (bird.y < 0 || bird.y > HEIGHT) gameOver = true;
    }

    boolean collision(Bird b, Pipe p) {
        return b.x < p.x + pipeW &&
               b.x + birdW > p.x &&
               b.y < p.y + pipeH &&
               b.y + birdH > p.y;
    }

    // ================= TIMER =================
    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    // ================= KEY CONTROLS =================
    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_P) {
            togglePause();
            return;
        }

        if (paused) return;

        if (e.getKeyCode() == KeyEvent.VK_UP) bird.y -= 25;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) bird.y += 25;
        if (e.getKeyCode() == KeyEvent.VK_SPACE) velocityY = -10;

        if (e.getKeyCode() == KeyEvent.VK_R && gameOver) restartGame();
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) System.exit(0);
    }

    void restartGame() {
        bird.y = birdY;
        velocityY = 0;
        pipes.clear();
        score = 0;
        gameOver = false;
        paused = false;
        pauseButton.setText("Pause");
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    // ================= MAIN =================
    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setResizable(true);

        FlappyBird game = new FlappyBird();
        frame.add(game);
        frame.setVisible(true);
    }
}
