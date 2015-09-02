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

import static omero.rtypes.rstring;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;

/**
 * Basic textual annotation used to add comments to a given object.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class TextualAnnotationData extends AnnotationData {

    /** Creates a new instance. */
    public TextualAnnotationData() {
        super(CommentAnnotationI.class);
        setContent("");
    }

    /**
     * Creates a new instance.
     * 
     * @param text
     *            The text to set.
     */
    public TextualAnnotationData(String text) {
        super(CommentAnnotationI.class);
        setContent(text);
    }

    /**
     * Creates a new instance.
     * 
     * @param annotation
     *            The {@link CommentAnnotation} object corresponding to this
     *            <code>DataObject</code>. Mustn't be <code>null</code>.
     */
    public TextualAnnotationData(CommentAnnotation annotation) {
        super(annotation);
    }

    /**
     * Sets the text.
     * 
     * @param text
     *            The value to set.
     */
    public void setText(String text) {
        setContent(text);
    }

    /**
     * Returns the text of this annotation.
     * 
     * @return See above.
     */
    public String getText() {
        return getContentAsString();
    }

    /**
     * Returns the textual content of the annotation.
     * 
     * @see AnnotationData#getContent()
     */
    @Override
    public Object getContent() {
        omero.RString s = ((CommentAnnotation) asAnnotation()).getTextValue();
        return s == null ? null : s.getValue();
    }

    /**
     * Returns the textual content of the annotation.
     * 
     * @see AnnotationData#getContentAsString()
     */
    @Override
    public String getContentAsString() {
    	Object o = getContent();
    	if (o == null) return "";
        return (String) o;
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

        if (!(content instanceof String)) {
            throw new IllegalArgumentException("Object must be of type String");
        }
        String value = (String) content;
        if (value.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "Annotation value cannot be null.");
        }
        setDirty(true);
        ((CommentAnnotation) asAnnotation()).setTextValue(rstring(value));
    }

}
