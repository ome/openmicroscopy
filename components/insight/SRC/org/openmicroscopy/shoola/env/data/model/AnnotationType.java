package org.openmicroscopy.shoola.env.data.model;

import omero.gateway.model.BooleanAnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.DoubleAnnotationData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.LongAnnotationData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.RatingAnnotationData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TermAnnotationData;
import omero.gateway.model.TextualAnnotationData;
import omero.gateway.model.TimeAnnotationData;
import omero.gateway.model.XMLAnnotationData;

/**
 * The different types of Annotations
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public enum AnnotationType {
    
    /** Tag Annotation */
    TAG("Tags", TagAnnotationData.class), 
    
    /** Map Annotation */
    MAP("Key-Value Pairs", MapAnnotationData.class),
    
    /** File Attachment Annotation */
    ATTACHMENT("Attachments", FileAnnotationData.class), 
    
    /** Rating Annotation */
    RATING("Ratings", RatingAnnotationData.class), 
    
    /** Other Annotations */
    OTHER("Others", null), 
    
    /** Comment Annotation */
    COMMENT("Comments", TextualAnnotationData.class), 
    
    /** ROIs */
    ROI("ROIs", null),
    
    /** Boolean */
    BOOLEAN("Boolean", BooleanAnnotationData.class),
    
    /** Double */
    DOUBLE("Numeric (double)", DoubleAnnotationData.class),
    
    /** Long */
    LONG("Numeric (long)", LongAnnotationData.class),
    
    /** Term */
    TERM("Term", TermAnnotationData.class),
    
    /** Time */
    TIME("Time", TimeAnnotationData.class),
    
    /** XML */
    XML("XML", XMLAnnotationData.class);

    /** The {@link DataObject} type this annotation is represented by (if any) */
    private Class<? extends DataObject> pojoClass;

    /** Human readable name for this annotation type */
    private String descriptiveName;

    /**
     * Creates a new instance
     * 
     * @param name
     *            Human readable name for this annotation type
     * @param pojoClass
     *            The {@link DataObject} type this annotation is represented by
     *            (if any)
     */
    AnnotationType(String name, Class<? extends DataObject> pojoClass) {
        this.descriptiveName = name;
        this.pojoClass = pojoClass;
    }

    /**
     * Get the {@link DataObject} type this annotation is represented by (if
     * any)
     * 
     * @return See above
     */
    public Class<? extends DataObject> getPojoClass() {
        return pojoClass;
    }

    /**
     * Get the human readable name for this annotation type
     * 
     * @return See above
     */
    public String getDescriptiveName() {
        return descriptiveName;
    }

    /**
     * Determine the {@link AnnotationType} by the given pojo class
     * 
     * @param pojoClass
     *            The pojo class
     * @return See above.
     */
    public static AnnotationType getAnnotationType(
            Class<? extends DataObject> pojoClass) {
        for (AnnotationType type : AnnotationType.values()) {
            if (type.getPojoClass().equals(pojoClass))
                return type;
        }
        return OTHER;
    }
}
