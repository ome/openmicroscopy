/*
 * org.openmicroscopy.shoola.env.ui.DownloadActivity 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.ui;



//Java imports
import java.io.File;
import omero.model.OriginalFile;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DownloadActivityParam;
import org.openmicroscopy.shoola.util.filter.file.OMETIFFFilter;

/** 
 * Activity to download an image or file.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class DownloadActivity 
	extends ActivityComponent
{

	/** The description of the activity when finished. */
	private static final String		DESCRIPTION = "File downloaded";
	
    /** The parameters hosting information about the file to download. */
    private DownloadActivityParam parameters;
    
    /**
     * Returns the name of the file. 
     * 
     * @return See above.
     */
    private String getFileName()
    {
    	OriginalFile file = parameters.getFile();
    	File folder = parameters.getFolder();
    	File directory = folder.getParentFile();
    	
    	File[] files = directory.listFiles();
    	String dirPath = directory.getAbsolutePath()+File.separator;
    	String value = folder.getName();
    	if (parameters.getFileName() != null)
    		value = parameters.getFileName();
    	String extension = null;
    	if (value != null && value.trim().length() > 0) {
    		int lastDot = value.lastIndexOf(".");
    		if (lastDot == -1) { //no extension specified.
    			//get the extension from the file.
    			String s = file.getName().getValue();
        		if (s.endsWith(OMETIFFFilter.OME_TIF) ||
        				s.endsWith(OMETIFFFilter.OME_TIFF))
        			extension = OMETIFFFilter.OME_TIFF;
        		else {
        			lastDot = s.lastIndexOf(".");
        			if (lastDot != -1)
        				extension = s.substring(lastDot, s.length());
        		}
        		if (extension != null) value = value+extension;
    		}
    		return getFileName(files, value, value, dirPath, 1, extension);
    	}
    	value = file.getName().getValue();
    	return getFileName(files, value, value, dirPath, 1, null);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer		The viewer this data loader is for.
     *               		Mustn't be <code>null</code>.
     * @param registry		Convenience reference for subclasses.
     * @param parameters  	The parameters used to export the image.
     */
    public DownloadActivity(UserNotifier viewer, Registry registry,
    		DownloadActivityParam parameters)
    {
    	super(viewer, registry, "Download", parameters.getIcon());
		if (parameters == null)
			throw new IllegalArgumentException("Parameters not valid.");
		this.parameters = parameters;
		File folder = parameters.getFolder();
    	File directory = folder.getParentFile();
		messageLabel.setText(directory+File.separator+getFileName());
    }
    
	/**
	 * Creates a concrete loader.
	 * @see ActivityComponent#createLoader()
	 */
	protected UserNotifierLoader createLoader()
	{
		OriginalFile f = parameters.getFile();
		File folder = parameters.getFolder();
    	File directory = folder.getParentFile();
		return new FileLoader(viewer, registry, 
				directory+File.separator+getFileName(), 
				f.getId().getValue(), f.getSize().getValue(), this);
	}

	/**
	 * Modifies the text of the component. 
	 * @see ActivityComponent#notifyActivityEnd()
	 */
	protected void notifyActivityEnd() { type.setText(DESCRIPTION); }

}
