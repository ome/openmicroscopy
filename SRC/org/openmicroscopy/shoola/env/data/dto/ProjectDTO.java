/*
 * org.openmicroscopy.shoola.env.data.ProjectDTO
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

import java.util.List;
import java.util.Map;

import org.openmicroscopy.shoola.env.data.st.Experimenter;
import org.openmicroscopy.shoola.env.data.st.ExperimenterDTO;

public class ProjectDTO
    extends MappedDTO
    implements Project
{
    public ProjectDTO() { super(); }
    public ProjectDTO(Map elements) { super(elements); }

    protected void setMap(Map elements)
    {
        super.setMap(elements);
        parseChildElement("owner",ExperimenterDTO.class);
        parseListElement("datasets",DatasetDTO.class);
    }

    public String getName()
    { return getStringElement("name"); }
    public void setName(String name)
    { setElement("name",name); }

    public String getDescription()
    { return getStringElement("description"); }
    public void setDescription(String description)
    { setElement("name",description); }

    public Experimenter getOwner()
    { return (ExperimenterDTO) getObjectElement("owner"); }

    public void setOwner(Experimenter owner)
    { setElement("owner",owner); }

    public List getDatasets()
    { return (List) getObjectElement("datasets"); }

}
