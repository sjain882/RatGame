package gui.game.dependant.entitymap.redone;

import gui.game.dependant.tilemap.GridPaneFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.HashMap;

/**
 * Simple Entity map for the game entities.
 *
 * @author -Ry
 * @version 0.1
 * Copyright: N/A
 */
public class EntityMap {

    // Simple ID -> Node  mapping system that will allow the update of a new
    // position based on the row col.

    /**
     * Map of ID values and their node representation.
     */
    private final HashMap<Long, ImageView> entityMap;

    /**
     * The root grid pane.
     */
    private final GridPane root;

    /**
     * @param rows The number of rows the map has.
     * @param cols The number of columns the map has.
     */
    public EntityMap(final int rows,
                     final int cols) {
        this.entityMap = new HashMap<>();
        this.root = new GridPaneFactory.CenteredGridPane().supply(rows, cols);
    }

    /**
     * @return The root object hierarchy for the scene.
     */
    public GridPane getRoot() {
        return this.root;
    }

    /**
     * @param id   The id value of the view to add.
     * @param view The node representation of the value.
     * @param row  The row position to display the node at (2d array indexing
     *             Row, Col)
     * @param col  The col position to display the node at.
     */
    public void addView(final long id,
                        final ImageView view,
                        int row,
                        int col) {
        this.entityMap.put(id, view);
        this.root.add(view, col, row);
    }

    /**
     * Update a values position to the provided position.
     *
     * @param id  The id value for the node.
     * @param row The new row for the node.
     * @param col The new column for the node.
     */
    public void setPosition(final long id,
                            final int row,
                            final int col) {
        final ImageView view = this.entityMap.get(id);
        this.root.getChildren().remove(view);
        this.root.add(view, col, row);
    }
}
