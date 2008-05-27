/*
 * org.openmicroscopy.shoola.env.data.views.calls.ThumbnailSetLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.views.calls;


//Java imports
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import pojos.ImageData;
import pojos.PixelsData;

/** 
 * Command to load a given set of thumbnails.
 * <p>As thumbnails are retrieved from <i>OMERO</i>, they're posted back to the 
 * caller through <code>DSCallFeedbackEvent</code>s. </p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ThumbnailSetLoader
	extends BatchCallTree
{

	/** The maximum number of images per call. */
	private static final int			MAX_PER_CALL = 10;
	
    /** Helper reference to the image service. */
    private OmeroImageService		service;
    
    /** The maximum acceptable length of the thumbnails. */
    private int						maxLength;
    
    /** Collection of list of pixels set to handle. */
    private List<List> 				toHandle;
    
    /** Key, value pairs, Key is the pixels set id. */
    private Map<Long, ImageData> 	input;
    
    /** Collection of {@link ThumbnailData}s for not valid pixels set. */
    private List 					notValid;
    
    /** Collection of current {@link ThumbnailData}s. */
    private Object				 	currentThumbs;
    
    /**
     * Creates a default thumbnail for the passed image.
     * 
     * @param data The image to handle.
     * @return See above.
     */
    private BufferedImage createDefaultImage(ImageData data) 
    {
    	PixelsData pxd = null;
        try {
        	pxd = data.getDefaultPixels();
		} catch (Exception e) {} //something went wrong during import
        if (pxd == null)
        	return Factory.createDefaultImageThumbnail();
        
        int sizeX = maxLength, sizeY = maxLength;
        double pixSizeX = pxd.getSizeX();
    	double pixSizeY = pxd.getSizeY();
        double ratio = pixSizeX/pixSizeY;
        if (ratio < 1) sizeX *= ratio;
        else if (ratio > 1 && ratio != 0) sizeY *= 1/ratio;
		return Factory.createDefaultImageThumbnail(sizeX, sizeY);
    }
    
    /**
     * Loads the thumbnail for passed collection of pixels set.
     * 
     * @param ids The collection of pixels set id.
     */
    private void loadThumbails(List ids) 
    {
    	try {
        	Map m = service.getThumbnailSet(ids, maxLength);
        	List result = new ArrayList();
        	Iterator i = m.keySet().iterator();
        	long pixelsID;
        	BufferedImage thumbPix;
        	ImageData image;
        	while (i.hasNext()) {
        		pixelsID = (Long) i.next();
        		image = input.get(pixelsID);
        		thumbPix = (BufferedImage) m.get(pixelsID);
        		//System.err.println(thumbPix);
				if (thumbPix == null) 
					thumbPix = createDefaultImage(image);
				result.add(new ThumbnailData(image.getId(), thumbPix));
			}
        	currentThumbs = result;
        	
        } catch (RenderingServiceException e) {
        	e.printStackTrace();
        	context.getLogger().error(this, 
        			"Cannot retrieve thumbnail: "+e.getExtendedMessage());
        }
    }
    
	/**
     * Adds a {@link BatchCall} to the tree for each thumbnail to retrieve.
     * The batch call simply invokes {@link #loadThumbail(int)}.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree()
    {
    	Iterator<List> i = toHandle.iterator();
    	String description = "Loading collection of thumbnails";
    	List l;
    	while (i.hasNext()) {
			l = i.next();
			final List ids = l;
			add(new BatchCall(description) {
        		public void doCall() { loadThumbails(ids); }
        	});  
		}
    	currentThumbs = notValid;
    }
    
    /**
     * Returns the lastly retrieved thumbnail.
     * This will be packed by the framework into a feedback event and
     * sent to the provided call observer, if any.
     * 
     * @return A {@link ThumbnailData} containing the thumbnail pixels.
     */
    protected Object getPartialResult() { return currentThumbs; }
    
    /**
     * Returns <code>null</code> as there's no final result.
     * In fact, thumbnails are progressively delivered with 
     * feedback events. 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return null; }
    
    /**
     * Creates a new instance.
     * 
     * @param images    The collection of images to load thumbnails for.
     * @param maxLength The maximum length of a thumbail.
     */
    public ThumbnailSetLoader(Collection<ImageData> images, int maxLength)
    {
    	if (images == null) throw new NullPointerException("No images.");
    	if (maxLength <= 0)
    		throw new IllegalArgumentException(
    				"Non-positive height: "+maxLength+".");
    	this.maxLength = maxLength;
    	service = context.getImageService();
    	toHandle = new ArrayList<List>();
    	input = new HashMap<Long, ImageData>();
    	notValid = new ArrayList();
    	Iterator<ImageData> i = images.iterator();
    	ImageData img;
    	int index = 0;
    	List<Long> l = null;
    	PixelsData pxd = null;
    	
    	while (i.hasNext()) {
    		img = i.next();
    		try {
            	pxd = img.getDefaultPixels();
            	input.put(pxd.getId(), img);
    			if (index == 0) l = new ArrayList<Long>();
    			if (index < MAX_PER_CALL) {
    				l.add(pxd.getId());
    				index++;
    				if (index == MAX_PER_CALL) {
    					toHandle.add(l);
    					index = 0;
    				}
    			}
    		} catch (Exception e) {
    			notValid.add(new ThumbnailData(img.getId(), 
						createDefaultImage(img)));
    		} //something went wrong during import
		}
    	if (l != null && l.size() > 0) toHandle.add(l);
    }
    
}
