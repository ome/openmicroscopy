/*
 * omeis.providers.re.codomain.IdentityMap
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.codomain;

// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * The Identity map. This map is always in the codomain chain.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/20 10:59:54 $) </small>
 * @since OME2.2
 */
class IdentityMap implements CodomainMap {

    /**
     * Returns the value, no transformation needed in this case.
     * 
     * @see CodomainMap#transform(int)
     */
    public int transform(int x) {
        return x;
    }

    /**
     * Overridden to return the name of this map.
     * 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "IdentityMap";
    }

    /**
     * Required by I/F but no-op implementation in our case.
     * 
     * @see CodomainMap#setContext(CodomainMapContext)
     */
    public void setContext(CodomainMapContext cxt) {
    }

}
