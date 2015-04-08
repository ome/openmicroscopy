/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerControl
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

package org.openmicroscopy.shoola.agents.treeviewer.view;


//Java imports
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.MenuListener;

import org.apache.commons.collections.CollectionUtils;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ActivatedUserAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ActivationAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.AddAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.BrowseContainerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserSelectionAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ClearAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CreateAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CreateObjectWithChildren;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CreateTopContainerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.DisplayModeAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.DownloadAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ExitApplicationAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.FinderAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.FullScreenViewerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.GroupSelectionAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ImportAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.InspectorVisibilityAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.LogOffAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ManageObjectAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ManageRndSettingsAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ManagerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.MetadataVisibilityAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.MoveToAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.NewObjectAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.PasswordResetAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.PersonalManagementAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RefreshExperimenterData;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RefreshTreeAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RemoveExperimenterNode;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RemoveGroupNode;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RollOverAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RunScriptAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SaveResultsAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SearchAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SendFeedbackAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SwitchGroup;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SwitchUserAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.TaggingAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.TreeViewerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.UploadScriptAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ViewImageAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ViewInPlugin;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ViewOtherAction;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.CopyCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.CutCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.DeleteCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.PasteCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.PasteRndSettingsCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewCmd;
import org.openmicroscopy.shoola.agents.treeviewer.util.AddExistingObjectsDialog;
import org.openmicroscopy.shoola.agents.treeviewer.util.AdminDialog;
import org.openmicroscopy.shoola.agents.treeviewer.util.GenericDialog;
import org.openmicroscopy.shoola.agents.treeviewer.util.OpenWithDialog;
import org.openmicroscopy.shoola.agents.util.DataObjectRegistration;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.browser.TreeFileSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.finder.Finder;
import org.openmicroscopy.shoola.agents.util.ui.EditorDialog;
import org.openmicroscopy.shoola.agents.util.ui.GroupManagerDialog;
import org.openmicroscopy.shoola.agents.util.ui.ScriptingDialog;
import org.openmicroscopy.shoola.agents.util.ui.UserManagerDialog;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.data.model.DownloadActivityParam;
import org.openmicroscopy.shoola.env.data.model.DownloadAndLaunchActivityParam;
import org.openmicroscopy.shoola.env.data.model.FigureActivityParam;
import org.openmicroscopy.shoola.env.data.model.FigureParam;
import org.openmicroscopy.shoola.env.data.model.ScriptActivityParam;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.JXTaskPaneContainerSingle;
import org.openmicroscopy.shoola.util.ui.LoadingWindow;
import org.openmicroscopy.shoola.util.ui.MacOSMenuHandler;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.TagAnnotationData;
import pojos.WellData;
import pojos.WellSampleData;


/** 
 * The {@link TreeViewer}'s controller. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class TreeViewerControl
 	implements ChangeListener, PropertyChangeListener, WindowFocusListener
{

	/** Identifies the <code>Browse action</code> in the Edit menu. */
	static final Integer	BROWSE = Integer.valueOf(1);

	/** Identifies the <code>Create object action</code> in the File menu. */
	static final Integer	CREATE_OBJECT = Integer.valueOf(3);

	/** Identifies the <code>Copy object action</code> in the Edit menu. */
	static final Integer	COPY_OBJECT = Integer.valueOf(4);

	/** Identifies the <code>Paste object action</code> in the Edit menu. */
	static final Integer	PASTE_OBJECT = Integer.valueOf(5);

	/** Identifies the <code>Delete object action</code> in the Edit menu. */
	static final Integer	DELETE_OBJECT = Integer.valueOf(6);

	/** 
	 * Identifies the <code>Hierarchy Explorer</code> action in the View menu. 
	 */
	static final Integer	HIERARCHY_EXPLORER = Integer.valueOf(7);

	/** Identifies the <code>Images Explorer</code> action in the View menu. */
	static final Integer	IMAGES_EXPLORER = Integer.valueOf(9);

	/** Identifies the <code>Find action </code>in the Edit menu. */
	static final Integer	FIND = Integer.valueOf(10);

	/** Identifies the <code>Exit action</code> in the File menu. */
	static final Integer    EXIT = Integer.valueOf(14);

	/** Identifies the <code>Clear action</code> in the Edit menu. */
	static final Integer    CLEAR = Integer.valueOf(15);

	/** Identifies the <code>Add action</code> in the Edit menu. */
	static final Integer    ADD_OBJECT = Integer.valueOf(16);

	/** Identifies the <code>Create project</code> in the File menu. */
	static final Integer    CREATE_TOP_PROJECT = Integer.valueOf(17);

	/** 
	 * Identifies the <code>Refresh tree action</code> in the 
	 * File menu.
	 */
	static final Integer    REFRESH_TREE = Integer.valueOf(18);

	/** 
	 * Identifies the <code>Manager</code> in the 
	 * File menu.
	 */
	static final Integer    MANAGER = Integer.valueOf(19);

	/** 
	 * Identifies the <code>Cut action</code> in the 
	 * Edit menu.
	 */
	static final Integer    CUT_OBJECT = Integer.valueOf(21);

	/** 
	 * Identifies the <code>Activation action</code> in the 
	 * Edit menu.
	 */
	static final Integer    ACTIVATION = Integer.valueOf(22);

	/** 
	 * Identifies the <code>Switch user action</code> in the 
	 * File menu.
	 */
	static final Integer    SWITCH_USER = Integer.valueOf(23);

	/** Identifies the <code>Roll over action</code>. */
	static final Integer    ROLL_OVER = Integer.valueOf(26);

	/** Identifies the <code>Remove from display action</code>. */
	static final Integer    REMOVE_FROM_DISPLAY = Integer.valueOf(27);

	/** Identifies the <code>Refresh experimenter action</code>. */
	static final Integer    REFRESH_EXPERIMENTER = Integer.valueOf(29);

	/** Identifies the <code>Paste rendering settings action</code>. */
	static final Integer    PASTE_RND_SETTINGS = Integer.valueOf(31);

	/** Identifies the <code>Copy rendering settings action</code>. */
	static final Integer    COPY_RND_SETTINGS = Integer.valueOf(32);
	
	/** Identifies the <code>Reset rendering settings action</code>. */
	static final Integer    RESET_RND_SETTINGS = Integer.valueOf(33);
	
	/** Identifies the <code>Search action</code>. */
	static final Integer    SEARCH = Integer.valueOf(34);
	
	/** Identifies the <code>Tags action</code>. */
	static final Integer    TAGS_EXPLORER = Integer.valueOf(35);
	
	/** Identifies the <code>Set rendering settings</code>. */
	static final Integer    SET_RND_SETTINGS = Integer.valueOf(36);
	
	/** Identifies the <code>Create dataset</code> in the File menu. */
	static final Integer    CREATE_TOP_DATASET = Integer.valueOf(37);
	
	/** Identifies the <code>Create tag</code> in the File menu. */
	static final Integer    CREATE_TOP_TAG = Integer.valueOf(38);
	
	/** Identifies the <code>Screens Explorer</code> action in the View menu. */
	static final Integer	SCREENS_EXPLORER = Integer.valueOf(39);
	
	/** Identifies the <code>Create project</code> in the File menu. */
	static final Integer    CREATE_TOP_SCREEN = Integer.valueOf(40);
	
	/** Identifies the <code>View action</code> in the Edit menu. */
	static final Integer	VIEW = Integer.valueOf(41);
	
	/** Identifies the <code>Create project</code> in the File menu. */
	static final Integer    NEW_OBJECT = Integer.valueOf(42);
	
	/** Identifies the <code>Files Explorer</code> action in the View menu. */
	static final Integer	FILES_EXPLORER = Integer.valueOf(44);
	
	/** Identifies the <code>Create tag set</code> in the File menu. */
	static final Integer    CREATE_TOP_TAG_SET = Integer.valueOf(45);
	
	/** Identifies the <code>Create tag sets or tags</code> in the menu. */
	static final Integer    NEW_TAG_OBJECT = Integer.valueOf(46);
	
	/** Identifies the <code>Add or remove tag</code> in the menu. */
	static final Integer    TAGGING = Integer.valueOf(47);
	
	/** Identifies the <code>Show/hide inspector</code> in the menu. */
	static final Integer    INSPECTOR = Integer.valueOf(50);
	
	/** 
	 * Identifies the <code>File System Explorer</code> action in the View menu.
	 */
	static final Integer    FILE_SYSTEM_EXPLORER = Integer.valueOf(52);
	
	/** Identifies the <code>Import</code> in the menu. */
	static final Integer    IMPORT = Integer.valueOf(53);
	
	/** Identifies the <code>Download</code> in the menu. */
	static final Integer    DOWNLOAD = Integer.valueOf(54);

	/** Identifies the <code>Browse w/o thumbnails</code> in the menu. */
	static final Integer    BROWSE_NO_THUMBNAILS = Integer.valueOf(55);
	
	/** Identifies the <code>View with Other</code> in the menu. */
	static final Integer    VIEWER_WITH_OTHER = Integer.valueOf(56);
	
	/** Identifies the <code>Personal</code> in the menu. */
	static final Integer   PERSONAL = Integer.valueOf(57);
	
	/** Identifies the <code>Full Screen</code> in the menu. */
	static final Integer   FULLSCREEN = Integer.valueOf(58);
	
	/** Identifies the <code>Metadata display</code> in the menu. */
	static final Integer   METADATA = Integer.valueOf(59);
	
	/** Identifies the <code>Personal</code> in the menu. */
	static final Integer   UPLOAD_SCRIPT = Integer.valueOf(60);
	
	/** Identifies the <code>Create group</code> in the File menu. */
	static final Integer    CREATE_TOP_GROUP = Integer.valueOf(61);
	
	/** Identifies the <code>Create experimenter</code> in the File menu. */
	static final Integer    CREATE_TOP_EXPERIMENTER = Integer.valueOf(62);
	
	/** Identifies the <code>Reset Password</code> action. */
	static final Integer    RESET_PASSWORD = Integer.valueOf(63);
	
	/** Identifies the <code>Reset the rendering settings action</code>. */
	static final Integer    SET_OWNER_RND_SETTINGS = Integer.valueOf(64);
	
	/** Identifies the <code>Send comment action</code>. */
	static final Integer    SEND_COMMENT = Integer.valueOf(65);
	
	/** Identifies the <code>Activated action</code>. */
	static final Integer    USER_ACTIVATED = Integer.valueOf(66);
	
	/** Identifies the <code>Import</code> action. */
	static final Integer    IMPORT_NO_SELECTION = Integer.valueOf(67);

	/** Identifies the <code>Log off</code> action. */
	static final Integer    LOG_OFF = Integer.valueOf(68);
	
	/** Identifies the <code>Create dataset</code> in the File menu. */
	static final Integer    CREATE_DATASET_FROM_SELECTION = Integer.valueOf(69);

	/** Identifies the <code>Available scripts/code>. */
	static final Integer    AVAILABLE_SCRIPTS = Integer.valueOf(70);
	
	/** Identifies the <code>Remove the group/code>. */
	static final Integer    REMOVE_GROUP = Integer.valueOf(71);
	
	/** Identifies the <code>View In ImageJ</code> in the menu. */
	static final Integer    VIEW_IN_IJ = Integer.valueOf(72);
	
	/** 
	 * Identifies the <code>Switch group action</code> in the 
	 * File menu.
	 */
	static final Integer    SWITCH_GROUP = Integer.valueOf(73);
	
	/** Identifies the <code>View In KNIME</code> in the menu. */
	static final Integer    VIEW_IN_KNIME = Integer.valueOf(74);
	
	/** Identifies the <code>Display Experimenter</code> in the menu. */
	static final Integer    DISPLAY_EXPERIMENTER = Integer.valueOf(75);
	
	/** Identifies the <code>Display Group</code> in the menu. */
	static final Integer    DISPLAY_GROUP = Integer.valueOf(76);

	/** Identifies the <code>Save OMERO</code> in the menu. */
	static final Integer SAVE_TO_OMERO = Integer.valueOf(77);

	/** 
	 * Reference to the {@link TreeViewer} component, which, in this context,
	 * is regarded as the Model.
	 */
	private TreeViewer      				model;

	/** Reference to the View. */
	private TreeViewerWin   				view;

	/** Maps actions ids onto actual <code>Action</code> object. */
	private Map<Integer, TreeViewerAction>	actionsMap;

	/** The tab pane listener. */
	private ChangeListener  				tabsListener;

	/** The loading window. */
	private LoadingWindow   				loadingWindow;
	
	/** One per group.*/
	private List<MoveToAction> moveActions;
	
	/**
	 * Downloads the possible script.
	 * 
	 * @param param The parameter holding the script.
	 */
	private void downloadScript(ScriptActivityParam param)
	{
		FileChooser chooser = new FileChooser(view, FileChooser.SAVE, 
				"Download", "Select where to download the file.", null, 
				true);
		IconManager icons = IconManager.getInstance();
		chooser.setTitleIcon(icons.getIcon(IconManager.DOWNLOAD_48));
		chooser.setSelectedFileFull(param.getScript().getName());
		chooser.setApproveButtonText("Download");
		final long id = param.getScript().getScriptID();
		chooser.addPropertyChangeListener(new PropertyChangeListener() {
		
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
					File[] files = (File[]) evt.getNewValue();
					File folder = files[0];
					IconManager icons = IconManager.getInstance();
					DownloadActivityParam activity;
					activity = new DownloadActivityParam(id, 
							DownloadActivityParam.ORIGINAL_FILE,
							folder, icons.getIcon(IconManager.DOWNLOAD_22));
					UserNotifier un = 
						TreeViewerAgent.getRegistry().getUserNotifier();
					SecurityContext ctx = new SecurityContext(
					TreeViewerAgent.getUserDetails().getDefaultGroup().getId());
					un.notifyActivity(ctx, activity);
				}
			}
		});
		chooser.centerDialog();
	}
	
	/** 
	 * Handles the selection of a <code>JXTaskPane</code>.
	 * 
	 * @param pane The selected component.
	 */
	private void handleTaskPaneSelection(JXTaskPane pane)
	{
		JXTaskPaneContainerSingle container = 
			(JXTaskPaneContainerSingle) pane.getParent();
		if (pane.isCollapsed() && container.hasTaskPaneExpanded()) return;
		int state = model.getState();
		if (state == TreeViewer.READY || state == TreeViewer.NEW) {
			model.clearFoundResults();
			if (!container.hasTaskPaneExpanded())
				model.setSelectedBrowser(null, true);
			else {
				if (pane instanceof TaskPaneBrowser) {
					TaskPaneBrowser p = (TaskPaneBrowser) pane;
					if (p.getBrowser() != null)
						model.setSelectedBrowser(p.getBrowser(), true);
					else {
						model.setSelectedBrowser(null, true);
						model.showSearch();
					}
				} else {
					model.setSelectedBrowser(null, true);
				}
			}
		} else pane.setCollapsed(true);
	}
	
	/** Helper method to create all the UI actions. */
	private void createActions()
	{
		actionsMap.put(BROWSE, new BrowseContainerAction(model));
		actionsMap.put(BROWSE_NO_THUMBNAILS, 
				new BrowseContainerAction(model, false));
		actionsMap.put(CREATE_OBJECT, new CreateAction(model));
		actionsMap.put(COPY_OBJECT, new ManageObjectAction(model, 
				ManageObjectAction.COPY));
		actionsMap.put(DELETE_OBJECT, new ManageObjectAction(model, 
				ManageObjectAction.REMOVE));
		actionsMap.put(PASTE_OBJECT, new ManageObjectAction(model, 
				ManageObjectAction.PASTE));
		actionsMap.put(CUT_OBJECT, new ManageObjectAction(model, 
				ManageObjectAction.CUT));
		actionsMap.put(SCREENS_EXPLORER, 
				new BrowserSelectionAction(model, Browser.SCREENS_EXPLORER));
		actionsMap.put(HIERARCHY_EXPLORER, 
				new BrowserSelectionAction(model, Browser.PROJECTS_EXPLORER));
		actionsMap.put(TAGS_EXPLORER, 
				new BrowserSelectionAction(model, Browser.TAGS_EXPLORER));
		actionsMap.put(IMAGES_EXPLORER, 
				new BrowserSelectionAction(model, Browser.IMAGES_EXPLORER));
		actionsMap.put(FILES_EXPLORER, 
				new BrowserSelectionAction(model, Browser.FILES_EXPLORER));
		actionsMap.put(FILE_SYSTEM_EXPLORER, new BrowserSelectionAction(model, 
						Browser.FILE_SYSTEM_EXPLORER));
		actionsMap.put(FIND,  new FinderAction(model));
		actionsMap.put(CLEAR, new ClearAction(model));
		actionsMap.put(EXIT, new ExitApplicationAction(model));
		actionsMap.put(ADD_OBJECT,  new AddAction(model));
		actionsMap.put(CREATE_TOP_PROJECT,  
				new CreateTopContainerAction(model, 
						CreateTopContainerAction.PROJECT));
		actionsMap.put(CREATE_TOP_DATASET,  
				new CreateTopContainerAction(model, 
						CreateTopContainerAction.DATASET));
		actionsMap.put(CREATE_TOP_TAG,  
				new CreateTopContainerAction(model, 
						CreateTopContainerAction.TAG));
		actionsMap.put(REFRESH_TREE, new RefreshTreeAction(model));
		actionsMap.put(MANAGER, new ManagerAction(model));
		actionsMap.put(ACTIVATION, new ActivationAction(model));
		actionsMap.put(SWITCH_USER, new SwitchUserAction(model));
		actionsMap.put(ROLL_OVER, new RollOverAction(model));
		actionsMap.put(REMOVE_FROM_DISPLAY, new RemoveExperimenterNode(model));
		actionsMap.put(REFRESH_EXPERIMENTER, 
				new RefreshExperimenterData(model));
		actionsMap.put(PASTE_RND_SETTINGS, new ManageRndSettingsAction(model, 
				ManageRndSettingsAction.PASTE));
		actionsMap.put(COPY_RND_SETTINGS, new ManageRndSettingsAction(model, 
				ManageRndSettingsAction.COPY));
		actionsMap.put(RESET_RND_SETTINGS, new ManageRndSettingsAction(model, 
				ManageRndSettingsAction.RESET));
		actionsMap.put(SET_OWNER_RND_SETTINGS, 
				new ManageRndSettingsAction(model, 
				ManageRndSettingsAction.SET_OWNER_SETTING));
		actionsMap.put(SEARCH, new SearchAction(model));
		actionsMap.put(SET_RND_SETTINGS, new ManageRndSettingsAction(model, 
				ManageRndSettingsAction.SET_MIN_MAX));
		actionsMap.put(CREATE_TOP_SCREEN, 
				new CreateTopContainerAction(model, 
						CreateTopContainerAction.SCREEN));
		actionsMap.put(VIEW, new ViewImageAction(model));
		actionsMap.put(NEW_OBJECT, new NewObjectAction(model, 
								NewObjectAction.NEW_CONTAINERS));
		actionsMap.put(CREATE_TOP_TAG_SET,  
				new CreateTopContainerAction(model, 
						CreateTopContainerAction.TAG_SET));
		actionsMap.put(NEW_TAG_OBJECT, new NewObjectAction(model,
				NewObjectAction.NEW_TAGS));
		actionsMap.put(TAGGING, new TaggingAction(model));
		actionsMap.put(INSPECTOR, new InspectorVisibilityAction(model));
		actionsMap.put(IMPORT, new ImportAction(model, false));
		actionsMap.put(DOWNLOAD, new DownloadAction(model));
		actionsMap.put(VIEWER_WITH_OTHER, new ViewOtherAction(model, null));
		actionsMap.put(PERSONAL, new PersonalManagementAction(model));
		actionsMap.put(FULLSCREEN, new FullScreenViewerAction(model));
		actionsMap.put(METADATA, new MetadataVisibilityAction(model));
		actionsMap.put(UPLOAD_SCRIPT, new UploadScriptAction(model));
		//actionsMap.put(ADMIN, new AdminAction(model));
		actionsMap.put(CREATE_TOP_GROUP,  
				new CreateTopContainerAction(model, 
						CreateTopContainerAction.GROUP));
		actionsMap.put(CREATE_TOP_EXPERIMENTER,  
				new CreateTopContainerAction(model, 
						CreateTopContainerAction.EXPERIMENTER));
		actionsMap.put(RESET_PASSWORD,  new PasswordResetAction(model));
		actionsMap.put(USER_ACTIVATED,  new ActivatedUserAction(model));
		actionsMap.put(SEND_COMMENT,  new SendFeedbackAction(model));
		actionsMap.put(IMPORT_NO_SELECTION, new ImportAction(model, true));
		actionsMap.put(LOG_OFF, new LogOffAction(model));
		actionsMap.put(CREATE_DATASET_FROM_SELECTION,  
				new CreateObjectWithChildren(model, 
						CreateObjectWithChildren.DATASET));
		actionsMap.put(VIEW_IN_IJ, new ViewInPlugin(model, LookupNames.IMAGE_J));
		actionsMap.put(VIEW_IN_KNIME, new ViewInPlugin(model,
		        LookupNames.KNIME));
		actionsMap.put(AVAILABLE_SCRIPTS, new RunScriptAction(model));
		actionsMap.put(REMOVE_GROUP, new RemoveGroupNode(model));
		actionsMap.put(SWITCH_GROUP, new SwitchGroup(model));
		actionsMap.put(DISPLAY_GROUP, new DisplayModeAction(model,
				LookupNames.GROUP_DISPLAY));
		actionsMap.put(DISPLAY_EXPERIMENTER, new DisplayModeAction(model,
				LookupNames.EXPERIMENTER_DISPLAY));
        actionsMap.put(SAVE_TO_OMERO, new SaveResultsAction(model,
                LookupNames.IMAGE_J));
	}

	/** 
	 * Creates the windowsMenuItems. 
	 * 
	 * @param menu The menu to handle.
	 */
	private void createWindowsMenuItems(JMenu menu)
	{
		Set viewers = TreeViewerFactory.getViewers();
		Iterator i = viewers.iterator();
		menu.removeAll();
		while (i.hasNext()) 
			menu.add(new JMenuItem(
					new ActivationAction((TreeViewer) i.next())));
	}

	/** 
	 * Attaches a window listener to the view to discard the model when 
	 * the user closes the window. 
	 */
	private void attachListeners()
	{
		if (UIUtilities.isMacOS()) {
			try {
				MacOSMenuHandler handler = new MacOSMenuHandler(view);
				handler.initialize();
				view.addPropertyChangeListener(this);
			} catch (Throwable e) {}
        }
		Map browsers = model.getBrowsers();
		Iterator i = browsers.values().iterator();
		Browser browser;
		while (i.hasNext()) {
			browser = (Browser) i.next();
			browser.addPropertyChangeListener(this);
			browser.addChangeListener(this);
		}
		view.addWindowFocusListener(this);
		model.addPropertyChangeListener(this);
		view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		view.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { model.closeWindow(); }
		});

		//
		JMenu menu = TreeViewerFactory.getWindowMenu();
		menu.addMenuListener(new MenuListener() {

			public void menuSelected(MenuEvent e)
			{ 
				Object source = e.getSource();
				if (source instanceof JMenu)
					createWindowsMenuItems((JMenu) source);
			}

			/** 
			 * Required by I/F but not actually needed in our case, 
			 * no-operation implementation.
			 * @see MenuListener#menuCanceled(MenuEvent)
			 */ 
			public void menuCanceled(MenuEvent e) {}

			/** 
			 * Required by I/F but not actually needed in our case, 
			 * no-operation implementation.
			 * @see MenuListener#menuDeselected(MenuEvent)
			 */ 
			public void menuDeselected(MenuEvent e) {}

		});

		//Listen to keyboard selection
		menu.addMenuKeyListener(new MenuKeyListener() {

			public void menuKeyReleased(MenuKeyEvent e)
			{
				Object source = e.getSource();
				if (source instanceof JMenu)
					createWindowsMenuItems((JMenu) source);
			}

			/** 
			 * Required by I/F but not actually needed in our case, 
			 * no-operation implementation.
			 * @see MenuKeyListener#menuKeyPressed(MenuKeyEvent)
			 */
			public void menuKeyPressed(MenuKeyEvent e) {}

			/** 
			 * Required by I/F but not actually needed in our case, 
			 * no-operation implementation.
			 * @see MenuKeyListener#menuKeyTyped(MenuKeyEvent)
			 */
			public void menuKeyTyped(MenuKeyEvent e) {}

		});
	}

	/**
	 * Creates a new instance.
	 * The {@link #initialize(TreeViewerWin) initialize} method 
	 * should be called straight 
	 * after to link this Controller to the other MVC components.
	 * 
	 * @param model  Reference to the {@link TreeViewer} component, which, in 
	 *               this context, is regarded as the Model.
	 *               Mustn't be <code>null</code>.
	 */
	TreeViewerControl(TreeViewer model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		actionsMap = new HashMap<Integer, TreeViewerAction>();
	}
	
	/**
	 * Links this Controller to its View.
	 * 
	 * @param view   Reference to the View. Mustn't be <code>null</code>.
	 */
	void initialize(TreeViewerWin view)
	{
		if (view == null) throw new NullPointerException("No view.");
		this.view = view;
		createActions();
		model.addChangeListener(this);
		attachListeners();
		TreeViewerFactory.attachWindowMenuToTaskBar();
		loadingWindow = new LoadingWindow(view);
		loadingWindow.setAlwaysOnTop(false);
		loadingWindow.setStatus("Saving changes");
	}

	/**
	 * Returns the type of the selected browser or <code>-1</code> if
	 * no browser selected.
	 * 
	 * @return See above.
	 */
	int getSelectedBrowserType()
	{
	    Browser browser = model.getSelectedBrowser();
	    if (browser == null) return -1;
	    return browser.getBrowserType();
	}

	/**
	 * Returns the collections of actions identifying the viewers used
	 * for the type of images.
	 * 
	 * @return See above.
	 */
	List<ViewOtherAction> getApplicationActions()
	{
		List<ViewOtherAction> l = new ArrayList<ViewOtherAction>();
		Browser browser = model.getSelectedBrowser();
		if (browser == null) return l;
		TreeImageDisplay d = browser.getLastSelectedDisplay();
		if (d == null) return l;
		Object object = d.getUserObject();
		if (!(object instanceof DataObject)) return l;
		String type = view.getObjectMimeType();
		if (type == null) return l;
		List<ApplicationData> 
			applications = TreeViewerFactory.getApplications(type);
		if (applications == null) return l;
		Iterator<ApplicationData> i = applications.iterator();
		ApplicationData data;
		
		while (i.hasNext()) {
			data = i.next();
			l.add(new ViewOtherAction(model, data));
		}
		return l;
	}

	/**
	 * Returns the actions used to move data between groups. 
	 * 
	 * @return See abo.ve
	 */
	List<MoveToAction> getMoveAction()
	{
		//First check that we can move the data.
		Browser browser = model.getSelectedBrowser();
		List selection = null;
		Iterator j;
		List<Long> owners = new ArrayList<Long>();
		if (browser != null) {
			selection = browser.getSelectedDataObjects();
			if (selection == null) return null;
			int count = 0;
			j = selection.iterator();
			Object o;
			DataObject data;
			
			while (j.hasNext()) {
				o = j.next();
				if (o instanceof DataObject) {
					if (!(o instanceof GroupData ||
						o instanceof ExperimenterData ||
						o instanceof PlateAcquisitionData ||
						o instanceof TagAnnotationData)) {
						if (model.canChgrp(o)) {
							data = (DataObject) o;
							if (!owners.contains(data.getOwner().getId()))
								owners.add(data.getOwner().getId());
							count++;
						}
					}
				}
			}
			if (count != selection.size()) return null;
			if (owners.size() > 1) return null;
		}
		long userID = TreeViewerAgent.getUserDetails().getId();
		long ownerID = -1;
		if (owners.size() > 0) ownerID = owners.get(0);
		
		Collection l = null;
		if (ownerID == userID) {
			l = TreeViewerAgent.getAvailableUserGroups();
		} else {
			if (TreeViewerAgent.isAdministrator()) {
				//load the group the user is member of
				SecurityContext ctx = TreeViewerAgent.getAdminContext();
				try {
					l = TreeViewerAgent.getRegistry().
						getAdminService().loadGroupsForExperimenter(ctx,
								ownerID);
				} catch (Exception e) {
					TreeViewerAgent.getRegistry().getLogger().error(this,
							"cannot retrieve user's groups");
				}
			}
		}
		if (l == null) return null;
		if (moveActions == null)
			moveActions = new ArrayList<MoveToAction>(l.size());
		moveActions.clear();
		List<Long> ids = new ArrayList<Long>();
		if (browser != null && selection != null) {
			j = selection.iterator();
			DataObject data;
			while (j.hasNext()) {
				data = (DataObject) j.next();
				if (!ids.contains(data.getGroupId()))
					ids.add(data.getGroupId());
			}
		}
		
		ViewerSorter sorter = new ViewerSorter();
		List values = sorter.sort(l);
		if (moveActions == null)
			moveActions = new ArrayList<MoveToAction>(l.size());
		moveActions.clear();
		GroupData group;
		Iterator i = values.iterator();
		while (i.hasNext()) {
			group = (GroupData) i.next();
			if (!ids.contains(group.getGroupId()))
				moveActions.add(new MoveToAction(model, group));
		}
		return moveActions;
	}
	
	/**
	 * Returns the {@link ChangeListener} attached to the tab pane,
	 * or creates one if none initialized.
	 * 
	 * @return See above.
	 */
	ChangeListener getTabbedListener()
	{
		if (tabsListener ==  null) {
			tabsListener = new ChangeListener() {
				// This method is called whenever the selected tab changes
				public void stateChanged(ChangeEvent ce) {
					JTabbedPane pane = (JTabbedPane) ce.getSource();
					model.clearFoundResults();
					Component c = pane.getSelectedComponent();
					if (c == null) {
						model.setSelectedBrowser(null, true);
						return;
					}
					Map browsers = model.getBrowsers();
					Iterator i = browsers.values().iterator();
					boolean selected = false;
					Browser browser;
					while (i.hasNext()) {
						browser = (Browser) i.next();
						if (c.equals(browser.getUI())) {
							model.setSelectedBrowser(browser, true);
							selected = true;
							break;
						}
					}
					if (!selected) model.setSelectedBrowser(null, true);
				}
			};
		}
		return tabsListener;
	}
	
	/**
	 * Adds listeners to UI components.
	 *
	 * @param component The component to attach a listener to.
	 */
	void attachUIListeners(JComponent component)
	{
		//Register listener
		if (component instanceof JTabbedPane) {
			((JTabbedPane) component).addChangeListener(getTabbedListener());
		} else if (component instanceof JXTaskPaneContainer) {
			component.addPropertyChangeListener(
					JXTaskPaneContainerSingle.SELECTED_TASKPANE_PROPERTY, this);
		}
	}

	/**
	 * Returns the action corresponding to the specified id.
	 * 
	 * @param id One of the flags defined by this class.
	 * @return The specified action.
	 */
	TreeViewerAction getAction(Integer id) { return actionsMap.get(id); }

	/**
	 * Returns the list of group the user is a member of.
	 * 
	 * @param add Passes <code>true</code> to add the group to the display,
	 * <code>false</code> to switch.
	 * @return See above.
	 */
	List<GroupSelectionAction> getUserGroupAction(boolean add)
	{
		List<GroupSelectionAction> l = new ArrayList<GroupSelectionAction>();
		Collection m = TreeViewerAgent.getAvailableUserGroups();
		if (m == null || m.size() == 0) return l;
		ViewerSorter sorter = new ViewerSorter();
		Iterator i = sorter.sort(m).iterator();
		while (i.hasNext()) {
			l.add(new GroupSelectionAction(model, (GroupData) i.next(), add));
		}
		return l;
	}
	
	/**
	 * Handles the selection of the script.
	 * 
	 * @param script The script to handle.
	 * @param index Indicates to <code>view, download or run</code> the script.
	 */
	private void handleScript(ScriptObject script, int index)
	{
		if (script == null) return;
		
		SecurityContext ctx = new SecurityContext(
				TreeViewerAgent.getUserDetails().getDefaultGroup().getId());
		UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
		if (index == ScriptActivityParam.VIEW) {
			Environment env = (Environment) 
			TreeViewerAgent.getRegistry().lookup(LookupNames.ENV);
			String path = env.getOmeroFilesHome();
			path += File.separator+script.getName();
			File f = new File(path);
			DownloadAndLaunchActivityParam activity;
			activity = new DownloadAndLaunchActivityParam(
					script.getScriptID(), 
					DownloadAndLaunchActivityParam.ORIGINAL_FILE, f, null);

			un.notifyActivity(ctx, activity);
		} else if (index == ScriptActivityParam.DOWNLOAD) {
			downloadScript(new ScriptActivityParam(script,
					ScriptActivityParam.DOWNLOAD));
		} else {
		    long groupID = script.getGroupID();
		    if (groupID < 0) {
		        GroupData g = model.getSelectedGroup();
	            if (g == null) 
	                g = TreeViewerAgent.getUserDetails().getDefaultGroup();
	            groupID = g.getId();
		    }
			ctx = new SecurityContext(groupID);
			un.notifyActivity(ctx, new ScriptActivityParam(script,
					ScriptActivityParam.RUN));
		}
	}
	
	/**
	 * Returns the last node selected.
	 * 
	 * @return See above.
	 */
	TreeImageDisplay getLastSelectedDisplay()
	{
		Browser browser = model.getSelectedBrowser();
		if (browser == null) return null;
		return browser.getLastSelectedDisplay();
	}
	
	/** Activates or not the user. */
	void activateUser()
	{
		TreeImageDisplay node = getLastSelectedDisplay();
		if (node != null && node.getUserObject() instanceof ExperimenterData)
			model.activateUser((ExperimenterData) node.getUserObject());
	}
	
	/** Forwards call to the {@link TreeViewer}. */
	void cancel() { model.cancel(); }
	
	/** 
	 * Reloads the available scripts.
	 * 
	 * @param location The location of the mouse click.
	 */
	void reloadAvailableScripts(Point location, Component source)
	{
		model.showMenu(TreeViewer.AVAILABLE_SCRIPTS_MENU, source, location);
	}
	
	/**
	 * Handles the selection of a script.
	 * 
	 * @param object The object to handle.
	 */
	void handleScriptSelection(ScriptObject object)
	{
		if (object == null) return;
		if (!object.isParametersLoaded())
			model.loadScript(object.getScriptID());
		else model.setScript(object);
	}
	
	/**
	 * Brings up the menu on top of the specified component at 
	 * the specified location.
	 * 
	 * @param menuID    The id of the menu.
	 * @param invoker   The component that requested the pop-up menu.
	 * @param loc       The point at which to display the menu, relative to the
	 *                  <code>component</code>'s coordinates.
	 */
	void showMenu(int menuID, Component invoker, Point loc)
	{
		model.showMenu(menuID, invoker, loc);
	}
	
	/**
	 * Sets the group and the users to add to the display.
	 * 
	 * @param group The selected group
	 * @param seletectedUsers The selected users.
	 * @param removeGroup Flag indicating to remove the group from the display.
	 */
	void setSelection(GroupData group, List<ExperimenterData> seletectedUsers,
			boolean removeGroup)
	{
		if (removeGroup) {
			model.removeGroup(group.getId());
		} else {
			if (model.getGroups().size() > 1)
				model.setUserGroup(Arrays.asList(group));
			model.setHierarchyRoot(group.getId(), seletectedUsers);
		}
	}
	
	/** 
	 * Adds the specified groups to the display. If other groups are already
	 * added to the display and are not in the list, they will be removed.
	 * 
	 * @param toAdd The groups to add.
	 * @param toRemove The groups to remove from the display.
	 */
	void setSelectedGroups(List<GroupData> toAdd, List<GroupData> toRemove)
	{
		if (toRemove != null) {
			Iterator<GroupData> i = toRemove.iterator();
			while (i.hasNext())
				model.removeGroup(i.next().getId());
		}
		model.setUserGroup(toAdd);
	}
	
	/**
	 * Returns <code>true</code> if the selected objects are orphaned images,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isOrphanedImagesSelected()
	{
		Browser b = model.getSelectedBrowser();
		if (b == null) return false;
		TreeImageDisplay[] nodes = b.getSelectedDisplays();
		if (nodes == null || nodes.length == 0) return false;
		TreeImageDisplay n, np;
		for (int i = 0; i < nodes.length; i++) {
			n = nodes[i];
			np = n.getParentDisplay();
			if (np instanceof TreeFileSet &&
				((TreeFileSet) np).getType() == TreeFileSet.ORPHANED_IMAGES)
				return true;
		}
		return false;
	}

	/**
	 * Sets the display mode.
	 *
	 * @param mode The mode to set.
	 */
	void setDisplayMode(int mode) { model.setDisplayMode(mode); }

	/**
	 * Reacts to property changed. 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent pce)
	{
		String name = pce.getPropertyName();
		if (name == null) return;
		if (TreeViewer.CANCEL_LOADING_PROPERTY.equals(name)) {
			Browser browser = model.getSelectedBrowser();
			if (browser != null) browser.cancel();
		} else if (Browser.POPUP_MENU_PROPERTY.equals(name)) {
			Integer c = (Integer) pce.getNewValue();
			Browser browser = model.getSelectedBrowser();
			if (browser != null)
				view.showPopup(c.intValue(), browser.getClickComponent(), 
						browser.getClickPoint());
		} else if (Browser.CLOSE_PROPERTY.equals(name)) {
			Browser browser = (Browser) pce.getNewValue();
			if (browser != null) view.removeBrowser(browser);
		} else if (TreeViewer.FINDER_VISIBLE_PROPERTY.equals(name)) {
			Boolean b = (Boolean) pce.getNewValue();
			if (!b.booleanValue()) {
				model.clearFoundResults();
				model.onComponentStateChange(true);
			}
		} else if (TreeViewer.SELECTED_BROWSER_PROPERTY.equals(name)) {
			Browser  b = model.getSelectedBrowser();
			Iterator i = model.getBrowsers().values().iterator();
			Browser browser;
			while (i.hasNext()) {
				browser = (Browser) i.next();
				browser.setSelected(browser.equals(b));
			}
		} else if (Browser.SELECTED_TREE_NODE_DISPLAY_PROPERTY.equals(name)) {
			model.onSelectedDisplay();
			view.updateMenuItems();
		} else if (TreeViewer.HIERARCHY_ROOT_PROPERTY.equals(name)) {
			/*
          Map browsers = model.getBrowsers();
          Iterator i = browsers.values().iterator();
          Browser browser;
          while (i.hasNext()) {
          	browser = (Browser) i.next();
          	//browser.cleanFilteredNodes();
          	//browser.switchUser();
          }
			 */
		} else if (AddExistingObjectsDialog.EXISTING_ADD_PROPERTY.equals(
				name)) {
			model.addExistingObjects((Set) pce.getNewValue());
		} else if (UserManagerDialog.USER_SWITCH_PROPERTY.equals(name)) {
			Map m = (Map) pce.getNewValue();
			Iterator i = m.entrySet().iterator();
			Long groupID;
			List<ExperimenterData> users;
			Entry entry;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				groupID = (Long) entry.getKey();
				users = (List<ExperimenterData>) entry.getValue();
				model.setHierarchyRoot(groupID, users);
			}
		} else if (UserManagerDialog.NO_USER_SWITCH_PROPERTY.equals(name)) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("User Selection", "Please select a user first.");
		} else if (EditorDialog.CREATE_PROPERTY.equals(name)) {
			DataObject data = (DataObject) pce.getNewValue();
			model.createObject(data, true);
		} else if (EditorDialog.CREATE_NO_PARENT_PROPERTY.equals(name)) {
			DataObject data = (DataObject) pce.getNewValue();
			model.createObject(data, false);
		} else if (MetadataViewer.ON_DATA_SAVE_PROPERTY.equals(name)) {
			Object object =  pce.getNewValue();
			if (object != null) {
				if (object instanceof DataObject)
					model.onDataObjectSave((DataObject) object, 
											TreeViewer.UPDATE_OBJECT);
				else 
					model.onDataObjectSave((List) object, 
							TreeViewer.UPDATE_OBJECT);
			}
		} else if (DataBrowser.SELECTED_NODE_DISPLAY_PROPERTY.equals(name)) {
			model.setSelectedNode(pce.getNewValue());
		} else if (DataBrowser.UNSELECTED_NODE_DISPLAY_PROPERTY.equals(name)) {
			model.setUnselectedNode(pce.getNewValue());
		} else if (DataBrowser.DATA_OBJECT_CREATED_PROPERTY.equals(name)) {
			Map map = (Map) pce.getNewValue();
			if (map != null && map.size() == 1) {
				DataObject data = null;
				Set set = map.entrySet();
				Entry entry;
				Iterator i = set.iterator();
				Object o;
				DataObject parent = null;
				while (i.hasNext()) {
					entry = (Entry) i.next();
					data = (DataObject) entry.getKey();
					o = entry.getValue();
					if (o != null)
						parent = (DataObject) o;
					break;
				}
				if (parent == null)
					model.onOrphanDataObjectCreated(data);
				else model.onDataObjectSave(data, parent, 
									TreeViewer.CREATE_OBJECT);
			}
		} else if (DataBrowser.ADDED_TO_DATA_OBJECT_PROPERTY.equals(name)) {
			//Browser browser =  model.getSelectedBrowser();
			//if (browser != null) browser.refreshLoggedExperimenterData();
			model.refreshTree();
		} else if (DataBrowser.COPY_RND_SETTINGS_PROPERTY.equals(name)) {
			Object data = pce.getNewValue();
			if (data != null) model.copyRndSettings((ImageData) data);
			else model.copyRndSettings(null);
		} else if (DataBrowser.PASTE_RND_SETTINGS_PROPERTY.equals(name)) {
			Object data = pce.getNewValue();
			PasteRndSettingsCmd cmd;
			if (data instanceof Collection) 
				cmd = new PasteRndSettingsCmd(model, PasteRndSettingsCmd.PASTE,
						(Collection) data);
			else cmd = new PasteRndSettingsCmd(model, 
						PasteRndSettingsCmd.PASTE);
			cmd.execute();
		} else if (DataBrowser.RESET_RND_SETTINGS_PROPERTY.equals(name)) {
			Object data = pce.getNewValue();
			PasteRndSettingsCmd cmd;
			if (data instanceof Collection) 
				cmd = new PasteRndSettingsCmd(model, PasteRndSettingsCmd.RESET,
						(Collection) data);
			else cmd = new PasteRndSettingsCmd(model, 
						PasteRndSettingsCmd.RESET);
			cmd.execute();
		} else if (DataBrowser.SET__ORIGINAL_RND_SETTINGS_PROPERTY.equals(
				name)) {
			Object data = pce.getNewValue();
			PasteRndSettingsCmd cmd;
			if (data instanceof Collection) 
				cmd = new PasteRndSettingsCmd(model, 
						PasteRndSettingsCmd.SET_MIN_MAX,
						(Collection) data);
			else cmd = new PasteRndSettingsCmd(model, 
					PasteRndSettingsCmd.SET_MIN_MAX);
			cmd.execute();
		} else if (DataBrowser.SET__ORIGINAL_RND_SETTINGS_PROPERTY.equals(
				name)) {
			Object data = pce.getNewValue();
			PasteRndSettingsCmd cmd;
			if (data instanceof Collection) 
				cmd = new PasteRndSettingsCmd(model, 
						PasteRndSettingsCmd.SET_OWNER,
						(Collection) data);
			else cmd = new PasteRndSettingsCmd(model, 
					PasteRndSettingsCmd.SET_OWNER);
			cmd.execute();
		} else if (DataBrowser.CUT_ITEMS_PROPERTY.equals(name)) {
			CutCmd cmd = new CutCmd(model);
			cmd.execute();
		} else if (DataBrowser.COPY_ITEMS_PROPERTY.equals(name)) {
			CopyCmd cmd = new CopyCmd(model);
			cmd.execute();
		} else if (DataBrowser.PASTE_ITEMS_PROPERTY.equals(name)) {
			PasteCmd cmd = new PasteCmd(model);
			cmd.execute();
		} else if (DataBrowser.REMOVE_ITEMS_PROPERTY.equals(name)) {
			DeleteCmd cmd = new DeleteCmd(model.getSelectedBrowser());
	        cmd.execute();
		} else if (DataBrowser.VIEW_IMAGE_NODE_PROPERTY.equals(name)) {
			//view.get
			Browser browser = model.getSelectedBrowser();
			if (browser != null) {
				TreeImageDisplay node = browser.getLastSelectedDisplay();
				model.browse(node, (DataObject) pce.getNewValue(), false);
			}
		} else if (DataBrowser.INTERNAL_VIEW_NODE_PROPERTY.equals(name)) {
			ViewCmd cmd = new ViewCmd(model, true);
			cmd.execute();
		} else if (Finder.RESULTS_FOUND_PROPERTY.equals(name)) {
			model.setSearchResult(pce.getNewValue());
		} else if (GenericDialog.SAVE_GENERIC_PROPERTY.equals(name)) {
			Object parent = pce.getNewValue();
			if (parent instanceof MetadataViewer) {
				MetadataViewer mv = (MetadataViewer) parent;
				mv.saveData();
			}
		} else if (Browser.DATA_REFRESHED_PROPERTY.equals(name)) {
			model.onSelectedDisplay(); 
		} else if (MetadataViewer.ADMIN_UPDATED_PROPERTY.equals(name)) {
			Object data = pce.getNewValue();
			Map browsers = model.getBrowsers();
			Set set = browsers.entrySet();
			Entry entry;
			Iterator i = set.iterator();
			Browser browser;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				browser = (Browser) entry.getValue();
				browser.refreshAdmin(data);
			}
			view.createTitle();
		} else if (DataBrowser.TAG_WIZARD_PROPERTY.equals(name)) {
			model.showTagWizard();
		} else if (DataBrowser.FIELD_SELECTED_PROPERTY.equals(name)) {
			model.setSelectedField(pce.getNewValue());
		} else if (MetadataViewer.RENDER_THUMBNAIL_PROPERTY.equals(name)) {
			long imageID = ((Long) pce.getNewValue()).longValue();
			List<Long> ids = new ArrayList<Long>(1);
			ids.add(imageID);
			view.reloadThumbnails(ids);
		} else if (MetadataViewer.APPLY_SETTINGS_PROPERTY.equals(name)) {
			Object object = pce.getNewValue();
			if (object instanceof ImageData) {
				ImageData img = (ImageData) object;
				model.copyRndSettings((ImageData) object);
				List<Long> ids = new ArrayList<Long>(1);
				ids.add(img.getId());
				view.reloadThumbnails(ids);
				
				//improve code to speed it up
				List l = model.getSelectedBrowser().getSelectedDataObjects();
				Collection toUpdate;
				if (l.size() > 1) toUpdate = l;
				else toUpdate = model.getDisplayedImages();
				if (toUpdate != null) {
					PasteRndSettingsCmd cmd = new PasteRndSettingsCmd(model, 
							PasteRndSettingsCmd.PASTE, toUpdate);
					cmd.execute();
				}
			} else if (object instanceof Object[]) {
				Object[] objects = (Object[]) object;
				WellSampleData wsd = (WellSampleData) objects[0];
				WellData well = (WellData) objects[1];
				ImageData img = wsd.getImage();
				model.copyRndSettings(img);
				List<Long> ids = new ArrayList<Long>(1);
				ids.add(img.getId());
				view.reloadThumbnails(ids);
				ids = new ArrayList<Long>(1);
				ids.add(well.getPlate().getId());
				model.pasteRndSettings(ids, PlateData.class);
			}
		} else if (JXTaskPaneContainerSingle.SELECTED_TASKPANE_PROPERTY.equals(
				name)) {
			handleTaskPaneSelection((JXTaskPane) pce.getNewValue());
		} else if (MetadataViewer.GENERATE_FIGURE_PROPERTY.equals(name)) {
			Object object = pce.getNewValue();
			if (!(object instanceof FigureParam)) return;
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			IconManager icons = IconManager.getInstance();
			Icon icon = icons.getIcon(IconManager.SPLIT_VIEW_FIGURE_22);
			FigureActivityParam activity;
			List<Long> ids = new ArrayList<Long>();
			Iterator i;
			DataObject obj;
			FigureParam param = (FigureParam) object;
			Collection l;
			if (param.isSelectedObjects()) {
				Browser b = model.getSelectedBrowser();
				if (b != null) l = b.getSelectedDataObjects();
				else {
				    l = new ArrayList<DataObject>();
	                Collection<DataObject> nodes = model.getSelectedObjectsFromBrowser();
	                if (nodes != null) {
	                    l.addAll(nodes);
	                }
				}
			} else {
				l = model.getDisplayedImages();
			}
			if (CollectionUtils.isEmpty(l)) return;
			Class klass = null;
			Object p = null;
			if (param.getIndex() == FigureParam.THUMBNAILS) {
				Browser browser = model.getSelectedBrowser();
				if (browser != null) {
					TreeImageDisplay[] nodes = browser.getSelectedDisplays();
					if (nodes != null && nodes.length > 0) {
						TreeImageDisplay node = nodes[0];
						Object ho = node.getUserObject();
						TreeImageDisplay pNode;
						if (ho instanceof DatasetData) {
							klass = ho.getClass();
							p = ho;
						} else if (ho instanceof ImageData) {
							klass = ho.getClass();
							pNode = node.getParentDisplay();
							if (pNode != null) {
								p = pNode.getUserObject();
								if (!(p instanceof DatasetData)) p = null;
							}
							if (p == null) p = ho;
						}
						if (p != null) param.setAnchor((DataObject) p);
					}
				}
			}
			
			i = l.iterator();
			int n = 0;
			List<Long> groupIds = new ArrayList<Long>();
			boolean canRun = true;
			while (i.hasNext()) {
				obj = (DataObject) i.next();
				if (groupIds.size() == 0)
					groupIds.add(obj.getGroupId());
				if (groupIds.contains(obj.getGroupId())) {
					ids.add(obj.getId());
					if (n == 0) p = obj;
					n++;
				} else {
					canRun = false;
					break;
				}
			}
			if (!canRun) {
				un.notifyInfo("Script", "You can run the script only\non " +
						"objects from the same group");
				return;
			}
			if (ids.size() == 0) return;
			// not set
			if (param.getIndex() != FigureParam.THUMBNAILS) 
				param.setAnchor((DataObject) p);

			activity = new FigureActivityParam(object, ids, klass,
					FigureActivityParam.SPLIT_VIEW_FIGURE);
			activity.setIcon(icon);
			un.notifyActivity(new SecurityContext(groupIds.get(0)), activity);
		} else if (MetadataViewer.HANDLE_SCRIPT_PROPERTY.equals(name)) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			ScriptActivityParam p = (ScriptActivityParam) pce.getNewValue();
			int index = p.getIndex();
			ScriptObject script = p.getScript();
			if (index == ScriptActivityParam.VIEW) {
				Environment env = (Environment) 
				TreeViewerAgent.getRegistry().lookup(LookupNames.ENV);
				String path = env.getOmeroFilesHome();
				path += File.separator+script.getName();
				File f = new File(path);
				DownloadAndLaunchActivityParam activity = new DownloadAndLaunchActivityParam(
						p.getScript().getScriptID(), 
						DownloadAndLaunchActivityParam.ORIGINAL_FILE, f, null);

				un.notifyActivity(model.getSecurityContext(), activity);
			} else if (index == ScriptActivityParam.DOWNLOAD) {
				downloadScript(p);
			}
		} else if (OpenWithDialog.OPEN_DOCUMENT_PROPERTY.equals(name)) {
			ApplicationData data = (ApplicationData) pce.getNewValue();
			//Register 
			if (data == null) return;
			String format = view.getObjectMimeType();
			//if (format == null) return;
			if (format != null)	
				TreeViewerFactory.register(data, format);
			model.openWith(data);
		} else if (DataBrowser.OPEN_EXTERNAL_APPLICATION_PROPERTY.equals(name)) {
			model.openWith((ApplicationData) pce.getNewValue());
		} else if (AdminDialog.CREATE_ADMIN_PROPERTY.equals(name)) {
			AdminObject object = (AdminObject) pce.getNewValue();
			model.administrate(object);
		} else if (MetadataViewer.REGISTER_PROPERTY.equals(name)) {
			model.register((DataObjectRegistration) pce.getNewValue());
		} else if (MetadataViewer.RESET_PASSWORD_PROPERTY.equals(name)) {
			model.resetPassword((String) pce.getNewValue());
		} else if (MetadataViewer.UPLOAD_SCRIPT_PROPERTY.equals(name)) {
			TreeViewerAction action = getAction(UPLOAD_SCRIPT);
			action.actionPerformed(new ActionEvent(this, 1, ""));
		} else if (SelectionWizard.SELECTED_ITEMS_PROPERTY.equals(name)) {
			Map m = (Map) pce.getNewValue();
			if (m == null || m.size() != 1) return;
			Entry entry;
			Iterator i = m.entrySet().iterator();
			Class klass;
			TreeImageDisplay node;
			Collection<ExperimenterData> list;
			Object uo;
			AdminObject object;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				klass = (Class) entry.getKey();
				if (ExperimenterData.class.equals(klass)) {
					list = (Collection<ExperimenterData>) entry.getValue();
					node = model.getSelectedBrowser().getLastSelectedDisplay();
					if (node != null) {
						uo = node.getUserObject();
						if (uo instanceof GroupData) {
							object = new AdminObject((GroupData) uo, list);
							model.administrate(object);
						}
					}
				}
			}
		} else if (DataBrowser.SET__OWNER_RND_SETTINGS_PROPERTY.equals(name)) {
			Object data = pce.getNewValue();
			PasteRndSettingsCmd cmd;
			if (data instanceof Collection) 
				cmd = new PasteRndSettingsCmd(model,
						PasteRndSettingsCmd.SET_OWNER, (Collection) data);
			else cmd = new PasteRndSettingsCmd(model,
					PasteRndSettingsCmd.SET_OWNER);
			cmd.execute();
		} else if (ScriptingDialog.RUN_SELECTED_SCRIPT_PROPERTY.equals(name)) {
			handleScript((ScriptObject) pce.getNewValue(),
					ScriptActivityParam.RUN);
		} else if (ScriptingDialog.DOWNLOAD_SELECTED_SCRIPT_PROPERTY.equals(
				name)) {
			Object value = pce.getNewValue();
			if (value instanceof ScriptObject)
				handleScript((ScriptObject) value, 
						ScriptActivityParam.DOWNLOAD);
			else if (value instanceof String) {
				ScriptObject script = view.getScriptFromName((String) value);
				if (script != null)
					handleScript(script, ScriptActivityParam.DOWNLOAD);
			}
		} else if (ScriptingDialog.VIEW_SELECTED_SCRIPT_PROPERTY.equals(name)) {
			Object value = pce.getNewValue();
			if (value instanceof ScriptObject)
				handleScript((ScriptObject) value, ScriptActivityParam.VIEW);
			else if (value instanceof String) {
				ScriptObject script = view.getScriptFromName((String) value);
				if (script != null)
					handleScript(script, ScriptActivityParam.VIEW);
			}
		} else if (TreeViewer.SCRIPTS_LOADING_PROPERTY.equals(name)) {
			view.setScriptsLoadingStatus(true);
		} else if (TreeViewer.SCRIPTS_LOADED_PROPERTY.equals(name)) {
			view.setScriptsLoadingStatus(false);
		} else if (
				DataBrowser.SELECTED_DATA_BROWSER_NODES_DISPLAY_PROPERTY.equals(
						name)) {
			model.setSelectedNodes(pce.getNewValue());
		} else if (TreeViewer.GROUP_CHANGED_PROPERTY.equals(name)) {
			view.setPermissions();
		} else if (MacOSMenuHandler.QUIT_APPLICATION_PROPERTY.equals(name)) {
			Action a = getAction(EXIT);
			ActionEvent event = 
				new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");
			a.actionPerformed(event);
		} else if (GroupManagerDialog.GROUP_SWITCH_PROPERTY.equals(name)) {
			List<GroupData> groups = (List<GroupData>) pce.getNewValue();
			if (groups.size() == 0) {
				UserNotifier un = 
					TreeViewerAgent.getRegistry().getUserNotifier();
				un.notifyInfo(GroupManagerDialog.TITLE, "At least one group " +
						"must be selected.");
				return;
			}
			model.setUserGroup(groups);
		} else if (DataBrowser.ACTIVATE_USER_PROPERTY.equals(name)) {
		    ExperimenterData exp = (ExperimenterData) pce.getNewValue();
		    if (exp != null) model.activateUser(exp);
		} else if (DataBrowser.RESET_PASSWORD_PROPERTY.equals(name)) {
            getAction(RESET_PASSWORD).actionPerformed(
                    new ActionEvent(pce.getNewValue(), 0, "Reset Password"));
        }
	}
	
	/**
	 * Reacts to state changes in the {@link TreeViewer} and in the
	 * {@link Browser}.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent ce)
	{
		Browser browser = model.getSelectedBrowser();
		if (browser != null) {
			switch (browser.getState()) {
				case Browser.BROWSING_DATA:
					loadingWindow.setStatus(TreeViewer.LOADING_TITLE);
					UIUtilities.centerAndShow(loadingWindow);
					return;
				case Browser.READY:
					loadingWindow.setVisible(false);
					break;
			}
		}
		
		switch (model.getState()) {
			case TreeViewer.DISCARDED:
				view.closeViewer();
				break;
			case TreeViewer.LOADING_DATA:
				view.setStatus(TreeViewer.LOADING_TITLE, false);
				view.setStatusIcon(true);
				view.onStateChanged(false);
				break;
			case TreeViewer.SAVE:
				view.setStatus(TreeViewer.SAVING_TITLE, false);
				view.setStatusIcon(true);
				view.onStateChanged(false);
				break;
			case TreeViewer.READY:
			case TreeViewer.LOADING_SELECTION:
				loadingWindow.setVisible(false);
				view.setStatus(null, true);
				view.setStatusIcon(false);
				view.onStateChanged(true);
				view.requestFocus();
				break;  
			case TreeViewer.SETTINGS_RND:
				UIUtilities.centerAndShow(loadingWindow);
		}
	}

	/**
	 * Refreshes the renderer.
	 * @see WindowFocusListener#windowGainedFocus(WindowEvent)
	 */
	public void windowGainedFocus(WindowEvent e)
	{
		view.refreshRenderer();
	}

	/**
	 * Required by the I/F but no-operation implementation in our case.
	 * @see WindowFocusListener#windowLostFocus(WindowEvent)
	 */
	public void windowLostFocus(WindowEvent e) {}

}
