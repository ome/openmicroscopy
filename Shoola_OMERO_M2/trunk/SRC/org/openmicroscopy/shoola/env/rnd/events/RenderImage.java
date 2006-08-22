/*
 * org.openmicroscopy.shoola.env.rnd.events.RenderImage
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

package org.openmicroscopy.shoola.env.rnd.events;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;

/** 
 * Encapsulates a request to the <i>Rendering Engine</i> to render a given
 * pixels set within an image.
 * The <i>Rendering Engine</i> replies to this request with a 
 * {@link ImageRendererd} event.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class RenderImage
	extends RequestEvent
{

	/** The ID of the pixels set. */
	private int       pixelsID;
		
	/** 
	 * Defines what data, within the pixels set, to render.
	 * May be <code>null</code>, if this is a request to render the 
     * default plane. 
	 */
	private PlaneDef	planeDef;
    
	/**
	 * Creates a request to render the current plane within the given
	 * pixels set.
	 * 
	 * @param pixelsID	The ID of the pixels set.
	 */
	public RenderImage(int pixelsID)
	{
		this.planeDef = null;
		this.pixelsID = pixelsID;
	}
	
	/**
	 * Creates a request to render the given plane within the given
	 * pixels set.
	 * 
	 * @param pixelsID	The ID of the pixels set.
	 * @param planeDef	Selects a plane orthogonal to one of the <i>X</i>, 
	 * 					<i>Y</i>, or <i>Z</i> axes.  Mustn't be 
	 * 					<code>null</code>.
	 */
	public RenderImage(int pixelsID, PlaneDef planeDef)
	{
		if (planeDef == null)
			throw new NullPointerException("No plane definition.");
		this.planeDef = planeDef;
		this.pixelsID = pixelsID;
	}

	/** Return the ID of the pixels set. */
	public int getPixelsID() { return pixelsID; }

	/**
	 * Returns the definition of what data, within the pixels set, to render.
	 * Will return <code>null</code> if this is a request to render the current
	 * plane.
	 * 
	 * @return	See above.
	 */
	public PlaneDef getPlaneDef() { return planeDef; }

}
