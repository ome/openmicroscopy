/*
 * org.openmicroscopy.shoola.agents.rnd.model.WavelengthEditor
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

package org.openmicroscopy.shoola.agents.rnd.editor;

//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.IconManager;
import org.openmicroscopy.shoola.agents.rnd.model.WavelengthData;
import org.openmicroscopy.shoola.env.config.Registry;

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
class WavelengthEditor
	extends JDialog
{
	public static final Color   	STEELBLUE = new Color(0x4682B4);

	/** Width of the editor dialog window. */
	public static final int			EDITOR_WIDTH = 300;
	
	/** Height of the editor dialog window. */
	public static final int			EDITOR_HEIGHT = 300;
	
	
	/** Reference to the registry. */
	private Registry 				registry;
	
	private WavelengthPane			wavelengthPane;
	
	public WavelengthEditor(Registry registry, WavelengthData wd)
	{
		super((JFrame) registry.getTopFrame().getFrame(), true);
		this.registry = registry;
		WavelengthEditorManager manager = new WavelengthEditorManager(this, wd);
		wavelengthPane = new WavelengthPane(manager);
		buildGUI();
		setSize(EDITOR_WIDTH+100, EDITOR_HEIGHT);
	}
	
	/** Returns the TextArea displayed in {@link WavelengthPane}. */
	JTextArea getInterpretationArea()
	{
		return wavelengthPane.getInterpretationArea();
	}

	/** 
 	* Returns the save button displayed in {@link CreateDatasetPane}.
 	*/
	JButton getSaveButton()
	{
		return wavelengthPane.getSaveButton();
	}
	
	/** Build and layout the GUI. */
	private void buildGUI()
	{
		//create and initialize the tabs
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, 
										  JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);
		IconManager IM = IconManager.getInstance(registry);
		//TODO: specify lookup name.
		Font font = (Font) registry.lookup("/resources/fonts/Titles");
		tabs.addTab("Info", IM.getIcon(IconManager.INFO), wavelengthPane);			
		tabs.setFont(font);
		tabs.setForeground(STEELBLUE);
		//set layout and add components
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(tabs, BorderLayout.CENTER);
	}
	
}
