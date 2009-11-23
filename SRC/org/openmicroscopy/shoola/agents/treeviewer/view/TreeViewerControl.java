/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerControl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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

//Third-party libraries

//Application-internal dependencies
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.treeviewer.ImportManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ActivationAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.AddAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserSelectionAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ClearAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CopyAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CopyRndSettingsAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CreateAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CreateTopContainerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CutAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.DeleteAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.EditorAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ExitApplicationAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.FinderAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ImportAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ImporterVisibilityAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.InspectorVisibilityAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ManagerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.NewObjectAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.PasteAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.PasteRndSettingsAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RefreshExperimenterData;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RefreshTreeAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RemoveExperimenterNode;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ResetRndSettingsAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RollOverAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SearchAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SetRndSettingsAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SwitchUserAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.TaggingAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.TreeViewerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.BrowseContainerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ViewImageAction;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.CopyCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.CutCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.DeleteCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.PasteCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.PasteRndSettingsCmd;
import org.openmicroscopy.shoola.agents.treeviewer.util.AddExistingObjectsDialog;
import org.openmicroscopy.shoola.agents.treeviewer.util.GenericDialog;
import org.openmicroscopy.shoola.agents.treeviewer.util.ImportDialog;
import org.openmicroscopy.shoola.agents.treeviewer.util.ImportableObject;
import org.openmicroscopy.shoola.agents.util.finder.Finder;
import org.openmicroscopy.shoola.agents.util.ui.EditorDialog;
import org.openmicroscopy.shoola.agents.util.ui.UserManagerDialog;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.JXTaskPaneContainerSingle;
import org.openmicroscopy.shoola.util.ui.LoadingWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PlateData;
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
 	implements ChangeListener, PropertyChangeListener
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
	
	/** Identifies the <code>Launch Editor</code> in the menu. */
	static final Integer    EDITOR_NO_SELECTION = Integer.valueOf(43);
	
	/** Identifies the <code>Files Explorer</code> action in the View menu. */
	static final Integer	FILES_EXPLORER = Integer.valueOf(44);
	
	/** Identifies the <code>Create tag set</code> in the File menu. */
	static final Integer    CREATE_TOP_TAG_SET = Integer.valueOf(45);
	
	/** Identifies the <code>Create tag sets or tags</code> in the menu. */
	static final Integer    NEW_TAG_OBJECT = Integer.valueOf(46);
	
	/** Identifies the <code>Add or remove tag</code> in the menu. */
	static final Integer    TAGGING = Integer.valueOf(47);
	
	/** Identifies the <code>Launch Editor</code> in the menu. */
	static final Integer    EDITOR_WITH_SELECTION = Integer.valueOf(48);
	
	/** Identifies the <code>Launch Editor</code> in the menu. */
	static final Integer    EDITOR_NEW_WITH_SELECTION = Integer.valueOf(49);
	
	/** Identifies the <code>Show/hide inspector</code> in the menu. */
	static final Integer    INSPECTOR = Integer.valueOf(50);
	
	/** Identifies the <code>Show/hide importer</code> in the menu. */
	static final Integer    IMPORTER = Integer.valueOf(51);
	
	/** 
	 * Identifies the <code>File System Explorer</code> action in the View menu.
	 */
	static final Integer    FILE_SYSTEM_EXPLORER = Integer.valueOf(52);
	
	/** Identifies the <code>Import</code> in the menu. */
	static final Integer    IMPORT = Integer.valueOf(53);
	
	/** Identifies the <code>Browse w/o thumbnails</code> in the menu. */
	static final Integer    BROWSE_NO_THUMBNAILS = Integer.valueOf(54);
	
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
				model.setSelectedBrowser(null);
			else {
				if (pane instanceof TaskPaneBrowser) {
					TaskPaneBrowser p = (TaskPaneBrowser) pane;
					if (p.getBrowser() != null)
						model.setSelectedBrowser(p.getBrowser());
					else model.showSearch();
				} else {
					model.setSelectedBrowser(null);
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
		actionsMap.put(COPY_OBJECT, new CopyAction(model));
		actionsMap.put(DELETE_OBJECT, new DeleteAction(model));
		actionsMap.put(PASTE_OBJECT, new PasteAction(model));
		actionsMap.put(SCREENS_EXPLORER, 
				new BrowserSelectionAction(model, Browser.SCREENS_EXPLORER));
		actionsMap.put(HIERARCHY_EXPLORER, 
				new BrowserSelectionAction(model, Browser.PROJECT_EXPLORER));
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
		actionsMap.put(CUT_OBJECT, new CutAction(model));
		actionsMap.put(ACTIVATION, new ActivationAction(model));
		actionsMap.put(SWITCH_USER, new SwitchUserAction(model));
		actionsMap.put(ROLL_OVER, new RollOverAction(model));
		actionsMap.put(REMOVE_FROM_DISPLAY, new RemoveExperimenterNode(model));
		actionsMap.put(REFRESH_EXPERIMENTER, 
				new RefreshExperimenterData(model));
		actionsMap.put(PASTE_RND_SETTINGS, new PasteRndSettingsAction(model));
		actionsMap.put(COPY_RND_SETTINGS, new CopyRndSettingsAction(model));
		actionsMap.put(RESET_RND_SETTINGS, new ResetRndSettingsAction(model));
		actionsMap.put(SEARCH, new SearchAction(model));
		actionsMap.put(SET_RND_SETTINGS, new SetRndSettingsAction(model));
		actionsMap.put(CREATE_TOP_SCREEN, 
				new CreateTopContainerAction(model, 
						CreateTopContainerAction.SCREEN));
		actionsMap.put(VIEW, new ViewImageAction(model));
		actionsMap.put(NEW_OBJECT, new NewObjectAction(model, 
								NewObjectAction.NEW_CONTAINERS));
		actionsMap.put(EDITOR_NO_SELECTION, new EditorAction(model, 
				EditorAction.NO_SELECTION));
		actionsMap.put(EDITOR_WITH_SELECTION, new EditorAction(model, 
				EditorAction.WITH_SELECTION));
		actionsMap.put(CREATE_TOP_TAG_SET,  
				new CreateTopContainerAction(model, 
						CreateTopContainerAction.TAG_SET));
		actionsMap.put(NEW_TAG_OBJECT, new NewObjectAction(model, 
				NewObjectAction.NEW_TAGS));
		actionsMap.put(TAGGING, new TaggingAction(model));
		actionsMap.put(EDITOR_NEW_WITH_SELECTION, new EditorAction(model, 
				EditorAction.NEW_WITH_SELECTION));
		actionsMap.put(INSPECTOR, new InspectorVisibilityAction(model));
		actionsMap.put(IMPORTER, new ImporterVisibilityAction(model));
		actionsMap.put(IMPORT, new ImportAction(model));
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
		Map browsers = model.getBrowsers();
		Iterator i = browsers.values().iterator();
		Browser browser;
		while (i.hasNext()) {
			browser = (Browser) i.next();
			browser.addPropertyChangeListener(this);
			browser.addChangeListener(this);
		}
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
			 * no-op implementation.
			 * @see MenuListener#menuCanceled(MenuEvent)
			 */ 
			public void menuCanceled(MenuEvent e) {}

			/** 
			 * Required by I/F but not actually needed in our case, 
			 * no-op implementation.
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
			 * no op implementation.
			 * @see MenuKeyListener#menuKeyPressed(MenuKeyEvent)
			 */
			public void menuKeyPressed(MenuKeyEvent e) {}

			/** 
			 * Required by I/F but not actually needed in our case, 
			 * no op implementation.
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
						model.setSelectedBrowser(null);
						return;
					}
					Map browsers = model.getBrowsers();
					Iterator i = browsers.values().iterator();
					boolean selected = false;
					Browser browser;
					while (i.hasNext()) {
						browser = (Browser) i.next();
						if (c.equals(browser.getUI())) {
							model.setSelectedBrowser(browser);
							selected = true;
							break;
						}
					}
					if (!selected) model.setSelectedBrowser(null);
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

	/** Forwards call to the {@link TreeViewer}. */
	void cancel() { model.cancel(); }
	
	/**
	 * Reacts to property changed. 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent pce)
	{
		String name = pce.getPropertyName();
		if (name == null) return;
		if (name.equals(TreeViewer.CANCEL_LOADING_PROPERTY)) {
			Browser browser = model.getSelectedBrowser();
			if (browser != null) browser.cancel();
		} else if (name.equals(Browser.POPUP_MENU_PROPERTY)) {
			Integer c = (Integer) pce.getNewValue();
			Browser browser = model.getSelectedBrowser();
			if (browser != null)
				view.showPopup(c.intValue(), browser.getClickComponent(), 
						browser.getClickPoint());
		} else if (name.equals(Browser.CLOSE_PROPERTY)) {
			Browser browser = (Browser) pce.getNewValue();
			if (browser != null) view.removeBrowser(browser);
		} else if (name.equals(TreeViewer.FINDER_VISIBLE_PROPERTY)) {
			Boolean b = (Boolean) pce.getNewValue();
			if (!b.booleanValue()) {
				model.clearFoundResults();
				model.onComponentStateChange(true);
			}
		} else if (name.equals(TreeViewer.SELECTED_BROWSER_PROPERTY)) {
			Browser  b = model.getSelectedBrowser();
			Iterator i = model.getBrowsers().values().iterator();
			Browser browser;
			while (i.hasNext()) {
				browser = (Browser) i.next();
				browser.setSelected(browser.equals(b));
			}
		} else if (name.equals(Browser.SELECTED_TREE_NODE_DISPLAY_PROPERTY)) {
			model.onSelectedDisplay();
			view.updateMenuItems();
		} else if (name.equals(TreeViewer.HIERARCHY_ROOT_PROPERTY)) {
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
		} else if (name.equals(
				AddExistingObjectsDialog.EXISTING_ADD_PROPERTY)) {
			model.addExistingObjects((Set) pce.getNewValue());
		} else if (UserManagerDialog.USER_SWITCH_PROPERTY.equals(name)) {
			Map m = (Map) pce.getNewValue();
			Iterator i = m.entrySet().iterator();
			Long groupID;
			ExperimenterData d;
			Entry entry;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				groupID = (Long) entry.getKey();
				d = (ExperimenterData) entry.getValue();
				model.setHierarchyRoot(groupID, d);
				break;
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
			 model.getSelectedBrowser().refreshLoggedExperimenterData();
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
				cmd = new PasteRndSettingsCmd(model, PasteRndSettingsCmd.SET,
						(Collection) data);
			else cmd = new PasteRndSettingsCmd(model, PasteRndSettingsCmd.SET);
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
		} else if (MetadataViewer.EXPERIMENTER_UPDATED_PROPERTY.equals(name)) {
			Map browsers = model.getBrowsers();
			Set set = browsers.entrySet();
			Entry entry;
			Iterator i = set.iterator();
			Browser browser;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				browser = (Browser) entry.getValue();
				browser.refreshExperimenter();
			}
		} else if (DataBrowser.TAG_WIZARD_PROPERTY.equals(name)) {
			model.showTagWizard();
		} else if (DataBrowser.CREATE_NEW_EXPERIMENT_PROPERTY.equals(name)) {
			model.openEditorFile(TreeViewer.NEW_WITH_SELECTION);
		} else if (DataBrowser.FIELD_SELECTED_PROPERTY.equals(name)) {
			model.setSelectedField(pce.getNewValue());
		} else if (ImportManager.VIEW_IMAGE_PROPERTY.equals(name)) {
			ImageData image = (ImageData) pce.getNewValue();
			EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
			ViewImage evt = new ViewImage(image, view.getBounds());
			bus.post(evt);
		} else if (ImportManager.SEND_FILES_PROPERTY.equals(name)) {
			Map files = (Map) pce.getNewValue();
			if (files != null && files.size() > 0) {
				UserNotifier un = 
					TreeViewerAgent.getRegistry().getUserNotifier();
				un.notifyError("Import failures", 
						"Submit the files to development team", "", files);
				//Notifications dialog.
			}
		} else if (Browser.FILE_FORMATS_PROPERTY.equals(name)) {
			view.showSupportedFileFormats();
		} else if (MetadataViewer.RENDER_THUMBNAIL_PROPERTY.equals(name)) {
			long imageID = ((Long) pce.getNewValue()).longValue();
			List<Long> ids = new ArrayList<Long>(1);
			ids.add(imageID);
			view.reloadThumbnails(ids);
		} else if (MetadataViewer.APPLY_SETTINGS_PROPERTY.equals(name)) {
			Object object = pce.getNewValue();
			if (object instanceof ImageData) {
				model.copyRndSettings((ImageData) object);
				//improve code to speed it up
				List l = model.getSelectedBrowser().getSelectedDataObjects();
				PasteRndSettingsCmd cmd = null;
				Collection toUpdate;
				if (l.size() > 1) toUpdate = l;
				else toUpdate = model.getDisplayedImages();
				if (toUpdate != null) {
					cmd = new PasteRndSettingsCmd(model, 
							PasteRndSettingsCmd.PASTE, toUpdate);
					cmd.execute();
				}
			} else if (object instanceof Object[]) {
				Object[] objects = (Object[]) object;
				WellSampleData wsd = (WellSampleData) objects[0];
				WellData well = (WellData) objects[1];
				model.copyRndSettings(wsd.getImage());
				List<Long> ids = new ArrayList<Long>(1);
				ids.add(well.getPlate().getId());
				model.pasteRndSettings(ids, PlateData.class);
			}
		} else if (ImportDialog.IMPORT_PROPERTY.equals(name)) {
			ImportableObject object = (ImportableObject) pce.getNewValue();
			model.importFiles(object);
		} else if (JXTaskPaneContainerSingle.SELECTED_TASKPANE_PROPERTY.equals(
				name)) {
			handleTaskPaneSelection((JXTaskPane) pce.getNewValue());
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

}
