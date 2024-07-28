package com.example.stickherogame;

import javafx.animation.AnimationTimer;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class StickHeroGame extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    private static final int PLATFORM_HEIGHT = 50;
    private static final double HERO_DISTANCE_FROM_EDGE = 5;
    private static final double PLATFORM_SCROLL_SPEED = 3;
    private static final double HERO_SCROLL_SPEED = 8;
    private static final int NUM_INITIAL_PLATFORMS = 5;

    private double cameraX = 0;
    private Canvas canvas;
    private GraphicsContext gc;
    private Button restartButton;
    private Button closeButton;
    private Button Score;
    private List<Platform> platforms;
    int total_platforms = 0;
    private List<Stick> sticks;
    private Character character;
    private int score;
    private GamePhase phase;
    private boolean stretching;
    private long lastTimestamp;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Stick Hero Game");

        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        restartButton = new Button("RESTART");
        closeButton = new Button("QUIT");

        Image backgroundImage = new Image("mountains-background-game-vector.jpg");
        ImageView imageView = new ImageView(backgroundImage);


        StackPane root = new StackPane();
        root.getChildren().add(imageView);
        root.getChildren().add(canvas);

        root.getChildren().add(closeButton);
        root.getChildren().add(restartButton);

        Scene scene = new Scene(root);

        scene.setOnMouseClicked(event -> handleMouseClick());
        scene.setOnMouseReleased(event -> handleMouseRelease());
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                handleSpacePress();
            }
        });
        scene.setOnKeyPressed(event ->{
            if (event.getCode() == KeyCode.DOWN){
                handleDownPress();
            }
        });

        Random r = new Random();
//        double firstPlatformWidth = (Math.random() * 100) + 50;
//        Platform platform = new Platform(HERO_DISTANCE_FROM_EDGE, firstPlatformWidth);
//        int numCherries = 1; // Adjust the number of cherries per platform
//        for (int j = 0; j < numCherries; j++) {
//            addCherry(root,platform);
//        }

        // Bind the dimensions of the image to the dimensions of the stage
        imageView.fitWidthProperty().bind(primaryStage.widthProperty());
        imageView.fitHeightProperty().bind(primaryStage.heightProperty());

        restartButton.setTranslateX(290);
        restartButton.setTranslateY(-170);
        closeButton.setTranslateX(370);
        closeButton.setTranslateY(-170);
        primaryStage.setHeight(440);
        primaryStage.setWidth(900);
        primaryStage.centerOnScreen();

        primaryStage.setScene(scene);
        primaryStage.show();

        restartButton.setOnAction(event -> resetGame());
        closeButton.setOnAction(event -> closeGame() );

        resetGame();
        startGameLoop();
    }

    private void resetGame() {
        platforms = new ArrayList<>();
        sticks = new ArrayList<>();
        score = 0;
        phase = GamePhase.WAITING;
        stretching = false;
        lastTimestamp = 0;
        cameraX = 0; // Reset camera position

        // Add the first platform
        double firstPlatformWidth = (Math.random() * 100) + 50;
        Platform firstPlatform = new Platform(HERO_DISTANCE_FROM_EDGE, firstPlatformWidth);
        platforms.add(firstPlatform);

        // Add additional initial platforms
        for (int i = 1; i < NUM_INITIAL_PLATFORMS; i++) {
            generatePlatform();
        }

        sticks.add(new Stick(platforms.get(0).getX() + platforms.get(0).getWidth(), 0));
        character = new Character("char_frame1.png", platforms.get(0).getX() + platforms.get(0).getWidth() - HERO_DISTANCE_FROM_EDGE - 70 , HEIGHT - PLATFORM_HEIGHT - HERO_DISTANCE_FROM_EDGE );
        character.changeSize(70,70);
        draw();
    }

    public void handleDownPress() {
        RotateTransition rotate = new RotateTransition();
        rotate.setNode(character);
        rotate.setByAngle(180);
        rotate.setAxis(Rotate.X_AXIS);
        rotate.play();
    }

    public void closeGame(){
//        total_platforms = platforms.size();
//        System.out.println(platforms.get(0).total_platforms);
        javafx.application.Platform.exit();
    }
    private void handleMouseClick() {
        if (phase == GamePhase.WAITING) {
            stretching = true;
            lastTimestamp = System.currentTimeMillis();
            phase = GamePhase.STRETCHING;
        }
    }

    private void handleSpacePress() {
        if (phase == GamePhase.WAITING) {
            stretching = true;
            lastTimestamp = System.currentTimeMillis();
            phase = GamePhase.STRETCHING;
        }
    }

    private void handleMouseRelease() {
        if (phase == GamePhase.STRETCHING) {
            stretching = false;
            phase = GamePhase.TURNING;
            lastTimestamp = System.currentTimeMillis();
        }
    }

    private void startGameLoop() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                draw();
            }
        }.start();
    }
//    private void addCherry(StackPane root, Platform platform) {
//        Random random = new Random();
//        Image cherryImage = new Image("Cherries111.png");
//        ImageView cherryImageView = new ImageView(cherryImage);
//        cherryImageView.setFitWidth(140);  // Adjust the width of the cherry image
//        cherryImageView.setFitHeight(100); // Adjust the height of the cherry image
//
//        // Randomly position cherries on the side of the platform
//
//        double cherryX = platform.getX() - 60;
//        double cherryY = platform.getY() - 60;
//
//        cherryImageView.setX(cherryX);
//        cherryImageView.setY(cherryY);
//
////        root.getChildren().add(cherryImageView);
//        Character cherry = new Character("Cherries111.png", cherryX, cherryY );
//        cherry.changeSize(70,70);
//
//
//    }
    private void resetCharacterPosition() {
        character.setX(platforms.get(platforms.size() - 1).getX() + platforms.get(platforms.size() - 1).getWidth() - HERO_DISTANCE_FROM_EDGE);
        character.setY(HEIGHT - PLATFORM_HEIGHT - HERO_DISTANCE_FROM_EDGE);
    }

    private void update() {
//        System.out.println("Current Phase: " + phase); // Debugging

        if (stretching) {
            updateStretching();
        } else if (phase == GamePhase.TURNING) {
            updateTurning();
        } else if (phase == GamePhase.WALKING) {
            updateCharacterWalking();
        } else if (phase == GamePhase.TRANSITIONING) {
            updateTransitioning();
        } else if (phase == GamePhase.FALLING) {
            updateFalling();
        }

        // Generate platforms continuously
        generateContinuousPlatforms();

        // Check for game over condition after generating platforms
        if (checkCollisions()) {
            handleGameOver();
        }
    }

    private void updateStretching() {
        sticks.get(sticks.size() - 1).setLength(sticks.get(sticks.size() - 1).getLength() + (System.currentTimeMillis() - lastTimestamp) / 4.0);
        lastTimestamp = System.currentTimeMillis();
    }

    private void updateTurning() {
        Stick currentStick = sticks.get(sticks.size() - 1);
        currentStick.setRotation(currentStick.getRotation() + (System.currentTimeMillis() - lastTimestamp) / 4.0);

        if (currentStick.getRotation() >= 90) {
            currentStick.setRotation(90);

            Platform nextPlatform = getNextPlatform();
            if (nextPlatform != null) {
                // Increase score
                score += perfectHit() ? 2 : 1;

                generatePlatform();

                phase = GamePhase.WALKING;
            } else {
                phase = GamePhase.FALLING;
            }
        }

        lastTimestamp = System.currentTimeMillis();
    }

    private void updateCharacterWalking() {

        // Check if the character is off the screen, generate a new stick and platform
        // Check if the character is off the last stick, generate a new stick and platform
        Stick currentStick = sticks.get(sticks.size() - 1);
        double stickEndX = currentStick.getX() + currentStick.getLength();

        if (character.getX() > stickEndX) {
            generatePlatform();
            resetCharacterPosition();
        }


        // Detach the stick from the platform when the character reaches the end
        if (character.getX() > stickEndX) {
            character.setX(stickEndX);
            phase = GamePhase.TRANSITIONING;
            stretching = false;
            lastTimestamp = System.currentTimeMillis();
            return;
        }

        // Scroll the character based on the HERO_SCROLL_SPEED
        double characterScrollDistance = HERO_SCROLL_SPEED;
        character.setX(character.getX() + characterScrollDistance);

        // Update the camera position to follow the character
        cameraX += characterScrollDistance;

        // Scroll the platforms along with the character
        scrollPlatforms(characterScrollDistance);

        // Move the stick along with the character
        currentStick.setX(currentStick.getX() - characterScrollDistance);

        // Check if it's time to generate a new platform
        generatePlatform();

        lastTimestamp = System.currentTimeMillis();

    }

    private void scrollPlatforms(double distance) {
        platforms.forEach(platform -> platform.setX(platform.getX() - distance));

        // Add new platforms to maintain a certain number
        while (platforms.size() < NUM_INITIAL_PLATFORMS) {
            generatePlatform();
        }
    }


    private void updateTransitioning() {
        // Check if the character has reached the end of the stick
        if (character.getX() >= sticks.get(sticks.size() - 1).getX() + sticks.get(sticks.size() - 1).getLength()) {
            // Generate a new stick and transition to the waiting phase
            Platform nextPlatform = getNextPlatform();
            if (nextPlatform != null) {
                Stick newStick = new Stick(nextPlatform.getX() + nextPlatform.getWidth(), 0);
                sticks.add(newStick);
                phase = GamePhase.WAITING;
            } else {
                phase = GamePhase.FALLING; // If there is no next platform, transition to falling phase
            }
        }

        lastTimestamp = System.currentTimeMillis();
    }

    private static final double TIME_STEP = 0.016; // Adjust the time step as needed

    private void updateFalling() {
        Stick currentStick = sticks.get(sticks.size() - 1);
        if (currentStick.getRotation() < 180) {
            currentStick.setRotation(currentStick.getRotation() + TIME_STEP * 60.0); // Rotate at a fixed rate
        }

        character.setY(character.getY() + TIME_STEP * 100.0); // Adjust the vertical position at a fixed rate
        Platform currentPlatform = platforms.get(platforms.size() - 1);
        double maxHeroY = HEIGHT - PLATFORM_HEIGHT - HERO_DISTANCE_FROM_EDGE - character.getY();

        if (maxHeroY < 0) {
            handleGameOver();
            return;
        }

        lastTimestamp = System.currentTimeMillis();
    }


    private void draw() {
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        // Draw platforms, sticks, trees, and character with the updated cameraX
        for (Platform platform : platforms) {
            platform.draw(gc, cameraX);
        }

        for (Stick stick : sticks) {
            stick.draw(gc, cameraX);
        }

        character.draw(gc, cameraX);

        // Draw score
        gc.setFill(Color.BLACK);
//        gc.setFont(Font.font(15));
        gc.fillText("Score: " + score, WIDTH - 150, 90);


        // Check for collisions and handle game over
        if (checkCollisions()) {
            handleGameOver();
        }
    }

    private boolean checkCollisions() {
        Stick currentStick = sticks.get(sticks.size() - 1);
        Platform currentPlatform = platforms.get(platforms.size() - 1);
        double stickEndX = currentStick.getX() + currentStick.getLength();
        double platformStartX = currentPlatform.getX();
        double platformEndX = currentPlatform.getX() + currentPlatform.getWidth();

        if (stickEndX > platformStartX && character.getX() < platformEndX) {
            double perfectAreaStartX = currentPlatform.getX() + currentPlatform.getWidth() / 2 - 5;
            double perfectAreaEndX = currentPlatform.getX() + currentPlatform.getWidth() / 2 + 5;

            if (stickEndX > perfectAreaStartX && stickEndX < perfectAreaEndX) {
                return false; // Perfect hit, continue playing
            } else {
                handleGameOver(); // Missed the perfect hit, game over
                return true;
            }
        }

        return false;
    }



    private void handleGameOver() {
//        gc.setFill(Color.BLACK);
//        gc.fillText("Your score is " + score, 500,150);
        gc.setFont(Font.font(35));
//        System.out.println("Game Over! Score: " + score);
        restartButton.setVisible(true);
    }

    private Platform getNextPlatform() {
        Stick currentStick = sticks.get(sticks.size() - 1);
        double stickFarX = currentStick.getX() + currentStick.getLength();
        return platforms.stream()
                .filter(platform -> platform.getX() < stickFarX && stickFarX < platform.getX() + platform.getWidth())
                .findFirst()
                .orElse(null);
    }

    private boolean perfectHit() {
        Stick currentStick = sticks.get(sticks.size() - 1);
        double stickEndX = currentStick.getX() + currentStick.getLength();
        Platform currentPlatform = platforms.get(platforms.size() - 1);
        double perfectAreaStartX = currentPlatform.getX() + currentPlatform.getWidth() / 2 - 5;
        double perfectAreaEndX = currentPlatform.getX() + currentPlatform.getWidth() / 2 + 5;
        return stickEndX > perfectAreaStartX && stickEndX < perfectAreaEndX;
    }

    private void generatePlatform() {
        double lastPlatformX = platforms.get(platforms.size() - 1).getX() + platforms.get(platforms.size() - 1).getWidth() + 200;
        double lastPlatformWidth = (Math.random() * 100) + 50;
        Platform newPlatform = new Platform(lastPlatformX, lastPlatformWidth);
        platforms.add(newPlatform);
    }

    private void generateContinuousPlatforms() {
        double lastPlatformX = platforms.get(platforms.size() - 1).getX() + platforms.get(platforms.size() - 1).getWidth() + 200;
        double lastPlatformWidth = (Math.random() * 100) + 50;
        Platform newPlatform = new Platform(lastPlatformX, lastPlatformWidth);
        platforms.add(newPlatform);

        // Remove platforms that are off-screen to keep the list manageable
        platforms.removeIf(platform -> platform.getX() + platform.getWidth() < cameraX);
    }

    private enum GamePhase {
        WAITING, STRETCHING, TURNING, WALKING, TRANSITIONING, FALLING
    }

    public static class Platform {
        private double x;
        private double y = 150;
        private final double width;
        int total_platforms = 5;

        public Platform(double x, double width) {
            this.x = x;
            this.width = width;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
        public int getTotal_platforms(){
            return total_platforms;
        }

        public double getWidth() {
            return width;
        }

        public void setX(double x) {
            this.x = x;
        }

        public void draw(GraphicsContext gc, double cameraX) {
            gc.setFill(Color.BLACK);
            gc.fillRect(x - cameraX, HEIGHT - PLATFORM_HEIGHT, width, PLATFORM_HEIGHT);
//            total_platforms+= 1;
        }
    }

    public static class Stick {
        private double length;
        private double x;
        private double y;
        private double rotation;

        public Stick(double x, double length) {
            this.x = x;
            this.length = length;
            this.y = HEIGHT - PLATFORM_HEIGHT;
            this.rotation = 0;
        }

        public double getLength() {
            return length;
        }

        public void setLength(double length) {
            this.length = length;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getRotation() {
            return rotation;
        }

        public void setRotation(double rotation) {
            this.rotation = rotation;
        }

        public void draw(GraphicsContext gc, double cameraX) {
            gc.save();
            gc.translate(x - cameraX, y);
            gc.rotate(rotation);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.strokeLine(0, 0, 0, -length);
            gc.restore();

        }
    }

    public static class Character extends Node {
        private Image image;
        private double x;
        private double y;
        private double width;
        private double height;

        public Character(String imagePath, double x, double y) {
            this.image = new Image(imagePath);
            this.x = x;
            this.y = y;
            this.width = image.getWidth();
            this.height = image.getHeight();
        }
        public void get_size(){
            System.out.println(this.image.getHeight());
            System.out.println(this.image.getWidth());

        }
        public void changeSize(double newWidth, double newHeight) {
            this.width = newWidth;
            this.height = newHeight;
            // Update the image with the new dimensions
            this.image = new Image(image.getUrl(), newWidth, newHeight, false, false);

            // Optionally, update the x and y coordinates based on the new size logic
            // For example, you might want to center the character after resizing
//             this.x = calculateNewX();
//             this.y = calculateNewY();
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public void draw(GraphicsContext gc, double cameraX) {
            gc.drawImage(image, x - cameraX, y - image.getHeight());
//            gc.drawImage(image, x - cameraX, y);
        }

        @Override
        public Node getStyleableNode() {
            return super.getStyleableNode();
        }
    }
}
