/*
 * org.openmicroscopy.shoola.env.data.Dataset
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */




/*------------------------------------------------------------------------------
 *
 * Written by:    Douglas Creager <dcreager@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */




package org.openmicroscopy.shoola.env.data.dto;

import java.util.Map;
import java.util.List;

import org.openmicroscopy.shoola.env.data.st.Experimenter;
import org.openmicroscopy.shoola.env.data.st.ExperimenterDTO;

/**
 * <p>Represents a collection of {@link Image Images}.  Datasets and
 * images form a many-to-map map, as to datasets and projects.  A
 * user's session usually has a single dataset selected as the "active
 * dataset".  Datasets also form the unit of analysis for the OME
 * analysis engine; {@link Chain Chains} are batch-executed against
 * all of the images in a dataset.</p>
 *
 * @author Douglas Creager (dcreager@alum.mit.edu)
 * @version 2.2
 * @since OME2.2
 */

public abstract class DatasetDTO
    extends MappedDTO
    implements Dataset
{
    public DatasetDTO() { super(); }
    public DatasetDTO(Map elements) { super(elements); }

    protected void setMap(Map elements)
    {
        super.setMap(elements);
        parseChildElement("owner",ExperimenterDTO.class);
        parseListElement("projects",ProjectDTO.class);
        //parseListElement("images",ImageDTO.class);
    }

    /**
     * Returns the name of this dataset.
     * @return the name of this dataset
     */
    public String getName()
    { return getStringElement("name"); }

    /**
     * Sets the name of this dataset.
     * @param name the name of this dataset
     */
    public void setName(String name)
    { setElement("name",name); }

    /**
     * Returns the description of this dataset.
     * @return the description of this dataset
     */
    public String getDescription()
    { return getStringElement("description"); }

    /**
     * Sets the description of this dataset.
     * @param description the description of this dataset
     */
    public void setDescription(String description)
    { setElement("description",description); }

    /**
     * Returns whether this dataset is locked.  A dataset must be
     * locked once it is analyzed; nothing is allowed to add or remove
     * images from a locked dataset.  (Its other properties, such and
     * name and description, however, can still be modified.)
     * @return whether this dataset is locked
     */
    public boolean isLocked()
    { return getBooleanElement("locked"); }

    /**
     * Sets whether this dataset is locked.  A dataset must be locked
     * once it is analyzed; nothing is allowed to add or remove images
     * from a locked dataset.  (Its other properties, such and name
     * and description, however, can still be modified.)
     * @param locked whether this dataset is locked
     */
    public void setLocked(boolean locked)
    { setElement("locked",new Boolean(locked)); }

    /**
     * Returns the owner of this dataset.  The {@link Attribute}
     * returned will be of the <code>Experimenter</code> semantic
     * type.
     * @return the owner of this dataset
     */
    public Experimenter getOwner()
    { return (ExperimenterDTO) getObjectElement("owner"); }

    /**
     * Sets the owner of this dataset.  The {@link Attribute} provided
     * must be of the <code>Experimenter</code> semantic type.
     * @param owner the owner of this dataset
     */
    public void setOwner(Experimenter owner)
    { setElement("owner",owner); }

    /**
     * Returns a list off the projects that this dataset belongs to.
     * @return a {@link List} of {@link Project Projects}
     */
    public List getProjects()
    { return (List) getObjectElement("projects"); }

    /**
     * Returns a list of the images in this dataset.
     * @return a {@link List} of {@link Image Images}
     */
    public List getImages()
    { return (List) getObjectElement("images"); }

}
