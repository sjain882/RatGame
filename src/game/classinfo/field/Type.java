package game.classinfo.field;

import game.classinfo.field.supported.BooleanType;
import game.classinfo.field.supported.EnumerationWrapper;
import game.classinfo.field.supported.IntegerType;
import javafx.scene.control.TextFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Java interface created on 09/02/2022 for usage in project RatGame-A2.
 * Wraps some Type that can be created using some variable args.
 *
 * @author -Ry
 */
public interface Type {

    ///////////////////////////////////////////////////////////////////////////
    // If you don't know what you're doing here just know that
    // construct(String[]) and isComplete(String[]) are all you really care
    // about.
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Array of all known supported Types.
     */
    Type[] SUPPORTED_TYPES = collectSupported();

    /**
     * Compiles all known supported types into a single array.
     *
     * @return Array of all supported {@link Type} objects.
     */
    private static Type[] collectSupported() {
        final List<Type> types = new ArrayList<>();
        types.addAll(Arrays.asList(BooleanType.values()));
        types.addAll(Arrays.asList(IntegerType.values()));
        types.addAll(Arrays.asList(EnumerationWrapper.values()));

        return types.toArray(new Type[0]);
    }

    /**
     * Creates an instance of the target type from the provided arguments.
     *
     * @param args The args to use to create this type.
     * @return Newly constructed Instance.
     */
    Object construct(String... args);

    /**
     * Checks to see if the provided args are complete in the sense that
     * {@link #construct(String...)} will not throw an exception.
     *
     * @param args The args to test.
     * @return {@code true} if the args are safe to use.
     */
    boolean isComplete(String... args);

    /**
     * Used specifically for a JavaFX scene this enforces that some field
     * only contain the correct data.
     *
     * @return Text Formatter for the target type.
     */
    UnaryOperator<TextFormatter.Change> getTextFieldHandler();

    /**
     * @return The class type of this type.
     */
    Class<?> getTarget();

    /**
     * @return Factory object that can be used to construct this type.
     */
    GenericFactory<?> getFactory();
}
