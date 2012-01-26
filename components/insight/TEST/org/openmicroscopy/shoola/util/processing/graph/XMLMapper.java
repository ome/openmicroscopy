

/*
 * .XMLMapper
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
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
 *----------------------------------------------------------------------------*/
package org.openmicroscopy.shoola.util.processing.graph;

//Java imports
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;


//Third-party libraries
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.XMLElement;

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class XMLMapper
{
	/** Root element of the XML document. */
	XMLElement root;
	
	/** The graph nodes from the root XML document. */
	GraphNode graphRoot;
	
	/** Map of the XMLElement to the GraphNode.	 */
	Map<XMLElement, GraphNode> elementGraphMap;
	
	/**
	 * The file to parse.
	 * @param file See above.
	 */
	public XMLMapper(XMLElement root)
	{
		
		 this.root = root;
		 elementGraphMap = new HashMap<XMLElement, GraphNode>();
	}
	
	/**
	 * Parse the document.
	 */
	public void parseDocument()
	{
		LinkedList<XMLElement> queue = new LinkedList<XMLElement>();
		queue.addLast(root);
		while(queue.size()!=0)
		{
			XMLElement elementNode = queue.removeFirst();
			addNode(elementNode);
			/*
			Vector v = elementNode.getChildren();
			for(int i = 0 ; i < v.size() ; i++)
				queue.addLast((XMLElement) v.get(i));
				*/
		}
	}
	
	/**
	 * Add the node to the graph model.
	 * @param node See above.
	 */
	public void addNode(XMLElement xmlNode)
	{
		if(graphRoot==null)
		{
			GraphNode node = new GraphNode(new XMLNode(xmlNode));
			elementGraphMap.put(xmlNode, node);
			graphRoot = node;
		}
		else
		{
			GraphNode node = new GraphNode(new XMLNode(xmlNode));
			elementGraphMap.put(xmlNode, node);
			IXMLElement parentNode = xmlNode.getParent();
			GraphNode parentGraphNode = elementGraphMap.get(parentNode);
			parentGraphNode.addChild(node);
			node.setParent(parentGraphNode);
		}
	}
	
	/**
	 * Get the root node of the mapped GraphNode.
	 * @return See above.
	 */
	public GraphNode getRoot()
	{
		return graphRoot;
	}
	
}
