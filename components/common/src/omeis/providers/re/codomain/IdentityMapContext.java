/*
 * omeis.providers.re.codomain.IdentityMapContext
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.codomain;

/**
 * An empty context for the identity map.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class IdentityMapContext extends CodomainMapContext {

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
        return new IdentityMap();
    }

    /**
     * Implemented as specified by superclass.
     * 
     * @see CodomainMapContext#copy()
     */
    @Override
    public CodomainMapContext copy() {
        IdentityMapContext copy = new IdentityMapContext();
        copy.intervalEnd = intervalEnd;
        copy.intervalStart = intervalStart;
        return copy;
    }

}
