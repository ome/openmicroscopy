 /*
 * ui.components.ImportDialog 
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

//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.util.ui.TitlePanel;

import tree.ITreeModel;
import tree.Tree;
import ui.IModel;
import ui.SelectionObserver;
import ui.XMLUpdateObserver;

/** 
* This is the base class for imported dialogs.
* Used by the TextImporter and TableImporter.
*
* @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $Date: $)
* </small>
* @since OME3.0
*/

public abstract class ImportDialog extends JPanel{

	/**
	 * A reference to the interface required for opening a newly created tree.
	 */
	IModel model;
	
	/**
	 * A reference to the root of the newly created tree. 
	 */
	ITreeModel tree;
	
	/**
	 * The JFrame in which this dialog is displayed.
	 */
	JFrame frame;
	
	/**
	 * The JEditorPane used to display and edit the text to be imported.
	 */ 
	JTextComponent textArea;
	
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
	 * The container that holds the Title panel (NORTH), to which
	 * subclasses can add eg tool bars. 
	 */
	protected JPanel titleAndToolbarContainer;
	
	/**
	 * Creates a new instance of this class, but doesn't build and display UI.
	 * After calling this constructor, subclasses can use setter methods
	 * to set subTitle, headerMessage and headerIcon. 
	 * They must also instantiate 
	 * 
	 * @param model
	 * @param title
	 */
	public ImportDialog(IModel model, String title) {
		
		initialise(model);
		
		this.title = title;
		
		initialiseTextArea();
		
		buildAndDisplayUI();
	}
	
	
	/**
	 * This method is called by the constructor, after calling 
	 * initialiseTextArea();
	 * This will build the UI, placing the textArea in a JScrollPane,
	 * and place the whole UI panel in a JFrame. 
	 */
	protected void buildAndDisplayUI() {
		
		setLayout(new BorderLayout());
		
		int panelWidth = 800;

		this.setPreferredSize(new Dimension(panelWidth, 500));
		
		// Header.
		TitlePanel titlePanel = new TitlePanel(title, subTitle, headerMessage, headerIcon);
		
		/*
		 * A JPanel with borderLayout, which allows subclasses to add 
		 * components between the titlePanel and the textArea.
		 * Eg. adding a toolBar to the EAST of the container. 
		 */
		titleAndToolbarContainer = new JPanel(new BorderLayout());
		titleAndToolbarContainer.add(titlePanel, BorderLayout.NORTH);
		
		add(titleAndToolbarContainer, BorderLayout.NORTH);
		
		/*
		 * The text area should have been instantiated. 
		 * But, if not, create a JTextArea here. 
		 */
		if (textArea == null) 
			textArea = new JTextArea();
		textArea.addFocusListener(new TextAreaFocusListener());
		
		// put the text area in a scroll pane
		Dimension scrollPaneSize = new Dimension(panelWidth, 350);
		textArea.setMaximumSize(scrollPaneSize);
		JScrollPane scrollPane = new JScrollPane(textArea, 
				 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
				 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(scrollPaneSize);
		scrollPane.setMinimumSize(scrollPaneSize);
		
		this.add(scrollPane, BorderLayout.CENTER);
		
		
		// Buttons at bottom of window.
		JButton importButton = new JButton("Import");
		importButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				importText();
			}
		});
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
	
		displayInFrame(this);
	}
	
	/**
	 * Called by constructor. 
	 * Creates a new Tree, with the model as it's selection and update observer.
	 * 
	 * @param model		The IModel that the new tree will notify of updates.
	 * 					So, after it is presented in the UI, it will behave
	 * 					correctly. 
	 */
	public void initialise(IModel model) {
		this.model = model;
		
		SelectionObserver sO = (SelectionObserver)model;
		XMLUpdateObserver xO = (XMLUpdateObserver)model;
		
		tree = new Tree(sO, xO);
	}
	
	/**
	 * Abstract method to initialise the JTextComponent textArea. 
	 * This method can also be used as a place to set the
	 * subTitle, headerMessage and headerIcon. 
	 */
	public abstract void initialiseTextArea();
	
	/**
	 * The abstract import method which subclasses use to perform the
	 * import process. 
	 * This should take the text, use it to build a tree model, and 
	 * pass this to IModel model, to open the file.
	 */
	public abstract void importText();
	
	/**
	 * A focus listener added to the textArea.
	 * The first time that this component gains focus, all the text
	 * will become selected.
	 * This makes it easier for the user to paste their text in. 
	 * 
	 * @author will
	 *
	 */
	public class TextAreaFocusListener implements FocusListener {
		
		boolean selectedOnce = false;
		
		public void focusGained(FocusEvent e) {
			if (selectedOnce) return;
			
			if (e.getSource() instanceof JTextComponent) {
				JTextComponent source = (JTextComponent)e.getSource();
				int textLength = source.getText().length();
				source.setSelectionStart(0);
				source.setSelectionEnd(textLength);
				selectedOnce = true;
			}
		}
		public void focusLost(FocusEvent e) {}
		
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
}

