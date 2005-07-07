/*
 * org.openmicroscopy.xdoc.navig.xml.Element
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.xdoc.navig.xml;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Represents a parsed <i>XML</i> element.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class Element
{
    
    /** Denotes an element tag in the form <code>&lt;xxx /&gt;</code>. */
    public static final int     INLINE = 1;
    
    /** Denotes an element tag in the form <code>&lt;xxx&gt;</code>. */
    public static final int     START = 2;
    
    /** Denotes an element tag in the form <code>&lt;/xxx&gt;</code>. */
    public static final int     END = 3;
    
    
    /**
     * Finds out what kind of element is <code>tag</code>.
     * 
     * @param tag The <i>XML</i> representation of the element.  Mustn't be
     *              <code>null</code>.
     * @return One of the constants defined by this class.
     */
    private static int determineType(String tag)
    {
        int type;
        char endDelimiter = '/';
        if (tag.indexOf(endDelimiter) == -1) type = START;
        else {
            int pos = 1;
            while (tag.charAt(pos) == ' ') ++pos;
            if (tag.charAt(pos) == endDelimiter) type = END;
            else type = INLINE;
        }
        return type;
    }
    
    /** The <i>XML</i> representation of this element. */
    private String      xml;
    
    /** The element type as defined by one of this class' constants. */
    private int         type;
    
    
    /**
     * Creates a new instance.
     * 
     * @param xml The <i>XML</i> representation of this element.  Mustn't be
     *              <code>null</code>.
     */
    Element(String xml)
    {
        if (xml == null) throw new NullPointerException("No xml.");
        this.xml = xml;
        type = determineType(xml);
    }
    
    /**
     * Returns this element's type.
     * 
     * @return The element type as defined by one of this class' constants.
     */
    public int getType()
    {
        return type;
    }
    
    /**
     * Returns this element's name.
     * 
     * @return See above.
     */
    public String getName()
    {
        int start = 1;
        if (type == END) start = xml.indexOf('/')+1;
        while (xml.charAt(start) == ' ') ++start;
        int end = start;
        char c = xml.charAt(end);
        while (c != ' ' && c != '/' && c != '>') c = xml.charAt(++end); 
        return xml.substring(start, end);
    }
    
    /**
     * Returns the value of the specified attribute, if any.
     * If no attribute exists with the specified <code>name</code>, then
     * <code>null</code> is returned.  In particular, <code>null</code> is
     * returned if <code>name</code> is <code>null</code>.  Otherwise, the
     * value of that attribute is returned as a <code>String</code>.  This
     * will be the empty string if the attribute has no content, like in
     * <code>attr=""</code>. 
     * 
     * @param name The attribute name.
     * @return The attribute value.
     */
    public String getAttribute(String name)
    {
        String value = null;
        if (name != null) {
            int n = xml.indexOf(name);
            if (n != -1) {
                int q1 = xml.indexOf('"', n);
                if (q1 != -1) {
                    int q2 = xml.indexOf('"', q1+1);
                    if (q2 != -1) {
                        value = xml.substring(q1+1, q2);
                    }
                }
            }
        }
        return value;
    }
    
    
    //NOTE: The element name and attributes are not cached after being computed.
    //      The reason is that the TOCParser only calls getName/Attribute once
    //      per element.  So it's not worth the extra work.
    
}
