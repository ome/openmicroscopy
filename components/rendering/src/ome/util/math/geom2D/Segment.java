/*
 * ome.util.math.geom2D.Segment
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.math.geom2D;

// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * A segment in the Euclidean space <b>R</b><sup>2</sup>.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/09 15:01:32 $) </small>
 * @since OME2.2
 */
public class Segment {

    /** The origin point of the segment. */
    public final PlanePoint origin;

    /** The end point of the segment. */
    public final PlanePoint direction;

    /**
     * Creates a new instance.
     * 
     * @param o
     *            The origin point of the segment.
     * @param e
     *            The end point of the segment.
     */
    public Segment(PlanePoint o, PlanePoint e) {
        if (o == null) {
            throw new NullPointerException("No origin.");
        }
        if (e == null) {
            throw new NullPointerException("No end p.");
        }
        if (o.equals(e)) {
            throw new IllegalArgumentException("Need two different points.");
        }
        origin = o;
        direction = origin.vec(e);
    }

    /**
     * Returns the point of this line defined by <code>k</code>. More
     * precisely, this method returns the
     * <code>{@link #origin}+k{@link #direction}</code> point.
     * 
     * @param k
     *            The coefficient to select the point. Must be in the range
     *            <code>[0, 1]</code>.
     * @return See above.
     */
    public PlanePoint getPoint(double k) {
        if (k < 0 || k > 1) {
            throw new IllegalArgumentException("Coefficient must be in the "
                    + "range [0, 1].");
        }
        return new PlanePoint(origin.x1 + k * direction.x1, origin.x2 + k
                * direction.x2);
    }

    /**
     * Tells whether the specified point lies on this line.
     * 
     * @param p
     *            The point to test. Mustn't be <code>null</code>.
     * @return <code>true</code> if <code>p</code> lies on this line,
     *         <code>false</code> otherwise.
     */
    public boolean lies(PlanePoint p) {
        if (p == null) {
            throw new NullPointerException("No point.");
        }
        boolean result = false;
        double k1, k2;
        if (direction.x1 == 0 && direction.x2 != 0) {
            k2 = (p.x2 - origin.x2) / direction.x2;
            if (k2 < 0 || k2 > 1) {
                result = false;
            } else {
                result = p.x1 == origin.x1;
            }
        } else if (direction.x1 != 0 && direction.x2 == 0) {
            k1 = (p.x1 - origin.x1) / direction.x1;
            if (k1 < 0 || k1 > 1) {
                result = false;
            } else {
                result = p.x2 == origin.x2;
            }
        } else if (direction.x1 != 0 && direction.x2 != 0) {
            k1 = (p.x1 - origin.x1) / direction.x1;
            k2 = (p.x2 - origin.x2) / direction.x2;
            if (k1 == k2) {
                if (k1 < 0 || k1 > 1) {
                    result = false;
                } else {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Overridden to reflect equality of abstract values (data object) as
     * opposite to object identity.
     * 
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        boolean isEqual = false;
        if (o != null && o instanceof Line) {
            Line other = (Line) o;
            isEqual = origin == other.origin && direction == other.direction;
        }
        return isEqual;
    }

    /**
     * Overridden to reflect equality of abstract values (data object) as
     * opposite to object identity.
     * 
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return origin.hashCode();
    }

}
