/*
 * org.openmicroscopy.shoola.agents.metadata.editor.Editor 
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



import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

import org.openmicroscopy.shoola.agents.metadata.FileAnnotationCheckResult;
import org.openmicroscopy.shoola.agents.metadata.util.AnalysisResultsItem;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.model.DiskQuota;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.data.util.Target;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

import pojos.ChannelAcquisitionData;
import pojos.ChannelData;
import pojos.FileAnnotationData;
import pojos.FilesetData;
import pojos.ImageAcquisitionData;
import pojos.InstrumentData;

/** 
 * Defines the interface provided by the viewer component. 
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
public interface Editor 
	extends ObservableComponent
{

	/** Indicates to select the renderer tab. */
	public static final int		RENDERER_TAB = EditorUI.RND_INDEX;
	
	/** Indicates to select the general tab. */
	public static final int		GENERAL_TAB = EditorUI.GENERAL_INDEX;
	
	/** Indicates to select the acquisition tab. */
	public static final int		ACQUISITION_TAB = EditorUI.ACQUISITION_INDEX;
	
	/** Identified the <code>Format</code> enumeration. */
	public static final String FORMAT = OmeroMetadataService.FORMAT;
	
	/** Identified the <code>Immersion</code> enumeration. */
	public static final String IMMERSION = OmeroMetadataService.IMMERSION;
	
	/** Identified the <code>Correction</code> enumeration. */
	public static final String CORRECTION = OmeroMetadataService.CORRECTION;
	
	/** Identified the <code>Medium</code> enumeration. */
	public static final String MEDIUM = OmeroMetadataService.MEDIUM;
	
	/** Identified the <code>Microscope type</code> enumeration. */
	public static final String MICROSCOPE_TYPE = 
					OmeroMetadataService.MICROSCOPE_TYPE;
	
	/** Identified the <code>Detector type</code> enumeration. */
	public static final String DETECTOR_TYPE = 
					OmeroMetadataService.DETECTOR_TYPE;
	
	/** Identified the <code>Detector type</code> enumeration. */
	public static final String FILTER_TYPE = 
					OmeroMetadataService.FILTER_TYPE;
	
	/** Identified the <code>Laser medium</code> enumeration. */
	public static final String LASER_MEDIUM = 
					OmeroMetadataService.LASER_MEDIUM;
	
	/** Identified the <code>Laser medium</code> enumeration. */
	public static final String LASER_PULSE = 
					OmeroMetadataService.LASER_PULSE;
	
	/** Identified the <code>Binning</code> enumeration. */
	public static final String BINNING = OmeroMetadataService.BINNING;
	
	/** Identified the <code>contrast method</code> enumeration. */
	public static final String CONTRAST_METHOD = 
				OmeroMetadataService.CONTRAST_METHOD;
	
	/** Identified the <code>illumination type</code> enumeration. */
	public static final String ILLUMINATION_TYPE = 
				OmeroMetadataService.ILLUMINATION_TYPE;
	
	/** Identified the <code>photometric Interpretation</code> enumeration. */
	public static final String PHOTOMETRIC_INTERPRETATION = 
		OmeroMetadataService.PHOTOMETRIC_INTERPRETATION;
	
	/** Identified the <code>arc type</code> enumeration. */
	public static final String ARC_TYPE = OmeroMetadataService.ARC_TYPE;
	
	/** Identified the <code>filament type</code> enumeration. */
	public static final String FILAMENT_TYPE = 
		OmeroMetadataService.FILAMENT_TYPE;
	
	/** Identified the <code>laser type</code> enumeration. */
	public static final String LASER_TYPE = 
		OmeroMetadataService.LASER_TYPE;
	
	/** Identified the <code>mode</code> enumeration. */
	public static final String MODE = OmeroMetadataService.ACQUISITION_MODE;

	/** Feeds the metadata back to the editor. */
	public void setStructuredDataResults();
	
	/**
	 * Returns the View.
	 * 
	 * @return See above.
	 */
	public JComponent getUI();
	
	/**
	 * Sets the root of the tree.
	 * 
	 * @param refObject The object hosted by the root node of the tree.
	 * 					Mustn't be <code>null</code>.
	 */
	public void setRootObject(Object refObject);
	

	/**
	 * Sets the collection of existing tags.
	 * 
	 * @param collection The tags to set.
	 */
	public void setExistingTags(Collection collection);

	/**
	 * Sets the collection of channel data.
	 * 
	 * @param channels 		The collection to set.
	 * @param updateView 	Pass <code>true</code> to update the view,
	 * 						<code>false</code> otherwise.
	 */
	public void setChannelsData(Map channels, boolean updateView);

	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasDataToSave();
	
	/**
	 * Sets the used and free disk space.
	 * 
	 * @param quota The value to set.
	 */
	public void setDiskSpace(DiskQuota quota);

	/**
	 * Indicates that the password was successfully modified or not.
	 * 
	 * @param changed 	Pass <code>true</code> if the password was successfully
	 * 					changed, <code>false</code> otherwise.
	 */
	public void passwordChanged(boolean changed);

	/** Brings up on screen the image's information. */
	public void loadChannelData();

	/**
	 * Sets the collection of existing attachments, attachments added
	 * by the currently logged in user.
	 * 
	 * @param attachments The value to set.
	 */
	public void setExistingAttachments(Collection attachments);
	
	/**
	 * Sets either to single selection or to multiple selection.
	 * 
	 * @param single	Pass <code>true</code> when single selection, 
	 * 					<code>false</code> otherwise.
	 */
	public void setSelectionMode(boolean single);

	/** Loads the container hosting the currently edited object. */
	public void loadParents();

    /**
     * Sets to <code>true</code> if loading data, to <code>false</code>
     * otherwise.
     * 
     * @param busy 	Pass <code>true</code> while loading data, 
     * 				<code>false</code> otherwise.
     */
	public void setStatus(boolean busy);

	/** Loads the existing tags. */
	public void loadExistingTags();

	public void removeFileAnnotations(List<FileAnnotationData> annotations);
	
	public void handleFileAnnotationRemoveCheck(FileAnnotationCheckResult result);
	
	/**
	 * Sets the image acquisition data.
	 * 
	 * @param data The value to set.
	 */
	public void setImageAcquisitionData(ImageAcquisitionData data);

	/** Loads the image acquisition data. */
	public void loadImageAcquisitionData();

	/**
	 * Sets the enumerations for the metadata related to an image.
	 * 
	 * @param map The value to set.
	 */
	public void setImageEnumerations(Map map);

	/**
	 * Sets the enumerations for the metadata related to a channel.
	 * 
	 * @param map The value to set.
	 */
	public void setChannelEnumerations(Map map);

	/**
	 * Sets the acquisition data for the specified channel.
	 * 
	 * @param index The index of the channel.
	 * @param data	The acquisition data.
	 */
	public void setChannelAcquisitionData(int index, 
			ChannelAcquisitionData data);

	/**
	 * Loads the channel acquisition for the specified channel.
	 * 
	 * @param channel The channel to handle.
	 */
	public void loadChannelAcquisitionData(ChannelData channel);

	/** Loads the existing attachments. */
	public void loadExistingAttachments();

	/** 
	 * Downloads the archived files 
	 * 
	 * @param file The file where to download the content.
	 * If it is a multi-images file a zip will be created.
	 * @param override Flag indicating to override the existing file if it
	 * exists, <code>false</code> otherwise.
	 */
	public void download(File file, boolean override);

	/**
	 * Sets the parent of the root object. This will be taken into account
	 * only if the root is a well sample.
	 * 
	 * @param parentRefObject The parent of the root object.
	 * @param grandParent     The grand parent of the root object.
	 */
	public void setParentRootObject(Object parentRefObject, Object grandParent);

	/**
	 * Sets the plane info corresponding to the channel and the pixels set.
	 * 
	 * @param result	The collection of plane.
	 * @param pixelsID	The id of the pixels set.
	 * @param channel	The selected channel.
	 */
	public void setPlaneInfo(Collection result, long pixelsID, int channel);

	/**
	 * Sets the rendering control
	 * 
	 * @param rndControl The value to set.
	 */
	public void setRenderingControl(RenderingControl rndControl);

	/** 
	 * Loads the rendering control for the first selected image. 
	 * 
	 * @param index One of the loading index.
	 */
	public void loadRenderingControl(int index);

	/** 
	 * Reloads the rendering control for the first selected image. 
	 * (Note: This is a blocking method, for asynchronous call use 
         *   {@link loadRenderingControl(int)} instead
	 */
	public void loadRenderingControl();
	
	/**
	 * Sets the result back to the viewer.
	 * 
	 * @param data The annotation hosted the file to load.
	 * @param file The local copy.
	 * @param uiView The object handle the result.
	 */
	public void setLoadedFile(FileAnnotationData data, File file, 
			Object uiView);

	/**
	 * Returns the renderer. This method will always return 
	 * <code>null</code> if the type is not {@link MetadataViewer#RND_SPECIFIC}.
	 * 
	 * @return See above.
	 */
	public Renderer getRenderer();
	
	/** Loads the instrument related to the image. */
	public void loadInstrumentData();

	/**
	 * Sets the instrument used to capture the image.
	 * 
	 * @param data The value to set.
	 */
	public void setInstrumentData(InstrumentData data);

	/** Refreshes the currently selected tab. */
	public void refresh();

	/**
	 * Exports the image.
	 * 
	 * @param folder The folder where to export the image.
	 * @param target The selected schema or <code>null</code>.
	 */
	public void exportImageAsOMETIFF(File folder, Target target);

	/**
	 * Indicates that the color of the passed channel has changed.
	 * 
	 * @param index The index of the channel.
	 */
	public void onChannelColorChanged(int index);
	
	/** 
	 * Set the index of the selected tab.
	 * 
	 * @param index The selected index.
	 */
	public void setSelectedTab(int index);

	/**
	 * Loads the rendering control if not already loaded for the primary select.
	 * 
	 * @param index The index of the figure to create.
	 */
	public void createFigure(int index);
	
	/**
	 * Sets the ROI associated to the specified image. 
	 * 
	 * @param roi 		The collection of ROI.
	 * @param imageID 	The id of the image.
	 * @param index   	The index of the figure to create.
	 */
	public void setROI(Collection roi, long imageID, int index);
	
	/** 
	 * Returns the channel data.
	 * 
	 * @return See above.
	 */
	public Map getChannelData();

	/** 
	 * Sets the scripts uploaded by the user.
	 * 
	 * @param scripts The scripts to display.
	 */
	public void setScripts(List scripts);

	/**
	 * Returns the id of the user.
	 * 
	 * @return See above.
	 */
	public long getUserID();

	/**
	 * Sets the loaded script.
	 * 
	 * @param script The script to set.
	 */
	public void setScript(ScriptObject script);
	
	/**
	 * Loads the specified script.
	 * 
	 * @param scriptID The id of the script to load.
	 */
	void loadScript(long scriptID);

	/**
	 * Sets the photo of the user.
	 * 
	 * @param photo The user's photo.
	 * @param experimenterID The id of the experimenter the photo is for.
	 */
	void setUserPhoto(BufferedImage photo, long experimenterID);
	
	/**
	 * Returns <code>true</code> if the object can be edited,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean canEdit();
	
	/**
	 * Returns <code>true</code> if the object can be edited,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean canAnnotate();
	
	/**
	 * Returns <code>true</code> if the object can be hard linked,
	 * i.e. image added to dataset, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean canLink();
	
	/**
	 * Displays the results of analysis.
	 * 
	 * @param analysis Object hosting information about the results.
	 */
	void displayAnalysisResults(AnalysisResultsItem analysis);
	
	/**
	 * Notifies that the results have been loaded.
	 * 
	 * @param analysis Object hosting information about the results.
	 */
	void analysisResultsLoaded(AnalysisResultsItem analysis);

	/**
	 * Saves the selected images as <code>JPEG</code>, <code>PNG</code> or
	 * <code>TIFF</code>.
	 * 
	 * @param folder
	 *            The folder to save.
	 * @param format
	 *            The format to use.
	 * @param filename
	 *            The filename to use for the batch export file (without
	 *            extension)
	 * @see org.openmicroscopy.shoola.env.data.model.FigureParam
	 */
	public void saveAs(File folder, int format, String filename);

	/** 
	 * Invokes when the user has switched group.
	 * 
	 * @param success Pass <code>true</code> if success, <code>false</code>
	 * otherwise.
	 */
	void onGroupSwitched(boolean success);
	
	/**
     * Returns the security context.
     * 
     * @return See above.
     */
    SecurityContext getSecurityContext();

    /**
     * Indicates if the image is a large image or not.
     * 
     * @param result The value to set.
     */
	void setLargeImage(Boolean result);
	
	/**
	 * Invokes when the channels have been modified. Updates the values
	 * displayed in the measurement tool.
	 * 
	 * @param channels The channels to handle.
	 */
	void onUpdatedChannels(List<ChannelData> channels);

	/**
	 * Sets the file set associated to the image.
	 * 
	 * @param result The value to set.
	 */
	void setFileset(Set<FilesetData> result);

	/** 
	 * Loads the file set associated to the image.
	 * */
	void loadFileset();

	/**
	 * Loads the rendering engine depending on the selected pane or component
	 * usage.
	 */
	void loadRnd();

	/**
	 * Sets the LDAP details of the specified user.
	 *
	 * @param userID The user's id.
	 * @param result The value to set.
	 */
    void setLDAPDetails(long userID, String result);

    ScriptObject getScriptFromName(String name);
    
    /**
     * Returns the selected FileAnnotations or an empty Collection
     * if there are no FileAnnotations
     * 
     * @return See above
     */
    public Collection<FileAnnotationData> getSelectedFileAnnotations();
    
}
