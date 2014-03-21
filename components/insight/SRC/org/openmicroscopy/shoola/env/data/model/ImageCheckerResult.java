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
 * A container holding the results of a pre-delete check; in particular 
 * a list of {@link MIFResultObject}s (MIF delete/chgrp check)
 * and a {@link MultiDatasetImageLinkResult} (multiple dataset link check);
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.0
 */
public class ImageCheckerResult {
	
	/** Holds all MIFResultObjects */
	private List<MIFResultObject> mifResults = new ArrayList<MIFResultObject>();
	
	/** Holds the result of the Image-Dataset Linkcheck */
	private MultiDatasetImageLinkResult multiLinkResult;
	
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
	 * Get the link check result
	 */
        public MultiDatasetImageLinkResult getMultiLinkResult() {
            return multiLinkResult;
        }
        
        /**
	 * Set the link check result
	 * @param mdlResult
	 */
        public void setMultiLinkResult(MultiDatasetImageLinkResult mdlResult) {
            this.multiLinkResult = mdlResult;
        }

	

}
