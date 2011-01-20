/*
 * org.openmicroscopy.shoola.agents.dataBrowser.browser.RollOverNode 
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
package org.openmicroscopy.shoola.agents.dataBrowser.browser;


//Java imports
import java.awt.Point;

//Third-party libraries

//Application-internal dependencies

/** 
 * Helper class hosting a node and its location on screen
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class RollOverNode 
{

	/** The node to host. */
	private ImageNode 	node;
	
	/** The location of the node. */
	private Point 		locationOnScreen;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param node The node to host.
	 * @param locationOnScreen The location of the node on screen. 
	 */
	public RollOverNode(ImageNode node, Point locationOnScreen)
	{
	
		this.node = node;
		this.locationOnScreen = locationOnScreen;
	}
	
	/**
	 * Returns the hosted node.
	 * 
	 * @return See above.
	 */
	public ImageNode getNode() { return node; }
	
	/**
	 * Returns the location.
	 * 
	 * @return See above.
	 */
	public Point getLocationOnScreen() { return locationOnScreen; }

}
