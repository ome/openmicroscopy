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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ActivationAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.AddAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.AnnotateChildrenAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.BrowseImageCategoriesAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserSelectionAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ClassifierAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ClassifyAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ClassifyChildrenAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ClearAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CopyAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CopyRndSettingsAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CreateAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CreateTopContainerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CutAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.DeleteAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ExitApplicationAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.FinderAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ManagerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.PasteAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.PasteRndSettingsAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RefreshExperimenterData;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RefreshTreeAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RemoveExperimenterNode;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ResetRndSettingsAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RollOverAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SearchAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SwitchUserAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.TreeViewerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ViewAction;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.util.AddExistingObjectsDialog;
import org.openmicroscopy.shoola.agents.util.DataHandler;
import org.openmicroscopy.shoola.agents.util.tagging.view.Tagger;
import org.openmicroscopy.shoola.agents.util.ui.UserManagerDialog;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;


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

	/** Identifies the <code>View action</code> in the Edit menu. */
	static final Integer	VIEW = new Integer(1);

	/** Identifies the <code>Create object action</code> in the File menu. */
	static final Integer	CREATE_OBJECT = new Integer(3);

	/** Identifies the <code>Copy object action</code> in the Edit menu. */
	static final Integer	COPY_OBJECT = new Integer(4);

	/** Identifies the <code>Paste object action</code> in the Edit menu. */
	static final Integer	PASTE_OBJECT = new Integer(5);

	/** Identifies the <code>Delete object action</code> in the Edit menu. */
	static final Integer	DELETE_OBJECT = new Integer(6);

	/** 
	 * Identifies the <code>Hierarchy Explorer</code> action in the View menu. 
	 */
	static final Integer	HIERARCHY_EXPLORER = new Integer(7);

	/** 
	 * Identifies the <code>Category Explorer</code> action in the View menu.
	 */
	static final Integer	CATEGORY_EXPLORER = new Integer(8);

	/** Identifies the <code>Images Explorer</code> action in the View menu. */
	static final Integer	IMAGES_EXPLORER = new Integer(9);

	/** Identifies the <code>Find action </code>in the Edit menu. */
	static final Integer	FIND = new Integer(10);

	/** Identifies the <code>Classify action</code> in the Edit menu. */
	static final Integer    CLASSIFY = new Integer(11);

	/** Identifies the <code>Declassify action</code> in the Edit menu. */
	static final Integer    DECLASSIFY = new Integer(12);

	/** Identifies the <code>Exit action</code> in the File menu. */
	static final Integer    EXIT = new Integer(14);

	/** Identifies the <code>Clear action</code> in the Edit menu. */
	static final Integer    CLEAR = new Integer(15);

	/** Identifies the <code>Add action</code> in the Edit menu. */
	static final Integer    ADD_OBJECT = new Integer(16);

	/** 
	 * Identifies the <code>Create top container action</code> in the 
	 * File menu.
	 */
	static final Integer    CREATE_TOP_CONTAINER = new Integer(17);

	/** 
	 * Identifies the <code>Refresh tree action</code> in the 
	 * File menu.
	 */
	static final Integer    REFRESH_TREE = new Integer(18);

	/** 
	 * Identifies the <code>Manager</code> in the 
	 * File menu.
	 */
	static final Integer    MANAGER = new Integer(19);

	/** 
	 * Identifies the <code>Classifier action</code> in the 
	 * File menu.
	 */
	static final Integer    CLASSIFIER = new Integer(20);

	/** 
	 * Identifies the <code>Cut action</code> in the 
	 * Edit menu.
	 */
	static final Integer    CUT_OBJECT = new Integer(21);

	/** 
	 * Identifies the <code>Activation action</code> in the 
	 * Edit menu.
	 */
	static final Integer    ACTIVATION = new Integer(22);

	/** 
	 * Identifies the <code>Switch user action</code> in the 
	 * File menu.
	 */
	static final Integer    SWITCH_USER = new Integer(23);

	/** Identifies the <code>Annotate children action</code>. */
	static final Integer    ANNOTATE_CHILDREN = new Integer(24);

	/** Identifies the <code>Classify children action</code>. */
	static final Integer    CLASSIFY_CHILDREN = new Integer(25);

	/** Identifies the <code>Roll over action</code>. */
	static final Integer    ROLL_OVER = new Integer(26);

	/** Identifies the <code>Remove from display action</code>. */
	static final Integer    REMOVE_FROM_DISPLAY = new Integer(27);

	/** Identifies the <code>Refresh experimenter action</code>. */
	static final Integer    REFRESH_EXPERIMENTER = new Integer(29);

	/** Identifies the <code>Browse categories action</code>. */
	static final Integer    BROWSE_CATEGORIES = new Integer(30);

	/** Identifies the <code>Paste rendering settings action</code>. */
	static final Integer    PASTE_RND_SETTINGS = new Integer(31);

	/** Identifies the <code>Copy rendering settings action</code>. */
	static final Integer    COPY_RND_SETTINGS = new Integer(32);
	
	/** Identifies the <code>Reset rendering settings action</code>. */
	static final Integer    RESET_RND_SETTINGS = new Integer(33);
	
	/** Identifies the <code>Search action</code>. */
	static final Integer    SEARCH = new Integer(34);
	
	/** 
	 * Reference to the {@link TreeViewer} component, which, in this context,
	 * is regarded as the Model.
	 */
	private TreeViewer      				model;

	/** Reference to the View. */
	private TreeViewerWin   				view;

	/** Maps actions ids onto actual <code>Action</code> object. */
	private Map<Integer, TreeViewerAction>	actionsMap;

	/** The tabbed pane listener. */
	private ChangeListener  				tabsListener;

	/** Helper method to create all the UI actions. */
	private void createActions()
	{
		actionsMap.put(VIEW, new ViewAction(model));
		actionsMap.put(CREATE_OBJECT, new CreateAction(model));
		actionsMap.put(COPY_OBJECT, new CopyAction(model));
		actionsMap.put(DELETE_OBJECT, new DeleteAction(model));
		actionsMap.put(PASTE_OBJECT, new PasteAction(model));
		actionsMap.put(HIERARCHY_EXPLORER, 
				new BrowserSelectionAction(model, Browser.PROJECT_EXPLORER));
		actionsMap.put(CATEGORY_EXPLORER, 
				new BrowserSelectionAction(model, Browser.CATEGORY_EXPLORER));
		actionsMap.put(IMAGES_EXPLORER, 
				new BrowserSelectionAction(model, Browser.IMAGES_EXPLORER));
		actionsMap.put(FIND,  new FinderAction(model));
		actionsMap.put(CLASSIFY, new ClassifyAction(model,
				ClassifyAction.CLASSIFY));
		actionsMap.put(DECLASSIFY, new ClassifyAction(model, 
				ClassifyAction.DECLASSIFY));
		actionsMap.put(CLEAR, new ClearAction(model));
		actionsMap.put(EXIT, new ExitApplicationAction(model));
		actionsMap.put(ADD_OBJECT,  new AddAction(model));
		actionsMap.put(CREATE_TOP_CONTAINER,  
				new CreateTopContainerAction(model));
		actionsMap.put(REFRESH_TREE, new RefreshTreeAction(model));
		actionsMap.put(CLASSIFIER, new ClassifierAction(model));
		actionsMap.put(MANAGER, new ManagerAction(model));
		actionsMap.put(CUT_OBJECT, new CutAction(model));
		actionsMap.put(ACTIVATION, new ActivationAction(model));
		actionsMap.put(SWITCH_USER, new SwitchUserAction(model));
		actionsMap.put(ANNOTATE_CHILDREN, new AnnotateChildrenAction(model));
		actionsMap.put(CLASSIFY_CHILDREN, new ClassifyChildrenAction(model));
		actionsMap.put(ROLL_OVER, new RollOverAction(model));
		actionsMap.put(REMOVE_FROM_DISPLAY, new RemoveExperimenterNode(model));
		actionsMap.put(REFRESH_EXPERIMENTER, 
				new RefreshExperimenterData(model));
		actionsMap.put(BROWSE_CATEGORIES, 
				new BrowseImageCategoriesAction(model));
		actionsMap.put(PASTE_RND_SETTINGS, new PasteRndSettingsAction(model));
		actionsMap.put(COPY_RND_SETTINGS, new CopyRndSettingsAction(model));
		actionsMap.put(RESET_RND_SETTINGS, new ResetRndSettingsAction(model));
		actionsMap.put(SEARCH, new SearchAction(model));
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
	 * Resets the selected node. 
	 * 
	 * @param d The node to reset.
	 */
	private void resetSelectedDisplay(TreeImageDisplay d)
	{
		Browser b = model.getSelectedBrowser();
		if (b != null) {
			//b.setSelectedDisplay(null);
			b.setSelectedDisplay(d);
		}
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
	}

	/**
	 * Returns the {@link ChangeListener} attached to the tabbed pane,
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
	 * @param tabs
	 */
	void attachUIListeners(JTabbedPane tabs)
	{
		//Register listener
		tabs.addChangeListener(getTabbedListener());
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
		} else if (name.equals(Browser.SELECTED_DISPLAY_PROPERTY)) {
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
		} else if (name.equals(Tagger.TAGGED_PROPERTY)) {
			Map browsers = model.getBrowsers();
			Iterator i = browsers.values().iterator();

			while (i.hasNext()) 
				((Browser) i.next()).refreshTree();
		} else if (name.equals(DataHandler.ANNOTATED_PROPERTY) ||
				name.equals(DataHandler.CLASSIFIED_PROPERTY) ) {
			
			if (view.getDataHandler() == null) return;
			view.discardDataHandler();
			Map browsers = model.getBrowsers();
			Iterator i = browsers.values().iterator();

			while (i.hasNext()) 
				((Browser) i.next()).refreshTree();

		} else if (UserManagerDialog.USER_SWITCH_PROPERTY.equals(name)) {
			Map m = (Map) pce.getNewValue();
			Iterator i = m.keySet().iterator();
			Long groupID;
			ExperimenterData d;
			while (i.hasNext()) {
				groupID = (Long) i.next();
				d = (ExperimenterData) m.get(groupID);
				model.setHierarchyRoot(groupID, d);
				break;
			}
		} else if (UserManagerDialog.NO_USER_SWITCH_PROPERTY.equals(name)) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("User Selection", "Please select a user first.");
		
		/*
		 } 
		else if (ProfileEditor.EXPERIMENTER_CHANGED_PROPERTY.equals(name)) {
			Map browsers = model.getBrowsers();
			Iterator i = browsers.values().iterator();

			while (i.hasNext()) 
				((Browser) i.next()).refreshExperimenter();
				*/
		} else if (EditorDialog.CREATE_PROPERTY.equals(name)) {
			DataObject data = (DataObject) pce.getNewValue();
			model.createObject(data);
		} else if (MetadataViewer.ON_DATA_SAVE_PROPERTY.equals(name)) {
			DataObject data = (DataObject) pce.getNewValue();
			if (data != null)
				model.onDataObjectSave(data, TreeViewer.UPDATE_OBJECT);
		}
	}

	/**
	 * Reacts to state changes in the {@link TreeViewer} and in the
	 * {@link Browser}.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent ce)
	{
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
			view.setStatus(null, true);
			view.setStatusIcon(false);
			view.onStateChanged(true);
			break;  
		}
	}

}
