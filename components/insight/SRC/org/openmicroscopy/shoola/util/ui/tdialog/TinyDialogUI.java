/*
 * org.openmicroscopy.shoola.util.ui.tdialog.TinyWindowUI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.List;

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
 * (<b>Internal version:</b> $Revision: 4724 $ $Date: 2007-01-17 08:46:48 +0000 (Wed, 17 Jan 2007) $)
 * </small>
 * @since OME2.2
 */
public class TinyDialogUI
{
    
    /** The maximum of width of the window. */
    public static final int MAX_WIDTH = 300;
    
    /** The maximum of height of the window. */
    public static final int MAX_HEIGHT = 300;
    
    /** The thickness of the frame's border. */
    static final int        BORDER_THICKNESS = 2;

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

    /** The inner padding value. */
    static final int        INNER_PADDING = 1;
    
    /** The window that owns this UI delegate. */
    private TinyDialog      window;
    
    /** The component that draws the window's title bar. */
    private TitleBar        titleBar;  
    
    /** The component that displays the image. */
    private JComponent      canvas;
      
    /** Creates and sets the window and its content's borders. */
    void makeBorders(int increment)
    {
        JRootPane rootPane = window.getRootPane();
        rootPane.setBorder(
               BorderFactory.createLineBorder(BORDER_COLOR,
                       BORDER_THICKNESS+increment));
        if (canvas != null) {
            canvas.setBorder(BorderFactory.createBevelBorder(
                    BevelBorder.LOWERED, INNER_BORDER_HIGHLIGHT, 
                    INNER_BORDER_SHADOW)); 
        }
    }
    
    /** 
     * Sets the size and preferred size of the canvas. 
     * 
     * @param w The width of the image.
     * @param h The height of the image.
     */
    private void makeComponentsSize(int w, int h)
    {
        if (canvas == null) return;
        Insets i = canvas.getInsets();
        int width = w+i.right+i.left;
        int height = h+i.top+i.bottom;
        Dimension d = new Dimension(width, height);
        canvas.setPreferredSize(d);
        canvas.setSize(d);
    }
    
    /** Builds and lays out the UI. */ 
    private void buildUI()
    {
        Container container = window.getContentPane();
        container.add(titleBar, BorderLayout.NORTH);
        if (canvas != null) container.add(canvas, BorderLayout.CENTER);
    }
    
    /** 
     * Adds the specified component to the container. 
     * 
     * @param c The component to add.
     */
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
    }

    /** 
     * Attaches the <code>controller</code> and sets an action command
     * to the specified button.
     * 
     * @param controller    An instance of {@link ActionListener}.
     * @param b             The button.
     * @param id            Action command ID.
     */ 
    private void attachButtonListener(ActionListener controller, 
                                        JButton b, int id)
    {
    	if (b == null) return;
        b.addActionListener(controller);
        b.setActionCommand(""+id);
    }
    
    /** 
     * Sets the window and initializes the title bar.
     * 
     * @param window The window to set.
     */
    private void initialize(TinyDialog window)
    {
        if (window == null) throw new NullPointerException("No window.");
        this.window = window;
        titleBar = new TitleBar(window.getTitle(), window.getButtonIndex());
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
        canvas.setToolTipText(window.title);
        makeComponentsSize(image.getWidth(), image.getHeight()); 
        makeBorders(0);
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
        makeBorders(0);
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
        makeBorders(0);
        buildUI();
    }
    
    /**
     * Sets the image to paint if the canvas is an instance of 
     * <code>ThumbnailCanvas</code>.
     * 
     * @param image The image to paint.
     */
    void setImage(BufferedImage image)
    {
    	if (canvas instanceof ThumbnailCanvas) {
    		makeComponentsSize(image.getWidth(), image.getHeight()); 
    		((ThumbnailCanvas) canvas).setImage(image);
    		//window.getContentPane().removeAll();
    		//buildUI();
    		window.pack();
    	}
    }
    
    /**
     * Adds a {@link MouseWheelListener} to the canvas if the canvas is 
     * an instance of <code>ThumbnailCanvas</code>.
     * 
     * @param controller The listener to add.
     */
    void attachMouseWheelListener(MouseWheelListener controller)
    {
    	if (canvas instanceof ThumbnailCanvas) 
    		canvas.addMouseWheelListener(controller);
    }
    
    /**
     * Attaches the <code>controller</code> to the buttons of the titleBar.
     * 
     * @param controller An instance of {@link DialogControl}.
     */
    void attachActionListener(ActionListener controller)
    {
        attachButtonListener(controller, 
        					titleBar.getButton(TitleBar.SIZE_BUTTON), 
                            DialogControl.SIZE);
        attachButtonListener(controller, 
        					titleBar.getButton(TitleBar.CLOSE_BUTTON), 
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
    	SizeButton button = 
    			(SizeButton) titleBar.getButton(TitleBar.SIZE_BUTTON);
        if (window.isCollapsed()) {
            removeComponent(canvas);
            Dimension d = new Dimension(window.getWidth(), 
                    TitleBar.HEIGHT+2*BORDER_THICKNESS);
            titleBar.setPreferredSize(d);
            button.setActionType(SizeButton.EXPAND);
            window.setSize(d.width, d.height);
        } else {
            addComponent(canvas);
            button.setActionType(SizeButton.COLLAPSE);
            Dimension dT = titleBar.getPreferredSize();
            Dimension dW = window.getRestoreSize();
            window.setSize(dT.width, dW.height);
        }
        window.validate();
        window.repaint();
    }

    /** Hides and disposes depending on the state of the window. */
    void updateClosedState()
    {
        if (window.isClosed()) {
            window.setVisible(false);
            window.dispose();
        }
    }
     
    /**
     * Sets the node's decoration.
     * 
     * @param l The collection of <code>component</code>s to add to the
     * 			<code>TitleBar</code>.
     */
	void setDecoration(List l)
	{
		if (titleBar == null) return;
		titleBar.setDecoration(l, window.getButtonIndex());
	}
	
    /** 
     * Sets the canvas. 
     * 
     * @param c The component to set.
     */
    void setCanvas(JComponent c)
    { 
        canvas = c;
        addComponent(canvas);
    }
    
    /**
     * Returns the canvas.
     * 
     * @return See above.
     */
    JComponent getCanvas() { return canvas; }
    
    /**
     * Derives the default font of the title bar.
     * 
     * @param style The new style to set.
     */
    void setFontStyle(int style) { titleBar.setFontStyle(style); }
    
    /**
     * Attaches the <code>controller</code> to the buttons of the titleBar.
     * 
     * @param controller An instance of {@link DialogControl}.
     */
    public void attachMouseListener(MouseListener controller)
    {
        titleBar.addMouseListener(controller);
        if (canvas instanceof ThumbnailCanvas) 
        	canvas.addMouseListener(controller);
    }
    
}
