/*
 * org.openmicroscopy.shoola.agents.chainbuilder.ui.ChaiinSaveFrame
 * 
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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




/*------------------------------------------------------------------------------
 *
 * Written by:    Harry Hochheiser <hsh@nih.gov>
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.chainbuilder.ui;

//Java imports
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.ChainDataManager;
/** 
 * A frame containing fields used when saving a chain
 * 
 * @author Harry Hochheiser
 * @version 2.1
 * @since OME2.1
 */
public class ChainSaveFrame extends JFrame implements ActionListener {
	
	private ChainFrame frame;
	private JButton cancel;
	private JButton save;
	
	private static int GAP=5;
	
	JTextField nameField;
	JTextArea descField;
	
	/** the chain data manager */
	private ChainDataManager manager;
	/**
	 * 
	 * @param The {@link Chain Frame} that contains the chain being saved
	 */
	public ChainSaveFrame(ChainFrame frame,ChainDataManager manager) {
		super("Save Chain");
		this.frame = frame;
		this.manager = manager;
		setResizable(false);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		Container pane  = getContentPane();
		pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));

		JPanel formPanel = new JPanel();
		formPanel.setLayout(new BoxLayout(formPanel,BoxLayout.Y_AXIS));
		
		pane.add(formPanel);
		
		Dimension heightGap = new Dimension(0,GAP);
		Dimension widthGap = new Dimension(GAP,0);
		
		formPanel.add(Box.createRigidArea(heightGap));
		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BoxLayout(namePanel,BoxLayout.X_AXIS));
		namePanel.add(Box.createRigidArea(widthGap));
		formPanel.add(namePanel);
		
		JLabel nameLabel = new JLabel("Name:");
		nameLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		namePanel.add(nameLabel);
		namePanel.add(Box.createRigidArea(widthGap));
		nameField = new JTextField(20);
		nameField.setAlignmentY(Component.TOP_ALIGNMENT);
		namePanel.add(nameField);
		
		formPanel.add(Box.createRigidArea(heightGap));
		JPanel descPanel = new JPanel();
		
		descPanel.setLayout(new BoxLayout(descPanel,BoxLayout.X_AXIS));
		formPanel.add(descPanel);		
		descPanel.add(Box.createRigidArea(widthGap));
		JLabel desc  = new JLabel("Description:");
		desc.setAlignmentY(Component.TOP_ALIGNMENT);
		descPanel.add(desc);
		
		descPanel.add(Box.createRigidArea(widthGap));
		descField =  new JTextArea(5,20);
		descField.setAlignmentY(Component.TOP_ALIGNMENT);
		descPanel.add(descField);
		
		JPanel warnPanel = new JPanel();
		JLabel warnLabel = new JLabel("Please specify a name for the new chain");
		warnPanel.add(warnLabel);
		formPanel.add(warnPanel);
		
		Dimension d = new Dimension((int) desc.getPreferredSize().getWidth(),
			(int) nameLabel.getPreferredSize().getHeight());
		nameLabel.setPreferredSize(d); 
	
		formPanel.add(Box.createRigidArea(heightGap));
		JPanel buttonPanel  = new JPanel();
		//buttons
	//	buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		pane.add(buttonPanel);
		
		save = new JButton("Save");
		buttonPanel.add(save);
		save.addActionListener(this);
		
		cancel = new JButton("Cancel");
		buttonPanel.add(cancel);
		cancel.addActionListener(this);
		
		pack();
	}	
	
	/**
	 * Processing of user selection of the buttons.
	 * 
	 * @param e The user event
	 */
	public void actionPerformed(ActionEvent e) {
		JButton src = (JButton) e.getSource();
		if (src == save) {
			processSave();
		}
		dispose();
	}
	
	private void processSave() {
		String name = nameField.getText();
		if (name == null || name.length() == 0) {
			String 
				info = "A Chain must have a name to be stored in the OME Database";
			JOptionPane.showMessageDialog(this,info,"Please provide a name",
					JOptionPane.INFORMATION_MESSAGE);
		}
		else if (manager.hasChainWithName(name)) {
			String warn = 
				"A chain named \""+name+"\" already exists in the database.\n"+
				"Please Choose another name";
			JOptionPane.showMessageDialog(this,warn,
					"Each chain must have a unique name",
					JOptionPane.WARNING_MESSAGE);
		}
		else
			frame.completeSave(name,descField.getText());
	}
	
}
