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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.RenderingAgtCtrl;
import org.openmicroscopy.shoola.agents.rnd.metadata.ChannelData;

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
public class ChannelEditor
	extends JDialog
{
	
	static final Color   			STEELBLUE = new Color(0x4682B4);

	/** Width of the editor dialog window. */
	private static final int		EDITOR_WIDTH = 300;
	
	/** Height of the editor dialog window. */
	private static final int		EDITOR_HEIGHT = 300;
	
	private static final int		EXTRA = 100;
	
	private ChannelPane				channelPane;
	
	public ChannelEditor(RenderingAgtCtrl eventManager, ChannelData data, int w)
	{
		super((JFrame) eventManager.getRegistry().getTopFrame().getFrame(), 
				"Channel Info", true);
		ChannelEditorManager manager = new ChannelEditorManager(eventManager, 
											this, data, w);
		channelPane = new ChannelPane(manager);
		buildGUI();
		setSize(EDITOR_WIDTH+EXTRA, EDITOR_HEIGHT);
	}
	
	/** Returns the TextArea displayed in {@link WavelengthPane}. */
	JTextArea getInterpretationArea()
	{
		return channelPane.getInterpretationArea();
	}

	/** Returns the save button displayed in {@link CreateDatasetPane}. */
	JButton getSaveButton() { return channelPane.getSaveButton();}
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		//create and initialize the tabs
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, 
										  JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);
		tabs.addTab("Info", channelPane);			
		tabs.setForeground(STEELBLUE);
		//set layout and add components
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(tabs, BorderLayout.CENTER);
	}
	
}
