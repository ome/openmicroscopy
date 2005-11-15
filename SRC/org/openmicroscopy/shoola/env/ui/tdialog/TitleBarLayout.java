/*
 * org.openmicroscopy.shoola.env.ui.tdialog.TitleBarLayout
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

package org.openmicroscopy.shoola.env.ui.tdialog;




//Java imports
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

//Third-party libraries

//Application-internal dependencies

/** 
 * The {@link TitleBar}'s layout manager.
 * This class makes sure the minimum dimension of the title bar is always
 * {@link TitleBar#MIN_WIDTH}x{@link TitleBar#HEIGHT}.
 * This is possible because the title bar has a <code>null</code> UI delegate
 * and its dimensions are never set. So every call to a <code>getXXXSize</code>
 * method will eventually be answered by this class' 
 * {@link #minimumLayoutSize(Container) minimumLayoutSize} method.
 * The layout assumes the title bar has no borders.
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
class TitleBarLayout
    implements LayoutManager
{
    
    /**
     * Returns the {@link #minimumLayoutSize(Container) minimumLayoutSize}.
     * 
     * @return See above.
     */
    public Dimension preferredLayoutSize(Container c)  
    {
        return minimumLayoutSize(c);
    }

    /**
     * Returns the {@link TitleBar}'s minimum layout size according to the
     * {@link TitleBar#MIN_WIDTH} and {@link TitleBar#HEIGHT} constants.
     * 
     * @return See above.
     */
    public Dimension minimumLayoutSize(Container c) 
    {
        return new Dimension(TitleBar.MIN_WIDTH, TitleBar.HEIGHT);
    }

    /**
     * Lays out the {@link TitleBar}'s components.
     */
    public void layoutContainer(Container c) 
    {
        TitleBar titleBar = (TitleBar) c;
        titleBar.sizeButton.setBounds(
                TitleBar.H_SPACING,  //x, space from the left edge. 
                (TitleBar.HEIGHT-TitleBar.SIZE_BUTTON_DIM)/2, //y, centered.
                TitleBar.SIZE_BUTTON_DIM, //w=h, it must be a square.
                TitleBar.SIZE_BUTTON_DIM);
        titleBar.closeButton.setBounds(
                //x, next to the sizeButton. 
                2*TitleBar.H_SPACING+TitleBar.SIZE_BUTTON_DIM,  
                (TitleBar.HEIGHT-TitleBar.SIZE_BUTTON_DIM)/2, //y, centered.
                TitleBar.SIZE_BUTTON_DIM, //w=h, it must be a square.
                TitleBar.SIZE_BUTTON_DIM);
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
