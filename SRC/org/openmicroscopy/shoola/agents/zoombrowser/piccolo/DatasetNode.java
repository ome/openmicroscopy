/*
 * org.openmicroscopy.shoola.agents.zoombrowser.piccolo.DatasetNode
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
import java.util.Collection;
import java.util.Iterator;

//Third-party libraries
import edu.umd.cs.piccolo.util.PBounds;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserDatasetData;
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserImageSummary;
import org.openmicroscopy.shoola.util.ui.Constants;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericBox;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericEventHandler;
import org.openmicroscopy.shoola.util.ui.piccolo.MouseableNode;


/** 
 * A subclass of {@link CategoryBox} that is used to provide a colored 
 * background to the display of images in a dataest 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */
 

public class DatasetNode extends GenericBox implements MouseableNode {
	
	/**
	 * The dataset of interest
	 */
	private BrowserDatasetData dataset;
		
	/** 
	 * Positional coordinates
	 */
	private double x=Constants.DATASET_IMAGE_GAP;
	private double y=Constants.DATASET_IMAGE_GAP;
	
	/** 
	 * Name and chains for the datasets
	 */
	private ScalableDatasetLabel nameLabel;
//	private ChainLabels chainLabels;
	

	/**
	 * Width and height values,
	 *  with prevWidth so we can revert - needed to create treemap layout.
	 * see {@link PBrowserCanvas}
	 */
	private double prevWidth;
	private double width;
	private double height = 0;
	
	/**
	 * The node that holds the thumbnails
	 */
	private DatasetImagesNode images=null;
	
	/**
	 * The max width of any row
	 */
	double maxWidth = 0;
	
	/** 
	 * Max width of any thumbnail. Used to guarantee aligned columns
	 */
	
	/** THE canvas that we're on */
	private DatasetBrowserCanvas canvas;
	
	/** The calculated maximum width of a thumbnail */
	private double maxThumbWidth = 0;
	
	public DatasetNode(BrowserDatasetData dataset,DatasetBrowserCanvas canvas) {
		super();
		this.dataset = dataset;
		this.canvas = canvas;
		dataset.setNode(this);
	}

	/** 
	 * The main procedure for laying out the images thumbnails
	 *
	 */
	public void layoutImages() {
		
		removeAllChildren();
		// initial starting point
		double x=Constants.DATASET_IMAGE_GAP;
		double y= Constants.DATASET_IMAGE_GAP;
		
		// add the name label and move down.
		nameLabel = new ScalableDatasetLabel(dataset,width);
		addChild(nameLabel);
		nameLabel.setOffset(x,y);
		y+= nameLabel.getBounds().getHeight()+Constants.DATASET_IMAGE_GAP;
		
		
		
		Collection imageCollection = dataset.getImages();
		double totalArea = buildImages(imageCollection,x,y);
	
		// calculate remaining height
		double effectiveHeight = height -y;
		double effectiveWidth = width;
		
		// account for chain exections..	
/*		Collection chains = dataset.getChains(connection);
		if (chains.size() > 0) {
			double h = buildChainLabels(chains);
			effectiveHeight -= h+2*Constants.DATASET_IMAGE_GAP;
		} */
			
		// find the scaled area
		double scaledArea = effectiveWidth*effectiveHeight;
		double scalefactor = Math.sqrt(totalArea/scaledArea);
		
		// this is the scaled width and height that we must fit into
		double scaledWidth = scalefactor*effectiveWidth;
		double scaledHeight = scalefactor*effectiveHeight;
		
		// layout the images in this space
		if (imageCollection.size() > 0) {
			y = arrangeImages(scaledWidth,scaledHeight);
		}
	
		// update height
		if (y > scaledHeight) 
			scaledHeight = y;
		
		// calculate effective scale factor - compare available height
		// to what was used, and this gives us the effective scale factor.
		double scaleEffective = scaledHeight/effectiveHeight;
		
		// turn that scale into a scale ratio
		if (scaleEffective == 0 || imageCollection.size() == 0)
			scaleEffective = 1; 
		double scaleRatio = 1/scaleEffective;
		
		// adjust the max width
		maxWidth /=scaleEffective;
	
		
		// scale the node holding the thumbnail to by  that ratio
		if (images != null)
			images.setScale(scaleRatio);
			
		// if I have any chain executions, position the label
		/*if (chains.size() > 0)  {
			if (imageCollection.size() > 0) { 
				PBounds b= images.getGlobalFullBounds();
				y = b.getY()+b.getHeight()+Constants.DATASET_IMAGE_GAP;
			}
			else {
				y = Constants.DATASET_IMAGE_GAP+nameLabel.getBounds().getHeight()+Constants.DATASET_IMAGE_GAP;
			}
			chainLabels.setOffset(Constants.DATASET_IMAGE_GAP,y);
			// update the width if need be.
			if (maxWidth < chainLabels.getGlobalFullBounds().getWidth())
				maxWidth = chainLabels.getGlobalFullBounds().getWidth();
		}*/
		
		// if necessary, adjust width to hold the dataset's name label
		if (imageCollection.size() == 0 && 
			maxWidth < nameLabel.getBounds().getWidth())
			maxWidth = nameLabel.getBounds().getWidth();
			
		// adjust the name label to fit.
		nameLabel.resetWidth(maxWidth);
				
		setExtent(maxWidth+Constants.SMALL_BORDER,
				height+Constants.SMALL_BORDER);
	}
	
	/**
	 * To build the images, iterate over the colection, build thumbnails for 
	 * each, and add them to the {@link DatasetImagesNode}, calculating the 
	 * total area along the way
	 * 
	 * @param imageCollection images to be laid out
	 * @param x horiz coord of thumbnail node
	 * @param y vert coord of thumbnail node
 	 * @return area occupied
	 */
	private double buildImages(Collection imageCollection,double x,double y) {
		double totalArea = 0;
		Iterator iter;
		Thumbnail thumb;
		PBounds b;
		int rowCount = 0;
		if (imageCollection.size() > 0) { 
			// if there are images 
			images = new DatasetImagesNode();
			addChild(images);
	
			iter  = imageCollection.iterator();
			while (iter.hasNext()) {
				BrowserImageSummary image = (BrowserImageSummary) iter.next();
				thumb  =new Thumbnail(image);
				images.addImage(thumb);
				b = thumb.getGlobalFullBounds();
				totalArea += b.getWidth()*b.getHeight();
				// track the width of the widest thumbnail
				if (b.getWidth() > maxThumbWidth)
					maxThumbWidth = b.getWidth();
			}
			images.setOffset(x,y);
		}
		return totalArea;
	}
	
	/**
	 * Create chain labels, adjusting max widths and returning the heigh of 
	 * the label
	 * @param chains set of chains that have executions for this dataset.
	 * @return height of the label
	 */
	/*private double buildChainLabels(Collection chains) {
		chainLabels = new ChainLabels(chains);
		addChild(chainLabels);
			
		chainLabels.layout(width);
		PBounds b =chainLabels.getGlobalFullBounds();
		maxWidth = b.getWidth();
		return b.getHeight();
	} */
	
	/**
	 * Place the images in rows according to the provided constraints
	 * 
	 * @param scaledWidth
	 * @param scaledHeight
	 * @return
	 */
	private double arrangeImages(double scaledWidth,double scaledHeight) {
		double x=0;
		double y=0;
		double maxHeight = 0;
		Thumbnail thumb;
		PBounds b;
		int row = 0;
		int rowSz = 0;
		Iterator iter = images.getImageIterator();
		while (iter.hasNext()) {
			thumb = (Thumbnail) iter.next();
			b =  thumb.getGlobalFullBounds();
			double thumbWidth = b.getWidth();
			if (x+thumbWidth  < scaledWidth) {
				// place thumb on current row
				thumb.setOffset(x,y);
			}
			else {
				// move to next row
				y += maxHeight+Constants.DATASET_IMAGE_GAP;
				x = 0;
				if (rowSz > 0) {
					// finalize row statistics
					images.setRowCount(row,rowSz);
					row++;
					rowSz=0;
				}
				maxHeight = 0;
			}
			// update row size, place image, and update stats.
			rowSz++;
			thumb.setOffset(x,y);
			if (b.getHeight() > maxHeight) 
				maxHeight = (float)b.getHeight();
			x+= maxThumbWidth;
			if (x > maxWidth) 
				maxWidth =  x;
			x+= Constants.DATASET_IMAGE_GAP;
		}	
		// finalize last row and then complete the image node.
		images.setRowCount(row,rowSz);
		images.completeImages();
		y+= maxHeight;
		return y;
	}

	/**
	 * The "area" of the dataset is pseudo-logarithmic. For datasets with <=20
	 * items, the area is simply the number of items in the dataset. For larger
	 * datasets, the area is the log(datset_size-20).
	 * 
	 * The use of the log lets us handle a wide range of dataset sizes without
	 * too much trouble.
	 * 
	 * @return
	 */
	public double getContentsArea() {
		int count = dataset.getImageCount();
		double num =1;
		if (count > 20)
			num = 20+ Math.log(dataset.getImageCount()-20);
		else if (count >0)
			num = count;
		else 
			num =1;
			 
		return num; 
	}
	

	/**
	 * @return Returns the dataset.
	 */
	public BrowserDatasetData getDataset() {
		return dataset;
	}
	
	/**
	 * The buffered bounds, for zooming in
	 */
	public PBounds getBufferedBounds() {
		PBounds b = getFullBoundsReference();
		return new PBounds(b.getX()-Constants.SMALL_BORDER,
			b.getY()-Constants.SMALL_BORDER,
			b.getWidth()+2*Constants.SMALL_BORDER,
			b.getHeight()+2*Constants.SMALL_BORDER);
	}
	
	/**
	 * @return Returns the width.
	 */
	public double getWidth() {
		return width;
	}
	/**
	 * @param width The width to set.
	 */
	public void setWidth(double width) {
		prevWidth = this.width;
		this.width = width;
	}
	
	/**
	 * Revert back to a previously calculated width
	 *
	 */
	public void revertWidth() {
		width = prevWidth;
	}
	
	/**
	 * Set current and previous width to zero
	 *
	 */
	public void clearWidths() {
		width = prevWidth = 0;
	}
	
	public void setHeight(double height) {
		this.height = height;
	}
	
	/**
	 * scale the height and width. Used to finalize treemap layout. 
	 * see {@link PBrowserCanvas}
	 */
	public void scaleArea(double scale) {
		width *=scale;
		height *=scale;
	}
	
	/**
	 * Set the highlighted state: when i mouse in/out
	 * @param v true if highlighted, else false
	 */
	public void setHighlighted(boolean v) {
		super.setHighlighted(v);
		if (images !=null) 
			images.setSelected(v);
	}
		
	/**
	 * Set selected state - when something that I correspond to (like a chain exec)
	 * is highlighted..
	 * @param v
	 */
	public void setSelected(boolean v) {
		if (v == true) 
			setPaint(Constants.SELECTED_FILL);
		else 
			setPaint(null);
		repaint();
	}
	
	
	public void setHandler(DatasetBrowserEventHandler handler) {
		if (images != null)
			images.setHandler(handler);
	}
	
	public void mouseEntered(GenericEventHandler handler) {
		canvas.setRolloverDataset(dataset);
	}
	
	public void mouseExited(GenericEventHandler handler) {
		canvas.setRolloverDataset(null);
	}
	
	public void mouseClicked(GenericEventHandler handler) {
		DatasetBrowserEventHandler 
			dsHandler = (DatasetBrowserEventHandler) handler;
		if (dsHandler != null && dsHandler.getZoomLevel() == 0) {
			dsHandler.animateToNode(this);
			canvas.setSelectedDataset(getDataset());
		}
	}
	
	public void mousePopup(GenericEventHandler handler) {
		DatasetBrowserEventHandler 
			dsHandler = (DatasetBrowserEventHandler) handler;
		if (dsHandler != null)
			dsHandler.resetZoomLevel();
		canvas.setSelectedDataset(null);
	}
	
	public void mouseDoubleClicked(GenericEventHandler handler) {
	}
}
