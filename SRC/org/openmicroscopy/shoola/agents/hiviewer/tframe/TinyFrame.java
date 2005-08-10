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
 * minimize, etc. buttons.  It is possible to collapse the frame: only the
 * title bar will be showing.  It is also possible to view the contents of
 * the internal desktop one component at a time &#151; single-view mode; by
 * default all components that were added to the internal desktop are shown
 * (multi-view mode).  However, the buttons that control those features are not
 * available on all title bars with which this frame can be fitted, so whether
 * those features are accessible depends on the title bar you select.</p>
 * <p>Also note that when in single-view mode, the component that is currently
 * showing in the frame is temporarily removed from the internal desktop &#151;
 * this is so because under the hood a temporary desktop replaces the default
 * inner desktop to ease the task of showing one component at a time.  This
 * component (referred to as the <i>child view</i>) is then returned to the
 * internal desktop when another child component is selected for display or
 * the frame is switched back to the normal multi-view mode.</p>
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
    
    /**
     * Specifies that the frame should be headless.
     * Use this constant to remove the title bar from the frame completely.
     */
    public final static int    NO_BAR = 0;
    
    /**
     * Identifies the full title bar.
     * Use this constant to fit the frame with a title bar having the frame
     * icon and title, a button to collapse/expand the frame, and a button to
     * switch between single-view and multi-view mode.  In the single-view mode,
     * only one of the components in the internal desktop is shown at a time.
     * The user can choose which one from a drop-down menu triggred by the
     * button.  In the multi-view mode (the default) all of the components
     * are shown in the internal desktop.  (However, you're required to 
     * lay them out manually.) 
     */
    public final static int    FULL_BAR = 1;
    
    /**
     * Identifies the small title bar.
     * Use this constant to fit the frame with a title bar having just a
     * button to collapse/expand the frame and the frame title.  This title bar
     * is smaller (in height) than the {@link #FULL_BAR}.
     */
    public final static int    SMALL_BAR = 2;
    
    /**
     * Identifies the header bar.
     * Use this constant to fit the frame with a very thin header.  No buttons,
     * icon, nor title is shown.
     */
    public final static int    HEADER_BAR = 3;
    
    /**
     * Identifies the static title bar.
     * This title bar shows the frame icon and title, but has no buttons to
     * control the frame behavior.
     */
    public final static int    STATIC_BAR = 4;

    /** Bound property name indicating if the frame is collapsed. */
    public final static String COLLAPSED_PROPERTY = "collapsed";
    
    /** Bound property name indicating if the frame is to be highlighted. */
    public final static String HIGHLIGHT_PROPERTY = "highlight";
    
    /** Bound property name indicating the type of the frame title bar. */
    public final static String TITLEBAR_TYPE_PROPERTY = "titleBarType";
    
    /** Bound property name indicating if the frame is in single-view mode. */
    public final static String SINGLE_VIEW_PROPERTY = "singleViewMode";
    
    
    /** The View component that renders this frame. */
    private TinyFrameUI     uiDelegate;
    
    /** This component's Model. */
    private TinyFrameModel  model;
    
    
    /**
     * Creates the View to use as UI delegate for this component.
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
        
        //Create the MVC triad.
        model = new TinyFrameModel(super.getContentPane(), 
          //NOTE: We *need* to use super b/c getContent pane is overridden.                   
                                   new Dimension(getWidth(), getHeight()));
        uiDelegate = createUIDelegate();
        setUI(null);  //Will set it to uiDelegate.
        model.setSingleViewMode(false, uiDelegate);
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
        boolean collapsed = model.isCollapsed();
        if (b == collapsed) return;  //We're already in the requested state.
        
        //What state are we going to transition to?
        if (collapsed) {  //We're going to expand.
            setResizable(true);  //Allow to resize when expanded.
        } else {  //We're going to collapse.
            //Remember current size, so it can be restored later.
            model.setRestoreSize(new Dimension(getWidth(), getHeight()));
            setResizable(false);  //Disallow resizing when collapsed.
        }
        
        //Fire the state change.
        Boolean oldValue = collapsed ? Boolean.TRUE : Boolean.FALSE,
                newValue = b ? Boolean.TRUE : Boolean.FALSE;
        model.setCollapsed(b);
        firePropertyChange(COLLAPSED_PROPERTY, oldValue, newValue);
    }
    
    /** 
     * Tells if this frame is expanded or collapsed.
     * 
     * @return <code>true</code> if collapsed, <code>false</code> if expanded. 
     */
    public boolean isCollapsed() { return model.isCollapsed(); }
    
    /**
     * The size of the frame before the last collapse request.
     * That is before the last call to the 
     * {@link #setCollapsed(boolean) setCollapsed} method with a 
     * <code>true</code> argument while the frame is expanded.
     * 
     * @return The size of the frame before the last collapse request.
     * @see #isCollapsed()
     */
    public Dimension getRestoreSize() { return model.getRestoreSize(); }
    
    /**
     * Returns a color to use for highlighting the frame's title bar or
     * <code>null</code> if the frame is not to be highlighted.
     * 
     * @return See above.
     */
    public Color getHighlight() { return model.getHighlight(); }
    
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
        Color oldValue = model.getHighlight();
        model.setHighlight(highlight);
        firePropertyChange(HIGHLIGHT_PROPERTY, oldValue, highlight);
    }
    
    /**
     * Returns the type of the title bar this frame is currently fitted with.
     * 
     * @return One of the static constants defined by this class.
     */
    public int getTitleBarType() { return model.getTitleBarType(); }
    
    /**
     * Fits this frame with the specified title bar.
     * 
     * @param type One of the static constants defined by this class.
     */
    public void setTitleBarType(int type)
    {
        Integer oldValue = new Integer(model.getTitleBarType()),
                newValue = new Integer(type);
        if (model.setTitleBarType(type))
            firePropertyChange(TITLEBAR_TYPE_PROPERTY, oldValue, newValue);
    }
    
    /**
     * Tells if this frame is in sinlge or multi-view mode
     * 
     * @return <code>true</code> for single-view mode, <code>false</code> for
     *         multi-view mode.
     * @see #setSingleViewMode(boolean)
     */
    public boolean isSingleViewMode() { return model.isSingleViewMode(); }
    
    /**
     * Sets the view mode of this frame.
     * Two modes are possible: single-view and multi-view.  In the single-view
     * mode, only one of the components in the internal desktop is shown at a
     * time.  The user can choose which one from a drop-down menu triggred by
     * a button in the title bar.  In the multi-view mode (the default) all of
     * the components are shown in the internal desktop.  (However, you're 
     * required to lay them out manually.)  Note that by default the frame is
     * in multi-view mode and the only title bar that has controls for managing
     * the single-view mode is the {@link #FULL_BAR}.  So you shouldn't set the
     * single-view mode unless the frame is fitted with a {@link #FULL_BAR}. 
     *  
     * @param singleViewMode <code>true</code> to switch to the single-view
     *                       mode, <code>false</code> to return to the default
     *                       multi-view mode.
     */
    public void setSingleViewMode(boolean singleViewMode)
    {
        Boolean oldValue = 
                    model.isSingleViewMode() ? Boolean.TRUE : Boolean.FALSE,
                newValue = singleViewMode ? Boolean.TRUE : Boolean.FALSE;
        model.setSingleViewMode(singleViewMode, uiDelegate);
        firePropertyChange(SINGLE_VIEW_PROPERTY, oldValue, newValue);
    }
    
    /**
     * Returns the component that is showing in the frame's desktop when
     * in single-view mode.
     * The returned value will always be <code>null</code> if the frame is
     * not in single-view mode.  However, it can also be <code>null</code>
     * if the frame is in single-view mode but no components were added to
     * the internal desktop.
     * 
     * @return The child view, that is the component that is showing in the
     *         frame's desktop when in single-view mode.
     * @see #setSingleViewMode(boolean)
     * @see #setChildView(Component)
     */
    public Component getChildView() { return model.getChildView(); }
    
    /**
     * Sets the given component to be the child view of the frame when in
     * single-view mode.
     * This method does nothing if the frame is not in single-view mode.
     * Otherwise, the passed component is assumed to be a child of the 
     * internal desktop.
     * 
     * @param child The child component. 
     */
    void setChildView(Component child) { model.setChildView(child); }
    
    /**
     * Returns the component that serves as title bar for this frame.
     * 
     * @return See above.
     */
    public JComponent getTitleBar() { return uiDelegate.getTitleBar(); }
    
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
    public Rectangle getContentsBounds() { return model.getContentsBounds(); }
    
    /**
     * Returns the internal desktop, which serves as the content pane for
     * this frame.
     * 
     * @return See above.
     */
    public JDesktopPane getInternalDesktop() { return model.getDesktopPane(); }
    
    /**
     * Overridden to return the internal desktop pane.
     * 
     * @return The internal desktop which is our substitute to the content pane.
     */
    public Container getContentPane() { return model.getDesktopPane(); }
    
    /**
     * Overridden to disallow setting the original content pane.
     */
    public void setContentPane(Container c) {}
    
    /**
     * Overridden to avoid a L&F switch changing the {@link #uiDelegate}.
     */
    public void setUI(InternalFrameUI ui) { super.setUI(uiDelegate); }
    
}
