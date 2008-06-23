
/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package ui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.StringReader;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import org.openmicroscopy.shoola.util.ui.TitlePanel;

import tree.DataFieldConstants;
import tree.DataFieldNode;
import tree.ITreeModel;
import tree.Tree;
import ui.IModel;
import ui.SelectionObserver;
import ui.XMLUpdateObserver;
import util.ImageFactory;
import xmlMVC.XMLModel;


public class TextImporter extends JPanel{

	IModel model;
	
	ITreeModel tree;
	
	JFrame frame;
	
	protected JTextArea textArea;
	
	private String title;
	
	private String headerMessage;
	
	private Icon headerIcon;
	
	
	public TextImporter() {}
	
	public TextImporter(IModel model) {
		
		initialise(model);
		
		title = "Import Text";
		 
		headerMessage = "Please paste the text you wish to import into the text area below. \n" +
			"Each new line will become a new field when imported.";
		 
		headerIcon = ImageFactory.getInstance().getIcon(ImageFactory.KORGANIZER_ICON);
		
		 
		buildAndDisplayUI();
	}
	
	
	
	public void buildAndDisplayUI() {
		

		setLayout(new BorderLayout());
		
		int panelWidth = 800;
		
		
		this.setPreferredSize(new Dimension(panelWidth, 500));
		
		// Header.
		TitlePanel titlePanel = new TitlePanel(title, headerMessage, headerIcon);
		add(titlePanel, BorderLayout.NORTH);
		
		// Text Area...
		textArea = new JTextArea();
		textArea.setText("<Paste your text here>");
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.addFocusListener(new TextAreaFocusListener());
		
		textArea.setRows(20);
		textArea.setColumns(40);
		
		// ... in a scroll pane
		Dimension scrollPaneSize = new Dimension(panelWidth, 350);
		JScrollPane scrollPane = new JScrollPane(textArea, 
				 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(scrollPaneSize);
		scrollPane.setMinimumSize(scrollPaneSize);
		
		this.add(scrollPane, BorderLayout.CENTER);
		
		
		// Buttons
		JButton importButton = new JButton("Import");
		importButton.addActionListener(new ImportListener());
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
	
	public void initialise(IModel model) {
		this.model = model;
		
		SelectionObserver sO = (SelectionObserver)model;
		XMLUpdateObserver xO = (XMLUpdateObserver)model;
		
		tree = new Tree(sO, xO);
	}
	
	public class ImportListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			importTextToTree();
		}
		
	}
	
	public void importTextToTree() {
		String wholeText = textArea.getText();
		
		StringReader sr = new StringReader(wholeText);
		
		BufferedReader br = new BufferedReader(sr);
		
		try {
			String newLine = br.readLine();
			
			/*
			 * Create the root node of a tree.
			 */
			
			HashMap<String,String> attributeMap = new HashMap<String,String>();
			attributeMap.put(DataFieldConstants.INPUT_TYPE, DataFieldConstants.PROTOCOL_TITLE);
			attributeMap.put(DataFieldConstants.ELEMENT_NAME, newLine);
			
			DataFieldNode rootNode = new DataFieldNode(attributeMap, tree);
			
			tree.setRootNode(rootNode);
			
			newLine = br.readLine();
			
			while (newLine != null) {
				System.out.println(newLine);
				
				/*
				 * For each new line, Take the text, put it in a new node,
				 * and add the node as a child of root node. 
				 */
				if (newLine.length() > 0) {
					HashMap<String,String> map = new HashMap<String,String>();
					map.put(DataFieldConstants.INPUT_TYPE, DataFieldConstants.FIXED_PROTOCOL_STEP);
					map.put(DataFieldConstants.ELEMENT_NAME, newLine);
				
					DataFieldNode newNode = new DataFieldNode(map, tree);
					rootNode.addChild(newNode);
				}
				
				newLine = br.readLine();
			}
			
			model.openTree((Tree)tree);
			
		} catch (IOException ioEx) {
			// TODO Auto-generated catch block
			ioEx.printStackTrace();
		}
	}
	
	public class TextAreaFocusListener implements FocusListener {
		public void focusGained(FocusEvent e) {
			if (e.getSource() instanceof JTextComponent) {
				JTextComponent source = (JTextComponent)e.getSource();
				int textLength = source.getText().length();
				source.setSelectionStart(0);
				source.setSelectionEnd(textLength);
			}
		}
		public void focusLost(FocusEvent e) {}
		
	}
	
	public void displayInFrame(JPanel panel) {
		
		frame = new JFrame();
		
		frame.getContentPane().add(panel);
		
		frame.pack();
		frame.setLocation(50, 50);
		frame.setVisible(true);
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setHeaderMessage(String message) {
		this.headerMessage = message;
	}
	
	public void setHeaderIcon(Icon icon) {
		this.headerIcon = icon;
	}
}
