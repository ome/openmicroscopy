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
 * Metadata gateway for the {@link omeis.providers.re.RenderingEngine} and
 * clients. This service provides all DB access that the rendering engine 
 * needs as well as Pixels services to a client. It also allows the rendering 
 * engine to also be run external to the server (e.g. client-side).
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @author <br>
 *         Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/08 15:21:59 $) </small>
 * @since OME2.2
 */
public interface IPixels extends ServiceInterface {
    /**
     * Retrieves the pixels metadata with the following objects pre-linked:
     * <ul>
     * <li>pixels.pixelsType</li>
     * <li>pixels.pixelsDimensions</li>
     * <li>pixels.pixelsFileMaps</li>
     * <li>pixels.pixelsFileMaps.parent</li>
     * <li>pixels.pixelsFileMaps.parent.format</li>
     * <li>pixels.channels</li>
     * <li>pixels.channnels.statsInfo</li>
     * <li>pixels.channnels.colorComponent</li>
     * <li>pixels.channnels.logicalChannel</li>
     * <li>pixels.channnels.logicalChannel.photometricInterpretation</li>
     * </ul>
     * 
     * @param pixelsId
     *            Pixels id.
     * @return Pixels object which matches <i>id</i>.
     */
    public Pixels retrievePixDescription(long pixelsId);

    /**
     * Retrieves the rendering settings for a given pixels set and the currently
     * logged in user with the following objects pre-linked:
     * <ul>
     * <li>renderingDef.quantization</li>
     * <li>renderingDef.model</li>
     * <li>renderingDef.waveRendering</li>
     * <li>renderingDef.waveRendering.color</li>
     * <li>renderingDef.waveRendering.family</li>
     * <li>renderingDef.spatialDomainEnhancement</li>
     * </ul>
     * 
     * @param pixelsId
     *            Pixels id.
     * @return Rendering definition.
     */
    public RenderingDef retrieveRndSettings(long pixelsId);
    
    /**
     * Copies the metadata, and <b>only</b> the metadata linked to a Pixels
     * object into a new Pixels object of equal or differing size across one
     * or many of its three physical dimensions or temporal dimension.
     * Modification of the number of channels is <b>not</b> allowed through
     * this method due to the sheer number of changes that would have to take
     * place. Callers who wish to modify the number of channels are encouraged
     * to build up a new Pixels set and associated metadata objects and save
     * them through {@link IUpdate}. Furthermore, it is beyond the scope
     * of this method to handle updates or changes to the raw pixel data
     * available through {@link RawPixelsStore} or to add and link
     * {@link StatsInfo}, {@link PlaneInfo} and/or other Pixels set specific
     * metadata. It is also assumed that the caller wishes the physical 
     * {@link PixelsDimensions} and {@link PixelsType} to remain the same;
     * changing these is outside the scope of this method. <b>NOTE:</b> As 
     * {@link  Channel} objects are only able to apply to a single set of
     * Pixels any annotations or linkage to these objects will be lost.
     * 
     * @param pixelsId The source Pixels set id.
     * @param sizeX The new size across the X-axis. <code>null</code> if the
     * copy should maintain the same size.
     * @param sizeY The new size across the Y-axis. <code>null</code> if the
     * copy should maintain the same size.
     * @param sizeZ The new size across the Z-axis. <code>null</code> if the
     * copy should maintain the same size.
     * @param sizeT The new number of timepoints. <code>null</code> if the
     * copy should maintain the same number.
     * @param methodology An optional string signifying the methodology that
     * will be used to produce this new Pixels set.
     * @return Id of the new Pixels object on success or <code>null</code> on
     * failure.
     * @throws ValidationException If the X, Y, Z or T dimensions are out
     * of bounds or the Pixels object corresponding to <code>pixelsId</code> is 
     * unlocatable. 
     */
    public Long copyAndResizePixels(long pixelsId, Integer sizeX, Integer sizeY,
                                    Integer sizeZ, Integer sizeT,
                                    String methodology);

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
