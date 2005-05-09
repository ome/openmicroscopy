/*
 * org.openmicroscopy.shoola.agents.hiviewer.tframe.TinyFrameModel
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
import javax.swing.JDesktopPane;

//Third-party libraries

//Application-internal dependencies

/** 
 * The Model sub-component in the {@link TinyFrame}'s MVC triad.
 * This is a presentation Model which holds and manages presentation data
 * in behalf of the {@link TinyFrame}, which has pass-through methods that
 * delegate to this class, but manage the change notification process.  So
 * all the other sub-components regard the {@link TinyFrame} as the Model.
 * 
 * <p><i>Note</i>: Not all presentation data is held by this class; for example
 * the frame's title and icon are part of the <code>JInternalFrame</code> from
 * which the {@link TinyFrame} inherits.  This is unavoidable because the 
 * <code>JInternalFrame</code> doesn't have a separate Model from which we can
 * inherit and, to keep things more manageable, we don't want to collapse the
 * Model into the {@link TinyFrame}.</p> 
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
class TinyFrameModel
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
    
    /** Substitutes the original content pane, all contents are added here. */
    private JDesktopPane    desktopPane;
    
    /** Tells if the frame is in single or multi-view mode. */
    private boolean         singleViewMode;
    
    /** Replaces the {@link #desktopPane} when we're in single-view mode. */
    private JDesktopPane    singleViewDesktop;
    
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
     */
    private void setParentSizeForSingleView(Container parent)
    {
        if (parent instanceof TinyFrame) {
            
            Dimension d = ((TinyFrame) parent).getTitleBar().getPreferredSize();
            Dimension dDesktop = singleViewDesktop.getPreferredSize();
            parent.setSize(d.width+dDesktop.width, d.height+dDesktop.height+4);
        } else setParentSizeForSingleView(parent.getParent());
    }
    
    /** 
     * Sets the size of the container when we display
     * the children in multi-view mode.
     */
    private void setParentSizeForMultiView(Container parent)
    {
        if (parent instanceof TinyFrame)  
            parent.setSize(parent.getPreferredSize());
        else setParentSizeForMultiView(parent.getParent());
    }
    
    /**
     * Creates a new instance.
     * After creation the model is not collapsed, the title bar is the 
     * {@link TinyFrame#FULL_BAR}, and there's no highlight.
     * However, the internal desktop is not installed yet and you need
     * to call {@link #setSingleViewMode(boolean, TinyFrameUI)} passing
     * <code>false</code> to complete intialization.  At which point, the
     * model will be in multi-view mode and the internal desktop will be 
     * installed.  
     * 
     * @param contentPane The frame's content pane.  
     *                    Mustn't be <code>null</code>.
     * @param restoreSize The initial frame's size.
     *                    Mustn't be <code>null</code>.
     * @param uiDelegate The frame's View.  We need it here to decorate the
     *                   desktops.  Mustn't be <code>null</code>.
     */
    //TODO: The ugly call to setSingleViewMode() after creation is needed
    //to avoid circular dependencies.  In fact, that call use to be in this
    //constructor, but then b/c the uiDelegate is required we had to strip
    //it out.  Reason? Simple, the uiDelegate needs to access the Model at
    //creation time and the Model (TinyFrame) just forwards calls to this
    //object, which doesn't exist yet.  Pleeeaze come up with a better
    //solution!
    TinyFrameModel(Container contentPane, Dimension restoreSize)
    {
        if (contentPane == null) 
            throw new NullPointerException("No content pane.");
        this.contentPane = contentPane;
        setRestoreSize(restoreSize);
        desktopPane = new JDesktopPane();
        setTitleBarType(TinyFrame.FULL_BAR);
    }
    
    /** Tells if we're in single or multi-view mode. */
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
    void setSingleViewMode(boolean singleViewMode, TinyFrameUI uiDelegate)
    {
        if (uiDelegate == null) 
            throw new NullPointerException("No UI delegate.");
        this.singleViewMode = singleViewMode;
        if (singleViewMode) {  //Create the single-view tmp desktop.
            singleViewDesktop = new JDesktopPane();
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
            if (oldChild instanceof TinyFrame) {
                TinyFrame tf = (TinyFrame) oldChild;
                tf.setTitleBarType(childViewTitleBarType);
                tf.setCollapsed(childViewCollapsed);
                if (child == null) setParentSizeForMultiView(tf.getParent());
            }
        }
        if (isChild(child)) {  //We've got a new child to display.
            desktopPane.remove(child);
            singleViewDesktop.add(child);
            childViewBounds = child.getBounds();
            singleViewDesktop.setPreferredSize(
                    new Dimension(childViewBounds.width, childViewBounds.height)
                    );
            child.setLocation(2, 2);
            child.setSize(child.getPreferredSize());
            
            if (child instanceof TinyFrame) {
                TinyFrame tf = (TinyFrame) child;
                childViewTitleBarType = tf.getTitleBarType();
                childViewCollapsed = tf.isCollapsed();
                tf.setTitleBarType(TinyFrame.HEADER_BAR);
                tf.setCollapsed(false);
                setParentSizeForSingleView(tf.getParent());
            }
        }
        contentPane.validate();
        contentPane.repaint();
    }
    

    /** 
     * Returns the title bar type.
     * 
     * @return One of the constants defined by {@link TinyFrame}. 
     */
    int getTitleBarType() { return titleBarType; }
    
    /**
     * Sets the title bar type.
     * Does nothing if <code>type</code> is not a valid one.
     * 
     * @param type One of the constants defined by {@link TinyFrame}.
     * @return <code>true</code> if <code>type</code> is a valid flag and so
     *         it has been set; <code>false</code> if <code>type</code> is
     *         not a valid flag and so it has been ignored.
     */
    boolean setTitleBarType(int type)
    {
        switch (type) {
            case TinyFrame.FULL_BAR:
            case TinyFrame.SMALL_BAR:
            case TinyFrame.HEADER_BAR: 
            case TinyFrame.STATIC_BAR:
            case TinyFrame.NO_BAR:
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
        if (singleViewMode) {  
            //Then one of the desktopPane's components could
            //be in the tmp single-view desktop.
            comp = singleViewDesktop.getComponents();
            if (comp.length != 0) {
                x = Math.min(x, childViewBounds.x);
                y = Math.min(y, childViewBounds.y);
                br_x = Math.max(br_x, childViewBounds.x+childViewBounds.width);
                br_y = Math.max(br_y, childViewBounds.y+childViewBounds.height);
            }
        }
        return new Rectangle(x, y, br_x-x, br_y-y);
    }
    
    /** Returns the restore size. */
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
    
    /** Returns the frame's internal desktop. */
    JDesktopPane getDesktopPane() { return desktopPane; }
    
    /** Returns the highlight property. */
    Color getHighlight() { return highlight; }
    
    /** Sets the highlight property. */
    void setHighlight(Color highlight) { this.highlight = highlight; }
    
    /** Returns the collapsed state. */
    boolean isCollapsed() { return collapsed; }
    
    /** Sets the collapsed state. */
    void setCollapsed(boolean collapsed) { this.collapsed = collapsed; }
    
}
