package ca.isupeene.imagewarp.Warp;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;

/**
 * Created by isaac on 06/02/15.
 */
public abstract class WarpTask extends AsyncTask<Bitmap, Integer, Bitmap> {
    private WarpTaskListener _listener;

    protected WarpTask(WarpTaskListener listener) {
        super();

        _listener = listener;
    }

    @Override
    protected Bitmap doInBackground(Bitmap... params) {
        Bitmap bitmap = params[0];

        RenderScript rs = RenderScriptSingleton.get();
        Allocation input = Allocation.createFromBitmap(rs, bitmap);
        Allocation result = Allocation.createTyped(rs, input.getType());

        ScriptC_Warp script = new ScriptC_Warp(rs);
        script.set_xCenter(bitmap.getWidth() / 2);
        script.set_yCenter(bitmap.getHeight() / 2);
        script.set_xMax(bitmap.getWidth() - 1);
        script.set_yMax(bitmap.getHeight() - 1);
        script.set_inputAllocation(input);

        invokeKernel(script, result);

        Bitmap resultBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        result.copyTo(resultBitmap);

        return resultBitmap;
    }

    abstract protected void invokeKernel(ScriptC_Warp script, Allocation result);

    @Override
    protected void onPostExecute(Bitmap result) {
        _listener.onPostExecute(result);
    }
}
