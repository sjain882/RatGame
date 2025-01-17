package game;

import game.contextmap.ContextualMap;
import game.contextmap.TileData;
import game.entity.Entity;
import game.event.GameActionListener;
import game.event.impl.entity.specific.load.GameLoadEvent;
import game.event.impl.entity.specific.load.GeneratorLoadEvent;
import game.generator.ItemGenerator;
import game.generator.RatItemInventory;
import game.level.Level;
import game.level.reader.RatGameFile;
import game.level.reader.RatGameSaveFile;
import game.player.Player;
import gui.game.dependant.tilemap.Coordinates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is responsible for building a game from a given level file,
 * player and event listener. It will construct the necessary dependencies
 * including the leaderboard, item generator, and entity list.
 *
 * @author Morgan Gardner
 * @version 0.2
 * Copyright: N/A
 */
public class RatGameBuilder {

    /**
     * The listener that receives all the game events.
     */
    private final GameActionListener listener;

    /**
     * The default file for the level to play.
     */
    private final RatGameFile defaultFile;

    /**
     * The save file for level.
     */
    private final RatGameSaveFile saveFile;

    /**
     * The player whose playing the game.
     */
    private final Player player;

    /**
     * @param listener    The listener that will handle all game events.
     * @param defaultFile The default file for the level to play.
     * @param player      The player whose playing the level.
     */
    public RatGameBuilder(final GameActionListener listener,
                          final RatGameFile defaultFile,
                          final Player player) {
        this.listener = listener;
        this.defaultFile = defaultFile;
        this.player = player;

        player.setLevel(defaultFile.getLevel());

        this.saveFile = null;
    }

    /**
     * @param listener The listener that will handle all game events.
     * @param saveFile The save file to load from.
     */
    public RatGameBuilder(final GameActionListener listener,
                          final RatGameSaveFile saveFile) {
        this.listener = listener;
        // The save file implies the default file
        this.defaultFile = saveFile;
        this.saveFile = saveFile;

        // Load the player
        this.player = saveFile.getPlayer();
        this.player.setLevel(saveFile.getLevel());
    }

    /**
     * Loads the game up firing relevant events as they need to. Then returns
     * a non started ready to play game.
     *
     * @return Setup game ready to play.
     */
    public RatGame buildGame() {

        if (this.saveFile != null) {
            // Load save map
            this.listener.onAction(new GameLoadEvent(
                    this.saveFile,
                    this.player
            ));
        } else {
            // Load map
            this.listener.onAction(new GameLoadEvent(
                    this.defaultFile,
                    this.player
            ));
        }

        if (this.saveFile != null) {
            // Load generators
            final RatItemInventory inv = this.saveFile.getSaveFileGenerator();
            for (ItemGenerator<?> gen : inv.getGenerators()) {
                this.listener.onAction(new GeneratorLoadEvent(gen));
                gen.setListener(this.listener);
            }

        } else {
            // Load generators
            final RatItemInventory inv
                    = this.defaultFile.getDefaultGenerator();
            for (ItemGenerator<?> gen : inv.getGenerators()) {
                this.listener.onAction(new GeneratorLoadEvent(gen));
                gen.setListener(this.listener);
            }
        }

        final RatGameProperties properties = loadProperties();
        final RatGameManager manager = loadManager();

        return new RatGame(manager, properties);
    }

    /**
     * Loads the game manager with all the entities from the target files.
     *
     * @return Newly constructed ready to use game manager.
     */
    private RatGameManager loadManager() {
        if (saveFile != null) {
            return loadManagerFromPosMap(saveFile.getEntityPositionMap());

        } else {
            return loadManagerFromPosMap(defaultFile.getEntityPositionMap());
        }
    }

    /**
     * Loads the Game manager from the provided entity position map.
     * Populating a contextual map with all the entities and setting their
     * pre-occupied tiles to the specified.
     *
     * @param entityPosMap Map of the entities and the positions that they
     *                     should occupy.
     * @return Fully loaded rat game manager ready to use.
     */
    private RatGameManager loadManagerFromPosMap(
            final HashMap<Entity, List<Coordinates<Integer>>> entityPosMap) {
        final Level level = defaultFile.getLevel();

        final ContextualMap map = new ContextualMap(
                level.getTiles(),
                level.getRows(),
                level.getColumns()
        );

        final List<Entity> entities = new ArrayList<>();
        entityPosMap.forEach((entity, positions) -> {
            entities.add(entity);
            entity.setListener(this.listener);

            // Place the entity then inform it of its placement into a game
            map.placeIntoGame(entity);
            entity.entityPlacedByLoader(map.getOriginTile(entity), map);


            // Occupy the tiles for the entity while also informing it of the
            // tile it now occupies.
            positions.forEach(i -> {
                map.occupyCoordinate(entity, i);
                final TileData occupied
                        = map.getTileDataAt(i.getRow(), i.getCol());

                entity.positionOccupiedByLoader(occupied, map);
            });
        });

        return new RatGameManager(
                entities.toArray(new Entity[0]),
                map
        );
    }

    /**
     * Loads the File properties for the level that is to be played into a
     * RatGameProperties object.
     *
     * @return Setup properties object.
     */
    private RatGameProperties loadProperties() {
        // We would just copy all the variable data to a save file and only
        // use that for it. But we don't have anything setup for that yet.
        if (saveFile != null) {
            return new RatGameProperties(
                    this.listener,
                    this.saveFile.getSaveFileGenerator(),
                    this.defaultFile.getDefaultProperties().getMaxRats(),
                    this.saveFile.getLeaderboard(),
                    this.defaultFile.getDefaultProperties().getTimeLimit(),
                    this.player,
                    this.defaultFile,
                    this.saveFile
            );

            // Default file loading
        } else {
            return new RatGameProperties(
                    this.listener,
                    this.defaultFile.getDefaultGenerator(),
                    this.defaultFile.getDefaultProperties().getMaxRats(),
                    this.defaultFile.getLeaderboard(),
                    this.defaultFile.getDefaultProperties().getTimeLimit(),
                    this.player,
                    this.defaultFile,
                    null
            );
        }
    }
}
