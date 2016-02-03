/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
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

import static omero.rtypes.rstring;
import omero.model.Folder;
import omero.model.FolderI;

/**
 * Pojo wrapper for an <i>OME</i> Folder.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class FolderData extends DataObject {

    /** Creates a new instance. */
    public FolderData() {
        setDirty(true);
        setValue(new FolderI());
    }

    /**
     * Creates a new instance.
     *
     * @param folder
     *            Back pointer to the {@link Folder} model object. Mustn't be
     *            <code>null</code>.
     * @throws IllegalArgumentException
     *             If the object is <code>null</code>.
     */
    public FolderData(Folder folder) {
        if (folder == null) {
            throw new IllegalArgumentException("Object cannot null.");
        }
        setValue(folder);
    }

    /**
     * Get the name of the Folder
     * 
     * @return See above
     */
    public String getName() {
        omero.RString s = asFolder().getName();
        if (s == null)
            return null;
        return s.getValue();
    }

    /**
     * Set the name of the Folder
     * 
     * @param name
     *            The name
     */
    public void setName(String name) {
        asFolder().setName(rstring(name));
    }

    /**
     * Get the description of the Folder
     * 
     * @return See above
     */
    public String getDescription() {
        omero.RString s = asFolder().getDescription();
        if (s == null)
            return null;
        return s.getValue();
    }

    /**
     * Set the description of the Folder
     * 
     * @param desc
     *            The description
     */
    public void setDescription(String desc) {
        asFolder().setDescription(rstring(desc));
    }
    
    /**
     * Get the the parent folder
     * 
     * @return See above.
     */
    public FolderData getParentFolder() {
        Folder f = asFolder().getParentFolder();
        return f == null ? null : new FolderData(f);
    }
    
    /**
     * Set the the parent folder
     * 
     * @param parent
     *            The parent folder
     */
    public void setParentFolder(Folder parent) {
        Folder f = asFolder();
        f.setParentFolder(parent);
    }
    
    /**
     * Set the {@link Folder}
     * 
     * @param f
     *            The folder
     */
    public void setFolder(Folder f) {
        setValue(f);
    }
}
