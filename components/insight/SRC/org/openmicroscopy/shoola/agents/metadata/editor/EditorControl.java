/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.editor;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.RenderingControlLoader;
import org.openmicroscopy.shoola.agents.metadata.util.AnalysisResultsItem;
import org.openmicroscopy.shoola.agents.metadata.util.FigureDialog;
import org.openmicroscopy.shoola.agents.util.ui.DowngradeChooser;
import org.openmicroscopy.shoola.agents.util.ui.ScriptingDialog;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.util.DataComponent;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.agents.util.editorpreview.PreviewPanel;
import org.openmicroscopy.shoola.agents.util.ui.ScriptMenuItem;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.events.ViewInPluginEvent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.AnalysisParam;
import org.openmicroscopy.shoola.env.data.model.FigureParam;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.data.util.Target;
import org.openmicroscopy.shoola.env.event.EventBus;

import omero.log.LogMessage;
import omero.log.Logger;

import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.env.ui.RefWindow;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
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
import org.openmicroscopy.shoola.util.filter.file.ZipFilter;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import org.openmicroscopy.shoola.util.ui.omeeditpane.OMEWikiComponent;
import org.openmicroscopy.shoola.util.ui.omeeditpane.WikiDataObject;

import omero.gateway.model.BooleanAnnotationData;
import omero.gateway.model.ChannelData;
import omero.gateway.model.DataObject;
import omero.gateway.model.DoubleAnnotationData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.ImageData;
import omero.gateway.model.LongAnnotationData;
import omero.gateway.model.PixelsData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TermAnnotationData;
import omero.gateway.model.WellSampleData;
import omero.gateway.model.XMLAnnotationData;

/** 
 * The Editor's controller.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
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
	
	/** Action ID to view the image.*/
	static final int	VIEW_IMAGE_IN_IJ = 23;

	/** Action ID to load the file paths.*/
	static final int   SHOW_FILE_PATHS = 24;

	/** Action id indicating to remove other annotations. */
	static final int REMOVE_OTHER_ANNOTATIONS = 25;
	
    /**
     * Action ID to load the file path triggered by click on inplace import
     * icon.
     */
    static final int SHOW_LOCATION = 26;
	
    /** Reference to the Model. */
    private Editor		model;
    
    /** Reference to the View. */
    private EditorUI	view;
    
	/** Collection of supported file formats. */
	private List<FileFilter>	filters; 
	
	/** Collection of supported export formats. */
	private List<FileFilter>	exportFilters;
	
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
		ViewImage evt = new ViewImage(model.getSecurityContext(),
				new ViewImageObject(imageID), null);
		evt.setPlugin(MetadataViewerAgent.runAsPlugin());
		bus.post(evt);
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
	
    /** Brings up the folder chooser. */
    private void download() {
        JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();

        int type = FileChooser.FOLDER_CHOOSER;

        FileChooser chooser = new FileChooser(f, type,
                FileChooser.DOWNLOAD_TEXT, FileChooser.DOWNLOAD_DESCRIPTION);

        IconManager icons = IconManager.getInstance();
        chooser.setTitleIcon(icons.getIcon(IconManager.DOWNLOAD_48));
        chooser.setApproveButtonText(FileChooser.DOWNLOAD_TEXT);
        chooser.setCheckOverride(true);
        chooser.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                FileChooser src = (FileChooser) evt.getSource();
                if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
                    String path = (String) evt.getNewValue();
                    model.downloadOriginal(path, src.isOverride());
                }
            }
        });
        chooser.centerDialog();
    }

	/** Brings up the folder chooser to select where to save the files. 
	 * 
	 * @param format One of the formats defined by <code>FigureParam</code>.
	 * @see org.openmicroscopy.shoola.env.data.model.FigureParam
	 */
	void saveAs(final int format)
	{
		String v = FigureParam.FORMATS.get(format);
		final JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		List<FileFilter> filters = new ArrayList<FileFilter>();
		filters.add(new ZipFilter());
		FileChooser chooser = new FileChooser(f, FileChooser.SAVE, 
				"Save As", "Select where to save locally the images as "+v,
				filters);
		try {
			File file = UIUtilities.getDefaultFolder();
			if (file != null) 
				chooser.setCurrentDirectory(file);
			chooser.setSelectedFile(UIUtilities.generateFileName(file,
					"Batch_Image_Export", "zip"));
		} catch (Exception ex) {}
		chooser.setApproveButtonText("Save");
		IconManager icons = IconManager.getInstance();
		chooser.setTitleIcon(icons.getIcon(IconManager.SAVE_AS_48));
		chooser.addPropertyChangeListener(new PropertyChangeListener() {
		
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
					File[] files = (File[]) evt.getNewValue();
					if (files == null || files.length == 0)
						return;
					File file = files[0];
					if (file == null)
						file = UIUtilities.generateFileName(
								UIUtilities.getDefaultFolder(),
								"Batch_Image_Export", "zip");
					if (file.exists()) {
						MessageBox msg = new MessageBox(f, "File Exists",
								"Do you want to overwrite the file?");
						int option = msg.centerMsgBox();
						if (option == MessageBox.NO_OPTION) {
							return;
						}
					}
					Object src = evt.getSource();
					if (src instanceof FileChooser) {
						((FileChooser) src).setVisible(false);
						((FileChooser) src).dispose();
					}
					model.saveAs(file.getParentFile(), format,
							UIUtilities.removeFileExtension(file.getName()));
				}
			}
		});
		chooser.centerDialog();
	}
	
	
	/** Brings up the folder chooser. */
	private void export()
	{
		DowngradeChooser chooser = new DowngradeChooser(new RefWindow(),
				FileChooser.SAVE, "Export",
				"Select where to export the image as OME-TIFF.", exportFilters);
		try {
			chooser.parseData();
		} catch (Exception e) {
			LogMessage msg = new LogMessage();
			msg.print(e);
			MetadataViewerAgent.getRegistry().getLogger().debug(this, msg);
		}
		String s = UIUtilities.removeFileExtension(view.getRefObjectName());
		chooser.setSelectedFileFull(s);
		chooser.setCheckOverride(true);
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
					Target target = null;
					if (src instanceof DowngradeChooser) {
						((FileChooser) src).setVisible(false);
						((FileChooser) src).dispose();
						target = ((DowngradeChooser) src).getSelectedSchema();
					}
					model.exportImageAsOMETIFF(folder, target);
				} else if (DowngradeChooser.HELP_DOWNGRADE_PROPERTY.equals(
						name)) {
					Registry reg = MetadataViewerAgent.getRegistry();
					String url = (String) reg.lookup("HelpDowngrade");
					reg.getTaskBar().openURL(url);
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
	 * Loads the file set linked to the image.
	 * */
	void loadFileset() { model.loadFileset(); }
	
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
				MetadataViewer.ADMIN_UPDATED_PROPERTY.equals(name)) {
			view.clearData();
		} else if (MetadataViewer.ON_DATA_SAVE_PROPERTY.equals(name)
				&& evt.getNewValue() == view.getRefObject()) {
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
				
				
				if (data instanceof FileAnnotationData) {
				    model.removeFileAnnotations(Collections.singletonList((FileAnnotationData)data));
				}
				
				else if (data instanceof TagAnnotationData ||
						data instanceof TermAnnotationData ||
						data instanceof XMLAnnotationData ||
						data instanceof LongAnnotationData ||
						data instanceof DoubleAnnotationData ||
						data instanceof BooleanAnnotationData)
					view.removeObject((DataObject) data);
			} 
			else if (object instanceof TextualAnnotationComponent) {
                            TextualAnnotationComponent doc = 
                                    (TextualAnnotationComponent) object;
                            view.removeObject(doc.getData());
                    }
		} else if (AnnotationUI.EDIT_TAG_PROPERTY.equals(name)) {
			Object object = evt.getNewValue();
			if (object instanceof DocComponent) {
				//Save the tag w/o update.
				DataObject d = (DataObject) ((DocComponent) object).getData();
				//Save the tag
				OmeroMetadataService svc = 
					MetadataViewerAgent.getRegistry().getMetadataService();
				long id = MetadataViewerAgent.getUserDetails().getId();
				try {
					svc.saveData(model.getSecurityContext(), Arrays.asList(d),
							null, null, id);
				} catch (Exception e) {
					Logger l = MetadataViewerAgent.getRegistry().getLogger();
					LogMessage msg = new LogMessage();
					msg.print("Saving object");
					msg.print(e);
					l.error(this, msg);
				}
			}
		} else if (OMEWikiComponent.WIKI_DATA_OBJECT_PROPERTY.equals(name)) {
			WikiDataObject object = (WikiDataObject) evt.getNewValue();
			long id;
			switch (object.getIndex()) {
				case WikiDataObject.IMAGE:
					id = object.getId();
					if (id < 0) viewImage(object.getName());
					else viewImage(id);
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
		} else if (MetadataViewer.RELATED_NODES_PROPERTY.equals(name)) {
			view.onRelatedNodesSet();
		} else if (ScriptingDialog.RUN_SELECTED_SCRIPT_PROPERTY.equals(name)) {
			//view.manageScript((ScriptObject) evt.getNewValue(), 
			//		MetadataViewer.RUN);
		} else if (ScriptingDialog.DOWNLOAD_SELECTED_SCRIPT_PROPERTY.equals(
				name)) {
			Object value = evt.getNewValue();
			if (value instanceof ScriptObject)
				view.manageScript((ScriptObject) value, 
						MetadataViewer.DOWNLOAD);
			else if (value instanceof String) {
				ScriptObject script = model.getScriptFromName((String) value);
				if (script != null)
					view.manageScript(script, MetadataViewer.DOWNLOAD);
			}
		} else if (ScriptingDialog.VIEW_SELECTED_SCRIPT_PROPERTY.equals(name)) {
			Object value = evt.getNewValue();
			if (value instanceof ScriptObject)
				view.manageScript((ScriptObject) value, 
						MetadataViewer.VIEW);
			else if (value instanceof String) {
				ScriptObject script = model.getScriptFromName((String) value);
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
					if (model.getRenderer() != null) {
					    RndProxyDef def = model.getRenderer().getSelectedDef();
					    if (def != null) {
					        vio.setSelectedRndDef(
					                def.getData().getId().getValue());
					    }
					}
					MetadataViewerAgent.getRegistry().getEventBus().post(
						new ViewImage(model.getSecurityContext(), vio, null));
				}
				break;
			case VIEW_IMAGE_IN_IJ:
				Object object = view.getRefObject();
				ImageData image = null;
				if (object instanceof ImageData) {
					image = (ImageData) object;
		        } else if (object instanceof WellSampleData) {
		        	image = ((WellSampleData) object).getImage();
		        }
				if (image != null) {
					ViewInPluginEvent event = new ViewInPluginEvent(
						model.getSecurityContext(),
						(DataObject) object, LookupNames.IMAGE_J);
					MetadataViewerAgent.getRegistry().getEventBus().post(event);
				}
				break;
			case SHOW_FILE_PATHS:
				if (view.getFileset() != null) {
				    view.displayFileset();
				} 
				else {
				    loadFileset();
				}
				break;
			case SHOW_LOCATION:
			    	view.displayLocation();
		}
	}

	/**
	 * Removes the tags or files.
	 * @see MouseListener#mouseReleased(MouseEvent)
	 */
	public void mouseReleased(MouseEvent e)
	{
		JButton src = (JButton) e.getSource();
		if (!src.isEnabled()) return;
		int index = Integer.parseInt(src.getActionCommand());
		Point p = e.getPoint();
		switch (index) {
			case REMOVE_TAGS:
				view.removeTags(src, p);
				break;
			case REMOVE_DOCS:
				view.removeAttachedFiles(src, p);
				break;
			case REMOVE_OTHER_ANNOTATIONS:
				view.removeOtherAnnotations(src, p);
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