/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.openmicroscopy.shoola.agents.dataBrowser.RateFilter;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.CellDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.data.util.FilterContext;

import omero.gateway.SecurityContext;
import omero.gateway.model.TableResult;

import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

import pojos.DataObject;
import pojos.ExperimenterData;


/** 
 * Defines the interface provided by the hierarchy viewer component.
 * The hierarchy viewer provides a top-level window to host a hierarchy display
 * and let the user interact with it.  A hierarchy display is a screen with
 * one or more visualization trees, all of the same kind.  A visualization tree
 * is a graphical tree that represents objects in a given <i>OME</i> hierarchy,
 * like Project/Dataset/Image  Two such trees
 * are said to be of the same kind if they represent objects which belong in 
 * the same logical hierarchy.
 * <p>The typical life-cycle of a hierarchy viewer is as follows. The object
 * is first created using the {@link DataBrowserFactory} and specifying what
 * kind of hierarchy the viewer is for along with the root nodes to load. After
 * creation the object is in the {@link #NEW} state and is waiting for the
 * {@link #activate() activate} method to be called. 
 * Such a call triggers the
 * retrieval of all the <i>OME</i> objects of the specified hierarchy kind that
 * are rooted by the specified nodes and the hierarchy display is built and set
 * on screen and the object automatically starts loading the thumbnails for all 
 * the images in the display, which makes
 * it transition to the {@link #LOADING} state. When all thumbnails
 * have been downloaded, the object is {@link #READY} for interacting with the
 * user.  (The viewer allows the user to interact with it even before the
 * {@link #READY} state is reached, as long as the data required for the
 * interaction is already in memory.)  When the user quits the window, the
 * {@link #discard() discard} method is invoked and the object transitions to
 * the {@link #DISCARDED} state.  At which point, all clients should 
 * de-reference the component to allow for garbage collection.</p>
 *
 * @see Browser
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public interface DataBrowser
	extends ObservableComponent
{

    /** Identifies the <code>DatasetsModel</code>. */
    public static final int DATASETS = 0;
    
    /** Identifies the <code>ImagesModel</code>. */
    public static final int IMAGES = 1;
    
    /** Identifies the <code>ProjectsModel</code>. */
    public static final int PROJECTS = 2;
    
    /** Identifies the <code>SearchModel</code>. */
    public static final int SEARCH = 3;
    
    /** Identifies the <code>TagSetsModel</code>. */
    public static final int TAGSETS = 4;
    
    /** Identifies the <code>WellsModel</code>. */
    public static final int WELLS = 5;
    
    /** Identifies the <code>TagsModel</code>. */
    public static final int TAGS = 6;
    
    /** Identifies the <code>PlatesModel</code>. */
    public static final int PLATES = 7;
    
    /** Identifies the <code>GroupModel</code>. */
    public static final int GROUP = 8;
    
    /** Identifies the <code>FSFolderModel</code>. */
    public static final int FS_FOLDER = 9;

	/** Bound property indicating to activate the user.*/
	public static final String ACTIVATE_USER_PROPERTY = "activateUser";

	/** Bound property indicating to reset the password of the selected user.*/
	public static final String RESET_PASSWORD_PROPERTY = "resetUserPassword";
	
	/** Bound property indicating to tag selected objects. */
	public static final String		TAG_WIZARD_PROPERTY = "tagWizard";
	
	/** 
	 * Bound property indicating that a <code>DataObject</code> has 
	 * been created.
	 */
	public static final String		DATA_OBJECT_CREATED_PROPERTY = 
										"dataObjectCreated";
	
	/** 
	 * Bound property indicating that the images have been added to a collection
	 * of <code>DataObject</code>s.
	 */
	public static final String		ADDED_TO_DATA_OBJECT_PROPERTY = 
										"addedToDataObject";

	/** 
	 * Bound property name indicating an {@link ImageDisplay} object has been
	 * selected in the visualization tree. 
	 */
	public static final String 		SELECTED_NODE_DISPLAY_PROPERTY = 
												"selectedNodeDisplay";
	
	/** 
	 * Bound property name indicating {@link ImageDisplay} objects have been
	 * selected in the visualization tree. 
	 */
	public static final String SELECTED_DATA_BROWSER_NODES_DISPLAY_PROPERTY = 
		"selectedNodesDisplay";
	
	/** 
	 * Bound property name indicating that nodes have been marked as selected.
	 */
	public static final String 		SELECTION_UPDATED_PROPERTY = 
												"selectionUpdated";
	
	/** 
	 * Bound property name indicating an {@link ImageDisplay} object has been
	 * unselected in the visualization tree. 
	 */
	public static final String 		UNSELECTED_NODE_DISPLAY_PROPERTY = 
												"unselectedNodeDisplay";
	
	/** Bound property indicating to copy the rendering settings. */
	public static final String		COPY_RND_SETTINGS_PROPERTY = 
										"copyRndSettings";
	
	/** Bound property indicating to paste the rendering settings. */
	public static final String		PASTE_RND_SETTINGS_PROPERTY = 
										"pasteRndSettings";
	
	/** Bound property indicating to reset the rendering settings. */
	public static final String		RESET_RND_SETTINGS_PROPERTY = 
										"resetRndSettings";
	
	/** Bound property indicating to set the original rendering settings. */
	public static final String		SET__ORIGINAL_RND_SETTINGS_PROPERTY = 
										"setOriginalRndSettings";
	
	/** 
	 * Bound property indicating to set the rendering settings used
	 * by the owner. 
	 */
	public static final String		SET__OWNER_RND_SETTINGS_PROPERTY = 
										"setOwnerRndSettings";
	
	/** 
	 * Bound property indicating that some rendering settings can be copied. 
	 */
	public static final String		RND_SETTINGS_TO_COPY_PROPERTY = 
										"rndSettingToCopy";
	
	/** Bound property indicating that a new field has been selected. */
	public static final String		FIELD_SELECTED_PROPERTY = "fieldSelected";
	
	/** 
	 * Bound property indicating to that some data can be copied. 
	 */
	public static final String		ITEMS_TO_COPY_PROPERTY = "itemsToCopy";
	
	/** Bound property indicating to copy the items. */
	public static final String		COPY_ITEMS_PROPERTY = "copyItems";
	
	/** Bound property indicating to paste the items. */
	public static final String		PASTE_ITEMS_PROPERTY = "pasteItems";
	
	/** Bound property indicating to cut the items. */
	public static final String		CUT_ITEMS_PROPERTY = "cutItems";
	
	/** Bound property indicating to remove the items. */
	public static final String		REMOVE_ITEMS_PROPERTY = "removeItems";

	/** 
	 * Bound property indicating to open the document with an external 
	 * application. 
	 */
	public static final String		OPEN_EXTERNAL_APPLICATION_PROPERTY = 
		"openExternalApplication";

	/** Bound property indicating to view the image node. */
	public static final String		VIEW_IMAGE_NODE_PROPERTY = "viewImageNode";
	
	/** Bound property indicating to view the image node. */
	public static final String INTERNAL_VIEW_NODE_PROPERTY =
		"internalViewImageNode";
	
	/** Indicates to lay out the nodes as thumbnails. */
	public static final int			THUMBNAIL_VIEW = 0;
	
	/** Indicates to lay out the nodes as table rows. */
	public static final int			COLUMN_VIEW = 1;
	
	/** Indicates to lay out the nodes in a slide show. */
	public static final int			SLIDE_SHOW_VIEW = 2;
	
	/** Indicates to retrieve the node rated one or higher. */
	public static final int			RATE_ONE = RateFilter.RATE_ONE;
	
	/** Indicates to retrieve the node rated two or higher. */
	public static final int			RATE_TWO = RateFilter.RATE_TWO;
	
	/** Indicates to retrieve the node rated three or higher. */
	public static final int			RATE_THREE = RateFilter.RATE_THREE;
	
	/** Indicates to retrieve the node rated four or higher. */
	public static final int			RATE_FOUR = RateFilter.RATE_FOUR;
	
	/** Indicates to retrieve the node rated five. */
	public static final int			RATE_FIVE = RateFilter.RATE_FIVE;
	
	/** Indicates to retrieve the node rated two or higher. */
	public static final int			UNRATED = RateFilter.UNRATED;;
	
	/** Flag to denote the <i>New</i> state. */
    public static final int     	NEW = 1;

    /** Flag to denote the <i>Loading</i> state. */
    public static final int     	LOADING = 2;

    /** Flag to denote the <i>Ready</i> state. */
    public static final int     	READY = 3;

    /** Flag to denote the <i>Filtering</i> state. */
    public static final int     	FILTERING = 4;
    
    /** Flag to denote the <i>Discarded</i> state. */
    public static final int     	DISCARDED = 5;
    
    /** Flag to denote the <i>Loading slide view</i> state. */
    public static final int     	LOADING_SLIDE_VIEW = 6;
 
    /**
     * Queries the current state.
     * 
     * @return One of the state flags defined by this interface.
     */
    public int getState();
    
    /**
     * Indicates what kind of hierarchy the viewer is displaying.
     * 
     * @return One of the hierarchy flags defined by this interface.
     */
    public int getHierarchyType();
    
    /**
     * Starts the data loading process when the current state is {@link #NEW} 
     * and puts the window on screen.
     * If the state is not {@link #NEW}, then this method simply moves the
     * window to front.
     * 
     * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
     */
    public void activate();
    
    /**
     * Call-back used by a data loader to set thumbnails as they are retrieved.
     * 
     * @param ref    The id of the image or to the object of reference
     * 				 which the thumbnail belongs.
     * @param thumb  The thumbnail pixels.
     * @param valid  Pass <code>true</code> if it is a valid thumbnail,
     * 				 <code>false</code> otherwise.
     * @param maxEntries The number of thumbnails to load.
     * @see org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailLoader
     */
    public void setThumbnail(Object ref, BufferedImage thumb, boolean valid, 
    		int maxEntries);
    
    /**
     * Call-back used by data loaders to provide the viewer with feedback about
     * the data retrieval.
     * 
     * @param description Textual description of the ongoing operation.
     * @param perc Percentage of the total work done.  If negative, it is
     *             interpreted as not available.
     * @see org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader
     */
    public void setStatus(String description, int perc);
        
    /**
     * Transitions the viewer to the {@link #DISCARDED} state.
     * Any ongoing data loading is cancelled.
     */
    public void discard();
    
    /** 
     * Returns the UI component. 
     * 
     * @param full  Pass <code>true</code> to view the full view,
     * 				<code>false</code> otherwise.
     * @return See above.
     * @throws IllegalStateException If the current state is
     *                               {@link #DISCARDED}.
     */
    public JComponent getUI(boolean full);
    
    /**
     * Sets the selected node.
     * 
     * @param node The node to set.
     */
	public void setSelectedDisplay(ImageDisplay node);

    /**
     * Sets the selected nodes.
     * 
     * @param nodes The node to set.
     */
	public void setSelectedDisplays(List<ImageDisplay> nodes);
	
	/**
	 * Sets the collection of selected nodes.
	 * 
	 * @param nodes 		The selected nodes.
	 * @param applications 	The external applications previously used
	 * 						to open the selected document
	 */
	public void setSelectedNodes(List<DataObject> nodes, List<ApplicationData>
								applications);
	
	/**
	 * Sets the external applications used to open the selected document.
	 * 
	 * @param applications The value to set.
	 */
	public void setApplications(List<ApplicationData> applications);
	
	/**
	 * Filters by tags.
	 * 
	 * @param tags The collection of selected tags.
	 */
	public void filterByTags(List<String> tags);
	
	/**
	 * Filters by comments.
	 * 
	 * @param comments The collection of comments to handle.
	 */
	public void filterByComments(List<String> comments);
	
	/**
	 * Filters the nodes by name and description.
	 * 
	 * @param terms The collection of terms to handle.
	 */
	public void filterByFullText(List<String> terms);
	
	/**
	 * Filters the nodes by rate.
	 * 
	 * @param rate 	The selected rate. One of the constants defined by this 
	 * 				class.
	 */
	public void filterByRate(int rate);
	
	/** Shows all the nodes. */
	public void showAll();
	
	/**
	 * Sets the collection of filtered nodes.
	 * 
	 * @param objects The nodes to filter.
	 * @param names	  The collection of terms to filter by.
	 */
	public void setFilteredNodes(List<DataObject> objects, List<String> names);

	/**
	 * Filters the images.
	 * 
	 * @param context The filtering context.
	 */
	public void filterByContext(FilterContext context);
	
	/** Loads the existing tags. */
	public void loadExistingTags();

	/**
	 * Sets the existing tags.
	 * 
	 * @param collection The collection to set.
	 */
	public void setExistingTags(Collection collection);
	
	 /**
     * Call-back used by data loaders to provide the viewer with feedback about
     * the data retrieval.
     * 
     * @param description 	Textual description of the ongoing operation.
     * @param perc 			Percentage of the total work done.  
     * 						If negative, it is interpreted as not available.
     */
    public void setSlideViewStatus(String description, int perc);
    
    /**
     * Call-back used by a data loader to set thumbnails as they are retrieved.
     * 
     * @param imageID The id of the image to which the thumbnail belongs.
     * @param thumb The thumbnail pixels.
     */
    public void setSlideViewImage(long imageID, BufferedImage thumb);

    /**
     * Creates a new data object containing the displayed nodes.
     * 
     * @param data 			The object to create.
     */
	public void createDataObject(DataObject data);

	/**
	 * Sets the newly created data object.
	 * 
	 * @param object	The created object.
	 * @param parent	The parent of the object.
	 */
	public void setDataObjectCreated(DataObject object, DataObject parent);

	/**
	 * Sets the collection of nodes, selected via the table view.
	 * 
	 * @param selected The collection of selected nodes.
	 */
	public void setTableNodesSelected(List<ImageDisplay> selected);
	
	/**
	 * Sets the unselected node.
	 * 
	 * @param node The unselected node.
	 */
	public void setUnselectedDisplay(ImageDisplay node);

	/**
	 * Returns the {@link Browser} I/F.
	 * 
	 * @return See above.
	 */
	public Browser getBrowser();

	/** 
	 * Pastes the stored rendering settings if any across the selected images. 
	 */
	public void pasteRndSettings();

	/** 
	 * Resets the stored rendering settings if any across the selected images. 
	 */
	public void resetRndSettings();
	
	/** 
	 * Copies the rendering settings if any across the selected images. 
	 */
	public void copyRndSettings();
	
	/** Copies the selected items. */
	public void copy();
	
	/** Pastes the selected items. */
	public void paste();
	
	/** Cuts the selected items. */
	public void cut();
	
	/** Removes the selected items. */
	public void remove();
	
	/**
	 * Returns <code>true</code> if the specified object can be deleted.
	 * <code>false</code> otherwise, depending on the permission.
	 * 
	 * @param ho The data object to check.
	 * @return See above.
	 */
	public boolean canDelete(Object ho);
	
	/**
	 * Returns <code>true</code> if the specified object can be edited,
	 * <code>false</code> otherwise, depending on the permission.
	 * 
	 * @param ho The data object to check.
	 * @return See above.
	 */
	public boolean canEdit(Object ho);
	
	/**
	 * Returns <code>true</code> if the specified object can be moved to 
	 * another group, <code>false</code> otherwise, depending on the permission.
	 * 
	 * @param ho The data object to check.
	 * @return See above.
	 */
	public boolean canChgrp(Object ho);

	/**
	 * Returns <code>true</code> if the specified object can be annotated,
	 * <code>false</code> otherwise, depending on the permission.
	 * 
	 * @param ho The data object to check.
	 * @return See above.
	 */
	public boolean canAnnotate(Object ho);
	
	/**
	 * Returns <code>true</code> if the specified object can be linked,
	 * e.g. image added to dataset.
	 * <code>false</code> otherwise, depending on the permission.
	 * 
	 * @param ho The data object to check.
	 * @return See above.
	 */
	public boolean canLink(Object ho);
	
	/**
	 * Reloads the thumbnails. 
	 * 
	 * @param ids Collection of images to reload the thumbnails or 
	 *            <code>null</code> if all the thumbnails have to be reloaded.
	 */
	public void reloadThumbnails(Collection ids);

	/** Sets the original rendering settings. */
	public void setOriginalSettings();

	/** Sets the rendering settings used by the owner of the images. */
	public void setOwnerSettings();
	
	/**
	 * Shows the tagged nodes if the passed value is <code>true</code>,
	 * shows the untagged nodes if the passed value is <code>false</code>.
	 * 
	 * @param tagged Pass <code>true</code> to show the tagged nodes,
	 * 				 <code>false</code> to show the untagged nodes.
	 */
	public void filterByTagged(boolean tagged);
	
	/**
	 * Shows the commented nodes if the passed value is <code>true</code>,
	 * shows the uncommented nodes if the passed value is <code>false</code>.
	 * 
	 * @param commented Pass <code>true</code> to show the commented nodes,
	 * 				 <code>false</code> to show the uncommented nodes.
	 */
	public void filterByCommented(boolean commented);

	/**
         * Shows the nodes with ROIs if the passed value is <code>true</code>,
         * shows the nodes without ROIs if the passed value is <code>false</code>.
         * 
         * @param hasROIs Pass <code>true</code> to show the nodes with ROIs,
         *                               <code>false</code> to show the nodes without ROIs.
         */
	public void filterByROIs(boolean hasROIs);
	
	/** 
	 * Sets the passed title in the header of the browser.
	 * 
	 * @param title The value to set.
	 */
	public void setComponentTitle(String title);
	
	/**
	 * Views the selected well sample field while browsing a plate.
	 * 
	 * @param field The index of the field.
	 */
	public void viewField(int field);
	
	/** 
	 * Saves the displayed thumbnails as a single image. 
	 * 
	 * @param file The file where to save the data.
	 */
	public void saveThumbnails(File file);
	
	/**
	 * Returns <code>true</code> if the model is of type
	 * <code>Dataset</code>, <code>Images</code>, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isImagesModel();

	/**
	 * Sets the data for the report.
	 * 
	 * @param data 	The data to add to report.
	 * @param types	The types of annotation to add to the report.
	 * @param path	The name of the report.
	 */
	public void setReportData(Map<ImageNode, StructuredDataResults> data, 
			List<Class> types, String path);

	/**
	 * Creates a report.
	 * 
	 * @param name The name of the report.
	 */
	public void createReport(String name);

	/**
	 * Returns <code>true</code> if we can paste some rendering settings,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasRndSettings();

	/**
	 * Returns the type of objects to copy or <code>null</code> if no objects
	 * selected.
	 * 
	 * @return See above.
	 */
	public Class hasDataToCopy();
	
	/**
	 * Marks the nodes on which a given operation could not be performed
	 * e.g. paste rendering settings.
	 * 
	 * @param type The type of data objects.
	 * @param ids  Collection of object's ids.
	 */
	public void markUnmodifiedNodes(Class type, Collection<Long> ids);

	/**
	 * Brings up the tag wizard.
	 */
	public void showTagWizard();

	/**
	 * Sets the selected cell. This method can only be invoked if
	 * the model is <code>WellsModel</code>.
	 *  
	 * @param cell The selected cell.
	 */
	public void setSelectedCell(CellDisplay cell);

	/** Loads the existing owned by the user. */
	public void loadExistingDatasets();

	/**
	 * Sets the existing datasets.
	 * 
	 * @param result The value to set.
	 */
	public void setExistingDatasets(Collection result);

	/**
	 * Adds the selected images to the passed datasets.
	 * 
	 * @param selected The collection of selected datasets
	 */
	public void addToDatasets(Collection selected);

	/** Indicates to refresh when images are added to datasets. */
	public void refresh();

	/** Shows or hides the fields view. */
	public void displayFieldsView();
	
	/** 
	 * Loads all the fields for the specified well.
	 * 
	 * @param row The row identifying the well.
	 * @param column The column identifying the well.
	 * @param multiSelection Pass <code>true</code> for multiple selection,
	 * 		                 <code>false</code> otherwise.
	 */
	public void viewFieldsFor(int row, int column, boolean multiSelection);
	
	/**
	 * Sets the thumbnails for all the fields of the specified well.
	 * 
	 * @param list 	 The collection of thumbnails.
	 * @param row    The row identifying the well.
	 * @param column The column identifying the well.
	 */
	public void setThumbnailsFieldsFor(List list, int row, int column);

	/**
	 * Opens the currently selected object with an external application.
	 * 
	 * @param data The external application.
	 */
	public void openWith(ApplicationData data);
	
	/**
	 * Sets the experimenter currently selected. This method should
	 * only be invoked when browsing time interval.
	 * 
	 * @param exp The user currently selected.
	 */
	public void setExperimenter(ExperimenterData exp);
	
	/**
	 * Returns the grid representing the plate.
	 * 
	 * @return See above.
	 */
	PlateGridUI getGridUI();
	
	/**
	 * Returns the parent of the nodes.
	 * 
	 * @return See above.
	 */
	Object getParentOfNodes();

	/**
	 * Sets the tabular data.
	 * 
	 * @param data The value to set.
	 */
	void setTabularData(List<TableResult> data);
	
	/** Lays out the nodes.*/
	void layoutDisplay();
	
	/**
	 * Views the passed node if supported.
	 * 
	 * @param node The node to handle.
	 * @param internal Pass <code>true</code> to open using the internal viewer.
	 * <code>false</code> otherwise.
	 */
	void viewDisplay(ImageDisplay node, boolean internal);
	
	/**
	 * Returns the security context.
	 * 
	 * @return See above.
	 */
	SecurityContext getSecurityContext();
	
	/**
	 * Returns <code>true</code> if the image to copy the rendering settings
	 * from is in the specified group, <code>false</code> otherwise.
	 * 
	 * @param groupID The group to handle.
	 * @return See above.
	 */
	boolean areSettingsCompatible(long groupID);
	
	/**
	 * Returns the display mode. One of the constants defined by 
	 * {@link LookupNames}.
	 * 
	 * @return See above.
	 */
	int getDisplayMode();

	/**
	 * Sets the display mode.
	 * 
	 * @param displayMode The value to set.
	 */
	void setDisplayMode(int displayMode);

	/** 
	 * Returns the type of the model.
	 * 
	 * @return See above.
	 */
	int getType();
	
    /**
     * Activates the user or de-activates the user.
     * 
     * @param exp The experimenter to handle.
     */
    void activateUser(ExperimenterData exp);

    /** Indicates to reset the password of the selected user.*/
    void resetPassword();

    /**
     * Returns <code>true</code> if the user is a system user e.g. root
     * <code>false</code> otherwise.
     *
     * @param userID The identifier of the user.
     * @return See above.
     */
    boolean isSystemUser(long userID);

    /**
     * Returns <code>true</code> if the user is a system user e.g. root
     * <code>false</code> otherwise.
     *
     * @param userID The identifier of the user.
     * @param key One of the constants defined by <code>GroupData</code>.
     * @return See above.
     */
    boolean isSystemUser(long userID, String key);

    /**
     * Returns <code>true</code> if the group is a system group,
     * <code>false</code> otherwise.
     *
     * @param groupID The identifier of the group.
     * @param key One of the constants defined by <code>GroupData</code>.
     * @return See above.
     */
    boolean isSystemGroup(long groupID, String key);

    /**
     * Returns the user currently logged in.
     * 
     * @return See above.
     */
    ExperimenterData getCurrentUser();
}
