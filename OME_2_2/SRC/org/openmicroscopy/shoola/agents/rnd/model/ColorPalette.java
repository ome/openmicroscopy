/*
 * org.openmicroscopy.shoola.agents.rnd.model.ColorPalette
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

package org.openmicroscopy.shoola.agents.rnd.model;

//Java imports
import java.awt.Color;
import javax.swing.JLayeredPane;

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
class ColorPalette
	extends JLayeredPane
{
	

	static final int 				WIDTH_BAR = 120; 
	static final int				HEIGHT_BAR = 10;
	static final int 				VSPACE = 15;
	static final int 				leftBorder = 15;
	static final int 				topBorder = 10;
	static final int				triangleW = 6;
	static final int 				triangleH = 7;
	static final int 				WIDTH_PANEL = WIDTH_BAR+2*leftBorder;
	static final int				HEIGHT_PANEL = HEIGHT_BAR+VSPACE;	 
	static final int				HEIGHT_LP = topBorder+3*HEIGHT_PANEL;

    				
	private ColorPaletteBar			cbH;
	private ColorPaletteManager 	cpm;
	private ColorBar				cbS;
	private ColorBar 				cbB;
    
    private ColorChooserManager 	ccManager;
    		
	ColorPalette(ColorChooserManager ccManager)
	{
		this.ccManager = ccManager;
		cpm = new ColorPaletteManager(this, ccManager);
		buildGUI();
		cpm.attachListeners();
	}
	
	/** Returns the manager. */
	ColorPaletteManager getManager(){ return cpm; }
	
	/** Builds the palette containing the three color bars. */
	private void buildGUI()
	{
		float[] vals = new float[3];
		int[] rgba = ccManager.getRGBA();
		Color.RGBtoHSB(rgba[ColorChooser.RED], rgba[ColorChooser.GREEN],
						rgba[ColorChooser.BLUE], vals);
		Color c = new Color(rgba[ColorChooser.RED], rgba[ColorChooser.GREEN],
							rgba[ColorChooser.BLUE]);
		cpm.setHSB(vals[0], vals[1], vals[2]);
		
		setLayout(null);
		if (c.equals(Color.white)) c = Color.red;
		
		cbH = new ColorPaletteBar(realToBar(vals[0]));
		cbS = new ColorBar(realToBar(vals[1]), Color.white, c, "S");
		cbB = new ColorBar(realToBar(vals[2]), Color.black, c, "B");
		cbH.setBounds(0, topBorder, WIDTH_PANEL, HEIGHT_PANEL);
		cbS.setBounds(0, topBorder+HEIGHT_PANEL, WIDTH_PANEL, HEIGHT_PANEL);
		cbB.setBounds(0, topBorder+2*HEIGHT_PANEL, WIDTH_PANEL, 
		HEIGHT_PANEL);
		add(cbH);
		add(cbS);
		add(cbB);	
	}
	
	/** Converts a value to a coordinate. */
	int realToBar(float v) { return (int) (v*WIDTH_BAR); }
	
	/** Returns the <i>Hue</i> graphical slider. */
	ColorPaletteBar getBarH() { return cbH; }
	
	/** Returns the <i>Saturation</i> graphical slider. */
	ColorBar getBarS() { return cbS; }
	
	/** Returns the <i>Brightness</i> graphical slider. */
	ColorBar getBarB() { return cbB; }
	
}

