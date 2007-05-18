/*
 * org.openmicroscopy.shoola.env.data.views.ImageDataView
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data.views;



//Java imports

//Third-party libraries

//Application-internal dependencies
import omeis.providers.re.data.PlaneDef;
import org.openmicroscopy.shoola.env.event.AgentEventListener;

/** 
 * Provides methods to support image viewing and analysing
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public interface ImageDataView
    extends DataServicesView
{

    /**
     * Retrieves the metadata.
     * 
     * @param imageID	The id of the image.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadChannelMetadata(long imageID,
                                    AgentEventListener observer);
    
    /**
     * Loads the rendering proxy associated to the pixels set.
     * 
     * @param pixelsID  The id of the pixels set.
     * @param reload	Pass <code>true</code> to reload the rendering engine,
     * 					<code>false</code> otherwise.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadRenderingControl(long pixelsID, boolean reload,
                        AgentEventListener observer);
    
    /**
     * Renders the specified plane.
     * 
     * @param pixelsID  The id of the pixels set.
     * @param pd        The plane to render.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle render(long pixelsID, PlaneDef pd, 
                            AgentEventListener observer);

    /**
     * Retrieves an iconified version of the currently displayed image.
     * 
     * @param pixelsID      The id of the pixels set.
     * @param iconWidth     The width of the icon.
     * @param iconHeight    The height of the icon.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadIconImage(long pixelsID, int iconWidth, 
                            int iconHeight, AgentEventListener observer);
    
    /**
     * Retrieves the dimensions in microns of the pixels set.
     * 
     * @param pixelsID	The id of the pixels set.
     * @param observer	Callback handler.
     * @return See above.
     */
    public CallHandle loadPixelsDimension(long pixelsID, 
    					AgentEventListener observer);
    
    /**
     * Retrieves the pixels set.
     * 
     * @param pixelsID	The id of the pixels set.
     * @param observer	Callback handler.
     * @return See above.
     */
    public CallHandle loadPixels(long pixelsID, 
    					AgentEventListener observer);
    
}
