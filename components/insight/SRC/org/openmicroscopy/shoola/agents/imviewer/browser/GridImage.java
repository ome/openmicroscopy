/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.GridImage
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports

//Third-party libraries

//Application-internal dependencies

/**
 * Paints the grid.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class GridImage 
{

	/** The index of the channel. */
	private int 	channel;
	
	/** Flag indicating if the channel is on or off. */
	private boolean active;
	
	/** Indicates how to use color mask. */
	private boolean[] rgb;
	
	/** The label of the channel. */
	private String	  label;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param channel  The selected channel.
	 * @param active   Pass <code>true</code> if the channel is active,
	 * 				   <code>false</code> otherwise.
	 * @param label	   The label associated to the channel.
	 */
	GridImage(int channel, boolean active, String label)
	{
		this(channel, active, label, null);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param channel  The selected channel.
	 * @param active   Pass <code>true</code> if the channel is active,
	 * 				   <code>false</code> otherwise.
	 * @param rgb	   Indicates how to set the color mask.
	 */
	GridImage(int channel, boolean active, String label, boolean[] rgb)
	{
		this.channel = channel;
		this.active = active;
		this.rgb = rgb;
		this.label = label;
	}
	
	/**
	 * Returns the index of the channel.
	 * 
	 * @return See above.
	 */
	int getChannel() { return channel; }
	
	/**
	 * Returns <code>true</code> if the channel is active, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean isActive() { return active; }
	
	/**
	 * Returns the array indicating how to use color mask.
	 * 
	 * @return See above.
	 */
	boolean[] getRGB() { return  rgb; }
	
	/**
	 * Returns the label associated to the channel.
	 * 
	 * @return See above.
	 */
	String getLabel() { return label; }
	
} 
