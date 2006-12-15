/*
 * omeis.providers.re.data.XYPlane
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.data;

// Java imports
import java.nio.MappedByteBuffer;

// Third-party libraries

// Application-internal dependencies
import ome.model.core.Pixels;

/**
 * Provides the {@link Plane2D} implementation for <i>XY</i> planes.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision: 1.1 $ $Date:
 *          2005/06/08 20:15:03 $) </small>
 * @since OME2.2
 */
class XYPlane extends Plane2D {

    /**
     * Creates a new instance.
     * 
     * @param pDef
     *            The type of plane.
     * @param pixels
     *            The pixels set which the Plane2D references.
     * @param data
     *            The raw pixels.
     */
    XYPlane(PlaneDef pDef, Pixels pixels, MappedByteBuffer data) {
        super(pDef, pixels, data);
    }

    /**
     * Implemented as specified by the superclass.
     * 
     * @see Plane2D#calculateOffset(int, int)
     */
    protected int calculateOffset(int x1, int x2) {
        return bytesPerPixel * (sizeX * x2 + x1);
    }

}
