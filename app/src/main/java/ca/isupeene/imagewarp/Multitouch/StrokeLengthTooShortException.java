package ca.isupeene.imagewarp.Multitouch;

/**
 * Created by isaac on 05/02/15.
 */
public class StrokeLengthTooShortException extends IllegalStateException {
    public StrokeLengthTooShortException(int requiredLength, int actualLength)
    {
        super(String.format("This operation requires strokes of at least length %d.\n" +
                            "A stroke involved in this operation had length %d.",
                            requiredLength, actualLength
        ));
    }
}
