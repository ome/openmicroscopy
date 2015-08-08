/*
 * ome.util.math.geom2D.Line
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.math.geom2D;

/**
 * An orientated line in the Euclidean space <b>R</b><sup>2</sup>.
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
public class Line {

    /**
     * The origin point of the line. Any point (within the line) that falls on
     * the half line that has the same orientation as the {@link #direction}
     * unit vector is said to have positive orientation &#151; this also
     * includes the {@link #origin} point. All other points of the line are said
     * to have (strictly) negative orientation.
     */
    public final PlanePoint origin;

    /**
     * The unit vector that, given the {@link #origin} point, identifies this
     * line.
     */
    public final PlanePoint direction;

    /**
     * Creates a new object to represent the line passing through the
     * <code>o</code> and <code>p</code> points. The <code>o</code> point
     * is taken to define the origin of the line and the direction is defined by
     * the <i>op</i> vector.
     * 
     * @param o
     *            The origin of the line. Mustn't be <code>null</code>.
     * @param p
     *            A point of the line. Mustn't be <code>null</code> nor the
     *            same as <code>o</code>.
     */
    public Line(PlanePoint o, PlanePoint p) {
        if (o == null) {
            throw new NullPointerException("No origin.");
        }
        if (p == null) {
            throw new NullPointerException("No point p.");
        }
        if (o.equals(p)) {
            throw new IllegalArgumentException("Need two different points.");
        }
        origin = o;
        direction = origin.vec(p).normalize();
    }

    /**
     * Creates a new object to represent the line passing through <code>o</code>
     * and having direction <i>pq</i>. The <code>p</code> and <code>q</code>
     * points are, respectively, the tail and head of the vector <i>pq</i> that
     * is taken to define the direction of the line.
     * 
     * @param p
     *            Tail of a vector. Mustn't be <code>null</code>.
     * @param q
     *            Head of a vector. Mustn't be <code>null</code> nor the same
     *            as <code>q</code>.
     * @param o
     *            The origin of the line. Mustn't be <code>null</code>.
     */
    public Line(PlanePoint p, PlanePoint q, PlanePoint o) {
        if (p == null) {
            throw new NullPointerException("No point p.");
        }
        if (q == null) {
            throw new NullPointerException("No point q.");
        }
        if (o == null) {
            throw new NullPointerException("No origin.");
        }
        if (p.equals(q)) {
            throw new IllegalArgumentException("Need two different points.");
        }
        origin = o;
        direction = p.vec(q).normalize();
    }

    /**
     * Returns the point of this line defined by <code>k</code>. More
     * precisely, this method returns the
     * <code>{@link #origin}+k{@link #direction}</code> point.
     * 
     * @param k
     *            The coefficient to select the point.
     * @return See above.
     */
    public PlanePoint getPoint(double k) {
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
        if (direction.x1 == 0) {
            return p.x1 == origin.x1;
        }
        if (direction.x2 == 0) {
            return p.x2 == origin.x2;
        }
        double k1 = (p.x1 - origin.x1) / direction.x1, k2 = (p.x2 - origin.x2)
                / direction.x2;
        return k1 == k2;
    }

    /**
     * Tells whether the specified point lies on this line and within the
     * specified orientation. The <code>positiveOrientation</code> parameter
     * is used to specify which side (with respect to the {@link #origin}) of
     * the line to check. If <code>true</code>, then we check to see whether
     * <code>p</code> falls on the half line that has the same orientation as
     * the {@link #direction} unit vector &#151; this also includes the
     * {@link #origin}. If <code>false</code>, we check to see whether
     * <code>p</code> falls on the opposite half line.
     * 
     * @param p
     *            The point to test. Mustn't be <code>null</code>.
     * @param positiveOrientation
     *            <code>true</code> for the positive orientation,
     *            <code>false</code> for the strictly negative orientation.
     * @return <code>true</code> if <code>p</code> lies on this line,
     *         <code>false</code> otherwise.
     */
    public boolean lies(PlanePoint p, boolean positiveOrientation) {
        if (!lies(p)) {
            return false;
        }
        double k;
        if (direction.x1 != 0) {
            k = (p.x1 - origin.x1) / direction.x1;
        } else {
            k = (p.x2 - origin.x2) / direction.x2; // direction.x2 can't be 0
        }
                                                    // too.
        if (positiveOrientation) {
            return 0 <= k;
        }
        return k < 0;
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
