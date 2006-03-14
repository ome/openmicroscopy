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

// Java imports
import java.util.HashSet;
import java.util.Set;

// Third-party libraries

// Application-internal dependencies
import ome.model.IObject;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.util.CBlock;

/**
 * The data that makes up an <i>OME</i> Group along with the various members of
 * the Group
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
 *          </small>
 * @since OME2.2
 */
public class GroupData extends DataObject
{

    public final static String NAME                   = ExperimenterGroup.NAME;

    public final static String DESCRIPTION            = ExperimenterGroup.DESCRIPTION;

    public final static String GROUP_EXPERIMENTER_MAP = ExperimenterGroup.GROUPEXPERIMENTERMAP;

    /** All experimenters in this group */
    private Set                experimenters;

    public GroupData()
    {
        setDirty( true );
        setValue( new ExperimenterGroup() );
    }
    
    public GroupData( ExperimenterGroup value )
    {
        setValue( value );
    }
    
    // Immutables
    
    public String getName()
    {
        return asGroup().getName();
    }

    public void setName(String name)
    {
        setDirty( true );
        asGroup().setName( name );
    }

    // Lazy loaded links
    
    public Set getExperimenters()
    {
        
        if ( experimenters == null && asGroup().sizeOfGroupExperimenterMap() >= 0 )
        {
            experimenters = new HashSet( asGroup().eachLinkedExperimenter( new CBlock(){
                public Object call(IObject object) {
                    return new ExperimenterData( (Experimenter) object );
                };
            }));
        }
        
        return experimenters == null ? null : new HashSet( experimenters );
    }

    // Link mutations
    
    public void setExperimenters( Set newValue )
    {
        Set currentValue = getExperimenters(); 
        SetMutator m = new SetMutator( currentValue, newValue );
        
        while ( m.moreDeletions() )
        {
            setDirty( true );
            asGroup().unlinkExperimenter( m.nextDeletion().asExperimenter() );
        }
        
        while ( m.moreAdditions() )
        {
            setDirty( true );
            asGroup().linkExperimenter( m.nextAddition().asExperimenter() );
        }

        experimenters = m.result();
        
    }

}
