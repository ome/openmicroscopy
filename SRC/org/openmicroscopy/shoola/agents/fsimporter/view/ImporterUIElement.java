/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.ImporterUIElement 
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.fsimporter.view;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.importer.ImportStatusEvent;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponent;
import org.openmicroscopy.shoola.env.data.model.ImportObject;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.ui.ClosableTabbedPaneComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

/** 
 * Component displaying an import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class ImporterUIElement 
	extends ClosableTabbedPaneComponent
{

	/** The object hosting information about files to import. */
	private ImportableObject object;
	
	/** The components to lay out. */
	private LinkedHashMap<String, FileImportComponent>	components;

	/** Component hosting the entries. */
	private JPanel	entries;
	
	/** The columns for the layout of the {@link #entries}. */
	private static final double[] COLUMNS = {TableLayout.FILL};
	
	/** The number of files imported. */
	private int countImported;
	
	/** The total number of tiles to import. */
	private int totalToImport;
	
	/** The time when the import started. */
	private long startImport;
	
	/** The component displaying the duration of the import. */
	private JLabel timeLabel;
	
	/** Initializes the components. */
	private void initialize()
	{
		countImported = 0;
		setClosable(false);
		entries = new JPanel();
		entries.setBackground(UIUtilities.BACKGROUND);
		components = new LinkedHashMap<String, FileImportComponent>();
		Map<File, List<Boolean>> files = object.getFiles();
		FileImportComponent c;
		File f;
		ImportObject obj;
		Entry entry;
		Iterator i = files.entrySet().iterator();
		List<ImportObject> objects = new ArrayList<ImportObject>();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			f = (File) entry.getKey();
			c = new FileImportComponent(f);
			obj = new ImportObject(f, c.getStatus());
			objects.add(obj);
			components.put(f.getAbsolutePath(), c);
		}
		totalToImport = objects.size();
		object.setFilesToImport(objects);
	}
	
	/** 
	 * Builds and lays out the header.
	 * 
	 * @return See above.
	 */
	private JPanel buildHeader()
	{
		JPanel header = new JPanel();
		header.setBackground(UIUtilities.BACKGROUND_COLOR);
		header.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		header.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridy = 0;
		c.gridx = 0;
		JLabel label = UIUtilities.setTextFont("Import Time:", Font.BOLD);
		JLabel value;
		startImport = System.currentTimeMillis();
		timeLabel = UIUtilities.createComponent(null);
		timeLabel.setText(UIUtilities.formatShortDateTime(null));
    	header.add(label, c);
    	c.gridx = c.gridx+2;
    	header.add(timeLabel, c);
    	
    	
    	c.gridy++; 	
    	c.gridx = 0;
		DataObject ho = object.getContainer();
		String text = "Imported in ";
		String name = "";
		if (ho != null) {
			if (ho instanceof DatasetData) {
				text += "Dataset: ";
				name = ((DatasetData) ho).getName();
			} else if (ho instanceof ScreenData) {
				text += "Screen: ";
				name = ((ScreenData) ho).getName();
			} else if (ho instanceof ProjectData) {
				text += "Project: ";
				name = ((ProjectData) ho).getName();
			} else text = null;
		} else {
			if (DatasetData.class.equals(object.getType())) {
				text += "Dataset: ";
				name = UIUtilities.formatDate(null, UIUtilities.D_M_Y_FORMAT);
			}
		}
		if (text != null) {
			label = UIUtilities.setTextFont(text, Font.BOLD);
			value = UIUtilities.createComponent(null);
	    	value.setText(name);
			header.add(label, c);
	    	c.gridx = c.gridx+2;
	    	header.add(value, c);
	    	c.gridy++; 	
	    	c.gridx = 0;
		}
		Collection<TagAnnotationData> tags = object.getTags();
		if (tags != null && tags.size() > 0) {
			label = UIUtilities.setTextFont("Images Tagged with: ", Font.BOLD);
			value = UIUtilities.createComponent(null);
			text = "";
			Iterator<TagAnnotationData> i = tags.iterator();
			int index = 0;
			int n = tags.size()-1;
			while (i.hasNext()) {
				text += (i.next()).getTagValue();
				if (index < n) text +=", ";
				index++;
			}
			value.setText(text);
			header.add(label, c);
	    	c.gridx = c.gridx+2;
	    	header.add(value, c);
	    	c.gridy++; 	
	    	c.gridx = 0;
		}
		JPanel content = UIUtilities.buildComponentPanel(header);
		content.setBackground(UIUtilities.BACKGROUND_COLOR);
		return content;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		layoutEntries();
		JScrollPane pane = new JScrollPane(entries);
		pane.setOpaque(false);
		pane.setBorder(new LineBorder(Color.LIGHT_GRAY));
		setLayout(new BorderLayout(0, 0));
		add(buildHeader(), BorderLayout.NORTH);
		add(pane, BorderLayout.CENTER);
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
		Iterator i = components.entrySet().iterator();
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
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index The index of the component.
	 * @param name The name of the component.
	 * @param object the object to handle.
	 */
	ImporterUIElement(int index, String name, ImportableObject object)
	{
		super(index, name);
		if (object == null) 
			throw new IllegalArgumentException("No object specified.");
		this.object = object;
		initialize();
		buildGUI();
	}

	/**
	 * Sets the result of the import for the specified file.
	 * 
	 * @param f The file to import.
	 * @param result The result.
	 */
	void setImportedFile(File f, Object result)
	{
		FileImportComponent c = components.get(f.getAbsolutePath());
		if (c != null) {
			c.setStatus(false, result);
			countImported++;
			boolean done = countImported == totalToImport;
			setClosable(done);
			if (done) {
				long duration = System.currentTimeMillis()-startImport;
				String text = timeLabel.getText();
				String time = UIUtilities.calculateHMS((int) (duration/1000));
				timeLabel.setText(text+" Duration: "+time);
				EventBus bus = ImporterAgent.getRegistry().getEventBus();
				bus.post(new ImportStatusEvent(false));
			}
		}
	}
	
	/**
	 * Returns the collection of files that could not be imported.
	 * 
	 * @return See above.
	 */
	List<FileImportComponent> getMarkedFiles()
	{
		List<FileImportComponent> list = new ArrayList<FileImportComponent>();
		Entry entry;
		Iterator i = components.entrySet().iterator();
		FileImportComponent fc;
		File f;
		List<FileImportComponent> l;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			fc = (FileImportComponent) entry.getValue();
			l = fc.getImportErrors();
			if (l != null && l.size() > 0)
				list.addAll(l);
			
		}
		return list;
	}
	
}
