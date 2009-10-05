/*
 * org.openmicroscopy.shoola.util.ui.lens.StatusPanel 
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
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * StatusPanel shows the current X,Y coordinates of the lens, it's width and
 * height and the zoomFactor in the bottom of the zoomWindowUI.  
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class StatusPanel
	extends JPanel
{
	
	/** Text for lens X position. */
	final static String		LENS_X = "X : ";
	
	/** Text for lens Y position. */
	final static String		LENS_Y = "Y : ";

	/** Text for lens Width. */
	final static String 	LENS_W = "W : ";
	
	/** Text for lens Height. */
	final static String 	LENS_H = "H : ";
	
	/** Text for lens Magnification. */
	final static String 	LENS_MAGNIFICATION = "x";
	
	/** Label showing position of the lens. */
	private	JLabel			lensPosition;
	
	/** Label showing size of the lens. */
	private JLabel			lensSize;
	
	/** Label showing zoomFactor of the lens. */
	private JLabel			lensZoom;
	
	/** Number of pixels per micron in x axis. */
	private double 	micronInXAxis = 1;

	/** Number of pixels per micron in y axis. */
	private double 	micronInYAxis = 1;
	
	/** Display the units: width, height and x,y in pixels. */
	private boolean displayInPixels = true;
	
	/**
	 * Constructor of the status panel which will show the x, y position of
	 * the lens, it's width and height and the zoomFactor of the lens. 
	 */
	StatusPanel()
	{
		lensPosition = new JLabel(LENS_X + " 000 " + LENS_Y + " 000");
		lensSize = new JLabel(LENS_W + " 000 " + LENS_H + " 000");
		lensZoom = new JLabel(LENS_MAGNIFICATION+"00");
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(lensPosition);
		Dimension minSize = new Dimension(15, 1);
		Dimension prefSize = new Dimension(15, 1);
		Dimension maxSize = new Dimension(15, 1);
		this.add(new Box.Filler(minSize, prefSize, maxSize));
		this.add(lensSize);
		this.add(new Box.Filler(minSize, prefSize, maxSize));
		this.add(lensZoom);
	}
	
	/**
	 * Display in pixels if <code>true</code> or in microns otherwise.
	 * 
	 * @param b see above.
	 */
	void setDisplayInPixels(boolean b)
	{
		displayInPixels = b;
	}
	
	/** set the XY values of the lens position text 
	 * 
	 * @param x See above.
	 * @param y See above.
	 */
	void setLensXY(int x, int y)
	{
		if (displayInPixels)
			lensPosition.setText(LENS_X + x + " " + LENS_Y + y);
		else
			lensPosition.setText(LENS_X + 
					UIUtilities.twoDecimalPlaces(x/micronInXAxis) + " " 
					+ LENS_Y + UIUtilities.twoDecimalPlaces(y/micronInYAxis));
			
	}

	/** set the W, H values of the lens Width, Height text. 
	 * 
	 * @param w See above.
	 * @param h See above.
	 */
	void setLensWidthHeight(int w, int h)
	{
		if(displayInPixels)
			lensSize.setText(LENS_W + w + " " + LENS_H + h);
		else
			lensSize.setText(
					LENS_W +UIUtilities.twoDecimalPlaces(w/micronInXAxis) 
					+ " " + LENS_H + 
					UIUtilities.twoDecimalPlaces(h/micronInYAxis));
	}
	
	/** Set the zoomFactor of the lens.
	 * 
	 * @param zoomFactor See above.
	 */
	void setLensZoomFactor(float zoomFactor)
	{
		lensZoom.setText(LENS_MAGNIFICATION + zoomFactor);
	}
	
	/**
	 * Set the mapping from pixel size to microns along the x and y axis. 
	 * @param x mapping in x axis.
	 * @param y mapping in y axis.
	 */
	public void setXYPixelMicron(double x, double y)
	{
		micronInXAxis = x;
		micronInYAxis = y;
	}
	
}
