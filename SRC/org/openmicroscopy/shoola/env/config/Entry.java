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
import java.util.Map;

//Third-party libraries 
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

//Application-internal dependencies

/**  
 * Sits at the base of a hierarchy of classes that represent entries in 
 * configuration files.
 * It represents a name-value pair, where the name is the content of the 
 * <i>name</i> attribute of a configuration entry (which is stored by the
 * <code>name</code> field) and the value is the object representing the
 * entry’s content.
 * <p>As the logic for building an object from the entry’s content depends on
 * what is specified by the <i>type</i> attribute, this class declares an
 * abstract {@link #getValue() getValue} method which subclasses implement to
 * return the desired object.  So we have subclasses ({@link StringEntry}, 
 * {@link IntegerEntry}, {@link IconFactoryEntry}, etc.) to handle the content
 * of an entry tag (either <i>entry</i> or <i>structuredEntry</i>) in 
 * correspondence of each predefined value of the <i>type</i> attribute 
 * (<i>"string"</i>, <i>"integer"</i>, <i>"icons"</i>, and so on).</p> 
 * <p>Given an entry tag, the {@link #createEntryFor(Node) createEntryFor}
 * static method (which can be considered a Factory Method) creates a concrete
 * <code>Entry</code> object to handle the conversion of that tag’s content
 * into an object.  Subclasses implement the
 * {@link #setContent(Node) setContent} method to grab the tag’s content, which
 * is then used for building the object returned by the implementation of 
 * {@link #getValue()}.</p>
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
	
	/** The <i>entry</i> tag. */
	static String				ENTRY = "entry";
	
	/** The <i>structuredEntry</i> tag. */
	static String				STRUCT_ENTRY = "structuredEntry";

	/** The <i>name</i> attribute. */
	static private String      	NAME = "name";
    
	/** The <i>type</i> attribute. */
	static private String		TYPE = "type";	
	
	/** 
	 * The default value of the <i>type</i> attribute for the <i>entry</i> tag.
	 */
	static private String		DEFAULT_ENTRY = "string";
	
	/** 
	 * The default value of the <i>type</i> attribute for the
	 * <i>structuredEntry</i> tag.
	 */
	static private String		DEFAULT_STRUCT_ENTRY ="map";
    
    /**
     * Maps each predefined value of the <i>type</i> attribute onto the 
     * <i>FQN</i> of the hanlder class.
     */
	static private Map	     	contentHandlers;
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
    
    /**
     * Holds the contents of the <i>name</i> and <i>type<i> attributes of an
     * entry tag (either <i>entry</i> or <i>structuredEntry</i>).
     */
    private static class NameTypePair
    {
        String  name, type;
    }
    
    
	/** 
	 * Creates a concrete <code>Entry</code> object to handle the conversion of
	 * the passed tag’s content into an object. 
	 *
	 * @param tag	DOM node representing either an <i>entry</i> or 
	 * 				<i>structuredEntry</i> tag.
	 * @return See above.
	 * @throws ConfigException If the configuration entry couldn't be handled.                  
	 */  
    static Entry createEntryFor(Node tag)
		throws ConfigException
    {
        Entry entry = null;
		
		//TODO: remove this check when we have a proper schema.
		if (!tag.hasAttributes())
			throw new ConfigException("Missing tag's attributes.");
        	
    	//First get the couple (name, type) -- type will be set to the 
    	//appropriate default if necessary.  
        NameTypePair ntp = retrieveEntryAttributes(tag);
          
        //Retrieve the hanlder for type if it's a built-in type.   
        Class handler = (Class) contentHandlers.get(ntp.type);
        
        try {
            if (handler == null)
            	//Then type is not one of built-in types, this means that
            	//it must be the FQN of a custom handler. Load it.
            	handler = Class.forName(ntp.type);
            	
            //Finally create the hanlder.
            entry = (Entry) handler.newInstance();
        } catch(Exception e) {
        	throw new ConfigException("Can't instantiate tag's handler.", e); 
        } 
        
        //Set name, but delegate value setting.
        entry.name = ntp.name;
        entry.setContent(tag);
            
        return entry;
    }
    
	/** 
	 * Retrieves the value of the <i>name</i> and <i>type</i> attributes.
	 * Sets the appropriate default for <i>type</i> if no value was provided.
	 *
	 * @param tag	DOM node representing either an <i>entry</i> or 
	 * 				<i>structuredEntry</i> tag.
	 * @return A <code>NameTypePair</code> object that stores the the value of
	 * 			the <i>name</i> and <i>type</i> attributes.
	 * @throws ConfigException If no value was provided for the name attribute.
	 */    
	private static NameTypePair retrieveEntryAttributes(Node tag)
		throws ConfigException
	{
		NameTypePair ntp = new NameTypePair();
		NamedNodeMap attrList = tag.getAttributes();  //Get this tag's attrs.
		Node attribute;
		
		//Store the values of name and type attributes into ntp.
		for (int i = 0; i < attrList.getLength(); ++i) {
			attribute = attrList.item(i);
			if (attribute.getNodeName() == NAME) 
				ntp.name = attribute.getNodeValue();
			else if (attribute.getNodeName() == TYPE)  
				ntp.type = attribute.getNodeValue();
		}
		
		//Complain if no name attribute was provided.
		//TODO: remove this check when we have a proper schema.
		if (ntp.name == null || ntp.name.length() == 0)
			throw new ConfigException("Missing name attribute.");
		
		//Set appropriate default for type if no value was provided.
		if (ntp.type == null)
			ntp.type = (tag.getNodeName() == ENTRY ? DEFAULT_ENTRY : 
						//if not entry tag then must be structuredEntry tag
							DEFAULT_STRUCT_ENTRY);
		
		return ntp;
	}



	/** The content of the <i>name</i> attribute. */
	private String      name;
	
	
	/** 
	 * Returns the content of the <i>name</i> attribute 
	 * of a configuration entry.
	 *
	 * @return The content of the <i>name</i> attribute.
	 */  
    String getName()
    {
        return name;
    }
    
    /**
     * Subclasses implement this method to grab the tag’s content, which is
     * then used for building the object returned by the implementation of 
     * {@link #getValue()}.
     * 
     * @param tag	DOM node representing either an <i>entry</i> or 
	 * 				<i>structuredEntry</i> tag.
     */
    protected abstract void setContent(Node tag);
    
    /**
     * Subclasses implement this method to return an object that represents the
     * contents of the configuration entry.
     * 
     * @return See above.
     */
	abstract Object getValue();
    
}
