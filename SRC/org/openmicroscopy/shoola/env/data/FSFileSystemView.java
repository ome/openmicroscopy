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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.filechooser.FileSystemView;

//Third-party libraries

//Application-internal dependencies
import omero.grid.RepositoryPrx;
import omero.model.OriginalFile;
import pojos.FileData;


/** 
 * Implementation following Swing FileSystemView.
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
	//extends FileSystemView
{

	/** Reference to the repositories. */
	private Map<FileData, RepositoryPrx> repositories;
	
	/**
	 * Returns the repository corresponding to the passed file.
	 * 
	 * @param file The file to handle.
	 * @return See above.
	 */
    private RepositoryPrx getRepository(FileData file)
    {
    	if (isRoot(file))
    		return repositories.get(file);
    	String refPath = file.getAbsolutePath();
    	Entry entry;
    	Iterator i = repositories.entrySet().iterator();
    	String path;
    	FileData data;
    	while (i.hasNext()) {
			entry = (Entry) i.next();
			data = (FileData) entry.getKey();
			path = data.getAbsolutePath();
			if (refPath.startsWith(path)) 
					return (RepositoryPrx) entry.getValue();
		}
    	return null;
    }
    
	/** 
	 * Creates a new instance.
	 * 
	 * @param repositories The repositories. Mustn't be <code>null</code>.
	 */
	FSFileSystemView(Map<FileData, RepositoryPrx> repositories)
	{
		if (repositories == null)
			throw new IllegalArgumentException("No repositories specified.");
		this.repositories = repositories;
	}

    /**
	 * Overridden to handle <code>FileData</code>.
	 * @see FileSystemView#isRoot(FileData)
	 */
    public boolean isRoot(FileData f)
    {
    	if (f == null) return false;
    	Entry entry;
    	Iterator i = repositories.entrySet().iterator();
    	String path;
    	FileData data;
    	while (i.hasNext()) {
			entry = (Entry) i.next();
			data = (FileData) entry.getKey();
			path = data.getAbsolutePath();
			if (path.equals(f.getAbsolutePath()) && data.getId() == f.getId())
				return true;
		}
    	return false;
    }
    
    /**
	 * Returns the roots.
	 * @see FileSystemView#getRoots()
	 */
    public FileData[] getRoots()
    {
    	FileData[] files = new FileData[repositories.size()];
    	Entry entry;
    	Iterator i = repositories.entrySet().iterator();
    	int index = 0;
    	while (i.hasNext()) {
			entry = (Entry) i.next();
			files[index] = (FileData) entry.getKey();
			index++;
		}
    	
        return files;
    }
    
    /**
     * Returns the files contained in the passed directory.
     * 
     * @param dir 			The directory to handle.
     * @param useFileHiding Pass <code>true</code> to return the files not
     * 						hidden, <code>false</code> otherwise.
     *  @see FileSystemView#getFiles(FileData, boolean)
     */
    public FileData[] getFiles(FileData dir, boolean useFileHiding)
    {
    	if (dir == null) return null;
    	if (!dir.isDirectory()) return null;
    	RepositoryPrx proxy = getRepository(dir);
    	if (proxy == null) return null;
    	Vector<FileData> files = new Vector<FileData>();
    	try {
    		List<OriginalFile> list = proxy.list(dir.getAbsolutePath());
    		if (list == null) return null;
    		Iterator<OriginalFile> i = list.iterator();
    		FileData f;
    		//OriginalFileI of;
    		while (i.hasNext()) {
    			//of = new OriginalFileI();
    			//of.setName(omero.rtypes.rstring(i.next()));
				f = new FileData(i.next());
				if (!useFileHiding) {
					if (!isHiddenFile(f)) files.addElement(f);
				} else files.addElement(f);
			}
		} catch (Exception e) { 
			e.printStackTrace();
		}
    	return (FileData[]) files.toArray(new FileData[files.size()]);
    }
    
    /**
     * Returns <code>true</code> if the file is hidden, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    public boolean isHiddenFile(FileData f) { return f.isHidden(); }
    
}
