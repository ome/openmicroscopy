/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.LinkTarget
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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



/*------------------------------------------------------------------------------
 *
 * Written by:    Harry Hochheiser <hsh@nih.gov>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.chainbuilder.piccolo;

//Java imports
import java.awt.Color;
import java.awt.geom.Point2D;
 
//Third-party libraries
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.Constants;
import org.openmicroscopy.shoola.util.ui.piccolo.Link;
/** 
 * A Piccolo widget for a linkable target. These targets are found on the 
 * edges of {@llink PormalParameter} nodes, indicating where users should 
 * click to end {@link Link} nodes
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public class LinkTarget extends PPath {
	
	/**
	 * The color the target will have by default
	 */
	private static final Color LINK_TARGET_COLOR = new Color(0,105,0);
	
	/**
	 * A darker shade of green is used once there is something linked 
	 * to this target
	 * 
	 */
	private static final Color LINK_TARGET_LINKED_COLOR = new Color(0,55,0);
	
	
	
 	private Color currentColor;
 	
 	public LinkTarget() {
 		super();
 		setPathToRectangle(0,0,getSize(),getSize());
 		currentColor = LINK_TARGET_COLOR;
 		setPaint(currentColor);
 	}	
 	
 	public float getSize() {
 		return Constants.LINK_TARGET_SIZE;
 	}
 	
 	public Point2D getCenter() {
		PBounds b = getGlobalFullBounds();
		float x = (float) (b.getX()+b.getWidth()/2);
		float y = (float) (b.getY()+b.getHeight()/2);
		Point2D.Float result = new Point2D.Float(x,y);
		return result;
 	}
 	
 	/**
 	 * The target switches color when selected, reverting back to the unlinked
 	 * color when not selected
 	 * 
 	 * @param v true if the link has been selected
 	 */
 	public void setSelected(boolean v) {
 		if (v == true) 
 			setPaint(Constants.LINK_HIGHLIGHT_COLOR);
 		else
 			setPaint(currentColor);
 		repaint();
 	}
 	
 	/**
 	 * When something is attached to this target, the default color becomes
 	 * LINK_TARGET_LINKED_COLOR
 	 * 
 	 * @param v true if something has been linked to this target, else false
 	 */
 	public void setLinked(boolean v) {
 		if (v == true)
 			currentColor = LINK_TARGET_LINKED_COLOR;
 		else
 			currentColor = LINK_TARGET_COLOR;
 		setPaint(currentColor);
 	}
 	
 	public void setHighlighted(boolean v) {
 		if (v== true)
 			setPaint(Constants.HIGHLIGHT_COLOR);
 		else
 			setPaint(currentColor);
 		repaint();
 	}
}


	