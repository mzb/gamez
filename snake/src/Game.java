import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;

public class Game extends Application {
    static final int SCREEN_WIDTH = 320, SCREEEN_HEIGHT = 240;
    static final int GRID = 10;

    long lastFrameTime = System.nanoTime();
    double delay;
    GraphicsContext gc;

    Deque<Point2D> snake = new LinkedList<>();
    enum SnakeDir {UP, DOWN, LEFT, RIGHT };
    SnakeDir snakeDir;
    Point2D food;
    long score;

    boolean gameOver = false;
    final AnimationTimer loop = new AnimationTimer() {
        @Override
        public void handle(long now) {
            processFrame(now);
        }
    };

    @Override
    public void init() throws Exception {

    }

    @Override
    public void start(Stage stage) {
        final Group root = new Group();
        final Scene scene = new Scene(root, SCREEN_WIDTH, SCREEEN_HEIGHT, Color.BLACK);
        final Canvas canvas = new Canvas(scene.getWidth(), scene.getHeight());
        root.getChildren().add(canvas);
        stage.setTitle("SNAKE");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        gc = canvas.getGraphicsContext2D();
        scene.setOnKeyPressed(this::handleInput);
        scene.setOnKeyReleased(this::handleInput);

        restart();
    }

    void processFrame(long now) {
        final double dt = (now - lastFrameTime) / 1_000_000_000d;
        if (delay >= 0) {
            delay -= dt;
            return;
        }
        System.out.printf("FPS=%d\n", (int) (1 / dt));
        lastFrameTime = now;

        delay = 0.5;

        update(dt);
        render();
    }

    void update(double dt) {
        if (gameOver) {
            System.out.println("Game over!");
            loop.stop();
            return;
        }

        Point2D head = snake.getFirst();
        switch(snakeDir) {
            case RIGHT:
                head = head.add(GRID, 0);
                break;
            case LEFT:
                head = head.subtract(GRID, 0);
                break;
            case UP:
                head = head.subtract(0, GRID);
                break;
            case DOWN:
                head = head.add(0, GRID);
                break;
        }

        snake.push(head);
        snake.pollLast();

        handleCollisions(dt);
    }

    void handleCollisions(double dt) {
        Point2D head = snake.getFirst();

        if (head.getX() < 0 || head.getX() + GRID > SCREEN_WIDTH
                || head.getY() < 0 || head.getY() + GRID > SCREEEN_HEIGHT) {
            gameOver = true;
            return;
        }

        for (Point2D s : snake) {
            if (head.equals(s) && head != s) {
                gameOver = true;
                return;
            }
        }

        if (head.equals(food)) {
            score++;
            snake.addLast(snake.getLast());
            // TODO: Update points
            placeFood();
        }
    }

    void render() {
        gc.clearRect(0, 0, SCREEN_WIDTH, SCREEEN_HEIGHT);

        gc.setFill(Color.YELLOW);
        String status = String.format("SCORE: %d%s", score, gameOver ? " - GAME OVER" : "");
        gc.fillText(status, 8, SCREEEN_HEIGHT - 8);

        gc.setFill(Color.RED);
        gc.fillRect(food.getX(), food.getY(), GRID, GRID);

        for (Point2D s : snake) {
            gc.setFill(s == snake.getFirst() ? Color.GREEN : Color.DARKGREEN);
            gc.fillRect(s.getX(), s.getY(), GRID, GRID);
        }
    }

    void handleInput(KeyEvent e) {
        switch(e.getCode()) {
            case RIGHT:
                snakeDir = SnakeDir.RIGHT;
                break;
            case LEFT:
                snakeDir = SnakeDir.LEFT;
                break;
            case UP:
                snakeDir = SnakeDir.UP;
                break;
            case DOWN:
                snakeDir = SnakeDir.DOWN;
                break;
            case SPACE:
                if (gameOver) {
                    gameOver = false;
                    restart();
                }
        }
    }

    void restart() {
        score = 0;

        snakeDir = SnakeDir.RIGHT;
        snake.clear();
        // TODO: Randomize snake pos
        for (int i=3; i>0; i--) {
            snake.add(new Point2D(i*GRID, GRID));
        }

        placeFood();

        loop.start();
    }

    void placeFood() {
        Random rand = new Random();
        do {
            food = new Point2D(GRID * rand.nextInt(SCREEN_WIDTH/GRID), GRID * rand.nextInt(SCREEEN_HEIGHT/GRID));
        } while (snake.contains(food));
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}

