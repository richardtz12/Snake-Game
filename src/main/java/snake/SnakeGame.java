package snake;

import engine.Difficulty;
import engine.GameWorld;

import java.util.Scanner;

import javafx.animation.AnimationTimer;

import javafx.application.Application;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.geometry.Pos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.collections.ObservableList;

import javafx.stage.Stage;

import model.Direction;

/**
 * The main entry point for the snake program. This class handles all graphics
 * not related to the actual game (i.e the start screen and score screen), user
 * input for each screen, etc. Also handles the updating of world on a timed
 * interval.
 *
 * @author  Susanna Dong, Jim Harris
 * @version 1.0
 */
public class SnakeGame extends Application {

    private Stage window;
    private Scene startScene;
    private Scene gameScene;
    private Scene scoreScene;
    private ToggleGroup gameMode;
    private int finalScore;
    private GameWorld world;
    private long lastUpdateTime;

    public static final int SCREEN_WIDTH = 512;
    public static final int TILE_WIDTH = 32;

    @Override
    public void start(Stage stage) {
        finalScore = 0;
        window = stage;
        gameMode = new ToggleGroup();
        setupStartScene();
        window.setScene(startScene);
        window.setResizable(false);
        window.show();

    }

    /**
     * Sets startScene and adds elements to it. startScene is composed of:
     *     1) A title label
     *     2) A group of radio buttons for setting the game mode
     *     3) A button that when pressed will call setupGameScene and call play
     */
    private void setupStartScene() {
        Label label = new Label("Snake Game");
        label.setFont(new Font("Serif", 26));

        RadioButton easy = new RadioButton("easy");
        easy.setUserData(Difficulty.EASY);
        easy.setToggleGroup(gameMode);
        RadioButton normal = new RadioButton("Normal");
        normal.setUserData(Difficulty.NORMAL);
        normal.setToggleGroup(gameMode);
        RadioButton hard = new RadioButton("Hard");
        hard.setUserData(Difficulty.HARD);
        hard.setToggleGroup(gameMode);
        normal.setSelected(true);

        HBox hbox = new HBox();
        hbox.getChildren().addAll(easy, normal, hard);
        hbox.setAlignment(Pos.CENTER);

        Button start = new Button("Start");
        start.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent setup) {
                setupGameScene();
                window.setScene(gameScene);
                play();
            }
        });

        VBox vbox = new VBox();
        vbox.getChildren().addAll(label, hbox, start);
        vbox.setAlignment(Pos.CENTER);

        StackPane stackpane = new StackPane();
        stackpane.getChildren().addAll(vbox);

        startScene = new Scene(stackpane, SnakeGame.SCREEN_WIDTH,
        SnakeGame.SCREEN_WIDTH);
    }


    /**
     * Sets the gameScene and adds elements to it. gameScene is composed of:
     *     1) A Rectangle for the background
     *     2) All of the elements from world
     * world handles the addition of all the game graphics to the screen with
     * the exception of the background, which you must add to gameScene
     * manually. You will need to set world in this method as well. Also, this
     * method must add an event to gameScene such that when WASD or the arrow
     * keys are pressed the snake will change direction.
     */
    private void setupGameScene() {
        Group parent = new Group();
        gameScene = new Scene(parent, SnakeGame.SCREEN_WIDTH,
        SnakeGame.SCREEN_WIDTH);
        Rectangle newRectangle = new Rectangle(SnakeGame.SCREEN_WIDTH,
            SnakeGame.SCREEN_WIDTH, Color.BLUE);
        newRectangle.setX(0);
        newRectangle.setX(0);
        parent.getChildren().add(newRectangle);
        gameScene.setOnKeyPressed(event -> {
                if (event.getCode().equals(KeyCode.W)) {
                    world.setDirection(Direction.UP);
                }
                if (event.getCode().equals(KeyCode.S)) {
                    world.setDirection(Direction.DOWN);
                }
                if (event.getCode().equals(KeyCode.A)) {
                    world.setDirection(Direction.LEFT);
                }
                if (event.getCode().equals(KeyCode.D)) {
                    world.setDirection(Direction.RIGHT);
                }
                if (event.getCode().equals(KeyCode.UP)) {
                    world.setDirection(Direction.UP);
                }
                if (event.getCode().equals(KeyCode.DOWN)) {
                    world.setDirection(Direction.DOWN);
                }
                if (event.getCode().equals(KeyCode.LEFT)) {
                    world.setDirection(Direction.LEFT);
                }
                if (event.getCode().equals(KeyCode.RIGHT)) {
                    world.setDirection(Direction.RIGHT);
                }
            });
        world = new GameWorld(newRectangle, gameScene,
            (Difficulty) gameMode.getSelectedToggle().getUserData());
    }

    /**
     * Sets the scoreScene and adds elements to it. scoreScene is composed of:
     *     1) A label that shows the user's score from world.
     *     2) A highscore list of the top 10 scores that is composed of:
     *         a) A ListView of Nodes for player usernames.
     *             - If the player makes it into the top 10, they need to be
     *             able to set their username, so a TextField should be at the
     *             point in the list where they belong. All other fields can
     *             just be labels for existing users.
     *         b) A ListView of Integers for player scores.
     *             - If the player makes it into the top 10, they're score
     *             should be displayed at the proper place in the list.
     *         * Existing high scores can be found in highScores.csv in the
     *         resources folder.
     *     3) A button that when pressed will write the high scores in the list
     *     to highScores.csv in the resources folder in the same format in which
     *     you originally accessed them. The button should also change the scene
     *     for window to startScene.
     */
    private void setupScoreScene() {
        Label label = new Label(Integer.toString(world.getScore()));
        label.setFont(new Font("Serif", 26));
        ListView<Node> list = new ListView<Node>();
        ListView<Integer> list2 = new ListView<Integer>();
        HBox hbox = new HBox();
        hbox.getChildren().addAll(list, list2);
        File file = new File("src/main/resources/highScores.csv");
        ObservableList<Node> winners = list.getItems();
        ObservableList<Integer> winnerScores = list2.getItems();
        try {
            Scanner scanner = new Scanner(file);
            int counter = 0;
            boolean highScoreFound = false;
            while (scanner.hasNext() && counter <= 10) {
                String line = scanner.nextLine();
                String part1 = line.split(",")[0];
                Label name = new Label(part1);
                String part2 = line.split(",")[1];
                int score = Integer.parseInt(part2);
                if (world.getScore() < score) {
                    winners.add(name);
                    winnerScores.add(score);
                    counter = counter + 1;
                } else {
                    if (!(highScoreFound)) {
                        TextField newUser = new TextField();
                        winners.add(newUser);
                        winnerScores.add(world.getScore());
                        counter = counter + 1;
                        highScoreFound = true;
                    }

                    if (highScoreFound && counter < 10) {
                        winners.add(name);
                        winnerScores.add(score);
                        counter = counter + 1;
                    }
                }
            }
            if (!(scanner.hasNext()) && counter < 10) {
                TextField newUser = new TextField();
                winners.add(newUser);
                winnerScores.add(world.getScore());
                counter = counter + 1;
                highScoreFound = true;
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        list.setItems(winners);
        list2.setItems(winnerScores);

        Button save = new Button("save");
        save.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent saves) {
                try {
                    PrintWriter writer = new PrintWriter(file);
                    for (int i = 0; i < winners.size(); i = i + 1) {
                        if (list.getItems().get(i) instanceof TextField) {
                            TextField a = (TextField) list.getItems().get(i);
                            writer.println(a.getText() + ","
                                + list2.getItems().get(i));
                        }
                        if (list.getItems().get(i) instanceof Label) {
                            Label b = (Label) list.getItems().get(i);
                            writer.println(b.getText() + ","
                                + list2.getItems().get(i));
                        }
                    }
                    writer.close();
                    window.setScene(startScene);
                } catch (FileNotFoundException e) {
                    System.out.println(e.getMessage());
                }
            }
        });
        VBox scoreboard = new VBox();
        scoreboard.getChildren().addAll(label, hbox, save);
        StackPane stackpane2 = new StackPane();
        stackpane2.getChildren().addAll(scoreboard);
        scoreScene = new Scene(stackpane2, SnakeGame.SCREEN_WIDTH,
            SnakeGame.SCREEN_WIDTH);

    }

    /**
     * Starts the game loop. Assumes that the scene for window has been set to
     * gameScene and that world has been properly reset to the starting game
     * state.
     */
    public void play() {
        AnimationTimer timey = new AnimationTimer() {
            @Override
            public void handle(long currentTime) {
                if (System.currentTimeMillis()
                    - lastUpdateTime > world.getDelayTime()) {
                    world.update();
                    // DO NOT MODIFY ABOVE THIS LINE
                    if (world.isGameOver()) {
                        stop();
                        setupScoreScene();
                        window.setScene(scoreScene);
                    }

                    // DO NOT MODIFY BELOW THIS LINE
                    lastUpdateTime = System.currentTimeMillis();
                }
            }
        };
        lastUpdateTime = System.currentTimeMillis();
        timey.start();
    }
}
