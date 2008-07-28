
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;

import javax.swing.Action;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tree.DataFieldConstants;
import tree.DataFieldNode;
import tree.ITreeModel;
import tree.Tree;
import ui.IModel;
import ui.SelectionObserver;
import ui.XMLUpdateObserver;
import ui.components.htmlActions.InsertSpanAction;
import util.ImageFactory;
import util.XMLMethods;
import xmlMVC.ConfigConstants;
import xmlMVC.XMLModel;


public class TextImporter extends JPanel{

	IModel model;
	
	ITreeModel tree;
	
	JFrame frame;
	
	// protected JTextArea textArea;
	
	//alternative, 
	SimpleHTMLEditorPane textArea;
	
	private String title;
	
	private String headerMessage;
	
	private Icon headerIcon;
	
	public static final String P = "p";
	
	public static final String SPAN = "span";
	
	public static final String PARAM = "param";
	
	public static final String UNITS = "units";
	
	
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
		
		
		// Text Area...
		/*
		textArea = new JTextArea();
		textArea.setText("<Paste your text here>");
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.addFocusListener(new TextAreaFocusListener());
		
		textArea.setRows(20);
		textArea.setColumns(40);
		*/
		
		// .. or a text editor pane
		textArea = new SimpleHTMLEditorPane();
		
		// ..with a tool bar..
		Box toolbarBox = Box.createHorizontalBox();
		
		JButton b2 = new JButton(textArea.getHtmlEditorKitAction("BoldRed"));
		b2.setText("Field Title");
		b2.setToolTipText("Turn on / off the Field Title");
		toolbarBox.add(b2);
		
		Action paramAction = new InsertSpanAction("Parameter", PARAM, Color.blue);
		JButton paramButton = new JButton(paramAction);
		toolbarBox.add(paramButton);
		
		Action unitsAction = new InsertSpanAction("Units", UNITS, Color.yellow);
		JButton unitsButton = new JButton(unitsAction);
		toolbarBox.add(unitsButton);
		
		toolbarBox.add(new JPanel());	// so to align buttons to left.
		
		Box titleAndToolbarContainer = Box.createVerticalBox();
		titleAndToolbarContainer.add(titlePanel);
		titleAndToolbarContainer.add(toolbarBox);
		
		add(titleAndToolbarContainer, BorderLayout.NORTH);
		
		
		// ... in a scroll pane
		Dimension scrollPaneSize = new Dimension(panelWidth, 350);
		textArea.setMaximumSize(scrollPaneSize);
		JScrollPane scrollPane = new JScrollPane(
				textArea, 
				 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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
			//importTextToTree();
			
			importXhtmlToTree();
		}
		
	}
	
	public void importXhtmlToTree() {
		
		String htmlText = textArea.getText();
		
		File tempFile = new File(
				ConfigConstants.OMERO_EDITOR_FILE + 
				File.separator + "importFile");
		

		FileWriter writer;
		try {
			writer = new FileWriter(tempFile);
			Writer output = new BufferedWriter(writer);
			output.write(htmlText);
			output.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	
		
		
		try {
			Document xhtmlDoc = XMLMethods.readXMLtoDOM(tempFile);
			
			NodeList paragraphs = xhtmlDoc.getElementsByTagName(P);
			
			String newLine =  "";
			
			/*
			 * Define the root node of a tree.
			 * This will get instantiated by the first paragraph of the NodeList
			 */
			DataFieldNode rootNode = null;
			
			
			String fieldName = null;
			String desc = "";
			String paramDefault = null;
			String paramUnits = null;
			
			/*
			 * Loop through all the paragraphs...
			 */
			for (int i=0; i< paragraphs.getLength(); i++) {
				
				Element paraElement = (Element)paragraphs.item(i);

				newLine = paraElement.getTextContent().trim();
				
				/*
				 * If this element has some text, 
				 */
				if (newLine.length() > 0) {
					
					
					/*
					 * Does this element have children? Get the first <b>
					 */
					NodeList bolds = paraElement.getElementsByTagName("b");
					if (bolds.getLength() > 0) {
						Element firstBoldElement = (Element)bolds.item(0);
						fieldName = firstBoldElement.getTextContent();
					}
					
					/*
					 * Go through <span> elements, looking for 
					 */
					NodeList spans = paraElement.getElementsByTagName("span");
					for (int s=0; s<spans.getLength(); s++) {
						Element span = (Element)spans.item(s);
						String className = span.getAttribute("class");
						/*
						 * Look for <span> elements that have class="param" 
						 */
						if ("param".equals(className) && (paramDefault == null)) {
							paramDefault = span.getTextContent().trim();
						} 
						/*
						 * Does this field have units? 
						 * Check to see if this <span> defines units...
						 */
						else if ("units".equals(className) && (paramUnits == null)) {
							paramUnits = span.getTextContent().trim();
						}
						
					}
					
					
					
					if (desc.length() > 0)
						desc = desc + "<br>";
					
					desc = desc + newLine;
					
				}
				
				/*
				 * For an empty line (or last line) you have reached the 
				 * end of a field.
				 * Take the description, put it in a new node,
				 * and add the node as a child of root node. 
				 */
				if ((newLine.length() == 0) || 
						(i == paragraphs.getLength() -1 )) {
				
					System.out.println(fieldName);
					System.out.println(desc);
				
					HashMap<String,String> map = new HashMap<String,String>();
					
					if ((fieldName == null) && (desc != null)) {
						if (desc.length() < 40) {
							fieldName = desc;
							desc = "";
						} else {
							fieldName = desc.substring(0, 38) + "...";
						}
					}
					if (fieldName != null)
						map.put(DataFieldConstants.ELEMENT_NAME, fieldName);
					if (desc.length() > 0) 
						map.put(DataFieldConstants.DESCRIPTION, desc);
				
					if ((paramDefault == null) && (paramUnits == null)) {
						// Field has no parameter or units. Fixed step.
						map.put(DataFieldConstants.INPUT_TYPE, 
								DataFieldConstants.FIXED_PROTOCOL_STEP);
					} 
					else if (paramUnits == null) {
						// This field has parameter, but no units. Text field.
						map.put(DataFieldConstants.INPUT_TYPE, 
								DataFieldConstants.TEXT_ENTRY_STEP);
						map.put(DataFieldConstants.DEFAULT, paramDefault);
					}
					else {
						// Field has units. Number Field
						map.put(DataFieldConstants.INPUT_TYPE, 
								DataFieldConstants.NUMBER_ENTRY_STEP);
						if (paramDefault != null)
							map.put(DataFieldConstants.DEFAULT, paramDefault);
						map.put(DataFieldConstants.UNITS, paramUnits);
					}
				
					
					
					if (rootNode == null) {
						System.out.println("Creating root node...");
						rootNode = new DataFieldNode(map, tree);
						tree.setRootNode(rootNode);
					} else {
						System.out.println(" Adding childNode");
						DataFieldNode newNode = new DataFieldNode(map, tree);
						rootNode.addChild(newNode);
					}
					
					desc = "";
					fieldName = null;
					paramDefault = null;
					paramUnits = null;
				}
				
			}	// end of for-loop (all <p> elements)
			
			model.openTree((Tree)tree);
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		/*
		 * Delete the temp file (unless bug-fixing)
		 */
	   // tempFile.delete();
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
