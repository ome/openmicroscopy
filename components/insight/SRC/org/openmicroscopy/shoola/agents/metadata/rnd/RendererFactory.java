/*
 * org.openmicroscopy.shoola.agents.metadata.rnd.RendererFactory 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.rnd;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import pojos.ImageData;

/** 
 * Factory to create the {@link Renderer} components.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class RendererFactory
{

    /**
     * Creates a new {@link Renderer}.
     * 
     * @param rndControl    Reference to the component that controls the
     *                      rendering settings. Mustn't be <code>null</code>.
     * @param image			The image the component is for.                    
     * @param rndIndex		The index of the renderer.
     * @return See above.
     */
    public static Renderer createRenderer(SecurityContext ctx, 
    		RenderingControl rndControl, 
    		ImageData image, int rndIndex)
    {
        RendererModel model = new RendererModel(ctx, rndControl, rndIndex);
        model.setImage(image);
        RendererComponent rnd = new RendererComponent(model);
        rnd.initialize();
        return rnd;
    }
    
}
