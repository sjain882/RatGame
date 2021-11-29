package game.level.levels;

import game.level.reader.RatGameFile;

import java.io.File;
import java.net.URL;
import java.util.Objects;

/**
 * Known Rat Game Levels that can be played.
 *
 * @author -Ry
 * @version 0.1
 * Copyright: N/A
 */
public enum RatGameLevel {

    /**
     * Level one of the Rat game.
     */
    LEVEL_ONE(RatGameLevel.class.getResource("LevelOne.rgf"));

    /**
     * Error message for when the level is unknown.
     */
    private static final String ERR_UNKNOWN_NAME = "The name: \"%s\" is an "
            + "Unknown Level...";

    /**
     * Resource of the default rat game level file.
     */
    private final URL defaultLevel;

    /**
     * Checks to see if there is an enum constant with the provided name;
     * matching case-insensitive.
     *
     * @param name The name to find a match for.
     * @return The match found.
     * @throws IllegalArgumentException If no match was found.
     */
    public static RatGameLevel getLevelFromName(final String name) {
        for (RatGameLevel level : RatGameLevel.values()) {
            //final String lName = level.name().replaceAll("_", "");
            if (level.name().matches("(?i)" + name)) {
                return level;
            }
        }
        // No matching name
        throw new IllegalArgumentException(String.format(
                ERR_UNKNOWN_NAME,
                name
        ));
    }

    /**
     * Constructs the ordinal from the default file resource.
     *
     * @param file Default rat game level file.
     */
    RatGameLevel(final URL file) {
        Objects.requireNonNull(file);
        this.defaultLevel = file;
    }

    /**
     * @return Resource attached to the Rat game level file.
     */
    public URL getDefaultLevel() {
        return defaultLevel;
    }

    /**
     * Parses the associated target level into a {@link RatGameFile} so that
     * all modules can be accessed.
     *
     * @return Rat game file of the target level.
     */
    public RatGameFile getRatGameFile() {
        try {
            return new RatGameFile(new File(this.getDefaultLevel().toURI().getPath()));

            // Shouldn't occur
        } catch (Exception e) {
            throw new IllegalStateException(
                    e.getMessage()
            );
        }
    }
}
