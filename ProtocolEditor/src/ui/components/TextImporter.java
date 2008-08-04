
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

//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//Third-party libraries

//Application-internal dependencies

import tree.DataFieldConstants;
import tree.DataFieldNode;
import tree.Tree;
import ui.IModel;
import ui.components.htmlActions.InsertSpanAction;
import util.BareBonesBrowserLaunch;
import util.ImageFactory;
import util.XMLMethods;
import xmlMVC.ConfigConstants;

/** 
* The Text importer is a dialog containing a JEditorPane, in which users can 
* paste and edit text for importing. 
* The JEditorPane is a SimpleHTMLEditorPane, add editing actions defined by
* the TextImporter class are used to format the html, highlighting text
* that should be used for field titles, parameters and units. 
* Upon import, the html (as XHTML) is converted to a DOM document and 
* parsed to retrieve this defined text, which is used to build a "flat"
* Tree of OMERO.editor fields (All fields are children of the root), which
* is then opened in the main OMERO.editor window. 
*
* @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $Date: $)
* </small>
* @since OME3.0
*/

public class TextImporter 
	extends ImportDialog {

	
	/**
	 * The HTML tag for paragraph. Used to retrieve all the p elements from 
	 * the DOM document.
	 */
	public static final String P = "p";
	
	/**
	 * The HTML span tag. Used to retrieve all the span elements from DOM doc.
	 */
	public static final String SPAN = "span";
	
	/**
	 * A class name, added as an attribute to the span tag. Used to identify
	 * span elements that highlight the an experimental parameter. 
	 */
	public static final String PARAM = "param";
	
	/**
	 * A class name, added as an attribute to the span tag. Used to identify
	 * span elements that highlight the units of a parameter. 
	 */
	public static final String UNITS = "units";
	
	/**
	 * The number of characters of text (taken from the description) to use
	 * as the field title, if no field title is defined by the user. 
	 */
	public static final int MAX_NAME_CHARS = 40;
	
	/**
	 * The pixel spacing between buttons in the toolbar. 
	 */
	public static final int BUTTON_SPACING = 4;
	
	
	/**
	 * Creates an instance of this class. 
	 * After initialising and building the UI (done by the superclass
	 * constructor), this method creates a toolBar and a help button, 
	 * which are added to the 
	 * titleAndToolbarContainer.
	 * 
	 * @param model		The model required for opening new files etc. 
	 */
	public TextImporter(IModel model) {
		
		super(model, "Import Text");
		
		
		// add a tool bar..
		Box toolbarBox = Box.createHorizontalBox();
		Border eb = new EmptyBorder(3,2,3,2);
		Border bb = BorderFactory.createRaisedBevelBorder();
		Border tb = BorderFactory.createCompoundBorder(bb, eb);
		
		toolbarBox.setBorder(eb);
		
		/*
		 * ...with buttons for the following HTML-editing Actions:
		 * 
		 * This adds a <b> tag, and also adds a red background, using a <
		 * font> tag. 
		 * Used to highlight field titles. 
		 */
		Action boldRed = ((SimpleHTMLEditorPane)textArea)
				.getHtmlEditorKitAction("BoldRed");
		JButton fieldTitle = new JButton(boldRed);
		fieldTitle.setText("<html><span <span style='color: red ; font-weight: bold'>" +
				"Field Title</span></html>");
		fieldTitle.setToolTipText("Specify a custom field title");
		fieldTitle.setBorder(tb);
		toolbarBox.add(fieldTitle);
		
		toolbarBox.add(Box.createHorizontalStrut(BUTTON_SPACING));
		toolbarBox.add(new JSeparator(SwingConstants.VERTICAL));
		toolbarBox.add(Box.createHorizontalStrut(BUTTON_SPACING));
		
		/*
		 * This Action adds a <span> tag, with an orange background.
		 * The tag has a class="param" attribute, and is used to 
		 * highlight experimental parameters. 
		 */
		Action paramAction = new InsertSpanAction("Parameter", PARAM, 
				new Color(226, 92, 2));
		JButton paramButton = new JButton(paramAction);
		paramButton.setBorder(tb);
		paramButton.setBackground(new Color(226, 92, 2));
		toolbarBox.add(paramButton);
		toolbarBox.add(Box.createHorizontalStrut(BUTTON_SPACING));
		
		/*
		 * This Action adds a <span> tag, with a yellow background.
		 * The tag has a class="units" attribute, and is used to 
		 * highlight field units. 
		 */
		Action unitsAction = new InsertSpanAction("Units", UNITS, Color.yellow);
		JButton unitsButton = new JButton(unitsAction);
		unitsButton.setBorder(tb);
		unitsButton.setBackground(Color.yellow);
		toolbarBox.add(unitsButton);
		toolbarBox.add(Box.createHorizontalStrut(BUTTON_SPACING));
		
		/*
		 * This Action adds a <span> tag, with a white background.
		 * The tag has no attributes, and is used to 
		 * clear the highlighting of Parameters and Units.
		 */
		Action clearAction = new InsertSpanAction("Clear", null, Color.white);
		JButton clearButton = new JButton(clearAction);
		clearButton.setBorder(tb);
		clearButton.setBackground(null);
		toolbarBox.add(clearButton);
		
		toolbarBox.add(new JPanel());	// so to align buttons to left.
		
		/*
		 * A help function to the online guide at
		 * http://trac.openmicroscopy.org.uk/shoola/wiki/TextImport
		 */
		Icon infoIcon = ImageFactory.getInstance().getIcon(ImageFactory.INFO_ICON);
		JButton helpButton = new JButton("Online help", infoIcon);
		helpButton.setToolTipText("Open an on-line guide to text importing");
		helpButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				BareBonesBrowserLaunch.openURL(
					"http://trac.openmicroscopy.org.uk/shoola/wiki/TextImport");
			}
		});
		
		JPanel toolBarContainer = new JPanel(new BorderLayout());
		toolBarContainer.add(toolbarBox, BorderLayout.WEST);
		toolBarContainer.add(helpButton, BorderLayout.EAST);
		
		textAndToolbarContainer.add(toolBarContainer, BorderLayout.NORTH);
		
	}
	
	public void setHeaderComponents() {
		
		setOkButtonText("Import Text");
		
		setSubTitle("Please paste the text you wish to import into " +
		"the text area below."); 

		setHeaderMessage("Each paragraph will become a new field when imported. \n" +
				"The text will become the field description. \n" +
				"The field title will be the first " + MAX_NAME_CHARS + " characters " +
				"of the description, unless specified.\n" +
		"Additional parameters and units can also be specified.");
 
		setHeaderIcon(ImageFactory.getInstance().getIcon(
				ImageFactory.KORGANIZER_ICON));
	}
	
	/**
	 * The text component is initialised as an instance of 
	 * SimpleHTMLEditorPane, a JEditorPane that uses an HTML editor kit. 
	 */
	public JTextComponent initialiseTextArea() {
		
		/*
		 * A new SimpleHTMLEditorPane.
		 */
		textArea = new SimpleHTMLEditorPane();
		try {
			// have to use this method, as setText() changes the Editor kit to
			// a non-HTML editor kit. 
			textArea.getDocument().insertString(0, "Paste text here", null);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		
		return textArea;
	}
	
	/**
	 * The import method involves saving the HTML text as a temporary text file, 
	 * which allows it to be converted into a DOM document. 
	 * The DOM document is then parsed to obtain all the paragraphs (P elements).
	 * Currently, paragraphs have no padding, so a user is forced to 
	 * separate paragraphs with "blank" paragraphs (paragraph with no text). 
	 * The parsing uses these as markers between fields.
	 * Each collection of p elements that are combined to create a field are
	 * queried for span elements, of class "param" or "units". 
	 * If present, these will affect the type of field that is created
	 * eg Number or Text field, and the text in these elements
	 * is used to populate the Default and Units attributes.
	 */
	public void actionPerformed(ActionEvent evt) {
		
		/*
		 * Get the HTML text from the text area, and write it to a 
		 * temporary file...
		 */
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
	
		
		/*
		 * ... Read the file as a DOM document. 
		 */
		try {
			Document xhtmlDoc = XMLMethods.readXMLtoDOM(tempFile);
			
			/*
			 * Get all the <p> elements (paragraphs) in a list.
			 */
			NodeList paragraphs = xhtmlDoc.getElementsByTagName(P);
			
			String newLine =  "";
			
			/*
			 * Define the root node of a tree.
			 * This will get instantiated by the first paragraph of the NodeList
			 * Subsequent paragraphs are added as children of this node. 
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
				
				//System.out.println(newLine);
				
				/*
				 * If this element has some text, it is not a break between
				 * paragraphs...
				 */
				if (newLine.length() > 0) {
					
					/*
					 * Concatenate all the lines from a contiguous <p> elements
					 * to form the description of a single field. 
					 */
					if (desc.length() > 0)
						desc = desc + "<br>";
					
					desc = desc + newLine;
					
					/*
					 * Does this element have children? Get the <b>
					 * elements and concatenate their text content...
					 */
					NodeList bolds = paraElement.getElementsByTagName("b");
					for (int b =0; b < bolds.getLength(); b++) {
						Element bold = (Element)bolds.item(b);
						if (fieldName == null) fieldName = bold.getTextContent();
						else fieldName = fieldName + " " + bold.getTextContent();
					}
					
					/*
					 * Go through <span> elements, looking for...
					 */
					NodeList spans = paraElement.getElementsByTagName("span");
					for (int s=0; s<spans.getLength(); s++) {
						Element span = (Element)spans.item(s);
						String className = span.getAttribute("class");
						/*
						 * Look for <span> elements that have class="param" 
						 * Just save the first value. Ignore others.
						 */
						if ("param".equals(className) && (paramDefault == null)) {
							paramDefault = span.getTextContent().trim();
						} 
						/*
						 * Does this field have units? 
						 * Check to see if this <span> defines units...
						 * Just save the first value. Ignore others.
						 */
						else if ("units".equals(className) && (paramUnits == null)) {
							paramUnits = span.getTextContent().trim();
						}
						
					}
					
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
						if (desc.length() < MAX_NAME_CHARS) {
							fieldName = desc;
							desc = "";
						} else {
							fieldName = desc.substring(0, MAX_NAME_CHARS - 2) + "...";
						}
					}
					if (fieldName != null)
						map.put(DataFieldConstants.ELEMENT_NAME, fieldName);
					if (desc.length() > 0) 
						map.put(DataFieldConstants.DESCRIPTION, desc);
				
					/*
					 * Define the type of field, based on the attributes found
					 */
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
				
					/*
					 * If the rootNode is null, this must be the first 
					 * field created. Therefore, instantiate the root and
					 * add it to the tree. 
					 */
					if (rootNode == null) {
						//System.out.println("Creating root node...");
						rootNode = new DataFieldNode(map, tree);
						tree.setRootNode(rootNode);
						
					/*
					 * Otherwise, the root node has been created, so add the
					 * new field as a child of the root. 
					 */
					} else {
						//System.out.println(" Adding childNode");
						DataFieldNode newNode = new DataFieldNode(map, tree);
						rootNode.addChild(newNode);
					}
					
					/*
					 * Having added a field, these values are reset for 
					 * a new field. 
					 */
					desc = "";
					fieldName = null;
					paramDefault = null;
					paramUnits = null;
					
				}	// end of if (newLine.length == 0)
				
			}	// end of for-loop (all <p> elements)
			
			model.openTree((Tree)tree);
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		 * Finally, delete the temp file (unless bug-fixing)
		 */
	    tempFile.delete();
	}
}
