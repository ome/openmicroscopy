/*
 * org.openmicroscopy.shoola.agents.datamng.DataManagerUIF
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

package org.openmicroscopy.shoola.agents.datamng;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.editors.DatasetEditor;
import org.openmicroscopy.shoola.agents.datamng.editors.ImageEditor;
import org.openmicroscopy.shoola.agents.datamng.editors.ProjectEditor;
import org.openmicroscopy.shoola.env.config.IconFactory;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ProjectData;

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
class DataManagerUIF
	extends JInternalFrame
{

	private static final Color   	STEELBLUE = new Color(0x4682B4);
	private static final int 		WIN_WIDTH = 350;
	private static final int 		WIN_HEIGHT = 350;
	private static final int		X_LOCATION = 0;
	private static final int		Y_LOCATION = 0;
	
	/** 
	 * UI component to view a summary of the user's data
	 * and to mark the currently viewed image. 
	 */
	private ExplorerPane	explPane;
	
	/** Reference to the regisry. */
	private Registry		registry;
	
	/** Reference to the regisry. */
	private DataManagerCtrl	control;
			
	DataManagerUIF(DataManagerCtrl control, Registry registry)
	{
		//name, resizable, closable, maximizable, iconifiable.
		super("DataManager", true, true, true, true);
		this.registry = registry;
		this.control = control;
		explPane = new ExplorerPane(control, registry);
		buildGUI();
	}
	
	JMenuItem getViewMenuItem()
	{
		JMenuItem menuItem = new JMenuItem("DataManager");
		return menuItem;
	}
    
	/** 
	 * Brings up the property sheet dialog for the specified project.
	 *
	 * @param   p   The project whose properties will be displayed by the 
	 * 				property sheet dialog. Mustn't be <code>null</code>.
	 */
	void showProjectPS(ProjectData p)
	{
		ProjectEditor   ps = new ProjectEditor(registry, p);
		showPS((JDialog) ps);
	}
    
	/** 
	 * Brings up the property sheet dialog for the specified dataset.
	 *
	 * @param   d   The dataset whose properties will be displayed by the
	 * 				property sheet dialog. Mustn't be <code>null</code>.
	 */
	void showDatasetPS(DatasetData d)
	{
		DatasetEditor   ps = new DatasetEditor(registry, d);
		showPS((JDialog) ps);
	}
    
	/** 
	 * Brings up the property sheet dialog for the specified image.
	 *
	 * @param   i   The image whose properties will be displayed by the 
	 * 				property sheet dialog. Mustn't be <code>null</code>.
	 */
	void showImagePS(ImageData i) 
	{
		ImageEditor   ps = new ImageEditor(registry, i);
		showPS((JDialog) ps);
	}	
	
	/** 
	 * Sizes, centers and brings up the specified editor dialog.
	 *
	 * @param   editor	The editor dialog.
	 */
	private void showPS(JDialog editor)
	{
		//editor.pack();
		JFrame topFrame = (JFrame) registry.getTopFrame().getFrame();
		Rectangle   tfB = topFrame.getBounds(), 
					psB = editor.getBounds();
		int         offsetX = (tfB.width-psB.width)/2, 
					offsetY = (tfB.height-psB.height)/2;
		if (offsetX < 0)   offsetX = 0;
		if (offsetY < 0)   offsetY = 0;
		editor.setLocation(tfB.x+offsetX, tfB.y+offsetY);
		editor.setVisible(true);
	}

	/** 
	 * Builds and lays out the GUI.
	 */
	private void buildGUI()
	{
		//create and initialize the tabs
		JTabbedPane     tabs = new JTabbedPane(JTabbedPane.BOTTOM, 
												JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);
		//TODO: specify lookup name.
		Font font = (Font) registry.lookup("/resources/fonts/Titles");
		IconFactory factory = (IconFactory) 
								registry.lookup("/resources/icons/Factory");
		Icon icon = factory.getIcon("OME16.png");
		
		//TODO: image not loaded						
		tabs.addTab("Explorer", icon, explPane);
		tabs.setFont(font);
		tabs.setForeground(STEELBLUE);
		tabs.setSelectedComponent(explPane);
		
		//set layout and add components
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(tabs, BorderLayout.CENTER);
		
		setFrameIcon(icon);
		//set the size and position the window.
		setBounds(X_LOCATION, Y_LOCATION, WIN_WIDTH, WIN_HEIGHT);	
	} 	
	
	
}
