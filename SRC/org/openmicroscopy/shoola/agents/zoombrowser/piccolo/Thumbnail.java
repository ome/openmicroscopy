/*
 * org.openmicroscopy.shoola.agents.zoombrowser.piccolo.Thumbnail
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
import java.awt.Image;

//Third-party libraries
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

//Appliaction-internal dependencies
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserImageSummary;
import org.openmicroscopy.shoola.agents.events.ViewImageModuleExecutions;
import org.openmicroscopy.shoola.agents.events.datamng.ViewImageInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.rnd.events.LoadImage;
import org.openmicroscopy.shoola.util.ui.Constants;
import org.openmicroscopy.shoola.util.ui.piccolo.BufferedObject;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericEventHandler;
import org.openmicroscopy.shoola.util.ui.piccolo.MouseableNode;



/** 
 * A node for displaying a thumbnail of an OME IMAGe
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public class Thumbnail extends PImage implements BufferedObject, 
	BrowserNodeWithToolTip, MouseableNode {

	private final static String DEFAULT_LABEL="No Thumbnail";
	
	private BrowserImageSummary image;	
	
	
	private PPath highlightRect=null;
	
	public Thumbnail(BrowserImageSummary image) {
		super();
	//	setAccelerated(false);
		this.image=image;
		
		Image im = image.getThumbnail();
		if (im !=  null)
			setImage(im);
		
		image.addThumbnail(this);
	}
	

	
	/**
	 * Buffered Bounds of the thumbnail, as required by {@link BufferedObject}.
	 * Buffer must be scaled to
	 * account for scaling of the node
	 * 
	 * @return buffered bounds
	 */	
	public PBounds getBufferedBounds() {
		PBounds b = getGlobalFullBounds();
		return new PBounds(b.getX()-Constants.SMALL_BORDER*getGlobalScale(),
			b.getY()-Constants.SMALL_BORDER*getGlobalScale(),
			b.getWidth()+2*Constants.SMALL_BORDER*getGlobalScale(),
			b.getHeight()+2*Constants.SMALL_BORDER*getGlobalScale());
	}
	
	
	/**
	 * To turn the highlight for this image on or off, tell the associated
	 * {@link BrowserImageSummary} to highlight all of its thumbnails. This will cause
	 * thumbnails of this image in other datasets to be similarly highlighted.
	 * 
	 * @parm v true if highlighting, false if not.
	 */
	public void setHighlighted(boolean v) {
	
		// Show highlighted path.
		if (v == true) {
			if (highlightRect == null) 
				highlightRect = makeHighlight();
			addChild(highlightRect);
		}
		else {
			if (highlightRect != null && isAncestorOf(highlightRect))
					removeChild(highlightRect);
			//highlightRect = null;
		}
		image.highlightThumbnails(v);
	}
	
	private PPath makeHighlight() {
		 PBounds b = getBounds();
		 PPath path = new PPath(b);
		 path.setStroke(Constants.THUMBNAIL_HIGHLIGHT_STROKE);
		 path.setStrokePaint(Constants.SELECTED_HIGHLIGHT_COLOR);
		 path.setPickable(false);
		 return path;
 	}

	/**
	 * The full tooltip for a thumbnail contains both a scaled version of the
	 * thumbnail image and the name of the image.
	 */
	public PNode getFullToolTip() {
		PNode n = new PNode();
		Image im = getImage();
		PImage imNode = new PImage(); // had as second arg,false);
		imNode.setImage(im);
		n.addChild(imNode);
		PPath p = new PPath();
		//PText text  = new PText(image.getName());
		//text.setFont(Constants.TOOLTIP_FONT);
		PNode text = getShortToolTip();
		p.addChild(text);
		n.addChild(p);
		p.moveToBack();
		p.setPathTo(text.getBounds());
		p.setPaint(Constants.TOOLTIP_FILL_COLOR);
		p.setStrokePaint(Constants.TOOLTIP_BORDER_COLOR);
		p.setBounds(p.getUnionOfChildrenBounds(null));
		p.setOffset(0.0,imNode.getHeight());
		n.setPickable(false);
		return n;
	}
	
	/**
	 * The shorter tooltip contains simply the name of the image.
	 */
	public PNode getShortToolTip() {
		PText text  = new PText(image.getName());
		text.setFont(Constants.TOOLTIP_FONT);
		text.setPickable(false);
		return text;
	}
	
	/**
	 * To get the buffered parent node  for a thumbnail, go up the chain 
	 * of ancenstors until a {@link BufferedObject} is found.
	 * 
	 * @return the first {@link BufferedObject} in the ancestros, or null. 
	 */
	public BufferedObject getBufferedParentNode() {
		
		PNode parent=this.getParent();
		while (parent != null  && !(parent instanceof BufferedObject))
			parent = parent.getParent();
		return (BufferedObject) parent;
		
	}
	
	/**
	 * These objects will generally be used underneath
	 * {@link DatasetImagesNode} objects.  However, they are grandchildren
	 * of the {@link DatasetImagesNode} objects. Use this relationship to find our 
	 * way back to the {@link DatasetImagesNode}
	 */
	public DatasetImagesNode getDatasetImagesNode() {
		PNode parent = getParent();
		if (parent == null)
			return null;
	
		parent = parent.getParent();
		if (parent == null)
			return null; // case a also

		if (!(parent instanceof DatasetImagesNode))
			return null; // case a, yet again
	
		DatasetImagesNode pin = (DatasetImagesNode) parent;
		return pin;
	}
	
 	/**
 	 * Called to turn the highlight of this thumbnail on or off. 
 	 * Finds the appropriate {@link DatasetImagesNode} and calls 
 	 * the appropriate procedure for turning the highlight on or off.
 	 * @param v true if highlighted. else false. 
 	 * 	See {@link DatasetImagesNode}  for descriptions of these levels
 	 */
	private void setZoomingHalo(boolean v) {
		//	ok. if I'm under a pdatasetimages node, setup the halo
		DatasetImagesNode pin = getDatasetImagesNode();
		if (pin != null)
		 	pin.highlightThumbnail(this,v);
	} 	
	
	/**
	 *  Called on mouse events from {@link DatasetBrowserEventHandler},
	 *  to set the highlight on this node and then the halo.
	 * @param v
	 */
	
	private boolean haloHighlighted = false;
	public void setHighlightedWithHalo(boolean v) {
		if (v != haloHighlighted) {
			setHighlighted(v);
			setZoomingHalo(v);
			haloHighlighted = v;
		}
	}
	
	/**
	 * When the user clicks on a thumbnail, the thumbnail will be zoomed to the 
	 * next greater level of magnification
	 *  
	 */
	public void zoomInToHalo() {
		DatasetImagesNode pin = getDatasetImagesNode();
		if (pin != null)
			pin.zoomInToHalo(this);	
	}
	
	/**
	 * When the user right-clicks on a thumbnail, the thumbnail will be 
	 * zoomed to the next lower level of magnification
	 * 
	 */
	public void zoomOutOfHalo() {
		DatasetImagesNode pin = getDatasetImagesNode();
		if (pin != null)
			pin.zoomOutOfHalo(this);
	}
	
	public void mouseEntered(GenericEventHandler handler,PInputEvent e) {
		if (haloHighlighted == false)
			setHighlightedWithHalo(true);
	}
	
	public void mouseExited(GenericEventHandler handler,PInputEvent e) {
		if (haloHighlighted == true)
			setHighlightedWithHalo(false);
	}
	
	public void mouseClicked(GenericEventHandler handler,PInputEvent e) {
		zoomInToHalo();
	}
	
	public void mousePopup(GenericEventHandler handler,PInputEvent e) {
		zoomOutOfHalo();
	}
	
	public void mouseDoubleClicked(GenericEventHandler handler,PInputEvent e) {
	}
	
	public void viewImage(Registry registry) {
		int id = image.getID();
		int[] pxSets = image.getPixelsIDs();
		String name =image.getName();
		LoadImage request = new LoadImage(id, pxSets[0], name);
		registry.getEventBus().post(request);	
	}
	
	public void viewImageInfo(Registry registry) {
		ViewImageInfo request  = new ViewImageInfo(image);
		registry.getEventBus().post(request);
	}
	
	public void viewImageModuleExecutions(Registry registry) {
		ViewImageModuleExecutions request  = new ViewImageModuleExecutions(image);
		registry.getEventBus().post(request);
	}
}