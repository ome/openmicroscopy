/*
 * org.openmicroscopy.shoola.agents.treeviewer.ImportManager 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.util.FileImportComponent;
import org.openmicroscopy.shoola.env.data.model.ImportObject;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ImageData;

/** 
 * Manages the components to imports.
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
public class ImportManager 
	extends JPanel
	implements ActionListener, PropertyChangeListener
{

	/** Bound property indicating to view the image. */
	public static final String	VIEW_IMAGE_PROPERTY = "viewImage";
	
	/** Bound property indicating to send the files. */
	public static final String SEND_FILES_PROPERTY = "sendFiles";
	
	/** The title of the dialog. */
	private static final String	TITLE = "Import";
	
	/** The columns for the layout of the {@link #entries}. */
	private static final double[] COLUMNS = {TableLayout.FILL};
						//{TableLayout.PREFERRED, TableLayout.FILL};
	
	/** Action ID indicating to clear the list of imported files. */
	private static final int	CLEAR = 0;
	
	/** Action ID indicating to send the list files that failed to import. */
	private static final int	SEND = 1;
	
	/** The components to lay out. */
	private LinkedHashMap<String, FileImportComponent>	components;
	
	/** Contains the file already imported. */
	private Map<String, FileImportComponent>			imported;
	
	/** The collection of objects to import. */
	private List<File>									toImport;

	/** Component hosting the entries. */
	private JPanel	entries;
	
	/** Button to clear the list of imported files. */
	private JButton clearButton;
	
	/** Button to send the selected file that failed to import.. */
	private JButton sendButton;

	/** The number of files to import. */
	private int		total;
	
	/** Initializes the components. */
	private void initComponents()
	{
		toImport = new ArrayList<File>();
		imported = new LinkedHashMap<String, FileImportComponent>();
		components = new LinkedHashMap<String, FileImportComponent>();
		entries = new JPanel();
		entries.setBackground(UIUtilities.BACKGROUND);
		clearButton = new JButton("Clear");
		clearButton.setToolTipText("Removes the imported files " +
				"from the list.");
		clearButton.setActionCommand(""+CLEAR);
		clearButton.addActionListener(this);
		sendButton = new JButton("Send");
		sendButton.setToolTipText("Sends the files that failed to import.");
		sendButton.setActionCommand(""+SEND);
		sendButton.addActionListener(this);
		sendButton.setEnabled(false);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JScrollPane pane = new JScrollPane(entries);
		pane.setOpaque(false);
		pane.setBorder(new LineBorder(Color.LIGHT_GRAY));
		TitlePanel tp = new TitlePanel(TITLE, "Imported files and " +
				"on-going import.", null);
		setLayout(new BorderLayout(0, 0));
		add(tp, BorderLayout.NORTH);
		add(pane, BorderLayout.CENTER);
		add(buildStatusBar(), BorderLayout.SOUTH);
	}
	
	/**
	 * Builds and lays out the controls.
	 * 
	 * @return See above.
	 */
	private JPanel buildStatusBar()
	{
		JPanel p = new JPanel();
		p.add(clearButton);
		p.add(Box.createHorizontalStrut(5));
		p.add(sendButton);
		JPanel bar = UIUtilities.buildComponentPanelRight(p);
		bar.setBorder(new LineBorder(Color.LIGHT_GRAY));
		return bar;
	}
	
	/**
	 * Adds a new row.
	 * 
	 * @param layout The layout.
	 * @param index	 The index of the row.
	 * @param c		 The component to add.
	 */
	private void addRow(TableLayout layout, int index, FileImportComponent c)
	{
		layout.insertRow(index, TableLayout.PREFERRED);
		if (index%2 == 0)
			c.setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
		else 
			c.setBackground(UIUtilities.BACKGROUND_COLOUR_ODD);
		//entries.add(c.getNameLabel(), "0, "+index);
		entries.add(c, "0, "+index+"");
	}
	
	/** Lays out the entries. */
	private void layoutEntries()
	{
		entries.removeAll();
		
		TableLayout layout = new TableLayout();
		layout.setColumn(COLUMNS);
		entries.setLayout(layout);
		int index = 0;
		Entry entry;
		
		FileImportComponent c;
		Iterator i = imported.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			c = (FileImportComponent) entry.getValue();
			if (c != null) {
				addRow(layout, index, c);
				index++;
			}
		}
		i = components.entrySet().iterator();
		//index = 0;
		int first = 0;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			c = (FileImportComponent) entry.getValue();
			if (first == 0)
				c.setStatus(true, null);
			addRow(layout, index, c);
			index++;
			first++;
		}
		entries.revalidate();
		repaint();
	}
	
	/** Removes the files already imported from the list. */
	private void clearList()
	{
		if (imported == null) return;
		imported.clear();
		layoutEntries();
		sendButton.setEnabled(false);
	}
	
	/** Sends the files that failed to import. */
	private void send()
	{
		if (imported == null) return;
		Map<File, Exception> files = new HashMap<File, Exception>();
		Entry entry;
		FileImportComponent c;
		Iterator i = imported.entrySet().iterator();
		List<String> names = new ArrayList<String>();
		File f;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			c = (FileImportComponent) entry.getValue();
			if (c != null && c.isSelected()) {
				f = c.getOriginalFile();
				if (!names.contains(f.getAbsolutePath()))
					files.put(f, c.getImportException());
			}
		}
		if (files.size() == 0) return;
		firePropertyChange(SEND_FILES_PROPERTY, null, files);
	}
	
	/** Creates a new instance. */
	public ImportManager()
	{
		initComponents();
		buildGUI();
	}
	
	/**
	 * Initializes the collection of files to import.
	 * Returns the observable component displaying the status of the on-going
	 * import.
	 * 
	 * @param files The files to import.
	 * @param depth The depth used for the name. Only taken into account
	 * 				if the file is a directory.
	 * @return See above.
	 */
	public List<ImportObject> initialize(Map<File, String> files, int depth)
	{
		List<ImportObject> list = new ArrayList<ImportObject>();
		if (files == null) return null;
		total = 0;
		toImport.clear();
		components.clear();
		Entry entry;
		Iterator i = files.entrySet().iterator();
		FileImportComponent c;
		File f;
		ImportObject obj;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			f = (File) entry.getKey();
			toImport.add(f);
			c = new FileImportComponent(this, f);
			obj = new ImportObject(f, c.getStatus(), (String) entry.getValue());
			if (f.isDirectory()) obj.setDepth(depth);
			list.add(obj);
			c.addPropertyChangeListener(
					FileImportComponent.SEND_FILE_PROPERTY, this);
			components.put(f.getAbsolutePath(), c);
		}
		total = toImport.size();
		layoutEntries();
		return list;
	}
	
	/**
	 * Views the image.
	 * 
	 * @param image The image to view
	 */
	public void viewImage(ImageData image)
	{
		firePropertyChange(VIEW_IMAGE_PROPERTY, null, image);
	}
	
	/**
	 * Sets the status for the specified file.
	 * 
	 * @param f 		The file.
	 * @param image 	The imported image.
	 */
	public void setStatus(File f, Object image)
	{
		if (f == null) return;
		String path = f.getAbsolutePath();
		FileImportComponent c = components.get(path);
		if (c != null) {
			imported.put(path, c);
			c.setStatus(false, image);
			components.remove(path);
			total--;
			//if (image != null) {
			
			//}
			if (image instanceof Map) {
				Map m = (Map) image;
				if (m != null && m.size() > 0) {
					Iterator k = m.entrySet().iterator();
					Entry entry;
					while (k.hasNext()) {
						entry = (Entry) k.next();
						c = new FileImportComponent(this, (File) entry.getKey());
						c.setStatus(false, entry.getValue());
						c.addPropertyChangeListener(
								FileImportComponent.SEND_FILE_PROPERTY, this);
						components.put(f.getAbsolutePath(), c);
					}
					layoutEntries();
				}
			}
			int index = 0;
			File ff;
			Iterator<File> i = toImport.iterator();
			while (i.hasNext()) {
				ff = i.next();
				if (ff.getAbsolutePath().equals(path)) break;
				index++;
			}
			int n = toImport.size();
			index++;
			if (index < n) {
				ff = toImport.get(index);
				c = components.get(ff.getAbsolutePath());
				if (c != null) c.setStatus(true, null);
			}
		}
	}

	/**
	 * Returns <code>true</code> if there are still files to import,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasFilesToImport() { return total != 0; }
	
	/**
	 * Returns the UI delegate.
	 * 
	 * @return See above.
	 */
	public JComponent getUIDelegate() { return this; }
	
	/**
	 * Reacts to the controls.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CLEAR:
				clearList();
				break;
			case SEND:
				send();
				break;
			default:
				break;
		}
	}

	/** 
	 * Listens to property sent by the {@link FileImportComponent}s.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (FileImportComponent.SEND_FILE_PROPERTY.equals(name)) {
			boolean selected = false;
			Entry entry;
			
			FileImportComponent c;
			Iterator i = imported.entrySet().iterator();
			while (i.hasNext()) {
				entry = (Entry) i.next();
				c = (FileImportComponent) entry.getValue();
				if (c != null && c.isSelected()) {
					selected = true;
					break;
				}
			}
			sendButton.setEnabled(selected);
		}
	}

}
