
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
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.openmicroscopy.shoola.util.ui.TitlePanel;

import ui.IModel;
import util.FileDownload;
import util.ImageFactory;
import xmlMVC.XMLModel;

/**
 * This pop-up panel (displayed in it's own JFrame) displays a web page:
 * @see DEMO_FILES_URL
 * This has links to several demo OMERO.editor files. Clicking on each link displays the link
 * in a text box (or the user could enter their own URL).
 * When user clicks "Import", the file at the URL is downloaded and opened. 
 * 
 * @author will
 */
public class UrlChooser extends JPanel {
	
	/**
	 * A reference to the application model, for opening a file
	 */
	protected IModel model;
	
	/**
	 * The JFrame that displays this pop-up dialog
	 */
	protected JFrame frame;
	
	/**
	 * A text field to display the selected URL, or allow users to type their own.
	 */
	protected JTextField urlField;
	
	/**
	 * A counter, to give new files temporary names. download1, download2 etc...
	 */
	private static int newFileIndex = 1;
	
	/**
	 * The URL of the web-page that contains links to downloadable files. 
	 * Clicking on the links in this page will place the link URL in the urlField. 
	 */
	public static final String DEMO_FILES_URL = "http://cvs.openmicroscopy.org.uk/snapshots/omero/editor/demoFiles/demoFiles.html";
	
	/**
	 * Creates an instance. 
	 * Saves a reference to IModel, then builds and displays the UI.
	 * @param model
	 */
	public UrlChooser(IModel model) {
		
		this.model = model;
		
		buildAndDisplayUI();
	}
	
	/**
	 * Builds the JPanel UI, then displays it in a new JFrame. 
	 */
	public void buildAndDisplayUI() {
		
		setLayout(new BorderLayout());
		
		int panelWidth = 600;
		
		String headerMessage= "Choose an example file to open.";
		Icon headerIcon = ImageFactory.getInstance().getIcon(ImageFactory.KORGANIZER_ICON);
		
		//this.setPreferredSize(new Dimension(panelWidth, 400));
		
		// Header.
		TitlePanel titlePanel = new TitlePanel("Import Demo File", headerMessage, headerIcon);
		add(titlePanel, BorderLayout.NORTH);
		
		// Editor Pane to display the web-page
		JEditorPane webPage = new JEditorPane();
		try {
			webPage.setPage(DEMO_FILES_URL);
		} catch (IOException e1) {
			// Warn the user that they may not be online.
			JOptionPane.showMessageDialog(this, "Could not find the download web-page. \n" +
					"Please check your internet connection.",
					"Problem loading download page", JOptionPane.WARNING_MESSAGE);
		}
		webPage.setEditable(false);
		webPage.addHyperlinkListener(new DownloadHyperLinkListener());
		
		// ... in a scroll pane
		Dimension scrollPaneSize = new Dimension(panelWidth, 350);
		JScrollPane scrollPane = new JScrollPane(webPage, 
				 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(scrollPaneSize);
		scrollPane.setMinimumSize(scrollPaneSize);
		
		this.add(scrollPane, BorderLayout.CENTER);
		
		
		// ...above a text-box showing URL
		urlField = new JTextField("http://");
		
		// Buttons
		JButton importButton = new JButton("Import");
		importButton.addActionListener(new DownloadFileListener());
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
		
		Box verticalBox = Box.createVerticalBox();
		verticalBox.add(urlField);
		verticalBox.add(buttonBoxContainer);
		this.add(verticalBox, BorderLayout.SOUTH);
	
		displayInFrame(this);
	}
	
	/**
	 * A hyperlink listener that simply puts the URL of the selected link into the 
	 * urlField text box. 
	 * 
	 * @author will
	 */
	public class DownloadHyperLinkListener implements HyperlinkListener {
		public void hyperlinkUpdate (HyperlinkEvent event) {
			if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				if (event != null) {
					String url = event.getURL().toString();
					if (url != null) {
						urlField.setText(url);
					}
				}
			}
		}
	}
	
	/**
	 * The listener for the "Import" button. 
	 * Takes the text from the urlField text box, and calls downloadUrl(url);
	 * 
	 * @author will
	 *
	 */
	public class DownloadFileListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			/*
			 * If the down-load is successful, close this window. 
			 */
			if (downloadUrl(urlField.getText())) 
				frame.setVisible(false);
		}
	}
	
	/**
	 * Creates a new file by down-loading from the given URL. 
	 * Passes this to the model, to open a new file. 
	 * 
	 * @param url	The URL to down-load and open. 
	 * @return		true if the down-load is OK (file found OK etc). 
	 */
	public boolean downloadUrl(String url) {
		
		try {
			
			String newFileName = XMLModel.OMERO_EDITOR_FILE + "/download" + newFileIndex++ ;
			
			File downloadedFile = FileDownload.downloadFile(url, newFileName);
			model.openThisFile(downloadedFile);
			downloadedFile.delete();
			
			return true;
		} catch (MalformedURLException ex) {
			JOptionPane.showMessageDialog(frame, "Invalid URL, please try again", 
					"Invalid URL", JOptionPane.WARNING_MESSAGE);
			return false;
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(frame, "Could not open file. \n" +
					"URL may be incorrect, or internet connection failed.",
					"Could not open file", JOptionPane.WARNING_MESSAGE);
			return false;
		}
	}
	
	/**
	 * Displays this JPanel class in a JFrame. 
	 * @param panel
	 */
	public void displayInFrame(JPanel panel) {
		
		frame = new JFrame();
		
		frame.getContentPane().add(panel);
		
		frame.pack();
		frame.setLocation(100, 50);
		frame.setVisible(true);
	}

}
