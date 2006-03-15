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
public class AnnotationData extends DataObject
{

    public final static int IMAGE_ANNOTATION = 0;
    
    public final static int DATASET_ANNOTATION = 1;
    
    public final static String IMAGE_ANNOTATION_CONTENT   = ImageAnnotation.CONTENT;

    public final static String IMAGE_ANNOTATION_IMAGE     = ImageAnnotation.IMAGE;

    public final static String DATASET_ANNOTATION_CONTENT = DatasetAnnotation.CONTENT;

    public final static String DATASET_ANNOTATION_DATASET   = DatasetAnnotation.DATASET;

    /**
     * The object this annotation refers to, for example Image or Dataset. This
     * field may not be <code>null</code>.
     */
    private DataObject         annotatedObject;

    private boolean            isImage;

    // protected because we wouldn't know what type of base class;
    private AnnotationData()
    {}

    public AnnotationData( int annotationType )
    {
        switch (annotationType)
        {
            case IMAGE_ANNOTATION:
                isImage = true;
                setValue( new ImageAnnotation() );
                break;

            case DATASET_ANNOTATION:
                isImage = false;
                setValue( new DatasetAnnotation() );
                break;
            default:
                throw new IllegalArgumentException( 
                        "Unkown annotation type: " + annotationType );
        }
    }
    
    public AnnotationData(ImageAnnotation imageAnnotation)
    {
        isImage = true;
        setValue( imageAnnotation );
    }

    public AnnotationData(DatasetAnnotation datasetAnnotation)
    {
        isImage = false;
        setValue( datasetAnnotation );
    }

    // Immutables
    
    public void setText(String text)
    {
        if (isImage) 
            asImageAnnotation().setContent( text );
        else
            asDatasetAnnotation().setContent( text );

    }

    public String getText()
    {
        return isImage ? asImageAnnotation().getContent() 
                : asDatasetAnnotation() .getContent();
    }

    public Timestamp getLastModified()
    {
        if ( nullDetails() ) return null;
        return timeOfEvent(isImage ? getDetails().getUpdateEvent() 
                : getDetails().getUpdateEvent());
    }

    // Entities
    
    public void setAnnotatedObject( DataObject annotatedObject )
    {
        if ( annotatedObject != this.annotatedObject )
        {
            setDirty( true );
            this.annotatedObject = annotatedObject;
            
            if ( annotatedObject != null )
                if ( isImage )
                    asImageAnnotation().setImage( annotatedObject.asImage() );
                else
                    asDatasetAnnotation().setDataset( annotatedObject.asDataset() );
        }
    }

    public DataObject getAnnotatedObject()
    {
        if ( annotatedObject == null )
            if ( isImage )
            {
                Image i = asImageAnnotation().getImage();
                this.annotatedObject = i == null ? null : new ImageData( i );
            } else {
                Dataset d = asDatasetAnnotation().getDataset();
                this.annotatedObject = d == null ? null : new DatasetData( d );
            }

        return annotatedObject;
    }

}
