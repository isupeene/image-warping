package ca.isupeene.imagewarp.Multitouch;

/**
 * Created by isaac on 05/02/15.
 */
public class TouchPoint {
    float _x;
    float _y;

    public TouchPoint(float x, float y) {
        _x = x;
        _y = y;
    }

    public boolean leftOf(TouchPoint other)
    {
        return _x < other._x;
    }

    public boolean verticallyCloseTo(TouchPoint other)
    {
        return Math.abs(_y - other._y) < 150;
    }

    public double angleTo(TouchPoint other)
    {
        // We add pi so that we can return an angle between 0 and 2pi, instead of
        // between -pi and pi.
        return Math.atan2(other._y - _y, other._x - _x) + Math.PI;
    }

    public boolean equals(Object other) {
        if (other instanceof TouchPoint)
        {
            TouchPoint otherTouchpoint = (TouchPoint)other;
            return _x == otherTouchpoint._x && _y == otherTouchpoint._y;
        }
        else
        {
            return false;
        }
    }

    public String toString()
    {
        return String.format("(%f, %f)", _x, _y);
    }
}
