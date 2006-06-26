/*
 * ome.api.IPixels
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.api;

//Java imports

//Third-party libraries

//Application-internal dependencies
import java.util.List;

import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.model.enums.PixelsType;

/** 
 * metadata gateway for the {@link omeis.providers.re.RenderingEngine}. This 
 * service provides all DB access that the rendering engine needs. This allows
 * the rendering engine to also be run external to the server (e.g. client-side)  
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @author  <br>Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:josh.moore@gmx.de">
 *                  josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: 1.2 $ $Date: 2005/06/08 15:21:59 $)
 * </small>
 * @since OME2.2
 */
public interface IPixels extends ServiceInterface
{
    /**
     * Retrieves the pixels metadata (description).
     * @param pixId Pixels id.
     * @return Pixels object which matches <i>id</i>.
     */
    public Pixels retrievePixDescription(long pixId);
    
    /**
     * Retrieves the rendering settings for a given pixels set and the currently
     * logged in user.
     * @param pixId Pixels id.
     * @return Rendering definition.
     */
    public RenderingDef retrieveRndSettings(long pixId);
    
    /**
     * Saves the specified rendering settings.
     * @param rndSettings Rendering settings.
     */
    public void saveRndSettings(RenderingDef rndSettings);

    /**
     * Bit depth for a given pixel type.
     * @param type Pixels type.
     * @return Bit depth in bits.
     */
    public int getBitDepth(PixelsType type); 
 
    /**
     * Retrieves a particular enumeration for a given enumeration class.
     * @param klass Enumeration class.
     * @param value Enumeration string value.
     * @return Enumeration object.
     */
    public Object getEnumeration(Class klass, String value);
    
    /**
     * Retrieves the exhaustive list of enumerations for a given enumeration
     * class.
     * @param klass Enumeration class.
     * @return List of all enumeration objects for the <i>klass</i>.
     */
    public List getAllEnumerations(Class klass);
}
