/*
 * ome.util.math.geom2D.Segment
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.math.geom2D;

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

	/** The origin of the segment's first element. */
	public final double originX1;
	
	/** The origin of the segment's first element. */
	public final double originX2;

    /** The end point of the segment's first element. */
    public final double directionX1;
    
    /** The end point of the segment's second element. */
    public final double directionX2;

    /**
     * Creates a new instance.
     * 
	 * @param originX1 The origin of the segment's first element.
	 * @param originX2 The origin of the segment's second element.
     * @param endX1 The end point's first element.
     * @param endX2 The end point's second element.
     */
    public Segment(double originX1, double originX2, double endX1, double endX2)
    {
    	if (originX1 == endX1 && originX2 == endX2)
    	{
            throw new IllegalArgumentException("Need two different points.");
        }
    	this.originX1 = originX1;
    	this.originX2 = originX2;

        /*
         * Calculate the vector associated to the origin and the destination end 
         * point of this segment. This is the map that makes the set 
         * A = <b>R</b><sup>2</sup> an affine space over the vector space 
         * V = <b>R</b><sup>2</sup> and is defined by:
         * <p>
         * <nobr><i> f: AxA ---&gt; V <br>
         * f(a, b) = b - a = (b<sub>1</sub> - a<sub>1</sub>, b<sub>2</sub> - a<sub>2</sub>)
         * </i></nobr>
         * </p>
         */
    	directionX1 = endX1 - originX1;
    	directionX2 = endX2 - originX2;
    }
    
    /**
     * Returns the point of this line defined by <code>k</code>. More
     * precisely, this method returns the
     * <code>{@code origin}+k{@code direction}</code> point.
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
        return new PlanePoint(originX1 + k * directionX1, originX2 + k
                * directionX2);
    }

    /**
     * Tells whether a specified point lies on this line.
     * 
     * @param x1 The first element of the point to test
     * @param x2 The second element of the point to test
     * @return <code>true</code> if <code>p</code> lies on this line,
     *         <code>false</code> otherwise.
     */
    public boolean lies(double x1, double x2) {
        boolean result = false;
        double k1, k2;
        if (directionX1 == 0 && directionX2 != 0) {
            k2 = (x2 - originX2) / directionX2;
            if (k2 < 0 || k2 > 1) {
                result = false;
            } else {
                result = x1 == originX1;
            }
        } else if (directionX1 != 0 && directionX2 == 0) {
            k1 = (x1 - originX1) / directionX1;
            if (k1 < 0 || k1 > 1) {
                result = false;
            } else {
                result = x2 == originX2;
            }
        } else if (directionX1 != 0 && directionX2 != 0) {
            k1 = (x1 - originX1) / directionX1;
            k2 = (x2 - originX2) / directionX2;
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
     * Performs an equality test based on a point on this line defined
     * by <code>k</code> as in {@link #getPoint(double)} and another given
     * point.
     * @param k The coefficient to select the point. Must be in the range
     * <code>[0, 1]</code>.
     * @param x1 The point to test's first element.
     * @param x2 The point to test's second element.
     * @return <code>true</code> if the points are geometrically equal, 
     * <code>false</code> otherwise.
     */
    public boolean equals(double k, double x1, double x2)
    {
        if (k < 0 || k > 1) {
            throw new IllegalArgumentException("Coefficient must be in the "
                    + "range [0, 1].");
        }
    	double kPointX1 = originX1 + k * directionX1;
    	double kPointX2 = originX2 + k * directionX2;
    	return kPointX1 == x1 && kPointX2 == x2;

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
            isEqual = 
            	other.origin.x1 == originX1
            	&& other.origin.x2 == originX2
            	&& other.direction.x1 == directionX1
            	&& other.direction.x2 == directionX2;
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
        long bits = Double.doubleToLongBits(originX1);
        bits ^= Double.doubleToLongBits(originX2) * 31;
        return (int) bits ^ (int) (bits >> 32);
    }

}
