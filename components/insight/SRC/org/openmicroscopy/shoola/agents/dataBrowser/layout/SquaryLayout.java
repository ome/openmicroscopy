/*
 * org.openmicroscopy.shoola.agents.dataBrowser.layout.SquaryLayout 
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
package org.openmicroscopy.shoola.agents.dataBrowser.layout;


//Java imports
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import pojos.DataObject;
import pojos.ImageData;

/** 
 * Recursively lays out all nodes in a container display in a square grid.
 * The size of each cell in the grid is that of the largest child in the
 * container. 
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
    private static int      browserWidth;
    
    /** Collection of nodes previously layed out. */
    private Set				oldNodes;
    
    /** The number of items per row. */
    private int				itemsPerRow;
    
    /** Maximum width of the BrowserView.*/
    private void setBrowserSize()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        browserWidth = 7*(screenSize.width/10);
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

        Object[] children = sorter.sortAsArray(node.getChildrenDisplay());
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
            child.setCollapsed(false);
            /*
            if (x+d.width <= browserWidth) {
                x += d.width;
                maxY = Math.max(maxY, d.height); 
            } else {
                x = 0;
                if (maxY == 0) y += d.height; 
                else y += maxY;
                maxY = 0;
            } 
            */
            x = 0;
            if (maxY == 0) y += d.height; 
            else y += maxY;
            maxY = 0;
        }
       
        Rectangle bounds = node.getContentsBounds();
        d = bounds.getSize();
        node.getInternalDesktop().setSize(d);
        node.getInternalDesktop().setPreferredSize(d);
        node.setCollapsed(false);
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
    	ImageDisplay child;
    	Iterator i;
    	if (!(object instanceof DataObject)) {
    		//root
    		boolean image = false;
    		if (oldNodes != null) {
    			i = oldNodes.iterator();
    			while (i.hasNext()) {
					ImageDisplay element = (ImageDisplay) i.next();
					if (element.getHierarchyObject() instanceof ImageData)
						image = true;
					break;
				}
    		}
    		if (image) return oldNodes;
    		return n.getChildrenDisplay();
    	}
    		
    	DataObject ho = (DataObject) object;
    	Class klass = ho.getClass();
    	long id = ho.getId();
    	i = oldNodes.iterator();
    	
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
     * Returns the node already layed out corresponding to the specified
     * node or <code>null</code> if no node found.
     * 
     * @param n	The node to handle.
     * @return See above.
     */
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
     * @param sorter 		A {@link ViewerSorter sorter} to order nodes.
     * @param itemsPerRow 	The number of items per row.
     */
    SquaryLayout(ViewerSorter sorter, int itemsPerRow)
    {
        setBrowserSize();
        this.sorter = sorter;
        this.itemsPerRow = itemsPerRow;
    }

    /**
     * Returns the width of the browser.
     * 
     * @return See above.
     */
    static int getBrowserWidth() { return browserWidth; }
    
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
        		LayoutUtils.doSquareGridLayout(node, sorter, itemsPerRow);
        	else visitContainerNode(node);
        } else {
        	/*
        	Set o = getOldChildren(node);
        	Set n = node.getChildrenDisplay();
        	if (o != null && n != null && o.size() >= n.size())
        		LayoutUtils.redoLayout(node, (ImageSet) getOldNode(node), n, o);
        	else {
        		node.restoreDisplay();
                if (node.isSingleViewMode()) return;
                if (node.getChildrenDisplay().size() == 0) {//node with no child
                    LayoutUtils.noChildLayout(node);
                    return;
                }
            	if (node.containsImages()) 
            		LayoutUtils.doSquareGridLayout(node, sorter, itemsPerRow);
            	else visitContainerNode(node);
        	}
        		*/
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
	
    /**
     * Implemented as specified by the {@link Layout} interface.
     * @see Layout#setImagesPerRow(int)
     */
	public void setImagesPerRow(int number) { itemsPerRow = number; }
	
    /**
     * Implemented as specified by the {@link Layout} interface.
     * @see Layout#getImagesPerRow()
     */
	public int getImagesPerRow() { return itemsPerRow; }
	
}
