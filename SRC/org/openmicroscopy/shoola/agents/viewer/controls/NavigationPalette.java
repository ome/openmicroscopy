/*
 * org.openmicroscopy.shoola.agents.viewer.controls.NavigationPalette
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

package org.openmicroscopy.shoola.agents.viewer.controls;

//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.IconManager;
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;

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
public class NavigationPalette
	extends JDialog
{
	/** Width of the editor dialog window. */
	private static final int			EDITOR_WIDTH = 300;
	
	/** Height of the editor dialog window. */
	private static final int			EDITOR_HEIGHT = 200;
	
	static final Color					STEELBLUE = new Color(0x4682B4);
	
	private TNavigator      			tNavigator;
	private	XYZNavigator    			xyzNavigator;
	
	private Registry					registry;
	
	private NavigationPaletteManager	manager;
	
	public NavigationPalette(ViewerCtrl eventManager)
	{
		super(eventManager.getReferenceFrame(), "Navigation Palette", true);
		registry = eventManager.getRegistry();
		manager = new NavigationPaletteManager(this, eventManager);
		PixelsDimensions pxsDims = eventManager.getPixelsDims();
		tNavigator = new TNavigator(manager, pxsDims.sizeT, 
						eventManager.getDefaultT());
		xyzNavigator = new XYZNavigator(manager, pxsDims.sizeX, pxsDims.sizeY, 
							pxsDims.sizeZ, eventManager.getDefaultZ());
		buildGUI();
		setResizable(false);
		setSize(EDITOR_WIDTH, EDITOR_HEIGHT);
	}

	public TNavigator getTNavigator()
	{
		return tNavigator;
	}

	public XYZNavigator getXYZNavigator()
	{
		return xyzNavigator;
	}
	
	/** Build and layout the GUI. */
	private void buildGUI()
	{
		Font font = (Font) registry.lookup("/resources/fonts/Titles");
		IconManager im = IconManager.getInstance(registry);
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, 
												  JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);
		tabs.addTab("TNavigation", im.getIcon(IconManager.CLOCK), tNavigator);
		tabs.addTab("XYZ Navigation", im.getIcon(IconManager.XYZ), 
					xyzNavigator);			
		tabs.setSelectedComponent(xyzNavigator);
		tabs.setFont(font);
		//set layout and add components
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(tabs, BorderLayout.CENTER);
	}
	
	

}
