/*
 * org.openmicroscopy.shoola.agents.hiviewer.tframe.TinyFrame
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

package org.openmicroscopy.shoola.agents.hiviewer.tframe;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.plaf.InternalFrameUI;

//Third-party libraries

//Application-internal dependencies

/** 
 * A tiny-looking internal frame.
 * <p>This frame has a small title bar, small automatic scroll bars, and an
 * internal desktop as the content pane.  This latter feature allows to easily
 * nest <code>TinyFrame</code>s.</p>
 * <p>The frame behaves mostly like a regular internal frame, but has no close,
 * minimize, etc. buttons.  Instead a single sizing button is provided that
 * allows to expand/collapse the frame contents.</p>
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
public class TinyFrame
    extends JInternalFrame
{

    /** Bound property name indicating if the frame is collapsed. */
    public final static String COLLAPSED_PROPERTY = "collapsed";
    
    /** Bound property name indicating if the frame is to be highlighted. */
    public final static String HIGHLIGHT_PROPERTY = "highlight";
    
    
    /** The View component that renders this frame. */
    private TinyFrameUI     uiDelegate;
    
    /** 
     * Tells if this frame has to be highlighted.
     * If <code>null</code>, the frame's title bar will display the normal
     * background.  If a color is specified, the title bar will be highlighted
     * using the specified color. 
     */
    private Color           highlight;
    
    /** Tells if this frame is expanded or collapsed. */
    private boolean         collapsed;
    
    /** The size of the frame before the last collapse request. */
    private Dimension       restoreSize;
    
    /** Substitutes the original content pane, all contents are added here. */
    protected JDesktopPane  desktopPane;
    
    
    /**
     * Creates the View to use as UI delegate for this component.
     * This method is protected to allow subclasses to return a different
     * UI delegate.
     * 
     * @return The UI delegate for this component.
     */
    protected TinyFrameUI createUIDelegate() { return new TinyFrameUI(this); }
    
    /**
     * Creates a new internal frame.
     * You have to add this frame to a desktop pane.
     */
    public TinyFrame() { this(null); }
    
    /**
     * Creates a new internal frame.
     * You have to add this frame to a desktop pane.
     * 
     * @param title The frame's title.
     */
    public TinyFrame(String title)
    {
        super(title);
        
        //Window properties.
        setResizable(true);
        setIconifiable(false);
        setMaximizable(false);
        setClosable(false);
        
        //Create the View and the Controller.
        uiDelegate = createUIDelegate();
        setUI(null);  //Will set it to uiDelegate.
        new FrameControl(this, uiDelegate);
        restoreSize = new Dimension(getWidth(), getHeight());
        
        //Install the desktop which will replace the default content pane.
        desktopPane = new JDesktopPane();
        super.getContentPane().add(uiDelegate.decorateDesktopPane(desktopPane),
                                    BorderLayout.CENTER);
        //NOTE: We *need* to use super b/c getContent pane is overridden.
    }
    
    /**
     * Collapses or expands this frame depending on the passed value.
     * This is a bound property; property change listeners are notified
     * of any change to this property. 
     * 
     * @param b Pass <code>true</code> to collapse, <code>false</code> to
     *          expand.
     */
    public void setCollapsed(boolean b)
    {
        if (b == collapsed) return;  //We're already in the requested state.
        
        //What state are we going to transition to?
        if (collapsed) {  //We're going to expand.
            setResizable(true);  //Allow to resize when expanded.
        } else {  //We're going to collapse.
            //Remember current size, so it can be restored later.
            restoreSize = new Dimension(getWidth(), getHeight());
            setResizable(false);  //Disallow resizing when collapsed.
        }
        
        //Fire the state change.
        Boolean oldValue = collapsed ? Boolean.TRUE : Boolean.FALSE,
                newValue = b ? Boolean.TRUE : Boolean.FALSE;
        collapsed = b;
        firePropertyChange(COLLAPSED_PROPERTY, oldValue, newValue);
    }
    
    /** 
     * Tells if this frame is expanded or collapsed.
     * 
     * @return <code>true</code> if collapsed, <code>false</code> if expanded. 
     */
    public boolean isCollapsed() { return collapsed; }
    
    /**
     * The size of the frame before the last collapse request.
     * That is before the last call to the 
     * {@link #setCollapsed(boolean) setCollapsed} method with a 
     * <code>true</code> argument while the frame is expanded.
     * 
     * @return The size of the frame before the last collapse request.
     * @see #isCollapsed()
     */
    public Dimension getRestoreSize() { return restoreSize; }
    
    /**
     * Returns a color to use for highlighting the frame's title bar or
     * <code>null</code> if the frame is not to be highlighted.
     * 
     * @return See above.
     */
    public Color getHighlight() { return highlight; }
    
    /**
     * Sets the highlight mode of this frame.
     * If a color is specified, then the title bar will be highlighted using
     * that color.  If you pass <code>null</code>, then the title bar will
     * be painted in the normal background.
     * 
     * @param highlight A color for highlighting or <code>null</code> to
     *                  restore the normal background.
     */
    public void setHighlight(Color highlight)
    {
        Color oldValue = this.highlight;
        this.highlight = highlight;
        firePropertyChange(HIGHLIGHT_PROPERTY, oldValue, highlight);
    }
    
    /**
     * Returns the component that serves as title bar for this frame.
     * 
     * @return See above.
     */
    public JComponent getTitleBar() { return uiDelegate.titleBar; }
    
    /**
     * Returns the size this frame should have to fully display the internal
     * desktop at its preferred size.
     * 
     * @return See above.
     */
    public Dimension getPreferredSize() { return uiDelegate.getIdealSize(); }
    
    /**
     * Returns the bounds of the area, within the internal desktop, which is
     * occupied by the contained components.
     * 
     * @return See above.
     */
    public Rectangle getContentsBounds()
    {
        Component[] comp = desktopPane.getComponents();
        int x = 0, y = 0, //Top left corner coordinates.
            br_x = 0, br_y = 0;  //Bottom right corner coordinates.
        Rectangle bounds;
        for (int i = 0; i < comp.length; ++i) {
            bounds = comp[i].getBounds();
            x = Math.min(x, bounds.x);
            y = Math.min(y, bounds.y);
            br_x = Math.max(br_x, bounds.x+bounds.width);
            br_y = Math.max(br_y, bounds.y+bounds.height);
        }
        return new Rectangle(x, y, br_x-x, br_y-y);
    }
    
    /**
     * Returns the internal desktop, which serves as the content pane for
     * this frame.
     * 
     * @return See above.
     */
    public JDesktopPane getInternalDesktop() { return desktopPane; }
    
    /**
     * Overridden to return the internal desktop pane.
     * 
     * @return The internal desktop which is our substitute to the content pane.
     */
    public Container getContentPane() { return desktopPane; }
    
    /**
     * Overridden to disallow setting the original content pane.
     */
    public void setContentPane(Container c) {}
    
    /**
     * Overridden to avoid a L&F switch changing the {@link #uiDelegate}.
     */
    public void setUI(InternalFrameUI ui) { super.setUI(uiDelegate); }
    
    /** Shows the titleBar. */
    public void showTitleBar(boolean b) { uiDelegate.showTitleBar(b); }
    
}
