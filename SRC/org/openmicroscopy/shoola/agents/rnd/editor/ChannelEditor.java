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
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

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
	private ChannelEditorBar		bar;
	
	public ChannelEditor(RenderingAgtCtrl eventManager, ChannelData data)
	{
		super(eventManager.getReferenceFrame(), "Channel Info", true);
		ChannelEditorManager manager = new ChannelEditorManager(eventManager, 
											this, data);
		channelPane = new ChannelPane(manager);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		bar = new ChannelEditorBar(manager);
		manager.attachListeners();
		buildGUI();
		setSize(EDITOR_WIDTH+EXTRA, EDITOR_HEIGHT);
	}
	
	/** Returns the TextField displayed in {@link ChannelPane}. */
	JTextField getFluor() { return channelPane.getFluor(); }
		
	/** Returns the TextField displayed in {@link ChannelPane}. */
	JTextField getExcitation() { return channelPane.getExcitation(); }
	
	/** Returns the TextArea displayed in {@link ChannelPane}. */
	JTextArea getInterpretationArea()
	{
		return channelPane.getInterpretationArea();
	}

	/** Returns the save button displayed in {@link ChannelEditorBar}. */
	JButton getSaveButton() { return bar.getSaveButton(); }
	
	/** Returns the save button displayed in {@link ChannelEditorBar}. */
	JButton getCancelButton() { return bar.getCancelButton(); }
	
	/** Returns the help button displayed in {@link ChannelEditorBar}. */
	JButton getHelpButton() { return bar.getHelpButton(); }
	
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
		getContentPane().add(bar, BorderLayout.SOUTH);	
	}
	
}
