/*
 * org.openmicroscopy.shoola.agents.editor.uiComponents.UrlChooser
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package org.openmicroscopy.shoola.agents.editor.uiComponents;

//Java imports

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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.util.FileDownload;
import org.openmicroscopy.shoola.agents.editor.view.Editor;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.filter.file.EditorFileFilter;
import org.openmicroscopy.shoola.util.ui.BrowserLauncher;
import org.openmicroscopy.shoola.util.ui.TitlePanel;

/**
 * This pop-up panel (displayed in it's own JFrame) displays a web page:
 * @see DEMO_FILES_URL
 * This has links to several demo OMERO.editor files. Clicking on each link displays the link
 * in a text box (or the user could enter their own URL).
 * When user clicks "Import", the file at the URL is downloaded and opened. 
 * 
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class UrlChooser 
	extends JPanel 
	implements ActionListener,
	HyperlinkListener
{
	
	/**  A reference to the {@link Editor} model, for opening a file */
	private Editor 					model;
	
	/** The JFrame that displays this pop-up dialog */
	protected JFrame 				frame;
	
	/**
	 * The button that starts the download and import. 
	 */
	private JButton					importButton;
	
	/**
	 * The URL of the web-page that contains links to downloadable files.  
	 * Clicking on the links in this page will place the link URL in the urlField. 
	 */
	private String 					demoFilesUrl;

	private static int 				fileNameIncrementer = 1;
	/**
	 * A text field to display the selected URL, or allow users 
	 * to type their own.
	 */
	protected JTextField 			urlField;
	
	/**
	 * Displays this JPanel class in a JFrame. 
	 * @param panel
	 */
	private void displayInFrame(JPanel panel) {
		
		frame = new JFrame();
		
		frame.getContentPane().add(panel);
		
		frame.pack();
		frame.setLocation(100, 50);
		frame.setVisible(true);
	}

	/**
	 * Creates a new file by down-loading from the given URL. 
	 * Passes this to the model, to open a new file. 
	 * 
	 * @param url	The URL to down-load and open. 
	 * @return		true if the down-load is OK (file found OK etc). 
	 */
	private boolean downloadUrl(String url) {
		
		Registry reg = EditorAgent.getRegistry();
		UserNotifier un = reg.getUserNotifier();
		
		try {
			int lastSlash = url.lastIndexOf("/");
			if (lastSlash < 0) return false;	// can't be valid url
			String newFileName = url.substring(lastSlash);
			if (new File(newFileName).exists()) {	// just in case! 
				// rename eg. file.cpe.xml to file1.cpe.xml
				String fileExt = "."+EditorFileFilter.CPE_XML;
				if (newFileName.contains(fileExt)) {
					newFileName = newFileName.replace(fileExt,
									fileNameIncrementer++ + fileExt);
				} else
				newFileName = newFileName + fileNameIncrementer++;
			}
			String dirName = EditorAgent.getEditorHome();
			String filePath = dirName + newFileName;
			File downloadedFile = FileDownload.downloadFile(url, filePath);
			model.openLocalFile(downloadedFile);
			// set to edited, so that Save button is activated. 
			//model.setEdited(true);
			downloadedFile.delete();
			
			return true;
		} catch (MalformedURLException ex) {
			// notify user
		    un.notifyInfo("Invalid URL, please try again", 
					"Invalid URL");
			return false;
		} catch (IOException ex) {
			un.notifyInfo("Could not open file. \n" +
					"URL may be incorrect, or internet connection failed.",
					"Could not open file");
			return false;
		} catch (IllegalArgumentException ex) {
			un.notifyInfo("Invalid URL", 
			"Invalid URL, please try again");
			return false;
		}
	}

	/**
	 * Creates an instance. 
	 * Saves a reference to IModel, then builds and displays the UI.
	 * 
	 * @param model			The {@link Editor} model for opening a file
	 */
	public UrlChooser(Editor model) {
		
		this.model = model;
		
		demoFilesUrl = (String)EditorAgent.getRegistry().lookup("/demo/index");
		
		buildAndDisplayUI();
	}
	
	/**
	 * Builds the JPanel UI, then displays it in a new JFrame. 
	 */
	public void buildAndDisplayUI() {
		
		setLayout(new BorderLayout());
		
		int panelWidth = 700;
		
		String headerMessage= "Choose an example file to open.";
		Icon headerIcon = IconManager.getInstance().getIcon(
				IconManager.WWW_FOLDER_ICON_48);
		
		//this.setPreferredSize(new Dimension(panelWidth, 400));
		
		// Header.
		TitlePanel titlePanel = new TitlePanel("Import Demo File", headerMessage, headerIcon);
		add(titlePanel, BorderLayout.NORTH);
		
		// Editor Pane to display the web-page
		JEditorPane webPage = new JEditorPane();
		try {
			webPage.setPage(demoFilesUrl);
		} catch (IOException e1) {
			// Warn the user that they may not be online.
			Registry registry = EditorAgent.getRegistry();
			registry.getUserNotifier().notifyInfo(
					"Problem accessing online files",
					"Could not access the online example files. \n" +
					"Please check your internet connection.");
			
			return;
		}
		webPage.setEditable(false);
		webPage.addHyperlinkListener(this);
		
		// ... in a scroll pane
		Dimension scrollPaneSize = new Dimension(panelWidth, 450);
		JScrollPane scrollPane = new JScrollPane(webPage, 
				 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(scrollPaneSize);
		scrollPane.setMinimumSize(scrollPaneSize);
		
		this.add(scrollPane, BorderLayout.CENTER);
		
		
		// ...above a text-box showing URL
		urlField = new JTextField("http://");
		
		// Buttons
		importButton = new JButton("Import");
		importButton.setEnabled(false); 	// until link is clicked
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
		
		Box verticalBox = Box.createVerticalBox();
		verticalBox.add(urlField);
		verticalBox.add(buttonBoxContainer);
		this.add(verticalBox, BorderLayout.SOUTH);
	
		displayInFrame(this);
	}
	
	/**
	 * Implemented as specified by the {@link ActionListener} interface.
	 * Handles the "Load" button action, starting file download. 
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		/*
		 * If the down-load is successful, close this window. 
		 */
		if (downloadUrl(urlField.getText())) 
			frame.setVisible(false);
	}
	
	/**
	 * Implemented as specified by the {@link HyperlinkListener} interface.
	 * Loads any link clicked into the {@link #urlField};
	 * 
	 * @see HyperlinkListener#hyperlinkUpdate(HyperlinkEvent)
	 */
	public void hyperlinkUpdate (HyperlinkEvent event) {
		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			if (event != null) {
				String url = event.getURL().toString();
				if (url != null) {
					
					boolean cpeFile = url.endsWith(EditorFileFilter.CPE_XML);
					
					if (cpeFile) {
						urlField.setText(url);
						importButton.setEnabled(true);
					}
					else {
						new BrowserLauncher().openURL(url);
					}
				}
			}
		}
	}

}
