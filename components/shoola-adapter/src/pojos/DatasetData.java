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
    public final static String NAME = Dataset.NAME;
    public final static String DESCRIPTION = Dataset.DESCRIPTION;
    public final static String IMAGE_LINKS = Dataset.IMAGELINKS;
    public final static String PROJECT_LINKS = Dataset.PROJECTLINKS;
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

    public DatasetData()
    {
        setDirty( true );
        setValue( new Dataset() );
    }
    
    public DatasetData( Dataset value )
    {
        setValue( value );
    }
    
    // IMMUTABLES
    
    public void setName(String name) {
        setDirty( true );
        asDataset().setName( name );
    }

    public String getName() {
        return asDataset().getName();
    }

    public void setDescription(String description) {
        setDirty( true );
        asDataset().setDescription( description );
    }

    public String getDescription() {
        return asDataset().getDescription();
    }

    // Lazy loaded links
    
    public Set getImages() {
        if (images == null && asDataset().sizeOfImageLinks() >= 0 )
        {
            images = new HashSet(asDataset().eachLinkedImage(new CBlock()
            {
                public Object call(IObject object)
                {
                    return new ImageData( (Image) object );
                }
            }));
        }
        return images == null ? null : new HashSet( images );
    }

    public Set getProjects() {
        
        if ( projects == null && asDataset().sizeOfProjectLinks() >= 0 )
        {
            projects = new HashSet( asDataset().eachLinkedProject( new CBlock () {
                public Object call(IObject object) 
                {
                    return new ProjectData( (Project) object ); 
                };
            }));
        }
        
        return projects == null ? null : new HashSet( projects );
    }

    // Link mutations
    
    public void setImages( Set newValue ) 
    {
        Set currentValue = getImages(); 
        SetMutator m = new SetMutator( currentValue, newValue );
        
        while ( m.moreDeletions() )
        {
            setDirty( true );
            asDataset().unlinkImage( m.nextDeletion().asImage() );
        }
        
        while ( m.moreAdditions() )
        {
            setDirty( true );
            asDataset().linkImage( m.nextAddition().asImage() );
        }

        images = m.result();    }


    public void setProjects( Set newValue ) 
    {
        
        Set currentValue = getProjects(); 
        SetMutator m = new SetMutator( currentValue, newValue );
        
        while ( m.moreDeletions() )
        {
            setDirty( true );
            asDataset().unlinkProject( m.nextDeletion().asProject() );
        }
        
        while ( m.moreAdditions() )
        {
            setDirty( true );
            asDataset().linkProject( m.nextAddition().asProject() );
        }

        projects = m.result();
    }

    
    // SETS
    
    public Set getAnnotations() {
        
        if ( annotations == null && asDataset().sizeOfAnnotations() >= 0 )
        {
            annotations = new HashSet( asDataset().collectAnnotations( new CBlock() {
               public Object call(IObject object)
                {
                   return new AnnotationData( (DatasetAnnotation) object );
                } 
            }));
        }
        
        return annotations == null ? null : new HashSet( annotations );
    }

    public void setAnnotations( Set newValue ) 
    {
        Set currentValue = getAnnotations(); 
        SetMutator m = new SetMutator( currentValue, newValue );
        
        while ( m.moreDeletions() )
        {
            setDirty( true );
            asDataset().removeFromAnnotations( m.nextDeletion().asDatasetAnnotation() );
            annotationCount = annotationCount == null ? null :
                new Integer( annotationCount.intValue() - 1 );
        }
        
        while ( m.moreAdditions() )
        {
            setDirty( true );
            asDataset().removeFromAnnotations( m.nextAddition().asDatasetAnnotation() );
            annotationCount = annotationCount == null ? null :
                new Integer( annotationCount.intValue() + 1 );
        }

        annotations = m.result();

    }


    
    // COUNTS
    
    public Integer getAnnotationCount()
    {
        if ( annotationCount == null )
        {
            annotationCount = getCount( Dataset.ANNOTATIONS );
        }
        return annotationCount;
    }
	
}
