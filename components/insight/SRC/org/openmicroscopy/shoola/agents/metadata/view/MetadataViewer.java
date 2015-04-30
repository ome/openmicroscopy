/*
 * org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.view;


//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserDisplay;
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.agents.metadata.util.DataToSave;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import pojos.AnnotationData;
import pojos.ChannelData;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
 * Defines the interface provided by the viewer component. 
 * The Viewer provides a component hosting a browser for metadata.
 *
 * When the user quits the window, the {@link #discard() discard} method is
 * invoked and the object transitions to the {@link #DISCARDED} state.
 * At which point, all clients should de-reference the component to allow for
 * garbage collection.
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
public interface MetadataViewer
	extends ObservableComponent
{

	/** Indicates to run the script. */
	public static final int		RUN = 100;
	
	/** Indicates to download the script. */
	public static final int		DOWNLOAD = 101;
	
	/** Indicates to view the script. */
	public static final int		VIEW = 102;
	
	/** Indicates to select the renderer tab. */
	public static final int		RENDERER_TAB = Editor.RENDERER_TAB;
	
	/** Indicates to select the general tab. */
	public static final int		GENERAL_TAB =  Editor.GENERAL_TAB;
	
	/** Indicates to select the general tab. */
	public static final int		ACQUISITION_TAB =  Editor.ACQUISITION_TAB;
	
	/** Indicates that the renderer is for general purpose. */
	public static final int 	RND_GENERAL = 0;
	
	/** Indicates that the renderer is for specific purpose. */
	public static final int 	RND_SPECIFIC = 1;
	
	/** Bound property indicating that the rendering control is loaded. */
	public static final String  RND_LOADED_PROPERTY = "rndLoaded";
	
	/** Bound property indicating that the data have been saved. */
	public static final String	ON_DATA_SAVE_PROPERTY = "onDataSave";
	
	/** Bound property indicating to save the data. */
	public static final String	SAVE_DATA_PROPERTY = "saveData";
	
	/** Bound property indicating to clear the data to save. */
	public static final String	CLEAR_SAVE_DATA_PROPERTY = "clearSaveData";
	
	/** 
	 * Bound property indicating that the experimenter or group
	 * has been updated. 
	 */
	public static final String	ADMIN_UPDATED_PROPERTY = "adminUpdated";
	
	/**
	 * Bound property indicating that parents of the currently edited objects
	 * are loaded.
	 */
	public static final String	LOADING_PARENTS_PROPERTY = "loadingParents";

	/** Bound property indicating to create a movie. */
	public static final String	CREATING_MOVIE_PROPERTY = "creatingMovie";
	
	/** Bound property indicating to analyze. */
	public static final String	ANALYSE_PROPERTY = "analyse";
	
	/** Bound property indicating to render a plane. */
	public static final String	RENDER_PLANE_PROPERTY = "renderPlane";
	
	/** Bound property name indicating that a new channel is selected. */
    public final static String  SELECTED_CHANNEL_PROPERTY = "selectedChannel";
    
	/** 
	 * Bound property indicating to apply settings to all the
	 * displayed or selected images. 
	 */
	public static final String	APPLY_SETTINGS_PROPERTY = "applySettings";
	
	/** Bound property indicating that the settings have been applied. */
	public static final String	SETTINGS_APPLIED_PROPERTY = "settingsApplied";
	
	/** Bound property indicating to bring up the activity options. */
	public static final String	ACTIVITY_OPTIONS_PROPERTY = "activityOptions";
	
	/** Bound property indicating to create a figure. */
	public static final String	GENERATE_FIGURE_PROPERTY = "generateFigure";
	
	/** Bound property indicating to close the renderer. */
	public static final String	CLOSE_RENDERER_PROPERTY = "closeRenderer";
	
	/** 
	 * Bound property indicating that the color of a channel has been modified. 
	 */
	public static final String	CHANNEL_COLOR_CHANGED_PROPERTY = 
		"channelColorChanged";

	/** Bound property indicating to handle a script. */
	public static final String	HANDLE_SCRIPT_PROPERTY = "handleScript";
	
	/** Bound property indicating to upload a script. */
	public static final String	UPLOAD_SCRIPT_PROPERTY = "uploadScript";
	
	/** Bound property indicating to register a file. */
	public static final String	REGISTER_PROPERTY = "register";
	
	/** Bound property indicating to reset password. */
	public static final String	RESET_PASSWORD_PROPERTY = "resetPassword";
	
	/**
	 * Bound property indicating the related nodes have been set.
	 */
	public static final String	RELATED_NODES_PROPERTY = "relatedNodes";
	
	/** Flag to denote the <i>New</i> state. */
	public static final int     NEW = 1;

	/** Flag to denote the <i>Loading Metadata</i> state. */
	public static final int     LOADING_METADATA = 2;

	/** Flag to denote the <i>Ready</i> state. */
	public static final int     READY = 3;

	/** Flag to denote the <i>Discarded</i> state. */
	public static final int     DISCARDED = 4;
	
	/** Flag to denote the <i>Batch Saving</i> state. */
	public static final int 	BATCH_SAVING = 5;
	
	/** Flag to denote the <i>Batch Saving</i> state. */
	public static final int 	SAVING = 6;
	
	/** Indicates to launch the publishing option. */
	public static final int		PUBLISHING_OPTION = 100;
	
	/** Indicates to launch the analysis option. */
	public static final int		ANALYSIS_OPTION = 101;
	
	/** Indicates to launch the scripts option. */
	public static final int		SCRIPTS_OPTION = 102;
	
	/** Indicates to launch the analysis option. */
	public static final int		SAVE_OPTION = 103;
	
	/**
	 * Starts the data loading process when the current state is {@link #NEW} 
	 * and puts the window on screen.
	 * If the state is not {@link #NEW}, then this method simply moves the
	 * window to front.
	 * 
	 * @param channelData 	The channel data if already loaded, 
	 * 						<code>null</code> otherwise.
	 * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
	 */
	public void activate(Map channelData);

        /**
	 * Applies the specified rendering settings.
	 * 
	 * @param rndDef The rendering settings to apply.
	 */
	public void applyRenderingSettings(RndProxyDef rndDef);
	
	/**
	 * Transitions the viewer to the {@link #DISCARDED} state.
	 * Any ongoing data loading is canceled.
	 */
	public void discard();

	/**
	 * Queries the current state.
	 * 
	 * @return One of the state flags defined by this interface.
	 */
	public int getState();

	/** 
	 * Cancels any ongoing data loading. 
	 *
	 * @param loaderID The identifier to the loader to cancel.
	 */
	public void cancel(int loaderID);
	
	/**
	 * Feeds the metadata back to the viewer.
	 * 
	 * @param results The result to feed back.
	 * @param loader The identifier of the loader.
	 */
	public void setMetadata(Map<DataObject, StructuredDataResults> results,
			int loader);
	
	/**
	 * Returns the UI used to select the metadata.
	 * 
	 * @return See above.
	 */
	public JComponent getSelectionUI();
	
	/**
	 * Returns the UI used to select the metadata.
	 * 
	 * @return See above.
	 */
	public JComponent getEditorUI();
	
	/**
	 * Checks if the renderer has already been initialized
	 * @return See above.
	 */
	public boolean isRendererLoaded();
	
	/**
	 * Returns the component hosted by the view.
	 * 
	 * @return See above.
	 */
	public JComponent getUI();
	
	/**
	 * Sets the root of the metadata browser.
	 * 
	 * @param root The object to set.
	 * @param userID The id of the user.
	 * @param ctx The security context.
	 */
	public void setRootObject(Object root, long userID, SecurityContext ctx);

	/**
	 * Loads the parent containers of the object hosted by the passed node.
	 * 
	 * @param node The node to handle.
	 */
	public void loadContainers(TreeBrowserDisplay node);
	
	/**
	 * Sets the containers.
	 * 
	 * @param node		The node to handle.
	 * @param result	The value to set.
	 * @see #loadContainers(TreeBrowserDisplay)
	 */
	public void setContainers(TreeBrowserDisplay node, Object result);
	
	/**
	 * Saves the annotations back to the server.
	 * 
	 * @param object The annotation/link to add or remove.
	 * @param toDelete	The annotations to delete.
	 * @param metadata	The metadata to save.
	 * @param data		The data object to annotate.
	 * @param asynch 	Pass <code>true</code> to save data asynchronously,
     * 				 	<code>false</code> otherwise.
	 */
	public void saveData(DataToSave object, List<AnnotationData> toDelete,
			List<Object> metadata, DataObject data, boolean asynch);
	
	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasDataToSave();

	/** Saves the data if any to save. */
	public void saveData();
	
	/** Clears the data to save. */
	public void clearDataToSave();

	/**
	 * Refreshes the view when the metadata has been saved.
	 * 
	 * @param dataObject The updated object.
	 */
	public void onDataSave(List<DataObject> dataObject);
	
	/**
	 * Sets to <code>true</code> if single selection, 
	 * to <code>false</code> if multiple nodes are selected.
	 * 
	 * @param single	Pass <code>true</code> when single selection, 
	 * 				 	<code>false</code> otherwise.
	 */
	public void setSelectionMode(boolean single);
	
	/** 
	 * Returns <code>true</code> if the model is in single mode,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isSingleMode();
	
	/**
	 * Returns the collection of related nodes.
	 * 
	 * @return See above.
	 */
	public List getRelatedNodes();
	
	/**
	 * Sets the nodes related to the root node when multiple nodes are selected.
	 * 
	 * @param nodes The nodes to set.
	 */
	public void setRelatedNodes(List nodes);

	/**
	 * Updates the view when the experimenter or group 
	 * details have been modified.
	 * 
	 * @param data The data to update.
	 */
	public void onAdminUpdated(Object data);
	
	/**
	 * Loads the containers hosting the currently edited object.
	 */
	public void loadParents();
	
	/**
	 * Returns the metadata linked to the currently edited object
	 * or <code>null</code> if not loaded.
	 * 
	 * @return See above.
	 */
	public StructuredDataResults getStructuredData();
	
	/**
	 * Returns the metadata linked to the currently edited object
	 * or <code>null</code> if not loaded.
	 * 
	 * @return See above.
	 */
	public StructuredDataResults getParentStructuredData();
	
    /**
     * Sets to <code>true</code> if loading data, to <code>false</code>
     * otherwise.
     * 
     * @param busy 	Pass <code>true</code> while loading data, 
     * 				<code>false</code> otherwise.
     */
	public void setStatus(boolean busy);

	/** Displays the tag wizard. */
	public void showTagWizard();
	
	/** 
	 * Returns the object path i.e. if a dataset is selected,
	 * the name of the project_name of the dataset.
	 * 
	 * @return See above.
	 */
	public String getObjectPath();

	/**
	 * Sets the parent of the root object. This will be taken into account
	 * only if the root is a well sample.
	 * 
	 * @param parent The parent of the root object.
	 * @param grandParent The grandParent of the root object.
	 */
	public void setParentRootObject(Object parent, Object grandParent);

	/**
	 * Brings up the dialog to create a movie.
	 * 
	 * @param scaleBar 	   The value of the scale bar. 
	 * 					   If not greater than <code>0</code>, the value is not 
	 * 					   taken into account.
	 * @param overlayColor The color of the scale bar and text. 
	 */
	public void makeMovie(int scaleBar, Color overlayColor);

	/**
	 * Returns one of the rnd constants defined by this class.
	 * 
	 * @return See above.
	 */
	public int getRndIndex();

	/** 
	 * Indicates to render a plane for the primary select object.
	 * This method should only be invoked if the object is an image.
	 */
	public void renderPlane();
	
	/** 
	 * Applies the rendering settings to the selected or displayed images. 
	 * The settings of the primary select will be taken into account.
	 */
	void applyToAll();
	
	/** Notifies that the settings have been applied. */
	void onSettingsApplied();
	
	/** Notifies to set the settings. */
	void saveSettings();
	
	/**
	 * Returns the renderer. This method will always return 
	 * <code>null</code> if the type is not {@link #RND_SPECIFIC}.
	 * 
	 * @return See above.
	 */
	public Renderer getRenderer();
	
	/** 
	 * Notifies that the rendering control has been loaded. 
	 * 
	 * @param reload Pass <code>true</code> if the rendering control has been
	 * 				 reloaded following an exception, <code>false</code> if 
	 * 				 it is an initial load.
	 */
	public void onRndLoaded(boolean reload);
	
	/**
	 * Notifies when a channel is selected or deselected.
	 * 
	 * @param index The index of the channel.
	 */
	public void onChannelSelected(int index);
	
	/**
	 * Returns the ideal size of the renderer.
	 * 
	 * @return See above.
	 */
	Dimension getIdealRendererSize();
	
	/**
	 * Analyzes the image.
	 * 
	 *  @param index The index identifying the type of analysis routine.
	 */
	void analyse(int index);

	/**
	 * Notifies that the rendering settings have been copied.
	 * Updates the UI if the renderer corresponds to one of the passed image.
	 * 
	 * @param imageIds The collection of images.
	 */
	void onRndSettingsCopied(Collection<Long> imageIds);
	
	/**
	 * Returns <code>true</code> if it is an image with a lot of channels.
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isNumerousChannel();
	
	/** 
	 * Set the index of the selected tab.
	 * 
	 * @param index The selected index.
	 */
	public void setSelectedTab(int index);

	/**
	 * Brings up the activity options.
	 * 
	 * @param source   The source of the mouse pressed.
	 * @param location The location of the mouse pressed.
	 * @param index
	 */
	public void activityOptions(Component source, Point location, int index);

	/**
	 * Creates a figure.
	 * 
	 * @param value The parameters for the figure.
	 */
	public void createFigure(Object value);
	
	/**
	 * Runs the passed script.
	 * 
	 * @param script The script to run.
	 * @param index  Indicated to run, download or view.
	 */
	public void manageScript(ScriptObject script, int index);
	
	/**
	 * Reloads the renderer (asynchronous) if the passed value is
         * <code>true</code>, discards the components if <code>false</code>.
	 * 
	 * @param value Pass <code>true</code> to reload, 
	 * 				<code>false</code> to discard.
	 */
	public void reloadRenderingControl(boolean value);
	
	public void resetRenderingControl();
	 
	/**
	 * Reloads the renderer (synchronous)
         */
	void reloadRenderingControl();
	
	/** 
	 * Indicates that the color of the passed channel has changed.
	 * 
	 * @param index The index of the channel.
	 */
	void onChannelColorChanged(int index);
	
	/**
	 * Returns the object of reference.
	 * 
	 * @return See above.
	 */
	Object getRefObject();

	/**
	 * Updates the experimenter or the group.
	 * 
	 * @param data 		The object to handle.
	 * @param asynch 	Pass <code>true</code> to save data asynchronously,
     * 					 <code>false</code> otherwise.
	 */
	public void updateAdminObject(Object data, boolean asynch);

	/**
	 * Returns the id of the possible owner. This should only be used to 
	 * handle unregistered objects.
	 * 
	 * @return See above.
	 */
	long getUserID();

	/**
	 * Resets the password of the edited experimenter.
	 * 
	 * @param newPass The new password.
	 */
	void resetPassword(String newPass);
	
	/**
	 * Loads the settings associated to a given image.
	 * 
	 */
	void loadViewedBy();
	
	/**
	 * Sets the settings linked to a given image.
         *
	 * @param The map containing the rendering definitions
	 */
	void setViewedBy(Map result);
	
	/**
	 * Sets the thumbnails of the image currently selected.
	 * 
	 * @param thumbnails 	The thumbnails to set, one per user.
	 * @param imageID		The id of the image the thumbnails are for.
	 */
	public void setThumbnails(Map<Long, BufferedImage> thumbnails, 
							long imageID);

	 /** Notifies to upload the script. */
	void uploadScript();

	/** 
	 * Invokes when the user has switched group.
	 * 
	 * @param success Pass <code>true</code> if success, <code>false</code>
	 * otherwise.
	 */
	void onGroupSwitched(boolean success);

	/**
	 * Returns the parent UI.
	 * 
	 * @return See above.
	 */
	JFrame getParentUI();
	
	/** Refreshes the view. */
	void refresh();
	
	/**
	 * Returns the security context.
	 * 
	 * @return See above.
	 */
	SecurityContext getSecurityContext();

	/**
	 * Returns <code>true</code> if the passed object is the reference object,
	 * <code>false</code> otherwise.
	 * 
	 * @param object The object to compare.
	 * @return See above.
	 */
	boolean isSameObject(Object object);

	/**
	 *  Returns all the metadata objects corresponding to the selected objects.
	 * 
	 * @return See above.
	 */
	Map<DataObject, StructuredDataResults> getAllStructuredData();
	
	/**
	 * Returns the metadata linked to the currently edited object
	 * or <code>null</code> if not loaded.
	 * 
	 * @param refObject The object to handle.
	 * @return See above.
	 */
	StructuredDataResults getStructuredData(Object refObject);

	/**
	 * Invokes when the channels have been modified. Updates the values
	 * displayed in the measurement tool.
	 * 
	 * @param channels The channels to handle.
	 */
	void onUpdatedChannels(List<ChannelData> channels);

	/**
	 * Returns the user currently logged in.
	 * 
	 * @return See above.
	 */
	ExperimenterData getCurrentUser();
	
	/**
         * Applies the settings of a previous set image to
         * the renderer (does not save them).
         * See also {@link #setRndSettingsToCopy(ImageData)}
         */
	void applyCopiedRndSettings();
	
        /**
         * Returns if there are copied rendering settings which could be pasted.
         * 
         * @return
         */
        boolean hasRndSettingsCopied();
}
