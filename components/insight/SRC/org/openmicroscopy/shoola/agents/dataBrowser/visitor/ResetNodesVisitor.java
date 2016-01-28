/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JComponent;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;
import omero.gateway.model.DataObject;


/** 
 * Adds the visible nodes to the desktop.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class ResetNodesVisitor
	implements ImageDisplayVisitor
{

	/** The collection of nodes to reset. */
	private Collection<ImageDisplay> 	nodes;
	
	/** The collection of data object hosted by the visible nodes. */
	private Collection<DataObject>		visibleImages;
	
	/** 
	 * Flag indicating if the visitor is used to reset the nodes or
	 * to collect the nodes displayed.
	 */
	private boolean						reset;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param nodes The collection of <code>DataObject</code>s to find.
	 * @param reset Pass <code>true</code> to reset the nodes, 
	 * 				<code>false</code> to count the visible nodes.
	 */
	public ResetNodesVisitor(Collection<ImageDisplay> nodes, boolean reset)
	{
		if (nodes == null && reset)
			throw new IllegalArgumentException("No nodes to find.");
		this.nodes = nodes;
		visibleImages = new ArrayList<DataObject>();
		this.reset = reset;
	}
	
	/**
	 * Returns the collection of data object hosted by the visible nodes.
	 * 
	 * @return See above.
	 */
	public Collection<DataObject> getVisibleImages() { return visibleImages; }
	
    /** 
     * Implemented as specified by {@link ImageDisplayVisitor}. 
     * @see ImageDisplayVisitor#visit(ImageNode)
     */
	public void visit(ImageNode node) {}

    /** 
     * Implemented as specified by {@link ImageDisplayVisitor}. 
     * @see ImageDisplayVisitor#visit(ImageSet)
     */
	public void visit(ImageSet node)
	{ 
		if (node.containsImages()) {
			JComponent desktop = node.getInternalDesktop();
			ImageDisplay child;
			if (reset) {
				desktop.removeAll();
				Iterator<ImageDisplay> i = nodes.iterator();
				ImageDisplay parent;
				DataObject ho;
		        while (i.hasNext()) {
		        	child = i.next();
		        	parent = child.getParentDisplay();
		        	if (parent == null || node == parent) {
		        		ho = (DataObject) child.getHierarchyObject();
		        		//To be on the save side, shouldn't happen
		        		if (!visibleImages.contains(ho)) {
		        			visibleImages.add(ho);
		        			desktop.add(child);
		        		}
		        	}
		        }
			} else { //count visible nodes
				Component[] comps = desktop.getComponents();
				Component comp;
				for (int i = 0; i < comps.length; i++) {
					comp = comps[i];
					if (comp instanceof ImageDisplay) {
						child = (ImageDisplay) comp;
						visibleImages.add(
	        					(DataObject) child.getHierarchyObject());
					}
				}
			}
		}
	}
	
}
