/*
 * ome.api.IPixels
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

// Java imports

// Third-party libraries

// Application-internal dependencies
import java.util.List;

import ome.model.IObject;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.model.enums.PixelsType;

/**
 * metadata gateway for the {@link omeis.providers.re.RenderingEngine}. This
 * service provides all DB access that the rendering engine needs. This allows
 * the rendering engine to also be run external to the server (e.g. client-side)
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/08 15:21:59 $) </small>
 * @since OME2.2
 */
public interface IPixels extends ServiceInterface {
    /**
     * Retrieves the pixels metadata (description). With the following objects
     * prelinked:
     * <ul>
     * <li>pixels.pixelsDimensions</li>
     * <li>pixels.channels</li>
     * <li>pixels.channnels.logicalChannel</li>
     * <li>pixels.channnels.statsInfo</li>
     * </ul>
     * 
     * @param pixId
     *            Pixels id.
     * @return Pixels object which matches <i>id</i>.
     */
    public Pixels retrievePixDescription(long pixId);

    /**
     * Retrieves the rendering settings for a given pixels set and the currently
     * logged in user.
     * 
     * @param pixId
     *            Pixels id.
     * @return Rendering definition.
     */
    public RenderingDef retrieveRndSettings(long pixId);

    /**
     * Saves the specified rendering settings.
     * 
     * @param rndSettings
     *            Rendering settings.
     */
    public void saveRndSettings(RenderingDef rndSettings);

    /**
     * Bit depth for a given pixel type.
     * 
     * @param type
     *            Pixels type.
     * @return Bit depth in bits.
     */
    public int getBitDepth(PixelsType type);

    /**
     * Retrieves a particular enumeration for a given enumeration class.
     * 
     * @param klass
     *            Enumeration class.
     * @param value
     *            Enumeration string value.
     * @return Enumeration object.
     */
    public <T extends IObject> T getEnumeration(Class<T> klass, String value);

    /**
     * Retrieves the exhaustive list of enumerations for a given enumeration
     * class.
     * 
     * @param klass
     *            Enumeration class.
     * @return List of all enumeration objects for the <i>klass</i>.
     */
    public <T extends IObject> List<T> getAllEnumerations(Class<T> klass);
}
