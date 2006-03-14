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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.util.CBlock;

/** 
 * The data that makes up an <i>OME</i> Experimenter along with information
 * about the Group the Experimenter belongs in.
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
public class ExperimenterData
    extends DataObject
{
    
    public final static String FIRSTENAME = Experimenter.FIRSTNAME;
    public final static String MIDDLENAME = Experimenter.MIDDLENAME;
    public final static String LASTNAME = Experimenter.LASTNAME;
    public final static String EMAIL = Experimenter.EMAIL;
    public final static String OMENAME = Experimenter.OMENAME;
    public final static String INSTITUTION = Experimenter.INSTITUTION;
    public final static String GROUP_EXPERIMENTER_MAP = Experimenter.GROUPEXPERIMENTERMAP;
    
    /** The other Groups this Experimenter belongs in. */
    private Set         groups;

    public ExperimenterData()
    {
        setDirty( true );
        setValue( new Experimenter() );
    }
    
    public ExperimenterData( Experimenter value )
    {
        setValue( value );
    }
    
    // Immutables
    
    public void setFirstName(String firstName) {
        setDirty( true );
        asExperimenter().setFirstName( firstName );
    }

    public String getFirstName() {
        return asExperimenter().getFirstName();
    }

    public void setLastName(String lastName) {
        setDirty( true );
        asExperimenter().setLastName( lastName );
    }

    public String getLastName() {
        return asExperimenter().getLastName();
    }

    public void setEmail(String email) {
        setDirty( true );
        asExperimenter().setEmail( email );
    }

    public String getEmail() {
        return asExperimenter().getEmail();
    }

    public void setInstitution(String institution) {
        setDirty( true );
        asExperimenter().setInstitution( institution );
    }

    public String getInstitution() {
        return asExperimenter().getInstitution();
    }

    // Lazy loaded links

    public Set getGroups() {
        
        if ( groups == null && asExperimenter().sizeOfGroupExperimenterMap() >= 0 )
        {
            groups = new HashSet( asExperimenter()
                    .eachLinkedExperimenterGroup(new CBlock() {
                        public Object call(IObject object) {
                            return new GroupData( (ExperimenterGroup) object );
                        }
                    }));
        }
        
        return groups == null ? null : new HashSet( groups );
    }

    // Link mutations
    
    public void setGroups( Set newValue ) 
    {
        
        Set currentValue = getGroups(); 
        SetMutator m = new SetMutator( currentValue, newValue );
        
        while ( m.moreDeletions() )
        {
            setDirty( true );
            asExperimenter().unlinkExperimenterGroup( m.nextDeletion().asGroup() );
        }
        
        while ( m.moreAdditions() )
        {
            setDirty( true );
            asExperimenter().linkExperimenterGroup( m.nextAddition().asGroup() );
        }

        groups = m.result();
        
    }

}
