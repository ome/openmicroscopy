/*
 * org.openmicroscopy.shoola.env.config.Parser
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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  

//Third-party libraries
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Application-internal dependencies


/**
 * Fills up a registry with the entries in a configuration file.
 * Parses a configuration file, extracts its entries (only <code>entry</code>
 * and <code>structuredEntry</code> tags are taken into account), obtains an
 * {@link Entry} object to represent each of those entries and adds these
 * objects to a given {@link RegistryImpl} object. 
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
class Parser
{

	/** The tags that we handle. */
	static private String[]		tagsEntry = { Entry.ENTRY, Entry.STRUCT_ENTRY };
	 
	/** Points to the configuration file schema. */ 
	private String          	configFileXMLSchema;
	
	/** 
	 * Tells whether or not we're validating the configuration file against
	 * a schema.
	 */  
	private boolean         	validating;
	
	/** The configuration file. */
    private Document        	document;
    
    /** Points to the configuration file. */
    private String          	configFile;
    
    /** Collects all tags that we have to handle from the configuration file. */
    private List	        	entriesTags;
    
    /** The registry that we have to fill up. */
    private RegistryImpl    	registry;
    
	/** 
	 * Creates a new instance to fill up the specified registry with the
	 * entries from the specified coniguration file.
	 * The configuration file is not validated against a schema.
	 *
	 * @param configFile	Path to the configuration file.
	 * @param registry		The registry to fill up.         
	 */
    Parser(String configFile, RegistryImpl registry)
    { 
		validating = false;
        this.configFile = configFile;
        this.registry = registry;
		entriesTags = new ArrayList();
    }
    
	/** 
	 * Creates a new instance to fill up the specified registry with the
	 * entries from the specified coniguration file.
	 * The configuration file is validated against the specified schema
	 * (currently not implemented).
	 *
	 * @param configFile			Path to the configuration file.
	 * @param configFileXMLSchema	The schema to be used for validation.
	 * @param registry				The registry to fill up. 
	 */    
    Parser(String configFile, String configFileXMLSchema, RegistryImpl registry)
    {
        this.configFileXMLSchema = configFileXMLSchema;
        validating = true;
		this.configFile = configFile;
		entriesTags = new ArrayList();
		this.registry = registry;
    }
    
	/** 
	 * Parses the configuration file, extracts its entries (only
	 * <code>entry</code> and <code>structuredEntry</code> tags are taken into
	 * account), obtains an {@link Entry} object to represent each of those
	 * entries and adds these objects to the given {@link RegistryImpl} object.
	 * 
	 * @throws ConfigException	If an error occurs and the registry can't be
	 * 							filled up.
	 */
    void parse()
    	throws ConfigException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new File(configFile));
            if (validating) {
                factory.setValidating(true);   
                factory.setNamespaceAware(true);
                validate();
            }
            readConfigEntries();
            Iterator i = entriesTags.iterator();
			Node node;
            while (i.hasNext()) {
               node = (Node) i.next();
               Entry entry = Entry.createEntryFor(node);
               registry.addEntry(entry);
            }
        } catch (ConfigException ce) {
        	throw ce;
        } catch (Exception e) { 
        	rethrow(e);
        }   
    }
    
    /**
     * Wraps the original exception into a {@link ConfigException}, which is
     * then re-thrown with an error message.
     * 
     * @param e	The original exception.
     * @throws ConfigException	Wraps the original exception and contains an
     * 							error message.
     */
    private void rethrow(Exception e)
    	throws ConfigException
    {
		StringBuffer msg = new StringBuffer(
							"An error occurred while attempting to process ");
		msg.append(configFile);
		msg.append(".");
		String explanation = e.getMessage();
		if (explanation != null && explanation.length() != 0) {
			msg.append(" (");
			msg.append(explanation);
			msg.append(")");	
		}
		throw new ConfigException(msg.toString(), e); 
    }
    
	/** 
	 * Retrieves the content of the tags that we handle.
	 * Stores their DOM representation (DOM node) into a list.
	 */
    private void readConfigEntries()
    {
		NodeList list;
		Node n;
        for (int k = 0; k < tagsEntry.length; ++k) {
            list = document.getElementsByTagName(tagsEntry[k]);
			for (int i = 0; i < list.getLength(); ++i) {
				n = list.item(i);
				if (n.hasChildNodes()) entriesTags.add(n);
			}
        }
    }
    
	/**
	 * Validates the configuration file against the configuration schema.
	 * Not implemented yet.
	 */
    private void validate()
    	throws ConfigException
    {
    	//TODO: implement when we define a schema for cfg files.
    }
       
}
