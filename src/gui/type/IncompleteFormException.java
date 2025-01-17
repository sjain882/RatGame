package gui.type;

/**
 * Java class created on 18/02/2022 for usage in project RatGame-A2.
 *
 * @author -Ry
 * @version 0.1
 * Copyright: N/A
 */
public class IncompleteFormException extends Exception {

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public IncompleteFormException(final String message) {
        super(message);
    }
}
