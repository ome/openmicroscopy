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
    
    /** Identifies the {@link Image#NAME} field. */
    public final static String NAME = Image.NAME;
    
    /** Identifies the {@link Image#DESCRIPTION} field. */
    public final static String DESCRIPTION = Image.DESCRIPTION;
    
    /** Identifies the {@link Image#PIXELS} field. */
    public final static String PIXELS = Image.PIXELS;
    
    /** Identifies the {@link Image#ANNOTATIONS} field. */
    public final static String ANNOTATIONS = Image.ANNOTATIONS;
    
    /** Identifies the {@link Image#DATASETLINKS} field. */
    public final static String DATASET_LINKS = Image.DATASETLINKS;
    
    /**
     * The default image data associated to this Image.
     * An <i>OME</i> Image can be associated to more than one 5D pixels set
     * (that is, the raw image data) if all those sets are derived from an
     * initial image file. An example is a deconvolved image and the original
     * file: those two pixels sets would be represented by the same <i>OME</i>
     * Image.  
     * In the case there's more than one pixels set, this field identifies the
     * pixels that are used by default for analysis and visualization. If the
     * Image only has one pixels set, then this field just points to that set.
     * This field may not be <code>null</code>.
     */
    private PixelsData defaultPixels;
    
    /**
     * All the Pixels that belong to this Image.
     * The elements of this set are {@link PixelsData} objects.
     * This field may not be <code>null</code> nor empty. As a minimum, it
     * will contain the {@link #defaultPixels default} Pixels.
     * 
     * @see #defaultPixels
     */
    private Set        allPixels;
    
    /** 
     * All the Datasets that contain this Image.
     * The elements of this set are {@link DatasetData} objects. If this
     * Image is not contained in any Dataset, then this set will be empty
     * &#151; but never <code>null</code>. 
     */
    private Set      datasets;
    
    /** 
     * All the Categories that contain this Image.
     * The elements of this set are {@link CategoryData} objects.
     */
    private Set     categories;
    
    /**
     * All the annotations related to this Image.
     * The elements of the set are {@link AnnotationData} objetcs.
     * If this Image hasn't been annotated, then this set will be empty
     * &#151; but never <code>null</code>. 
     */
    private Set     annotations;
    
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

    /** Creates a new instance. */
    public ImageData()
    {
        setDirty(true);
        setValue(new Image());
    }
    
    /**
     * Creates a new instance.
     * 
     * @param image     Back pointer to the {@link Image} model object.
     *                  Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If the object is <code>null</code>.
     */
    public ImageData(Image image)
    {
        if (image == null)
            throw new IllegalArgumentException("Object cannot null.");
        setValue(image);
    }

    // Immutables
    
    /**
     * Sets the name of the image.
     * 
     * @param name The name of the image. Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If the name is <code>null</code>.
     */
    public void setName(String name)
    {
        if (name == null) 
            throw new IllegalArgumentException("The name cannot be null.");
        setDirty(true);
        asImage().setName(name);
    }

    /** 
     * Returns the name of the image.
     * 
     * @return See above.
     */
    public String getName() { return asImage().getName(); }

    /**
     * Sets the description of the image.
     * 
     * @param description The description of the image.
     */
    public void setDescription(String description)
    {
        setDirty(true);
        asImage().setDescription(description);
    }

    /**
     * Returns the description of the image.
     * 
     * @return See above.
     */
    public String getDescription() { return asImage().getDescription(); }

    /**
     * Returns the creation time of the image.
     * 
     * @return See above.
     */
    public Timestamp getCreated()
    {
        return timeOfEvent( asImage().getDetails().getCreationEvent() );
    }

    /**
     * Returns the insertion time of the image.
     * 
     * @return See above.
     */
    public Timestamp getInserted()
    {
        return timeOfEvent( asImage().getDetails().getUpdateEvent() );
    }

    // Single-valued objects.
    
    /**
     * Returns the default set of pixels.
     * 
     * @return See above.
     */
    public PixelsData getDefaultPixels()
    {
        if (defaultPixels == null && asImage().getDefaultPixels() != null)
            defaultPixels = new PixelsData(asImage().getDefaultPixels());
        return defaultPixels;
    }

    /**
     * Sets the default set of pixels.
     * 
     * @param defaultPixels The default set of pixels.
     */
    public void setDefaultPixels(PixelsData defaultPixels)
    {
        if (getDefaultPixels() == defaultPixels) return;
        setDirty(true);
        this.defaultPixels = defaultPixels;
        if (defaultPixels != null) {
            asImage().collectPixels(new CBlock() {
                public Object call(IObject object)
                {
                    ((Pixels) object).setDefaultPixels(Boolean.FALSE);
                    return null;
                }
            });
            defaultPixels.asPixels().setDefaultPixels(Boolean.TRUE);
        }
    }
    
    // Sets
    
    /**
     * Returns all the sets of pixels related to this image.
     * 
     * @return See above.
     */
    public Set getAllPixels()
    {
        if (allPixels == null && asImage().sizeOfPixels() >= 0) {
            allPixels = new HashSet(asImage().collectPixels(new CBlock() {
                public Object call(IObject object)
                {
                    return new PixelsData((Pixels) object);
                }
            }));
        }
        return allPixels == null ? null : new HashSet(allPixels);
    }

    /**
     * Sets the set of pixels related to this image.
     * 
     * @param newValue The set of pixels' set.
     */
    public void setAllPixels(Set newValue)
    {
        Set currentValue = getAllPixels(); 
        SetMutator m = new SetMutator(currentValue, newValue);
        
        while (m.moreDeletions()) {
            setDirty(true);
            asImage().removePixels(m.nextDeletion().asPixels());
        }
        
        while (m.moreAdditions()) {
            setDirty(true);
            asImage().addPixels(m.nextAddition().asPixels());
        }

        allPixels = m.result();    
    }

    /** 
     * Returns the datasets containing this image.
     * 
     * @return See above.
     */
    public Set getDatasets() 
    {
        if (datasets == null && asImage().sizeOfDatasetLinks() >= 0) {
            datasets = new HashSet( asImage().eachLinkedDataset(new CBlock() {
                public Object call(IObject object) {
                    return new DatasetData((Dataset) object);
                };
            }));
        }
        return datasets == null ? null : new HashSet(datasets);
    }
    
    /**
     * Sets the datasets containing the image.
     * 
     * @param newValue The set of datasets.
     */
    public void setDatasets( Set newValue ) 
    {
        Set currentValue = getDatasets(); 
        SetMutator m = new SetMutator(currentValue, newValue);
        
        while (m.moreDeletions()) {
            setDirty(true);
            asImage().unlinkDataset(m.nextDeletion().asDataset());
        }
        
        while (m.moreAdditions()) {
            setDirty(true);
            asImage().linkDataset(m.nextAddition().asDataset());
        }

        datasets = m.result();    
    }

    /**
     * Returns the categories containing this image.
     * 
     * @return See above.
     */
    public Set getCategories() 
    {
        if (categories == null && asImage().sizeOfCategoryLinks() >= 0)
        {
            categories = new HashSet( asImage().eachLinkedCategory(
                        new CBlock() {
                            public Object call(IObject object) {
                                return new CategoryData((Category) object);
                            };
                            }));
        }
        return categories == null ? null : new HashSet(categories);
    }
    
    /**
     * Sets the categories containing the image.
     * 
     * @param newValue The set of catories.
     */
    public void setCategories(Set newValue) 
    {
        Set currentValue = getCategories(); 
        SetMutator m = new SetMutator(currentValue, newValue);
        
        while (m.moreDeletions())
        {
            setDirty(true);
            asImage().unlinkCategory(m.nextDeletion().asCategory());
            classificationCount = classificationCount == null ? null :
                    new Integer(classificationCount.intValue()-1);
        }
        
        while (m.moreAdditions())
        {
            setDirty(true);
            asImage().linkCategory(m.nextAddition().asCategory());
            classificationCount = classificationCount == null ? null :
                new Integer(classificationCount.intValue()+1);
        }

        categories = m.result();    
    }
    
    /**
     * Returns the annotations
     * @return See above.
     */
    public Set getAnnotations()
    {
        if (annotations == null && asImage().sizeOfAnnotations() >= 0)
        {
            annotations = new HashSet(asImage().collectAnnotations(
                    new CBlock() {
                        public Object call(IObject object) {
                            return new AnnotationData((ImageAnnotation) object);
                        };
                    }));
        }
        return annotations == null ? null : new HashSet( annotations );
    }

    /**
     * Sets the image's annotations.
     * 
     * @param newValue The set of annotations.
     */
    public void setAnnotations(Set newValue)
    {
        Set currentValue = getAnnotations(); 
        SetMutator m = new SetMutator(currentValue, newValue);
        
        while (m.moreDeletions())
        {
            setDirty(true);
            asImage().removeImageAnnotation(
                        m.nextDeletion().asImageAnnotation());
            annotationCount = annotationCount == null ? null :
                    new Integer(annotationCount.intValue()-1);
        }
        
        while (m.moreAdditions()) {
            setDirty(true);
            asImage().addImageAnnotation(m.nextAddition().asImageAnnotation());
            annotationCount =  annotationCount == null ? null :
                new Integer(annotationCount.intValue()+1);
        }

        annotations = m.result();
    }

    // COUNTS
    
    /**
     * Returns the number of annotations related to this image.
     * 
     * @return See above.
     */
    public Integer getAnnotationCount()
    {
        if (annotationCount == null)
            annotationCount = getCount(Image.ANNOTATIONS);
        return annotationCount;
    }

    /**
     * Returns the number of categories containing this image.
     * 
     * @return See above.
     */
    public Integer getClassificationCount()
    {
        if (classificationCount == null)
            classificationCount = getCount(Image.CATEGORYLINKS);
        return classificationCount;
    }

}
