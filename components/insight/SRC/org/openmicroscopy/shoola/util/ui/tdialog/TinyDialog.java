/*
 * org.openmicroscopy.shoola.util.ui.tdialog.TinyWindow
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui.tdialog;


//Java imports
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies

/** 
 * A tiny-looking non-modal and non-resizable JDialog without decoration.
 * <p>This window has a small title bar and an JComponent to display the 
 * image.
 * <p>The window behaves mostly like a regular window, but has a close and
 * sizing button allowing to collapse/expand the window contents.</p>
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class TinyDialog
    extends JDialog
{

    /** Bound property name indicating if the window is collapsed. */
    public final static String COLLAPSED_PROPERTY = "collapsed";

    /** Bound property name indicating if the window is closed. */
    public final static String CLOSED_PROPERTY = "closedDialog";

    /** Bound property name indicating if the window's title has changed. */
    public final static String TITLE_PROPERTY = "title";

    /** Indicates to show both the close and size buttons. */
    public final static int BOTH = 0;

    /** Indicates to only show the close button. */
    public final static int CLOSE_ONLY = 1;

    /** Indicates to only show the size button. */
    public final static int SIZE_ONLY = 2;

    /** Indicates to only show the size button. */
    public final static int NO_BUTTON = 3;

    /** The minimum magnification value. */
    final static int MINIMUM_ZOOM = 1;

    /** The maximum magnification value. */
    final static int MAXIMUM_ZOOM = 2;

    /** The size of the frame before the last collapse request. */
    private Dimension restoreSize;

    /** The View component that renders this frame. */
    protected TinyDialogUI uiDelegate;

    /** The Controller component that renders this frame. */
    protected DialogControl controller;

    /** Tells if this window is expanded or collapsed. */
    private boolean collapsed;

    /** Tells if this window is closed or not. */
    private boolean closed;

    /** The image to display. */
    private BufferedImage originalImage;

    /** The magnification factor. */
    private float zoomFactor;

    /** 
     * One of the following: {@link #BOTH}, {@link #CLOSE_ONLY} or 
     * {@link #SIZE_ONLY}.
     */
    private int buttonIndex;

    /** The title displayed in this window's title bar. */
    protected String title;

    /** Sets the property of the dialog window. */ 
    private void setProperties()
    {
        setModal(false);
        setResizable(false);
        setUndecorated(true);
        setRestoreSize(new Dimension(getWidth(), getHeight()));
        zoomFactor = MINIMUM_ZOOM;
        addWindowFocusListener(new WindowFocusListener() {

            /**
             * Closes the dialog when the window loses focus.
             * 
             * @see WindowFocusListener#windowLostFocus(WindowEvent)
             */
            public void windowLostFocus(WindowEvent evt) {
                TinyDialog d = (TinyDialog) evt.getSource();
                d.setClosed(true);
                d.closeWindow();
            }

            /**
             * Required by the I/F but no-operation in our case.
             * 
             * @see WindowFocusListener#windowGainedFocus(WindowEvent)
             */
            public void windowGainedFocus(WindowEvent evt) {
            }
        });
    }

    /** 
     * Returns the original image to display.
     * 
     * @return See above.
     */
    BufferedImage getOriginalImage() { return originalImage; }

    /**
     * Returns the magnification factor.
     * 
     * @return See above.
     */
    float getZoomFactor() { return zoomFactor; }

    /**
     * Sets the magnification factor.
     * 
     * @param v The value to set.
     */
    void setZoomFactor(float v) { zoomFactor = v; }

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
     * Returns the type of buttons to display in the tool bar.
     * 
     * @return See above.
     */
    int getButtonIndex() { return buttonIndex; }

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
        if (image == null) throw new NullPointerException("No image.");
        this.title = title;
        originalImage = image;
        zoomFactor = MINIMUM_ZOOM;
        buttonIndex = BOTH;
        //Create the View and the Controller.
        uiDelegate = new TinyDialogUI(this, image);
        controller = new DialogControl(this, uiDelegate);
        uiDelegate.attachMouseWheelListener(controller);
    }

    /**
     * Creates a new window with the specified owner frame.
     * 
     * @param owner The parent's of the window. Mustn't be <code>null</code>.
     * @param c The component to display. Mustn't be <code>null</code>.
     */
    public TinyDialog(Frame owner, JComponent c)
    {
        this(owner, c, null);
    }

    /**
     * Creates a new window with the specified owner frame.
     * 
     * @param owner The parent's of the window. Mustn't be <code>null</code>.
     * @param c The component to display. Mustn't be <code>null</code>.
     * @param index The type of button to display in the tool bar.
     */
    public TinyDialog(Frame owner, JComponent c, int index)
    {
        this(owner, c, index, null);
    }

    /**
     * Creates a new window with the specified owner frame.
     * 
     * @param owner The parent's of the window. Mustn't be <code>null</code>.
     * @param c The component to display. Mustn't be <code>null</code>.
     * @param index The type of button to display in the tool bar.
     * @param title The window's title.
     */
    public TinyDialog(Frame owner, JComponent c, int index, String title)
    {
        super(owner);
        //if (owner == null) throw new NullPointerException("No owner.");
        this.title = title;
        buttonIndex = index;
        //Create the View and the Controller.
        if (c == null) uiDelegate = new TinyDialogUI(this);
        else uiDelegate = new TinyDialogUI(this, c);
        controller = new DialogControl(this, uiDelegate);
        setProperties();
    }

    /**
     * Creates a new window with the specified owner frame.
     * 
     * @param owner The parent's of the window. Mustn't be <code>null</code>.
     * @param c The component to display. Mustn't be <code>null</code>.
     * @param title The window's title.
     */
    public TinyDialog(Frame owner, JComponent c, String title)
    {
        this(owner, c, BOTH, title);
    }

    /**
     * Creates a new window with the specified owner frame.
     * 
     * @param owner The parent's of the window. Mustn't be <code>null</code>.
     * @param title The window's title.
     * @param index The type of button to display in the tool bar.
     */
    public TinyDialog(Frame owner, String title, int index)
    {
        super(owner);
        this.title = title;
        this.buttonIndex = index;
        if (owner == null) throw new NullPointerException("No owner.");
        uiDelegate = new TinyDialogUI(this);
        controller = new DialogControl(this, uiDelegate);
        setProperties();
    }

    /**
     * Returns the canvas.
     * 
     * @return See above.
     */
    public JComponent getCanvas() { return uiDelegate.getCanvas(); }

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
     * Sets the node's decoration.
     * 
     * @param l The collection of <code>component</code>s to add to the
     * 			<code>TitleBar</code>.
     */
    public void setDecoration(List l)
    {
        if (uiDelegate == null) return;
        uiDelegate.setDecoration(l);
    }

    /** 
     * Sets the canvas.
     * 
     * @param c The component to set.
     */
    public void setCanvas(JComponent c)
    {
        if (uiDelegate == null || c == null) return;
        uiDelegate.setCanvas(c);
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
     * Modifies the style of the font of the title.
     * 
     * @param style The style to set.
     */
    public void setFontTitleStyle(int style)
    { 
        uiDelegate.setFontStyle(style);
    }

    /** 
     * Overrides the method.
     * If the specified flag is <code>true</code>, we attach a 
     * {@link BorderListener}.
     * @see JDialog#setResizable(boolean)
     */
    public void setResizable(boolean b)
    {
        super.setResizable(b);
        if (b) {
            BorderListener l = new BorderListener(this);
            getRootPane().addMouseMotionListener(l);
            getRootPane().addMouseListener(l);
            //increase border.
            uiDelegate.makeBorders(4);
        }
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
