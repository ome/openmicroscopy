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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.annotations.DatasetAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.util.CBlock;

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
    
    /** Identifies the {@link Dataset#NAME} field. */
    public final static String NAME = Dataset.NAME;
    
    /** Identifies the {@link Dataset#DESCRIPTION} field. */
    public final static String DESCRIPTION = Dataset.DESCRIPTION;
    
    /** Identifies the {@link Dataset#IMAGELINKS} field. */
    public final static String IMAGE_LINKS = Dataset.IMAGELINKS;
    
    /** Identifies the {@link Dataset#PROJECTLINKS} field. */
    public final static String PROJECT_LINKS = Dataset.PROJECTLINKS;
    
    /** Identifies the {@link Dataset#ANNOTATIONS} field. */
    public final static String ANNOTATIONS = Dataset.ANNOTATIONS;
    
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
     * The number of annotations attached to this Dataset.
     * This field may be <code>null</code> meaning no count retrieved,
     * and it may be less than the actual number if filtered by user.
     */
    private Integer annotationCount;

    /** Creates a new instance. */
    public DatasetData()
    {
        setDirty(true);
        setValue(new Dataset());
    }
    
    /**
     * Creates a new instance.
     * 
     * @param dataset   Back pointer to the {@link Dataset} model object.
     *                  Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If the object is <code>null</code>.
     */
    public DatasetData(Dataset dataset)
    {
        if (dataset == null)
            throw new IllegalArgumentException("Object cannot null.");
        setValue(dataset);
    }
    
    // IMMUTABLES
    
    /**
     * Sets the name of the dataset.
     * 
     * @param name The name of the dataset. Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If the name is <code>null</code>.
     */
    public void setName(String name)
    {
        if (name == null) 
            throw new IllegalArgumentException("The name cannot be null.");
        setDirty(true);
        asDataset().setName(name);
    }

    /** 
     * Returns the name of the dataset.
     * 
     * @return See above.
     */
    public String getName() { return asDataset().getName(); }

    /**
     * Sets the description of the dataset.
     * 
     * @param description The description of the dataset.
     */
    public void setDescription(String description)
    {
        setDirty(true);
        asDataset().setDescription(description);
    }

    /**
     * Returns the description of the dataset.
     * 
     * @return See above.
     */
    public String getDescription() { return asDataset().getDescription(); }

    // Lazy loaded links
    
    /**
     * Returns a set of images contained in the dataset.
     *
     * @return See above.
     */
    public Set getImages()
    {
        if (images == null && asDataset().sizeOfImageLinks() >= 0) {
            images = new HashSet(asDataset().eachLinkedImage(new CBlock()
            {
                public Object call(IObject object)
                {
                    return new ImageData((Image) object);
                }
            }));
        }
        return images == null ? null : new HashSet(images);
    }

    /**
     * Returns a set of projects containing the dataset.
     * 
     * @return See above.
     */
    public Set getProjects()
    {
        if (projects == null && asDataset().sizeOfProjectLinks() >= 0) {
            projects = new HashSet( asDataset().eachLinkedProject(
                    new CBlock () {
                public Object call(IObject object) 
                {
                    return new ProjectData((Project) object); 
                };
            }));
        }
        
        return projects == null ? null : new HashSet(projects);
    }

    // Link mutations
    
    /**
     * Sets the images contained in this dataset.
     * 
     * @param newValue The set of images.
     */
    public void setImages(Set newValue) 
    {
        Set currentValue = getImages(); 
        SetMutator m = new SetMutator(currentValue, newValue);
        
        while (m.moreDeletions()) {
            setDirty(true);
            asDataset().unlinkImage(m.nextDeletion().asImage());
        }
        
        while (m.moreAdditions()) {
            setDirty(true);
            asDataset().linkImage(m.nextAddition().asImage());
        }

        images = m.result();
    }

    /**
     * Sets the projects containing the dataset.
     * 
     * @param newValue The set of projects.
     */
    public void setProjects(Set newValue) 
    {
        Set currentValue = getProjects(); 
        SetMutator m = new SetMutator(currentValue, newValue);
        
        while (m.moreDeletions()) {
            setDirty(true);
            asDataset().unlinkProject(m.nextDeletion().asProject());
        }
        
        while (m.moreAdditions())
        {
            setDirty(true);
            asDataset().linkProject(m.nextAddition().asProject());
        }

        projects = m.result();
    }

    
    // SETS
    /**
     * Returns the annotations related to this dataset. Not sure we
     * are going to keep this method.
     * 
     * @return See Above
     */
    public Set getAnnotations()
    {
        
        if (annotations == null && asDataset().sizeOfAnnotations() >= 0) {
            annotations = new HashSet( asDataset().collectAnnotations(
                    new CBlock() {
               public Object call(IObject object)
                {
                   return new AnnotationData( (DatasetAnnotation) object );
                } 
            }));
        }
        
        return annotations == null ? null : new HashSet(annotations);
    }

    /**
     * Sets the annotations related to this dataset.
     * 
     * @param newValue The set of annotations.
     */
    public void setAnnotations(Set newValue) 
    {
        Set currentValue = getAnnotations(); 
        SetMutator m = new SetMutator(currentValue, newValue);
        
        while (m.moreDeletions()) {
            setDirty(true);
            asDataset().removeDatasetAnnotation(
                        m.nextDeletion().asDatasetAnnotation());
            annotationCount = annotationCount == null ? null :
                new Integer(annotationCount.intValue()-1);
        }
        
        while (m.moreAdditions()) {
            setDirty(true);
            asDataset().removeDatasetAnnotation(
                        m.nextAddition().asDatasetAnnotation());
            annotationCount = annotationCount == null ? null :
                new Integer(annotationCount.intValue()+1);
        }

        annotations = m.result();
    }

    /** 
     * Returns the number of dataset annotations done by the current user.
     * 
     * @return See above.
     */
    public Integer getAnnotationCount()
    {
        if (annotationCount == null)
            annotationCount = getCount(Dataset.ANNOTATIONS);
        return annotationCount;
    }
	
}
