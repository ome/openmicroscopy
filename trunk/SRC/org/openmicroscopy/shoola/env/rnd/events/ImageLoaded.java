/*
 * org.openmicroscopy.shoola.env.rnd.events.ImageLoaded
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
import org.openmicroscopy.shoola.env.event.ResponseEvent;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;

/** 
 * The <i>Rendering Engine</i> posts this in response to a {@link LoadImage}
 * event when the image loading process has finished.
 * The <i>Rendering Engine</i> builds an instance of this class by passing the
 * original request and a proxy which agents can use to tune the rendering
 * settings of the loaded image.
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
public class ImageLoaded 
	extends ResponseEvent
{
	
	/** A proxy to the rendering context. */
	private RenderingControl	proxy;

	/**
	 * Creates a new instance.
	 * 
	 * @param act	The original request.
	 * @param proxy	A proxy to the rendering context of the loaded image.
	 */
	public ImageLoaded(LoadImage act, RenderingControl proxy)
	{
		super(act);
		this.proxy = proxy;
	}

	/** Returns a proxy to the rendering context of the loaded image. */
	public RenderingControl getProxy() { return proxy; }
	
}
