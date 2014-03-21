/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openmicroscopy.shoola.env.data.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import pojos.DatasetData;
import pojos.ImageData;

/**
 * Represents the result of a check, if particular images are linked 
 * to multiple datasets
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class MultiDatasetImageLinkResult {

    /** The thumbnails of the images which have been checked */
    private Collection<ThumbnailData> thumbnails = new ArrayList<ThumbnailData>();
    
    /** The Datasets (values) the images (keys) are linked to */
    private Map<Long, Collection<DatasetData>> datasets = new HashMap<Long, Collection<DatasetData>>();
    
    /**
     * Creaets a new instance
     */
    public MultiDatasetImageLinkResult() {
        
    }
    
    /**
     * Add thumbnails
     * @param thumbs The thumbnail to add
     */
    public void addThumbnails(Collection<ThumbnailData> thumbs) {
        thumbnails.addAll(thumbs);
    }
    
    /**
     * Add {@link DatasetData}s an {@link ImageData} is linked to
     * @param img The image
     * @param ds The datasets the image is linked to
     */
    public void addDatasets(ImageData img, Collection<DatasetData> ds) {
        Collection<DatasetData> datasetsForImage = datasets.get(img.getId());
        if(datasetsForImage==null) {
            datasetsForImage = new ArrayList<DatasetData>();
            datasets.put(img.getId(), datasetsForImage);
        }
        datasetsForImage.addAll(ds);
    }
    
    /**
     * Get all {@link DatasetData}s a particular image is linked to
     * @param imgId The ID of the image
     * @return See above
     */
    public Collection<DatasetData> getDatasets(long imgId) {
        Collection<DatasetData> result = datasets.get(imgId);
        if(result==null) {
            result = new ArrayList<DatasetData>();
        }
        return result;
    }
    
    /**
     * Get all image IDs this {@link MultiDatasetImageLinkResult} contains
     * @return See above
     */
    public Collection<Long> getImageIds() {
        Collection<Long> result = new ArrayList<Long>();
        result.addAll(datasets.keySet());
        return result;
    }
    
    /**
     * Returns <code>true</code> if this {@link MultiDatasetImageLinkResult} does not 
     * contain any images, <code>false</code> otherwise
     * @return
     */
    public boolean isEmpty() {
        return datasets.keySet().isEmpty();
    }
    
    /**
     * Get the thumbnail of a particular image
     * @param imgId The ID of the image
     * @return See above
     */
    public ThumbnailData getThumbnail(long imgId) {
        for(ThumbnailData thumb : thumbnails) {
            if(thumb.getImageID()==imgId) {
                return thumb;
            }
        }
        return null;
    }
}
