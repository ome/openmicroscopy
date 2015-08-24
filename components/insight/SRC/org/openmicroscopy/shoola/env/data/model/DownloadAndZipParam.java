/*
 * org.openmicroscopy.shoola.env.data.model.DownloadAndZipParam 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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

import pojos.FileAnnotationData;

/** 
 * Parameters required to download and zip the files.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class DownloadAndZipParam 
{

	 /** The icon associated to the parameters. */
    private Icon			icon;
    
    /** The folder where to download the file. */
    private File			folder; 
    
    /** The files to download. */
    private List<FileAnnotationData>	files;
    
    /**
     * Creates a new instance.
     * 
     * @param files  The files to download.
     * @param folder The folder where 
     * @param icon   The associated icon.
     */
    public DownloadAndZipParam(List<FileAnnotationData> files, File folder,
    		Icon icon)
    {
    	if (files == null || files.size() == 0)
    		throw new IllegalArgumentException("No files to download");
    	this.files = files;
    	this.folder = folder;
    	this.icon = icon;
    }
    
	/**
	 * Returns the icon if set or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public Icon getIcon() { return icon; }
	
	/** 
	 * Returns the folder where to download the file.
	 * 
	 * @return See above.
	 */
	public File getFolder() { return folder; }
	
	/** 
	 * Returns the files to download.
	 * 
	 * @return See above.
	 */
	public List<FileAnnotationData> getFiles() { return files; }
	
    
}
