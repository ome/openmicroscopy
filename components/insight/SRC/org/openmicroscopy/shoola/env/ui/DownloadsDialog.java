/*
 * org.openmicroscopy.shoola.env.ui.DownloadsDialog 
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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;


//Third-party libraries
import info.clearthought.layout.TableLayout;


//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Dialog displaying the files previously downloaded.
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
class DownloadsDialog 
	extends JDialog
	implements PropertyChangeListener
{

	/** 
	 * Bound property indicating to cancel the data loading for
	 * a given file.
	 */
	static final String 		CANCEL_LOADING_PROPERTY = "cancelLoading";
	
	/** Indicates that the dialog is a download dialog. */
	static final int	DOWNLOAD = 0;
	
	/** Indicates that the dialog is a upload dialog. */
	static final int	UPLOAD = 1;
	
	/** Indicates that the dialog is an activity dialog. */
	static final int	ACTIVITY = 2;
	
	/** The title of the dialog if it is a download dialog. */
	private static final String TITLE_DOWNLOAD = "Downloads";
	
	/** The title of the dialog if it is a upload dialog. */
	private static final String TITLE_UPLOAD = "Uploads";
	
	/** The title of the dialog if it is an activity dialog. */
	private static final String TITLE_ACTIVITY = "Activities";
	
	/** The columns for the layout of the {@link #entries}. */
	private static final double[] COLUMNS = {TableLayout.FILL};
	
	/** Convenience reference to the icons manager. */
	private IconManager 					icons;
	
	/** Component hosting the entries. */
	private JPanel 							entries;
	
	/** Button used to clean up all the entries. */
	private JButton							cleanupButton;
	
	/** Collection of the entries. */
	private List<JComponent>				components;
	
	/** The index of the dialog. */
	private int								dialogIndex;
	
	/** Initializes the components. */
	private void initComponents()
	{
		components = new ArrayList<JComponent>();
		entries = new JPanel();
		entries.setBackground(UIUtilities.BACKGROUND);
		cleanupButton = new JButton("Clear List");
		getRootPane().setDefaultButton(cleanupButton);
		cleanupButton.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) { cleanup(); }
		
		});
	}
	
	/** Removes all entries. */
	private void cleanup()
	{
		List<JComponent> toRemove = new ArrayList<JComponent>();
		
		Iterator<JComponent> i = components.iterator();
		JComponent comp;
		while (i.hasNext()) {
			comp = i.next();
			if (comp instanceof FileLoadingComponent) {
				if (!((FileLoadingComponent) comp).isOngoingActivity())
					toRemove.add(comp);
			} else if (comp instanceof ActivityComponent) {
				if (!((ActivityComponent) comp).isOngoingActivity())
					toRemove.add(comp);
			}
		}
		components.removeAll(toRemove);
		layoutEntries();
	}
	
	/**
	 * Builds and lays out the controls.
	 * 
	 * @return See above.
	 */
	private JPanel buildStatusBar()
	{
		JPanel p = new JPanel();
		p.add(cleanupButton);
		JPanel bar = UIUtilities.buildComponentPanelRight(p);
		bar.setBorder(new LineBorder(Color.LIGHT_GRAY));
		return bar;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		Container c = getContentPane();
		JScrollPane pane = new JScrollPane(entries);
		pane.setOpaque(false);
		pane.setBorder(new LineBorder(Color.LIGHT_GRAY));
		c.add(pane, BorderLayout.CENTER);
		c.add(buildStatusBar(), BorderLayout.SOUTH);
	}
	
	/** Lays out the entries. */
	private void layoutEntries()
	{
		entries.removeAll();
		TableLayout layout = new TableLayout();
		layout.setColumn(COLUMNS);
		entries.setLayout(layout);
		int index = 0;
		ListIterator<JComponent> i = components.listIterator(components.size());
		JComponent c;
		while (i.hasPrevious()) {
			layout.insertRow(index, TableLayout.PREFERRED);
			c = (JComponent) i.previous();
			entries.add(c, "0, "+index+", FULL, CENTER");
			if (index%2 == 0)
				c.setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
			else 
				c.setBackground(UIUtilities.BACKGROUND_COLOUR_ODD);
			index++;
		}
		entries.revalidate();
		repaint();
	}
	
	/** 
	 * Sets the index.
	 * 
	 * @param value The value to set.
	 */
	private void setDialogIndex(int value)
	{
		switch (value) {
			case DOWNLOAD:
			default:
				dialogIndex = DOWNLOAD;
				setTitle(TITLE_DOWNLOAD);
				break;
			case UPLOAD:
				dialogIndex = value;
				setTitle(TITLE_UPLOAD);
				break;
			case ACTIVITY:
				dialogIndex = ACTIVITY;
				setTitle(TITLE_ACTIVITY);
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner	The owner of the dialog.
	 * @param icons	Reference to the icons manager.
	 * @param index	One of the constants defined by this class.
	 */
	DownloadsDialog(JFrame owner, IconManager icons, int index)
	{
		super(owner);
		setModal(false);
		if (icons == null)
			throw new IllegalArgumentException("No icons manager specified.");
		setDialogIndex(index);
		this.icons = icons;
		initComponents();
		buildGUI();
		setSize(350, 250);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner	The owner of the dialog.
	 * @param icons	Reference to the icons manager.
	 */
	DownloadsDialog(JFrame owner, IconManager icons)
	{
		this(owner, icons, DOWNLOAD);
	}
	
	/**
	 * Sets the loading status.
	 * 
	 * @param name		The name of the entry.
	 * @param percent 	The value to set.
	 * @param fileID	The id of the file corresponding to the status.
	 */
	void setLoadingStatus(int percent, String name, long fileID)
	{
		Iterator i = components.iterator();
		FileLoadingComponent c;
		Object obj;
		while (i.hasNext()) {
			obj = i.next();
			if (obj instanceof FileLoadingComponent) {
				c = (FileLoadingComponent) obj;
				if (c.getFileID() == fileID && c.getAbsolutePath().equals(name))
					c.setStatus(percent);
			}
		}
	}
	
	/**
	 * Adds a new activity entry.
	 * 
	 * @param directory		The directory where to download the file.
	 * @param fileName 		The name of the file.
	 * @param fileID		The id of the file.
	 */
	void addActivityEntry(String directory, String fileName, long fileID)
	{
		FileLoadingComponent c = new FileLoadingComponent(directory, 
											fileName, fileID, icons);
		c.addPropertyChangeListener(this);
		components.add(c);
		layoutEntries();
	}

	/**
	 * Adds the passed component to the list.
	 * 
	 * @param component The component to add.
	 */
	void addActivityEntry(JComponent component)
	{
		if (component == null) return;
		component.addPropertyChangeListener(this);
		components.add(component);
		layoutEntries();
	}
	
	/**
	 * Listen to property fired by {@link FileLoadingComponent}s.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (FileLoadingComponent.CANCEL_PROPERTY.equals(name)) {
			firePropertyChange(CANCEL_LOADING_PROPERTY, evt.getOldValue(), 
								evt.getNewValue());
		} else if (FileLoadingComponent.REMOVE_PROPERTY.equals(name)) {
			String path = (String) evt.getNewValue();
			
			FileLoadingComponent c;
			Iterator i = components.iterator();
			FileLoadingComponent toRemove = null;
			Object obj;
			while (i.hasNext()) {
				obj = i.next();
				if (obj instanceof FileLoadingComponent) {
					c = (FileLoadingComponent) obj;
					if (c.getAbsolutePath().equals(path)) {
						toRemove = c;
						break;
					}
				}
			}
			if (toRemove != null) {
				components.remove(toRemove);
				layoutEntries();
			}
		} else if (ActivityComponent.REMOVE_ACTIVITY_PROPERTY.equals(name)) {
			components.remove(evt.getNewValue());
			layoutEntries();
		}
	}

}
