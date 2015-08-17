/*
 * omeis.providers.re.codomain.ContrastStretchingMap
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.codomain;

// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * Basic piecewise linear functions. The idea is to increase the dynamic range
 * of levels in the image being processed. The locations of the points
 * <code>pStart</code> and <code>pEnd</code> (cf.
 * {@link ContrastStretchingContext}) determine the equation of the linear
 * functions.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
class ContrastStretchingMap implements CodomainMap {

    /** The mapping context. */
    private ContrastStretchingContext csCtx;

    /**
     * Implemented as specified in {@link CodomainMap}.
     * 
     * @see CodomainMap#setContext(CodomainMapContext)
     */
    public void setContext(CodomainMapContext ctx) {
        csCtx = (ContrastStretchingContext) ctx;
    }

    /**
     * Implemented as specified in {@link CodomainMap}.
     * 
     * @see CodomainMap#transform(int)
     */
    public int transform(int x) {
        int y = csCtx.intervalStart;
        if (x >= csCtx.intervalStart && x < csCtx.getXStart()) {
            y = (int) (csCtx.getA0() * x + csCtx.getB0());
        } else if (x >= csCtx.getXStart() && x < csCtx.getXEnd()) {
            y = (int) (csCtx.getA1() * x + csCtx.getB1());
        } else if (x >= csCtx.getXEnd() && x <= csCtx.intervalStart) {
            y = (int) (csCtx.getA2() * x + csCtx.getB2());
        }
        return y;
    }

    /**
     * Overridden to return the name of this map.
     * 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "ContrastStretchingMap";
    }

}
