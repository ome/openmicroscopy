/*
 * pojos.DatasetData
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

import ome.api.OMEModel;
import ome.model.Dataset;
import ome.model.DatasetAnnotation;
import ome.model.Image;
import ome.model.Project;
import ome.util.ModelMapper;

//Third-party libraries

//Application-internal dependencies

/** 
 * The data that makes up an <i>OME</i> Dataset along with links to its
 * contained Images and enclosing Project as well as the Experimenter that 
 * owns this Dataset.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/05/09 19:50:41 $)
 * </small>
 * @since OME2.2
 */
public class DatasetData
    implements DataObject
{

    /** The Dataset ID. */
    public int      id;
    
    /** 
     * The Dataset's name.
     * This field may not be <code>null</code>.  
     */
    public String   name;
    
    /** The Dataset's description. */
    public String   description;
    
    /** 
     * All the Images contained in this Dataset.
     * The elements of this set are {@link ImageData} objects.  If this
     * Dataset contains no Images, then this set will be empty &#151;
     * but never <code>null</code>. 
     */
    public Set      images;
    
    /** 
     * All the Projects that contain this Dataset.
     * The elements of this set are {@link ProjectData} objects.  If this
     * Dataset is not contained in any Project, then this set will be empty
     * &#151; but never <code>null</code>. 
     */
    public Set      projects;
    
    /**
     * All the annotations related to this Dataset.
     * The elements of the set are {@link AnnotationData} objetcs.
     * If this Dataset hasn't been annotated, then this set will be empty
     * &#151; but never <code>null</code>. 
     */
    public Set      annotations;
    
    /** 
     * The Experimenter that owns this Dataset.
     * This field may not be <code>null</code>.  
     */
    public ExperimenterData owner;
    
    public void copy(OMEModel model, ModelMapper mapper) {
    	if (model instanceof Dataset) {
			Dataset d = (Dataset) model;
			this.id=mapper.nullSafeInt(d.getDatasetId());
			this.name=d.getName();
			this.description=d.getDescription();
			this.images=(Set)mapper.createCollection(d.getImages());
			this.projects=(Set)mapper.createCollection(d.getProjects());
			this.annotations=(Set)mapper.createCollection(d.getDatasetAnnotations());
			this.owner=(ExperimenterData)mapper.findTarget(d.getExperimenter());
		} else {
			throw new IllegalArgumentException("DatasetData can only copy from Dataset");
		}
    }
}
