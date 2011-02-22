/*
 * org.openmicroscopy.shoola.agents.dataBrowser.visitor.AnnotatedNodesVisitor 
 *
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
 */
package org.openmicroscopy.shoola.agents.dataBrowser.visitor;


//Java imports
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;
import org.openmicroscopy.shoola.agents.util.EditorUtil;

/** 
 * Founds the nodes either annotated or not annotated depending on the passed
 * parameter.
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
public class AnnotatedNodesVisitor 	
	implements ImageDisplayVisitor
{

	/** 
	 * Set to <code>true</code> to show the annotated nodes,
	 * to <code>false</code> to show the nodes not yet annotated.
	 */
	private boolean 			annotated;
	
	/** 
	 * Collection of annotated or not annotated nodes depending on the
	 * value of the {@link #annotated} flag.
	 */
	private List<ImageDisplay> 	foundNodes;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param annotated Pass <code>true</code> to show the annotated nodes,
	 * 					<code>false</code> to show the nodes not yet annotated.
	 */
	public AnnotatedNodesVisitor(boolean annotated)
	{
		this.annotated = annotated;
		foundNodes = new ArrayList<ImageDisplay>();
	}
	
	/**
	 * Returns the collection of nodes.
	 * 
	 * @return See above.
	 */
	public List<ImageDisplay> getFoundNodes() { return foundNodes; }
	
	/** 
     * Implemented as specified by {@link ImageDisplayVisitor}. 
     * @see ImageDisplayVisitor#visit(ImageNode)
     */
	public void visit(ImageNode node)
	{	 
		if (annotated) {
			if (EditorUtil.isAnnotated(node.getHierarchyObject())) 
				foundNodes.add(node);
		} else {
			if (!EditorUtil.isAnnotated(node.getHierarchyObject())) 
				foundNodes.add(node);
		}
	}

    /** 
     * Implemented as specified by {@link ImageDisplayVisitor}. 
     * @see ImageDisplayVisitor#visit(ImageSet)
     */
	public void visit(ImageSet node) {}
	
}
