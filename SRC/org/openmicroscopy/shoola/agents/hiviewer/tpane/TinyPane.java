/*
 * org.openmicroscopy.shoola.agents.hiviewer.tpane.TinyPane
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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;

import javax.swing.DesktopManager;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

//Third-party libraries

//Application-internal dependencies

/** 
 * A tiny-looking panel. We view it as a static internal frame, so behaves 
 * mostly like an internal frame.
 * <p>This panel has a small title bar, small automatic scroll bars, and an
 * layered pane as the content pane. 
 * <p>It is possible to collapse the panel: only the
 * title bar will be showing. This feature accessibility depends on the 
 * title bar you select.</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TinyPane
    extends JPanel
{

    /**
     * Specifies that the frame should be headless.
     * Use this constant to remove the title bar from the frame completely.
     */
    public final static int     NO_BAR = 0;
    
    /**
     * Identifies the small title bar.
     * Use this constant to fit the frame with a title bar having just a
     * button to collapse/expand the frame and the frame title. 
     */
    public final static int     SMALL_BAR = 1;
    
    /**
     * Identifies the header bar.
     * Use this constant to fit the frame with a very thin header. No buttons,
     * icon, nor title is shown.
     */
    public final static int     HEADER_BAR = 2;
    
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
    public final static int     FULL_BAR = 3;
    
    /**
     * Identifies the static title bar.
     * This title bar shows the frame icon and title, but has no buttons to
     * control the frame behavior.
     */
    public final static int     STATIC_BAR = 4;
    
    /** Bound property name indicating if the frame is collapsed. */
    public final static String  COLLAPSED_PROPERTY = "collapsed";
    
    /** Bound property name indicating if the frame is to be highlighted. */
    public final static String  HIGHLIGHT_PROPERTY = "highlight";
    
    /** Bound property name indicating the type of the frame title bar. */
    public final static String  TITLEBAR_TYPE_PROPERTY = "titleBarType";
    
    /** Bound property name indicating the value of title. */
    public final static String  TITLE_PROPERTY = "title";
    
    /** Bound property name indicating if the frame is in single-view mode. */
    public final static String  SINGLE_VIEW_PROPERTY = "singleViewMode";
    
    /** Bound property name. */
    public final static String  FRAME_ICON_PROPERTY = "frameIcon";
    
    /** Bound property name indicating if the frame can be moved. */
    public final static String  BORDER_LISTENER_PROPERTY = "borderListener";
    
    /** Bound property name indicating if the frame is resizable. */
    public final static String  RESIZABLE_PROPERTY = "resizable";  
    
    /** The View component that renders this frame. */
    private TinyPaneUI     uiDelegate;
    
    /** This component's Model. */
    private TinyPaneModel  model;
    
    /**
     * Contains the Component that focus is to go when
     * <code>restoreSubcomponentFocus</code> is invoked, that is,
     * <code>restoreSubcomponentFocus</code> sets this to the value returned
     * from <code>getMostRecentFocusOwner</code>.
     */
    private Component lastFocusOwner;

    /**
     * Called by the constructor methods to create the default 
     * <code>contentPane</code>. 
     * By default this method creates a new <code>JComponent</code> add sets a 
     * <code>BorderLayout</code> as its <code>LayoutManager</code>.
     * @return the default <code>contentPane</code>
     */
    protected Container createContentPane()
    {
        JComponent c = new JPanel();
        c.setName(this.getName()+".contentPane");
        c.setLayout(new BorderLayout() {
            /* This BorderLayout subclass maps a null constraint to CENTER.
             * Although the reference BorderLayout also does this, some VMs
             * throw an IllegalArgumentException.
             */
        
            public void addLayoutComponent(Component comp, Object constraints) {
                if (constraints == null) {
                    constraints = BorderLayout.CENTER;
                }
                super.addLayoutComponent(comp, constraints);
            }
        });
        
        return c; 
    }
    
    /**
     * Creates the View to use as UI delegate for this component.
     * 
     * @return The UI delegate for this component.
     */
    protected TinyPaneUI createUIDelegate() { return new TinyPaneUI(this); }
    
    /** Creates a new instance. */
    public TinyPane() { this(null, null); }
    
    /**
     * Creates a new instance.
     * 
     * @param title The title displayed in the <code>TitleBar</code>.
     */
    public TinyPane(String title)
    {
        this(title, null);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param title The title displayed in the <code>TitleBar</code>.
     * @param note The note added to the <code>TitleBar</code>.
     */
    public TinyPane(String title, String note) 
    {
        super();
        model = null;
        Container container = createContentPane();
        model = new TinyPaneModel(container, title, 
                                new Dimension(getWidth(), getHeight()));
        model.setNote(note);
        uiDelegate = createUIDelegate();
        setLayout(new TinyPaneLayout());
        add(uiDelegate.getTitleBar());
        container.add(getInternalDesktop());
        add(container);
        setUI(uiDelegate);
        model.setSingleViewMode(false, uiDelegate);
    }
    
    /**
     * Returns the proper DesktopManager. Calls the {@link TinyPaneUI} to 
     * find and returns the desktopManager
     * 
     * @return See above.
     */
    DesktopManager getDesktopManager()
    {
        return uiDelegate.getDesktopManager();
    }
    
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
     * Returns the note added to the <code>TitleBar</code>.
     * 
     * @return See above.
     */
    String getNote() { return model.getNote(); }
    
    /** Resizes and repaints the component. */
    public void pack()
    {
        setSize(getPreferredSize());
        validate();
        repaint();
    }
    
    /** 
     * Returns the title of this component.
     * 
     * @return See above.
     */
    public String getTitle() { return model.getTitle(); }
    
    /**
     * Sets the title of the frame.
     * 
     * @param title The title to set.
     */
    public void setTitle(String title)
    {
        String oldValue = model.getTitle();
        model.setTitle(title);
        firePropertyChange(TITLE_PROPERTY, oldValue, title);
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
        
        if (!collapsed)
            //Remember current size, so it can be restored later.
            model.setRestoreSize(new Dimension(getWidth(), getHeight()));

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
    public JComponent getInternalDesktop() { return model.getDesktopPane(); }
    
    /**
     * Overridden to return the internal desktop pane.
     * 
     * @return The internal desktop which is our substitute to the content pane.
     */
    public Container getContentPane() { return model.getContentPane(); }
    
    /** 
     * Returns the scroll pane that contains the internal desktop.
     * 
     * @return See above.
     */
    public JScrollPane getDeskDecorator() 
    {
        return uiDelegate.getDeskDecorator();
    }
    
    public void setResizable(boolean resizable) 
    {
        Boolean oldValue = 
            model.isResizable() ? Boolean.TRUE : Boolean.FALSE,
        newValue = resizable ? Boolean.TRUE : Boolean.FALSE;
        model.setResizable(resizable);
        firePropertyChange(RESIZABLE_PROPERTY, oldValue, newValue);
    }
    
    /**
     * Returns the resizable state.
     * 
     * @return See above.
     */
    public boolean isResizable() { return model.isResizable(); }
    
    
    /**
     * Sets the icon of this frame.
     * 
     * @param icon The icon to set.
     */
    public void setFrameIcon(Icon icon)
    { 
        Icon oldIcon = model.getFrameIcon();
        model.setFrameIcon(icon);
        firePropertyChange(FRAME_ICON_PROPERTY, oldIcon, icon);
    }
     
    /**
     * Returns the frame's icon.
     * 
     * @return See above.
     */
    public Icon getFrameIcon() { return model.getFrameIcon(); }
    
    /**
     * Passes <code>true</code> to listen to the border.
     * <code>false</code> otherwise.
     * 
     * @param listenToBorder The flag to set.
     */
    public void setListenToBorder(boolean listenToBorder) 
    {
        Boolean oldValue = 
            model.isListenToBorder() ? Boolean.TRUE : Boolean.FALSE,
        newValue = listenToBorder ? Boolean.TRUE : Boolean.FALSE;
        model.setListenToBorder(listenToBorder);
        firePropertyChange(BORDER_LISTENER_PROPERTY, oldValue, newValue);
    }
    
    /**
     * Returns <code>true</code> if a border listener is set, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isListenToBorder() { return model.isListenToBorder(); }
    
    /** Moves the frame to the front. */
    public void moveToFront() 
    {
        if (getParent() != null && getParent() instanceof JLayeredPane) {
            // Because move to front typically involves adding and removing
            // Components, it will often times result in focus changes. We
            // either install focus to the lastFocusOwner, or our desendant
            // that has focus. We use the ivar for lastFocusOwner as in
            // some cases requestFocus is async and won't have completed from
            // the requestFocus call in setSelected so that getFocusOwner
            // will return the wrong component.
            Component focusOwner = (lastFocusOwner != null) ? lastFocusOwner :
                         KeyboardFocusManager.getCurrentKeyboardFocusManager().
                         getFocusOwner();

            if (focusOwner != null &&
                     !SwingUtilities.isDescendingFrom(focusOwner, this))
                focusOwner = null;
            ((JLayeredPane) getParent()).moveToFront(this);
            if (focusOwner != null) focusOwner.requestFocus();
        }
    }
    
    /** Moves the component to the back. */
    public void moveToBack()
    {
        if (getParent() != null && getParent() instanceof JLayeredPane) 
            ((JLayeredPane) getParent()).moveToBack(this);

    }
    
    /**
     * Replaces the original display by the specified one.
     * 
     * @param c The new display.
     */
    public void setChangeDisplay(JComponent c)
    {
       if (c == null) return;
       model.setChangeDisplay(c, uiDelegate);
    }
    
    /** Restores the original display. */
    public void restoreDisplay() { model.restoreDisplay(uiDelegate); }
    
    /**
     * Overrides to return the title of the frame.
     * @see JPanel#toString()
     */
    public String toString() { return getTitle(); }
    
}
