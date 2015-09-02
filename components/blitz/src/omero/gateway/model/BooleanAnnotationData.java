/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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

import static omero.rtypes.*;
import omero.model.BooleanAnnotation;
import omero.model.BooleanAnnotationI;


/**
 * Boolean annotation.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $)
 *          </small>
 * @since OME3.0
 */
public class BooleanAnnotationData extends AnnotationData {

	
	/**
     * The name space used to indicate that the tag is used a tag set.
     */
    public static final String INSIGHT_PUBLISHED_NS = 
    	"openmicroscopy.org/omero/insight/published";
    
    
    /** Creates a new instance. */
    public BooleanAnnotationData() {
        super(BooleanAnnotationI.class);
        setContent(null);
    }

    /**
     * Creates a new instance.
     * 
     * @param value
     *            The value to set.
     */
    public BooleanAnnotationData(boolean value) {
        super(BooleanAnnotationI.class);
        setContent(value);
    }

    /**
     * Creates a new instance.
     * 
     * @param annotation
     *            The {@link BooleanAnnotation} object corresponding to this
     *            <code>DataObject</code>. Mustn't be <code>null</code>.
     */
    public BooleanAnnotationData(BooleanAnnotation annotation) {
        super(annotation);
    }

    /**
     * Returns the text.
     * 
     * @param value
     *            The value to set.
     */
    public void setValue(boolean value) {
        setContent(value);
    }

    /**
     * Returns the text of this annotation.
     * 
     * @return See above.
     */
    public Boolean getValue() {
        return (Boolean) getContent();
    }

    /**
     * Returns the textual content of the annotation.
     * 
     * @see AnnotationData#getContent()
     */
    @Override
    public Object getContent() {
        omero.RBool b = ((BooleanAnnotation) asAnnotation()).getBoolValue();
        return b == null ? null : b.getValue();
    }

    /**
     * Returns the textual content of the annotation.
     * 
     * @see AnnotationData#getContentAsString()
     */
    @Override
    public String getContentAsString() {
        Object o = getContent();
        return o == null ? "null" : o.toString();
    }

    /**
     * Sets the text annotation.
     * 
     * @see AnnotationData#setContent(Object)
     */
    @Override
    public void setContent(Object content) {
        if (content == null) {
            throw new IllegalArgumentException("Annotation value cannot "
                    + "be null.");
        }

        if (!(content instanceof Boolean)) {
            throw new IllegalArgumentException("Object must be of type "
                    + "Boolean");
        }
        setDirty(true);
        omero.RBool b = rbool(((Boolean) content).booleanValue());
        ((BooleanAnnotation) asAnnotation()).setBoolValue(b);
    }

}
