/*
 * org.openmicroscopy.shoola.agents.browser.colormap.ColorCellRenderer
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.colormap;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Renderer for color boxes in the classification list.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ColorCellRenderer extends ColorBoxLabel
                               implements ListCellRenderer
{
    public ColorCellRenderer()
    {
        setOpaque(true);
    }
    /**
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int cellIndex,
                                                  boolean isSelected,
                                                  boolean hasFocus)
    {
        if(!(value instanceof ColorPair))
        {
            setText(value.toString());
        }
        else
        {
            ColorPair cp = (ColorPair)value;
            setText(cp.getName());
            setBoxColor(cp.getColor());
        }
        if(isSelected)
        {
            setBackground(new Color(51,153,255));
        }
        else
        {
            setBackground(list.getBackground());
        }
        if(hasFocus)
        {
            setBorder(BorderFactory.createLineBorder(Color.blue,2));
        }
        else
        {
            setBorder(BorderFactory.createLineBorder(getBackground(),2));
        }
        return this;
    }

}
