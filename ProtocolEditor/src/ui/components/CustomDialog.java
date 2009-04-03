 /*
 * ui.components.CustomDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package ui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.openmicroscopy.shoola.util.ui.TitlePanel;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public abstract class CustomDialog 
	extends JPanel 
	implements ActionListener {
	
	/**
	 * The JFrame in which this dialog is displayed.
	 */
	JFrame frame;
	
	/**
	 * The title of the page.
	 */
	private String title;
	
	/**
	 * The sub-title of the page.
	 */
	private String subTitle;
	
	/**
	 * A header message, to give additional information
	 */
	private String headerMessage;
	
	/**
	 * An icon to display at the top of the page
	 */
	private Icon headerIcon;
	
	/**
	 * The Text to display on the button that calls 
	 * ActionPerformed. Default text is "OK"
	 */
	private String okButtonText = "OK";
	
	private JComponent contentUI;
	
	/**
	 * The default width, in pixels, of the dialog
	 */
	public static final int PANEL_WIDTH = 800;

	
	public CustomDialog(String title) {
		
		setTitle(title);
		
		buildUI();
		
		displayInFrame(this);
	}
	
	/**
	 * This method is called by the constructor, after calling 
	 * initialiseTextArea();
	 * This will build the UI, placing the textArea in a JScrollPane,
	 * and place the whole UI panel in a JFrame. 
	 */
	protected void buildUI() {
		
		setLayout(new BorderLayout());

		this.setPreferredSize(new Dimension(PANEL_WIDTH, 500));
		
		
		contentUI = getDialogContent();
		if (contentUI == null) {
			contentUI = new JPanel();
		}
		setDialogContent(contentUI);
		
		// Header.
		setHeaderComponents();
		TitlePanel titlePanel = new TitlePanel(title, subTitle, headerMessage, headerIcon);
		this.add(titlePanel, BorderLayout.NORTH);
		
		// Buttons at bottom of window.
		JButton importButton = new JButton(okButtonText);
		importButton.addActionListener(this);
		importButton.setSelected(true);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
			}
		});
		
		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(cancelButton);
		buttonBox.add(importButton);
		buttonBox.add(Box.createHorizontalStrut(12));
		JPanel buttonBoxContainer = new JPanel(new BorderLayout());
		buttonBoxContainer.add(buttonBox, BorderLayout.EAST);
		this.add(buttonBoxContainer, BorderLayout.SOUTH);
	
		
	}
	
	public abstract void setHeaderComponents();
	
	public abstract JComponent getDialogContent();

	public void setDialogContent(JComponent newContent) {
		
		this.remove(contentUI);
		
		contentUI = newContent;
		
		this.add(contentUI, BorderLayout.CENTER);
		this.validate();
		this.repaint();
	}

	/**
	 * This method displays the JPanel in a JFrame. 
	 * 
	 * @param panel		the JPanel to display.
	 */
	public void displayInFrame(JPanel panel) {
		
		frame = new JFrame();
		
		frame.getContentPane().add(panel);
		
		frame.pack();
		frame.setLocation(50, 50);
		frame.setVisible(true);
	}
	

	public abstract void actionPerformed(ActionEvent e);
	
	
	/**
	 * Method to allow subclasses to set the title, before the title panel
	 * is created.
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Method to allow subclasses to set the subTitle, before the title panel
	 * is created.
	 * 
	 * @param title
	 */
	public void setSubTitle(String title) {
		this.subTitle = title;
	}
	
	/**
	 * Method to allow subclasses to set the header message, before the title panel
	 * is created.
	 * 
	 * @param message
	 */
	public void setHeaderMessage(String message) {
		this.headerMessage = message;
	}
	
	/**
	 * Method to allow subclasses to set the header Icon, before the title panel
	 * is created.
	 * 
	 * @param icon	An icon displayed in the header panel. 
	 */
	public void setHeaderIcon(Icon icon) {
		this.headerIcon = icon;
	}
	
	/**
	 * Method to allow subclasses to set the text of the "OK" button, that 
	 * calls ActionPerformed(). 
	 * Should be called before the Button is created.
	 * 
	 * @param okButtonText
	 */
	public void setOkButtonText(String okButtonText) {
		this.okButtonText = okButtonText;
	}

}
