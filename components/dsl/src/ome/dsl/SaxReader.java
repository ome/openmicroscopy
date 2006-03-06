/*
 * ome.dsl.SaxReader
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package ome.dsl;

//Java imports
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

// Third-party libraries
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

// Application-internal dependencies


/** reads semantic-type-xml and produces a Set of SemanticType objects. 
 * Most logic is passed off to the {@see ome.dsl.SemanticType ST} and 
 * {@see ome.dsl.Property Property} classes. 
 */
public class SaxReader {

    private static Log log = LogFactory.getLog(SaxReader.class);
    
	/** input file */
	URL xmlFile; 
		
	/** handler which collects all types and properties from the input file */
	DSLHandler handler = new DSLHandler();
	
	/** SAXparser which does the actualy processing */
	javax.xml.parsers.SAXParser parser;
		
	/**
	 * @param file input file containing DSL for semantic types
	 */
	public SaxReader(String filename){
		xmlFile = this.getClass().getClassLoader().getResource(filename); 
		init();
	}
	
	public SaxReader(File file){
		try {
			xmlFile = file.toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(
                    "Error determining file's path:"+file+" :\n"+e.getMessage(),e);
		}
		init();
	}
	
	private void init(){
		try {
		    SAXParserFactory factory = SAXParserFactory.newInstance();
		    parser = factory.newSAXParser();
		    
		    // XMLReader reader = parser.getXMLReader();  
		} 
		catch (Exception e){
			throw new RuntimeException(
                    "Error setting up SaxReader :\n"+e.getMessage(),e);
		}	
	}
	
	/** parses file and returns types */
	public Set parse(){
	    try {
			parser.parse(xmlFile.getPath(), handler);
		} catch (Exception e) {
			throw new RuntimeException(
                    "Error parsing "+xmlFile+" :\n"+e.getMessage(),e);
		}
		
		return handler.process();

	}
	
}
	
class DSLHandler extends DefaultHandler {

    private static Log log = LogFactory.getLog(DSLHandler.class);
    
    	// Turns output on/off
	public boolean verbose = false;
    
	// Indention for output
	private String depth="";

	// For handling
	private Set types = new HashSet();	
	private SemanticType type;
	private Property property;
	
	/** dispatches to output (printing) and handling (object-creation) routines */
	public void startElement(String arg0, String arg1, String element,
			Attributes attrs) throws SAXException {
		if (verbose) outputStart(element,attrs);
		handleEntry(element,attrs);
		super.startElement(arg0, arg1, element, attrs);
	}
	
	/** dispatches to output (printing) and handling (object-creation) routines */ 
	public void endElement(String arg0, String arg1, String element)
			throws SAXException {
		super.endElement(arg0, arg1, element);
		handleExit(element);
		if (verbose) outputStop(element);
	}
	
	/** creates a new type or property based on element name */
	private void handleEntry(String element, Attributes attrs){
		if (Property.FIELDS.contains(element)){
		
			if (null!=property){
				throw new IllegalStateException(
                        "Trying to enter property "+element+
                        " from within property"+property);
			}
			
            if (null==type){
                throw new IllegalStateException(
                        "Trying to create property "+element+
                        " without a type!");
            }
            
			property = Property.makeNew(element,type,attrs2props(attrs));
            
		} else if ("properties".equals(element)) {
            // ok. these usually contains lots of properties
        } else if (SemanticType.TYPES.contains(element)){
			if (null!=type){
				throw new IllegalStateException(
                        "Trying to enter type "+element+
                        " from within type "+type);
			}
			
			type = SemanticType.makeNew(element,attrs2props(attrs));
            
		} else if ("types".equals(element)) {
		    // also ok.
        } else {
            log.warn("Deprecated: In the future elements of type "+element+
                    " will be considered an error.");
        }
            
	}
	
	/** checks to see that after type creation, the model is in a valid state */
	private void handleExit(String element){
		
		if (Property.FIELDS.contains(element)){
			
			if (null==property){
				throw new IllegalStateException("Exiting non-extant property!\n"+"Element:"+element+"\nType:"+type+"\nProperty:"+property);
			}
			
			if (null==type){
				throw new IllegalStateException("Inside of non-extant type!!\n"+"Element:"+element+"\nType:"+type);
			}
			
			property.validate();
			type.getProperties().add(property);
			property=null;
		
		} else if (SemanticType.TYPES.contains(element)){
		
			if (null==type){
				throw new IllegalStateException("Exiting non-extent type!\n"+"Element:"+element+"\nType:"+type);
			}
			
			type.validate();
			types.add(type);
			type=null;
		
		} 
	}
	
    /* TODO */
    public Set process(){
        return types;
    }
    
	/** simple outputting routine with indention */
	private void outputStart(String element, Attributes attrs){
		System.out.print(depth+element);
		System.out.print("(");
		for (int i = 0; i < attrs.getLength(); i++) {
			String attr = attrs.getQName(i);
			String value = attrs.getValue(i);
			System.out.print(" "+attr+"=\""+value+"\" ");
		}
		System.out.print("):");
		System.out.println("");
		depth+="  ";
	}
	
	/** reduces indention for output */
	private void outputStop(String element){
		depth=depth.substring(2);
	}
	
	/** converts xml attributes to java.util.Properties */
	private Properties attrs2props(Attributes attrs){
		Properties p = new Properties();
		for (int i = 0; i < attrs.getLength(); i++) {
			String key = attrs.getQName(i);
			String value = attrs.getValue(i);
			p.put(key,value);
		}
		return p;
	}
	
}
