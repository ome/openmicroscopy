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
package org.openmicroscopy.shoola.agents.treeviewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeFileSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.MetadataHandlerView;

import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;

/** 
 * Reloads the data for the specified experimenters.
 * This class calls the <code>refreshHierarchy</code> in the
 * <code>DataManagerView</code>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class RefreshExperimenterDataLoader
    extends DataBrowserLoader
{

    /** The type of the nodes to select when done. */
    private Class<?> type;

    /** The identifier of the object to select when done. */
    private long id;

    /** The type of the root node. */
    private Class<?> rootNodeType;

    /** Collection of {@link RefreshExperimenterDef} objects. */
    private Map<SecurityContext, RefreshExperimenterDef> expNodes;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /** The node of reference hosting the node to browse. */
    private Object refNode;

    /** The data object to browse. */
    private DataObject toBrowse;

    /** The smart folder for tags.*/
    private Map<SecurityContext, TreeImageSet> smartFolders;

    /**
     * Controls if the passed class is supported.
     * 
     * @param klass The class to check.
     */
    private void checkClass(Class<?> klass)
    {
        if (ProjectData.class.equals(klass) || ImageData.class.equals(klass) ||
                TagAnnotationData.class.equals(klass) ||
                DatasetData.class.equals(klass) ||
                FileAnnotationData.class.equals(klass) ||
                ScreenData.class.equals(klass) || GroupData.class.equals(klass))
            return;
        throw new IllegalArgumentException("Root node not supported.");
    }

    /**
     * Formats the results.
     * 
     * @param expId The user's id.
     * @param result The result of the call for the passed user.
     */
    private void setExperimenterResult(SecurityContext ctx, Object result)
    {
        RefreshExperimenterDef node = getNode(ctx);
        Map<Object, Object> map;
        if (result instanceof Map)
            map = (Map) result;
        else {
            Collection set = (Collection) result;
            Iterator j = set.iterator();
            map = new HashMap<Object, Object>();
            Object parent;
            Set children = null;
            Object ob;
            TreeImageSet display;
            while (j.hasNext()) {
                parent = j.next();
                if (parent instanceof TimeRefObject) { //for tag support.
                    if (smartFolders != null) {
                        display = smartFolders.get(ctx);
                        if (display != null) {
                            display.removeAllChildren();
                            display.removeAllChildrenDisplay();
                            map.put(display,
                                    ((TimeRefObject) parent).getResults());
                        }
                    }
                } else {
                    if (parent instanceof ProjectData) {
                        children = ((ProjectData) parent).getDatasets();
                    } else if (parent instanceof DatasetData) {
                        children = new HashSet(1);
                        children.add(parent);
                    } else if (parent instanceof TagAnnotationData) {
                        children = new HashSet(1);
                        children.add(parent);
                    } else if (parent instanceof ScreenData) {
                        children = ((ScreenData) parent).getPlates();
                    } else if (parent instanceof GroupData) {
                        children = ((GroupData) parent).getExperimenters();
                    }
                    map.put(parent, children);
                }
            }
        }
        node.setResults(map);
    }

    /**
     * Returns the node corresponding to the security context.
     * 
     * @param ctx The context to handle.
     * @return
     */
    private RefreshExperimenterDef getNode(SecurityContext ctx)
    {
        Iterator<Entry<SecurityContext, RefreshExperimenterDef>>
        kk = expNodes.entrySet().iterator();
        Entry<SecurityContext, RefreshExperimenterDef> e;
        while (kk.hasNext()) {
            e = kk.next();
            if (e.getKey().equals(ctx)) {
                return e.getValue();
            }
        }
        return null;
    }
    /**
     * Formats the results.
     * 
     * @param ctx The security context.
     * @param result The result of the call for the passed user.
     */
    private void formatSmartFolderResult(SecurityContext ctx, List result)
    {
        RefreshExperimenterDef node = expNodes.get(ctx);
        List nodes = node.getExpandedNodes();
        int n = nodes.size();
        TreeImageSet display;
        TimeRefObject ref;
        Map m = new HashMap();

        for (int i = 0; i < n; i++) {
            display = (TreeImageSet) nodes.get(i);
            ref = (TimeRefObject) result.get(i);
            m.put(display, ref.getResults());
        }
        node.setResults(m);
    }

    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param rootNodeType The root node either <code>Project</code> or
     *                     <code>Screen</code>.
     * @param expNodes Collection of nodes hosting information about the nodes
     *                 to refresh. Mustn't be <code>null</code>.
     * @param refNode The node of reference.
     * @param toBrowse The node to browse.
     */
    public RefreshExperimenterDataLoader(Browser viewer, SecurityContext ctx,
            Class<?> rootNodeType,
            Map<SecurityContext, RefreshExperimenterDef> expNodes,
            Class<?> type, long id, Object refNode, DataObject toBrowse)
    {
        super(viewer, ctx);
        if (expNodes == null || expNodes.size() == 0)
            throw new IllegalArgumentException("Nodes not valid.");
        checkClass(rootNodeType);
        this.rootNodeType = rootNodeType;
        this.expNodes = expNodes;
        this.type = type;
        this.id = id;
        this.refNode = refNode;
        this.toBrowse = toBrowse;
    }

    /**
     * Retrieves the data.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
        Entry<SecurityContext, RefreshExperimenterDef> entry;
        Iterator<Entry<SecurityContext, RefreshExperimenterDef>> 
        i = expNodes.entrySet().iterator();
        RefreshExperimenterDef def;
        long userID;
        TimeRefObject ref = null;
        List nodes;
        List<TimeRefObject> times;
        Iterator j;
        TreeImageSet node;
        Map<SecurityContext, List> 
        m = new HashMap<SecurityContext, List>(expNodes.size());
        SecurityContext ctx;
        if (ImageData.class.equals(rootNodeType) || 
                FileAnnotationData.class.equals(rootNodeType)) {
            TreeImageTimeSet time;
            TreeFileSet file;
            while (i.hasNext()) {
                entry = i.next();
                ctx = entry.getKey();
                userID = ctx.getExperimenter();
                def = (RefreshExperimenterDef) entry.getValue();
                nodes = def.getExpandedNodes();
                j = nodes.iterator();
                times = new ArrayList<TimeRefObject>(nodes.size());
                while (j.hasNext()) {
                    node = (TreeImageSet) j.next();
                    if (node instanceof TreeImageTimeSet) {
                        time = (TreeImageTimeSet) node;
                        ref = new TimeRefObject(userID, TimeRefObject.TIME);
                        ref.setTimeInterval(time.getStartTime(), 
                                time.getEndTime());

                    } else if (node instanceof TreeFileSet) {
                        file = (TreeFileSet) node;
                        ref = new TimeRefObject(userID, TimeRefObject.FILE);
                        ref.setFileType(file.getType());
                    }
                    if (ref != null) times.add(ref);
                }
                m.put(ctx, times);
            }
        } else {
            List l;
            List<Object> nl;
            Iterator k;
            Object ob;
            while (i.hasNext()) {
                entry = i.next();
                ctx = entry.getKey();
                userID = ctx.getExperimenter();
                def = (RefreshExperimenterDef) entry.getValue();
                if (GroupData.class.equals(rootNodeType)) {
                    l = (List) def.getExpandedTopNodes().get(GroupData.class);
                    if (l == null) l = new ArrayList();
                    m.put(ctx, l);
                } else {
                    if (TagAnnotationData.class.equals(rootNodeType)) {
                        l  = def.getExpandedNodes();
                        nl = new ArrayList<Object>();
                        j = l.iterator();
                        while (j.hasNext()) {
                            ob = (Object) j.next();
                            if (ob instanceof TreeFileSet) {
                                ref = new TimeRefObject(userID,
                                        TimeRefObject.FILE);
                                ref.setFileType(
                                        MetadataHandlerView.TAG_NOT_OWNED);
                                nl.add(ref);
                                if (smartFolders == null) 
                                    smartFolders = new 
                                    HashMap<SecurityContext, TreeImageSet>();
                                smartFolders.put(ctx, (TreeImageSet) ob);
                            } else nl.add(ob);
                        }
                        m.put(ctx, nl);
                    } else {
                        l  = def.getExpandedNodes();
                        nl = new ArrayList<Object>();
                        j = l.iterator();
                        while (j.hasNext()) {
                            ob = (Object) j.next();
                            if (ob instanceof TreeFileSet) {
                                ref = new TimeRefObject(userID,
                                        TimeRefObject.FILE);
                                ref.setFileType(TimeRefObject.FILE_IMAGE_TYPE);
                                nl.add(ref);
                                if (smartFolders == null) 
                                    smartFolders = new 
                                    HashMap<SecurityContext, TreeImageSet>();
                                smartFolders.put(ctx, (TreeImageSet) ob);
                            } else nl.add(ob);
                        }
                        m.put(ctx, nl);
                    }
                }
            }
        }
        handle = dmView.refreshHierarchy(rootNodeType, m, this);
    }

    /**
     * Cancels the data loading.
     * @see DataBrowserLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Browser.DISCARDED) return;  //Async cancel.
        if (GroupData.class.equals(rootNodeType)) {
            Entry<SecurityContext, RefreshExperimenterDef> entry;
            RefreshExperimenterDef def;
            Iterator<Entry<SecurityContext, RefreshExperimenterDef>>
            i = expNodes.entrySet().iterator();
            Map nodes;
            List l;
            Iterator j;
            while (i.hasNext()) {
                entry = i.next();
                def = entry.getValue();
                nodes = def.getExpandedTopNodes();
                viewer.setGroups((Collection) result,
                        (List) nodes.get(GroupData.class));
            }
            return;
        }
        Map m = (Map) result;
        Entry entry;
        Iterator i = m.entrySet().iterator();
        SecurityContext ctx;
        long expId;
        if (ImageData.class.equals(rootNodeType) || 
                FileAnnotationData.class.equals(rootNodeType)) {
            while (i.hasNext()) {
                entry = (Entry) i.next();
                ctx = (SecurityContext) entry.getKey();
                formatSmartFolderResult(ctx, (List) entry.getValue());
            }
        } else {

            while (i.hasNext()) {
                entry = (Entry) i.next();
                ctx = (SecurityContext) entry.getKey();
                Object o = entry.getValue();
                if (smartFolders != null && o instanceof Map) {
                    //need to extract. 
                    Map map = (Map) o;
                    Iterator k = map.entrySet().iterator();
                    Entry e;
                    TreeImageSet display;
                    Map newMap = new HashMap();
                    Object key;
                    while (k.hasNext()) {
                        e = (Entry) k.next();
                        key = e.getKey();
                        if (key instanceof TimeRefObject) {
                            if (smartFolders != null) {
                                display = smartFolders.get(ctx);
                                if (display != null) {
                                    display.removeAllChildren();
                                    display.removeAllChildrenDisplay();
                                    newMap.put(display, 
                                            ((TimeRefObject) key).getResults());
                                }
                            }
                        } else newMap.put(key, e.getValue());
                    }
                    setExperimenterResult(ctx, newMap);
                } else {
                    setExperimenterResult(ctx, o);
                }
            }
        }
        viewer.setRefreshExperimenterData(expNodes, type, id);
        if (refNode instanceof TreeImageDisplay || refNode == null)
            viewer.browse((TreeImageDisplay) refNode, toBrowse, true);
    }

}
