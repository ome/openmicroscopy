/*
 * pojos.AnnotationData
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package pojos;

import java.sql.Timestamp;

import ome.model.annotations.Annotation;
import ome.model.annotations.StringAnnotation;
import ome.model.containers.Dataset;
import ome.model.core.Image;

/**
 * Holds a textual annotation of a given data object and a reference to the
 * Experimenter that wrote it.
 * 
 * @author Jean-Marie Burel, j.burel at dundee.ac.uk
 * @author Andrea Falconi, a.falconi at dundee.ac.uk
 * @author Josh Moore, josh at glencoesoftware.com
 * @since OME2.2
 */
public class AnnotationData extends DataObject {

    /**
     * The object this annotation refers to, for example Image or Dataset. This
     * field may not be <code>null</code>.
     */
    private DataObject annotatedObject;

    /**
     * A wrapper which handles dispatching all {@link Annotation} needs to a 
     * wrapper hierarchy.
     */
    private Ann wrapper;
    
    /**
     * Creates a new instance.
     * 
     * @param annotationType
     *            The type of annotation to create. One of the following
     *            constants: {@link #IMAGE_ANNOTATION},
     *            {@link #DATASET_ANNOTATION}.
     * @throws IllegalArgumentException
     *             If the type is supported.
     */
    public AnnotationData(Class<? extends Annotation> annotationClass) {
        if (annotationClass == null) {
            throw new IllegalArgumentException("annotationClass cannot be null");
        }
        try {
            Annotation a = annotationClass.newInstance();
            setWrapper(a);
            setValue(a);
        } catch (Exception e) { 
            throw new IllegalArgumentException("Unkown annotation type: "
                    + annotationClass.getName());
        }
    }

    /**
     * Creates a new instance.
     * 
     * @param annotation
     *            The {@link Annotation} object corresponding to this
     *            <code>DataObject</code>. Mustn't be <code>null</code>.
     * @throws IllegalArgumentException
     *             If the object is <code>null</code>.
     */
    public <A extends Annotation> AnnotationData(A annotation) {
        if (annotation == null) {
            throw new IllegalArgumentException("Annotation cannot null.");
        }
        setWrapper(annotation);
        setValue(annotation);
    }
    
    public Class<? extends Annotation> getAnnotationType() {
        return wrapper.getType();
     }

    // Immutables
    /**
     * Sets the annotation value. If the improper content is given for the 
     * current {@link Annotation} {@link #getAnnotationType()}, then an 
     * {@link IllegalArgumentException} will be thrown.
     * 
     * @param content
     */
    public void setContent(Object content) {
        wrapper.setContent(content);
    }

    /**
     * Returns the content of the annotation.
     * 
     * @return See above.
     */
    public Object getContent() {
        return wrapper.getContent();
    }
    
    /**
     * Returns the content of the annotation as a {@link String}, which is
     * parsed on a {@link Class}-by-{@link Class} basis.
     * @return
     */
    public String getContentAsString() {
        return wrapper.getContentAsString();
    }

    /**
     * Returns the time when the annotation was last modified.
     * 
     * @return See above.
     */
    public Timestamp getLastModified() {
        if (nullDetails()) {
            return null;
        }
        return timeOfEvent(getDetails().getUpdateEvent());
    }

    private void setWrapper(Annotation a) {
        if (a == null) {
            wrapper = null;
        } else if (a instanceof StringAnnotation ) {
            wrapper = new StringAnn((StringAnnotation)a);
        } else {
            throw new IllegalArgumentException("Unknown annotation " + a);
        }
    }
    
    // ~ Wrapper Hierarchy
    // =========================================================================
    
    public static interface Ann {
        public void setContent(Object o);
        public Object getContent();
        public String getContentAsString();
        public Class<? extends Annotation> getType();
    }
    
    public static class StringAnn implements Ann {

        private StringAnnotation ann;
        
        public StringAnn(StringAnnotation ann) {
            this.ann = ann;
        }
        
        public Class<? extends Annotation> getType() {
            return StringAnnotation.class;
        }
        
        public Object getContent() {
            return ann.getStringValue();
        }

        public String getContentAsString() {
            return ann.getStringValue();
        }

        public void setContent(Object o) {
            if (o == null) {
                ann.setStringValue(null);
            } else if (o instanceof String) {
                ann.setStringValue((String) o);
            } else {
                throw new IllegalArgumentException(o + " is an incompatible type for "+ann);
            }
        }
        
    }

}
