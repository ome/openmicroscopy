/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ParamLink
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
import edu.umd.cs.piccolo.PNode;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.piccolo.PConstants;
/** 
 * A Piccolo node for links between directly between two formal parameter.
 *
 * 
  * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public class ParamLink extends  Link {
	
	
	/**
	 * Note that "start" and "end" refer to where the _drawing_
	 * started and ended, not to the direction of the link.
	 * thus, start might be an input and end an output (even
	 * though links go output-input) or vice-versa.
	 */ 
	 private FormalParameter start;
	 private FormalParameter end;
	   
	public ParamLink() {
		super();
	}
	
	public ParamLink(FormalInput in,FormalOutput out) {
		super();
		//System.err.println("link between parameters");
		//this.start = in;
		//this.end = out;
		this.start=out;
		this.end=in;
		start.addNodeEventListener(this);
		end.addNodeEventListener(this);
		start.setLinkedTo(end,this);
		end.setLinkedTo(start,this);
		setStartPoint();
		setEndPoint();	
	}
	
	
	/**
	 * Since start is the first parameter provided, we can't 
	 * tell what it's linked to you. However, we can say that this link
	 * will listen to changes on the start parameter.
	 * @param start
	 */
	public void setStartParam(FormalParameter start) {
	
		this.start = start;
		start.addNodeEventListener(this);
		setStartPoint();
	}
	
	/**
	 * When the end parameter is set, update both start and end to 
	 * say that they are linked to each other, and set the end point of the link
	 * 
	 * 
	 * @param end
	 */
	public void setEndParam(FormalParameter end) {
		this.end = end;
		if (start != null) {
			end.setLinkedTo(start,this);  
			start.setLinkedTo(end,this);
		}
		end.addNodeEventListener(this);
		setEndPoint();
	}

	private void setStartPoint() {
		Point2D point = getEndPointCoords(start);
		globalToLocal(point);
		if (point != null)
			setStartCoords((float)point.getX(),(float) point.getY());
	}
	
	private void setEndPoint() {
		Point2D point = getEndPointCoords(end);
		globalToLocal(point);
		if (point != null)
			setEndCoords((float) point.getX(),(float) point.getY());
	}
		
	private Point2D getEndPointCoords(FormalParameter p) {
		Point2D paramCirclePoint = p.getLinkCenter();
		if (p instanceof FormalInput) {
			// For PFormalInputs, the point is actually to the left- 
			// in the middle of the LinkSelectionTarget
			Point2D offset = new 
				Point2D.Double(paramCirclePoint.getX()-PConstants.LINK_BULB_RADIUS,
								paramCirclePoint.getY());
				
			paramCirclePoint.setLocation(offset);
			
		}
		return paramCirclePoint;
	}
	
	/**
	 * To finish the link, draw the link bulb at the end that corresponds to the
	 * FormalInput -either the first point (if start is a FormalInput) or 
	 * the last point otherwise.
	 * 
	 * This procedure has some commented out code that's  left from when the 
	 * bulb was actuall an arrow. That may be reinstated someday, if the 
	 * Java rendering bug on macs that made arrows not work gets fixed.
	 * 
	 */
	protected void setLine() {
		Point2D second;
		try {
	
			if ( start instanceof FormalInput) {
				// in this case, we started at the input and drew back to output
			//	System.err.println("went from end to start");
				//first = (Point2D) points.get(1);
				second = (Point2D) points.get(0);	
			}
			else {
				int n = points.size();
		//		first = (Point2D) points.get(n-2);
				second = (Point2D) points.get(n-1);
			}
			//theta = getAngle((float) first.getX(),(float)first.getY(),
			//					(float)second.getX(),(float)second.getY());
			//System.err.println("ending link at "+second.getX()+","+second.getY());
			drawLinkEnd((float) second.getX(),(float)second.getY());
		}
		catch(Exception e) {
		}
	}


	/**
	 * When either formal parameter that this is linked to changes, adjust
	 * the start or end point and reset the end of the line.
	 */
	public void nodeChanged(NodeEvent e) {
		PNode node =e.getNode();
		if (!(node instanceof FormalParameter))
			return;
		if (node == start) {
			setStartPoint();
			setLine();
		}
		else if (node == end) 
			setEndPoint(); // setLine() call is implied.
	}		
	
	/**
	 * When a link between parameters is removed, the link between the modules
	 * may need to be removed, so ask the {@link LinkLayer} to do so.
	 */
	public void remove() {
		super.remove();
		if (linkLayer != null)
			linkLayer.removeModuleLinks(this);
		clearLinks();
	}
	
	/**
	 * Remove any state that indicates that start and end are linked to
	 * each other.
	 *
	 */	
	public void clearLinks() {
		start.clearLinkedTo(end);
		end.clearLinkedTo(start);
	}

	
	/**
	 * 
	 * @return the {@link FormalParameter} for whichever end point is an input
	 */
	public FormalInput getInput() {
		if (start instanceof FormalInput)
			return (FormalInput) start;
		else
			return (FormalInput) end;
	}
	
	/**
	 * 
	 * @return the {@link FormalParameter} for whichever end point is an output
	 */
	public FormalOutput getOutput() {
		if (start instanceof FormalOutput)
				return (FormalOutput) start;
			else
				return (FormalOutput) end;
	}
	
	/**
	 * @return the {@link LinkTarget} for the start end 
	 */
	public LinkTarget getStartLinkTarget() {
		return start.getLinkTarget();
	}
	
	/**
	 * @return the {@link LinkTarget} for the end parameter.. 
	 */
	public LinkTarget getEndLinkTarget() {
		return end.getLinkTarget();
	}
	
	/**
	 * @return the {@link FormalParameter} for the start parameter.. 
	 */
	public FormalParameter getStartParam() {
		return start;
	}
	
	/**
	 * @return the {@link FormalParameter} for the end parameter.. 
	 */
	public FormalParameter getEndParam() {
		return end;
	}
}
