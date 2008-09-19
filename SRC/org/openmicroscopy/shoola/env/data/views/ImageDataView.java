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
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import ome.model.core.Pixels;
import omeis.providers.re.data.PlaneDef;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;

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

	/** Indicates to load the rendering engine. */
	public static final int LOAD = 0;
	
	/** Indicates to reload the rendering engine. */
	public static final int RELOAD = 1;
	
	/** Indicates to reload the rendering engine. */
	public static final int RESET = 2;
	
    /**
     * Retrieves the metadata.
     * 
     * @param pixelsID	The id of the pixels set.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadChannelMetadata(long pixelsID,
                                    AgentEventListener observer);
    
    /**
     * Loads the rendering proxy associated to the pixels set.
     * 
     * @param pixelsID  The id of the pixels set.
     * @param index		One of the constants defined by this class.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadRenderingControl(long pixelsID, int index,
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
    
    /**
     * Retrieves the dimensions in microns of the pixels set.
     * 
     * @param pixels	The pixels set to analyse.
     * @param channels	Collection of active channels. 
     * 					Mustn't be <code>null</code>.
     * @param shapes	Collection of shapes to analyse. 
     * 					Mustn't be <code>null</code>.
     * @param observer	Callback handler.
     * @return See above.
     */
    public CallHandle analyseShapes(Pixels pixels, List channels, List shapes, 
    								AgentEventListener observer);
    
    /**
     * Retrieves all the rendering settings associated to a given set of pixels.
     * 
     * @param pixelsID The id of the pixels set.
     * @param observer	Callback handler.
     * @return See above.
     */
    public CallHandle getRenderingSettings(long pixelsID, 
    									AgentEventListener observer);
    
    /**
     * Projects a section of the stack and returns the projected image.
     * 
     * @param pixelsID The id of the pixels set.
     * @param startZ   The first optical section.
     * @param endZ     The last optical section.
     * @param stepping Stepping used while projecting. 
     *                 Default is <code>1</code>
     * @param type     The type of projection.
     * @param channels The collection of channels to project.
     * @param observer Callback handler.
     * @return See above.
     */
    public CallHandle renderProjected(long pixelsID, int startZ, int endZ,
    		int stepping, int type, List<Integer> channels,
    		AgentEventListener observer);
    
    /**
     * Projects a section of the stack and returns the projected image.
     * 
     * @param ref 		The object hosting the projection's parameters.
     * @param observer 	Callback handler.
     * @return See above.
     */
    public CallHandle projectImage(ProjectionParam ref, 
    							AgentEventListener observer);

    /**
     * Creates rendering setting for the specified pixels set and
     * copies the settings from the passed rendering setting object if
     * not <code>null</code>.
     * 
     * @param pixelsID	The id of the pixels set to handle.
     * @param rndToCopy The rendering settings to copy to the newly created one.
     * @param indexes	Collection of channel's indexes. 
     * 					Mustn't be <code>null</code>.
     * @param observer 	Callback handler.
     * @return See above.
     */
    public CallHandle createRndSetting(long pixelsID, RndProxyDef rndToCopy,
    		List<Integer> indexes, AgentEventListener observer);
    
}
