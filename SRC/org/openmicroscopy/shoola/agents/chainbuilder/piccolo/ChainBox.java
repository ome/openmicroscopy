/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ChainBox;
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
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

//Third-party libraries
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutChainData;
import org.openmicroscopy.shoola.agents.events.SelectAnalysisChain;
import org.openmicroscopy.shoola.agents.events.MouseOverAnalysisChain;

import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.Constants;
import org.openmicroscopy.shoola.util.ui.piccolo.BufferedObject;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericBox;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericEventHandler;
import org.openmicroscopy.shoola.util.ui.piccolo.MouseableNode;


/** 
 * A subclass of {@link PCategoryBox} that is used to provide a colored 
 * background for {@link PChain} widgets in the {@link PChainLibraryCannvas}
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */
public class ChainBox extends GenericBox implements MouseableNode 
		/*,SelectionEventListener*/ {
	

	/**
	 * The size of an orthogonal side of the lock icon
	 */
	public static final int SIZE_LENGTH=50;
	
	public static final double MAX_NAME_SCALE=3;
	

	
	private LayoutChainData chain;
	
	
	private static final float VGAP=10;
	private static final float HGAP=20;
	
	private PText name;
	
	float x =0;
	float y = 0;
	
	private PLayer chainLayer;
	
	/** the chain being represented */
	private ChainView chainView;
	
	/** The OME registry */
	private Registry registry;
	
	public ChainBox(LayoutChainData chain,Registry registry) {
		super();
		this.chain = chain;
		this.registry = registry;
		
		chainLayer = new PLayer();
		addChild(chainLayer);
		chainLayer.setPickable(false);
		
	
		// add name
		name = new PText(chain.getName());
		name.setGreekThreshold(0);
		name.setFont(Constants.LABEL_FONT);
		name.setPickable(false);
		name.setScale(MAX_NAME_SCALE);
		chainLayer.addChild(name);
		name.setOffset(HGAP,VGAP*3);
		
		double width = name.getGlobalFullBounds().getWidth();
		//		 one VGAP below + 3 above
		y = (float) (name.getGlobalFullBounds().getHeight()+VGAP*2); 
		
		addLockedIndicator();
		
		// add ower name
		PText owner = new PText(chain.getOwner());
		owner.setFont(Constants.LABEL_FONT);
		owner.setPickable(false);
	
		
		chainLayer.addChild(owner);
		owner.setOffset(x+HGAP,y+VGAP);
		y += owner.getHeight()+VGAP*5;
		
		// build the chain..
		chainView = new PaletteChainView(chain,registry);
		
		chainLayer.addChild(chainView);
		chainView.setOffset(HGAP*2,y);
		y += chainView.getHeight()+VGAP; 

		//		 find width. use it in layout of datasets/executions..
		if (chainView.getWidth() > width)
			width = chainView.getWidth();	
		setExtent(width+HGAP*2,y);
		invalidateFullBounds();
	}
	
	/**
	 * 
	 * @return the ID of the chain stored in the box
	 */
	public int getChainID() {
		return chain.getID();
	}
	
	/**
	 * @return the chain stored in the box
	 * 
	 */
	public LayoutChainData getChain() {
		return chain;
	}
	
	public ChainView getChainView() {
		return chainView;
	}
	
	/*public void setExtent(double width,double height) {
		super.setExtent(width,height);
		// add a triangle in the corner.
		if (chain.getIsLocked()) {
			addLockedIndicator();
		}
	}*/
		
	public void centerChain() {
		// get my extent
		PBounds b = getBounds();
		double boxWidth = b.getWidth();
		double boxHeight = b.getHeight();
		
		// get chain's extent
		double chainWidth = chainView.getWidth();
		double chainHeight = chainView.getHeight();
		// set offset of chain to be in center.
		
		// topmost part of where chain goes
		float y = (float) (name.getGlobalFullBounds().getHeight()+VGAP*2);
		
		double boxVertRange = boxHeight -y;
		
		// width
		double xCenter = boxWidth/2;
		double newX = xCenter-chainWidth/2;
		
		// height
		double yCenter = y+boxVertRange/2;
		double newY = yCenter-chainHeight/2;
		chainView.setOffset(newX,newY);
	}
	
	private void addLockedIndicator() {
		//PBounds b = getFullBoundsReference();
		PText locked = new PText("Locked");
		locked.setGreekThreshold(0);
		locked.setFont(Constants.LABEL_FONT);
		locked.setTextPaint(Constants.LOCKED_COLOR);
		locked.setScale(1.5);
		locked.setPickable(false);
		chainLayer.addChild(locked);
		//PBounds lockedBounds = locked.getGlobalFullBounds();
		//float x = (float) (b.getX()+b.getWidth()-lockedBounds.getWidth()-HGAP);
		locked.setOffset(HGAP,y);
		y += locked.getHeight();
	}
			
	

	public void mouseClicked(GenericEventHandler handler) {
		((ChainPaletteEventHandler) handler).animateToNode(this);
		((ChainPaletteEventHandler) handler).setLastEntered(chainView);
		((ChainPaletteEventHandler) handler).hideLastChainView();
		SelectAnalysisChain event = new SelectAnalysisChain(chainView.getChain());
		registry.getEventBus().post(event); 
	}
	
	public PBounds getBufferedBounds() {
		PBounds b = getGlobalBounds();
		
		PBounds p=  new PBounds(b.getX()-2*Constants.BORDER,
				b.getY()-2*Constants.BORDER,
				b.getWidth()+4*Constants.BORDER,
				b.getHeight()+4*Constants.BORDER);
		
		return p;
	}

	public void mouseDoubleClicked(GenericEventHandler handler) {
		PNode node = ((PaletteChainView)chainView).getFullView();
		PBounds b = node.getFullBounds();
		BufferedImage image = (BufferedImage) node.toImage((int) (b.getWidth()*7),
					(int) (b.getHeight()*7),Constants.CANVAS_BACKGROUND_COLOR);
		try { 
			ImageIO.write(image,"png",new File("foo.png"));
			System.err.println("Saved chain snapshot");
		} catch (Exception e) {
			System.err.println("Failed to save chain snapshot");
			
		}
	}

	public void mouseEntered(GenericEventHandler handler) {
		((ChainPaletteEventHandler) handler).setLastHighlighted(this);
		setHighlighted(true);
		MouseOverAnalysisChain event = 
			new MouseOverAnalysisChain(chainView.getChain());
		registry.getEventBus().post(event);
	}

	public void mouseExited(GenericEventHandler handler) {
		((ChainPaletteEventHandler) handler).setLastHighlighted(null);
		((ChainPaletteEventHandler) handler).hideLastChainView();
		setHighlighted(false);
		MouseOverAnalysisChain event = 
			new MouseOverAnalysisChain(null);
		registry.getEventBus().post(event);
	}

	public void mousePopup(GenericEventHandler handler) {
		System.err.println("right click on chain box");
		PNode p = getParent();
		if (p instanceof BufferedObject)  
			((ChainPaletteEventHandler) handler).animateToNode(p);	
		else
			((ChainPaletteEventHandler) handler).animateToCanvasBounds();
		((ChainPaletteEventHandler) handler).hideLastChainView();
	}	
		
}