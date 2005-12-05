/*
 * org.openmicroscopy.shoola.agents.hiviewer.tpane.TinyPaneLayout
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

package org.openmicroscopy.shoola.agents.hiviewer.tpane;

//Java imports
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies

/** 
 * The {@link TinyPane}'s layout manager.
 * This class makes sure that the <code>TitleBar</code> and the 
 * <code>Internal desktop</code> are layout properly.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TinyPaneLayout
    implements LayoutManager
{

    /**
     * Returns the {@link TinyPane#getPreferredSize() preferredSize} of the 
     * container.
     * 
     * @return See above.
     */
    public Dimension preferredLayoutSize(Container c)
    {
        TinyPane frame = (TinyPane) c;
        return frame.getPreferredSize();
    }

    /**
     * Returns the minimum size of the <code>TitleBar</code>.
     */
    public Dimension minimumLayoutSize(Container c)
    {
        TinyPane frame = (TinyPane) c;
        Dimension d = frame.getTitleBar().getMinimumSize();
        Insets i = frame.getInsets();
        d.width += i.left+i.right;
        d.height += i.top+i.bottom;
        return d;
    }


    /**
     * Lays out the {@link TinyPane}'s components.
     */
    public void layoutContainer(Container c)
    {
        TinyPane frame = (TinyPane) c;
        Insets i = frame.getInsets();
        int cx, cy, cw, ch;
        cx = i.left;
        cy = i.top;
        cw = frame.getWidth()-i.left-i.right;
        ch = frame.getHeight()-i.top-i.bottom;
        JComponent titlebar = frame.getTitleBar();
        if (titlebar != null) {
            Dimension size = titlebar.getPreferredSize();
            titlebar.setBounds(cx, cy, cw, size.height);
            cy += size.height;
            ch -= size.height;
        }
        if (frame.getContentPane() != null)
            frame.getContentPane().setBounds(cx, cy, cw, ch);  
    }

    /**
     * No-op implementation.
     * Required by {@link LayoutManager}, but not needed here.
     */
    public void addLayoutComponent(String name, Component c) {}
    
    /**
     * No-op implementation.
     * Required by {@link LayoutManager}, but not needed here.
     */
    public void removeLayoutComponent(Component c) {} 
    
}
