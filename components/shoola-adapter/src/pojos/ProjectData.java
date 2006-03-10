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
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.adapters.pojos.MapperBlock;
import ome.model.IObject;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;

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
    
    /** 
     * The Project's name.
     * This field may not be <code>null</code>. 
     */
    private String   name;
    
    /** The Project's description. */
    private String   description;
    
    /**
     * All the Datasets contained in this Project.
     * The elements of this set are {@link DatasetData} objects.  If this
     * Project contains no Datasets, then this set will be empty &#151;
     * but never <code>null</code>.
     */
    private Set          datasets;
    
    /** 
     * The Experimenter that owns this Project.
     * This field may not be <code>null</code>.  
     */
    private ExperimenterData owner;
    
    public void copy(IObject model, ModelMapper mapper) {
    	if (model instanceof Project) {
			Project p = (Project) model;
            
            super.copy(model,mapper);

            // Details 
            if (p.getDetails() != null){
                this.setOwner((ExperimenterData) 
                        mapper.findTarget(p.getDetails().getOwner()));
            }
            
            // Fields
            this.setName(p.getName());
            this.setDescription(p.getDescription());

            // Collections
            MapperBlock block = new MapperBlock( mapper );
            setDatasets( makeSet(
                    p.sizeOfDatasetLinks(),
                    p.eachLinkedDataset( block )));

		} else { 
			throw new IllegalArgumentException(
                    "ProjectData copies only from Project");
		}
    }

    public IObject newIObject()
    {
        return new Project();
    }
    
    public IObject fillIObject( IObject obj, ReverseModelMapper mapper)
    {
        if ( obj instanceof Project )
        {
            Project p = (Project) obj;
            if (super.fill(p)) {
                p.setName(this.getName());
                p.setDescription(this.getDescription());
                
                // TODO / NOTE: could also take care of this in the getters and setters.
                // further we could just store the IObject and put all the logic in 
                // the getters/setters!
                if (this.getDatasets() != null) {
                    for (Iterator it = this.getDatasets().iterator(); it.hasNext();)
                    {
                        DatasetData d = (DatasetData) it.next();
                        p.linkDataset((Dataset) mapper.map(d));
                    }
                }
                
            }
            return p;
        } else {
            throw new IllegalArgumentException("ProjectData can only fill Project.");
        }
    }
    
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDatasets(Set datasets) {
		this.datasets = datasets;
	}

	public Set getDatasets() {
		return datasets;
	}

	public void setOwner(ExperimenterData owner) {
		this.owner = owner;
	}

	public ExperimenterData getOwner() {
		return owner;
	}

}
