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
import java.util.List;
import java.util.Map;

import pojos.DatasetData;
import pojos.ImageData;

/**
 * A container holding the results of a pre-delete check; in particular 
 * a list of {@link MIFResultObject}s (MIF delete/chgrp check)
 * and a map of {@link DatasetData}s to which the images are linked to;
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.0
 */
public class ImageCheckerResult {
	
	/** Holds all MIFResultObjects */
	private List<MIFResultObject> mifResults = new ArrayList<MIFResultObject>();
	
	/** A Map holding the datasets an image is linked to */
	private Map<ImageData, List<DatasetData>> datasets = new HashMap<ImageData, List<DatasetData>>();
	
	/**
	 * Get all MIFResultObjects.
	 * @return See above.
	 */
	public List<MIFResultObject> getMifResults() {
		return mifResults;
	}

	/**
	 * Set the MIFResultObjects.
	 * @param mifResults
	 */
	public void setMifResults(List<MIFResultObject> mifResults) {
		this.mifResults = mifResults;
	}
	
	
        /**
         * Adds Datasets a certain images is linked to
         * 
         * @param img
         *            The image
         * @param ds
         *            The datasets this image is linked to
         */
        public void addDatasets(ImageData img, Collection<DatasetData> ds) {
            List<DatasetData> sets = datasets.get(img);
            if (sets == null) {
                sets = new ArrayList<DatasetData>();
                datasets.put(img, sets);
            }
            sets.addAll(ds);
        }
	
        /**
         * Get all datasets the given images is linked to
         * 
         * @param img
         *            The image
         * @return See above
         */
        public List<DatasetData> getDatasets(ImageData img) {
            List<DatasetData> result = datasets.get(img);
            if (result == null) {
                result = new ArrayList<DatasetData>();
            }
            return result;
        }
	
        /**
         * Get the images which are linked to multiple datasets
         * 
         * @return See above
         */
        public List<ImageData> getMultiLinkedImages() {
            List<ImageData> result = new ArrayList<ImageData>();
            for (ImageData img : datasets.keySet()) {
                Collection<DatasetData> ds = datasets.get(img);
                if (ds.size() > 1) {
                    result.add(img);
                }
            }
            return result;
        }
}
