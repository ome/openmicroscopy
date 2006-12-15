/*
 * omeis.providers.re.RenderingTask
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re;

import java.util.concurrent.Callable;

import omeis.providers.re.quantum.QuantizationException;

/**
 * This interface represents a rendering operation task. Its main purpose is to
 * explicitly define the exception strategy for the <code>call()</code>
 * method.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackat.ca</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/17 12:57:33 $) </small>
 * @since OMERO3.0
 */
public interface RenderingTask extends Callable {
    /**
     * Renders wavelength.
     * 
     * @throws QuantizationException
     *             If an error occurs while quantizing a pixels intensity value.
     */
    public Object call() throws QuantizationException;
}
