/*
 * org.openmicroscopy.shoola.env.ui.tdialog.TinyWindow
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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies

/** 
 *  A tiny-looking non-modal and non-resizable JDialog without decoration.
 * <p>This window has a small title bar and an JComponent to display the 
 * image.
 * <p>The window behaves mostly like a regular window, but has a close and
 * sizing button allowing to collapse/expand the window contents.</p>
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
public class TinyDialog
    extends JDialog
{
    
    /** Bound property name indicating if the window is collapsed. */
    public final static String COLLAPSED_PROPERTY = "collapsed";
    
    /** Bound property name indicating if the window is closed. */
    public final static String CLOSED_PROPERTY = "closed";
     
    /** Bound property name indicating if the window's title has changed. */
    public final static String TITLE_PROPERTY = "title";
    
    /** The size of the frame before the last collapse request. */
    private Dimension       restoreSize;
    
    /** The View component that renders this frame. */
    protected TinyDialogUI  uiDelegate;
    
    /** The Controller component that renders this frame. */
    protected DialogControl controller;
    
    /** Tells if this window is expanded or collapsed. */
    private boolean         collapsed;
    
    /** Tells if this window is closed or not. */
    private boolean         closed;
    
    /** Tells if the close button is displayed. */
    private boolean         closedButton;
    
    /** The title displayed in this window's title bar. */
    protected String        title;
    
    /** Sets the property of the dialog window. */ 
    private void setProperties()
    {
        setModal(false);
        setResizable(false);
        setUndecorated(true);
        setRestoreSize(new Dimension(getWidth(), getHeight()));
    }
    
    /**
     * Sets the size to used to restore the size of the component
     * when the frame is expanded.
     *  
     * @param d The size to set.
     */
    void setRestoreSize(Dimension d) { restoreSize = d; }
    
    /**
     * Returns the size used to restore the size of the component
     * when the frame is expanded.
     * 
     * @return See above.
     */
    Dimension getRestoreSize() { return restoreSize; }
    
    /**
     * Returns <code>true</code> if the close button is shown, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean hasClosedButton() { return closedButton; }
    
    /**
     * Creates a new window with the specified owner frame.
     * 
     * @param owner The parent's of the window. Mustn't be <code>null</code>.
     * @param image The bufferedImage to display. Mustn't be <code>null</code>.
     */
    public TinyDialog(Frame owner, BufferedImage image)
    { 
        this(owner, image, null);
    }
    
    /**
     * Creates a new window with the specified owner frame.
     * 
     * @param owner The parent's of the window. Mustn't be <code>null</code>.
     * @param image The bufferedImage to display. Mustn't be <code>null</code>.
     * @param title The window's title.
     */
    public TinyDialog(Frame owner, BufferedImage image, String title)
    {
        super(owner);
        setProperties();
        if (owner == null) throw new NullPointerException("No owner.");
        if (image == null) throw new NullPointerException("No image.");
        this.title = title;
        closedButton = true;
        //Create the View and the Controller.
        uiDelegate = new TinyDialogUI(this, image);
        controller = new DialogControl(this, uiDelegate);
    }
    
    /**
     * Creates a new window with the specified owner frame.
     * 
     * @param owner The parent's of the window. Mustn't be <code>null</code>.
     * @param c     The component to display. Mustn't be <code>null</code>.
     */
    public TinyDialog(Frame owner, JComponent c)
    {
        this(owner, c, null);
    }
    
    /**
     * Creates a new window with the specified owner frame.
     * 
     * @param owner The parent's of the window. Mustn't be <code>null</code>.
     * @param c     The component to display. Mustn't be <code>null</code>.
     * @param title The window's title.
     */
    public TinyDialog(Frame owner, JComponent c, String title)
    {
        super(owner);
        if (owner == null) throw new NullPointerException("No owner.");
        this.title = title;
        closedButton = true;
        //Create the View and the Controller.
        if (c == null) uiDelegate = new TinyDialogUI(this);
        else uiDelegate = new TinyDialogUI(this, c);
        controller = new DialogControl(this, uiDelegate);
        setProperties();
    }
    
    /**
     * Creates a new window with the specified owner frame.
     * 
     * @param owner         The parent's of the window.
     *                      Mustn't be <code>null</code>.
     * @param title         The window's title.
     * @param closedButton  Passed <code>true</code> if the  closeButton
     *                      is shown, <code>false</code> otherwise.
     */
    public TinyDialog(Frame owner, String title, boolean closedButton)
    {
        super(owner);
        this.title = title;
        this.closedButton = closedButton;
        if (owner == null) throw new NullPointerException("No owner.");
        uiDelegate = new TinyDialogUI(this);
        controller = new DialogControl(this, uiDelegate);
        setProperties();
    }
    
    /** Moves the window to the Front. */
    public void moveToFront() { setVisible(true); }
    
    /** 
     * Moves the window to the Front and sets the location.
     * 
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public void moveToFront(int x, int y)
    {
        setLocation(x, y);
        setVisible(true);
    }
    
    /**
     * Moves the window and sets the location.
     * 
     * @param p The new location.
     */
    public void moveToFront(Point p) { moveToFront(p.x, p.y); }
    
    /**
     * Returns the title of the <code>TinyWindow</code>.
     *
     * @return a <code>String</code> containing this window's title
     * @see #setTitle
     */
    public String getTitle() { return title; }
    
    /** 
     * Sets the <code>TinyWindow</code> title. The <code>title</code>
     * may have a <code>null</code> value.
     * @see #getTitle
     *
     * @param title The <code>String</code> to display in the title bar.
     */
    public void setTitle(String title)
    {
        String oldValue = this.title;
        this.title = title;
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
        if (b == collapsed) return;  //We're already in the requested state.
        if (!collapsed)
            setRestoreSize(new Dimension(getWidth(), getHeight()));
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
     * Closes or opens this window depending on the passed value.
     * This is a bound property; property change listeners are notified
     * of any change to this property. 
     * 
     * @param b Pass <code>true</code> to close, <code>false</code> otherwise.
     */
    public void setClosed(boolean b)
    {
        if (b == closed) return;  //We're already in the requested state.
        //Fire the state change.
        Boolean oldValue = closed ? Boolean.TRUE : Boolean.FALSE,
                newValue = b ? Boolean.TRUE : Boolean.FALSE;
        closed = b;
        firePropertyChange(CLOSED_PROPERTY, oldValue, newValue);
    }
    
    /** 
     * Tells if this window is closed or not.
     * 
     * @return <code>true</code> if closed, <code>false</code> otherwise. 
     */
    public boolean isClosed() { return closed; }
    
    /** Closes and disposes. */
    public void closeWindow() { uiDelegate.updateClosedState(); }
    
    /** 
     * Overrides the method. 
     * If the specified flag is <code>true</code>, we attach a 
     * {@link BorderListener}.
     * @see JDialog#setResizable(boolean)
     */
    public void setResizable(boolean b)
    {
        super.setResizable(b);
        if (b)
            getRootPane().addMouseMotionListener(new BorderListener(this));
    }
    
    /** 
     * Overrides the method to make sure that we have no decoration. 
     * @see JDialog#setUndecorated(boolean)
     */
    public void setUndecorated(boolean b)
    {
        super.setUndecorated(true);
    }



}
