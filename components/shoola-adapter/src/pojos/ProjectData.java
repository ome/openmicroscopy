/*
 * pojos.ProjectData
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
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.util.CBlock;


/** 
 * The data that makes up an <i>OME</i> Project along with links to its
 * contained Datasets and the Experimenter that owns this Project.
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
public class ProjectData
    extends DataObject
{
    
    public final static String NAME = Project.NAME;
    public final static String DESCRIPTION = Project.DESCRIPTION;
    public final static String DATASET_LINKS = Project.DATASETLINKS;
    
    // Constructors
    
    public ProjectData()
    {
        setDirty( true );
        setValue ( new Project() );
    }
    
    public ProjectData( Project value )
    {
        setValue ( value );
    }
    
    // Immutables
    
    public void setName(String name) {
        setDirty( true );
        asProject().setName( name );
    }

    public String getName() {
        return asProject().getName();
    }

    public void setDescription(String description) {
        setDirty( true );
        asProject().setDescription( description );
    }

    public String getDescription() {
        return asProject().getDescription();
    }

    // Lazy loaded Links

    private Set          datasets;
    
    public Set getDatasets() {
        if ( datasets == null && asProject().sizeOfDatasetLinks() >= 0 )
        {
            datasets = new HashSet( asProject().eachLinkedDataset( new CBlock() {
                public Object call(IObject object)
                {
                    return new DatasetData( (Dataset) object );
                }
            }));
        }
        
        return datasets == null ? null : new HashSet( datasets );
    }

    // Link mutations

    public void setDatasets( Set newValue ) 
    {
        Set currentValue = getDatasets(); 
        SetMutator m = new SetMutator( currentValue, newValue );
        
        while ( m.moreDeletions() )
        {
            setDirty( true );
            asProject().unlinkDataset( m.nextDeletion().asDataset() );
        }
        
        while ( m.moreAdditions() )
        {
            setDirty( true );
            asProject().linkDataset( m.nextAddition().asDataset() );
        }

        datasets = m.result();
        
    }
    
}
