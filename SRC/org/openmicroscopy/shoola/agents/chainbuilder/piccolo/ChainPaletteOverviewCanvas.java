/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ChainPaletteOverviewCanvas;
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
import java.awt.Color;

//Third-party libraries
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.Constants;
import org.openmicroscopy.shoola.util.ui.piccolo.BufferedCanvas;


/** 
 * A {@link PCanvas} that will hold the overview of zoomed chain palette 
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

public class ChainPaletteOverviewCanvas extends BufferedCanvas  {

	private ChainPaletteCanvas detail;
	private PPath viewRect;
	
	public ChainPaletteOverviewCanvas() {
		super();
		setBackground(Constants.CANVAS_BACKGROUND_COLOR);
		//		 make sure that rendering is always high quality
		setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);	
		setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		
	}
	
	public void setDetailCanvas(ChainPaletteCanvas detail) {
		this.detail = detail;
		removeInputEventListener(getZoomEventHandler());
		removeInputEventListener(getPanEventHandler());
		
		detail.setOverview(this);
		PLayer detailLayer = new PLayer();
		detailLayer.addChild(detail.getLayer());
		getLayer().addChild(detailLayer);
		viewRect = new PPath();
		viewRect.setPaint(new Color(200,0,0,100));
		getLayer().addChild(viewRect);
		viewRect.moveToFront();
		
		addInputEventListener(new ChainPaletteOverviewDragEventHandler(this));
		
	}
	
	public PLayer getDetailLayer() {
		return detail.getLayer();
	}
	
	public void setScale() {
	//	double ratio  = ((double) ChainPaletteOverviewWindow.SIDE)/
		//	((double)ModulePaletteWindow.SIDE);
		getCamera().animateViewToCenterBounds(getLayer().getGlobalFullBounds(),
					true,0);
	}
	
	public void updateOverview(PBounds b) {
		viewRect.setOffset(0,0);
		viewRect.setPathTo(b);
		viewRect.setVisible(true);
	}
	
	public void updateMainView() {
		detail.panView(viewRect.getGlobalBounds());
	}
	
	public void hideOverview() {
		viewRect.setVisible(false);
	}
	
	public PPath getViewRect() {
		return viewRect;
	}
	
	public void mouseClick(PInputEvent e) {
		detail.overviewClick(e);
	}
	
	public void mousePopup(PInputEvent e) {
		detail.overviewPopup(e);
	}
}