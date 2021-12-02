package gui.game.dependant.entitymap.redone;

import game.contextmap.CardinalDirection;
import gui.game.dependant.tilemap.GridPaneFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
    private final HashMap<Long, EntityView> entityMap;

    /**
     * Maps a Node ID and the tiles that should occupy/be displayed on.
     */
    private final HashMap<Long, List<EntityView>> entityOccupyMap;

    /**
     * The root grid pane.
     */
    private final GridPane root;

    /**
     * Construct an Entity Map, which can be used to display entities by
     * accessing the GridPane and adding/removing ImageViews. The entities
     * are tracked in Hashmaps.
     *
     * @param rows The number of rows the map has.
     * @param cols The number of columns the map has.
     */
    public EntityMap(final int rows,
                     final int cols) {
        this.entityMap = new HashMap<>();
        this.entityOccupyMap = new HashMap<>();
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
                        final int row,
                        final int col) {
        this.entityMap.put(id, new EntityView(view, row, col));
        this.root.add(view, col, row);
    }

    /**
     * Adds an existing entity to another position on the map. Is used to
     * spread an entity across multiple tiles.
     *
     * @param id   The id of the entity to add.
     * @param view The node representation of the entity.
     * @param row  The row position to insert the view at.
     * @param col  The col position to insert the view at.
     */
    public void occupyPosition(final long id,
                               final ImageView view,
                               final int row,
                               final int col) {
        if (!this.entityOccupyMap.containsKey(id)) {
            this.entityOccupyMap.put(id, new ArrayList<>());
        }

        this.entityOccupyMap.get(id).add(new EntityView(view, row, col));
        this.root.add(view, col, row);
    }

    /**
     * Removes the given entity from the map at the given position. Will not
     * remove the whole entity if it is spread across multiple tiles.
     *
     * @param id  The id of the entity to remove.
     * @param row The row position to remove the entity from.
     * @param col The col position to remove the entity from.
     */
    public void deOccupyPosition(final long id,
                                 final int row,
                                 final int col) {
        if (this.entityOccupyMap.containsKey(id)) {
            final Iterator<EntityView> viewIterator =
                    this.entityOccupyMap.get(id).listIterator();

            while (viewIterator.hasNext()) {
                final EntityView entityView = viewIterator.next();

                if (entityView.getRow() == row && entityView.getCol() == col) {
                    this.root.getChildren().remove(entityView.getImageView());
                    viewIterator.remove();

                    if (this.entityOccupyMap.get(id).isEmpty()) {
                        this.entityOccupyMap.remove(id);
                    }
                }
            }
        }
    }

    /**
     * Removes the given entity from the map. Will remove all instances of an
     * entity, even it is spread across multiple tiles.
     * @param id The id of the entity to remove.
     */
    public void deOccupyPosition(final long id) {
        if (this.entityOccupyMap.containsKey(id)) {
            this.root.getChildren().remove(
                    this.entityMap.get(id).getImageView());
            this.entityOccupyMap.remove(id);
        }
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
        final ImageView view = getOriginView(id);
        this.root.getChildren().remove(view);
        this.root.add(view, col, row);
    }

    /**
     * Update a values position to the provided position, with direction.
     *
     * @param id  The id value for the node.
     * @param row The new row for the node.
     * @param col The new column for the node.
     * @param dir The direction in which to face the new node.
     */
    public void setPosition(final long id,
                            final int row,
                            final int col,
                            final CardinalDirection dir) {
        final ImageView view = getOriginView(id);
        view.getRotate();
        int rotationAngle = switch (dir) {
            case NORTH -> 0;
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
        };

        view.setRotate(rotationAngle);
        this.root.getChildren().remove(view);
        this.root.add(view, col, row);
    }

    /**
     * Updates the image for the id of this node to the provided image.
     *
     * @param id    The value to update.
     * @param image The new image to display.
     */
    public void setImage(final long id,
                         final Image image) {
        getOriginView(id).setImage(image);
    }

    /**
     * Fetch the image for a provided node's id.
     *
     * @param id The value to get.
     * @return The image for the provided id.
     */
    public Image getImage(final long id) {
        return this.entityMap.get(id).getImageView().getImage();
    }

    /**
     * Wraps an ImageView with a row and column variable to allow storing
     * entities views on each column, per id (type of entity).
     */
    public class EntityView {
        /**
         * The image view for this entity.
         */
        private final ImageView imageView;
        /**
         * The row of this entity.
         */
        private final int row;
        /**
         * The column of this entity.
         */
        private final int col;

        /**
         * Constructor for the EntityView. Initializes the image view, row, and
         * column.
         *
         * @param imageView The image view for this entity.
         * @param row       The row of this entity.
         * @param col       The column of this entity.
         */
        public EntityView(final ImageView imageView,
                          final int row,
                          final int col) {
            this.imageView = imageView;
            this.row = row;
            this.col = col;
        }

        /**
         * Getter for the image view.
         *
         * @return The image view for this entity.
         */
        public ImageView getImageView() {
            return imageView;
        }

        /**
         * Getter for the row.
         *
         * @return The row of this entity.
         */
        public int getRow() {
            return row;
        }

        /**
         * Getter for the column.
         *
         * @return The column of this entity.
         */
        public int getCol() {
            return col;
        }
    }

    /**
     * Get the origin view that represents exactly where the entity of the
     * provided id exists.
     *
     * @param id The id of the entity.
     * @return The view that represents the entity.
     */
    public ImageView getOriginView(final long id) {
        return this.entityMap.get(id).getImageView();
    }
}
