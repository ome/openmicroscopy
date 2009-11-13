/*
 * org.openmicroscopy.shoola.agents.metadata.editor.EditorControl 
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
package org.openmicroscopy.shoola.agents.metadata.editor;



//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.agents.events.editor.EditFileEvent;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.RenderingControlLoader;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.util.DataComponent;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.agents.util.editorpreview.PreviewPanel;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.filter.file.EditorFileFilter;
import org.openmicroscopy.shoola.util.filter.file.ExcelFilter;
import org.openmicroscopy.shoola.util.filter.file.HTMLFilter;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.OMETIFFFilter;
import org.openmicroscopy.shoola.util.filter.file.PDFFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.filter.file.PowerPointFilter;
import org.openmicroscopy.shoola.util.filter.file.TEXTFilter;
import org.openmicroscopy.shoola.util.filter.file.TIFFFilter;
import org.openmicroscopy.shoola.util.filter.file.WordFilter;
import org.openmicroscopy.shoola.util.filter.file.XMLFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import org.openmicroscopy.shoola.util.ui.omeeditpane.OMEWikiComponent;
import org.openmicroscopy.shoola.util.ui.omeeditpane.WikiDataObject;
import pojos.ChannelData;
import pojos.FileAnnotationData;
import pojos.TagAnnotationData;

/** 
 * The Editor's controller.
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
class EditorControl
	implements ActionListener, ChangeListener, PropertyChangeListener
{

	/** Bound property indicating that the save status has been modified. */
	static final String SAVE_PROPERTY = "save";
	
	/** Action id indicating to upload attach documents. */
	static final int	ADD_LOCAL_DOCS = 0;
	
	/** Action id indicating to upload attach documents. */
	static final int	ADD_UPLOADED_DOCS = 1;
	
	/** Action id indicating to attach documents. */
	static final int	ADD_TAGS = 2;
	
	/** Action ID to save the data. */
	static final int 	SAVE = 3;
	
	/** Action ID to download archived files. */
	static final int	DOWNLOAD = 4;

	/** Action ID to display the acquisition metadata. */
	static final int	ACQUISITION_METADATA = 5;
	
	/** Action ID indicating to create a new experiment. */
	static final int	CREATE_NEW_EXPERIMENT = 6;
	
	/** Action ID to create a movie. */
	static final int	CREATE_MOVIE = 7;
	
	/** 
	 * Action ID indicating to load the renderer for the primary selected
	 * image. 
	 */
	static final int	RENDERER = 8;
	
	/** Action ID to analyze the image. */
	static final int	ANALYSE_FLIM = 9;
	
	/** Action ID to refresh the selected tab. */
	static final int	REFRESH = 10;
	
	/** Action ID to export the image. */
	static final int	EXPORT = 11;
	
    /** Reference to the Model. */
    private Editor		model;
    
    /** Reference to the View. */
    private EditorUI	view;
    
	/** Collection of supported file formats. */
	private List<FileFilter>	filters; 
	
	/** Collection of supported export formats. */
	private List<FileFilter>	exportFilters;
	
	/** Creates the collection of supported file filters. */
	private void createFileFilters()
	{
		filters = new ArrayList<FileFilter>();
		filters.add(new PDFFilter());
		filters.add(new PNGFilter());
		filters.add(new HTMLFilter());
		filters.add(new JPEGFilter());
		filters.add(new ExcelFilter());
		filters.add(new WordFilter());
		filters.add(new PowerPointFilter());
		filters.add(new EditorFileFilter());
		filters.add(new XMLFilter());
		filters.add(new TIFFFilter());
		filters.add(new TEXTFilter());
		exportFilters = new ArrayList<FileFilter>();
		exportFilters.add(new OMETIFFFilter());
	}

	/** 
	 * Launches a dialog to select the file to attach to the 
	 * <code>DataObject</code>.
	 */
	private void selectFileToAttach()
	{
		JFrame owner = 
			MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		FileChooser chooser = new FileChooser(owner, FileChooser.LOAD, 
				"Choose File", "Select the file to attach.", filters, true);
		IconManager icons = IconManager.getInstance();
		chooser.setTitleIcon(icons.getIcon(IconManager.ATTACHMENT_48));
		chooser.setApproveButtonText("Attach");
		chooser.addPropertyChangeListener(
				FileChooser.APPROVE_SELECTION_PROPERTY, this);
		UIUtilities.centerAndShow(chooser);
	}
	
	/**
	 * Posts an event to view the image.
	 * 
	 * @param imageID The id of the image to view.
	 */
	private void viewImage(long imageID)
	{
		EventBus bus = MetadataViewerAgent.getRegistry().getEventBus();
		bus.post(new ViewImage(imageID, null));
	}
	
	/**
	 * Posts an event to view the image.
	 * 
	 * @param imageName The name of the image.
	 */
	private void viewImage(String imageName)
	{
		//EventBus bus = MetadataViewerAgent.getRegistry().getEventBus();
		//bus.post(new ViewImage(imageID, null));
	}
	
	/**
	 * Posts an event to view the protocol.
	 * 
	 * @param protocolID The id of the protocol to view.
	 */
	private void viewProtocol(long protocolID)
	{
		EventBus bus = MetadataViewerAgent.getRegistry().getEventBus();
		bus.post(new EditFileEvent(protocolID));
	}
	
	/** Brings up the folder chooser. */
	private void download()
	{
		JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		FileChooser chooser = new FileChooser(f, FileChooser.SAVE, 
				"Download", "Select where to download the file.", null, true);
		chooser.setSelectedFileFull(view.getRefObjectName());
		IconManager icons = IconManager.getInstance();
		chooser.setTitleIcon(icons.getIcon(IconManager.DOWNLOAD_48));
		chooser.setApproveButtonText("Download");
		chooser.addPropertyChangeListener(new PropertyChangeListener() {
		
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
					File folder = (File) evt.getNewValue();
					if (folder == null)
						folder = UIUtilities.getDefaultFolder();
					model.download(folder);
				}
			}
		});
		chooser.centerDialog();
	}
	
	/** Brings up the folder chooser. */
	private void export()
	{
		JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		FileChooser chooser = new FileChooser(f, FileChooser.SAVE, 
				"Export", "Select where to export the image as OME-TIFF.",
				exportFilters);
		String s = UIUtilities.removeFileExtension(view.getRefObjectName());
		if (s != null && s.trim().length() > 0) chooser.setSelectedFile(s);
		chooser.setApproveButtonText("Export");
		IconManager icons = IconManager.getInstance();
		chooser.setTitleIcon(icons.getIcon(IconManager.EXPORT_AS_OMETIFF_48));
		chooser.addPropertyChangeListener(new PropertyChangeListener() {
		
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
					File folder = (File) evt.getNewValue();
					if (folder == null)
						folder = UIUtilities.getDefaultFolder();
					Object src = evt.getSource();
					if (src instanceof FileChooser) {
						((FileChooser) src).setVisible(false);
						((FileChooser) src).dispose();
					}
					model.exportImageAsOMETIFF(folder);
				}
			}
		});
		chooser.centerDialog();
	}
	
	/**
     * Links this Controller to its Model and its View.
     * 
     * @param model	Reference to the Model. Mustn't be <code>null</code>.
     * @param view	Reference to the View. Mustn't be <code>null</code>.
     */
    void initialize(Editor model, EditorUI view)
    {
        if (view == null) throw new NullPointerException("No view.");
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        this.view = view;
        createFileFilters();
    }
	
	/** Displays the image info. */
	void loadChannelData() { model.loadChannelData(); }
	
	/** 
	 * Loads the container hosting the currently edited object,
	 * forwards call to the model. 
	 */
	void loadParents() { model.loadParents(); }

	/** Loads the image acquisition data. */
	void loadImageAcquisitionData() { model.loadImageAcquisitionData(); }
	
	/** 
	 * Loads the channel acquisition data. 
	 * 
	 * @param channel The channel to handle.
	 */
	void loadChannelAcquisitionData(ChannelData channel)
	{ 
		model.loadChannelAcquisitionData(channel);
	}
	
	/** Loads the instrument related to the image. */
	void loadInstrumentData() { model.loadInstrumentData(); }
	
	/** Loads the existing Tags. */
	void loadExistingTags() { model.loadExistingTags(); }

	/**
	 * Returns <code>true</code> if the display is for a single 
	 * object, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isSingleMode() { return view.isSingleMode(); }

	/**
	 * Reacts to state changes in the {@link ImViewer}.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() instanceof JTabbedPane) {
			JTabbedPane pane = (JTabbedPane) e.getSource();
			if (pane.getSelectedIndex() == EditorUI.RND_INDEX)
				model.loadRenderingControl(RenderingControlLoader.LOAD);
		}
	}
	
	/**
	 * Reacts to property change.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (SAVE_PROPERTY.equals(name) || 
				DataComponent.DATA_MODIFIED_PROPERTY.equals(name) ||
				PreviewPanel.PREVIEW_EDITED_PROPERTY.equals(name)) {
			view.setDataToSave(view.hasDataToSave());
		} else if (MetadataViewer.SAVE_DATA_PROPERTY.equals(name)) {
			view.saveData();
		} else if (MetadataViewer.CLEAR_SAVE_DATA_PROPERTY.equals(name) ||
				MetadataViewer.ON_DATA_SAVE_PROPERTY.equals(name)) {
			view.clearData();
		} else if (UIUtilities.COLLAPSED_PROPERTY_JXTASKPANE.equals(name)) {
			view.handleTaskPaneCollapsed((JXTaskPane) evt.getSource());
		} else if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
			view.attachFile((File) evt.getNewValue());
		} else if (AnnotationUI.REMOVE_ANNOTATION_PROPERTY.equals(name)) {
			Object object = evt.getNewValue();
			if (object instanceof DocComponent) {
				DocComponent doc = (DocComponent) object;
				Object data = doc.getData();
				if (data instanceof File) view.removeAttachedFile(data);
				else if ((data instanceof FileAnnotationData))
					view.removeAttachedFile(data);
				else if (data instanceof TagAnnotationData)
					view.removeTag((TagAnnotationData) data);
			} 
		} else if (AnnotationUI.EDIT_TAG_PROPERTY.equals(name)) {
			Object object = evt.getNewValue();
			if (object instanceof DocComponent) {
				view.setDataToSave(view.hasDataToSave());
			}
		} else if (OMEWikiComponent.WIKI_DATA_OBJECT_PROPERTY.equals(name)) {
			WikiDataObject object = (WikiDataObject) evt.getNewValue();
			long id;
			switch (object.getIndex()) {
				case WikiDataObject.IMAGE:
					id = object.getId();
					if (id < 0) viewImage(object.getName());
					else viewImage(id);
					break;
				case WikiDataObject.PROTOCOL:
					viewProtocol(object.getId());
					break;
			}
		} else if (SelectionWizard.SELECTED_ITEMS_PROPERTY.equals(name)) {
			Map m = (Map) evt.getNewValue();
			if (m == null || m.size() != 1) return;
			Set set = m.entrySet();
			Entry entry;
			Iterator i = set.iterator();
			Class type;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				type = (Class) entry.getKey();
				view.handleObjectsSelection(type, 
						(Collection) entry.getValue());
			}
		} else if (PreviewPanel.OPEN_FILE_PROPERTY.equals(name)) {
			Long id = (Long) evt.getNewValue();
			if (id != null) viewProtocol(id.longValue());
		} else if (MetadataViewer.CREATING_MOVIE_PROPERTY.equals(name)) {
			boolean b = (Boolean) evt.getNewValue();
			view.createMovie(b);
		} else if (MetadataViewer.SETTINGS_APPLIED_PROPERTY.equals(name)) {
			model.loadRenderingControl(RenderingControlLoader.RELOAD);
			view.onSettingsApplied(true);
		} else if (MetadataViewer.ANALYSE_PROPERTY.equals(name)) {
			boolean b = (Boolean) evt.getNewValue();
			view.analyse(b);
		} else if (MetadataViewer.EXPORT_PROPERTY.equals(name)) {
			export();
		} else if (MetadataViewer.CLOSE_RENDERER_PROPERTY.equals(name)) {
			view.discardRenderer(evt.getNewValue());
		}
	}

	/**
	 * Handles events.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case ADD_LOCAL_DOCS:
				selectFileToAttach();
				break;
			case ADD_UPLOADED_DOCS:
				model.loadExistingAttachments();
				break;
			case SAVE:
				view.saveData();
				break;
			case DOWNLOAD:
				download();
				break;
			case ADD_TAGS:
				loadExistingTags();
				break;
			case CREATE_NEW_EXPERIMENT:
				view.createNewExperiment();
				break;
			case CREATE_MOVIE:
				view.makeMovie(-1, null);
				break;
			case RENDERER:
				model.loadRenderingControl(RenderingControlLoader.LOAD);
				break;
			case ANALYSE_FLIM:
				view.analyse();
				break;
			case REFRESH:
				model.refresh();
				break;
			case EXPORT:
				export();
		}
	}
	
}

