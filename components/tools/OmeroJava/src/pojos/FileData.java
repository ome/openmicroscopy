/*
 * pojos.FileData 
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package pojos;

//Java imports
import java.io.File;

//Third-party libraries

//Application-internal dependencies
import omero.RTime;
import omero.model.Image;
import omero.model.IObject;
import omero.model.OriginalFile;

/** 
 * DataObject used to handle the file visible via FS.
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
public class FileData 
	extends DataObject
{

	/** The file representing the original file. */
	private File file;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param object The object to store.
	 */
	public FileData(IObject object)
	{
		if (!(object instanceof OriginalFile || object instanceof Image))
			throw new IllegalArgumentException("File not supported.");
		setValue(object);
		String path = null;
		if (object != null) {
			if (object instanceof OriginalFile) {
				path = ((OriginalFile) object).getName().getValue();
			} else if (object instanceof Image) {
				path = ((Image) object).getName().getValue();
			}
			
		}
		file = new File(path);
	}
	
	/**
	 * Sets the registered file.
	 * 
	 * @param object The object to store.
	 */
	public void setRegisteredFile(IObject object)
	{
		if (object == null) return;
		if (!(object instanceof OriginalFile || object instanceof Image))
			throw new IllegalArgumentException("File not supported.");
		//String v = getAbsolutePath();
		//String path = oFile.getName().getValue();
			
		//if (!v.equals(path)) return;
		setValue(object);
	}
	
	/**
	 * Returns the name of the file.
	 * 
	 * @return See above.
	 */
	public String getName() { return file.getName(); }
	
	/**
	 * Returns <code>true</code> if the file is hidden, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isHidden() { return file.isHidden(); }
	
	/**
	 * Returns <code>true</code> if the file is an image, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isImage()
	{
		IObject o = asIObject();
		return (o instanceof Image);
	}
	
	/**
	 * Returns when the file was last modified.
	 * 
	 * @return See above.
	 */
	public long lastModified()
	{
		IObject o = asIObject();
		if (o == null) return -1;
		if (o instanceof OriginalFile) {
			return ((OriginalFile) o).getCtime().getValue();
		} else if (o instanceof Image) {
			RTime time = ((Image) o).getAcquisitionDate();
	    	if (time == null) return -1;
	    	return time.getValue();
		}
		return -1;
	}
	
	/**
	 * Returns <code>true</code> if the file is a directory, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isDirectory() { return file.isDirectory(); }

	/**
	 * Returns <code>true</code> if the file is a file, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isFile() { return file.isFile(); }
	
	/**
     * Returns the absolute pathname string.
     * 
     * @return  See above.
     */
	public String getAbsolutePath() { return file.getAbsolutePath(); }
	
}
