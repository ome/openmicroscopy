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
import java.util.Set;

import ome.api.OMEModel;
import ome.model.Experimenter;
import ome.model.Project;
import ome.util.ModelMapper;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

//Third-party libraries

//Application-internal dependencies

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
    implements DataObject
{
    
    /** The Project ID. */
    private int      id;
    
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
    
    public void copy(OMEModel model, ModelMapper mapper) {
    	if (model instanceof Project) {
			Project p = (Project) model;
			this.setId(mapper.nullSafeInt(p.getProjectId()));
			this.setName(p.getName());
			this.setDescription(p.getDescription());
			this.setDatasets((Set) mapper.findCollection(p.getDatasets()));
			this.setOwner((ExperimenterData) mapper.findTarget(p.getExperimenter()));
		} else { 
			throw new IllegalArgumentException("ProjectData copies only from Project");
		}
    }

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
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

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
				.append("name", this.name)
				.toString();
	}
}
