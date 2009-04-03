 /*
 * org.openmicroscopy.shoola.agents.editor.uiComponents.ImageBorderFactory 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.uiComponents;

//Java imports

import java.awt.Color;

import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.util.ui.border.ImageBorder;

/** 
 * This is a Factory for creating ImageBorders for the Editor.
 * 
 * It specifies the images to use for the "Plain" image border
 * and the "Highlighted" Image border. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ImageBorderFactory 
{
	
	/**
	 * Creates an ImageBorder using the icons 
	 * IconManager.BORDER_TOP_LEFT, IconManager.BORDER_TOP etc.
	 * 
	 * @return		An ImageBorder.
	 * @see 	org.openmicroscopy.shoola.util.ui.border.ImageBorder
	 */
	public static Border getImageBorder() 
	{	
    	IconManager iM = IconManager.getInstance();
    	
    	return new ImageBorder( 
    			iM.getImageIcon(IconManager.BORDER_TOP_LEFT).getImage(),
    			iM.getImageIcon(IconManager.BORDER_TOP).getImage(),
    			iM.getImageIcon(IconManager.BORDER_TOP_RIGHT).getImage(),
    			iM.getImageIcon(IconManager.BORDER_LEFT).getImage(),
    			iM.getImageIcon(IconManager.BORDER_RIGHT).getImage(),
    			iM.getImageIcon(IconManager.BORDER_BOTTOM_LEFT).getImage(),
    			iM.getImageIcon(IconManager.BORDER_BOTTOM).getImage(),
    			iM.getImageIcon(IconManager.BORDER_BOTTOM_RIGHT).getImage());
    }
    
	
	/**
	 * Creates an ImageBorder using the icons 
	 * IconManager.BORDER_TOP_LEFT_HLT, IconManager.BORDER_TOP_HLT etc.
	 * 
	 * @return		A Highlighted ImageBorder.
	 * @see 	org.openmicroscopy.shoola.util.ui.border.ImageBorder
	 */
    public static Border getImageBorderHighLight() 
    {	
    	IconManager iM = IconManager.getInstance();
    	
    	return new ImageBorder( 
    			iM.getImageIcon(IconManager.BORDER_TOP_LEFT_HLT).getImage(),
    			iM.getImageIcon(IconManager.BORDER_TOP_HLT).getImage(),
    			iM.getImageIcon(IconManager.BORDER_TOP_RIGHT_HLT).getImage(),
    			iM.getImageIcon(IconManager.BORDER_LEFT_HLT).getImage(),
    			iM.getImageIcon(IconManager.BORDER_RIGHT_HLT).getImage(),
    			iM.getImageIcon(IconManager.BORDER_BOTTOM_LEFT_HLT).getImage(),
    			iM.getImageIcon(IconManager.BORDER_BOTTOM_HLT).getImage(),
    			iM.getImageIcon(IconManager.BORDER_BOTTOM_RIGHT_HLT).getImage());
    }


	/**
	 * A default colour for the background of this field. Matches the 
	 * colour of the default border. 
	 */
	public static final Color DEFAULT_BACKGROUND = new Color(237, 239, 246);

}
