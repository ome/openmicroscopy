/*
 * org.openmicroscopy.shoola.agents.editor.util.XMLMethods
 * 
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package org.openmicroscopy.shoola.agents.editor.util;

// Java imports
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//Third-party libraries
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;

/** 
 * A class that contains some useful static XML manipulation methods.
 * 
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class XMLMethods 
{
	
	/**
	 * Static method to convert an XML file into a DOM Document. 
	 * 
	 * @param xmlFile
	 * @return	A DOM Document
	 * @throws SAXException
	 */
	public static Document readXMLtoDOM(File xmlFile) 
		throws ParsingException 
		{
		DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
        //factory.setValidating(true);   
        //factory.setNamespaceAware(true);
        Document document = null;
		
        try {
           DocumentBuilder builder = factory.newDocumentBuilder();

           builder.setErrorHandler(
                   new org.xml.sax.ErrorHandler() {
                       // ignore fatal errors (an exception is guaranteed)
                       public void fatalError(SAXParseException exception)
                       throws SAXException {
                       }

                       // treat validation errors as fatal
                       public void error(SAXParseException e)
                       throws SAXParseException
                       {
                         throw e;
                       }

                       // dump warnings too
                       public void warning(SAXParseException err)
                       throws SAXParseException
                       {
                         
                       }
                   }
                 ); 

           document = builder.parse( xmlFile );
           
        } catch (SAXException sxe) {
            throw new ParsingException(sxe.getMessage(), sxe);

         } catch (ParserConfigurationException pce) {
             // Parser with specified options can't be built
        	// show error and give user a chance to submit error
        	 EditorAgent.getRegistry().getUserNotifier().notifyError(
        			 "Parser Confuguration Exception",
 					"Parser with specified options can't be built.", pce);

         } catch (IOException ioe) {
        	 EditorAgent.getRegistry().getUserNotifier().notifyError(
        			 "I/O error",
  					"File could not be read", ioe);
         } 
         return document;
	}
	
}
