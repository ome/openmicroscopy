/*
 * org.openmicroscopy.shoola.env.ui.UserNotifierDialog
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



//Java imports
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;



/** 
 * Brings up a dialog to tell the user about something that has happened.
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

public class UserNotifierDialog
	extends JDialog
	implements ActionListener
{
	
	/** The width of the dialog window. */
	static final int				WIN_W = 250;  
	
	/** The height of the dialog window. */
	static final int				WIN_H = 200;
	
	private static final Dimension	D_WIN = new Dimension(WIN_W, WIN_H);
		
	/** Summary's Detail to display if requested. */
	private String 					detail;
	
	/** Value used to hide or display the message. */
	private boolean					isShown;

	/** Contents the component to display in the JDialog. */
	private Container				contentPane;
	
	/** Details's button. */
	private JButton					button;
	
	/** Reference to the registry .*/
	private Registry				registry;
	
	public UserNotifierDialog(String title, String summary, String detail, 
							int iconID)
	{
		this.detail = detail;
		setTitle(title);
		setModal(true);
		isShown = false;
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		contentPane = getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		buildGUI(summary, getIcon(iconID, true), true);
		pack();
	}
	
	public UserNotifierDialog(String title, String summary, int iconID)
	{
		setTitle(title);
		setModal(true);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		contentPane = getContentPane();	
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		buildGUI(summary, getIcon(iconID, true), false);
		pack();	
	}
	
	/**
	 * Creates a new instance of {@link UserNotifierDialog}.
	 * 
	 * @param registry		reference to the {@link Registry}.
	 * @param frame			parentComponent, reference to the topFrame.
	 * @param title			dialog's window title.
	 * @param summary		summary to display.
	 * @param iconID		icon ID.
	 */
	public UserNotifierDialog(Registry registry, JFrame frame, String title, 
								String summary, int iconID)
	{
		super(frame, title, true);
		this.registry = registry;
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		contentPane = getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS)); 	
		buildGUI(summary, getIcon(iconID, false), false);
		pack();	
	}
	
	/**
	 * Creates a new instance of {@link UserNotifierDialog}.
	 * 
	 * @param registry		reference to the {@link Registry}.
	 * @param frame			parentComponent, reference to the topFrame.
	 * @param title			dialog's window title.
	 * @param summary		summary to display.
	 * @param message		complement of informations.
	 * @param iconID		icon ID.
	 */
	public UserNotifierDialog(Registry registry, JFrame frame, String title, 
								String summary, String detail, int iconID)
	{
		super(frame, title, true);
		this.registry = registry;
		this.detail = detail;
		isShown = false;
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		contentPane = getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS)); 	
		buildGUI(summary, getIcon(iconID, false), true);
		pack();
	}
	
	/** Handles event fired by the JButton. */
	 public void actionPerformed(ActionEvent e)
	 {
		try {
			if (isShown) hideError();
			else displayError();
		} catch(NumberFormatException nfe) {
				throw nfe;  //just to be on the safe side...
		}    
	 }
	 
	 /** Hides the details. */
	 private void hideError()
	 {
		button.setText("Details >>");
		isShown = false;
		Component[] list = contentPane.getComponents();
		Component c = null;
		for (int i = 0; i < list.length; i++)
			if (list[i] instanceof JScrollPane) c = list[i];
		contentPane.remove(c);
		pack();	
	 }
	 
	/** Shows the details. */
	 private void displayError()
	 {
		button.setText("<< Details");
		isShown = true;
		JTextArea txtArea = new JTextArea(detail);
		txtArea.setEditable(false);
		txtArea.setLineWrap(true);
		txtArea.setWrapStyleWord(true);
		JScrollPane scrollPane  = new JScrollPane(txtArea);
		scrollPane.setPreferredSize(D_WIN);
		contentPane.add(scrollPane);
		pack(); 
	}
	
	private Icon getIcon(int iconID, boolean init)
	{
		Icon icon = null;
		if (init)
			icon = UIFactory.getIcon(UIFactory.ERROR);
		else {
			IconManager im = IconManager.getInstance(registry);
			icon = im.getIcon(iconID);
		}
		return icon;
	}
	
	/**
	 * Build and lay out the GUI.
	 * 
	 * @param summary		summary of information/ warning.
	 * @param iconID		icon to display.
	 */
	private void buildGUI(String summary, Icon icon, boolean withButton)
	{
		JPanel content = new JPanel(), iconPanel = new JPanel();
		JTextArea label = new JTextArea(summary);
		label.setLineWrap(true);
		label.setWrapStyleWord(true);
		label.setBorder(null);
		label.setEditable(false);
		label.setOpaque(false);
		iconPanel.add(new JLabel(icon));
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		content.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
		gridbag.setConstraints(iconPanel, c); 
		content.add(iconPanel);
		c.gridx = 1;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(label, c);
		content.add(label);
		if (withButton) {
			button = new JButton("Details >>");
			button.addActionListener(this);
			c.insets = new Insets(10, 0, 0, 0);  //top padding
			c.gridy = 1;
			gridbag.setConstraints(button, c); 
			content.add(button);
		}
		content.setPreferredSize(D_WIN);
		content.setSize(D_WIN);
		contentPane.add(content);
	}	

}