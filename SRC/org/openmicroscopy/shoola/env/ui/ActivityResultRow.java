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
import java.util.Iterator;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import omero.api.delete.DeleteReport;
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

	/** Action ID indicating that the user select one of the action. */
	static final String ACTION_PROPERTY = "action";
	
	/** Indicates to download the object. */
	private static final int DOWNLOAD = 0;
	
	/** Indicates to view the object. */
	private static final int VIEW = 1;
	
	/** Indicates to plot the results. */
	private static final int PLOT = 2;

	/** Reference to the activity. */
	private ActivityComponent activity;
	
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
		} else if (row instanceof DeleteReport) {
			DeleteReport report = (DeleteReport) row;
			Map<String, long[]> undeletedFiles = report.undeletedFiles;
			int count = 0;
			Iterator<String> i = undeletedFiles.keySet().iterator();
			while (i.hasNext()) {
				count += undeletedFiles.get(i.next()).length;
			}
			text = report.error;
			text += " Unable to delete "+count+" file";
			if (count > 1) text += "s";
		} else text = row.toString();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(new JLabel(text));
		if (activity.isDownloadable(row)) {
			add(Box.createHorizontalStrut(5));
			add(activity.createButton("Download", DOWNLOAD, this));
		}
		if (activity.isViewable(row)) {
			add(Box.createHorizontalStrut(5));
			add(activity.createButton(activity.getViewText(row), VIEW, this));
		}
		if (activity.canPlotResult(row)) {
			add(Box.createHorizontalStrut(5));
			add(activity.createButton("Plot", PLOT, this));
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
			case PLOT:
				firePropertyChange(ACTION_PROPERTY, Boolean.valueOf(false), 
						Boolean.valueOf(true));
				activity.plotResult(row);
		}
	}
	
}
