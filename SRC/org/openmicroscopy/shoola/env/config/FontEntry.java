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
 * Creates the font.
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
	
	private static Map fontStyle;
	
	static {
		fontStyle = new HashMap();
		fontStyle.put("plain", new Integer(Font.PLAIN));
		fontStyle.put("italic", new Integer(Font.ITALIC));
		fontStyle.put("bold", new Integer(Font.BOLD));	
	}
	
	/** tag's name. */
	private static final String		NAME = "family", SIZE = "size", 
									STYLE = "style";
	/** Default size. */
    private static int				DEFAULT_SIZE = 12; 
    
    /** Default style. */
    private static int 				DEFAULT_STYLE = Font.PLAIN;
    
    private static int				MAX_SIZE = 20, MIN_SIZE =2;
    
    /** Font attributes. */
    private static int 				size;
    private static int 				style;
    private static String 			name;
    
    
    private Font value;
    
    FontEntry() {}
    
	/** Implemented as specified by {@link Entry}. */  
    protected void setContent(Node node) { 
    	
        try {
            //the node is supposed to have tags as children, add control
            //b/c we don't use yet a XMLSchema config
            if (node.hasChildNodes()) {
                NodeList childList = node.getChildNodes();
				Node child;
                for (int i = 0; i < childList.getLength(); i++) {
                    child = childList.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE)  
						setFontAttribute(child.getFirstChild().getNodeValue(), 
                                child.getNodeName()) ;
                }
				value = new Font(name, style, size);   
            } 
        } catch (DOMException dex) { throw new RuntimeException(dex); }
    }
    
	/** Implemented as specified by {@link Entry}. */  
    Object getValue() { return value; }
      
	/** 
	 * Initializes the fields to create the font.
	 * 
	 * @param tagValue			tag's value.
	 * @param tagName			tag's name.
	 */
	void setFontAttribute(String tagValue, String tagName)
	{
		if (tagName.equals(NAME)) name = tagValue;
		else if (tagName.equals(SIZE)) {
			try {
				size = Integer.parseInt(tagValue);
				if (size > MAX_SIZE || size <= MIN_SIZE) size = DEFAULT_SIZE; 
			} catch (Exception e) {
				size = DEFAULT_SIZE;
			}
		} else if (tagName.equals(STYLE)) {
			Integer i = (Integer) fontStyle.get(tagValue);
			if (i == null) style = DEFAULT_STYLE; 
			else style = i.intValue();
		}
	}
	
}
