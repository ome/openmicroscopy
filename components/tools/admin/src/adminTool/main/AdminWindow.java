package adminTool.main;
/*
 * *.AdminMain 
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
//Java imports
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import adminTool.ui.messenger.CommentMessenger;


//Third-party libraries

//Application-internal dependencies

/** 
 * This is the colourpicker which instatiates a dialog. Once the user hits 
 * cancel or accept the dialog will disappear. It contains 3 panels, HSV wheel
 * RGB Sliders and ColourSwatchs. 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 * 
 */
public class AdminWindow 
	extends JFrame
	implements ActionListener
{
	private JMenuBar	menuBar;
	private JMenu		fileMenu;
	private JMenuItem	loginMenu;
	private JMenuItem	exit;
	private JMenu		helpMenu;
	private JMenuItem	help;
	private JMenuItem	addComment;
	private MainPanel 	mainPanel;
	
	public AdminWindow() 
	{
		setTitle("Admin Tool");
		setLocation(300, 100);
		setSize(800,500);
		createMenu();
		mainPanel	= new MainPanel(this);
		this.setLayout(new BorderLayout());
		this.add(mainPanel, BorderLayout.CENTER);
	}
	
	public void createMenu()
	{
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		loginMenu = new JMenuItem("Login to Server");
		exit = new JMenuItem("Exit");
		fileMenu.add(loginMenu);
		fileMenu.add(new JSeparator());
		fileMenu.add(exit);
		menuBar.add(fileMenu);
		loginMenu.setActionCommand("login");
		loginMenu.addActionListener(this);
		exit.setActionCommand("exit");
		exit.addActionListener(this);
		helpMenu = new JMenu("Help");
		help = new JMenuItem("About AdminTool");
		addComment = new JMenuItem("Send Comment To Developers");
		helpMenu.add(help);
		helpMenu.add(new JSeparator());
		helpMenu.add(addComment);
		addComment.addActionListener(this);
		addComment.setActionCommand("comment");
		menuBar.add(helpMenu);
		
		this.setJMenuBar(menuBar);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getActionCommand().equals("login"))
		{
			mainPanel.startLogin();
			pack();
		}
		if(e.getActionCommand().equals("exit"))
			this.dispose();
		if(e.getActionCommand().equals("comment"))
		{
			CommentMessenger comments = new CommentMessenger(this, "Send Comment to Developers", true);
			
		}
	}
	

}
