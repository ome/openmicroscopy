/*
 * org.openmicroscopy.shoola.examples.viewer.ControlPalette 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.examples.viewer;


//Java imports

//Third-party libraries
import java.awt.image.BufferedImage;

import processing.core.PApplet;
import controlP5.CColor;
import controlP5.ColorPicker;
import controlP5.ControlGroup;
import controlP5.ControlP5;
import controlP5.Radio;
import controlP5.Slider;
import controlP5.Toggle;

//Application-internal dependencies

/** 
 * 
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class ControlPalette 
	extends ControlP5
{
	
	ControlPalette(PApplet applet)
	{
		super(applet);
	}
	
	void controlUI(int color)
	{
		ControlGroup ui = addGroup("Settings", 0, 10, 215);
		ui.setMoveable(true);
		ui.setBackgroundColor(color);
		ui.setBackgroundHeight(300);
		ui.mousePressed(); // Menu is hidden by default
		//ColorPicker picker = addColorPicker("picker", 0, 0, 100,20);
		//picker.setGroup(ui);
		
		Toggle r = addToggle("channel_1", 0, 70, 10, 10);
		CColor c = new CColor();
		c.setBackground(230);
		r.setColor(c);
		r.setGroup(ui);
		Slider s = addSlider("z", 0, 200, 128, 0, 100, 100, 10);
		s.setGroup(ui);
		s.setColorBackground(255);
	}

}
