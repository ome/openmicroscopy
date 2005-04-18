/*
 * org.openmicroscopy.shoola.agents.hiviewer.twindow.SizeButton
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

package org.openmicroscopy.shoola.agents.hiviewer.twindow;




//Java imports
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies

/** 
 * The sizing button in the {@link TitleBar}.
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
class SizeButton
    extends JButton
{
    
    //TODO: replace this code with proper code for icons!!!
    static String DIR = "../graphx/";
    static ImageIcon MINUS, PLUS, MINUS_OVER, PLUS_OVER;
    static
    {
        java.net.URL imgURL = SizeButton.class.getResource(DIR+"minus.png");
        MINUS = new ImageIcon(imgURL);
        imgURL = SizeButton.class.getResource(DIR+"minus_over.png");
        MINUS_OVER = new ImageIcon(imgURL);
        imgURL = SizeButton.class.getResource(DIR+"plus.png");
        PLUS = new ImageIcon(imgURL);
        imgURL = SizeButton.class.getResource(DIR+"plus_over.png");
        PLUS_OVER = new ImageIcon(imgURL);
    }
    
    /** 
     * Identifies the collapse action.
     * @see #setActionType(int) 
     */
    static final int    COLLAPSE = 1;
    
    /** 
     * Identifies the expand action.
     * @see #setActionType(int) 
     */
    static final int    EXPAND = 2;
    
    /** Tooltip text when the button repsents the collapse action. */
    static final String COLLAPSE_TOOLTIP = "Collapse";
    
    /** Tooltip text when the button repsents the expand action. */
    static final String EXPAND_TOOLTIP = "Expand";
       
    /**
     * Creates a new instance.
     */
    SizeButton() 
    {
        setBorder(BorderFactory.createEmptyBorder());  //No border around icon.
        //Just to make sure button sz=icon sz.
        setMargin(new Insets(0, 0, 0, 0));  
        setOpaque(false);  //B/c button=icon.
        setFocusPainted(false);  //Don't paint focus box on top of icon.
        setRolloverEnabled(true);
    }
    
    /**
     * Sets the button to represent the specified action.
     * 
     * @param type One of the constants defined by this class.
     */
    void setActionType(int type)
    {
        switch (type) {
            case COLLAPSE:
                setIcon(MINUS);
                setRolloverIcon(MINUS_OVER);
                setToolTipText(COLLAPSE_TOOLTIP);
                break;
            case EXPAND:
                setIcon(PLUS);
                setRolloverIcon(PLUS_OVER);
                setToolTipText(EXPAND_TOOLTIP);
        }
    }
    
    /** Overridden to make sure no focus is painted on top of the icon. */
    public boolean isFocusTraversable() { return false; }
    
    /** Overridden to make sure no focus is painted on top of the icon. */
    public void requestFocus() {}

}
