/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ChainPaletteOverviewDragEventHandler;
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

//Java Imports
import java.awt.geom.Rectangle2D;

//Third-party libraries


import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolo.event.PDragEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

//Application-internal dependencies


/** 
 * An event handler for dragging around the overview canvas.
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public class ChainPaletteOverviewDragEventHandler extends PDragEventHandler  {

	private static final double BOUNDARY=30;
	private Rectangle2D overBounds;
	private PPath viewRect;
	private ChainPaletteOverviewCanvas overviewCanvas;
	private PCamera camera;
	
	public ChainPaletteOverviewDragEventHandler
		(ChainPaletteOverviewCanvas overviewCanvas) {
		super();
		this.overviewCanvas = overviewCanvas;
		camera = overviewCanvas.getCamera();
		viewRect =overviewCanvas.getViewRect();
	}


	
	protected void drag(PInputEvent event) {

		PDimension d = event.getDeltaRelativeTo(event.getPickedNode());
		
		PBounds b = viewRect.getGlobalBounds();
		double myX = b.getX()+d.getWidth();
		double myY = b.getY()+d.getHeight();
		double right = myX+b.getWidth();
		double bottom = myY+b.getHeight();
		
		double xdiff=d.getWidth();
		double ydiff=d.getHeight();
		
		PBounds cbounds = overviewCanvas.getDetailLayer().getGlobalFullBounds();
		
		
		// move as far as we can.
		if (myX > cbounds.getWidth()-BOUNDARY)
			xdiff = cbounds.getWidth()-BOUNDARY-b.getX();
		else if ( right < cbounds.getX()+BOUNDARY)
			xdiff = cbounds.getX()+BOUNDARY-b.getX()-b.getWidth();
		
		if  (myY >cbounds.getHeight()-BOUNDARY)
			ydiff = cbounds.getHeight()-BOUNDARY-b.getY();
		else if (bottom < cbounds.getY()+BOUNDARY)
			ydiff = cbounds.getY()+BOUNDARY-b.getY()-b.getHeight();
		d.setSize(xdiff,ydiff);
		
		
		
		// do something 
		viewRect.localToParent(d);
		viewRect.offset(d.getWidth(), d.getHeight());
		overviewCanvas.updateMainView();
	}
}