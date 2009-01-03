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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserFactory;
import org.openmicroscopy.shoola.agents.events.SaveData;
import org.openmicroscopy.shoola.agents.events.iviewer.CopyRndSettings;
import org.openmicroscopy.shoola.agents.events.iviewer.RndSettingsCopied;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerFactory;
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerTranslator;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.agents.treeviewer.finder.ClearVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.finder.Finder;
import org.openmicroscopy.shoola.agents.treeviewer.util.AddExistingObjectsDialog;
import org.openmicroscopy.shoola.agents.treeviewer.util.GenericDialog;
import org.openmicroscopy.shoola.agents.util.DataHandler;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.classifier.view.Classifier;
import org.openmicroscopy.shoola.agents.util.tagging.view.Tagger;
import org.openmicroscopy.shoola.agents.util.tagging.view.TaggerFactory;
import org.openmicroscopy.shoola.agents.util.ui.UserManagerDialog;
import org.openmicroscopy.shoola.env.data.events.ExitApplication;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

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

	/** 
	 * Displays the user groups.
	 * 
	 * @param map 	The map whose key is a <code>GroupData</code>s
	 * 				and the value a collection of 
	 * 				<code>ExperimenterData</code>s.
	 */
	private void displayUserGroups(Map map)
	{
		if (switchUserDialog == null) {
			JFrame f = (JFrame) TreeViewerAgent.getRegistry().getTaskBar();
			IconManager icons = IconManager.getInstance();
			switchUserDialog = new UserManagerDialog(f, model.getUserDetails(), 
					map, icons.getIcon(IconManager.OWNER), 
					icons.getIcon(IconManager.OWNER_48));
			switchUserDialog.addPropertyChangeListener(controller);
			//switchUserDialog.pack();
			switchUserDialog.setDefaultSize();
		}
		UIUtilities.centerAndShow(switchUserDialog);
	}
	
	/**
	 * Displays the data browser corresponding to the passed node.
	 * 
	 * @param object 	The object of reference.
	 * @param display	The node to handle.
	 */
	private void showDataBrowser(Object object, TreeImageDisplay display)
	{
		DataBrowser db;
		TreeImageDisplay parent = null;
		Browser browser = model.getSelectedBrowser();
		if (display != null) parent = display.getParentDisplay();
		if (object instanceof ImageData) {
			if (parent != null) {
				Object ho = parent.getUserObject();
				db = DataBrowserFactory.getDataBrowser(ho);
				if (db != null) {
					db.setComponentTitle("");
					view.removeAllFromWorkingPane();
					view.addComponent(db.getUI());
					db.setSelectedNodes(browser.getSelectedDataObjects());
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
	        						db.setSelectedNodes(
	        								browser.getSelectedDataObjects());
	        					} 
	        					else if (object instanceof TagAnnotationData) {
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
		        						db.setSelectedNodes(
		        							browser.getSelectedDataObjects());
	        						}
	        					} 
	        					//else view.removeAllFromWorkingPane();
	        				}
	        			}
					} else
						showDataBrowser(object, parent.getParentDisplay());
				}
			} else {
				view.removeAllFromWorkingPane();
			}
		} else {
        	db = DataBrowserFactory.getDataBrowser(object);
        	if (db != null) {
        		db.setComponentTitle("");
        		view.removeAllFromWorkingPane();
        		view.addComponent(db.getUI());
        		if (object instanceof DataObject) {
        			List<DataObject> nodes = new ArrayList<DataObject>();
            		nodes.add((DataObject) object);
            		db.setSelectedNodes(nodes);
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
        						db.setSelectedNodes(
        								browser.getSelectedDataObjects());
        					}
        					else if (object instanceof TagAnnotationData) {
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
            						db.setSelectedNodes(
            								browser.getSelectedDataObjects());
        						}
        					} 
        				}
        			}// else showDataBrowser(object, parent);
        		}// else view.removeAllFromWorkingPane();
        	}
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
	 * Sets the ids used to copy rendering settings.
	 * 
	 * @param pixelsID	The id of the pixels set of reference.
	 */
	void setRndSettings(long pixelsID)
	{
		if (model.getState() == DISCARDED) return;
		model.setRndSettings(pixelsID);
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
				model.getSelectedBrowser().activate(); 
				view.setOnScreen();
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
	public Map getBrowsers() { return model.getBrowsers(); }

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
	 * @see TreeViewer#setSelectedBrowser(Browser)
	 */
	public void setSelectedBrowser(Browser browser)
	{
		switch (model.getState()) {
		case DISCARDED:
		case SAVE:
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED or SAVE " +
			"state.");
		}
		Browser oldBrowser = model.getSelectedBrowser();
		if (oldBrowser == null || !oldBrowser.equals(browser)) {
			model.setSelectedBrowser(browser);
			if (browser != null) browser.activate();
			removeEditor();
			model.getMetadataViewer().setSelectionMode(false);
			firePropertyChange(SELECTED_BROWSER_PROPERTY, oldBrowser, browser);
		}
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
		Map browsers = model.getBrowsers();
		Browser browser = (Browser) browsers.get(browserType);
		if (browser.isDisplayed()) {
			view.removeBrowser(browser);
		} else {
			model.setSelectedBrowser(browser);
			view.addBrowser(browser);
		}
		browser.setDisplayed(!browser.isDisplayed());
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
		EditorDialog d = new EditorDialog(view, object, withParent);
		d.addPropertyChangeListener(controller);
		UIUtilities.centerAndShow(d);
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
		firePropertyChange(REMOVE_EDITOR_PROPERTY, Boolean.FALSE, Boolean.TRUE);
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
	 * @see TreeViewer#removeObjects(List)
	 */
	public void removeObjects(List nodes)
	{
		/*
      switch (model.getState()) {
          case READY:
          case NEW:  
          case LOADING_THUMBNAIL:
              break;
          default:
              throw new IllegalStateException("This method should only be " +
              "invoked in the READY or NEW state.");
      }
		 */
		/*
		if (nodes == null) return;
		boolean askQuestion = true;
		Iterator i = nodes.iterator();
		TreeImageDisplay node;
		while (i.hasNext()) {
			node = (TreeImageDisplay) i.next();
			if (node.getNumberOfItems() > 0) askQuestion = true;
		}
		if (askQuestion) {
			DeleteBox dialog = new DeleteBox(view);
			if (dialog.centerMsgBox() == DeleteBox.YES_OPTION) {
				model.fireDataObjectsDeletion(nodes);
				fireStateChange();
			}
		} else {
			model.fireDataObjectsDeletion(nodes);
			fireStateChange();
		}
		*/
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#classify(Set, int)
	 */
	public void classify(Set<ImageData> images, int mode)
	{
		switch (model.getState()) {
		//case READY:
		case DISCARDED:
			throw new IllegalStateException("This method should cannot " +
			"be invoked in the DISCARDED state.");
		}

		if (images == null) 
			throw new IllegalArgumentException("Object cannot be null.");
		if (images.size() == 0)
			throw new IllegalArgumentException("No images to classify or " +
			"declassify.");
		if (mode == Classifier.CLASSIFY_MODE) {
			Iterator i = images.iterator();
			Set<Long> ids = new HashSet<Long>(images.size());
			while (i.hasNext()) 
				ids.add(((ImageData) i.next()).getId());
			
			Tagger tagger = TaggerFactory.getImageTagger(
								TreeViewerAgent.getRegistry(), ids);
			if (tagger != null) {
				tagger.addPropertyChangeListener(controller);
				
				tagger.activate();
				UIUtilities.centerAndShow(tagger.getUI());
			}
		} else {
			DataHandler dh = model.classifyImageObjects(view, images, mode);
			dh.addPropertyChangeListener(controller);
			dh.activate();
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#onSelectedDisplay()
	 */
	public void onSelectedDisplay()
	{
		switch (model.getState()) {
			case DISCARDED:
			case SAVE:  
				throw new IllegalStateException("This method cannot be " +
				"invoked in the DISCARDED, SAVE state.");
		}
	
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
        	 if (!time.containsImages()) {
        		 metadata.setRootObject(null);
        		 return;
        	 }
        } 
        metadata.setSelectionMode(single);
        //TODO: handle TreeImageSet
        if (display != null) { // && !(display instanceof TreeImageTimeSet)) {
        	  Object object = display.getUserObject();
        	  metadata.setRootObject(object);
        	  if (!single) {
        		  List l = new ArrayList();
        		  Object child;
        		  for (int i = 0; i < selection.length; i++) {
        			  child = selection[i].getUserObject();
        			  if (!child.equals(object)) {
        				  l.add(child);
        			  }
        		  }
        		  if (l.size() > 0)
        		  	metadata.setRelatedNodes(l);
        	  }
              showDataBrowser(object, display);
        } else metadata.setRootObject(null);
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
						"The selected image is not valid");
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
		
		Collection siblings = (Collection) l.get(0);;
		
		
		int size = siblings.size();
		if (view.getDisplayMode() != SEARCH_MODE) {
			Browser browser = model.getSelectedBrowser();
			browser.onSelectedNode(parent, selected, size > 0);
		}
		
		
		mv.setSelectionMode(size == 0);
		mv.setRootObject(selected);
		if (size > 0) mv.setRelatedNodes(siblings);
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
					browser = model.getBrowser(Browser.PROJECT_EXPLORER);
				 else if (data instanceof ScreenData)
					browser = model.getBrowser(Browser.SCREENS_EXPLORER);
				else if (data instanceof TagAnnotationData)
					browser = model.getBrowser(Browser.TAGS_EXPLORER);
			}
			if (browser != null) {
				model.setSelectedBrowser(browser);
				view.addBrowser(browser);
			}
		}
		browser = model.getSelectedBrowser();
		browser.refreshEdition(data, parent, operation);
		
		if (operation == REMOVE_OBJECT || operation == CREATE_OBJECT) {
			DataBrowserFactory.discardAll();
			view.removeAllFromWorkingPane();
		}
	    
		//Browser browser = model.getSelectedBrowser();
		//browser.refreshEdition(data, operation);
		//browser.refreshLoggedExperimenterData();
		/*
		if (operation == UPDATE_OBJECT) {
			Map browsers = model.getBrowsers();
			Iterator i = browsers.keySet().iterator();
			while (i.hasNext()) {
				browser = (Browser) browsers.get(i.next());
				if (!(browser.equals(model.getSelectedBrowser())))
					browser.refreshEdition(data, operation);
			}
		}
		*/
		//onSelectedDisplay();
		
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
		//TODO
		Browser browser = model.getSelectedBrowser();
		//browser.refreshEdition(data, operation);
		
		setStatus(false, "", true);
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#onNodesRemoved()
	 */
	public void onNodesRemoved()
	{
		if (model.getState()!= SAVE)
			throw new IllegalStateException("This method can only be " +
			"invoked in the SAVE state");
		model.setState(READY);
		fireStateChange();
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		view.removeAllFromWorkingPane();
		DataBrowserFactory.discardAll();
		Map browsers = model.getBrowsers();
		Browser browser;
		//REview that code depending on the type of objects deleted.
		Iterator i = browsers.keySet().iterator();
		while (i.hasNext()) {
			browser = (Browser) browsers.get(i.next());
			browser.refreshTree();
		}
		//onSelectedDisplay();
		model.getMetadataViewer().setRootObject(null);
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
			throw new IllegalStateException("This method cannot be " +
					"invoked in the DISCARDED, SAVE or LOADING_THUMBNAIL " +
			"state");
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
	 * @see TreeViewer#onImageClassified(ImageData[], Set, int)
	 */
	public void onImageClassified(ImageData[] images, Set categories, int mode)
	{
		switch (model.getState()) {
		case DISCARDED:
			throw new IllegalStateException("This method cannot be " +
					"invoked in the DISCARDED, SAVE or LOADING_THUMBNAIL " +
			"state");
		}
		if (categories == null)
			throw new IllegalArgumentException("Categories shouln't be null.");
		if (images == null)
			throw new IllegalArgumentException("No image.");
		if (images.length == 0)
			throw new IllegalArgumentException("No image.");
		/*
		if (mode != Classifier.CLASSIFY_MODE && 
				mode != Classifier.DECLASSIFY_MODE)
			throw new IllegalArgumentException("Classification mode not " +
			"supported.");
			*/
		TreeImageDisplay d = getSelectedBrowser().getLastSelectedDisplay();
		Map browsers = model.getBrowsers();
		Iterator b = browsers.keySet().iterator();
		Browser browser;
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		while (b.hasNext()) {
			browser = (Browser) browsers.get(b.next());
			browser.refreshTree();
			//browser.refreshClassification(images, categories, mode);
		}
		getSelectedBrowser().setSelectedDisplay(d);

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
		Map browsers = model.getBrowsers();
		Iterator i = browsers.keySet().iterator();
		Browser browser;
		Browser selected = model.getSelectedBrowser();
		while (i.hasNext()) {
			browser = (Browser) browsers.get(i.next());
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
		long groupId = model.getUserGroupID();
		return EditorUtil.isWritable(ho, id, groupId);
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
	 * @see TreeViewer#setExistingObjects(Set)
	 */
	public void setExistingObjects(Set nodes)
	{
		if (model.getState() != LOADING_DATA)
			throw new IllegalStateException(
					"This method cannot be invoked in the LOADING_DATA state.");
		if (nodes == null)
			throw new IllegalArgumentException("Nodes cannot be null.");
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		Set n = TreeViewerTranslator.transformIntoCheckNodes(nodes, 
				getUserDetails().getId(), model.getUserGroupID());
		model.setState(LOADING_SELECTION);
		AddExistingObjectsDialog 
		dialog = new AddExistingObjectsDialog(view, n);
		dialog.addPropertyChangeListener(controller);
		UIUtilities.centerAndShow(dialog);  
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
		else model.fireAddExistingObjects(set);
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
			case CREATE_MENU:  
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
		Boolean oldValue = Boolean.TRUE;
		if (b) oldValue = Boolean.FALSE;
		view.onStateChanged(b);
		firePropertyChange(ON_COMPONENT_STATE_CHANGED_PROPERTY, oldValue, 
				new Boolean(b));
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
		//controller.getAction(TreeViewerControl.PASTE_OBJECT).setEnabled(true);
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
			"the nodes to copy into"); 
		}
		TreeImageDisplay[] nodes = model.getNodesToCopy();
		if (nodes == null || nodes.length == 0) return; //shouldn't happen
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
	 * @see TreeViewer#annotate(Class, Set)
	 */
	public void annotate(Class klass, Set<DataObject> nodes)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked " +
			"in the DISCARDED state.");
		if (nodes == null)
			throw new IllegalArgumentException("No dataObject to annotate");
		if (ImageData.class.equals(klass) || DatasetData.class.equals(klass)) {
			DataHandler dh = model.annotateDataObjects(view, klass, nodes);
			dh.addPropertyChangeListener(controller);
			dh.activate();
		}
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
	 * @see TreeViewer#retrieveUserGroups()
	 */
	public void retrieveUserGroups()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		displayUserGroups(model.getAvailableUserGroups());
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
	 * @see TreeViewer#annotateChildren(Class, Set)
	 */
	public void annotateChildren(Class klass, Set<DataObject> nodes)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		if (nodes == null || nodes.size() == 0)
			throw new IllegalArgumentException("No specified container.");
		if (DatasetData.class.equals(klass)) {
			DataHandler dh = model.annotateChildren(view, klass, nodes);
			dh.addPropertyChangeListener(controller);
			dh.activate();
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#classifyChildren(Class, Set)
	 */
	public void classifyChildren(Class klass, Set<DataObject> nodes)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		if (nodes == null || nodes.size() == 0)
			throw new IllegalArgumentException("No specified container.");
		if (DatasetData.class.equals(klass)) {
			Iterator i = nodes.iterator();
			Set<Long> ids = new HashSet<Long>(nodes.size());
			while (i.hasNext()) 
				ids.add(((DataObject) i.next()).getId());
			
			Tagger tagger = TaggerFactory.getContainerTagger(
								TreeViewerAgent.getRegistry(), ids, klass, 
									Tagger.BULK_TAGGING_MODE);
			if (tagger != null) {
				tagger.addPropertyChangeListener(controller);
				
				tagger.activate();
				UIUtilities.centerAndShow(tagger.getUI());
			}
		}
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
	 * @see TreeViewer#isReadable(DataObject)
	 */
	public boolean isReadable(DataObject ho)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		//Check if current user can write in object
		long id = model.getUserDetails().getId();
		long groupId = model.getUserGroupID();
		return EditorUtil.isReadable(ho, id, groupId);
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
		Map browsers = model.getBrowsers();
		Iterator i = browsers.keySet().iterator();
		while (i.hasNext()) {
			browser = (Browser) browsers.get(i.next());
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
		Collection failure = (Collection) map.get(Boolean.FALSE);
		EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
		bus.post(new RndSettingsCopied((Collection) map.get(Boolean.TRUE)));
		
		UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
		model.setState(READY);
		fireStateChange();
		if (failure.size() == 0) {
			un.notifyInfo("Paste settings", "Rendering settings have been " +
			"applied \n to all selected images.");
		} else {
			String s = "";
			Iterator i = failure.iterator();
			int index = 1;
			int size = failure.size();
			while (i.hasNext()) {
				s += (Long) i.next();
				if (index != size) {
					if (index%10 == 0) s += "\n";
					else s += ", ";
				}
				index++;
			}
			s.trim();
			un.notifyInfo("Paste settings", "Rendering settings couldn't be " +
					"applied to the following images: \n"+s);
		}
		model.setState(READY);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#annotate(TimeRefObject)
	 */
	public void annotate(TimeRefObject ref)
	{
		if (ref == null)
			throw new IllegalArgumentException("No time object");
		//TODO: check state
		DataHandler dh = model.annotateDataObjects(view, ref);
		dh.addPropertyChangeListener(controller);
		dh.activate();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#classify(TimeRefObject)
	 */
	public void classify(TimeRefObject ref)
	{
		if (ref == null)
			throw new IllegalArgumentException("No time object");
		Tagger tagger = TaggerFactory.getImageTagger(
				TreeViewerAgent.getRegistry(), ref);
		if (tagger != null) {
			tagger.addPropertyChangeListener(controller);
		
			tagger.activate();
			UIUtilities.centerAndShow(tagger.getUI());
		}
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
		//TODO: check state
		model.fireDataObjectCreation(object, withParent);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setLeaves(TreeImageSet, Set)
	 */
	public void setLeaves(TreeImageSet parent, Set leaves)
	{
		Object parentObject = parent.getUserObject();
		TreeImageDisplay display = parent.getParentDisplay();
		Object grandParentObject = null;
		if (display != null) grandParentObject =  display.getUserObject();
		DataBrowser dataBrowser = DataBrowserFactory.getDataBrowser(
					grandParentObject, parentObject, leaves);
		dataBrowser.addPropertyChangeListener(controller);
		dataBrowser.activate();
		view.removeAllFromWorkingPane();
		view.addComponent(dataBrowser.getUI());
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#browseHierarchyRoots(Object, Set)
	 */
	public void browseHierarchyRoots(Object parent, Set roots)
	{
		if (roots == null) return;
		Iterator i = roots.iterator();
		//Map<ProjectData, Set<DatasetData>>
		DataBrowser db = null;
		if (roots.size() != 1) return;
		DataObject node;
		TagAnnotationData tag, tagImage;
		ProjectData project;
		Iterator j;
		TreeImageDisplay child, value;
		long id; 
		Set set, images;
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
						images = d.getImages();
						if (images != null) {
							value.removeAllChildrenDisplay();
							k = images.iterator();
							while (k.hasNext()) {
								value.addChildDisplay(
								 TreeViewerTranslator.transformDataObject(
										 (ImageData) k.next(), userID, groupID)
										);
							}
						}
						value.setChildrenLoaded(true);
					}
				}
				db = DataBrowserFactory.getDataBrowser(project, set);
			} else if (node instanceof TagAnnotationData) {
				tag = (TagAnnotationData) node;
				set = tag.getTags();
				j = set.iterator();
				while (j.hasNext()) {
					tagImage = (TagAnnotationData) j.next();
					value = m.get(tagImage.getId());
					if (value != null) {
						images = tagImage.getImages();
						if (images != null) {
							value.removeAllChildrenDisplay();
							k = images.iterator();
							while (k.hasNext()) {
								value.addChildDisplay(
								 TreeViewerTranslator.transformDataObject(
										 (ImageData) k.next(), userID, groupID)
										);
							}
						}
						value.setChildrenLoaded(true);
					}
				}
				db = DataBrowserFactory.getDataBrowser(tag, set);
			}
			if (db != null) {
				db.addPropertyChangeListener(controller);
				db.activate();
				view.removeAllFromWorkingPane();
				view.addComponent(db.getUI());
			}
		}
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
		MetadataViewer mv = model.getMetadataViewer();
		//Check siblings first.

		boolean multi = (Boolean) multiSelection;

		browser.onDeselectedNode(parent, selected, (Boolean) multiSelection);
		if (!multi) {
			mv.setRootObject(selected);
			mv.setSelectionMode(true);
		} else {
			TreeImageDisplay[] nodes = browser.getSelectedDisplays();
			
			if (nodes != null && nodes.length == 1) {
				mv.setRootObject(nodes[0].getUserObject());
				mv.setSelectionMode(true);
			} else mv.setRootObject(null);
		}

		/*
		mv.setRootObject(selected);
		l = browser.getSelectedDataObjects();
		if (l != null && l.size() > 0) {
			int size = l.size()-1;
			mv.setRootObject(l.get(size));
			l.remove(l.get(size));
			List<DataObject> siblings = new ArrayList<DataObject>();
			Iterator i = l.iterator();
			Object o;
			while (i.hasNext()) {
				o = i.next();
				if ((o instanceof DataObject) && !o.equals(selected))
					siblings.add((DataObject) o);
			}
			if (siblings.size() > 1)
				mv.setSiblings(siblings);
		} else {
			mv.setRootObject(null);
			mv.setSiblings(null);
		}
		*/
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
			Object o = node.getUserObject();
			if (!(o instanceof ImageData)) return;
			image = (ImageData) o;
		}
		if (image == null) return;
		long pixelsID = image.getDefaultPixels().getId();
		EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
		bus.post(new CopyRndSettings(pixelsID));
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setOriginalRndSettings(List, Class)
	 */
	public void setOriginalRndSettings(List<Long> ids, Class klass)
	{
		if (ids == null || ids.size() == 0) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Set settings", "Please select at one element.");
			return;
		}
		model.fireSetOriginalRenderingSettings(ids, klass);
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
			un.notifyInfo("Set settings", "Please select at one element.");
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
		switch (newMode) {
			case EXPLORER_MODE:
				onSelectedDisplay();
				break;
			case SEARCH_MODE:
				model.getMetadataViewer().setRootObject(null);
				if (db != null) 
					view.addComponent(db.getUI());
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
		if (results == null || results.size() == 0) {
			//UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
        	//un.notifyInfo("Search", "No results found.");
        	view.removeAllFromWorkingPane();
			return;
		}
		//Need to recycle the search browser.
		DataBrowser db = DataBrowserFactory.getSearchBrowser(results);
		if (db != null && view.getDisplayMode() == SEARCH_MODE) {
			db.addPropertyChangeListener(controller);
			db.activate();
			view.removeAllFromWorkingPane();
			view.addComponent(db.getUI());
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
        				time.getStartTime(), time.getEndTime());
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
        if (b != null) b.refreshTree();
        model.getMetadataViewer().setRootObject(null);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#browseTimeInterval(TreeImageTimeSet, Set)
	 */
	public void browseTimeInterval(TreeImageTimeSet parent, Set leaves)
	{
		if (leaves == null) return;
		
		//TODO create db browser for children of parent if any
		Object parentObject = parent.getUserObject();
		TreeImageDisplay display = parent.getParentDisplay();
		Object grandParentObject = null;
		if (display != null) grandParentObject =  display.getUserObject();
		DataBrowser dataBrowser = DataBrowserFactory.getDataBrowser(
					grandParentObject, parentObject, leaves);
		dataBrowser.addPropertyChangeListener(controller);
		dataBrowser.activate();
		view.removeAllFromWorkingPane();
		view.addComponent(dataBrowser.getUI());
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setWells(TreeImageSet, Set)
	 */
	public void setWells(TreeImageSet parent, Set wells)
	{
		Object parentObject = parent.getUserObject();
		TreeImageDisplay display = parent.getParentDisplay();
		Object grandParentObject = null;
		if (display != null) grandParentObject =  display.getUserObject();
		DataBrowser dataBrowser = DataBrowserFactory.getWellsDataBrowser(
					grandParentObject, parentObject, wells);
		dataBrowser.addPropertyChangeListener(controller);
		dataBrowser.activate();
		view.removeAllFromWorkingPane();
		view.addComponent(dataBrowser.getUI());
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#browse(Object)
	 */
	public void browse(TreeImageDisplay node)
	{
		if (node == null) return;
		Object uo = node.getUserObject();
		if (uo instanceof ProjectData) {
			model.browseProject(node);
		} else if (uo instanceof TagAnnotationData) {
			TagAnnotationData tag = (TagAnnotationData) uo;
			Set tags = tag.getTags();
			if (tags != null && tags.size() > 0) {
				model.browseTagset(node);
			}
		} else if (node instanceof TreeImageTimeSet) {
			model.browseTimeInterval((TreeImageTimeSet) node);
		} else if (uo instanceof PlateData) {
			model.browsePlate(node);
		}
		fireStateChange();
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#deleteObjects(List)
	 */
	public void deleteObjects(List nodes)
	{
		/*
      switch (model.getState()) {
          case READY:
          case NEW:  
          case LOADING_THUMBNAIL:
              break;
          default:
              throw new IllegalStateException("This method should only be " +
              "invoked in the READY or NEW state.");
      }
		 */
		if (nodes == null) return;
		Iterator i = nodes.iterator();
		TreeImageDisplay node;
		Class type = null;
		while (i.hasNext()) {
			node = (TreeImageDisplay) i.next();
			type = node.getUserObject().getClass();
			break;
		}
		DeleteBox dialog = new DeleteBox(type, nodes.size(), view);
		if (dialog.centerMsgBox() == DeleteBox.YES_OPTION) {
			boolean ann = dialog.deleteAnnotations();
			boolean content = dialog.deleteContents();
			List<Class> types = dialog.getAnnotationTypes();
			i = nodes.iterator();
			Object obj;
			List<DeletableObject> l = new ArrayList<DeletableObject>();
			DeletableObject d;
			while (i.hasNext()) {
				node = (TreeImageDisplay) i.next();
				obj = node.getUserObject();
				if (obj instanceof DataObject) {
					d = new DeletableObject((DataObject) obj, content, ann);
					d.setAttachmentTypes(types);
					l.add(d);
				}
			}
			
			if (l.size() > 0) {
				model.fireObjectsDeletion(l);
				fireStateChange();
			}
		}
	}
	
}
