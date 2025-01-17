package gui.game;

import game.RatGame;
import game.RatGameBuilder;
import game.entity.Item;
import game.entity.subclass.bomb.Bomb;
import game.entity.subclass.deathRat.DeathRat;
import game.entity.subclass.femaleSexChange.FemaleSexChange;
import game.entity.subclass.gas.Gas;
import game.entity.subclass.maleSexChange.MaleSexChange;
import game.entity.subclass.noentry.NoEntry;
import game.entity.subclass.poison.Poison;
import game.entity.subclass.sterilisation.Sterilisation;
import game.event.GameEvent;
import game.event.adapter.AbstractGameAdapter;
import game.event.impl.entity.specific.game.GameEndEvent;
import game.event.impl.entity.specific.game.GamePausedEvent;
import game.event.impl.entity.specific.game.GameStateUpdateEvent;
import game.event.impl.entity.specific.general.EntityDeOccupyTileEvent;
import game.event.impl.entity.specific.general.EntityDeathEvent;
import game.event.impl.entity.specific.general.EntityMovedEvent;
import game.event.impl.entity.specific.general.EntityOccupyTileEvent;
import game.event.impl.entity.specific.general.GenericAudioEvent;
import game.event.impl.entity.specific.general.SpriteChangeEvent;
import game.event.impl.entity.specific.item.GeneratorUpdateEvent;
import game.event.impl.entity.specific.load.EntityLoadEvent;
import game.event.impl.entity.specific.load.GameLoadEvent;
import game.event.impl.entity.specific.load.GeneratorLoadEvent;
import game.event.impl.entity.specific.player.ScoreUpdateEvent;
import game.level.reader.RatGameFile;
import game.level.reader.RatGameSaveFile;
import game.level.reader.exception.RatGameFileException;
import game.player.Player;
import game.tile.Tile;
import game.tile.exception.UnknownSpriteEnumeration;
import gui.levelwiki.LevelWikiController;
import gui.game.dependant.endscreen.EndScreenController;
import gui.game.dependant.entitymap.EntityMap;
import gui.game.dependant.itemview.ItemViewController;
import gui.game.dependant.tilemap.GameMap;
import gui.game.dependant.tilemap.GridPaneFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import launcher.Main;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Main Game Window Controller; This would implement the 'RatGameActionListener'
 * which would be the bridge required to get events from the game to the GUI.
 *
 * @author -Ry
 * Copyright: N/A
 * @version 0.8
 */
public class GameController extends AbstractGameAdapter {

    /**
     * Hardcode the Scene Object Hierarchy Resource to the Controller so that
     * it can be accessed.
     */
    private static final URL SCENE_FXML =
            GameController.class.getResource("GameScene.fxml");

    /**
     * Item selected for the purposes of placing. a4f
     */
    private Class<? extends Item> itemSelected;

    /**
     * Root scene node object.
     */
    private Parent root;

    /**
     * Represents the Male rat count visually. In order to display refer to
     * the {@link #femaleRatColumnConstraint}.
     */
    @FXML
    private ColumnConstraints maleRatColumnConstraint;

    /**
     * Represents the female rat count visually. In order to display you must
     * set the {@link ColumnConstraints#getPercentWidth()} based on what
     * the number of female rats is. Where 0 is no female rats and 100 is all
     * female rats.
     */
    @FXML
    private ColumnConstraints femaleRatColumnConstraint;

    /**
     * The pause button for the scene.
     */
    @FXML
    private Button pauseButton;

    /**
     * The save button for the scene.
     */
    @FXML
    private Button saveButton;

    /**
     * Game stack pane that gives depth to the nodes on the game.
     */
    @FXML
    private StackPane gameStackPane;

    /**
     * Scroll pane that the game is played inside.
     */
    @FXML
    private ScrollPane gameScrollPane;

    /**
     * This pane contains all the nodes for the scene it is the 'root' so
     * to call it.
     */
    @FXML
    private BorderPane mainPane;

    /**
     * Name of the player.
     */
    @FXML
    private Label playerNameLabel;

    /**
     * Message of the day.
     */
    @FXML
    private Label messageOfTheDayLabel;

    /**
     * Game background pane, this is where we would display the tile map.
     */
    @FXML
    private Pane gameBackground;

    /**
     * Game foreground, this is where we would display the entities in the
     * game.
     */
    @FXML
    private Pane gameForeground;

    /**
     * Pane that stands in front of the game foreground. Unused but the idea
     * is that we can have the leaderboard be shown in game on a button press.
     */
    @FXML
    private Pane frontOfAllPane;

    /**
     * Label representing how much time is remaining.
     */
    @FXML
    private Label timeRemainingLabel;

    /**
     * Number of rats in the game.
     */
    @FXML
    private Label numberOfRatsLabel;

    /**
     * The total score the player has amassed.
     */
    @FXML
    private Label scoreLabel;

    /**
     * All items available to the player to use.
     */
    @FXML
    private VBox itemVbox;

    /**
     * The player who is playing the rat game.
     */
    private Player player;

    /**
     * The level the player is playing.
     */
    private RatGameFile level;

    /**
     * The underlying rat game.
     */
    private RatGame game;

    /**
     * Underlying game tile map.
     */
    private GameMap tileMap;

    /**
     * Map of entities and their javafx node representations.
     */
    private EntityMap entityMap;

    /**
     * Optional result for when the player of the game finishes the game. It
     * is not guaranteed that they will finish the game thus optional.
     */
    private GameEndEvent gameResult;

    /**
     * All the game generators and their current usage states.
     */
    private HashMap<Class<?>, ItemViewController> generatorMap;

    /**
     * Method used to initiate the game with the target player. Loads the
     * game and initiates all essential data and then waits. To finally
     * initiate the game call {@link GameController#startGame}.
     *
     * @param player The player whose playing the RatGame.
     * @param level  The level that the player is playing.
     * @return Newly constructed and initiated Rat game scene controller.
     * @throws NullPointerException If any parameter is null.
     * @throws IOException          If one occurs whilst setting up the scene.
     */
    public static GameController loadAndGet(final Player player,
                                            final RatGameFile level)
            throws IOException, NullPointerException {

        final FXMLLoader loader = new FXMLLoader(SCENE_FXML);
        final Parent p = loader.load();

        Objects.requireNonNull(player);
        Objects.requireNonNull(level);

        final GameController c = loader.getController();

        c.root = p;
        c.setGameData(player, level);
        Platform.runLater(c::setStyleSheet);

        return c;
    }

    /**
     * Set the target player and the target level.
     *
     * @param activePlayer The player who is playing the game.
     * @param activelevel  The level that the player is playing.
     */
    private void setGameData(final Player activePlayer,
                             final RatGameFile activelevel) {
        this.player = activePlayer;
        this.level = activelevel;
        this.generatorMap = new HashMap<>();

        // Bind game scene sizes
        this.gameForeground.heightProperty().addListener((val, old, cur) -> {
            this.gameBackground.setMinHeight(cur.doubleValue());
            this.gameBackground.setMaxHeight(cur.doubleValue());
            this.gameBackground.setPrefHeight(cur.doubleValue());

            this.gameScrollPane.setMaxHeight(cur.doubleValue());
        });
        this.gameForeground.widthProperty().addListener((val, old, cur) -> {
            this.gameBackground.setMinWidth(cur.doubleValue());
            this.gameBackground.setMaxWidth(cur.doubleValue());
            this.gameBackground.setPrefHeight(cur.doubleValue());

            this.gameScrollPane.setMaxWidth(cur.doubleValue());
        });

        // Disable save button (only allowed to save whilst paused)
        saveButton.setDisable(true);

        RatGameBuilder builder;
        if (this.level instanceof RatGameSaveFile) {
            builder = new RatGameBuilder(
                    this,
                    (RatGameSaveFile) this.level
            );
        } else {
            builder = new RatGameBuilder(
                    this,
                    this.level,
                    this.player
            );
        }

        this.game = builder.buildGame();
    }

    /**
     * @return Root node scene object.
     */
    public Parent getRoot() {
        return root;
    }

    /**
     * Game starting sequence requirements.
     */
    public void initStartSequence() {
        this.game.startGame();
        this.onPauseClicked();
    }

    /**
     * Starts the Rat game and displays it into the provided stage then waits
     * until the user closes the game.
     *
     * @param s The stage to display the game on.
     */
    public void startGame(final Stage s) {
        final Scene scene = new Scene(mainPane);
        s.setScene(scene);
        initStartSequence();

        s.setOnHidden((e) -> {
            if (!game.isGameOver()) {
                game.forceEnd();
            }
        });
        s.showAndWait();
    }

    /**
     * Set the message of the day text label to the provided text.
     *
     * @param s The message of the day to display.
     */
    public void setMotdText(final String s) {
        this.messageOfTheDayLabel.setText(s);
    }

    /**
     * @return The result of the game if the game has concluded normally.
     */
    public Optional<GameEndEvent> getGameResult() {
        return Optional.ofNullable(gameResult);
    }

    /**
     * @return The rat game default file that is being played.
     */
    public RatGameFile getLevel() {
        return this.level;
    }

    /**
     * Sets the style for this scene to the application default style.
     */
    private void setStyleSheet() {
        mainPane.getScene().getRoot().getStylesheets().clear();
        mainPane.getScene().getRoot().getStylesheets().add(
                Main.getCurrentStyle()
        );
    }

    /**
     * Pauses the game.
     */
    @FXML
    private void onPauseClicked() {
        this.saveButton.setDisable(!this.saveButton.isDisabled());

        // Start game if paused.
        if (this.game.isGamePaused()) {
            this.game.startGame();
            this.pauseButton.setText("Pause");

            // Pause game if not over
        } else if (!this.game.isGameOver()) {
            this.game.pauseGame();
            this.pauseButton.setText("Start");
        }
    }

    /**
     * Saves the current game to a save file and then alerts of either
     * success or failure.
     */
    @FXML
    private void onSaveClicked() {
        this.saveButton.setDisable(true);
        this.pauseButton.setDisable(true);

        // Save the game; file saved in a default location.
        if (this.game.isGamePaused() && !this.game.isGameOver()) {
            try {
                this.game.saveGame();
                this.saveButton.setDisable(false);
                this.pauseButton.setDisable(false);

                // Alert of success
                final Alert ae = new Alert(Alert.AlertType.INFORMATION);
                ae.setHeaderText("Save Successful!");
                ae.setContentText("You can now close the game and resume "
                        + "where you left off whenever you want."
                );
                ae.showAndWait();

            } catch (final UnknownSpriteEnumeration
                    | RatGameFileException
                    | IOException e) {
                // Alert of failure
                final Alert ae = new Alert(Alert.AlertType.ERROR);
                ae.setHeaderText("Save was not successful!");
                ae.setContentText("Some issue stopped the game from saving "
                        + "see: "
                        + e.getMessage()
                );
                ae.showAndWait();
            }
        }

    }

    /**
     * Hides the scroll bars.
     */
    @FXML
    private void onHideScrollBarsClicked() {
        ScrollPane.ScrollBarPolicy policy
                = ScrollPane.ScrollBarPolicy.AS_NEEDED;

        if (this.gameScrollPane.getVbarPolicy() == policy) {
            policy = ScrollPane.ScrollBarPolicy.NEVER;
        }

        this.gameScrollPane.setVbarPolicy(policy);
        this.gameScrollPane.setHbarPolicy(policy);
    }

    /**
     * Zooms in on the game.
     *
     * @param e Mouse event data.
     */
    @FXML
    private void onZoomIn(final MouseEvent e) {
        final float increment = 0.1f;

        // Zoom in on game scene
        if (e.getButton().equals(MouseButton.PRIMARY)) {
            final double x = this.gameBackground.getScaleX();
            final double y = this.gameBackground.getScaleY();

            this.gameBackground.setScaleX(x + increment);
            this.gameBackground.setScaleY(y + increment);

            this.gameForeground.setScaleX(x + increment);
            this.gameForeground.setScaleY(y + increment);

            // Zoom in on scroll pane
        } else {
            final double x = this.gameScrollPane.getScaleX();
            final double y = this.gameScrollPane.getScaleY();

            this.gameScrollPane.setScaleX(x + increment);
            this.gameScrollPane.setScaleY(y + increment);
        }
    }

    /**
     * Zooms out in the game.
     *
     * @param e Mouse event data.
     */
    @FXML
    private void onZoomOut(final MouseEvent e) {
        final double increment = 0.1;

        if (e.getButton().equals(MouseButton.PRIMARY)) {
            final double x = this.gameBackground.getScaleX();
            final double y = this.gameBackground.getScaleY();

            this.gameBackground.setScaleX(x - increment);
            this.gameBackground.setScaleY(y - increment);

            this.gameForeground.setScaleX(x - increment);
            this.gameForeground.setScaleY(y - increment);

            // Zoom out on scroll pane
        } else {
            final double x = this.gameScrollPane.getScaleX();
            final double y = this.gameScrollPane.getScaleY();

            this.gameScrollPane.setScaleX(x - increment);
            this.gameScrollPane.setScaleY(y - increment);
        }
    }


    /**
     * Makes the Game scroll pane view wider.
     *
     * @param event The mouse event to handle.
     */
    @FXML
    private void onZoomWidth(final MouseEvent event) {
        final MouseButton button = event.getButton();
        final double increment = 0.5;

        if (button.equals(MouseButton.PRIMARY)) {
            this.gameScrollPane.setScaleX(
                    this.gameScrollPane.getScaleX() + increment
            );

        } else {
            this.gameScrollPane.setScaleX(
                    this.gameScrollPane.getScaleX() - increment
            );
        }
    }

    /**
     * Resets the game zoom to the original values.
     */
    @FXML
    private void onResetZoom() {
        final int defaultScale = 1;

        this.gameBackground.setScaleX(defaultScale);
        this.gameBackground.setScaleY(defaultScale);

        this.gameForeground.setScaleX(defaultScale);
        this.gameForeground.setScaleY(defaultScale);

        this.gameScrollPane.setScaleX(defaultScale);
        this.gameScrollPane.setScaleY(defaultScale);
    }

    /**
     * Opens a new window containing the about data about the game with hints
     * and else
     */
    public void onLevelWikiClicked() {
        final FXMLLoader loader
                = new FXMLLoader(LevelWikiController.SCENE_FXML);

        try {
            final Scene scene = new Scene(loader.load());



            final Stage s = new Stage();
            s.setAlwaysOnTop(true);
            s.setScene(scene);
            s.getIcons().add(new Image(Objects.requireNonNull(getClass()
                    .getResourceAsStream("../menu/rat1.png"))));

            s.showAndWait();

        } catch (IOException e) {

            final Alert ae = new Alert(Alert.AlertType.ERROR);
            ae.setHeaderText("Failed to load the About Section!");
            ae.setContentText("Some issue stopped the about section from "
                    + "loading see: " + e.getMessage());
            Stage stage = (Stage) ae.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass()
                    .getResourceAsStream("rat1.png"))));
            ae.showAndWait();
        }
    }

    /**
     * Displays the leaderboard for the target level.
     */
    public void onShowScoreboardClicked() {

    }


    /**
     * Generic base event that all events should be sent to as this acts as
     * an Event filter routing the correct event to the correct handler.
     *
     * @param event The event to delegate.
     */
    @Override
    public void onAction(final GameEvent<?> event) {
        Platform.runLater(() -> super.onAction(event));
    }

    /**
     * Called when the game is paused.
     *
     * @param e Contains the game that was paused.
     */
    @Override
    public void onGamePaused(final GamePausedEvent e) {
    }

    /**
     * Called when the player of the game either loses or wins.
     *
     * @param e Information about specifically if they won or lost. And who
     *          the player was.
     */
    @Override
    public void onGameEndEvent(final GameEndEvent e) {

        final Parent root
                = EndScreenController.loadAndWait(e);
        this.gameResult = e;

        // Set up the stage
        final Stage s = new Stage();
        s.setScene(new Scene(root));
        s.initModality(Modality.APPLICATION_MODAL);

        s.showAndWait();

        // If save file delete the save file
        if (this.level instanceof RatGameSaveFile) {
            final String saveFilePath =
                    ((RatGameSaveFile) this.level).getSaveFile();
            final File file = new File(saveFilePath);

            if (!file.delete()) {
                final Alert ae = new Alert(Alert.AlertType.WARNING);
                ae.setHeaderText("Save file failed to delete!");
                ae.setContentText(
                        "Failed to delete the save file: "
                                + saveFilePath
                );
                ae.showAndWait();
            }
        }

        // Close game stage (returns to the main menu call)
        this.gameBackground.getScene().getWindow().hide();
    }

    /**
     * Called when the game is first being initialised it contains all the
     * relevant map data for displaying the underlying Tile Map and is used
     * as the point to for the GUI to be capable of preparing.
     *
     * @param e Information about which game is being loaded.
     */
    @Override
    public void onGameLoadEvent(final GameLoadEvent e) {

        this.entityMap = new EntityMap(
                e.getMapRows(),
                e.getMapColumns()
        );

        final GridPane pane = this.entityMap.getRoot();
        pane.getRowConstraints().forEach(i -> {
            i.setMinHeight(Tile.DEFAULT_SIZE);
            i.setMaxHeight(Tile.DEFAULT_SIZE);
        });

        pane.getColumnConstraints().forEach(i -> {
            i.setMinWidth(Tile.DEFAULT_SIZE);
            i.setMaxWidth(Tile.DEFAULT_SIZE);
        });
        // it allows to utilize the right click in-game to put down selected
        // item a4f
        pane.addEventFilter(MouseEvent.MOUSE_PRESSED, event->{
            if (event.getButton() == MouseButton.SECONDARY){
                double x = event.getX();
                double y = event.getY();
                final int row = (int) Math.floor(y / Tile.DEFAULT_SIZE);
                final int col = (int) Math.floor(x / Tile.DEFAULT_SIZE);

                game.useItem(
                        (Class<? extends Item>) itemSelected,
                        row,
                        col);
            }
        });
        // Allows to switch between items using hotkeys a4f
        root.addEventFilter(KeyEvent.KEY_PRESSED, event->{
            if (event.getCode() == KeyCode.P) {
                itemSelected = Poison.class;
            } else if (event.getCode() == KeyCode.B) {
                itemSelected = Bomb.class;
            } else if (event.getCode() == KeyCode.D) {
                itemSelected = DeathRat.class;
            } else if (event.getCode() == KeyCode.G) {
                itemSelected = Gas.class;
            } else if (event.getCode() == KeyCode.M) {
                itemSelected = MaleSexChange.class;
            } else if (event.getCode() == KeyCode.F) {
                itemSelected = FemaleSexChange.class;
            } else if (event.getCode() == KeyCode.N) {
                itemSelected = NoEntry.class;
            } else if (event.getCode() == KeyCode.S) {
                itemSelected = Sterilisation.class;
            }
        });

        pane.setOnDragOver(event -> {
            // Mark the drag event as acceptable by the gameStackPane.
            event.acceptTransferModes(TransferMode.ANY);
            // Mark the event as dealt.
            event.consume();
        });

        pane.setOnDragDropped(dragEvent -> {
            itemDropped(dragEvent);
            // Mark the event as dealt.
            dragEvent.consume();
        });

        this.gameForeground.getChildren().add(this.entityMap.getRoot());

        // Load game map
        this.tileMap = new GameMap(
                e.getMapRows(),
                e.getMapColumns(),
                new GridPaneFactory.CenteredGridPane()
        );
        for (Tile[] tiles : e.getTileMap()) {
            for (Tile tile : tiles) {
                tileMap.setNodeAt(
                        tile.getRow(),
                        tile.getCol(),
                        tile.getFXSpriteView()
                );
            }
        }
        this.tileMap.displayIn(gameBackground);

        this.playerNameLabel.setText(e.getPlayerName());
    }

    /**
     * Event for when an entity is loaded into the game this, specifically is
     * about the Origin loading point.
     *
     * @param e Information about which entity loaded and where it loaded to.
     */
    @Override
    public void onEntityLoadEvent(final EntityLoadEvent e) {

        final ImageView view = new ImageView();
        if (e.getImageResource() != null) {
            view.setImage(new Image(e.getImageResource().toExternalForm()));
        }
        view.setSmooth(false);
        view.setFitWidth(Tile.DEFAULT_SIZE);
        view.setFitHeight(Tile.DEFAULT_SIZE);

        // Tooltip which is immediately shown
        final Tooltip tip = new Tooltip(e.toString());
        Tooltip.install(view, tip);
        tip.setShowDuration(Duration.INDEFINITE);
        tip.setShowDelay(Duration.ZERO);


        this.entityMap.addView(
                e.getEntityID(),
                view,
                e.getRow(),
                e.getCol()
        );
    }

    /**
     * Event for when an ItemGenerator is first loaded into the game. Has all
     * of its initial values and states set.
     *
     * @param e Event about which specific generator has loaded.
     */
    @Override
    public void onGeneratorLoadEvent(final GeneratorLoadEvent e) {
        try {
            final ItemViewController c = ItemViewController.loadView(
                    e.getTargetClass());
            c.setMaxUsages(e.getMaxUsages());
            c.setItemImage(new Image(e.getDisplaySprite().toExternalForm()));
            c.setCurrentUsages(e.getCurUsages());
            c.setItemName(e.getTargetClass().getSimpleName());

            c.setOnDragDetectedEventListener();

            c.setStylesheet(Main.getCurrentStyle());

            itemVbox.getChildren().add(c.getRoot());
            this.generatorMap.put(e.getTargetClass(), c);

        } catch (Exception ex) {
            System.out.println("Error loading inventory item: "
                    + e.getTargetClass().getSimpleName()
            );
        }
    }

    /**
     * Called by the game for when the players score has changed.
     *
     * @param e Information about the player and their new score.
     */
    @Override
    public void onScoreUpdate(final ScoreUpdateEvent e) {
        this.scoreLabel.setText("Score: " + e.getPlayer().getCurrentScore());
    }

    /**
     * Event for when the Origin point of some entity has changed where the
     * origin is exactly where the entity exists. For instance if some Entity
     * has the origin (1,1) but occupies (2,3) and (4,5) then only 1,1 should
     * be modified/moved.
     *
     * @param e Event information about what entity moved and where it moved to.
     */
    @Override
    public void onEntityMovedEvent(final EntityMovedEvent e) {

        entityMap.setPosition(
                e.getEntityID(),
                e.getRow(),
                e.getCol(),
                e.getDirection()
        );

        final ImageView view = entityMap.getOriginView(e.getEntityID());
        final Tooltip t = new Tooltip(e.toString());
        t.setShowDelay(Duration.ONE);
        Tooltip.install(view, t);
    }

    /**
     * Whenever an Entity wants to be shown visually on a new Tile this event
     * is called. This does not mean that the Entity exists twice but rather
     * the same entity exists on more than one tile. This is used heavily by
     * items that would span many tiles visually.
     *
     * @param e The information of the target entity and which tile it
     *          actually occupied (position: Row, Col).
     */
    @Override
    public void onEntityOccupyTileEvent(final EntityOccupyTileEvent e) {
        final ImageView view = new ImageView();

        // Tooltip which is immediately shown
        final Tooltip tip = new Tooltip(e.toString());
        Tooltip.install(view, tip);
        tip.setShowDuration(Duration.INDEFINITE);
        tip.setShowDelay(Duration.ZERO);

        view.setImage(new Image(e.getImageResource().toExternalForm()));
        view.setSmooth(false);
        view.setFitWidth(e.getImageSize());
        view.setFitHeight(e.getImageSize());

        this.entityMap.occupyPosition(
                e.getEntityID(),
                view,
                e.getOccupiedRow(),
                e.getOccupiedCol()
        );
    }

    /**
     * Whenever an Entity no longer wants to be visually seen on a Tile this
     * event is called. Which should then remove that specific sprite from
     * the provided Row and Column values.
     *
     * @param e The event of the target entity and which position it should
     *          no longer be displayed on visually.
     */
    @Override
    public void onEntityDeOccupyTileEvent(final EntityDeOccupyTileEvent e) {
        this.entityMap.removeView(
                e.getEntityID(),
                e.getDeOccupiedRow(),
                e.getDeOccupiedCol()
        );
    }

    /**
     * Whenever an Entity ceases to exist; due to dying or just no longer
     * wanting to be displayed in the game. This event is called stating
     * which entity should be removed, and what sprite should be displayed as
     * its death sprite.
     *
     * @param e The event with the target entities death information.
     */
    @Override
    public void onEntityDeathEvent(final EntityDeathEvent e) {
        entityMap.removeView(
                e.getEntityID(),
                true
        );
    }

    /**
     * When an Entity wants to change the sprite that represents it; it will
     * create this event. This is used for say when a Rat has a Sex change
     * and is now female.
     *
     * @param e Event data of what exactly should be changed.
     */
    @Override
    public void onSpriteChangeEvent(final SpriteChangeEvent e) {
        if (e.getImageResource() != null) {

            this.entityMap.setImage(
                    e.getEntityID(),
                    new Image(e.getImageResource().toExternalForm()),
                    e.getImageRotation()
            );
        } else {
            this.entityMap.setImage(
                    e.getEntityID(),
                    null,
                    e.getImageRotation()
            );
        }

    }

    /**
     * When a generator has some state update this event occurs. This means
     * that the number of usages or time frame has been changed in some way.
     *
     * @param e Event data of what exactly changed/updated.
     */
    @Override
    public void onGeneratorUpdate(final GeneratorUpdateEvent e) {
        final ItemViewController cont
                = generatorMap.get(e.getTargetClass());

        final int maxTime = e.getRefreshTime();
        final int curTime = e.getCurRefreshTime();

        if (e.getCurUsages() != e.getMaxUsages()) {
            cont.setCurrentProgress((double) curTime / maxTime);

        } else {
            cont.setCurrentProgress(1.0);
        }

        cont.setCurrentUsages(e.getCurUsages());
    }

    /**
     * Update game state label information such as the players score, number
     * of rats, and time remaining.
     *
     * @param e The game state event.
     */
    @Override
    public void onGameStateUpdate(final GameStateUpdateEvent e) {
        // Matches everything but those specified
        final String baseRegex = "[^a-zA-Z\\s:]+";

        // Replace only the numerical part of the labels
        String labelText = timeRemainingLabel.getText();
        this.timeRemainingLabel.setText(labelText.replaceAll(
                baseRegex, String.valueOf((int) e.getClearTimeSeconds())
        ));

        labelText = numberOfRatsLabel.getText();
        this.numberOfRatsLabel.setText(labelText.replaceAll(
                baseRegex, String.valueOf(e.getNumHostileEntities())
        ));

        labelText = scoreLabel.getText();
        this.scoreLabel.setText(labelText.replaceAll(
                baseRegex, String.valueOf(player.getCurrentScore())
        ));

        // Set visual ratio
        this.setMaleToFemaleStats(
                e.getNumFemaleHostileEntities(),
                e.getNumMaleHostileEntities(),
                level.getDefaultProperties().getMaxRats()
        );
    }

    /**
     * Called whenever something in the game wants to produce some audio for
     * the action that they are doing or have completed.
     *
     * @param e The event containing the audio to play.
     */
    @Override
    public void onGenericAudio(final GenericAudioEvent e) {
        if (e.getAudioClip() != null) {
            final AudioClip clip =
                    new AudioClip(e.getAudioClip().toExternalForm());
            final double volume = 0.05;
            clip.setCycleCount(0);
            clip.setVolume(volume);
            clip.play();
        }
    }

    /**
     * Sets the visual display for the number of male rats to the number of
     * female rats.
     *
     * @param nFemales The total number of female rats.
     * @param nMales   The total number of male rats.
     * @param nMaxRats The max number of rats.
     */
    private void setMaleToFemaleStats(final int nFemales,
                                      final int nMales,
                                      final int nMaxRats) {

        //Calculate the percentage of each sex of rat
        final double femalePercentage = (double) nFemales / nMaxRats;
        final double malePercentage = (double) nMales / nMaxRats;
        final int scaleFactor = 100;

        this.femaleRatColumnConstraint.setPercentWidth(
                femalePercentage * scaleFactor
        );

        this.maleRatColumnConstraint.setPercentWidth(
                malePercentage * scaleFactor
        );

    }

    /**
     * Accepts a drag drop where the target data is an Item Class of the item
     * that is to be spawned into the game.
     *
     * @param event The drag event that was accepted.
     */
    @SuppressWarnings("unchecked")
    private void itemDropped(final DragEvent event) { //TODO modding
        double x = event.getX();
        double y = event.getY();

        final Object objectData =
                event.getDragboard().getContent(ItemViewController.DATA_FORMAT);
        if (objectData instanceof Class<?> objectClass) {
            final Class<Item> baseClass = Item.class;

            if (baseClass.isAssignableFrom(objectClass)) {
                final int row = (int) Math.floor(y / Tile.DEFAULT_SIZE);
                final int col = (int) Math.floor(x / Tile.DEFAULT_SIZE);

                // This cast is checked twice; first ensures objectData is a
                // Class, second ensures that it is assignable to Item; or
                // more specifically that Item is a super class of ObjectData.
                game.useItem(
                        (Class<? extends Item>) objectClass,
                        row,
                        col
                );
            }
        }
    }



    /**
     * Changes the games speed based on the target of the event.
     *
     * @param event Event data about which button was clicked.
     */
    @FXML
    private void onChangeGameSpeedClicked(final MouseEvent event) {
        // todo temporary method.
        final Object o = event.getSource();

        if (o instanceof final Button b
                && this.game.isGamePaused()) {
            final String slowDownID = "slow";
            final String resetSpeedID = "reset";
            final String speedUpID = "speedup";

            final int speedIncrement = 5;
            final int currentTimeFrame = this.game.getUpdateTimeFrame();

            // Internal speed caps are applied by the RatGame
            switch (b.getId()) {

                // Slow the game
                case slowDownID -> this.game.setUpdateTimeFrame(
                        currentTimeFrame + speedIncrement
                );

                // Reset the speed to default
                case resetSpeedID -> this.game.resetTimeFrame();


                // Speed up the game
                case speedUpID -> this.game.setUpdateTimeFrame(
                        currentTimeFrame - speedIncrement
                );

                default -> throw new IllegalStateException();
            }
        }
    }
}
