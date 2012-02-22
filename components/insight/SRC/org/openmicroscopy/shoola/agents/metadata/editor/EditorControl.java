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
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

//Third-party libraries
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.events.editor.EditFileEvent;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.RenderingControlLoader;
import org.openmicroscopy.shoola.agents.metadata.util.AnalysisResultsItem;
import org.openmicroscopy.shoola.agents.metadata.util.FigureDialog;
import org.openmicroscopy.shoola.agents.util.ui.ScriptingDialog;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.util.DataComponent;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.agents.util.editorpreview.PreviewPanel;
import org.openmicroscopy.shoola.agents.util.ui.ScriptMenuItem;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.AnalysisParam;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
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
import pojos.DataObject;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;
import pojos.WellSampleData;

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
	implements ActionListener, ChangeListener, PropertyChangeListener,
	MouseListener
{

	/** Bound property indicating that the save status has been modified. */
	static final String SAVE_PROPERTY = "save";
	
	/** Action id indicating to upload attach documents. */
	static final int	ADD_LOCAL_DOCS = 0;
	
	/** Action id indicating to upload attach documents. */
	static final int	ADD_UPLOADED_DOCS = 1;
	
	/** Action id indicating to attach tags. */
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
	
	/** Action ID to export the image as OME-TIFF. */
	static final int	EXPORT_AS_OMETIFF = 11;
	
	/** Action ID to create a figure with split view of images. */
	static final int	SPLIT_VIEW_FIGURE = 12;
	
	/** Action ID to create a figure with split view of images. */
	static final int	SPLIT_VIEW_ROI_FIGURE = 13;
	
	/** Action ID to create a thumbnail figure with the collection of images. */
	static final int	THUMBNAILS_FIGURE = 14;
	
	/** Action ID to analyze the image. */
	static final int	ANALYSE_FRAP = 15;
	
	/** Action ID to create a movie figure with the collection of images. */
	static final int	MOVIE_FIGURE = 16;
	
	/** Action ID to upload a script to the server. */
	static final int	UPLOAD_SCRIPT = 17;
	
	/** Action ID to upload a script to the server. */
	static final int	RELOAD_SCRIPT = 18;
	
	/** Action id indicating to remove tags. */
	static final int	REMOVE_TAGS = 19;
	
	/** Action id indicating to remove documents. */
	static final int	REMOVE_DOCS = 20;
	
	/** Action ID to save the images as full size <code>JPEG</code>.*/
	static final int	SAVE_AS = 21;
	
	/** Action ID to view the image.*/
	static final int	VIEW_IMAGE = 22;
	
    /** Reference to the Model. */
    private Editor		model;
    
    /** Reference to the View. */
    private EditorUI	view;
    
	/** Collection of supported file formats. */
	private List<FileFilter>	filters; 
	
	/** Collection of supported export formats. */
	private List<FileFilter>	exportFilters;
	
	/** Collection of supported formats. */
	private List<FileFilter>	saveAsFilters;
	
	/** Reference to the figure dialog. */
	private FigureDialog		figureDialog;
	
	/** Launches RAPID. */
	private void openFLIM()
	{
		String url = (String) 
			MetadataViewerAgent.getRegistry().lookup(LookupNames.RAPID);
		MetadataViewerAgent.getRegistry().getTaskBar().openURL(url);
	}
	
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
		saveAsFilters  = new ArrayList<FileFilter>();
		saveAsFilters.add(new JPEGFilter());
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
		chooser.setMultiSelectionEnabled(true);
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
		bus.post(new ViewImage(new ViewImageObject(imageID), null));
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
		FileChooser chooser = new FileChooser(f, FileChooser.FOLDER_CHOOSER, 
				"Download", "Select where to download the file.", null, true);
		chooser.setSelectedFileFull(view.getRefObjectName());
		IconManager icons = IconManager.getInstance();
		chooser.setTitleIcon(icons.getIcon(IconManager.DOWNLOAD_48));
		chooser.setApproveButtonText("Download");
		chooser.addPropertyChangeListener(new PropertyChangeListener() {
		
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
					String path = (String) evt.getNewValue();
					if (path == null) {
						path = UIUtilities.getDefaultFolderAsString();
					}
					if (!path.endsWith(File.separator))
						path += File.separator;
					model.download(new File(path));
				}
			}
		});
		chooser.centerDialog();
	}
	
	/** Brings up the folder chooser to select where to save the files. */
	private void saveAsJPEG()
	{
		JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		FileChooser chooser = new FileChooser(f, FileChooser.FOLDER_CHOOSER, 
				"Save As", "Select where to save locally the images as JPEG.",
				saveAsFilters);
		String s = UIUtilities.removeFileExtension(view.getRefObjectName());
		if (s != null && s.trim().length() > 0) chooser.setSelectedFile(s);
		chooser.setApproveButtonText("Save");
		IconManager icons = IconManager.getInstance();
		chooser.setTitleIcon(icons.getIcon(IconManager.SAVE_AS_48));
		chooser.addPropertyChangeListener(new PropertyChangeListener() {
		
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
					//File[] files = (File[]) evt.getNewValue();
					String value = (String) evt.getNewValue();
					File folder = null;//files[0];
					if (value == null || value.trim().length() == 0)
						folder = UIUtilities.getDefaultFolder();
					else folder = new File(value);
					Object src = evt.getSource();
					if (src instanceof FileChooser) {
						((FileChooser) src).setVisible(false);
						((FileChooser) src).dispose();
					}
					model.saveAs(folder);
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
					File[] files = (File[]) evt.getNewValue();
					File folder = files[0];
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
	 * Creates or recycles the existing figure dialog.
	 * 
	 * @param name   The name to display.
	 * @param pixels The pixels object of reference.
	 * @param index One of the constants defined by this class.
	 * @return See above.
	 */
	FigureDialog createFigureDialog(String name, PixelsData pixels, int index)
	{
		if (figureDialog != null) return figureDialog;
		UserNotifier un = MetadataViewerAgent.getRegistry().getUserNotifier();
		if (FigureDialog.needPixels(index) && pixels == null) {
			un.notifyInfo("Figure", "The image is not valid," +
					" cannot create the figure.");
			return null;
		}
		JFrame f = 
			MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		figureDialog = new FigureDialog(f, name, pixels, index, 
				view.getRefObject().getClass());
		figureDialog.addPropertyChangeListener(this);
		return figureDialog;
	}
	
	/**
	 * Returns the dialog.
	 * 
	 * @return See above.
	 */
	FigureDialog getFigureDialog() { return figureDialog; }
	
	/**
	 * Reacts to state changes in the {@link ImViewer}.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() instanceof JTabbedPane) {
			JTabbedPane pane = (JTabbedPane) e.getSource();
			if (view.checkIfTabEnabled(pane.getSelectedIndex())) {
				if (pane.getSelectedIndex() == EditorUI.RND_INDEX)
					model.loadRenderingControl(RenderingControlLoader.LOAD);
			}
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
			Boolean b = (Boolean) evt.getNewValue();
			view.saveData(b.booleanValue());
		} else if (MetadataViewer.CLEAR_SAVE_DATA_PROPERTY.equals(name) ||
				MetadataViewer.ON_DATA_SAVE_PROPERTY.equals(name) ||
				MetadataViewer.ADMIN_UPDATED_PROPERTY.equals(name)) {
			view.clearData();
		} else if (UIUtilities.COLLAPSED_PROPERTY_JXTASKPANE.equals(name)) {
			view.handleTaskPaneCollapsed((JXTaskPane) evt.getSource());
		} else if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
			File[] files = (File[]) evt.getNewValue();
			view.attachFiles(files);
		} else if (AnnotationUI.REMOVE_ANNOTATION_PROPERTY.equals(name)) {
			Object object = evt.getNewValue();
			if (object instanceof DocComponent) {
				DocComponent doc = (DocComponent) object;
				Object data = doc.getData();
				if (data instanceof File) view.removeAttachedFile(data);
				else if (data instanceof FileAnnotationData)
					view.removeAttachedFile(data);
				else if (data instanceof TagAnnotationData)
					view.removeObject((DataObject) data);
			} 
		} else if (AnnotationUI.DELETE_ANNOTATION_PROPERTY.equals(name)) {
			Object object = evt.getNewValue();
			if (object instanceof DocComponent) {
				DocComponent doc = (DocComponent) object;
				Object data = doc.getData();
				if (data instanceof FileAnnotationData) {
					view.deleteAnnotation((FileAnnotationData) data);
					view.removeAttachedFile(data);
				}
			} else if (object instanceof TextualAnnotationComponent) {
				TextualAnnotationComponent doc = 
					(TextualAnnotationComponent) object;
				TextualAnnotationData data = doc.getData();
				//view.deleteAnnotation((TextualAnnotationData) data);
				view.removeObject(data);
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
		}  else if (SelectionWizard.SELECTED_ITEMS_PROPERTY.equals(name)) {
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
		} else if (MetadataViewer.SETTINGS_APPLIED_PROPERTY.equals(name)) {
			model.loadRenderingControl(RenderingControlLoader.RELOAD);
			view.onSettingsApplied(true);
		} else if (MetadataViewer.ACTIVITY_OPTIONS_PROPERTY.equals(name)) {
			List l = (List) evt.getNewValue();
			view.activityOptions((Component) l.get(0), (Point) l.get(1),
					(Integer) l.get(2));
		} else if (FigureDialog.CREATE_FIGURE_PROPERTY.equals(name)) {
			view.createFigure(evt.getNewValue());
		} else if (FigureDialog.CLOSE_FIGURE_PROPERTY.equals(name)) {
			figureDialog = null;
		} else if (MetadataViewer.CLOSE_RENDERER_PROPERTY.equals(name)) {
			view.discardRenderer(evt.getNewValue());
		} else if (ScriptingDialog.RUN_SELECTED_SCRIPT_PROPERTY.equals(name)) {
			view.manageScript((ScriptObject) evt.getNewValue(), 
					MetadataViewer.RUN);
		} else if (ScriptingDialog.DOWNLOAD_SELECTED_SCRIPT_PROPERTY.equals(
				name)) {
			Object value = evt.getNewValue();
			if (value instanceof ScriptObject)
				view.manageScript((ScriptObject) value, 
						MetadataViewer.DOWNLOAD);
			else if (value instanceof String) {
				ScriptObject script = view.getScriptFromName((String) value);
				if (script != null)
					view.manageScript(script, MetadataViewer.DOWNLOAD);
			}
		} else if (ScriptingDialog.VIEW_SELECTED_SCRIPT_PROPERTY.equals(name)) {
			Object value = evt.getNewValue();
			if (value instanceof ScriptObject)
				view.manageScript((ScriptObject) value, 
						MetadataViewer.VIEW);
			else if (value instanceof String) {
				ScriptObject script = view.getScriptFromName((String) value);
				if (script != null)
					view.manageScript(script, MetadataViewer.VIEW);
			}
		} else if (AnalysisResultsItem.ANALYSIS_RESULTS_DELETE.equals(name)) {
			AnalysisResultsItem item = (AnalysisResultsItem) evt.getNewValue();
			List<FileAnnotationData> list = item.getAttachments();
			view.fireAnnotationsDeletion(list);
		} else if (AnalysisResultsItem.ANALYSIS_RESULTS_VIEW.equals(name)) {
			AnalysisResultsItem item = (AnalysisResultsItem) evt.getNewValue();
			if (view.getRndIndex() == MetadataViewer.RND_GENERAL) {
				model.displayAnalysisResults(item);
				/*
				ViewImage event = new ViewImage(item.getData(), null);
				event.setAnalysis(item);
				EventBus bus = MetadataViewerAgent.getRegistry().getEventBus();
				bus.post(event);
				*/
			} else {
				model.displayAnalysisResults(item);
			}
		} else if (AnalysisResultsItem.ANALYSIS_RESULTS_CANCEL.equals(name)) {
			AnalysisResultsItem item = (AnalysisResultsItem) evt.getNewValue();
			view.cancelAnalysisResultsLoading(item);
		}
	}

	/**
	 * Handles events.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() instanceof ScriptMenuItem) {
			ScriptMenuItem item = (ScriptMenuItem) e.getSource();
			if (item.isScriptWithUI()) {
				switch (item.getIndex()) {
					case ScriptMenuItem.MOVIE_FIGURE_SCRIPT:
						model.createFigure(FigureDialog.MOVIE);
						break;
					case ScriptMenuItem.ROI_FIGURE_SCRIPT:
						model.createFigure(FigureDialog.SPLIT_ROI);
						break;
					case ScriptMenuItem.THUMBNAIL_FIGURE_SCRIPT:
						model.createFigure(FigureDialog.THUMBNAILS);
						break;
					case ScriptMenuItem.SPLIT_VIEW_FIGURE_SCRIPT:
						model.createFigure(FigureDialog.SPLIT);
						break;
					case ScriptMenuItem.MOVIE_EXPORT_SCRIPT:
						view.makeMovie(-1, null);
						break;
					case ScriptMenuItem.FLIM_SCRIPT:
						openFLIM();
				}
			} else {
				ScriptObject object = item.getScript();
				if (!object.isParametersLoaded())
					model.loadScript(object.getScriptID());
				else model.setScript(object);
			}
			return;
		}
		
		int index = Integer.parseInt(e.getActionCommand());
		
		switch (index) {
			case ADD_LOCAL_DOCS:
				selectFileToAttach();
				break;
			case ADD_UPLOADED_DOCS:
				model.loadExistingAttachments();
				break;
			case SAVE:
				view.saveData(true);
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
				view.analyse(AnalysisParam.FLIM);
				break;
			case ANALYSE_FRAP:
				view.analyse(AnalysisParam.FRAP);
				break;
			case REFRESH:
				model.refresh();
				break;
			case EXPORT_AS_OMETIFF:
				export();
				break;
			case SPLIT_VIEW_FIGURE:
				model.createFigure(FigureDialog.SPLIT);
				break;
			case SPLIT_VIEW_ROI_FIGURE:
				model.createFigure(FigureDialog.SPLIT_ROI);
				break;
			case THUMBNAILS_FIGURE:
				model.createFigure(FigureDialog.THUMBNAILS);
				break;
			case MOVIE_FIGURE:
				model.createFigure(FigureDialog.MOVIE);
				break;
			case UPLOAD_SCRIPT:
				view.uploadScript();
				break;
			case RELOAD_SCRIPT:
				view.reloadScript();
				break;
				/*
			case REMOVE_TAGS:
				view.removeTags();
				break;
			case REMOVE_DOCS:
				view.removeAttachedFiles();
				break;
				*/
			case SAVE_AS:
				saveAsJPEG();
				break;
			case VIEW_IMAGE:
				Object refObject = view.getRefObject();
				ImageData img = null;
				if (refObject instanceof ImageData) {
		        	img = (ImageData) refObject;
		        } else if (refObject instanceof WellSampleData) {
		        	img = ((WellSampleData) refObject).getImage();
		        }
				if (img != null) {
					ViewImageObject vio = new ViewImageObject(img);
					EditorAgent.getRegistry().getEventBus().post(
							new ViewImage(vio, null));
				}
		}
	}

	/**
	 * Removes the tags or files.
	 * @see MouseListener#mouseReleased(MouseEvent)
	 */
	public void mouseReleased(MouseEvent e)
	{
		JButton src = (JButton) e.getSource();
		int index = Integer.parseInt(src.getActionCommand());
		Point p = e.getPoint();
		SwingUtilities.convertPointToScreen(p, src);
		switch (index) {
			case REMOVE_TAGS:
				view.removeTags(p);
				break;
			case REMOVE_DOCS:
				view.removeAttachedFiles(p);
		}
	}
	
	/**
	 * Required by the {@link MouseListener} I/F but no-operation
	 * implementation in our case.
	 * @see MouseListener#mouseClicked(MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {}

	/**
	 * Required by the {@link MouseListener} I/F but no-operation
	 * implementation in our case.
	 * @see MouseListener#mouseEntered(MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {}

	/**
	 * Required by the {@link MouseListener} I/F but no-operation
	 * implementation in our case.
	 * @see MouseListener#mouseExited(MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {}

	/**
	 * Required by the {@link MouseListener} I/F but no-operation
	 * implementation in our case.
	 * @see MouseListener#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {}

}