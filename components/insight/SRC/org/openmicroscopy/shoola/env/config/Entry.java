/*
 * org.openmicroscopy.shoola.env.config.Entry
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
 * content of the entry tag.
 * <p>As the logic for building an object from the content of the entry tag
 * depends on what is specified by the <i>type</i> attribute, this class 
 * declares an abstract {@link #getValue() getValue} method which subclasses
 * implement to return the desired object.  So we have subclasses 
 * ({@link StringEntry}, {@link IntegerEntry}, {@link IconFactoryEntry}, etc.) 
 * to handle the content of an entry tag (either <i>entry</i> or 
 * <i>structuredEntry</i>) in correspondence of each predefined value of 
 * the <i>type</i> attribute (<i>"string"</i>, <i>"integer"</i>, 
 * <i>"icons"</i>, and so on).</p> 
 * <p>Given an entry tag, the {@link #createEntryFor(Node) createEntryFor}
 * static method (which can be considered a Factory Method) creates a concrete
 * <code>Entry</code> object to handle the conversion of the content of that tag
 * into an object.  Subclasses implement the
 * {@link #setContent(Node) setContent} method to grab the content of the tag,
 * which is then used for building the object returned by the implementation of 
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
     * <i>FQN</i> of the handler class.
     */
	static private Map<String, Class<?>>	contentHandlers;
    static {
        contentHandlers = new HashMap<String, Class<?>>();
        contentHandlers.put("map", MapEntry.class);
        contentHandlers.put("string", StringEntry.class);
        contentHandlers.put("integer", IntegerEntry.class);
        contentHandlers.put("float", FloatEntry.class);
        contentHandlers.put("long", LongEntry.class);
        contentHandlers.put("double", DoubleEntry.class);
        contentHandlers.put("boolean", BooleanEntry.class);
        contentHandlers.put("OMERODS", OMEROEntry.class);
        contentHandlers.put("font", FontEntry.class);
        contentHandlers.put("color", ColorEntry.class);
        contentHandlers.put("icons", IconFactoryEntry.class);
        contentHandlers.put("agents", AgentsEntry.class);
        contentHandlers.put("agents", AgentsEntry.class);
        contentHandlers.put("plugins", PluginEntry.class);
    }
    
    /**
     * Holds the contents of the <i>name</i> and <i>type</i> attributes of an
     * entry tag (either <i>entry</i> or <i>structuredEntry</i>).
     */
    private static class NameTypePair
    {
    	/** The name attribute. */
        String  name;
        
        /** The type attribute. */
        String  type;
    }
      
	/** 
	 * Creates a concrete <code>Entry</code> object to handle the conversion of
	 * the content of the passed tag into an object. 
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
          
        //Retrieve the handler for type if it's a built-in type.   
        Class<?> handler = contentHandlers.get(ntp.type);
        
        try {
            if (handler == null)
            	//Then type is not one of built-in types, this means that
            	//it must be the FQN of a custom handler. Load it.
            	handler = Class.forName(ntp.type);
            	
            //Finally create the handler.
            entry = (Entry) handler.newInstance();
        } catch(Exception e) {
        	rethrow(ntp, e);
        } 
        
        //Set name, but delegate value setting.
        entry.name = ntp.name;
        entry.setContent(tag);
            
        return entry;
    }
    
	/**
	 * Convenience method to wrap and re-throw an exception occurred while
	 * trying to create a tag handler. 
	 * Wraps the original exception into a {@link ConfigException}, which is
	 * then re-thrown with an error message.
	 * 
	 * @param ntp 	The name and type of the tag.
	 * @param e		The original exception.
	 * @throws ConfigException	Wraps the original exception and contains an
	 * 							error message.
	 */
	private static void rethrow(NameTypePair ntp, Exception e)
		throws ConfigException
	{
		StringBuffer msg = new StringBuffer(
								"Can't instantiate tag's handler. Tag name: ");
		msg.append(ntp.name);
		msg.append(", type: ");
		msg.append(ntp.type);
		msg.append(".");        	
		throw new ConfigException(msg.toString(), e); 
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
		NamedNodeMap attrList = tag.getAttributes();  //Get attrs.
		Node attribute;
		
		//Store the values of name and type attributes into ntp.
		for (int i = 0; i < attrList.getLength(); ++i) {
			attribute = attrList.item(i);
			if (NAME.equals(attribute.getNodeName())) 
				ntp.name = attribute.getNodeValue();
			else if (TYPE.equals(attribute.getNodeName()))
				ntp.type = attribute.getNodeValue();
		}
		//Complain if no name attribute was provided.
		//TODO: remove this check when we have a proper schema.
		if (ntp.name == null || ntp.name.length() == 0)
			throw new ConfigException("Missing name attribute");
		
		//Set appropriate default for type if no value was provided.
		if (ntp.type == null)
			ntp.type = (ENTRY.equals(tag.getNodeName()) ? DEFAULT_ENTRY : 
						//if not entry tag then must be structuredEntry tag
							DEFAULT_STRUCT_ENTRY);
		
		return ntp;
	}

	/** The content of the <i>name</i> attribute. */
	private String      name;

	/**
	 * Wraps the original exception into a {@link ConfigException}, which is
	 * then re-thrown with an error message.
	 * The error message will contain the specified context information plus
	 * the message, if any, of the original exception. 
	 * This method is used by subclasses to re-throw exceptions that may occur
	 * during the process of parsing a tag and build an object in the 
	 * {@link #setContent(Node) setContent} method.
	 * 
	 * @param message Some context information.
	 * @param e	The original exception.
	 * @throws ConfigException	Wraps the original exception and contains an
	 * 							error message.
	 */
	protected void rethrow(String message, Exception e)
		throws ConfigException
	{
		StringBuffer msg = new StringBuffer();
		if (message == null || message.length() == 0)
			message = "An error occurred.";	
		msg.append(message);
		String explanation = e.getMessage();
		if (explanation != null && explanation.length() != 0) {
			msg.append(" (");
			msg.append(explanation);
			msg.append(")");	
		}
		throw new ConfigException(msg.toString(), e); 
	}
	
    /**
     * Subclasses implement this method to grab the content of the tag,
     * which is then used for building the object returned by the 
     * implementation of {@link #getValue()}.
     * 
     * @param tag	DOM node representing either an <i>entry</i> or 
	 * 				<i>structuredEntry</i> tag.
	 * @throws ConfigException	If an error occurs in the process of 
	 * 							transforming the configuration entry into an
	 * 							object.
     */
    protected abstract void setContent(Node tag) throws ConfigException;
    
    /**
     * Subclasses implement this method to return an object that represents the
     * contents of the configuration entry.
     * 
     * @return See above.
     */
	abstract Object getValue();
	
	/** 
	 * Returns the content of the <i>name</i> attribute 
	 * of a configuration entry.
	 *
	 * @return The content of the <i>name</i> attribute.
	 */  
	String getName() { return name; }
    
}
