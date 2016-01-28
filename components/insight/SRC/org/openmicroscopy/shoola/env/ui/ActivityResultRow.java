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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.openmicroscopy.shoola.env.data.ProcessReport;
import org.openmicroscopy.shoola.util.filter.file.GIFFilter;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.filter.file.TIFFFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.model.OriginalFile;
import omero.gateway.model.DatasetData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;


/** 
 * Displays a result.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class ActivityResultRow 
	extends JButton
	implements ActionListener
{

	/** Action ID indicating that the user select one of the action. */
	static final String ACTION_PROPERTY = "action";
	
	/** Text associated to the view action. */
	private static final String VIEW_TEXT = "View ";
	
	/** Text associated to the browse action. */
	private static final String BROWSE_TEXT = "Go to ";
	
	/** Text associated to the browse action. */
	private static final String DOWNLOAD_TEXT = "Download";
	
	/** Text associated to the browse action. */
	static final String INFO_TEXT = "Info";
	
	/** Text associated to the browse action. */
	static final String ERROR_TEXT = "Error";
	
	/** Indicates to download the object. */
	private static final int DOWNLOAD = 0;
	
	/** Indicates to view the object. */
	private static final int VIEW = 1;
	
	/** Indicates to browse the object. */
	private static final int BROWSE = 2;

	/** The maximum length.*/
	private static final int MAX_LENGTH = 27;
	
	/** Reference to the activity. */
	private ActivityComponent activity;
	
	/** The result to handle. */
	private Object row;
	
	/** The name to set. */
	private String name;

	/** The menu.*/
	private JPopupMenu menu;
	
	/** The item to view the object.*/
	private JMenuItem viewItem;
	
	/**
	 * Returns <code>true</code> if the object can be downloaded, 
	 * <code>false</code> otherwise.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 */
	private boolean isDownloadable(Object object)
	{
		return (object instanceof FileAnnotationData || 
				object instanceof OriginalFile);
	}
	
	/**
	 * Returns <code>true</code> if the object can be viewed, 
	 * <code>false</code> otherwise.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 */
	private boolean isViewable(Object object)
	{
		return (object instanceof ImageData ||
				object instanceof FileAnnotationData ||
				object instanceof OriginalFile);
	}
	
	/**
	 * Creates or recycles the menu.
	 * 
	 * @return See above.
	 */
	private void createMenu(int x, int y)
	{
		if (menu != null) {
			menu.show(this, x, y);
			return;
		}
		menu = new JPopupMenu();
		//create the item.
		String text = getType();
		JMenuItem item;
		if (text != null) {
			item = createMenuItem(BROWSE_TEXT+text, BROWSE);
			menu.add(item);
		}
		if (isViewable(row)) {
			String mt = getMimetype();
			if (mt != null) {
				viewItem = createMenuItem(VIEW_TEXT+mt, VIEW);
				menu.add(viewItem);
			} else {
				if (!isDownloadable(row)) {
					viewItem = createMenuItem(VIEW_TEXT+text, VIEW);
					menu.add(viewItem);
				}
			}
		}
		if (isDownloadable(row)) {
			item = createMenuItem(DOWNLOAD_TEXT, DOWNLOAD);
			menu.add(item);
		}
		if (activity.hasError()) {
			menu.add(new ActivityResultMenu(ERROR_TEXT, activity.errorObject,
					activity));
		}
		if (activity.hasInfo()) {
			menu.add(new ActivityResultMenu(INFO_TEXT, activity.infoObject,
					activity));
		}
		menu.show(this, x, y);
	}
	
    /**
     * Creates a menu item.
     * 
     * @param text The text of the button.
     * @param actionID The action command id.
     * @return See above.
     */
    private JMenuItem createMenuItem(String text, int actionID)
    {
    	JMenuItem b = new JMenuItem(text);//UIUtilities.createHyperLinkMenuItem(text);
		b.setActionCommand(""+actionID);
		b.addActionListener(this);
		return b;
    }
    
	/** Attaches the listeners.*/
	private void attachListeners()
	{
		addMouseListener(new MouseAdapter() {
			
			public void mouseReleased(MouseEvent e) {
				createMenu(e.getX(), e.getY());
			}
		});
	}
	
	/**
	 * Returns the type of object to view or browse.
	 * 
	 * @return See above
	 */
	private String getType()
	{
		if (row instanceof ImageData) return "Image";
		if (row instanceof DatasetData) return "Dataset";
		if (row instanceof ProjectData) return "Project";
		if (row instanceof ScreenData) return "Screen";
		if (row instanceof PlateData) return "Plate";
		if (row instanceof FileAnnotationData) return "Attachment";
		return null;
	}
	
	/**
	 * Returns the text corresponding to the mimetype of the file.
	 * 
	 * @return See above.
	 */
	private String getMimetype()
	{
		String mimetype = null;
		if (row instanceof FileAnnotationData) {
			FileAnnotationData fa = (FileAnnotationData) row;
			if (fa.isLoaded()) {
				OriginalFile of = (OriginalFile) fa.getContent();
				if (of.isLoaded() && of.getMimetype() != null)
					mimetype = of.getMimetype().getValue();
			} 
		}
		if (row instanceof OriginalFile) {
			OriginalFile of = (OriginalFile) row;
			if (of.isLoaded() && of.getMimetype() != null)
				mimetype = of.getMimetype().getValue();
		}
		if (mimetype == null) return null;
		if (JPEGFilter.MIMETYPE.equals(mimetype)) return JPEGFilter.JPEG;
		else if (PNGFilter.MIMETYPE.equals(mimetype)) return PNGFilter.PNG;
		else if (TIFFFilter.MIMETYPE.equals(mimetype)) return TIFFFilter.TIFF;
		else if (GIFFilter.MIMETYPE.equals(mimetype)) return GIFFilter.GIF;
		return null;
	}
	
	/** 
	 * Returns the text associated to the object.
	 * 
	 * @return See above.
	 */
	private String getObjectText()
	{
		String text = "";
		if (row instanceof ImageData) {
			ImageData data = (ImageData) row;
			if (data.isLoaded()) text += data.getName();
			else {
				text += "Image ID:";
				text += data.getId();
			}
		} else if (row instanceof DatasetData) {
			DatasetData data = (DatasetData) row;
			if (data.isLoaded()) text += data.getName();
			else {
				text += "Dataset ID:";
				text += data.getId();
			}
		} else if (row instanceof PlateData) {
			PlateData data = (PlateData) row;
			if (data.isLoaded()) text += data.getName();
			else {
				text += "Plate ID:";
				text += data.getId();
			}
		} else if (row instanceof ScreenData) {
			ScreenData data = (ScreenData) row;
			if (data.isLoaded()) text += data.getName();
			else {
				text += "Screen ID:";
				text += data.getId();
			}
		} else if (row instanceof ProjectData) {
			ProjectData data = (ProjectData) row;
			if (data.isLoaded()) text += data.getName();
			else {
				text += "Project ID:";
				text += data.getId();
			}
		} else if (row instanceof FileAnnotationData) {
			FileAnnotationData data = (FileAnnotationData) row;
			if (data.isLoaded()) text += data.getFileName();
			else {
				text += "Annotation ID:";
				text += data.getId();
			}
			
		} else if (row instanceof OriginalFile) {
			OriginalFile data = (OriginalFile) row;
			if (data.isLoaded()) {
				if (data.getName() != null) 
					text += data.getName().getValue();
			} else {
				text += "File ID:";
				text += data.getId().getValue();
			}
		} else if (row instanceof ProcessReport) {
			ProcessReport report = (ProcessReport) row;
			StringBuffer buffer = new StringBuffer();
			buffer.append("Category:"+report.getCategory());
			buffer.append("Name:"+report.getName());
			text = buffer.toString();
		} else text = row.toString();
		return text;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setOpaque(false);
		setForeground(UIUtilities.HYPERLINK_COLOR);
		UIUtilities.unifiedButtonLookAndFeel(this);
		String text = getObjectText();
		//Format the t
		int l = text.length();
		if (l <= MAX_LENGTH) setText(text);
		else {
			setText("..."+text.substring(l-MAX_LENGTH-1, l-1));
		}
		setToolTipText(text);
		Font f = getFont();
		setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
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
		attachListeners();
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
				activity.download("", row);
				break;
			case VIEW:
				activity.view(row, viewItem);
				break;
			case BROWSE:
				activity.browse(row, viewItem);
		}
	}

}
