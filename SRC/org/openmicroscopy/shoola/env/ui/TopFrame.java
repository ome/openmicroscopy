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
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
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
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

public interface TopFrame
 {
	/** 
	 * To be used with the {@link #addToDesktop(Component, int) addToDesktop} 
	 * method to position a component on the bottom most layer of the
	 * application's internal desktop.
	 */
	public static final int       	DEFAULT_LAYER = 0;
	
	/** 
	 * To be used with the {@link #addToDesktop(Component, int) addToDesktop} 
	 * method to position a component on the palette layer of the application's 
	 * internal desktop.
	 * The palette layer sits above the default layer. Useful for floating 
	 * toolbars and palettes, so they can be positioned above other components. 
	 */
	public static final int       	PALETTE_LAYER = 1;
	
	/** ID corresponding to the specified menu. */
	public static final int        	FILE = 0;
	public static final int        	VIEW = 1;
	public static final int        	HELP = 2;
	public static final int        	CONNECT = 3;
	
	
	public static final int			OMEDS = 0;
	public static final int			OMEIS = 1; 
	
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
	* @param menuType  		ID of specified menu.
	* @param item         	component to be removed.
	*/
    public void removeFromMenu(int menuType, JMenuItem item);
    
    /** 
     * Retrieves an item in the specified menu.
     * 
     * @param menuType		ID of the specified menu.
     * @param itemPosition	position index of the item.
     * @return 	JMenuItem or <code>null</code> if there exists no item for 
     * 			the specified position
     */
    public JMenuItem getItemFromMenu(int menuType, int itemPosition);
    
    /**
     * Pops up the top frame window.
     */
    public void open();
    
    /** Returns the presentation. */
    public JFrame getFrame();
    
	public void deiconifyFrame(JInternalFrame frame);
	
}
