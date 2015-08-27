/*
 * omeis.providers.re.codomain.PlaneSlicingMap
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.codomain;

/**
 * We assume that an image is composed of eight <code>1-bit</code> planes. Two
 * types of plane Slicing transformations {@link #transformConstant} and
 * {@link #transformNonConstant} are available. Let l denote the level of the
 * <code>planeSelected</code>. 1- Map all levels &lt; l to the constant
 * <code>lowerLimit</code> and the levels &gt; l to the constant
 * <code>upperLimit</code>. This transformation highlights the range l and
 * reduces all others to a constant level. 2- This transformation highlights the
 * rang l and preserves all other levels.
 * 
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
class PlaneSlicingMap implements CodomainMap {

    /** The mapping context of this map. */
    private PlaneSlicingContext psCtx;

    /**
     * Highlights the level of the <code>planeSelected</code> and reduces all
     * others to a constant level.
     * 
     * @param x
     *            The value to transform.
     * @return The transformed value.
     */
    private int transformConstant(int x) {
        if (x < psCtx.getPlaneSelected()) {
            return psCtx.getLowerLimit();
        } else if (x > psCtx.getPlaneSelected() + 1) {
            return psCtx.getUpperLimit();
        }
        return psCtx.getPlaneSelected();
    }

    /**
     * Highlights the level of the <code>planeSelected</code> but preserves
     * all other levels.
     * 
     * @param x
     *            The value to transform.
     * @return The transformed value.
     */
    private int transformNonConstant(int x) {
        if (x > psCtx.getPlanePrevious() && x <= psCtx.getPlaneSelected()) {
            return psCtx.getPlaneSelected();
        }
        return x;
    }

    /**
     * Implemented as specified in {@link CodomainMap}.
     * 
     * @see CodomainMap#setContext(CodomainMapContext)
     */
    public void setContext(CodomainMapContext ctx) {
        psCtx = (PlaneSlicingContext) ctx;
    }

    /**
     * Implemented as specified in {@link CodomainMap}.
     * 
     * @see CodomainMap#transform(int)
     */
    public int transform(int x) {
        if (psCtx.IsConstant()) {
            return transformConstant(x);
        }
        return transformNonConstant(x);
    }

    /**
     * Overridden to return the name of this map.
     * 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "PlaneSlicingMap";
    }

}
