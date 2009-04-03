/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import util.ImageFactory;

/**
 * This is a class similar to AttributeMemoEditor, except this one doesn't deal with dataFields.
 * This is simply a html formatting and editing panel, that you can getText from, 
 * or add property change listeners to
 */

public class SimpleHTMLEditorPanel extends JPanel{

	public static final String HTML_TEXT = "htmlText";
	public static final String HTML_TEXT_NO_BODY_TAG_OR_HTML_TAG = "htmlTextNoBodyTagOrHtmlTag";
	
	boolean textChanged = false;
	
	SimpleHTMLEditorPane editorPane;
	Box toolBarBox;
	Border toolBarButtonBorder;
	
	TextChangedListener textChangedListener = new TextChangedListener();
	FocusListener focusChangedListener = new FocusChangedListener();

	protected JPanel topPanel;
	
	public SimpleHTMLEditorPanel(String title) {
		this();
		JLabel titleLabel = new JLabel(title);
		addComponentOppositeToolBar(titleLabel);
	}
	
	public SimpleHTMLEditorPanel() {
		
		this.setBorder(new EmptyBorder(3,3,3,3));
		
		editorPane = new SimpleHTMLEditorPane();
		editorPane.addKeyListener(textChangedListener);
		editorPane.addFocusListener(focusChangedListener);
		
		int spacing = 2;
		toolBarButtonBorder = new EmptyBorder(spacing,spacing,spacing,spacing);
		toolBarBox = Box.createHorizontalBox();
		
		Icon boldIcon = ImageFactory.getInstance().getIcon(ImageFactory.BOLD_ICON); 
		JButton boldButton = new JButton(boldIcon);
		boldButton.setFocusable(false);		// don't want to lose focus from the editorPane when clicked
		boldButton.setActionCommand(SimpleHTMLEditorPane.FONT_BOLD);
		boldButton.addActionListener(new FontFormattingChangedListener());
		boldButton.setBorder(toolBarButtonBorder);
		toolBarBox.add(boldButton);
		
		Icon underlineIcon = ImageFactory.getInstance().getIcon(ImageFactory.UNDERLINE_ICON); 
		JButton underlineButton = new JButton(underlineIcon);
		underlineButton.setFocusable(false);		// don't want to lose focus from the editorPane when clicked
		underlineButton.setActionCommand(SimpleHTMLEditorPane.FONT_UNDERLINE);
		underlineButton.addActionListener(new FontFormattingChangedListener());
		underlineButton.setBorder(toolBarButtonBorder);
		toolBarBox.add(underlineButton);
		
		topPanel = new JPanel(new BorderLayout());
		topPanel.add(toolBarBox, BorderLayout.EAST);
		
		this.setLayout(new BorderLayout());
		this.add(topPanel, BorderLayout.NORTH);
		this.add(editorPane, BorderLayout.CENTER);
	}
	
	public void addComponentOppositeToolBar(JComponent newComponent) {
		topPanel.add(newComponent, BorderLayout.WEST);
	}
	
	public String getTextAreaText() {
		return editorPane.getText();
	}
	public void setTextAreaText(String text) {
		editorPane.addHtmlTagsAndSetText(text);
	}
	public JEditorPane getTextArea() {
		return editorPane;
	}
	public void addToToolBar(JComponent component) {
		component.setBorder(toolBarButtonBorder);
		toolBarBox.add(component);
	}

	
	/*
	 * Listens to buttons for changing the editorPane formatting eg Underline, bold.
	 * First carries out the appropriate Action
	 * Then updates the dataField with newly modified text from the editorPane
	 */
	public class FontFormattingChangedListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			// the ActionCommand of the button corresponds to the name of the Action to be 
			// carried out by the editorPane. 
			String actionCommand = e.getActionCommand();
			
			// change the formatting of the text in the editorPane
			editorPane.getHtmlEditorKitAction(actionCommand).actionPerformed(e);
			
			// notify listeners of the changes 
			textChanged();
		}
	}
	
	// notify listeners of a change to the text
	public void textChanged() {
		
		firePropertyChange(HTML_TEXT, "", editorPane.getText());
		firePropertyChange(HTML_TEXT_NO_BODY_TAG_OR_HTML_TAG, "", editorPane.getTextNoBodyTagOrHtmlTag());
		
		textChanged = false;
	}

	public class TextChangedListener implements KeyListener {
		
		public void keyTyped(KeyEvent event) {
			textChanged = true;		// some character was typed, so set this flag
		}
		public void keyPressed(KeyEvent event) {}
		public void keyReleased(KeyEvent event) {}
	
	}
	
	public class FocusChangedListener implements FocusListener {
		
		public void focusLost(FocusEvent event) {
			if (textChanged) {
				textChanged();
			}
		}
		public void focusGained(FocusEvent event) {}
	}
	
	public String getText() {
		return editorPane.getText();
	}
	
	public String getTextNoBodyTagOrHtmlTag() {
		return editorPane.getTextNoBodyTagOrHtmlTag();
	}
	
	public SimpleHTMLEditorPane getEditorPane() {
		return editorPane;
	}
	
	public static void main(String[] args) {
		
		JPanel simpleHTMLEditorPanel = new SimpleHTMLEditorPanel("Test");
		simpleHTMLEditorPanel.addPropertyChangeListener(HTML_TEXT_NO_BODY_TAG_OR_HTML_TAG, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println(HTML_TEXT_NO_BODY_TAG_OR_HTML_TAG);
            	System.out.println(evt.getNewValue());
            }
        });
		
		JButton testGetTextButton = new JButton("Take focus from editor");

		
		JFrame frame = new JFrame("Drop Down Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add("Center", simpleHTMLEditorPanel);
		frame.getContentPane().add("North", testGetTextButton);
		frame.pack();
		frame.setSize(new Dimension(300, 300));
	    frame.setVisible(true);
	}
}