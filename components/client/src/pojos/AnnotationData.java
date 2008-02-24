/*
 * pojos.AnnotationData
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package pojos;

import java.sql.Timestamp;

import ome.model.annotations.Annotation;
import ome.model.annotations.TextAnnotation;

/**
 * Holds a textual annotation of a given data object and a reference to the
 * Experimenter that wrote it. This class wraps a given {@link Annotation}
 * instance in a wrapper.
 * 
 * @author Jean-Marie Burel, j.burel at dundee.ac.uk
 * @author Andrea Falconi, a.falconi at dundee.ac.uk
 * @author Josh Moore, josh at glencoesoftware.com
 * @since OME2.2
 */
public class AnnotationData extends DataObject {

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
            a.setNs("");
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
     * Sets the name of the underlying {@link Annotation} instance. Unlike the
     * unwrapped {@link Annotation} the name is initialized to the empty string
     * for backwards compatibility.
     */
    public void setNs(String name) {
        wrapper.setNs(name);
    }

    /**
     * Retrieves the {@link Annotation#getName() name} of the underlying
     * {@link Annotation} instance.
     */
    public String getNs() {
        return wrapper.getNs();
    }

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
     * 
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
        return timeOfEvent(getDetails().getCreationEvent());
    }

    private void setWrapper(Annotation a) {
        if (a == null) {
            wrapper = null;
        } else if (a instanceof TextAnnotation) {
            wrapper = new StringAnn((TextAnnotation) a);
        } else {
            throw new IllegalArgumentException("Unknown annotation " + a);
        }
    }

    // ~ Wrapper Hierarchy
    // =========================================================================

    public static interface Ann {
        public void setNs(String name);

        public String getNs();

        public void setContent(Object o);

        public Object getContent();

        public String getContentAsString();

        public Class<? extends Annotation> getType();
    }

    public static class StringAnn implements Ann {

        private final TextAnnotation ann;

        public StringAnn(TextAnnotation ann) {
            this.ann = ann;
        }

        public Class<? extends Annotation> getType() {
            return TextAnnotation.class;
        }

        public String getNs() {
            return ann.getNs();
        }

        public void setNs(String name) {
            ann.setNs(name);
        }

        public Object getContent() {
            return ann.getTextValue();
        }

        public String getContentAsString() {
            return ann.getTextValue();
        }

        public void setContent(Object o) {
            if (o == null) {
                ann.setTextValue(null);
            } else if (o instanceof String) {
                ann.setTextValue((String) o);
            } else {
                throw new IllegalArgumentException(o
                        + " is an incompatible type for " + ann);
            }
        }

    }

}
