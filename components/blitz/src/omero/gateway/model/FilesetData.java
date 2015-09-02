/*
 * pojos.FilesetData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013-2014 University of Dundee & Open Microscopy Environment.
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
package omero.gateway.model;

//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.model.Fileset;
import omero.model.FilesetEntry;
import omero.model.Image;
import omero.model.OriginalFile;

/** 
 * Wraps a file set object.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class FilesetData
	extends DataObject
{
	
	/**
	 * Creates a new instance.
	 * 
	 * @param set The value to set.
	 */
	public FilesetData(Fileset set)
	{
		if (set == null)
			throw new IllegalArgumentException("No set specified");
		setValue(set);
	}
	
	/**
	 * Returns the collections of absolute paths.
	 * 
	 * @return See above.
	 */
	public List<String> getAbsolutePaths()
	{
		List<String> paths = new ArrayList<String>();
		Fileset set = (Fileset) asIObject();
		List<FilesetEntry> entries = set.copyUsedFiles();
		if (entries == null) return paths;
		Iterator<FilesetEntry> i = entries.iterator();
		OriginalFile f;
		StringBuffer buffer;
		while (i.hasNext()) {
			f = i.next().getOriginalFile();
			buffer = new StringBuffer();
			if (f.getPath() != null)
				buffer.append(f.getPath().getValue());
			if (f.getName() != null)
				buffer.append(f.getName().getValue());
			paths.add(buffer.toString());
		}
		return paths;
	}
	
	/**
	 * Returns the collection of file paths used for importing the files
	 * (in case of in-place imports these are the targets of the (sym)links)
	 * 
	 * @return See above.
	 */
	public List<String> getUsedFilePaths()
        {
	    List<String> paths = new ArrayList<String>();
            Fileset set = (Fileset) asIObject();
            List<FilesetEntry> entries = set.copyUsedFiles();
            Iterator<FilesetEntry> i = entries.iterator();
            while (i.hasNext()) {
                FilesetEntry next = i.next();
                paths.add(next.getClientPath().getValue());
            }
            return paths;
        }
	

	/**
	 * Returns the collection of images related to the file set.
	 *
	 * @return See above.
	 */
	public List<Long> getImageIds()
	{
		List<Long> ids = new ArrayList<Long>();
		Fileset fs = (Fileset) asIObject();
		List<Image> images = fs.copyImages();
		if (images == null || images.size() == 0) return ids;
		Iterator<Image> i = images.iterator();
		while (i.hasNext()) {
			ids.add(i.next().getId().getValue());
		}
		return ids;
	}

}
