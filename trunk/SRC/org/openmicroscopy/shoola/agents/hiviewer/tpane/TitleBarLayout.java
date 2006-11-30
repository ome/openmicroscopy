/*
 * org.openmicroscopy.shoola.agents.hiviewer.tpane.TitleBarLayout
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
import java.awt.LayoutManager;

//Third-party libraries

//Application-internal dependencies

/** 
 * The {@link TitleBar}s layout manager.
 * This class makes sure the minimum dimension of the title bar is always
 * {@link TitleBar#MIN_WIDTH}x{@link TitleBar#getFixedHeight()}.
 * This is possible because the title bar has a <code>null</code> UI delegate
 * and its dimensions are never set. So every call to a <code>getXXXSize</code>
 * method will eventually be answered by this class' 
 * {@link #minimumLayoutSize(Container) minimumLayoutSize} method.
 * This class lays out all the components found in the {@link TitleBar} from
 * left to right, leaving {@link TitleBar#H_SPACING} pixels between them and
 * centering them along the <code>y</code> axis. Every component is sized to
 * its preferred dimensions, unless its height exceeds the title bar's fixed 
 * height less <code>2</code> pixels; in this case the latter value is used
 * instead. If a {@link TinyPane} component is found, then it receives
 * a special treatment: it's given all available width in the bar after all
 * other components have been laid out.  The layout assumes the title bar has
 * no borders.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
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
     * @param c The component to lay out.
     * @return See above.
     */
    public Dimension preferredLayoutSize(Container c)  
    {
        return minimumLayoutSize(c);
    }
    
    /**
     * Returns the {@link TitleBar}'s minimum layout size according to the
     * {@link TitleBar#MIN_WIDTH} constant and the value returned by the
     * {@link TitleBar#getFixedHeight() getFixedHeight} method.
     * 
     * @param c The component to lay out.
     * @return See above.
     */
    public Dimension minimumLayoutSize(Container c) 
    {
        TitleBar titleBar = (TitleBar) c;
        return new Dimension(TitleBar.MIN_WIDTH, titleBar.getFixedHeight());
    }
    
    /**
     * Lays out the {@link TitleBar}'s components.
     * 
     * @param c The component to lay out.
     */
    public void layoutContainer(Container c) 
    {
        TitleBar titleBar = (TitleBar) c;
        Component[] comp = titleBar.getComponents();
        TinyPaneTitle title = null;
        int nextX = TitleBar.H_SPACING,  //x of the next comp to lay out.
            barH = titleBar.getFixedHeight(),
            maxH = 0 < barH-2 ? barH-2 : 0;  
        //NOTE: maxH is the maximum height that we allow for a component.
        //This accounts for the fact that we always want to leave at least
        //1px between the bottom egde of the bar and the bottom of the
        //component and 1px between the top edge of the bar and the top edge
        //of the component.
        Dimension d;
        for (int i = 0, h = 0; i < comp.length; ++i) {
            if (comp[i] instanceof TinyPaneTitle) {
                title = (TinyPaneTitle) comp[i];  //We'll deal w/ it later.
            } else {
                d = comp[i].getPreferredSize();
                h = Math.min(maxH, d.height);
                comp[i].setBounds(nextX, (barH-h)/2, d.width, h);
                nextX += d.width+TitleBar.H_SPACING;
            }
        }
        if (title != null)  //Then give it all the space that is left.
            title.setBounds(nextX+4, 0, //(barH-maxH)/2, 
                            titleBar.getWidth()-nextX-4, barH); //maxH);
    }

    /**
     * Required by {@link LayoutManager} but no-op implementation in our case.
     * @see LayoutManager#addLayoutComponent(String, Component)
     */
    public void addLayoutComponent(String name, Component c) {}
    
    /**
     * Required by {@link LayoutManager} but no-op implementation in our case.
     * @see LayoutManager#removeLayoutComponent(Component)
     */
    public void removeLayoutComponent(Component c) {} 
    
}
