/*
 * org.openmicroscopy.shoola.agents.chainbuilder.data.layout.ChainStructureErrors
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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




/*------------------------------------------------------------------------------
 *
 * Written by:    Harry Hochheiser <hsh@nih.gov>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.chainbuilder.data;
 
//Java imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.AnalysisChainData;
import org.openmicroscopy.shoola.env.ui.UIFactory;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

 

/** 
 * <p>A collection of {@link ChainStructureError}s.
 * 
 * @author Harry Hochheiser
 * @version 2.1
 * @since OME2.1
 */

public class ChainStructureErrors {
	
	private Vector errors;
	private AnalysisChainData chain;
	
	public ChainStructureErrors(AnalysisChainData chain) {
		this.chain = chain;
	}
	
	public void addError(ChainStructureError error) {
		if (errors == null)
			errors = new Vector();
		errors.add(error);
	}
	
	public void addErrors(Vector newErrors) {
		if (errors == null)
			errors = new Vector();
		errors.addAll(newErrors);
	}
	
	public void display() {
		// for now
		if (errors == null) 
			return;
		Iterator iter = errors.iterator();
		String res = new String();
		
		HashMap nodes = new HashMap();
		DefaultMutableTreeNode classNode;
		DefaultMutableTreeNode objNode;
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		
		while (iter.hasNext()) {
			ChainStructureError error = (ChainStructureError) iter.next();
			//			 get the node for its class from the hash
			// get it's error as a tree node
			Object obj = nodes.get(error.getClass());
			if (obj == null) {
				classNode = new DefaultMutableTreeNode(error.getDescription());
				root.add(classNode);
				nodes.put(error.getClass(),classNode);
			}
			else
				classNode = (DefaultMutableTreeNode) obj;
			// add it.
			objNode = new DefaultMutableTreeNode(error.describeError());
			classNode.add(objNode);
		}
		UserNotifier un = UIFactory.makeUserNotifier();
		
		String msg = "Chain: "+chain.getName()+
			" has structural errors that may prevent it ";
		msg += "from being executed.\n";
		JTree tree = new JTree(root);
		tree.setRootVisible(false);
		un.notifyWarning("Improper Chain Structure",msg,tree);
	}
}
	
	