/*
 * org.openmicroscopy.shoola.agents.metadata.editor.EditorComponent 
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
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.ImageData;

/** 
 * 
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

	/**
	 * Creates a new instance.
	 * The {@link #initialize() initialize} method should be called straigh 
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
		model.getObservable() .addPropertyChangeListener(controller);
	}
	
	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#getUI()
	 */
	public JComponent getUI() { return view; }

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setStructuredDataResults(StructuredDataResults)
	 */
	public void setStructuredDataResults(StructuredDataResults result)
	{
		if (result == null) return;
		model.setStructuredDataResults(result);
		view.layoutUI();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setRootObject(Object)
	 */
	public void setRootObject(Object refObject)
	{
		if (refObject == null)
			throw new IllegalArgumentException("Root object not valid.");
		model.setRootObject(refObject);
		view.setRootObject();
		model.loadUserThumbnail();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#loadThumbnails()
	 */
	public void loadThumbnails()
	{
		if (!model.isThumbnailsLoaded())
			model.loadThumbnails();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setThumbnails(Map, long)
	 */
	public void setThumbnails(Map<Long, BufferedImage> thumbnails, 
							long imageID)
	{
		Object ref = model.getRefObject();
		if (ref instanceof ImageData) {
			if (((ImageData) ref).getId() == imageID) {
				model.setThumbnails(thumbnails);
				view.setThumbnails();
			}
		}
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setExistingTags(Collection)
	 */
	public void setExistingTags(Collection tags)
	{
		model.setExistingTags(tags);
		view.setExistingTags();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setChannelsData(List)
	 */
	public void setChannelsData(List channelData)
	{
		model.setChannelData(channelData);
		view.showChannelData();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setThumbnail(BufferedImage, long)
	 */
	public void setThumbnail(BufferedImage thumbnail, long imageID)
	{
		Object ref = model.getRefObject();
		if (!(ref instanceof ImageData)) return;
		ImageData img = (ImageData) ref;
		if (img.getId() == imageID && thumbnail != null)
			view.setThumbnail(thumbnail);
			//model.setThumbnail(thumbnail);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#hasDataToSave()
	 */
	public boolean hasDataToSave()
	{
		return view.hasDataToSave();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#addComponent(JComponent, int)
	 */
	public void addComponent(JComponent c, int location)
	{
		switch (location) {
			case MetadataViewer.TOP_LEFT:
				view.addTopLeftComponent(c);
				break;
	
			default:
				break;
		}
	}
	
	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setDownloadedFiles(Collection)
	 */
	public void setDownloadedFiles(Collection files)
	{
		if (files == null || files.size() == 0) return;
		UserNotifier un = MetadataViewerAgent.getRegistry().getUserNotifier();
		un.notifyDownload(files);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setDiskSpace(List)
	 */
	public void setDiskSpace(List list)
	{
		if (list == null || list.size() != 2) return;
		model.setDiskSpace(list);
		view.layoutUI();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
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
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#showImageInfo()
	 */
	public void showImageInfo()
	{
		if (model.getChannelData() == null) model.loadChannelData();
		else view.showChannelData();
	}
	
}
