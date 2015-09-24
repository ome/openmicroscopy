/*
 * org.openmicroscopy.shoola.util.ui.lens.ZoomWindow.java
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;

import ome.model.units.BigResult;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.enums.UnitsLength;

//Third-party libraries

//Application-internal dependencies

/** 
*	ZoomWindow is the component of the zoomWindowUI showing the zoomed version
*	of the lens.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $Date: $)
* </small>
* @since OME2.2
*/
class ZoomWindow
	extends JDialog
	implements ComponentListener
{
	
	/** The default size of the window. */
	static final Dimension	DEFAULT_SIZE = new Dimension(300, 300);
	
	/** The default location of the window. */
	private static final Point		DEFAULT_LOC = new Point(900, 200);
	
	/** The UI which displays the zoomed image. */
	private ZoomWindowUI 	zoomWindowUI;
	
	/** Lens menu bar. */
	private JMenuBar			menu;
	
	/** Lens options popup menu. */
	private LensMenu			lensMenu;
	
    private JLayeredPane        layeredPane;
    
	/** The statusPanel shows the position, size and zoomFactor of the lens. */
	private StatusPanel        statusPanel;
	
	/** Parent component of the lens and zoomWindowUI. */
	private LensComponent      lensComponent;
	
	private void initComponents()
	{
		setTitle("Zoom Window");
		//setSize(DEFAULT_SIZE);
		setLocation(DEFAULT_LOC);
		//setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		addWindowListener( new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{ 
				lensComponent.zoomWindowClosed();
			}
		});
		addComponentListener(this);
		lensMenu = new LensMenu(lensComponent);
		statusPanel = new StatusPanel();
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(zoomWindowUI, BorderLayout.CENTER);
		c.add(statusPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Creates a  new  instance. This creates an instance of the 
	 * ZoomWindowUI(JDialog).
	 * 
	 * @param parent JFrame parent window.  
	 * @param lensComponent The parent component of the ZoomWindow.
	 *
	 */
	ZoomWindow(JFrame parent, LensComponent lensComponent, LensModel model)
	{
		super(parent);
		if (lensComponent == null)
			throw new IllegalArgumentException("No parent.");
		this.lensComponent = lensComponent;
		zoomWindowUI = new ZoomWindowUI(model);
		initComponents();
		buildGUI();
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
	void setXYPixelMicron(Length x, Length y) 
	{
	    try {
            Length x1 = new LengthI(x, UnitsLength.MICROMETER);
            Length y1 = new LengthI(y, UnitsLength.MICROMETER);
            statusPanel.setXYPixelMicron(x1.getValue(), y1.getValue());
        } catch (BigResult e) {
        }
		statusPanel.repaint();
	}
	
	/**
	 * Sets the X,Y coordinates of the lens on the ZoomWindowUI.
	 * 
	 * @param x See above.
	 * @param y See above.
	 */
	void setLensXY(int x, int y) { statusPanel.setLensXY(x, y); }
	
	/**
	 * Sets the w,h size of the lens on the ZoomWindowUI.
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
	 * Sets the size of the zoomWindowUI to scale with the zoomfactor. 
	 * 
	 * @param w width of zoomed image.
	 * @param h height of the zoomed image. 
	 */
	void setZoomUISize(float w, float h) 
	{
		zoomWindowUI.setZoomUISize((int) w, (int) h);
	} 
	
	/**
	 * Returns the UI of the zoomWindow. 
	 * 
	 * @return zoomWindowUI.
	 */
	ZoomWindowUI getUI() { return zoomWindowUI; }

	/**
	 * Forwards call to {@link #zoomWindowUI}.
	 * 
	 * @param index The index. 
	 */
	void setSelectedSize(int index) { lensMenu.setSelectedSize(index); }

	/**
	 * Forwards call to {@link #zoomWindowUI}.
	 * 
	 * @param index The index. 
	 */
	void setZoomIndex(int index) { lensMenu.setZoomIndex(index); }

	/** Repaints the image canvas. */
	void paintImage()
	{ 
		zoomWindowUI.repaint();
	}

	/** Updates the background color of the view port. */
	void updateBackgroundColor() { zoomWindowUI.updateBackgroundColor(); }

	/**
	 * Ce
	 */
	public void componentResized(ComponentEvent e)
	{
		zoomWindowUI.setBounds(zoomWindowUI.getBounds());
	}

	
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
