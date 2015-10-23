/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JLabel;

import omero.gateway.model.AnnotationData;
import omero.gateway.model.BooleanAnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.DoubleAnnotationData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.LongAnnotationData;
import omero.gateway.model.TermAnnotationData;
import omero.gateway.model.TimeAnnotationData;
import omero.gateway.model.XMLAnnotationData;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * A {@link AnnotationTaskPaneUI} for displaying {@link FileAnnotationData}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class OtherTaskPaneUI extends AnnotationTaskPaneUI {

    /** Hold the {@link DocComponent}s representing the annotations */
    private List<DocComponent> otherList;

    private JButton removeButton;
    
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
    OtherTaskPaneUI(EditorModel model, EditorUI view,
            EditorControl controller) {
        super(model, view, controller);

        otherList = new ArrayList<DocComponent>();

        setLayout(new GridBagLayout());
        setBackground(UIUtilities.BACKGROUND_COLOR);
    }

    @Override
    void clearDisplay() {
        removeAll();
        otherList.clear();
    }
    
    @Override
    void refreshUI() {
        clearDisplay();
        
        Collection l;
        if (!model.isMultiSelection())
            l = model.getOtherAnnotations();
        else
            l = model.getAllOtherAnnotations();
        
        layoutOthers(l);
    }

    /**
     * Returns the collection of other annotations.
     * 
     * @return See above.
     */
    List<AnnotationData> removeOtherAnnotation()
    {
        List<AnnotationData> list = new ArrayList<AnnotationData>();
        if (otherList.size() == 0)  {
            return list;
        }
        List<AnnotationData> toKeep = new ArrayList<AnnotationData>();
        AnnotationData data;
        DocComponent doc;
        Object object;
        Iterator<DocComponent> i = otherList.iterator();
        while (i.hasNext()) {
            doc = i.next();
            object = doc.getData();
            if (doc.canUnlink()) {
                if (object instanceof AnnotationData) {
                    data = (AnnotationData) object;
                    if (data.getId() > 0)
                        list.add(data);
                } 
            } else {
                toKeep.add((AnnotationData) object);
            }
        }
        handleObjectsSelection(AnnotationData.class, toKeep, false);
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
        layoutOthers(objects);
    }
    
    /**
     * Lays out the other annotations.
     * 
     * @param list The collection of annotation to layout.
     */
    private void layoutOthers(Collection list)
    {
        clearDisplay();
        
        DocComponent doc;
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(1, 2, 1, 2);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.NONE;       
        
        if(!CollectionUtils.isEmpty(list)) {
            
            Iterator i = list.iterator();
            while (i.hasNext()) {
                
                c.gridx = 0;
                c.weightx = 0;
                c.fill = GridBagConstraints.NONE;
                
                DataObject item = (DataObject) i.next();
                if(filter==Filter.SHOW_ALL || (filter==Filter.ADDED_BY_ME && model.isLinkOwner(item)) || (filter==Filter.ADDED_BY_OTHERS && model.isAnnotatedByOther(item))) {
                    doc = new DocComponent(item, model);
                    doc.addPropertyChangeListener(controller);
                    
                    otherList.add(doc);
                    
                    add(new JLabel(getType((AnnotationData)item)+":"), c);
                    
                    c.gridx = 1;
                    c.weightx = 1;
                    c.fill = GridBagConstraints.HORIZONTAL;
                    add(doc, c);
                    
                    c.gridy++;
                }
                    
            }
        }
    }
    
    /**
     * Gets a readable name for the type of Annotation
     * 
     * @param d
     *            The Annotation
     * @return See above.
     */
    private String getType(AnnotationData d) {
        if (d instanceof XMLAnnotationData)
            return "XML";
        if (d instanceof BooleanAnnotationData)
            return "Boolean";
        if (d instanceof DoubleAnnotationData)
            return "Double";
        if (d instanceof LongAnnotationData)
            return "Long";
        if (d instanceof TermAnnotationData)
            return "Term";
        if (d instanceof TimeAnnotationData)
            return "Time";
        return "";
    }
    
    @Override
    List<AnnotationData> getAnnotationsToSave() {
        List<AnnotationData> l = new ArrayList<AnnotationData>();
        
        Collection<AnnotationData> original = model.getAllOtherAnnotations();
        Iterator<AnnotationData> j = original.iterator();
        List<Long> ids = new ArrayList<Long>();
        while (j.hasNext()) {
            ids.add(((AnnotationData) j.next()).getId());
        }
        Iterator<DocComponent> i = otherList.iterator();
        Map<Long, Integer> map = new HashMap<Long, Integer>();
        Map<Long, AnnotationData> 
            annotations = new HashMap<Long, AnnotationData>();
        Integer count;
        while (i.hasNext()) {
            DocComponent doc = i.next();
            Object object = doc.getData();
            if (object instanceof AnnotationData) {
                AnnotationData annotation = (AnnotationData) object;
                long id = annotation.getId();
                if (!ids.contains(id)) {
                    l.add(annotation);
                } else {
                    count = map.get(id);
                    if (count != null) {
                        count++;
                        map.put(id, count);
                    } else {
                        count = 1;
                        annotations.put(id, annotation);
                        map.put(id, count);
                    }
                }
            }
        }
        
        //check the count
        Entry<Long, Integer> entry;
        Iterator<Entry<Long, Integer>> k = map.entrySet().iterator();
        int n = otherList.size();
        Map<DataObject, Boolean> m;
        while (k.hasNext()) {
            entry = k.next();
            count = entry.getValue();
            if (count != null && count == n) {
                //Check if the annotation needs to be added
                AnnotationData annotation = annotations.get(entry.getKey());
                m = model.getObjectsWith(annotation);
                if (m.size() < count) {
                    l.add(annotation);
                }
            }
        }
        
        return l;
    }

    @Override
    List<Object> getAnnotationsToRemove() {
        List<Object> l = new ArrayList<Object>();
        
        Set<Long> idsToKeep = new HashSet<Long>();
        Iterator<DocComponent> i = otherList.iterator();
        while (i.hasNext()) {
            DocComponent doc = i.next();
            Object object = doc.getData();
            if (object instanceof AnnotationData) {
                AnnotationData annotation = (AnnotationData) object;
                long id = annotation.getId();
                if (id > 0) 
                    idsToKeep.add(id);
            }
        }
        
        Collection<AnnotationData> original = model.getAllOtherAnnotations();
        Iterator<AnnotationData> j = original.iterator();
        while (j.hasNext()) {
            AnnotationData annotation = (AnnotationData) j.next();
            long id = annotation.getId();
            if (!idsToKeep.contains(id))
                l.add(annotation);
        }
        
        return l;
    }

    List<AnnotationData> getCurrentSelection() {
        List<AnnotationData> result = new ArrayList<AnnotationData>();
        for(DocComponent c : otherList)
            result.add((AnnotationData)c.getData());
        return result;
    }
    
    /**
     * Returns <code>true</code> some tags can be unlinked,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean hasOtherAnnotationsToUnlink()
    {
        if (otherList.size() == 0) return false;
        DocComponent doc;
        Object object;
        Iterator<DocComponent> i = otherList.iterator();
        while (i.hasNext()) {
            doc = i.next();
            object = doc.getData();
            if (doc.canUnlink()) {
                if (object instanceof AnnotationData) {
                    return true;
                }
            }
        }
        return false;
    }
    
    

    @Override
    List<JButton> getToolbarButtons() {
        List<JButton> l = new ArrayList<JButton>();
        IconManager icons = IconManager.getInstance();
        removeButton = new JButton(
                icons.getIcon(IconManager.MINUS_12));
        UIUtilities.unifiedButtonLookAndFeel(removeButton);
        removeButton.setBackground(UIUtilities.BACKGROUND_COLOR);
        removeButton.setToolTipText("Remove Annotations.");
        removeButton.addMouseListener(controller);
        removeButton.setActionCommand(
                ""+EditorControl.REMOVE_OTHER_ANNOTATIONS);
        l.add(removeButton);
        return l;
    }

    @Override
    void onRelatedNodesSet() {
        removeButton.setEnabled(model.canAddAnnotationLink());
    }
    
    
}
