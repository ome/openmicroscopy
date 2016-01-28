/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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


import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.filechooser.FileSystemView;

import omero.grid.RepositoryPrx;
import omero.gateway.model.DataObject;
import omero.gateway.model.FileData;
import omero.gateway.model.ImageData;


/** 
 * Implementation following Swing FileSystemView.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class FSFileSystemView 
{

	/** Indicates that no name was set. */
	private static final String NO_NAME = "NoName";//NONAMESET.value;
	
	/** Reference to the repositories. */
	private Map<FileData, RepositoryPrx> repositories;
	
	/** The id of the user the directory structure is for. */ 
	private long userID;
	
	/** Default configuration. */
	//private RepositoryListConfig config;
	
	/**
	 * Returns the repository corresponding to the passed file.
	 * 
	 * @param file The file to handle.
	 * @return See above.
	 */
    private Entry getRepository(DataObject file)
    {
    	Entry entry;
    	Iterator i;
    	String path;
    	FileData data;
    	String refPath;
    	if (file instanceof ImageData) {
    		ImageData img = (ImageData) file;
    		refPath = img.getPathToFile();
    		if (img.getIndex() >= 0) refPath = img.getParentFilePath();
        	i = repositories.entrySet().iterator();

        	while (i.hasNext()) {
    			entry = (Entry) i.next();
    			data = (FileData) entry.getKey();
    			path = data.getAbsolutePath();
    			if (refPath.startsWith(path)) 
    				return entry;
    		}
    	} else if (file instanceof FileData) {
    		FileData f = (FileData) file;
        	refPath = f.getAbsolutePath();
        	i = repositories.entrySet().iterator();
        	while (i.hasNext()) {
    			entry = (Entry) i.next();
    			data = (FileData) entry.getKey();
    			path = data.getAbsolutePath();
    			if (refPath.startsWith(path)) 
    				return entry;
    		}
    	}
    	return null;
    }

	/** 
	 * Creates a new instance.
	 * 
	 * @param userID	   The id of the user the directory structure if for.
	 * @param repositories The repositories. Mustn't be <code>null</code>.
	 */
	FSFileSystemView(long userID, Map<FileData, RepositoryPrx> repositories)
	{
		if (repositories == null)
			throw new IllegalArgumentException("No repositories specified.");
		this.userID = userID;
		this.repositories = repositories;
	}

	/**
	 * Returns the id of the user the directory structure is for.
	 * 
	 * @return See above.
	 */
	public long getUserID() { return userID; }
	
    /**
     * Checks if the file is the root.
     *
     * @param f The file to handle.
     * @return See above.
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
     * Registers the passed file. Returns the updated data object.
     * 
     * @param file The file to register.
     * @return See above.
     */
    public DataObject register(DataObject file)
    	throws FSAccessException
    {
    	/*
    	if (file == null) return null;
    	if (!(file instanceof FileData || file instanceof ImageData)) 
    		return null;
    	if (file.getId() > 0) return file;
    	Entry entry = getRepository(file);
    	if (entry == null) return null;
    	RepositoryPrx proxy = (RepositoryPrx) entry.getValue();
    	String value;
    	String name;
    	IObject r;
    	List<Image> images;
    	OriginalFile of;
    	List<IObject> objects;
    	if (file instanceof ImageData) {
    		ImageData img = (ImageData) file;
    		try {
    			images = new ArrayList<Image>();
    			images.add(img.asImage());
    			objects = proxy.registerFileSet(img.getReference(), images);
    			if (objects != null && objects.size() > 1)
    				img.setRegisteredFile((Image) objects.get(1));
    			return img;
			} catch (Exception e) {
				throw new FSAccessException("Cannot register the image: " +
						""+img.getName(), e);
			}
    	} else if (file instanceof MultiImageData) {
    		MultiImageData mi = (MultiImageData) file;
    		of = (OriginalFile) mi.asIObject();
    		List<ImageData> files = mi.getComponents();
    		//sort then by index.
    		sortImageByIndex(files);
    		images = new ArrayList<Image>();
    		Iterator<ImageData> i = files.iterator();
    		while (i.hasNext()) {
				images.add(i.next().asImage());
			}
    		try {
    			int index = 0;
    			objects = proxy.registerFileSet(of, images);
    			mi.setRegisteredFile((OriginalFile) objects.get(index));
    			i = files.iterator();
    			ImageData data;
    			index++;
    			while (i.hasNext()) {
					data = i.next();
					data.setRegisteredFile((Image) objects.get(index));
					index++;
				}
    			return mi;
			} catch (Exception e) {
				throw new FSAccessException(
						"Cannot register the multi-images file:" +
						" "+mi.getName(), e);
			}
    	}  else if (file instanceof FileData) {
    		FileData f = (FileData) file;
    		of = (OriginalFile) file.asIObject();
    		try {
    			r = proxy.registerOriginalFile(of);
    			f.setRegisteredFile((OriginalFile) r);
    			return f;
			} catch (Exception e) {
				throw new FSAccessException("Cannot register the file: " +
						""+f.getAbsolutePath(), e);
			}
    	}
    	*/
    	return null;
    }
    
    /**
     * Returns the path to the thumbnail.
     * 
     * @param object The object to handle.
     * @return See above.
     * @throws FSAccessException
     */
    public String getThumbnail(DataObject object)
    	throws FSAccessException
    {
    	/*
    	if (object == null) return null;
    	Entry entry;
    	RepositoryPrx proxy;
    	FileData root;
    	if (object instanceof ImageData) {
    		ImageData img = (ImageData) object;
    		String name = img.getPathToFile();
    		int index = img.getIndex();
    		if (index >= 0) name = img.getParentFilePath();
        	entry = getRepository(object);
        	if (entry == null) return null;
        	proxy = (RepositoryPrx) entry.getValue();
        	try {
        		if (index >= 0) 
        			return proxy.getThumbnailByIndex(name, index);
        		return proxy.getThumbnail(name);
    		} catch (Exception e) {
    			throw new FSAccessException("Cannot retrieve the thumbnail " +
    					"for: "+name, e);
    		}
    	}
    	*/
    	return null;
    }
    
    /**
     * Returns the files contained in the passed directory.
     * 
     * @param dir 			The directory to handle.
     * @param useFileHiding Pass <code>true</code> to return the files not
     * 						hidden, <code>false</code> otherwise.
     */
    public DataObject[] getFiles(FileData dir, boolean useFileHiding)
    	throws FSAccessException
    {
    	/*
    	if (dir == null) return null;
    	if (!dir.isDirectory()) return null;
    	Entry entry = getRepository(dir);
    	if (entry == null) return null;
    	Vector<DataObject> files = new Vector<DataObject>();
    	try {
    		//reset the config, if needed
    		String s = dir.getAbsolutePath();
    		FileData root = (FileData) entry.getKey();
    		RepositoryPrx proxy = (RepositoryPrx) entry.getValue();
    		populate(root, files, proxy.listFileSets(s, config));
		} catch (Exception e) { 
			throw new FSAccessException(
					"Cannot retrieves the files contained in: " +
					dir.getAbsolutePath(), e);
		}
    	return (DataObject[]) files.toArray(new DataObject[files.size()]);
    	*/
    	return null;
    }
    
    /**
     * Returns <code>true</code> if the file is hidden, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    public boolean isHiddenFile(FileData f) { return f.isHidden(); }

}
