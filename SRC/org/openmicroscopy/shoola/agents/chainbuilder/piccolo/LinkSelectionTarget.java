/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.LinkSelectionTarget
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
import java.awt.geom.Point2D;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.Constants;

/** 
 * A Piccolo widget indicating a control point for a selected {@link Link}
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */
public class LinkSelectionTarget extends LinkTarget {
	
	/**
	 * The {@link Link} in question
	 */
	private Link link;
	
	/**
	 * The index of this point in the {@link Link}, from 0 to n-1 (inclusive),
	 * where n is the number of control points.
	 */
	private int index;
	
	
	public LinkSelectionTarget(Link link,int index) {
		super();
		this.link = link;
		this.index = index;
	}
	
	public Link getLink() {
		return link;
	}
	
	public int getIndex() {
		return index;
	}
	
	/**
	 * When the selection is translated, the {@link Link} containing this 
	 * control point is updated ton coontain the new control point. 
	 * The {@link Link} is also redrawn
	 * 
	 * param x   the new coordinates of the control point 
	 * param y
	 */
	public void translate(double x,double y) {
		
		super.translate(x,y);
		Point2D pt = getOffset();
	//	System.err.println("new offset is "+pt.getX()+","+pt.getY());
		Point2D newPt = new Point2D.Float((float) pt.getX()+
			Constants.LINK_TARGET_HALF_SIZE,
			(float) pt.getY()+Constants.LINK_TARGET_HALF_SIZE);
		link.setPoint(index,newPt);
		link.setLine();
		link.setSelected(true);
		// adjust point in line
	}
	
}