/*
 * org.openmicroscopy.shoola.agents.metadata.editor.EditorComponent 
 *
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


//Java imports
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;



//Third-party libraries
import org.apache.commons.collections.CollectionUtils;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.FileAnnotationCheckResult;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.RenderingControlLoader;
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.agents.metadata.util.AnalysisResultsItem;
import org.openmicroscopy.shoola.agents.metadata.util.FigureDialog;
import org.openmicroscopy.shoola.agents.metadata.util.FileAttachmentWarningDialog;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.agents.util.ui.ScriptingDialog;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.AnnotationLinkData;
import org.openmicroscopy.shoola.env.data.model.DiskQuota;
import org.openmicroscopy.shoola.env.data.model.ExportActivityParam;
import org.openmicroscopy.shoola.env.data.model.ROIResult;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.env.data.util.Target;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

import pojos.AnnotationData;
import pojos.ChannelAcquisitionData;
import pojos.ChannelData;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.FilesetData;
import pojos.ImageAcquisitionData;
import pojos.ImageData;
import pojos.InstrumentData;
import pojos.PixelsData;
import pojos.PlateData;
import pojos.TagAnnotationData;
import pojos.WellSampleData;

/** 
 * Implements the {@link Editor} interface to provide the functionality
 * required of the hierarchy viewer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Tunneled MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class EditorComponent 
	extends AbstractComponent
	implements Editor
{

	/** The Model sub-component. */
	private EditorModel		model;
	
	/** The Control sub-component. */
	private EditorControl	controller;
	
	/** The View sub-component. */
	private EditorUI		view;
	
	/** The dialog used to display script.*/
	private ScriptingDialog dialog;
	
	/**
	 * Returns the collection of annotation that cannot be removed 
	 * by the user currently logged.
	 * 
	 * @param type The type of annotations to handle.
	 * @param common The collection of common annotations. The list will
	 * @return See above.
	 */
	private Collection<DataObject> getImmutableAnnotation(Class type,
			Collection<DataObject> common)
	{
		List<DataObject> list = new ArrayList<DataObject>();
		Map<DataObject, StructuredDataResults>
			data = model.getAllStructuredData();
		if (data == null || data.size() == 0) return list;
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>>
		i = data.entrySet().iterator();
		StructuredDataResults result;
		Collection<AnnotationLinkData> links;
		Iterator<AnnotationLinkData> j;
		AnnotationLinkData link;
		
		AnnotationData child;
		if (model.isMultiSelection()) {
			List<Long> selected = new ArrayList<Long>();
			List<Long> ids = new ArrayList<Long>(common.size());
			Iterator<DataObject> k = common.iterator();
			while (k.hasNext()) {
				ids.add(k.next().getId());
			}
			
			
			while (i.hasNext()) {
				e = i.next();
				result = e.getValue();
				links = result.getAnnotationLinks();
				if (links != null) {
					j = links.iterator();
					while (j.hasNext()) {
						link = j.next();
						child = (AnnotationData) link.getChild();
						//Exclude some file is tag
						if (child.getClass().equals(type) &&
							!model.isNameSpaceExcluded(child.getNameSpace())) {
							if (!ids.contains(link.getChild().getId())) {
								if (!selected.contains(link.getChild().getId()))
									list.add(link.getChild());
							}
						}
					}
				}
			}
		} else {
			while (i.hasNext()) {
				e = i.next();
				result = e.getValue();
				links = result.getAnnotationLinks();
				if (links != null) {
					j = links.iterator();
					while (j.hasNext()) {
						link = j.next();
						child = (AnnotationData) link.getChild();
						if (!model.isNameSpaceExcluded(child.getNameSpace()) &&
							!link.canDelete() && link.getChild().equals(type))
							list.add(link.getChild());
					}
				}
			}
		}
		return list;
	}
	/**
	 * Shows the selection wizard.
	 * 
	 * @param type			The type of objects to handle.
	 * @param available 	The available objects.
	 * @param selected  	The selected objects.
	 * @param addCreation	Pass <code>true</code> to add a component
	 * 						allowing creation of object of the passed type,
	 * 						<code>false</code> otherwise.
	 */
	private void showSelectionWizard(Class type, Collection available, 
									Collection selected, boolean addCreation)
	{
		IconManager icons = IconManager.getInstance();
		Registry reg = MetadataViewerAgent.getRegistry();
		String title = "";
		String text = "";
		Icon icon = null;
		if (TagAnnotationData.class.equals(type)) {
			title = "Tags Selection";
			text = "Select from available tags";
			icon = icons.getIcon(IconManager.TAGS_48);
		} else if (FileAnnotationData.class.equals(type)) {
			title = "Attachments Selection";
			text = "Select from available attachments";
			icon = icons.getIcon(IconManager.ATTACHMENT_48);
		}
		SelectionWizard wizard = new SelectionWizard(
				reg.getTaskBar().getFrame(), available, selected, type,
				addCreation, model.getCurrentUser());
		wizard.setImmutableElements(getImmutableAnnotation(type, selected));
		if (model.isMultiSelection())
			wizard.setAcceptButtonText("Save");
		wizard.setTitle(title, text, icon);
		wizard.addPropertyChangeListener(controller);
		UIUtilities.centerAndShow(wizard);
	}
	
	/**
	 * Creates a new instance.
	 * The {@link #initialize() initialize} method should be called straight 
	 * after to complete the MVC set up.
	 * 
	 * @param model The Model sub-component. Mustn't be <code>null</code>.
	 */
	EditorComponent(EditorModel model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		view = new EditorUI();
		controller = new EditorControl();
	}
	
	/** Links up the MVC triad. */
	void initialize()
	{
		controller.initialize(this, view);
		view.initialize(model, controller);
		model.getObservable().addPropertyChangeListener(controller);
	}
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#getUI()
	 */
	public JComponent getUI() { return view; }

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setStructuredDataResults()
	 */
	public void setStructuredDataResults()
	{
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		view.layoutUI();
		view.setStatus(false);
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setRootObject(Object)
	 */
	public void setRootObject(Object refObject)
	{
		if (refObject == null)
			throw new IllegalArgumentException("Root object not valid.");
		Object oldObject = model.getRefObject();
		
		model.setRootObject(refObject);
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		view.setRootObject(oldObject);
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setParentRootObject(Object, Object)
	 */
	public void setParentRootObject(Object parentRefObject, Object grandParent)
	{
		model.setParentRootObject(parentRefObject, grandParent);
		view.setParentRootObject();
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setExistingTags(Collection)
	 */
	public void setExistingTags(Collection tags)
	{
		model.setExistingTags(tags);
		List<TagAnnotationData> selected = new ArrayList<TagAnnotationData>();
		List<Long> ids = new ArrayList<Long>();
		TagAnnotationData tag;
		Collection<TagAnnotationData> setTags = model.getCommonTags();
		if (setTags != null) {
			Iterator<TagAnnotationData> k = setTags.iterator();
			while (k.hasNext()) {
				tag = k.next();
				if (model.isAnnotationUsedByUser(tag)) {
				    ids.add(tag.getId());
				    selected.add(tag);
				}
			}
		}
		
		List<TagAnnotationData> available = new ArrayList<TagAnnotationData>();
		if (CollectionUtils.isNotEmpty(tags)) {
			Iterator i = tags.iterator();
			TagAnnotationData data;
			Set<TagAnnotationData> l;
			Iterator<TagAnnotationData> j;
			while (i.hasNext()) {
				data = (TagAnnotationData) i.next();
				if (!ids.contains(data.getId())) {
                    available.add(data);
				}
			}
		}
		if (controller.getFigureDialog() != null) {
			List<TagAnnotationData> all = new ArrayList<TagAnnotationData>();
			all.addAll(available);
			if (CollectionUtils.isNotEmpty(setTags)) {
			    all.addAll(setTags);
			}
			controller.getFigureDialog().setTags(all);
			return;
		}
		showSelectionWizard(TagAnnotationData.class, available, selected, true);
		setStatus(false);
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setChannelsData(Map, boolean)
	 */
	public void setChannelsData(Map channels, boolean updateView)
	{
		model.setChannelData(channels);
		if (updateView) view.showChannelData();
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#hasDataToSave()
	 */
	public boolean hasDataToSave()
	{
		return view.hasDataToSave();
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setDiskSpace(DiskQuota)
	 */
	public void setDiskSpace(DiskQuota quota)
	{
		if (quota == null) return;
		view.setDiskSpace(quota);
		view.layoutUI();
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#passwordChanged(boolean)
	 */
	public void passwordChanged(boolean changed)
	{
		UserNotifier un = MetadataViewerAgent.getRegistry().getUserNotifier();
		if (changed) {
			un.notifyInfo("Password change", "The password has been " +
					"successfully modified.");
		} else {
			un.notifyInfo("Password change", "The password could not be " +
					"modified. Please try again.");
		}
		view.passwordChanged();
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#loadChannelData()
	 */
	public void loadChannelData()
	{
		if (model.isNumerousChannel()) return;
		if (model.getRndIndex() == MetadataViewer.RND_GENERAL) {
			if (model.getChannelData() == null) 
				model.loadChannelData();
		} else view.showChannelData();
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#getChannelData()
	 */
	public Map getChannelData()
	{
		return model.getChannelData();
	}
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setExistingAttachments(Collection)
	 */
	public void setExistingAttachments(Collection attachments)
	{
		if (attachments == null) return;
		model.setExistingAttachments(attachments);
		Collection setAttachments = model.getCommonAttachments();
		List selected = new ArrayList();
		List<Long> ids = new ArrayList<Long>();
		if (setAttachments != null) {
			Iterator<FileAnnotationData> k = setAttachments.iterator();
			FileAnnotationData file;
			while (k.hasNext()) {
				file = k.next();
				if (model.isAnnotationUsedByUser(file)) {
				    selected.add(file);
				    ids.add(file.getId());
				}
			}
		}
		
		List available = new ArrayList();
		
		if (attachments != null) {
			Iterator i = attachments.iterator();
			FileAnnotationData data;
			while (i.hasNext()) {
				data = (FileAnnotationData) i.next();
				if (!ids.contains(data.getId()))
					available.add(data);
			}
		}
		showSelectionWizard(FileAnnotationData.class, available, selected,
							true);
		setStatus(false);
	}
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setSelectionMode(boolean)
	 */
	public void setSelectionMode(boolean single)
	{
		if (!single) view.layoutUI();
		view.repaint();
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#loadParents()
	 */
	public void loadParents() { model.loadParents(); }

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setStatus(boolean)
	 */
	public void setStatus(boolean busy) { view.setStatus(busy); }

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setStatus(boolean)
	 */
	public void loadExistingTags()
	{
		model.loadExistingTags();
		setStatus(true);
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setImageAcquisitionData(ImageAcquisitionData)
	 */
	public void setImageAcquisitionData(ImageAcquisitionData map)
	{
		if (map == null) return;
		model.setImageAcquisitionData(map);
		view.setImageAcquisitionData();
		view.setStatus(false);
	}

    public void removeFileAnnotations(List<FileAnnotationData> annotations) {
            model.fireFileAnnotationRemoveCheck(annotations);
    }

    public void handleFileAnnotationRemoveCheck(final FileAnnotationCheckResult result) {
        if (!result.getDeleteCandidates().isEmpty()) {
            
            JFrame f = MetadataViewerAgent.getRegistry().getTaskBar()
                    .getFrame();
            
            FileAttachmentWarningDialog dlg = new FileAttachmentWarningDialog(f, result);
            dlg.addPropertyChangeListener(new PropertyChangeListener() {
                
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    if (arg0.getPropertyName().equals(FileAttachmentWarningDialog.DELETE_PROPERTY)) {
                        for (FileAnnotationData fd : result.getDeleteCandidates()) {
                          view.deleteAnnotation(fd);
                        }
                        for (FileAnnotationData fd : result.getAllAnnotations()) {
                            view.unlinkAttachedFile(fd);
                        }
                    }
                    
                }
            });
            UIUtilities.centerAndShow(dlg);
        }

        else {
            for (FileAnnotationData fd : result.getAllAnnotations()) {
                view.unlinkAttachedFile(fd);
            }
        }
    }
	
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#loadImageAcquisitionData()
	 */
	public void loadImageAcquisitionData()
	{
		Object refObject = model.getRefObject();
		if ((refObject instanceof ImageData) || 
				(refObject instanceof WellSampleData)) {
			Object data = model.getImageAcquisitionData();
			if (data != null) return;
			model.fireImagAcquisitionDataLoading();
			view.setStatus(true);
		}
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#loadChannelAcquisitionData(ChannelData)
	 */
	public void loadChannelAcquisitionData(ChannelData channel)
	{
		Object refObject = model.getRefObject();
		if ((refObject instanceof ImageData) || 
				(refObject instanceof WellSampleData)) {
			if (channel == null) return;
			Object data = model.getChannelAcquisitionData(channel.getIndex());
			if (data != null) return;
			model.fireChannelAcquisitionDataLoading(channel);
			view.setStatus(true);
		}
	}
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#loadInstrumentData()
	 */
	public void loadInstrumentData()
	{
		Object refObject = model.getRefObject();
		if (refObject instanceof WellSampleData) {
			WellSampleData wsd = (WellSampleData) refObject;
			refObject = wsd.getImage();
		}
		if (refObject instanceof ImageData) {
			ImageData img = (ImageData) refObject;
			long id = img.getInstrumentId();
			if (id > 0) {
				model.fireInstrumentDataLoading(id);
				view.setStatus(true);
			}
		}
	}
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setChannelEnumerations(Map)
	 */
	public void setChannelEnumerations(Map map)
	{
		model.setChannelEnumerations(map);
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setImageEnumerations(Map)
	 */
	public void setImageEnumerations(Map map)
	{
		model.setImageEnumerations(map);
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setChannelAcquisitionData(int, ChannelAcquisitionData)
	 */
	public void setChannelAcquisitionData(int index, 
			ChannelAcquisitionData data)
	{
		model.setChannelAcquisitionData(index, data);
		view.setChannelAcquisitionData(index);
		view.setStatus(false);
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#loadExistingAttachments()
	 */
	public void loadExistingAttachments()
	{
		model.loadExistingAttachments();
		setStatus(true);
	}
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#download(File, boolean)
	 */
	public void download(File file, boolean override)
	{
		if (file == null) return;
		model.download(file, override);
	}
	
    /**
     * Implemented as specified by the {@link Editor} interface.
     * 
     * @see Editor#downloadOriginal(String, boolean)
     */
    public void downloadOriginal(String path, boolean override) {
        if (CommonsLangUtils.isEmpty(path))
            return;

        model.downloadOriginal(path, override);
    }

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setPlaneInfo(Collection, long, int)
	 */
	public void setPlaneInfo(Collection result, long pixelsID, int channel)
	{
		Object ref = model.getRefObject();
		if (ref instanceof WellSampleData) {
		    WellSampleData ws = (WellSampleData) ref;
		    ref = ws.getImage();
		}
		if (!(ref instanceof ImageData)) return;
		ImageData img = (ImageData) ref;
		if (pixelsID != img.getDefaultPixels().getId()) return;
		FigureDialog d = controller.getFigureDialog();
		if (channel >= 0) {
			model.setPlaneInfo(channel, result);
			view.setStatus(false);
		} 
		if (d != null) {
			d.setPlaneInfo(model.getChannelPlaneInfo(
					EditorModel.DEFAULT_CHANNEL));
		} else {
			view.setPlaneInfo(channel);
		}
	}
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setRenderingControl(RenderingControl)
	 */
	public void setRenderingControl(RenderingControl rndControl)
	{
		boolean loaded = model.isRendererLoaded();
		
		setStatus(false);
		if (rndControl == null) { //exception
			setSelectedTab(GENERAL_TAB);
			return;
		}
		
		//is the rendering control for the correct pixels set
		FigureDialog d = controller.getFigureDialog();
		PixelsData data = model.getPixels();
		if (data == null) {
			setSelectedTab(GENERAL_TAB);
			return;
		}
		if (data.getId() != rndControl.getPixelsID()) {
			setSelectedTab(GENERAL_TAB);
			return;
		}
		model.setRenderingControl(rndControl);
		if (loaded) 
		    view.onSettingsApplied(false);
		if (d == null) 
		    view.setRenderer();
		if (model.getRndIndex() == MetadataViewer.RND_SPECIFIC)
			loadChannelData();
		model.getRenderer().addPropertyChangeListener(controller);
		model.onRndLoaded(false);
		if (d != null) {
			d.setRenderer(model.getRenderer());
			if (d.getDialogType() == FigureDialog.ROI_MOVIE)
				model.firePlaneInfoLoading(EditorModel.DEFAULT_CHANNEL, 0);
		}
		model.getRenderer().refresh();
		// load other users' rendering settings
		model.getRenderer().retrieveRelatedSettings();
		model.getRenderer().updatePasteAction();
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#loadRenderingControl(int)
	 */
	public void loadRenderingControl(int index)
	{
		if (model.getRenderer() == null && 
				index == RenderingControlLoader.RELOAD)
			return;
		ImageData image = model.getImage();
		if (image == null || image.getId() < 0) return;
		PixelsData pixels = image.getDefaultPixels();
		if  (pixels == null) return;
		int value;
		switch (index) {
			case RenderingControlLoader.LOAD:
			case RenderingControlLoader.RELOAD:
				value = index;
				break;
			default:
				value = index;
		}
		setStatus(model.fireRenderingControlLoading(pixels.getId(), value));
	}
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#loadRenderingControl()
	 */
	public void loadRenderingControl() {
	    ImageData image = model.getImage();
            if (image == null || image.getId() < 0) return;
            PixelsData pixels = image.getDefaultPixels();
	    model.loadRenderingControl(pixels.getId());
	}
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setLoadedFile(FileAnnotationData, File, Object)
	 */
	public void setLoadedFile(FileAnnotationData data, File file, Object uiView)
	{
		if (uiView instanceof DocComponent) {
			if (file == null) return;
			DocComponent doc = (DocComponent) uiView;
			if (doc.getData() == data) {
				doc.setThumbnail(file.getAbsolutePath());
				file.delete();
			}
		} else if (uiView instanceof OriginalMetadataComponent) {
			((OriginalMetadataComponent) uiView).setOriginalFile(file);
		}
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#getRenderer()
	 */
	public Renderer getRenderer()
	{
		return model.getRenderer();
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setInstrumentData(InstrumentData)
	 */
	public void setInstrumentData(InstrumentData data)
	{
		if (data == null) return;
		model.setInstrumentData(data);
		view.setInstrumentData();
		view.setStatus(false);
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#refresh()
	 */
	public void refresh()
	{
		/*
		switch (view.getSelectedTab()) {
			case EditorUI.GENERAL_INDEX:
				model.refresh();
				break;
			case EditorUI.RND_INDEX:
				Renderer rnd = getRenderer();
				if (rnd != null) rnd.refresh();
				break;
			case EditorUI.ACQUISITION_INDEX:
				view.refreshAcquisition();
		};
		*/
		model.refresh();
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#exportImageAsOMETIFF(File, Target)
	 */
	public void exportImageAsOMETIFF(File folder, Target target)
	{
		Object refObject = model.getRefObject();
		ImageData image = null;
		if (refObject instanceof ImageData)
			image = (ImageData) refObject;
		else if (refObject instanceof WellSampleData) {
			image = ((WellSampleData) refObject).getImage();
		}
		if (image == null) return;
		if (folder == null) folder = UIUtilities.getDefaultFolder();
		ExportActivityParam param = new ExportActivityParam(folder, 
				image, ExportActivityParam.EXPORT_AS_OME_TIFF, target);
		IconManager icons = IconManager.getInstance();
		param.setIcon(icons.getIcon(IconManager.EXPORT_22));
		UserNotifier un = MetadataViewerAgent.getRegistry().getUserNotifier();
		un.notifyActivity(model.getSecurityContext(), param);
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#onChannelColorChanged(int)
	 */
	public void onChannelColorChanged(int index)
	{
		view.onChannelColorChanged(index);
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#onChannelColorChanged(int)
	 */
	public void setSelectedTab(int index)
	{
		switch (index) {
			case RENDERER_TAB:
			case ACQUISITION_TAB:
			case GENERAL_TAB:
				view.setSelectedTab(index);
				break;
			default:
				return;
		}
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#createFigure()
	 */
	public void createFigure(int index)
	{
		if (controller.getFigureDialog() == null) {
			String name = model.getRefObjectName();
			UserNotifier un = 
				MetadataViewerAgent.getRegistry().getUserNotifier();
			FigureDialog dialog;
			PixelsData pixels = model.getPixels();
			boolean b = view.isNumerousChannel();
			switch (index) {
				case FigureDialog.SPLIT:
					if (pixels == null) {
						un.notifyInfo("Split View Figure", 
								"Image not valid. Cannot create figure.");
						return;
					}
					if (b) {
						un.notifyInfo("Split View Figure", 
								"The selected type of figure " +
						"is not available for the image.");
						return;
					}
					dialog = controller.createFigureDialog(name, pixels, 
							FigureDialog.SPLIT);
					if (!model.isRendererLoaded()) {
						loadRenderingControl(RenderingControlLoader.LOAD);
					} else {
						dialog.setRenderer(model.getRenderer());
					}
					dialog.centerDialog();
					break;
				case FigureDialog.SPLIT_ROI:
					if (b) {
						un.notifyInfo("ROI Split Figure", 
								"The selected type of figure " +
						"is not available for the image.");
						return;
					}
					model.fireROILoading(FigureDialog.SPLIT_ROI);
					break;
				case FigureDialog.THUMBNAILS:
					Object ref = model.getRefObject();
					if (ref instanceof WellSampleData || 
							ref instanceof PlateData) {
						un.notifyInfo("Thumbnails Figure", "Script not" +
								" available for Wells or Plate");
						return;
					}
						
					Collection tags = model.isMultiSelection() ? model.getAllTags() : model.getTags();
					dialog = controller.createFigureDialog(name, 
							pixels, FigureDialog.THUMBNAILS);
					dialog.setParentRef(model.getParentRootObject());
					if (tags != null) 
					    dialog.setTags(tags);
					else 
					    model.loadExistingTags();
					dialog.centerDialog();
					break;
				case FigureDialog.MOVIE:
					if (pixels == null) {
						un.notifyInfo("Movie Figure", 
								"Image not valid. Cannot create figure.");
						return;
					}
					Collection planes = model.getChannelPlaneInfo(
							EditorModel.DEFAULT_CHANNEL);
					dialog = controller.createFigureDialog(name, pixels, 
							FigureDialog.MOVIE);
					if (planes != null) dialog.setPlaneInfo(planes);
					else model.firePlaneInfoLoading(EditorModel.DEFAULT_CHANNEL, 
							0);
					dialog.centerDialog();
					break;
				case FigureDialog.ROI_MOVIE:
					model.fireROILoading(FigureDialog.ROI_MOVIE);
					break;
			}
		}
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setROI(Collection, long, int)
	 */
	public void setROI(Collection rois, long imageID, int index)
	{
		if (index != FigureDialog.SPLIT_ROI && index != FigureDialog.ROI_MOVIE)
			return;
		ImageData img = model.getImage();
		if (img == null || img.getId() != imageID) return;  
		UserNotifier un = 
			MetadataViewerAgent.getRegistry().getUserNotifier();
		if (rois == null || rois.size() == 0) {	
			un.notifyInfo("ROI Split Figure", 
			"The primary select does not have Region of Interests.");
			return;
		}
		Iterator r = rois.iterator();
		ROIResult result;
		int count = 0;
		try {
			Collection list;
			while (r.hasNext()) {
				result = (ROIResult) r.next();
				list = result.getROIs();
				if (list.size() > 0) count++;
			}
		} catch (Exception e) {}
		if (count == 0) {
			un.notifyInfo("ROI Split Figure", 
					"The primary select does not have Region of Interests.");
			return;
		}
		
		if (controller.getFigureDialog() == null) {
			PixelsData pixels = model.getPixels();
			if (pixels == null) {
				un.notifyInfo("ROI Split Figure", 
						"Image not valid. Cannot create figure.");
				return;
			}
			String name = model.getRefObjectName();
			FigureDialog dialog = controller.createFigureDialog(name, 
					pixels, index);
			if (dialog == null) return;
			dialog.setROIs(rois);
			if (!model.isRendererLoaded()) {
				loadRenderingControl(RenderingControlLoader.LOAD);
			} else {
				dialog.setRenderer(model.getRenderer());
				if (index == FigureDialog.ROI_MOVIE)
					model.firePlaneInfoLoading(EditorModel.DEFAULT_CHANNEL, 0);
			}
			dialog.centerDialog();
		}
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setScripts(List)
	 */
	public void setScripts(List scripts)
	{
		model.setScripts(scripts);
		view.setScripts();
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#getUserID()
	 */
	public long getUserID() { return model.getUserID(); }

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#loadScript(long)
	 */
	public void loadScript(long scriptID)
	{
		if (scriptID < 0) return;
		model.loadScript(scriptID);
		setStatus(true);
	}
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setScript(ScriptObject)
	 */
	public void setScript(ScriptObject script)
	{
		if (script == null) throw new IllegalArgumentException("No script.");
		model.setScript(script);
		setStatus(false);
		JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		if (dialog == null) {
			dialog = new ScriptingDialog(f, 
					model.getScript(script.getScriptID()), 
					model.getSelectedObjects(), 
					MetadataViewerAgent.isBinaryAvailable());
			dialog.addPropertyChangeListener(controller);
			UIUtilities.centerAndShow(dialog);
		} else {
			dialog.reset(model.getScript(script.getScriptID()), 
					model.getSelectedObjects());
			if (!dialog.isVisible())
				UIUtilities.centerAndShow(dialog);
		}
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setUserPhoto(BufferedImage, long)
	 */
	public void setUserPhoto(BufferedImage photo, long experimenterID)
	{
		if (photo == null) return;
		Object o = model.getRefObject();
		if (o instanceof ExperimenterData) {
			ExperimenterData exp = (ExperimenterData) o;
			if (exp.getId() == experimenterID) {
				model.setUserPhoto(photo, experimenterID);
				view.setUserPhoto(photo);
			}
		}
	}
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#canLink()
	 */
	public boolean canLink() { return model.canLink(); }
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#canEdit()
	 */
	public boolean canEdit() { return model.canEdit(); }
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#canAnnotate()
	 */
	public boolean canAnnotate() { return model.canAnnotate(); }

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#displayAnalysisResults(AnalysisResultsItem)
	 */
	public void displayAnalysisResults(AnalysisResultsItem analysis)
	{
		if (analysis == null) return;
		List<FileAnnotationData> list = analysis.getAttachments();
		if (list == null || list.size() == 0) return;
		Map<FileAnnotationData, File> results = analysis.getResults();
		if (results != null) {
			analysisResultsLoaded(analysis);
		} else {
			model.loadAnalysisResults(analysis);
			analysis.notifyLoading(true);
		}
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#analysisResultsLoaded(AnalysisResultsItem)
	 */
	public void analysisResultsLoaded(AnalysisResultsItem analysis)
	{
		if (analysis == null) return;
		analysis.notifyLoading(false);
		model.removeAnalysisResultsLoading(analysis);
		//now display results.
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#saveAs(File, int)
	 */
	public void saveAs(File folder, int format, String fileName)
	{
		if (folder == null) folder = UIUtilities.getDefaultFolder();
		model.saveAs(folder, format, fileName);
	}
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#onGroupSwitched(boolean)
	 */
	public void onGroupSwitched(boolean success)
	{
		if (!success) return;
		if (dialog != null) {
			dialog.setVisible(false);
			dialog.dispose();
			dialog = null;
		}
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#getSecurityContext()
	 */
    public SecurityContext getSecurityContext()
    { 
    	return model.getSecurityContext();
    }

    /** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setLargeImage(Boolean)
	 */
	public void setLargeImage(Boolean value)
	{
	    ImageData img = model.getImage();
	    if (img == null) return;
		model.setLargeImage(value);
		view.onSizeLoaded();
		view.handleImageSelection();
		loadRnd();
	}
	
    /** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#onUpdatedChannels(List)
	 */
	public void onUpdatedChannels(List<ChannelData> channels)
	{
		model.updateChannels(channels);
		view.showChannelData();
	}
	
    /** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setFileset(Set)
	 */
	public void setFileset(Set<FilesetData> set)
	{
		model.setFileset(set);
		view.displayFileset();
	}

    /**
     * Implemented as specified by the {@link Editor} interface.
     * 
     * @see Editor#loadFileset(int)
     */
    public void loadFileset() {
        model.fireFilesetLoading();
    }

    /** 
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#loadRnd()
     */
	public void loadRnd()
	{
	    if (model.getRndIndex() == MetadataViewer.RND_SPECIFIC) {
	        if (!model.isRendererLoaded()) {
	            loadRenderingControl(RenderingControlLoader.LOAD);
	        }
	    } else {
	        if (view.getSelectedTab() == EditorUI.RND_INDEX)
	            loadRenderingControl(RenderingControlLoader.LOAD);
	    }
	}

    /** 
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#setLDAPDetails(long, String)
     */
    public void setLDAPDetails(long userID, String result) {
        Object o = model.getRefObject();
        if (o instanceof ExperimenterData) {
            ExperimenterData exp = (ExperimenterData) o;
            if (exp.getId() == userID) {
                view.setLDAPDetails(result);
            }
        }
    }

    /** 
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#getScriptFromName(String)
     */
    public ScriptObject getScriptFromName(String name)
    {
        return model.getScriptFromName(name);
    }
    
    /** 
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#getSelectedFileAnnotations()
     */
    public Collection<FileAnnotationData> getSelectedFileAnnotations() {
        return view.getSelectedFileAnnotations();
    }
}
