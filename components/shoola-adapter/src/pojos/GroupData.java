/*
 * pojos.Experimenter
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package pojos;

import java.util.Set;

import ome.api.OMEModel;
import ome.model.Experimenter;
import ome.model.Group;
import ome.model.ModuleExecution;
import ome.util.ModelMapper;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * The data that makes up an <i>OME</i> Group along with the various members
 * of the Group
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class GroupData
    implements DataObject
{

    /** The Grop ID. */
    private int         id;
    
    /** The Group's name. */
    private String      name;

    /** All experimenters in this group */
    private Set         experimenters;

    /** The leader for this group */
    private ExperimenterData leader;
    
    /** The contact for this group */
    private ExperimenterData contact;
    
    public void copy(OMEModel model, ModelMapper mapper) {
    	if (model instanceof Group) {
			Group grp = (Group) model;
			if (grp.getAttributeId()!=null){
				this.setId(grp.getAttributeId().intValue());
			}
			this.setName(grp.getName());
			this.setLeader((ExperimenterData) mapper.findTarget(grp.getLeader()));
            this.setContact((ExperimenterData) mapper.findTarget(grp.getContact()));
            this.setExperimenters((Set) mapper.findCollection(grp.getExperimenters()));
   	} else {
			throw new IllegalArgumentException("GroupData can only copy from Group, not "+model.getClass().getName());
		}
    }
	
	public String toString() {
		return getClass().getName()+":"+getName()+" (id="+getId()+")";
	}

    
    public ExperimenterData getContact()
    {
        return contact;
    }

    
    public void setContact(ExperimenterData contact)
    {
        this.contact = contact;
    }

    
    public Set getExperimenters()
    {
        return experimenters;
    }

    
    public void setExperimenters(Set experimenters)
    {
        this.experimenters = experimenters;
    }

    
    public int getId()
    {
        return id;
    }

    
    public void setId(int id)
    {
        this.id = id;
    }

    
    public ExperimenterData getLeader()
    {
        return leader;
    }

    
    public void setLeader(ExperimenterData leader)
    {
        this.leader = leader;
    }

    
    public String getName()
    {
        return name;
    }

    
    public void setName(String name)
    {
        this.name = name;
    }
}
