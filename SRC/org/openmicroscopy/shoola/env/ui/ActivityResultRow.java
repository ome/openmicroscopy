/*
 * org.openmicroscopy.shoola.env.ui.ActivityResultRow 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import omero.model.OriginalFile;
import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.ProjectData;


/** 
 * Displays a result.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class ActivityResultRow 
	extends JPanel
	implements ActionListener
{

	static final String ACTION_PROPERTY = "action";
	
	/** Indicates to dowload the object. */
	private static final int DOWNLOAD = 0;
	
	/** Indicates to view the object. */
	private static final int VIEW = 1;
	
	/** Indicates to download and view the object. */
	private static final int DOWNLOAD_AND_VIEW = 2;
	
	/** Reference to the activity. */
	private ActivityComponent activity;
	
	/** Button used to view the file. */
	private JButton	viewButton;
	
	/** The result to handle. */
	private Object row;
	
	/** The name to set. */
	private String name;
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		String text = "";
		if (row instanceof ImageData) {
			ImageData data = (ImageData) row;
			if (data.isLoaded()) text += data.getName();
			else text += "Image";
			text += " ID:"+data.getId();
		} else if (row instanceof DatasetData) {
			DatasetData data = (DatasetData) row;
			if (data.isLoaded()) text += data.getName();
			else text += "Dataset";
			text += " ID:"+data.getId();
		} else if (row instanceof ProjectData) {
			ProjectData data = (ProjectData) row;
			if (data.isLoaded()) text += data.getName();
			else text += "Project";
			text += " ID:"+data.getId();
		} else if (row instanceof FileAnnotationData) {
			FileAnnotationData data = (FileAnnotationData) row;
			if (data.isLoaded()) text += data.getFileName();
			else text += "Annotation";
			text += " ID:"+data.getId();
		} else if (row instanceof OriginalFile) {
			OriginalFile data = (OriginalFile) row;
			if (data.isLoaded()) {
				if (data.getName() != null) 
					text += data.getName().getValue();
			} else text += "File";
			text += " ID:"+data.getId().getValue();
		} else text = row.toString();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(new JLabel(text));
		if (activity.isDownloadable(row)) {
			add(activity.createButton("Download", DOWNLOAD, this));
		}
		if (activity.isViewable(row)) {
			viewButton = activity.createButton("View", VIEW, this);
			add(viewButton);
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param row The object to display.
	 * @param activity The activity of reference.
	 */
	ActivityResultRow(Object row, ActivityComponent activity)
	{
		this("", row, activity);
	}

	
	/**
	 * Creates a new instance.
	 * 
	 * @param row The object to display.
	 * @param activity The activity of reference.
	 */
	ActivityResultRow(String name, Object row, ActivityComponent activity)
	{
		this.activity = activity;
		this.row = row;
		this.name = name;
		buildGUI();
	}

	/** Allows to download and views the result. */
	void allowDownloadAndView()
	{
		if (viewButton == null)
			viewButton = activity.createButton("View", DOWNLOAD_AND_VIEW, this);
		else viewButton.setActionCommand(""+DOWNLOAD_AND_VIEW);
		add(viewButton);
	}
	
	/**
	 * Either views or downloads the results.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case DOWNLOAD:
				firePropertyChange(ACTION_PROPERTY, Boolean.valueOf(false), 
						Boolean.valueOf(true));
				activity.download(name, row);
				break;
			case VIEW:
				firePropertyChange(ACTION_PROPERTY, Boolean.valueOf(false), 
						Boolean.valueOf(true));
				activity.view(row);
				break;
			case DOWNLOAD_AND_VIEW:
				firePropertyChange(ACTION_PROPERTY, Boolean.valueOf(false), 
						Boolean.valueOf(true));
				activity.open(row);
				break;
		}
	}
	
}
