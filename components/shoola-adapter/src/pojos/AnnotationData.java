/*
 * pojos.AnnotationData
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

// Java imports
import java.sql.Timestamp;

// Third-party libraries

// Application-internal dependencies
import ome.model.annotations.DatasetAnnotation;
import ome.model.annotations.ImageAnnotation;
import ome.model.containers.Dataset;
import ome.model.core.Image;


/**
 * Holds a textual annotation of a given data object and a reference to the
 * Experimenter that wrote it.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
 *          </small>
 * @since OME2.2
 */
public class AnnotationData
    extends DataObject
{

    /** Identifies an <code>Image</code> annotation. */
    public final static int IMAGE_ANNOTATION = 0;
    
    /** Identifies an <code>Datase</code> annotation. */
    public final static int DATASET_ANNOTATION = 1;
    
    /** Identifies the {@link ImageAnnotation#CONTENT} field. */
    public final static String IMAGE_ANNOTATION_CONTENT = 
                                                ImageAnnotation.CONTENT;

    /** Identifies the {@link ImageAnnotation#IMAGE} field. */
    public final static String IMAGE_ANNOTATION_IMAGE = ImageAnnotation.IMAGE;

    /** Identifies the {@link DatasetAnnotation#CONTENT} field. */
    public final static String DATASET_ANNOTATION_CONTENT = 
                                                DatasetAnnotation.CONTENT;

    /** Identifies the {@link DatasetAnnotation#DATASET} field. */
    public final static String DATASET_ANNOTATION_DATASET = 
                                                DatasetAnnotation.DATASET;

    /**
     * The object this annotation refers to, for example Image or Dataset. This
     * field may not be <code>null</code>.
     */
    private DataObject          annotatedObject;

    /** 
     * One of the following constants:
     * {@link #IMAGE_ANNOTATION}, {@link #DATASET_ANNOTATION}.
     */ 
    private int                 annotationType;

    /**
     * Creates a new instance.
     * 
     * @param annotationType    The type of annotation to create.
     *                          One of the following constants:
     *                          {@link #IMAGE_ANNOTATION},
     *                          {@link #DATASET_ANNOTATION}.
     * @throws IllegalArgumentException If the type is supported.            
     */
    public AnnotationData(int annotationType)
    {
        switch (annotationType) {
            case IMAGE_ANNOTATION:
                this.annotationType = annotationType;
                setValue(new ImageAnnotation());
                break;
            case DATASET_ANNOTATION:
                this.annotationType = annotationType;
                setValue(new DatasetAnnotation());
                break;
            default:
                throw new IllegalArgumentException( 
                        "Unkown annotation type: " + annotationType);
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param imageAnnotation   The {@link ImageAnnotation} object corresponding
     *                          to this <code>DataObject</code>.
     *                          Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If the object is <code>null</code>.                    
     */
    public AnnotationData(ImageAnnotation imageAnnotation)
    {
        if (imageAnnotation == null)
            throw new IllegalArgumentException("Annotation cannot null.");
        annotationType = IMAGE_ANNOTATION;
        setValue(imageAnnotation);
    }

    /**
     * Creates a new instance.
     * 
     * @param datasetAnnotation The {@link DatasetAnnotation} object
     *                          corresponding to this <code>DataObject</code>.
     *                          Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If the object is <code>null</code>.                     
     */
    public AnnotationData(DatasetAnnotation datasetAnnotation)
    {
        if (datasetAnnotation == null)
            throw new IllegalArgumentException("Annotation cannot null.");
        annotationType = DATASET_ANNOTATION;
        setValue(datasetAnnotation);
    }

    // Immutables
    /**
     * Sets the textual annotation.
     * 
     * @param text The text to set.
     */
    public void setText(String text)
    {
        switch (annotationType) {
            case IMAGE_ANNOTATION:
                asImageAnnotation().setContent(text);
                break;
            case DATASET_ANNOTATION:
                asDatasetAnnotation().setContent(text);
        }
    }

    /**
     * Returns the text of the annotation.
     * 
     * @return See above.
     */
    public String getText()
    {
        switch (annotationType) {
            case IMAGE_ANNOTATION:
                return asImageAnnotation().getContent();
            case DATASET_ANNOTATION:
                return asDatasetAnnotation().getContent();
            default: // shouldn't happen
                return null;
        }
    }

    /**
     * Returns the time when the annotation was last modified.
     * 
     * @return See above.
     */
    public Timestamp getLastModified()
    {
        if (nullDetails()) return null;
        return timeOfEvent(getDetails().getUpdateEvent());
    }

    // Entities
    /**
     * Sets the annotated object.
     * 
     * @param annotatedObject The annotated object.
     * @throws IllegalArgumentException If the passed object is 
     * <code>null</code> or not a {@link DatasetData} or an {@link ImageData}.
     */
    public void setAnnotatedObject(DataObject annotatedObject)
    {
        if (annotatedObject == null)
            throw new IllegalArgumentException("The annotated object cannot" +
                                                "be null.");
        if (!(annotatedObject instanceof DatasetData) && 
            !(annotatedObject instanceof DatasetData))
            throw new IllegalArgumentException("DataObject not valid.");
        if (this.annotatedObject == annotatedObject) return;
        setDirty(true);
        this.annotatedObject = annotatedObject;
        switch (annotationType) {
            case IMAGE_ANNOTATION:
                asImageAnnotation().setImage(annotatedObject.asImage());
                break;
            case DATASET_ANNOTATION:
                asDatasetAnnotation().setDataset(
                                        annotatedObject.asDataset());
        }
    }

    /**
     * Returns the annotated object.
     * 
     * @return See above.
     */
    public DataObject getAnnotatedObject()
    {
        if (annotatedObject == null) {
            switch (annotationType) {
                case IMAGE_ANNOTATION:
                    Image i = asImageAnnotation().getImage();
                    this.annotatedObject = i == null ? null : new ImageData(i);
                    break;
                case DATASET_ANNOTATION:
                    Dataset d = asDatasetAnnotation().getDataset();
                    this.annotatedObject = d == null ? null : 
                                            new DatasetData(d);
            }
        }
        return annotatedObject;
    }

}
