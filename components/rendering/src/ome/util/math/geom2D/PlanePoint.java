/*
 * ome.util.math.geom2D.PlanePoint
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.math.geom2D;

/**
 * Represents a point in the Euclidean space <b>R</b><sup>2</sup>.
 * <p>
 * Because this space is built on the vector space <b>R</b><sup>2</sup>, any
 * instance of this class also represents a vector in <b>R</b><sup>2</sup>.
 * Moreover, unless otherwise stated, we assume the orthonormal frame <i>{O, e<sub>1</sub>,
 * e<sub>2</sub>}</i> where <i>O=(0,0), e<sub>1</sub>=(1, 0), e<sub>2</sub>=(0,
 * 1)</i>.
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
public class PlanePoint {

    /** The first element. */
    public final double x1;

    /** The second element. */
    public final double x2;

    /**
     * Creates a new instance.
     * 
     * @param x1  The first element of the point
     * @param x2  The second element of the point
     */
    public PlanePoint(double x1, double x2) {
        this.x1 = x1;
        this.x2 = x2;
    }

    /**
     * Calculates the distance between this point and the specified argument. We
     * use the standard distance of two points in the Euclidean space <b>R</b><sup>2</sup>.
     * That is, if <nobr><i>A=(a<sub>1</sub>, b<sub>1</sub>)</i></nobr>
     * and <nobr><i>B=(a<sub>2</sub>, b<sub>2</sub>)</i></nobr> are two
     * points of <b>R</b><sup>2</sup>, then the distance is defined by the
     * square root of <nobr><i> (a<sub>1</sub> - a<sub>2</sub>)<sup>2</sup> +
     * (b<sub>1</sub> - b<sub>2</sub>)<sup>2</sup> </i></nobr>.
     * 
     * @param p
     *            The other point. Mustn't be <code>null</code>.
     * @return The distance between this point and <i>p</i>.
     */
    public double distance(PlanePoint p) {
        PlanePoint tp = vec(p); // The vector associated to this point and p.
        return tp.norm(); // sqrt( (p.x1-x1)^2 + (p.x2-x2)^2 )
    }

    /**
     * Calculates the sum of this vector with the specified argument. This is
     * the standard sum of two vectors in the <b>R</b><sup>2</sup> group and
     * is given by the sum of their components.
     * 
     * @param vec
     *            The other vector. Mustn't be a <code>null</code> reference.
     * @return The sum of this vector with <code>vec</code>.
     */
    public PlanePoint sum(PlanePoint vec) {
        if (vec == null) {
            throw new NullPointerException("No vector.");
        }
        return new PlanePoint(x1 + vec.x1, x2 + vec.x2);
    }

    /**
     * Calculates the sum of this vector with the reciprocal of the specified
     * argument. The sum is the standard sum of two vectors in the <b>R</b><sup>2</sup>
     * group &#151; the sum of their components. Under this sum, the reciprocal
     * of an element <nobr><i>v=(x<sub>1</sub>, x<sub>2</sub>)</i></nobr>
     * of <b>R</b><sup>2</sup> is given by <nobr><i>-v=(-x<sub>1</sub>, -x<sub>2</sub>)</i></nobr>.
     * 
     * @param vec
     *            The other vector. Mustn't be a <code>null</code> reference.
     * @return The sum of this vector with <code>-vec</code>.
     */
    public PlanePoint diff(PlanePoint vec) {
        if (vec == null) {
            throw new NullPointerException("No vector.");
        }
        return new PlanePoint(x1 - vec.x1, x2 - vec.x2);
    }

    /**
     * Multiplies this vector by the specified scalar. This is the standard
     * scalar multiplication in the vector space <b>R</b><sup>2</sup>, which
     * is done by multiplying each component by the specified scalar.
     * 
     * @param k
     *            The scalar.
     * @return The vector obtained by multiplying this vector by <code>k</code>.
     */
    public PlanePoint scalar(double k) {
        return new PlanePoint(k * x1, k * x2);
    }

    /**
     * Calculates the vector associated to this point and the specified
     * argument. This is the map that makes the set A = <b>R</b><sup>2</sup>
     * an affine space over the vector space V = <b>R</b><sup>2</sup> and is
     * defined by:
     * <p>
     * <nobr><i> f: AxA ---&gt; V <br>
     * f(a, b) = b - a = (b<sub>1</sub> - a<sub>1</sub>, b<sub>2</sub> - a<sub>2</sub>)
     * </i></nobr>
     * </p>
     * This method returns <nobr><i>f(t, p)</i></nobr>, where <i>t</i> is
     * this point and <i>p</i> is the specified argument.
     * 
     * @param p
     *            The other point. Mustn't be a <code>null</code> reference.
     * @return The vector associated to this point and <i>p</i>.
     */
    public PlanePoint vec(PlanePoint p) {
        if (p == null) {
            throw new NullPointerException("No point.");
        }
        return new PlanePoint(p.x1 - x1, p.x2 - x2);
    }
    
    /**
     * Calculates the vector associated to this point and the specified
     * argument. This is the map that makes the set A = <b>R</b><sup>2</sup>
     * an affine space over the vector space V = <b>R</b><sup>2</sup> and is
     * defined by:
     * <p>
     * <nobr><i> f: AxA ---&gt; V <br>
     * f(a, b) = b - a = (b<sub>1</sub> - a<sub>1</sub>, b<sub>2</sub> - a<sub>2</sub>)
     * </i></nobr>
     * </p>
     * This method returns <nobr><i>f(t, p)</i></nobr>, where <i>t</i> is
     * this point and <i>p</i> is the specified argument.
     * 
     * @param x1  The first element of the point
     * @param x2  The second element of the point
     * @return The vector associated to this point and <i>p</i>.
     */
    public PlanePoint vec(double x1, double x2) {
        return new PlanePoint(x1 - this.x1, x2 - this.x2);
    }

    /**
     * Calulates the dot product of this vector by the specified argument. We
     * use the standard dot product of two vectors in <b>R</b><sup>2</sup>.
     * That is, if <nobr><i>v=(v<sub>1</sub>, v<sub>2</sub>)</i></nobr>
     * and <nobr><i>w=(w<sub>1</sub>, w<sub>2</sub>)</i></nobr> are two
     * vectors of <b>R</b><sup>2</sup>, then the dot product is defined by
     * <nobr> <i>v<sub>1</sub>*w<sub>1</sub> + v<sub>2</sub>*w<sub>2</sub></i></nobr>.
     * 
     * @param vec
     *            The other vector in the product. Mustn't be a
     *            <code>null</code> reference.
     * @return The dot product.
     */
    public double dot(PlanePoint vec) {
        if (vec == null) {
            throw new NullPointerException("No vector.");
        }
        return x1 * vec.x1 + x2 * vec.x2;
    }

    /**
     * Calculates the Euclidian norm of this vector. That is, the square root of
     * the {@link #dot(PlanePoint) dot} product of this vector by itself.
     * 
     * @return The norm of this vector.
     */
    public double norm() {
        return Math.sqrt(dot(this));
    }

    /**
     * Calculates the unit vector of this vector, provided this is not the null
     * vector. If this is not the null vector, then the returned vector is given
     * by <nobr><i>t</i> / ||<i>t</i>||</nobr>, where <i>t</i> is this
     * vector and ||<i>t</i>|| is its norm. Otherwise we return the null
     * vector.
     * 
     * @return A unit vector if this is not the null vector, the null vector
     *         otherwise.
     */
    public PlanePoint normalize() {
        double n = norm();
        if (n == 0) {
            return this; // Null vector.
        }
        return new PlanePoint(x1 / n, x2 / n);
    }

    /**
     * Calculates the angle between this vector and the specified argument,
     * provided none of these vectors is the null vector. We use the standard
     * definition of an angle between two non-null vectors. This is given by the
     * arc cosine of the dot product of the two vectors divided by the product
     * of their norms: <nobr>acos(<i>v<b>.</b>w</i> / ||<i>v</i>||*||<i>w</i>||)</nobr>.
     * If any of the two vectors is the null vector we throw an exception.
     * 
     * @param vec
     *            The other vector. Mustn't be a <code>null</code> reference
     *            and mustn't be the null vector.
     * @return The angle between this vector and <code>vec</code>, in the
     *         range of <code>0</code> through <code>pi</code>.
     * @throws IllegalArgumentException
     *             If this vector or <code>vec</code> or both are the null
     *             vector.
     */
    public double angle(PlanePoint vec) {
        if (vec == null) {
            throw new NullPointerException("No vector.");
        }
        double thisNorm = norm(), vecNorm = vec.norm(), dotPrd = this.dot(vec);
        if (thisNorm == 0 || vecNorm == 0) {
            throw new IllegalArgumentException(
                    "The angle is not defined for a null vector.");
        }
        return Math.acos(dotPrd / (thisNorm * vecNorm));
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
        if (o != null && o instanceof PlanePoint) {
            PlanePoint other = (PlanePoint) o;
            isEqual = x1 == other.x1 && x2 == other.x2;
        }
        return isEqual;
    }
    
    /**
     * Check to see if the point values are equal.
     * 
     * @param x1 The comparison point's first element.
     * @param x2 The comparison point's second element.
     * @return <code>true</code> if the points are equal, <code>false</code>
     * otherwise.
     */
    public boolean equals(double x1, double x2) {
    	return this.x1 == x1 && this.x2 == x2;
    }

    /**
     * Overridden to reflect equality of abstract values (data object) as
     * opposite to object identity.
     * 
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(x1);
        bits ^= Double.doubleToLongBits(x2) * 31;
        return (int) bits ^ (int) (bits >> 32);
    }

}
