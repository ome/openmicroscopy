/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015-2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;

import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.TagAnnotationData;

import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.WrapLayout;

/**
 * A {@link AnnotationTaskPaneUI} for displaying {@link TagAnnotationData}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class TagsTaskPaneUI extends AnnotationTaskPaneUI {

    /** Hold the {@link DocComponent}s representing the tag data */
    private List<DocComponent> tagsDocList;

    /** The add button */
    private JButton addTagsButton;
    
    /** The remove button */
    private JButton removeTagsButton;
    
    /** Tags which have to be removed */
    private List<TagAnnotationData> toRemove = new ArrayList<TagAnnotationData>();
    
    /** Tags which have to be stored */
    private List<TagAnnotationData> toAdd = new ArrayList<TagAnnotationData>();
    
    /**
     * Creates a new instance
     * 
     * @param model
     *            Reference to the {@link EditorModel}
     * @param view
     *            Reference to the {@link EditorUI}
     * @param controller
     *            Reference to the {@link EditorControl}
     */
    TagsTaskPaneUI(EditorModel model, EditorUI view,
            EditorControl controller) {
        super(model, view, controller);

        setLayout(new WrapLayout(WrapLayout.LEFT));
        setBackground(UIUtilities.BACKGROUND_COLOR);

        tagsDocList = new ArrayList<DocComponent>();
    }

    @Override
    void clearDisplay() {
        removeAll();
        tagsDocList.clear();
    }
    
    /**
     * Lays out the tags.
     * 
     * @param list The collection of tags to layout.
     */
    private void layoutTags(Collection list)
    {
        removeAll();
        tagsDocList.clear();
        DocComponent doc;
        
        filter(list);
        
        if (list != null && list.size() > 0) {
            Iterator i = list.iterator();
            DataObject data;
            switch (filter) {
                case SHOW_ALL:
                    while (i.hasNext()) {
                        doc = new DocComponent(i.next(), model);
                        doc.addPropertyChangeListener(controller);
                        tagsDocList.add(doc);
                        add(doc);
                    }
                    break;
                case ADDED_BY_ME:
                    while (i.hasNext()) {
                        data = (DataObject) i.next();
                        if (model.isLinkOwner(data)) {
                            doc = new DocComponent(data, model);
                            doc.addPropertyChangeListener(controller);
                            tagsDocList.add(doc);
                            add(doc);
                        }
                    }
                    break;
                case ADDED_BY_OTHERS:
                    while (i.hasNext()) {
                        data = (DataObject) i.next();
                        if (model.isAnnotatedByOther(data)) {
                            doc = new DocComponent(data, model);
                            doc.addPropertyChangeListener(controller);
                            tagsDocList.add(doc);
                            add(doc);
                        }
                    }
            }

        }
        if (tagsDocList.size() == 0) {
            doc = new DocComponent(null, model);
            tagsDocList.add(doc);
            add(doc);
        }
    }
    
    /**
     * Removes duplicate entries from the collection
     * 
     * @param collection
     */
    void filter(Collection collection) {
        Set<Long> ids = new HashSet<Long>();
        Iterator it = collection.iterator();
        while (it.hasNext()) {
            long id = ((DataObject) it.next()).getId();
            if (ids.contains(id)) {
                it.remove();
                continue;
            }
            ids.add(id);
        }
    }
    
    @Override
    void refreshUI() {
        clearDisplay();
        
        toAdd.clear();
        toRemove.clear();
        
        Collection l;
        if (!model.isMultiSelection()) 
            l = model.getTags();
        else
            l = model.getAllTags();
        
        layoutTags(l);
        
        addTagsButton.setEnabled(model.canAddAnnotationLink());
        removeTagsButton.setEnabled(model.canAddAnnotationLink());
    }
    
    /**
     * Returns the collection of tags.
     * 
     * @return See above.
     */
    List<TagAnnotationData> removeTags()
    {
        List<TagAnnotationData> list = new ArrayList<TagAnnotationData>();
        if (tagsDocList.size() == 0)  {
            return list;
        }
        List<TagAnnotationData> toKeep = new ArrayList<TagAnnotationData>();
        TagAnnotationData data;
        DocComponent doc;
        Object object;
        Iterator<DocComponent> i = tagsDocList.iterator();
        while (i.hasNext()) {
            doc = i.next();
            object = doc.getData();
            if (doc.canUnlink()) {
                if (object instanceof TagAnnotationData) {
                    data = (TagAnnotationData) object;
                    if (data.getId() > 0)
                        list.add(data);
                } 
            } else {
                toKeep.add((TagAnnotationData) object);
            }
        }
        handleObjectsSelection(TagAnnotationData.class, toKeep, false);
        return list;
    }
    
    /**
     * Handles the selection of objects via the selection wizard.
     * 
     * @param type    The type of objects to handle.
     * @param objects The objects to handle.
     * @param fire    Pass <code>true</code> to notify, <code>false</code>
     *                otherwise.
     */
    void handleObjectsSelection(Class<?> type, Collection objects, boolean fire)
    {
        toAdd.clear();
        toRemove.clear();
        
        Set<Long> selectedIds = new HashSet<Long>();
        for(Object obj : objects) {
            if(obj instanceof TagAnnotationData) {
                selectedIds.add(((TagAnnotationData)obj).getId());
            }
        }
        
        // determine which tags to remove
        Set<Long> originalIds = new HashSet<Long>();
        Collection<TagAnnotationData> originalTags = model.getAllTags();
        for(TagAnnotationData originalTag : originalTags) {
            if(!selectedIds.contains(originalTag.getId()))
                toRemove.add(originalTag);
            originalIds.add(originalTag.getId());
        }
        
        // determine which tags to add
        for(Object obj : objects) {
            if(obj instanceof TagAnnotationData) {
                TagAnnotationData tag = (TagAnnotationData)obj;
                if(!originalIds.contains(tag.getId()))
                    toAdd.add(tag);
            }
        }
        
        layoutTags(objects);
    }
    
    @Override
    List<JButton> getToolbarButtons() {
        List<JButton> buttons = new ArrayList<JButton>();

        IconManager icons = IconManager.getInstance();

        addTagsButton = new JButton(icons.getIcon(IconManager.PLUS_12));
        UIUtilities.unifiedButtonLookAndFeel(addTagsButton);
        addTagsButton.setBackground(UIUtilities.BACKGROUND_COLOR);
        addTagsButton.setToolTipText("Add Tags.");
        addTagsButton.addActionListener(controller);
        addTagsButton.setActionCommand(""+EditorControl.ADD_TAGS);
        buttons.add(addTagsButton);
        
        removeTagsButton = new JButton(icons.getIcon(IconManager.MINUS_12));
        UIUtilities.unifiedButtonLookAndFeel(removeTagsButton);
        removeTagsButton.setBackground(UIUtilities.BACKGROUND_COLOR);
        removeTagsButton.setToolTipText("Remove Tags.");
        removeTagsButton.addMouseListener(controller);
        removeTagsButton.setActionCommand(""+EditorControl.REMOVE_TAGS);
        buttons.add(removeTagsButton);
        
        return buttons;
    }

    @Override
    List<AnnotationData> getAnnotationsToSave() {
        return new ArrayList<AnnotationData>(toAdd);
    }

    @Override
    List<Object> getAnnotationsToRemove() {
        return new ArrayList<Object>(toRemove);
    }

    List<TagAnnotationData> getCurrentSelection() {
        List<TagAnnotationData> result = new ArrayList<TagAnnotationData>();
        for(DocComponent c : tagsDocList) 
            result.add((TagAnnotationData)c.getData());
        return result;
    }
    
    /**
     * Returns <code>true</code> some tags can be unlinked,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean hasTagsToUnlink()
    {
        if (tagsDocList.size() == 0) return false;
        DocComponent doc;
        Object object;
        Iterator<DocComponent> i = tagsDocList.iterator();
        while (i.hasNext()) {
            doc = i.next();
            object = doc.getData();
            if (doc.canUnlink()) {
                if (object instanceof TagAnnotationData) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    void onRelatedNodesSet() {
        addTagsButton.setEnabled(model.canAddAnnotationLink());
        removeTagsButton.setEnabled(model.canDeleteAnnotationLink());
    }
    
}
