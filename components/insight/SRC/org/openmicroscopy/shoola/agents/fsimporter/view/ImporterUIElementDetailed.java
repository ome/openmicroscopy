/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.fsimporter.view;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.jdesktop.swingx.JXBusyLabel;
import org.openmicroscopy.shoola.agents.events.importer.BrowseContainer;
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.util.CheckSumDialog;
import org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponent;
import org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI;
import org.openmicroscopy.shoola.agents.fsimporter.util.ImportStatus;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.DownloadAndLaunchActivityParam;
import org.openmicroscopy.shoola.env.data.model.FileObject;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.util.Status;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;
import org.openmicroscopy.shoola.util.ui.ClosableTabbedPaneComponent;
import org.openmicroscopy.shoola.util.ui.RotationIcon;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.FilesetData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;

/** 
 * Component displaying an import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class ImporterUIElementDetailed 
	extends ImporterUIElement
{
	
	/** Description of the component. */
	private static final String DESCRIPTION = 
		"Closing will cancel imports that have not yet started.";
	
	/** Text indicating to only show the failure.*/
	private static final String SHOW_FAILURE = "Show Failed";
	
	/** Text indicating to show all the imports.*/
	private static final String SHOW_ALL = "Show All";
	
	/** The columns for the layout of the {@link #entries}. */
	private static final double[] COLUMNS = {TableLayout.FILL};
	
	/** The message to display in the header.*/
	private static final String MESSAGE = 
			"When upload is complete, the import" +CommonsLangUtils.LINE_SEPARATOR+
			"window and OMERO session can be closed." +CommonsLangUtils.LINE_SEPARATOR+
			"Reading will continue on the server.";

	/** Component hosting the entries. */
	private JPanel	entries;
	
	/**
	 * Browses the specified object.
	 * 
	 * @param data The object to handle.
	 * @param node The node hosting the object to browse or <code>null</code>.
	 */ 
	private void browse(Object data, Object node)
	{
		EventBus bus = ImporterAgent.getRegistry().getEventBus();
		if (data instanceof TreeImageDisplay || data instanceof DataObject) {
			bus.post(new BrowseContainer(data, node));
		} else if (data instanceof FileImportComponent) {
			FileImportComponentI fc = (FileImportComponentI) data;
			if (fc.getContainerFromFolder() != null)
				bus.post(new BrowseContainer(fc.getContainerFromFolder(), 
						node));
		}
	}

	/**
	 * Displays only the failures or all the results.
	 */
	void filterFailures()
	{
		String v = filterButton.getText();
		if (SHOW_FAILURE.equals(v)) {
			filterButton.setText(SHOW_ALL);
			layoutEntries(true);
		} else {
			filterButton.setText(SHOW_FAILURE);
			layoutEntries(false);
		}
	}
	
	@Override
	FileImportComponentI buildComponent(ImportableFile importable, boolean
            browsable, boolean singleGroup, int index,
            Collection<TagAnnotationData> tags) {
	    return new FileImportComponent(importable,
                !controller.isMaster(), singleGroup, getID(), object.getTags());
	}
	
	/**
	 * Downloads the log file.
	 * 
	 * @param logFileID
	 */
	void downloadLogFile(long logFileID)
	{
		if (logFileID < 0) return;
		Environment env = (Environment) 
				ImporterAgent.getRegistry().lookup(
						LookupNames.ENV);
		String path = env.getOmeroFilesHome();
		File f = new File(path, "importLog_"+logFileID);
		DownloadAndLaunchActivityParam
		activity = new DownloadAndLaunchActivityParam(logFileID,
				DownloadAndLaunchActivityParam.ORIGINAL_FILE,
				f, null);
		activity.setUIRegister(false);
		UserNotifier un =
				ImporterAgent.getRegistry().getUserNotifier();
		un.notifyActivity(model.getSecurityContext(), activity);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
	    setLayout(new BorderLayout(0, 0));
	    
	    add(buildHeader(), BorderLayout.NORTH);
	    
        entries = new JPanel();
        entries.setBackground(UIUtilities.BACKGROUND);
		layoutEntries(false);
		JScrollPane pane = new JScrollPane(entries);
		pane.setOpaque(false);
		pane.setBorder(new LineBorder(Color.LIGHT_GRAY));
		add(pane, BorderLayout.CENTER);
	}
	
	/** 
	 * Lays out the entries.
	 * 
	 * @param failure Pass <code>true</code> to display the failed import only,
	 * <code>false</code> to display all the entries.
	 */
	private void layoutEntries(boolean failure)
	{
		entries.removeAll();
		TableLayout layout = new TableLayout();
		layout.setColumn(COLUMNS);
		entries.setLayout(layout);
		int index = 0;
		Entry<String, FileImportComponentI> entry;
		Iterator<Entry<String, FileImportComponentI>>
		i = components.entrySet().iterator();
		FileImportComponent fc;
		if (failure) {
			while (i.hasNext()) {
				entry = i.next();
				fc = (FileImportComponent) entry.getValue();
				if (fc.hasComponents()) {
					addRow(layout, index, fc);
					fc.layoutEntries(failure);
					index++;
				} else {
					if (fc.hasImportFailed()) {
						addRow(layout, index, fc);
						index++;
					}
				}
			}
		} else {
			while (i.hasNext()) {
				entry = i.next();
				fc = (FileImportComponent) entry.getValue();
				addRow(layout, index, fc);
				fc.layoutEntries(failure);
				index++;
			}
		}
		
		entries.revalidate();
		repaint();
		setNumberOfImport();
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
		entries.add(c, new TableLayoutConstraints(0, index));
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller Reference to the control. Mustn't be <code>null</code>.
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 * @param view Reference to the model. Mustn't be <code>null</code>.
	 * @param id The identifier of the component.
	 * @param index The index of the component.
	 * @param name The name of the component.
	 * @param object the object to handle. Mustn't be <code>null</code>.
	 */
	ImporterUIElementDetailed(ImporterControl controller, ImporterModel model,
			ImporterUI view, int id, int index, String name,
			ImportableObject object)
	{
		super(controller, model,
	            view, id, index, name,
	            object);
		buildGUI();
	}

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (FileImportComponentI.BROWSE_PROPERTY.equals(name)) {
            List<Object> refNodes = object.getRefNodes();
            Object node = null;
            if (refNodes != null && refNodes.size() > 0)
                node = refNodes.get(0);
            browse(evt.getNewValue(), node);
        } else if (
            FileImportComponentI.IMPORT_FILES_NUMBER_PROPERTY.equals(
                    name)) {
            //-1 to remove the entry for the folder.
            Integer v = (Integer) evt.getNewValue()-1;
            totalToImport += v;
            setNumberOfImport();
        } else if (FileImportComponentI.LOAD_LOGFILEPROPERTY.equals(
                name)) {
            FileImportComponentI fc = (FileImportComponentI)
                    evt.getNewValue();
            if (fc == null) return;
            long logFileID = fc.getStatus().getLogFileID();
            if (logFileID <= 0) {
                FilesetData data = fc.getStatus().getFileset();
                if (data == null) return;
                model.fireImportLogFileLoading(data.getId(),
                        fc.getIndex());
            } else downloadLogFile(logFileID);
        } else if (
            FileImportComponentI.RETRIEVE_LOGFILEPROPERTY.equals(
                name)) {
            FilesetData data = (FilesetData) evt.getNewValue();
            if (data != null)
                model.fireImportLogFileLoading(data.getId(), id);
        } else if (
            FileImportComponentI.CHECKSUM_DISPLAY_PROPERTY.equals(
                name)) {
            Status label = (Status) evt.getNewValue();
            CheckSumDialog d = new CheckSumDialog(view, label);
            UIUtilities.centerAndShow(d);
        } else if (FileImportComponentI.RETRY_PROPERTY.equals(name)) {
            controller.retryUpload(
                    (FileImportComponent) evt.getNewValue());
        } else if (
            FileImportComponentI.CANCEL_IMPORT_PROPERTY.equals(name)) {
            controller.cancel(
                    (FileImportComponentI) evt.getNewValue());
        }
    }

}
