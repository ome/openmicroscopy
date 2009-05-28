 /*
 * org.openmicroscopy.shoola.agents.editor.model.ExperimentalInfo 
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
package org.openmicroscopy.shoola.agents.editor.model;

//Java imports

import java.util.Date;
import java.util.HashMap;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.openmicroscopy.shoola.agents.editor.EditorAgent;

import pojos.ExperimenterData;

//Third-party libraries

//Application-internal dependencies

/** 
 * This object holds details of an experiment instance,
 * E.g. investigator name, date, etc. 
 * The presence of this object in root protocol node/title indicates that the
 * protocol has been performed as an experiment. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ExperimentInfo 
	implements IAttributes {
	
	/**
	 * A map of the attributes that define this experimental info.
	 * Can be used to store any name, value pair. 
	 */
	private HashMap<String, String> valueAttributesMap;
	
	/**  
	 * The name of the element within 'exp-info' that has exp date,
	 * stored as UTC millisecs 
	 */
	
	public static final String 			EXP_DATE = "experiment-date";
	/**  
	 * The name of the element within 'exp-info' that has 
	 * investigator's name 
	 */
	public static final String 			INVESTIG_NAME = "investigator-name";

	/**
	 * Boolean attribute to allow editing of the Protocol part of the 
	 * Experiment. If this is not set to 'true', Protocol will not be editable. 
	 */
	public static final String 			EDIT_PROTOCOL = "editProtocol";
	
	/**
	 * Creates an instance. 
	 */
	public ExperimentInfo(String name, String utcDate) 
	{
		if (name == null) {
			if (EditorAgent.isServerAvailable()) {
				ExperimenterData ed = EditorAgent.getUserDetails();
				name = ed.getFirstName() + " " + ed.getLastName();
			} else {
				name = System.getProperty("user.name");
			}
		}
		if (utcDate == null) {
			Date now = new Date();
			utcDate = now.getTime() + "";
		}
		valueAttributesMap = new HashMap<String, String>();
		
		setAttribute(INVESTIG_NAME, name);
		setAttribute(EXP_DATE, utcDate);
	}
	
	/**
	 * Creates an instance without specifying name and date. These will
	 * be filled by default values (username and "now"). 
	 */
	public ExperimentInfo() 
	{
		this (null, null);
	}
	
	/**
	 * Method for classes to determine whether a TreeModel of a protocol
	 * contains an Experimental Info object at the root node, thereby 
	 * defining it as an Experiment. 
	 * 
	 * @param model		The data model
	 * @return			True if the root node has an Experiment Info object. 
	 */
	public static boolean isModelExperiment(TreeModel model)
	{
		IAttributes expInfo = getExpInfo(model);
		return (expInfo != null);
	}
	
	/** 
	 * Gets the Experimental info for this tree-model. 
	 * @param model
	 * @return
	 */
	public static IAttributes getExpInfo(TreeModel model)
	{
		if (model == null) return null;
		TreeNode tn = (TreeNode)model.getRoot();
		if (!(tn instanceof DefaultMutableTreeNode)) return null;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tn;
		Object userOb = node.getUserObject();
		if (!(userOb instanceof ProtocolRootField)) return null;
		ProtocolRootField prf = (ProtocolRootField)userOb;
		return prf.getExpInfo();
	}
	
	/**
	 * Implemented as specified by the {@link IAttributes} interface. 
	 * Gets an attribute of this experimental info.
	 * 
	 * @param name		Name of the attribute. 
	 * @return			The value of this attribute, or null if not set. 
	 */
	public String getAttribute(String name) 
	{
		return valueAttributesMap.get(name);
	}
	
	/**
	 * Implemented as specified by the {@link IAttributes} interface. 
	 * 
	 * @see IAttributes#setAttribute(String, String)
	 */
	public void setAttribute(String name, String value) 
	{
		valueAttributesMap.put(name, value);
	}

	/**
	 * Implemented as specified by the {@link IAttributes} interface. 
	 * 
	 * @see IAttributes#isAttributeTrue(String)
	 */
	public boolean isAttributeTrue(String attributeName) {
		return "true".equals(valueAttributesMap.get(attributeName));
	}
}
