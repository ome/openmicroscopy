/*
 * pojos.DoubleAnnotationData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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

import static omero.rtypes.rdouble;
import omero.model.DoubleAnnotation;
import omero.model.DoubleAnnotationI;

/**
 * Wraps a double annotation.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class DoubleAnnotationData
    extends AnnotationData
{

    /**
     * Creates a new instance.
     * 
     * @param value The value to set.
     */
    public DoubleAnnotationData(double value)
    {
        super(DoubleAnnotationI.class);
        setDataValue(value);
    }

    /** Creates a new instance. */
    public DoubleAnnotationData()
    {
        super(DoubleAnnotationI.class);
    }

    /**
     * Creates a new instance.
     * 
     * @param annotation The {@link DoubleAnnotation} object corresponding to
     *                   this <code>DataObject</code>.
     *                   Mustn't be <code>null</code>.
     */
    public DoubleAnnotationData(DoubleAnnotation annotation)
    {
        super(annotation);
    }

    /**
     * Sets the rating value.
     *
     * @param value The value to set.
     */
    public void setDataValue(double value)
    {
        omero.RDouble l = rdouble(value);
        setDirty(true);
        ((DoubleAnnotation) asAnnotation()).setDoubleValue(l);
    }

    /**
     * Returns the value.
     *
     * @return See above.
     */
    public double getDataValue()
    {
        Double l = (Double) getContent();
        return l == null ? -1 : l.doubleValue();
    }

    /**
     * Returns the value.
     *
     * @see AnnotationData#getContent()
     */
    @Override
    public Object getContent()
    {
        omero.RDouble l = ((DoubleAnnotation) asAnnotation()).getDoubleValue();
        return l == null ? null : l.getValue();
    }

    /**
     * Returns the value as a string.
     *
     * @see AnnotationData#getContentAsString()
     */
    @Override
    public String getContentAsString()
    {
        return "" + getDataValue();
    }

    /**
     * Sets the text annotation.
     *
     * @see AnnotationData#setContent(Object)
     */
    @Override
    public void setContent(Object content)
    {
        if (content == null)  return;
        if (content instanceof Number) {
            Number n = (Number) content;
            setDataValue(n.doubleValue());
        } else {
            throw new IllegalArgumentException("Value not supported.");
        }
    }

}
