/*
 * omeis.providers.re.codomain.ReverseIntensityContext
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.codomain;

/**
 * The empty context of the reverse intensity map.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class ReverseIntensityContext extends CodomainMapContext {

    /**
     * Implemented as specified by superclass.
     * 
     * @see CodomainMapContext#buildContext()
     */
    @Override
    void buildContext() {
    }

    /**
     * Implemented as specified by superclass.
     * 
     * @see CodomainMapContext#getCodomainMap()
     */
    @Override
    CodomainMap getCodomainMap() {
        return new ReverseIntensityMap();
    }

    /**
     * Implemented as specified by superclass.
     * 
     * @see CodomainMapContext#copy()
     */
    @Override
    public CodomainMapContext copy() {
        ReverseIntensityContext copy = new ReverseIntensityContext();
        copy.intervalEnd = intervalEnd;
        copy.intervalStart = intervalStart;
        return this;
    }

}
