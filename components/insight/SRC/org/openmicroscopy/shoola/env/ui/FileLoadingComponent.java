/*
 * org.openmicroscopy.shoola.env.ui.FileLoadingComponent 
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
package org.openmicroscopy.shoola.env.ui;


//Java imports
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToolBar;

//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * UI component displaying the status of the download.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class FileLoadingComponent 
	extends JPanel
	implements ActionListener
{

	/** Bound property indicating to cancel any ongoing data loading. */
	static final String 		CANCEL_PROPERTY = "cancel";
	
	/** Bound property indicating to remove the entry from the display. */
	static final String 		REMOVE_PROPERTY = "remove";
	
	/** ID to cancel any ongoing data loading. */
	private static final int 	CANCEL = 0;
	
	/** ID to remove the entry from the display. */
	private static final int 	REMOVE = 1;
	
	/** Message displayed when the downloading is completed. */
	private static final String DONE = "Downloaded in: ";
	
	/** Message displayed when the downloading is cancelled. */
	private static final String CANCEL_LOADING = "Cancelled";
		
	/** Button to cancel any ongoing data loading. */
	private JButton 	cancelButton;
	
	/** Button to remove the entry from the display. */
	private JButton 	removeButton;
	
	/** The id of the file to download. */
	private long 		fileID;
	
	/** The name of the file to download. */
	private String 		fileName;
	
	/** The directory where to download the file. */
	private String 		directory;
	
	/** Convenience reference to the icons manager. */
	private IconManager icons;
	
	/** Component notifying of progress. */
	private JPanel		barPane;
	
	/** Component notifying of progress. */
	private JPanel		textPane;
	
	/** Component hosting the buttons. */
	private JToolBar 	toolBar;
	
	/** Initializes the components. */
	private void initComponents()
	{
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(""+CANCEL);
		cancelButton.setOpaque(false);
		UIUtilities.unifiedButtonLookAndFeel(cancelButton);
		removeButton = new JButton("Remove");
		removeButton.setActionCommand(""+REMOVE);
		removeButton.addActionListener(this);
		removeButton.setOpaque(false);
		UIUtilities.unifiedButtonLookAndFeel(removeButton);
		double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL, 
						TableLayout.PREFERRED}, 
				{TableLayout.PREFERRED}};
		setLayout(new TableLayout(tl));
		
		barPane = new JPanel();
		barPane.setOpaque(false);
		barPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		double[][] size = {{TableLayout.FILL}, 
						{TableLayout.PREFERRED, TableLayout.PREFERRED}};
		barPane.setLayout(new TableLayout(size));
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setBorder(null);
		textPane = UIUtilities.buildComponentPanel(barPane);
	}
	
	/**
	 * Builds and lays out the UI. 
	 * 
	 * @param init 		Pass <code>true</code> to lay out the display when 
	 * 					the data loading starts, <code>false</code> when it is 
	 * 					done.
	 * @param message 	The message to display.
	 */
	private void buildGUI(boolean init, String message)
	{
		removeAll();
		barPane.removeAll();
		add(new JLabel(icons.getIcon(IconManager.DOCUMENT_32)), "0, 0");
		toolBar.removeAll();
		cancelButton.setVisible(false);
		if (init) {
			barPane.add(new JLabel(fileName), "0, 0, CENTER, CENTER");
			JProgressBar bar = new JProgressBar();
			bar.setIndeterminate(true);
			cancelButton.setVisible(true);
			bar.setBackground(barPane.getBackground());
			barPane.add(bar, "0, 1");
			toolBar.add(cancelButton);
			add(toolBar, "2, 0");
		} else {
			barPane.add(new JLabel(fileName), "0, 0, CENTER, CENTER");
			barPane.add(UIUtilities.setTextFont(message, Font.ITALIC, 10), 
												"0, 1, CENTER, CENTER");
			toolBar.add(removeButton);
			add(toolBar, "2, 0");
		}
		add(textPane, "1, 0");
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param directory	 	The directory where to load the file.
	 * @param fileName		The name of the file.
	 * @param fileID		The id of the file.
	 * @param icons			Reference to the icons manager.
	 */
	FileLoadingComponent(String directory, String fileName, 
						long fileID, IconManager icons)
	{
		if (icons == null)
			throw new IllegalArgumentException("No icons manager specified.");
		this.fileName = fileName;
		this.directory = directory;
		this.fileID = fileID;
		this.icons = icons;
		initComponents();
		buildGUI(true, null);
	}
	
	/**
	 * Returns <code>true</code> if the activity is still on-going,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isOngoingActivity() { return cancelButton.isVisible(); }
	
	/** 
	 * Lays out component depending of the passed percentage.
	 * 
	 * @param percent
	 */
	void setStatus(int percent)
	{
		if (percent == 0) buildGUI(false, DONE+directory);
		else if (percent == -1) buildGUI(false, CANCEL_LOADING);
		revalidate();
		repaint();
	}
	
	/**
	 * Returns the id of the file.
	 * 
	 * @return See above.
	 */
	long getFileID() { return fileID; }
	
	/**
	 * Returns the absolute path.
	 * 
	 * @return See above.
	 */
	String getAbsolutePath() { return directory+fileName; }
	
	/**
	 * Cancels the downloading.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				firePropertyChange(CANCEL_PROPERTY, -1, getAbsolutePath());
				break;
			case REMOVE:
				firePropertyChange(REMOVE_PROPERTY, -1, getAbsolutePath());
		}
	}

	/**
	 * Overridden to make sure that all the components have the correct 
	 * background.
	 * @see JPanel#setBackground(Color)
	 */
	public void setBackground(Color color)
	{
		super.setBackground(color);
		if (barPane != null) barPane.setBackground(color);
		if (textPane != null) textPane.setBackground(color);
		if (cancelButton != null) cancelButton.setBackground(color);
		if (removeButton != null) removeButton.setBackground(color);
		if (toolBar != null) toolBar.setBackground(color);
	}
	
}
