/*
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
package org.openmicroscopy.shoola.agents.treeviewer.browser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;

import org.apache.commons.collections.CollectionUtils;

import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserDeleteAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserImportAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserManageAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserPasswordResetAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserRefreshAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CloseAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CollapseAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ShowNameAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SortAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SortByDateAction;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.browser.TreeFileSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.FileData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;

/** 
 * The Browser's Controller.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *          <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
class BrowserControl
    implements ChangeListener
{

    /** Identifies the <code>Collapse</code> action. */
    static final Integer COLLAPSE = Integer.valueOf(0);

    /** Identifies the <code>Close</code> action. */
    static final Integer CLOSE = Integer.valueOf(1);

    /** Identifies the <code>Sort</code> action. */
    static final Integer SORT = Integer.valueOf(2);

    /** Identifies the <code>Sort by Date</code> action. */
    static final Integer SORT_DATE = Integer.valueOf(3);

    /** Identifies the <code>Partial Name</code> action.*/
    static final Integer PARTIAL_NAME = Integer.valueOf(4);

    /** Identifies the <code>Info</code> action. */
    static final Integer INFO = Integer.valueOf(5);

    /** Identifies the <code>Delete</code> action. */
    static final Integer DELETE = Integer.valueOf(6);

    /** Identifies the <code>New container</code> action. */
    static final Integer NEW_CONTAINER = Integer.valueOf(7);

    /** Identifies the <code>New tag</code> action. */
    static final Integer NEW_TAG = Integer.valueOf(8);

    /** Identifies the <code>Import</code> action. */
    static final Integer IMPORT = Integer.valueOf(9);

    /** Identifies the <code>Refresh</code> action. */
    static final Integer REFRESH = Integer.valueOf(10);

    /** Identifies the <code>New Admin</code> action. */
    static final Integer NEW_ADMIN = Integer.valueOf(11);

    /** Identifies the <code>Reset Password</code> action. */
    static final Integer RESET_PASSWORD = Integer.valueOf(12);

    /** Identifies the <code>New container</code> action. */
    static final Integer CUT = Integer.valueOf(13);

    /** Identifies the <code>New container</code> action. */
    static final Integer COPY = Integer.valueOf(14);

    /** Identifies the <code>New container</code> action. */
    static final Integer PASTE = Integer.valueOf(15);
    /** 
     * Reference to the {@link Browser} component, which, in this context,
     * is regarded as the Model.
     */
    private Browser model;

    /** Reference to the View. */
    private BrowserUI view;

    /** Maps actions identifiers onto actual <code>Action</code> object. */
    private Map<Integer, Action>	actionsMap;

    /** Helper method to create all the UI actions. */
    private void createActions()
    {
        actionsMap.put(COLLAPSE, new CollapseAction(model));
        actionsMap.put(CLOSE, new CloseAction(model));
        actionsMap.put(SORT, new SortAction(model));
        actionsMap.put(SORT_DATE, new SortByDateAction(model));
        actionsMap.put(PARTIAL_NAME, new ShowNameAction(model));
        actionsMap.put(DELETE, new BrowserDeleteAction(model));
        actionsMap.put(NEW_CONTAINER, new BrowserManageAction(model,
                BrowserManageAction.NEW_CONTAINERS));
        actionsMap.put(NEW_ADMIN, new BrowserManageAction(model,
                BrowserManageAction.NEW_ADMIN));
        actionsMap.put(NEW_TAG, new BrowserManageAction(model,
                BrowserManageAction.NEW_TAGS));
        actionsMap.put(IMPORT, new BrowserImportAction(model));
        actionsMap.put(REFRESH, new BrowserRefreshAction(model));
        actionsMap.put(RESET_PASSWORD, new BrowserPasswordResetAction(model));
        actionsMap.put(CUT, new BrowserManageAction(model,
                BrowserManageAction.CUT));
        actionsMap.put(COPY, new BrowserManageAction(model,
                BrowserManageAction.COPY));
        actionsMap.put(PASTE, new BrowserManageAction(model,
                BrowserManageAction.PASTE));
    }

    /**
     * Creates a new instance.
     * The {@link #initialize(BrowserUI) initialize} method 
     * should be called straight after to link this Controller to the other 
     * MVC components.
     *
     * @param model Reference to the {@link Browser} component, which, in 
     *               this context, is regarded as the Model.
     *               Mustn't be <code>null</code>.
     */
    BrowserControl(Browser model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        actionsMap = new HashMap<Integer, Action>();
        createActions();
    }

    /**
     * Links this Controller to its Model and its View.
     * 
     * @param view Reference to the View. Mustn't be <code>null</code>.
     */
    void initialize(BrowserUI view)
    {
        if (view == null) throw new NullPointerException("No view.");
        this.view = view;
        model.addChangeListener(this);
    }

    /**
     * Selects the specified nodes.
     * 
     * @param nodes The nodes to select.
     * @param updateView Pass <code>true</code> to update the view,
     * <code>false</code> otherwise.
     */
    void selectNodes(List nodes, Class ref)
    {
        if (CollectionUtils.isEmpty(nodes)) return;
        //make sure we have node of the same type.
        Iterator<?> i = nodes.iterator();
        TreeImageDisplay n;
        List<TreeImageDisplay> values = new ArrayList<TreeImageDisplay>();
        Object o;
        while (i.hasNext()) {
            n = (TreeImageDisplay) i.next();
            o = n.getUserObject();
            if (o.getClass().equals(ref))
                values.add(n);
        }
        if (CollectionUtils.isEmpty(values)) return;
        TreeImageDisplay[] array = values.toArray(
                new TreeImageDisplay[values.size()]);
        model.setSelectedDisplays(array, false);
        view.setFoundNode(array);
    }

    /**
     * Invokes the right-click is selected.
     */
    void onRightClick(TreeImageDisplay node)
    {
        TreeImageDisplay d  = model.getLastSelectedDisplay();
        if (node != d) {
            model.setSelectedDisplay(node);
        }
    }

    /**
     * Reacts to tree expansion events.
     * 
     * @param display The selected node.
     * @param expanded Pass <code>true</code> if the node is expanded,
     *                 <code>false</code> otherwise.
     */
    void onNodeNavigation(TreeImageDisplay display, boolean expanded)
    {
        display.setExpanded(expanded);
        Object ho = display.getUserObject();
        if (model.getBrowserType() == Browser.FILE_SYSTEM_EXPLORER) {
            if (ho instanceof FileData) {
                FileData f = (FileData) ho;
                if (f.isDirectory()) model.loadDirectory(display);
                return;
            } else if ((ho instanceof ImageData) && display.isChildrenLoaded())
                return;
        } 
        if (!expanded) {
            model.cancel();
            return;
        }
        int state = model.getState();
        if (state == Browser.LOADING_DATA || state == Browser.LOADING_LEAVES)
            return;
        model.setSelectedDisplay(display);
        int browserType = model.getBrowserType();
        if ((browserType == Browser.IMAGES_EXPLORER ||
                browserType == Browser.FILES_EXPLORER) &&
                !display.isChildrenLoaded() && (ho instanceof ExperimenterData
                        || ho instanceof GroupData)) {
            model.countExperimenterImages(display);
            return;
        } 
        if (display.isChildrenLoaded()) {
            if (view.isFirstChildMessage(display)) {
                List<?> l = display.getChildrenDisplay();
                List<Object> list = new ArrayList<Object>(l.size());
                Iterator<?> i = l.iterator();
                while (i.hasNext()) {
                    list.add(i.next());
                }
                view.setLeavesViews(list, (TreeImageSet) display);
            }
            return;
        }
        if (ho instanceof ProjectData || ho instanceof ScreenData ||
                ho instanceof PlateData || ho instanceof GroupData) {
            if (display.getNumberOfItems() == 0) return;
        }

        TreeImageDisplay node;
        if (display instanceof TreeImageTimeSet ||
                display instanceof TreeFileSet) {
            view.loadAction(display);
            switch (model.getDisplayMode()) {
            case TreeViewer.GROUP_DISPLAY:
                node = EditorUtil.getDataGroup(display);
                break;
            case TreeViewer.EXPERIMENTER_DISPLAY:
            default:
                node = BrowserFactory.getDataOwner(display);
            }
            model.loadExperimenterData(node, display);
            return;
        }
        if (ho instanceof DatasetData || ho instanceof TagAnnotationData) {
            view.loadAction(display);
            //Load the data of the logged in user.

            switch (model.getDisplayMode()) {
            case TreeViewer.GROUP_DISPLAY:
                node = EditorUtil.getDataGroup(display);
                break;
            case TreeViewer.EXPERIMENTER_DISPLAY:
            default:
                node = BrowserFactory.getDataOwner(display);
            }
            model.loadExperimenterData(node, display);
        } else if (ho instanceof ExperimenterData) {
            view.loadAction(display);
            model.loadExperimenterData(display, null);
        } else if (ho instanceof GroupData) {
            if (browserType == Browser.ADMIN_EXPLORER) {
                view.loadAction(display);
                model.loadExperimenterData(display, display);
            } else {
                //Load the data of the logged in user.
                switch (model.getDisplayMode()) {
                case TreeViewer.GROUP_DISPLAY:
                    view.loadAction(display);
                    model.loadExperimenterData(display, null);
                    break;
                case TreeViewer.EXPERIMENTER_DISPLAY:
                default:
                    List<?> l = display.getChildrenDisplay();
                    if (l != null & l.size() > 0) {
                        view.expandNode((TreeImageDisplay) l.get(0), true);
                    }
                }
            }
        }
    }

    /**
     * Brings up the pop-up menu.
     * 
     * @param index The index of the menu.
     */
    void showPopupMenu(int index) { model.showPopupMenu(index); }

    /** 
     * Reacts to click events in the tree.
     * 
     *  @param added The collection of added paths.
     */
    void onClick(List<TreePath> added)
    {
        JTree tree = view.getTreeDisplay();
        TreePath[] paths = tree.getSelectionPaths();
        if (paths == null) {
            return;
        }
        TreeImageDisplay node;
        TreePath path;
        List<TreePath> toRemove = new ArrayList<TreePath>();
        TreeImageDisplay[] nodes;
        if (paths.length == 1) {
            Object p = paths[0].getLastPathComponent();
            if (!(p instanceof TreeImageDisplay))
                return;
            node = (TreeImageDisplay) p;
            if (node.isSelectable()) {
                nodes = new TreeImageDisplay[1];
                nodes[0] = node;
                model.setSelectedDisplays(nodes, false);
            } else {
                toRemove.add(paths[0]);
                view.removeTreePaths(toRemove);
                paths = tree.getSelectionPaths();
                nodes = new TreeImageDisplay[paths.length];
                for (int j = 0; j < paths.length; j++) {
                    nodes[j] =
                            (TreeImageDisplay) paths[j].getLastPathComponent();
                }
                model.setSelectedDisplays(nodes, false);
            }
            return;
        }
        //more than one node selected.
        TreeImageDisplay previous = model.getLastSelectedDisplay();
        Class<?> ref = null;
        Object ho = null;
        if (previous != null) {
            ho = previous.getUserObject();
            ref = ho.getClass();
        }

        List<TreeImageDisplay> l = new ArrayList<TreeImageDisplay>();
        TagAnnotationData tag;
        String ns = null;
        if (TagAnnotationData.class.equals(ref)) {
            ns = ((TagAnnotationData) ho).getNameSpace();
        }
        if (added != null) {
            Iterator<TreePath> i = added.iterator();
            Object nho;
            String nsNode;
            Object lastPath;
            while (i.hasNext()) {
                path = i.next();
                lastPath = path.getLastPathComponent();
                if (lastPath instanceof TreeImageDisplay) {
                    node = (TreeImageDisplay) lastPath;
                    nho = node.getUserObject();
                    if (nho.getClass().equals(ref) && node.isSelectable()) {
                        if (nho.getClass().equals(TagAnnotationData.class)) {
                            nsNode = ((TagAnnotationData) nho).getNameSpace();
                            if (ns == null && nsNode == null) l.add(node);
                            else if (ns == null && nsNode != null &&
                                    TagAnnotationData.INSIGHT_TAGSET_NS.equals(
                                            nsNode))
                                toRemove.add(path);
                            else if (ns != null && nsNode == null &&
                                    TagAnnotationData.INSIGHT_TAGSET_NS.equals(
                                            ns))
                                toRemove.add(path);
                            else if (ns != null && nsNode != null) {
                                if (ns.equals(nsNode))
                                    l.add(node);
                            }
                        } else l.add(node);
                    }
                    else toRemove.add(path);
                }
            }
        }

        if (toRemove.size() > 0) {
            String text = null;
            if (ImageData.class.equals(ref)) text = "Images.";
            else if (ProjectData.class.equals(ref)) text = "Projects.";
            else if (DatasetData.class.equals(ref)) text = "Datasets.";
            else if (ScreenData.class.equals(ref)) text = "Screens.";
            else if (PlateData.class.equals(ref)) text = "Plates.";
            else if (PlateAcquisitionData.class.equals(ref)) 
                text = "Acquisitions.";
            else if (TagAnnotationData.class.equals(ref)) {
                tag = (TagAnnotationData) ho;
                if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(
                        tag.getNameSpace())) text = "Tag Sets.";
                else text = "Tags.";
            } else if (FileAnnotationData.class.equals(ref)) text = "Files.";
            else if (FileData.class.equals(ref)) text = "Files.";
            else if (ExperimenterData.class.equals(ref)) text = "Experimenters";
            else if (GroupData.class.equals(ref)) text = "User Groups";
            //smart folder selected
            if (text == null) text = "nodes of the same type.";
            UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Tree selection", "You can only select "+text);
            view.removeTreePaths(toRemove);
        }
        paths = tree.getSelectionPaths();

        List<TreeImageDisplay> list = new ArrayList<TreeImageDisplay>();
        if (paths != null) {
            for (int j = 0; j < paths.length; j++) {
                if (paths[j].getLastPathComponent() instanceof TreeImageDisplay)
                    list.add((TreeImageDisplay) paths[j].getLastPathComponent());
            }
        }

        if (list.size() > 0) {
            model.setSelectedDisplays(list.toArray(
                    new TreeImageDisplay[list.size()]), false);
        }
    }

    /**
     * Returns the action corresponding to the specified id.
     * 
     * @param id One of the flags defined by this class.
     * @return The specified action.
     */
    Action getAction(Integer id) { return actionsMap.get(id); }

    /**
     * Detects when the {@link Browser} is ready and then registers for
     * property change notification.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        int state = model.getState();
        switch (state) {
        case Browser.BROWSING_DATA:
            break;
        default:
            break;
        }
        view.onStateChanged(state == Browser.READY);
    }

}
