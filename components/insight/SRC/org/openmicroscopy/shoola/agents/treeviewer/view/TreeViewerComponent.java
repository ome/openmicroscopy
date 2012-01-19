/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerComponent
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
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import omero.model.OriginalFile;

import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserFactory;
import org.openmicroscopy.shoola.agents.events.SaveData;
import org.openmicroscopy.shoola.agents.events.editor.EditFileEvent;
import org.openmicroscopy.shoola.agents.events.editor.ShowEditorEvent;
import org.openmicroscopy.shoola.agents.events.iviewer.CopyRndSettings;
import org.openmicroscopy.shoola.agents.events.iviewer.RndSettingsCopied;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.agents.events.treeviewer.BrowserSelectionEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.CopyItems;
import org.openmicroscopy.shoola.agents.events.treeviewer.DeleteObjectEvent;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerFactory;
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.UpdateVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.finder.ClearVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.finder.Finder;
import org.openmicroscopy.shoola.agents.treeviewer.util.AdminDialog;
import org.openmicroscopy.shoola.agents.treeviewer.util.GenericDialog;
import org.openmicroscopy.shoola.agents.treeviewer.util.NotDeletedObjectDialog;
import org.openmicroscopy.shoola.agents.treeviewer.util.OpenWithDialog;
import org.openmicroscopy.shoola.agents.util.browser.ContainerFinder;
import org.openmicroscopy.shoola.agents.util.browser.NodesFinder;
import org.openmicroscopy.shoola.agents.util.browser.TreeFileSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeViewerTranslator;
import org.openmicroscopy.shoola.agents.util.ui.EditorDialog;
import org.openmicroscopy.shoola.agents.util.ui.ScriptingDialog;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.DataObjectRegistration;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.agents.util.ui.UserManagerDialog;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.ExitApplication;
import org.openmicroscopy.shoola.env.data.events.SwitchUserGroup;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.model.DeleteActivityParam;
import org.openmicroscopy.shoola.env.data.model.DownloadActivityParam;
import org.openmicroscopy.shoola.env.data.model.OpenActivityParam;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.ui.ActivityComponent;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.FileData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.MultiImageData;
import pojos.PermissionData;
import pojos.PlateData;
import pojos.PlateAcquisitionData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.WellSampleData;

/** 
 * Implements the {@link TreeViewer} interface to provide the functionality
 * required of the tree viewer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @see org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerModel
 * @see org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerWin
 * @see org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerControl
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class TreeViewerComponent
 	extends AbstractComponent
 	implements TreeViewer
{
  
	/** The Model sub-component. */
	private TreeViewerModel     model;

	/** The Controller sub-component. */
	private TreeViewerControl   controller;

	/** The View sub-component. */
	private TreeViewerWin       view;
	
	/** The dialog presenting the list of available users. */
	private UserManagerDialog	switchUserDialog;
	
	/** The dialog displaying the selected script.*/
	private ScriptingDialog     scriptDialog;
	
	/**
	 * Returns the name corresponding the specified object.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 */
	private String getObjectType(Object object)
	{
		if (object instanceof DatasetData) return "dataset";
		else if (object instanceof ProjectData) return "project";
		else if (object instanceof GroupData) return "group";
		else if (object instanceof ScreenData) return "screen";
		else if (object instanceof PlateData) return "plate";
		else if (object instanceof ImageData) return "image";
		else if (object instanceof TagAnnotationData) {
			TagAnnotationData tag = (TagAnnotationData) object;
			if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(tag.getNameSpace()))
				return "tagSet";
			return "tag";
		}
		return "item";
	}
	
	/**
	 * Downloads the files.
	 * 
	 * @param folder 	The folder to save the file into.
	 * @param override 	Pass <code>true</code> to keep the name of the 
	 * 					file, <code>false</code> otherwise.
	 * @param fa 		The file to download.
	 * @param data	 	The application to open the document with or 
	 * 				 	<code>null</code>.
	 */
	private void downloadFile(File folder, boolean override, 
			FileAnnotationData fa, ApplicationData data)
	{
		UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
		OriginalFile f = (OriginalFile) fa.getContent();
		IconManager icons = IconManager.getInstance();
		DownloadActivityParam activity = new DownloadActivityParam(f,
				folder, icons.getIcon(IconManager.DOWNLOAD_22));
		if (override)
			activity.setFileName(fa.getFileName());
		activity.setApplicationData(data);
		un.notifyActivity(activity);
	}
	
	/** 
	 * Displays the user groups.
	 * 
	 * @param groups The groups the current user is a member of.
	 * @param location The location of the mouse pressed.
	 */
	private void displayUserGroups(Set groups, Point location)
	{
		JFrame f = (JFrame) TreeViewerAgent.getRegistry().getTaskBar();
		IconManager icons = IconManager.getInstance();
		switchUserDialog = new UserManagerDialog(f, model.getUserDetails(), 
				groups, icons.getIcon(IconManager.OWNER), 
				icons.getIcon(IconManager.OWNER_48));
		switchUserDialog.addPropertyChangeListener(controller);
		switchUserDialog.setDefaultSize();
		UIUtilities.showOnScreen(switchUserDialog, location);
	}
	
	/**
	 * Displays the data browser corresponding to the passed node.
	 * 
	 * @param object 	The object of reference.
	 * @param display	The node to handle.
	 * @param visible	Pass <code>true</code> to display the browser,
	 * 					<code>false</code> otherwise.
	 */
	private void showDataBrowser(Object object, TreeImageDisplay display, 
			boolean visible)
	{
		DataBrowser db = null;
		TreeImageDisplay parent = null;
		Browser browser = model.getSelectedBrowser();
		if (display != null) parent = display.getParentDisplay();
		List list;
		List<ApplicationData> app = null;
		if (display instanceof TreeImageTimeSet) {
			db = DataBrowserFactory.getDataBrowser(display);
			if (db != null) {
				db.setComponentTitle("");
				if (visible) {
					view.removeAllFromWorkingPane();
					view.displayBrowser(db);
				}
			} else {
				if (DataBrowserFactory.hasBeenDiscarded(display)) {
					if (display.isChildrenLoaded() 
							&& display.containsImages()) {
						List l = display.getChildrenDisplay();
        				if (l != null) {
        					Set s = new HashSet();
        					Iterator i = l.iterator();
        					TreeImageDisplay child;
    						//copy the node.
        					while (i.hasNext()) {
        						child = (TreeImageDisplay) i.next();
        						s.add(child.getUserObject());
        					}
    						setLeaves((TreeImageSet) display, s);
    						db = DataBrowserFactory.getDataBrowser(display);
    						list = browser.getSelectedDataObjects();
    						if (list != null && list.size() == 1) {
    							app = TreeViewerFactory.getApplications(
    							model.getObjectMimeType(list.get(0)));
    						}
    						db.setSelectedNodes(list, app);
        				}
					}
				}
			}
			if (db != null) db.setSelectedNodes(null, null);
			return;
		}
		if (object instanceof ImageData) {
			if (display.getParentDisplay() instanceof TreeImageTimeSet) {
				db = DataBrowserFactory.getDataBrowser(
						display.getParentDisplay());
				if (db != null) {
					db.setComponentTitle("");
					if (visible) {
						view.removeAllFromWorkingPane();
						view.displayBrowser(db);
					}
					List<DataObject> nodes = new ArrayList<DataObject>();
            		//nodes.add((DataObject) object);
            		nodes = browser.getSelectedDataObjects();
            		db.setSelectedNodes(nodes, 
            				TreeViewerFactory.getApplications(
							model.getObjectMimeType(object)));
				}
				return;
			}
			if (parent != null) {
				Object ho = parent.getUserObject();
				db = DataBrowserFactory.getDataBrowser(ho);
				DataBrowser mdb = model.getDataViewer();
				if (mdb != db) {
					DataBrowser gdb = DataBrowserFactory.getDataBrowser(
							parent.getParentDisplay().getUserObject());
					if (gdb != null) db = gdb;
				}
				if (db != null) {
					db.setComponentTitle("");
					if (visible) {
						view.removeAllFromWorkingPane();
						view.displayBrowser(db);
					}
					list = browser.getSelectedDataObjects();
					if (list != null && list.size() == 1) {
						app = TreeViewerFactory.getApplications(
								model.getObjectMimeType(list.get(0)));
					}
					db.setSelectedNodes(list, app);
				} else {
					if (DataBrowserFactory.hasBeenDiscarded(ho)) {
						//refresh 
						if (parent.isChildrenLoaded()) {
	        				List l = parent.getChildrenDisplay();
	        				if (l != null) {
	        					Set s = new HashSet();
	        					Iterator i = l.iterator();
	        					if (ho instanceof DatasetData) {
	        						TreeImageDisplay child;
	        						//copy the node.
	            					while (i.hasNext()) {
	            						child = (TreeImageDisplay) i.next();
	            						s.add(child.getUserObject());
	            					}
	        						setLeaves((TreeImageSet) parent, s);
	        						db = DataBrowserFactory.getDataBrowser(ho);
	        						list = browser.getSelectedDataObjects();
	        						if (list != null && list.size() == 1) {
	        							app = TreeViewerFactory.getApplications(
	        							model.getObjectMimeType(list.get(0)));
	        						}
	        						db.setSelectedNodes(list, app);
	        					} else if (ho instanceof GroupData) {
	        						TreeImageDisplay child;
	        						//copy the node.
	            					while (i.hasNext()) {
	            						child = (TreeImageDisplay) i.next();
	            						s.add(child.getUserObject());
	            					}
	        						setLeaves((TreeImageSet) parent, s);
	        						db = DataBrowserFactory.getDataBrowser(ho);
	        						list = browser.getSelectedDataObjects();
	        						db.setSelectedNodes(list, app);
	        					} else if (ho instanceof TagAnnotationData) {
	        						TagAnnotationData tag = 
	        							(TagAnnotationData) ho;
	        						if (tag.getTags() == null) {
	        							TreeImageDisplay child;
	                					while (i.hasNext()) {
	                						child = (TreeImageDisplay) i.next();
	                						s.add(child.getUserObject());
	                					}
	                					setLeaves((TreeImageSet) parent, s);
	                					db = DataBrowserFactory.getDataBrowser(
	                							ho);
	                					list = browser.getSelectedDataObjects();
	                					if (list != null && list.size() == 1) {
	                						app = 
	                						TreeViewerFactory.getApplications(
	                						model.getObjectMimeType(
	                								list.get(0)));
	                					}
	                					db.setSelectedNodes(list, app);
	        						}
	        					} 
	        				}
	        			}
					} else
						showDataBrowser(object, parent.getParentDisplay(), 
								visible);
				}
			} else {
				view.removeAllFromWorkingPane();
			}
		} else if (object instanceof ExperimenterData && 
				browser.getBrowserType() == Browser.ADMIN_EXPLORER) {
			Object ho = null;
			if (display.getParentDisplay() != null) {
				ho = display.getParentDisplay().getUserObject();
				db = DataBrowserFactory.getDataBrowser(ho);
			} else db = null; 
        	if (db != null) {
        		db.setComponentTitle("");
        		if (visible) {
        			view.removeAllFromWorkingPane();
            		view.addComponent(db.getUI(model.isFullScreen()));
        		}
        		List<DataObject> nodes = new ArrayList<DataObject>();
        		nodes.add((DataObject) object);
        		db.setSelectedNodes(nodes, 
        				TreeViewerFactory.getApplications(
						model.getObjectMimeType(object)));
        	}
        } else {
        	TreeImageDisplay[] displayedNodes = browser.getSelectedDisplays();
        	if (displayedNodes != null && displayedNodes.length > 1) {
        		if (object instanceof DatasetData || 
        				object instanceof PlateAcquisitionData || 
        			object instanceof PlateData) {
        			view.removeAllFromWorkingPane();
        			model.setDataViewer(null);
        			return;
        		}
        	}
        	Object p = object;
        	if (object instanceof PlateData) {
        		PlateData plate = (PlateData) object;
        		Set<PlateAcquisitionData> set = plate.getPlateAcquisitions();
        		if (set != null && set.size() == 1) {
        			Iterator<PlateAcquisitionData> k = set.iterator();
        			while (k.hasNext()) {
						p = k.next();
					}
        		}
        	}
        	db = DataBrowserFactory.getDataBrowser(p);
        	if (db != null) {
        		db.setComponentTitle("");
        		if (visible) {
        			view.removeAllFromWorkingPane();
            		view.addComponent(db.getUI(model.isFullScreen()));
        		}
        		if (object instanceof DataObject) {
        			List<DataObject> nodes = new ArrayList<DataObject>();
            		nodes.add((DataObject) object);
            		db.setSelectedNodes(nodes, 
            				TreeViewerFactory.getApplications(
							model.getObjectMimeType(object)));
        		}
        	} else {
        		//depending on object
        		view.removeAllFromWorkingPane();
        		if (display != null) {
        			if (display.isChildrenLoaded()) {
        				List l = display.getChildrenDisplay();
        				if (l != null) {
        					Set s = new HashSet();
        					Iterator i = l.iterator();
        					if (object instanceof DatasetData) {
        						TreeImageDisplay child;
        						//copy the node.
            					while (i.hasNext()) {
            						child = (TreeImageDisplay) i.next();
            						s.add(child.getUserObject());
            					}
        						setLeaves((TreeImageSet) display, s);
        						db = DataBrowserFactory.getDataBrowser(
        								display.getUserObject());
        						list = browser.getSelectedDataObjects();
        						if (list != null && list.size() == 1) {
        							app = TreeViewerFactory.getApplications(
        								model.getObjectMimeType(list.get(0)));
        						}
        						db.setSelectedNodes(list, app);
        					} else if (object instanceof GroupData) {
        						TreeImageDisplay child;
        						//copy the node.
            					while (i.hasNext()) {
            						child = (TreeImageDisplay) i.next();
            						s.add(child.getUserObject());
            					}
        						setLeaves((TreeImageSet) display, s);
        						db = DataBrowserFactory.getDataBrowser(
        								display.getUserObject());
        						list = browser.getSelectedDataObjects();
        						db.setSelectedNodes(list, app);
        					} else if (object instanceof TagAnnotationData) {
        						TagAnnotationData tag = 
        							(TagAnnotationData) object;
        						if (tag.getTags() == null) {
        							TreeImageDisplay child;
                					while (i.hasNext()) {
                						child = (TreeImageDisplay) i.next();
                						s.add(child.getUserObject());
                					}
            						setLeaves((TreeImageSet) display, s);
            						db = DataBrowserFactory.getDataBrowser(
            								display.getUserObject());
            						list = browser.getSelectedDataObjects();
            						db.setSelectedNodes(list, null);
        						}
        					} 
        				}
        			}// else showDataBrowser(object, parent);
        		}// else view.removeAllFromWorkingPane();
        	}
        }
		model.setDataViewer(db);
		if (db != null) {
			Browser b = getSelectedBrowser();
			if (b != null && b.getBrowserType() == Browser.SCREENS_EXPLORER)
				b.addComponent(db.getGridUI());
		}
	}
	
	/**
	 * Creates a new instance.
	 * The {@link #initialize() initialize} method should be called straight 
	 * after to complete the MVC set up.
	 * 
	 * @param model The Model sub-component.
	 */
	TreeViewerComponent(TreeViewerModel model)
	{
		if (model == null) throw new NullPointerException("No model."); 
		this.model = model;
		controller = new TreeViewerControl(this);
		view = new TreeViewerWin();
		Finder f = new Finder(this);
		model.setFinder(f);
		f.addPropertyChangeListener(controller);
	}

	/** 
	 * Links up the MVC triad. 
	 * 
	 * @param bounds	The bounds of the component invoking a new 
	 * 					{@link TreeViewer}.
	 */
	void initialize(Rectangle bounds)
	{
		controller.initialize(view);
		view.initialize(controller, model, bounds);
		model.getMetadataViewer().addPropertyChangeListener(controller);
	}

	/**
	 * Sets the image to copy the rendering settings from.
	 * 
	 * @param image The image to copy the rendering settings from.
	 */
	void setRndSettings(ImageData image)
	{
		if (model.getState() == DISCARDED) return;
		model.setRndSettings(image);
	}

	/**
	 * Notifies that the rendering settings have been copied.
	 * 
	 * @param imageIds The collection of updated images
	 */
	void onRndSettingsCopied(Collection imageIds)
	{
		if (model.getState() == DISCARDED) return;
		MetadataViewer mv = model.getMetadataViewer();
		if (mv != null) mv.onRndSettingsCopied(imageIds);
	}
	
	/**
	 * Returns the Model sub-component.
	 * 
	 * @return See above.
	 */
	TreeViewerModel getModel() { return model; }

	/**
	 * Sets to <code>true</code> if the component is recycled, 
	 * to <code>false</code> otherwise.
	 * 
	 * @param b The value to set.
	 */
	void setRecycled(boolean b) { model.setRecycled(b); }

	/**
	 * Saves the data before closing.
	 * 
	 * @param evt The event to handle.
	 */
	void saveOnClose(SaveData evt)
	{
		/*
		Editor editor = model.getEditor();
		switch (evt.getType()) {
			case SaveData.DATA_MANAGER_ANNOTATION:
				if (editor == null) 
					editor.saveData();
				break;
			case SaveData.DATA_MANAGER_EDIT:
				if (editor == null) 
					editor.saveData();
				break;
		};
		*/
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getState()
	 */
	public int getState() { return model.getState(); }

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#activate()
	 */
	public void activate()
	{
		switch (model.getState()) {
			case NEW:
				view.setOnScreen();
				view.selectFirstPane();
				model.getSelectedBrowser().activate();
				model.setState(READY);
				break;
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED state.");
		} 
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getBrowsers()
	 */
	public Map<Integer, Browser> getBrowsers() { return model.getBrowsers(); }

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#discard()
	 */
	public void discard()
	{
		Map browsers = getBrowsers();
		Iterator i = browsers.values().iterator();
		while (i.hasNext())
			((Browser) i.next()).discard();
		model.discard();
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getSelectedBrowser()
	 */
	public Browser getSelectedBrowser() { return model.getSelectedBrowser(); }

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getDefaultBrowser()
	 */
	public Browser getDefaultBrowser()
	{ 
		Map<Integer, Browser> browsers = model.getBrowsers();
		return browsers.get(TreeViewerAgent.getDefaultHierarchy());
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setSelectedBrowser(Browser, boolean)
	 */
	public void setSelectedBrowser(Browser browser, boolean activate)
	{
		switch (model.getState()) {
			case DISCARDED:
			case SAVE:
				throw new IllegalStateException(
						"This method cannot be invoked in the DISCARDED or " +
						"SAVE state.");
		}
		if (view.getDisplayMode() == TreeViewer.SEARCH_MODE) {
			view.showAdvancedFinder();
			view.removeAllFromWorkingPane();
		}
			
		Browser oldBrowser = model.getSelectedBrowser();
		if (oldBrowser == null || !oldBrowser.equals(browser)) {
			if (!activate) {
				view.selectPane(browser.getBrowserType());
			}
			model.setSelectedBrowser(browser);
			if (browser != null && activate) {
				browser.activate();
				if (browser.getBrowserType() == Browser.ADMIN_EXPLORER) {
					ExperimenterData exp = model.getUserDetails();
					model.getMetadataViewer().setRootObject(null, exp.getId());
				}
					
			}
			removeEditor();
			model.getMetadataViewer().setSelectionMode(false);
			firePropertyChange(SELECTED_BROWSER_PROPERTY, oldBrowser, browser);
		}
		Browser b = model.getSelectedBrowser();
		int t = -1;
		if (b != null) t = b.getBrowserType();
		EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
		bus.post(new BrowserSelectionEvent(t));
		view.updateMenuItems();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#displayBrowser(int)
	 */
	public void displayBrowser(int browserType)
	{
		switch (model.getState()) {
			case DISCARDED:
			case SAVE:
				throw new IllegalStateException(
						"This method cannot be invoked in the DISCARDED " +
						"or SAVE state.");
		}
		Map<Integer, Browser> browsers = model.getBrowsers();
		Browser browser = browsers.get(browserType);
		if (browser.isDisplayed()) {
			view.removeBrowser(browser);
		} else {
			model.setSelectedBrowser(browser);
			view.addBrowser(browser);
		}
		browser.setDisplayed(!browser.isDisplayed());
		removeEditor();
	}

	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#createDataObject(DataObject, boolean)
	 */
	public void createDataObject(DataObject object, boolean withParent)
	{
		switch (model.getState()) {
			case DISCARDED:
			case SAVE:
				throw new IllegalStateException(
						"This method cannot be invoked in the DISCARDED " +
						"or SAVE state.");
		}
		if (object == null) return;
		JDialog d = null;
		if ((object instanceof ProjectData) || (object instanceof DatasetData) 
				|| (object instanceof ScreenData) || 
				(object instanceof TagAnnotationData)) {
			d = new EditorDialog(view, object, withParent);
		}  else if ((object instanceof GroupData) || 
				(object instanceof ExperimenterData)) {
			Object uo = null;
			Set<DataObject> nodes = null;
    		if (object instanceof ExperimenterData) {
    			Browser browser = getSelectedBrowser();
    			TreeImageDisplay node = null;
    			if (browser != null) node = browser.getLastSelectedDisplay();
        		if (node != null) uo = node.getUserObject();
        		ContainerFinder finder = new ContainerFinder(GroupData.class);
        		getSelectedBrowser().accept(finder, 
        				TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
        		 nodes = finder.getContainers();
        		 if (nodes.size() == 0) {
        			 //Notify user
        			 UserNotifier un = 
        				 TreeViewerAgent.getRegistry().getUserNotifier();
        			 un.notifyInfo("Experimenter Creation", 
        				"No group available. Please create a group first.");
        			 return;
        		 }
    		}
			d = new AdminDialog(view, object.getClass(), uo, nodes);
		}
		
		if (d != null) {
			d.addPropertyChangeListener(controller);
			UIUtilities.centerAndShow(d);
		}
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#cancel()
	 */
	public void cancel()
	{
		if (model.getState() != DISCARDED) {
			model.cancel();
			fireStateChange(); 
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#removeEditor()
	 */
	public void removeEditor()
	{
		switch (model.getState()) {
		case DISCARDED:
			//case SAVE: 
			throw new IllegalStateException("This method cannot be " +
			"invoked in the DISCARDED, SAVE state.");
		}
		view.removeAllFromWorkingPane();
		model.getMetadataViewer().setRootObject(null, -1);
		firePropertyChange(REMOVE_EDITOR_PROPERTY, Boolean.valueOf(false), 
				Boolean.valueOf(true));
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getUserDetails()
	 */
	public ExperimenterData getUserDetails() { return model.getUserDetails(); }

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#showFinder(boolean)
	 */
	public void showFinder(boolean b)
	{
		switch (model.getState()) {
		case DISCARDED:
			throw new IllegalStateException("This method should cannot " +
			"be invoked in the DISCARDED state.");
		}
		if (model.getSelectedBrowser() == null) return;
		Finder finder = model.getFinder();
		if (b == finder.isDisplay())  return;
		Boolean oldValue = 
			finder.isDisplay() ? Boolean.TRUE : Boolean.FALSE,
					newValue = b ? Boolean.TRUE : Boolean.FALSE;
		view.showFinder(b);
		firePropertyChange(FINDER_VISIBLE_PROPERTY, oldValue, newValue);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#closeWindow()
	 */
	public void closeWindow()
	{
		cancel();
		if (TreeViewerFactory.isLastViewer()) {
			EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
			bus.post(new ExitApplication());
		} else discard();

	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#onSelectedDisplay()
	 */
	public void onSelectedDisplay()
	{
		switch (model.getState()) {
		case DISCARDED:
		//case SAVE:
			throw new IllegalStateException("This method cannot be " +
			"invoked in the DISCARDED, SAVE state.");
		}
		view.initializeDisplay();
		Browser browser = model.getSelectedBrowser();
		if (browser == null) return;
		TreeImageDisplay display = browser.getLastSelectedDisplay();
		
		MetadataViewer metadata = model.getMetadataViewer();
		TreeImageDisplay[] selection = browser.getSelectedDisplays();
		
		boolean single = selection.length == 1;
		//Need to review. that
		if (display instanceof TreeImageTimeSet) {
			single = false;
			TreeImageTimeSet time = (TreeImageTimeSet) display;
			view.removeAllFromWorkingPane();
			if (!time.containsImages()) {
				metadata.setRootObject(null, -1);
			}
			showDataBrowser(display.getUserObject(), display, true);
			return;
		} 
		metadata.setSelectionMode(single);
		if (display != null) { 
			Object object = display.getUserObject();
			if (!single) {
				List l = new ArrayList();
				Object child;
				for (int i = 0; i < selection.length; i++) {
					child = selection[i].getUserObject();
					if (!child.equals(object)) 
						l.add(child);
				}
				if (l.size() > 0)
					metadata.setRelatedNodes(l);
			} else {
				ExperimenterData exp = browser.getNodeOwner(display);
				if (exp == null) exp = model.getUserDetails();
				metadata.setRootObject(object, exp.getId());
				TreeImageDisplay p = display.getParentDisplay();
				if (p != null) {
					TreeImageDisplay pp = p.getParentDisplay();
					Object gpp = null;
					if (pp != null) gpp = pp.getUserObject();
					metadata.setParentRootObject(p.getUserObject(), gpp);
				} 
			}
			if (!model.isFullScreen()) {
				showDataBrowser(object, display, false);
				browse(display, null, true);
			} else showDataBrowser(object, display, true);
		} else {
			DataBrowser db = model.getDataViewer();
			if (db != null) db.setSelectedNodes(new ArrayList(), null);
			metadata.setRootObject(null, -1);
		}
		if (display != null && 
			display.getUserObject() instanceof ExperimenterData &&
			display.isToRefresh()) {
			refreshTree();
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setSelectedNodes(Object)
	 */
	public void setSelectedNodes(Object nodes)
	{
		if (nodes == null) return;
		if (!(nodes instanceof List)) return;
		List l = (List) nodes;
		int n = l.size();
		List selection = (List) l.get(0);
		Object parent = null;
		if (n == 1) parent = l.get(1);
		if (selection == null || selection.size() == 0) return;
		MetadataViewer mv = model.getMetadataViewer();
		if (hasDataToSave()) {
			MessageBox dialog = new MessageBox(view, "Save data", 
					"Do you want to save the modified " +
					"data \n before selecting a new item?");
			if (dialog.centerMsgBox() == MessageBox.YES_OPTION) mv.saveData();
			else mv.clearDataToSave();
		}
		Object selected = selection.get(0);
		if (view.getDisplayMode() != SEARCH_MODE) {
			Browser browser = model.getSelectedBrowser();
			browser.onSelectedNode(parent, selection, selection.size() > 0);
		}
		int size = selection.size();
		if (size == 1) {
			Browser browser = model.getSelectedBrowser();
			ExperimenterData exp = null;
			TreeImageDisplay last = null;
			if (browser != null) last = browser.getLastSelectedDisplay();
			if (last != null) exp = browser.getNodeOwner(last);
			if (exp == null) exp = model.getUserDetails();
			mv.setRootObject(selected, exp.getId());
			mv.setParentRootObject(parent, null);
			if (model.getDataViewer() != null)
				model.getDataViewer().setApplications(
					TreeViewerFactory.getApplications(
							model.getObjectMimeType(selected)));
			if (!model.isFullScreen()) {
				//Browser browser = model.getSelectedBrowser();
				browse(browser.getLastSelectedDisplay(), null, false);
			}
			//Notifies actions.
			firePropertyChange(SELECTION_PROPERTY, Boolean.valueOf(false), 
					Boolean.valueOf(true));
			return;
		}
		List result = new ArrayList();
		selection.remove(0);
		result.add(selection);
		result.add(selected);
		result.add(parent);
		setSelectedNode(result);
		/*
		int size = selection.size();
		
		Browser browser = model.getSelectedBrowser();
		ExperimenterData exp = null;
		TreeImageDisplay last = null;
		if (browser != null) last = browser.getLastSelectedDisplay();
		if (last != null) exp = browser.getNodeOwner(last);
		if (exp == null) exp = model.getUserDetails();
		mv.setRootObject(selected, exp.getId());
		mv.setParentRootObject(parent, null);
		
		if (size > 1) {
			//mv.setSelectionMode(false);
			mv.setRelatedNodes(selection.subList(1, size-1));
		}

		if (model.getDataViewer() != null)
			model.getDataViewer().setApplications(
				TreeViewerFactory.getApplications(
						model.getObjectMimeType(selected)));
		if (!model.isFullScreen()) {
			//Browser browser = model.getSelectedBrowser();
			browse(browser.getLastSelectedDisplay(), null, false);
		}
		//Notifies actions.
		firePropertyChange(SELECTION_PROPERTY, Boolean.valueOf(false), 
				Boolean.valueOf(true));
				*/
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setSelectedNode(Object)
	 */
	public void setSelectedNode(Object object)
	{
		if (object == null) return;
		if (!(object instanceof List)) return;
		List l = (List) object;
		int n = l.size();
		if (n > 3) return;
		Object selected = l.get(1);
		Object parent = null;
		if (n == 3) parent = l.get(2);
		if (selected instanceof ImageData) {
			ImageData img = (ImageData) selected;
			try {
				img.getDefaultPixels();
			} catch (Exception e) {
				UserNotifier un = 
					TreeViewerAgent.getRegistry().getUserNotifier();
				un.notifyInfo("Image Not valid", 
						"The selected image is not valid.");
				return;
			}
		} else if (selected instanceof WellSampleData) {
			WellSampleData ws = (WellSampleData) selected;
			if (ws.getId() < 0) {
				UserNotifier un = 
					TreeViewerAgent.getRegistry().getUserNotifier();
				un.notifyInfo("Well Not valid", 
						"The selected well is not valid.");
				return;
			}
		}
		
		MetadataViewer mv = model.getMetadataViewer();
		if (hasDataToSave()) {
			MessageBox dialog = new MessageBox(view, "Save data", 
					"Do you want to save the modified " +
					"data \n before selecting a new item?");
			if (dialog.centerMsgBox() == MessageBox.YES_OPTION) mv.saveData();
			else mv.clearDataToSave();
		}
		
		List siblings = (List) l.get(0);
		int size = siblings.size();
		if (view.getDisplayMode() != SEARCH_MODE) {
			Browser browser = model.getSelectedBrowser();
			browser.onSelectedNode(parent, selected, size > 0);
		}
		mv.setSelectionMode(size == 0);
		Browser browser = model.getSelectedBrowser();
		ExperimenterData exp = null;
		TreeImageDisplay last = null;
		if (browser != null) last = browser.getLastSelectedDisplay();
		if (last != null) exp = browser.getNodeOwner(last);
		if (exp == null) exp = model.getUserDetails();
		mv.setRootObject(selected, exp.getId());
		mv.setParentRootObject(parent, null);
		if (size > 0) 
			mv.setRelatedNodes(siblings);

		if (model.getDataViewer() != null)
			model.getDataViewer().setApplications(
				TreeViewerFactory.getApplications(
						model.getObjectMimeType(selected)));
		if (!model.isFullScreen()) {
			//Browser browser = model.getSelectedBrowser();
			browse(browser.getLastSelectedDisplay(), null, false);
		}
		
		//Notifies actions.
		firePropertyChange(SELECTION_PROPERTY, Boolean.valueOf(false), 
				Boolean.valueOf(true));
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setSelectedField(Object)
	 */
	public void setSelectedField(Object object)
	{
		if (object == null) return;
		if (!(object instanceof List)) return;
		List l = (List) object;
		int n = l.size();
		if (n != 2) return;
		Object selected = l.get(0);
		Object parent = l.get(1);
		if (selected instanceof WellSampleData) {
			WellSampleData ws = (WellSampleData) selected;
			if (ws.getId() < 0) {
				UserNotifier un = 
					TreeViewerAgent.getRegistry().getUserNotifier();
				un.notifyInfo("Well Not valid", 
						"The selected well is not valid.");
				return;
			}
		}
		MetadataViewer mv = model.getMetadataViewer();
		if (hasDataToSave()) {
			MessageBox dialog = new MessageBox(view, "Save data", 
					"Do you want to save the modified \n" +
					"data before selecting a new item?");
			if (dialog.centerMsgBox() == MessageBox.YES_OPTION) mv.saveData();
			else mv.clearDataToSave();
		}
		Browser browser = model.getSelectedBrowser();
		ExperimenterData exp = null;
		TreeImageDisplay last = browser.getLastSelectedDisplay();
		if (last != null) exp = browser.getNodeOwner(last);
		if (exp == null) exp = model.getUserDetails();
		mv.setSelectionMode(true);
		mv.setRootObject(selected, exp.getId());
		mv.setParentRootObject(parent, null);
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#onDataObjectSave(DataObject, int)
	 */
	public void onDataObjectSave(DataObject data, int operation)
	{
		onDataObjectSave(data, null, operation);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#onDataObjectSave(DataObject, DataObject, int)
	 */
	public void onDataObjectSave(DataObject data, DataObject parent, 
								int operation)
	{
		int state = model.getState();
		if (operation == REMOVE_OBJECT && state != SAVE)
			throw new IllegalStateException("This method can only be " +
									"invoked in the SAVE state");
		switch (state) {
			case DISCARDED:
				throw new IllegalStateException("This method cannot be " +
				"invoked in the DISCARDED state");
		}
		if (data == null) 
			throw new IllegalArgumentException("No data object. ");
		switch (operation) {
			case CREATE_OBJECT:
			case UPDATE_OBJECT: 
			case REMOVE_OBJECT:  
				break;
			default:
				throw new IllegalArgumentException("Save operation not " +
						"supported.");
		}  
		//removeEditor(); //remove the currently selected editor.
		if (operation == REMOVE_OBJECT) {
			model.setState(READY);
			fireStateChange();
		}
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		Browser browser = null;
		if (operation == CREATE_OBJECT) {
			if (parent == null) {
				if ((data instanceof ProjectData) || 
					(data instanceof DatasetData))
					browser = model.getBrowser(Browser.PROJECTS_EXPLORER);
				 else if (data instanceof ScreenData)
					browser = model.getBrowser(Browser.SCREENS_EXPLORER);
				else if (data instanceof TagAnnotationData)
					browser = model.getBrowser(Browser.TAGS_EXPLORER);
			}
			if (browser != null) {
				model.setSelectedBrowser(browser);
				view.addBrowser(browser);
				removeEditor();
			}
		}
		browser = model.getSelectedBrowser();
		if (browser != null && operation != UPDATE_OBJECT) 
			browser.refreshTree(null, null);
		if (operation == REMOVE_OBJECT || operation == CREATE_OBJECT) {
			DataBrowserFactory.discardAll();
			view.removeAllFromWorkingPane();
		}
		if (operation == UPDATE_OBJECT && browser != null) {
			browser.accept(new UpdateVisitor(browser, data));
			browser.getUI().repaint();
		}
		setStatus(false, "", true);
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#onOrphanDataObjectCreated(DataObject, int)
	 */
	public void onOrphanDataObjectCreated(DataObject data)
	{
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		Browser browser = model.getSelectedBrowser();
		if (browser != null)
			browser.onOrphanDataObjectCreated(data);
		
		setStatus(false, "", true);
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#onDataObjectSave(List, int)
	 */
	public void onDataObjectSave(List data, int operation)
	{
		int state = model.getState();
		if (operation == REMOVE_OBJECT && state != SAVE)
			throw new IllegalStateException("This method can only be " +
									"invoked in the SAVE state");
		switch (state) {
			case DISCARDED:
				throw new IllegalStateException("This method cannot be " +
				"invoked in the DISCARDED state");
		}
		if (data == null) 
			throw new IllegalArgumentException("No data object. ");
		switch (operation) {
			case CREATE_OBJECT:
			case UPDATE_OBJECT: 
			case REMOVE_OBJECT:  
				break;
			default:
				throw new IllegalArgumentException("Save operation not " +
						"supported.");
		}  
		//removeEditor(); //remove the currently selected editor.
		if (operation == REMOVE_OBJECT) {
			model.setState(READY);
			fireStateChange();
		}
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		setStatus(false, "", true);
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#clearFoundResults()
	 */
	public void clearFoundResults()
	{
		switch (model.getState()) {
		//case LOADING_THUMBNAIL:
		case DISCARDED:
		case SAVE:  
			/*
			throw new IllegalStateException("This method cannot be " +
					"invoked in the DISCARDED, SAVE or LOADING_THUMBNAIL " +
			"state");
			*/
			return;
		}
		//removeEditor(); //remove the currently selected editor.
		Browser browser = model.getSelectedBrowser();
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (browser != null) {
			browser.accept(new ClearVisitor());
			browser.setFoundInBrowser(null); 
		}
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**            
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#moveToBack()
	 */
	public void moveToBack()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		view.toBack();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#moveToFront()
	 */
	public void moveToFront()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		view.toFront();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setHierarchyRoot(long, ExperimenterData)
	 */
	public void setHierarchyRoot(long userGroupID, 
			ExperimenterData experimenter)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		if (experimenter == null) return;
		Map<Integer, Browser> browsers = model.getBrowsers();
		Iterator i = browsers.entrySet().iterator();
		Browser browser;
		Entry entry;
		Browser selected = model.getSelectedBrowser();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			browser = (Browser) entry.getValue();
			browser.addExperimenter(experimenter, browser == selected);
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#isObjectWritable(Object)
	 */
	public boolean isObjectWritable(Object ho)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		//Check if current user can write in object
		long id = model.getUserDetails().getId();
		boolean b = false;
		if (ho instanceof TreeImageTimeSet) {
			Browser browser = model.getSelectedBrowser();
			ExperimenterData exp = browser.getNodeOwner((TreeImageDisplay) ho);
			if (exp.getId() == id) b = true;
		} else b = EditorUtil.isUserOwner(ho, id);
		if (b) return b; //user it the owner.
		int level = 
			TreeViewerAgent.getRegistry().getAdminService().getPermissionLevel();
		switch (level) {
			case AdminObject.PERMISSIONS_GROUP_READ_LINK:
			case AdminObject.PERMISSIONS_PUBLIC_READ_WRITE:
				return true;
		}
		return false;
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#isObjectWritable(Object)
	 */
	public boolean isUserOwner(Object ho)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		//Check if current user can write in object
		long id = model.getUserDetails().getId();
		if (ho instanceof TreeImageTimeSet) {
			Browser browser = model.getSelectedBrowser();
			ExperimenterData exp = browser.getNodeOwner((TreeImageDisplay) ho);
			return (exp.getId() == id);
		}
		return EditorUtil.isUserOwner(ho, id);
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#canDeleteObject(Object)
	 */
	public boolean canDeleteObject(Object ho)
	{
		if (isUserOwner(ho)) return true;
		if (TreeViewerAgent.isLeaderOfCurrentGroup()) return true;
		if (TreeViewerAgent.isAdministrator()) return true;
		return false;
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#addExistingObjects(DataObject)
	 */
	public void addExistingObjects(DataObject ho)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		if (ho == null) 
			throw new IllegalArgumentException("No object.");
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		model.fireDataExistingObjectsLoader(ho);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setExistingObjects(List)
	 */
	public void setExistingObjects(List objects)
	{
		if (model.getState() != LOADING_DATA)
			throw new IllegalStateException(
					"This method cannot be invoked in the LOADING_DATA state.");
		setStatus(false, "", true);
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		model.setState(READY);
		if (objects == null || objects.size() == 0)
			return;
		Browser b = model.getSelectedBrowser();
		Set selected = null;
		List available = new ArrayList();
		if (b != null) {
			TreeImageDisplay[] values = b.getSelectedDisplays();
			if (values != null && values.length == 1) {
				Object value = values[0].getUserObject();
				if (value instanceof GroupData) {
					GroupData group = (GroupData) value;
					Set l = group.getExperimenters();
					if (l != null) {
						selected = new HashSet();
						Iterator i = l.iterator();
						DataObject o;
						List<Long> ids = new ArrayList<Long>();
						ExperimenterData exp = TreeViewerAgent.getUserDetails();
						long userID = exp.getId();
						while (i.hasNext()) {
							o = (DataObject) i.next();
							if (o.getId() != userID) {
								ids.add(o.getId());
								selected.add(o);
							}
						}
						i = objects.iterator();
						while (i.hasNext()) {
							o = (DataObject) i.next();
							if (!ids.contains(o.getId()) && o.getId() != userID)
								available.add(o);
						}
					}
				}
			} else available.addAll(objects);
		}
		fireStateChange();
		SelectionWizard d = new SelectionWizard(view, available, selected,
				objects.get(0).getClass(), 
				TreeViewerAgent.getUserDetails().getId());
		IconManager icons = IconManager.getInstance();
		String title = "Experimenters Selection";
		String text = "Select the Experimenters to add to the selected group.";
		Icon icon = icons.getIcon(IconManager.OWNER_48);
		d.setTitle(title, text, icon);
		d.addPropertyChangeListener(controller);
		UIUtilities.centerAndShow(d);  
		/*
		Set n = TreeViewerTranslator.transformIntoCheckNodes(nodes, 
				getUserDetails().getId(), model.getUserGroupID());
		model.setState(LOADING_SELECTION);
		AddExistingObjectsDialog dialog = new AddExistingObjectsDialog(view, n);
		dialog.addPropertyChangeListener(controller);
		UIUtilities.centerAndShow(dialog);  
		*/
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#addExistingObjects(Set)
	 */
	public void addExistingObjects(Set set)
	{
		if (model.getState() != LOADING_SELECTION)
			throw new IllegalStateException(
					"This method cannot be invoked in the LOADING_DATA state.");
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (set == null || set.size() == 0) model.setState(READY);
		else {
			model.fireAddExistingObjects(set);
		}
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#showMenu(int, Component, Point)
	 */
	public void showMenu(int menuID, Component c, Point p)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		switch (menuID) {
			case MANAGER_MENU:
			case CREATE_MENU_CONTAINERS:  
			case CREATE_MENU_TAGS:  
			case CREATE_MENU_ADMIN:
			case PERSONAL_MENU:
			case CREATE_MENU_SCREENS:
				break;
			case AVAILABLE_SCRIPTS_MENU:
				if (model.getAvailableScripts() == null) {
					model.loadScripts(p);
					firePropertyChange(SCRIPTS_LOADING_PROPERTY,
							Boolean.valueOf(false), Boolean.valueOf(true));
					return;
				}
				break;
			default:
				throw new IllegalArgumentException("Menu not supported.");
		}
		view.showMenu(menuID, c, p);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setStatus(boolean, String, boolean)
	 */
	public void setStatus(boolean enable, String text, boolean hide)
	{
		view.setStatus(text, hide);
		view.setStatusIcon(enable);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#onComponentStateChange(boolean)
	 */
	public void onComponentStateChange(boolean b)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		Browser browser = model.getSelectedBrowser();
		if (browser != null) browser.onComponentStateChange(b);
		view.onStateChanged(b);
		firePropertyChange(ON_COMPONENT_STATE_CHANGED_PROPERTY, 
				Boolean.valueOf(!b), Boolean.valueOf(b));
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setNodesToCopy(TreeImageDisplay[], int)
	 */
	public void setNodesToCopy(TreeImageDisplay[] nodes, int index)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		if (nodes == null || nodes.length == 0) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Copy action", "You first need to select " +
			"the nodes to copy."); 
			return;
		}
		switch (index) {
			case CUT_AND_PASTE:
			case COPY_AND_PASTE:
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
		model.setNodesToCopy(nodes, index);
		
		EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
		bus.post(new CopyItems(model.getDataToCopyType()));
		if (index == CUT_AND_PASTE) {
			if (model.cut()) {
				fireStateChange();
			}
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#paste(TreeImageDisplay[])
	 */
	public void paste(TreeImageDisplay[] parents)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
		if (parents == null || parents.length == 0) {
			un.notifyInfo("Paste action", "You first need to select " +
			"the nodes to copy into.");
			return;	
		}
		TreeImageDisplay[] nodes = model.getNodesToCopy();
		if (nodes == null || nodes.length == 0) return; 
		boolean b = model.paste(parents);
		if (!b) {
			un.notifyInfo("Paste action", "The nodes to copy cannot " +
			"be added to the selected nodes."); 
		} else fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getUI()
	 */
	public JFrame getUI()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked " +
			"in the DISCARDED state.");
		return view;
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#hasDataToSave()
	 */
	public boolean hasDataToSave()
	{
		MetadataViewer metadata = model.getMetadataViewer();
		if (metadata == null) return false;
		return metadata.hasDataToSave();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#showPreSavingDialog()
	 */
	public void showPreSavingDialog()
	{
		MetadataViewer metadata = model.getMetadataViewer();
		if (metadata == null) return;
		if (!(metadata.hasDataToSave())) return;
		
		MessageBox dialog = new MessageBox(view, "Save data", 
				"Do you want to save the modified \n" +
				"data before selecting a new item?");
		if (dialog.centerMsgBox() == MessageBox.YES_OPTION) {
			model.getMetadataViewer().saveData();
		} else {
			model.getMetadataViewer().clearDataToSave();
			//removeEditor();
			Browser browser = model.getSelectedBrowser();
			if (browser != null) browser.setSelectedNode();
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getUserGroupID()
	 */
	public long getUserGroupID()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		return model.getUserGroupID();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#retrieveUserGroups(Point)
	 */
	public void retrieveUserGroups(Point location)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		displayUserGroups(TreeViewerAgent.getAvailableUserGroups(), location);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getExperimenterNames()
	 */
	public String getExperimenterNames()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		return model.getExperimenterNames();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getSelectedExperimenter()
	 */
	public ExperimenterData getSelectedExperimenter()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		Browser b = model.getSelectedBrowser();
		ExperimenterData exp = model.getExperimenter();
		if (b != null) {
			TreeImageDisplay node = b.getLastSelectedDisplay();
			if (node != null) exp = b.getNodeOwner(node);
		}
		return exp;
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#isRecycled()
	 */
	public boolean isRecycled() 
	{ 
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		return model.isRecycled(); 
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#isRollOver()
	 */
	public boolean isRollOver()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		return model.isRollOver();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setRollOver(boolean)
	 */
	public void setRollOver(boolean rollOver)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		model.setRollOver(rollOver);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#removeExperimenterData()
	 */
	public void removeExperimenterData()
	{
		//TODO: Check state

		Browser browser = model.getSelectedBrowser();
		TreeImageDisplay expNode = browser.getLastSelectedDisplay();
		Object uo = expNode.getUserObject();
		if (uo == null || !(uo instanceof ExperimenterData)) return;
		ExperimenterData exp = (ExperimenterData) uo;
		Map<Integer, Browser> browsers = model.getBrowsers();
		Iterator i = browsers.entrySet().iterator();
		Entry entry;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			browser = (Browser) entry.getValue();
			browser.removeExperimenter(exp);
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#hasRndSettings()
	 */
	public boolean hasRndSettings()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked " +
			"in the DISCARDED state.");
		return model.hasRndSettingsToPaste();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#pasteRndSettings(List, Class)
	 */
	public void pasteRndSettings(List<Long> ids, Class klass)
	{
		if (!hasRndSettings()) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Paste settings", "No rendering settings to" +
			"paste. \n Please first copy settings.");
			return;
		}
		if (ids == null || ids.size() == 0) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Paste settings", "Please select the nodes \n" +
			"you wish to apply the settings to.");
			return;
		}
		model.firePasteRenderingSettings(ids, klass);
		fireStateChange();
	}

	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#pasteRndSettings(TimeRefObject)
	 */
	public void pasteRndSettings(TimeRefObject ref)
	{
		//TODO Check state.
		if (!hasRndSettings()) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Paste settings", "No rendering settings to" +
			"paste. Please first copy settings.");
			return;
		}
		if (ref == null) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Paste settings", "Please select the nodes" +
			"you wish to apply the settings to.");
			return;
		}
		model.firePasteRenderingSettings(ref);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#rndSettingsPasted(Map)
	 */
	public void rndSettingsPasted(Map map)
	{
		if (map == null || map.size() != 2) return;
		Collection failure = (Collection) map.get(Boolean.valueOf(false));
		Collection success = (Collection) map.get(Boolean.valueOf(true));
		EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
		bus.post(new RndSettingsCopied(success, -1));
		/*
		UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
		String name = model.getRefImageName();
		int n = success.size();
		String text = "The rendering settings ";
		if (name != null && name.length() > 0)
			text += "of "+name;
		if (failure.size() == 0) {
			text += "\nhave been applied to the selected image";
			if (n > 1) text += "s.";
			else text += ".";
			un.notifyInfo("Rendering Settings Applied", text);
		} else {
			DataBrowser db = model.getDataViewer();
			String message = "";
			StringBuffer buffer;
			if (db != null) {
				Set<DataObject> images = db.getBrowser().getImages();
				Map<Long, ImageData> m = new HashMap<Long, ImageData>();
				if (images != null) {
					Iterator<DataObject> k = images.iterator();
					ImageData img;
					DataObject obj;
					while (k.hasNext()) {
						obj = k.next();
						if (obj instanceof ImageData) {
							img = (ImageData) obj;
							m.put(img.getId(), img);
						}
					}
					
					Iterator i = failure.iterator();
					long id;
					buffer = new StringBuffer();
					while (i.hasNext()) {
						id = (Long) i.next();
						if (m.containsKey(id)) {
							buffer.append(EditorUtil.getPartialName(
									m.get(id).getName()));
							buffer.append("\n");
						}
					}
					message = text+"\ncould not be applied to the following " +
							"images:\n"+buffer.toString();
				}
			} 
			if (message.length() == 0) {
				String s = " image";
				if (n > 1) s+="s";
				s += ".";
				message = text+"\ncould not be applied to "+n+s;
			}
			un.notifyInfo("Rendering Settings Applied", message);
			
			//if (db != null) 
			//	db.markUnmodifiedNodes(ImageData.class, failure);
		}
		*/
		MetadataViewer mv = model.getMetadataViewer();
		if (mv != null) mv.onSettingsApplied();
		model.setState(READY);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#resetRndSettings(List, Class)
	 */
	public void resetRndSettings(List<Long> ids, Class klass)
	{
		if (ids == null || ids.size() == 0) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Reset settings", "Please select at one element.");
			return;
		}
		model.fireResetRenderingSettings(ids, klass);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#resetRndSettings(TimeRefObject)
	 */
	public void resetRndSettings(TimeRefObject ref)
	{
		if (ref == null) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Reset settings", "Please select at one element.");
			return;
		}
		model.fireResetRenderingSettings(ref);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#createObject(DataObject, boolean)
	 */
	public void createObject(DataObject object, boolean withParent)
	{
		if (model.getState() == DISCARDED) return;
		if (object == null) return;
		Browser browser = null;
		if (object instanceof DatasetData || object instanceof ProjectData) 
			browser = model.getBrowser(Browser.PROJECTS_EXPLORER);
		else if (object instanceof ScreenData) 
			browser = model.getBrowser(Browser.SCREENS_EXPLORER);
		if (browser != null) browser.expandUser();
		model.fireDataObjectCreation(object, withParent);
		fireStateChange();
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setLeaves(TreeImageSet, Collection)
	 */
	public void setLeaves(TreeImageSet parent, Collection leaves)
	{
		if (parent instanceof TreeFileSet) {
			view.removeAllFromWorkingPane();
			return;
		}
		Object parentObject = parent.getUserObject();
		TreeImageDisplay display = parent.getParentDisplay();
		Object grandParentObject = null;
		if (display != null) grandParentObject = display.getUserObject();
		DataBrowser db = null;
		if (parentObject instanceof TagAnnotationData) {
			db = DataBrowserFactory.getTagsBrowser(
					(TagAnnotationData) parentObject, leaves, false);
		} else if (parentObject instanceof GroupData) {
			db = DataBrowserFactory.getGroupsBrowser(
					(GroupData) parentObject, leaves);
		} else if (parentObject instanceof FileData) {
			FileData f = (FileData) parentObject;
			if (!f.isHidden()) {
				if (f.isDirectory() || f instanceof MultiImageData) 
					db = DataBrowserFactory.getFSFolderBrowser(
							(FileData) parentObject, leaves);
			}
		} else {
			db = DataBrowserFactory.getDataBrowser(grandParentObject, 
					parentObject, leaves, parent);
			if (parent instanceof TreeImageTimeSet) {
				ExperimenterData exp = getSelectedBrowser().getNodeOwner(parent);
				db.setExperimenter(exp);
			}
		}
			
		if (db == null) return;
		db.addPropertyChangeListener(controller);
		//db.activate();
		view.displayBrowser(db);
		db.activate();
		model.setDataViewer(db);
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getDisplayedImages()
	 */
	public Collection getDisplayedImages()
	{
		DataBrowser db = model.getDataViewer();
		if (db == null) return null;
		return db.getBrowser().getVisibleImages();
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#browseHierarchyRoots(Object, Collection)
	 */
	public void browseHierarchyRoots(Object parent, Collection roots)
	{
		if (roots == null) return;
		Iterator i = roots.iterator();
		//Map<ProjectData, Set<DatasetData>>
		DataBrowser db = null;
		if (roots.size() != 1) return;
		DataObject node;
		TagAnnotationData tag;
		ProjectData project;
		Iterator j;
		TreeImageDisplay child, value;
		long id; 
		Set set, dataObjects;
		DatasetData d;
		long userID = model.getExperimenter().getId();
		long groupID = model.getUserGroupID();
		Iterator k;
		
		Map<Long, TreeImageDisplay> m = new HashMap<Long, TreeImageDisplay>();
		if (parent instanceof TreeImageDisplay) {
			TreeImageDisplay display = (TreeImageDisplay) parent;
			List l = display.getChildrenDisplay();
			if (l != null) {
				j = l.iterator();
				while (j.hasNext()) {
					child = (TreeImageDisplay) j.next();
					id = child.getUserObjectId();
					if (id >= 0) m.put(id, child);
				}
			}
		}
		DataObject object;
		Set datasets;
		Iterator l;
		while (i.hasNext()) {
			node = (DataObject) i.next();
			if (node instanceof ProjectData) {
				project = (ProjectData) node;
				set = project.getDatasets();
				j = set.iterator();
				while (j.hasNext()) {
					d = (DatasetData) j.next();
					value = m.get(d.getId());
					if (value != null) {
						dataObjects = d.getImages();
						if (dataObjects != null && !value.isChildrenLoaded()) {
							value.removeAllChildrenDisplay();
							k = dataObjects.iterator();
							while (k.hasNext()) {
								value.addChildDisplay(
										TreeViewerTranslator.transformDataObject(
										 (ImageData) k.next(), userID, groupID));
							}
						}
						value.setChildrenLoaded(true);
					}
				}
				model.setState(READY);
				fireStateChange();
				db = DataBrowserFactory.getDataBrowser(project, set);
			} else if (node instanceof TagAnnotationData) {
				tag = (TagAnnotationData) node;
				set = tag.getDataObjects();//tag.getTags();
				j = set.iterator();
				
				while (j.hasNext()) {
					object = (DataObject) j.next();
					value = m.get(object.getId());
					
					if (value != null) {
						if (object instanceof DatasetData) {
							dataObjects = ((DatasetData) object).getImages();
							if (dataObjects != null) {
								value.removeAllChildrenDisplay();
								k = dataObjects.iterator();
								while (k.hasNext()) {
									value.addChildDisplay(
									 TreeViewerTranslator.transformDataObject(
										(ImageData) k.next(), userID, groupID)
											);
								}
							}
							value.setChildrenLoaded(true);
						} else if (object instanceof ProjectData) {
							datasets = ((ProjectData) object).getDatasets();
							l = datasets.iterator();
							while (l.hasNext()) {
								d = (DatasetData) j.next();
								dataObjects = d.getImages();
								if (dataObjects != null) {
									value.removeAllChildrenDisplay();
									k = dataObjects.iterator();
									while (k.hasNext()) {
										value.addChildDisplay(
										 TreeViewerTranslator.transformDataObject(
												 (ImageData) k.next(), 
												 userID, groupID)
												);
									}
								}
								value.setChildrenLoaded(true);
							}
						}
					}
				}
				model.setState(READY);
				fireStateChange();
				db = DataBrowserFactory.getTagsBrowser(tag, set, true);
			}
		}
		if (db != null) {
			db.addPropertyChangeListener(controller);
			view.removeAllFromWorkingPane();
			view.displayBrowser(db);
			db.activate();
		}
		model.setDataViewer(db);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setUnselectedNode(Object)
	 */
	public void setUnselectedNode(Object object)
	{
		if (object == null) return;
		if (!(object instanceof List)) return;
		//Need to notify the browser without having 
		List l = (List) object;
		int n = l.size();
		if (n > 3) return;
		Object multiSelection = l.get(0);
		Object selected = l.get(1);
		Object parent = null;
		if (n == 3) parent = l.get(2);
		if (selected instanceof ImageData) {
			ImageData img = (ImageData) selected;
			try {
				img.getDefaultPixels();
			} catch (Exception e) {
				UserNotifier un = 
					TreeViewerAgent.getRegistry().getUserNotifier();
				un.notifyInfo("Image Not valid", 
						"The selected image is not valid");
				return;
			}
		}

		Browser browser = model.getSelectedBrowser();
		browser.onDeselectedNode(parent, selected, (Boolean) multiSelection);
		onSelectedDisplay();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#copyRndSettings(ImageData)
	 */
	public void copyRndSettings(ImageData image)
	{
		if (image == null) {
			Browser browser = model.getSelectedBrowser();
			if (browser == null) return;
			TreeImageDisplay node = browser.getLastSelectedDisplay();
			if (node == null) return;
			Object o = node.getUserObject();
			if (!(o instanceof ImageData)) return;
			image = (ImageData) o;
		}
		EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
		bus.post(new CopyRndSettings(image));
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setMinMax(List, Class)
	 */
	public void setMinMax(List<Long> ids, Class klass)
	{
		if (ids == null || ids.size() == 0) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Set settings", "Please select at least " +
					"one element.");
			return;
		}
		model.fireSetMinMax(ids, klass);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setOriginalRndSettings(TimeRefObject)
	 */
	public void setOriginalRndSettings(TimeRefObject ref)
	{
		if (ref == null) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Set settings", "Please select at least" +
					" one element.");
			return;
		}
		model.fireSetOwnerRenderingSettings(ref);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setOwnerRndSettings(List, Class)
	 */
	public void setOwnerRndSettings(List<Long> ids, Class klass)
	{
		if (ids == null || ids.size() == 0) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Set settings", "Please select at least " +
					"one element.");
			return;
		}
		model.fireSetOwnerRenderingSettings(ids, klass);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setOwnerRndSettings(TimeRefObject)
	 */
	public void setOwnerRndSettings(TimeRefObject ref)
	{
		if (ref == null) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Set settings", "Please select at least" +
					" one element.");
			return;
		}
		model.fireSetOriginalRenderingSettings(ref);
		fireStateChange();
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setOriginalRndSettings(TimeRefObject)
	 */
	public void showSearch()
	{
		int oldMode = view.getDisplayMode();
		view.showAdvancedFinder();
		DataBrowser db = DataBrowserFactory.getSearchBrowser();
		int newMode = view.getDisplayMode();
		view.removeAllFromWorkingPane();
		model.getAdvancedFinder().requestFocusOnField();
		switch (newMode) {
			case EXPLORER_MODE:
				onSelectedDisplay();
				break;
			case SEARCH_MODE:
				ExperimenterData exp = model.getUserDetails();
				model.getMetadataViewer().setRootObject(null, exp.getId());
				if (db != null) {
					view.displayBrowser(db);
					model.setDataViewer(db);
				}
		}
		firePropertyChange(DISPLAY_MODE_PROPERTY, oldMode, newMode);
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setSearchResult(Object)
	 */
	public void setSearchResult(Object result)
	{
		Collection<DataObject> results = (Collection<DataObject>) result;
		MetadataViewer metadata = model.getMetadataViewer();
		if (metadata != null) {
			metadata.setRootObject(null, -1);
		}
		if (results == null || results.size() == 0) {
			//UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
        	//un.notifyInfo("Search", "No results found.");
        	view.removeAllFromWorkingPane();
			return;
		}
		//Need to recycle the search browser.
		DataBrowser db = DataBrowserFactory.getSearchBrowser(results);
		if (db != null && view.getDisplayMode() == SEARCH_MODE) {
			db.setExperimenter(TreeViewerAgent.getUserDetails());
			db.addPropertyChangeListener(controller);
			view.removeAllFromWorkingPane();
			view.displayBrowser(db);
			db.activate();
			model.setDataViewer(db);
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#addMetadata()
	 */
	public void addMetadata()
	{
		//Need to make difference between batch and selection.
		Browser browser = model.getSelectedBrowser();
    	if (browser == null) return;
    	TreeImageDisplay[] nodes = browser.getSelectedDisplays();
    	if (nodes == null || nodes.length == 0) return;
    	List<Object> data = new ArrayList<Object>();
    	Object uo;
    	TreeImageDisplay n;
    	Class type = null;
    	String text = "Add metadata to ";
    	if (nodes.length == 1) {
    		n = nodes[0];
    		uo = n.getUserObject();
    		if (uo instanceof TagAnnotationData) {
    			data.add(uo);
    			type = TagAnnotationData.class;
    			text += "images linked to the selected tag.";
    		} else if (uo instanceof DatasetData) {
    			data.add( uo);
    			type = DatasetData.class;
    			text += "images linked to the selected dataset.";
    		} else if (n instanceof TreeImageTimeSet) {
    			TreeImageTimeSet time = (TreeImageTimeSet) n;
        		ExperimenterData exp = model.getUserDetails();
        		TimeRefObject ref = new TimeRefObject(exp.getId(), 
        				TimeRefObject.TIME);
    			ref.setTimeInterval(time.getStartTime(), time.getEndTime());
        		data.add(ref);
    			type = TimeRefObject.class;
    			text += "images imported during the selected period.";
    		}
    	} else {
    		for (int i = 0; i < nodes.length; i++) {
    			n = nodes[i];
        		uo = n.getUserObject();
        		if (uo instanceof ImageData)
        			data.add(uo);
			}
    		text += "the selected images.";
    	}
    	if (data.size() > 0) {
    		IconManager icons = IconManager.getInstance();
    		MetadataViewer viewer = MetadataViewerFactory.getViewer(data, type);
    		GenericDialog dialog = new GenericDialog(view, "Add Metadata...");
    		dialog.initialize("Add Metadata...", text, 
    				icons.getIcon(IconManager.ADD_METADATA_48), 
    				viewer.getEditorUI());
    		dialog.setParent(viewer);
    		viewer.setSelectionMode(true);
    		dialog.addPropertyChangeListener(controller);
    		UIUtilities.centerAndShow(dialog);
    	}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#refreshTree()
	 */
	public void refreshTree()
	{
		int state = model.getState();
		if (state == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked " +
					"in the DISCARDED state");
		Browser b = model.getSelectedBrowser();
		DataBrowserFactory.discardAll();
	    view.removeAllFromWorkingPane();
        if (b != null) b.refreshTree(null, null);
        ExperimenterData exp = model.getUserDetails();
        model.getMetadataViewer().setRootObject(null, exp.getId());
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#browseTimeInterval(TreeImageTimeSet, Set)
	 */
	public void browseTimeInterval(TreeImageTimeSet parent, Set leaves)
	{
		if (leaves == null) return;
		
		if (parent == null) return;
		Object parentObject = null;
		ExperimenterData exp = null;
		Object grandParentObject = null;
		//Set the userID of the owner of the time interval.
		parentObject = parent.getUserObject();
		TreeImageDisplay display = parent.getParentDisplay();
		if (display != null) grandParentObject =  display.getUserObject();
		Browser browser = model.getSelectedBrowser();
		if (browser != null) 
			exp = browser.getNodeOwner(parent);
		DataBrowser db = DataBrowserFactory.getDataBrowser(grandParentObject,
				parentObject, leaves, parent);
		db.setExperimenter(exp);
		db.addPropertyChangeListener(controller);
		view.removeAllFromWorkingPane();
		view.displayBrowser(db);
		db.activate();
		model.setDataViewer(db);
		model.setState(READY);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setPlates(Map, boolean)
	 */
	public void setPlates(Map<TreeImageSet, Set> plates, boolean withThumbnails)
	{
		if (plates == null || plates.size() == 0) {
			return;
		}
		int n = plates.size();
		Iterator i = plates.entrySet().iterator();
		Entry entry;
		Object parentObject;
		TreeImageSet parent;
		TreeImageDisplay display;
		Object gpo = null;
		DataBrowser db = null;
		if (n == 1) {
			Map<Class, Object> m = new HashMap<Class, Object>();
			
			while (i.hasNext()) {
				entry = (Entry) i.next();
				parent = (TreeImageSet) entry.getKey();
				parentObject = parent.getUserObject();
				display = parent.getParentDisplay();
				if (display != null) {
					gpo = display.getUserObject();
					if (gpo != null)
						m.put(gpo.getClass(), gpo);
					if (gpo instanceof PlateData) {
						display = display.getParentDisplay();
						if (display != null) {
							gpo = display.getUserObject();
							if (gpo != null)
								m.put(gpo.getClass(), gpo);
						}
					}
				}
				
				db = DataBrowserFactory.getWellsDataBrowser(m, parentObject, 
						(Set) entry.getValue(), withThumbnails);
			}
		}
		if (db != null) {
			db.addPropertyChangeListener(controller);
			view.removeAllFromWorkingPane();
			
			db.activate();
			view.displayBrowser(db);
			model.setDataViewer(db);
			getSelectedBrowser().addComponent(db.getGridUI());
		}
		model.setState(READY);
		fireStateChange();
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#browse(TreeImageDisplay, DataObject, boolean)
	 */
	public void browse(TreeImageDisplay node, DataObject data, 
			boolean withThumbnails)
	{
		NodesFinder finder;
		Set<TreeImageDisplay> nodes;
		Iterator<TreeImageDisplay> i;
		Browser browser = model.getSelectedBrowser();
		if (node == null) {
			if (browser == null) return;
			if (data instanceof DatasetData) {
				finder = new NodesFinder((DataObject) data);
				browser.accept(finder);
				nodes = finder.getNodes();
				ExperimenterData exp;
				MetadataViewer mv = model.getMetadataViewer();
				int size = nodes.size();
				if (size == 1) {
					i = nodes.iterator();
					TreeImageDisplay parent;
					mv.setSelectionMode(size == 1);
					while (i.hasNext()) {
						node = i.next();
						parent = BrowserFactory.getDataOwner(node);
						browser.loadExperimenterData(parent, node);
						exp = (ExperimenterData) parent.getUserObject();
						if (exp == null) exp = model.getUserDetails();
						node.setExpanded(true);
						mv.setRootObject(node.getUserObject(), exp.getId());
						browser.onSelectedNode(null, node, false);
						break;
					}
				}
			} else if (data instanceof PlateData) {
				finder = new NodesFinder((DataObject) data);
				browser.accept(finder);
				nodes = finder.getNodes();
				if (nodes.size() > 0) { //node found.
					model.browsePlates(nodes, withThumbnails);
				}
			} else if (data instanceof ImageData) {
				finder = new NodesFinder((DataObject) data);
				browser.accept(finder);
				nodes = finder.getNodes();
				ExperimenterData exp;
				MetadataViewer mv = model.getMetadataViewer();
				int size = nodes.size();
				if (size == 1) {
					i = nodes.iterator();
					TreeImageDisplay parent, expNode;
					mv.setSelectionMode(size == 1);
					while (i.hasNext()) {
						node = i.next();
						parent = node.getParentDisplay();
						expNode = BrowserFactory.getDataOwner(node);
						exp = (ExperimenterData) expNode.getUserObject();
						if (exp == null) exp = model.getUserDetails();
						node.setExpanded(true);
						//mv.setRootObject(node.getUserObject(), exp.getId());
						
						List l = parent.getChildrenDisplay();
						Iterator j = l.iterator();
						List leaves = new ArrayList();
						while (j.hasNext()) {
							TreeImageDisplay o = (TreeImageDisplay) j.next();
							leaves.add(o.getUserObject());
						}
						
						DataBrowser db = DataBrowserFactory.getDataBrowser(null,
								parent.getUserObject(), leaves, parent);
						if (db == null) return;
						db.addPropertyChangeListener(controller);
						db.activate();
						view.displayBrowser(db);
						//db.activate();
						model.setDataViewer(db);
						browser.setSelectedDisplay(node);
					}
				}
			}
			return;
		}
		Object uo = node.getUserObject();
		if (uo instanceof ProjectData) {
			model.browseProject(node);
		} else if (uo instanceof DatasetData) {
			if (browser != null)
				browser.loadExperimenterData(BrowserFactory.getDataOwner(node), 
        			node);
		} else if (uo instanceof ScreenData) {
			if (data instanceof PlateData) {
				//Find node
				finder = new NodesFinder((DataObject) data);
				browser.accept(finder);
				nodes = finder.getNodes();
				if (nodes.size() > 0) { //node found.
					TreeImageDisplay[] values = 
						new TreeImageDisplay[nodes.size()];
					i = nodes.iterator();
					int index = 0;
					TreeImageDisplay n;
					while (i.hasNext()) {
						n = i.next();
						node = n.getParentDisplay();
						values[index] = n;
						index++;
					}
					browser.setSelectedDisplays(values, true);
					model.browsePlates(nodes, withThumbnails);
				}
			} else if (data instanceof ScreenData) {
				//Find node
				finder = new NodesFinder((DataObject) data);
				browser.accept(finder);
				nodes = finder.getNodes();
				if (nodes.size() > 0) { //node found.
					TreeImageDisplay[] values = 
						new TreeImageDisplay[nodes.size()];
					i = nodes.iterator();
					int index = 0;
					TreeImageDisplay n;
					while (i.hasNext()) {
						n = i.next();
						node = n.getParentDisplay();
						values[index] = n;
						index++;
					}
					browser.setSelectedDisplays(values, true);
				}
			}
		} else if (uo instanceof TagAnnotationData) {
			TagAnnotationData tag = (TagAnnotationData) uo;
			if (!TagAnnotationData.INSIGHT_TAGSET_NS.equals(tag.getNameSpace()))
				model.browseTag(node);
		} else if (uo instanceof ImageData) {
			EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
			ViewImageObject vo = new ViewImageObject((ImageData) uo);
			TreeImageDisplay p = node.getParentDisplay();
			TreeImageDisplay gp = null;
			DataObject po = null;
			DataObject gpo = null;
			if (p != null) {
				uo = p.getUserObject();
				gp = p.getParentDisplay();
				if (uo instanceof DataObject) 
					po = (DataObject) uo;
				if (gp != null) {
					//gp = gp.getParentDisplay();
					//if (gp != null) {
					uo = gp.getUserObject();
					if (uo instanceof DataObject) {
						gpo = (DataObject) uo;
					}
					//}	
				}
			}
			vo.setContext(po, gpo);
			ViewImage evt = new ViewImage(vo, view.getBounds());
			evt.setSeparateWindow(model.isFullScreen());
			bus.post(evt);
		} else if (node instanceof TreeImageTimeSet) {
			model.browseTimeInterval((TreeImageTimeSet) node);
		} else if (uo instanceof PlateData) {
			List<TreeImageDisplay> plates = new ArrayList<TreeImageDisplay>();
			List l = node.getChildrenDisplay();
			if (l == null || l.size() == 0) {
				plates.add(node);
			}
			if (l.size() == 1) {
				plates.add((TreeImageDisplay) l.get(0));
			}
			model.browsePlates(plates, withThumbnails);
		} else if (uo instanceof PlateAcquisitionData) {
			List<TreeImageDisplay> plates = new ArrayList<TreeImageDisplay>();
			plates.add(node);
			model.browsePlates(plates, withThumbnails);
		} else if (uo instanceof File) {
			File f = (File) uo;
			if (f.isDirectory() && !f.isHidden()) {
				List l = node.getChildrenDisplay();
				if (l != null && l.size() > 0) {
					Set leaves = new HashSet();
					i = l.iterator();
					Object object;
					TreeImageDisplay child;
					while (i.hasNext()) {
						child = (TreeImageDisplay) i.next();
						object = child.getUserObject();
						if (object instanceof ImageData) 
							leaves.add(object);
					}
					if (leaves.size() > 0)
						setLeaves((TreeImageSet) node, leaves);
				}
			}
		} else if (uo instanceof FileData) {
			FileData fa = (FileData) uo;
			if (!fa.isHidden()) {
				if (fa.isDirectory() || fa instanceof MultiImageData) {
					model.getSelectedBrowser().loadExperimenterData(
							BrowserFactory.getDataOwner(node), 
		        			node);
				} else {
					//Import, register and view.
				}
			}
		}
		fireStateChange();
	}
	
	/**
	 * Checks for the images if loaded.
	 * 
	 * @param object The object to handle.
	 * @param objects The list to add the found image if any.
	 * @param content Pass <code>true</code> if the content has to be deleted.
	 * 				  <code>false</code> otherwise.
	 */
	private void checkForImages(TreeImageDisplay object, 
				List<DataObject> objects, boolean content)
	{
		List list = object.getChildrenDisplay();
		Iterator i, j;
		TreeImageDisplay child, child2;
		List children;
		DataObject ho = (DataObject) object.getUserObject();
		if (ho instanceof ImageData) {
			objects.add(ho);
		} else if (ho instanceof DatasetData && content) {
			if (object.isChildrenLoaded()) {
				/*
				i = list.iterator();
				while (i.hasNext()) {
					child = (TreeImageDisplay) i.next();
					if (child.getUserObject() instanceof ImageData) {
						objects.add((DataObject) child.getUserObject());
					}
				}
				*/
				objects.add(ho);
			}
			
		} else if (ho instanceof ProjectData && content) {
			if (object.isChildrenLoaded()) {
				i = list.iterator();
				while (i.hasNext()) {
					child = (TreeImageDisplay) i.next();
					if (child.getUserObject() instanceof DatasetData) {
						if (child.isChildrenLoaded()) {
							/*
							children = child.getChildrenDisplay();
							j = children.iterator();
							while (j.hasNext()) {
								child2 =  (TreeImageDisplay) j.next();
								objects.add(
										(DataObject) child2.getUserObject());
							}
							*/
							objects.add((DataObject) child.getUserObject());
						}
					}
				}
			}
		} else if (ho instanceof PlateData) {
			objects.add(ho);
		} else if (ho instanceof PlateAcquisitionData) {
			TreeImageDisplay parent = object.getParentDisplay();
			objects.add((DataObject) parent.getUserObject());
		} else if (ho instanceof ScreenData && content) {
			if (object.isChildrenLoaded()) {
				i = list.iterator();
				while (i.hasNext()) {
					child = (TreeImageDisplay) i.next();
					if (child.getUserObject() instanceof PlateData) {
						if (child.isChildrenLoaded()) {
							/*
							children = child.getChildrenDisplay();
							j = children.iterator();
							while (j.hasNext()) {
								child2 =  (TreeImageDisplay) j.next();
								objects.add(
										(DataObject) child2.getUserObject());
							}
							*/
							objects.add((DataObject) child.getUserObject());
						}
					}
				}
			}
		}
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#deleteObjects(List)
	 */
	public void deleteObjects(List nodes)
	{
		if (nodes == null) return;
		Iterator i = nodes.iterator();
		TreeImageDisplay node;
		Class type = null;
		boolean ann = false;
		boolean content = false;
		String ns = null;
		while (i.hasNext()) {
			node = (TreeImageDisplay) i.next();
			if (node.isAnnotated()) ann = true;
			if (node.hasChildren()) content = true;
			Object uo = node.getUserObject();
			type = uo.getClass();
			if (uo instanceof TagAnnotationData) 
				ns = ((TagAnnotationData) uo).getNameSpace();
			break;
		}
		ann = true;
		DeleteBox dialog = new DeleteBox(type, ann, content, nodes.size(), ns, 
										view);
		if (dialog.centerMsgBox() == DeleteBox.YES_OPTION) {
			content = dialog.deleteContents();
			List<Class> types = dialog.getAnnotationTypes();
			i = nodes.iterator();
			Object obj;
			List<DeletableObject> l = new ArrayList<DeletableObject>();
			//List<TreeImageDisplay> toRemove = new ArrayList<TreeImageDisplay>();
			DeletableObject d;
			List<DataObject> objects = new ArrayList<DataObject>();
			List<DataObject> values = null;
			List<Long> ids = new ArrayList<Long>();
			Class klass = null;
			while (i.hasNext()) {
				node = (TreeImageDisplay) i.next();
				obj = node.getUserObject();
				if (obj instanceof GroupData || 
						obj instanceof ExperimenterData) {
					if (values == null)
						values = new ArrayList<DataObject>(); 
					values.add((DataObject) obj);
					//toRemove.add(node);
				} else if (obj instanceof DataObject) {
					d = new DeletableObject((DataObject) obj, content);
					if (!(obj instanceof TagAnnotationData || 
							obj instanceof FileAnnotationData)) 
						d.setAttachmentTypes(types);
					checkForImages(node, objects, content);
					l.add(d);
					//toRemove.add(node);
				}
				klass = obj.getClass();
				ids.add(((DataObject) obj).getId());
			}
			if (l.size() > 0) {
				model.setNodesToCopy(null, -1);
				EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
				bus.post(new DeleteObjectEvent(objects));
				
				NodesFinder finder = new NodesFinder(klass, ids);
				Browser browser = model.getSelectedBrowser();
				browser.accept(finder);
				browser.removeTreeNodes(finder.getNodes());
				view.removeAllFromWorkingPane();
				DataBrowserFactory.discardAll();
				model.getMetadataViewer().setRootObject(null, -1);
				IconManager icons = IconManager.getInstance();
				DeleteActivityParam p = new DeleteActivityParam(
						icons.getIcon(IconManager.APPLY_22), l);
				p.setFailureIcon(icons.getIcon(IconManager.DELETE_22));
				UserNotifier un = 
					TreeViewerAgent.getRegistry().getUserNotifier();
				un.notifyActivity(p);
			} 
			if (values != null) {
				NodesFinder finder = new NodesFinder(klass, ids);
				Browser browser = model.getSelectedBrowser();
				browser.accept(finder);
				model.getSelectedBrowser().removeTreeNodes(finder.getNodes());
				view.removeAllFromWorkingPane();
				DataBrowserFactory.discardAll();
				model.getMetadataViewer().setRootObject(null, -1);
				model.fireObjectsDeletion(values);
				fireStateChange();
			}
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#hasDataToCopy()
	 */
	public Class hasDataToCopy()
	{
		if (model.getState() == DISCARDED) return null;
		return model.getDataToCopyType();
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#onNodesMoved()
	 */
	public void onNodesMoved()
	{
		if (model.getState() != SAVE) return;
		model.setState(READY);
		fireStateChange();
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		view.removeAllFromWorkingPane();
		DataBrowserFactory.discardAll();
		
		Browser browser = model.getSelectedBrowser();
		browser.refreshTree(null, null);
		/*
		Map browsers = model.getBrowsers();
		Iterator i = browsers.keySet().iterator();
		while (i.hasNext()) {
			browser = (Browser) browsers.get(i.next());
			browser.refreshTree();
		}
		*/
		//onSelectedDisplay();
		model.getMetadataViewer().setRootObject(null, -1);
		setStatus(false, "", true);
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#onNodesDeleted(Collection)
	 */
	public void onNodesDeleted(Collection<DataObject> deleted)
	{
		if (model.getState() == DISCARDED) return;
		if (deleted == null || deleted.size() == 0) {
			onNodesMoved();
			return;
		}
		NotDeletedObjectDialog nd = new NotDeletedObjectDialog(view, 
								deleted);
		if (nd.centerAndShow() == NotDeletedObjectDialog.CLOSE)
			onNodesMoved();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#openEditorFile(int)
	 */
	public void openEditorFile(int index)
	{
		EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
		Browser browser = model.getSelectedBrowser();
		TreeImageDisplay d;
		Object object;
		switch (index) {
			case WITH_SELECTION:
				if (browser == null) return;
				d  = browser.getLastSelectedDisplay();
				if (d == null) return;
				object = d.getUserObject();
				if (object == null) return;
				if (object instanceof FileAnnotationData) {
					FileAnnotationData fa = 
						(FileAnnotationData) d.getUserObject();
					EditFileEvent evt = new EditFileEvent(fa);
					bus.post(evt);
				}
				break;
			case NO_SELECTION:
				bus.post(new ShowEditorEvent());
				break;
			case NEW_WITH_SELECTION:
				if (browser == null) return;
				d  = browser.getLastSelectedDisplay();
				if (d == null) return;
				object = d.getUserObject();
				TreeImageDisplay parent = d.getParentDisplay();
				Object po = null;
				if (parent != null) po = parent.getUserObject();
				if (object == null) return;
				String name = null;
				if (object instanceof ProjectData)
					name = ((ProjectData) object).getName();
				else if (object instanceof DatasetData) {
					if (po != null && po instanceof ProjectData) {
						name = ((ProjectData) po).getName();
						name += "_";
						name += ((DatasetData) object).getName();
					} else {
						name = ((DatasetData) object).getName();
					}
				} else if (object instanceof ImageData)
					name = ((ImageData) object).getName();
				else if (object instanceof ScreenData)
					name = ((ScreenData) object).getName();
				else if (object instanceof PlateData) {
					if (po != null && po instanceof ScreenData) {
						name = ((ScreenData) po).getName();
						name += "_";
						name += ((PlateData) object).getName();
					} else {
						name = ((PlateData) object).getName();
					}
				}
				if (name != null) {
					name += ShowEditorEvent.EXPERIMENT_EXTENSION;
					ShowEditorEvent event = new ShowEditorEvent(
							(DataObject) object, name, 
							ShowEditorEvent.EXPERIMENT);
					bus.post(event);
				}
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#showTagWizard()
	 */
	public void showTagWizard()
	{
		if (model.getState() == DISCARDED) return;
		model.getMetadataViewer().showTagWizard();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setInspectorVisibility()
	 */
	public void setInspectorVisibility()
	{
		if (model.getState() == DISCARDED) return;
		view.setInspectorVisibility();
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#onActivityProcessed(ActivityComponent, boolean)
	 */
	public void onActivityProcessed(ActivityComponent activity, 
			boolean finished)
	{
		if (model.getState() == DISCARDED) return;
		if (finished) {
			view.onActivityTerminated(activity);
		} 
		//TODO: IF DELETE ACTIVITY NEED TO CLEAN THE BROWSER.
		firePropertyChange(IMPORTED_PROPERTY, Boolean.valueOf(!finished), 
				Boolean.valueOf(finished));
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#download(File, boolean)
	 */
	public void download(File folder)
	{
		if (model.getState() == DISCARDED) return;
		Browser browser = model.getSelectedBrowser();
		if (browser == null) return;
		List l = browser.getSelectedDataObjects();
		if (l == null) return;
		Iterator i = l.iterator();
		Object object;
		List<ImageData> archived = new ArrayList<ImageData>();
		boolean override = l.size() > 1;
		ImageData image;
		while (i.hasNext()) {
			object = i.next();
			if (object instanceof ImageData) {
				image = (ImageData) object;
				if (image.isArchived()) archived.add(image);
			} else if (object instanceof FileAnnotationData) {
				downloadFile(folder, override, (FileAnnotationData) object, 
						null);
			}
		}
		if (archived.size() > 0) {
			model.downloadImages(archived, folder, null);
		}
	}

	/**
	 * Downloads the documents.
	 * 
	 * @param folder The folder where to download the file.
	 * @param data	 The application to open the document with or 
	 * 				 <code>null</code>.
	 */
	private void download(File folder, ApplicationData data)
	{
		Browser browser = model.getSelectedBrowser();
		if (browser == null) return;
		List l = browser.getSelectedDataObjects();
		if (l == null) return;
		Iterator i = l.iterator();
		Object object;
		List<ImageData> archived = new ArrayList<ImageData>();
		List<ImageData> notArchived = new ArrayList<ImageData>();
		boolean override = l.size() > 1;
		ImageData image;
		while (i.hasNext()) {
			object = i.next();
			if (object instanceof ImageData) {
				image = (ImageData) object;
				if (image.isArchived()) archived.add(image);
				else notArchived.add(image);
			} else if (object instanceof FileAnnotationData) {
				downloadFile(folder, override, (FileAnnotationData) object, 
						data);
			}
		}
		if (archived.size() > 0) {
			model.downloadImages(archived, folder, data);
		}
		if (notArchived.size() > 0) {
			image = notArchived.get(0);
			try {
				File f = new File(
						folder.getAbsolutePath()+File.separator+image.getName()+".ome.tiff");
				TreeViewerAgent.getRegistry().getImageService().exportImageAsOMETiff(
						image.getId(), f);
				TreeViewerAgent.getRegistry().getUserNotifier().openApplication(
						data, f.getAbsolutePath());
			} catch (Exception e) {
			}
		}
	}
	
	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setDownloadedFiles(File, ApplicationData, Collection)
	 */
	public void setDownloadedFiles(File folder, ApplicationData data, 
			Collection files)
	{
		if (files == null || files.size() == 0) return;
		UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
		IconManager icons = IconManager.getInstance();
		Iterator i = files.iterator();
		OriginalFile file;
		DownloadActivityParam activity;
		while (i.hasNext()) {
			file = (OriginalFile) i.next();
			activity = new DownloadActivityParam(file,
					folder, icons.getIcon(IconManager.DOWNLOAD_22));
			activity.setApplicationData(data);
			un.notifyActivity(activity);
		}
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#openWith(ApplicationData)
	 */
	public void openWith(ApplicationData data)
	{
		Browser browser = model.getSelectedBrowser();
		if (browser == null) return;
		if (data != null) {
			Environment env = (Environment) 
				TreeViewerAgent.getRegistry().lookup(LookupNames.ENV);
			String dir = env.getTmpDir();
			List l = browser.getSelectedDataObjects();
			if (l == null) return;
			Iterator i = l.iterator();
			Object object;
			OpenActivityParam activity;
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			while (i.hasNext()) {
				object = i.next();
				if (object instanceof ImageData || 
					object instanceof FileAnnotationData) {
					activity = new OpenActivityParam(data, (DataObject) object, 
							dir);
					un.notifyActivity(activity);
				}
			}
			return;
		}
		TreeImageDisplay d = browser.getLastSelectedDisplay();
		if (d == null) return;
		Object uo = d.getUserObject();
		String name = null;
		if (uo instanceof ImageData) {
			ImageData img = (ImageData) uo;
			name = EditorUtil.getObjectName(img.getName());
		} else if (uo instanceof FileAnnotationData) {
			FileAnnotationData fa = (FileAnnotationData) uo;
			name = EditorUtil.getObjectName(fa.getFileName());
		}
		if (name == null) return;
		OpenWithDialog dialog = new OpenWithDialog(view, 
				ApplicationData.getDefaultLocation(), name);
		dialog.addPropertyChangeListener(controller);
		UIUtilities.centerAndShow(dialog);
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setUserGroup(GroupData)
	 */
	public void setUserGroup(GroupData group)
	{
		if (model.getState() != READY) return;
		if (group == null) return;
		ExperimenterData exp = TreeViewerAgent.getUserDetails();
		long oldId = model.getUserGroupID();
		if (group.getId() == oldId) return;
		Registry reg = TreeViewerAgent.getRegistry();
		reg.getEventBus().post(new SwitchUserGroup(exp, group.getId()));
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setFullScreen()
	 */
	public void setFullScreen()
	{
		/*
		if (model.getState() == DISCARDED) return;
		boolean value = model.isFullScreen();
		model.setFullScreen(!value);
		if (value) view.displayBrowser(model.getDataViewer());
		*/
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setMetadataVisibility()
	 */
	public void setMetadataVisibility()
	{
		if (model.getState() == DISCARDED) return;
		view.setMetadataVisibility();
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getScriptsAsString()
	 */
	public Map<Long, String> getScriptsAsString()
	{
		Registry reg = TreeViewerAgent.getRegistry();
		try {
			//TODO: asynchronous call instead
			return reg.getImageService().getScriptsAsString();
		} catch (Exception e) {
			String s = "Data Retrieval Failure: ";
	        LogMessage msg = new LogMessage();
	        msg.print(s);
	        msg.print(e);
	        reg.getLogger().error(this, msg);
		}
		return new HashMap<Long, String>();
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#uploadScript(ScriptObject)
	 */
	public boolean isLeaderOfSelectedGroup()
	{
		if (model.getState() == DISCARDED) return false;
		return model.isLeaderOfSelectedGroup();
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#isLeaderOfGroup(GroupData)
	 */
	public boolean isLeaderOfGroup(GroupData group)
	{
		if (model.getState() == DISCARDED || group == null) return false;
		Set groups = TreeViewerAgent.getGroupsLeaderOf();
		if (groups.size() == 0) return false;
		Iterator i = groups.iterator();
		GroupData g;
		while (i.hasNext()) {
			g = (GroupData) i.next();
			if (g.getId() == group.getId())
				return true;
		}
		return false;
	}
	
	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#administrate(AdminObject)
	 */
	public void administrate(AdminObject object)
	{
		if (model.getState() == DISCARDED) return;
		if (object == null)
			throw new IllegalArgumentException("Object not valid.");
		model.fireAdmin(object);
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#register(DataObjectRegistration)
	 */
	public void register(DataObjectRegistration file)
	{
		if (model.getState() == DISCARDED) return;
		if (file == null)
			throw new IllegalArgumentException("No file to register.");
		Browser browser = model.getSelectedBrowser();
		if (browser == null || browser.getBrowserType() != 
			Browser.FILE_SYSTEM_EXPLORER) return;
		if (browser.register(file.getData())) 
			model.getMetadataViewer().saveData(file.getToAdd(), 
					file.getToRemove(), file.getToDelete(), 
					file.getMetadata(), file.getData(), true);
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getSelectedGroupPermissions()
	 */
	public int getSelectedGroupPermissions()
	{
		GroupData group = getSelectedGroup();
		int level = AdminObject.PERMISSIONS_PRIVATE;
		if (group != null) {
			PermissionData data = group.getPermissions();
			if (data.isGroupRead()) {
				if (data.isGroupWrite()) 
					level = AdminObject.PERMISSIONS_GROUP_READ_LINK;
				else level = AdminObject.PERMISSIONS_GROUP_READ;
			} else if (data.isWorldRead()) {
				if (data.isWorldWrite()) 
					level = AdminObject.PERMISSIONS_PUBLIC_READ_WRITE;
				else level = AdminObject.PERMISSIONS_PUBLIC_READ;
			}
		}
		return level;
	}
	
	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getSelectedGroup()
	 */
	public GroupData getSelectedGroup()
	{
		Set m = TreeViewerAgent.getAvailableUserGroups();
		Iterator i = m.iterator();
		long id = model.getUserGroupID();
		GroupData group = null;
		while (i.hasNext()) {
			group = (GroupData) i.next();
			if (group.getId() == id) {
				return group;
			}
		}
		return null;
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#resetPassword(String)
	 */
	public void resetPassword(String password)
	{
		if (password == null)
			throw new IllegalArgumentException("No password specified.");
		Browser browser = model.getSelectedBrowser();
		if (browser == null) return;
		List l = browser.getSelectedDataObjects();
		if (l == null) return;
		Map<ExperimenterData, UserCredentials>
			map = new HashMap<ExperimenterData, UserCredentials>();
		Iterator i = l.iterator();
		Object o;
		ExperimenterData exp;
		UserCredentials uc;
		while (i.hasNext()) {
			o = i.next();
			if (o instanceof ExperimenterData) {
				exp = (ExperimenterData) o;
				uc = new UserCredentials(exp.getUserName(), password);
				map.put(exp, uc);
			}
		}
		if (map.size() == 0) return;
		AdminObject admin = new AdminObject(null, map, 
				AdminObject.RESET_PASSWORD);
		model.fireAdmin(admin);
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#resetPassword(String)
	 */
	public void onGroupSwitched(boolean success)
	{
		ExperimenterData exp = TreeViewerAgent.getUserDetails();
		GroupData group = exp.getDefaultGroup();
		long oldGroup = model.getUserGroupID();
		if (success) {
			model.setRndSettings(null);
			model.setNodesToCopy(null, -1);
			model.setGroupId(group.getId());
			view.removeAllFromWorkingPane();
			model.setDataViewer(null);
			model.getMetadataViewer().onGroupSwitched(success);
			//model.resetMetadataViewer();
			Map<Integer, Browser> browsers = model.getBrowsers();
			Entry entry;
			Browser browser;
			Iterator i = browsers.entrySet().iterator();
			while (i.hasNext()) {
				entry = (Entry) i.next();
				browser = (Browser) entry.getValue();
				browser.reActivate();
			}
			firePropertyChange(GROUP_CHANGED_PROPERTY, oldGroup, 
					model.getUserGroupID());
		}
	}
	
	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#findDataObject(Class, int, boolean)
	 */
	public void findDataObject(Class type, long id, boolean selectTab)
	{
		Browser browser = null;
		if (selectTab) {
			if (ProjectData.class.equals(type) || 
					DatasetData.class.equals(type)) {
				view.selectPane(Browser.PROJECTS_EXPLORER);
				browser = model.getBrowser(Browser.PROJECTS_EXPLORER);
				model.setSelectedBrowser(browser);
			} else if (ScreenData.class.equals(type) ||
				PlateData.class.equals(type)) {
				view.selectPane(Browser.SCREENS_EXPLORER);
				browser = model.getBrowser(Browser.SCREENS_EXPLORER);
				model.setSelectedBrowser(browser);
			}
		}
		if (browser != null) {
			NodesFinder finder = new NodesFinder(type, id);
			browser.accept(finder);
			Set<TreeImageDisplay> nodes = finder.getNodes();
			if (nodes.size() == 0) { //not found so, reloads the data.
				if (ProjectData.class.equals(type) || 
					DatasetData.class.equals(type) || 
					ScreenData.class.equals(type)) {
					DataBrowserFactory.discardAll();
				    view.removeAllFromWorkingPane();
			        browser.refreshBrowser(type, id);
			        ExperimenterData exp = model.getUserDetails();
			        model.getMetadataViewer().setRootObject(null, exp.getId());
				}
			} else {
				/*
				Iterator<TreeImageDisplay> i = nodes.iterator();
				TreeImageDisplay node;
				if (DatasetData.class.equals(type)) {
					while (i.hasNext()) {
						node = i.next();
						if (node.isChildrenLoaded())
							browser.setSelectedDisplay(node);
						else browser.refreshBrowser(type, id);
					}
				} else {
					while (i.hasNext()) {
						browser.setSelectedDisplay(i.next());
					}
				}
				*/
				Iterator<TreeImageDisplay> i = nodes.iterator();
				TreeImageDisplay node;
				while (i.hasNext()) {
					node = i.next();
					browser.setSelectedDisplay(node);
					browser.onSelectedNode(null, node, false);
					browseContainer(node, null);
					break;
				}
			}
		}
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#isImporting()
	 */
	public boolean isImporting()
	{
		if (model.getState() == DISCARDED) return false;
		return model.isImporting();
	}
	
	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#indicateToRefresh(List, boolean)
	 */
	public void indicateToRefresh(List<DataObject> containers, 
			boolean refreshTree)
	{
		TreeImageDisplay node;
		Browser browser = null;
		if (containers != null && containers.size() > 0) {
			NodesFinder finder = new NodesFinder(containers);
			DataObject ho = containers.get(0);
			if (ho instanceof DatasetData || ho instanceof ProjectData) {
				browser = model.getBrowser(Browser.PROJECTS_EXPLORER);
				browser.accept(finder);
			} else if (ho instanceof ScreenData) {
				browser = model.getBrowser(Browser.SCREENS_EXPLORER);
				browser.accept(finder);
			}
			Set<TreeImageDisplay> nodes = finder.getNodes();
			//mark 
			if (browser != null && nodes != null && nodes.size() > 0) {
				Iterator<TreeImageDisplay> i = nodes.iterator();
				TreeImageDisplay parent;
				while (i.hasNext()) {
					node = i.next();
					node.setToRefresh(true);
					parent = node.getParentDisplay();
					if (parent != null && !parent.isExpanded())
						browser.expand(parent);
				}
				browser.getUI().repaint();
			}
			/*
			if (browser == null) return;
			Set<TreeImageDisplay> nodes = finder.getNodes();
			//mark 
			if (nodes != null && nodes.size() > 0) {
				Iterator<TreeImageDisplay> i = nodes.iterator();
				TreeImageDisplay parent;
				while (i.hasNext()) {
					node = i.next();
					node.setToRefresh(true);
					parent = node.getParentDisplay();
					if (parent != null && !parent.isExpanded())
						browser.expand(parent);
				}
				browser.getUI().repaint();
			} else {
				node = browser.getLoggedExperimenterNode();
				if (node != null) {
					node.setToRefresh(refreshTree);
					browser.getUI().repaint();
				}
			}
			node = browser.getLoggedExperimenterNode();
			if (node != null) {
				node.setToRefresh(refreshTree);
				browser.getUI().repaint();
			}
			*/
		} 
		/*else {
			browser = model.getSelectedBrowser();
			if (browser != null) {
				node = browser.getLoggedExperimenterNode();
				if (node != null) {
					node.setExpanded(true);
					node.setToRefresh(refreshTree);
					browser.getUI().repaint();
				}
			}
		}
		*/
		if (browser == null) browser = model.getSelectedBrowser();
		if (browser != null) {
			node = browser.getLoggedExperimenterNode();
			if (node != null) {
				node.setExpanded(true);
				node.setToRefresh(refreshTree);
				browser.getUI().repaint();
			}
		}
	}
	
	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setImporting(boolean, List, boolean)
	 */
	public void setImporting(boolean importing, List<DataObject> containers, 
			boolean refreshTree)
	{
		if (model.getState() == DISCARDED) return;
		model.setImporting(importing);
		if (!importing)
			indicateToRefresh(containers, refreshTree);
		firePropertyChange(IMPORT_PROPERTY, importing, !importing);
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#browseContainer(Object, Object)
	 */
	public void browseContainer(Object data, Object refNode)
	{
		if (model.getState() == DISCARDED) return;
		if (data == null) return;
		Browser browser = null;
		NodesFinder finder;
		Set<TreeImageDisplay> nodes;
		Iterator<TreeImageDisplay> i;
		TreeImageDisplay n;
		moveToFront();
		if (data instanceof TreeImageDisplay) {
			TreeImageDisplay node = (TreeImageDisplay) data;
			Object ho = node.getUserObject();
			if (ho instanceof DatasetData || ho instanceof ProjectData) {
				browser = model.getBrowser(Browser.PROJECTS_EXPLORER);
				if (browser != null) {
					setSelectedBrowser(browser, false);
					finder = new NodesFinder((DataObject) ho);
					browser.accept(finder);
					nodes = finder.getNodes();
					if (nodes.size() > 0) { //not found.
						i = nodes.iterator();
						while (i.hasNext()) {
							n = i.next();
							if (n == node) {
								browse(node, null, true);
								node.setToRefresh(false);
							}
						}
					}
				}
			} else if (ho instanceof ScreenData) {
				browser = model.getBrowser(Browser.SCREENS_EXPLORER);
				DataBrowserFactory.discardAll();
				setSelectedBrowser(browser, false);
			    //view.removeAllFromWorkingPane();
			    browser.refreshTree(refNode, (DataObject) ho);
			}
			
		} else if (data instanceof DataObject) { //should be for new node
			if (data instanceof DatasetData || data instanceof ProjectData ||
				data instanceof ImageData) {
				browser = model.getBrowser(Browser.PROJECTS_EXPLORER);
			} else if (data instanceof PlateData ||
					data instanceof ScreenData) {
				browser = model.getBrowser(Browser.SCREENS_EXPLORER);
			} else if (data instanceof FileAnnotationData) {
				model.loadParentOf((FileAnnotationData) data);
				return;
			}
			if (browser != null) {
				DataBrowserFactory.discardAll();
				if (data instanceof ImageData)
					view.removeAllFromWorkingPane();
				setSelectedBrowser(browser, false);
				browser.refreshTree(refNode, (DataObject) data);
			}
		}
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#activateUser(ExperimenterData)
	 */
	public void activateUser(ExperimenterData exp)
	{
		if (exp == null)
			throw new IllegalArgumentException("No password specified.");
		Browser browser = model.getSelectedBrowser();
		if (browser == null) return;
		List l = browser.getSelectedDataObjects();
		if (l == null) return;
		Map<ExperimenterData, UserCredentials>
			map = new HashMap<ExperimenterData, UserCredentials>();
		UserCredentials uc = new UserCredentials(exp.getUserName(), "");
		uc.setActive(!exp.isActive());
		map.put(exp, uc);
		if (map.size() == 0) return;
		AdminObject admin = new AdminObject(null, map, 
				AdminObject.ACTIVATE_USER);
		model.fireAdmin(admin);
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#createDataObjectWithChildren(DataObject)
	 */
	public void createDataObjectWithChildren(DataObject data)
	{
		if (data == null) return;
		Browser browser = model.getSelectedBrowser();
		if (browser == null) return;
		TreeImageDisplay[] selection = browser.getSelectedDisplays();
		if (selection.length == 0) return;
		if (data instanceof DatasetData) {
			Set images = new HashSet();
			Object ho; 
			for (int i = 0; i < selection.length; i++) {
				ho = selection[i].getUserObject();
				if (ho instanceof ImageData && isUserOwner(ho))
					images.add(ho);
			}
			if (images.size() == 0) {
				UserNotifier un = 
					DataBrowserAgent.getRegistry().getUserNotifier();
				un.notifyInfo("Dataset Creation", 
						"No images to add to the dataset.");
			}
			model.fireDataSaving(data, images);
		}
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setAvailableScripts(List, Point)
	 */
	public void setAvailableScripts(List scripts, Point location)
	{
		if (model.getState() == DISCARDED) return;
		model.setAvailableScripts(scripts);
		firePropertyChange(SCRIPTS_LOADED_PROPERTY,
				Boolean.valueOf(false), Boolean.valueOf(true));
		if (location != null)
			view.showMenu(AVAILABLE_SCRIPTS_MENU, null, location);
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#loadScript(long)
	 */
	public void loadScript(long scriptID)
	{
		if (scriptID < 0) return;
		model.loadScript(scriptID);
		firePropertyChange(SCRIPTS_LOADING_PROPERTY, 
				Boolean.valueOf(false), Boolean.valueOf(true));
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setScript(ScriptObject)
	 */
	public void setScript(ScriptObject script)
	{
		firePropertyChange(SCRIPTS_LOADED_PROPERTY, 
				Boolean.valueOf(false), Boolean.valueOf(true));
		if (script == null) return;
		model.setScript(script);
		Browser browser = model.getSelectedBrowser();
		List<DataObject> objects;
		if (browser == null) objects = new ArrayList<DataObject>();
		else objects = browser.getSelectedDataObjects();

		//setStatus(false);
		if (scriptDialog == null) {
			scriptDialog = new ScriptingDialog(view, 
					model.getScript(script.getScriptID()), objects, 
					TreeViewerAgent.isBinaryAvailable());
			scriptDialog.addPropertyChangeListener(controller);
			UIUtilities.centerAndShow(scriptDialog);
		} else {
			scriptDialog.reset(model.getScript(script.getScriptID()), objects);
			if (!scriptDialog.isVisible())
				UIUtilities.centerAndShow(scriptDialog);
		}
	}

	
	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#transfer(TreeImageDisplay, List, int)
	 */
	public void transfer(TreeImageDisplay target, List<TreeImageDisplay> nodes,
			int transferAction)
	{
		if (target == null || nodes == null || nodes.size() == 0) return;
		Browser browser = model.getSelectedBrowser();
		if (browser == null) return;
		Object ot = target.getUserObject();
		Object os;
		if (nodes.size() == 1) { //check if src = destination
			TreeImageDisplay src = nodes.get(0);
			if (src == target) {
				browser.rejectTransfer();
				return;
			}
		}
		UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
		if (browser.getBrowserType() == Browser.ADMIN_EXPLORER) {
			if (!(ot instanceof GroupData) ||
					!TreeViewerAgent.isAdministrator()) {
				un.notifyInfo("DnD", "Only administrator can perform" +
						"such action.");
				browser.rejectTransfer();
				return;
			}
		}
		if (!isUserOwner(ot)) {
			un.notifyInfo("DnD", 
					"You must be the owner of the container.");
			browser.rejectTransfer();
			return;
		}
		List<TreeImageDisplay> list = new ArrayList<TreeImageDisplay>();
		Iterator<TreeImageDisplay> i = nodes.iterator();
		TreeImageDisplay n;
		
		int count = 0;
		String child;
		String parent;
		os = null;
		int childCount = 0;
		while (i.hasNext()) {
			n = i.next();
			os = n.getUserObject();
			if (target.contains(n)) {
				childCount++;
			} else {
				if (EditorUtil.isTransferable(ot, os)) {
					count++;
					if (ot instanceof GroupData) {
						if (TreeViewerAgent.isAdministrator())
							list.add(n);
					} else {
						if (isUserOwner(os)) list.add(n);
					}
				}
			}
		}
		if (childCount == nodes.size()) {
			browser.rejectTransfer();
			return;
		}
		if (list.size() == 0) {
			String s = "";
			if (nodes.size() > 1) s = "s";
			un.notifyInfo("DnD", 
			"The "+getObjectType(os)+s+" cannot be moved to the selected "+
				getObjectType(ot)+".");
			browser.rejectTransfer();
			return;
		}
		model.transfer(target, list);
	}

}
