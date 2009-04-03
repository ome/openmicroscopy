/*
 * org.openmicroscopy.shoola.util.ui.border.BorderFactory 
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
package org.openmicroscopy.shoola.util.ui.border;



//Java imports
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;

/** 
 * Factory class for vending customized <code>Border</code> objects. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CustomizedBorderFactory
{

	/** Index of the close button. */
	public static final int CLOSE_INDEX = 1;
	
	/** Index of the save button. */
	public static final int EDIT_INDEX = 0;
	
	/**
	 * Creates a {@link TitledLineBorder} with <code>close</code>
	 * and <code>save</code> button.
	 * 
	 * @param text The title.
	 * @return See above.
	 */
	public static TitledLineBorder 
		createClosableAndEditableTitledLineBorder(String text)
	{
		IconManager icons = IconManager.getInstance();
		ImageIcon icon = icons.getImageIcon(IconManager.SAVE);
		List<Image> images = new ArrayList<Image>(1);
		images.add(icon.getImage());
		icon = icons.getImageIcon(IconManager.CLOSE);
		images.add(icon.getImage());
		TitledLineBorder border = new TitledLineBorder(text);
		border.setImages(images);
		return border;
	}
	
	/**
	 * Creates a {@link TitledLineBorder} with a <code>save</code> button.
	 * 
	 * @param text The title.
	 * @return See above.
	 */
	public static TitledLineBorder 
		createEditableTitledLineBorder(String text)
	{
		IconManager icons = IconManager.getInstance();
		ImageIcon icon = icons.getImageIcon(IconManager.SAVE);
		List<Image> images = new ArrayList<Image>(1);
		images.add(icon.getImage());
		TitledLineBorder border = new TitledLineBorder(text);
		border.setImages(images);
		return border;
	}
	
}
