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
import monitors.MonitorServerPrx;


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

	/** Default name. */
	static final String FS_NAME = "omero-fs://";
	
	/** The default file directory. */
	private FSFile 				defaultDirectory;
	
	/** Reference to the proxy. */
	private MonitorServerPrx 	server;
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param defaultPath	The default path.
	 * @param server		Reference to the monitor service.
	 */
	FSFileSystemView(String defaultPath, MonitorServerPrx server)
	{
		if (server == null)
			throw new IllegalArgumentException("No server specified.");
		this.server = server;
		if (defaultPath != null && defaultPath.trim().length() > 0) {
			try {
				String s = FS_NAME+defaultPath;
				defaultDirectory = new FSFile(new URI(s));
				setDefaultDirectory(defaultDirectory);
			} catch (Exception e) {}
		}
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
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }//ignore
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
    	if (f == null) return false;
    	/*
        if (f instanceof FSFile) {
        	FSFile fsFile = (FSFile) f;
            f = new File(fsFile.getPath());
        }*/
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
        return new FSFile[0];
    }
 
    /**
     *  Overridden to handle <code>FSFile</code>.
     *  @see FileSystemView#getFiles(File, boolean)
     */
    public File[] getFiles(File dir, boolean useFileHiding)
    {
    	if (dir == null) return null;
    	String root = dir.getPath();
    	if (dir instanceof FSFile) {
    		root = ((FSFile) dir).getPath();
    	}
    	FSFile[] files = null;
    	try {
    		String[] paths = server.getDirectory(root, "*");
    		if (paths == null) return files;
    		files = new FSFile[paths.length];
    		for (int i = 0; i < paths.length; i++)
				files[i] = new FSFile(new URI(FS_NAME+paths[i]));
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return files;
    }
    
    public Boolean isTraversable(File f) { return false;
        }
}
