/*
 * org.openmicroscopy.shoola.env.event.ServiceActivationRequest
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

package org.openmicroscopy.shoola.env.event;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Agents post this event to tell the container to start one of its services.
 * When an agent is notified (usually through an exception) that a container's
 * service is not active (because of a broken network connection, for example),
 * it can try and ask the container to start that service again by posting an
 * instance of this class on the {@link EventBus}.
 * However, this is only useful for those services that depend on external
 * servers. 
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
public class ServiceActivationRequest
	extends RequestEvent
{
	
	/**
	 * Indicates that this request is bogus.  
	 * This is just a convenience flag used by the constructor of this class.
	 */
	public static final int		NO_SERVICE = 0;
	
	/**
	 * Pass this to the constructor to tell the container to start the data
	 * services.
	 */
	public static final int		DATA_SERVICES = 1;
	
	/**
	 * Pass this to the constructor to tell the container to start the image
	 * services.
	 */
	public static final int		IMAGE_SERVICES = 2;
	
	
	
	
	/** Flag indicating which service to activate. */
	private int		whichService;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param whichService Flag indicating which service to activate. Must be
	 * 						one defined by the static fields of this class.
	 */
	public ServiceActivationRequest(int whichService)
	{
		if (whichService == DATA_SERVICES || whichService == IMAGE_SERVICES)
			this.whichService = whichService;
		else	whichService = NO_SERVICE;
	}
	
	/**
	 * Returns a flag indicating which service to activate.
	 * The returned value is one of those defined by the static fields of this
	 * class.
	 * 
	 * @return	See above.
	 */
	public int getWhichService()
	{
		return whichService;
	}

}
