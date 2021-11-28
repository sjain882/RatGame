package game.entity.subclass.deathRat;

import game.RatGame;
import game.contextmap.ContextualMap;
import game.entity.Item;
import game.level.reader.exception.ImproperlyFormattedArgs;
import game.level.reader.exception.InvalidArgsContent;

import java.net.URL;
import java.util.Arrays;
import java.util.Random;

/**
 * Rat.java - A death rat entity.
 * Uses the Entity class as a base.
 * Uses the MovementHandler class to handle autonomous movement around the
 * map, with random direction choices. It will interact with other rats by
 * murdering them mercilessly
 *
 * @author Maksim Samokhvalov
 * @version 0.1
 * Copyright: N/A
 */

public class DeathRat extends Item {
    //private MovementHandler movementHandler

    /**
     * Maximum number of rats the death rat will kill before dying.
     */
    private static final int MAX_KILL_COUNT = 8;

    /**
     * The minimum number of rats the death rat will kill before dying.
     */
    private static final int MIN_KILL_COUNT = 3;

    /**
     * The number of rats the death rat has to kill before dying.
     */
    private int killsRemaining;

    /**
     * Builds a death rat object from the provided args string.
     *
     * @param args Arguments used to build a bomb.
     * @return Newly constructed Bomb.
     */
    public static DeathRat build(final String[] args) throws ImproperlyFormattedArgs, InvalidArgsContent {
        final int expectedArgsLength = 4;

        if (args.length != expectedArgsLength) {
            throw new ImproperlyFormattedArgs(Arrays.deepToString(args));
        }

        try {
            final int row = Integer.parseInt(args[0]);
            final int col = Integer.parseInt(args[1]);
            final int health = Integer.parseInt(args[2]);
            final int remainingKills = Integer.parseInt(args[3]);

            return new DeathRat(row, col, health, remainingKills);
        } catch (Exception e) {
            throw new InvalidArgsContent(Arrays.deepToString(args));
        }
    }

    /**
     * Construct an Entity from the base starting Row and Column.
     *
     * @param initRow Row in a 2D Array. A[ROW][COL]
     * @param initCol Col in a 2D Array. A[ROW][COL]
     */
    public DeathRat(final int initRow,
                    final int initCol) {
        super(initRow, initCol);
        final Random r = new Random();

        // Generate random number of kills
        this.killsRemaining = r.nextInt(MIN_KILL_COUNT, MAX_KILL_COUNT);
    }

    /**
     * Construct an Entity from the base starting x, y, and health values.
     *
     * @param initialRow Row in a 2D Array. A[ROW][COL]
     * @param initialCol Col in a 2D Array. A[ROW][COL]
     * @param curHealth  Current health of the Entity.
     */
    public DeathRat(final int initialRow,
                    final int initialCol,
                    final int curHealth) {
        super(initialRow, initialCol, curHealth);
        final Random r = new Random();

        // Generate random number of kills
        this.killsRemaining = r.nextInt(MIN_KILL_COUNT, MAX_KILL_COUNT);
    }

    /**
     * Construct an Entity from the base starting x, y, and health values.
     *
     * @param initialRow Row in a 2D Array. A[ROW][COL]
     * @param initialCol Col in a 2D Array. A[ROW][COL]
     * @param curHealth  Current health of the Entity.
     */
    public DeathRat(final int initialRow,
                    final int initialCol,
                    final int curHealth,
                    final int killsRemaining) {
        super(initialRow, initialCol, curHealth);
        this.killsRemaining = killsRemaining;
    }

    /**
     * @return Remaining number of kills before the death rat dies.
     */
    public int getKillsRemaining() {
        return killsRemaining;
    }

    /**
     * Place where this rat can be updated and, do something once provided
     * some context objects.
     *
     * @param contextMap The map that this entity may exist on.
     * @param ratGame    The game that updated this entity.
     * @implNote Both Objects are Object because we don't have
     * implementations for these objects just yet.
     */
    @Override
    public void update(final ContextualMap contextMap,
                       final RatGame ratGame) {
        //TODO : Implement rat update, utilising movementHandler to move the
        // rat within the level.
    }

    /**
     * Get the display sprite resource for this item.
     *
     * @return Resource attached to an image file to display.
     */
    @Override
    public URL getDisplaySprite() {
        return null;
    }

    /**
     * Build the Rat to a String that can be saved to a File; all
     * parameters to construct the current state of the entity are required.
     *
     * @param contextMap The context map which contains extra info that may
     *                   not be stored directly in the Rat class.
     * @implNote Context map is Object since we don't have an implementation
     * of it yet.
     */
    @Override
    public String buildToString(final ContextualMap contextMap) {
        return String.format(
                "[DeathRat, [%s, %s, %s, %s], []]",
                getRow(),
                getCol(),
                getHealth(),
                getKillsRemaining()
        );
    }

    /**
     * Kills the Death Rat.
     */
    @Override
    public void kill() {
        super.kill();
    }
}