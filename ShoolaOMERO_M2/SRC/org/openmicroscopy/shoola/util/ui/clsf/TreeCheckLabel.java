/*
 * org.openmicroscopy.shoola.util.ui.clsf.TreeCheckLabel
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

package org.openmicroscopy.shoola.util.ui.clsf;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

//Third-party libraries

//Application-internal dependencies

/** 
 * A component to paint the icon and the text.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class TreeCheckLabel
    extends JLabel
{

    /** Flag to indicate that the label is selected. */
    private boolean selected;
    
    /** Flag to indicate that the label has focus. */
    private boolean hasFocus;
    
    /** Creates a new instance. */
    TreeCheckLabel()
    {
        selected = false;
        hasFocus = false;
    }
    
    /**
     * Sets to <code>true</code> if selected, <code>false</code> otherwise.
     * 
     * @param selected The value to set.
     */ 
    void setSelected(boolean selected) { this.selected = selected; }
    
    /**
     * Sets to <code>true</code> if the component has focus, <code>false</code>
     * otherwise.
     * 
     * @param hasFocus The value to set.
     */ 
    void setFocus(boolean hasFocus) { this.hasFocus = hasFocus; }
    
    /**
     * Overriden to set the background color.
     * @see JLabel#setBackground(Color)
     */
    public void setBackground(Color color)
    {
        if (color instanceof ColorUIResource) color = null;
        super.setBackground(color);
    }
    
    /**
     * Overriden to set the ideal size of the component.
     * @see JLabel#getPreferredSize()
     */
    public Dimension getPreferredSize()
    {
        Dimension d = super.getPreferredSize();
        if (d != null)  d = new Dimension(d.width+3, d.height);
        return d;
    }
    
    /**
     * Overriden to properly paint the icon and the text.
     * @see JLabel#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        Color c = UIManager.getColor("Tree.textBackground");
        if (selected) c = UIManager.getColor("Tree.selectionBackground");
        g.setColor(c);
        Dimension d = getPreferredSize();
        int offset = 0;
        Icon currentI = getIcon();
        if (currentI != null)
            offset = currentI.getIconWidth()+Math.max(0, getIconTextGap()-1);
        g.fillRect(offset, 0, d.width-offset-1, d.height);
        if (hasFocus) {
            g.setColor(UIManager.getColor("Tree.selectionBorderColor"));
            g.drawRect(offset, 0, d.width-1-offset, d.height-1);     
        }
        super.paintComponent(g);
    }
    
}
