/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package omero.gateway.model;

import java.sql.Timestamp;

import static omero.rtypes.*;
import omero.model.Annotation;

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
public abstract class AnnotationData extends DataObject {

    /**  The name space used to identify the file transfer type. */
    public static final String FILE_TRANSFER_NS =
            omero.constants.namespaces.NSFILETRANSFER.value;
    /**
     * Creates a new instance.
     *
     * @param annotationClass
     *            The type of annotation to create.
     * @throws IllegalArgumentException
     *             If the type is not supported or if the passed parameter is
     *             <code>null</code>.
     */
    protected AnnotationData(Class<? extends Annotation> annotationClass) {
        if (annotationClass == null) {
            throw new IllegalArgumentException("annotationClass cannot be null");
        }
        try {
            Annotation a = annotationClass.newInstance();
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
    protected <A extends Annotation> AnnotationData(A annotation) {
        if (annotation == null) {
            throw new IllegalArgumentException("Annotation cannot null.");
        }
        setValue(annotation);
    }

    // Immutables

    /**
     * Sets the name space of the underlying {@link Annotation} instance.
     *
     * @param name
     *            The value to set.
     */
    public void setNameSpace(String name) {
        asAnnotation().setNs(name == null ? null : rstring(name));
    }

    /**
     * Retrieves the {@link Annotation#getNs() nameSpace} of the underlying
     * {@link Annotation} instance.
     *
     * @return See above.
     */
    public String getNameSpace() {
        omero.RString ns = asAnnotation().getNs();
        return ns == null ? null : ns.getValue();
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

    /**
     * Retrieves the {@link Annotation#getDescription() description} of the
     * underlying {@link Annotation} instance.
     *
     * @return See above
     */
    public String getDescription() {
        omero.RString desc = asAnnotation().getDescription();
        return desc == null ? null : desc.getValue();
    }

    /**
     * Sets the description of the underlying {@link Annotation} instance.
     *
     * @param description
     *            The description
     */
    public void setDescription(String description) {
        asAnnotation().setDescription(
                description == null ? null : rstring(description));
    }

    /**
     * Sets the annotation value. If the improper content is given for the
     * current {@link Annotation}, then an
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param content
     *            The value to set.
     */
    public abstract void setContent(Object content);

    /**
     * Returns the content of the annotation.
     *
     * @return See above.
     */
    public abstract Object getContent();

    /**
     * Returns the content of the annotation as a {@link String}, which is
     * parsed on a {@link Class}-by-{@link Class} basis.
     *
     * @return See above
     */
    public abstract String getContentAsString();

}
