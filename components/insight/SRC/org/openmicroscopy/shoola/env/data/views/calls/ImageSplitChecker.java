/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013-2014 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.views.calls;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.model.ImageCheckerResult;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;

import omero.gateway.SecurityContext;

import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.util.image.geom.Factory;

import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.exception.RenderingServiceException;

import org.openmicroscopy.shoola.env.data.model.MIFResultObject;

import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;

/**
 * Checks if the images in the specified containers are split between
 * or not all selected.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class ImageSplitChecker
	extends BatchCallTree
{
	/** Result of the call. */
	private ImageCheckerResult result = new ImageCheckerResult();

	/** Loads the specified tree. */
	private BatchCall loadCall;

	/** The service used to load the thumbnails.*/
	private OmeroImageService service;
	
	/**
     * Loads the thumbnails for passed collection of images.
     * 
     * @param ctx The security context to use.
     * @param images The collection of images to handle.
     */
    private List<ThumbnailData> loadThumbails(SecurityContext ctx,
    		List<ImageData> images)
    {
    	List<ThumbnailData> thumbnails = new ArrayList<ThumbnailData>();
    	try {
    		Map<Long, ImageData> map = new HashMap<Long, ImageData>();
    		Iterator<ImageData> i = images.iterator();
    		ImageData img;
    		while (i.hasNext()) {
    			img = i.next();
    			map.put(img.getDefaultPixels().getId(), img);

    		}
    		Map<Long, BufferedImage>
    		m = service.getThumbnailSet(ctx, map.keySet(),
    				Factory.THUMB_DEFAULT_WIDTH);
    		Iterator<Long> j = m.keySet().iterator();
    		long pixelsID;
    		BufferedImage thumbPix;
    		ImageData obj;
    		long imageID = -1;
    		while (j.hasNext()) {
    			pixelsID = j.next();
    			obj = map.get(pixelsID);
    			imageID = ((ImageData) obj).getId();
    			thumbPix = (BufferedImage) m.get(pixelsID);
    			if (thumbPix != null)
    				thumbnails.add(new ThumbnailData(imageID, thumbPix, true));
    		}
    	} catch (RenderingServiceException e) {
    		context.getLogger().error(this, 
    				"Cannot retrieve thumbnail: "+e.getExtendedMessage());
    	}
    	return thumbnails;
    }
    
	/**
	 * Creates a {@link BatchCall} to retrieve rendering control.
	 * 
	 * @param ctx The security context.
	 * @param rootType The top-most type which will be searched.
	 * @param rootIDs A set of the IDs of objects.
	 * @return The {@link BatchCall}.
	 */
	private BatchCall makeBatchCall(final
			Map<SecurityContext, List<DataObject>> objects)
	{
		return new BatchCall("Checking for split MIF ") {
			public void doCall() throws Exception
			{
				OmeroDataService svc = context.getDataService();
				Entry<SecurityContext, List<DataObject>> e;
				Iterator<Entry<SecurityContext, List<DataObject>>> i =
						objects.entrySet().iterator();
				Iterator<DataObject> j;
				List<Long> ids;
				DataObject uo;
				Map<Long, Map<Boolean, List<ImageData>>> r;
				MIFResultObject mif;
				List<ImageData> images;
				List<ImageData> linkCheckImages = new ArrayList<ImageData>();
				while (i.hasNext()) {
				        linkCheckImages.clear();;
					e = i.next();
					j = e.getValue().iterator();
					ids = new ArrayList<Long>();
					Class<?> klass = null;
					while (j.hasNext()) {
						uo = j.next();
						klass = uo.getClass();
						ids.add(uo.getId());
						if(uo instanceof ImageData) {
							ImageData img = (ImageData)uo;
							linkCheckImages.add(img);
						}
					}
					r = svc.getImagesBySplitFilesets(e.getKey(),
							klass, ids);
					if (r != null && r.size() > 0) {
						mif = new MIFResultObject(e.getKey(), r);
						//load the thumbnails for a limited number of images.
						images = mif.getImages();
						mif.setThumbnails(loadThumbails(e.getKey(), images));
						result.getMifResults().add(mif);
					}
					
					if(!linkCheckImages.isEmpty()) {
					    loadDatasetLinks(e.getKey(), linkCheckImages);
					}
				}
			}
		};
	} 
	
	/**
	 * Loads the datasets the given images are linked to
	 * @param ctx The security context to use for the query
	 * @param imgs The images 
	 */
        private void loadDatasetLinks(SecurityContext ctx, List<ImageData> imgs) {
            try {
                List<Long> imgIds = new ArrayList<Long>();
                for(ImageData img : imgs) {
                    imgIds.add(img.getId());
                }
                
                OmeroDataService svc = context.getDataService();
                Map<Long, List<DatasetData>> queryResult = svc.findDatasetsByImageId(ctx, imgIds);

                for(Long imgId: queryResult.keySet()) {
                    List<DatasetData> ds = queryResult.get(imgId);
                    ImageData img = null;
                    for(ImageData tmp : imgs) {
                        if(tmp.getId()==imgId) {
                            img = tmp;
                            break;
                        }
                    }
                    if(img!=null) {
                        result.addDatasets(img, ds);
                    }
                }
                
            } catch (DSOutOfServiceException e) {
                context.getLogger().error(this,
                        "Cannot retrieve datasets: " + e.getMessage());
            } catch (DSAccessException e) {
                context.getLogger().error(this,
                        "Cannot retrieve datasets: " + e.getMessage());
            }
        }
        
	/**
	 * Adds the {@link #loadCall} to the computation tree.
	 * 
	 * @see BatchCallTree#buildTree()
	 */
	protected void buildTree() { add(loadCall); }

	/**
	 * Returns the {@link RenderingControl}.
	 * 
	 * @see BatchCallTree#getResult()
	 */
	protected Object getResult() { return result; }

	/**
	 * Creates a new instance.
	 * If bad arguments are passed, we throw a runtime exception so to fail
	 * early and in the caller's thread.
	 * 
	 * @param objects The object to handle.
	 */
	public ImageSplitChecker(Map<SecurityContext, List<DataObject>> objects)
	{
		if (objects == null || objects.size() == 0)
			throw new IllegalArgumentException("No object to check.");
		service = context.getImageService();
		loadCall = makeBatchCall(objects);
	}
}
