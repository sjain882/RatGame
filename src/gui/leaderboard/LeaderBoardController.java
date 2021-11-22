package gui.leaderboard;

import game.motd.MOTDClient;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import launcher.Main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main menu scene controller.
 *
 * @author -Ry
 * @version 0.1
 * Copyright: N/A
 */
public class LeaderBoardController implements Initializable {

    /**
     * Hardcode the Scene Object Hierarchy Resource to the Controller
     * so that it can be accessed.
     */
    public static final URL SCENE_FXML =
            LeaderBoardController.class.getResource("LeaderBoard.fxml");

    @FXML
    private TableView<LeaderboardPlayer> leaderboardTableView;

    /**
     * Pane which sits behind the main menu buttons.
     */
    @FXML
    private Pane backgroundPane;

    /**
     * Message of the day label.
     */
    @FXML
    private Label motdLabel;

    private static ArrayList<LeaderboardPlayer> leaderboardPlayers =
            new ArrayList<LeaderboardPlayer>();

    /**
     * Setup MOTD pinger to constantly update the new
     * message of the day.
     *
     * @param url Un-used.
     * @param unused Un-used.
     */
    @Override
    public void initialize(final URL url,
                           final ResourceBundle unused) {
        //TODO

        addPlayerToLeaderboard(new LeaderboardPlayer(1, "Morgan",
                1000));

        //TODO Temporary tests
        leaderboardPlayers.add(new LeaderboardPlayer(3, "Bob", 200));
        leaderboardPlayers.add(new LeaderboardPlayer(1, "John", 700));
        leaderboardPlayers.add(new LeaderboardPlayer(2, "Jane", 500));

    }

    private void addPlayerToLeaderboard(LeaderboardPlayer player) {
        leaderboardTableView.getItems().add(player);
    }



}