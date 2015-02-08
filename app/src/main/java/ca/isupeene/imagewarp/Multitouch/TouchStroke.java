package ca.isupeene.imagewarp.Multitouch;

import java.util.ArrayList;
import java.util.List;

/**
 * A very simple representation of a TouchStroke tailored towards dealing with
 * horizontal movement.
 *
 * Created by isaac on 05/02/15.
 */
public class TouchStroke {
    private List<TouchPoint> _points = new ArrayList<TouchPoint>();

    private TouchPoint startingPoint() {
        return _points.get(0);
    }

    private TouchPoint endingPoint() {
        return _points.get(_points.size() - 1);
    }


    public TouchStroke(TouchPoint startingPoint) {
        _points.add(startingPoint);
    }

    public void addPoint(TouchPoint point) {
        if (point != endingPoint())
        {
            _points.add(point);
        }
    }

    public int size() {
        return _points.size();
    }

    public boolean overlapsHorizontallyWith(TouchStroke other) {
        if (startingPoint().leftOf(other.startingPoint()))
        {
            for (TouchPoint p1 : _points) {
                for (TouchPoint p2 : other._points)
                {
                    if (p2.leftOf(p1)) {
                        return true;
                    }
                }
            }
        }
        else
        {
            for (TouchPoint p1 : _points) {
                for (TouchPoint p2 : other._points)
                {
                    if (p1.leftOf(p2)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean coaxialWith(TouchStroke other)
    {
        // We are assuming horizontal swipes here, so really, it's just
        // "did these two strokes start on the same y?"
        return startingPoint().verticallyCloseTo(other.startingPoint());
    }

    /**
     * Returns true if the two TouchStrokes are moving in the opposite direction.
     */
    public boolean movingOppositeTo(TouchStroke other) throws StrokeLengthTooShortException
    {
        checkStrokeLength(2);
        other.checkStrokeLength(2);

        return Math.abs(
                (theta() + Math.PI) % (2 * Math.PI) - other.theta()
        ) < Math.PI / 6;
    }

    /**
     * Returns true if this TouchStroke is moving towards the other.
     */
    public boolean movingTowards(TouchStroke other) throws StrokeLengthTooShortException
    {
        checkStrokeLength(2);

        return Math.abs(
                theta() - startingPoint().angleTo(other.startingPoint())
        ) < Math.PI / 6;
    }

    /**
     * Returns true if this TouchStroke is moving away from the other.
     */
    public boolean movingAwayFrom(TouchStroke other) throws StrokeLengthTooShortException
    {
        checkStrokeLength(2);

        return Math.abs(
                (theta() + Math.PI) % (2 * Math.PI) - startingPoint().angleTo(other.startingPoint())
        ) < Math.PI / 6;
    }

    private void checkStrokeLength(int minimum) throws StrokeLengthTooShortException
    {
        if (size() < minimum)
        {
            throw new StrokeLengthTooShortException(minimum, size());
        }
    }

    private double theta()
    {
        return startingPoint().angleTo(endingPoint());
    }


    public String toString()
    {
        return String.format("%s;, theta = %f", _points, theta());
    }
}
