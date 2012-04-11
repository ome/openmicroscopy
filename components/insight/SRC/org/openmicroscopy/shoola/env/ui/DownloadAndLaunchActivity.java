/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
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
package org.openmicroscopy.shoola.env.ui;

import java.io.File;

import org.openmicroscopy.shoola.env.data.model.DownloadAndLaunchActivityParam;
import org.openmicroscopy.shoola.util.filter.file.OMETIFFFilter;

/** 
 * 
 *
 * @author Scott Littlewood, <a href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class DownloadAndLaunchActivity {


	/** The parameters hosting information about the file to download. */
	private DownloadAndLaunchActivityParam parameters;
	
	/**
     * Returns the name of the file. 
     * 
     * @return See above.
     */
    private String getFileName()
    {
    	return parameters.getOriginalFileName();
    }
    
}
