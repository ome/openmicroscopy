/*
 * org.openmicroscopy.shoola.env.ui.tdialog.TinyWindowUI
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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.border.BevelBorder;

//Third-party libraries

//Application-internal dependencies

/** 
 * The UI delegate for the {@link TinyDialog}.
 * A delegate can't be shared among different instances of {@link TinyDialog} 
 * and has a life-time dependency with its owning frame.
 *
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
public class TinyDialogUI
{
    
    public static final int MAX_WIDTH = 300;
    
    public static final int MAX_HEIGHT = 300;
    
    /** The thickness of the frame's border. */
    static final int        BORDER_THICKNESS = 1;

    /** The color of the frame's border. */
    static final Color      BORDER_COLOR = new Color(99, 130, 191);
    
    /** 
     * The highlight color to use for the inner border surrounding the
     * frame's contents.
     */
    static final Color      INNER_BORDER_HIGHLIGHT = new Color(240, 240, 240);
    
    /** 
     * The shadow color to use for the inner border surrounding the
     * frame's contents.
     */
    static final Color      INNER_BORDER_SHADOW = new Color(200, 200, 200);

    static final int        INNER_PADDING = 1;
    
    /** The window that owns this UI delegate. */
    private TinyDialog      window;
    
    /** The component that draws the window's title bar. */
    private TitleBar        titleBar;  
    
    /** The component that displays the image. */
    private JComponent      canvas;
    
    
    /**
     * Creates and sets the window and its content's borders.
     */
    private void makeBorders()
    {
        JRootPane rootPane = window.getRootPane();
        rootPane.setBorder(
               BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS));
        if (canvas != null) {
            canvas.setBorder(BorderFactory.createBevelBorder(
                    BevelBorder.LOWERED, INNER_BORDER_HIGHLIGHT, 
                    INNER_BORDER_SHADOW)); 
        }
    }
    
    /** Set the size to the content. */
    private void makeComponentsSize(BufferedImage image)
    {
        if (canvas == null) return;
        Insets i = canvas.getInsets();
        int width = image.getWidth()+i.right+i.left;
        int height = image.getHeight()+i.top+i.bottom;
        Dimension d = new Dimension(width, height);
        canvas.setPreferredSize(d);
        canvas.setSize(d);
    }
    
    /** Build and lay out the UI. */ 
    private void buildUI()
    {
        Container container = window.getContentPane();
        container.add(titleBar, BorderLayout.NORTH);
        if (canvas != null) container.add(canvas, BorderLayout.CENTER);
    }
    
    /** Adds the specified component to the container. */
    private void addComponent(JComponent c)
    {
        Container container = window.getContentPane();
        Component[] comps = container.getComponents();
        boolean in = false;
        for (int i = 0; i < comps.length; i++) {
            if (comps[i].equals(c)) {
                in = true;
                break;
            }
        } 
        if (!in) {
            container.add(c, BorderLayout.CENTER);
            window.repaint();
        }
    }
    
    /** 
     * Removes the specified component from the container.
     * 
     * @param c The component to remove.
     */
    private void removeComponent(JComponent c)
    {
        window.getContentPane().remove(c);
        //window.repaint();
    }

    /** 
     * Attaches the <code>controller</code> and sets an action command
     * to the specified button.
     * 
     * @param controller    An instance of {@link FrameControl}.
     * @param b             The button.
     * @param id            Action command ID.
     */ 
    private void attachButtonListener(ActionListener controller, 
            JButton b, int id)
    {
        b.addActionListener(controller);
        b.setActionCommand(""+id);
    }
    
    /** Set the window and initializes the TitleBar. */
    private void initialize(TinyDialog window)
    {
        if (window == null) throw new NullPointerException("No window.");
        this.window = window;
        titleBar = new TitleBar(window.getTitle());
    }
    
    /**
     * Creates a new UI delegate for the specified <code>window</code>.
     * 
     * @param window    The window that will own this UI delegate. 
     *                  Mustn't be <code>null</code>.
     * @param image     The bufferedImage to display. 
     *                  Mustn't be <code>null</code>.
     */
    TinyDialogUI(TinyDialog window, BufferedImage image)
    {
        initialize(window);
        canvas = new ThumbnailCanvas(image);
        makeComponentsSize(image); 
        makeBorders();
        buildUI();
    }
    
    /**
     * Creates a new UI delegate for the specified <code>window</code>.
     * 
     * @param window    The window that will own this UI delegate. 
     *                  Mustn't be <code>null</code>.
     * @param c         The component to display. 
     *                  Mustn't be <code>null</code>.
     */
    TinyDialogUI(TinyDialog window, JComponent c)
    {
        if (c == null) throw new NullPointerException("No component.");
        initialize(window);
        canvas = c;
        makeBorders();
        buildUI();
    }
    
    /**
     * Creates a new UI delegate for the specified <code>window</code>.
     * 
     * @param window    The window that will own this UI delegate. 
     *                  Mustn't be <code>null</code>.
     */
    TinyDialogUI(TinyDialog window)
    {
        initialize(window);
        makeBorders();
        buildUI();
    }
    
    /**
     * Attaches the <code>controller</code> to the buttons of the titleBar.
     * 
     * @param controller An instance of {@link DialogControl}.
     */
    void attachActionListener(ActionListener controller)
    {
        attachButtonListener(controller, titleBar.sizeButton, 
                            DialogControl.SIZE);
        attachButtonListener(controller, titleBar.closeButton, 
                DialogControl.CLOSE);
    }
    
    /**
     * Attaches the <code>controller</code> to the titleBar.
     * 
     * @param controller An instance of {@link DialogControl}.
     */
    void attachMouseMotionListener(MouseMotionListener controller)
    {
        titleBar.addMouseMotionListener(controller);
    }
    
    /** Updates and repaints the title bar. */
    void updateTitleBar()  { titleBar.update(window.getTitle()); }
    
    /**
     * Repaints the window according to whether the window is currently 
     * collapsed or expanded.
     */
    void updateCollapsedState()
    {
        if (window.isCollapsed()) {
            removeComponent(canvas);
            Dimension d = new Dimension(window.getWidth(), 
                    TitleBar.HEIGHT+2*BORDER_THICKNESS);
            titleBar.setPreferredSize(d);
            titleBar.sizeButton.setActionType(SizeButton.EXPAND);
        } else {
            addComponent(canvas);
            titleBar.sizeButton.setActionType(SizeButton.COLLAPSE);
        }
        Dimension d = window.getContentPane().getPreferredSize();
        Dimension dT = titleBar.getPreferredSize();
        int h = d.height;
        if (h > MAX_HEIGHT) h = MAX_HEIGHT;
        window.setSize(dT.width, h);
        window.validate();
        window.repaint();
    }

    /** 
     * Shows or not the window according to whether the window is currently 
     * closed or opened. 
     */
    void updateClosedState()
    {
        if (window.isClosed()) {
            window.setVisible(false);
            window.dispose();
        }
    }
     
    /** Set the canvas. */
    public void setCanvas(JComponent c)
    { 
        canvas = c;
        addComponent(canvas);
    }
    
    /**
     * Attaches the <code>controller</code> to the buttons of the titleBar.
     * 
     * @param controller An instance of {@link DialogControl}.
     */
    public void attachMouseListener(MouseListener controller)
    {
        titleBar.addMouseListener(controller);
    }
    
}
