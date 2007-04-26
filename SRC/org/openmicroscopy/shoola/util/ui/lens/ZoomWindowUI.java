/*
 * org.openmicroscopy.shoola.util.ui.lens.ZoomWindowUI.java
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.lens;

//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

//Third-party libraries

//Application-internal dependencies

/** 
 * The ZoomWindowUI is the dialog box used to display the zoomed image. It 
 * contains the zoomPanel which displays the zoomed image and the coordinates 
 * of the lens.  
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME2.2
 */
class ZoomWindowUI 
	extends	JDialog 
{

	/** The default size of the window. */
	private static final Dimension	DEFAULT_SIZE = new Dimension(300, 300);
	
	/** The default location of the window. */
	private static final Point		DEFAULT_LOC = new Point(900, 200);
	
	/** Panel holding the zoomed Image */
	private ZoomPanel          zoomPanel;
	
	/** ScrollPane which will contain the zoomPanel which in turn holds the 
	 * zoomed image.
	 */
	private JScrollPane        scrollPanel;
	
	/** The statusPanel shows the position, size and zoomFactor of the lens. */
	private StatusPanel        statusPanel;
	
	/** Parent component of the lens and zoomWindowUI. */
	private LensComponent      lensComponent;
	
	/** Lens menu bar. */
	private JMenuBar			menu;
	
	/** Lens options popup menu. */
	private LensMenu			lensMenu;
	
	/** Close the window and post message to. */
	private void close()
	{
		lensComponent.zoomWindowClosed();
	}
	
	/** 
     * Creates a new instance. 
     * 
     * @param parent        The parent of the dialog.
	 * @param lensComponent The parent component of the control. 
	 * 						Mustn't be <code>null</code>.
	 */
	ZoomWindowUI(JFrame parent, LensComponent lensComponent)
	{
		super(parent);
		if (lensComponent == null)
			throw new IllegalArgumentException("No parent.");
		this.lensComponent = lensComponent;
		setTitle("Zoom Window");
		setSize(DEFAULT_SIZE);
		setLocation(DEFAULT_LOC);
		//setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener( new WindowAdapter() {
			public void windowClosing(WindowEvent e) { close(); }
		});
		
		zoomPanel = new ZoomPanel();
		scrollPanel = new JScrollPane(zoomPanel);
		statusPanel = new StatusPanel();
		this.setLayout(new BorderLayout());
		this.add(scrollPanel, BorderLayout.CENTER);
		this.add(statusPanel, BorderLayout.SOUTH);
		lensMenu =  new LensMenu(lensComponent);
		menu = lensMenu.getMenubar();
		this.setJMenuBar(menu);
	}
	
	/** 
	 * Sets the size of the zoomedImage panel, called after the zoomfactor
	 * or lens has chaned. 
	 * 
	 * @param w The new width.
	 * @param h The new height.
	 */
	void setZoomedImageSize(int w, int h)
	{
		zoomPanel.setSize(new Dimension(w, h));
		zoomPanel.setPreferredSize(new Dimension(w, h));
		zoomPanel.setMinimumSize(new Dimension(w, h));
		zoomPanel.setMaximumSize(new Dimension(w, h));
		JViewport currentView = scrollPanel.getViewport();
        int viewportW = currentView.getWidth();
	    int viewportH = currentView.getHeight();
	    int x = w/2-viewportW/2;
	    if (x<0) x = 0;
	    int y = h/2-viewportH/2;
	    if (y<0) y = 0;
	        
	    currentView.setViewPosition(new Point(x, y));
	}
	
	/**
	 * Displays in pixels if <code>true</code> or in microns otherwise.
	 * 
	 * @param b see above.
	 */
	void setDisplayInPixels(boolean b)
	{
		statusPanel.setDisplayInPixels(b);
		statusPanel.repaint();
	}

	/**
	 * Sets the mapping from pixel size to microns along the x and y axis.
     *  
	 * @param x mapping in x axis.
	 * @param y mapping in y axis.
	 */
	void setXYPixelMicron(float x, float y)
	{
		statusPanel.setXYPixelMicron(x, y);
		statusPanel.repaint();
	}
	
	/** 
     * Sets the XY values of the lens position text.
	 * 
	 * @param x See above.
	 * @param y See above.
	 */
	void setLensXY(int x,int y) { statusPanel.setLensXY(x, y); }
	
	/** 
     * Sets the W, H values of the lens width, Height text.
	 * 
	 * @param w See above.
	 * @param h See above.
	 */
	void setLensWidthHeight(int w, int h)
	{
		statusPanel.setLensWidthHeight(w, h);
	}
		
	/** 
     * Sets the zoomFactor of the lens.
	 * 
	 * @param zoomFactor See above.
	 */
	void setLensZoomFactor(float zoomFactor)
	{
		statusPanel.setLensZoomFactor(zoomFactor);
	}
	
	/**
	 * Sets the image shown on the zoomWindow.
	 * 
	 * @param zoomImage See above.
	 */
	void setZoomImage(BufferedImage zoomImage)
	{
		zoomPanel.setZoomImage(zoomImage);
	}
	
}
