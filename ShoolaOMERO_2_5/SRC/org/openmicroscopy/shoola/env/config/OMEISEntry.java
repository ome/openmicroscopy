/*
 * org.openmicroscopy.shoola.env.config.OMEISEntry
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
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

// Third-party libraries
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Application-internal dependencies

/**
 * Hanldes a <i>structuredEntry</i> of type <i>OMEIS</i>.
 * The content of the entry is stored in a {@link OMEISInfo} object, which is
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
class OMEISEntry
    extends Entry
{
    
	/** 
	 * The name of the tag, within this <i>structuredEntry</i>, that specifies
	 * the <i>URL</i> to connect to <i>OMEIS</i>.
	 */
	private static final String		URL_TAG = "url";
    
    
	/** Holds the contents of the entry. */
	private OMEISInfo value;
    
    
	/** Creates a new instance. */
	OMEISEntry() {}
	
	/** 
	 * Returns a {@link OMEISInfo} object, which contains the <i>OMEIS</i>
	 * configuration information.
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
			value = new OMEISInfo();
			value.setServerAddress((String) tags.get(URL_TAG));
		} catch (DOMException dex) { 
			rethrow("Can't parse OMEIS entry.", dex);
		} catch (MalformedURLException mue) {
			rethrow("Malformed URL for OMEIS entry.", mue);
		}
	}
    
	/**
	 * Extracts the values of the tags within the entry tag and puts them in
	 * a map keyed by tag names.
	 *  
	 * @param entry	The <i>OMEIS</i> entry tag.
	 * @return A map whose keys are names of the tags within the entry and
	 * 			values are the corresponding tag contents.
	 * @throws DOMException If the entry contents couldn't be retrieved.
	 * @throws ConfigException If the entry is not structured as expected.
	 */
	private Map extractValues(Node entry)
		throws DOMException, ConfigException
	{
		if (entry.hasChildNodes()) {
			Map tags = new HashMap();
			NodeList children = entry.getChildNodes();
			int n = children.getLength();
			Node child;
			while (0 < n) {
				child = children.item(--n);
				if (child.getNodeType() == Node.ELEMENT_NODE && 
					URL_TAG.equals(child.getNodeName())) {
						tags.put(URL_TAG, child.getFirstChild().getNodeValue());
						return tags;
					}
			}
		}
		throw new ConfigException(
			"The content of the OMEDS structured entry is not valid.");
	}
	//TODO: remove the checks (which are not complete anyway) and the
	//ConfigException when we have an XML schema for config files. 
    
}
