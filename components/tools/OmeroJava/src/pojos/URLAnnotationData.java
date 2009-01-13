/*
 * pojos.URLAnnotationData
 *
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
package pojos;

import static omero.rtypes.*;
import omero.model.UrlAnnotation;
import omero.model.UrlAnnotationI;

/**
 * Define a URL Annotation. Note that a URL annotation is a specific text
 * annotation.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $)
 *          </small>
 * @since OME3.0
 */
public class URLAnnotationData extends AnnotationData {

    /** The <code>http</code> scheme for the url. */
    public static final String HTTP = "http";

    /** The <code>https</code> scheme for the url. */
    public static final String HTTPS = "https";

    /** The supported schemes for URL. */
    public static final String[] URLS;

    static {
        URLS = new String[2];
        URLS[0] = HTTP;
        URLS[1] = HTTPS;
    }

    /**
     * Controls if the value is valid URL.
     * 
     * @param value
     *            The value to check.
     */
    private void validateURL(String value) {
        if (value == null) {
            throw new IllegalArgumentException("URL not valid.");
        }
        // FIXME - No longer using commons-validator
    }

    /**
     * Creates a new instance.
     * 
     * @param url
     *            The value to set.
     */
    public URLAnnotationData(String url) {
        super(UrlAnnotationI.class);
        setURL(url);
    }

    /**
     * Creates a new instance.
     * 
     * @param annotation
     *            The value to set.
     */
    public URLAnnotationData(UrlAnnotation annotation) {
        super(annotation);
    }

    /**
     * Sets the url.
     * 
     * @param url
     *            The value to set.
     */
    public void setURL(String url) {
        validateURL(url);
        ((UrlAnnotation) asAnnotation()).setTextValue(rstring(url));
    }

    /**
     * Returns the <code>url</code>.
     * 
     * @return See above.
     */
    public String getURL() {
        return getContentAsString();
    }

    /**
     * Returns the textual content of the annotation.
     * 
     * @see AnnotationData#getContent()
     */
    @Override
    public Object getContent() {
        omero.RString s = ((UrlAnnotation) asAnnotation()).getTextValue();
        return s == null ? null : s.getValue();
    }

    /**
     * Returns the textual content of the annotation.
     * 
     * @see AnnotationData#getContentAsString()
     */
    @Override
    public String getContentAsString() {
        return (String) getContent();
    }

    /**
     * Sets the text annotation.
     * 
     * @see AnnotationData#setContent(Object)
     */
    @Override
    public void setContent(Object content) {
        if (content == null) {
            throw new IllegalArgumentException("URL not valid.");
        }
        if (content instanceof String) {
            setURL((String) content);
        } else {
            throw new IllegalArgumentException("URL not valid.");
        }
    }

}
