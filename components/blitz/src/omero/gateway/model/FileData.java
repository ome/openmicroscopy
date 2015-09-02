/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package omero.gateway.model;

import omero.RString;
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
 * @since 3.0-Beta4
 */
public class FileData
    extends DataObject
{

    /** The description to associated to the object. */
    private String description;

    /** Flag indicating that the object corresponds to a directory or not. */
    private boolean directory;

    /** Flag indicating that the object is hidden or not. */
    private boolean hidden;

    /** The path of the repository. */
    private String repositoryPath;

    /**
     * Creates a new instance.
     *
     * @param object The object to store.
     */
    public FileData(OriginalFile object)
    {
        this(object, false, false);
    }

    /**
     * Creates a new instance.
     *
     * @param object The object to store.
     * @param directory Pass <code>true</code> if the object is a directory, 
     *                  <code>false</code> otherwise.
     */
    public FileData(OriginalFile object, boolean directory)
    {
        this(object, directory, false);
    }

    /**
     * Creates a new instance.
     * 
     * @param object The object to store.
     * @param directory Pass <code>true</code> if the object is a directory,
     *                  <code>false</code> otherwise.
     */
    public FileData(OriginalFile object, boolean directory, boolean hidden)
    {
        if (!(object instanceof OriginalFile))
            throw new IllegalArgumentException("File not supported.");
        setValue(object);
        this.directory = directory;
        this.hidden = hidden;
        repositoryPath = "";
    }

    /**
     * Sets the registered file.
     *
     * @param object The object to store.
     */
    public void setRegisteredFile(OriginalFile object)
    {
        if (object == null) return;
        if (!(object instanceof OriginalFile))
            throw new IllegalArgumentException("File not supported.");
        OriginalFile of = (OriginalFile) asIObject();
        of.setId(object.getId());
    }

    /**
     * Returns the description of the file.
     *
     * @return See above.
     */
    public String getDescription() { return description; }

    /**
     * Sets the description.
     *
     * @param description The value to set.
     */
    public void setDescription(String description)
    { 
        this.description = description;
    }

    /**
     * Sets the path to the parent.
     *
     * @param path The value to set.
     */
    public void setRepositoryPath(String path)
    {
        if (path == null) path = "";
        this.repositoryPath = path;
    }

    /**
     * Returns the name of the file.
     *
     * @return See above.
     */
    public String getName()
    { 
        OriginalFile of = (OriginalFile) asIObject();
        RString value = of.getName();
        if (value == null) return "";
        return value.getValue(); 
    }

    /**
     * Returns the (relative) path of the file.
     *
     * @return See above.
     */
    public String getPath()
    { 
        OriginalFile of = (OriginalFile) asIObject();
        RString value = of.getPath();
        if (value == null) return "";
        return value.getValue(); 
    }

    /**
     * Returns the absolute path.
     *
     * @return See above.
     */
    public String getAbsolutePath()
    {
        return repositoryPath+getPath()+getName();
    }

    /**
     * Returns <code>true</code> if the file is hidden, <code>false</code>
     * otherwise.
     *
     * @return See above.
     */
    public boolean isHidden() { return hidden; }

    /**
     * Returns when the file was last modified.
     *
     * @return See above.
     */
    public long lastModified()
    {
        IObject o = asIObject();
        if (o == null) return -1;
        if (o instanceof OriginalFile) 
            return ((OriginalFile) o).getCtime().getValue();
        return -1;
    }

    /**
     * Returns <code>true</code> if the file is a directory, <code>false</code>
     * otherwise.
     *
     * @return See above.
     */
    public boolean isDirectory() { return directory; }

}
