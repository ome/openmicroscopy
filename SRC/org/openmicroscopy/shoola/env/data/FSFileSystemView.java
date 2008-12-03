/*
 * org.openmicroscopy.shoola.agents.fsimporter.util.FSFileSystemView 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data;


//Java imports
import java.io.File;
import java.io.IOException;
import java.net.URI;
import javax.swing.filechooser.FileSystemView;


//Third-party libraries

//Application-internal dependencies


/** 
 * 
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
public class FSFileSystemView 
	extends FileSystemView
{

	private static final String FS_NAME = "omero-fs://";
	
	/** The default file directory. */
	private FSFile defaultDirectory;
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param defaultPath
	 */
	FSFileSystemView(String defaultPath)
	{
		if (defaultPath != null) {
			try {
				defaultDirectory = new FSFile(new URI(FS_NAME+defaultPath));
			} catch (Exception e) {}
		}
	}
	
	/**
	 * Overridden to return a file of the correct type.
	 * @see FileSystemView#createNewFolder(File)
	 */
	public File createNewFolder(File containingDir) 
		throws IOException
	{
		return null;
	}

	/**
	 * Overridden to return a file of the correct type.
	 * @see FileSystemView#getParentDirectory(File)
	 */
	public File getParentDirectory(File dir)
	{
		if (dir == null) return null;
		FSFile file = (FSFile) dir;
	    File realFile = new File(file.getPath());
	    try {
	    	String parent = realFile.getParent();
	    	if (parent != null) {
	    		URI uri = new URI(FS_NAME+realFile.getParent());
	    		return new FSFile(uri);
	    	}
	    	return null;
	    } catch (Exception e) {}//ignore
	    return null;
	}
	
	/**
	 * Overridden to return <code>false</code>.
	 * @see FileSystemView#isComputerNode(File)
	 */
    public boolean isComputerNode(File dir) { return false; }

    /**
	 * Overridden to return <code>false</code>.
	 * @see FileSystemView#isDrive(File)
	 */
    public boolean isDrive(File dir) { return false; }
    
    /**
	 * Overridden to return <code>false</code>.
	 * @see FileSystemView#isFloppyDrive(File)
	 */
    public boolean isFloppyDrive(File dir) { return false; }
    
    /**
	 * Overridden to handle <code>FSFile</code>.
	 * @see FileSystemView#isRoot(File)
	 */
    public boolean isRoot(File f)
    {
        if (f instanceof FSFile) {
        	FSFile fsFile = (FSFile) f;
            f = new File(fsFile.getPath());
        }
        return super.isRoot(f);
    }
    
    /**
	 * Overridden to handle <code>FSFile</code>.
	 * @see FileSystemView#getRoots()
	 */
    public File[] getRoots()
    {
        try {
            return new FSFile[] { new FSFile(new URI(FS_NAME)) };
        } catch (Exception e) {}
        return null;
    }
    
    /**
     * Sets the default directory.
     * 
     * @param file The default directory.
     */
    public void setDefaultDirectory(File file)
    {
    	if (file == null) return;
    	if (file instanceof FSFile)
    		defaultDirectory = (FSFile) file;
    }
    
    /**
     * Returns the default directory.
     * 
     * @return See above.
     */
    public File getDefaultDirectory() { return defaultDirectory; }
    
}
