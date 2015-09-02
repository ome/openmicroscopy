/*
 * org.openmicroscopy.shoola.env.data.model.DownloadArchivedActivityParam 
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.model;


import java.io.File;
import java.util.List;

import javax.swing.Icon;

import pojos.ImageData;

/** 
 * Hosts the parameters required to download the archived image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class DownloadArchivedActivityParam 
{

	/** The icon associated to the parameters. */
    private Icon icon;
    
    /** The file to download the content into. */
    private File location;
    
    /** The collection of archived images to download. */
    private List<ImageData> images;
    
    /** Flag indicating to override or not the files when saving.*/
    private boolean override;

    /** Flag for zipping the downloaded images */
    private boolean zip = false;
    
    /** Flag for preserving the original folder structure */
    private boolean keepOriginalPaths = true;
    
    /**
     * Creates a new instance.
     * 
     * @param location The file to download the content into.
     * @param images The archived images to download.
     * @param icon The icon associated to the parameters.
     */
    public DownloadArchivedActivityParam(File location, List<ImageData> images,
    		Icon icon)
    {
    	this.location = location;
    	this.images = images;
    	this.icon = icon;
    	this.override = false;
    }

    /**
     * Sets to <code>true</code> to override the files when saving,
     * <code>false</code> otherwise. Default is <code>false</code>.
     *
     * @param override The value to set.
     */
    public void setOverride(boolean override) { this.override = override; }

    /**
     * Returns <code>true</code> to override the files when saving,
     * <code>false</code> otherwise. Default is <code>false</code>.
     *
     * @return See above.
     */
    public boolean isOverride() { return override; }

    /**
     * Returns the icon.
     * 
     * @return See above.
     */
    public Icon getIcon() { return icon; }
    
    /**
     * Returns the path to the folder where to download the archived files.
     * 
     * @return See above.
     */
    public File getLocation() { return location; }
    
    /**
     * Returns the archived image to download.
     * 
     * @return See above.
     */
    public List<ImageData> getImages() { return images; }

    /**
     * Returns if the downloaded images should be zipped
     * 
     * @return See above
     */
    public boolean isZip() {
        return zip;
    }

    /**
     * Sets the zip flag
     * 
     * @param zip
     *            Pass <code>true</code> if the downloaded images should be
     *            zipped
     */
    public void setZip(boolean zip) {
        this.zip = zip;
    }

    /**
     * Returns if the original folder structure should be preserved
     * 
     * @return See above
     */
    public boolean isKeepOriginalPaths() {
        return keepOriginalPaths;
    }

    /**
     * Sets the keepOriginalPaths flag
     * 
     * @param keepOriginalPaths
     *            Pass <code>true</code> to preserve the original folder
     *            structure
     */
    public void setKeepOriginalPaths(boolean keepOriginalPaths) {
        this.keepOriginalPaths = keepOriginalPaths;
    }

}
