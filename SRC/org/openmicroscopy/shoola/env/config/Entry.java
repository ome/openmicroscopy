/*
 * org.openmicroscopy.shoola.env.config.Entry
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

//Java imports
import java.util.HashMap;

//Third-party libraries 
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

//Application-internal dependencies

/** 
 * Sits at the base of a hierarchy of classes that represent entries in 
 * configuration file.
 * It represents a name-value pair, where the name is the content of 
 * the <code>name</code> attribute.
 * Subclasses of <code>Entry</code> implement the <code>setContent()</code> 
 * method to grab the tag's content which is then used for building the object
 * returned by the implementation of <code>getValue()<code>
 *
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

abstract class Entry
{
    
    static HashMap     	contentHandlers;
    static String      	NAME = "name", TYPE = "type",
						ENTRY = "entry", STRUCT_ENTRY = "structuredEntry",
						DEFAULT_ENTRY = "string", DEFAULT_STRUCT_ENTRY ="map";
    static {
        contentHandlers = new HashMap();
        contentHandlers.put("map", MapEntry.class);
        contentHandlers.put("string", StringEntry.class);
        contentHandlers.put("integer", IntegerEntry.class);
        contentHandlers.put("float", FloatEntry.class);
        contentHandlers.put("double", DoubleEntry.class);
        contentHandlers.put("boolean", BooleanEntry.class);
        contentHandlers.put("OMEIS", OMEISEntry.class);
        contentHandlers.put("OMEDS", OMEDSEntry.class);
        contentHandlers.put("font", FontEntry.class);
        contentHandlers.put("icons", IconFactoryEntry.class);
        contentHandlers.put("agents", AgentsEntry.class);
    }
    
    private static class NameTypePair
    {
        String  name, type;
    }
    private String      name;
    
	/** 
	 * For a given entry or structuredEntry tag, creates a concrete 
	 * <code>Entry</code> object to handle the conversion of the tag's content 
	 * into an object.
	 *
	 * @param node             DOM node representing the tag.
	 * @return Entry                    
	 */  
    static Entry createEntryFor(Node node)
    {
        Entry entry = null;
        //to be removed when we have xmlSchema (config)
        if (node.hasAttributes()) { 
            NameTypePair ntp = retrieveEntryAttributes(node);
            String key = null;
            if (node.getNodeName() == ENTRY) // entry tag
                key = ntp.type == null? DEFAULT_ENTRY : ntp.type;
            else if (node.getNodeName() == STRUCT_ENTRY) // structuredEntry tag 
                key = ntp.type == null? DEFAULT_STRUCT_ENTRY : ntp.type;
            Class handler = (Class) contentHandlers.get(key);
            try {
                if (handler == null) handler = Class.forName(key); 
                entry = (Entry) handler.newInstance();
            } catch(Exception e) { throw new RuntimeException(e); } 
            entry.name = ntp.name;
            entry.setContent(node);
        } //throw...
        return entry;
    }
    
	/** 
	 * Retrieves the value of the attributes name and type and initializes.
	 *
	 * @param n    DOM node.
	 * @return NameTypePair
	 */    
	private static NameTypePair retrieveEntryAttributes(Node n)
	{
		NameTypePair    ntp = new NameTypePair();
		NamedNodeMap    list = n.getAttributes();
		try {
			for (int i = 0; i < list.getLength(); ++i) {
				Node na = list.item(i);
				if (na.getNodeName() == NAME) 
					ntp.name = na.getNodeValue();
				else if (na.getNodeName() == TYPE)  
					ntp.type = na.getNodeValue();
			}
		} catch (DOMException dex) { throw new RuntimeException(dex); }
		if (ntp.name == null || ntp.name.length() == 0)
			throw new RuntimeException(" Blah..");
		return ntp;
	}
    
	/** 
	 * Returns the content of the <code>name</code> attribute 
	 * of a configuration entry.
	 *
	 * @return String   the content of the <code>name</code> attribute.
	 */  
    public String getName()
    {
        return name;
    }
    
    abstract Object getValue();
    protected abstract void setContent(Node node);
    abstract void setContent(Object content);
    
}
