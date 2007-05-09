/*
 * org.openmicroscopy.shoola.agents.hiviewer.layout.SquaryLayout
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
 *------------------------------------------------------------------------------s
 */

package org.openmicroscopy.shoola.agents.hiviewer.layout;


//Java imports
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;

import pojos.DataObject;

/** 
 * Recursively lays out all nodes in a container display in a square grid.
 * The size of each cell in the grid is that of the largest child in the
 * container. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class SquaryLayout
    implements Layout
{

    //NOTE: The algorithm for this layout *relies* on the fact that
    //visualization trees are visited in a depth-first fashion.
    //When we'll implement iterators to visit a tree, then this class
    //will ask for a depth-first iterator.
    
    /** Textual description of this layout. */
    static final String DESCRIPTION = "Recursively lays out all nodes in a "+
                                      "container display in a square grid. "+
                                      "The size of each cell in the grid "+
                                      "is that of the largest child in the "+
                                      "container.";
    
    /** 
     * A {@link ViewerSorter sorter} to order nodes in ascending 
     * alphabetical order.
     */
    private ViewerSorter    sorter;
    
    /** Maximum width used to displayed the thumbnail. */
    private int             browserW;
    
    /** Collection of nodes previously layed out. */
    private Set				oldNodes;
    
    /** Maximum width of the BrowserView.*/
    private void setBrowserSize()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        browserW = 8*(screenSize.width/10);
    }
    
    /**
     * Visits an {@link ImageSet} node that contains {@link ImageSet} nodes. 
     * 
     * @param node The parent {@link ImageSet} node.
     */
    private void visitContainerNode(ImageSet node)
    {
        //Then figure out the number of columns, which is the same as the
        //number of rows.    
        if (node.getChildrenDisplay().size() == 0) {   //node with no child
            LayoutUtils.noChildLayout(node);
            return;
        }

        Object[] children = sorter.sortArray(node.getChildrenDisplay());
        //Finally do layout.
        //ImageDisplay[] children = 
        //    LayoutUtils.sortChildrenByPrefWidth(node, false);
        Dimension d;
        int maxY = 0;
        int x = 0, y = 0;
        ImageDisplay child;
        for (int i = 0; i < children.length; i++) {
        	child = (ImageDisplay) children[i];
            d = child.getPreferredSize();
            child.setBounds(x, y, d.width, d.height);
            //children[i].setVisible(true);
            child.setCollapsed(false);
            if (x+d.width <= browserW) {
                x += d.width;
                maxY = Math.max(maxY, d.height); 
            } else {
                x = 0;
                if (maxY == 0) y += d.height; 
                else y += maxY;
                maxY = 0;
            } 
        }
       
        Rectangle bounds = node.getContentsBounds();
        d = bounds.getSize();
        node.getInternalDesktop().setSize(d);
        node.getInternalDesktop().setPreferredSize(d);
        //node.validate();
        //node.repaint();
        node.setCollapsed(false);
        //node.setVisible(true);
    }

    /**
     * Returns the collection of children already layout form the passed node.
     * 
     * @param n The node to handle.
     * @return See above.
     */
    private Set getOldChildren(ImageSet n)
    {
    	Object object = n.getHierarchyObject();
    	if (!(object instanceof DataObject)) //root
    		return n.getChildrenDisplay();
    	DataObject ho = (DataObject) object;
    	Class klass = ho.getClass();
    	long id = ho.getId();
    	Iterator i = oldNodes.iterator();
    	ImageDisplay child;
    	Object oho;
    	while (i.hasNext()) {
    		child = (ImageDisplay) i.next();
			oho = child.getHierarchyObject();
			if (oho instanceof DataObject) {
				if (((DataObject) oho).getId() == id && 
						klass.equals(oho.getClass()))
						return child.getChildrenDisplay();
			}
		}
    	return new HashSet(0);
    }
    
    /**
     * Visits an {@link ImageSet} node that contains {@link ImageSet} nodes. 
     * 
     * @param node	The parent {@link ImageSet} node.
     */
    private void setContainerNodeBounds(ImageSet node, Set oldChildren)
    {
        //Then figure out the number of columns, which is the same as the
        //number of rows.    
        if (node.getChildrenDisplay().size() == 0) {   //node with no child
            LayoutUtils.noChildLayout(node);
            return;
        }

        Iterator k = oldNodes.iterator();
        ImageDisplay oldNode = null;
        long id = -1;
        Object ho = node.getHierarchyObject();
        if (ho instanceof DataObject) id = ((DataObject) ho).getId();
        Class hoClass = ho.getClass();
        Object oho;
        while (k.hasNext()) {
        	oldNode = (ImageDisplay) k.next();
			oho = oldNode.getHierarchyObject();
			if (oho instanceof DataObject) {
				if (((DataObject) oho).getId() == id &&
						oho.getClass().equals(hoClass)) break;
			} else { //root
				break;
			}
		}
        if (oldNode == null) {
        	visitContainerNode(node);
        	return;
        }
        //System.err.println("oldNode: "+oldNode);
        Set children = node.getChildrenDisplay();
        //Set oldChildren = oldNode.getChildrenDisplay();
        Iterator i = children.iterator();
        Iterator j;
        ImageDisplay n, oldChild;
        while (i.hasNext()) {
			n = (ImageDisplay) i.next();
			j = oldChildren.iterator();
			ho = n.getHierarchyObject();
			if (ho instanceof DataObject) {
				id = ((DataObject) ho).getId();
				while (j.hasNext()) {
					oldChild = (ImageDisplay) j.next();
					oho = oldChild.getHierarchyObject();
					if (oho instanceof DataObject) {
						if (((DataObject) oho).getId() == id) {
							n.setBounds(oldChild.getBounds());
						}
					}
				}
			}			
		}
        Rectangle bounds = oldNode.getContentsBounds();
        Dimension d = oldNode.getSize();
        node.setBounds(bounds);
        node.getInternalDesktop().setSize(d);
        node.getInternalDesktop().setPreferredSize(d);
    }
    
    private ImageDisplay getOldNode(ImageDisplay n)
    {
    	if (n.getParentDisplay() == null) {
    		Iterator i = oldNodes.iterator();
        	ImageDisplay child;
        	while (i.hasNext()) {
        		child = (ImageDisplay) i.next();
        		if (child.getParentDisplay() == null) return child;
    		}
    	}
    	Object object = n.getHierarchyObject();
    	
    	DataObject ho = (DataObject) object;
    	Class klass = object.getClass();
    	long id = ho.getId();
    	Iterator i = oldNodes.iterator();
    	ImageDisplay child;
    	Object oho;
    	while (i.hasNext()) {
    		child = (ImageDisplay) i.next();
			oho = child.getHierarchyObject();
			if (oho instanceof DataObject) {
				if (((DataObject) oho).getId() == id && 
						klass.equals(oho.getClass()))
						return child;
			}
		}
    	return null;
    }
    
    /**
     * Package constructor so that objects can only be created by the
     * {@link LayoutFactory}.
     * 
     * @param sorter A {@link ViewerSorter sorter} to order nodes.
     */
    SquaryLayout(ViewerSorter sorter)
    {
        setBrowserSize();
        this.sorter = sorter;
    }

    /**
     * Lays out the current container display.
     * @see Layout#visit(ImageSet)
     */
    public void visit(ImageSet node)
    {
        if (oldNodes == null || oldNodes.size() == 0) {
        	node.restoreDisplay();
            if (node.isSingleViewMode()) return;
            if (node.getChildrenDisplay().size() == 0) {   //node with no child
                LayoutUtils.noChildLayout(node);
                return;
            }
        	if (node.containsImages()) 
        		LayoutUtils.doSquareGridLayout(node, sorter);
        	else visitContainerNode(node);
        } else {
        	Set o = getOldChildren(node);
        	Set n = node.getChildrenDisplay();
        	//if (node.containsImages()) {
        	if (o == null || n == null || o.size() != n.size()) {
        		node.restoreDisplay();
                if (node.isSingleViewMode()) return;
                if (node.getChildrenDisplay().size() == 0) {//node with no child
                    LayoutUtils.noChildLayout(node);
                    return;
                }
            	if (node.containsImages()) 
            		LayoutUtils.doSquareGridLayout(node, sorter);
            	else visitContainerNode(node);
        	} else
        		LayoutUtils.redoLayout(node, getOldNode(node), n, o);
        	//} else {
        	//	setContainerNodeBounds(node, getOldChildren(node));
        	//}
        }
    }
    
    /**
     * Implemented as specified by the {@link Layout} interface.
     * @see Layout#getDescription()
     */
    public String getDescription() { return DESCRIPTION; }

    /**
     * Implemented as specified by the {@link Layout} interface.
     * @see Layout#getIndex()
     */
    public int getIndex() { return LayoutFactory.SQUARY_LAYOUT; }
    
    /**
     * No-op implementation, as we only layout container displays.
     * @see Layout#visit(ImageNode)
     */
    public void visit(ImageNode node) {}
    
    /**
     * No-op implementation in our case.
     * @see Layout#doLayout()
     */
    public void doLayout() {}

    /**
     * Implemented as specified by the {@link Layout} interface.
     * @see Layout#setOldNodes(Set)
     */
	public void setOldNodes(Set oldNodes) { this.oldNodes = oldNodes; }

}
