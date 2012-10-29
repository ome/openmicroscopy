/*
 * org.openmicroscopy.shoola.agents.dataBrowser.layout.PlateLayout 
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.CellDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellSampleNode;
import pojos.WellSampleData;


/** 
 * Lays out the plate.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class PlateLayout 	
	implements Layout
{
	
    //NOTE: The algorithm for this layout *relies* on the fact that
    //visualization trees are visited in a depth-first fashion.
    //When we'll implement iterators to visit a tree, then this class
    //will ask for a depth-first iterator.
    
    /** Textual description of this layout. */
    static final String DESCRIPTION = "Layout the plate.";
    
    /** Collection of nodes previously layed out. */
    private Set					oldNodes;
    
    /**
     * Lays out the wells.
     * @see Layout#doLayout()
     */
    public void doLayout() {}
    
    /**
     * Retrieves the images.
     * @see Layout#visit(ImageNode)
     */
    public void visit(ImageNode node) {}

    /**
     * Retrieves the root node.
     * @see Layout#visit(ImageSet)
     */
    public void visit(ImageSet node)
    {
    	if (node.getParentDisplay() != null) return;
		if (oldNodes == null || oldNodes.size() == 0) {
        	
    		Set nodes = node.getChildrenDisplay();
    		Iterator i = nodes.iterator();
    		ImageDisplay n;
    		List<ImageDisplay> l = new ArrayList<ImageDisplay>();
    		List<ImageDisplay> col = new ArrayList<ImageDisplay>();
    		List<ImageDisplay> row = new ArrayList<ImageDisplay>();
    		CellDisplay cell;
    		while (i.hasNext()) {
				n = (ImageDisplay) i.next();
				if (n instanceof CellDisplay) {
					cell = (CellDisplay) n;
					if (cell.getType() == CellDisplay.TYPE_HORIZONTAL)
						col.add(cell);
					else row.add(cell);
				} else {
					l.add(n);
				}
			}
    		Dimension maxDim = new Dimension(0, 0);
            Iterator children = l.iterator();
            ImageDisplay child;
           // ImageData img;
            WellSampleData wsd;
            while (children.hasNext()) {
            	child = (ImageDisplay) children.next();
                wsd = (WellSampleData) child.getHierarchyObject();
                if (wsd.getId() >= 0)
                	maxDim = LayoutUtils.max(maxDim, child.getPreferredSize());
            }
    		 //First need to set width and height
    		Dimension d;
    		int height = 0;
    		int width = 0;
    		/*
    		 * remove comments if using cell nodes
    		if (col.size() > 0){
    			d = col.get(0).getPreferredSize();
    			height = d.height;
    		}
    		if (row.size() > 0) {
    			d = row.get(0).getPreferredSize();
    			width = d.width+15;
    		}
    		
    		i = col.iterator();
    		while (i.hasNext()) {
    			cell = (CellDisplay) i.next();
    			d = cell.getPreferredSize();
    			cell.setBounds(width+cell.getIndex()*maxDim.width, 0,
    					maxDim.width, d.height);
    		}
    		i = row.iterator();
    		while (i.hasNext()) {
    			cell = (CellDisplay) i.next();
    			d = cell.getPreferredSize();
    			cell.setBounds(0, height+cell.getIndex()*maxDim.height, 
    					width, maxDim.height);
    		}
    		*/
    		i = l.iterator();
    		WellSampleNode wsNode;
    		int r, c;
    		int h = 0;
    		int hh;
    		while (i.hasNext()) {
    			wsNode = (WellSampleNode) i.next();
    			h = 0;
    			hh = 0;
    			r = wsNode.getLayedoutRow();
    			c = wsNode.getLayedoutColumn();
    			d = wsNode.getPreferredSize();
    			//if (r > 0) h = wsNode.getTitleHeight();
    			if (r > 0) h = wsNode.getTitleHeight();
    			if (wsNode.getTitleBarType() == ImageNode.NO_BAR) {
    				h = wsNode.getTitleHeight();
    				hh = h;
    			}
    				
    			wsNode.setBounds(width+c*maxDim.width,
    					height+r*(maxDim.height+h) + hh, d.width, d.height);
			}
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
    public int getIndex() { return LayoutFactory.PLATE_LAYOUT; }

    /**
     * Implemented as specified by the {@link Layout} interface.
     * @see Layout#setOldNodes(Set)
     */
	public void setOldNodes(Set oldNodes) { this.oldNodes = oldNodes; }

    /**
     * Implemented as specified by the {@link Layout} interface.
     * @see Layout#setImagesPerRow(int)
     */
	public void setImagesPerRow(int number) {}
	
	 /**
     * Implemented as specified by the {@link Layout} interface.
     * @see Layout#getImagesPerRow()
     */
	public int getImagesPerRow() { return 0; }
	
}
