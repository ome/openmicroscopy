/*
 * org.openmicroscopy.shoola.env.config.FontEntry
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


package org.openmicroscopy.shoola.env.config;

// Java imports 
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

// Third-party libraries
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Application-internal dependencies

/**
 * Hanldes a <i>structuredEntry</i> of type <i>font</i>.
 * The content of the entry is stored in a {@link Font} object, which is
 * then returned by the {@link #getValue() getValue} method.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class FontEntry
    extends Entry
{
	
	/** 
	 * Maps the supported font styles to the corresponding constants defined
	 * by the {@link Font} class.
	 */
	private static Map 		FONT_STYLES;
	static {
		FONT_STYLES = new HashMap();
		FONT_STYLES.put("plain", new Integer(Font.PLAIN));
		FONT_STYLES.put("italic", new Integer(Font.ITALIC));
		FONT_STYLES.put("bold", new Integer(Font.BOLD));	
	}
	
	/** 
	 * The name of the tag, within this <i>structuredEntry</i>, that is used
	 * to specify the font's family.
	 */
	private static final String		FAMILY_TAG = "family";
	
	/** 
	 * The name of the tag, within this <i>structuredEntry</i>, that is used
	 * to specify the font's size.
	 */
	private static final String		SIZE_TAG = "size";
	
	/** 
	 * The name of the tag, within this <i>structuredEntry</i>, that is used
	 * to specify the font's style.
	 */
	private static final String		STYLE_TAG = "style";
	
	/** 
	 * Default font size.
	 * This is the value that we use to create a {@link Font} object if the 
	 * <i>size</i> tag can't be parsed into an integer or if the parsed value
	 * is not between the {@link #MIN_SIZE}, {@link #MAX_SIZE} interval. 
	 */
    private static final int		DEFAULT_SIZE = 12; 
    
    /** The minimum allowed font size. */
	private static final int		MIN_SIZE =2;
	
	/** The maximum allowed font size. */
	private static final int		MAX_SIZE = 20;
	
    /** 
     * Id of the default font style. 
     * This is the value that we use to create a {@link Font} object if the
     * <i>style</i> tag doesn't specify one of the styles defined by
     * {@link #FONT_STYLES}.
     */
    private static final int 		DEFAULT_STYLE = Font.PLAIN;
    
    
	/** The font built from the configuration information. */
    private Font value;
    
    
	/** Creates a new instance. */
    FontEntry() {}
    
	/** 
	 * Returns a {@link Font} object, built from the configuration information.
	 * 
	 * @return	See above.
	 */  
	Object getValue() { return value; }
	
    /** 
     * Implemented as specified by {@link Entry}. 
     * @see Entry#setContent(Node)
     * @throws ConfigException If the configuration entry couldn't be handled.
     */   
    protected void setContent(Node node)
    	throws ConfigException 
    { 
		try {
			Map tags = extractValues(node);
			value = new Font((String) tags.get(FAMILY_TAG),
								getStyle(tags), getSize(tags));
		} catch (DOMException dex) { 
			rethrow("Can't parse font entry.", dex);
		} 
    }
	
	/**
	 * Figures out what font size to use from the configuration information.
	 * This method tries to parse the value of the size tag into an integer.
	 * Failing that or if the parsed value is not between the {@link #MIN_SIZE},
	 * {@link #MAX_SIZE} interval, then the {@link #DEFAULT_SIZE} is returned.
	 * 
	 * @param fontAttributes The pairs <i>(tag-name, tag-value)</i> built from
	 * 						the tags contained within the entry tag.
	 * @return See above.
	 */
	private int getSize(Map fontAttributes)
	{
		String value = (String) fontAttributes.get(SIZE_TAG);
		int size = DEFAULT_SIZE;
		try {
			size = Integer.parseInt(value);
			if (size < MIN_SIZE || MAX_SIZE < size)	size = DEFAULT_SIZE;	
		} catch (NumberFormatException nfe) {}
		return size;
	}
	
	/**
	 * Figures out what font style to use from the configuration information.
	 * 
	 * @param fontAttributes The pairs <i>(tag-name, tag-value)</i> built from
	 * 						the tags contained within the entry tag.
	 * @return The id of the font style as defined in {@link #FONT_STYLES}. 
	 * 			If the font style specified by the configuration entry doesn't
	 * 			map to a style defined by {@link #FONT_STYLES}, then the 
	 * 			{@link #DEFAULT_STYLE} is returned.
	 */
	private int getStyle(Map fontAttributes)
	{
		String value = (String) fontAttributes.get(STYLE_TAG);
		int style = DEFAULT_STYLE;
		Integer id = (Integer) FONT_STYLES.get(value);
		if (id != null)		style = id.intValue();
		return style;
	}
	
	/**
	 * Extracts the values of the tags within an <i>font</i> tag and puts them
	 * in a map keyed by tag names.
	 *  
	 * @param entry	The <i>font</i> entry tag.
	 * @return The pairs <i>(tag-name, tag-value)</i> built from the tags
	 * 			contained within the <i>font</i> tag.
	 * @throws DOMException If the entry contents couldn't be retrieved.
	 * @throws ConfigException If the <i>font</i> tag is not structured as
	 * 							expected.
	 */
	private Map extractValues(Node entry)
		throws DOMException, ConfigException
	{
		Map tags = new HashMap();
		if (entry.hasChildNodes()) {
			NodeList children = entry.getChildNodes();
			int n = children.getLength();
			Node child;
			while (0 < n) {
				child = children.item(--n);
				if (child.getNodeType() == Node.ELEMENT_NODE)
					extractFontTag(child, tags); 
			}
		}
		if (tags.keySet().size() != 3)
			throw new ConfigException("Missing tags within font tag.");
		return tags;
	}
	//TODO: remove the checks (which are not complete anyway) and the
	//ConfigException when we have an XML schema for config files. 
	
	/**
	 * Adds the given tag name and value to the passed map.
	 * 
	 * @param tag	A tag within the <i>font</i> tag.
	 * @param values	The map containing the pairs 
	 * 					<i>(tag-name, tag-value)</i> relative to the
	 * 					<i>font</i> tag that contains <code>tag</code>.
	 * @throws DOMException If the entry contents couldn't be retrieved.
	 * @throws ConfigException If <code>tag</code> is not one of the tags that
	 * 							we expect to be within an <i>font</i> tag.
	 */
	private void extractFontTag(Node tag, Map values) 
		throws DOMException, ConfigException
	{
		String tagName = tag.getNodeName(),
				tagValue = tag.getFirstChild().getNodeValue();
		if (FAMILY_TAG.equals(tagName) || 
			SIZE_TAG.equals(tagName) ||
			STYLE_TAG.equals(tagName)) {
				values.put(tagName, tagValue);
				return;
			}
		throw new ConfigException(
			"Unrecognized tag within the font entry: "+tagName);
	}
	//TODO: remove the checks (which are not complete anyway) and the
	//ConfigException when we have an XML schema for config files.
	
}
