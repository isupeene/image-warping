package ca.isupeene.imagewarp;

/**
 * Created by isaac on 06/02/15.
 */
public class Debug {
    public static void Assert(boolean condition) {
        if (BuildConfig.DEBUG && !condition) { throw new AssertionError(); }
    }
}
