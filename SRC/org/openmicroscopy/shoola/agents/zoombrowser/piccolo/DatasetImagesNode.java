/*
 * org.openmicroscopy.shoola.agents.zoombrowser.piccolo.DatasetImagesNode
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


/*------------------------------------------------------------------------------
 *
 * Written by:    Harry Hochheiser <hsh@nih.gov>
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.zoombrowser.piccolo;


//Java imports
import java.util.ArrayList;
import java.util.Iterator;

import org.openmicroscopy.shoola.util.ui.piccolo.BufferedObject;

//Third-party libraries
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;


//Application-internal dependencies

/**
 * 
 * A node for images in a dataset. Renders either the thumbnails 
 * of images in the dataset,  or an image of the thumbnail grid layout, 
 * depending on the scale. If the view is zoomed in to display the images up 
 * close, or if the dataset is selected - via click or mouse over -
 * individual thumbnails will be displayed. This display will allow users to 
 * mouse over thumbnails, see tooltips, and zoom in via a 
 * {@link ThumbnailSelectionHalo}, When datasets are shown at lower 
 * magnifications, or are deselected, an single image - essentially a screen
 * shot of the view containing all of the images - is displayed instead.
 * This simplifies rendering (one image vs. n thumbnails) in those situtations 
 * where rendering all of the thumbnails would be cost-prohibitive and 
 * not particularly useful.
 * 
 * 
 * To support the zooming via the {@link ThumbnailSelectionHalo}, this node also
 * queries the thumbnail nodes to derive the bounds of the halo, and updates 
 * those bounds as the view zooms in and out.
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
public class DatasetImagesNode extends PNode  {

	/**
	*  minimum number of images that a dataset must have before an icon is used
	*/
 	private static final int MIN_ICON_DATASET_SIZE=100;
 	
 	/**
	* Threshold below which we'll show the icon instead of the actual thumbnails.
	*/
 	private static final double SCALE_THRESHOLD=.75;
 		
	/**
	 * Parent node for thumbnails
	 */
	private PNode imagesNode = new PNode();
	
	/**
	 * An image that will take the place of thumbnails as we zoom out
	 */
	private PImage thumbnailNode = null;
		
	/**
	 * flag indicating when we are selected.
	 */
	private boolean selected = false;
	
	/**
	 * the halo
	 */
	private ThumbnailSelectionHalo zoomHalo = new ThumbnailSelectionHalo();
	
	
	/**
	 * A list of sizes of the rows. To be fully general, we must account 
	 * for the possibility that rows in the thumbnail display will have 
	 * differing numbers of items. In practice, this is unlikely to be the case,
	 * because the layout scheme in {@link DatasetNode} enforces common
	 * horizontal spacing of nodes, thus guaranteeing regular columns and column
	 * widths (for all but the last column). Thus, this list could probably be
	 * replaced by a single integer, but we maintain it for generality.
	 * 
	 */
	private ArrayList rowSzs = new ArrayList();
	
	/**
	 * The row and column of the highlighted thumbnail
	 */
	private int highlightRow;
	private int highlightColumn;
	
	/**
	 * a cache of highlighted thumbnail, to avoid re-highlighting
	 */
	private Thumbnail currentHighlight;
	
	/**
	 * The event handler for the canvas that this node is on
	 */
	private DatasetBrowserEventHandler handler;
	
	public DatasetImagesNode() {
		super();	
		addChild(imagesNode);
		// the zoom halo is a separate child, rendered in front of other nodes.
		addChild(zoomHalo);
		zoomHalo.moveToFront();
		setPickable(true);
	}
	
	
	public void setHandler(DatasetBrowserEventHandler handler) {
		this.handler = handler;
	}
	
	/**
	 * Add a thumbnail
	 * @param thumb
	 */
	public void addImage(Thumbnail thumb) {
		imagesNode.addChild(thumb);
	}
	
	/**
	 * Add the size of a new row
	 */
	public void setRowCount(int row,int sz) {
		rowSzs.add(row,new Integer(sz));
	}
	
	/**
	 * Iterator for all of the thumbnails
	 */
	public Iterator getImageIterator() {
		return imagesNode.getChildrenIterator();
	}
	
	/**
	 * When all of the thumbnails for the dataset have been added, 
	 * create an icon image add add it to this node.
	 * 
	 * @param width
	 * @param height
	 */
	public void completeImages() {
		PBounds b = imagesNode.getGlobalFullBounds();
		// only do this if the images have non-zero bounds and there are
		// more than MIN_ICON_DATSET_SIZE images
		if (b.getWidth() > 0 && b.getHeight() > 0 &&
						imagesNode.getChildrenCount() > MIN_ICON_DATASET_SIZE) {
                  
			// use {@link PNode.toImage} to get a snapshot of the thumbnails.       
			thumbnailNode = new PImage(imagesNode.toImage((int)b.getWidth(),
							(int) b.getHeight(),null),false);
			addChild(thumbnailNode);
			moveToBack(thumbnailNode);
		}
	}
        
        
	/**
	 * If there is no icon node, this node is selected, or we're zoomed in
	 * to a scale greater than SCALE_THRESHOLD, hide the icon image and show 
	 * individual thumbnails.
	 * 
	 * Otherwise, show the icon image.
	 *
	 */
	public void paint(PPaintContext aPaintContext) {
                
		if (thumbnailNode == null || selected == true || 
						aPaintContext.getScale() > SCALE_THRESHOLD){
			if (thumbnailNode != null) {
							thumbnailNode.setVisible(false);
							thumbnailNode.setPickable(false);
			}
			imagesNode.setVisible(true);
			setPickable(true);
		}
		else {
	                
			// show images node
			imagesNode.setVisible(false);
			if (thumbnailNode != null) {
							thumbnailNode.setVisible(true);
							thumbnailNode.setPickable(true);        
			}
				setPickable(false);
		}
		super.paint(aPaintContext);
	}	
	
	public void setSelected(boolean v) {
		selected = v;
	}
	
	/**
	 * Turn the highlight on or off for a thumbnail at a given zoom level
	 * @param thumb the thumbnail to be highlighted (or not)
	 * @param v true if highlight on, else false
	 * @param level the zoom level
	 */
	public void highlightThumbnail(Thumbnail thumb,boolean v) {
		// magnification level of the canvas. How many times have 
		// we already zoomed in?
		int level = handler.getZoomLevel();
		// # of children we're dealing with
		int count = imagesNode.getChildrenCount();
		
		// the radius of the halo at the given magnification level		
		int radius = getRadius(level);
		// # of items that would fall within the halo
		int size = radius*radius;
	
		zoomHalo.hide();
		
		// if flag is false or if the # of items that would fall within the halo
		// is more than the size of the dataset, don't do anything
		if (v == false || count < size || radius <0) { 
			currentHighlight = null;
		}
		else  if (thumb != currentHighlight) {
			// If this thumbnail is not  not already highlighted,
			// calculate the bounds and show the halo
			doHighlightThumbnail(thumb,radius);
			zoomHalo.show();
			currentHighlight = thumb;
		}
	}
	
	/**
	 * For zoom level i, the radius of the halo'd region will be 
	 * side/2^(level+2), where side=sqrt(# of thumbnails). This provides
	 * a roughly exponential decrease in the size of the halo with each zoom-in
	 *  
	 * @param level
	 * @return the radius for the halo at that level
	 */
	private int getRadius(int level) {
		int count = imagesNode.getChildrenCount();
		// find the number of items on each side
		double side = Math.sqrt(count);
		double denom = Math.pow(2,level+2);
		return (int) Math.floor(side/denom);
	}
	
	/** 
	 * To highlight a thumbail, get the bounds,  and update the halo path
	 */
	private void doHighlightThumbnail(Thumbnail thumb,int radius) {
		// get index
		PBounds b  = getHaloBounds(thumb,radius);
		
		zoomHalo.setPathTo(b);
	}
	
	
	/**
	 * To get the halo bounds, find the index of the node in the list of 
	 * children and build highlight bounds around it
	 * @param thumb
	 * @param radius
	 * @return bounds of the highlight of the given radius around thumb
	 */
	private PBounds getHaloBounds(Thumbnail thumb,int radius) {
		int index = imagesNode.indexOfChild(thumb);
		PBounds b = getHighlight(index,radius);
	
		return b;
	}
	
	/**
	 * A shortcut to get the size of a row
	 * @param i a row index
	 * @return # of items in row i.
	 */
	private int getRowSize(int i) {
		Integer v = (Integer) rowSzs.get(i);
		return v.intValue();
	}
	
	/**
	 * To build the highlight, iterate over the range implied by the radius
	 * and add everythin in that region
	 * 
	 * @param index
	 * @param radius
	 * @return halo bounds around item index of the given radius
	 */
	private PBounds getHighlight(int index,int radius) {
		calculatePosition(index);
		PBounds b = new PBounds();
		
		//	build up bounds of zoomhalo
		int lowRow = highlightRow-radius;
		int highRow = highlightRow+radius;
		
		int lowCol = highlightColumn-radius;
		int highCol = highlightColumn+radius;
		
		for (int i = lowRow; i<=highRow; i++) {
			for (int j = lowCol; j <= highCol; j++) {
				addToHighlight(b,i,j);
			}
		}
		
		return b;
	}
	
	/**
	 * Find the row and column of the item being highlighted. 
	 * 
	 * @param index
	 */
	private void calculatePosition(int index) {
		int curRowSize;
		int curRow = 0;

		// since rows may be of different sizes, this isn't just simple
		// array indexing. iterate over rows until I find the index i'm looking
		// for 
		curRowSize =getRowSize(curRow);
		while (index >= curRowSize && curRow < rowSzs.size()) {
			index -= curRowSize;
			curRow++;
			curRowSize = getRowSize(curRow);
		}
		
		// set these vars to hold my current position
		highlightRow = curRow;
		highlightColumn = index; // whatever is left over is column
	}

	/**
	 * To add a position to the highlight, make sure the position is not out of
	 * bounds and then add its bounds.
	 * @param b
	 * @param row
	 * @param col
	 */	
	private void addToHighlight(PBounds b,int row,int col) {
		if (row <0 || row >= rowSzs.size())
			return;
		
		int curRowSize = getRowSize(row);
		if (col <0 || col >= curRowSize)
			return;
		int index = getThumbIndex(row,col);
		Thumbnail thumb = (Thumbnail) imagesNode.getChild(index);
		PBounds tBounds = thumb.getFullBoundsReference();
		b.add(tBounds);
	}
	
	/**
	 * More or less the inverse of the 
	 * {@link #calculatePosition calculatePosition} call. find the position of 
	 * the item at row, col in the node's children
	 */
	private int getThumbIndex(int row,int col) {
		int index = 0;
		for (int i = 0; i < row; i++) {
			index +=getRowSize(i);
		}
		
		return index+col;
	}
	
	
	/**
	 * Zoom in to a thumbnail at a given level
	 * @param thumb
	 * @param level
	 */
	public void zoomInToHalo(Thumbnail thumb) {
	
		// get the zoom level	
		int level = handler.getZoomLevel();
	
		
		zoomHalo.hide();
		int radius = getRadius(level);
	
		//	calculate the current halo
		doHighlightThumbnail(thumb,radius);
		
		  
	   	//zoom to it.
		handler.animateToNode(zoomHalo);
		// update the level, If the current radius is zero and the
		// next radius would be zero, then I have just zoomed in to a single
		// node. In that case, don't advance level
		if (radius >0  || getRadius(level+1) > 0)
			level++;
	    //update the zoom level
	    handler.setZoomLevel(level);	
	 }
	
	/**
	 * Zoom out from a thumbnail at a given level. This code has a slight 
	 * idiosyncracy: when the user is zoomed into the lowest level (viewing 
	 * an individual thumbnail close-up), the zoom out will be to the view where
	 * the radius is one - effectively skipping the radius =0 view. However, 
	 * given that that view still has a fair amount of detail, this may actually
	 * be beneficial, as it will give users some navigational control without 
	 * zooming out too far. If this proves confusing to users, we may wish to 
	 * reconsider.
	 * 
	 * @param thumb
	 * @param level
	 * @return the new level
	 */
	public void zoomOutOfHalo(Thumbnail thumb) {
		// get the zoom level
		int level = handler.getZoomLevel();
		if (level <= 1) {
			// go to top level.
			BufferedObject b = thumb.getBufferedParentNode();
			// zoom to the dataset
			handler.animateToBufferedObject(b);
		}
		else {
			// go up by two
			int upperLevel = level-2;
			int radius = getRadius(upperLevel);
			if (radius > 0) {
				// and calculate the halo for that level
				zoomHalo.hide();
				doHighlightThumbnail(thumb,radius);
				// zooming into it. This has the net effect of zooming us out 
				// by one step.
				handler.animateToNode(zoomHalo);
			}
		}	
			
		// adjust level
		level--;
		if (level < 0)
			level = 0;
		handler.setZoomLevel(level);		
	}
}
