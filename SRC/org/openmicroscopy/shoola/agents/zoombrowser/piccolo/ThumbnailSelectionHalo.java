/*
 * org.openmicroscopy.shoola.agents.zoombrowser.piccolo.ThumbnailSelectionHalo.java
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

package org.openmicroscopy.shoola.agents.zoombrowser.piccolo;

//Java imports
import java.awt.BasicStroke;

//Third-party libraries
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

//Application-internal dependencies

/** 
 * Popup halo path that can go around a PThumbnail or a group of PThumbnails
 * for zooming into a Dataset. Generally contained as a child of   
 * {@link PDatasetImagesNode}, this rect scales its stroke to give appearance
 * of a constant width (in terms of screen pixels) at any magnification
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

public class ThumbnailSelectionHalo extends PPath implements BufferedObject,
	MouseableNode {

	// leave on pixel on either side of border
	public static final int OFFSET=3;
	public static final float BASE_STROKE_WIDTH=PConstants.DATASET_IMAGE_GAP-
		OFFSET;
	

	public ThumbnailSelectionHalo() {
		super();
		setVisible(false);
		setPickable(false);
		setStrokePaint(PConstants.HALO_COLOR);	
	}
	
	public void hide() {
		setStatus(false);
	}
	
	public void show() {
		setStatus(true);
	}
	
	private void setStatus(boolean v) {
		setVisible(v);
		setPickable(v);
	}
	
	/**
	 * Return the bounds as defined in {@link PBufferedObject},  so we can zoom 
	 * into the halo while still leading a buffer.
	 * 
	 * Note that since this node is generally enclosed in other nodes, we need 
	 * multiply the spacing by the global scale. This is because of the 
	 * scaling used in {@link PDatasetImagesNode}, which scales thumbnails
	 * as needed to put a large set of thumbnails in a given space. Since that 
	 * may lead the halo to be displayed with a small scale, we need to multiply
	 * the buffer by that scale in order to avoid a disproportionately large 
	 * buffer.
	 * 
	 * @return the buffered bounds to zoom to
	 */	
	public PBounds getBufferedBounds() {
		PBounds b = getGlobalFullBounds();
		return new PBounds(b.getX()-PConstants.SMALL_BORDER*getGlobalScale(),
			b.getY()-PConstants.SMALL_BORDER*getGlobalScale(),
			b.getWidth()+2*PConstants.SMALL_BORDER*getGlobalScale(),
			b.getHeight()+2*PConstants.SMALL_BORDER*getGlobalScale());
	}
	
	/**
	 * To set that path to given bouds, add a border that is a scaled version
	 * of OFFSET, as needed to put the border outside the thumbnail(s) being 
	 * highlighted. As with {@link #getBufferedBounds getBufferedBounds}, 
	 * this spacing must be scaled.
	 * 
	 * @param b original bounds
	 */
	public void setPathTo(PBounds b) {
		double scale = getGlobalScale();
		double border =OFFSET*scale;
		PBounds b2 = new PBounds(b.getX()-border,b.getY()-border,
			b.getWidth()+2*border,b.getHeight()+2*border);
		super.setPathTo(b2);
	}
		
	/**
	 * To paint the halo, create a stroke with a width that is divided by the
	 * width of the {@link PPaintContext}, and call the superclass method. 
	 * This makes the width seem constant, independent of magnification.
	 * 
	 * @param aPaintContext the context for painting.
	 */
	public void paint(PPaintContext aPaintContext) {
		float scale = (float) aPaintContext.getScale();
		float strokeScale = BASE_STROKE_WIDTH/scale;
		setStroke(new BasicStroke(strokeScale));
		super.paint(aPaintContext);
	}
	
	/** The halo should not respond to any mouse events */
	public void mouseEntered(GenericEventHandler handler) {
	}
	
	public void mouseExited(GenericEventHandler handler) {
	}
	
	public void mouseClicked(GenericEventHandler handler) {
	}
	
	public void mousePopup(GenericEventHandler handler) {
	}
	
	public void mouseDoubleClicked(GenericEventHandler handler) {
	}
}