/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
import omero.RString;
import omero.model.XmlAnnotation;
import omero.model.XmlAnnotationI;

/** 
 * Defines an XML Annotation.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class XMLAnnotationData
extends AnnotationData
{

    /**  The name space used to identify modulo concept. */
    public static final String MODULO_NS = 
            omero.constants.namespaces.NSMODULO.value;

    /** Creates a new instance. */
    public XMLAnnotationData()
    {
        super(XmlAnnotationI.class);
        setContent("");
    }

    /**
     * Creates a new instance.
     *
     * @param text The text to set.
     */
    public XMLAnnotationData(String text) 
    {
        super(XmlAnnotationI.class);
        setContent(text);
    }

    /**
     * Creates a new instance.
     *
     * @param annotation The {@link XmlAnnotation} object corresponding to this
     *            		 <code>DataObject</code>. Mustn't be <code>null</code>.
     */
    public XMLAnnotationData(XmlAnnotation annotation)
    {
        super(annotation);
    }

    /**
     * Sets the description of the annotation.
     *
     * @param value The value to set.
     */
    public void setDescription(String value)
    {
        if (value == null || value.trim().length() == 0) 
            return;
        setDirty(true);
        asAnnotation().setDescription(rstring(value));
    }

    /**
     * Returns the description of the annotation.
     *
     * @return See above.
     */
    public String getDescription()
    {
        RString value = asAnnotation().getDescription();
        if (value == null) return "";
        return value.getValue();
    }

    /**
     * Sets the text value.
     *
     * @param value The value to set.
     */
    public void setText(String value) { setContent(value); }

    /**
     * Returns the text of this annotation.
     *
     * @return See above.
     */
    public String getText() { return getContentAsString(); }

    /**
     * Returns the content of the annotation.
     *
     * @see AnnotationData#getContent()
     */
    @Override
    public Object getContent()
    {
        omero.RString s = ((XmlAnnotation) asAnnotation()).getTextValue();
        return s == null ? null : s.getValue();
    }

    /**
     * Returns the textual content of the annotation.
     *
     * @see AnnotationData#getContentAsString()
     */
    @Override
    public String getContentAsString()
    {
        return (String) getContent();
    }

    /**
     * Sets the annotation.
     *
     * @see AnnotationData#setContent(Object)
     */
    @Override
    public void setContent(Object content)
    {
        if (content == null) {
            throw new IllegalArgumentException("Annotation not valid.");
        }
        if (!(content instanceof String)) {
            throw new IllegalArgumentException("Object must be of type String");
        }
        String value = (String) content;
        setDirty(true);
        ((XmlAnnotation) asAnnotation()).setTextValue(rstring(value));
    }

}
