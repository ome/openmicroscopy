/*
 * org.openmicroscopy.shoola.agents.datamng.editors.ImageEditor
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

package org.openmicroscopy.shoola.agents.datamng.editors;

//Java imports
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManager;
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.agents.datamng.IconManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ImageData;

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
public class ImageEditor
	extends JDialog
{
	
	/** Reference to the manager. */
	private ImageEditorManager 		manager;
	
	/** Reference to the registry. */
	private Registry				registry;
	
	private ImageGeneralPane		generalPane;
	private ImageInfoPane			infoPane;
	private ImageOwnerPane			ownerPane;
	
	public ImageEditor(Registry registry, DataManagerCtrl control,
					ImageData model)
	{
		super((JFrame) registry.getTopFrame().getFrame(), true);
		this.registry = registry;
		manager = new ImageEditorManager(this, control, model);
		generalPane = new ImageGeneralPane(manager, registry);
		infoPane = new ImageInfoPane(manager);
		ownerPane = new ImageOwnerPane(manager);
		buildGUI();
		manager.initListeners();
		setSize(DataManager.EDITOR_WIDTH, DataManager.EDITOR_HEIGHT);
	}
	
	/** Build and layout the GUI. */
	void buildGUI()
	{
		//create and initialize the tabs
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, 
										  JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);
		//TODO: specify lookup name.
		IconManager im = IconManager.getInstance(registry);
		
		Font font = (Font) registry.lookup("/resources/fonts/Titles");
		
		tabs.addTab("General", im.getIcon(IconManager.IMAGE), generalPane);
		tabs.addTab("Info", im.getIcon(IconManager.INFO), infoPane);
		tabs.addTab("Owner", im.getIcon(IconManager.OME), ownerPane);
		tabs.setSelectedComponent(generalPane);
		tabs.setFont(font);
		tabs.setForeground(DataManager.STEELBLUE);
		//set layout and add components
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(tabs, BorderLayout.CENTER);	
	}
	
	/** Returns the annotate button displayed in {@link ImageGeneralPane}. */
	JButton getAnnotateButton()
	{
		return generalPane.getAnnotateButton();
	}
	
	/** Returns the save button displayed in {@link ImageGeneralPane}. */
	JButton getSaveButton()
	{
		return generalPane.getSaveButton();
	}

	/** Returns the reload button displayed in {@link ImageGeneralPane}. */
	JButton getReloadButton()
	{
		return generalPane.getReloadButton();
	}
	
	/** Returns the TextArea displayed in {@link ImageGeneralPane}. */
	JTextArea getDescriptionArea()
	{
		return generalPane.getDescriptionArea();
	}

	/** Returns the textfield displayed in {@link ImageGeneralPane}. */
	JTextArea getNameField()
	{
		return generalPane.getNameField();
	}
	
}
