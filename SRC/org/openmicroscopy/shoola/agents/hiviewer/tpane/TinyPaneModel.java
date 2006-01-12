/*
 * org.openmicroscopy.shoola.agents.hiviewer.tpane.TinyPaneModel
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
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;

//Third-party libraries

//Application-internal dependencies

/** 
 * The Model sub-component in the {@link TinyPane}'s MVC triad.
 * This is a presentation Model which holds and manages presentation data
 * in behalf of the {@link TinyPane}, which has pass-through methods that
 * delegate to this class, but manage the change notification process.  So
 * all the other sub-components regard the {@link TinyPane} as the Model.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class TinyPaneModel
{
    
    /** The frame's content pane. */
    private Container       contentPane;
    
    /** 
     * Tells if the frame has to be highlighted.
     * If <code>null</code>, the frame's title bar will display the normal
     * background.  If a color is specified, the title bar will be highlighted
     * using the specified color. 
     */
    private Color           highlight;
    
    
    /** Tells if the frame is expanded or collapsed. */
    private boolean         collapsed;
    
    /** The size of the frame before the last collapse request. */
    private Dimension       restoreSize;
    
    /** Identifies the title bar the frame is fitted with. */
    private int             titleBarType;
    
    /** All contents are added to this container. */
    private JLayeredPane    desktopPane;
    
    /** Tells if the frame is in single or multi-view mode. */
    private boolean         singleViewMode;
    
    /** Replaces the {@link #desktopPane} when we're in single-view mode. */
    private JLayeredPane    singleViewDesktop;
    
    /** Replaces the current desktop when we modify the display. */
    private JLayeredPane    changeDisplayDesktop;
    
    /** 
     * The bounds of the currently displayed child view relative to the
     * {@link #desktopPane} in which it was originally contained, if we're
     * in single-view mode or <code>null</code> when in multi-view mode.
     * We save this object so that we can restore the original bounds when we
     * add the component back to the internal desktop &#151; that is, after
     * the user selects another child to display in the single-view desktop
     * or when the user switches back to the multi-view mode.
     */
    private Rectangle       childViewBounds;
    
    /** 
     * The type of the currently displayed child view's title bar, if we're in
     * single-view mode.
     * We save this value so that we can restore it when the child view is
     * returned to the original {@link #desktopPane}.
     */
    private int             childViewTitleBarType;
    
    /** 
     * The collapsed state of the the currently displayed child view, if we're
     * in single-view mode.
     * We save this value so that we can restore it when the child view is
     * returned to the original {@link #desktopPane}.
     */
    private boolean         childViewCollapsed;
    
    /** The title of the component. */
    private String          title;
    
    /** 
     * Flag to indicate if the frame is resizable. Default value is
     * <code>false</code>.
     */
    private boolean         resizable;
    
    /**
     * Flag to indicate if the frame is draggable, resizable etc.
     */
    private boolean         listenToBorder;
    
    /** The frame's icon. */
    private Icon            frameIcon;
    
    /**
     * Checks that <code>c</code> is one of the components that were added
     * to the internal desktop.
     * 
     * @param c The component to check.
     * @return <code>true</code> if <code>c</code> is a child to the internal
     *         desktop; <code>false</code> otherwise.
     */
    private boolean isChild(Component c)
    {
        if (c != null) {
            //First check if it's a child to the desktopPane.
            Component[] children = desktopPane.getComponents();
            for (int i = 0; i < children.length; ++i) 
                if (c == children[i]) return true;
            
            //We might be in single-view mode and c was removed from the
            //desktopPane.  Check.
            if (c == getChildView()) return true;   
        }
        return false;
    }
    
    /**
     * Utility method to pick the first child component out of a given
     * container.
     * 
     * @param c The container.
     * @return Returns the first component in <code>c</code> if <code>c</code>
     *         is not <code>null</code> and has children; returns <code>null
     *         </code> if <code>c</code> is <code>null</code> or has no
     *         children.
     */
    private Component getFirstChild(Container c)
    {
        Component child = null;
        if (c != null) {
            Component[] comp = c.getComponents();
            if (0 < comp.length) child = comp[0];
        }
        return child;
    }
    
    /** 
     * Sets the size of the container when we display
     * a child in single view mode.
     * 
     * @param parent The container.
     */
    private void setParentSizeForSingleView(Container parent)
    {
        if (parent instanceof TinyPane) {
            Dimension d = ((TinyPane) parent).getTitleBar().getPreferredSize();
            Dimension dDesktop = singleViewDesktop.getPreferredSize();
            parent.setSize(d.width+dDesktop.width, d.height+dDesktop.height+4);
            parent.validate();
        } else setParentSizeForSingleView(parent.getParent());
    }
    
    /** 
     * Sets the size of the container when we display
     * the children in multi-view mode.
     * 
     * @param parent The container.
     */
    private void setParentSizeForMultiView(Container parent)
    {
        if (parent instanceof TinyPane) {
            parent.setSize(parent.getPreferredSize());
            parent.validate();
        } else setParentSizeForMultiView(parent.getParent());
    }
    
    /**
     * Creates a new instance.
     * After creation the model is not collapsed, the title bar is the 
     * {@link TinyPane#FULL_BAR}, and there's no highlight.
     * However, the internal desktop is not installed yet and you need
     * to call {@link #setSingleViewMode(boolean, TinyPaneUI)} passing
     * <code>false</code> to complete intialization.  At which point, the
     * model will be in multi-view mode and the internal desktop will be 
     * installed.  
     * 
     * @param contentPane	The container hosting the display.
     * @param title         The frame's title.
     * @param restoreSize   The initial frame's size. 
     *                      Mustn't be <code>null</code>.
     */
    TinyPaneModel(Container contentPane, String title, Dimension restoreSize)
    {
        if (contentPane == null) 
            throw new NullPointerException("No content pane.");
        this.contentPane = contentPane;
        this.title = title;
        setRestoreSize(restoreSize);
        desktopPane = new JLayeredPane();
        setTitleBarType(TinyPane.FULL_BAR);
        resizable = true;
        listenToBorder = true;
    }
    
    /**
     * Replaces the original display by the specified one.
     * 
     * @param c The new display.
     * @param uiDelegate The frame's View.  We need it here to decorate the
     *                   desktops. Mustn't be <code>null</code>.
     */
    void setChangeDisplay(JComponent c, TinyPaneUI uiDelegate)
    {
        if (uiDelegate == null) 
            throw new NullPointerException("No UI delegate.");
        Rectangle r = contentPane.getBounds();
        contentPane.removeAll();
        changeDisplayDesktop = new JLayeredPane();
        contentPane.removeAll();
        contentPane.add(
                uiDelegate.decorateDesktopPane(changeDisplayDesktop),
                BorderLayout.CENTER);
        c.setSize(r.getSize());
        changeDisplayDesktop.setLayout(new BorderLayout());
        changeDisplayDesktop.add(c, BorderLayout.CENTER);
        changeDisplayDesktop.setSize(r.getSize());
        contentPane.validate();
        contentPane.repaint();
    }
    
    /**
     * Restores the original view.
     * 
     * @param uiDelegate The frame's View.  We need it here to decorate the
     *                   desktops.  Mustn't be <code>null</code>.
     */
    void restoreDisplay(TinyPaneUI uiDelegate)
    {
        if (changeDisplayDesktop == null) return;
        contentPane.removeAll();
        if (singleViewMode) {
            if (singleViewDesktop != null)
                contentPane.add(
                    uiDelegate.decorateDesktopPane(singleViewDesktop),
                    BorderLayout.CENTER);
        } else 
        contentPane.add(
                uiDelegate.decorateDesktopPane(desktopPane),
                BorderLayout.CENTER);
        contentPane.validate();
        contentPane.repaint();
    }
    
    /**
     * Tells if we're in single or multi-view mode. 
     * 
     * @return See above.
     */
    boolean isSingleViewMode() { return singleViewMode; }
    
    /**
     * Sets the frame's view mode.
     *  
     * @param singleViewMode <code>true</code> to switch to the single-view
     *                       mode, <code>false</code> to return to the default
     *                       multi-view mode.
     * @param uiDelegate The frame's View.  We need it here to decorate the
     *                   desktops.  Mustn't be <code>null</code>.
     */
    void setSingleViewMode(boolean singleViewMode, TinyPaneUI uiDelegate)
    {
        if (uiDelegate == null) 
            throw new NullPointerException("No UI delegate.");
        this.singleViewMode = singleViewMode;
        if (singleViewMode) {  //Create the single-view tmp desktop.
            singleViewDesktop = new JLayeredPane();
            contentPane.removeAll();
            contentPane.add(
                    uiDelegate.decorateDesktopPane(singleViewDesktop),
                    BorderLayout.CENTER);
            
            //Make the first child (if any) of the internal desktop the 
            //current child view.
            setChildView(getFirstChild(desktopPane));
        } else {  //Normal multi-view mode, restore the internal desktop.
            contentPane.removeAll();
            contentPane.add(
                    uiDelegate.decorateDesktopPane(desktopPane),
                    BorderLayout.CENTER);
            
            //Return the current child view to the internal desktop.
            setChildView(null);
            singleViewDesktop = null;
        }
    }
    
    /**
     * Sets the given component in the single-view desktop.
     * This method does nothing if the frame is not in single-view mode.
     * Otherwise, the passed component is assumed to be a child of the 
     * internal desktop or is <code>null</code> if the single-view desktop
     * has to be cleared.
     * 
     * @param child The child component. 
     */
    void setChildView(Component child)
    {
        Component oldChild = getChildView();
        if (oldChild != null) {  //Return it to the internal desktop.
            singleViewDesktop.remove(oldChild);
            desktopPane.add(oldChild);
            oldChild.setBounds(childViewBounds);
            if (oldChild instanceof TinyPane) {
                TinyPane tf = (TinyPane) oldChild;
                tf.setTitleBarType(childViewTitleBarType);
                tf.setCollapsed(childViewCollapsed);
                if (child == null) {
                    setParentSizeForMultiView(tf.getParent());
                    //contentPane.validate();
                    //contentPane.repaint(); 
                }
            }
        }
        if (isChild(child)) {  //We've got a new child to display.
            desktopPane.remove(child);
            singleViewDesktop.add(child);
            childViewBounds = child.getBounds();
            //was new Dimension(childViewBounds.width, childViewBounds.height)
            singleViewDesktop.setPreferredSize(child.getPreferredSize());
            if (child instanceof TinyPane) {
                TinyPane tf = (TinyPane) child;
                childViewTitleBarType = tf.getTitleBarType();
                childViewCollapsed = tf.isCollapsed();
                tf.setTitleBarType(TinyPane.HEADER_BAR);
                tf.setCollapsed(false);
                setParentSizeForSingleView(tf.getParent());
            }
            //Need to set the size of the child after setting the title bar.
            Dimension d = child.getPreferredSize();
            child.setBounds(2, 2, d.width, d.height);
            //Do it this way otherwise the methods are called at init time.
            //contentPane.validate();
            //contentPane.repaint(); 
        }
    }
    
    /**
     * Returns the component that is showing in the single-view desktop when
     * in single-view mode.
     * The returned value will always be <code>null</code> if the frame is
     * not in single-view mode.  However, it can also be <code>null</code>
     * if the frame is in single-view mode but no components were added to
     * the internal desktop.
     * 
     * @return The child view, that is the component that is showing in the
     *         single-view desktop when in single-view mode.
     */
    Component getChildView() { return getFirstChild(singleViewDesktop); }
    
    /**
     * Returns the title of this component.
     * 
     * @return See below.
     */
    String getTitle() { return title; }
    
    /**
     * Sets the title of this component.
     * 
     * @param title The title to set.
     */
    void setTitle(String title) { this.title = title; }
    
    /** 
     * Returns the title bar type.
     * 
     * @return One of the constants defined by {@link TinyPane}. 
     */
    int getTitleBarType() { return titleBarType; }
    
    /**
     * Sets the title bar type.
     * Does nothing if <code>type</code> is not a valid one.
     * 
     * @param type One of the constants defined by {@link TinyPane}.
     * @return <code>true</code> if <code>type</code> is a valid flag and so
     *         it has been set; <code>false</code> if <code>type</code> is
     *         not a valid flag and so it has been ignored.
     */
    boolean setTitleBarType(int type)
    {
        switch (type) {
            case TinyPane.FULL_BAR: 
            case TinyPane.STATIC_BAR:
            case TinyPane.SMALL_BAR:
            case TinyPane.HEADER_BAR: 
            case TinyPane.NO_BAR:
                titleBarType = type;
                return true;
        }
        return false;
    }
    
    /**
     * Returns the bounds of the area, within the internal desktop, which is
     * occupied by the contained components.
     * 
     * @return See above.
     */
    Rectangle getContentsBounds()
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
     * Returns the restore size.
     * 
     * @return See above.
     */
    Dimension getRestoreSize() { return restoreSize; }
    
    /** 
     * Sets the restore size.
     * 
     * @param rs The restore size.  Mustn't be <code>null</code>. 
     */
    void setRestoreSize(Dimension rs) 
    { 
        if (rs == null) throw new NullPointerException("No restore size.");
        this.restoreSize = rs;
    }
    
    /**
     * Returns the frame's internal desktop.
     * 
     * @return See above.
     */
    JComponent getDesktopPane() { return desktopPane; }
    
    /**
     * Returns the highlight color. 
     * 
     * @return See aoove.
     */
    Color getHighlight() { return highlight; }
    
    /**
     * Sets the highlight color. 
     * 
     * @param highlight The color to set.
     */
    void setHighlight(Color highlight) { this.highlight = highlight; }
    
    /**
     * Returns the collapsed state.
     * 
     * @return See above.
     */
    boolean isCollapsed() { return collapsed; }
    
    /**
     * Sets the collapsed state. 
     * 
     * @param collapsed The state to set.
     */
    void setCollapsed(boolean collapsed) { this.collapsed = collapsed; }
    
    /**
     * Sets the resizable state.
     * 
     * @param resizable The state to set.
     */
    void setResizable(boolean resizable) { this.resizable = resizable; }
    
    /**
     * Returns the resizable state.
     * 
     * @return See above.
     */
    boolean isResizable() { return resizable; }
    
    /**
     * Sets the frame's icon.
     * 
     * @param frameIcon The icon to set.
     */
    void setFrameIcon(Icon frameIcon) { this.frameIcon = frameIcon; }
    
    /**
     * Returns the frame's icon.
     * 
     * @return See above.
     */
    Icon getFrameIcon() { return frameIcon; }
    
    /**
     * Installs a border listener if <code>true</code>, removes
     * it if <code>false</code>.
     * 
     * @param listenToBorder The border listener flag.
     */
    void setListenToBorder(boolean listenToBorder)
    { 
        this.listenToBorder = listenToBorder;
    }
    
    /**
     * Returns the border listener flag.
     * 
     * @return See below.
     */
    boolean isListenToBorder() { return listenToBorder; }

    /**
     * Returns the frame's container.
     * 
     * @return See above.
     */
    public Container getContentPane() { return contentPane; }
    
}
