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
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;

/**
 * Wraps a long annotation.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class LongAnnotationData extends AnnotationData {

    /**
     * Creates a new instance.
     * 
     * @param value
     *            The value to set. 
     */
    public LongAnnotationData(long value) {
        super(LongAnnotationI.class);
        setDataValue(value);
    }

    /** Creates a new instance. */
    public LongAnnotationData() {
        super(LongAnnotationI.class);
    }

    /**
     * Creates a new instance.
     * 
     * @param annotation
     *            The {@link LongAnnotation} object corresponding to this
     *            <code>DataObject</code>. Mustn't be <code>null</code>.
     */
    public LongAnnotationData(LongAnnotation annotation) {
        super(annotation);
    }

    /**
     * Sets the rating value.
     * 
     * @param value
     *            The value to set. 
     */
    public void setDataValue(long value) {
    	setDirty(true);
        omero.RLong l = rlong(value);
        ((LongAnnotation) asAnnotation()).setLongValue(l);
    }

    /**
     * Returns the value.
     * 
     * @return See above.
     */
    public long getDataValue() {
        Long l = (Long) getContent();
        return l == null ? -1 : l.longValue();
    }

    /**
     * Returns the rating value.
     * 
     * @see AnnotationData#getContent()
     */
    @Override
    public Object getContent() {
        omero.RLong l = ((LongAnnotation) asAnnotation()).getLongValue();
        return l == null ? null : l.getValue();
    }

    /**
     * Returns the value as a string.
     * 
     * @see AnnotationData#getContentAsString()
     */
    @Override
    public String getContentAsString() {
        return "" + getDataValue();
    }

    /**
     * Sets the text annotation.
     * 
     * @see AnnotationData#setContent(Object)
     */
    @Override
    public void setContent(Object content) {
        if (content == null) {
            return;
        }
        if (content instanceof Number) {
            Number n = (Number) content;
            setDataValue(n.longValue());
        } else {
            throw new IllegalArgumentException("Value not supported.");
        }
    }

}
