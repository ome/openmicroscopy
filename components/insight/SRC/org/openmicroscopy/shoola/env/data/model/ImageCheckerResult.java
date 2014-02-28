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
import java.util.List;

/**
 * A container holding MIFResultObjects (MIF delete/chgrp check)
 * and the number of images which are linked to multiple 
 * datasets (multiple dataset link check) as well as thumbnails 
 * for the first part of these images if there are any.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.0
 */
public class ImageCheckerResult {

	/** Maximum number of thumbnails to get for the multi-dataset-images */
	public static final int MAX_MULTILINK_THUMBS = 25;
	
	/** Holds all MIFResultObjects */
	private List<MIFResultObject> mifResults = new ArrayList<MIFResultObject>();
	
	/** Holds thumbnails to images linked to multiple datasets */
	private List<ThumbnailData> multiLinkImages = new ArrayList<ThumbnailData>();
	
	/** Count of images which are linked to multiple datasets */
	private int multiLinkImageCount = 0;
	
	/**
	 * Get all MIFResultObjects.
	 * @return
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
	 * Get thumbnails of the images linked to multiple datasets.
	 * This may not be all images! See {@link #getMultiLinkImageCount()}
	 * @return See above
	 */
	public List<ThumbnailData> getMultiLinkImages() {
		return multiLinkImages;
	}

	/**
	 * Set thumbnails of images linked to multiple datasets.
	 * @param multiLinkImages
	 */
	public void setMultiLinkImages(List<ThumbnailData> multiLinkImages) {
		this.multiLinkImages = multiLinkImages;
	}

	/**
	 * Get the total count of images linked to multiple datasets
	 * @return See above
	 */
	public int getMultiLinkImageCount() {
		return multiLinkImageCount;
	}

	/**
	 * Set the total count of images linked to multiple datasets
	 * @param multiLinkImageCount
	 */
	public void setMultiLinkImageCount(int multiLinkImageCount) {
		this.multiLinkImageCount = multiLinkImageCount;
	}

}
