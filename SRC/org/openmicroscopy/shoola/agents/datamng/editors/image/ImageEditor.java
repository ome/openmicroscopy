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

package org.openmicroscopy.shoola.agents.datamng.editors.image;

//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.agents.datamng.DataManagerUIF;
import org.openmicroscopy.shoola.agents.datamng.IconManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.util.ui.TitlePanel;

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
	private ImageEditorBar			bar;
	
	public ImageEditor(Registry registry, DataManagerCtrl control,
					ImageData model)
	{
		super(control.getReferenceFrame(), true);
		this.registry = registry;
		manager = new ImageEditorManager(this, control, model);
		generalPane = new ImageGeneralPane(manager);
		infoPane = new ImageInfoPane(manager);
		ownerPane = new ImageOwnerPane(manager);
		bar = new ImageEditorBar();
		buildGUI();
		manager.initListeners();
		setSize(DataManagerUIF.EDITOR_WIDTH, DataManagerUIF.EDITOR_HEIGHT);
	}
	
	/** Returns the save button displayed in {@link ImageEditorBar}. */
	JButton getSaveButton() { return bar.getSaveButton(); }

	/** Returns the cancel button displayed in {@link ImageEditorBar}. */
	JButton getCancelButton() { return bar.getCancelButton(); }
	
	/** Returns the TextArea displayed in {@link ImageGeneralPane}. */
	JTextArea getDescriptionArea() { return generalPane.getDescriptionArea(); }

	/** Returns the textfield displayed in {@link ImageGeneralPane}. */
	JTextArea getNameField() { return generalPane.getNameField(); }
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		//create and initialize the tabs
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, 
										  JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);
		IconManager im = IconManager.getInstance(registry);
		Font font = (Font) registry.lookup("/resources/fonts/Titles");
		
		tabs.addTab("General", im.getIcon(IconManager.IMAGE), generalPane);
		tabs.addTab("Info", im.getIcon(IconManager.INFO), infoPane);
		tabs.addTab("Owner", im.getIcon(IconManager.OWNER), ownerPane);
		tabs.setSelectedComponent(generalPane);
		tabs.setFont(font);
		tabs.setForeground(DataManagerUIF.STEELBLUE);
		TitlePanel tp = new TitlePanel("Image", "Edit an existing image.", 
								im.getIcon(IconManager.IMAGE_BIG));
		//set layout and add components
        Container c = getContentPane();
		c.setLayout(new BorderLayout(0, 0));
		c.add(tp, BorderLayout.NORTH);
		c.add(tabs, BorderLayout.CENTER);
		c.add(bar, BorderLayout.SOUTH);		
	}

}
