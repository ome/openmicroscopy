/*
 * org.openmicroscopy.shoola.env.ui.TopFrame
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

package org.openmicroscopy.shoola.env.ui;

// Java Imports
import java.awt.Component;
import javax.swing.JMenuItem;

//Third-party libraries

//Application-internal dependencies

/** 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * <br><b>Internal version:</b> $Revision$  $Date$
 * @version 2.2
 * @since OME2.2
 */

public interface TopFrame
 {

	/** 
	* Positions a component on the layer of the application internal desktop.
	* 
	* @param    c			component to position.
	* @param    position    specified position.
	*/    
    public void addToDesktop(Component c, int position);
    
	/** 
	* Removes a component form the application internal desktop.
	* 
	* @param   c component to be removed.
	*/
    public void removeFromDesktop(Component c);
    
	/** 
	* Adds a component to a specified menu. 
	*
	* @param menuType  		ID which corresponds to specified menu.
	* @param item         	component to be added.
	*/
    public void addToMenu(int menuType, JMenuItem item);
    
	/** 
	* Removes a component from a specified menu. 
	*
	* @param menuType  		ID which corresponds to specified menu.
	* @param item         	component to be removed.
	*/
    public void removeFromMenu(int menuType, JMenuItem item);
    
    
}
