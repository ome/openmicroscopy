/*
 * org.openmicroscopy.shoola.env.data.views.calls.ThumbnailSetLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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


import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;

import omero.gateway.SecurityContext;
import omero.gateway.exception.RenderingServiceException;

import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.util.image.geom.Factory;

import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.FileData;
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
 * @since OME3.0
 */
public class ThumbnailSetLoader
extends BatchCallTree
{

    /** 
     * Indicates that the thumbnails are associated to an 
     * <code>ImageData</code>.
     */
    public static final int IMAGE = 0;

    /** 
     * Indicates that the thumbnails are associated to an 
     * <code>ExperimenterData</code>.
     */
    public static final int EXPERIMENTER = 1;

    /** 
     * Indicates that the thumbnails are associated to an <code>FileData</code>.
     */
    public static final int FS_FILE = 2;

    /** Maximum number of thumbnails retrieved asynchronously. */
    private static final int FETCH_SIZE = 10;

    /** 
     * Factor by which the maximum number of thumbnails to fetch
     * is multiplied with when the connection's speed is <code>Low</code>.
     */
    private static final double FETCH_LOW_SPEED = 0.25;

    /** 
     * Factor by which the maximum number of thumbnails to fetch
     * is multiplied with when the connection's speed is <code>Medium</code>.
     */
    private static final double FETCH_MEDIUM_SPEED = 0.5;

    /** Helper reference to the image service. */
    private OmeroImageService service;

    /** The maximum acceptable length of the thumbnails. */
    private int maxLength;

    /** Collection of list of pixels set to handle. */
    private List<List> toHandle;

    /** Key, value pairs, Key is the pixels set id. */
    private Map<Long, DataObject> input;

    /** Collection of {@link ThumbnailData}s for not valid pixels set. */
    private List notValid;

    /** Collection of current {@link ThumbnailData}s. */
    private Object currentThumbs;

    /** The maximum number of the tumbnails fetched. */
    private int fetchSize;

    /** The type of nodes to handle. */
    private Class type;

    /** The security context.*/
    private SecurityContext ctx;

    /**
     * Creates a default thumbnail for the passed pixels set.
     * 
     * @param pxd The pixels set to handle.
     * @return See above.
     */
    private BufferedImage createDefaultImage(PixelsData pxd) 
    {
        if (pxd == null) return Factory.createDefaultImageThumbnail(-1);
        Dimension d = Factory.computeThumbnailSize(maxLength, maxLength,
                pxd.getSizeX(), pxd.getSizeY());
        return Factory.createDefaultImageThumbnail(d.width, d.height);
    }

    /** 
     * Computes the maximum number of thumbnails to fetch 
     * depending on the initial value and the speed of the connection.
     */
    private void computeFetchSize()
    {
        int value = -1;
        Object fSize = context.lookup(LookupNames.THUMBNAIL_FETCH_SZ);

        if (fSize != null && (fSize instanceof Integer)) 
            value = (Integer) fSize;
        else context.getLogger().warn(this, "Thumbnail fetching size not set");
        if (value <= 0) value = FETCH_SIZE;
        UserCredentials uc = 
                (UserCredentials) context.lookup(LookupNames.USER_CREDENTIALS);
        double f = 0;
        Object fSpeed = null;
        switch (uc.getSpeedLevel()) {
            case UserCredentials.MEDIUM:
                fSpeed = context.lookup(
                        LookupNames.THUMBNAIL_FETCH_MEDIUM_SPEED);
                if (fSpeed != null && (fSpeed instanceof Double)) 
                    f = (Double) fSpeed;
                else 
                    context.getLogger().warn(this, "Thumbnail " +
                            "fetching factor not set");
                if (f <= 0 || f > 1) f = FETCH_MEDIUM_SPEED;
                fetchSize = (int) (value*f);
                break;
            case UserCredentials.LOW:
                fSpeed = context.lookup(
                        LookupNames.THUMBNAIL_FETCH_LOW_SPEED);
                if (fSpeed != null && (fSpeed instanceof Double)) 
                    f = (Double) fSpeed;
                else 
                    context.getLogger().warn(this, "Thumbnail " +
                            "fetching factor not set");
                if (f <= 0 || f > 1) f = FETCH_LOW_SPEED;
                fetchSize = (int) (value*f);
                break;
            default:
                fetchSize = value;
        }
    }

    /**
     * Loads the thumbnails for the passed collection of files.
     * 
     * @param files The collection of files to handle.
     */
    private void loadFSThumbnails(List files)
    {
        List result = new ArrayList();
        try {
            ExperimenterData exp = (ExperimenterData) context.lookup(
                    LookupNames.CURRENT_USER_DETAILS);
            long id = exp.getId();
            Map<DataObject, BufferedImage> m = service.getFSThumbnailSet(ctx,
                    files, maxLength, id);
            Entry<DataObject, BufferedImage> entry;
            Iterator<Entry<DataObject, BufferedImage>> i = m.entrySet().iterator();
            BufferedImage thumb;
            DataObject obj;
            boolean valid = true;
            FileData f;
            while (i.hasNext()) {
                entry = i.next();
                obj = entry.getKey();
                thumb = entry.getValue();
                if (thumb == null) {
                    thumb = Factory.createDefaultImageThumbnail(
                            Factory.IMAGE_ICON);
                }
                if (obj.getId() > 0)
                    result.add(new ThumbnailData(obj.getId(), thumb, valid));
                else 
                    result.add(new ThumbnailData(obj, thumb, valid));
            }
            currentThumbs = result;
        } catch (Exception e) {
            currentThumbs = result;
            context.getLogger().error(this, 
                    "Cannot retrieve thumbnail: "+e.getMessage());
        }
    }

    /**
     * Loads the thumbnails for the passed collection of experimenters.
     * 
     * @param experimenters The collection of experimenters to handle.
     */
    private void loadExperimenterThumbnails(List experimenters)
    {
        try {
            ExperimenterData exp = (ExperimenterData) context.lookup(
                    LookupNames.CURRENT_USER_DETAILS);
            Map<DataObject, BufferedImage> m = 
                    service.getExperimenterThumbnailSet(ctx, experimenters,
                            maxLength);
            List result = new ArrayList();
            Entry<DataObject, BufferedImage> entry;
            Iterator<Entry<DataObject, BufferedImage>> i = m.entrySet().iterator();
            BufferedImage thumb;
            DataObject obj;
            boolean valid = true;
            while (i.hasNext()) {
                entry = i.next();
                obj = (DataObject) entry.getKey();
                thumb = entry.getValue();
                if (thumb == null) 
                    thumb = Factory.createDefaultImageThumbnail(
                            Factory.EXPERIMENTER_ICON);
                if (obj.getId() > 0)
                    result.add(new ThumbnailData(obj.getId(), thumb, valid));
                else 
                    result.add(new ThumbnailData(obj, thumb, valid));
            }
            currentThumbs = result;
        } catch (Exception e) {
            context.getLogger().error(this, 
                    "Cannot retrieve thumbnail: "+e.getMessage());
        }
    }


    /**
     * Loads the thumbnail for passed collection of pixels set.
     * 
     * @param ids The collection of pixels set id.
     */
    private void loadThumbnails(List ids) 
    {
        try {
            Map<Long, BufferedImage>
            m = service.getThumbnailSet(ctx, ids, maxLength);
            List<Object> result = new ArrayList<Object>();
            Iterator<Long> i = m.keySet().iterator();
            long pixelsID;
            BufferedImage thumbPix;
            DataObject obj;
            boolean valid = true;
            long imageID = -1;
            PixelsData pxd = null;
            while (i.hasNext()) {
                pixelsID = i.next();
                obj = input.get(pixelsID);
                if (obj instanceof ImageData) {
                    imageID = ((ImageData) obj).getId();
                    pxd = ((ImageData) obj).getDefaultPixels();
                } else if (obj instanceof PixelsData) {
                    pxd = (PixelsData) obj;
                    imageID = pxd.getImage().getId();
                }
                if (pxd != null) {
                    thumbPix = (BufferedImage) m.get(pixelsID);
                    if (thumbPix == null) thumbPix = createDefaultImage(pxd);
                    result.add(new ThumbnailData(imageID, thumbPix,  valid));
                }
            }
            currentThumbs = result;

        } catch (RenderingServiceException e) {
            context.getLogger().error(this, 
                    "Cannot retrieve thumbnail: "+e.getExtendedMessage());
        }
    }

    /**
     * Adds a {@link BatchCall} to the tree for each thumbnail to retrieve.
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
                public void doCall() { 
                    if (ImageData.class.equals(type)) {
                        loadThumbnails(ids);
                    } else if (FileData.class.equals(type)) {
                        loadFSThumbnails(ids);
                    } else if (ExperimenterData.class.equals(type)) {
                        loadExperimenterThumbnails(ids);
                    }
                }
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
     * @param ctx The security context.
     * @param images    The collection of images to load thumbnails for.
     * @param maxLength The maximum length of a thumbnail.
     * @param nodeType	One of the constants defined by this class.
     */
    public ThumbnailSetLoader(SecurityContext ctx,
            Collection<DataObject> images, int maxLength, int nodeType)
    {
        if (images == null) throw new NullPointerException("No images.");
        if (maxLength <= 0)
            throw new IllegalArgumentException(
                    "Non-positive height: "+maxLength+".");
        computeFetchSize();
        this.ctx = ctx;
        this.maxLength = maxLength;
        service = context.getImageService();
        toHandle = new ArrayList<List>();
        input = new HashMap<Long, DataObject>();
        notValid = new ArrayList();
        Iterator<DataObject> i = images.iterator();
        ImageData img;
        DataObject object;
        int index = 0;
        List<Object> l = null;
        PixelsData pxd = null;
        while (i.hasNext()) {
            object = i.next();
            if (object instanceof ImageData) {
                if (nodeType == FS_FILE) {
                    input.put(object.getId(), object);
                    type = FileData.class;
                    if (index == 0) l = new ArrayList<Object>();
                    if (index < fetchSize) {
                        l.add(object);
                        index++;
                        if (index == fetchSize) {
                            toHandle.add(l);
                            index = 0;
                            l = null;
                        }
                    }
                } else {
                    img = (ImageData) object;
                    type = ImageData.class;
                    try {
                        pxd = img.getDefaultPixels();
                        input.put(pxd.getId(), img);
                        if (index == 0) l = new ArrayList<Object>();
                        if (index < fetchSize) {
                            l.add(pxd.getId());
                            index++;
                            if (index == fetchSize) {
                                toHandle.add(l);
                                index = 0;
                                l = null;
                            }
                        }
                    } catch (Exception e) {
                        notValid.add(new ThumbnailData(img.getId(), 
                                createDefaultImage(pxd), false));
                    } //something went wrong during import
                }
            } else if (object instanceof FileData) {
                input.put(object.getId(), object);
                type = FileData.class;
                if (index == 0) l = new ArrayList<Object>();
                if (index < fetchSize) {
                    l.add(object);
                    index++;
                    if (index == fetchSize) {
                        toHandle.add(l);
                        index = 0;
                        l = null;
                    }
                }
            } else if (object instanceof ExperimenterData) {
                input.put(object.getId(), object);
                type = ExperimenterData.class;
                if (index == 0) l = new ArrayList<Object>();
                if (index < fetchSize) {
                    l.add(object);
                    index++;
                    if (index == fetchSize) {
                        toHandle.add(l);
                        index = 0;
                        l = null;
                    }
                }
            } else if (object instanceof PixelsData) {
                pxd = (PixelsData) object;
                type = ImageData.class;
                input.put(pxd.getId(), pxd);
                if (index == 0) l = new ArrayList<Object>();
                if (index < fetchSize) {
                    l.add(pxd.getId());
                    index++;
                    if (index == fetchSize) {
                        toHandle.add(l);
                        index = 0;
                        l = null;
                    }
                }
            }
        }
        if (l != null && l.size() > 0) toHandle.add(l);
    }

}
