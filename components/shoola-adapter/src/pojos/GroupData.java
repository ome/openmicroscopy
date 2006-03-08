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

//Java imports
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.adapters.pojos.MapperBlock;
import ome.model.IObject;
import ome.model.containers.CategoryGroup;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;

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
    extends DataObject
{
    
    public final static String NAME = ExperimenterGroup.NAME;
    public final static String DESCRIPTION = ExperimenterGroup.DESCRIPTION;
    public final static String GROUP_EXPERIMENTER_MAP = ExperimenterGroup.GROUPEXPERIMENTERMAP;
    
    /** The Group's name. */
    private String      name;

    /** All experimenters in this group */
    private Set         experimenters;

    /** The owner for this group */
    private ExperimenterData owner;
    
    /** The contact for this group */
    // private ExperimenterData contact;
    
    public void copy(IObject model, ModelMapper mapper) {
    	if (model instanceof ExperimenterGroup) {
			ExperimenterGroup grp = (ExperimenterGroup) model;
            super.copy(model,mapper);

            // Details
            if (grp.getDetails() != null){
                this.setOwner((ExperimenterData) mapper.findTarget(grp.getDetails().getOwner()));
            }
            
            // Fields
			this.setName(grp.getName());

            // Collections
            MapperBlock block = new MapperBlock( mapper );
            setExperimenters( new HashSet(grp.eachLinkedExperimenter( block )));
    	} else {
			throw new IllegalArgumentException(
                    "GroupData can only copy from Group, not "+
                    model.getClass().getName()); // TODO all errors like this.
		}
    }

    public IObject newIObject()
    {
        return new ExperimenterGroup();
    }
    
    public IObject fillIObject( IObject obj, ReverseModelMapper mapper)
    {
        if ( obj instanceof ExperimenterGroup)
        {
            ExperimenterGroup g = (ExperimenterGroup) obj;
          
            if (super.fill(g)) {
                g.setName(this.getName());
                // TODO what to do with DESCRIPTION and other missing fields
                
                if (this.getExperimenters() != null){
                    for (Iterator it = this.getExperimenters().iterator(); it.hasNext();)
                    {
                        ExperimenterData e = (ExperimenterData) it.next();
                        g.linkExperimenter((Experimenter) mapper.map(e));
                    }
                }
                
            }
            return g;
            
        } else {
            
            throw new IllegalArgumentException(
                    "GroupData can only fill ExperimenterGroup.");
        }
    }
    
//    public ExperimenterData getContact()
//    {
//        return contact;
//    }
//
//    
//    public void setContact(ExperimenterData contact)
//    {
//        this.contact = contact;
//    }

    
    public Set getExperimenters()
    {
        return experimenters;
    }

    
    public void setExperimenters(Set experimenters)
    {
        this.experimenters = experimenters;
    }
    
    public ExperimenterData getOwner()
    {
        return owner;
    }

    
    public void setOwner(ExperimenterData owner)
    {
        this.owner = owner;
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
