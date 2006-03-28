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

import ome.model.display.RenderingDef;


//Java imports

//Third-party libraries

//Application-internal dependencies

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
     * Retrieves the IPixels Description i.e. the dimensions of the pixels set
     * in microns, the omeis id and the image id.
     * Other information will retrieve from omeis.
     * @return See below.
     * @throws PixServiceException If the connection is broken, or logged in
     *          or if an error occured while trying to. 
     */
    public ome.model.core.Pixels retrievePixDescription(long pixId);
    
    /**
     * Retrieves the rendering settings.
     * @return See below.
     * @throws PixServiceException If the connection is broken, or logged in
     *          or if an error occured while trying to. 
     */
    public RenderingDef retrieveRndSettings(long pixId);
    
    /**
     * Saves the specified rendering settings.
     * @param rndSettings
     * @throws PixServiceException If the connection is broken, or logged in
     *          or if an error occured while trying to. 
     */
    public void saveRndSettings(RenderingDef rndSettings);
    
}
