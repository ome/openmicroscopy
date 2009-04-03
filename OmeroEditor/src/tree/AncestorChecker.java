
/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

package tree;

import java.util.List;

/**
 * This class has methods for iterating through a list of nodes and their ancestors, and checking
 * if an attribute is "true" or if an attribute exists (is not null). 
 * Eg. Used to check whether a field or any of it's ancestors are "locked". 
 * 
 * @author will
 *
 */
public class AncestorChecker {
	
	
	/**
	 * This method checks to see whether an attribute is "true", for all the nodes
	 * in childNodes and their ancestors. 
	 * If the attribute is true for any of these dataFields, this
	 * method will return true. 
	 * 
	 * @param attribute		The name of the attribute to check
	 * @param childNodes 	A list of the child nodes to check (don't need to be siblings: ancestors checked for each)
	 * @return		true if any nodes/dataFields have a value for the named attribute
	 */
	public static boolean isAttributeTrue(String attribute, List<DataFieldNode> childNodes) {
		
		for (DataFieldNode node : childNodes) {
			if (isAttributeTrue(attribute, node)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isAttributeTrue(String attribute, DataFieldNode node) {
		
		/*
		 * If attribute is true for this field, return true
		 */
		if (node.getDataField().isAttributeTrue(attribute)) {
			return true;
		}
		
		/*
		 * Check all ancestors, if attribute is true for any, return true
		 */
		DataFieldNode currentNode = node;
		while (currentNode.getParentNode() != null) {
			// move to parent
			currentNode = currentNode.getParentNode();
			// check if attribute is true
			if (currentNode.getDataField().isAttributeTrue(attribute)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * This method checks to see whether an attribute exists, for all the nodes
	 * in childNodes. 
	 * If the attribute exists (is not null) for any of these dataFields, this
	 * method will return true. 
	 * 
	 * @param attribute		The name of the attribute to check
	 * @param childNodes 	A list of the nodes to check.
	 * @return		true if any nodes/dataFields have a value for the named attribute
	 */
	public static boolean isAttributeNotNull(String attribute, List<DataFieldNode> childNodes) {
		
		for (DataFieldNode node : childNodes) {
			if (isAttributeNotNull(attribute, node)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This method checks to see whether an attribute exists, for all the node.
	 * If the attribute exists (is not null) for any of this field, this
	 * method will return true. 
	 * 
	 * @param attribute		The name of the attribute to check
	 * @param node 	The node to check.
	 * @return		true if the node has a value for the named attribute
	 */
	public static boolean isAttributeNotNull(String attribute, DataFieldNode node) {
		
		/*
		 * If attribute is true for this field, return true
		 */
		if (node.getDataField().getAttribute(attribute) != null) {
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * This method checks to see whether an attribute exists, for all the ancestors of the nodes
	 * in childNodes (but NOT the nodes themselves). 
	 * If the attribute exists (is not null) for any of these ancestor nodes/fields, this
	 * method will return true. 
	 * 
	 * @param attribute		The name of the attribute to check
	 * @param childNodes 	A list of the nodes, whose ancestors must be checked.
	 * @return		true if any ancestor nodes/dataFields have a value for the named attribute
	 */
	public static boolean isAncestorAttributeNotNull(String attribute, List<DataFieldNode> childNodes) {
		for (DataFieldNode node : childNodes) {
			if (isAncestorAttributeNotNull(attribute, node)) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * This method checks to see whether an attribute exists, for all the ancestors of the node.
	 * (but NOT the node itself). 
	 * If the attribute exists (is not null) for any of these ancestor nodes/fields, this
	 * method will return true. 
	 * 
	 * @param attribute		The name of the attribute to check
	 * @param node 		The node whose ancestors must be checked.
	 * @return		true if any ancestor nodes/dataFields have a value for the named attribute
	 */
	public static boolean isAncestorAttributeNotNull(String attribute, DataFieldNode node) {
		
		/*
		 * Check all ancestors, if attribute is true for any, return true
		 */
		DataFieldNode currentNode = node;
		while (currentNode.getParentNode() != null) {
			// move to parent
			currentNode = currentNode.getParentNode();
			// check if attribute is true
			if (currentNode.getDataField().getAttribute(attribute) != null) {
				return true;
			}
		}
		return false;
	}
}
