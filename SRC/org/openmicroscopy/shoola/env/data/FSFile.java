/*
 * org.openmicroscopy.shoola.agents.fsimporter.util.FSFile 
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
import java.net.URI;

//Third-party libraries

//Application-internal dependencies

/** 
 * File to connect to an OMERO.fs instance.
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
public class FSFile 
	extends File
{

	/** The URI for the file. */
	private URI uri;
	
	/**
	 * Creates a new <code>File</code> instance by converting the given
     * <code>file:</code> URI into an abstract pathname.
     * 
	 * @param uri 	An absolute, hierarchical URI with a scheme equal to
     *         		<code>"file"</code>, a non-empty path component, 
     *         		and undefined authority, query, and fragment components
	 */
	public FSFile(URI uri)
	{
		super(uri == null? null : uri.toString());
		this.uri = uri;
	}
	
	/**
	 * Overridden to return the actual time.
	 * @see File#lastModified()
	 */
	public long lastModified()
    {
        return System.currentTimeMillis();
    }
	
	/**
	 * Overridden to return <code>true</code>.
	 * @see File#isAbsolute()
	 */
    public boolean isAbsolute() { return true; }

    /**
	 * Overridden to return <code>true</code>.
	 * @see File#isDirectory()
	 */
    public boolean isDirectory()
    {
    	if (uri == null) return false;
        return uri.getPath().endsWith("/");
    }

    /**
	 * Overridden to the path of the <code>URI</code>.
	 * @see File#getPath()
	 */
    public String getPath()
    {
    	if (uri == null) return null;
        return uri.getPath();
    }
    
    /**
	 * Overridden to the path of the <code>URI</code>.
	 * @see File#getAbsolutePath()
	 */
    public String getAbsolutePath() { return getPath(); }
    
    /**
	 * Overridden to return the correct name of the file.
	 * @see File#getName()
	 */
    public String getName()
    {
    	if (uri == null) return null;
        File f = new File(getPath());
        return f.getName();
    }

    /**
	 * Overridden to return the correct name of the file.
	 * @see File#getParent()
	 */
    public String getParent()
    {
    	if (uri == null) return null;
        File realFile = new File(getPath());
        return realFile.getParent();
    }
    
    /**
     * Overridden to make sure that the file is a <code>FSFile</code>.
     * @see File#equals(Object)
     */
    public boolean equals(Object object)
    {
        if (object instanceof FSFile) {
        	FSFile to = (FSFile) object;
            if (to.getPath().equals(getPath()))
                return true;
        }
        return super.equals(object);
    }
    
    
    /**
     * Overridden to make sure that the file is a <code>FSFile</code>.
     * @see File#compareTo(File)
     */
    public int compareTo(File file)
    {
        String path = file.getPath();
        if (file instanceof FSFile) {
        	FSFile f = (FSFile) file;
            path = f.getPath();
        }
        return getPath().compareTo(path)-1;
    }
    
}
