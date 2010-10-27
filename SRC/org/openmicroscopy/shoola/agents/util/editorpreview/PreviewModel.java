 /*
 * org.openmicroscopy.shoola.agents.util.editorpreview.PreviewModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.editorpreview;

//Java imports
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

//Third-party libraries
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

//Application-internal dependencies

/** 
 * This model parses the XML that is used to summarise an OMERO.editor file.
 * The XML string is stored as the file Annotation description for all 
 * Editor files on the server.  
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class PreviewModel 
{
	
	/** The title of the protocol */
	private String 				protocolName;
	
	/** The protocol description or Abstract */
	private String 				protocolDescription;
	
	/** A list of the steps in the protocol */
	private List<StepObject> 	protocolSteps;
	
	/**
	 * Parses the XML string and retrieve the summary data. 
	 * 
	 * @param xmlDescription The string to parse.
	 */
	private void parseXmlDescription(String xmlDescription)
	{
		IXMLElement root = null;
		try {
			IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
	
			ByteArrayInputStream bs = new ByteArrayInputStream(
												xmlDescription.getBytes());
			IXMLReader reader = new StdXMLReader(bs);
	
			parser.setReader(reader);
			
			root = (IXMLElement) parser.parse();
			
			bs.close();
		} catch (Throwable ex) {
			return;
		} 
		
		// name and abstract
		protocolName =  getChildContent(root, PreviewPanel.NAME);
		protocolDescription =  getChildContent(root, PreviewPanel.DESCRIPTION);
		
		// list of steps
		IXMLElement ss = root.getFirstChildNamed(PreviewPanel.STEPS);
		if (ss == null)		return;		// no steps. 
		List<IXMLElement> steps = ss.getChildrenNamed(PreviewPanel.STEP);
		
		String name;
		int level;
		StepObject so;
		for (IXMLElement step : steps) {
			// get the name of the step, default is "Step"
			name = step.getAttribute(PreviewPanel.NAME, "Step"); 
			level = step.getAttribute(PreviewPanel.LEVEL, 0);
			so = new StepObject(name, level); // make a step object
			
			// parameters within each step 
			List<IXMLElement> params = step.getChildrenNamed(
					PreviewPanel.PARAMETER);
			String key, value;
			for (IXMLElement element : params) {
				key = getChildContent(element, PreviewPanel.NAME);
				value = getChildContent(element, PreviewPanel.VALUE);
				so.addParam(key, value);
			}
			protocolSteps.add(so);
		}
	}

	/**
	 * A handy method for getting the content of a child XML element. 
	 * 		
	 * @param parent	The parent element
	 * @param childName	The name of the child you want the text content of. 
	 * @return See above.
	 */
	private static String getChildContent(IXMLElement parent, String childName) 
	{
		if (parent == null) return null;
		IXMLElement child = parent.getFirstChildNamed(childName);
		if (child == null) return null;
		return child.getContent();
	}

	/**
	 * Creates an instance and parses the XML summary.
	 * 
	 * @param summary The XML that summarizes an Editor file. 
	 */
	PreviewModel(String summary)
	{
		protocolSteps = new ArrayList<StepObject>();
		if (summary != null)
			parseXmlDescription(summary);
	}

	/**
	 * Returns the protocol Title. 
	 * 
	 * @return See above.
	 */
	String getTitle() { return protocolName; }
	
	/**
	 * Returns the description or Abstract of the protocol. 
	 * 
	 * @return See above.
	 */
	String getDescription()	{ return protocolDescription; }
	
	/**
	 * Returns the list of steps that define the protocol. 
	 * 
	 * @return See above.
	 */
	List<StepObject> getSteps()	{ return protocolSteps; }
	
}
