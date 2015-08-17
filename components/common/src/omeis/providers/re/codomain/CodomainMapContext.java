/*
 * omeis.providers.re.codomain.CodomainMapContext
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.codomain;

// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * Each concrete subclass defines transformation parameters for a
 * {@link CodomainMap} implementation.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public abstract class CodomainMapContext {

    /** The lower bound of the codomain interval. */
    protected int intervalStart;

    /** The upper bound of the codomain interval. */
    protected int intervalEnd;

    /**
     * Sets the codomain interval. No checks are needed as this method is
     * controlled by the <code>codomainChain</code>, which passes in
     * consistent values.
     * 
     * @param intervalStart
     *            The lower bound of the codomain interval.
     * @param intervalEnd
     *            The upper bound of the codomain interval.
     */
    public void setCodomain(int intervalStart, int intervalEnd) {
        this.intervalStart = intervalStart;
        this.intervalEnd = intervalEnd;
    }

    /**
     * This method is overridden so that objects of the same class are
     * considered the same. We need this trick to hanlde nicely
     * <code>CodomainMapContext</code> objects in collections.
     * 
     * @see Object#equals(Object)
     */
    @Override
    public final boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        return o.getClass() == getClass();
    }

    /**
     * Computes any parameter that depends on the codomain interval. The
     * <code>codomainChain</code> always calls this method after setting the
     * interval via {@link #setCodomain(int, int) setCodomain()}.
     */
    abstract void buildContext();

    /**
     * Returns an instance of the {@link CodomainMap} class that pairs up with
     * this concrete context class.
     * 
     * @return See above.
     */
    abstract CodomainMap getCodomainMap();

    /**
     * Returns a deep copy of this object.
     * 
     * @return See above.
     */
    public abstract CodomainMapContext copy();

}
