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

//Third-party libraries

//Application-internal dependencies
import ome.adapters.pojos.MapperBlock;
import ome.model.IObject;
import ome.model.annotations.DatasetAnnotation;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.internal.Details;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;

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
    extends DataObject
{
    public final static String NAME = Dataset.NAME;
    public final static String DESCRIPTION = Dataset.DESCRIPTION;
    public final static String IMAGE_LINKS = Dataset.IMAGELINKS;
    public final static String PROJECT_LINKS = Dataset.PROJECTLINKS;
    public final static String ANNOTATIONS = Dataset.ANNOTATIONS;
    
    /** 
     * The Dataset's name.
     * This field may not be <code>null</code>.  
     */
    private String   name;
    
    /** The Dataset's description. */
    private String   description;
    
    /** 
     * All the Images contained in this Dataset.
     * The elements of this set are {@link ImageData} objects.  If this
     * Dataset contains no Images, then this set will be empty &#151;
     * but never <code>null</code>. 
     */
    private Set      images;
    
    /** 
     * All the Projects that contain this Dataset.
     * The elements of this set are {@link ProjectData} objects.  If this
     * Dataset is not contained in any Project, then this set will be empty
     * &#151; but never <code>null</code>. 
     */
    private Set      projects;
    
    /**
     * All the annotations related to this Dataset.
     * The elements of the set are {@link AnnotationData} objetcs.
     * If this Dataset hasn't been annotated, then this set will be empty
     * &#151; but never <code>null</code>. 
     */
    private Set      annotations;
    
    /** 
     * The Experimenter that owns this Dataset.
     * This field may not be <code>null</code>.  
     */
    private ExperimenterData owner;
    
    /** 
     * The number of annotations attached to this Dataset.
     * This field may be <code>null</code> meaning no count retrieved,
     * and it may be less than the actual number if filtered by user.
     */
    private Integer annotationCount;
    
    public void copy(IObject model, ModelMapper mapper) {
    	if (model instanceof Dataset) {
			Dataset d = (Dataset) model;
            super.copy(model,mapper);

            // Details
            if (d.getDetails() != null){
                
                Details details = d.getDetails();
                this.setOwner((ExperimenterData)mapper.findTarget(
                        details.getOwner()));         
                if ( details.getCounts() != null )
                {
                    Object annotationCount = details.getCounts().get( Dataset.ANNOTATIONS );
                    if ( annotationCount instanceof Integer )
                        this.setAnnotationCount( (Integer) annotationCount  );
                }

           }
            
            // Fields
			this.setName(d.getName());
			this.setDescription(d.getDescription());
            
            // Collections
            MapperBlock block = new MapperBlock( mapper );
            setImages( new HashSet( d.collectFromImageLinks( block )));
            setProjects( new HashSet( d.collectFromProjectLinks( block )));
            setAnnotations( new HashSet( d.collectFromAnnotations( block )));
            
		} else {
			throw new IllegalArgumentException(
                    "DatasetData can only copy from Dataset");
		}
    }

    public IObject newIObject()
    {
        return new Dataset();
    }
    
    public IObject fillIObject( IObject obj, ReverseModelMapper mapper)
    {
        if ( obj instanceof Dataset)
        {
            Dataset d = (Dataset) obj;
          
            if (super.fill(d)) {
                d.setName(this.getName());
                d.setDescription(this.getDescription());
                if (this.getImages() != null) {
                    for (Iterator it = this.getImages().iterator(); it.hasNext();)
                    {
                        ImageData i = (ImageData) it.next();
                        d.linkImage((Image) mapper.map(i));
                    }
                }
                
                if (this.getProjects() != null) {
                    for (Iterator it = this.getProjects().iterator(); it.hasNext();)
                    {
                        ProjectData p = (ProjectData) it.next();
                        d.linkProject((Project) mapper.map(p));
                    }
                }
                
                if (this.getAnnotations() != null) {
                    for (Iterator it = this.getAnnotations().iterator(); it.hasNext();)
                    {
                        AnnotationData ann = (AnnotationData) it.next();
                        d.addToAnnotations( (DatasetAnnotation) mapper.map(ann));
                    }
                }
                
            }
            return d;
            
        } else {
            
            throw new IllegalArgumentException(
                    "DatasetData can only fill Dataset.");
            
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

	public void setImages(Set images) {
		this.images = images;
	}

	public Set getImages() {
		return images;
	}

	public void setProjects(Set projects) {
		this.projects = projects;
	}

	public Set getProjects() {
		return projects;
	}

	public void setAnnotations(Set annotations) {
		this.annotations = annotations;
	}

	public Set getAnnotations() {
		return annotations;
	}

	public void setOwner(ExperimenterData owner) {
		this.owner = owner;
	}

	public ExperimenterData getOwner() {
		return owner;
	}

    public Integer getAnnotationCount()
    {
        return annotationCount;
    }
    
    public void setAnnotationCount(Integer annotationCount)
    {
        this.annotationCount = annotationCount;
    }
	
}
