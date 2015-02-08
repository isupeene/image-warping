package ca.isupeene.imagewarp.Warp;

import android.support.v8.renderscript.Allocation;

/**
 * Created by isaac on 06/02/15.
 */
public class NarrowTask extends WarpTask {

    public NarrowTask(WarpTaskListener listener) {
        super(listener);
    }

    @Override
    protected void invokeKernel(ScriptC_Warp script, Allocation result) {
        script.forEach_narrow(result);
    }
}
