/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata.util;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXBusyLabel;

import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.env.data.model.DownloadAndZipParam;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import org.openmicroscopy.shoola.util.ui.tdialog.TinyDialog;
import omero.gateway.model.DataObject;
import omero.gateway.model.FileAnnotationData;

/** 
 * Component used to indicate that a given analysis was done.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class AnalysisResultsItem 
	extends JPanel
	implements ActionListener, PropertyChangeListener
{

	/** Bound property indicating to view the results. */ 
	public static final String ANALYSIS_RESULTS_VIEW = "analysisResultsView";
	
	/** Bound property indicating to view the results. */ 
	public static final String ANALYSIS_RESULTS_DELETE = "analysisResultsDelete";
	
	/** Bound property indicating to cancel the loading. */ 
	public static final String ANALYSIS_RESULTS_CANCEL = "analysisResultsCancel";
	
	/** The size of the loading icon. */
	private static final Dimension SIZE = new Dimension(14, 14);
	
	/** The default text. */
	private static final String DEFAULT = "Results";
	
	/** Action id indicating to delete the results. */
	private static final int DELETE = 0;
	
	/** Action id indicating to view the results. */
	private static final int VIEW = 1;
	
	/** Action id indicating to cancel the loading of the results. */
	private static final int CANCEL = 2;
	
	/** Action id indicating to view the results. */
	private static final int DOWNLOAD = 3;
	
	/** The collection of attachments related to the objects. */
	private List<FileAnnotationData> attachments;
	
	/** The data object to host. */
	private DataObject data;
	
	/** The name space. */
	private String		nameSpace;
	
	/** Button indicating to display the results. */
	private JButton		resultsButton;
	
	/** Button indicating to delete the results. */
	private JMenuItem	deleteButton;
	
	/** Button indicating to cancel the data loading. */
	private JButton		cancelButton;
	
	/** Display the menu. */
	private JButton		menuButton;
	
	/** Download the files. */
	private JMenuItem	downloadButton;
	
	/** Display the information about the analysis. */
	private JMenuItem	infoButton;
	
	/** The pop-up menu. */
	private JPopupMenu	popMenu;
	
	/** The loaded results. */
	private Map<FileAnnotationData, File> results;
	
	/** The time when the analysis was done. */
	private Timestamp time;
	
	/** 
	 * Brings up a dialog so that the user can select where to 
	 * download the file.
	 */
	private void download()
	{
		String name = null;
		if (data instanceof FileAnnotationData) {
			name = ((FileAnnotationData) data).getFileName();
		}
		JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		FileChooser chooser = new FileChooser(f, FileChooser.SAVE, 
				"Download", "Select where to download the files.", null, true);
		if (name != null && name.trim().length() > 0) 
			chooser.setSelectedFileFull(name);
		IconManager icons = IconManager.getInstance();
		chooser.setTitleIcon(icons.getIcon(IconManager.DOWNLOAD_48));
		chooser.setApproveButtonText("Download");
		chooser.addPropertyChangeListener(this);
		chooser.centerDialog();
	}
	
	/**
	 * Displays information about the analysis.
	 * 
	 * @param p The location of the mouse pressed.
	 */
	private void displayInformation(Point p)
	{
		StringBuffer buf = new StringBuffer();
		buf.append("<html><body>");
		buf.append("<b>Analysis Run: </b>"+UIUtilities.formatTime(time));
		buf.append("<br>");
		buf.append("<b>Number of files: </b>"+attachments.size());
		buf.append("<br>");
		Iterator<FileAnnotationData> i = attachments.iterator();
		while (i.hasNext()) {
			buf.append(i.next().getFileName());
			buf.append("<br>");
		}
		buf.append("</body></html>");
		JLabel l = new JLabel();
		l.setText(buf.toString());
		TinyDialog d = new TinyDialog(null, l, TinyDialog.CLOSE_ONLY);
		d.setModal(true);
		d.getContentPane().setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
		SwingUtilities.convertPointToScreen(p, this);
		d.pack();
		d.setLocation(p);
		d.setVisible(true);
	}
	
	/** 
	 * Brings up the menu. 
	 * 
	 * @param invoker The component where the clicks occurred.
	 * @param p The location of the mouse pressed.
	 */
	private void showMenu(JComponent invoker, Point p)
	{
		if (popMenu == null) {
			popMenu = new JPopupMenu();
			popMenu.add(deleteButton);
			popMenu.add(downloadButton);
			popMenu.add(infoButton);
		}
		popMenu.show(invoker, p.x, p.y);
	}
	
	/**
	 * Converts the specified name space.
	 * 
	 * @param nameSpace The value to handle.
	 * @return See above.
	 */
	private String convertNameSpace(String nameSpace)
	{
		if (nameSpace == null || nameSpace.trim().length() == 0)
			return DEFAULT;
		String[] values = UIUtilities.splitString(nameSpace);
		return values[values.length-1].toUpperCase();
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		add(resultsButton);
		//add(deleteButton);
		add(menuButton);
	}
	
	/** 
	 * Initializes the components. 
	 * 
	 * @param nameSpace The name space to use to determine the name.
	 * @param index The value of the results.
	 */
	private void initComponents(String nameSpace, int index)
	{
		attachments = new ArrayList<FileAnnotationData>();
		this.nameSpace = nameSpace;
		resultsButton = new JButton();
		resultsButton.setText(convertNameSpace(nameSpace)+" #"+index);
		resultsButton.setOpaque(false);
		resultsButton.setForeground(UIUtilities.HYPERLINK_COLOR);
		resultsButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		UIUtilities.unifiedButtonLookAndFeel(resultsButton);
		resultsButton.setActionCommand(""+VIEW);
		resultsButton.addActionListener(this);
		
		cancelButton = new JButton();
		cancelButton.setText("Cancel");
		cancelButton.setToolTipText("Cancel results loading.");
		cancelButton.setOpaque(false);
		cancelButton.setForeground(UIUtilities.HYPERLINK_COLOR);
		cancelButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		UIUtilities.unifiedButtonLookAndFeel(cancelButton);
		cancelButton.setActionCommand(""+CANCEL);
		cancelButton.addActionListener(this);
		
		IconManager icons = IconManager.getInstance();
		deleteButton = new JMenuItem(icons.getIcon(IconManager.DELETE_12));
		deleteButton.setText("Delete");
		deleteButton.setToolTipText("Delete the results.");
		deleteButton.setActionCommand(""+DELETE);
		deleteButton.addActionListener(this);
		downloadButton = new JMenuItem(icons.getIcon(
				IconManager.DOWNLOAD_12));
		downloadButton.setText("Download...");
		downloadButton.setToolTipText("Download the selected file.");
		downloadButton.setActionCommand(""+DOWNLOAD);
		downloadButton.addActionListener(this);
		
		menuButton = new JButton(icons.getIcon(IconManager.UP_DOWN_9_12));
		UIUtilities.unifiedButtonLookAndFeel(menuButton);
		menuButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		menuButton.addMouseListener(new MouseAdapter() {
			
			public void mousePressed(MouseEvent e)
			{
				Point p = e.getPoint();
				showMenu(menuButton, p);
			}
		});
		infoButton = new JMenuItem(icons.getIcon(IconManager.INFO));
		infoButton.setText("Info...");
		infoButton.addMouseListener(new MouseAdapter() {
			
			public void mousePressed(MouseEvent e)
			{
				Point p = e.getPoint();
				displayInformation(p);
			}
		});
		
		setBackground(UIUtilities.BACKGROUND_COLOR);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param data The data object to host.
	 * @param nameSpace The name space to use to determine the name.
	 * @param index The index of the results.
	 */
	public AnalysisResultsItem(DataObject data, String nameSpace, int index)
	{
		if (data == null) 
			throw new IllegalArgumentException("Object cannot be null.");
		this.data = data;
		initComponents(nameSpace, index);
		buildGUI();
	}
	
	/**
	 * Returns the name space.
	 * 
	 * @return See above.
	 */
	public String getNameSpace() { return nameSpace; }
	
	/**
	 * Adds a new attachment. 
	 * 
	 * @param file The attachment to add.
	 */
	public void addAttachment(FileAnnotationData file)
	{
		long userID = MetadataViewerAgent.getUserDetails().getId();
		if (time == null) {
			time = file.getLastModified();
			resultsButton.setToolTipText("Analysis run "+
					UIUtilities.formatTime(time));
		}
		if (file.getOwner().getId() == userID)
			deleteButton.setVisible(true);
		if (!attachments.contains(file))
			attachments.add(file);
	}
	
	/**
	 * Returns the data object hosted by this component.
	 * 
	 * @return See above.
	 */
	public DataObject getData() { return data; }
	
	/**
	 * Returns the attachments.
	 * 
	 * @return See above.
	 */
	public List<FileAnnotationData> getAttachments() { return attachments; }
	
	/**
	 * Sets the loaded files.
	 * 
	 * @param results The value to set.
	 */
	public void setLoadedFiles(Map<FileAnnotationData, File> results)
	{
		this.results = results;
	}
	
	/**
	 * Returns the loaded results.
	 * 
	 * @return See above.
	 */
	public Map<FileAnnotationData, File> getResults() { return results; }
	
	/**
	 * Indicates on-going loading or not.
	 * 
	 * @param load Pass <code>true</code> to load, <code>false</code> otherwise.
	 */
	public void notifyLoading(boolean load)
	{
		removeAll();
		if (load) {
			JXBusyLabel label = new JXBusyLabel(SIZE);
			label.setBusy(true);
			label.setEnabled(true);
			add(label);
			add(cancelButton);
		} else {
			//resultsButton.setEnabled(false);
			add(resultsButton);
			add(menuButton);
		}
		revalidate();
		repaint();
	}
	
	/**
	 * Fires a property when the user clicks to view or delete the object.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case DELETE:
				firePropertyChange(ANALYSIS_RESULTS_DELETE, null, this);
				break;
			case VIEW:
				firePropertyChange(ANALYSIS_RESULTS_VIEW, null, this);
				break;
			case CANCEL:
				firePropertyChange(ANALYSIS_RESULTS_CANCEL, null, this);
				break;
			case DOWNLOAD:
				download();
		}
	}
	
	/**
	 * Listens to property fired by the Editor dialog.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
			File[] files = (File[]) evt.getNewValue();
			File folder = files[0];
			if (folder == null)
				folder = UIUtilities.getDefaultFolder();
			UserNotifier un = MetadataViewerAgent.getRegistry().getUserNotifier();
			
			if (attachments == null || attachments.size() == 0) return;
			IconManager icons = IconManager.getInstance();
			DownloadAndZipParam param = new DownloadAndZipParam(attachments, 
					folder, icons.getIcon(IconManager.DOWNLOAD_22));
			//TODO: review
			//un.notifyActivity(param);
		}
	}
	
	/**
	 * Overridden to return the text associated to this component.
	 * @see JMenuItem#toString()
	 */
	public String toString()
	{
		return resultsButton.getText();
	}

}
