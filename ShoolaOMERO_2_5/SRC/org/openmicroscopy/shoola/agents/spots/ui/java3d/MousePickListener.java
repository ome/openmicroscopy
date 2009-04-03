/*
 * org.openmicroscopy.shoola.agents.spots.ui.java3d.MousePickListener
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

package org.openmicroscopy.shoola.agents.spots.ui.java3d;

//Java imports
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickResult;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.media.j3d.Node;
import javax.media.j3d.SceneGraphPath;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.events.TrajectoryEventManager;


/** 
 * Handler for mouse-picking of trajectories. 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */



public class MousePickListener extends MouseAdapter {
	
	private PickCanvas pickCanvas;
	
	
	public MousePickListener(PickCanvas pickCanvas) {
		this.pickCanvas = pickCanvas;
		
	}
	
	    
    public void mouseClicked(MouseEvent e) {
    	
    	pickCanvas.setShapeLocation( e );
		PickResult pickResult = pickCanvas.pickClosest( );
		try {
			if( pickResult != null  && 
					pickResult.numIntersections() >0) {
				SceneGraphPath path = pickResult.getSceneGraphPath();
				for (int i = 0; i < path.nodeCount(); i++) {
					Node n = path.getNode(i);
					if (n instanceof TrajSwitch) {
						TrajSwitch ts = (TrajSwitch) n;
						ts.setSelected(true);
						
					}
				}
			}	 
			else
				TrajectoryEventManager.clearSelection();
		}
		catch (Exception ex) {
		}
    }
}