/*
 * omeis.providers.re.codomain.CodomainMap
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.codomain;

// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * Declares the interface common to all codomain maps. Subclasses encapsulates a
 * parameterized map:
 * <p>
 * <code>
 * y = f(x, p[1], ..., p[n])
 * </code>
 * </p>
 * <p>
 * The actual values of the parameters <code>p[k]</code> are defined by the
 * {@link CodomainMapContext} which is passed to the
 * {@link #setContext(CodomainMapContext) setContext} method.
 * </p>
 * 
 * @see CodomainMapContext
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public interface CodomainMap {

    /**
     * Sets the parameters used to write the equation of the specified codomain
     * transformation.
     * 
     * @param cxt
     *            Specifies the parameters.
     */
    public void setContext(CodomainMapContext cxt);

    /**
     * Performs the transformation.
     * 
     * @param x
     *            The input value.
     * @return The output value, y.
     */
    public int transform(int x);

}
