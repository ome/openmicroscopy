/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.events.metadata;

import java.util.List;

import omero.gateway.SecurityContext;

import org.openmicroscopy.shoola.env.event.RequestEvent;
import omero.gateway.model.ChannelData;

/**
 * Event posted when the channels have been updated.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class ChannelSavedEvent 
	extends RequestEvent
{

	/** The modified channels.*/
	private List<ChannelData> channels;
	
	/** The images whose channels have been updated.*/
	private List<Long> imageIds;
	
	/** Indicates the security context.*/
	private SecurityContext ctx;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param channels The modified channels.
	 * @param imageIds The images whose channels have been updated.
	 */
	public ChannelSavedEvent(SecurityContext ctx,
			List<ChannelData> channels, List<Long> imageIds)
	{
		this.channels = channels;
		this.imageIds = imageIds;
		this.ctx = ctx;
	}
	
	/**
	 * Returns the modified channels.
	 * 
	 * @return See above.
	 */
	public List<ChannelData> getChannels() { return channels; }
	
	/**
	 * Returns the images whose channels have been updated.
	 * 
	 * @return See above.
	 */
	public List<Long> getImageIds() { return imageIds; }

	/**
	 * Returns the security context.
	 * 
	 * @return See above.
	 */
	public SecurityContext getSecurityContext() { return ctx; }
	
}
