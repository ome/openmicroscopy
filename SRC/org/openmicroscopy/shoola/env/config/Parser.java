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
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  

//Third-party libraries
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Application-internal dependencies


/**
 * In charge of parsing a configuration file.
 * Extracting entries 
 * (<code>entry</code> and <code>structuredEntry</code> tags) obtaining 
 * a <code>Entry</code> object to represent each of those entries, adding 
 * the object to a given <code>RegistryImpl</code> object.
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * <br><b>Internal version:</b> $Revision$  $Date$
 * @version 2.2
 * @since OME2.2
 */
class Parser
{

    private Document        document;
    private String          configFile;
    private String          configFileXMLSchema;
    private ArrayList       entriesTags;
    private RegistryImpl    registry;
    //validate against the XMLschema: not yet implemented 
    private boolean         validating = false; 
    // we only retrieve the content of the following tags
    static String[]         tagsEntry = {
        "entry",
        "structuredEntry",
    };
	/** 
	 * Creates an instance of Parser with one parameter.
	 *
	 * @param configFile		configuration file (XML file).
	 * @param registry      	registryImpl.         
	 */
    Parser(String configFile, RegistryImpl registry)
    { 
        this.configFile = configFile;
        this.registry = registry;
    }
    
	/** 
	 * Creates an instance of Parser with two parameters
	 * not useful now b/c no XMLSchema for configFile available.
	 *
	 * @param  configFile				configuration file (XML file).
	 * @param configFileXMLSchema   	XML schema of configuration file.
	 * @param registry					registryImpl.
	 */    
    Parser(String configFile, String configFileXMLSchema, RegistryImpl registry)
    {
        this.configFile = configFile;
        this.configFileXMLSchema = configFileXMLSchema;
        this.registry = registry;
        validating = true;
    }
    
	/** 
	 * Parse the XML configuration file and build a DOM tree. 
	 * 
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
            while (i.hasNext()) {
               Node node = (Node)i.next();
               Entry entry = Entry.createEntryFor(node);
               registry.addEntry(entry);
            }
        } catch (Exception e) { 
        	throw new ConfigException("An error occurred while attempting"+
        								"to process: "+configFile, e); 
        }   
    }
    
	/** 
	 * Retrieves the content of the  entry and structuredEntry tags.
	 * Stores the DOM representation i.e. DOM node into an arrayList
	 */
    private void readConfigEntries()
    {
        entriesTags = new ArrayList();
        for (int k = 0; k < tagsEntry.length; ++k) {
            NodeList list = document.getElementsByTagName(tagsEntry[k]);
            int l = entriesTags.size();
            for (int i = 0; i < list.getLength(); ++i) {
                Node n = list.item(i);
                if (n.hasChildNodes()) entriesTags.add(k*l+i, n);
            }
        }
    }
	/**
	 * Validate against the config schema not yet implemented.
	 */
    private void validate()
    {
    }
    
    
}
