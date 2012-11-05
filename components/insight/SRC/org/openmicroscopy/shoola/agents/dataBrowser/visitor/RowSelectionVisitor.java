/*
 * org.openmicroscopy.shoola.agents.dataBrowser.visitor.SelectionVisitor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
import java.awt.Rectangle;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.Colors;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;


/**
 * Highlights and selects the nodes, working row-wise from first to last
 * selected node. This should handle images with different sizes, but relies
 * on them all being aligned to the top left of the grid.
 *
 * @author Simon Li
 * <a href="mailto:spli@dundee.ac.uk">spli@dundee.ac.uk</a>
 * @since 4.4
 */
public class RowSelectionVisitor
    implements ImageDisplayVisitor
{

    /** The upper left corner of the first image in the selection*/
    private Point first;

    /** The upper left corner of the last image in the selection*/
    private Point last;

    /** The collection of selected nodes.*/
    private List<ImageDisplay> selected;

    /** The colors to set when nodes are selected or not.*/
    private Colors colors;

    /**
     * Creates a new instance.
     *
     * @param a The bounding rectangle of the first selected object
     * @param b The bounding rectangle of the second selected object
     * @param collect Pass <code>true</code> to collect the selected node,
     *                <code>false</code> otherwise.
     */
    public RowSelectionVisitor(Rectangle a, Rectangle b, boolean collect)
    {
        if (a.y < b.y || (a.y == b.y && a.x < b.x)) {
            first = new Point(a.x, a.y);
            last = new Point(b.x, b.y);
        }
        else {
            first = new Point(b.x, b.y);
            last = new Point(a.x, a.y);
        }

        colors = Colors.getInstance();
        if (collect) selected = new ArrayList<ImageDisplay>();
    }

    /**
     * Returns the collection of selected nodes or <code>null</code>.
     * 
     * @return See above.
     */
    public List<ImageDisplay> getSelected() { return selected; }

    /**
     * Highlights the selected nodes.
     * @see ImageDisplayVisitor#visit(ImageNode)
     */
    public void visit(ImageNode node)
    {
        Rectangle bounds = node.getBounds();
        if ((bounds.y == first.y && bounds.x >= first.x && bounds.y < last.y) ||
                (bounds.y > first.y && bounds.y < last.y) ||
                (bounds.y == last.y && bounds.x <= last.x && bounds.y > first.y) ||
                (bounds.y == first.y && bounds.y == last.y &&
                bounds.x >= first.x && bounds.x <= last.x)) {
            node.setHighlight(colors.getSelectedHighLight(node, false));
            if (selected != null)
                selected.add(node);
        }
        else {
            node.setHighlight(colors.getDeselectedHighLight(node));
        }
    }

    /**
     * Required by {@link ImageDisplayVisitor} I/F no-operation in our case
     * @see ImageDisplayVisitor#visit(ImageSet)
     */
    public void visit(ImageSet node) {}

}
