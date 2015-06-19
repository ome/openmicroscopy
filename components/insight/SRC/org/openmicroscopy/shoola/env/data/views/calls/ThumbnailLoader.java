/*
 * org.openmicroscopy.shoola.env.data.views.calls.ThumbnailLoader
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries


import omero.ServerError;
//Application-internal dependencies
import omero.api.ThumbnailStorePrx;

import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;

import omero.gateway.SecurityContext;
import omero.gateway.exception.RenderingServiceException;

import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

import omero.log.LogMessage;

import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.image.io.WriterImage;

import pojos.DataObject;
import pojos.ImageData;
import pojos.PixelsData;

/** 
 * Command to load a given set of thumbnails.
 * <p>As thumbnails are retrieved from <i>OMERO</i>, they're posted back to the 
 * caller through <code>DSCallFeedbackEvent</code>s. Each thumbnail will be
 * posted in a single event; the caller can then invoke the <code>
 * getPartialResult</code> method to retrieve a <code>ThumbnailData</code>
 * object for that thumbnail. The final <code>DSCallOutcomeEvent</code> will
 * have no result.</p>
 * <p>Thumbnails are generated respecting the <code>X/Y</code> ratio of the
 * original image and so that their area doesn't exceed <code>maxWidth*
 * maxHeight</code>, which is specified to the constructor.</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ThumbnailLoader
    extends BatchCallTree
{

    /** The images for which we need thumbnails. */
    private Collection<DataObject> images;
    
    /** The maximum acceptable width of the thumbnails. */
    private int maxWidth;
    
    /** The maximum acceptable height of the thumbnails. */
    private int maxHeight;
    
    /** The lastly retrieved thumbnail. */
    private Object currentThumbnail;

    /** Flag to indicate if the class was invoked for a pixels ID. */
    private boolean pixelsCall;
    
    /** The id of the pixels set this loader is for. */
    private long pixelsID;
    
    /** Collection of user IDs. */
    private Set<Long> userIDs;
    
    /** Helper reference to the image service. */
    private OmeroImageService service;
    
    /** Load the thumbnail as an full size image. */
    private boolean asImage;
    
    /** The security context.*/
    private SecurityContext ctx;
    
    /**
     * Loads the thumbnail for {@link #images}<code>[index]</code>.
     * 
     * @param pxd The image the thumbnail for.
     * @param userID The id of the user the thumbnail is for.
     * @param store The thumbnail store to use.
     * @param imageID The id of the image associated to the pixels set.
     */
    private void loadThumbail(PixelsData pxd, long userID,
    		ThumbnailStorePrx store, boolean last, long imageID)
    {
    	BufferedImage thumbPix = null;
        boolean valid = true;
        int sizeX = maxWidth, sizeY = maxHeight;
        try {
        	if (asImage) {
        		sizeX = pxd.getSizeX();
        		sizeY = pxd.getSizeY();
        	} else {
        		Dimension d = Factory.computeThumbnailSize(sizeX, sizeY,
        				pxd.getSizeX(), pxd.getSizeY());
        		sizeX = d.width;
        		sizeY = d.height;
        	}

        	if (!store.setPixelsId(pxd.getId())) {
    			store.resetDefaults();
    			store.setPixelsId(pxd.getId());
    		}
    		if (userID >= 0) {
    			store.setRenderingDefId(service.getRenderingDef(ctx,
    					pxd.getId(), userID));
			}
    		thumbPix = WriterImage.bytesToImage(
    				store.getThumbnail(omero.rtypes.rint(sizeX),
    				omero.rtypes.rint(sizeY)));
        } catch (Throwable e) {
        	thumbPix = null;
        	LogMessage msg = new LogMessage();
            msg.print("Cannot retrieve thumbnail");
            msg.print(e);
        	context.getLogger().error(this, msg);
        } finally {
        	if (last) {
        		context.getDataService().closeService(ctx, store);
        	}
        }
        if (thumbPix == null) {
        	valid = false;
        	thumbPix = Factory.createDefaultImageThumbnail(sizeX, sizeY);
        }
        currentThumbnail = new ThumbnailData(imageID, thumbPix, userID, valid);
    }

    /**
     * Creates a {@link BatchCall} to retrieve rendering control.
     * 
     * @return          The {@link BatchCall}.
     */
    private BatchCall makeBatchCall()
    {
        return new BatchCall("Loading thumbnail for: "+pixelsID) {
            public void doCall() throws Exception
            {
                BufferedImage thumbPix = null;
                try {
                    thumbPix = service.getThumbnail(ctx, pixelsID, maxWidth,
                                maxHeight, -1);
                    
                } catch (RenderingServiceException e) {
                    context.getLogger().error(this, 
                    "Cannot retrieve thumbnail from ID: "+
                    e.getExtendedMessage());
                }
                if (thumbPix == null) 
                	thumbPix = Factory.createDefaultImageThumbnail(-1);
                currentThumbnail = thumbPix;
            }
        };
    } 
    
    /**
     * Adds a {@link BatchCall} to the tree for each thumbnail to retrieve.
     * The batch call simply invokes {@link #loadThumbail(int)}.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree()
    {
    	if (pixelsCall) {
    		add(makeBatchCall());
    		return;
    	}
    	String description;
    	Iterator<Long> j = userIDs.iterator();
    	Long id;
    	Iterator<DataObject> i;
    	DataObject image;
    	PixelsData pxd;
    	while (j.hasNext()) {
    		id = j.next();
    		final long userID = id;
    		i = images.iterator();
    		ThumbnailStorePrx store = null;
    		try {
    			store = service.createThumbnailStore(ctx);
			} catch (Exception e) {
				context.getLogger().debug(this,
						"Cannot start thumbnail store.");
			}
    		try {
        		final ThumbnailStorePrx value = store;
        		int size = images.size()-1;
        		int k = 0;
        		long imageID = -1;
        		while (i.hasNext()) {
        			image = (DataObject) i.next();
        			if (image instanceof ImageData) {
        			    pxd = ((ImageData) image).getDefaultPixels();
        			    imageID = image.getId();
        			} else {
        			    pxd = (PixelsData) image;
        			    if (pxd != null) imageID = pxd.getImage().getId();
        			}
        			description = "Loading thumbnail";
        			final PixelsData index = pxd;
        			final boolean last = size == k;
        			k++;
        			final long iid = imageID;
        			add(new BatchCall(description) {
        				public void doCall() {
        					loadThumbail(index, userID, value, last, iid);
        				}
        			});
        		}
    		} catch (RuntimeException r) {
    		    // If we fail to pass control to loadThumbnail
    		    // then we need to clean up the service.
    		    if (store != null) {
    		        try {
                        store.close();
                    } catch (ServerError e) {
                        context.getLogger().warn(this, "Failed to close " + store);
                    }
    		    }
    		}
    	}
    }

    /**
     * Returns the lastly retrieved thumbnail.
     * This will be packed by the framework into a feedback event and
     * sent to the provided call observer, if any.
     * 
     * @return A {@link ThumbnailData} containing the thumbnail pixels.
     */
    protected Object getPartialResult() { return currentThumbnail; }
    
    /**
     * Returns the last loaded thumbnail (important for the BirdsEyeLoader to
     * work correctly). But in fact, thumbnails are progressively delivered with 
     * feedback events. 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return currentThumbnail; }
    
    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime exception so to fail
     * early and in the caller's thread.
     * 
     * @param ctx The security context.
     * @param imgs Contains {@link DataObject}s, one 
     *             for each thumbnail to retrieve.
     * @param maxWidth The maximum acceptable width of the thumbnails.
     * @param maxHeight The maximum acceptable height of the thumbnails.
     * @param userIDs The users the thumbnail are for.
     */
    public ThumbnailLoader(SecurityContext ctx, Set<DataObject> imgs,
    		int maxWidth, int maxHeight, Set<Long> userIDs)
    {
        if (imgs == null) throw new NullPointerException("No images.");
        if (maxWidth <= 0)
            throw new IllegalArgumentException(
                    "Non-positive width: "+maxWidth+".");
        if (maxHeight <= 0)
            throw new IllegalArgumentException(
                    "Non-positive height: "+maxHeight+".");
        this.ctx = ctx;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        images = imgs;
        this.userIDs = userIDs;
        asImage = false;
        service = context.getImageService();
    }

    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime exception so to fail
     * early and in the caller's thread.
     * 
     * @param ctx The security context.
     * @param imgs Contains {@link DataObject}s, one for each thumbnail to 
     *             retrieve.
     * @param userID The user the thumbnail are for.
     */
    public ThumbnailLoader(SecurityContext ctx, Collection<DataObject> imgs,
    		long userID)
    {
        if (imgs == null) throw new NullPointerException("No images.");
        this.ctx = ctx;
        asImage = true;
        images = imgs;
        userIDs = new HashSet<Long>(1);
        userIDs.add(userID);
        service = context.getImageService();
    }
    
    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime exception so to fail
     * early and in the caller's thread.
     * 
     * @param ctx The security context.
     * @param imgs Contains {@link DataObject}s, one for each thumbnail to
     *             retrieve.
     * @param maxWidth The maximum acceptable width of the thumbnails.
     * @param maxHeight The maximum acceptable height of the thumbnails.
     * @param userID The user the thumbnail are for.
     */
    public ThumbnailLoader(SecurityContext ctx, Collection<DataObject> imgs,
    		int maxWidth, int maxHeight, long userID)
    {
        if (imgs == null) throw new NullPointerException("No images.");
        if (maxWidth <= 0)
            throw new IllegalArgumentException(
                    "Non-positive width: "+maxWidth+".");
        if (maxHeight <= 0)
            throw new IllegalArgumentException(
                    "Non-positive height: "+maxHeight+".");
        this.ctx = ctx;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        images = imgs;
        userIDs = new HashSet<Long>(1);
        userIDs.add(userID);
        asImage = false;
        service = context.getImageService();
    }
    
    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime exception so to fail
     * early and in the caller's thread.
     * 
     * @param ctx The security context.
     * @param image The {@link ImageData}, the thumbnail
     * @param maxWidth The maximum acceptable width of the thumbnails.
     * @param maxHeight The maximum acceptable height of the thumbnails.
     * @param userID The user the thumbnails are for.
     */
    public ThumbnailLoader(SecurityContext ctx, ImageData image, int maxWidth,
    		int maxHeight, long userID)
    {
        if (image == null) throw new IllegalArgumentException("No image.");
        if (maxWidth <= 0)
            throw new IllegalArgumentException(
                    "Non-positive width: "+maxWidth+".");
        if (maxHeight <= 0)
            throw new IllegalArgumentException(
                    "Non-positive height: "+maxHeight+".");
        this.ctx = ctx;
        userIDs = new HashSet<Long>(1);
        userIDs.add(userID);
        images = new HashSet<DataObject>(1);
        images.add(image);
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        asImage = false;
        service = context.getImageService();
    }
    
    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime exception so to fail
     * early and in the caller's thread.
     * 
     * @param ctx The security context.
     * @param pixelsID The id of the pixel set.
     * @param maxWidth The maximum acceptable width of the thumbnails.
     * @param maxHeight The maximum acceptable height of the thumbnails.
     * @param userID The user the thumbnail are for.
     */
    public ThumbnailLoader(SecurityContext ctx, long pixelsID, int maxWidth,
    		int maxHeight, long userID)
    {
        if (maxWidth <= 0)
            throw new IllegalArgumentException(
                    "Non-positive id: "+pixelsID+".");
        if (maxWidth <= 0)
            throw new IllegalArgumentException(
                    "Non-positive width: "+maxWidth+".");
        if (maxHeight <= 0)
            throw new IllegalArgumentException(
                    "Non-positive height: "+maxHeight+".");
        this.ctx = ctx;
        pixelsCall = true;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.pixelsID = pixelsID;
        userIDs = new HashSet<Long>(1);
        userIDs.add(userID);
        service = context.getImageService();
    }
    
    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime exception so to fail
     * early and in the caller's thread.
     * 
     * @param ctx The security context.
     * @param image The {@link ImageData}, the thumbnail
     * @param maxWidth The maximum acceptable width of the thumbnails.
     * @param maxHeight The maximum acceptable height of the thumbnails.
     * @param userIDs The users the thumbnail are for.
     */
    public ThumbnailLoader(SecurityContext ctx, ImageData image, int maxWidth,
    		int maxHeight, Set<Long> userIDs)
    {
        if (image == null) throw new IllegalArgumentException("No image.");
        if (maxWidth <= 0)
            throw new IllegalArgumentException(
                    "Non-positive width: "+maxWidth+".");
        if (maxHeight <= 0)
            throw new IllegalArgumentException(
                    "Non-positive height: "+maxHeight+".");
        this.ctx = ctx;
        images = new HashSet<DataObject>(1);
        images.add(image);
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.userIDs = userIDs;
        asImage = false;
        service = context.getImageService();
    }
    
}
