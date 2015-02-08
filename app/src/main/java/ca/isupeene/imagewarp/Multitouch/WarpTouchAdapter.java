package ca.isupeene.imagewarp.Multitouch;

import android.view.MotionEvent;
import android.view.View;

/**
 * Simple touch listener for warp effects.  Since all warps only
 * require 2 fingers, this listener only considers the first two fingers
 * for touch motions.
 *
 * Created by isaac on 05/02/15.
 */
public class WarpTouchAdapter implements View.OnTouchListener {

    private WarpActionListener _listener;

    private TouchStroke stroke0;
    private TouchStroke stroke1;
    private boolean recognized = false;

    public WarpTouchAdapter(WarpActionListener listener) {
        _listener = listener;
    }

    // TODO: Maybe refactor this function?
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            int index = event.findPointerIndex(0);
            if (stroke0 == null && index != -1)
            {
                stroke0 = new TouchStroke(new TouchPoint(event.getX(index), event.getY(index)));
            }
        }
        else if ((event.getAction() & MotionEvent.ACTION_POINTER_DOWN) == MotionEvent.ACTION_POINTER_DOWN)
        {
            int index = event.findPointerIndex(1);
            if (stroke1 == null && index != -1) {
                stroke1 = new TouchStroke(new TouchPoint(event.getX(index), event.getY(index)));
            }
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE)
        {
            int index0 = event.findPointerIndex(0);
            int index1 = event.findPointerIndex(1);

            if (index0 != -1 && stroke0 != null)
            {
                stroke0.addPoint(new TouchPoint(event.getX(index0), event.getY(index0)));
            }
            if (index1 != -1 && stroke1 != null)
            {
                stroke1.addPoint(new TouchPoint(event.getX(index1), event.getY(index1)));
            }
        }
        else if (event.getAction() == MotionEvent.ACTION_UP)
        {
            stroke0 = null;
            stroke1 = null;
            recognized = false;
        }

        if (readyToCheckForWarpCondition())
        {
            if (swirl()) {
                _listener.onSwirl();
                recognized = true;
            }
            else if (fisheye()) {
                _listener.onFisheye();
                recognized = true;
            }
            else if (narrow()) {
                _listener.onNarrow();
                recognized = true;
            }
        }

        return true;
    }

    private boolean readyToCheckForWarpCondition() {
        return stroke0 != null && stroke1 != null &&
                stroke0.size() >= 3 && stroke1.size() >= 3 &&
                !recognized;
    }

    private boolean swirl() {
        return stroke0.movingOppositeTo(stroke1) &&
              !stroke0.coaxialWith(stroke1) &&
               stroke0.overlapsHorizontallyWith(stroke1);
    }

    private boolean fisheye() {
        return stroke0.movingAwayFrom(stroke1) &&
               stroke1.movingAwayFrom(stroke0) &&
               stroke0.coaxialWith(stroke1) &&
              !stroke0.overlapsHorizontallyWith(stroke1);
    }

    private boolean narrow() {
        return stroke0.movingTowards(stroke1) &&
               stroke1.movingTowards(stroke0) &&
               stroke0.coaxialWith(stroke1) &&
              !stroke0.overlapsHorizontallyWith(stroke1);
    }
}
