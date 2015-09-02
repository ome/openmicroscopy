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

import static omero.rtypes.rstring;

import org.apache.commons.lang.StringUtils;

import omero.RString;
import omero.model.TermAnnotation;
import omero.model.TermAnnotationI;

/**
 * Defines a Term Annotation.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since Beta4.2
 */
public class TermAnnotationData extends AnnotationData {

    /**
     * Creates a new instance.
     *
     * @param term
     *            The value to set.
     */
    public TermAnnotationData(String term) {
        super(TermAnnotationI.class);
        setTerm(term);
    }

    /**
     * Creates a new instance.
     *
     * @param annotation
     *            The value to set.
     */
    public TermAnnotationData(TermAnnotation annotation) {
        super(annotation);
    }

    /**
     * Sets the term.
     *
     * @param term The value to set.
     */
    public void setTerm(String term) {
        if (term == null) return;
        setDirty(true);
        ((TermAnnotation) asAnnotation()).setTermValue(rstring(term));
    }

    /**
     * Returns the <code>term</code>.
     *
     * @return See above.
     */
    public String getTerm() {
        return getContentAsString();
    }

    /**
     * Returns the description of the term.
     *
     * @return See above.
     */
    public String getTermDescription()
    {
        RString value = asAnnotation().getDescription();
        if (value == null) return "";
        return value.getValue();
    }

    /**
     * Sets the description of the tag.
     *
     * @param value The value to set.
     */
    public void setTermDescription(String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        setDirty(true);
        asAnnotation().setDescription(rstring(value));
    }

    /**
     * Returns the textual content of the annotation.
     *
     * @see AnnotationData#getContent()
     */
    @Override
    public Object getContent() {
        omero.RString s = ((TermAnnotation) asAnnotation()).getTermValue();
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
            throw new IllegalArgumentException("Term not valid.");
        }
        if (content instanceof String) {
            setTerm((String) content);
        } else {
            throw new IllegalArgumentException("Term not valid.");
        }
    }

}
