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
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;


//Third-party libraries
import layout.TableLayout;


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
	
	/** The title of the dialog. */
	private static final String TITLE = "Downloads";
	
	/** Convenience reference to the icons manager. */
	private IconManager 					icons;
	
	/** Component hosting the entries. */
	private JPanel 							entries;
	
	/** Button used to clean up all the entries. */
	private JButton							cleanupButton;
	
	/** Collection of the entries. */
	private List<FileLoadingComponent>		components;
	
	/** Initializes the components. */
	private void initComponents()
	{
		components = new ArrayList<FileLoadingComponent>();
		entries = new JPanel();
		entries.setBackground(UIUtilities.BACKGROUND);
		entries.setLayout(new BoxLayout(entries, BoxLayout.Y_AXIS));
		cleanupButton = new JButton("Clean Up");
		getRootPane().setDefaultButton(cleanupButton);
		cleanupButton.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) { cleanup(); }
		
		});
	}
	
	/** Removes all entries. */
	private void cleanup()
	{
		components.clear();
		entries.removeAll();
		repaint();
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
		double[] columns = {TableLayout.FILL};
		TableLayout layout = new TableLayout();
		layout.setColumn(columns);
		entries.setLayout(layout);
		int index = 0;
		Iterator i = components.iterator();
		FileLoadingComponent c;
		while (i.hasNext()) {
			layout.insertRow(index, TableLayout.PREFERRED);
			c = (FileLoadingComponent) i.next();
			entries.add(c, "0, "+index+", f, c");
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
	 * Creates a new instance.
	 * 
	 * @param owner	The owner of the dialog.
	 * @param icons	Reference to the icons manager.
	 */
	DownloadsDialog(JFrame owner, IconManager icons)
	{
		super(owner);
		this.icons = icons;
		setTitle(TITLE);
		initComponents();
		buildGUI();
		setSize(200, 300);
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
		while (i.hasNext()) {
			c = (FileLoadingComponent) i.next();
			if (c.getFileID() == fileID && c.getAbsolutePath().equals(name))
				c.setStatus(percent);
		}
	}
	
	/**
	 * Adds a new entry for the file to download.
	 * 
	 * @param absolutePath	The absolute path of the file.
	 * @param fileName 		The name of the file.
	 * @param fileID		The id of the file.
	 */
	void addDowloadEntry(String absolutePath, String fileName, long fileID)
	{
		FileLoadingComponent c = new FileLoadingComponent(absolutePath, 
											fileName, fileID, icons);
		c.addPropertyChangeListener(this);
		components.add(c);
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
			while (i.hasNext()) {
				c = (FileLoadingComponent) i.next();
				if (c.getAbsolutePath().equals(path)) {
					toRemove = c;
					break;
				}
			}
			if (toRemove != null) {
				components.remove(toRemove);
				layoutEntries();
			}
		}
	}

}
