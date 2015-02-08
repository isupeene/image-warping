package ca.isupeene.imagewarp.Warp;

import android.content.Context;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;

/**
 * Created by isaac on 08/02/15.
 */
public class RenderScriptSingleton {
    private static Context _context;
    private static RenderScript _rs;

    private static final String TAG = "RenderScriptSingleton";

    public static void setContext(Context context) {
        _context = context;
    }

    public static synchronized RenderScript get() {
        if (_context == null) {
            Log.wtf(TAG, "Tried to get the RenderScript singleton before a context was provided!");
        }

        if (_rs == null) {
            _rs = RenderScript.create(_context, RenderScript.ContextType.DEBUG);
        }

        return _rs;
    }
}
