/*
 * pojos.ImageData
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
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.annotations.ImageAnnotation;
import ome.model.containers.Category;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.util.CBlock;

/** 
 * The data that makes up an <i>OME</i> Image along with links to its
 * Pixels, enclosing Datasets, and the Experimenter that owns this Image.
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
public class ImageData
    extends DataObject
{
    
    public final static String NAME = Image.NAME;
    public final static String DESCRIPTION = Image.DESCRIPTION;
    public final static String PIXELS = Image.PIXELS;
    public final static String ANNOTATIONS = Image.ANNOTATIONS;
    public final static String DATASET_LINKS = Image.DATASETLINKS;
    
    /**
     * The default image data associated to this Image.
     * An <i>OME</i> Image can be associated to more than one 5D pixels set
     * (that is, the raw image data) if all those sets are derived from an
     * initial image file.  An example is a deconvolved image and the original
     * file: those two pixels sets would be represented by the same <i>OME</i>
     * Image.  
     * In the case there's more than one pixels set, this field identifies the
     * pixels that are used by default for analysis and visualization.  If the
     * Image only has one pixels set, then this field just points to that set.
     * This field may not be <code>null</code>.
     */
    private PixelsData defaultPixels;
    
    /**
     * All the Pixels that belong to this Image.
     * The elements of this set are {@link PixelsData} objects.
     * This field may not be <code>null</code> nor empty.  As a minimum, it
     * will contain the {@link #defaultPixels default} Pixels.
     * 
     * @see #defaultPixels
     */
    private Set        allPixels;
    
    /** 
     * All the Datasets that contain this Image.
     * The elements of this set are {@link DatasetData} objects.  If this
     * Image is not contained in any Dataset, then this set will be empty
     * &#151; but never <code>null</code>. 
     */
    private Set      datasets;
    
    /** All the Categories that contain this Image.
     * The elements of this set are {@link CategoryData} objects.
     */
    private Set     categories;
    
    /**
     * All the annotations related to this Image.
     * The elements of the set are {@link AnnotationData} objetcs.
     * If this Image hasn't been annotated, then this set will be empty
     * &#151; but never <code>null</code>. 
     */
    private Set      annotations;
    
    /** 
     * The number of annotations attached to this Image.
     * This field may be <code>null</code> meaning no count retrieved,
     * and it may be less than the actual number if filtered by user.
     */
    private Integer annotationCount;
    
    /** 
     * The number of categories attached to this Imaget.
     * This field may be <code>null</code> meaning no count retrieved,
     * and it may be less than the actual number if filtered by user.
     */
    private Integer classificationCount;

    
    public ImageData()
    {
        setDirty( true );
        setValue( new Image() );
    }
    
    public ImageData( Image value )
    {
        setValue( value );
    }

    // Immutables
    
    public void setName(String name) {
        setDirty( true );
        asImage().setName( name );
    }

    public String getName() {
        return asImage().getName();
    }

    public void setDescription(String description) {
        setDirty( true );
        asImage().setDescription( description );
    }

    public String getDescription() {
        return asImage().getDescription();
    }

    public Timestamp getCreated() {
        return timeOfEvent( asImage().getDetails().getCreationEvent() );
    }

    public Timestamp getInserted() {
        return timeOfEvent( asImage().getDetails().getUpdateEvent() );
    }

    // Single-valued objects.
    
    public PixelsData getDefaultPixels() {
        if ( defaultPixels == null && asImage().getDefaultPixels() != null )
        {
            defaultPixels = new PixelsData( asImage().getDefaultPixels() );
        }
        return defaultPixels;
    }

    public void setDefaultPixels( PixelsData defaultPixels ) {

        if ( getDefaultPixels() != defaultPixels )
        {
            setDirty( true );
            this.defaultPixels = defaultPixels;
            if ( defaultPixels != null )
            {
                asImage().collectPixels( new CBlock() {
                    public Object call(IObject object)
                    {
                        ((Pixels) object).setDefaultPixels( Boolean.FALSE );
                        return null;
                    }
                });
                defaultPixels.asPixels().setDefaultPixels( Boolean.TRUE );
            }

        }

    }
    
    // Sets
    
    public Set getAllPixels() {
        if ( allPixels == null && asImage().sizeOfPixels() >= 0 )
        {
            allPixels = new HashSet( asImage().collectPixels( new CBlock() {
                public Object call(IObject object)
                {
                    return new PixelsData( (Pixels) object );
                }
            }));
        }
        return allPixels == null ? null : new HashSet( allPixels );
    }

    public void setAllPixels( Set newValue ) {
        Set currentValue = getAllPixels(); 
        SetMutator m = new SetMutator( currentValue, newValue );
        
        while ( m.moreDeletions() )
        {
            setDirty( true );
            asImage().removeFromPixels( m.nextDeletion().asPixels() );
        }
        
        while ( m.moreAdditions() )
        {
            setDirty( true );
            asImage().addToPixels( m.nextAddition().asPixels() );
        }

        allPixels = m.result();    
    }

    public Set getDatasets() 
    {
        if ( datasets == null && asImage().sizeOfDatasetLinks() >= 0 )
        {
            datasets = new HashSet( asImage().eachLinkedDataset( new CBlock() {
                public Object call(IObject object) {
                    return new DatasetData( (Dataset) object );
                };
            }));
        }
        return datasets == null ? null : new HashSet( datasets );
    }
    
    public void setDatasets( Set newValue ) 
    {
        Set currentValue = getDatasets(); 
        SetMutator m = new SetMutator( currentValue, newValue );
        
        while ( m.moreDeletions() )
        {
            setDirty( true );
            asImage().unlinkDataset( m.nextDeletion().asDataset() );
        }
        
        while ( m.moreAdditions() )
        {
            setDirty( true );
            asImage().linkDataset( m.nextAddition().asDataset() );
        }

        datasets = m.result();    
    }

    public Set getCategories() 
    {
        if ( categories == null && asImage().sizeOfCategoryLinks() >= 0 )
        {
            categories = new HashSet( asImage().eachLinkedCategory( new CBlock() {
                public Object call(IObject object) {
                    return new CategoryData( (Category) object );
                };
            }));
        }
        return categories == null ? null : new HashSet( categories );
    }
    
    public void setCategories( Set newValue ) 
    {
        Set currentValue = getCategories(); 
        SetMutator m = new SetMutator( currentValue, newValue );
        
        while ( m.moreDeletions() )
        {
            setDirty( true );
            asImage().unlinkCategory( m.nextDeletion().asCategory() );
            classificationCount = classificationCount == null ? null :
                    new Integer( classificationCount.intValue() - 1 );
        }
        
        while ( m.moreAdditions() )
        {
            setDirty( true );
            asImage().linkCategory( m.nextAddition().asCategory() );
            classificationCount = classificationCount == null ? null :
                new Integer( classificationCount.intValue() + 1 );

        }

        categories = m.result();    
    }
    
    public Set getAnnotations() {
        if ( annotations == null && asImage().sizeOfAnnotations() >= 0 )
        {
            annotations = new HashSet( asImage().collectAnnotations( new CBlock() {
                public Object call(IObject object) {
                    return new AnnotationData( (ImageAnnotation) object );
                };
            }));
        }
        return annotations == null ? null : new HashSet( annotations );
    }

    
    public void setAnnotations( Set newValue ) {
        Set currentValue = getAnnotations(); 
        SetMutator m = new SetMutator( currentValue, newValue );
        
        while ( m.moreDeletions() )
        {
            setDirty( true );
            asImage().removeFromAnnotations( m.nextDeletion().asImageAnnotation() );
            annotationCount = annotationCount == null ? null :
                    new Integer( annotationCount.intValue() - 1 );
        }
        
        while ( m.moreAdditions() )
        {
            setDirty( true );
            asImage().addToAnnotations( m.nextAddition().asImageAnnotation() );
            annotationCount =  annotationCount == null ? null :
                new Integer( annotationCount.intValue() + 1 );
        }

        annotations = m.result();
    }

    // COUNTS
    
    public Integer getAnnotationCount()
    {
        if ( annotationCount == null )
            annotationCount = getCount( Image.ANNOTATIONS );
        
        return annotationCount;
    }

    public Integer getClassificationCount()
    {
        if ( classificationCount == null )
            classificationCount = getCount( Image.CATEGORYLINKS );
        
        return classificationCount;
    }

}
