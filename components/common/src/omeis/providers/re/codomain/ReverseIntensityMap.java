/*
 * omeis.providers.re.codomain.ReverseIntensityMap
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.codomain;

// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * Reverses the intensity levels of an image. It produces the equivalent of a
 * photographic negative. This type of transformation is suited fo enhancing
 * white or gray details embedded in dark regions of an image, especially when
 * the black areas are dominant in size.
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
class ReverseIntensityMap implements CodomainMap {

    /** The mapping context of this map. */
    private CodomainMapContext ctx;

    /**
     * Implemented as specified in {@link CodomainMap}.
     * 
     * @see CodomainMap#setContext(CodomainMapContext)
     */
    public void setContext(CodomainMapContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Implemented as specified in {@link CodomainMap}.
     * 
     * @see CodomainMap#transform(int)
     */
    public int transform(int x) {
        return ctx.intervalEnd - x;
    }

    /**
     * Overridden to return the name of this map.
     * 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "ReverseIntensityMap";
    }

}
