/*
 * org.openmicroscopy.shoola.agents.viewer.transform.zooming.ZoomMenu
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

package org.openmicroscopy.shoola.agents.viewer.transform.zooming;


//Java imports
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.transform.ImageInspectorManager;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ZoomMenu
	extends JMenu
{

	private ZoomMenuManager			manager;
	
	public ZoomMenu(ImageInspectorManager mng)
	{
		setText("Zooming");
		manager = new ZoomMenuManager(this, mng);
		buildGUI();
	} 
	
	public ZoomMenuManager getManager() { return manager; }

	/** Build the menu. */
	private void buildGUI()
	{
		JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("25%");
		manager.attachItemListener(menuItem, ZoomMenuManager.ZOOM_25);
		add(menuItem);
		menuItem = new JCheckBoxMenuItem("50%");
		manager.attachItemListener(menuItem, ZoomMenuManager.ZOOM_50);
		add(menuItem);
		menuItem = new JCheckBoxMenuItem("75%");
		manager.attachItemListener(menuItem, ZoomMenuManager.ZOOM_75);
		add(menuItem);
		menuItem = new JCheckBoxMenuItem("100%");
		manager.attachItemListener(menuItem, ZoomMenuManager.ZOOM_100);
		menuItem.setSelected(true);
		add(menuItem);
		menuItem = new JCheckBoxMenuItem("125%");
		manager.attachItemListener(menuItem, ZoomMenuManager.ZOOM_125);
		add(menuItem);
		menuItem = new JCheckBoxMenuItem("150%");
		manager.attachItemListener(menuItem, ZoomMenuManager.ZOOM_150);
		add(menuItem);
		menuItem = new JCheckBoxMenuItem("175%");
		manager.attachItemListener(menuItem, ZoomMenuManager.ZOOM_175);
		add(menuItem);
		menuItem = new JCheckBoxMenuItem("200%");
		manager.attachItemListener(menuItem, ZoomMenuManager.ZOOM_200);
		add(menuItem);
		menuItem = new JCheckBoxMenuItem("225%");
		manager.attachItemListener(menuItem, ZoomMenuManager.ZOOM_225);
		add(menuItem);
		menuItem = new JCheckBoxMenuItem("250%");
		manager.attachItemListener(menuItem, ZoomMenuManager.ZOOM_250);
		add(menuItem);
		menuItem = new JCheckBoxMenuItem("275%");
		manager.attachItemListener(menuItem, ZoomMenuManager.ZOOM_275);
		add(menuItem);
		menuItem = new JCheckBoxMenuItem("300%");
		manager.attachItemListener(menuItem, ZoomMenuManager.ZOOM_300);
		add(menuItem);
	}
	
}
