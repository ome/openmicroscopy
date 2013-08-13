/*
 * org.openmicroscopy.shoola.util.ui.tpane.TinyPaneUI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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

package org.openmicroscopy.shoola.util.ui.tpane;

//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.DesktopManager;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.basic.BasicPanelUI;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.border.FrameBorder;


/** 
 * The UI delegate for the {@link TinyPane}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 4806 $ $Date: 2007-05-03 13:20:36 +0000 (Thu, 03 May 2007) $)
 * </small>
 * @since OME2.2
 */
class TinyPaneUI
    extends BasicPanelUI
    implements ComponentListener, MouseListener, MouseMotionListener,
    PropertyChangeListener 
{

    /** The margin of the frame's border. */
    public static final int    BORDER_MARGIN = 2;
    
    /** The thickness of the frame's border. */
    public static final int    BORDER_THICKNESS = 1;
    
    /** The thickness of the frame's scrollbars. */
    public static final int    SCROLLBAR_THICKNESS = 8;
    
    /** The color of the frame's border. */
    public static final Color  BORDER_COLOR = new Color(99, 130, 191);
    
    /** 
     * The highlight color to use for the inner border surrounding the
     * frame's contents.
     */
    public static final Color  INNER_BORDER_HIGHLIGHT = 
                                                    new Color(240, 240, 240);
    
    /** 
     * The shadow color to use for the inner border surrounding the
     * frame's contents.
     */
    public static final Color  INNER_BORDER_SHADOW = new Color(200, 200, 200);
    
    /** The color of the desktop pane. */
    public static final Color  DESKTOP_COLOR = new Color(250, 253, 255);
    
    /** The scroll pane that contains the internal desktop. */
    private JScrollPane     dskDecorator;
    
    /** The frame that owns this UI delegate. */
    private TinyPane        frame;
    
    /** The component that draws the frame's title bar. */
    private TitleBar        titleBar;
    
    /** The component that draws the frame's border. */
    private FrameBorder     border;
    
    /** Listener to mouse events. */
    private BorderListener  borderListener;
    
    /** Adds a {@link BorderListener} to the components. */
    private void attachBorderListener()
    {
        frame.addMouseListener(this);
        frame.addMouseMotionListener(this);
        titleBar.addMouseListener(this);
        titleBar.addMouseMotionListener(this);
    }
    
    /** Removes the  {@link BorderListener}. */
    private void detachBorderListener()
    {
        frame.removeMouseListener(this);
        frame.removeMouseMotionListener(this);
        titleBar.removeMouseListener(this);
        titleBar.removeMouseMotionListener(this);
    }
    
    /**
     * Repaints the frame according to whether the frame is currently 
     * collapsed or expanded.
     */
    private void updateCollapsedState()
    {
        if (frame.isCollapsed()) {
            frame.remove(frame.getContentPane());
            int h = titleBar.getPreferredSize().height;
            frame.setSize(frame.getWidth(), h+ 
                    2*(BORDER_THICKNESS+BORDER_MARGIN));  
            //For example, if BORDER_THICKNESS=1, then +2 will make the border 
            //bottom show as the border thickness is 1, so we need 1px at the 
            //top and 1px at the bottom.  (Otherwise the border and the last 
            //px line of the title bar won't show.)
        } else {
            frame.add(frame.getContentPane());
            frame.setSize(frame.getRestoreSize());
            frame.moveToFront();
            frame.repaint();
        }
    }
    
    /**
     * Creates the frame's border.
     * 
     * @return The border to use with this frame.
     */
    protected FrameBorder makeBorder()
    {
        return new FrameBorder(BORDER_COLOR, null, BORDER_MARGIN);
    }
    
    /**
     * Creates a new UI delegate for the specified <code>frame</code>.
     * 
     * @param frame The frame that will own this UI delegate.
     *              Mustn't be <code>null</code>.
     */
    TinyPaneUI(TinyPane frame)
    {
        if (frame == null) throw new NullPointerException("No frame.");
        this.frame = frame;
        borderListener = new BorderListener(frame);
        titleBar = new TitleBar(frame);
        border = makeBorder();
        frame.setBorder(border);
        frame.setOpaque(false);
        frame.addPropertyChangeListener(this);
        frame.addComponentListener(this);
        attachBorderListener();
    }
    
    /**
     * Sets the color of the border.
     * 
     * @param color A color for highlighting or <code>null</code> to
     * restore the normal background.
     */
    void setBorderColor(Color color)
    {
    	border.setBackgroundColor(color);
    }

    /**
     * Decorates the frame's internal desktop with a scroll pane.
     * 
     * @param dp The internal desktop.
     * @return A scroll pane enclosing the <code>JLayeredPane</code>.
     */
    JScrollPane decorateDesktopPane(JLayeredPane dp)
    {
        dp.setOpaque(true);
        dp.setBackground(DESKTOP_COLOR);
        dskDecorator = new JScrollPane(dp);
        dskDecorator.setBorder(BorderFactory.createBevelBorder(
                BevelBorder.LOWERED,
                INNER_BORDER_HIGHLIGHT, INNER_BORDER_SHADOW)); 
        
        //dskDecorator.getHorizontalScrollBar().setPreferredSize(
        //        new Dimension(100, SCROLLBAR_THICKNESS));
        //dskDecorator.getVerticalScrollBar().setPreferredSize(
        //        new Dimension(SCROLLBAR_THICKNESS, 100));
        //NOTE: Makes the scrollars tiny.  100 is arbitrary and shouldn't 
        //matter in the end.  SCROLLBAR_THICKNESS should be respected though.
        
        return dskDecorator;
    }
    
    /** 
     * Returns the scroll pane that contains the internal desktop.
     * 
     * @return See above.
     */
    JScrollPane getDeskDecorator() { return dskDecorator; }
    
    /**
     * Returns the title bar component.
     * 
     * @return See above.
     */
    JComponent getTitleBar() { return titleBar; }
    
    /**
     * Returns the bounds of the icon. Returns a new <code>Rectangle</code>
     * whose top-left corner is at (0,&nbsp;0) in the coordinate space, 
     * and whose width and height are both zero if no icon. 
     * 
     * @return See above.
     */
    Rectangle getFrameIconBounds() { return titleBar.getFrameIconBounds(); }
    
    /**
     * Returns the size this widget should have to fully display the layered 
     * pane at its preferred size.
     * 
     * @return See above.
     */
    Dimension getIdealSize()
    {
        Dimension sz = new Dimension();
        int h = titleBar.getPreferredSize().height;
        JComponent dskDecorator = frame.getInternalDesktop();
        Dimension internalDesktopSz = frame.getContentPane().getPreferredSize();
        Insets scrollPaneInsets = dskDecorator.getInsets(),
                frameInsets = frame.getInsets();
        sz.width = frameInsets.left+scrollPaneInsets.left+
        			internalDesktopSz.width+ 
                    scrollPaneInsets.right+frameInsets.right;
        sz.height = frameInsets.top+scrollPaneInsets.top+
                    internalDesktopSz.height+scrollPaneInsets.bottom+
                    frameInsets.bottom+h;
        return sz;
    }
    
    /**
     * Returns the proper DesktopManager. Calls the {@link TinyPaneUI} to 
     * find and returns the desktopManager
     * 
     * @return See above.
     */
    DesktopManager getDesktopManager()
    {
        return borderListener.getDesktopManager();
    }
    
    /** 
     * Adds a close button to the title bar if the passed value
     * is <code>true</code>, no close button if <code>false</code>.
     * 
     * @param b Pass <code>true</code> to add a close button,
     * 			<code>false</code> to remove the close button.
     */
    void allowClose(boolean b) { titleBar.allowClose(b); }
    
    /** Removes all buttons, added by default, from the tool bar. */
    void clearDefaultButtons() { titleBar.clearDefaultButtons(); }
    
    /**
     * Derives the default font of the title bar.
     * 
     * @param style The new style to set.
     */
    void setFontStyle(int style) { titleBar.setFontStyle(style); }
    
    /**
     * Monitors frame's state changes and updates the UI accordingly.
     * 
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        String propChanged = pce.getPropertyName();
        if (TinyPane.HIGHLIGHT_PROPERTY.equals(propChanged)) {
            border.setBackgroundColor((Color) pce.getNewValue());
            //frame.repaint();
            frame.moveToFront();
        } else if (TinyPane.COLLAPSED_PROPERTY.equals(propChanged)) { 
            updateCollapsedState();
        } else if (TinyPane.TITLEBAR_TYPE_PROPERTY.equals(propChanged)) {
            frame.setSize(frame.getPreferredSize());
            if (frame.getWidth() < TitleBar.MIN_WIDTH)
                frame.setSize(TitleBar.MIN_WIDTH, frame.getHeight());
            frame.validate();
        } else if (TinyPane.BORDER_LISTENER_PROPERTY.equals(propChanged)) {
            boolean b = ((Boolean) pce.getNewValue()).booleanValue();
            if (b) attachBorderListener();
            else detachBorderListener();
        }
    }
    
    /**
     * Resets the inner desktop preferred size so to make sure every
     * contained component will still be reachable after resizing.
     * 
     * @see ComponentListener#componentResized(ComponentEvent)
     */
    public void componentResized(ComponentEvent ce)
    {
        Rectangle b = frame.getContentsBounds();
        Dimension d = new Dimension(b.width, b.height);
        if (b.x < 0) d.width += b.x;
        if (b.y < 0) d.height += b.y;
        frame.getInternalDesktop().setPreferredSize(d);
    }
    
    /** 
     * Repaints the frame whenever it is moved.
     * This is so because the frame doesn't get repainted when is dragged
     * out of the bounds of a desktop.
     * 
     * @see ComponentListener#componentMoved(ComponentEvent)
     */
    public void componentMoved(ComponentEvent ce) { frame.repaint(); }
    
    /** 
     * Required by {@link ComponentListener} I/F but no-op implementation in our
     * case. 
     * 
     * @see ComponentListener#componentHidden(ComponentEvent)
     */
    public void componentHidden(ComponentEvent ce) {}

    /** 
     * Required by {@link ComponentListener} I/F but no-op implementation in our
     * case. 
     * 
     * @see ComponentListener#componentShown(ComponentEvent)
     */
    public void componentShown(ComponentEvent ce) {}

    /** 
     * Forwards <code>mouseClicked</code> event to the {@link BorderListener}.
     * 
     * @see MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent me) { borderListener.mouseClicked(me); }

    /** 
     * Forwards <code>mousePressed</code> event to the {@link BorderListener}.
     * 
     * @see MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent me) { borderListener.mousePressed(me); }

    /** 
     * Forwards <code>mouseReleased</code> event to the {@link BorderListener}.
     * 
     * @see MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent me)
    {
       borderListener.mouseReleased(me);
    }

    /** 
     * Forwards <code>mouseReleased</code> event to the {@link BorderListener}.
     * 
     * @see MouseListener#mouseEntered(MouseEvent)
     */
    public void mouseEntered(MouseEvent me) { borderListener.mouseEntered(me); }

    /** 
     * Forwards <code>mouseExited</code> event to the {@link BorderListener}.
     * 
     * @see MouseListener#mouseExited(MouseEvent)
     */
    public void mouseExited(MouseEvent me) { borderListener.mouseExited(me); }

    /** 
     * Forwards <code>mouseExited</code> event to the {@link BorderListener}.
     * 
     * @see MouseMotionListener#mouseDragged(MouseEvent)
     */
    public void mouseDragged(MouseEvent me) { borderListener.mouseDragged(me); }

    /** 
     * Forwards <code>mouseMoved</code> event to the {@link BorderListener}.
     * 
     * @see MouseMotionListener#mouseMoved(MouseEvent)
     */
    public void mouseMoved(MouseEvent me) { borderListener.mouseMoved(me); }

}
