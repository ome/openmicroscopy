/*
 * org.openmicroscopy.shoola.util.ui.ColorPanel
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

package org.openmicroscopy.shoola.util.ui;

//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * <br><b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ColorPanel
	extends JPanel
{
	private static final Color	DEFAULT_BACKGROUND  = Color.white;
	private static final Color	DEFAULT_BORDER  = Color.black;
	private static final int	X_COORD = 5;
	private static final int 	Y_COORD = 5;
	private static final int	SPACE = 10;
	
	private Color		colorSelected;
	
	/** Sets the color. */
	public void setColor(Color c)
	{
		colorSelected = c;
		
	}
	
	/** Overrides the paintComponent method. */
	public void paintComponent(Graphics g)
	{
		Graphics2D      g2D = (Graphics2D) g;
		g2D.setColor(DEFAULT_BACKGROUND);
		Dimension   d = getSize();
		g2D.fillRect(0, 0, d.width, d.height); 
		g2D.setColor(colorSelected);
		g2D.fillRect(X_COORD, Y_COORD, d.width-SPACE, d.height-SPACE);
		g2D.setColor(DEFAULT_BORDER);
		g2D.draw(new Rectangle2D.Double(X_COORD, Y_COORD, d.width-SPACE,
										d.height-SPACE)); 
	}

}
