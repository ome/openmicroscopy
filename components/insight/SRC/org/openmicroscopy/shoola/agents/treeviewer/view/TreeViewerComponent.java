/*
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

package org.openmicroscopy.shoola.agents.treeviewer.view;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.apache.commons.collections.CollectionUtils;

import omero.model.OriginalFile;

import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserFactory;
import org.openmicroscopy.shoola.agents.events.SaveData;
import org.openmicroscopy.shoola.agents.events.iviewer.CopyRndSettings;
import org.openmicroscopy.shoola.agents.events.iviewer.RndSettingsCopied;
import org.openmicroscopy.shoola.agents.events.treeviewer.ActivitiesEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.BrowserSelectionEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.ChangeUserGroupEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.CopyItems;
import org.openmicroscopy.shoola.agents.events.treeviewer.DeleteObjectEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.DisplayModeEvent;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerFactory;
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.ImageChecker.ImageCheckerType;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ActionCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ExperimenterVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.UpdateVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewInPluginCmd;
import org.openmicroscopy.shoola.agents.treeviewer.finder.ClearVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.finder.Finder;
import org.openmicroscopy.shoola.agents.treeviewer.util.AdminDialog;
import org.openmicroscopy.shoola.agents.treeviewer.util.ChgrpObject;
import org.openmicroscopy.shoola.agents.treeviewer.util.GenericDialog;
import org.openmicroscopy.shoola.agents.treeviewer.util.LinkNotificationDialog;
import org.openmicroscopy.shoola.agents.treeviewer.util.MIFNotificationDialog;
import org.openmicroscopy.shoola.agents.treeviewer.util.MoveGroupSelectionDialog;
import org.openmicroscopy.shoola.agents.treeviewer.util.NotDeletedObjectDialog;
import org.openmicroscopy.shoola.agents.treeviewer.util.OpenWithDialog;
import org.openmicroscopy.shoola.agents.util.DataObjectRegistration;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.agents.util.browser.ContainerFinder;
import org.openmicroscopy.shoola.agents.util.browser.NodesFinder;
import org.openmicroscopy.shoola.agents.util.browser.TreeFileSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeViewerTranslator;
import org.openmicroscopy.shoola.agents.util.finder.AdvancedFinder;
import org.openmicroscopy.shoola.agents.util.ui.EditorDialog;
import org.openmicroscopy.shoola.agents.util.ui.GroupManagerDialog;
import org.openmicroscopy.shoola.agents.util.ui.ScriptingDialog;
import org.openmicroscopy.shoola.agents.util.ui.UserManagerDialog;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.ExitApplication;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.model.DeleteActivityParam;
import org.openmicroscopy.shoola.env.data.model.DownloadActivityParam;
import org.openmicroscopy.shoola.env.data.model.DownloadArchivedActivityParam;
import org.openmicroscopy.shoola.env.data.model.ImageCheckerResult;
import org.openmicroscopy.shoola.env.data.model.OpenActivityParam;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.model.TransferableActivityParam;
import org.openmicroscopy.shoola.env.data.model.TransferableObject;

import omero.gateway.SecurityContext;
import omero.gateway.model.SearchResultCollection;

import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.env.ui.ActivityComponent;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.FileData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PermissionData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.WellData;
import omero.gateway.model.WellSampleData;

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
 * @since OME2.2
 */
class TreeViewerComponent
 	extends AbstractComponent
 	implements TreeViewer
{
  
        /** Warning message shown when the rendering settings are to be reset */
        private static final String RENDERINGSETTINGS_WARNING = "This will save new rendering settings and cannot be undone.";
    
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
	 * Moves the object.
	 * 
	 * @param object The object to move.
	 */
	private void moveObject(ChgrpObject object)
	{
		GroupData group = object.getGroupData();
		SecurityContext ctx = new SecurityContext(group.getId());
		long userID = object.getUserID();
		if (userID >= 0) {
			ExperimenterData exp = model.getExperimenter();
			if (userID == exp.getId()) {
				Class<?> type = object.getDataType();
				if (ImageData.class.equals(type) ||
					DatasetData.class.equals(type)) type = ProjectData.class;
				else if (ProjectData.class.equals(type)) type = null;
				else if (PlateData.class.equals(type)) type = ScreenData.class;
				else if (ScreenData.class.equals(type)) type = null;
				else if (GroupData.class.equals(type) ||
						ExperimenterData.class.equals(type)) type = null;
				if (type != null) {
					MoveGroupSelectionDialog dialog =
							new MoveGroupSelectionDialog(view, userID, object,
									true);
					dialog.addPropertyChangeListener(
							new PropertyChangeListener() {

						/**
						 * Transfers the data.
						 */
						public void propertyChange(PropertyChangeEvent evt) {
							String name = evt.getPropertyName();
							if (MoveGroupSelectionDialog.TRANSFER_PROPERTY.equals(
									name)) {
								ChgrpObject v = (ChgrpObject) evt.getNewValue();
								GroupData group = v.getGroupData();
								SecurityContext ctx = new SecurityContext(
										group.getId());
								moveData(ctx, v.getTarget(),
										v.getTransferable());
							}
						}
					});
					model.fireMoveDataLoading(ctx, dialog, type, userID);
					UIUtilities.centerAndShow(dialog);
				}
				
				
			} else
				moveData(ctx, object.getTarget(),
						object.getTransferable());
		} else {
			moveData(ctx, object.getTarget(), object.getTransferable());
		}
	}
	
	/**
	 * Deletes the specified nodes.
	 * 
	 * @param nodes The nodes to delete.
	 */
	private void delete(List nodes)
	{
		if (nodes == null) return;
		Iterator i = nodes.iterator();
		TreeImageDisplay node;
		Class type = null;
		Boolean ann = null;
		Boolean leader = null;
		String ns = null;
		GroupData group;
		long userID = model.getExperimenter().getId();
		boolean notOwner = false;
		while (i.hasNext()) {
			node = (TreeImageDisplay) i.next();
			if (node.isAnnotated() && ann == null) ann = true;
			Object uo = node.getUserObject();
			type = uo.getClass();
			if (uo instanceof DataObject) {
			    if( leader == null) {
    				group = model.getGroup(((DataObject) uo).getGroupId());
    				if (EditorUtil.isUserGroupOwner(group, userID)) {
    					leader = true;
    				}
			    }
			    if(((DataObject)uo).getOwner().getId()!=userID)
			        notOwner = true;
			}
			if (uo instanceof TagAnnotationData) 
				ns = ((TagAnnotationData) uo).getNameSpace();
		}
		boolean b = false;
		if (ann != null) b = ann.booleanValue();
		boolean le = false;
		if (leader != null) le = leader.booleanValue();
		DeleteBox dialog = new DeleteBox(view, type, b, nodes.size(), ns, le, notOwner);
		if (dialog.centerMsgBox() == DeleteBox.YES_OPTION) {
			boolean content = dialog.deleteContents();
			List<Class> types = dialog.getAnnotationTypes();
			i = nodes.iterator();
			Object obj;
			List<DeletableObject> l = new ArrayList<DeletableObject>();
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
				} else if (obj instanceof DataObject) {
					d = new DeletableObject((DataObject) obj, content);
					if (!(obj instanceof TagAnnotationData || 
							obj instanceof FileAnnotationData)) 
						d.setAttachmentTypes(types);
					checkForImages(node, objects, content);
					l.add(d);
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
				model.getMetadataViewer().setRootObject(null, -1, null);
				IconManager icons = IconManager.getInstance();
				DeleteActivityParam p = new DeleteActivityParam(
						icons.getIcon(IconManager.APPLY_22), l);
				p.setFailureIcon(icons.getIcon(IconManager.DELETE_22));
				UserNotifier un = 
					TreeViewerAgent.getRegistry().getUserNotifier();
				un.notifyActivity(model.getSecurityContext(), p);
			} 
			if (values != null) {
				NodesFinder finder = new NodesFinder(klass, ids);
				Browser browser = model.getSelectedBrowser();
				browser.accept(finder);
				browser.removeTreeNodes(finder.getNodes());
				view.removeAllFromWorkingPane();
				DataBrowserFactory.discardAll();
				model.getMetadataViewer().setRootObject(null, -1, null);
				model.fireObjectsDeletion(values);
				fireStateChange();
			}
		}
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
				objects.add(ho);
			}
			
		} else if (ho instanceof ProjectData && content) {
			if (object.isChildrenLoaded()) {
				i = list.iterator();
				while (i.hasNext()) {
					child = (TreeImageDisplay) i.next();
					if (child.getUserObject() instanceof DatasetData) {
						if (child.isChildrenLoaded()) {
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
							objects.add((DataObject) child.getUserObject());
						}
					}
				}
			}
		}
	}
	/**
	 * Returns the name of the group.
	 * 
	 * @param groupId The id of the group.
	 * @return See above.
	 */
	private String getGroupName(long groupId)
	{
		Collection groups = TreeViewerAgent.getAvailableUserGroups();
		Iterator j = groups.iterator();
		GroupData group;
		while (j.hasNext()) {
			group = (GroupData) j.next();
			if (group.getId() == groupId) return group.getName();
		}
		return "";
	}
	
	/**
	 * Removes the nodes from the display.
	 * 
	 * @param trans The node to remove.
	 */
	private void removeNodes(Map<SecurityContext, List<DataObject>> trans)
	{
		EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
		List<DataObject> objects = new ArrayList<DataObject>();
		Iterator<List<DataObject>> i = trans.values().iterator();
		List<DataObject> l;
		while (i.hasNext()) {
			l = i.next();
			if (l != null) objects.addAll(l);
		}
		bus.post(new DeleteObjectEvent(objects));
		
		NodesFinder finder = new NodesFinder(objects);
		Browser browser = model.getSelectedBrowser();
		browser.accept(finder);
		browser.removeTreeNodes(finder.getNodes());
		view.removeAllFromWorkingPane();
		DataBrowserFactory.discardAll();
		model.getMetadataViewer().setRootObject(null, -1, null);
	}
	
	/**
	 * Moves the objects.
	 * 
	 * @param ctx The group to move the data to.
	 * @param target The target object.
	 * @param trans The objects to transfer.
	 */
	private void moveData(SecurityContext ctx, DataObject target,
			Map<SecurityContext, List<DataObject>> trans)
	{
		removeNodes(trans);
		TransferableObject t = new TransferableObject(ctx, target, trans);
		t.setGroupName(getGroupName(ctx.getGroupID()));
		IconManager icons = IconManager.getInstance();
		TransferableActivityParam param = new TransferableActivityParam(
				icons.getIcon(IconManager.MOVE_22), t);
		param.setFailureIcon(icons.getIcon(IconManager.MOVE_FAILED_22));
		UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
		un.notifyActivity(model.getSecurityContext(), param);
	}
	
	/** 
	 * Notifies the change of group.
	 * 
	 * @param oldId The previous group id.
	 */
	private void notifyChangeGroup(long oldId)
	{
		long id = model.getSelectedGroupId();
		ChangeUserGroupEvent evt = new ChangeUserGroupEvent(id, oldId);
		TreeViewerAgent.getRegistry().getEventBus().post(evt);
		firePropertyChange(GROUP_CHANGED_PROPERTY, oldId, id);
	}
	
	/**
	 * Returns the group the specified node belongs to.
	 * 
	 * @param node The node to handle.
	 * @return See above.
	 */
	private GroupData getContext(TreeImageDisplay node)
    {
    	if (node == null) return null;
    	if (node.getUserObject() instanceof GroupData) {
    		return (GroupData) node.getUserObject();
    	}
    	TreeImageDisplay parent = node.getParentDisplay();
    	Object ho;
    	if (parent == null) {
    		ho = node.getUserObject();
    		if (ho instanceof GroupData)
    			return (GroupData) ho;
    		return null;
    	}
    	ho = parent.getUserObject();
    	if (ho instanceof GroupData) return (GroupData) ho;
    	return getContext(parent);
    }
	
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
		activity.setFileName(fa.getFileName());

		un.notifyActivity(model.getSecurityContext(), activity);
	}

	/**
     * Returns the SecurityContext if already added or <code>null</code>.
     * 
     * @param map The map to check.
     * @param id The group's identifier.
     * @return See above.
     */
    private SecurityContext getKey(
    	Map<SecurityContext, List<DataObject>> map, long id)
    {
    	Iterator<SecurityContext> i = map.keySet().iterator();
    	SecurityContext ctx;
    	while (i.hasNext()) {
			ctx = i.next();
			if (ctx.getGroupID() == id)
				return ctx;
		}
    	return null;
    }
	
    /**
     * Checks if a browser associated to the passed node has been 
     * discarded.
     *  
     * @param display The node to handle.
     */
    private DataBrowser handleDiscardedBrowser(TreeImageDisplay display)
    {
    	if (!DataBrowserFactory.hasBeenDiscarded(display)) return null;
    	DataBrowser db = null;
    	Browser browser = model.getSelectedBrowser();
		List list;
		List<ApplicationData> app = null;
		
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
    	return db;
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
		if (display instanceof TreeImageTimeSet || 
			(display instanceof TreeFileSet &&
				((TreeFileSet) display).getType() ==
					TreeFileSet.ORPHANED_IMAGES)) {
			db = DataBrowserFactory.getDataBrowser(display);
			if (db != null) {
				db.setComponentTitle("");
				if (visible) {
					view.removeAllFromWorkingPane();
					view.displayBrowser(db);
				}
			} else {
				db = handleDiscardedBrowser(display);
				if (visible) {
				    view.removeAllFromWorkingPane();
				}
			}
			if (db != null) db.setSelectedNodes(null, null);
			return;
		}
		if (object instanceof ImageData) {
			TreeImageDisplay displayParent = null;
			if (display != null) {
			    displayParent = display.getParentDisplay();
			}
			
			if (displayParent instanceof TreeImageTimeSet ||
				(displayParent instanceof TreeFileSet &&
				((TreeFileSet) displayParent).getType() ==
					TreeFileSet.ORPHANED_IMAGES)) {
				db = DataBrowserFactory.getDataBrowser(displayParent);
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
				} else {
					db = handleDiscardedBrowser(displayParent);
				}
				model.setDataViewer(db);
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
        						if (browser.getBrowserType() ==
        								Browser.ADMIN_EXPLORER) {
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
        						}
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
			if (b != null)
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
	 * Notifies the model that the user has annotated data.
	 * 
	 * @param containers The objects to handle.
	 * @param count A positive value if annotations are added, a negative value
	 * if annotations are removed.
	 */
	void onAnnotated(List<DataObject> containers, int count)
	{
		Browser browser = model.getSelectedBrowser();
		if (containers != null && containers.size() > 0) {
			NodesFinder finder = new NodesFinder(containers);
			if (browser != null) browser.accept(finder);
			Set<TreeImageDisplay> nodes = finder.getNodes();
			//mark
			if (browser != null && nodes != null && nodes.size() > 0) {
				Iterator<TreeImageDisplay> i = nodes.iterator();
				while (i.hasNext()) {
					i.next().setAnnotationCount(count);
				}
				browser.getUI().repaint();
			}
		}
	}
	
	/**
	 * Sets the image to copy the rendering settings from.
	 * 
	 * @param image The image to copy the rendering settings from.
	 * @param rndProxyDef Copied 'pending' rendering settings (can be null)
	 */
	void setRndSettings(ImageData image, RndProxyDef rndProxyDef)
	{
		if (model.getState() == DISCARDED) return;
		model.setRndSettings(image, rndProxyDef);
	}

	/**
	 * Notifies that the rendering settings have been copied.
	 * 
	 * @param imageIds The collection of updated images
	 */
	void onRndSettingsCopied(Collection<Long> imageIds)
	{
		if (model.getState() == DISCARDED) return;
		MetadataViewer mv = model.getMetadataViewer();
		if (mv != null) mv.onRndSettingsCopied(imageIds);
	}
	
	/** Shuts down the component.*/
	void shutDown()
	{
		view.setVisible(false);
		view.dispose();
		discard();
		model.setState(NEW);
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
				view.setVisible(true);
				view.setOnScreen();
				view.selectFirstPane();
				model.getSelectedBrowser().activate();
				model.setState(READY);
				break;
			//case DISCARDED:
				//throw new IllegalStateException(
					//	"This method can't be invoked in the DISCARDED state.");
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
		Map<Integer, Browser> browsers = getBrowsers();
		Iterator<Browser> i = browsers.values().iterator();
		while (i.hasNext())
			i.next().discard();
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
					model.getMetadataViewer().setRootObject(null,
							exp.getId(), null);
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
		fireStateChange();
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
                                 un.notifyInfo("User Creation",
        				"No group available. Please create a group first.");
        			 return;
        		 }
    		}
			d = new AdminDialog(view, model.getAdminContext(),
					object.getClass(), uo, nodes);
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
		model.getMetadataViewer().setRootObject(null, -1, null);
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
			throw new IllegalStateException("This method cannot " +
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
			ExitApplication a = new ExitApplication(
			        !TreeViewerAgent.isRunAsPlugin());
			GroupData group = model.getSelectedGroup();
	        if (group != null)
	            a.setSecurityContext(new SecurityContext(group.getId()));
			bus.post(a);
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
		//Update the group.
		GroupData group = getContext(display);
		if (group != null) {
			long oldId = model.getSelectedGroupId();
			model.setSelectedGroupId(group.getId());
			notifyChangeGroup(oldId);
		}
		model.setDataViewer(null);
		MetadataViewer metadata = model.getMetadataViewer();
		TreeImageDisplay[] selection = browser.getSelectedDisplays();
		
		boolean single = selection.length == 1;
		//Need to review. that
		if (display instanceof TreeImageTimeSet) {
			single = false;
			TreeImageTimeSet time = (TreeImageTimeSet) display;
			view.removeAllFromWorkingPane();
			if (!time.containsImages()) {
				metadata.setRootObject(null, -1, null);
			}
			showDataBrowser(display.getUserObject(), display, true);
			return;
		} 
		
		if (display != null) {
			Object object = display.getUserObject();
			metadata.setSelectionMode(single);
			if (single) {
				ExperimenterData exp = browser.getNodeOwner(display);
				if (exp == null) exp = model.getUserDetails();
				// setRootObject relies on selection mode set above
				metadata.setRootObject(object, exp.getId(),
						browser.getSecurityContext(display));
				TreeImageDisplay p = display.getParentDisplay();
				if (p != null) {
					TreeImageDisplay pp = p.getParentDisplay();
					Object gpp = null;
					if (pp != null) gpp = pp.getUserObject();
					metadata.setParentRootObject(p.getUserObject(), gpp);
				}
			} else {
			    List<Object> l = new ArrayList<Object>(selection.length);
			    for (int i = 0; i < selection.length; i++) {
			        l.add(selection[i].getUserObject());
			    }
			    if (l.size() > 0) {
			        metadata.setRelatedNodes(l);
			    }
			}
			if (!model.isFullScreen()) {
				showDataBrowser(object, display, false);
				browse(display, null, true);
			} else showDataBrowser(object, display, true);
		} else {
			DataBrowser db = model.getDataViewer();
			if (db != null)
				db.setSelectedNodes(new ArrayList<DataObject>(), null);
			metadata.setRootObject(null, -1, null);
		}
		if (display != null && 
			display.getUserObject() instanceof ExperimenterData &&
			display.isToRefresh()) {
			refreshTree();
		}
		Browser b = model.getSelectedBrowser();
		if (b != null && display != null) {
			if (model.getDataViewer() != null)
				b.addComponent(model.getDataViewer().getGridUI());
			else b.addComponent(null);
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
		if (n == 2) parent = l.get(1);
		if (selection == null || selection.size() == 0) return;
		Object selected = selection.get(0);
		
		MetadataViewer mv = model.getMetadataViewer();
		if (hasDataToSave()) {
		    mv.saveData();
		}
		boolean sameSelection = false;
		if (view.getDisplayMode() != SEARCH_MODE) {
			Browser browser = model.getSelectedBrowser();
			List<Object> oldSelection = browser.getSelectedDataObjects();
			browser.onSelectedNode(parent, selection, selection.size() > 0);
			List<Object> newSelection = browser.getSelectedDataObjects();
			sameSelection = isSameSelection(oldSelection, newSelection);
		}
		
		int size = selection.size();
		if (size == 1 && model.getSelectedBrowser() != null) {
			Browser browser = model.getSelectedBrowser();
			ExperimenterData exp = null;
			TreeImageDisplay last = null;
			if (browser != null) last = browser.getLastSelectedDisplay();
			if (last != null) exp = browser.getNodeOwner(last);
			if (exp == null) exp = model.getUserDetails();
			mv.setRootObject(selected, exp.getId(), 
					browser.getSecurityContext(last));
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
		List<Object> result = new ArrayList<Object>();
		selection.remove(0);
		result.add(selection);
		result.add(selected);
		result.add(parent);
		setSelectedNode(result, sameSelection);
	}

	/**
	 * Sets the selected node and loads the annotations if requested.
	 *
	 * @param object The object to handle.
	 * @param sameSelection Pass <code>true</code> if it is the same selection.
	 *                      Annotation will not be loaded, <code>false</code>
	 *                      otherwise.
	 */
	private void setSelectedNode(Object object, boolean sameSelection)
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
                un.notifyInfo("Image Not Valid", 
                        "The selected image is not valid.");
                return;
            }
        } else if (selected instanceof WellSampleData) {
            WellSampleData ws = (WellSampleData) selected;
            if (ws.getId() < 0) {
                UserNotifier un =
                        TreeViewerAgent.getRegistry().getUserNotifier();
                un.notifyInfo("Well Not Valid", 
                        "The selected well is not valid.");
                return;
            }
        }
        MetadataViewer mv = model.getMetadataViewer();
        if (hasDataToSave()) {
            mv.saveData();
        }
        List<Object> siblings = (List<Object>) l.get(0);
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
        Object grandParent = null;
        if (selected instanceof WellSampleData) {
            if (parent instanceof WellData) 
                grandParent = ((WellData) parent).getPlate();
        }

        if (!sameSelection) {
            if (browser == null) {
                if (selected instanceof DataObject) {
                    SecurityContext ctx = new SecurityContext(
                            ((DataObject) selected).getGroupId());
                    mv.setRootObject(selected, exp.getId(), ctx);
                }
            } else {
                mv.setRootObject(selected, exp.getId(),
                        browser.getSecurityContext(last));
            }
            mv.setParentRootObject(parent, grandParent);
        }
        
        if (view.getDisplayMode() == SEARCH_MODE) {
            siblings.add(selected);
            mv.setRelatedNodes(siblings);
        }
        else {
            TreeImageDisplay[] selection = null;
            if (browser != null) selection = browser.getSelectedDisplays();
            if (selection != null && selection.length > 0) {
                if (selected instanceof WellSampleData) {
                    siblings.add(selected);
                    if (siblings.size() > 1 && !sameSelection)
                        mv.setRelatedNodes(siblings);
                } else {
                    siblings = new ArrayList<Object>(selection.length);
                    for (int i = 0; i < selection.length; i++) {
                        siblings.add(selection[i].getUserObject());
                    }
                    if (siblings.size() > 1 && !sameSelection)
                        mv.setRelatedNodes(siblings);
                }
            } else {
                if (selected instanceof WellSampleData) {
                    siblings.add(selected);
                    if (siblings.size() > 1 && !sameSelection)
                        mv.setRelatedNodes(siblings);
                }
            }
        }
        if (model.getDataViewer() != null)
            model.getDataViewer().setApplications(
                    TreeViewerFactory.getApplications(
                            model.getObjectMimeType(selected)));
        if (!model.isFullScreen()) {
            browse(browser.getLastSelectedDisplay(), null, false);
        }

        //Notifies actions.
        firePropertyChange(SELECTION_PROPERTY, Boolean.valueOf(false),
                Boolean.valueOf(true));
	}
	
	/**
	 * Checks if the specified lists contained the same elements.
	 * Returns <code>true</code> if it is the same selection,
	 * <code>false</code> otherwise.
	 *
	 * @param oldSelection The selection prior to change.
	 * @param newSelection The new selection
	 * @return See above.
	 */
	private boolean isSameSelection(List<Object> oldSelection,
	        List<Object> newSelection)
	{
	    if (oldSelection == null || newSelection == null) return false;
	    int s1 = oldSelection.size();
	    int s2 = newSelection.size();
	    if (s1 != s2 || (s1 == 0 && s2 > 0) || (s1 > 0 && s2 == 0)) {
	        return false;
	    }
	    List<Long> ids = new ArrayList<Long>();
	    Class<?> klass = null;
	    Object object;
	    DataObject data;
	    Iterator<Object> i = oldSelection.iterator();
	    while (i.hasNext()) {
	        object = i.next();
	        klass = object.getClass();
            if (object instanceof DataObject) {
                data = (DataObject) object;
                if (!ids.contains(data.getId())) {
                    ids.add(data.getId());
                }
            }
        }
	    int count = 0;
	    i = newSelection.iterator();
	    while (i.hasNext()) {
            object = i.next();
            if (!klass.equals(object.getClass())) {
                return false;
            }
            if (object instanceof DataObject) {
                data = (DataObject) object;
                if (ids.contains(data.getId())) {
                    count++;
                }
            }
        }
	    return count == ids.size();
	}

	public void handleSearchSelectionEvent(SearchSelectionEvent evt) {
	    // Force the metadataviewer to be shown and fake a treeviewer object
	    // selection in order to get the metadataviewer show the right stuff 
	    // (TODO: That's pretty weird... is there another/better way?)
	    view.forceShowMetaDataView();
	    List<Object> tmp = new ArrayList<Object>();
	    tmp.add(evt.getDataObjects());
	    setSelectedNodes(tmp);
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
        List<Object> siblings = (List<Object>) l.get(0);
        boolean sameSelection = true;
        if (view.getDisplayMode() != SEARCH_MODE) {
            Browser browser = model.getSelectedBrowser();
            List<Object> oldSelection = browser.getSelectedDataObjects();
            List<Object> newSelection = new ArrayList<Object>();
            newSelection.add(selected);
            sameSelection = isSameSelection(oldSelection, newSelection);
        }
        
	    setSelectedNode(object, sameSelection);
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
			mv.saveData();
		}
		Browser browser = model.getSelectedBrowser();
		ExperimenterData exp = null;
		TreeImageDisplay last = browser.getLastSelectedDisplay();
		if (last != null) exp = browser.getNodeOwner(last);
		if (exp == null) exp = model.getUserDetails();
		mv.setSelectionMode(true);
		mv.setRootObject(selected, exp.getId(),
				browser.getSecurityContext(last));
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
			case DISCARDED:
			case SAVE:
				return;
		}
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
	 * @see TreeViewer#setHierarchyRoot(long, List)
	 */
	public void setHierarchyRoot(long userGroupID, 
			List<ExperimenterData> experimenters)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		if (experimenters == null) return;
		Browser browser = model.getBrowser(Browser.PROJECTS_EXPLORER);
		//Check that the group is displayed.
		Collection groups = TreeViewerAgent.getAvailableUserGroups();
		if (groups == null) return;
		TreeImageDisplay refNode = null;
		ExperimenterVisitor visitor;
		List<TreeImageDisplay> nodes;
		if (groups.size() > 1) {
			visitor = new ExperimenterVisitor(browser, userGroupID);
			browser.accept(visitor);
			nodes = visitor.getNodes();
			if (nodes.size() == 0) {
				UserNotifier un = 
					TreeViewerAgent.getRegistry().getUserNotifier();
				un.notifyInfo("Add User",
						"The group is not displayed.");
				return;
			}
			refNode = nodes.get(0);
		}
		
		List<Long> ids = new ArrayList<Long>();
		Iterator<ExperimenterData> ii = experimenters.iterator();
		while (ii.hasNext()) {
			ids.add(ii.next().getId());
		}
		Map<Integer, Browser> browsers = model.getBrowsers();

		//first remove all the users displayed.
		visitor = new ExperimenterVisitor(browser, -1, -1);
		if (refNode != null) refNode.accept(visitor);
		else browser.accept(visitor);
		nodes = visitor.getNodes();
		List<ExperimenterData> users = new ArrayList<ExperimenterData>();
		Iterator<TreeImageDisplay> k = nodes.iterator();
		TreeImageDisplay n;
		ExperimenterData exp;
		while (k.hasNext()) {
			n = k.next();
			if (n.getUserObject() instanceof ExperimenterData) {
				exp = (ExperimenterData) n.getUserObject();
				if (!ids.contains(exp.getId())) {
					users.add(exp);
				}
			}
		}
		model.setSelectedGroupId(userGroupID);
		view.setPermissions();
		Entry<Integer, Browser> entry;
		userGroupID = model.getSelectedGroupId();
		
		//First remove;
		Iterator<ExperimenterData> j = users.iterator();
		Iterator<Entry<Integer, Browser>> i;
		while (j.hasNext()) {
			exp = j.next();
			i = browsers.entrySet().iterator();
			while (i.hasNext()) {
				entry = i.next();
				browser = entry.getValue();
				browser.removeExperimenter(exp, userGroupID);
			}
		}
		//Add
		j = experimenters.iterator();
		while (j.hasNext()) {
			exp = j.next();
			i = browsers.entrySet().iterator();
			while (i.hasNext()) {
				entry = i.next();
				browser = entry.getValue();
				browser.addExperimenter(exp, userGroupID);
			}
		}
		//clean display if 
		if (users.size() > 0) {
		      DataBrowserFactory.discardAll();
		        view.removeAllFromWorkingPane();
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#canEdit(Object)
	 */
	public boolean canEdit(Object ho)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		//Check if current user can write in object
		long id = model.getUserDetails().getId();
		boolean b = false;
		if (ho instanceof TreeImageTimeSet) {
			Browser browser = model.getSelectedBrowser();
			if (browser == null) return false;
			ExperimenterData exp = browser.getNodeOwner((TreeImageDisplay) ho);
			if (exp.getId() == id) b = true;
		} else b = EditorUtil.isUserOwner(ho, id);
		if (b) return b; //user is the owner.
		GroupData group = null;
		if (ho instanceof DataObject) {
			DataObject data = (DataObject) ho;
			return data.canEdit();
		} else if (ho instanceof TreeImageTimeSet) {
			Browser browser = model.getSelectedBrowser();
			if (browser == null) return false;
			group = browser.getNodeGroup((TreeImageDisplay) ho);
			
			switch (group.getPermissions().getPermissionsLevel()) {
				case GroupData.PERMISSIONS_GROUP_READ_WRITE:
				case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
					return true;
			}
			return EditorUtil.isUserGroupOwner(group, id);
		}
		return false;
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#canLink(Object)
	 */
	public boolean canLink(Object ho)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		//Check if current user can write in object
		boolean b = false;
		if (ho instanceof TreeImageTimeSet) {
			Browser browser = model.getSelectedBrowser();
			if (browser == null) return false;
			GroupData g = browser.getNodeGroup(
					(TreeImageDisplay) ho);
			return g.canLink();
		} else if (ho instanceof DataObject)
			b = ((DataObject) ho).canLink();
		return b;
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#canChgrp(Object)
	 */
	public boolean canChgrp(Object ho)
	{
		if (TreeViewerAgent.isAdministrator()) return true;
		long id = model.getUserDetails().getId();
		boolean b = false;
		if (ho instanceof TreeImageTimeSet) {
			Browser browser = model.getSelectedBrowser();
			ExperimenterData exp = browser.getNodeOwner((TreeImageDisplay) ho);
			if (exp.getId() == id) b = true;
		} else b = EditorUtil.isUserOwner(ho, id);
		return b;
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#canDelete(Object)
	 */
	public boolean canDelete(Object ho)
	{
		long id = model.getUserDetails().getId();
		boolean b = false;
		if (ho instanceof TreeImageTimeSet) {
			Browser browser = model.getSelectedBrowser();
			ExperimenterData exp = browser.getNodeOwner((TreeImageDisplay) ho);
			if (exp.getId() == id) b = true;
		} else b = EditorUtil.isUserOwner(ho, id);
		if (b) return b; //user is the owner.
		GroupData group = null;
		if (ho instanceof ExperimenterData || ho instanceof GroupData) {
		    // users and groups should not be deleted at all
		    return false;
		}
		else if (ho instanceof DataObject) {
			DataObject data = (DataObject) ho;
			return data.canDelete();
		} else if (ho instanceof TreeImageTimeSet) {
			Browser browser = model.getSelectedBrowser();
			if (browser == null) return false;
			group = browser.getNodeGroup((TreeImageDisplay) ho);
		} else {
		    return false;
		}
		//Do not have enough information about the group.
		if (group.getPermissions() == null) return false;
		switch (group.getPermissions().getPermissionsLevel()) {
			case GroupData.PERMISSIONS_GROUP_READ_WRITE:
			case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
				return true;
		}
		return EditorUtil.isUserGroupOwner(group, id);
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#canAnnotate(Object)
	 */
	public boolean canAnnotate(Object ho)
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
		if (b) return b; //user is the owner.
		if (ho instanceof DataObject) {
			DataObject data = (DataObject) ho;
			return data.canAnnotate();
		} else if (ho instanceof TreeImageTimeSet) {
			Browser browser = model.getSelectedBrowser();
			if (browser == null) return false;
			GroupData group = browser.getNodeGroup((TreeImageDisplay) ho);
			switch (group.getPermissions().getPermissionsLevel()) {
				case GroupData.PERMISSIONS_GROUP_READ_WRITE:
				case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
					return true;
			}
			return EditorUtil.isUserGroupOwner(group, id);
		}
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
		if (CollectionUtils.isEmpty(objects))
			return;
		Browser b = model.getSelectedBrowser();
		List<Object> available = new ArrayList<Object>();
		List<Object> immutable = new ArrayList<Object>();
		Set selected = null;
		if (b != null) {
			TreeImageDisplay[] values = b.getSelectedDisplays();
			if (values != null && values.length > 0) {
				// Only modify the first group from the list of groups
				Object value = values[0].getUserObject();
				if (value instanceof GroupData) {
					long groupId = ((GroupData) value).getId();
					ExperimenterData currentUser = model.getExperimenter();
					selected = new HashSet<ExperimenterData>();
					long userID = currentUser.getId();
					long id;
					List<Long> ids = new ArrayList<Long>();
					for (ExperimenterData experimenter :
						(List<ExperimenterData>) objects) {
					    id = experimenter.getId();
						if (experimenter.isMemberOfGroup(groupId)) {
						    ids.add(id);
						    selected.add(experimenter);
						    //Cannot move guest or root
						    if ((model.isSystemUser(id, GroupData.GUEST) &&
						       model.isSystemGroup(groupId, GroupData.GUEST))) {
						        immutable.add(experimenter);
						    } else if (model.isSystemGroup(groupId, GroupData.SYSTEM)) {
						        if (model.isSystemUser(id, GroupData.SYSTEM) ||
						                id == userID) {
						            immutable.add(experimenter);
						        }
						    }
						}
						if (!ids.contains(experimenter.getId())) {
							available.add(experimenter);
						}
					}
				}
			} else {
				available.addAll(objects);
			}
		}
		fireStateChange();
		SelectionWizard d = new SelectionWizard(view, available, selected,
				objects.get(0).getClass(), TreeViewerAgent.getUserDetails());
		d.setImmutableElements(immutable);
		IconManager icons = IconManager.getInstance();
		String title = "User Selection";
		String text = "Select the Users who should be in the selected group.";
		Icon icon = icons.getIcon(IconManager.OWNER_48);
		d.setTitle(title, text, icon);
		d.addPropertyChangeListener(controller);
		UIUtilities.centerAndShow(d);
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
			case VIEW_MENU:
				break;
			case AVAILABLE_SCRIPTS_MENU:
				if (model.getAvailableScripts() == null) {
					model.loadScripts(p, c);
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
		//check to transfer data.
		Map<Long, List<DataObject>> elements = 
			new HashMap<Long, List<DataObject>>();
		long gid;
		List<DataObject> l = new ArrayList<DataObject>();
		TreeImageDisplay n;
		Object os;
		boolean admin = false;
		Browser browser = model.getSelectedBrowser();
		if (browser != null) {
			admin = browser.getBrowserType() == Browser.ADMIN_EXPLORER;
		}
		for (int j = 0; j < nodes.length; j++) {
			n = nodes[j];
			os = n.getUserObject();
			if (os instanceof DataObject) {
				if (!(os instanceof ExperimenterData ||
					os instanceof GroupData)) {
					gid = ((DataObject) os).getGroupId();
					if (!elements.containsKey(gid)) {
						elements.put(gid, new ArrayList<DataObject>());
					}
					l = elements.get(gid);
					l.add((DataObject) os);
				} else if (os instanceof ExperimenterData) {
					l.add((DataObject) os);
				}
			}
		}
		if (admin && l.size() > 0) {
			boolean b = model.paste(parents);
			if (!b) {
				un.notifyInfo("Paste", 
				"The Users to copy cannot be added to the selected Groups."); 
			} else fireStateChange();
			return;
		}
		if (elements.size() == 0) return;
		Iterator<Long> i;
		List<Long> ids = new ArrayList<Long>();
		Map<Long, List<DataObject>> 
			parentMap = new HashMap<Long, List<DataObject>>();
		List<DataObject> list;
		if (elements.size() == 1) { //check if cut and paste
			TreeImageDisplay parent;
			for (int j = 0; j < parents.length; j++) {
				n = parents[j];
				os = n.getUserObject();
				if (os instanceof ExperimenterData) {
					parent = n.getParentDisplay();
					if (parent.getUserObject() instanceof GroupData) {
						gid = parent.getUserObjectId();
						if (!parentMap.containsKey(gid)) {
							parentMap.put(gid, new ArrayList<DataObject>());
						}
						if (!ids.contains(gid))
							ids.add(gid);
					}
				} else if (os instanceof DataObject) {
					gid = ((DataObject) os).getGroupId();
					if (!parentMap.containsKey(gid)) {
						parentMap.put(gid, new ArrayList<DataObject>());
					}
					list = parentMap.get(gid);
					if (!(os instanceof GroupData))
						list.add((DataObject) os);
					if (!ids.contains(gid))
						ids.add(gid);
				}
			}
			if (ids.size() == 1) { //check if it is a Paste in the group
				i = elements.keySet().iterator();
				while (i.hasNext()) {
					if (i.next().longValue() == ids.get(0).longValue()) {
						boolean b = model.paste(parents);
						if (!b) {
							un.notifyInfo("Paste", 
							"The nodes to copy cannot be added to the " +
							"selected nodes."); 
						} else fireStateChange();
						return;
					}
				}
			}
		}
		//Warn the user than the copy/paste not allowed between groups
		MessageBox box = new MessageBox(view, "Copy data",
				"Copying between groups is not yet supported.\n" +
		"To continue and move the data to the new group, click 'Yes'.\n"+
		"To leave the data in the current group, click 'No'.");
		if (box.centerMsgBox() != MessageBox.YES_OPTION) return;
		//if we are here moving data.
		i = elements.keySet().iterator();
		Map<SecurityContext, List<DataObject>> trans = 
			new HashMap<SecurityContext, List<DataObject>>();
		while (i.hasNext()) {
			gid = i.next();
			trans.put(new SecurityContext(gid), elements.get(gid));
		}
		IconManager icons = IconManager.getInstance();
		TransferableActivityParam param;
		Entry<Long, List<DataObject>> entry;
		Iterator<Entry<Long, List<DataObject>>>
		k = parentMap.entrySet().iterator();
		TransferableObject t;
		Long id;
		while (k.hasNext()) {
			entry = k.next();
			id = entry.getKey();
			removeNodes(trans);
			t = new TransferableObject(new SecurityContext(id), 
					(List<DataObject>) entry.getValue(), trans);
			t.setGroupName(getGroupName(id));
			param = new TransferableActivityParam(
					icons.getIcon(IconManager.MOVE_22), t);
			param.setFailureIcon(icons.getIcon(IconManager.MOVE_FAILED_22));
			un.notifyActivity(model.getSecurityContext(), param);
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getUI()
	 */
	public JFrame getUI() { return view; }

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

    public void saveMetadata() {
        MetadataViewer metadata = model.getMetadataViewer();
        if (metadata == null)
            return;
        if (!(metadata.hasDataToSave()))
            return;
        model.getMetadataViewer().saveData();
    }
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#retrieveUserGroups(Point, GroupData)
	 */
	public void retrieveUserGroups(Point location, GroupData group)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		JFrame f = (JFrame) TreeViewerAgent.getRegistry().getTaskBar();
		IconManager icons = IconManager.getInstance();
		Collection groups = TreeViewerAgent.getAvailableUserGroups();
		if (group == null) group = model.getSelectedGroup();
		
		int level = group.getPermissions().getPermissionsLevel();
		if (level == GroupData.PERMISSIONS_PRIVATE) {
			boolean owner = false;
			if (TreeViewerAgent.isAdministrator()) owner = true;
			else {
				ExperimenterData currentUser = model.getExperimenter();
				Set leaders = group.getLeaders();
				Iterator k = leaders.iterator();
				ExperimenterData exp;
				
				while (k.hasNext()) {
					exp = (ExperimenterData) k.next();
					if (exp.getId() == currentUser.getId()) {
						owner = true;
						break;
					}
				}
			}
			if (!owner) return;
		}
		Set experimenters = group.getExperimenters();
		if (experimenters == null || experimenters.size() == 0) return;
		Browser browser = model.getBrowser(Browser.PROJECTS_EXPLORER);
		TreeImageDisplay refNode = null;
		List<TreeImageDisplay> nodes;
		ExperimenterVisitor visitor;
		if (groups.size() > 1) {
			visitor = new ExperimenterVisitor(browser, group.getId());
			browser.accept(visitor);
			nodes = visitor.getNodes();
			if (nodes.size() != 1) return;
			refNode = nodes.get(0);
		}
		visitor = new ExperimenterVisitor(browser, -1, -1);
		if (refNode != null) refNode.accept(visitor);
		else browser.accept(visitor);
		nodes = visitor.getNodes();
		List<ExperimenterData> users = new ArrayList<ExperimenterData>();
		Iterator<TreeImageDisplay> j = nodes.iterator();
		TreeImageDisplay n;
		while (j.hasNext()) {
			n = j.next();
			if (n.getUserObject() instanceof ExperimenterData) {
				users.add((ExperimenterData) n.getUserObject());
			}
		}
		switchUserDialog = new UserManagerDialog(f, model.getUserDetails(), 
				group, users, icons.getIcon(IconManager.OWNER),
				icons.getIcon(IconManager.OWNER_48));
		switchUserDialog.addPropertyChangeListener(controller);
		switchUserDialog.setDefaultSize();
		UIUtilities.showOnScreen(switchUserDialog, location);
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
		if (model.getState() == DISCARDED) return;
		Browser browser = model.getSelectedBrowser();
		TreeImageDisplay expNode = browser.getLastSelectedDisplay();
		TreeImageDisplay parent = expNode.getParentDisplay();
		long groupID = -1;
		if (parent != null && parent.getUserObject() instanceof GroupData) {
			GroupData g = (GroupData) parent.getUserObject();
			groupID = g.getId();
		}
		Object uo = expNode.getUserObject();
		if (uo == null || !(uo instanceof ExperimenterData)) return;
		ExperimenterData exp = (ExperimenterData) uo;
		Map<Integer, Browser> browsers = model.getBrowsers();
		Iterator<Entry<Integer, Browser>> i = browsers.entrySet().iterator();
		Entry<Integer, Browser> entry;
		while (i.hasNext()) {
			entry = i.next();
			browser = entry.getValue();
			browser.removeExperimenter(exp, groupID);
		}
		DataBrowserFactory.discardAll();
        view.removeAllFromWorkingPane();
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
	 * @see TreeViewer#AreSettingsCompatible(long)
	 */
	public boolean areSettingsCompatible(long groupID)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked " +
			"in the DISCARDED state.");
		return model.areSettingsCompatible(groupID);
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
		
        MessageBox box = new MessageBox(getUI(), "Save rendering settings",
                RENDERINGSETTINGS_WARNING);
        if (box.centerMsgBox() == MessageBox.YES_OPTION) {
            model.firePasteRenderingSettings(ids, klass);
            fireStateChange();
        }
	}

	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#pasteRndSettings(TimeRefObject)
	 */
	public void pasteRndSettings(TimeRefObject ref)
	{
		if (model.getState() == DISCARDED) return;
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
		
		MessageBox box = new MessageBox(getUI(), "Reset rendering settings",
	                RENDERINGSETTINGS_WARNING);
	        if (box.centerMsgBox() == MessageBox.YES_OPTION) {
	            model.fireResetRenderingSettings(ids, klass);
	            fireStateChange();
	        }
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
		if (parent instanceof TreeFileSet &&
			((TreeFileSet) parent).getType() != TreeFileSet.ORPHANED_IMAGES) {
			view.removeAllFromWorkingPane();
			return;
		}
		Browser browser = model.getSelectedBrowser();
		Object parentObject = parent.getUserObject();
		TreeImageDisplay display = parent.getParentDisplay();
		Object grandParentObject = null;
		if (display != null) grandParentObject = display.getUserObject();
		DataBrowser db = null;
		if (parentObject instanceof TagAnnotationData) {
			db = DataBrowserFactory.getTagsBrowser(
					model.getSecurityContext(parent),
					(TagAnnotationData) parentObject, leaves, false);
		} else if (parentObject instanceof GroupData) {
			if (browser != null) {
				if (browser.getBrowserType() == Browser.ADMIN_EXPLORER) {
					db = DataBrowserFactory.getGroupsBrowser(
							model.getSecurityContext(parent),
							(GroupData) parentObject, leaves);
				}
			}
			
		} else if (parentObject instanceof FileData) {
			FileData f = (FileData) parentObject;
			if (!f.isHidden()) {
				if (f.isDirectory()) 
					db = DataBrowserFactory.getFSFolderBrowser(
							model.getSecurityContext(parent),
							(FileData) parentObject, leaves);
			}
		} else {
			db = DataBrowserFactory.getDataBrowser(
					model.getSecurityContext(parent), grandParentObject, 
					parentObject, leaves, parent);
			if (parent instanceof TreeImageTimeSet) {
				ExperimenterData exp = browser.getNodeOwner(parent);
				db.setExperimenter(exp);
			}
		}
			
		if (db != null) {
			db.addPropertyChangeListener(controller);
			view.displayBrowser(db);
			db.setDisplayMode(model.getDisplayMode());
			db.activate();
		}
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
										 (ImageData) k.next()));
							}
						}
						value.setChildrenLoaded(true);
					}
				}
				model.setState(READY);
				fireStateChange();
				db = DataBrowserFactory.getDataBrowser(
						model.getSecurityContext(), project, set);
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
										(ImageData) k.next()));
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
										   (ImageData) k.next()));
									}
								}
								value.setChildrenLoaded(true);
							}
						}
					}
				}
				model.setState(READY);
				fireStateChange();
				db = DataBrowserFactory.getTagsBrowser(
						model.getSecurityContext(), tag, set, true);
			}
		}
		if (db != null) {
			db.addPropertyChangeListener(controller);
			view.removeAllFromWorkingPane();
			view.displayBrowser(db);
			db.setDisplayMode(model.getDisplayMode());
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
		if (n > 3 || n <= 1) return;
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
		if (browser == null) return;
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
		
		MessageBox box = new MessageBox(getUI(), "Reset rendering settings",
	                RENDERINGSETTINGS_WARNING);
	        if (box.centerMsgBox() == MessageBox.YES_OPTION) {
	            model.fireSetOwnerRenderingSettings(ids, klass);
	            fireStateChange();
	        }
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
		switch (newMode) {
			case EXPLORER_MODE:
				onSelectedDisplay();
				break;
			case SEARCH_MODE:
				ExperimenterData exp = model.getUserDetails();
				model.getMetadataViewer().setRootObject(null, exp.getId(),
						null);
				if (db != null) {
					view.displayBrowser(db);
					model.setDataViewer(db);
				}
		}
		firePropertyChange(DISPLAY_MODE_PROPERTY, oldMode, newMode);
	}
	
	/**
	 * Handels a search event, i. e. triggers a search
         * with the query provided by the SearchEvent
         */ 
	public void handleSearchEvent(SearchEvent evt) {
	    view.selectSearchPane();
	    AdvancedFinder finder = model.getAdvancedFinder();
            finder.handleSearchEvent(evt);
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setSearchResult(Object)
	 */
        public void setSearchResult(Object result)
	{
	    SearchResultCollection results = (SearchResultCollection) result;

		MetadataViewer metadata = model.getMetadataViewer();
		if (metadata != null) {
			metadata.setRootObject(null, -1, null);
		}
		if (results == null ) {
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
			db.setDisplayMode(model.getDisplayMode());
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
        model.getMetadataViewer().setRootObject(null, exp.getId(), null);
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
		DataBrowser db = DataBrowserFactory.getDataBrowser(
				model.getSecurityContext(parent), grandParentObject,
				parentObject, leaves, parent);
		db.setExperimenter(exp);
		db.addPropertyChangeListener(controller);
		view.removeAllFromWorkingPane();
		view.displayBrowser(db);
		db.setDisplayMode(model.getDisplayMode());
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
				
				db = DataBrowserFactory.getWellsDataBrowser(
						model.getSecurityContext(parent), m, parentObject, 
						(Set) entry.getValue(), withThumbnails);
			}
		}
		if (db != null) {
			db.addPropertyChangeListener(controller);
			view.removeAllFromWorkingPane();
			db.setDisplayMode(model.getDisplayMode());
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
				ExperimenterData exp = null;
				MetadataViewer mv = model.getMetadataViewer();
				int size = nodes.size();
				if (size == 1) {
					i = nodes.iterator();
					TreeImageDisplay parent;
					mv.setSelectionMode(size == 1);
					int mode = model.getDisplayMode();
					while (i.hasNext()) {
						node = i.next();
						if (mode == TreeViewer.EXPERIMENTER_DISPLAY) {
							parent = BrowserFactory.getDataOwner(node);
							exp = (ExperimenterData) parent.getUserObject();
						} else parent = EditorUtil.getDataGroup(node);
						browser.loadExperimenterData(parent, node);
						if (exp == null) exp = model.getUserDetails();
						node.setExpanded(true);
						mv.setRootObject(node.getUserObject(), exp.getId(),
								browser.getSecurityContext(node));
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
						exp = expNode == null ? null : (ExperimenterData) expNode.getUserObject();
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
						
						DataBrowser db = DataBrowserFactory.getDataBrowser(
								model.getSecurityContext(node), null,
								parent.getUserObject(), leaves, parent);
						if (db == null) return;
						db.addPropertyChangeListener(controller);
						db.setDisplayMode(model.getDisplayMode());
						view.displayBrowser(db);
						db.activate();
						model.setDataViewer(db);
						browser.setSelectedDisplay(node);
					}
				}
			}
			return;
		}
		ActionCmd actionCmd = null;
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
			if (TreeViewerAgent.runAsPlugin() == LookupNames.IMAGE_J ||
			        TreeViewerAgent.runAsPlugin() == LookupNames.IMAGE_J_IMPORT) {
				actionCmd = new ViewInPluginCmd(this, LookupNames.IMAGE_J);
			} else {
				actionCmd = new ViewCmd(this, true);
			}
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
				if (fa.isDirectory()) {
					model.getSelectedBrowser().loadExperimenterData(
							BrowserFactory.getDataOwner(node), 
		        			node);
				} else {
					//Import, register and view.
				}
			}
		}
		fireStateChange();
		if (actionCmd != null)
			actionCmd.execute();
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#deleteObjects(List)
	 */
	public void deleteObjects(List nodes)
	{
		if (nodes == null) return;
		Iterator i = nodes.iterator();
		Class<?> klass = null;
		List<Long> ids = new ArrayList<Long>();
		TreeImageDisplay node;
		Object uo;
		Map<SecurityContext, List<DataObject>> objects =
				new HashMap<SecurityContext, List<DataObject>>();
		SecurityContext ctx;
		List<DataObject> l;
		while (i.hasNext()) {
			node = (TreeImageDisplay) i.next();
			uo = node.getUserObject();
			ctx = model.getSecurityContext(node);
			
			if (uo instanceof ProjectData || uo instanceof DatasetData ||
				uo instanceof ImageData) {
				l = objects.get(ctx);
				if (l == null) {
					l = new ArrayList<DataObject>();
					objects.put(ctx, l);
				}
				l.add((DataObject) uo);
			}
		}
		if (objects.size() == 0) delete(nodes);
		else model.fireImageChecking(objects, nodes, ImageCheckerType.DELETE);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getDataToCopy()
	 */
	public List<DataObject> getDataToCopy()
	{
		if (model.getState() == DISCARDED) return null;
		return model.getDataToCopy();
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#onNodesMoved(DataObject)
	 */
	public void onNodesMoved(DataObject target)
	{
		if (model.getState() != SAVE) return;
		model.setState(READY);
		fireStateChange();
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		view.removeAllFromWorkingPane();
		DataBrowserFactory.discardAll();
		
		Browser browser = model.getSelectedBrowser();
		browser.refreshTree(null, target);
		model.getMetadataViewer().setRootObject(null, -1, null);
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
		if (CollectionUtils.isEmpty(deleted)) {
			onNodesMoved(null);
			return;
		}
		NotDeletedObjectDialog nd = new NotDeletedObjectDialog(view, deleted);
		if (nd.centerAndShow() == NotDeletedObjectDialog.CLOSE)
			onNodesMoved(null);
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
	public void download(File folder, boolean override)
	{
	    if (model.getState() == DISCARDED) return;
	    Browser browser = model.getSelectedBrowser();
	    if (browser == null) return;
	    List l = browser.getSelectedDataObjects();
	    if (l == null) return;
	    Iterator i = l.iterator();
	    Object object;
	    List<ImageData> archived = new ArrayList<ImageData>();
	    List<Long> filesetIds = new ArrayList<Long>();
	    ImageData image;
	    long id;
	    while (i.hasNext()) {
	        object = i.next();
	        if (object instanceof ImageData) {
	            image = (ImageData) object;
	            if (image.isArchived()) {
	                id = image.getFilesetId();
	                if (id < 0) archived.add(image);
	                else if (!filesetIds.contains(id)) {
	                    archived.add(image);
	                    filesetIds.add(id);
	                }
	            }
	        } else if (object instanceof FileAnnotationData) {
	            downloadFile(folder, override, (FileAnnotationData) object,
	                    null);
	        }
	    }
	    if (archived.size() > 0) {
            UserNotifier un = MetadataViewerAgent.getRegistry()
                    .getUserNotifier();
            IconManager icons = IconManager.getInstance();
            Icon icon = icons.getIcon(IconManager.DOWNLOAD_22);
            SecurityContext ctx = getSecurityContext();

            DownloadArchivedActivityParam p = new DownloadArchivedActivityParam(
                    folder, archived, icon);
            p.setOverride(override);
            p.setZip(false);
            p.setKeepOriginalPaths(true);
            un.notifyActivity(ctx, p);
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
			un.notifyActivity(model.getSecurityContext(), activity);
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
			SecurityContext ctx = model.getSecurityContext();
			while (i.hasNext()) {
				object = i.next();
				if (object instanceof ImageData || 
					object instanceof FileAnnotationData) {
					activity = new OpenActivityParam(data, (DataObject) object, 
							dir);
					un.notifyActivity(ctx, activity);
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
	 * @see TreeViewer#setUserGroup(List)
	 */
	public void setUserGroup(List<GroupData> groups)
	{
		if (model.getState() != READY) return;
		if (groups == null || groups.size() == 0) return;
		ExperimenterData exp = TreeViewerAgent.getUserDetails();
		//if (group.getId() == model.getSelectedGroupId()) return;
		//Scan browser and check if group is there.
		Browser browser = model.getBrowser(Browser.PROJECTS_EXPLORER);
		if (browser == null) return;
		//First check if groups already displayed
		ExperimenterVisitor v = new ExperimenterVisitor(browser, -1);
		browser.accept(v, ExperimenterVisitor.TREEIMAGE_SET_ONLY);
		List<TreeImageDisplay> nodes = v.getNodes();
		Iterator<TreeImageDisplay> i = nodes.iterator();
		//ids of the groups to add
		Iterator<GroupData> k = groups.iterator();
		Object o;
		GroupData group;
		//Displayed.
		List<Long> already = new ArrayList<Long>();
		while (i.hasNext()) {
			o = i.next().getUserObject();
			if (o instanceof GroupData) {
				group = (GroupData) o;
				already.add(group.getId());
			}
		}
		
		Map<Integer, Browser> browsers = model.getBrowsers();
		Iterator<Browser> j = browsers.values().iterator();

		//First remove the one that no longer in the list.
		boolean displayed = false;
		boolean set;
		while (j.hasNext()) {
			browser = j.next();
			//now add.
			k = groups.iterator();
			set = false;
			while (k.hasNext()) {
				group = k.next();
				if (!already.contains(group.getId())) {
					browser.setUserGroup(group);
					model.setSelectedGroupId(group.getId());
					notifyChangeGroup(group.getId());
					set = true;
				}
			}
			if (!set) {
				group = groups.get(0);
				model.setSelectedGroupId(group.getId());
				notifyChangeGroup(group.getId());
			}
			view.setPermissions();
		}
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
		//TODO: review
		/*
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
		*/
		return new HashMap<Long, String>();
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
		if (browser.register(file.getData())) {
			/*
			model.getMetadataViewer().saveData(file.getToAdd(), 
					file.getToRemove(), file.getToDelete(), 
					file.getMetadata(), file.getData(), true);
					*/
		}
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getGroupPermissions(GroupData)
	 */
	public int getGroupPermissions(GroupData group)
	{
		if (group == null)
			return GroupData.PERMISSIONS_PRIVATE;
		PermissionData data = group.getPermissions();
		if (data.isGroupRead()) {
			if (data.isGroupWrite())
				return GroupData.PERMISSIONS_GROUP_READ_WRITE;
			if (data.isGroupAnnotate()) {
				return GroupData.PERMISSIONS_GROUP_READ_LINK;
			}
			return GroupData.PERMISSIONS_GROUP_READ;
		}
		if (data.isWorldRead()) {
			if (data.isWorldWrite()) 
				return GroupData.PERMISSIONS_PUBLIC_READ_WRITE;
			return GroupData.PERMISSIONS_PUBLIC_READ;
		}
		return GroupData.PERMISSIONS_PRIVATE;
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
		model.setRndSettings(null, null);
		model.setNodesToCopy(null, -1);
		//remove thumbnails browser
		view.removeAllFromWorkingPane();
		model.setDataViewer(null);
		
		//reset metadata
		MetadataViewer mv = view.resetMetadataViewer();

		//reset search
		clearFoundResults();
	}
	
	/**
	 * Invokes when reconnected to the server.
	 */
	void onReconnected()
	{
	        model.setRndSettings(null, null);
		model.setNodesToCopy(null, -1);
		//remove thumbnails browser
		view.removeAllFromWorkingPane();
		model.setDataViewer(null);
		model.setAvailableScripts(null);
		model.clearImportResult();
		view.onImport();
		view.clearMenus();
		//reset metadata
		MetadataViewer mv = view.resetMetadataViewer();

		//reset search
		clearFoundResults();
		model.getAdvancedFinder().reset(
				TreeViewerAgent.getAvailableUserGroups());
		ExperimenterData exp = model.getUserDetails();
		model.setSelectedGroupId(exp.getDefaultGroup().getId());
		
		
		view.createTitle();
		view.setPermissions();
		Browser b = view.resetLayout();
		if (b != null) {
			Browser old = model.getSelectedBrowser();
			model.setSelectedBrowser(b);
			model.getMetadataViewer().setSelectionMode(false);
			firePropertyChange(SELECTED_BROWSER_PROPERTY, old, b);
			int t = b.getBrowserType();
			EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
			bus.post(new BrowserSelectionEvent(t));
			view.updateMenuItems();
		}
		Map<Integer, Browser> browsers = model.getBrowsers();
		Entry entry;
		Browser browser;
		Iterator i = browsers.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			browser = (Browser) entry.getValue();
			browser.reActivate();
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
			        model.getMetadataViewer().setRootObject(null, exp.getId(),
			        		null);
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
		Set<Long> groupIds = new HashSet<Long>();
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
				Object object;
				while (i.hasNext()) {
					node = i.next();
					node.setToRefresh(true);
					object = node.getUserObject();
					if (object instanceof DataObject) {
						groupIds.add(((DataObject) object).getGroupId());
					}
					parent = node.getParentDisplay();
					if (parent != null && !parent.isExpanded())
						browser.expand(parent);
				}
				ExperimenterData exp = TreeViewerAgent.getUserDetails();
				ExperimenterVisitor v = new ExperimenterVisitor(browser, 
						exp.getId(), groupIds);
				browser.accept(v, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
				List<TreeImageDisplay> l = v.getNodes();
				if (l != null) {
					Iterator<TreeImageDisplay> j = l.iterator();
					while (j.hasNext()) {
						node = j.next();
						node.setExpanded(true);
						node.setToRefresh(refreshTree);
					}
				}
				browser.getUI().repaint();
			}
		}
	}
	
	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setImporting(boolean, List, boolean, Object)
	 */
	public void setImporting(boolean importing, List<DataObject> containers,
			boolean refreshTree, Object importResult)
	{
		if (model.getState() == DISCARDED) return;
		model.setImporting(importing, importResult);
		view.onImport();
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
				if (ho instanceof ImageData && canLink(ho))
					images.add(ho);
			}
			if (images.size() == 0) {
				UserNotifier un = 
					TreeViewerAgent.getRegistry().getUserNotifier();
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
		if (browser == null)
		    objects = new ArrayList<DataObject>();
		else 
		    objects = browser.getSelectedDataObjects();

		if (CollectionUtils.isEmpty(objects)) {
		    DataBrowser db = model.getDataViewer();
		    objects = new ArrayList<DataObject>();
		    if (db != null && db.getBrowser() != null) {
		        objects.addAll(db.getBrowser().getSelectedDataObjects());
		    }
		}
		
        // if it is a script which operates on FileAnnotations, pass on the selected
        // FileAnnotations in the MetadataViewer as additional reference objects
        ListMultimap<String, DataObject> addObjects = null;
        Pattern p = Pattern.compile("file.?annotation.*",
                Pattern.CASE_INSENSITIVE);
        for (String paramName : script.getInputs().keySet()) {
            Matcher m = p.matcher(paramName);
            if (m.matches()) {
                addObjects = ArrayListMultimap.create();
                if (model.getMetadataViewer() != null) {
                    addObjects.putAll(paramName, model.getMetadataViewer()
                            .getEditor().getSelectedFileAnnotations());
                }
            }
        }
		
		//setStatus(false);
		//Check if the objects are in the same group.
		Iterator<DataObject> i = objects.iterator();
		List<Long> ids = new ArrayList<Long>();
		DataObject object;
		long id = -1;
		while (i.hasNext()) {
			object = i.next();
			if (!(object instanceof ExperimenterData ||
					object instanceof GroupData)) {
				id = object.getGroupId();
				if (!ids.contains(id) && id != -1)
					ids.add(id);
			}
		}
		if (ids.size() > 1) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Run Script", ScriptingDialog.WARNING);
			return;
		}
		if (scriptDialog == null) {
			scriptDialog = new ScriptingDialog(view, 
					model.getScript(script.getScriptID()), objects, addObjects,
					TreeViewerAgent.isBinaryAvailable());
			scriptDialog.addPropertyChangeListener(controller);
			UIUtilities.centerAndShow(scriptDialog);
		} else {
			scriptDialog.reset(model.getScript(script.getScriptID()), objects, addObjects);
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
	    if (target == null || CollectionUtils.isEmpty(nodes)) return;
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
	        //Check the source
	        Iterator<TreeImageDisplay> i = nodes.iterator();
	        TreeImageDisplay n;
	        ExperimenterData exp;
	        long id = model.getUserDetails().getId();
	        TreeImageDisplay p;
	        GroupData g;
	        String message = null;
	        while (i.hasNext()) {
	            n = i.next();
	            if (n.getUserObject() instanceof ExperimenterData) {
	                exp = (ExperimenterData) n.getUserObject();
	                p = n.getParentDisplay();
	                if (p != null && p.getUserObject() instanceof GroupData) {
	                    g = (GroupData) p.getUserObject();
	                    if (model.isSystemGroup(g.getId(),
	                            GroupData.SYSTEM)) {
	                        if (exp.getId() == id) {
	                            message = "An administrator cannot remove " +
	                                    "himself/herself from "+g.getName()+
	                                    " Group.";
	                            break;
	                        } else if (model.isSystemUser(exp.getId(),
	                                GroupData.SYSTEM)) {
	                            message = "An administrator cannot remove " +
                                        exp.getUserName()+" from "+g.getName()
                                        +" Group.";
                                break;
	                        }
	                    } else if (model.isSystemGroup(g.getId(),
	                            GroupData.GUEST) &&
	                            model.isSystemUser(exp.getId(),
	                                    GroupData.GUEST)) {
	                        message = "An administrator cannot remove " +
                                    exp.getUserName()+" from "+g.getName()+
                                    " Group.";
	                        break;
	                    }
	                }
	            }
	        }
	        if (message != null) {
	            un.notifyInfo("DnD", message);
	            browser.rejectTransfer();
	            return;
	        }
	    }
	    if (!canLink(ot) && !(ot instanceof ExperimenterData ||
	            ot instanceof GroupData)) {
	        un.notifyInfo("DnD", 
	                "You must be the owner of the container.");
	        browser.rejectTransfer();
	        return;
	    }
	    TreeImageDisplay p = target.getParentDisplay();
	    List<TreeImageDisplay> list = new ArrayList<TreeImageDisplay>();
	    Iterator<TreeImageDisplay> i = nodes.iterator();
	    TreeImageDisplay n;

	    int count = 0;
	    String child;
	    String parent;
	    os = null;
	    int childCount = 0;
	    long userID = TreeViewerAgent.getUserDetails().getId();
	    boolean administrator = TreeViewerAgent.isAdministrator();
	    ExperimenterData exp;
	    long gId;
	    List<Long> groupIds = new ArrayList<Long>();
	    DataObject data;
	    while (i.hasNext()) {
	        n = i.next();
	        os = n.getUserObject();
	        if (target.contains(n)) {
	            childCount++;
	        } else {
	            if (EditorUtil.isTransferable(ot, os, userID)) {
	                count++;
	                if (ot instanceof GroupData) {
	                    if (os instanceof ExperimenterData &&
	                            administrator) {
	                        list.add(n);
	                    } else {
	                        if (canEdit(os)) {
	                            data = (DataObject) os;
	                            if (!groupIds.contains(data.getGroupId()))
	                                groupIds.add(data.getGroupId());
	                            list.add(n);
	                        }
	                    }
	                } else {
	                    if (ot instanceof ExperimenterData) {
	                        exp = (ExperimenterData) ot;
	                        if (exp.getId() == userID) {
	                            target = null;
	                            list.add(n);
	                            data = (DataObject) os;
	                            if (!groupIds.contains(data.getGroupId()))
                                    groupIds.add(data.getGroupId());
	                        }
	                    } else {
	                        if (canEdit(os)) {
	                            list.add(n);
	                            data = (DataObject) os;
	                            if (!groupIds.contains(data.getGroupId()))
	                                groupIds.add(data.getGroupId());
	                        }
	                    }
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
	                "The "+getObjectType(os)+s+" cannot be moved to the " +
	                        "selected "+getObjectType(ot)+".");
	        browser.rejectTransfer();
	        return;
	    }
	    DataObject otData = (DataObject) ot;
	    GroupData group = null;
	    if (ot instanceof ExperimenterData) {
	        Object po = p.getUserObject();
	        if (po instanceof GroupData) group = (GroupData) po;
	    } else if (ot instanceof GroupData) {
	        group = (GroupData)ot;
	    }
	    else { 
            for (Object gd : TreeViewerAgent.getAvailableUserGroups()) {
                if (((GroupData) gd).getId() == otData.getGroupId()) {
                    group = (GroupData) gd;
                    break;
                }
            }
	    }
	    //to review
	    if (browser.getBrowserType() == Browser.ADMIN_EXPLORER)
	        model.transfer(target, list);
	    else if (groupIds.size() == 1 && groupIds.get(0) == group.getId())
	        model.transfer(target, list);
	    else {
	        if (group == null) return;
	        MessageBox box = new MessageBox(view, "Change group", "Are you " +
	                "sure you want to move the selected items to another group?");
	        if (box.centerMsgBox() != MessageBox.YES_OPTION) return;
	        otData = null;
	        if (target != null && !(ot instanceof GroupData)) {
	            otData = (DataObject) ot;
	        }

	        Map<Long, List<DataObject>> elements =
	                new HashMap<Long, List<DataObject>>();
	        i = list.iterator();
	        long gid;
	        List<DataObject> l;
	        while (i.hasNext()) {
	            n = i.next();
	            os = n.getUserObject();
	            if (os instanceof DataObject &&
	                    !(os instanceof ExperimenterData ||
	                            os instanceof GroupData)) {
	                gid = ((DataObject) os).getGroupId();
	                if (!elements.containsKey(gid)) {
	                    elements.put(gid, new ArrayList<DataObject>());
	                }
	                l = elements.get(gid);
	                l.add((DataObject) os);
	            }
	        }
	        if (elements.size() == 0) return;
	        Iterator<Long> j = elements.keySet().iterator();
	        Map<SecurityContext, List<DataObject>> trans = 
	                new HashMap<SecurityContext, List<DataObject>>();
	        while (j.hasNext()) {
	            gid = j.next();
	            trans.put(new SecurityContext(gid), elements.get(gid));
	        }
	        if (target == null) otData = null;
	        ChgrpObject object = new ChgrpObject(group, otData, trans);
	        model.fireImageChecking(trans, object, ImageCheckerType.CHGRP);
	    }
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getSelectedGroup()
	 */
	public GroupData getSelectedGroup()
	{ 
		if (model.getState() == DISCARDED) return null;
		return model.getSelectedGroup();
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#hasSingleGroupDisplayed()
	 */
	public GroupData getSingleGroupDisplayed()
	{
	    if (model.getState() == DISCARDED) return null;
	    Collection<GroupData> groups = model.getAvailableGroups();
	    if (groups != null && groups.size() == 1) return null;
	    Browser browser = model.getSelectedBrowser();
	    if (browser == null) return null;

	    TreeImageDisplay node = null;
	    ExperimenterVisitor v = new ExperimenterVisitor(browser, -1);
	    browser.accept(v, ExperimenterVisitor.TREEIMAGE_SET_ONLY);
	    List<TreeImageDisplay> nodes = v.getNodes();
        if (nodes.size() == 1)
            return (GroupData) nodes.get(0).getUserObject();
        return null;
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#removeGroup(long)
	 */
	public void removeGroup(long groupId)
	{
		if (model.getState() == DISCARDED) return;
		Browser browser = model.getSelectedBrowser();
		if (browser == null) return;

		TreeImageDisplay node = null;
		ExperimenterVisitor v = new ExperimenterVisitor(browser, -1);
		browser.accept(v, ExperimenterVisitor.TREEIMAGE_SET_ONLY);
		//do not remove the last group.
		List<TreeImageDisplay> groups = v.getNodes();
		//Find the node
		Iterator<TreeImageDisplay> j = groups.iterator();
		TreeImageDisplay n;
		while (j.hasNext()) {
			n = j.next();
			if (n.getUserObjectId() == groupId) {
				node = n;
				break;
			}
		}
		if (node == null) return;
		TreeViewerAgent.getRegistry().getEventBus().post(
				new ActivitiesEvent(model.getSecurityContext(node)));
		GroupData group = (GroupData) node.getUserObject();
		Map<Integer, Browser> browsers = model.getBrowsers();
		Iterator i = browsers.entrySet().iterator();
		Entry entry;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			browser = (Browser) entry.getValue();
			browser.removeGroup(group);
		}
		j = groups.iterator();
		GroupData g;
		long gid;
		while (j.hasNext()) {
			g = (GroupData) j.next().getUserObject();
			if (g.getId() != group.getId()) {
				gid = model.getSelectedGroupId();
				model.setSelectedGroupId(g.getId());
				notifyChangeGroup(gid);
				break;
			}
		}
		DataBrowserFactory.discardAll();
        view.removeAllFromWorkingPane();
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#moveTo(GroupData, List)
	 */
	public void moveTo(GroupData group, List<DataObject> nodes)
	{
		if (group == null) 
			throw new IllegalArgumentException("No group to move data to.");
		
		if (nodes == null || nodes.size() == 0) return;
		Map<SecurityContext, List<DataObject>> 
		map = new HashMap<SecurityContext, List<DataObject>>();
		Iterator<DataObject> i = nodes.iterator();
		Class b = null;
		DataObject data;
		SecurityContext ctx;
		long gid;
		List<DataObject> l;
		long refgid = group.getId();
		List<Long> userIDs = new ArrayList<Long>();
		while (i.hasNext()) {
			data = i.next();
		    if (canChgrp(data)) {
		    	if (data instanceof ProjectData || data instanceof ScreenData)
					b = data.getClass();
				gid = data.getGroupId();
				if (!userIDs.contains(data.getOwner().getId()))
					userIDs.add(data.getOwner().getId());
				if (gid != refgid) {
					ctx = getKey(map, gid);
					if (ctx == null) {
						l = new ArrayList<DataObject>();
						ctx = new SecurityContext(gid);
						map.put(ctx, l);
					}
					l = map.get(ctx);
					l.add(data);
				}
		    }
		}
		if (userIDs.size() != 1) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Move Data ", "You can only move the data of one " +
					"member at a time.");
			return;
		}
		if (map.size() == 0) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Move Data ", "No data to move to "+group.getName());
			return;
		}
		long userID = -1;
		if (b == null) userID = userIDs.get(0);
		ChgrpObject object = new ChgrpObject(group, null, map);
		object.setUserID(userID);
		model.fireImageChecking(map, object, ImageCheckerType.CHGRP);
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#displayUserGroups(Point)
	 */
	public void displayUserGroups(Point location)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		JFrame f = (JFrame) TreeViewerAgent.getRegistry().getTaskBar();
		IconManager icons = IconManager.getInstance();
		Collection groups = TreeViewerAgent.getAvailableUserGroups();
		if (groups.size() <= 1) return;
		
		Browser browser = model.getBrowser(Browser.PROJECTS_EXPLORER);
		ExperimenterVisitor visitor = new ExperimenterVisitor(browser, -1);
		browser.accept(visitor);
		List<TreeImageDisplay> nodes = visitor.getNodes();
		List<GroupData> selected = new ArrayList<GroupData>();
		Iterator<TreeImageDisplay> j = nodes.iterator();
		TreeImageDisplay n;
		while (j.hasNext()) {
			n = j.next();
			if (n.getUserObject() instanceof GroupData) {
				selected.add((GroupData) n.getUserObject());
			}
		}
		GroupManagerDialog dialog = new GroupManagerDialog(f, groups, selected,
				icons.getIcon(IconManager.OWNER_GROUP_48));
		dialog.addPropertyChangeListener(controller);
		dialog.setDefaultSize();
		UIUtilities.showOnScreen(dialog, location);
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getSecurityContext()
	 */
	public SecurityContext getSecurityContext()
	{
		return model.getSecurityContext();
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getGroups()
	 */
	public Collection getGroups() { return model.getGroups(); }

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getDisplayMode()
	 */
	public int getDisplayMode() { return model.getDisplayMode(); }
	
	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setDisplayMode(int)
	 */
	public void setDisplayMode(int index)
	{
		if (model.getState() != READY || model.getDisplayMode() == index)
			return;
		//First check if groups already displayed
		model.setRndSettings(null, null);
		model.setNodesToCopy(null, -1);
		//remove thumbnails browser
		view.removeAllFromWorkingPane();
		model.setDataViewer(null);
		//reset search
		clearFoundResults();
				
		model.setDisplayMode(index);
		Map<Integer, Browser> browsers = model.getBrowsers();
		Entry entry;
		Browser browser;
		Iterator i = browsers.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			browser = (Browser) entry.getValue();
			browser.changeDisplayMode();
		}
		TreeViewerAgent.getRegistry().getEventBus().post(
				new DisplayModeEvent(model.getDisplayMode()));
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#handleSplitImage(Map, Object, int)
	 */
	public void handleSplitImage(ImageCheckerResult result,
			final Object action, ImageCheckerType index)
	{
		if (!CollectionUtils.isEmpty(result.getMifResults())) {
			//Indicate what do depending on the index.
			MIFNotificationDialog dialog = new MIFNotificationDialog(view,
					result.getMifResults(), action, index,
					TreeViewerAgent.getAvailableUserGroups());
			dialog.addPropertyChangeListener(new PropertyChangeListener() {
				
				/** 
				 * Moves the data.
				 */
				public void propertyChange(PropertyChangeEvent evt) {
					String name = evt.getPropertyName();
					if (MIFNotificationDialog.MOVE_ALL_PROPERTY.equals(name)) {
						moveObject((ChgrpObject) evt.getNewValue());
					}
				}
			});
			UIUtilities.centerAndShow(dialog);
			return;
		}
		// show a warning if the images to be deleted are linked to multiple datasets:
		if (ImageCheckerType.DELETE.equals(index) && !result.getMultiLinkedImages().isEmpty()) {
		        LinkNotificationDialog dialog = new LinkNotificationDialog(view, result);
			dialog.addPropertyChangeListener(new PropertyChangeListener() {
				
				/** 
				 * Removes the data
				 */
				public void propertyChange(PropertyChangeEvent evt) {
					String name = evt.getPropertyName();
					if (LinkNotificationDialog.DELETE_PROPERTY.equals(name)) {
						delete((List) action);
					}					
				}
				
			});
			UIUtilities.centerAndShow(dialog);
			return;
		}
		if (ImageCheckerType.DELETE.equals(index)) {
			delete((List) action);
		} else if (ImageCheckerType.CHGRP.equals(index)) {
			moveObject((ChgrpObject) action);
		}
	}

    /** 
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#isSystemUser(long)
     */
    public boolean isSystemUser(long userID)
    {
        return model.isSystemUser(userID);
    }

    /** 
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#isSystemUser(long, String)
     */
    public boolean isSystemUser(long userID, String key)
    {
        return model.isSystemUser(userID, key);
    }
    
    /** 
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#isSystemGroup(long, String)
     */
    public boolean isSystemGroup(long groupID, String key)
    {
        return model.isSystemGroup(groupID, key);
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#getSelectedObjectsFromBrowser()
     */
    public Collection<DataObject> getSelectedObjectsFromBrowser()
    {
        DataBrowser db = model.getDataViewer();
        if (db != null && db.getBrowser() != null) {
            return db.getBrowser().getSelectedDataObjects();
        }
        return null;
    }
}
